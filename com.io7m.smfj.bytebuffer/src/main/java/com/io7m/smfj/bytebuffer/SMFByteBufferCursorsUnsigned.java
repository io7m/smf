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
import java.util.Objects;
import com.io7m.jpra.runtime.java.JPRACursorByteReadableType;
import com.io7m.jtensors.storage.heap.VectorMutable2L;
import com.io7m.jtensors.storage.heap.VectorMutable3L;
import com.io7m.jtensors.storage.heap.VectorMutable4L;
import com.io7m.junreachable.UnreachableCodeException;

import java.nio.ByteBuffer;

final class SMFByteBufferCursorsUnsigned
{
  private SMFByteBufferCursorsUnsigned()
  {
    throw new UnreachableCodeException();
  }

  static final class Unsigned4b64 extends SMFByteBufferCursor implements
    SMFByteBufferIntegerUnsigned4Type
  {
    private final ByteBuffer buffer;

    Unsigned4b64(
      final ByteBuffer in_buffer,
      final JPRACursorByteReadableType in_cursor,
      final int in_size,
      final int in_offset)
    {
      super(in_cursor, in_size, in_offset);
      this.buffer = Objects.requireNonNull(in_buffer, "Buffer");
    }

    @Override
    public void get4UL(
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
    public void set4UL(
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

  static final class Unsigned3b64 extends SMFByteBufferCursor implements
    SMFByteBufferIntegerUnsigned3Type
  {
    private final ByteBuffer buffer;

    Unsigned3b64(
      final ByteBuffer in_buffer,
      final JPRACursorByteReadableType in_cursor,
      final int in_size,
      final int in_offset)
    {
      super(in_cursor, in_size, in_offset);
      this.buffer = Objects.requireNonNull(in_buffer, "Buffer");
    }

    @Override
    public void get3UL(
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
    public void set3UL(
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

  static final class Unsigned2b64 extends SMFByteBufferCursor implements
    SMFByteBufferIntegerUnsigned2Type
  {
    private final ByteBuffer buffer;

    Unsigned2b64(
      final ByteBuffer in_buffer,
      final JPRACursorByteReadableType in_cursor,
      final int in_size,
      final int in_offset)
    {
      super(in_cursor, in_size, in_offset);
      this.buffer = Objects.requireNonNull(in_buffer, "Buffer");
    }

    @Override
    public void get2UL(
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
    public void set2UL(
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

  static final class Unsigned1b64 extends SMFByteBufferCursor implements
    SMFByteBufferIntegerUnsigned1Type
  {
    private final ByteBuffer buffer;

    Unsigned1b64(
      final ByteBuffer in_buffer,
      final JPRACursorByteReadableType in_cursor,
      final int in_size,
      final int in_offset)
    {
      super(in_cursor, in_size, in_offset);
      this.buffer = Objects.requireNonNull(in_buffer, "Buffer");
    }

    @Override
    public long get1UL()
    {
      final long index = this.currentOffset();
      final int index_0 = Math.toIntExact(index);
      return this.buffer.getLong(index_0);
    }

    @Override
    public void set1UL(
      final long x)
    {
      final long index = this.currentOffset();
      final int index_0 = Math.toIntExact(index);
      this.buffer.putLong(index_0, x);
    }
  }

  static final class Unsigned4b32 extends SMFByteBufferCursor implements
    SMFByteBufferIntegerUnsigned4Type
  {
    private final ByteBuffer buffer;

    Unsigned4b32(
      final ByteBuffer in_buffer,
      final JPRACursorByteReadableType in_cursor,
      final int in_size,
      final int in_offset)
    {
      super(in_cursor, in_size, in_offset);
      this.buffer = Objects.requireNonNull(in_buffer, "Buffer");
    }

    @Override
    public void get4UL(
      final VectorMutable4L v)
    {
      final long index = this.currentOffset();
      final int index_0 = Math.toIntExact(index);
      final long x = Unsigned32.unpackFromBuffer(this.buffer, index_0);
      final int index_1 = Math.toIntExact(Math.addExact(index, 4L));
      final long y = Unsigned32.unpackFromBuffer(this.buffer, index_1);
      final int index_2 = Math.toIntExact(Math.addExact(index, 8L));
      final long z = Unsigned32.unpackFromBuffer(this.buffer, index_2);
      final int index_3 = Math.toIntExact(Math.addExact(index, 12L));
      final long w = Unsigned32.unpackFromBuffer(this.buffer, index_3);
      v.setXYZW(x, y, z, w);
    }

    @Override
    public void set4UL(
      final long x,
      final long y,
      final long z,
      final long w)
    {
      final long index = this.currentOffset();
      final int index_0 = Math.toIntExact(index);
      Unsigned32.packToBuffer(x, this.buffer, index_0);
      final int index_1 = Math.toIntExact(Math.addExact(index, 4L));
      Unsigned32.packToBuffer(y, this.buffer, index_1);
      final int index_2 = Math.toIntExact(Math.addExact(index, 8L));
      Unsigned32.packToBuffer(z, this.buffer, index_2);
      final int index_3 = Math.toIntExact(Math.addExact(index, 12L));
      Unsigned32.packToBuffer(w, this.buffer, index_3);
    }
  }

  static final class Unsigned3b32 extends SMFByteBufferCursor implements
    SMFByteBufferIntegerUnsigned3Type
  {
    private final ByteBuffer buffer;

    Unsigned3b32(
      final ByteBuffer in_buffer,
      final JPRACursorByteReadableType in_cursor,
      final int in_size,
      final int in_offset)
    {
      super(in_cursor, in_size, in_offset);
      this.buffer = Objects.requireNonNull(in_buffer, "Buffer");
    }

    @Override
    public void get3UL(
      final VectorMutable3L v)
    {
      final long index = this.currentOffset();
      final int index_0 = Math.toIntExact(index);
      final long x = Unsigned32.unpackFromBuffer(this.buffer, index_0);
      final int index_1 = Math.toIntExact(Math.addExact(index, 4L));
      final long y = Unsigned32.unpackFromBuffer(this.buffer, index_1);
      final int index_2 = Math.toIntExact(Math.addExact(index, 8L));
      final long z = Unsigned32.unpackFromBuffer(this.buffer, index_2);
      v.setXYZ(x, y, z);
    }

    @Override
    public void set3UL(
      final long x,
      final long y,
      final long z)
    {
      final long index = this.currentOffset();
      final int index_0 = Math.toIntExact(index);
      Unsigned32.packToBuffer(x, this.buffer, index_0);
      final int index_1 = Math.toIntExact(Math.addExact(index, 4L));
      Unsigned32.packToBuffer(y, this.buffer, index_1);
      final int index_2 = Math.toIntExact(Math.addExact(index, 8L));
      Unsigned32.packToBuffer(z, this.buffer, index_2);
    }
  }

  static final class Unsigned2b32 extends SMFByteBufferCursor implements
    SMFByteBufferIntegerUnsigned2Type
  {
    private final ByteBuffer buffer;

    Unsigned2b32(
      final ByteBuffer in_buffer,
      final JPRACursorByteReadableType in_cursor,
      final int in_size,
      final int in_offset)
    {
      super(in_cursor, in_size, in_offset);
      this.buffer = Objects.requireNonNull(in_buffer, "Buffer");
    }

    @Override
    public void get2UL(
      final VectorMutable2L v)
    {
      final long index = this.currentOffset();
      final int index_0 = Math.toIntExact(index);
      final long x = Unsigned32.unpackFromBuffer(this.buffer, index_0);
      final int index_1 = Math.toIntExact(Math.addExact(index, 4L));
      final long y = Unsigned32.unpackFromBuffer(this.buffer, index_1);
      v.setXY(x, y);
    }

    @Override
    public void set2UL(
      final long x,
      final long y)
    {
      final long index = this.currentOffset();
      final int index_0 = Math.toIntExact(index);
      Unsigned32.packToBuffer(x, this.buffer, index_0);
      final int index_1 = Math.toIntExact(Math.addExact(index, 4L));
      Unsigned32.packToBuffer(y, this.buffer, index_1);
    }
  }

  static final class Unsigned1b32 extends SMFByteBufferCursor implements
    SMFByteBufferIntegerUnsigned1Type
  {
    private final ByteBuffer buffer;

    Unsigned1b32(
      final ByteBuffer in_buffer,
      final JPRACursorByteReadableType in_cursor,
      final int in_size,
      final int in_offset)
    {
      super(in_cursor, in_size, in_offset);
      this.buffer = Objects.requireNonNull(in_buffer, "Buffer");
    }

    @Override
    public long get1UL()
    {
      final long index = this.currentOffset();
      final int index_0 = Math.toIntExact(index);
      return Unsigned32.unpackFromBuffer(this.buffer, index_0);
    }

    @Override
    public void set1UL(
      final long x)
    {
      final long index = this.currentOffset();
      final int index_0 = Math.toIntExact(index);
      Unsigned32.packToBuffer(x, this.buffer, index_0);
    }
  }

  static final class Unsigned4b16 extends SMFByteBufferCursor implements
    SMFByteBufferIntegerUnsigned4Type
  {
    private final ByteBuffer buffer;

    Unsigned4b16(
      final ByteBuffer in_buffer,
      final JPRACursorByteReadableType in_cursor,
      final int in_size,
      final int in_offset)
    {
      super(in_cursor, in_size, in_offset);
      this.buffer = Objects.requireNonNull(in_buffer, "Buffer");
    }

    @Override
    public void get4UL(
      final VectorMutable4L v)
    {
      final long index = this.currentOffset();
      final int index_0 = Math.toIntExact(index);
      final long x = (long) Unsigned16.unpackFromBuffer(this.buffer, index_0);
      final int index_1 = Math.toIntExact(Math.addExact(index, 2L));
      final long y = (long) Unsigned16.unpackFromBuffer(this.buffer, index_1);
      final int index_2 = Math.toIntExact(Math.addExact(index, 4L));
      final long z = (long) Unsigned16.unpackFromBuffer(this.buffer, index_2);
      final int index_3 = Math.toIntExact(Math.addExact(index, 6L));
      final long w = (long) Unsigned16.unpackFromBuffer(this.buffer, index_3);
      v.setXYZW(x, y, z, w);
    }

    @Override
    public void set4UL(
      final long x,
      final long y,
      final long z,
      final long w)
    {
      final long index = this.currentOffset();
      final int index_0 = Math.toIntExact(index);
      Unsigned16.packToBuffer((int) x, this.buffer, index_0);
      final int index_1 = Math.toIntExact(Math.addExact(index, 2L));
      Unsigned16.packToBuffer((int) y, this.buffer, index_1);
      final int index_2 = Math.toIntExact(Math.addExact(index, 4L));
      Unsigned16.packToBuffer((int) z, this.buffer, index_2);
      final int index_3 = Math.toIntExact(Math.addExact(index, 6L));
      Unsigned16.packToBuffer((int) w, this.buffer, index_3);
    }
  }

  static final class Unsigned3b16 extends SMFByteBufferCursor implements
    SMFByteBufferIntegerUnsigned3Type
  {
    private final ByteBuffer buffer;

    Unsigned3b16(
      final ByteBuffer in_buffer,
      final JPRACursorByteReadableType in_cursor,
      final int in_size,
      final int in_offset)
    {
      super(in_cursor, in_size, in_offset);
      this.buffer = Objects.requireNonNull(in_buffer, "Buffer");
    }

    @Override
    public void get3UL(
      final VectorMutable3L v)
    {
      final long index = this.currentOffset();
      final int index_0 = Math.toIntExact(index);
      final long x = (long) Unsigned16.unpackFromBuffer(this.buffer, index_0);
      final int index_1 = Math.toIntExact(Math.addExact(index, 2L));
      final long y = (long) Unsigned16.unpackFromBuffer(this.buffer, index_1);
      final int index_2 = Math.toIntExact(Math.addExact(index, 4L));
      final long z = (long) Unsigned16.unpackFromBuffer(this.buffer, index_2);
      v.setXYZ(x, y, z);
    }

    @Override
    public void set3UL(
      final long x,
      final long y,
      final long z)
    {
      final long index = this.currentOffset();
      final int index_0 = Math.toIntExact(index);
      Unsigned16.packToBuffer((int) x, this.buffer, index_0);
      final int index_1 = Math.toIntExact(Math.addExact(index, 2L));
      Unsigned16.packToBuffer((int) y, this.buffer, index_1);
      final int index_2 = Math.toIntExact(Math.addExact(index, 4L));
      Unsigned16.packToBuffer((int) z, this.buffer, index_2);
    }
  }

  static final class Unsigned2b16 extends SMFByteBufferCursor implements
    SMFByteBufferIntegerUnsigned2Type
  {
    private final ByteBuffer buffer;

    Unsigned2b16(
      final ByteBuffer in_buffer,
      final JPRACursorByteReadableType in_cursor,
      final int in_size,
      final int in_offset)
    {
      super(in_cursor, in_size, in_offset);
      this.buffer = Objects.requireNonNull(in_buffer, "Buffer");
    }

    @Override
    public void get2UL(
      final VectorMutable2L v)
    {
      final long index = this.currentOffset();
      final int index_0 = Math.toIntExact(index);
      final long x = (long) Unsigned16.unpackFromBuffer(this.buffer, index_0);
      final int index_1 = Math.toIntExact(Math.addExact(index, 2L));
      final long y = (long) Unsigned16.unpackFromBuffer(this.buffer, index_1);
      v.setXY(x, y);
    }

    @Override
    public void set2UL(
      final long x,
      final long y)
    {
      final long index = this.currentOffset();
      final int index_0 = Math.toIntExact(index);
      Unsigned16.packToBuffer((int) x, this.buffer, index_0);
      final int index_1 = Math.toIntExact(Math.addExact(index, 2L));
      Unsigned16.packToBuffer((int) y, this.buffer, index_1);
    }
  }

  static final class Unsigned1b16 extends SMFByteBufferCursor implements
    SMFByteBufferIntegerUnsigned1Type
  {
    private final ByteBuffer buffer;

    Unsigned1b16(
      final ByteBuffer in_buffer,
      final JPRACursorByteReadableType in_cursor,
      final int in_size,
      final int in_offset)
    {
      super(in_cursor, in_size, in_offset);
      this.buffer = Objects.requireNonNull(in_buffer, "Buffer");
    }

    @Override
    public long get1UL()
    {
      final long index = this.currentOffset();
      final int index_0 = Math.toIntExact(index);
      return Unsigned16.unpackFromBuffer(this.buffer, index_0);
    }

    @Override
    public void set1UL(
      final long x)
    {
      final long index = this.currentOffset();
      final int index_0 = Math.toIntExact(index);
      Unsigned16.packToBuffer((int) x, this.buffer, index_0);
    }
  }

  static final class Unsigned4b8 extends SMFByteBufferCursor implements
    SMFByteBufferIntegerUnsigned4Type
  {
    private final ByteBuffer buffer;

    Unsigned4b8(
      final ByteBuffer in_buffer,
      final JPRACursorByteReadableType in_cursor,
      final int in_size,
      final int in_offset)
    {
      super(in_cursor, in_size, in_offset);
      this.buffer = Objects.requireNonNull(in_buffer, "Buffer");
    }

    @Override
    public void get4UL(
      final VectorMutable4L v)
    {
      final long index = this.currentOffset();
      final int index_0 = Math.toIntExact(index);
      final long x = (long) Unsigned8.unpackFromBuffer(this.buffer, index_0);
      final int index_1 = Math.toIntExact(Math.addExact(index, 1L));
      final long y = (long) Unsigned8.unpackFromBuffer(this.buffer, index_1);
      final int index_2 = Math.toIntExact(Math.addExact(index, 2L));
      final long z = (long) Unsigned8.unpackFromBuffer(this.buffer, index_2);
      final int index_3 = Math.toIntExact(Math.addExact(index, 3L));
      final long w = (long) Unsigned8.unpackFromBuffer(this.buffer, index_3);
      v.setXYZW(x, y, z, w);
    }

    @Override
    public void set4UL(
      final long x,
      final long y,
      final long z,
      final long w)
    {
      final long index = this.currentOffset();
      final int index_0 = Math.toIntExact(index);
      Unsigned8.packToBuffer((int) x, this.buffer, index_0);
      final int index_1 = Math.toIntExact(Math.addExact(index, 1L));
      Unsigned8.packToBuffer((int) y, this.buffer, index_1);
      final int index_2 = Math.toIntExact(Math.addExact(index, 2L));
      Unsigned8.packToBuffer((int) z, this.buffer, index_2);
      final int index_3 = Math.toIntExact(Math.addExact(index, 3L));
      Unsigned8.packToBuffer((int) w, this.buffer, index_3);
    }
  }

  static final class Unsigned3b8 extends SMFByteBufferCursor implements
    SMFByteBufferIntegerUnsigned3Type
  {
    private final ByteBuffer buffer;

    Unsigned3b8(
      final ByteBuffer in_buffer,
      final JPRACursorByteReadableType in_cursor,
      final int in_size,
      final int in_offset)
    {
      super(in_cursor, in_size, in_offset);
      this.buffer = Objects.requireNonNull(in_buffer, "Buffer");
    }

    @Override
    public void get3UL(
      final VectorMutable3L v)
    {
      final long index = this.currentOffset();
      final int index_0 = Math.toIntExact(index);
      final long x = (long) Unsigned8.unpackFromBuffer(this.buffer, index_0);
      final int index_1 = Math.toIntExact(Math.addExact(index, 1L));
      final long y = (long) Unsigned8.unpackFromBuffer(this.buffer, index_1);
      final int index_2 = Math.toIntExact(Math.addExact(index, 2L));
      final long z = (long) Unsigned8.unpackFromBuffer(this.buffer, index_2);
      v.setXYZ(x, y, z);
    }

    @Override
    public void set3UL(
      final long x,
      final long y,
      final long z)
    {
      final long index = this.currentOffset();
      final int index_0 = Math.toIntExact(index);
      Unsigned8.packToBuffer((int) x, this.buffer, index_0);
      final int index_1 = Math.toIntExact(Math.addExact(index, 1L));
      Unsigned8.packToBuffer((int) y, this.buffer, index_1);
      final int index_2 = Math.toIntExact(Math.addExact(index, 2L));
      Unsigned8.packToBuffer((int) z, this.buffer, index_2);
    }
  }

  static final class Unsigned2b8 extends SMFByteBufferCursor implements
    SMFByteBufferIntegerUnsigned2Type
  {
    private final ByteBuffer buffer;

    Unsigned2b8(
      final ByteBuffer in_buffer,
      final JPRACursorByteReadableType in_cursor,
      final int in_size,
      final int in_offset)
    {
      super(in_cursor, in_size, in_offset);
      this.buffer = Objects.requireNonNull(in_buffer, "Buffer");
    }

    @Override
    public void get2UL(
      final VectorMutable2L v)
    {
      final long index = this.currentOffset();
      final int index_0 = Math.toIntExact(index);
      final long x = (long) Unsigned8.unpackFromBuffer(this.buffer, index_0);
      final int index_1 = Math.toIntExact(Math.addExact(index, 1L));
      final long y = (long) Unsigned8.unpackFromBuffer(this.buffer, index_1);
      v.setXY(x, y);
    }

    @Override
    public void set2UL(
      final long x,
      final long y)
    {
      final long index = this.currentOffset();
      final int index_0 = Math.toIntExact(index);
      Unsigned8.packToBuffer((int) x, this.buffer, index_0);
      final int index_1 = Math.toIntExact(Math.addExact(index, 1L));
      Unsigned8.packToBuffer((int) y, this.buffer, index_1);
    }
  }

  static final class Unsigned1b8 extends SMFByteBufferCursor implements
    SMFByteBufferIntegerUnsigned1Type
  {
    private final ByteBuffer buffer;

    Unsigned1b8(
      final ByteBuffer in_buffer,
      final JPRACursorByteReadableType in_cursor,
      final int in_size,
      final int in_offset)
    {
      super(in_cursor, in_size, in_offset);
      this.buffer = Objects.requireNonNull(in_buffer, "Buffer");
    }

    @Override
    public long get1UL()
    {
      final long index = this.currentOffset();
      final int index_0 = Math.toIntExact(index);
      return (long) Unsigned8.unpackFromBuffer(this.buffer, index_0);
    }

    @Override
    public void set1UL(
      final long x)
    {
      final long index = this.currentOffset();
      final int index_0 = Math.toIntExact(index);
      Unsigned8.packToBuffer((int) x, this.buffer, index_0);
    }
  }
}
