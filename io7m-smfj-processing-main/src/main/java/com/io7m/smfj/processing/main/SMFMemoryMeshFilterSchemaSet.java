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

import com.io7m.jnull.NullCheck;
import com.io7m.smfj.core.SMFSchemaIdentifier;
import com.io7m.smfj.parser.api.SMFParseError;
import com.io7m.smfj.processing.api.SMFMemoryMesh;
import com.io7m.smfj.processing.api.SMFMemoryMeshFilterType;
import com.io7m.smfj.processing.api.SMFProcessingError;
import javaslang.collection.List;
import javaslang.control.Validation;

import java.nio.file.Path;
import java.util.Optional;

/**
 * A filter that checks the existence and type of an attribute.
 */

public final class SMFMemoryMeshFilterSchemaSet implements
  SMFMemoryMeshFilterType
{
  /**
   * The command name.
   */

  public static final String NAME = "schema-set";

  private static final String SYNTAX =
    "<vendor-id> <schema-id> <schema-major> <schema-minor>";

  private final SMFSchemaIdentifier config;

  private SMFMemoryMeshFilterSchemaSet(
    final SMFSchemaIdentifier in_config)
  {
    this.config = NullCheck.notNull(in_config, "Config");
  }

  /**
   * Create a new filter.
   *
   * @param in_config The configuration
   *
   * @return A new filter
   */

  public static SMFMemoryMeshFilterType create(
    final SMFSchemaIdentifier in_config)
  {
    return new SMFMemoryMeshFilterSchemaSet(in_config);
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
    final Optional<Path> file,
    final int line,
    final List<String> text)
  {
    NullCheck.notNull(file, "file");
    NullCheck.notNull(text, "text");

    if (text.length() == 4) {
      try {
        final int vendor = Integer.parseUnsignedInt(text.get(0), 16);
        final int schema = Integer.parseUnsignedInt(text.get(1), 16);
        final int major = Integer.parseUnsignedInt(text.get(2));
        final int minor = Integer.parseUnsignedInt(text.get(3));
        return Validation.valid(create(
          SMFSchemaIdentifier.builder()
            .setVendorID(vendor)
            .setSchemaID(schema)
            .setSchemaMajorVersion(major)
            .setSchemaMinorVersion(minor).build()));
      } catch (final IllegalArgumentException e) {
        return SMFFilterCommandParsing.errorExpectedGotValidation(
          file, line, makeSyntax(), text);
      }
    }

    return SMFFilterCommandParsing.errorExpectedGotValidation(
      file, line, makeSyntax(), text);
  }

  private static String makeSyntax()
  {
    return NAME + " " + SYNTAX;
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
    final SMFMemoryMesh m)
  {
    NullCheck.notNull(m, "Mesh");
    return Validation.valid(
      m.withHeader(m.header().withSchemaIdentifier(this.config)));
  }
}
