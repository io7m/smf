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
import com.io7m.smfj.core.SMFErrorType;
import com.io7m.smfj.core.SMFHeader;
import com.io7m.smfj.core.SMFPartialLogged;
import com.io7m.smfj.processing.api.SMFAttributeArrayType;
import com.io7m.smfj.processing.api.SMFFilterCommandContext;
import com.io7m.smfj.processing.api.SMFMemoryMesh;
import com.io7m.smfj.processing.api.SMFMemoryMeshFilterType;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.SortedMap;
import java.util.stream.Collectors;

import static com.io7m.smfj.processing.api.SMFFilterCommandChecks.checkAttributeExists;
import static com.io7m.smfj.processing.api.SMFFilterCommandChecks.checkAttributeNonexistent;
import static com.io7m.smfj.processing.api.SMFFilterCommandParsing.errorExpectedGotValidation;

/**
 * A filter that renames a mesh attribute.
 */

public final class SMFMemoryMeshFilterAttributeRename implements
  SMFMemoryMeshFilterType
{
  /**
   * The command name.
   */

  public static final String NAME = "rename";

  private static final String SYNTAX = "<source> <target>";

  private final SMFAttributeName source;
  private final SMFAttributeName target;

  private SMFMemoryMeshFilterAttributeRename(
    final SMFAttributeName in_source,
    final SMFAttributeName in_target)
  {
    this.source = Objects.requireNonNull(in_source, "Source");
    this.target = Objects.requireNonNull(in_target, "Target");
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

    if (text.size() == 2) {
      try {
        final SMFAttributeName source = SMFAttributeName.of(text.get(0));
        final SMFAttributeName target = SMFAttributeName.of(text.get(1));
        return SMFPartialLogged.succeeded(create(source, target));
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
   * @param in_source The source attribute name
   * @param in_target The target attribute name
   *
   * @return A new filter
   */

  public static SMFMemoryMeshFilterType create(
    final SMFAttributeName in_source,
    final SMFAttributeName in_target)
  {
    return new SMFMemoryMeshFilterAttributeRename(in_source, in_target);
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

    final SortedMap<SMFAttributeName, SMFAttribute> by_name =
      m.header().attributesByName();

    /*
     * Check the source attribute exists, and check that no attribute
     * exists with the target name.
     */

    final var errors = new ArrayList<SMFErrorType>();
    errors.addAll(checkAttributeExists(List.of(), by_name, this.source));
    errors.addAll(checkAttributeNonexistent(List.of(), by_name, this.target));

    if (!errors.isEmpty()) {
      return SMFPartialLogged.failed(errors);
    }

    final Map<SMFAttributeName, SMFAttributeArrayType> arrays = m.arrays();
    final SMFHeader orig_header = m.header();

    /*
     * Rename array.
     */

    final HashMap<SMFAttributeName, SMFAttributeArrayType> newArrays =
      new HashMap<>(arrays);

    final var array = newArrays.get(this.source);
    newArrays.remove(this.source);
    newArrays.put(this.target, array);

    /*
     * Rename attribute.
     */

    final var newAttributes =
      orig_header.attributesInOrder()
        .stream()
        .map(this::replaceAttribute)
        .collect(Collectors.toList());

    final SMFHeader new_header =
      orig_header.withAttributesInOrder(newAttributes);

    return SMFPartialLogged.succeeded(
      SMFMemoryMesh.builder()
        .from(m)
        .setHeader(new_header)
        .setArrays(newArrays)
        .build());
  }

  private SMFAttribute replaceAttribute(
    final SMFAttribute attr)
  {
    if (Objects.equals(attr.name(), this.source)) {
      return attr.withName(this.target);
    }
    return attr;
  }
}
