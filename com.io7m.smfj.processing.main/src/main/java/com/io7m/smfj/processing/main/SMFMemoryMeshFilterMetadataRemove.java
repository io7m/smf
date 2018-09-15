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

import com.io7m.smfj.core.SMFSchemaName;
import com.io7m.smfj.parser.api.SMFParseError;
import com.io7m.smfj.processing.api.SMFFilterCommandContext;
import com.io7m.smfj.processing.api.SMFMemoryMesh;
import com.io7m.smfj.processing.api.SMFMemoryMeshFilterType;
import com.io7m.smfj.processing.api.SMFMetadata;
import com.io7m.smfj.processing.api.SMFProcessingError;
import io.vavr.Tuple;
import io.vavr.Tuple2;
import io.vavr.collection.List;
import io.vavr.collection.Seq;
import io.vavr.collection.Vector;
import io.vavr.control.Validation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.util.Objects;
import java.util.Optional;

import static com.io7m.smfj.processing.api.SMFFilterCommandParsing.errorExpectedGotValidation;
import static io.vavr.control.Validation.valid;

/**
 * A filter that removes matching metadata from a mesh.
 */

public final class SMFMemoryMeshFilterMetadataRemove implements
  SMFMemoryMeshFilterType
{
  /**
   * The command name.
   */

  public static final String NAME = "metadata-remove";

  private static final Logger LOG;
  private static final String SYNTAX =
    "(<schema-id> ((<version-major> <version-minor>) | '-')) | '-'";

  static {
    LOG = LoggerFactory.getLogger(SMFMemoryMeshFilterMetadataRemove.class);
  }

  private final Optional<SMFSchemaName> name;
  private final Optional<Tuple2<Integer, Integer>> version;

  private SMFMemoryMeshFilterMetadataRemove(
    final Optional<SMFSchemaName> in_schema_name,
    final Optional<Tuple2<Integer, Integer>> in_version)
  {
    this.name = Objects.requireNonNull(in_schema_name, "Schema name");
    this.version = Objects.requireNonNull(in_version, "Version");
  }

  /**
   * Create a new filter.
   *
   * @param in_schema_name The schema name
   * @param in_version     The schema version
   *
   * @return A new filter
   */

  public static SMFMemoryMeshFilterType create(
    final Optional<SMFSchemaName> in_schema_name,
    final Optional<Tuple2<Integer, Integer>> in_version)
  {
    return new SMFMemoryMeshFilterMetadataRemove(in_schema_name, in_version);
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

  public static Validation<Seq<SMFParseError>, SMFMemoryMeshFilterType> parse(
    final Optional<URI> file,
    final int line,
    final List<String> text)
  {
    Objects.requireNonNull(file, "file");
    Objects.requireNonNull(text, "text");

    if (text.length() > 0 && text.length() <= 3) {
      try {
        final Optional<SMFSchemaName> schema_name;
        Optional<Tuple2<Integer, Integer>> schema_version = Optional.empty();

        if (Objects.equals(text.get(0), "-")) {
          schema_name = Optional.empty();
        } else {
          schema_name = Optional.of(SMFSchemaName.of(text.get(0)));

          if (text.length() == 2) {
            schema_version = Optional.empty();
          }

          if (text.length() == 3) {
            final int major = Integer.parseUnsignedInt(text.get(1));
            final int minor = Integer.parseUnsignedInt(text.get(2));
            schema_version =
              Optional.of(Tuple.of(
                Integer.valueOf(major),
                Integer.valueOf(minor)));
          }
        }

        return valid(create(schema_name, schema_version));
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
  public Validation<Seq<SMFProcessingError>, SMFMemoryMesh> filter(
    final SMFFilterCommandContext context,
    final SMFMemoryMesh m)
  {
    Objects.requireNonNull(context, "Context");
    Objects.requireNonNull(m, "Mesh");

    final Vector<SMFMetadata> filtered = m.metadata().removeAll(meta -> {
      final boolean version_matches;
      final boolean schema_matches;

      if (this.name.isPresent()) {
        final SMFSchemaName s = this.name.get();
        schema_matches = Objects.equals(meta.schema().name(), s);
        if (this.version.isPresent()) {
          final Tuple2<Integer, Integer> v = this.version.get();
          version_matches =
            v._1().intValue() == meta.schema().versionMajor()
              && v._2().intValue() == meta.schema().versionMinor();
        } else {
          version_matches = true;
        }
      } else {
        schema_matches = true;
        version_matches = true;
      }

      final boolean match = schema_matches && version_matches;
      if (LOG.isDebugEnabled()) {
        if (match) {
          LOG.debug(
            "removing matched {} -> {}",
            this.matchString(),
            meta.schema().toHumanString());
        } else {
          LOG.debug(
            "preserving unmatched {} -> {}",
            this.matchString(),
            meta.schema().toHumanString());
        }
      }

      return match;
    });

    return valid(
      SMFMemoryMesh.builder()
        .from(m)
        .setMetadata(filtered)
        .build());
  }

  private String matchString()
  {
    final StringBuilder sb = new StringBuilder(64);
    if (this.name.isPresent()) {
      sb.append(this.name.get().value());
    } else {
      sb.append("-");
    }
    sb.append(" ");
    if (this.version.isPresent()) {
      sb.append(Integer.toUnsignedString(
        this.version.get()._1().intValue()));
      sb.append(" ");
      sb.append(Integer.toUnsignedString(
        this.version.get()._2().intValue()));
    } else {
      sb.append("-");
    }
    return sb.toString();
  }
}
