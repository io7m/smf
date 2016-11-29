/*
 * Copyright © 2016 <code@io7m.com> http://io7m.com
 *
 * Permission to use, copy, modify, and/or distribute this software for any
 * purpose with or without fee is hereby granted, provided that the above
 * copyright notice and this permission notice appear in all copies.
 *
 * THE SOFTWARE IS PROVIDED "AS IS" AND THE AUTHOR DISCLAIMS ALL WARRANTIES
 * WITH REGARD TO THIS SOFTWARE INCLUDING ALL IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS. IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY
 * SPECIAL, DIRECT, INDIRECT, OR CONSEQUENTIAL DAMAGES OR ANY DAMAGES
 * WHATSOEVER RESULTING FROM LOSS OF USE, DATA OR PROFITS, WHETHER IN AN
 * ACTION OF CONTRACT, NEGLIGENCE OR OTHER TORTIOUS ACTION, ARISING OUT OF OR
 * IN CONNECTION WITH THE USE OR PERFORMANCE OF THIS SOFTWARE.
 */

package com.io7m.smfj.cmdline;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.ParameterException;
import com.beust.jcommander.Parameters;
import com.io7m.jfunctional.Unit;
import com.io7m.jlexing.core.LexicalPosition;
import com.io7m.jnull.NullCheck;
import com.io7m.smfj.core.SMFAttribute;
import com.io7m.smfj.core.SMFFormatDescription;
import com.io7m.smfj.core.SMFFormatVersion;
import com.io7m.smfj.core.SMFHeader;
import com.io7m.smfj.frontend.SMFFCopier;
import com.io7m.smfj.frontend.SMFFCopierType;
import com.io7m.smfj.parser.api.SMFParseError;
import com.io7m.smfj.parser.api.SMFParserEventsType;
import com.io7m.smfj.parser.api.SMFParserProviderType;
import com.io7m.smfj.parser.api.SMFParserSequentialType;
import com.io7m.smfj.serializer.api.SMFSerializerProviderType;
import com.io7m.smfj.serializer.api.SMFSerializerType;
import javaslang.collection.SortedSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Optional;
import java.util.ServiceLoader;
import java.util.concurrent.Callable;

import static com.io7m.jfunctional.Unit.unit;

/**
 * The main command line program.
 */

public final class Main implements Runnable
{
  private static final Logger LOG;

  static {
    LOG = LoggerFactory.getLogger(Main.class);
  }

  private final Map<String, CommandType> commands;
  private final JCommander commander;
  private final String[] args;
  private int exit_code;

  private Main(final String[] in_args)
  {
    this.args = NullCheck.notNull(in_args);

    final CommandRoot r = new CommandRoot();
    final CommandValidate validate = new CommandValidate();
    final CommandFormats formats = new CommandFormats();
    final CommandTranscode transcode = new CommandTranscode();

    this.commands = new HashMap<>(8);
    this.commands.put("formats", formats);
    this.commands.put("validate", validate);
    this.commands.put("transcode", transcode);

    this.commander = new JCommander(r);
    this.commander.setProgramName("smf");
    this.commander.addCommand("validate", validate);
    this.commander.addCommand("formats", formats);
    this.commander.addCommand("transcode", transcode);
  }

  /**
   * The main entry point.
   *
   * @param args Command line arguments
   */

  public static void main(final String[] args)
  {
    final Main cm = new Main(args);
    cm.run();
    System.exit(cm.exitCode());
  }

  /**
   * @return The program exit code
   */

  public int exitCode()
  {
    return this.exit_code;
  }

