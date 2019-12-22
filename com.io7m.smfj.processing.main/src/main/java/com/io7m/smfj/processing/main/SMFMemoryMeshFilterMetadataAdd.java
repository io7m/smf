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

import com.io7m.smfj.core.SMFPartialLogged;
import com.io7m.smfj.core.SMFSchemaIdentifier;
import com.io7m.smfj.core.SMFSchemaName;
import com.io7m.smfj.processing.api.SMFFilterCommandContext;
import com.io7m.smfj.processing.api.SMFMemoryMesh;
import com.io7m.smfj.processing.api.SMFMemoryMeshFilterType;
import com.io7m.smfj.processing.api.SMFMetadata;
import com.io7m.smfj.processing.api.SMFProcessingError;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.io7m.smfj.processing.api.SMFFilterCommandParsing.errorExpectedGotValidation;

/**
 * A filter that adds metadata to a mesh.
 */

public final class SMFMemoryMeshFilterMetadataAdd
  implements SMFMemoryMeshFilterType
{
  /**
   * The command name.
   */

  public static final String NAME = "metadata-add";
  private static final Logger LOG;
  private static final String SYNTAX =
    "<schema-id> <version-major> <version-minor> <file>";

  static {
    LOG = LoggerFactory.getLogger(SMFMemoryMeshFilterMetadataAdd.class);
  }

  private final SMFSchemaIdentifier schema_id;
  private final Path meta_file;

  private SMFMemoryMeshFilterMetadataAdd(
    final SMFSchemaIdentifier in_schema,
    final Path in_file)
  {
    this.schema_id = Objects.requireNonNull(in_schema, "Schema");
    this.meta_file = Objects.requireNonNull(in_file, "File");
  }

  /**
   * Create a new filter.
   *
   * @param in_schema The schema ID
   * @param in_file   The schema file
   *
   * @return A new filter
   */

  public static SMFMemoryMeshFilterType create(
    final SMFSchemaIdentifier in_schema,
    final Path in_file)
  {
    return new SMFMemoryMeshFilterMetadataAdd(in_schema, in_file);
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

  public static SMFPartialLogged<SMFMemoryMeshFilterType> parse(
    final Optional<URI> file,
    final int line,
    final List<String> text)
  {
    Objects.requireNonNull(file, "file");
    Objects.requireNonNull(text, "text");

    if (text.size() == 4) {
      try {
        final SMFSchemaName name = SMFSchemaName.of(text.get(0));
        final int major = Integer.parseUnsignedInt(text.get(1));
        final int minor = Integer.parseUnsignedInt(text.get(2));
        final Path path = Paths.get(text.get(3));
        return SMFPartialLogged.succeeded(create(
          SMFSchemaIdentifier.of(
            name,
            major,
            minor),
          path));
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
  public SMFPartialLogged<SMFMemoryMesh> filter(
    final SMFFilterCommandContext context,
    final SMFMemoryMesh m)
  {
    Objects.requireNonNull(context, "Context");
    Objects.requireNonNull(m, "Mesh");

    final Path file = context.resolvePath(this.meta_file);
    LOG.debug("resolved metadata file: {}", file);

    try (InputStream stream = Files.newInputStream(file)) {
      final byte[] data = stream.readAllBytes();
      final SMFMetadata meta = SMFMetadata.of(this.schema_id, data);

      final var newMeta = new ArrayList<>(m.metadata());
      newMeta.add(meta);

      return SMFPartialLogged.succeeded(
        SMFMemoryMesh.builder()
          .from(m)
          .setMetadata(newMeta)
          .build());
    } catch (final IOException e) {
      return SMFPartialLogged.failed(
        SMFProcessingError.of(e.getMessage(), Optional.of(e)));
    }
  }
}
