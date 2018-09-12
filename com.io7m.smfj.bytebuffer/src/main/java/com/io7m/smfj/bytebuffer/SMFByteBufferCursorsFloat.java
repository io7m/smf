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

import com.io7m.ieee754b16.Binary16;
import java.util.Objects;
import com.io7m.jpra.runtime.java.JPRACursorByteReadableType;
import com.io7m.jtensors.storage.heap.VectorMutable2D;
import com.io7m.jtensors.storage.heap.VectorMutable3D;
import com.io7m.jtensors.storage.heap.VectorMutable4D;
import com.io7m.junreachable.UnreachableCodeException;

import java.nio.ByteBuffer;

final class SMFByteBufferCursorsFloat
{
  private SMFByteBufferCursorsFloat()
  {
    throw new UnreachableCodeException();
  }

  static final class Float4b16 extends SMFByteBufferCursor implements
    SMFByteBufferFloat4Type
  {
    private final ByteBuffer buffer;

    Float4b16(
      final ByteBuffer in_buffer,
      final JPRACursorByteReadableType in_cursor,
      final int in_size,
      final int in_offset)
    {
      super(in_cursor, in_size, in_offset);
      this.buffer = Objects.requireNonNull(in_buffer, "Buffer");
    }

    @Override
    public void set4D(
      final double x,
      final double y,
      final double z,
      final double w)
    {
      final long index = this.currentOffset();
      final int index_0 = Math.toIntExact(index);
      this.buffer.putChar(index_0, Binary16.packDouble(x));
      final int index_1 = Math.toIntExact(Math.addExact(index, 2L));
      this.buffer.putChar(index_1, Binary16.packDouble(y));
      final int index_2 = Math.toIntExact(Math.addExact(index, 4L));
      this.buffer.putChar(index_2, Binary16.packDouble(z));
      final int index_3 = Math.toIntExact(Math.addExact(index, 6L));
      this.buffer.putChar(index_3, Binary16.packDouble(w));
    }

    @Override
    public void get4D(final VectorMutable4D v)
    {
      final long index = this.currentOffset();
      final int index_0 = Math.toIntExact(index);
      final double x = Binary16.unpackDouble(this.buffer.getChar(index_0));
      final int index_1 = Math.toIntExact(Math.addExact(index, 2L));
      final double y = Binary16.unpackDouble(this.buffer.getChar(index_1));
      final int index_2 = Math.toIntExact(Math.addExact(index, 4L));
      final double z = Binary16.unpackDouble(this.buffer.getChar(index_2));
      final int index_3 = Math.toIntExact(Math.addExact(index, 6L));
      final double w = Binary16.unpackDouble(this.buffer.getChar(index_3));
      v.setXYZW(x, y, z, w);
    }
  }

  static final class Float4b32 extends SMFByteBufferCursor implements
    SMFByteBufferFloat4Type
  {
    private final ByteBuffer buffer;

    Float4b32(
      final ByteBuffer in_buffer,
      final JPRACursorByteReadableType in_cursor,
      final int in_size,
      final int in_offset)
    {
      super(in_cursor, in_size, in_offset);
      this.buffer = Objects.requireNonNull(in_buffer, "Buffer");
    }

    @Override
    public void set4D(
      final double x,
      final double y,
      final double z,
      final double w)
    {
      final long index = this.currentOffset();
      final int index_0 = Math.toIntExact(index);
      this.buffer.putFloat(index_0, (float) x);
      final int index_1 = Math.toIntExact(Math.addExact(index, 4L));
      this.buffer.putFloat(index_1, (float) y);
      final int index_2 = Math.toIntExact(Math.addExact(index, 8L));
      this.buffer.putFloat(index_2, (float) z);
      final int index_3 = Math.toIntExact(Math.addExact(index, 12L));
      this.buffer.putFloat(index_3, (float) w);
    }

    @Override
    public void get4D(final VectorMutable4D v)
    {
      final long index = this.currentOffset();
      final int index_0 = Math.toIntExact(index);
      final double x = (double) this.buffer.getFloat(index_0);
      final int index_1 = Math.toIntExact(Math.addExact(index, 4L));
      final double y = (double) this.buffer.getFloat(index_1);
      final int index_2 = Math.toIntExact(Math.addExact(index, 8L));
      final double z = (double) this.buffer.getFloat(index_2);
      final int index_3 = Math.toIntExact(Math.addExact(index, 12L));
      final double w = (double) this.buffer.getFloat(index_3);
      v.setXYZW(x, y, z, w);
    }
  }

