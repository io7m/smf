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

import java.util.Objects;
import com.io7m.jpra.runtime.java.JPRACursorByteReadableType;
import com.io7m.jtensors.storage.heap.VectorMutable2L;
import com.io7m.jtensors.storage.heap.VectorMutable3L;
import com.io7m.jtensors.storage.heap.VectorMutable4L;
import com.io7m.junreachable.UnreachableCodeException;

import java.nio.ByteBuffer;

final class SMFByteBufferCursorsSigned
{
  private SMFByteBufferCursorsSigned()
  {
    throw new UnreachableCodeException();
  }

  static final class Signed4b64 extends SMFByteBufferCursor implements
    SMFByteBufferIntegerSigned4Type
  {
    private final ByteBuffer buffer;

    Signed4b64(
      final ByteBuffer in_buffer,
      final JPRACursorByteReadableType in_cursor,
      final int in_size,
      final int in_offset)
    {
      super(in_cursor, in_size, in_offset);
      this.buffer = Objects.requireNonNull(in_buffer, "Buffer");
    }

    @Override
    public void get4SL(
      final VectorMutable4L v)
    {
      final long index = this.currentOffset();
      final int index_0 = Math.toIntExact(index);
      final long x = this.buffer.getLong(index_0);
      final int index_1 = Math.toIntExact(Math.addExact(index, 8L));
      final long y = this.buffer.getLong(index_1);
      final int index_2 = Math.toIntExact(Math.addExact(index, 16L));
      final long z = this.buffer.getLong(index_2);
      final int index_3 = Math.toIntExact(Math.addExact(index, 24L));
      final long w = this.buffer.getLong(index_3);
      v.setXYZW(x, y, z, w);
    }

    @Override
    public void set4SL(
      final long x,
      final long y,
      final long z,
      final long w)
    {
      final long index = this.currentOffset();
      final int index_0 = Math.toIntExact(index);
      this.buffer.putLong(index_0, x);
      final int index_1 = Math.toIntExact(Math.addExact(index, 8L));
      this.buffer.putLong(index_1, y);
      final int index_2 = Math.toIntExact(Math.addExact(index, 16L));
      this.buffer.putLong(index_2, z);
      final int index_3 = Math.toIntExact(Math.addExact(index, 24L));
      this.buffer.putLong(index_3, w);
    }
  }

  static final class Signed3b64 extends SMFByteBufferCursor implements
    SMFByteBufferIntegerSigned3Type
  {
    private final ByteBuffer buffer;

    Signed3b64(
      final ByteBuffer in_buffer,
      final JPRACursorByteReadableType in_cursor,
      final int in_size,
      final int in_offset)
    {
      super(in_cursor, in_size, in_offset);
      this.buffer = Objects.requireNonNull(in_buffer, "Buffer");
    }

    @Override
    public void get3SL(
      final VectorMutable3L v)
    {
      final long index = this.currentOffset();
      final int index_0 = Math.toIntExact(index);
      final long x = this.buffer.getLong(index_0);
      final int index_1 = Math.toIntExact(Math.addExact(index, 8L));
      final long y = this.buffer.getLong(index_1);
      final int index_2 = Math.toIntExact(Math.addExact(index, 16L));
      final long z = this.buffer.getLong(index_2);
      v.setXYZ(x, y, z);
    }

    @Override
    public void set3SL(
      final long x,
      final long y,
      final long z)
    {
      final long index = this.currentOffset();
      final int index_0 = Math.toIntExact(index);
      this.buffer.putLong(index_0, x);
      final int index_1 = Math.toIntExact(Math.addExact(index, 8L));
      this.buffer.putLong(index_1, y);
      final int index_2 = Math.toIntExact(Math.addExact(index, 16L));
      this.buffer.putLong(index_2, z);
    }
  }

