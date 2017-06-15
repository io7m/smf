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


package com.io7m.smfj.tests.format.text;

import com.io7m.smfj.format.text.SMFFormatText;
import com.io7m.smfj.parser.api.SMFParserEventsType;
import com.io7m.smfj.parser.api.SMFParserSequentialType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.bind.DatatypeConverter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;
import java.util.function.Consumer;

public abstract class SMFTextTest implements SMFTextTestType
{
  private static final Logger LOG;

  static {
    LOG = LoggerFactory.getLogger(SMFTextTest.class);
  }

  @Override
  public final SMFParserSequentialType parserFor(
    final SMFParserEventsType events,
    final Consumer<SMFTextTestWriterType> o)
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
        o.accept(s -> {
          try {
            os.write((s + System.lineSeparator())
                       .getBytes(StandardCharsets.UTF_8));
          } catch (final IOException e) {
            throw new UncheckedIOException(e);
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

      final InputStream stream = Files.newInputStream(target);
      return new SMFFormatText().parserCreateSequential(
        events, target.toUri(), stream);
    } catch (final IOException e) {
      throw new UncheckedIOException(e);
    }
  }
}
