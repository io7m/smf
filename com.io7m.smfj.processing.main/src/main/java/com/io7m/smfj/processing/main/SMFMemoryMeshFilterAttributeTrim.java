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
import com.io7m.smfj.core.SMFPartialLogged;
import com.io7m.smfj.processing.api.SMFAttributeArrayType;
import com.io7m.smfj.processing.api.SMFFilterCommandContext;
import com.io7m.smfj.processing.api.SMFMemoryMesh;
import com.io7m.smfj.processing.api.SMFMemoryMeshFilterType;
import com.io7m.smfj.processing.api.SMFProcessingError;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.SortedMap;
import java.util.stream.Collectors;

import static com.io7m.smfj.processing.api.SMFFilterCommandChecks.checkAttributeExists;
import static com.io7m.smfj.processing.api.SMFFilterCommandParsing.errorExpectedGotValidation;

/**
 * A filter that removes all mesh attributes that are not present in a list
 * of names.
 */

public final class SMFMemoryMeshFilterAttributeTrim
  implements SMFMemoryMeshFilterType
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

  public static SMFPartialLogged<SMFMemoryMeshFilterType> parse(
    final Optional<URI> file,
    final int line,
    final List<String> text)
  {
    Objects.requireNonNull(file, "file");
    Objects.requireNonNull(text, "text");

    if (text.size() >= 1) {
      try {
        final Set<SMFAttributeName> names =
          text.stream()
            .map(SMFAttributeName::of)
            .collect(Collectors.toSet());
        return SMFPartialLogged.succeeded(create(names));
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
  public SMFPartialLogged<SMFMemoryMesh> filter(
    final SMFFilterCommandContext context,
    final SMFMemoryMesh m)
  {
    Objects.requireNonNull(context, "Context");
    Objects.requireNonNull(m, "Mesh");

    final SMFHeader header = m.header();

    final SortedMap<SMFAttributeName, SMFAttribute> by_name =
      m.header().attributesByName();

    List<SMFProcessingError> errors = new ArrayList<>();
    for (final SMFAttributeName name : this.attributes) {
      errors = checkAttributeExists(errors, by_name, name);
    }

    if (errors.isEmpty()) {
      final List<SMFAttribute> new_by_order =
        header.attributesInOrder()
          .stream()
          .filter(a -> this.attributes.contains(a.name()))
          .collect(Collectors.toList());

      final Map<SMFAttributeName, SMFAttributeArrayType> new_arrays =
        m.arrays()
          .entrySet()
          .stream()
          .filter(p -> this.attributes.contains(p.getKey()))
          .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

      final SMFHeader new_header =
        header.withAttributesInOrder(new_by_order);
      return SMFPartialLogged.succeeded(
        SMFMemoryMesh.builder()
          .from(m)
          .setArrays(new_arrays)
          .setHeader(new_header)
          .build());
    }

    return SMFPartialLogged.failed(errors);
  }
}