  static final class Signed2b64 extends SMFByteBufferCursor implements
    SMFByteBufferIntegerSigned2Type
  {
    private final ByteBuffer buffer;

    Signed2b64(
      final ByteBuffer in_buffer,
      final JPRACursorByteReadableType in_cursor,
      final int in_size,
      final int in_offset)
    {
      super(in_cursor, in_size, in_offset);
      this.buffer = Objects.requireNonNull(in_buffer, "Buffer");
    }

    @Override
    public void get2SL(
      final VectorMutable2L v)
    {
      final long index = this.currentOffset();
      final int index_0 = Math.toIntExact(index);
      final long x = this.buffer.getLong(index_0);
      final int index_1 = Math.toIntExact(Math.addExact(index, 8L));
      final long y = this.buffer.getLong(index_1);
      v.setXY(x, y);
    }

    @Override
    public void set2SL(
      final long x,
      final long y)
    {
      final long index = this.currentOffset();
      final int index_0 = Math.toIntExact(index);
      this.buffer.putLong(index_0, x);
      final int index_1 = Math.toIntExact(Math.addExact(index, 8L));
      this.buffer.putLong(index_1, y);
    }
  }

  static final class Signed1b64 extends SMFByteBufferCursor implements
    SMFByteBufferIntegerSigned1Type
  {
    private final ByteBuffer buffer;

    Signed1b64(
      final ByteBuffer in_buffer,
      final JPRACursorByteReadableType in_cursor,
      final int in_size,
      final int in_offset)
    {
      super(in_cursor, in_size, in_offset);
      this.buffer = Objects.requireNonNull(in_buffer, "Buffer");
    }

    @Override
    public long get1SL()
    {
      final long index = this.currentOffset();
      final int index_0 = Math.toIntExact(index);
      return this.buffer.getLong(index_0);
    }

    @Override
    public void set1SL(
      final long x)
    {
      final long index = this.currentOffset();
      final int index_0 = Math.toIntExact(index);
      this.buffer.putLong(index_0, x);
    }
  }

  static final class Signed4b32 extends SMFByteBufferCursor implements
    SMFByteBufferIntegerSigned4Type
  {
    private final ByteBuffer buffer;

    Signed4b32(
      final ByteBuffer in_buffer,
      final JPRACursorByteReadableType in_cursor,
      final int in_size,
      final int in_offset)
    {
      super(in_cursor, in_size, in_offset);
      this.buffer = Objects.requireNonNull(in_buffer, "Buffer");
    }

    @Override
    public void get4SL(
      final VectorMutable4L v)
    {
      final long index = this.currentOffset();
      final int index_0 = Math.toIntExact(index);
      final long x = (long) this.buffer.getInt(index_0);
      final int index_1 = Math.toIntExact(Math.addExact(index, 4L));
      final long y = (long) this.buffer.getInt(index_1);
      final int index_2 = Math.toIntExact(Math.addExact(index, 8L));
      final long z = (long) this.buffer.getInt(index_2);
      final int index_3 = Math.toIntExact(Math.addExact(index, 12L));
      final long w = (long) this.buffer.getInt(index_3);
      v.setXYZW(x, y, z, w);
    }

    @Override
    public void set4SL(
      final long x,
      final long y,
      final long z,
      final long w)
    {
      final long index = this.currentOffset();
      final int index_0 = Math.toIntExact(index);
      this.buffer.putInt(index_0, (int) x);
      final int index_1 = Math.toIntExact(Math.addExact(index, 4L));
      this.buffer.putInt(index_1, (int) y);
      final int index_2 = Math.toIntExact(Math.addExact(index, 8L));
      this.buffer.putInt(index_2, (int) z);
      final int index_3 = Math.toIntExact(Math.addExact(index, 12L));
      this.buffer.putInt(index_3, (int) w);
    }
  }

  static final class Signed3b32 extends SMFByteBufferCursor implements
    SMFByteBufferIntegerSigned3Type
  {
    private final ByteBuffer buffer;

    Signed3b32(
      final ByteBuffer in_buffer,
      final JPRACursorByteReadableType in_cursor,
      final int in_size,
      final int in_offset)
    {
      super(in_cursor, in_size, in_offset);
      this.buffer = Objects.requireNonNull(in_buffer, "Buffer");
    }

    @Override
    public void get3SL(
      final VectorMutable3L v)
    {
      final long index = this.currentOffset();
      final int index_0 = Math.toIntExact(index);
      final long x = (long) this.buffer.getInt(index_0);
      final int index_1 = Math.toIntExact(Math.addExact(index, 4L));
      final long y = (long) this.buffer.getInt(index_1);
      final int index_2 = Math.toIntExact(Math.addExact(index, 8L));
      final long z = (long) this.buffer.getInt(index_2);
      v.setXYZ(x, y, z);
    }

    @Override
    public void set3SL(
      final long x,
      final long y,
      final long z)
    {
      final long index = this.currentOffset();
      final int index_0 = Math.toIntExact(index);
      this.buffer.putInt(index_0, (int) x);
      final int index_1 = Math.toIntExact(Math.addExact(index, 4L));
      this.buffer.putInt(index_1, (int) y);
      final int index_2 = Math.toIntExact(Math.addExact(index, 8L));
      this.buffer.putInt(index_2, (int) z);
    }
  }