  @Override
  public void run()
  {
    try {
      this.commander.parse(this.args);

      final String cmd = this.commander.getParsedCommand();
      if (cmd == null) {
        final StringBuilder sb = new StringBuilder(128);
        this.commander.usage(sb);
        LOG.info("Arguments required.\n{}", sb.toString());
        return;
      }

      final CommandType command = this.commands.get(cmd);
      command.call();

    } catch (final ParameterException e) {
      final StringBuilder sb = new StringBuilder(128);
      this.commander.usage(sb);
      LOG.error("{}\n{}", e.getMessage(), sb.toString());
      this.exit_code = 1;
    } catch (final Exception e) {
      LOG.error("{}", e.getMessage(), e);
      this.exit_code = 1;
    }
  }

  private interface CommandType extends Callable<Unit>
  {

  }

  private class CommandRoot implements CommandType
  {
    @Parameter(
      names = "-verbose",
      converter = SMFLogLevelConverter.class,
      description = "Set the minimum logging verbosity level")
    private SMFLogLevel verbose = SMFLogLevel.LOG_INFO;

    CommandRoot()
    {

    }

    @Override
    public Unit call()
      throws Exception
    {
      final ch.qos.logback.classic.Logger root =
        (ch.qos.logback.classic.Logger) LoggerFactory.getLogger(
          Logger.ROOT_LOGGER_NAME);
      root.setLevel(this.verbose.toLevel());
      return unit();
    }
  }

  @Parameters(commandDescription = "List supported formats")
  private final class CommandFormats extends CommandRoot
  {
    CommandFormats()
    {

    }

    @Override
    public Unit call()
      throws Exception
    {
      super.call();

      final String fmt_string = "%-6s : %-6s : %-32s : %-10s : %-6s : %s\n";

      System.out.printf(
        fmt_string,
        "# Name",
        "Suffix",
        "Mime type",
        "Version",
        "R/W",
        "Description");

      final ServiceLoader<SMFParserProviderType> parser_loader =
        ServiceLoader.load(SMFParserProviderType.class);
      final Iterator<SMFParserProviderType> parser_providers =
        parser_loader.iterator();

      while (parser_providers.hasNext()) {
        final SMFParserProviderType provider = parser_providers.next();
        final SMFFormatDescription format = provider.parserFormat();
        final SortedSet<SMFFormatVersion> versions = provider.parserSupportedVersions();
        versions.forEach(version -> {
          System.out.printf(
            fmt_string,
            format.name(),
            format.suffix(),
            format.mimeType(),
            String.format(
              "%d.%d",
              Integer.valueOf(version.major()),
              Integer.valueOf(version.minor())),
            "read",
            format.description());
        });
      }

      final ServiceLoader<SMFSerializerProviderType> serializer_loader =
        ServiceLoader.load(SMFSerializerProviderType.class);
      final Iterator<SMFSerializerProviderType> serializer_providers =
        serializer_loader.iterator();

      while (serializer_providers.hasNext()) {
        final SMFSerializerProviderType provider = serializer_providers.next();
        final SMFFormatDescription format = provider.serializerFormat();
        final SortedSet<SMFFormatVersion> versions = provider.serializerSupportedVersions();
        versions.forEach(version -> {
          System.out.printf(
            fmt_string,
            format.name(),
            format.suffix(),
            format.mimeType(),
            String.format(
              "%d.%d",
              Integer.valueOf(version.major()),
              Integer.valueOf(version.minor())),
            "write",
            format.description());
        });
      }

      return unit();
    }
  }

  @Parameters(commandDescription = "Transcode a mesh file")
  private final class CommandTranscode extends CommandRoot
  {
    @Parameter(
      names = "-file-in",
      required = true,
      description = "The input file")
    private String file_in;

    @Parameter(
      names = "-format-in",
      description = "The input file format")
    private String format_in;

    @Parameter(
      names = "-file-out",
      required = true,
      description = "The output file")
    private String file_out;

    @Parameter(
      names = "-format-out",
      description = "The output file format")
    private String format_out;

    CommandTranscode()
    {

    }

