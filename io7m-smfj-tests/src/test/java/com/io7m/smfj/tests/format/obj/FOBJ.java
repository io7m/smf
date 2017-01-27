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

package com.io7m.smfj.tests.format.obj;

import com.io7m.jlexing.core.LexicalPosition;
import com.io7m.smfj.core.SMFAttribute;
import com.io7m.smfj.core.SMFErrorType;
import com.io7m.smfj.core.SMFFormatVersion;
import com.io7m.smfj.core.SMFHeader;
import com.io7m.smfj.format.obj.SMFFormatOBJ;
import com.io7m.smfj.parser.api.SMFParseError;
import com.io7m.smfj.parser.api.SMFParserEventsType;
import com.io7m.smfj.parser.api.SMFParserSequentialType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Base64;
import java.util.Optional;

public final class FOBJ
{
  private static final Logger LOG;

  static {
    LOG = LoggerFactory.getLogger(FOBJ.class);
  }

  private FOBJ()
  {

  }

  public static void main(final String[] args)
    throws IOException
  {
    final SMFFormatOBJ fmt = new SMFFormatOBJ();
    final Path path = Paths.get("test.obj");
    try (final InputStream is = Files.newInputStream(path)) {
      try (final SMFParserSequentialType p = fmt.parserCreateSequential(new SMFParserEventsType()
      {
        @Override
        public boolean onMeta(
          final long vendor,
          final long schema,
          final long length)
        {
          return true;
        }

        @Override
        public void onMetaData(
          final long vendor,
          final long schema,
          final byte[] data)
        {
          LOG.debug(
            "metadata: {} {} {}",
            Long.toUnsignedString(vendor, 16),
            Long.toUnsignedString(schema, 16),
            Base64.getUrlEncoder().encodeToString(data));
        }

        @Override
        public void onHeaderParsed(final SMFHeader header)
        {
          LOG.debug("header parsed: {}", header);
        }

        @Override
        public void onStart()
        {
          LOG.debug("parser started");
        }

        @Override
        public void onError(
          final SMFErrorType e)
        {
          LOG.error(e.fullMessage());
        }

        @Override
        public void onVersionReceived(
          final SMFFormatVersion version)
        {
          LOG.info("version: {}", version);
        }

        @Override
        public void onDataAttributeStart(
          final SMFAttribute attribute)
        {
          LOG.debug("data start: {}", attribute);
        }

        @Override
        public void onDataAttributeValueIntegerSigned1(
          final long x)
        {
          LOG.debug(
            "data signed 1: {}",
            Long.valueOf(x));
        }

        @Override
        public void onDataAttributeValueIntegerSigned2(
          final long x,
          final long y)
        {
          LOG.debug(
            "data signed 2: {} {}",
            Long.valueOf(x),
            Long.valueOf(y));
        }

        @Override
        public void onDataAttributeValueIntegerSigned3(
          final long x,
          final long y,
          final long z)
        {
          LOG.debug(
            "data signed 3: {} {} {}",
            Long.valueOf(x),
            Long.valueOf(y),
            Long.valueOf(z));
        }

        @Override
        public void onDataAttributeValueIntegerSigned4(
          final long x,
          final long y,
          final long z,
          final long w)
        {
          LOG.debug(
            "data signed 4: {} {} {} {}",
            Long.valueOf(x),
            Long.valueOf(y),
            Long.valueOf(z),
            Long.valueOf(w));
        }

        @Override
        public void onDataAttributeValueIntegerUnsigned1(
          final long x)
        {
          LOG.debug(
            "data unsigned 1: {}",
            Long.valueOf(x));
        }

        @Override
        public void onDataAttributeValueIntegerUnsigned2(
          final long x,
          final long y)
        {
          LOG.debug(
            "data unsigned 2: {} {}",
            Long.valueOf(x),
            Long.valueOf(y));
        }

        @Override
        public void onDataAttributeValueIntegerUnsigned3(
          final long x,
          final long y,
          final long z)
        {
          LOG.debug(
            "data unsigned 3: {} {} {}",
            Long.valueOf(x),
            Long.valueOf(y),
            Long.valueOf(z));
        }

        @Override
        public void onDataAttributeValueIntegerUnsigned4(
          final long x,
          final long y,
          final long z,
          final long w)
        {
          LOG.debug(
            "data unsigned 4: {} {} {} {}",
            Long.valueOf(x),
            Long.valueOf(y),
            Long.valueOf(z),
            Long.valueOf(w));
        }

        @Override
        public void onDataAttributeValueFloat1(
          final double x)
        {
          LOG.debug(
            "data float 1: {}",
            Double.valueOf(x));
        }

        @Override
        public void onDataAttributeValueFloat2(
          final double x,
          final double y)
        {
          LOG.debug(
            "data float 2: {} {}",
            Double.valueOf(x),
            Double.valueOf(y));
        }

        @Override
        public void onDataAttributeValueFloat3(
          final double x,
          final double y,
          final double z)
        {
          LOG.debug(
            "data float 3: {} {} {}",
            Double.valueOf(x),
            Double.valueOf(y),
            Double.valueOf(z));
        }

        @Override
        public void onDataAttributeValueFloat4(
          final double x,
          final double y,
          final double z,
          final double w)
        {
          LOG.debug(
            "data float 4: {} {} {} {}",
            Double.valueOf(x),
            Double.valueOf(y),
            Double.valueOf(z),
            Double.valueOf(w));
        }

        @Override
        public void onDataAttributeFinish(
          final SMFAttribute attribute)
        {
          LOG.debug("data finish: {}", attribute);
        }

        @Override
        public void onDataTrianglesStart()
        {
          LOG.debug("triangles start");
        }

        @Override
        public void onDataTriangle(
          final long v0,
          final long v1,
          final long v2)
        {
          LOG.debug(
            "triangle: {} {} {}",
            Long.valueOf(v0),
            Long.valueOf(v1),
            Long.valueOf(v2));
        }

        @Override
        public void onDataTrianglesFinish()
        {
          LOG.debug("triangles finish");
        }

        @Override
        public void onFinish()
        {
          LOG.debug("parser finished");
        }
      }, path, is)) {
        p.parseHeader();
        p.parseData();
      }
    }
  }
}
