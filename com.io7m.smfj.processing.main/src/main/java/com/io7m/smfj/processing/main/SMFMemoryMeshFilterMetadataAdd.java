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
import com.io7m.smfj.parser.api.SMFParseError;
import com.io7m.smfj.processing.api.SMFFilterCommandContext;
import com.io7m.smfj.processing.api.SMFMemoryMesh;
import com.io7m.smfj.processing.api.SMFMemoryMeshFilterType;
import com.io7m.smfj.processing.api.SMFMetadata;
import com.io7m.smfj.processing.api.SMFProcessingError;
import javaslang.collection.List;
import javaslang.collection.Vector;
import javaslang.control.Validation;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

import static com.io7m.smfj.processing.api.SMFFilterCommandParsing.errorExpectedGotValidation;
import static javaslang.control.Validation.invalid;
import static javaslang.control.Validation.valid;

/**
 * A filter that adds metadata to a mesh.
 */

public final class SMFMemoryMeshFilterMetadataAdd implements
  SMFMemoryMeshFilterType
{
  /**
   * The command name.
   */

  public static final String NAME = "metadata-add";
  private static final Logger LOG;
  private static final String SYNTAX = "<vendor-id> <schema-id> <file>";

  static {
    LOG = LoggerFactory.getLogger(SMFMemoryMeshFilterMetadataAdd.class);
  }

  private final long vendor_id;
  private final long schema_id;
  private final Path meta_file;

  private SMFMemoryMeshFilterMetadataAdd(
    final long in_vendor_id,
    final long in_schema_id,
    final Path in_file)
  {
    this.vendor_id = in_vendor_id;
    this.schema_id = in_schema_id;
    this.meta_file = NullCheck.notNull(in_file, "File");
  }

  /**
   * Create a new filter.
   *
   * @param in_vendor_id The vendor ID
   * @param in_schema_id The schema ID
   * @param in_file      The schema file
   *
   * @return A new filter
   */

  public static SMFMemoryMeshFilterType create(
    final long in_vendor_id,
    final long in_schema_id,
    final Path in_file)
  {
    return new SMFMemoryMeshFilterMetadataAdd(
      in_vendor_id, in_schema_id, in_file);
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

    if (text.length() == 3) {
      try {
        final long vendor_id = Long.parseUnsignedLong(text.get(0), 16);
        final long schema_id = Long.parseUnsignedLong(text.get(1), 16);
        final Path path = Paths.get(text.get(2));
        return valid(create(vendor_id, schema_id, path));
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
    NullCheck.notNull(context, "Context");
    NullCheck.notNull(m, "Mesh");

    final Path file = context.resolvePath(this.meta_file);
    LOG.debug("resolved metadata file: {}", file);

    try (final InputStream stream = Files.newInputStream(file)) {
      final byte[] data = IOUtils.toByteArray(stream);
      final SMFMetadata meta =
        SMFMetadata.of(this.vendor_id, this.schema_id, data);

      final Vector<SMFMetadata> new_meta =
        m.metadata().append(meta);

      return valid(
        SMFMemoryMesh.builder()
          .from(m)
          .setMetadata(new_meta)
          .setHeader(m.header().withMetaCount((long) new_meta.size()))
          .build());
    } catch (final IOException e) {
      return invalid(List.of(
        SMFProcessingError.of(e.getMessage(), Optional.of(e))));
    }
  }
}
