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
import com.io7m.smfj.processing.api.SMFFilterCommandContext;
import com.io7m.smfj.processing.api.SMFMemoryMesh;
import com.io7m.smfj.processing.api.SMFMemoryMeshFilterType;
import com.io7m.smfj.processing.api.SMFProcessingError;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.SortedMap;
import java.util.stream.Collectors;

import static com.io7m.smfj.processing.api.SMFFilterCommandChecks.checkAttributeExists;
import static com.io7m.smfj.processing.api.SMFFilterCommandParsing.errorExpectedGotValidation;

/**
 * A filter that resamples attribute data.
 */

public final class SMFMemoryMeshFilterAttributeResample implements
  SMFMemoryMeshFilterType
{
  /**
   * The command name.
   */

  public static final String NAME = "resample";

  private static final String SYNTAX = "<name> <size>";

  private final SMFAttributeName attribute;
  private final int size;

  private SMFMemoryMeshFilterAttributeResample(
    final SMFAttributeName in_attribute,
    final int in_size)
  {
    this.attribute = Objects.requireNonNull(in_attribute, "Attribute");
    this.size = in_size;
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
        final SMFAttributeName name = SMFAttributeName.of(text.get(0));
        final int size = Integer.parseUnsignedInt(text.get(1));
        return SMFPartialLogged.succeeded(create(name, size));
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
   * @param in_attribute The name of the attribute that will be resampled
   * @param in_size      The new size in bits
   *
   * @return A new filter
   */

  public static SMFMemoryMeshFilterType create(
    final SMFAttributeName in_attribute,
    final int in_size)
  {
    return new SMFMemoryMeshFilterAttributeResample(in_attribute, in_size);
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

    final var errors =
      new ArrayList<>(checkAttributeExists(List.of(), by_name, this.attribute));

    if (errors.isEmpty()) {
      try {
        final SMFAttribute original =
          by_name.get(this.attribute);
        final SMFAttribute resampled =
          original.withComponentSizeBits(this.size);
        final List<SMFAttribute> newAttributes =
          header.attributesInOrder()
            .stream()
            .map(existing ->
                   replaceExistingWithResampled(original, resampled, existing))
            .collect(Collectors.toList());

        final SMFHeader new_header =
          header.withAttributesInOrder(newAttributes);

        return SMFPartialLogged.succeeded(
          SMFMemoryMesh.builder()
            .from(m)
            .setHeader(new_header)
            .build());
      } catch (final UnsupportedOperationException e) {
        errors.add(SMFProcessingError.of(e.getMessage(), Optional.of(e)));
      }
    }

    return SMFPartialLogged.failed(errors);
  }

  private static SMFAttribute replaceExistingWithResampled(
    final SMFAttribute original,
    final SMFAttribute resampled,
    final SMFAttribute existing)
  {
    if (Objects.equals(existing, original)) {
      return resampled;
    }
    return existing;
  }
}