  static final class Float4b64 extends SMFByteBufferCursor implements
    SMFByteBufferFloat4Type
  {
    private final ByteBuffer buffer;

    Float4b64(
      final ByteBuffer in_buffer,
      final JPRACursorByteReadableType in_cursor,
      final int in_size,
      final int in_offset)
    {
      super(in_cursor, in_size, in_offset);
      this.buffer = Objects.requireNonNull(in_buffer, "Buffer");
    }

    @Override
    public void set4D(
      final double x,
      final double y,
      final double z,
      final double w)
    {
      final long index = this.currentOffset();
      final int index_0 = Math.toIntExact(index);
      this.buffer.putDouble(index_0, x);
      final int index_1 = Math.toIntExact(Math.addExact(index, 8L));
      this.buffer.putDouble(index_1, y);
      final int index_2 = Math.toIntExact(Math.addExact(index, 16L));
      this.buffer.putDouble(index_2, z);
      final int index_3 = Math.toIntExact(Math.addExact(index, 24L));
      this.buffer.putDouble(index_3, w);
    }

    @Override
    public void get4D(final VectorMutable4D v)
    {
      final long index = this.currentOffset();
      final int index_0 = Math.toIntExact(index);
      final double x = this.buffer.getDouble(index_0);
      final int index_1 = Math.toIntExact(Math.addExact(index, 8L));
      final double y = this.buffer.getDouble(index_1);
      final int index_2 = Math.toIntExact(Math.addExact(index, 16L));
      final double z = this.buffer.getDouble(index_2);
      final int index_3 = Math.toIntExact(Math.addExact(index, 24L));
      final double w = this.buffer.getDouble(index_3);
      v.setXYZW(x, y, z, w);
    }
  }

  static final class Float3b64 extends SMFByteBufferCursor implements
    SMFByteBufferFloat3Type
  {
    private final ByteBuffer buffer;

    Float3b64(
      final ByteBuffer in_buffer,
      final JPRACursorByteReadableType in_cursor,
      final int in_size,
      final int in_offset)
    {
      super(in_cursor, in_size, in_offset);
      this.buffer = Objects.requireNonNull(in_buffer, "Buffer");
    }

    @Override
    public void set3D(
      final double x,
      final double y,
      final double z)
    {
      final long index = this.currentOffset();
      final int index_0 = Math.toIntExact(index);
      this.buffer.putDouble(index_0, x);
      final int index_1 = Math.toIntExact(Math.addExact(index, 8L));
      this.buffer.putDouble(index_1, y);
      final int index_2 = Math.toIntExact(Math.addExact(index, 16L));
      this.buffer.putDouble(index_2, z);
    }

    @Override
    public void get3D(final VectorMutable3D v)
    {
      final long index = this.currentOffset();
      final int index_0 = Math.toIntExact(index);
      final double x = this.buffer.getDouble(index_0);
      final int index_1 = Math.toIntExact(Math.addExact(index, 8L));
      final double y = this.buffer.getDouble(index_1);
      final int index_2 = Math.toIntExact(Math.addExact(index, 16L));
      final double z = this.buffer.getDouble(index_2);
      v.setXYZ(x, y, z);
    }
  }

  static final class Float3b32 extends SMFByteBufferCursor implements
    SMFByteBufferFloat3Type
  {
    private final ByteBuffer buffer;

    Float3b32(
      final ByteBuffer in_buffer,
      final JPRACursorByteReadableType in_cursor,
      final int in_size,
      final int in_offset)
    {
      super(in_cursor, in_size, in_offset);
      this.buffer = Objects.requireNonNull(in_buffer, "Buffer");
    }

    @Override
    public void set3D(
      final double x,
      final double y,
      final double z)
    {
      final long index = this.currentOffset();
      final int index_0 = Math.toIntExact(index);
      this.buffer.putFloat(index_0, (float) x);
      final int index_1 = Math.toIntExact(Math.addExact(index, 4L));
      this.buffer.putFloat(index_1, (float) y);
      final int index_2 = Math.toIntExact(Math.addExact(index, 8L));
      this.buffer.putFloat(index_2, (float) z);
    }

    @Override
    public void get3D(final VectorMutable3D v)
    {
      final long index = this.currentOffset();
      final int index_0 = Math.toIntExact(index);
      final double x = (double) this.buffer.getFloat(index_0);
      final int index_1 = Math.toIntExact(Math.addExact(index, 4L));
      final double y = (double) this.buffer.getFloat(index_1);
      final int index_2 = Math.toIntExact(Math.addExact(index, 8L));
      final double z = (double) this.buffer.getFloat(index_2);
      v.setXYZ(x, y, z);
    }
  }

