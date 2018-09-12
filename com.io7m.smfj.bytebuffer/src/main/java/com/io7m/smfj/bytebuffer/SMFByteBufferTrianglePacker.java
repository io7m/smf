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

import com.io7m.jpra.runtime.java.JPRACursor1DType;
import com.io7m.smfj.core.SMFErrorType;
import com.io7m.smfj.core.SMFSupportedSizes;
import com.io7m.smfj.core.SMFWarningType;
import com.io7m.smfj.parser.api.SMFParserEventsDataTrianglesType;
import com.io7m.smfj.parser.api.SMFParserEventsErrorType;

import java.nio.ByteBuffer;
import java.util.Objects;

/**
 * An event listener that packs data into a given {@link ByteBuffer}.
 */

public final class SMFByteBufferTrianglePacker implements
  SMFParserEventsDataTrianglesType
{
  private final JPRACursor1DType<SMFByteBufferIntegerUnsigned3Type> cursor;
  private final long triangles;
  private final SMFParserEventsErrorType events;

  /**
   * Construct a packer.
   *
   * @param in_events          A receiver of errors and warnings
   * @param in_triangle_buffer The byte buffer that will contain triangle data
   * @param in_size            The size in bits of a single triangle index
   * @param in_triangles       The number of triangles that will be packed
   */

  public SMFByteBufferTrianglePacker(
    final SMFParserEventsErrorType in_events,
    final ByteBuffer in_triangle_buffer,
    final int in_size,
    final long in_triangles)
  {
    final ByteBuffer buffer =
      Objects.requireNonNull(in_triangle_buffer, "Triangle Buffer");
    final int size = SMFSupportedSizes.checkIntegerUnsignedSupported(in_size);
    final int stride = Math.multiplyExact(size / 8, 3);
    this.cursor = SMFByteBufferCursors.createUnsigned3Raw(
      buffer, in_size, 0, stride);
    this.triangles = in_triangles;
    this.events = Objects.requireNonNull(in_events, "in_events");
  }

  private <T> void cursorNext(
    final JPRACursor1DType<T> c)
  {
    final int next = Math.addExact(c.getElementIndex(), 1);
    final long next_u = Integer.toUnsignedLong(next);
    if (Long.compareUnsigned(next_u, this.triangles) < 0) {
      c.setElementIndex(next);
    }
  }

  @Override
  public void onDataTriangle(
    final long v0,
    final long v1,
    final long v2)
  {
    this.cursor.getElementView().set3UL(v0, v1, v2);
    this.cursorNext(this.cursor);
  }

  @Override
  public void onDataTrianglesFinish()
  {
    // Nothing to be done here
  }

  @Override
  public void onError(
    final SMFErrorType e)
  {
    this.events.onError(e);
  }

  @Override
  public void onWarning(
    final SMFWarningType w)
  {
    this.events.onWarning(w);
  }
}
