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

import com.io7m.ieee754b16.Binary16;
import com.io7m.jnull.NullCheck;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;

final class SMFBDataStreamWriter
{
  private final Path path;
  private final OutputStream stream;
  private final ByteBuffer buffer8;
  private final ByteBuffer buffer4;
  private final ByteBuffer buffer2;
  private final ByteBuffer buffer1;
  private final byte[] byte8;
  private final byte[] byte4;
  private final byte[] byte2;
  private final byte[] byte1;
  private long position;

  SMFBDataStreamWriter(
    final Path in_path,
    final OutputStream in_stream)
  {
    this.path = NullCheck.notNull(in_path, "Path");
    this.stream = NullCheck.notNull(in_stream, "Stream");

    this.byte8 = new byte[8];
    this.buffer8 = ByteBuffer.wrap(this.byte8);
    this.buffer8.order(ByteOrder.BIG_ENDIAN);
    this.byte4 = new byte[4];
    this.buffer4 = ByteBuffer.wrap(this.byte4);
    this.buffer4.order(ByteOrder.BIG_ENDIAN);
    this.byte2 = new byte[2];
    this.buffer2 = ByteBuffer.wrap(this.byte2);
    this.buffer2.order(ByteOrder.BIG_ENDIAN);
    this.byte1 = new byte[1];
    this.buffer1 = ByteBuffer.wrap(this.byte1);
    this.buffer1.order(ByteOrder.BIG_ENDIAN);

    this.position = 0L;
  }

  long position()
  {
    return this.position;
  }

  Path path()
  {
    return this.path;
  }

  void writeBytes(
    final byte[] data)
    throws IOException
  {
    this.stream.write(data);
    this.position = Math.addExact(this.position, (long) data.length);
  }

  void writeU8(
    final long value)
    throws IOException
  {
    this.byte1[0] = (byte) (value & 0xffL);
    this.stream.write(this.byte1);
    this.position = Math.addExact(this.position, 1L);
  }

  void writePaddedString(
    final String text,
    final int maximum)
    throws IOException
  {
    final byte[] data = new byte[maximum];
    final byte[] textb = text.getBytes(StandardCharsets.UTF_8);
    System.arraycopy(textb, 0, data, 0, textb.length);
    this.writeU32((long) textb.length);
    this.stream.write(data);
    this.position = Math.addExact(this.position, (long) data.length);
  }

  void writeU16(
    final long value)
    throws IOException
  {
    this.checkAlignment(2L);
    this.buffer2.putChar(0, (char) (value & 0xffffL));
    this.stream.write(this.byte2);
    this.position = Math.addExact(this.position, 2L);
  }

  void writeU32(
    final long value)
    throws IOException
  {
    this.checkAlignment(4L);
    this.buffer4.putInt(0, (int) (value & 0xffffffffL));
    this.stream.write(this.byte4);
    this.position = Math.addExact(this.position, 4L);
  }

  void writeU64(
    final long value)
    throws IOException
  {
    this.checkAlignment(8L);
    this.buffer8.putLong(0, value);
    this.stream.write(this.byte8);
    this.position = Math.addExact(this.position, 8L);
  }

  void writeS16(
    final long value)
    throws IOException
  {
    this.checkAlignment(2L);
    this.buffer2.putShort(0, (short) (value & 0xffffL));
    this.stream.write(this.byte2);
    this.position = Math.addExact(this.position, 2L);
  }

  void writeS32(
    final long value)
    throws IOException
  {
    this.checkAlignment(4L);
    this.buffer4.putInt(0, (int) (value & 0xffffffffL));
    this.stream.write(this.byte4);
    this.position = Math.addExact(this.position, 4L);
  }

  void writeS64(
    final long value)
    throws IOException
  {
    this.checkAlignment(8L);
    this.buffer8.putLong(0, value);
    this.stream.write(this.byte8);
    this.position = Math.addExact(this.position, 8L);
  }

  void writeF16(
    final double value)
    throws IOException
  {
    this.checkAlignment(2L);
    this.buffer2.putChar(0, Binary16.packDouble(value));
    this.stream.write(this.byte2);
    this.position = Math.addExact(this.position, 2L);
  }

  void writeF32(
    final double value)
    throws IOException
  {
    this.checkAlignment(4L);
    this.buffer4.putFloat(0, (float) value);
    this.stream.write(this.byte4);
    this.position = Math.addExact(this.position, 4L);
  }

  private void checkAlignment(
    final long align)
    throws IOException
  {
    if (this.position % align != 0L) {
      final StringBuilder sb = new StringBuilder(128);
      sb.append("Unaligned write.");
      sb.append(System.lineSeparator());
      sb.append("  Alignment required: ");
      sb.append(align);
      sb.append(System.lineSeparator());
      sb.append("  Position:           ");
      sb.append(this.position);
      sb.append(System.lineSeparator());
      throw new IOException(sb.toString());
    }
  }

  void writeF64(
    final double value)
    throws IOException
  {
    this.checkAlignment(8L);
    this.buffer8.putDouble(0, value);
    this.stream.write(this.byte8);
    this.position = Math.addExact(this.position, 8L);
  }
}