  static final class Signed2b32 extends SMFByteBufferCursor implements
    SMFByteBufferIntegerSigned2Type
  {
    private final ByteBuffer buffer;

    Signed2b32(
      final ByteBuffer in_buffer,
      final JPRACursorByteReadableType in_cursor,
      final int in_size,
      final int in_offset)
    {
      super(in_cursor, in_size, in_offset);
      this.buffer = Objects.requireNonNull(in_buffer, "Buffer");
    }

    @Override
    public void get2SL(
      final VectorMutable2L v)
    {
      final long index = this.currentOffset();
      final int index_0 = Math.toIntExact(index);
      final long x = (long) this.buffer.getInt(index_0);
      final int index_1 = Math.toIntExact(Math.addExact(index, 4L));
      final long y = (long) this.buffer.getInt(index_1);
      v.setXY(x, y);
    }

    @Override
    public void set2SL(
      final long x,
      final long y)
    {
      final long index = this.currentOffset();
      final int index_0 = Math.toIntExact(index);
      this.buffer.putInt(index_0, (int) x);
      final int index_1 = Math.toIntExact(Math.addExact(index, 4L));
      this.buffer.putInt(index_1, (int) y);
    }
  }

  static final class Signed1b32 extends SMFByteBufferCursor implements
    SMFByteBufferIntegerSigned1Type
  {
    private final ByteBuffer buffer;

    Signed1b32(
      final ByteBuffer in_buffer,
      final JPRACursorByteReadableType in_cursor,
      final int in_size,
      final int in_offset)
    {
      super(in_cursor, in_size, in_offset);
      this.buffer = Objects.requireNonNull(in_buffer, "Buffer");
    }

    @Override
    public long get1SL()
    {
      final long index = this.currentOffset();
      final int index_0 = Math.toIntExact(index);
      return (long) this.buffer.getInt(index_0);
    }

    @Override
    public void set1SL(
      final long x)
    {
      final long index = this.currentOffset();
      final int index_0 = Math.toIntExact(index);
      this.buffer.putInt(index_0, (int) x);
    }
  }

  static final class Signed4b16 extends SMFByteBufferCursor implements
    SMFByteBufferIntegerSigned4Type
  {
    private final ByteBuffer buffer;

    Signed4b16(
      final ByteBuffer in_buffer,
      final JPRACursorByteReadableType in_cursor,
      final int in_size,
      final int in_offset)
    {
      super(in_cursor, in_size, in_offset);
      this.buffer = Objects.requireNonNull(in_buffer, "Buffer");
    }

    @Override
    public void get4SL(
      final VectorMutable4L v)
    {
      final long index = this.currentOffset();
      final int index_0 = Math.toIntExact(index);
      final long x = (long) this.buffer.getShort(index_0);
      final int index_1 = Math.toIntExact(Math.addExact(index, 2L));
      final long y = (long) this.buffer.getShort(index_1);
      final int index_2 = Math.toIntExact(Math.addExact(index, 4L));
      final long z = (long) this.buffer.getShort(index_2);
      final int index_3 = Math.toIntExact(Math.addExact(index, 6L));
      final long w = (long) this.buffer.getShort(index_3);
      v.setXYZW(x, y, z, w);
    }

    @Override
    public void set4SL(
      final long x,
      final long y,
      final long z,
      final long w)
    {
      final long index = this.currentOffset();
      final int index_0 = Math.toIntExact(index);
      this.buffer.putShort(index_0, (short) x);
      final int index_1 = Math.toIntExact(Math.addExact(index, 2L));
      this.buffer.putShort(index_1, (short) y);
      final int index_2 = Math.toIntExact(Math.addExact(index, 4L));
      this.buffer.putShort(index_2, (short) z);
      final int index_3 = Math.toIntExact(Math.addExact(index, 6L));
      this.buffer.putShort(index_3, (short) w);
    }
  }

