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
   * @return The packing configuration used to produce the packed mesh
   */

  @Value.Parameter
  SMFByteBufferPackingConfiguration configuration();

  /**
   * @return A byte buffer containing packed attribute data
   */

  @Value.Parameter
  Optional<ByteBuffer> attributeData();

  /**
   * @return A byte buffer containing packed triangle data
   */

  @Value.Parameter
  Optional<ByteBuffer> triangleData();
}
