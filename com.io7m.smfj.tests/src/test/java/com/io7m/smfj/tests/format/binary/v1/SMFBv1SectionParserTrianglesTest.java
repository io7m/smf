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

package com.io7m.smfj.tests.format.binary.v1;

import com.io7m.smfj.core.SMFErrorType;
import com.io7m.smfj.core.SMFHeader;
import com.io7m.smfj.core.SMFTriangles;
import com.io7m.smfj.format.binary.SMFBDataStreamReader;
import com.io7m.smfj.format.binary.SMFBDataStreamReaderType;
import com.io7m.smfj.format.binary.SMFBSectionTriangles;
import com.io7m.smfj.format.binary.v1.SMFBv1SectionParserTriangles;
import com.io7m.smfj.parser.api.SMFParserEventsBodyType;
import com.io7m.smfj.parser.api.SMFParserEventsDataTrianglesType;
import mockit.Delegate;
import mockit.Mocked;
import mockit.StrictExpectations;
import org.junit.Assert;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URI;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Optional;

public final class SMFBv1SectionParserTrianglesTest
{
  @Test
  public void testEmpty(
    final @Mocked SMFParserEventsDataTrianglesType events_tri,
    final @Mocked SMFParserEventsBodyType events_body)
  {
    final SMFBDataStreamReaderType reader =
      SMFBDataStreamReader.create(
        URI.create("urn:test"),
        new ByteArrayInputStream(new byte[]{}));

    final SMFHeader header =
      SMFHeader.builder()
        .setTriangles(SMFTriangles.of(1L, 32))
        .build();

    final SMFBv1SectionParserTriangles p = new SMFBv1SectionParserTriangles();
    Assert.assertEquals(SMFBSectionTriangles.MAGIC, p.magic());

    new StrictExpectations()
    {{
      events_body.onTriangles();
      this.result = Optional.of(events_tri);

      events_tri.onError(this.with(new Delegate<SMFErrorType>()
      {
        boolean check(final SMFErrorType e)
        {
          return e.exception().isPresent()
            && e.exception().get() instanceof IOException
            && e.message().contains(
            "Failed to read the required number of octets");
        }
      }));

      events_tri.onDataTrianglesFinish();
    }};

    p.parse(header, events_body, reader);
  }

  @Test
  public void testEmptyIgnored(
    final @Mocked SMFParserEventsDataTrianglesType events_tri,
    final @Mocked SMFParserEventsBodyType events_body)
  {
    final SMFBDataStreamReaderType reader =
      SMFBDataStreamReader.create(
        URI.create("urn:test"),
        new ByteArrayInputStream(new byte[]{}));

    final SMFHeader header =
      SMFHeader.builder()
        .setTriangles(SMFTriangles.of(1L, 32))
        .build();

    final SMFBv1SectionParserTriangles p = new SMFBv1SectionParserTriangles();
    Assert.assertEquals(SMFBSectionTriangles.MAGIC, p.magic());

    new StrictExpectations()
    {{
      events_body.onTriangles();
      this.result = Optional.empty();
    }};

    p.parse(header, events_body, reader);
  }

  @Test
  public void testEmptyNoneExpected(
    final @Mocked SMFParserEventsDataTrianglesType events_tri,
    final @Mocked SMFParserEventsBodyType events_body)
  {
    final SMFBDataStreamReaderType reader =
      SMFBDataStreamReader.create(
        URI.create("urn:test"),
        new ByteArrayInputStream(new byte[]{}));

    final SMFHeader header =
      SMFHeader.builder()
        .setTriangles(SMFTriangles.of(0L, 32))
        .build();

    final SMFBv1SectionParserTriangles p = new SMFBv1SectionParserTriangles();
    Assert.assertEquals(SMFBSectionTriangles.MAGIC, p.magic());

    new StrictExpectations()
    {{
      events_body.onTriangles();
      this.result = Optional.of(events_tri);
      events_tri.onDataTrianglesFinish();
    }};

    p.parse(header, events_body, reader);
  }

  @Test
  public void testTriangle8(
    final @Mocked SMFParserEventsDataTrianglesType events_tri,
    final @Mocked SMFParserEventsBodyType events_body)
  {
    final SMFBDataStreamReaderType reader =
      SMFBDataStreamReader.create(
        URI.create("urn:test"),
        new ByteArrayInputStream(new byte[]{(byte) 0, (byte) 1, (byte) 2, (byte) 0, (byte) 2, (byte) 3}));

    final SMFHeader header =
      SMFHeader.builder()
        .setTriangles(SMFTriangles.of(2L, 8))
        .build();

    final SMFBv1SectionParserTriangles p = new SMFBv1SectionParserTriangles();
    Assert.assertEquals(SMFBSectionTriangles.MAGIC, p.magic());

    new StrictExpectations()
    {{
      events_body.onTriangles();
      this.result = Optional.of(events_tri);
      events_tri.onDataTriangle(0L, 1L, 2L);
      events_tri.onDataTriangle(0L, 2L, 3L);
      events_tri.onDataTrianglesFinish();
    }};

    p.parse(header, events_body, reader);
  }

