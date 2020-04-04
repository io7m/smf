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
import com.io7m.smfj.core.SMFSchemaName;
import com.io7m.smfj.processing.api.SMFFilterCommandContext;
import com.io7m.smfj.processing.api.SMFMemoryMesh;
import com.io7m.smfj.processing.api.SMFMemoryMeshFilterType;
import com.io7m.smfj.processing.api.SMFMetadata;
import java.net.URI;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.io7m.smfj.processing.api.SMFFilterCommandParsing.errorExpectedGotValidation;

/**
 * A filter that removes matching metadata from a mesh.
 */

public final class SMFMemoryMeshFilterMetadataRemove
  implements SMFMemoryMeshFilterType
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

  public static final class Version
  {
    private int major;
    private int minor;

    public Version(
      final int inMajor,
      final int inMinor)
    {
      this.major = inMajor;
      this.minor = inMinor;
    }
  }

  private final Optional<SMFSchemaName> name;
  private final Optional<Version> version;

  private SMFMemoryMeshFilterMetadataRemove(
    final Optional<SMFSchemaName> in_schema_name,
    final Optional<Version> in_version)
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
    final Optional<Version> in_version)
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

  public static SMFPartialLogged<SMFMemoryMeshFilterType> parse(
    final Optional<URI> file,
    final int line,
    final List<String> text)
  {
    Objects.requireNonNull(file, "file");
    Objects.requireNonNull(text, "text");

    if (text.size() > 0 && text.size() <= 3) {
      try {
        final Optional<SMFSchemaName> schema_name;
        Optional<Version> schema_version = Optional.empty();

        if (Objects.equals(text.get(0), "-")) {
          schema_name = Optional.empty();
        } else {
          schema_name = Optional.of(SMFSchemaName.of(text.get(0)));

          if (text.size() == 2) {
            schema_version = Optional.empty();
          }

          if (text.size() == 3) {
            final int major = Integer.parseUnsignedInt(text.get(1));
            final int minor = Integer.parseUnsignedInt(text.get(2));
            schema_version = Optional.of(new Version(major, minor));
          }
        }

        return SMFPartialLogged.succeeded(create(schema_name, schema_version));
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

    final var filteredMeta =
      m.metadata()
        .stream()
        .filter(this::shouldPreserve)
        .collect(Collectors.toList());

    return SMFPartialLogged.succeeded(
      SMFMemoryMesh.builder()
        .from(m)
        .setMetadata(filteredMeta)
        .build());
  }

  private boolean shouldPreserve(final SMFMetadata meta)
  {
    final boolean version_matches;
    final boolean schema_matches;

    if (this.name.isPresent()) {
      final SMFSchemaName s = this.name.get();
      schema_matches = Objects.equals(meta.schema().name(), s);
      if (this.version.isPresent()) {
        final Version v = this.version.get();
        version_matches =
          v.major == meta.schema().versionMajor()
            && v.minor == meta.schema().versionMinor();
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

    return !match;
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
      sb.append(Integer.toUnsignedString(this.version.get().major));
      sb.append(" ");
      sb.append(Integer.toUnsignedString(this.version.get().minor));
    } else {
      sb.append("-");
    }
    return sb.toString();
  }
}
