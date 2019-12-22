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

package com.io7m.smfj.tests.format.text.v1;

import com.io7m.smfj.core.SMFErrorType;
import com.io7m.smfj.core.SMFHeader;
import com.io7m.smfj.core.SMFTriangles;
import com.io7m.smfj.format.text.SMFTLineReaderList;
import com.io7m.smfj.format.text.SMFTLineReaderType;
import com.io7m.smfj.format.text.SMFTParsingStatus;
import com.io7m.smfj.format.text.implementation.Flags;
import com.io7m.smfj.format.text.v1.SMFTV1BodySectionParserTriangles;
import com.io7m.smfj.parser.api.SMFParserEventsBodyType;
import com.io7m.smfj.parser.api.SMFParserEventsDataTrianglesType;
import java.net.URI;
import java.util.BitSet;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.mockito.internal.verification.Times;

import static com.io7m.smfj.format.text.SMFTParsingStatus.FAILURE;
import static com.io7m.smfj.format.text.SMFTParsingStatus.SUCCESS;

public final class SMFTV1BodyCommandTrianglesTest
{
  private ArgumentCaptor<SMFErrorType> captor;
  private SMFParserEventsBodyType events;
  private SMFParserEventsDataTrianglesType eventsTriangles;
  private SMFTLineReaderType reader;

  @BeforeEach
  public void testSetup()
  {
    this.events =
      Mockito.mock(SMFParserEventsBodyType.class);
    this.eventsTriangles =
      Mockito.mock(SMFParserEventsDataTrianglesType.class);
    this.reader =
      Mockito.mock(SMFTLineReaderType.class);
    this.captor =
      ArgumentCaptor.forClass(SMFErrorType.class);
  }

  @Test
  public void testOK_0()
    throws Exception
  {
    final BitSet state = new BitSet();

    final SMFHeader.Builder header_b = SMFHeader.builder();
    final SMFHeader header = header_b.build();

    final SMFTV1BodySectionParserTriangles cmd =
      new SMFTV1BodySectionParserTriangles(() -> header, this.reader, state);

    Mockito.when(this.reader.line())
      .thenReturn(Optional.of(List.of("end")));

    final SMFTParsingStatus r = cmd.parse(this.events, List.of("triangles"));
    Assertions.assertEquals(SUCCESS, r);
    Assertions.assertTrue(state.get(Flags.TRIANGLES_RECEIVED));
  }

  @Test
  public void testMalformedCommand()
    throws Exception
  {
    final BitSet state = new BitSet();

    final SMFTLineReaderType reader =
      SMFTLineReaderList.create(
        URI.create("urn:x"),
        List.of(
          "0 1 2",
          "end"),
        0);

    final SMFHeader.Builder header_b = SMFHeader.builder();
    header_b.setTriangles(SMFTriangles.of(0L, 32));
    final SMFHeader header = header_b.build();

    final SMFTV1BodySectionParserTriangles cmd =
      new SMFTV1BodySectionParserTriangles(() -> header, reader, state);

    final SMFTParsingStatus r =
      cmd.parse(this.events, List.of("triangles", "what?"));
    Assertions.assertEquals(FAILURE, r);

    Mockito.verify(this.events).onError(this.captor.capture());
    Assertions.assertTrue(this.captor.getValue().message().contains("Could not parse"));
  }

  @Test
  public void testTooManyTriangles()
    throws Exception
  {
    final BitSet state = new BitSet();

    final SMFTLineReaderType reader =
      SMFTLineReaderList.create(
        URI.create("urn:x"),
        List.of(
          "0 1 2",
          "end"),
        0);

    final SMFHeader.Builder header_b = SMFHeader.builder();
    header_b.setTriangles(SMFTriangles.of(0L, 32));
    final SMFHeader header = header_b.build();

    final SMFTV1BodySectionParserTriangles cmd =
      new SMFTV1BodySectionParserTriangles(() -> header, reader, state);

    Mockito.when(this.events.onTriangles())
      .thenReturn(Optional.of(this.eventsTriangles));

    final SMFTParsingStatus r = cmd.parse(this.events, List.of("triangles"));
    Assertions.assertEquals(FAILURE, r);

    Mockito.verify(this.eventsTriangles).onError(this.captor.capture());
    Assertions.assertTrue(this.captor.getValue().message().contains("Too many triangles"));
    Mockito.verify(this.eventsTriangles, new Times(1)).onDataTrianglesFinish();
  }

