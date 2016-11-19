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
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.input.CountingInputStream;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.file.Path;
import java.util.Optional;

/**
 * The default implementation of the {@link SMFBDataStreamReaderType} interface.
 */

public final class SMFBDataStreamReader implements SMFBDataStreamReaderType
{
  private final Path path;
  private final CountingInputStream stream;
  private final ByteBuffer buffer8;
  private final ByteBuffer buffer4;
  private final ByteBuffer buffer2;
  private final ByteBuffer buffer1;
  private final byte[] byte1;
  private final byte[] byte2;
  private final byte[] byte4;
  private final byte[] byte8;

  private SMFBDataStreamReader(
    final Path in_path,
    final InputStream in_stream)
  {
    this.path = NullCheck.notNull(in_path, "Path");
    this.stream = new CountingInputStream(NullCheck.notNull(in_stream, "Stream"));

    this.byte1 = new byte[1];
    this.byte2 = new byte[2];
    this.byte4 = new byte[4];
    this.byte8 = new byte[8];
    this.buffer8 = ByteBuffer.wrap(this.byte8);
    this.buffer8.order(ByteOrder.BIG_ENDIAN);
    this.buffer4 = ByteBuffer.wrap(this.byte4);
    this.buffer4.order(ByteOrder.BIG_ENDIAN);
    this.buffer2 = ByteBuffer.wrap(this.byte2);
    this.buffer2.order(ByteOrder.BIG_ENDIAN);
    this.buffer1 = ByteBuffer.wrap(this.byte1);
    this.buffer1.order(ByteOrder.BIG_ENDIAN);
  }

  /**
   * Create a data stream reader.
   *
   * @param in_path   The path
   * @param in_stream The stream
   *
   * @return A new data stream reader
   */

  public static SMFBDataStreamReaderType create(
    final Path in_path,
    final InputStream in_stream)
  {
    return new SMFBDataStreamReader(in_path, in_stream);
  }

  @Override
  public Path path()
  {
    return this.path;
  }

  @Override
  public void readBytes(
    final Optional<String> name,
    final byte[] b)
    throws IOException
  {
    try {
      IOUtils.readFully(this.stream, b);
    } catch (final IOException e) {
      throw this.makeIOException(
        name, this.position(), (long) b.length, e.getMessage());
    }
  }

  @Override
  public long position()
  {
    return this.stream.getByteCount();
  }

  @Override
  public long readU8(
    final Optional<String> name)
    throws IOException
  {
    try {
      IOUtils.readFully(this.stream, this.byte1);
      return (long) this.byte1[0] & 0xFFL;
    } catch (final IOException e) {
      throw this.makeIOException(name, this.position(), 1L, e.getMessage());
    }
  }

  @Override
  public long readS8(
    final Optional<String> name)
    throws IOException
  {
    try {
      IOUtils.readFully(this.stream, this.byte1);
      return (long) this.byte1[0];
    } catch (final IOException e) {
      throw this.makeIOException(name, this.position(), 1L, e.getMessage());
    }
  }

  @Override
  public long readU16(
    final Optional<String> name)
    throws IOException
  {
    try {
      IOUtils.readFully(this.stream, this.byte2);
      return (long) this.buffer2.getChar(0) & 0xFFFFL;
    } catch (final IOException e) {
      throw this.makeIOException(name, this.position(), 2L, e.getMessage());
    }
  }

  @Override
  public long readS16(
    final Optional<String> name)
    throws IOException
  {
    try {
      IOUtils.readFully(this.stream, this.byte2);
      return (long) this.buffer2.getShort(0);
    } catch (final IOException e) {
      throw this.makeIOException(name, this.position(), 2L, e.getMessage());
    }
  }

  @Override
  public long readU32(
    final Optional<String> name)
    throws IOException
  {
    try {
      IOUtils.readFully(this.stream, this.byte4);
      return (long) this.buffer4.getInt(0) & 0xFFFFFFFFL;
    } catch (final IOException e) {
      throw this.makeIOException(name, this.position(), 4L, e.getMessage());
    }
  }

  @Override
  public long readS32(
    final Optional<String> name)
    throws IOException
  {
    try {
      IOUtils.readFully(this.stream, this.byte4);
      return (long) this.buffer4.getInt(0);
    } catch (final IOException e) {
      throw this.makeIOException(name, this.position(), 4L, e.getMessage());
    }
  }

  @Override
  public long readU64(
    final Optional<String> name)
    throws IOException
  {
    try {
      IOUtils.readFully(this.stream, this.byte8);
      return this.buffer8.getLong(0);
    } catch (final IOException e) {
      throw this.makeIOException(name, this.position(), 8L, e.getMessage());
    }
  }

  @Override
  public long readS64(
    final Optional<String> name)
    throws IOException
  {
    try {
      IOUtils.readFully(this.stream, this.byte8);
      return this.buffer8.getLong(0);
    } catch (final IOException e) {
      throw this.makeIOException(name, this.position(), 8L, e.getMessage());
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

  @Override
  public double readF16(
    final Optional<String> name)
    throws IOException
  {
    try {
      IOUtils.readFully(this.stream, this.byte2);
      return Binary16.unpackDouble(this.buffer2.getChar(0));
    } catch (final IOException e) {
      throw this.makeIOException(name, this.position(), 2L, e.getMessage());
    }
  }

  @Override
  public double readF32(
    final Optional<String> name)
    throws IOException
  {
    try {
      IOUtils.readFully(this.stream, this.byte4);
      return (double) this.buffer4.getFloat(0);
    } catch (final IOException e) {
      throw this.makeIOException(name, this.position(), 4L, e.getMessage());
    }
  }

  @Override
  public double readF64(
    final Optional<String> name)
    throws IOException
  {
    try {
      IOUtils.readFully(this.stream, this.byte8);
      return this.buffer8.getDouble(0);
    } catch (final IOException e) {
      throw this.makeIOException(name, this.position(), 8L, e.getMessage());
    }
  }

  @Override
  public void skip(
    final long count)
    throws IOException
  {
    try {
      final long skipped = this.stream.skip(count);
      if (skipped != count) {
        throw new IOException("Could not skip the required number of octets");
      }
    } catch (final IOException e) {
      throw this.makeIOException(
        Optional.of("padding"), this.position(), count, e.getMessage());
    }
  }
}