  @Test
  public void testTriangle16(
    final @Mocked SMFParserEventsDataTrianglesType events_tri,
    final @Mocked SMFParserEventsBodyType events_body)
  {
    final byte[] data = new byte[6 * 2];

    final ByteBuffer wrap =
      ByteBuffer.wrap(data).order(ByteOrder.BIG_ENDIAN);
    wrap.putChar((char) 0);
    wrap.putChar((char) 1);
    wrap.putChar((char) 2);
    wrap.putChar((char) 0);
    wrap.putChar((char) 2);
    wrap.putChar((char) 3);

    final SMFBDataStreamReaderType reader =
      SMFBDataStreamReader.create(
        URI.create("urn:test"),
        new ByteArrayInputStream(data));

    final SMFHeader header =
      SMFHeader.builder()
        .setTriangles(SMFTriangles.of(2L, 16))
        .build();

    final SMFBv1SectionParserTriangles p = new SMFBv1SectionParserTriangles();
    Assert.assertEquals(SMFBSectionTriangles.MAGIC, p.magic());

    new StrictExpectations()
    {{
      events_body.onTriangles();
      this.result = Optional.of(events_tri);
      events_tri.onDataTriangle(0L, 1L, 2L);
      events_tri.onDataTriangle(0L, 2L, 3L);
      events_tri.onDataTrianglesFinish();
    }};

    p.parse(header, events_body, reader);
  }

  @Test
  public void testTriangle32(
    final @Mocked SMFParserEventsDataTrianglesType events_tri,
    final @Mocked SMFParserEventsBodyType events_body)
  {
    final byte[] data = new byte[6 * 4];

    final ByteBuffer wrap =
      ByteBuffer.wrap(data).order(ByteOrder.BIG_ENDIAN);
    wrap.putInt(0);
    wrap.putInt(1);
    wrap.putInt(2);
    wrap.putInt(0);
    wrap.putInt(2);
    wrap.putInt(3);

    final SMFBDataStreamReaderType reader =
      SMFBDataStreamReader.create(
        URI.create("urn:test"),
        new ByteArrayInputStream(data));

    final SMFHeader header =
      SMFHeader.builder()
        .setTriangles(SMFTriangles.of(2L, 32))
        .build();

    final SMFBv1SectionParserTriangles p = new SMFBv1SectionParserTriangles();
    Assert.assertEquals(SMFBSectionTriangles.MAGIC, p.magic());

    new StrictExpectations()
    {{
      events_body.onTriangles();
      this.result = Optional.of(events_tri);
      events_tri.onDataTriangle(0L, 1L, 2L);
      events_tri.onDataTriangle(0L, 2L, 3L);
      events_tri.onDataTrianglesFinish();
    }};

    p.parse(header, events_body, reader);
  }

  @Test
  public void testTriangle64(
    final @Mocked SMFParserEventsDataTrianglesType events_tri,
    final @Mocked SMFParserEventsBodyType events_body)
  {
    final byte[] data = new byte[6 * 8];

    final ByteBuffer wrap =
      ByteBuffer.wrap(data).order(ByteOrder.BIG_ENDIAN);
    wrap.putLong(0);
    wrap.putLong(1);
    wrap.putLong(2);
    wrap.putLong(0);
    wrap.putLong(2);
    wrap.putLong(3);

    final SMFBDataStreamReaderType reader =
      SMFBDataStreamReader.create(
        URI.create("urn:test"),
        new ByteArrayInputStream(data));

    final SMFHeader header =
      SMFHeader.builder()
        .setTriangles(SMFTriangles.of(2L, 64))
        .build();

    final SMFBv1SectionParserTriangles p = new SMFBv1SectionParserTriangles();
    Assert.assertEquals(SMFBSectionTriangles.MAGIC, p.magic());

    new StrictExpectations()
    {{
      events_body.onTriangles();
      this.result = Optional.of(events_tri);
      events_tri.onDataTriangle(0L, 1L, 2L);
      events_tri.onDataTriangle(0L, 2L, 3L);
      events_tri.onDataTrianglesFinish();
    }};

    p.parse(header, events_body, reader);
  }
}
