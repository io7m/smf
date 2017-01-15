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

import com.io7m.jintegers.Unsigned16;
import com.io7m.jintegers.Unsigned32;
import com.io7m.jintegers.Unsigned8;
import com.io7m.jnull.NullCheck;
import com.io7m.junreachable.UnreachableCodeException;
import com.io7m.smfj.core.SMFSupportedSizes;
import com.io7m.smfj.parser.api.SMFParserEventsDataTrianglesType;

import java.nio.ByteBuffer;

/**
 * An event listener that packs data into a given {@link ByteBuffer}.
 */

public final class SMFByteBufferTrianglePacker implements
  SMFParserEventsDataTrianglesType
{
  private final ByteBuffer buffer;
  private final int size;
  private final int stride;
  private int index;

  /**
   * Construct a packer.
   *
   * @param in_triangle_buffer The byte buffer that will contain triangle data
   * @param in_size            The size in bits of a single triangle index
   */

  public SMFByteBufferTrianglePacker(
    final ByteBuffer in_triangle_buffer,
    final int in_size)
  {
    this.buffer =
      NullCheck.notNull(in_triangle_buffer, "Triangle Buffer");
    this.size =
      SMFSupportedSizes.checkIntegerUnsignedSupported(in_size);
    this.stride = (this.size / 8) * 3;
  }

  private void next()
  {
    this.index = Math.addExact(this.index, this.stride);
  }

  @Override
  public void onDataTrianglesStart()
  {
    // Nothing to be done here
  }

  @Override
  public void onDataTriangle(
    final long v0,
    final long v1,
    final long v2)
  {
    switch (this.size) {
      case 8: {
        Unsigned8.packToBuffer((int) v0, this.buffer, this.index);
        Unsigned8.packToBuffer((int) v1, this.buffer, this.index + 1);
        Unsigned8.packToBuffer((int) v2, this.buffer, this.index + 2);
        break;
      }
      case 16: {
        Unsigned16.packToBuffer((int) v0, this.buffer, this.index);
        Unsigned16.packToBuffer((int) v1, this.buffer, this.index + 2);
        Unsigned16.packToBuffer((int) v2, this.buffer, this.index + 4);
        break;
      }
      case 32: {
        Unsigned32.packToBuffer(v0, this.buffer, this.index);
        Unsigned32.packToBuffer(v1, this.buffer, this.index + 4);
        Unsigned32.packToBuffer(v2, this.buffer, this.index + 8);
        break;
      }
      case 64: {
        this.buffer.putLong(this.index, v0);
        this.buffer.putLong(this.index + 8, v1);
        this.buffer.putLong(this.index + 16, v2);
        break;
      }
      default: {
        throw new UnreachableCodeException();
      }
    }

    this.next();
  }

  @Override
  public void onDataTrianglesFinish()
  {
    // Nothing to be done here
  }
}
