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
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URI;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.BitSet;
import java.util.Optional;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.mockito.internal.verification.Times;

public final class SMFBv1SectionParserTrianglesTest
{
  private ArgumentCaptor<SMFErrorType> captor;
  private SMFParserEventsBodyType eventsBody;
  private SMFParserEventsDataTrianglesType eventsTriangles;

  @BeforeEach
  public void testSetup()
  {
    this.eventsBody =
      Mockito.mock(SMFParserEventsBodyType.class);
    this.eventsTriangles =
      Mockito.mock(SMFParserEventsDataTrianglesType.class);
    this.captor =
      ArgumentCaptor.forClass(SMFErrorType.class);
  }

  @Test
  public void testEmpty()
  {
    final BitSet state = new BitSet();

    final SMFBDataStreamReaderType reader =
      SMFBDataStreamReader.create(
        URI.create("urn:test"),
        new ByteArrayInputStream(new byte[]{}));

    final SMFHeader header =
      SMFHeader.builder()
        .setTriangles(SMFTriangles.of(1L, 32))
        .build();

    final SMFBv1SectionParserTriangles p =
      new SMFBv1SectionParserTriangles(state);
    Assertions.assertEquals(SMFBSectionTriangles.MAGIC, p.magic());

    Mockito.when(this.eventsBody.onTriangles())
      .thenReturn(Optional.of(this.eventsTriangles));

    p.parse(header, this.eventsBody, reader);

    Mockito.verify(this.eventsTriangles, new Times(1))
      .onDataTrianglesFinish();
    Mockito.verify(this.eventsTriangles, new Times(1))
      .onError(this.captor.capture());

    final var e = this.captor.getValue();
    Assertions.assertTrue(
      e.exception().isPresent()
        && e.exception().get() instanceof IOException
        && e.message().contains("Failed to read the required number of octets"));
  }

  @Test
  public void testEmptyIgnored()
  {
    final BitSet state = new BitSet();

    final SMFBDataStreamReaderType reader =
      SMFBDataStreamReader.create(
        URI.create("urn:test"),
        new ByteArrayInputStream(new byte[]{}));

    final SMFHeader header =
      SMFHeader.builder()
        .setTriangles(SMFTriangles.of(1L, 32))
        .build();

    final SMFBv1SectionParserTriangles p =
      new SMFBv1SectionParserTriangles(state);
    Assertions.assertEquals(SMFBSectionTriangles.MAGIC, p.magic());

    Mockito.when(this.eventsBody.onTriangles())
      .thenReturn(Optional.empty());

    p.parse(header, this.eventsBody, reader);
  }

  @Test
  public void testEmptyNoneExpected()
  {
    final BitSet state = new BitSet();

    final SMFBDataStreamReaderType reader =
      SMFBDataStreamReader.create(
        URI.create("urn:test"),
        new ByteArrayInputStream(new byte[]{}));

    final SMFHeader header =
      SMFHeader.builder()
        .setTriangles(SMFTriangles.of(0L, 32))
        .build();

    final SMFBv1SectionParserTriangles p =
      new SMFBv1SectionParserTriangles(state);
    Assertions.assertEquals(SMFBSectionTriangles.MAGIC, p.magic());

    Mockito.when(this.eventsBody.onTriangles())
      .thenReturn(Optional.of(this.eventsTriangles));

    p.parse(header, this.eventsBody, reader);

    Mockito.verify(this.eventsTriangles, new Times(1))
      .onDataTrianglesFinish();
  }

  @Test
  public void testTriangle8()
  {
    final BitSet state = new BitSet();

    final SMFBDataStreamReaderType reader =
      SMFBDataStreamReader.create(
        URI.create("urn:test"),
        new ByteArrayInputStream(new byte[]{(byte) 0, (byte) 1, (byte) 2, (byte) 0, (byte) 2, (byte) 3}));

    final SMFHeader header =
      SMFHeader.builder()
        .setTriangles(SMFTriangles.of(2L, 8))
        .build();

    final SMFBv1SectionParserTriangles p =
      new SMFBv1SectionParserTriangles(state);
    Assertions.assertEquals(SMFBSectionTriangles.MAGIC, p.magic());

    Mockito.when(this.eventsBody.onTriangles())
      .thenReturn(Optional.of(this.eventsTriangles));

    p.parse(header, this.eventsBody, reader);

    Mockito.verify(this.eventsTriangles, new Times(1))
      .onDataTriangle(0L, 1L, 2L);
    Mockito.verify(this.eventsTriangles, new Times(1))
      .onDataTriangle(0L, 2L, 3L);
    Mockito.verify(this.eventsTriangles, new Times(1))
      .onDataTrianglesFinish();
  }

  @Test
  public void testTriangle16()
  {
    final BitSet state = new BitSet();

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

    final SMFBv1SectionParserTriangles p =
      new SMFBv1SectionParserTriangles(state);
    Assertions.assertEquals(SMFBSectionTriangles.MAGIC, p.magic());

    Mockito.when(this.eventsBody.onTriangles())
      .thenReturn(Optional.of(this.eventsTriangles));

    p.parse(header, this.eventsBody, reader);

    Mockito.verify(this.eventsTriangles, new Times(1))
      .onDataTriangle(0L, 1L, 2L);
    Mockito.verify(this.eventsTriangles, new Times(1))
      .onDataTriangle(0L, 2L, 3L);
    Mockito.verify(this.eventsTriangles, new Times(1))
      .onDataTrianglesFinish();
  }

  @Test
  public void testTriangle32()
  {
    final BitSet state = new BitSet();

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

    final SMFBv1SectionParserTriangles p =
      new SMFBv1SectionParserTriangles(state);
    Assertions.assertEquals(SMFBSectionTriangles.MAGIC, p.magic());

    Mockito.when(this.eventsBody.onTriangles())
      .thenReturn(Optional.of(this.eventsTriangles));

    p.parse(header, this.eventsBody, reader);

    Mockito.verify(this.eventsTriangles, new Times(1))
      .onDataTriangle(0L, 1L, 2L);
    Mockito.verify(this.eventsTriangles, new Times(1))
      .onDataTriangle(0L, 2L, 3L);
    Mockito.verify(this.eventsTriangles, new Times(1))
      .onDataTrianglesFinish();
  }

  @Test
  public void testTriangle64()
  {
    final BitSet state = new BitSet();

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

    final SMFBv1SectionParserTriangles p =
      new SMFBv1SectionParserTriangles(state);
    Assertions.assertEquals(SMFBSectionTriangles.MAGIC, p.magic());

    Mockito.when(this.eventsBody.onTriangles())
      .thenReturn(Optional.of(this.eventsTriangles));

    p.parse(header, this.eventsBody, reader);

    Mockito.verify(this.eventsTriangles, new Times(1))
      .onDataTriangle(0L, 1L, 2L);
    Mockito.verify(this.eventsTriangles, new Times(1))
      .onDataTriangle(0L, 2L, 3L);
    Mockito.verify(this.eventsTriangles, new Times(1))
      .onDataTrianglesFinish();
  }
}
