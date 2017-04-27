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
import com.io7m.smfj.core.SMFAttribute;
import com.io7m.smfj.core.SMFAttributeName;
import com.io7m.smfj.core.SMFHeader;
import com.io7m.smfj.parser.api.SMFParseError;
import com.io7m.smfj.processing.api.SMFAttributeArrayType;
import com.io7m.smfj.processing.api.SMFFilterCommandChecks;
import com.io7m.smfj.processing.api.SMFFilterCommandContext;
import com.io7m.smfj.processing.api.SMFFilterCommandParsing;
import com.io7m.smfj.processing.api.SMFMemoryMesh;
import com.io7m.smfj.processing.api.SMFMemoryMeshFilterType;
import com.io7m.smfj.processing.api.SMFProcessingError;
import javaslang.collection.List;
import javaslang.collection.Map;
import javaslang.collection.Seq;
import javaslang.control.Validation;

import java.nio.file.Path;
import java.util.Optional;

import static javaslang.control.Validation.invalid;

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
    this.source = NullCheck.notNull(in_source, "Source");
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

  public static Validation<List<SMFParseError>, SMFMemoryMeshFilterType> parse(
    final Optional<Path> file,
    final int line,
    final List<String> text)
  {
    NullCheck.notNull(file, "file");
    NullCheck.notNull(text, "text");

    if (text.length() == 1) {
      try {
        final SMFAttributeName attr = SMFAttributeName.of(text.get(0));
        return Validation.valid(create(attr));
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

    final Seq<SMFProcessingError> errors =
      SMFFilterCommandChecks.checkAttributeExists(
        List.empty(), m.header().attributesByName(), this.source);
    if (!errors.isEmpty()) {
      return invalid(List.ofAll(errors));
    }

    final Map<SMFAttributeName, SMFAttributeArrayType> arrays = m.arrays();
    final SMFHeader orig_header = m.header();

    /*
     * Remove array.
     */

    final Map<SMFAttributeName, SMFAttributeArrayType> removed_arrays =
      arrays.remove(this.source);

    /*
     * Remove attribute.
     */

    final List<SMFAttribute> orig_ordered =
      orig_header.attributesInOrder();
    final Map<SMFAttributeName, SMFAttribute> orig_by_name =
      orig_header.attributesByName();
    final SMFAttribute orig_attrib =
      orig_by_name.get(this.source).get();
    final List<SMFAttribute> new_ordered =
      orig_ordered.remove(orig_attrib);
    final SMFHeader new_header =
      orig_header.withAttributesInOrder(new_ordered);

    return Validation.valid(
      SMFMemoryMesh.builder()
        .from(m)
        .setHeader(new_header)
        .setArrays(removed_arrays)
        .build());
  }
}