  static final class Float3b16 extends SMFByteBufferCursor implements
    SMFByteBufferFloat3Type
  {
    private final ByteBuffer buffer;

    Float3b16(
      final ByteBuffer in_buffer,
      final JPRACursorByteReadableType in_cursor,
      final int in_size,
      final int in_offset)
    {
      super(in_cursor, in_size, in_offset);
      this.buffer = Objects.requireNonNull(in_buffer, "Buffer");
    }

    @Override
    public void set3D(
      final double x,
      final double y,
      final double z)
    {
      final long index = this.currentOffset();
      final int index_0 = Math.toIntExact(index);
      this.buffer.putChar(index_0, Binary16.packDouble(x));
      final int index_1 = Math.toIntExact(Math.addExact(index, 2L));
      this.buffer.putChar(index_1, Binary16.packDouble(y));
      final int index_2 = Math.toIntExact(Math.addExact(index, 4L));
      this.buffer.putChar(index_2, Binary16.packDouble(z));
    }

    @Override
    public void get3D(final VectorMutable3D v)
    {
      final long index = this.currentOffset();
      final int index_0 = Math.toIntExact(index);
      final double x = Binary16.unpackDouble(this.buffer.getChar(index_0));
      final int index_1 = Math.toIntExact(Math.addExact(index, 2L));
      final double y = Binary16.unpackDouble(this.buffer.getChar(index_1));
      final int index_2 = Math.toIntExact(Math.addExact(index, 4L));
      final double z = Binary16.unpackDouble(this.buffer.getChar(index_2));
      v.setXYZ(x, y, z);
    }
  }

  static final class Float2b64 extends SMFByteBufferCursor implements
    SMFByteBufferFloat2Type
  {
    private final ByteBuffer buffer;

    Float2b64(
      final ByteBuffer in_buffer,
      final JPRACursorByteReadableType in_cursor,
      final int in_size,
      final int in_offset)
    {
      super(in_cursor, in_size, in_offset);
      this.buffer = Objects.requireNonNull(in_buffer, "Buffer");
    }

    @Override
    public void set2D(
      final double x,
      final double y)
    {
      final long index = this.currentOffset();
      final int index_0 = Math.toIntExact(index);
      this.buffer.putDouble(index_0, x);
      final int index_1 = Math.toIntExact(Math.addExact(index, 8L));
      this.buffer.putDouble(index_1, y);
    }

    @Override
    public void get2D(final VectorMutable2D v)
    {
      final long index = this.currentOffset();
      final int index_0 = Math.toIntExact(index);
      final double x = this.buffer.getDouble(index_0);
      final int index_1 = Math.toIntExact(Math.addExact(index, 8L));
      final double y = this.buffer.getDouble(index_1);
      v.setXY(x, y);
    }
  }

  static final class Float2b32 extends SMFByteBufferCursor implements
    SMFByteBufferFloat2Type
  {
    private final ByteBuffer buffer;

    Float2b32(
      final ByteBuffer in_buffer,
      final JPRACursorByteReadableType in_cursor,
      final int in_size,
      final int in_offset)
    {
      super(in_cursor, in_size, in_offset);
      this.buffer = Objects.requireNonNull(in_buffer, "Buffer");
    }

    @Override
    public void set2D(
      final double x,
      final double y)
    {
      final long index = this.currentOffset();
      final int index_0 = Math.toIntExact(index);
      this.buffer.putFloat(index_0, (float) x);
      final int index_1 = Math.toIntExact(Math.addExact(index, 4L));
      this.buffer.putFloat(index_1, (float) y);
    }

    @Override
    public void get2D(final VectorMutable2D v)
    {
      final long index = this.currentOffset();
      final int index_0 = Math.toIntExact(index);
      final double x = (double) this.buffer.getFloat(index_0);
      final int index_1 = Math.toIntExact(Math.addExact(index, 4L));
      final double y = (double) this.buffer.getFloat(index_1);
      v.setXY(x, y);
    }
  }

