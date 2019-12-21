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

import com.io7m.smfj.core.SMFAttributeName;
import com.io7m.smfj.core.SMFHeader;
import com.io7m.smfj.core.SMFPartialLogged;
import com.io7m.smfj.processing.api.SMFAttributeArrayType;
import com.io7m.smfj.processing.api.SMFFilterCommandChecks;
import com.io7m.smfj.processing.api.SMFFilterCommandContext;
import com.io7m.smfj.processing.api.SMFMemoryMesh;
import com.io7m.smfj.processing.api.SMFMemoryMeshFilterType;
import com.io7m.smfj.processing.api.SMFProcessingError;
import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.io7m.smfj.processing.api.SMFFilterCommandParsing.errorExpectedGotValidation;

/**
 * A filter that removes a mesh attribute.
 */

public final class SMFMemoryMeshFilterAttributeRemove implements
  SMFMemoryMeshFilterType
{
  /**
   * The command name.
   */

  public static final String NAME = "remove";

  private static final String SYNTAX = "<name>";

  private final SMFAttributeName source;

  private SMFMemoryMeshFilterAttributeRemove(
    final SMFAttributeName in_source)
  {
    this.source = Objects.requireNonNull(in_source, "Source");
  }

  /**
   * Create a new filter.
   *
   * @param in_source The source attribute name
   *
   * @return A new filter
   */

  public static SMFMemoryMeshFilterType create(
    final SMFAttributeName in_source)
  {
    return new SMFMemoryMeshFilterAttributeRemove(in_source);
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

    if (text.size() == 1) {
      try {
        final SMFAttributeName attr = SMFAttributeName.of(text.get(0));
        return SMFPartialLogged.succeeded(create(attr));
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

    final List<SMFProcessingError> errors =
      SMFFilterCommandChecks.checkAttributeExists(
        List.of(),
        m.header().attributesByName(),
        this.source);

    if (!errors.isEmpty()) {
      return SMFPartialLogged.failed(errors);
    }

    /*
     * Filter the array from the existing arrays.
     */

    final Map<SMFAttributeName, SMFAttributeArrayType> newArrays =
      m.arrays()
        .entrySet()
        .stream()
        .filter(e -> !Objects.equals(e.getKey(), this.source))
        .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

    final SMFHeader origHeader = m.header();

    /*
     * Filter the attribute from the existing attributes.
     */

    final var newAttributes =
      origHeader.attributesInOrder()
        .stream()
        .filter(attr -> !Objects.equals(attr.name(), this.source))
        .collect(Collectors.toList());

    final SMFHeader newHeader =
      origHeader.withAttributesInOrder(newAttributes);

    return SMFPartialLogged.succeeded(
      SMFMemoryMesh.builder()
        .from(m)
        .setHeader(newHeader)
        .setArrays(newArrays)
        .build());
  }
}
