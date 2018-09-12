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
import io.vavr.Tuple;
import io.vavr.collection.List;
import io.vavr.collection.Map;
import io.vavr.collection.Seq;
import io.vavr.collection.SortedMap;
import io.vavr.control.Validation;

import java.net.URI;
import java.util.Objects;
import java.util.Optional;

import static com.io7m.smfj.processing.api.SMFFilterCommandChecks.checkAttributeExists;
import static com.io7m.smfj.processing.api.SMFFilterCommandChecks.checkAttributeNonexistent;
import static com.io7m.smfj.processing.api.SMFFilterCommandParsing.errorExpectedGotValidation;
import static io.vavr.control.Validation.invalid;

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

  public static Validation<Seq<SMFParseError>, SMFMemoryMeshFilterType> parse(
    final Optional<URI> file,
    final int line,
    final List<String> text)
  {
    Objects.requireNonNull(file, "file");
    Objects.requireNonNull(text, "text");

    if (text.length() == 2) {
      try {
        final SMFAttributeName source = SMFAttributeName.of(text.get(0));
        final SMFAttributeName target = SMFAttributeName.of(text.get(1));
        return Validation.valid(
          create(source, target));
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
  public Validation<Seq<SMFProcessingError>, SMFMemoryMesh> filter(
    final SMFFilterCommandContext context,
    final SMFMemoryMesh m)
  {
    Objects.requireNonNull(context, "Context");
    Objects.requireNonNull(m, "Mesh");

    final SortedMap<SMFAttributeName, SMFAttribute> by_name =
      m.header().attributesByName();
    Seq<SMFProcessingError> errors =
      checkAttributeExists(List.empty(), by_name, this.source);
    errors = checkAttributeNonexistent(errors, by_name, this.target);

    if (!errors.isEmpty()) {
      return invalid(List.ofAll(errors));
    }

    final Map<SMFAttributeName, SMFAttributeArrayType> arrays = m.arrays();
    final SMFHeader orig_header = m.header();

    /*
     * Rename array.
     */

    final SMFAttributeArrayType array = arrays.get(this.source).get();
    final Map<SMFAttributeName, SMFAttributeArrayType> renamed_arrays =
      arrays.replace(
        Tuple.of(this.source, array),
        Tuple.of(this.target, array));

    /*
     * Rename attribute.
     */

    final Map<SMFAttributeName, SMFAttribute> orig_by_name =
      orig_header.attributesByName();
    final SMFAttribute orig_attrib =
      orig_by_name.get(this.source).get();
    final SMFAttribute new_attrib =
      orig_attrib.withName(this.target);

    final SMFHeader new_header =
      orig_header.withAttributesInOrder(
        orig_header.attributesInOrder().replace(orig_attrib, new_attrib));

    return Validation.valid(
      SMFMemoryMesh.builder()
        .from(m)
        .setHeader(new_header)
        .setArrays(renamed_arrays)
        .build());
  }
}