  static final class Float2b16 extends SMFByteBufferCursor implements
    SMFByteBufferFloat2Type
  {
    private final ByteBuffer buffer;

    Float2b16(
      final ByteBuffer in_buffer,
      final JPRACursorByteReadableType in_cursor,
      final int in_size,
      final int in_offset)
    {
      super(in_cursor, in_size, in_offset);
      this.buffer = Objects.requireNonNull(in_buffer, "Buffer");
    }

    @Override
    public void set2D(
      final double x,
      final double y)
    {
      final long index = this.currentOffset();
      final int index_0 = Math.toIntExact(index);
      this.buffer.putChar(index_0, Binary16.packDouble(x));
      final int index_1 = Math.toIntExact(Math.addExact(index, 2L));
      this.buffer.putChar(index_1, Binary16.packDouble(y));
    }

    @Override
    public void get2D(final VectorMutable2D v)
    {
      final long index = this.currentOffset();
      final int index_0 = Math.toIntExact(index);
      final double x = Binary16.unpackDouble(this.buffer.getChar(index_0));
      final int index_1 = Math.toIntExact(Math.addExact(index, 2L));
      final double y = Binary16.unpackDouble(this.buffer.getChar(index_1));
      v.setXY(x, y);
    }
  }

  static final class Float1b64 extends SMFByteBufferCursor implements
    SMFByteBufferFloat1Type
  {
    private final ByteBuffer buffer;

    Float1b64(
      final ByteBuffer in_buffer,
      final JPRACursorByteReadableType in_cursor,
      final int in_size,
      final int in_offset)
    {
      super(in_cursor, in_size, in_offset);
      this.buffer = Objects.requireNonNull(in_buffer, "Buffer");
    }

    @Override
    public double get1D()
    {
      final long index = this.currentOffset();
      final int index_0 = Math.toIntExact(index);
      return this.buffer.getDouble(index_0);
    }

    @Override
    public void set1D(
      final double x)
    {
      final long index = this.currentOffset();
      final int index_0 = Math.toIntExact(index);
      this.buffer.putDouble(index_0, x);
    }
  }

  static final class Float1b32 extends SMFByteBufferCursor implements
    SMFByteBufferFloat1Type
  {
    private final ByteBuffer buffer;

    Float1b32(
      final ByteBuffer in_buffer,
      final JPRACursorByteReadableType in_cursor,
      final int in_size,
      final int in_offset)
    {
      super(in_cursor, in_size, in_offset);
      this.buffer = Objects.requireNonNull(in_buffer, "Buffer");
    }

    @Override
    public double get1D()
    {
      final long index = this.currentOffset();
      final int index_0 = Math.toIntExact(index);
      return (double) this.buffer.getFloat(index_0);
    }

    @Override
    public void set1D(
      final double x)
    {
      final long index = this.currentOffset();
      final int index_0 = Math.toIntExact(index);
      this.buffer.putFloat(index_0, (float) x);
    }
  }

  static final class Float1b16 extends SMFByteBufferCursor implements
    SMFByteBufferFloat1Type
  {
    private final ByteBuffer buffer;

    Float1b16(
      final ByteBuffer in_buffer,
      final JPRACursorByteReadableType in_cursor,
      final int in_size,
      final int in_offset)
    {
      super(in_cursor, in_size, in_offset);
      this.buffer = Objects.requireNonNull(in_buffer, "Buffer");
    }

    @Override
    public double get1D()
    {
      final long index = this.currentOffset();
      final int index_0 = Math.toIntExact(index);
      return Binary16.unpackDouble(this.buffer.getChar(index_0));
    }

    @Override
    public void set1D(
      final double x)
    {
      final long index = this.currentOffset();
      final int index_0 = Math.toIntExact(index);
      this.buffer.putChar(index_0, Binary16.packDouble(x));
    }
  }
}
