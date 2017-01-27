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

package com.io7m.smfj.bytebuffer;

import com.io7m.jaffirm.core.Preconditions;
import com.io7m.jpra.runtime.java.JPRACursor1DType;
import com.io7m.smfj.core.SMFImmutableStyleType;
import com.io7m.smfj.core.SMFSupportedSizes;
import org.immutables.javaslang.encodings.JavaslangEncodingEnabled;
import org.immutables.value.Value;

import java.nio.ByteBuffer;

/**
 * A set of triangles packed into a byte buffer.
 */

@Value.Immutable
@JavaslangEncodingEnabled
@SMFImmutableStyleType
public interface SMFByteBufferPackedTrianglesType
{
  /**
   * @return The byte buffer
   */

  @Value.Parameter
  ByteBuffer byteBuffer();

  /**
   * @return The number of triangles present
   */

  @Value.Parameter
  long triangleCount();

  /**
   * @return The size in bits of a single triangle index
   */

  @Value.Parameter
  int triangleIndexSizeBits();

  /**
   * @return The size in octets of a single triangle index
   */

  @Value.Lazy
  default int triangleIndexSizeOctets()
  {
    return this.triangleIndexSizeBits() / 8;
  }

  /**
   * @return The size in bits of a single triangle
   */

  @Value.Lazy
  default int triangleSizeBits()
  {
    return Math.multiplyExact(this.triangleIndexSizeBits(), 3);
  }

  /**
   * @return The size in octets of a single triangle
   */

  @Value.Lazy
  default int triangleSizeOctets()
  {
    return Math.multiplyExact(this.triangleIndexSizeOctets(), 3);
  }

  /**
   * @return A cursor that can access individual triangles
   */

  @Value.Lazy
  default JPRACursor1DType<SMFByteBufferIntegerUnsigned3Type> cursor()
  {
    Preconditions.checkPreconditionL(
      this.triangleCount(),
      Long.compareUnsigned(this.triangleCount(), 0L) > 0,
      x -> "Triangle count must be non-zero");

    return SMFByteBufferCursors.createUnsigned3Raw(
      this.byteBuffer(),
      this.triangleIndexSizeBits(),
      0,
      this.triangleSizeOctets());
  }

  /**
   * Check preconditions for the type.
   */

  @Value.Check
  default void checkPreconditions()
  {
    SMFSupportedSizes.checkIntegerUnsignedSupported(
      this.triangleIndexSizeBits());
  }
}
