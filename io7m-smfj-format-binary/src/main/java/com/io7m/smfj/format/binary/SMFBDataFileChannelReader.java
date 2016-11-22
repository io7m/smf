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
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.FileChannel;
import java.nio.file.Path;
import java.util.Optional;

final class SMFBDataFileChannelReader
{
  private final Path path;
  private final FileChannel channel;
  private final ByteBuffer buffer8;
  private final ByteBuffer buffer4;
  private final ByteBuffer buffer2;
  private final ByteBuffer buffer1;

  SMFBDataFileChannelReader(
    final Path in_path,
    final FileChannel in_channel)
  {
    this.path = NullCheck.notNull(in_path, "Path");
    this.channel = NullCheck.notNull(in_channel, "Channel");
    this.buffer8 = ByteBuffer.allocate(8);
    this.buffer8.order(ByteOrder.BIG_ENDIAN);
    this.buffer4 = ByteBuffer.allocate(4);
    this.buffer4.order(ByteOrder.BIG_ENDIAN);
    this.buffer2 = ByteBuffer.allocate(2);
    this.buffer2.order(ByteOrder.BIG_ENDIAN);
    this.buffer1 = ByteBuffer.allocate(1);
    this.buffer1.order(ByteOrder.BIG_ENDIAN);
  }

  Path path()
  {
    return this.path;
  }

  public void readBytes(
    final Optional<String> name,
    final byte[] b,
    final long position)
    throws IOException
  {
    try {
      final ByteBuffer buffer = ByteBuffer.wrap(b);
      this.channel.read(buffer, position);
      if (buffer.position() != b.length) {
        throw new IOException("I/O error; too few octets read");
      }
    } catch (final IOException e) {
      throw this.makeIOException(
        name, position, (long) b.length, e.getMessage());
    }
  }

  public long readUnsigned8(
    final Optional<String> name,
    final long position)
    throws IOException
  {
    try {
      this.buffer1.position(0);
      this.channel.read(this.buffer1, position);
      if (this.buffer1.position() != 1) {
        throw new IOException("I/O error; too few octets read");
      }
      return ((long) this.buffer1.get(0)) & 0xFFL;
    } catch (final IOException e) {
      throw this.makeIOException(name, position, 1L, e.getMessage());
    }
  }

  public long readSigned8(
    final Optional<String> name,
    final long position)
    throws IOException
  {
    try {
      this.buffer1.position(0);
      this.channel.read(this.buffer1, position);
      if (this.buffer1.position() != 1) {
        throw new IOException("I/O error; too few octets read");
      }
      return (long) this.buffer1.get(0);
    } catch (final IOException e) {
      throw this.makeIOException(name, position, 1L, e.getMessage());
    }
  }

  public long readUnsigned16(
    final Optional<String> name,
    final long position)
    throws IOException
  {
    try {
      this.buffer2.position(0);
      this.channel.read(this.buffer2, position);
      if (this.buffer2.position() != 2) {
        throw new IOException("I/O error; too few octets read");
      }
      return ((long) this.buffer2.getChar(0)) & 0xFFFFL;
    } catch (final IOException e) {
      throw this.makeIOException(name, position, 2L, e.getMessage());
    }
  }

  public long readSigned16(
    final Optional<String> name,
    final long position)
    throws IOException
  {
    try {
      this.buffer2.position(0);
      this.channel.read(this.buffer2, position);
      if (this.buffer2.position() != 2) {
        throw new IOException("I/O error; too few octets read");
      }
      return (long) this.buffer2.getShort(0);
    } catch (final IOException e) {
      throw this.makeIOException(name, position, 2L, e.getMessage());
    }
  }

