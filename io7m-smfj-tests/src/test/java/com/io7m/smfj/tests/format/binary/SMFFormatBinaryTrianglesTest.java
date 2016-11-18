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

import com.io7m.smfj.core.SMFAttribute;
import com.io7m.smfj.core.SMFAttributeName;
import com.io7m.smfj.core.SMFAttributeNameType;
import com.io7m.smfj.core.SMFComponentType;
import com.io7m.smfj.core.SMFFormatVersion;
import com.io7m.smfj.format.binary.SMFFormatBinary;
import com.io7m.smfj.parser.api.SMFParserEventsType;
import com.io7m.smfj.parser.api.SMFParserRandomAccessType;
import mockit.Mocked;
import mockit.StrictExpectations;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class SMFFormatBinaryTrianglesTest extends SMFBinaryTest
{
  private static final Logger LOG;

  static {
    LOG = LoggerFactory.getLogger(SMFFormatBinaryTrianglesTest.class);
  }

  @Test
  public void testTriangles8(
    final @Mocked SMFParserEventsType events)
  {
    new StrictExpectations()
    {{
      events.onStart();
      events.onVersionReceived(SMFFormatVersion.of(1, 0));
      events.onHeaderStart();
      events.onHeaderAttributeCountReceived(0L);
      events.onHeaderVerticesCountReceived(0L);
      events.onHeaderTrianglesCountReceived(2L);
      events.onHeaderTrianglesIndexSizeReceived(8L);
      events.onHeaderFinish();

      events.onDataTrianglesStart();
      events.onDataTriangle(0L, 1L, 2L);
      events.onDataTriangle(0L, 2L, 3L);
      events.onDataTrianglesFinish();
    }};

    final SMFParserRandomAccessType p = this.parserFor(events, out -> {
      out.putBytes(SMFFormatBinary.magicNumber());
      out.putU32(1L);
      out.putU32(0L);

      out.putU64(0L);
      out.putU64(2L);
      out.putU32(8L);
      out.putU32(0x7f7f7f7fL);
      out.putU32(0L);
      out.putU32(0x7f7f7f7fL);

      out.putU8(0L);
      out.putU8(1L);
      out.putU8(2L);

      out.putU8(0L);
      out.putU8(2L);
      out.putU8(3L);
    });

    p.parseHeader();
    p.parseTriangles();
  }

  @Test
  public void testTriangles16(
    final @Mocked SMFParserEventsType events)
  {
    new StrictExpectations()
    {{
      events.onStart();
      events.onVersionReceived(SMFFormatVersion.of(1, 0));
      events.onHeaderStart();
      events.onHeaderAttributeCountReceived(0L);
      events.onHeaderVerticesCountReceived(0L);
      events.onHeaderTrianglesCountReceived(2L);
      events.onHeaderTrianglesIndexSizeReceived(16L);
      events.onHeaderFinish();

      events.onDataTrianglesStart();
      events.onDataTriangle(0L, 1L, 2L);
      events.onDataTriangle(0L, 2L, 3L);
      events.onDataTrianglesFinish();
    }};

    final SMFParserRandomAccessType p = this.parserFor(events, out -> {
      out.putBytes(SMFFormatBinary.magicNumber());
      out.putU32(1L);
      out.putU32(0L);

      out.putU64(0L);
      out.putU64(2L);
      out.putU32(16L);
      out.putU32(0x7f7f7f7fL);
      out.putU32(0L);
      out.putU32(0x7f7f7f7fL);

      out.putU16(0L);
      out.putU16(1L);
      out.putU16(2L);

      out.putU16(0L);
      out.putU16(2L);
      out.putU16(3L);
    });

    p.parseHeader();
    p.parseTriangles();
  }

  @Test
  public void testTriangles32(
    final @Mocked SMFParserEventsType events)
  {
    new StrictExpectations()
    {{
      events.onStart();
      events.onVersionReceived(SMFFormatVersion.of(1, 0));
      events.onHeaderStart();
      events.onHeaderAttributeCountReceived(0L);
      events.onHeaderVerticesCountReceived(0L);
      events.onHeaderTrianglesCountReceived(2L);
      events.onHeaderTrianglesIndexSizeReceived(32L);
      events.onHeaderFinish();

      events.onDataTrianglesStart();
      events.onDataTriangle(0L, 1L, 2L);
      events.onDataTriangle(0L, 2L, 3L);
      events.onDataTrianglesFinish();
    }};

    final SMFParserRandomAccessType p = this.parserFor(events, out -> {
      out.putBytes(SMFFormatBinary.magicNumber());
      out.putU32(1L);
      out.putU32(0L);

      out.putU64(0L);
      out.putU64(2L);
      out.putU32(32L);
      out.putU32(0x7f7f7f7fL);
      out.putU32(0L);
      out.putU32(0x7f7f7f7fL);

      out.putU32(0L);
      out.putU32(1L);
      out.putU32(2L);

      out.putU32(0L);
      out.putU32(2L);
      out.putU32(3L);
    });

    p.parseHeader();
    p.parseTriangles();
  }
}
