/*
 * Copyright © 2017 <code@io7m.com> http://io7m.com
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

package com.io7m.smfj.core;

import com.io7m.junreachable.UnreachableCodeException;
import javaslang.collection.List;

import java.util.stream.Collectors;

/**
 * The supported sizes in bits of each type.
 */

public final class SMFSupportedSizes
{
  private static final List<Integer> SUPPORTED_INTEGER_UNSIGNED;
  private static final List<Integer> SUPPORTED_INTEGER_SIGNED;
  private static final List<Integer> SUPPORTED_FLOAT;

  static {
    SUPPORTED_INTEGER_UNSIGNED =
      List.of(
        Integer.valueOf(8),
        Integer.valueOf(16),
        Integer.valueOf(32),
        Integer.valueOf(64));
    SUPPORTED_INTEGER_SIGNED =
      List.of(
        Integer.valueOf(8),
        Integer.valueOf(16),
        Integer.valueOf(32),
        Integer.valueOf(64));
    SUPPORTED_FLOAT =
      List.of(
        Integer.valueOf(16),
        Integer.valueOf(32),
        Integer.valueOf(64));
  }

  private SMFSupportedSizes()
  {
    throw new UnreachableCodeException();
  }

  /**
   * @param bits The size in bits
   *
   * @return {@code true} if an unsigned integer type with the given size is
   * supported
   */

  public static boolean isIntegerUnsignedSupported(
    final int bits)
  {
    return SUPPORTED_INTEGER_UNSIGNED.contains(Integer.valueOf(bits));
  }

  /**
   * Check if an unsigned integer type with the given size is supported.
   *
   * @param bits The size in bits
   *
   * @return {@code bits}
   *
   * @throws UnsupportedOperationException If the size is not supported
   */

  public static int checkIntegerUnsignedSupported(
    final int bits)
    throws UnsupportedOperationException
  {
    if (!isIntegerUnsignedSupported(bits)) {
      return unsupported(bits, "unsigned integer", SUPPORTED_INTEGER_UNSIGNED);
    }
    return bits;
  }

  /**
   * @param bits The size in bits
   *
   * @return {@code true} if a signed integer type with the given size is
   * supported
   */

  public static boolean isIntegerSignedSupported(
    final int bits)
  {
    return SUPPORTED_INTEGER_SIGNED.contains(Integer.valueOf(bits));
  }

  /**
   * Check if a signed integer type with the given size is supported.
   *
   * @param bits The size in bits
   *
   * @return {@code bits}
   *
   * @throws UnsupportedOperationException If the size is not supported
   */

  public static int checkIntegerSignedSupported(
    final int bits)
    throws UnsupportedOperationException
  {
    if (!isIntegerSignedSupported(bits)) {
      return unsupported(bits, "signed integer", SUPPORTED_INTEGER_SIGNED);
    }
    return bits;
  }

  /**
   * @param bits The size in bits
   *
   * @return {@code true} if a floating point type with the given size is
   * supported
   */

  public static boolean isFloatSupported(
    final int bits)
  {
    return SUPPORTED_FLOAT.contains(Integer.valueOf(bits));
  }

  /**
   * Check if a floating point type with the given size is supported.
   *
   * @param bits The size in bits
   *
   * @return {@code bits}
   *
   * @throws UnsupportedOperationException If the size is not supported
   */

  public static int checkFloatSupported(
    final int bits)
    throws UnsupportedOperationException
  {
    if (!isFloatSupported(bits)) {
      return unsupported(bits, "float", SUPPORTED_FLOAT);
    }
    return bits;
  }

  private static int unsupported(
    final int bits,
    final String type,
    final List<Integer> sizes)
  {
    final StringBuilder sb = new StringBuilder(128);
    sb.append("Unsupported ");
    sb.append(type);
    sb.append(" size.");
    sb.append(System.lineSeparator());
    sb.append("  Received:  ");
    sb.append(bits);
    sb.append(System.lineSeparator());
    sb.append("  Supported: ");
    sb.append(sizes.toJavaStream()
                .map(x -> Integer.toUnsignedString(x.intValue()))
                .collect(Collectors.joining("|")));
    sb.append(System.lineSeparator());
    throw new UnsupportedOperationException(sb.toString());
  }
}
