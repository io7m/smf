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
import com.io7m.smfj.processing.api.SMFFilterCommandParsing;
import com.io7m.smfj.processing.api.SMFMemoryMesh;
import com.io7m.smfj.processing.api.SMFMemoryMeshFilterType;
import com.io7m.smfj.processing.api.SMFMetadata;
import com.io7m.smfj.processing.api.SMFProcessingError;
import javaslang.collection.List;
import javaslang.collection.Vector;
import javaslang.control.Validation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;
import java.util.Objects;
import java.util.Optional;
import java.util.OptionalLong;

/**
 * A filter that removes matching metadata from a mesh.
 */

public final class SMFMemoryMeshFilterMetadataRemove implements
  SMFMemoryMeshFilterType
{
  private static final Logger LOG;

  static {
    LOG = LoggerFactory.getLogger(SMFMemoryMeshFilterMetadataRemove.class);
  }

  /**
   * The command name.
   */

  public static final String NAME = "metadata-remove";

  private static final String SYNTAX = "(<vendor-id> | '-') (<schema-id> | '-')";

  private final OptionalLong vendor_id;
  private final OptionalLong schema_id;

  private SMFMemoryMeshFilterMetadataRemove(
    final OptionalLong in_vendor_id,
    final OptionalLong in_schema_id)
  {
    this.vendor_id = NullCheck.notNull(in_vendor_id, "Vendor ID");
    this.schema_id = NullCheck.notNull(in_schema_id, "Schema ID");
  }

  /**
   * Create a new filter.
   *
   * @param in_vendor_id The vendor ID
   * @param in_schema_id The schema ID
   *
   * @return A new filter
   */

  public static SMFMemoryMeshFilterType create(
    final OptionalLong in_vendor_id,
    final OptionalLong in_schema_id)
  {
    return new SMFMemoryMeshFilterMetadataRemove(in_vendor_id, in_schema_id);
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

    if (text.length() == 2) {
      try {
        final OptionalLong vendor_id;
        if (Objects.equals(text.get(0), "-")) {
          vendor_id = OptionalLong.empty();
        } else {
          vendor_id = OptionalLong.of(Long.parseUnsignedLong(text.get(0), 16));
        }

        final OptionalLong schema_id;
        if (Objects.equals(text.get(1), "-")) {
          schema_id = OptionalLong.empty();
        } else {
          schema_id = OptionalLong.of(Long.parseUnsignedLong(text.get(1), 16));
        }

        return Validation.valid(create(vendor_id, schema_id));
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
    final SMFFilterCommandContext context,
    final SMFMemoryMesh m)
  {
    NullCheck.notNull(context, "Context");
    NullCheck.notNull(m, "Mesh");

    final Vector<SMFMetadata> filtered = m.metadata().removeAll(meta -> {
      final boolean vendor_matches;
      if (this.vendor_id.isPresent()) {
        final long v = this.vendor_id.getAsLong();
        vendor_matches = meta.vendor() == v;
      } else {
        vendor_matches = true;
      }

      final boolean schema_matches;
      if (this.schema_id.isPresent()) {
        final long s = this.schema_id.getAsLong();
        schema_matches = meta.schema() == s;
      } else {
        schema_matches = true;
      }

      final boolean match = schema_matches && vendor_matches;
      if (LOG.isDebugEnabled()) {
        if (match) {
          LOG.debug(
            "removing matched {} -> {} {}",
            this.matchString(),
            Long.toUnsignedString(meta.vendor(), 16),
            Long.toUnsignedString(meta.schema(), 16));
        } else {
          LOG.debug(
            "preserving unmatched {} -> {} {}",
            this.matchString(),
            Long.toUnsignedString(meta.vendor(), 16),
            Long.toUnsignedString(meta.schema(), 16));
        }
      }

      return match;
    });

    return Validation.valid(
      SMFMemoryMesh.builder()
        .from(m)
        .setMetadata(filtered)
        .setHeader(m.header().withMetaCount((long) filtered.size()))
        .build());
  }

  private String matchString()
  {
    final StringBuilder sb = new StringBuilder(64);
    if (this.vendor_id.isPresent()) {
      sb.append(Long.toUnsignedString(this.vendor_id.getAsLong(), 16));
    } else {
      sb.append("-");
    }
    sb.append(" ");
    if (this.schema_id.isPresent()) {
      sb.append(Long.toUnsignedString(this.schema_id.getAsLong(), 16));
    } else {
      sb.append("-");
    }
    return sb.toString();
  }
}
