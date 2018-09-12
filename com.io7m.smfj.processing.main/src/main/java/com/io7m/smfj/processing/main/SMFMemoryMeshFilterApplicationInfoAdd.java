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

package com.io7m.smfj.processing.main;

import com.io7m.junreachable.UnreachableCodeException;
import com.io7m.smfj.core.SMFSchemaIdentifier;
import com.io7m.smfj.core.SMFSchemaName;
import com.io7m.smfj.parser.api.SMFParseError;
import com.io7m.smfj.processing.api.SMFFilterCommandContext;
import com.io7m.smfj.processing.api.SMFMemoryMesh;
import com.io7m.smfj.processing.api.SMFMemoryMeshFilterType;
import com.io7m.smfj.processing.api.SMFMetadata;
import com.io7m.smfj.processing.api.SMFProcessingError;
import javaslang.collection.List;
import javaslang.control.Validation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URI;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;
import java.util.Optional;
import java.util.Properties;

import static com.io7m.smfj.processing.api.SMFFilterCommandParsing.errorExpectedGotValidation;
import static javaslang.control.Validation.valid;

/**
 * A filter that adds application info as metadata to a mesh.
 */

public final class SMFMemoryMeshFilterApplicationInfoAdd implements
  SMFMemoryMeshFilterType
{
  /**
   * The command name.
   */

  public static final String NAME = "application-info-add";
  private static final Logger LOG;
  private static final String SYNTAX = "";

  static {
    LOG = LoggerFactory.getLogger(SMFMemoryMeshFilterApplicationInfoAdd.class);
  }

  private SMFMemoryMeshFilterApplicationInfoAdd()
  {

  }

  /**
   * Create a new filter.
   *
   * @return A new filter
   */

  public static SMFMemoryMeshFilterType create()
  {
    return new SMFMemoryMeshFilterApplicationInfoAdd();
  }

  /**
   * Attempt to parse a command.
   *
   * @param file The file, if any
   * @param line The line
   * @param text The text
   *
   * @return A parsed command or a list of parse errors
   */

  public static Validation<List<SMFParseError>, SMFMemoryMeshFilterType> parse(
    final Optional<URI> file,
    final int line,
    final List<String> text)
  {
    Objects.requireNonNull(file, "file");
    Objects.requireNonNull(text, "text");

    if (text.isEmpty()) {
      try {
        return valid(create());
      } catch (final IllegalArgumentException e) {
        return errorExpectedGotValidation(file, line, makeSyntax(), text);
      }
    }
    return errorExpectedGotValidation(file, line, makeSyntax(), text);
  }

  private static String makeSyntax()
  {
    return NAME + " " + SYNTAX;
  }

  private static String version()
  {
    final Package pack =
      SMFMemoryMeshFilterApplicationInfoAdd.class.getPackage();
    final String version =
      pack.getImplementationVersion();
    if (version != null) {
      return version;
    }
    return "0.0.0";
  }

  @Override
  public String name()
  {
    return NAME;
  }

  @Override
  public String syntax()
  {
    return makeSyntax();
  }

  @Override
  public Validation<List<SMFProcessingError>, SMFMemoryMesh> filter(
    final SMFFilterCommandContext context,
    final SMFMemoryMesh m)
  {
    Objects.requireNonNull(context, "Context");
    Objects.requireNonNull(m, "Mesh");

    final ZonedDateTime time =
      ZonedDateTime.now(ZoneId.of("UTC"));
    final String time_text =
      time.format(DateTimeFormatter.ofPattern("YYYY-mm-dd'T'HH:MM:SSZ"));

    try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
      final Properties props = new Properties();
      props.setProperty("time", time_text);
      props.setProperty("app.role", "filter");
      props.setProperty("app.version", "com.io7m.smfj " + version());
      props.setProperty("smf.version", version());
      props.store(out, null);

      final SMFMetadata appinfo =
        SMFMetadata.of(
          SMFSchemaIdentifier.of(
            SMFSchemaName.of("com.io7m.smf.application"),
            1,
            0),
          out.toByteArray());

      return valid(
        SMFMemoryMesh.builder()
          .from(m)
          .setMetadata(m.metadata().append(appinfo))
          .build());
    } catch (final IOException e) {
      throw new UnreachableCodeException();
    }
  }
}
