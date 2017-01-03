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
import com.io7m.smfj.core.SMFFormatDescription;
import com.io7m.smfj.core.SMFFormatVersion;
import com.io7m.smfj.frontend.SMFFilterCommandFile;
import com.io7m.smfj.frontend.SMFParserProviders;
import com.io7m.smfj.frontend.SMFSerializerProviders;
import com.io7m.smfj.parser.api.SMFParseError;
import com.io7m.smfj.parser.api.SMFParserProviderType;
import com.io7m.smfj.parser.api.SMFParserSequentialType;
import com.io7m.smfj.processing.api.SMFFilterCommandModuleResolver;
import com.io7m.smfj.processing.api.SMFFilterCommandModuleResolverType;
import com.io7m.smfj.processing.api.SMFMemoryMesh;
import com.io7m.smfj.processing.api.SMFMemoryMeshFilterType;
import com.io7m.smfj.processing.api.SMFMemoryMeshProducer;
import com.io7m.smfj.processing.api.SMFMemoryMeshProducerType;
import com.io7m.smfj.processing.api.SMFMemoryMeshSerializer;
import com.io7m.smfj.processing.api.SMFProcessingError;
import com.io7m.smfj.serializer.api.SMFSerializerProviderType;
import com.io7m.smfj.serializer.api.SMFSerializerType;
import javaslang.collection.List;
import javaslang.collection.Seq;
import javaslang.collection.SortedSet;
import javaslang.control.Validation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
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
    final CommandFormats formats = new CommandFormats();
    final CommandFilter filter = new CommandFilter();

    this.commands = new HashMap<>(8);
    this.commands.put("filter", filter);
    this.commands.put("formats", formats);

    this.commander = new JCommander(r);
    this.commander.setProgramName("smf");
    this.commander.addCommand("filter", filter);
    this.commander.addCommand("formats", formats);
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
        versions.forEach(
          version ->
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
              format.description()));
      }

      final ServiceLoader<SMFSerializerProviderType> serializer_loader =
        ServiceLoader.load(SMFSerializerProviderType.class);
      final Iterator<SMFSerializerProviderType> serializer_providers =
        serializer_loader.iterator();

      while (serializer_providers.hasNext()) {
        final SMFSerializerProviderType provider = serializer_providers.next();
        final SMFFormatDescription format = provider.serializerFormat();
        final SortedSet<SMFFormatVersion> versions = provider.serializerSupportedVersions();
        versions.forEach(
          version ->
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
              format.description()));
      }

      return unit();
    }
  }

  @Parameters(commandDescription = "Filter mesh data")
  private final class CommandFilter extends CommandRoot
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
      description = "The output file")
    private String file_out;

    @Parameter(
      names = "-format-out",
      description = "The output file format")
    private String format_out;

    @Parameter(
      names = "-commands",
      required = true,
      description = "The filter commands")
    private String file_commands;

    CommandFilter()
    {

    }

    @Override
    public Unit call()
      throws Exception
    {
      super.call();

      final Optional<List<SMFMemoryMeshFilterType>> filters_opt =
        this.parseFilterCommands();

      if (!filters_opt.isPresent()) {
        Main.this.exit_code = 1;
        return unit();
      }

      final List<SMFMemoryMeshFilterType> filters = filters_opt.get();

      final Optional<SMFParserProviderType> provider_parser_opt =
        SMFParserProviders.findParserProvider(
          Optional.ofNullable(this.format_in),
          this.file_in);

      if (!provider_parser_opt.isPresent()) {
        Main.this.exit_code = 1;
        return unit();
      }

      final SMFParserProviderType provider_parser = provider_parser_opt.get();
      final Path path_in = Paths.get(this.file_in);

      final Optional<SMFMemoryMesh> mesh_opt =
        this.loadMemoryMesh(provider_parser, path_in);

      if (!mesh_opt.isPresent()) {
        Main.this.exit_code = 1;
        return unit();
      }

      final Optional<SMFMemoryMesh> filtered_opt =
        this.runFilters(filters, mesh_opt.get());

      if (!filtered_opt.isPresent()) {
        Main.this.exit_code = 1;
        return unit();
      }

      final SMFMemoryMesh filtered = filtered_opt.get();

      if (this.file_out != null) {
        final Optional<SMFSerializerProviderType> provider_serializer_opt =
          SMFSerializerProviders.findSerializerProvider(
            Optional.ofNullable(this.format_out), this.file_out);

        if (!provider_serializer_opt.isPresent()) {
          Main.this.exit_code = 1;
          return unit();
        }

        final SMFSerializerProviderType provider_serializer =
          provider_serializer_opt.get();
        final Path path_out = Paths.get(this.file_out);
        try (final OutputStream os = Files.newOutputStream(path_out)) {
          try (final SMFSerializerType serializer =
                 provider_serializer.serializerCreate(
                   provider_serializer.serializerSupportedVersions().last(),
                   path_out,
                   os)) {
            SMFMemoryMeshSerializer.serialize(filtered, serializer);
          }
        } catch (final IOException e) {
          Main.this.exit_code = 1;
          LOG.error("could not serialize mesh: {}", e.getMessage());
          LOG.debug("i/o error: ", e);
        }
      }

      return unit();
    }

    private Optional<SMFMemoryMesh> runFilters(
      final Seq<SMFMemoryMeshFilterType> filters,
      final SMFMemoryMesh mesh)
    {
      SMFMemoryMesh mesh_current = mesh;
      for (int index = 0; index < filters.size(); ++index) {
        final SMFMemoryMeshFilterType filter = filters.get(index);
        LOG.debug("evaluating filter: {}", filter.name());

        final Validation<List<SMFProcessingError>, SMFMemoryMesh> result =
          filter.filter(mesh_current);
        if (result.isValid()) {
          mesh_current = result.get();
        } else {
          result.getError().map(e -> {
            LOG.error("filter: {}: {}", filter.name(), e.message());
            return unit();
          });
          return Optional.empty();
        }
      }

      return Optional.of(mesh_current);
    }

    private Optional<SMFMemoryMesh> loadMemoryMesh(
      final SMFParserProviderType provider_parser,
      final Path path_in)
      throws IOException
    {
      final SMFMemoryMeshProducerType loader =
        SMFMemoryMeshProducer.create();

      try (final InputStream is = Files.newInputStream(path_in)) {
        try (final SMFParserSequentialType parser =
               provider_parser.parserCreateSequential(loader, path_in, is)) {
          parser.parseHeader();
          if (!parser.parserHasFailed()) {
            parser.parseData();
          }
        }
        if (!loader.errors().isEmpty()) {
          loader.errors().map(e -> {
            final LexicalPosition<Path> lex = e.lexical();
            LOG.error(
              "{}:{}:{}: {}",
              this.file_in,
              Integer.valueOf(lex.line()),
              Integer.valueOf(lex.column()),
              e.message());
            return unit();
          });
          Main.this.exit_code = 1;
          return Optional.empty();
        }
      }
      return Optional.of(loader.mesh());
    }

    private Optional<List<SMFMemoryMeshFilterType>> parseFilterCommands()
      throws IOException
    {
      final Path path_commands = Paths.get(this.file_commands);
      final SMFFilterCommandModuleResolverType resolver =
        SMFFilterCommandModuleResolver.create();

      try (final InputStream stream = Files.newInputStream(path_commands)) {
        final Validation<List<SMFParseError>, List<SMFMemoryMeshFilterType>> r =
          SMFFilterCommandFile.parseFromStream(
            resolver, Optional.of(path_commands), stream);
        if (r.isValid()) {
          return Optional.of(r.get());
        }

        r.getError().map(e -> {
          final LexicalPosition<Path> lex = e.lexical();
          LOG.error(
            "{}:{}:{}: {}",
            path_commands,
            Integer.valueOf(lex.line()),
            Integer.valueOf(lex.column()),
            e.message());
          return unit();
        });

        return Optional.empty();
      }
    }
  }
}