    @Override
    public Unit call()
      throws Exception
    {
      super.call();

      final SMFParserProviderType provider_parser =
        findParserProvider(this.format_in, this.file_in);
      final SMFSerializerProviderType provider_serializer =
        findSerializerProvider(this.format_out, this.file_out);

      if (provider_parser != null && provider_serializer != null) {
        final Path path_in = Paths.get(this.file_in);
        final Path path_out = Paths.get(this.file_out);

        try (final OutputStream os = Files.newOutputStream(path_out)) {
          try (final SMFSerializerType serializer =
            provider_serializer.serializerCreate(
              provider_serializer.serializerSupportedVersions().last(),
              path_out,
              os)) {
            try (final InputStream is = Files.newInputStream(path_in)) {
              final SMFFCopierType copier = SMFFCopier.create(serializer);
              try (final SMFParserSequentialType parser =
                     provider_parser.parserCreateSequential(copier, path_out, is)) {
                parser.parseHeader();
                if (!parser.parserHasFailed()) {
                  parser.parseData();
                }
              }
              if (!copier.errors().isEmpty()) {
                Main.this.exit_code = 1;
              }
            }
          }
        }
      }

      return unit();
    }
  }

  @Parameters(commandDescription = "Validate a mesh file")
  private final class CommandValidate extends CommandRoot implements
    SMFParserEventsType
  {
    @Parameter(
      names = "-file",
      required = true,
      description = "The input file")
    private String file;

    @Parameter(
      names = "-format",
      description = "The input file format")
    private String format;

    CommandValidate()
    {

    }

    @Override
    public Unit call()
      throws Exception
    {
      super.call();

      final SMFParserProviderType provider =
        findParserProvider(this.format, this.file);

      if (provider != null) {
        final Path path = Paths.get(this.file);
        try (final InputStream is = Files.newInputStream(path)) {
          final SMFParserSequentialType parser =
            provider.parserCreateSequential(this, path, is);
          parser.parseHeader();
          if (!parser.parserHasFailed()) {
            parser.parseData();
          }

          if (Main.this.exit_code != 0) {
            LOG.error("validation failed due to errors");
          }
        }

      } else {
        LOG.error("Could not find a suitable format provider");
        Main.this.exit_code = 1;
      }

      return unit();
    }

    @Override
    public void onStart()
    {

    }

    @Override
    public void onVersionReceived(
      final SMFFormatVersion version)
    {

    }

    @Override
    public void onFinish()
    {

    }

    @Override
    public void onError(
      final SMFParseError e)
    {
      final LexicalPosition<Path> lex = e.lexical();
      final Optional<Path> file_opt = lex.file();
      if (file_opt.isPresent()) {
        LOG.error(
          "parse error: {}:{}:{}: {}",
          file_opt.get(),
          Integer.valueOf(lex.line()),
          Integer.valueOf(lex.column()),
          e.message());
        Main.this.exit_code = 1;
      } else {
        LOG.error(
          "parse error: {}:{}: {}",
          Integer.valueOf(lex.line()),
          Integer.valueOf(lex.column()),
          e.message());
        Main.this.exit_code = 1;
      }
    }

    @Override
    public void onHeaderParsed(
      final SMFHeader header)
    {

    }

    @Override
    public void onDataAttributeStart(
      final SMFAttribute attribute)
    {

    }

    @Override
    public void onDataAttributeValueIntegerSigned1(
      final long x)
    {

    }

    @Override
    public void onDataAttributeValueIntegerSigned2(
      final long x,
      final long y)
    {

    }

    @Override
    public void onDataAttributeValueIntegerSigned3(
      final long x,
      final long y,
      final long z)
    {

    }

    @Override
    public void onDataAttributeValueIntegerSigned4(
      final long x,
      final long y,
      final long z,
      final long w)
    {

    }

    @Override
    public void onDataAttributeValueIntegerUnsigned1(
      final long x)
    {

    }

    @Override
    public void onDataAttributeValueIntegerUnsigned2(
      final long x,
      final long y)
    {

    }

    @Override
    public void onDataAttributeValueIntegerUnsigned3(
      final long x,
      final long y,
      final long z)
    {

    }

    @Override
    public void onDataAttributeValueIntegerUnsigned4(
      final long x,
      final long y,
      final long z,
      final long w)
    {

    }

    @Override
    public void onDataAttributeValueFloat1(
      final double x)
    {

    }

    @Override
    public void onDataAttributeValueFloat2(
      final double x,
      final double y)
    {

    }

    @Override
    public void onDataAttributeValueFloat3(
      final double x,
      final double y,
      final double z)
    {

    }

    @Override
    public void onDataAttributeValueFloat4(
      final double x,
      final double y,
      final double z,
      final double w)
    {

    }

    @Override
    public void onDataAttributeFinish(
      final SMFAttribute attribute)
    {

    }

    @Override
    public void onDataTrianglesStart()
    {

    }

    @Override
    public void onDataTriangle(
      final long v0,
      final long v1,
      final long v2)
    {

    }

    @Override
    public void onDataTrianglesFinish()
    {

    }

    @Override
    public boolean onMeta(
      final int vendor,
      final int schema,
      final long length)
    {
      return true;
    }

    @Override
    public void onMetaData(
      final int vendor,
      final int schema,
      final byte[] data)
    {

    }
  }

