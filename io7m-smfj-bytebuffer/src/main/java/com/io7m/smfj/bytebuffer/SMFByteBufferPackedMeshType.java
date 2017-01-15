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

import com.io7m.smfj.core.SMFImmutableStyleType;
import javaslang.collection.SortedMap;
import org.immutables.value.Value;

import java.nio.ByteBuffer;
import java.util.Optional;

/**
 * A mesh that has been packed into a set of byte buffers.
 */

@SMFImmutableStyleType
@Value.Immutable
public interface SMFByteBufferPackedMeshType
{
  /**
   * The packing configurations used to produce the mesh. This is the value
   * returned by {@link SMFByteBufferPackerEventsType#onHeader(com.io7m.smfj.core.SMFHeader)}.
   *
   * @return The packing configurations used to produce the packed mesh
   */

  @Value.Parameter
  SortedMap<Integer, SMFByteBufferPackingConfiguration> configurations();

  /**
   * The set of produced byte buffers. Exactly one byte buffer is allocated
   * per packing configuration and the buffers are keyed by the corresponding
   * keys in the map of configurations.
   *
   * @return A byte buffer containing packed attribute data
   *
   * @see #configurations()
   */

  @Value.Parameter
  SortedMap<Integer, ByteBuffer> attributeBuffers();

  /**
   * @return A byte buffer containing packed triangle data
   */

  @Value.Parameter
  Optional<ByteBuffer> triangleBuffer();
}
