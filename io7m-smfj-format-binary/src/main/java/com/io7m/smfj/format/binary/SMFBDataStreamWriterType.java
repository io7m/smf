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

package com.io7m.smfj.format.binary;

import java.io.IOException;
import java.nio.file.Path;

/**
 * A stream writer.
 */

public interface SMFBDataStreamWriterType
{
  /**
   * @return The current stream position; the number of octets written (or
   * skipped) so far
   */

  long position();

  /**
   * @return The path referred to by the stream
   */

  Path path();

  /**
   * Write the given bytes to the stream.
   *
   * @param data The bytes
   *
   * @throws IOException On I/O errors
   */

  void putBytes(
    byte[] data)
    throws IOException;

  /**
   * Write a signed 8-bit value to the stream.
   *
   * @param value The value
   *
   * @throws IOException On I/O errors
   */

  void putS8(
    long value)
    throws IOException;

  /**
   * Write an unsigned 8-bit value to the stream.
   *
   * @param value The value
   *
   * @throws IOException On I/O errors
   */

  void putU8(
    long value)
    throws IOException;

  /**
   * Write a string to the stream in UTF-8 encoding, writing as many 0-bytes
   * as necessary after the string to bring it up to a size of {@code maximum}
   * octets.
   *
   * @param text    The string
   * @param maximum The maximum length
   *
   * @throws IOException On I/O errors
   */

  void putStringPadded(
    String text,
    int maximum)
    throws IOException;

  /**
   * Write an unsigned 16-bit value to the stream.
   *
   * @param value The value
   *
   * @throws IOException On I/O errors
   */

  void putU16(
    long value)
    throws IOException;

  /**
   * Write an unsigned 32-bit value to the stream.
   *
   * @param value The value
   *
   * @throws IOException On I/O errors
   */

  void putU32(
    long value)
    throws IOException;

  /**
   * Write an unsigned 64-bit value to the stream.
   *
   * @param value The value
   *
   * @throws IOException On I/O errors
   */

  void putU64(
    long value)
    throws IOException;

  /**
   * Write a signed 16-bit value to the stream.
   *
   * @param value The value
   *
   * @throws IOException On I/O errors
   */

  void putS16(
    long value)
    throws IOException;

  /**
   * Write a signed 32-bit value to the stream.
   *
   * @param value The value
   *
   * @throws IOException On I/O errors
   */

  void putS32(
    long value)
    throws IOException;

  /**
   * Write a signed 64-bit value to the stream.
   *
   * @param value The value
   *
   * @throws IOException On I/O errors
   */

  void putS64(
    long value)
    throws IOException;

  /**
   * Write an IEEE754-binary16 value to the stream.
   *
   * @param value The value
   *
   * @throws IOException On I/O errors
   */

  void putF16(
    double value)
    throws IOException;

  /**
   * Write an IEEE754-binary32 value to the stream.
   *
   * @param value The value
   *
   * @throws IOException On I/O errors
   */

  void putF32(
    double value)
    throws IOException;

  /**
   * Write an IEEE754-binary64 value to the stream.
   *
   * @param value The value
   *
   * @throws IOException On I/O errors
   */

  void putF64(
    double value)
    throws IOException;
}