  private static SMFParserProviderType findParserProvider(
    final String format,
    final String file)
  {
    final ServiceLoader<SMFParserProviderType> loader =
      ServiceLoader.load(SMFParserProviderType.class);

    if (format == null) {
      LOG.debug("attempting to infer format from file suffix");
      final int index = file.lastIndexOf('.');
      if (index != -1) {
        final String suffix = file.substring(index + 1);
        final Iterator<SMFParserProviderType> providers =
          loader.iterator();
        while (providers.hasNext()) {
          final SMFParserProviderType current_provider =
            providers.next();
          if (current_provider.parserFormat().suffix().equals(suffix)) {
            LOG.debug("using provider: {}", current_provider);
            return current_provider;
          }
        }
      }

      LOG.error("File {} does not have a recognized suffix", file);
    } else {
      LOG.debug("attempting to find provider for {}", format);
      final Iterator<SMFParserProviderType> providers =
        loader.iterator();
      while (providers.hasNext()) {
        final SMFParserProviderType current_provider =
          providers.next();
        if (current_provider.parserFormat().name().equals(format)) {
          LOG.debug("using provider: {}", current_provider);
          return current_provider;
        }
      }

      LOG.error("Could not find a provider for the format '{}'", format);
    }

    return null;
  }

  private static SMFSerializerProviderType findSerializerProvider(
    final String format,
    final String file)
  {
    final ServiceLoader<SMFSerializerProviderType> loader =
      ServiceLoader.load(SMFSerializerProviderType.class);

    if (format == null) {
      LOG.debug("attempting to infer format from file suffix");
      final int index = file.lastIndexOf('.');
      if (index != -1) {
        final String suffix = file.substring(index + 1);
        final Iterator<SMFSerializerProviderType> providers =
          loader.iterator();
        while (providers.hasNext()) {
          final SMFSerializerProviderType current_provider =
            providers.next();
          if (current_provider.serializerFormat().suffix().equals(suffix)) {
            LOG.debug("using provider: {}", current_provider);
            return current_provider;
          }
        }
      }

      LOG.error("File {} does not have a recognized suffix", file);
    } else {
      LOG.debug("attempting to find provider for {}", format);
      final Iterator<SMFSerializerProviderType> providers =
        loader.iterator();
      while (providers.hasNext()) {
        final SMFSerializerProviderType current_provider =
          providers.next();
        if (current_provider.serializerFormat().name().equals(format)) {
          LOG.debug("using provider: {}", current_provider);
          return current_provider;
        }
      }

      LOG.error("Could not find a provider for the format '{}'", format);
    }

    return null;
  }
}