  static final class Signed3b16 extends SMFByteBufferCursor implements
    SMFByteBufferIntegerSigned3Type
  {
    private final ByteBuffer buffer;

    Signed3b16(
      final ByteBuffer in_buffer,
      final JPRACursorByteReadableType in_cursor,
      final int in_size,
      final int in_offset)
    {
      super(in_cursor, in_size, in_offset);
      this.buffer = Objects.requireNonNull(in_buffer, "Buffer");
    }

    @Override
    public void get3SL(
      final VectorMutable3L v)
    {
      final long index = this.currentOffset();
      final int index_0 = Math.toIntExact(index);
      final long x = (long) this.buffer.getShort(index_0);
      final int index_1 = Math.toIntExact(Math.addExact(index, 2L));
      final long y = (long) this.buffer.getShort(index_1);
      final int index_2 = Math.toIntExact(Math.addExact(index, 4L));
      final long z = (long) this.buffer.getShort(index_2);
      v.setXYZ(x, y, z);
    }

    @Override
    public void set3SL(
      final long x,
      final long y,
      final long z)
    {
      final long index = this.currentOffset();
      final int index_0 = Math.toIntExact(index);
      this.buffer.putShort(index_0, (short) x);
      final int index_1 = Math.toIntExact(Math.addExact(index, 2L));
      this.buffer.putShort(index_1, (short) y);
      final int index_2 = Math.toIntExact(Math.addExact(index, 4L));
      this.buffer.putShort(index_2, (short) z);
    }
  }

  static final class Signed2b16 extends SMFByteBufferCursor implements
    SMFByteBufferIntegerSigned2Type
  {
    private final ByteBuffer buffer;

    Signed2b16(
      final ByteBuffer in_buffer,
      final JPRACursorByteReadableType in_cursor,
      final int in_size,
      final int in_offset)
    {
      super(in_cursor, in_size, in_offset);
      this.buffer = Objects.requireNonNull(in_buffer, "Buffer");
    }

    @Override
    public void get2SL(
      final VectorMutable2L v)
    {
      final long index = this.currentOffset();
      final int index_0 = Math.toIntExact(index);
      final long x = (long) this.buffer.getShort(index_0);
      final int index_1 = Math.toIntExact(Math.addExact(index, 2L));
      final long y = (long) this.buffer.getShort(index_1);
      v.setXY(x, y);
    }

    @Override
    public void set2SL(
      final long x,
      final long y)
    {
      final long index = this.currentOffset();
      final int index_0 = Math.toIntExact(index);
      this.buffer.putShort(index_0, (short) x);
      final int index_1 = Math.toIntExact(Math.addExact(index, 2L));
      this.buffer.putShort(index_1, (short) y);
    }
  }

  static final class Signed1b16 extends SMFByteBufferCursor implements
    SMFByteBufferIntegerSigned1Type
  {
    private final ByteBuffer buffer;

    Signed1b16(
      final ByteBuffer in_buffer,
      final JPRACursorByteReadableType in_cursor,
      final int in_size,
      final int in_offset)
    {
      super(in_cursor, in_size, in_offset);
      this.buffer = Objects.requireNonNull(in_buffer, "Buffer");
    }

    @Override
    public long get1SL()
    {
      final long index = this.currentOffset();
      final int index_0 = Math.toIntExact(index);
      return (long) this.buffer.getShort(index_0);
    }

    @Override
    public void set1SL(
      final long x)
    {
      final long index = this.currentOffset();
      final int index_0 = Math.toIntExact(index);
      this.buffer.putShort(index_0, (short) x);
    }
  }

  static final class Signed4b8 extends SMFByteBufferCursor implements
    SMFByteBufferIntegerSigned4Type
  {
    private final ByteBuffer buffer;

    Signed4b8(
      final ByteBuffer in_buffer,
      final JPRACursorByteReadableType in_cursor,
      final int in_size,
      final int in_offset)
    {
      super(in_cursor, in_size, in_offset);
      this.buffer = Objects.requireNonNull(in_buffer, "Buffer");
    }

    @Override
    public void get4SL(
      final VectorMutable4L v)
    {
      final long index = this.currentOffset();
      final int index_0 = Math.toIntExact(index);
      final long x = (long) this.buffer.get(index_0);
      final int index_1 = Math.toIntExact(Math.addExact(index, 1L));
      final long y = (long) this.buffer.get(index_1);
      final int index_2 = Math.toIntExact(Math.addExact(index, 2L));
      final long z = (long) this.buffer.get(index_2);
      final int index_3 = Math.toIntExact(Math.addExact(index, 3L));
      final long w = (long) this.buffer.get(index_3);
      v.setXYZW(x, y, z, w);
    }

    @Override
    public void set4SL(
      final long x,
      final long y,
      final long z,
      final long w)
    {
      final long index = this.currentOffset();
      final int index_0 = Math.toIntExact(index);
      this.buffer.put(index_0, (byte) x);
      final int index_1 = Math.toIntExact(Math.addExact(index, 1L));
      this.buffer.put(index_1, (byte) y);
      final int index_2 = Math.toIntExact(Math.addExact(index, 2L));
      this.buffer.put(index_2, (byte) z);
      final int index_3 = Math.toIntExact(Math.addExact(index, 3L));
      this.buffer.put(index_3, (byte) w);
    }
  }

