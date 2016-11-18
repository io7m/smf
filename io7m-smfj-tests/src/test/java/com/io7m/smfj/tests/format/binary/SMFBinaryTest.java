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


package com.io7m.smfj.tests.format.binary;

import com.io7m.ieee754b16.Binary16;
import com.io7m.jaffirm.core.Preconditions;
import com.io7m.smfj.format.binary.SMFFormatBinary;
import com.io7m.smfj.parser.api.SMFParserEventsType;
import com.io7m.smfj.parser.api.SMFParserRandomAccessType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.bind.DatatypeConverter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;
import java.util.function.Consumer;

public abstract class SMFBinaryTest implements SMFBinaryTestType
{
  private static final Logger LOG;

  static {
    LOG = LoggerFactory.getLogger(SMFBinaryTest.class);
  }

  @Override
  public final SMFParserRandomAccessType parserFor(
    final SMFParserEventsType events,
    final Consumer<SMFBinaryTestWriterType> o)
  {
    try {
      final Path dir = Files.createTempDirectory("smf-tests-");
      final Path target = dir.resolve("data");

      LOG.debug("path: {}", target);

      try (final OutputStream os =
             Files.newOutputStream(
               target,
               StandardOpenOption.CREATE,
               StandardOpenOption.TRUNCATE_EXISTING)) {
        o.accept(new SMFBinaryTestWriterType()
        {
          private long position;
          private byte[] byte2 = new byte[2];
          private byte[] byte4 = new byte[4];
          private byte[] byte8 = new byte[8];
          private ByteBuffer buffer2 = ByteBuffer.wrap(this.byte2);
          private ByteBuffer buffer4 = ByteBuffer.wrap(this.byte4);
          private ByteBuffer buffer8 = ByteBuffer.wrap(this.byte8);

          {
            this.buffer2.order(ByteOrder.BIG_ENDIAN);
            this.buffer4.order(ByteOrder.BIG_ENDIAN);
            this.buffer8.order(ByteOrder.BIG_ENDIAN);
          }

          @Override
          public void putS8(final long x)
          {
            try {
              os.write((byte) x);
              this.position += 1L;
            } catch (final IOException e) {
              throw new UncheckedIOException(e);
            }
          }

          @Override
          public void putS16(final long x)
          {
            Preconditions.checkPrecondition(
              this.position % 2L == 0L, "Position must be aligned");

            try {
              this.buffer2.putShort(0, (short) x);
              os.write(this.byte2);
              this.position += 2L;
            } catch (final IOException e) {
              throw new UncheckedIOException(e);
            }
          }

          @Override
          public void putS32(final long x)
          {
            Preconditions.checkPrecondition(
              this.position % 4L == 0L, "Position must be aligned");

            try {
              this.buffer4.putInt(0, (int) x);
              os.write(this.byte4);
              this.position += 4L;
            } catch (final IOException e) {
              throw new UncheckedIOException(e);
            }
          }

          @Override
          public void putS64(final long x)
          {
            Preconditions.checkPrecondition(
              this.position % 8L == 0L, "Position must be aligned");

            try {
              this.buffer8.putLong(0, x);
              os.write(this.byte8);
              this.position += 8L;
            } catch (final IOException e) {
              throw new UncheckedIOException(e);
            }
          }

          @Override
          public void putU8(final long x)
          {
            try {
              os.write((byte) (x & 0xffL));
              this.position += 1L;
            } catch (final IOException e) {
              throw new UncheckedIOException(e);
            }
          }

          @Override
          public void putU16(final long x)
          {
            Preconditions.checkPrecondition(
              this.position % 2L == 0L, "Position must be aligned");

            try {
              this.buffer2.putChar(0, (char) (x & 0xffffL));
              os.write(this.byte2);
              this.position += 2L;
            } catch (final IOException e) {
              throw new UncheckedIOException(e);
            }
          }

          @Override
          public void putU64(final long x)
          {
            Preconditions.checkPrecondition(
              this.position % 8L == 0L, "Position must be aligned");

            try {
              this.buffer8.putLong(0, x);
              os.write(this.byte8);
              this.position += 8L;
            } catch (final IOException e) {
              throw new UncheckedIOException(e);
            }
          }

          @Override
          public void putU32(final long x)
          {
            Preconditions.checkPrecondition(
              this.position % 4L == 0L, "Position must be aligned");

            try {
              this.buffer4.putInt(0, (int) (x & 0xFFFFFFFFL));
              os.write(this.byte4);
              this.position += 4L;
            } catch (final IOException e) {
              throw new UncheckedIOException(e);
            }
          }

          @Override
          public void putF16(final double x)
          {
            Preconditions.checkPrecondition(
              this.position % 2L == 0L, "Position must be aligned");

            try {
              this.buffer2.putChar(0, Binary16.packDouble(x));
              os.write(this.byte2);
              this.position += 2L;
            } catch (final IOException e) {
              throw new UncheckedIOException(e);
            }
          }

          @Override
          public void putF32(final double x)
          {
            Preconditions.checkPrecondition(
              this.position % 4L == 0L, "Position must be aligned");

            try {
              this.buffer4.putFloat(0, (float) x);
              os.write(this.byte4);
              this.position += 4L;
            } catch (final IOException e) {
              throw new UncheckedIOException(e);
            }
          }

          @Override
          public void putF64(final double x)
          {
            Preconditions.checkPrecondition(
              this.position % 8L == 0L, "Position must be aligned");

            try {
              this.buffer8.putDouble(0, x);
              os.write(this.byte8);
              this.position += 8L;
            } catch (final IOException e) {
              throw new UncheckedIOException(e);
            }
          }

          @Override
          public void putBytes(final byte[] x)
          {
            try {
              os.write(x);
              this.position = this.position + (long) x.length;
            } catch (final IOException e) {
              throw new UncheckedIOException(e);
            }
          }

          @Override
          public void putStringPad(
            final String text,
            final int max)
          {
            try {
              final byte[] data = new byte[max];
              final byte[] textb = text.getBytes(StandardCharsets.UTF_8);

              for (int index = 0; index < textb.length; ++index) {
                data[index] = textb[index];
              }

              this.putU32((long) textb.length);
              os.write(data);
              this.position = this.position + (long) data.length;
            } catch (final IOException e) {
              throw new UncheckedIOException(e);
            }
          }
        });
        os.flush();
      }

      LOG.debug("wrote {} octets", Long.valueOf(Files.size(target)));

      try (final InputStream is = Files.newInputStream(target)) {
        final byte[] buffer = new byte[16];
        while (true) {
          final int r = is.read(buffer);
          if (r == -1) {
            break;
          }
          LOG.debug(
            "{}",
            DatatypeConverter.printHexBinary(Arrays.copyOf(buffer, r)));
        }
      }

      final FileChannel channel = FileChannel.open(target);
      return new SMFFormatBinary()
        .parserCreateRandomAccess(events, target, channel);
    } catch (final IOException e) {
      throw new UncheckedIOException(e);
    }
  }
}