  @Test
  public void testTooFewTriangles()
    throws Exception
  {
    final BitSet state = new BitSet();

    final SMFTLineReaderType reader =
      SMFTLineReaderList.create(
        URI.create("urn:x"),
        List.of(
          "end"),
        0);

    final SMFHeader.Builder header_b = SMFHeader.builder();
    header_b.setTriangles(SMFTriangles.of(1L, 32));
    final SMFHeader header = header_b.build();

    final SMFTV1BodySectionParserTriangles cmd =
      new SMFTV1BodySectionParserTriangles(() -> header, reader, state);

    Mockito.when(this.events.onTriangles())
      .thenReturn(Optional.of(this.eventsTriangles));

    final SMFTParsingStatus r = cmd.parse(this.events, List.of("triangles"));
    Assertions.assertEquals(FAILURE, r);

    Mockito.verify(this.eventsTriangles).onError(this.captor.capture());
    Assertions.assertTrue(this.captor.getValue().message().contains("Too few triangles"));
    Mockito.verify(this.eventsTriangles, new Times(1)).onDataTrianglesFinish();
  }

  @Test
  public void testBadTriangles0()
    throws Exception
  {
    final BitSet state = new BitSet();

    final SMFTLineReaderType reader =
      SMFTLineReaderList.create(
        URI.create("urn:x"),
        List.of(
          "z 1 2",
          "end"),
        0);

    final SMFHeader.Builder header_b = SMFHeader.builder();
    header_b.setTriangles(SMFTriangles.of(1L, 32));
    final SMFHeader header = header_b.build();

    final SMFTV1BodySectionParserTriangles cmd =
      new SMFTV1BodySectionParserTriangles(() -> header, reader, state);

    Mockito.when(this.events.onTriangles())
      .thenReturn(Optional.of(this.eventsTriangles));

    final SMFTParsingStatus r = cmd.parse(this.events, List.of("triangles"));
    Assertions.assertEquals(FAILURE, r);

    Mockito.verify(this.eventsTriangles).onError(this.captor.capture());
    Assertions.assertTrue(this.captor.getValue().message().contains("Cannot parse triangle"));
    Mockito.verify(this.eventsTriangles, new Times(1)).onDataTrianglesFinish();
  }

  @Test
  public void testBadTriangles1()
    throws Exception
  {
    final BitSet state = new BitSet();

    final SMFTLineReaderType reader =
      SMFTLineReaderList.create(
        URI.create("urn:x"),
        List.of(
          "1 2 3 4",
          "end"),
        0);

    final SMFHeader.Builder header_b = SMFHeader.builder();
    header_b.setTriangles(SMFTriangles.of(1L, 32));
    final SMFHeader header = header_b.build();

    final SMFTV1BodySectionParserTriangles cmd =
      new SMFTV1BodySectionParserTriangles(() -> header, reader, state);

    Mockito.when(this.events.onTriangles())
      .thenReturn(Optional.of(this.eventsTriangles));

    final SMFTParsingStatus r = cmd.parse(this.events, List.of("triangles"));
    Assertions.assertEquals(FAILURE, r);

    Mockito.verify(this.eventsTriangles).onError(this.captor.capture());
    Assertions.assertTrue(this.captor.getValue().message().contains("Cannot parse triangle"));
    Mockito.verify(this.eventsTriangles, new Times(1)).onDataTrianglesFinish();
  }

  @Test
  public void testTriangles()
    throws Exception
  {
    final BitSet state = new BitSet();

    final SMFTLineReaderType reader =
      SMFTLineReaderList.create(
        URI.create("urn:x"),
        List.of(
          "1 2 3",
          "end"),
        0);

    final SMFHeader.Builder header_b = SMFHeader.builder();
    header_b.setTriangles(SMFTriangles.of(1L, 32));
    final SMFHeader header = header_b.build();

    final SMFTV1BodySectionParserTriangles cmd =
      new SMFTV1BodySectionParserTriangles(() -> header, reader, state);

    Mockito.when(this.events.onTriangles())
      .thenReturn(Optional.of(this.eventsTriangles));

    final SMFTParsingStatus r = cmd.parse(this.events, List.of("triangles"));
    Assertions.assertEquals(SUCCESS, r);

    Mockito.verify(this.eventsTriangles, new Times(1)).onDataTriangle(1L, 2L, 3L);
    Mockito.verify(this.eventsTriangles, new Times(1)).onDataTrianglesFinish();
  }
}