  public long readUnsigned32(
    final Optional<String> name,
    final long position)
    throws IOException
  {
    try {
      this.buffer4.position(0);
      this.channel.read(this.buffer4, position);
      if (this.buffer4.position() != 4) {
        throw new IOException("I/O error; too few octets read");
      }
      return ((long) this.buffer4.getInt(0)) & 0xFFFFFFFFL;
    } catch (final IOException e) {
      throw this.makeIOException(name, position, 4L, e.getMessage());
    }
  }

  public long readSigned32(
    final Optional<String> name,
    final long position)
    throws IOException
  {
    try {
      this.buffer4.position(0);
      this.channel.read(this.buffer4, position);
      if (this.buffer4.position() != 4) {
        throw new IOException("I/O error; too few octets read");
      }
      return (long) this.buffer4.getInt(0);
    } catch (final IOException e) {
      throw this.makeIOException(name, position, 4L, e.getMessage());
    }
  }

  public long readUnsigned64(
    final Optional<String> name,
    final long position)
    throws IOException
  {
    try {
      this.buffer8.position(0);
      this.channel.read(this.buffer8, position);
      if (this.buffer8.position() != 8) {
        throw new IOException("I/O error; too few octets read");
      }
      return this.buffer8.getLong(0);
    } catch (final IOException e) {
      throw this.makeIOException(name, position, 8L, e.getMessage());
    }
  }

  public long readSigned64(
    final Optional<String> name,
    final long position)
    throws IOException
  {
    try {
      this.buffer8.position(0);
      this.channel.read(this.buffer8, position);
      if (this.buffer8.position() != 8) {
        throw new IOException("I/O error; too few octets read");
      }
      return this.buffer8.getLong(0);
    } catch (final IOException e) {
      throw this.makeIOException(name, position, 8L, e.getMessage());
    }
  }

  private IOException makeIOException(
    final Optional<String> name,
    final long position,
    final long expected,
    final String cause)
  {
    final StringBuilder sb = new StringBuilder(128);
    sb.append("Failed to read the required number of octets.");
    sb.append(System.lineSeparator());
    sb.append("  Cause:    ");
    sb.append(cause);
    sb.append(System.lineSeparator());
    name.ifPresent(n -> {
      sb.append("  Target:   ");
      sb.append(n);
      sb.append(System.lineSeparator());
    });
    sb.append("  Position: ");
    sb.append(position);
    sb.append(System.lineSeparator());
    sb.append("  Expected: ");
    sb.append(expected);
    sb.append(" octets");
    sb.append(System.lineSeparator());
    sb.append("  Received: ");
    sb.append(this.buffer4.position());
    sb.append(" octets");
    sb.append(System.lineSeparator());
    return new IOException(sb.toString());
  }

  public double readFloat16(
    final Optional<String> name,
    final long position)
    throws IOException
  {
    try {
      this.buffer2.position(0);
      this.channel.read(this.buffer2, position);
      if (this.buffer2.position() != 2) {
        throw new IOException("I/O error; too few octets read");
      }
      return Binary16.unpackDouble(this.buffer2.getChar(0));
    } catch (final IOException e) {
      throw this.makeIOException(name, position, 2L, e.getMessage());
    }
  }

  public double readFloat32(
    final Optional<String> name,
    final long position)
    throws IOException
  {
    try {
      this.buffer4.position(0);
      this.channel.read(this.buffer4, position);
      if (this.buffer4.position() != 4) {
        throw new IOException("I/O error; too few octets read");
      }
      return (double) this.buffer4.getFloat(0);
    } catch (final IOException e) {
      throw this.makeIOException(name, position, 4L, e.getMessage());
    }
  }

  public double readFloat64(
    final Optional<String> name,
    final long position)
    throws IOException
  {
    try {
      this.buffer8.position(0);
      this.channel.read(this.buffer8, position);
      if (this.buffer8.position() != 8) {
        throw new IOException("I/O error; too few octets read");
      }
      return this.buffer8.getDouble(0);
    } catch (final IOException e) {
      throw this.makeIOException(name, position, 8L, e.getMessage());
    }
  }
}
