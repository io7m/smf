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

import com.io7m.smfj.format.binary.SMFBDataStreamWriter;
import com.io7m.smfj.format.binary.SMFBDataStreamWriterType;
import com.io7m.smfj.format.binary.SMFFormatBinary;
import com.io7m.smfj.parser.api.SMFParserEventsType;
import com.io7m.smfj.parser.api.SMFParserRandomAccessType;
import com.io7m.smfj.parser.api.SMFParserSequentialType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.bind.DatatypeConverter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;

public abstract class SMFBinaryTest implements SMFBinaryTestType
{
  private static final Logger LOG;

  static {
    LOG = LoggerFactory.getLogger(SMFBinaryTest.class);
  }

  @Override
  public SMFParserSequentialType parserSequentialFor(
    final SMFParserEventsType events,
    final IOConsumerType<SMFBDataStreamWriterType> o)
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
        o.accept(SMFBDataStreamWriter.create(target, os));
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

      return new SMFFormatBinary().parserCreateSequential(
        events, target, Files.newInputStream(target));
    } catch (final IOException e) {
      throw new UncheckedIOException(e);
    }
  }

  @Override
  public final SMFParserRandomAccessType parserRandomFor(
    final SMFParserEventsType events,
    final IOConsumerType<SMFBDataStreamWriterType> o)
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
        o.accept(SMFBDataStreamWriter.create(target, os));
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