  static final class Signed3b8 extends SMFByteBufferCursor implements
    SMFByteBufferIntegerSigned3Type
  {
    private final ByteBuffer buffer;

    Signed3b8(
      final ByteBuffer in_buffer,
      final JPRACursorByteReadableType in_cursor,
      final int in_size,
      final int in_offset)
    {
      super(in_cursor, in_size, in_offset);
      this.buffer = Objects.requireNonNull(in_buffer, "Buffer");
    }

    @Override
    public void get3SL(
      final VectorMutable3L v)
    {
      final long index = this.currentOffset();
      final int index_0 = Math.toIntExact(index);
      final long x = (long) this.buffer.get(index_0);
      final int index_1 = Math.toIntExact(Math.addExact(index, 1L));
      final long y = (long) this.buffer.get(index_1);
      final int index_2 = Math.toIntExact(Math.addExact(index, 2L));
      final long z = (long) this.buffer.get(index_2);
      v.setXYZ(x, y, z);
    }

    @Override
    public void set3SL(
      final long x,
      final long y,
      final long z)
    {
      final long index = this.currentOffset();
      final int index_0 = Math.toIntExact(index);
      this.buffer.put(index_0, (byte) x);
      final int index_1 = Math.toIntExact(Math.addExact(index, 1L));
      this.buffer.put(index_1, (byte) y);
      final int index_2 = Math.toIntExact(Math.addExact(index, 2L));
      this.buffer.put(index_2, (byte) z);
    }
  }

  static final class Signed2b8 extends SMFByteBufferCursor implements
    SMFByteBufferIntegerSigned2Type
  {
    private final ByteBuffer buffer;

    Signed2b8(
      final ByteBuffer in_buffer,
      final JPRACursorByteReadableType in_cursor,
      final int in_size,
      final int in_offset)
    {
      super(in_cursor, in_size, in_offset);
      this.buffer = Objects.requireNonNull(in_buffer, "Buffer");
    }

    @Override
    public void get2SL(
      final VectorMutable2L v)
    {
      final long index = this.currentOffset();
      final int index_0 = Math.toIntExact(index);
      final long x = (long) this.buffer.get(index_0);
      final int index_1 = Math.toIntExact(Math.addExact(index, 1L));
      final long y = (long) this.buffer.get(index_1);
      v.setXY(x, y);
    }

    @Override
    public void set2SL(
      final long x,
      final long y)
    {
      final long index = this.currentOffset();
      final int index_0 = Math.toIntExact(index);
      this.buffer.put(index_0, (byte) x);
      final int index_1 = Math.toIntExact(Math.addExact(index, 1L));
      this.buffer.put(index_1, (byte) y);
    }
  }

  static final class Signed1b8 extends SMFByteBufferCursor implements
    SMFByteBufferIntegerSigned1Type
  {
    private final ByteBuffer buffer;

    Signed1b8(
      final ByteBuffer in_buffer,
      final JPRACursorByteReadableType in_cursor,
      final int in_size,
      final int in_offset)
    {
      super(in_cursor, in_size, in_offset);
      this.buffer = Objects.requireNonNull(in_buffer, "Buffer");
    }

    @Override
    public long get1SL()
    {
      final long index = this.currentOffset();
      final int index_0 = Math.toIntExact(index);
      return (long) this.buffer.get(index_0);
    }

    @Override
    public void set1SL(
      final long x)
    {
      final long index = this.currentOffset();
      final int index_0 = Math.toIntExact(index);
      this.buffer.put(index_0, (byte) x);
    }
  }
}
