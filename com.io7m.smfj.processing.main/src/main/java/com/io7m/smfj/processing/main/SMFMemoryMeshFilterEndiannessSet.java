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
import com.io7m.smfj.processing.api.SMFFilterCommandContext;
import com.io7m.smfj.processing.api.SMFMemoryMesh;
import com.io7m.smfj.processing.api.SMFMemoryMeshFilterType;
import java.net.URI;
import java.nio.ByteOrder;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static com.io7m.smfj.processing.api.SMFFilterCommandParsing.errorExpectedGotValidation;

/**
 * A filter that sets the endianness of mesh data.
 */

public final class SMFMemoryMeshFilterEndiannessSet
  implements SMFMemoryMeshFilterType
{
  /**
   * The command name.
   */

  public static final String NAME = "endianness-set";

  private static final String SYNTAX = "('big' | 'little')";

  private final ByteOrder byteOrder;

  private SMFMemoryMeshFilterEndiannessSet(
    final ByteOrder in_config)
  {
    this.byteOrder = Objects.requireNonNull(in_config, "Config");
  }

  /**
   * Create a new filter.
   *
   * @param byteOrder The configuration
   *
   * @return A new filter
   */

  public static SMFMemoryMeshFilterType create(
    final ByteOrder byteOrder)
  {
    return new SMFMemoryMeshFilterEndiannessSet(byteOrder);
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
        final String order = text.get(0);
        switch (order) {
          case "big": {
            return SMFPartialLogged.succeeded(create(ByteOrder.BIG_ENDIAN));
          }
          case "little": {
            return SMFPartialLogged.succeeded(create(ByteOrder.LITTLE_ENDIAN));
          }
          default: {
            throw new IllegalArgumentException(
              "Byte order must be 'big' | 'little'");
          }
        }
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

    final var newHeader = m.header().withDataByteOrder(this.byteOrder);
    return SMFPartialLogged.succeeded(m.withHeader(newHeader));
  }
}
