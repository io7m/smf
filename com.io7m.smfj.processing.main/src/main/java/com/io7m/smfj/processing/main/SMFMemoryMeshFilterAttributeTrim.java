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

import com.io7m.smfj.core.SMFAttribute;
import com.io7m.smfj.core.SMFAttributeName;
import com.io7m.smfj.core.SMFHeader;
import com.io7m.smfj.parser.api.SMFParseError;
import com.io7m.smfj.processing.api.SMFAttributeArrayType;
import com.io7m.smfj.processing.api.SMFFilterCommandContext;
import com.io7m.smfj.processing.api.SMFMemoryMesh;
import com.io7m.smfj.processing.api.SMFMemoryMeshFilterType;
import com.io7m.smfj.processing.api.SMFProcessingError;
import io.vavr.collection.List;
import io.vavr.collection.Map;
import io.vavr.collection.Seq;
import io.vavr.collection.Set;
import io.vavr.collection.SortedMap;
import io.vavr.control.Validation;

import java.net.URI;
import java.util.Objects;
import java.util.Optional;

import static com.io7m.smfj.processing.api.SMFFilterCommandChecks.checkAttributeExists;
import static com.io7m.smfj.processing.api.SMFFilterCommandParsing.errorExpectedGotValidation;
import static io.vavr.control.Validation.invalid;
import static io.vavr.control.Validation.valid;

/**
 * A filter that renames a mesh attribute.
 */

public final class SMFMemoryMeshFilterAttributeTrim implements
  SMFMemoryMeshFilterType
{
  /**
   * The command name.
   */

  public static final String NAME = "trim";

  private static final String SYNTAX = "<name>+";

  private final Set<SMFAttributeName> attributes;

  private SMFMemoryMeshFilterAttributeTrim(
    final Set<SMFAttributeName> in_attributes)
  {
    this.attributes = Objects.requireNonNull(in_attributes, "Attributes");
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

    if (text.length() >= 1) {
      try {
        return valid(create(text.map(SMFAttributeName::of).toSet()));
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

  /**
   * Create a new filter.
   *
   * @param in_attributes The list of attributes to be preserved
   *
   * @return A new filter
   */

  public static SMFMemoryMeshFilterType create(
    final Set<SMFAttributeName> in_attributes)
  {
    return new SMFMemoryMeshFilterAttributeTrim(in_attributes);
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

    final SMFHeader header = m.header();

    final SortedMap<SMFAttributeName, SMFAttribute> by_name =
      m.header().attributesByName();
    Seq<SMFProcessingError> errors = List.empty();
    for (final SMFAttributeName name : this.attributes) {
      errors = checkAttributeExists(errors, by_name, name);
    }

    if (errors.isEmpty()) {
      final List<SMFAttribute> new_by_order =
        header.attributesInOrder().filter(a -> this.attributes.contains(a.name()));
      final Map<SMFAttributeName, SMFAttributeArrayType> new_arrays =
        m.arrays().filter(p -> this.attributes.contains(p._1));
      final SMFHeader new_header =
        header.withAttributesInOrder(new_by_order);
      return valid(
        SMFMemoryMesh.builder()
          .from(m)
          .setArrays(new_arrays)
          .setHeader(new_header)
          .build());
    }

    return invalid(List.ofAll(errors));
  }
}
