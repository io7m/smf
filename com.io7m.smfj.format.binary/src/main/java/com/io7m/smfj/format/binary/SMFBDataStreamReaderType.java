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

import com.io7m.jlexing.core.LexicalPosition;

import java.io.IOException;
import java.net.URI;
import java.util.Optional;

/**
 * A stream reader.
 */

public interface SMFBDataStreamReaderType
{
  /**
   * @return The URI referred to by the stream
   */

  URI uri();

  /**
   * Read exactly {@code b.length} octets from the stream, raising an error
   * if too few octets are available.
   *
   * @param name A field name for precise error reporting
   * @param b    The output octets
   *
   * @throws IOException On I/O errors
   */

  void readBytes(
    Optional<String> name,
    byte[] b)
    throws IOException;

  /**
   * @return The current stream position; the number of octets consumed so far
   */

  long position();

  /**
   * Read an unsigned 8-bit value from the stream.
   *
   * @param name A field name for precise error reporting
   *
   * @return The read value
   *
   * @throws IOException On I/O errors
   */

  long readU8(
    Optional<String> name)
    throws IOException;

  /**
   * Read a signed 8-bit value from the stream.
   *
   * @param name A field name for precise error reporting
   *
   * @return The read value
   *
   * @throws IOException On I/O errors
   */

  long readS8(
    Optional<String> name)
    throws IOException;

  /**
   * Read an unsigned 16-bit value from the stream.
   *
   * @param name A field name for precise error reporting
   *
   * @return The read value
   *
   * @throws IOException On I/O errors
   */

  long readU16(
    Optional<String> name)
    throws IOException;

  /**
   * Read a signed 16-bit value from the stream.
   *
   * @param name A field name for precise error reporting
   *
   * @return The read value
   *
   * @throws IOException On I/O errors
   */

  long readS16(
    Optional<String> name)
    throws IOException;

  /**
   * Read an unsigned 32-bit value from the stream.
   *
   * @param name A field name for precise error reporting
   *
   * @return The read value
   *
   * @throws IOException On I/O errors
   */

  long readU32(
    Optional<String> name)
    throws IOException;

  /**
   * Read a signed 32-bit value from the stream.
   *
   * @param name A field name for precise error reporting
   *
   * @return The read value
   *
   * @throws IOException On I/O errors
   */

  long readS32(
    Optional<String> name)
    throws IOException;

  /**
   * Read an unsigned 64-bit value from the stream.
   *
   * @param name A field name for precise error reporting
   *
   * @return The read value
   *
   * @throws IOException On I/O errors
   */

  long readU64(
    Optional<String> name)
    throws IOException;

  /**
   * Read a signed 64-bit value from the stream.
   *
   * @param name A field name for precise error reporting
   *
   * @return The read value
   *
   * @throws IOException On I/O errors
   */

  long readS64(
    Optional<String> name)
    throws IOException;

  /**
   * Read an IEEE754-binary16 value from the stream.
   *
   * @param name A field name for precise error reporting
   *
   * @return The read value
   *
   * @throws IOException On I/O errors
   */

  double readF16(
    Optional<String> name)
    throws IOException;

  /**
   * Read an IEEE754-binary32 value from the stream.
   *
   * @param name A field name for precise error reporting
   *
   * @return The read value
   *
   * @throws IOException On I/O errors
   */

  double readF32(
    Optional<String> name)
    throws IOException;

  /**
   * Read an IEEE754-binary64 value from the stream.
   *
   * @param name A field name for precise error reporting
   *
   * @return The read value
   *
   * @throws IOException On I/O errors
   */

  double readF64(
    Optional<String> name)
    throws IOException;

  /**
   * Skip ahead {@code count} octets in the stream.
   *
   * @param count The number of octets to skip
   *
   * @throws IOException On I/O errors
   */

  void skip(
    long count)
    throws IOException;

  /**
   * @return The current stream position; the number of octets consumed so far
   */

  LexicalPosition<URI> positionLexical();

  /**
   * Create a new view of this reader that is only allowed to read {@code size}
   * more octets from the current stream. Reading from the newly created reader
   * will advance the position of the current reader as well.
   *
   * @param size The maximum number of octets that can be read
   *
   * @return A new reader
   */

  SMFBDataStreamReaderType withBounds(long size);

  /**
   * Skip to the given offset. Has no effect if the writer has already passed
   * the given offset.
   *
   * @param offset The offset
   *
   * @throws IOException On I/O errors
   */

  default void skipTo(
    final long offset)
    throws IOException
  {
    final long position = this.position();
    if (Long.compareUnsigned(position, offset) < 0) {
      final long diff = Math.subtractExact(offset, position);
      this.skip(diff);
    }
  }
}
