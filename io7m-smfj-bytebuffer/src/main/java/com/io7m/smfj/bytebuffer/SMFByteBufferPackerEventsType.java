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

import com.io7m.smfj.core.SMFHeader;
import javaslang.collection.SortedMap;

import java.nio.ByteBuffer;

/**
 * The type of byte buffer packing event listeners.
 */

public interface SMFByteBufferPackerEventsType
{
  /**
   * A function that is evaluated for a parsed header. The function must
   * return exactly one {@link SMFByteBufferPackingConfiguration} for each
   * {@link  ByteBuffer} that should be created to hold attribute data.
   *
   * @param header The parsed header
   *
   * @return A set of packing configurations
   */

  SortedMap<Integer, SMFByteBufferPackingConfiguration> onHeader(
    SMFHeader header);

  /**
   * @return {@code true} iff triangles should be packed
   */

  boolean onShouldPackTriangles();

  /**
   * A function that will allocate a {@link ByteBuffer} to hold triangle
   * data.
   *
   * @param size The required size in octets of the allocated buffer
   *
   * @return A buffer of {@code size} octets
   */

  ByteBuffer onAllocateTriangleBuffer(
    long size);

  /**
   * A function that will allocate a {@link ByteBuffer} to hold attribute
   * data.
   *
   * @param id     The configuration key
   * @param config The  packing configuration
   * @param size   The required size in octets of the allocated buffer
   *
   * @return A buffer of {@code size} octets
   */

  ByteBuffer onAllocateAttributeBuffer(
    Integer id,
    SMFByteBufferPackingConfiguration config,
    long size);
}
