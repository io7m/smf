/*
 * Copyright © 2019 Mark Raynsford <code@io7m.com> http://io7m.com
 *
 * Permission to use, copy, modify, and/or distribute this software for
 * any purpose with or without fee is hereby granted, provided that the
 * above copyright notice and this permission notice appear in all copies.
 *
 * THE SOFTWARE IS PROVIDED "AS IS" AND THE AUTHOR DISCLAIMS ALL
 * WARRANTIES WITH REGARD TO THIS SOFTWARE INCLUDING ALL IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS. IN NO EVENT SHALL THE AUTHOR
 * BE LIABLE FOR ANY SPECIAL, DIRECT, INDIRECT, OR CONSEQUENTIAL DAMAGES
 * OR ANY DAMAGES WHATSOEVER RESULTING FROM LOSS OF USE, DATA OR PROFITS,
 * WHETHER IN AN ACTION OF CONTRACT, NEGLIGENCE OR OTHER TORTIOUS ACTION,
 * ARISING OUT OF OR IN CONNECTION WITH THE USE OR PERFORMANCE OF THIS
 * SOFTWARE.
 */


package com.io7m.smfj.cmdline;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;
import com.io7m.smfj.core.SMFErrorType;
import com.io7m.smfj.core.SMFFormatDescription;
import com.io7m.smfj.core.SMFFormatVersion;
import com.io7m.smfj.core.SMFHeader;
import com.io7m.smfj.core.SMFTriangles;
import com.io7m.smfj.core.SMFWarningType;
import com.io7m.smfj.parser.api.SMFParseError;
import com.io7m.smfj.parser.api.SMFParserEventsBodyType;
import com.io7m.smfj.parser.api.SMFParserEventsHeaderType;
import com.io7m.smfj.parser.api.SMFParserEventsType;
import com.io7m.smfj.parser.api.SMFParserSequentialType;
import com.io7m.smfj.probe.api.SMFVersionProbeControllerServiceLoader;
import com.io7m.smfj.probe.api.SMFVersionProbeControllerType;
import com.io7m.smfj.probe.api.SMFVersionProbed;
import io.vavr.collection.Seq;
import io.vavr.control.Validation;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Parameters(commandDescription = "Probe a mesh file and display information")
public final class CommandProbe extends CommandRoot
  implements SMFParserEventsType, SMFParserEventsHeaderType
{
  private static final Logger LOG =
    LoggerFactory.getLogger(CommandProbe.class);

  @Parameter(
    names = "--input-file",
    required = true,
    description = "The input file")
  private String file_in;

  CommandProbe()
  {

  }

  @Override
  public Integer call()
    throws Exception
  {
    super.call();

    final Path path_in = Paths.get(this.file_in);

    final SMFVersionProbeControllerType controller =
      new SMFVersionProbeControllerServiceLoader();

    final Validation<Seq<SMFParseError>, SMFVersionProbed> r =
      controller.probe(() -> {
        try {
          return Files.newInputStream(path_in);
        } catch (final IOException e) {
          throw new UncheckedIOException(e);
        }
      });

    if (r.isInvalid()) {
      r.getError().forEach(e -> LOG.error(e.fullMessage()));
      return Integer.valueOf(1);
    }

    final SMFVersionProbed version = r.get();
    final SMFFormatDescription format = version.provider().parserFormat();
    System.out.printf(
      "Format: %s (%s) %s\n",
      format.name(),
      format.mimeType(),
      version.version().toHumanString());

    try (InputStream stream = Files.newInputStream(path_in)) {
      try (SMFParserSequentialType p = version.provider()
        .parserCreateSequential(this, path_in.toUri(), stream)) {
        p.parse();
      }
    }

    return Integer.valueOf(0);
  }

  @Override
  public void onStart()
  {

  }

  @Override
  public Optional<SMFParserEventsHeaderType> onVersionReceived(
    final SMFFormatVersion version)
  {
    return Optional.of(this);
  }

  @Override
  public void onFinish()
  {

  }

  @Override
  public void onError(
    final SMFErrorType e)
  {
    LOG.error(e.fullMessage());
  }

  @Override
  public void onWarning(
    final SMFWarningType w)
  {
    LOG.warn(w.fullMessage());
  }

  @Override
  public Optional<SMFParserEventsBodyType> onHeaderParsed(
    final SMFHeader header)
  {
    header.schemaIdentifier().ifPresent(
      schema -> System.out.printf("Schema: %s\n", schema.toHumanString()));

    System.out.printf(
      "Vertices: %s\n",
      Long.toUnsignedString(header.vertexCount()));

    final SMFTriangles triangles = header.triangles();
    System.out.printf(
      "Triangles: %s (size %s)\n",
      Long.toUnsignedString(triangles.triangleCount()),
      Integer.toUnsignedString(triangles.triangleIndexSizeBits()));

    System.out.printf("Attributes:\n");

    header.attributesInOrder().forEach(
      attr -> System.out.printf(
        "  %-32s %s %s %s\n",
        attr.name().value(),
        attr.componentType().getName(),
        Integer.toUnsignedString(attr.componentCount()),
        Integer.toUnsignedString(attr.componentSizeBits())));

    return Optional.empty();
  }
}
