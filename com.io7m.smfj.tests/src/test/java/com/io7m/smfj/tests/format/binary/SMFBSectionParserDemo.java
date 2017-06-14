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

package com.io7m.smfj.tests.format.binary;

import com.io7m.junreachable.UnreachableCodeException;
import com.io7m.smfj.format.binary.SMFBDataStreamReader;
import com.io7m.smfj.format.binary.SMFBDataStreamReaderType;
import com.io7m.smfj.format.binary.SMFBSection;
import com.io7m.smfj.format.binary.SMFBSectionEnd;
import com.io7m.smfj.format.binary.SMFBSectionParser;
import com.io7m.smfj.format.binary.SMFBSectionParserType;
import com.io7m.smfj.parser.api.SMFParseError;
import javaslang.control.Validation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileInputStream;
import java.net.URI;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;

public final class SMFBSectionParserDemo
{
  private static final Logger LOG;

  static {
    LOG = LoggerFactory.getLogger(SMFBSectionParserDemo.class);
  }

  private SMFBSectionParserDemo()
  {
    throw new UnreachableCodeException();
  }

  public static void main(
    final String[] args)
    throws Exception
  {
    try (final FileInputStream stream = new FileInputStream(args[0])) {
      stream.skip(16L);

      final SMFBDataStreamReaderType reader =
        SMFBDataStreamReader.create(URI.create("urn:file"), stream);

      final SMFBSectionParserType sections = new SMFBSectionParser(reader);

      boolean ended = false;
      while (!ended) {
        final Validation<SMFParseError, SMFBSection> result = sections.parse();
        if (result.isValid()) {
          final SMFBSection section = result.get();
          final ByteBuffer buf = ByteBuffer.allocate(8);
          buf.order(ByteOrder.BIG_ENDIAN);
          buf.putLong(section.magic());
          final String text = new String(buf.array(), StandardCharsets.UTF_8);

          LOG.debug(
            "section: {} ({}) size {} at {}",
            Long.toUnsignedString(section.magic(), 16),
            text,
            Long.toUnsignedString(section.sizeOfData()),
            Long.toUnsignedString(section.offset()));

          ended = section.magic() == SMFBSectionEnd.MAGIC;
        } else {
          final SMFParseError error = result.getError();
          LOG.error("{}", error.fullMessage());
          error.exception().ifPresent(ex -> LOG.error("exception: ", ex));
        }
      }
    }
  }
}
