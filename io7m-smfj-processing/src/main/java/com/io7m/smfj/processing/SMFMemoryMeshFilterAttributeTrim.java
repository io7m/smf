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

package com.io7m.smfj.processing;

import com.io7m.jnull.NullCheck;
import com.io7m.smfj.core.SMFAttribute;
import com.io7m.smfj.core.SMFAttributeName;
import com.io7m.smfj.core.SMFHeader;
import com.io7m.smfj.parser.api.SMFParseError;
import javaslang.collection.List;
import javaslang.collection.Map;
import javaslang.collection.Set;
import javaslang.control.Validation;

import java.nio.file.Path;
import java.util.Optional;

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

  private final Set<SMFAttributeName> attributes;

  private SMFMemoryMeshFilterAttributeTrim(
    final Set<SMFAttributeName> in_attributes)
  {
    this.attributes = NullCheck.notNull(in_attributes, "Attributes");
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

    final String name = NAME;
    final String syntax = " <attribute>+";

    if (text.length() > 1) {
      try {
        return Validation.valid(create(text.drop(1L).map(SMFAttributeName::of).toSet()));
      } catch (final IllegalArgumentException e) {
        return SMFFilterCommandParsing.errorExpectedGot(
          file, line, name + syntax, text);
      }
    }
    return SMFFilterCommandParsing.errorExpectedGot(
      file, line, name + syntax, text);
  }

  @Override
  public String name()
  {
    return NAME;
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
  public Validation<List<SMFProcessingError>, SMFMemoryMesh> filter(
    final SMFMemoryMesh m)
  {
    NullCheck.notNull(m, "Mesh");

    final SMFHeader header = m.header();

    List<SMFProcessingError> errors = List.empty();
    for (final SMFAttributeName name : this.attributes) {
      if (!header.attributesByName().containsKey(name)) {
        errors = errors.append(SMFProcessingError.of(
          "Nonexistent attribute: " + name.value(), Optional.empty()));
      }
    }

    if (errors.isEmpty()) {
      final Map<SMFAttributeName, SMFAttribute> new_by_name =
        header.attributesByName().filter(p -> this.attributes.contains(p._1));
      final List<SMFAttribute> new_by_order =
        header.attributesInOrder().filter(a -> this.attributes.contains(a.name()));
      final Map<SMFAttributeName, SMFAttributeArrayType> new_arrays =
        m.arrays().filter(p -> this.attributes.contains(p._1));

      final SMFHeader new_header =
        SMFHeader.builder()
          .from(header)
          .setAttributesByName(new_by_name)
          .setAttributesInOrder(new_by_order)
          .build();

      return Validation.valid(
        SMFMemoryMesh.builder()
          .from(m)
          .setArrays(new_arrays)
          .setHeader(new_header)
          .build());
    }

    return Validation.invalid(errors);
  }
}
