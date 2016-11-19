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
import com.io7m.jaffirm.core.Preconditions;
import com.io7m.jnull.NullCheck;
import org.apache.commons.io.output.CountingOutputStream;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;

/**
 * The default implementation of the {@link SMFBDataStreamWriterType} interface.
 */

public final class SMFBDataStreamWriter implements SMFBDataStreamWriterType
{
  private final Path path;
  private final CountingOutputStream stream;
  private final ByteBuffer buffer8;
  private final ByteBuffer buffer4;
  private final ByteBuffer buffer2;
  private final ByteBuffer buffer1;
  private final byte[] byte8;
  private final byte[] byte4;
  private final byte[] byte2;
  private final byte[] byte1;

  private SMFBDataStreamWriter(
    final Path in_path,
    final OutputStream in_stream)
  {
    this.path = NullCheck.notNull(in_path, "Path");
    this.stream =
      new CountingOutputStream(NullCheck.notNull(in_stream, "Stream"));

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
  }

  /**
   * Create a data stream writer.
   *
   * @param in_path   The path
   * @param in_stream The stream
   *
   * @return A new data stream writer
   */

  public static SMFBDataStreamWriterType create(
    final Path in_path,
    final OutputStream in_stream)
  {
    return new SMFBDataStreamWriter(in_path, in_stream);
  }

  @Override
  public long position()
  {
    return this.stream.getByteCount();
  }

  @Override
  public Path path()
  {
    return this.path;
  }

  @Override
  public void putBytes(
    final byte[] data)
    throws IOException
  {
    this.stream.write(data);
  }

  @Override
  public void putS8(final long value)
    throws IOException
  {
    this.byte1[0] = (byte) (value & 0x7fL);
    this.stream.write(this.byte1);
  }

  @Override
  public void putU8(
    final long value)
    throws IOException
  {
    this.byte1[0] = (byte) (value & 0xffL);
    this.stream.write(this.byte1);
  }

  @Override
  public void putStringPadded(
    final String text,
    final int maximum)
    throws IOException
  {
    this.checkAlignment(8L);
    final byte[] textb = text.getBytes(StandardCharsets.UTF_8);

    Preconditions.checkPreconditionI(
      textb.length,
      textb.length <= maximum,
      n -> "Length " + n + " must be <= " + maximum);

    final byte[] data = new byte[maximum];
    System.arraycopy(textb, 0, data, 0, textb.length);
    this.putU32((long) textb.length);
    this.stream.write(data);
  }

  @Override
  public void putU16(
    final long value)
    throws IOException
  {
    this.checkAlignment(2L);
    this.buffer2.putChar(0, (char) (value & 0xffffL));
    this.stream.write(this.byte2);
  }

  @Override
  public void putU32(
    final long value)
    throws IOException
  {
    this.checkAlignment(4L);
    this.buffer4.putInt(0, (int) (value & 0xffffffffL));
    this.stream.write(this.byte4);
  }

  @Override
  public void putU64(
    final long value)
    throws IOException
  {
    this.checkAlignment(8L);
    this.buffer8.putLong(0, value);
    this.stream.write(this.byte8);
  }

  @Override
  public void putS16(
    final long value)
    throws IOException
  {
    this.checkAlignment(2L);
    this.buffer2.putShort(0, (short) (value & 0xffffL));
    this.stream.write(this.byte2);
  }

  @Override
  public void putS32(
    final long value)
    throws IOException
  {
    this.checkAlignment(4L);
    this.buffer4.putInt(0, (int) (value & 0xffffffffL));
    this.stream.write(this.byte4);
  }

  @Override
  public void putS64(
    final long value)
    throws IOException
  {
    this.checkAlignment(8L);
    this.buffer8.putLong(0, value);
    this.stream.write(this.byte8);
  }

  @Override
  public void putF16(
    final double value)
    throws IOException
  {
    this.checkAlignment(2L);
    this.buffer2.putChar(0, Binary16.packDouble(value));
    this.stream.write(this.byte2);
  }

  @Override
  public void putF32(
    final double value)
    throws IOException
  {
    this.checkAlignment(4L);
    this.buffer4.putFloat(0, (float) value);
    this.stream.write(this.byte4);
  }

  private void checkAlignment(
    final long align)
    throws IOException
  {
    if (this.position() % align != 0L) {
      final StringBuilder sb = new StringBuilder(128);
      sb.append("Unaligned write.");
      sb.append(System.lineSeparator());
      sb.append("  Alignment required: ");
      sb.append(align);
      sb.append(System.lineSeparator());
      sb.append("  Position:           ");
      sb.append(this.position());
      sb.append(System.lineSeparator());
      throw new IOException(sb.toString());
    }
  }

  @Override
  public void putF64(
    final double value)
    throws IOException
  {
    this.checkAlignment(8L);
    this.buffer8.putDouble(0, value);
    this.stream.write(this.byte8);
  }
}
