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

import com.io7m.jlexing.core.LexicalPosition;
import com.io7m.smfj.core.SMFAttribute;
import com.io7m.smfj.core.SMFAttributeName;
import com.io7m.smfj.core.SMFErrorType;
import com.io7m.smfj.core.SMFHeader;
import com.io7m.smfj.format.text.SMFTLineReaderList;
import com.io7m.smfj.format.text.SMFTLineReaderType;
import com.io7m.smfj.format.text.SMFTParsingStatus;
import com.io7m.smfj.format.text.implementation.Flags;
import com.io7m.smfj.format.text.v1.SMFTV1BodySectionParserVerticesNonInterleaved;
import com.io7m.smfj.parser.api.SMFParserEventsBodyType;
import com.io7m.smfj.parser.api.SMFParserEventsDataAttributeValuesType;
import com.io7m.smfj.parser.api.SMFParserEventsDataAttributesNonInterleavedType;
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

import static com.io7m.smfj.core.SMFComponentType.ELEMENT_TYPE_FLOATING;
import static com.io7m.smfj.core.SMFComponentType.ELEMENT_TYPE_INTEGER_SIGNED;
import static com.io7m.smfj.core.SMFComponentType.ELEMENT_TYPE_INTEGER_UNSIGNED;
import static com.io7m.smfj.format.text.SMFTParsingStatus.FAILURE;
import static com.io7m.smfj.format.text.SMFTParsingStatus.SUCCESS;

public final class SMFTV1BodyCommandVerticesNonInterleavedTest
{
  private SMFParserEventsBodyType events;
  private SMFTLineReaderType reader;
  private ArgumentCaptor<SMFErrorType> captor;
  private SMFParserEventsDataAttributeValuesType eventsValues;
  private SMFParserEventsDataAttributesNonInterleavedType eventsData;

  @BeforeEach
  public void testSetup()
  {
    this.events =
      Mockito.mock(SMFParserEventsBodyType.class);
    this.eventsValues =
      Mockito.mock(SMFParserEventsDataAttributeValuesType.class);
    this.eventsData =
      Mockito.mock(SMFParserEventsDataAttributesNonInterleavedType.class);
    this.reader =
      Mockito.mock(SMFTLineReaderType.class);
    this.captor =
      ArgumentCaptor.forClass(SMFErrorType.class);

    Mockito.when(this.reader.position())
      .thenReturn(LexicalPosition.of(0, 0, Optional.empty()));
  }

  @Test
  public void testOK_0()
    throws Exception
  {
    final BitSet state = new BitSet();

    final SMFHeader.Builder header_b = SMFHeader.builder();
    final SMFHeader header = header_b.build();

    final SMFTV1BodySectionParserVerticesNonInterleaved cmd =
      new SMFTV1BodySectionParserVerticesNonInterleaved(
        () -> header, this.reader, state);

    Mockito.when(this.reader.line())
      .thenReturn(Optional.of(List.of("end")));

    final SMFTParsingStatus r =
      cmd.parse(this.events, List.of("vertices", "noninterleaved"));
    Assertions.assertEquals(SUCCESS, r);
    Assertions.assertTrue(state.get(Flags.VERTICES_RECEIVED));
  }

  @Test
  public void testUndeclaredAttribute()
    throws Exception
  {
    final BitSet state = new BitSet();

    final SMFHeader.Builder header_b = SMFHeader.builder();
    final SMFHeader header = header_b.build();

    final SMFTV1BodySectionParserVerticesNonInterleaved cmd =
      new SMFTV1BodySectionParserVerticesNonInterleaved(
        () -> header, this.reader, state);

    Mockito.when(this.reader.line())
      .thenReturn(Optional.of(List.of("attribute", "x")));

    final SMFTParsingStatus r = cmd.parse(
      this.events,
      List.of(
        "vertices",
        "noninterleaved"));
    Assertions.assertEquals(FAILURE, r);

    Mockito.verify(this.events).onError(this.captor.capture());
    Assertions.assertTrue(this.captor.getValue().message().contains(
      "Unknown attribute"));
  }

  @Test
  public void testDuplicateAttribute()
    throws Exception
  {
    final BitSet state = new BitSet();

    final SMFTLineReaderType reader =
      SMFTLineReaderList.create(
        URI.create("urn:x"),
        List.of(
          "attribute x",
          "attribute x"),
        0);

    final SMFHeader.Builder header_b = SMFHeader.builder();
    header_b.addAttributesInOrder(
      SMFAttribute.of(
        SMFAttributeName.of("x"),
        ELEMENT_TYPE_FLOATING,
        3,
        32));

    final SMFHeader header = header_b.build();

    final SMFTV1BodySectionParserVerticesNonInterleaved cmd =
      new SMFTV1BodySectionParserVerticesNonInterleaved(
        () -> header, reader, state);

    final SMFTParsingStatus r = cmd.parse(
      this.events,
      List.of(
        "vertices",
        "noninterleaved"));
    Assertions.assertEquals(FAILURE, r);

    Mockito.verify(this.events).onError(this.captor.capture());
    Assertions.assertTrue(this.captor.getValue().message().contains(
      "Attribute already specified"));
  }

  @Test
  public void testBadAttribute()
    throws Exception
  {
    final BitSet state = new BitSet();

    final SMFTLineReaderType reader =
      SMFTLineReaderList.create(
        URI.create("urn:x"),
        List.of(
          "attribute"),
        0);

    final SMFHeader.Builder header_b = SMFHeader.builder();
    final SMFHeader header = header_b.build();

    final SMFTV1BodySectionParserVerticesNonInterleaved cmd =
      new SMFTV1BodySectionParserVerticesNonInterleaved(
        () -> header, reader, state);

    final SMFTParsingStatus r = cmd.parse(
      this.events,
      List.of(
        "vertices",
        "noninterleaved"));
    Assertions.assertEquals(FAILURE, r);

    Mockito.verify(this.events).onError(this.captor.capture());
    Assertions.assertTrue(this.captor.getValue().message().contains(
      "attribute <name>"));
  }

  @Test
  public void testUnexpectedEOF()
    throws Exception
  {
    final BitSet state = new BitSet();

    final SMFTLineReaderType reader =
      SMFTLineReaderList.create(
        URI.create("urn:x"),
        List.of(),
        0);

    final SMFHeader.Builder header_b = SMFHeader.builder();
    final SMFHeader header = header_b.build();

    final SMFTV1BodySectionParserVerticesNonInterleaved cmd =
      new SMFTV1BodySectionParserVerticesNonInterleaved(
        () -> header, reader, state);

    Mockito.when(this.events.onAttributesNonInterleaved())
      .thenReturn(Optional.of(this.eventsData));

    final SMFTParsingStatus r = cmd.parse(
      this.events,
      List.of(
        "vertices",
        "noninterleaved"));
    Assertions.assertEquals(FAILURE, r);

    Mockito.verify(this.eventsData).onError(this.captor.capture());
    Assertions.assertTrue(this.captor.getValue().message().contains(
      "Unexpected EOF"));
    Mockito.verify(
      this.eventsData,
      new Times(1)).onDataAttributesNonInterleavedFinish();
  }

  @Test
  public void testAttributeFloat4()
    throws Exception
  {
    final BitSet state = new BitSet();

    final SMFTLineReaderType reader =
      SMFTLineReaderList.create(
        URI.create("urn:x"),
        List.of(
          "attribute x",
          "0.0 1.0 2.0 3.0",
          "end"),
        0);

    final SMFHeader.Builder header_b = SMFHeader.builder();
    final SMFAttribute attribute =
      SMFAttribute.of(
        SMFAttributeName.of("x"), ELEMENT_TYPE_FLOATING, 4, 32);
    header_b.addAttributesInOrder(attribute);
    header_b.setVertexCount(1L);
    final SMFHeader header = header_b.build();

    final SMFTV1BodySectionParserVerticesNonInterleaved cmd =
      new SMFTV1BodySectionParserVerticesNonInterleaved(
        () -> header, reader, state);

    Mockito.when(this.events.onAttributesNonInterleaved())
      .thenReturn(Optional.of(this.eventsData));
    Mockito.when(this.eventsData.onDataAttributeStart(attribute))
      .thenReturn(Optional.of(this.eventsValues));

    final SMFTParsingStatus r =
      cmd.parse(this.events, List.of("vertices", "noninterleaved"));
    Assertions.assertEquals(SUCCESS, r);

    Mockito.verify(this.eventsValues, new Times(1))
      .onDataAttributeValueFloat4(0.0, 1.0, 2.0, 3.0);
    Mockito.verify(this.eventsValues, new Times(1))
      .onDataAttributeValueFinish();
    Mockito.verify(this.eventsData, new Times(1))
      .onDataAttributesNonInterleavedFinish();
  }

  @Test
  public void testAttributeFloat3()
    throws Exception
  {
    final BitSet state = new BitSet();

    final SMFTLineReaderType reader =
      SMFTLineReaderList.create(
        URI.create("urn:x"),
        List.of(
          "attribute x",
          "0.0 1.0 2.0",
          "end"),
        0);

    final SMFHeader.Builder header_b = SMFHeader.builder();
    final SMFAttribute attribute =
      SMFAttribute.of(
        SMFAttributeName.of("x"), ELEMENT_TYPE_FLOATING, 3, 32);
    header_b.addAttributesInOrder(attribute);
    header_b.setVertexCount(1L);
    final SMFHeader header = header_b.build();

    final SMFTV1BodySectionParserVerticesNonInterleaved cmd =
      new SMFTV1BodySectionParserVerticesNonInterleaved(
        () -> header, reader, state);

    Mockito.when(this.events.onAttributesNonInterleaved())
      .thenReturn(Optional.of(this.eventsData));
    Mockito.when(this.eventsData.onDataAttributeStart(attribute))
      .thenReturn(Optional.of(this.eventsValues));

    final SMFTParsingStatus r =
      cmd.parse(this.events, List.of("vertices", "noninterleaved"));
    Assertions.assertEquals(SUCCESS, r);

    Mockito.verify(this.eventsValues, new Times(1))
      .onDataAttributeValueFloat3(0.0, 1.0, 2.0);
    Mockito.verify(this.eventsValues, new Times(1))
      .onDataAttributeValueFinish();
    Mockito.verify(this.eventsData, new Times(1))
      .onDataAttributesNonInterleavedFinish();
  }

  @Test
  public void testAttributeFloat2()
    throws Exception
  {
    final BitSet state = new BitSet();

    final SMFTLineReaderType reader =
      SMFTLineReaderList.create(
        URI.create("urn:x"),
        List.of(
          "attribute x",
          "0.0 1.0",
          "end"),
        0);

    final SMFHeader.Builder header_b = SMFHeader.builder();
    final SMFAttribute attribute =
      SMFAttribute.of(
        SMFAttributeName.of("x"), ELEMENT_TYPE_FLOATING, 2, 32);
    header_b.addAttributesInOrder(attribute);
    header_b.setVertexCount(1L);
    final SMFHeader header = header_b.build();

    final SMFTV1BodySectionParserVerticesNonInterleaved cmd =
      new SMFTV1BodySectionParserVerticesNonInterleaved(
        () -> header, reader, state);

    Mockito.when(this.events.onAttributesNonInterleaved())
      .thenReturn(Optional.of(this.eventsData));
    Mockito.when(this.eventsData.onDataAttributeStart(attribute))
      .thenReturn(Optional.of(this.eventsValues));

    final SMFTParsingStatus r =
      cmd.parse(this.events, List.of("vertices", "noninterleaved"));
    Assertions.assertEquals(SUCCESS, r);

    Mockito.verify(this.eventsValues, new Times(1))
      .onDataAttributeValueFloat2(0.0, 1.0);
    Mockito.verify(this.eventsValues, new Times(1))
      .onDataAttributeValueFinish();
    Mockito.verify(this.eventsData, new Times(1))
      .onDataAttributesNonInterleavedFinish();
  }

  @Test
  public void testAttributeFloat1()
    throws Exception
  {
    final BitSet state = new BitSet();

    final SMFTLineReaderType reader =
      SMFTLineReaderList.create(
        URI.create("urn:x"),
        List.of(
          "attribute x",
          "0.0",
          "end"),
        0);

    final SMFHeader.Builder header_b = SMFHeader.builder();
    final SMFAttribute attribute =
      SMFAttribute.of(
        SMFAttributeName.of("x"), ELEMENT_TYPE_FLOATING, 1, 32);
    header_b.addAttributesInOrder(attribute);
    header_b.setVertexCount(1L);
    final SMFHeader header = header_b.build();

    final SMFTV1BodySectionParserVerticesNonInterleaved cmd =
      new SMFTV1BodySectionParserVerticesNonInterleaved(
        () -> header, reader, state);

    Mockito.when(this.events.onAttributesNonInterleaved())
      .thenReturn(Optional.of(this.eventsData));
    Mockito.when(this.eventsData.onDataAttributeStart(attribute))
      .thenReturn(Optional.of(this.eventsValues));

    final SMFTParsingStatus r =
      cmd.parse(this.events, List.of("vertices", "noninterleaved"));
    Assertions.assertEquals(SUCCESS, r);

    Mockito.verify(this.eventsValues, new Times(1))
      .onDataAttributeValueFloat1(0.0);
    Mockito.verify(this.eventsValues, new Times(1))
      .onDataAttributeValueFinish();
    Mockito.verify(this.eventsData, new Times(1))
      .onDataAttributesNonInterleavedFinish();
  }

  @Test
  public void testAttributeBadFloat4()
    throws Exception
  {
    final BitSet state = new BitSet();

    final SMFTLineReaderType reader =
      SMFTLineReaderList.create(
        URI.create("urn:x"),
        List.of(
          "attribute x",
          "z 1.0 2.0 3.0",
          "end"),
        0);

    final SMFHeader.Builder header_b = SMFHeader.builder();
    final SMFAttribute attribute =
      SMFAttribute.of(
        SMFAttributeName.of("x"), ELEMENT_TYPE_FLOATING, 4, 32);
    header_b.addAttributesInOrder(attribute);
    header_b.setVertexCount(1L);
    final SMFHeader header = header_b.build();

    final SMFTV1BodySectionParserVerticesNonInterleaved cmd =
      new SMFTV1BodySectionParserVerticesNonInterleaved(
        () -> header, reader, state);

    Mockito.when(this.events.onAttributesNonInterleaved())
      .thenReturn(Optional.of(this.eventsData));
    Mockito.when(this.eventsData.onDataAttributeStart(attribute))
      .thenReturn(Optional.of(this.eventsValues));

    final SMFTParsingStatus r =
      cmd.parse(this.events, List.of("vertices", "noninterleaved"));
    Assertions.assertEquals(FAILURE, r);

    Mockito.verify(this.eventsValues).onError(this.captor.capture());
    Assertions.assertTrue(this.captor.getValue().message().contains(
      "Cannot parse"));
    Mockito.verify(this.eventsValues, new Times(1))
      .onDataAttributeValueFinish();
    Mockito.verify(this.eventsData, new Times(1))
      .onDataAttributesNonInterleavedFinish();
  }

  @Test
  public void testAttributeBadFloat3()
    throws Exception
  {
    final BitSet state = new BitSet();

    final SMFTLineReaderType reader =
      SMFTLineReaderList.create(
        URI.create("urn:x"),
        List.of(
          "attribute x",
          "z 1.0 2.0",
          "end"),
        0);

    final SMFHeader.Builder header_b = SMFHeader.builder();
    final SMFAttribute attribute =
      SMFAttribute.of(
        SMFAttributeName.of("x"), ELEMENT_TYPE_FLOATING, 3, 32);
    header_b.addAttributesInOrder(attribute);
    header_b.setVertexCount(1L);
    final SMFHeader header = header_b.build();

    final SMFTV1BodySectionParserVerticesNonInterleaved cmd =
      new SMFTV1BodySectionParserVerticesNonInterleaved(
        () -> header, reader, state);

    Mockito.when(this.events.onAttributesNonInterleaved())
      .thenReturn(Optional.of(this.eventsData));
    Mockito.when(this.eventsData.onDataAttributeStart(attribute))
      .thenReturn(Optional.of(this.eventsValues));

    final SMFTParsingStatus r =
      cmd.parse(this.events, List.of("vertices", "noninterleaved"));
    Assertions.assertEquals(FAILURE, r);

    Mockito.verify(this.eventsValues).onError(this.captor.capture());
    Assertions.assertTrue(this.captor.getValue().message().contains(
      "Cannot parse"));
    Mockito.verify(this.eventsValues, new Times(1))
      .onDataAttributeValueFinish();
    Mockito.verify(this.eventsData, new Times(1))
      .onDataAttributesNonInterleavedFinish();
  }

  @Test
  public void testAttributeBadFloat2()
    throws Exception
  {
    final BitSet state = new BitSet();

    final SMFTLineReaderType reader =
      SMFTLineReaderList.create(
        URI.create("urn:x"),
        List.of(
          "attribute x",
          "z 1.0",
          "end"),
        0);

    final SMFHeader.Builder header_b = SMFHeader.builder();
    final SMFAttribute attribute =
      SMFAttribute.of(
        SMFAttributeName.of("x"), ELEMENT_TYPE_FLOATING, 2, 32);
    header_b.addAttributesInOrder(attribute);
    header_b.setVertexCount(1L);
    final SMFHeader header = header_b.build();

    final SMFTV1BodySectionParserVerticesNonInterleaved cmd =
      new SMFTV1BodySectionParserVerticesNonInterleaved(
        () -> header, reader, state);

    Mockito.when(this.events.onAttributesNonInterleaved())
      .thenReturn(Optional.of(this.eventsData));
    Mockito.when(this.eventsData.onDataAttributeStart(attribute))
      .thenReturn(Optional.of(this.eventsValues));

    final SMFTParsingStatus r =
      cmd.parse(this.events, List.of("vertices", "noninterleaved"));
    Assertions.assertEquals(FAILURE, r);

    Mockito.verify(this.eventsValues).onError(this.captor.capture());
    Assertions.assertTrue(this.captor.getValue().message().contains(
      "Cannot parse"));
    Mockito.verify(this.eventsValues, new Times(1))
      .onDataAttributeValueFinish();
    Mockito.verify(this.eventsData, new Times(1))
      .onDataAttributesNonInterleavedFinish();
  }

  @Test
  public void testAttributeBadFloat1()
    throws Exception
  {
    final BitSet state = new BitSet();

    final SMFTLineReaderType reader =
      SMFTLineReaderList.create(
        URI.create("urn:x"),
        List.of(
          "attribute x",
          "z",
          "end"),
        0);

    final SMFHeader.Builder header_b = SMFHeader.builder();
    final SMFAttribute attribute =
      SMFAttribute.of(
        SMFAttributeName.of("x"), ELEMENT_TYPE_FLOATING, 1, 32);
    header_b.addAttributesInOrder(attribute);
    header_b.setVertexCount(1L);
    final SMFHeader header = header_b.build();

    final SMFTV1BodySectionParserVerticesNonInterleaved cmd =
      new SMFTV1BodySectionParserVerticesNonInterleaved(
        () -> header, reader, state);

    Mockito.when(this.events.onAttributesNonInterleaved())
      .thenReturn(Optional.of(this.eventsData));
    Mockito.when(this.eventsData.onDataAttributeStart(attribute))
      .thenReturn(Optional.of(this.eventsValues));

    final SMFTParsingStatus r =
      cmd.parse(this.events, List.of("vertices", "noninterleaved"));
    Assertions.assertEquals(FAILURE, r);

    Mockito.verify(this.eventsValues).onError(this.captor.capture());
    Assertions.assertTrue(this.captor.getValue().message().contains(
      "Cannot parse"));
    Mockito.verify(this.eventsValues, new Times(1))
      .onDataAttributeValueFinish();
    Mockito.verify(this.eventsData, new Times(1))
      .onDataAttributesNonInterleavedFinish();
  }

  @Test
  public void testAttributeIntegerSigned4()
    throws Exception
  {
    final BitSet state = new BitSet();

    final SMFTLineReaderType reader =
      SMFTLineReaderList.create(
        URI.create("urn:x"),
        List.of(
          "attribute x",
          "0 1 2 3",
          "end"),
        0);

    final SMFHeader.Builder header_b = SMFHeader.builder();
    final SMFAttribute attribute =
      SMFAttribute.of(
        SMFAttributeName.of("x"), ELEMENT_TYPE_INTEGER_SIGNED, 4, 32);
    header_b.addAttributesInOrder(attribute);
    header_b.setVertexCount(1L);
    final SMFHeader header = header_b.build();

    final SMFTV1BodySectionParserVerticesNonInterleaved cmd =
      new SMFTV1BodySectionParserVerticesNonInterleaved(
        () -> header, reader, state);

    Mockito.when(this.events.onAttributesNonInterleaved())
      .thenReturn(Optional.of(this.eventsData));
    Mockito.when(this.eventsData.onDataAttributeStart(attribute))
      .thenReturn(Optional.of(this.eventsValues));

    final SMFTParsingStatus r =
      cmd.parse(this.events, List.of("vertices", "noninterleaved"));
    Assertions.assertEquals(SUCCESS, r);

    Mockito.verify(this.eventsValues, new Times(1))
      .onDataAttributeValueIntegerSigned4(0, 1, 2, 3);
    Mockito.verify(this.eventsValues, new Times(1))
      .onDataAttributeValueFinish();
    Mockito.verify(this.eventsData, new Times(1))
      .onDataAttributesNonInterleavedFinish();
  }

  @Test
  public void testAttributeIntegerSigned3()
    throws Exception
  {
    final BitSet state = new BitSet();

    final SMFTLineReaderType reader =
      SMFTLineReaderList.create(
        URI.create("urn:x"),
        List.of(
          "attribute x",
          "0 1 2",
          "end"),
        0);

    final SMFHeader.Builder header_b = SMFHeader.builder();
    final SMFAttribute attribute =
      SMFAttribute.of(
        SMFAttributeName.of("x"), ELEMENT_TYPE_INTEGER_SIGNED, 3, 32);
    header_b.addAttributesInOrder(attribute);
    header_b.setVertexCount(1L);
    final SMFHeader header = header_b.build();

    final SMFTV1BodySectionParserVerticesNonInterleaved cmd =
      new SMFTV1BodySectionParserVerticesNonInterleaved(
        () -> header, reader, state);

    Mockito.when(this.events.onAttributesNonInterleaved())
      .thenReturn(Optional.of(this.eventsData));
    Mockito.when(this.eventsData.onDataAttributeStart(attribute))
      .thenReturn(Optional.of(this.eventsValues));

    final SMFTParsingStatus r =
      cmd.parse(this.events, List.of("vertices", "noninterleaved"));
    Assertions.assertEquals(SUCCESS, r);

    Mockito.verify(this.eventsValues, new Times(1))
      .onDataAttributeValueIntegerSigned3(0, 1, 2);
    Mockito.verify(this.eventsValues, new Times(1))
      .onDataAttributeValueFinish();
    Mockito.verify(this.eventsData, new Times(1))
      .onDataAttributesNonInterleavedFinish();
  }

  @Test
  public void testAttributeIntegerSigned2()
    throws Exception
  {
    final BitSet state = new BitSet();

    final SMFTLineReaderType reader =
      SMFTLineReaderList.create(
        URI.create("urn:x"),
        List.of(
          "attribute x",
          "0 1",
          "end"),
        0);

    final SMFHeader.Builder header_b = SMFHeader.builder();
    final SMFAttribute attribute =
      SMFAttribute.of(
        SMFAttributeName.of("x"), ELEMENT_TYPE_INTEGER_SIGNED, 2, 32);
    header_b.addAttributesInOrder(attribute);
    header_b.setVertexCount(1L);
    final SMFHeader header = header_b.build();

    final SMFTV1BodySectionParserVerticesNonInterleaved cmd =
      new SMFTV1BodySectionParserVerticesNonInterleaved(
        () -> header, reader, state);

    Mockito.when(this.events.onAttributesNonInterleaved())
      .thenReturn(Optional.of(this.eventsData));
    Mockito.when(this.eventsData.onDataAttributeStart(attribute))
      .thenReturn(Optional.of(this.eventsValues));

    final SMFTParsingStatus r =
      cmd.parse(this.events, List.of("vertices", "noninterleaved"));
    Assertions.assertEquals(SUCCESS, r);

    Mockito.verify(this.eventsValues, new Times(1))
      .onDataAttributeValueIntegerSigned2(0, 1);
    Mockito.verify(this.eventsValues, new Times(1))
      .onDataAttributeValueFinish();
    Mockito.verify(this.eventsData, new Times(1))
      .onDataAttributesNonInterleavedFinish();
  }

  @Test
  public void testAttributeIntegerSigned1()
    throws Exception
  {
    final BitSet state = new BitSet();

    final SMFTLineReaderType reader =
      SMFTLineReaderList.create(
        URI.create("urn:x"),
        List.of(
          "attribute x",
          "0",
          "end"),
        0);

    final SMFHeader.Builder header_b = SMFHeader.builder();
    final SMFAttribute attribute =
      SMFAttribute.of(
        SMFAttributeName.of("x"), ELEMENT_TYPE_INTEGER_SIGNED, 1, 32);
    header_b.addAttributesInOrder(attribute);
    header_b.setVertexCount(1L);
    final SMFHeader header = header_b.build();

    final SMFTV1BodySectionParserVerticesNonInterleaved cmd =
      new SMFTV1BodySectionParserVerticesNonInterleaved(
        () -> header, reader, state);

    Mockito.when(this.events.onAttributesNonInterleaved())
      .thenReturn(Optional.of(this.eventsData));
    Mockito.when(this.eventsData.onDataAttributeStart(attribute))
      .thenReturn(Optional.of(this.eventsValues));

    final SMFTParsingStatus r =
      cmd.parse(this.events, List.of("vertices", "noninterleaved"));
    Assertions.assertEquals(SUCCESS, r);

    Mockito.verify(this.eventsValues, new Times(1))
      .onDataAttributeValueIntegerSigned1(0);
    Mockito.verify(this.eventsValues, new Times(1))
      .onDataAttributeValueFinish();
    Mockito.verify(this.eventsData, new Times(1))
      .onDataAttributesNonInterleavedFinish();
  }

  @Test
  public void testAttributeBadIntegerSigned4()
    throws Exception
  {
    final BitSet state = new BitSet();

    final SMFTLineReaderType reader =
      SMFTLineReaderList.create(
        URI.create("urn:x"),
        List.of(
          "attribute x",
          "z 1 2 3",
          "end"),
        0);

    final SMFHeader.Builder header_b = SMFHeader.builder();
    final SMFAttribute attribute =
      SMFAttribute.of(
        SMFAttributeName.of("x"), ELEMENT_TYPE_INTEGER_SIGNED, 4, 32);
    header_b.addAttributesInOrder(attribute);
    header_b.setVertexCount(1L);
    final SMFHeader header = header_b.build();

    final SMFTV1BodySectionParserVerticesNonInterleaved cmd =
      new SMFTV1BodySectionParserVerticesNonInterleaved(
        () -> header, reader, state);

    Mockito.when(this.events.onAttributesNonInterleaved())
      .thenReturn(Optional.of(this.eventsData));
    Mockito.when(this.eventsData.onDataAttributeStart(attribute))
      .thenReturn(Optional.of(this.eventsValues));

    final SMFTParsingStatus r =
      cmd.parse(this.events, List.of("vertices", "noninterleaved"));
    Assertions.assertEquals(FAILURE, r);

    Mockito.verify(this.eventsValues).onError(this.captor.capture());
    Assertions.assertTrue(this.captor.getValue().message().contains(
      "Cannot parse"));
    Mockito.verify(this.eventsValues, new Times(1))
      .onDataAttributeValueFinish();
    Mockito.verify(this.eventsData, new Times(1))
      .onDataAttributesNonInterleavedFinish();
  }

  @Test
  public void testAttributeBadIntegerSigned3()
    throws Exception
  {
    final BitSet state = new BitSet();

    final SMFTLineReaderType reader =
      SMFTLineReaderList.create(
        URI.create("urn:x"),
        List.of(
          "attribute x",
          "z 1 2",
          "end"),
        0);

    final SMFHeader.Builder header_b = SMFHeader.builder();
    final SMFAttribute attribute =
      SMFAttribute.of(
        SMFAttributeName.of("x"), ELEMENT_TYPE_INTEGER_SIGNED, 3, 32);
    header_b.addAttributesInOrder(attribute);
    header_b.setVertexCount(1L);
    final SMFHeader header = header_b.build();

    final SMFTV1BodySectionParserVerticesNonInterleaved cmd =
      new SMFTV1BodySectionParserVerticesNonInterleaved(
        () -> header, reader, state);

    Mockito.when(this.events.onAttributesNonInterleaved())
      .thenReturn(Optional.of(this.eventsData));
    Mockito.when(this.eventsData.onDataAttributeStart(attribute))
      .thenReturn(Optional.of(this.eventsValues));

    final SMFTParsingStatus r =
      cmd.parse(this.events, List.of("vertices", "noninterleaved"));
    Assertions.assertEquals(FAILURE, r);

    Mockito.verify(this.eventsValues).onError(this.captor.capture());
    Assertions.assertTrue(this.captor.getValue().message().contains(
      "Cannot parse"));
    Mockito.verify(this.eventsValues, new Times(1))
      .onDataAttributeValueFinish();
    Mockito.verify(this.eventsData, new Times(1))
      .onDataAttributesNonInterleavedFinish();
  }

  @Test
  public void testAttributeBadIntegerSigned2()
    throws Exception
  {
    final BitSet state = new BitSet();

    final SMFTLineReaderType reader =
      SMFTLineReaderList.create(
        URI.create("urn:x"),
        List.of(
          "attribute x",
          "z 1",
          "end"),
        0);

    final SMFHeader.Builder header_b = SMFHeader.builder();
    final SMFAttribute attribute =
      SMFAttribute.of(
        SMFAttributeName.of("x"), ELEMENT_TYPE_INTEGER_SIGNED, 2, 32);
    header_b.addAttributesInOrder(attribute);
    header_b.setVertexCount(1L);
    final SMFHeader header = header_b.build();

    final SMFTV1BodySectionParserVerticesNonInterleaved cmd =
      new SMFTV1BodySectionParserVerticesNonInterleaved(
        () -> header, reader, state);

    Mockito.when(this.events.onAttributesNonInterleaved())
      .thenReturn(Optional.of(this.eventsData));
    Mockito.when(this.eventsData.onDataAttributeStart(attribute))
      .thenReturn(Optional.of(this.eventsValues));

    final SMFTParsingStatus r =
      cmd.parse(this.events, List.of("vertices", "noninterleaved"));
    Assertions.assertEquals(FAILURE, r);

    Mockito.verify(this.eventsValues).onError(this.captor.capture());
    Assertions.assertTrue(this.captor.getValue().message().contains(
      "Cannot parse"));
    Mockito.verify(this.eventsValues, new Times(1))
      .onDataAttributeValueFinish();
    Mockito.verify(this.eventsData, new Times(1))
      .onDataAttributesNonInterleavedFinish();
  }

  @Test
  public void testAttributeBadIntegerSigned1()
    throws Exception
  {
    final BitSet state = new BitSet();

    final SMFTLineReaderType reader =
      SMFTLineReaderList.create(
        URI.create("urn:x"),
        List.of(
          "attribute x",
          "z",
          "end"),
        0);

    final SMFHeader.Builder header_b = SMFHeader.builder();
    final SMFAttribute attribute =
      SMFAttribute.of(
        SMFAttributeName.of("x"), ELEMENT_TYPE_INTEGER_SIGNED, 1, 32);
    header_b.addAttributesInOrder(attribute);
    header_b.setVertexCount(1L);
    final SMFHeader header = header_b.build();

    final SMFTV1BodySectionParserVerticesNonInterleaved cmd =
      new SMFTV1BodySectionParserVerticesNonInterleaved(
        () -> header, reader, state);

    Mockito.when(this.events.onAttributesNonInterleaved())
      .thenReturn(Optional.of(this.eventsData));
    Mockito.when(this.eventsData.onDataAttributeStart(attribute))
      .thenReturn(Optional.of(this.eventsValues));

    final SMFTParsingStatus r =
      cmd.parse(this.events, List.of("vertices", "noninterleaved"));
    Assertions.assertEquals(FAILURE, r);

    Mockito.verify(this.eventsValues).onError(this.captor.capture());
    Assertions.assertTrue(this.captor.getValue().message().contains(
      "Cannot parse"));
    Mockito.verify(this.eventsValues, new Times(1))
      .onDataAttributeValueFinish();
    Mockito.verify(this.eventsData, new Times(1))
      .onDataAttributesNonInterleavedFinish();
  }

  @Test
  public void testAttributeIntegerUnsigned4()
    throws Exception
  {
    final BitSet state = new BitSet();

    final SMFTLineReaderType reader =
      SMFTLineReaderList.create(
        URI.create("urn:x"),
        List.of(
          "attribute x",
          "0 1 2 3",
          "end"),
        0);

    final SMFHeader.Builder header_b = SMFHeader.builder();
    final SMFAttribute attribute =
      SMFAttribute.of(
        SMFAttributeName.of("x"), ELEMENT_TYPE_INTEGER_UNSIGNED, 4, 32);
    header_b.addAttributesInOrder(attribute);
    header_b.setVertexCount(1L);
    final SMFHeader header = header_b.build();

    final SMFTV1BodySectionParserVerticesNonInterleaved cmd =
      new SMFTV1BodySectionParserVerticesNonInterleaved(
        () -> header, reader, state);

    Mockito.when(this.events.onAttributesNonInterleaved())
      .thenReturn(Optional.of(this.eventsData));
    Mockito.when(this.eventsData.onDataAttributeStart(attribute))
      .thenReturn(Optional.of(this.eventsValues));

    final SMFTParsingStatus r =
      cmd.parse(this.events, List.of("vertices", "noninterleaved"));
    Assertions.assertEquals(SUCCESS, r);

    Mockito.verify(this.eventsValues, new Times(1))
      .onDataAttributeValueIntegerUnsigned4(0, 1, 2, 3);
    Mockito.verify(this.eventsValues, new Times(1))
      .onDataAttributeValueFinish();
    Mockito.verify(this.eventsData, new Times(1))
      .onDataAttributesNonInterleavedFinish();
  }

  @Test
  public void testAttributeIntegerUnsigned3()
    throws Exception
  {
    final BitSet state = new BitSet();

    final SMFTLineReaderType reader =
      SMFTLineReaderList.create(
        URI.create("urn:x"),
        List.of(
          "attribute x",
          "0 1 2",
          "end"),
        0);

    final SMFHeader.Builder header_b = SMFHeader.builder();
    final SMFAttribute attribute =
      SMFAttribute.of(
        SMFAttributeName.of("x"), ELEMENT_TYPE_INTEGER_UNSIGNED, 3, 32);
    header_b.addAttributesInOrder(attribute);
    header_b.setVertexCount(1L);
    final SMFHeader header = header_b.build();

    final SMFTV1BodySectionParserVerticesNonInterleaved cmd =
      new SMFTV1BodySectionParserVerticesNonInterleaved(
        () -> header, reader, state);

    Mockito.when(this.events.onAttributesNonInterleaved())
      .thenReturn(Optional.of(this.eventsData));
    Mockito.when(this.eventsData.onDataAttributeStart(attribute))
      .thenReturn(Optional.of(this.eventsValues));

    final SMFTParsingStatus r =
      cmd.parse(this.events, List.of("vertices", "noninterleaved"));
    Assertions.assertEquals(SUCCESS, r);

    Mockito.verify(this.eventsValues, new Times(1))
      .onDataAttributeValueIntegerUnsigned3(0, 1, 2);
    Mockito.verify(this.eventsValues, new Times(1))
      .onDataAttributeValueFinish();
    Mockito.verify(this.eventsData, new Times(1))
      .onDataAttributesNonInterleavedFinish();
  }

  @Test
  public void testAttributeIntegerUnsigned2()
    throws Exception
  {
    final BitSet state = new BitSet();

    final SMFTLineReaderType reader =
      SMFTLineReaderList.create(
        URI.create("urn:x"),
        List.of(
          "attribute x",
          "0 1",
          "end"),
        0);

    final SMFHeader.Builder header_b = SMFHeader.builder();
    final SMFAttribute attribute =
      SMFAttribute.of(
        SMFAttributeName.of("x"), ELEMENT_TYPE_INTEGER_UNSIGNED, 2, 32);
    header_b.addAttributesInOrder(attribute);
    header_b.setVertexCount(1L);
    final SMFHeader header = header_b.build();

    final SMFTV1BodySectionParserVerticesNonInterleaved cmd =
      new SMFTV1BodySectionParserVerticesNonInterleaved(
        () -> header, reader, state);

    Mockito.when(this.events.onAttributesNonInterleaved())
      .thenReturn(Optional.of(this.eventsData));
    Mockito.when(this.eventsData.onDataAttributeStart(attribute))
      .thenReturn(Optional.of(this.eventsValues));

    final SMFTParsingStatus r =
      cmd.parse(this.events, List.of("vertices", "noninterleaved"));
    Assertions.assertEquals(SUCCESS, r);
    Mockito.verify(this.eventsValues, new Times(1))
      .onDataAttributeValueIntegerUnsigned2(0, 1);
    Mockito.verify(this.eventsValues, new Times(1))
      .onDataAttributeValueFinish();
    Mockito.verify(this.eventsData, new Times(1))
      .onDataAttributesNonInterleavedFinish();
  }

  @Test
  public void testAttributeIntegerUnsigned1()
    throws Exception
  {
    final BitSet state = new BitSet();

    final SMFTLineReaderType reader =
      SMFTLineReaderList.create(
        URI.create("urn:x"),
        List.of(
          "attribute x",
          "0",
          "end"),
        0);

    final SMFHeader.Builder header_b = SMFHeader.builder();
    final SMFAttribute attribute =
      SMFAttribute.of(
        SMFAttributeName.of("x"), ELEMENT_TYPE_INTEGER_UNSIGNED, 1, 32);
    header_b.addAttributesInOrder(attribute);
    header_b.setVertexCount(1L);
    final SMFHeader header = header_b.build();

    final SMFTV1BodySectionParserVerticesNonInterleaved cmd =
      new SMFTV1BodySectionParserVerticesNonInterleaved(
        () -> header, reader, state);

    Mockito.when(this.events.onAttributesNonInterleaved())
      .thenReturn(Optional.of(this.eventsData));
    Mockito.when(this.eventsData.onDataAttributeStart(attribute))
      .thenReturn(Optional.of(this.eventsValues));

    final SMFTParsingStatus r =
      cmd.parse(this.events, List.of("vertices", "noninterleaved"));
    Assertions.assertEquals(SUCCESS, r);

    Mockito.verify(this.eventsValues, new Times(1))
      .onDataAttributeValueIntegerUnsigned1(0);
    Mockito.verify(this.eventsValues, new Times(1))
      .onDataAttributeValueFinish();
    Mockito.verify(this.eventsData, new Times(1))
      .onDataAttributesNonInterleavedFinish();
  }

  @Test
  public void testAttributeBadIntegerUnsigned4()
    throws Exception
  {
    final BitSet state = new BitSet();

    final SMFTLineReaderType reader =
      SMFTLineReaderList.create(
        URI.create("urn:x"),
        List.of(
          "attribute x",
          "z 1 2 3",
          "end"),
        0);

    final SMFHeader.Builder header_b = SMFHeader.builder();
    final SMFAttribute attribute =
      SMFAttribute.of(
        SMFAttributeName.of("x"), ELEMENT_TYPE_INTEGER_UNSIGNED, 4, 32);
    header_b.addAttributesInOrder(attribute);
    header_b.setVertexCount(1L);
    final SMFHeader header = header_b.build();

    final SMFTV1BodySectionParserVerticesNonInterleaved cmd =
      new SMFTV1BodySectionParserVerticesNonInterleaved(
        () -> header, reader, state);

    Mockito.when(this.events.onAttributesNonInterleaved())
      .thenReturn(Optional.of(this.eventsData));
    Mockito.when(this.eventsData.onDataAttributeStart(attribute))
      .thenReturn(Optional.of(this.eventsValues));

    final SMFTParsingStatus r =
      cmd.parse(this.events, List.of("vertices", "noninterleaved"));
    Assertions.assertEquals(FAILURE, r);

    Mockito.verify(this.eventsValues).onError(this.captor.capture());
    Assertions.assertTrue(this.captor.getValue().message().contains(
      "Cannot parse"));
    Mockito.verify(this.eventsValues, new Times(1))
      .onDataAttributeValueFinish();
    Mockito.verify(this.eventsData, new Times(1))
      .onDataAttributesNonInterleavedFinish();
  }

  @Test
  public void testAttributeBadIntegerUnsigned3()
    throws Exception
  {
    final BitSet state = new BitSet();

    final SMFTLineReaderType reader =
      SMFTLineReaderList.create(
        URI.create("urn:x"),
        List.of(
          "attribute x",
          "z 1 2",
          "end"),
        0);

    final SMFHeader.Builder header_b = SMFHeader.builder();
    final SMFAttribute attribute =
      SMFAttribute.of(
        SMFAttributeName.of("x"), ELEMENT_TYPE_INTEGER_UNSIGNED, 3, 32);
    header_b.addAttributesInOrder(attribute);
    header_b.setVertexCount(1L);
    final SMFHeader header = header_b.build();

    final SMFTV1BodySectionParserVerticesNonInterleaved cmd =
      new SMFTV1BodySectionParserVerticesNonInterleaved(
        () -> header, reader, state);

    Mockito.when(this.events.onAttributesNonInterleaved())
      .thenReturn(Optional.of(this.eventsData));
    Mockito.when(this.eventsData.onDataAttributeStart(attribute))
      .thenReturn(Optional.of(this.eventsValues));

    final SMFTParsingStatus r =
      cmd.parse(this.events, List.of("vertices", "noninterleaved"));
    Assertions.assertEquals(FAILURE, r);

    Mockito.verify(this.eventsValues).onError(this.captor.capture());
    Assertions.assertTrue(this.captor.getValue().message().contains(
      "Cannot parse"));
    Mockito.verify(this.eventsValues, new Times(1))
      .onDataAttributeValueFinish();
    Mockito.verify(this.eventsData, new Times(1))
      .onDataAttributesNonInterleavedFinish();
  }

  @Test
  public void testAttributeBadIntegerUnsigned2()
    throws Exception
  {
    final BitSet state = new BitSet();

    final SMFTLineReaderType reader =
      SMFTLineReaderList.create(
        URI.create("urn:x"),
        List.of(
          "attribute x",
          "z 1",
          "end"),
        0);

    final SMFHeader.Builder header_b = SMFHeader.builder();
    final SMFAttribute attribute =
      SMFAttribute.of(
        SMFAttributeName.of("x"), ELEMENT_TYPE_INTEGER_UNSIGNED, 2, 32);
    header_b.addAttributesInOrder(attribute);
    header_b.setVertexCount(1L);
    final SMFHeader header = header_b.build();

    final SMFTV1BodySectionParserVerticesNonInterleaved cmd =
      new SMFTV1BodySectionParserVerticesNonInterleaved(
        () -> header, reader, state);

    Mockito.when(this.events.onAttributesNonInterleaved())
      .thenReturn(Optional.of(this.eventsData));
    Mockito.when(this.eventsData.onDataAttributeStart(attribute))
      .thenReturn(Optional.of(this.eventsValues));

    final SMFTParsingStatus r =
      cmd.parse(this.events, List.of("vertices", "noninterleaved"));
    Assertions.assertEquals(FAILURE, r);

    Mockito.verify(this.eventsValues).onError(this.captor.capture());
    Assertions.assertTrue(this.captor.getValue().message().contains(
      "Cannot parse"));
    Mockito.verify(this.eventsValues, new Times(1))
      .onDataAttributeValueFinish();
    Mockito.verify(this.eventsData, new Times(1))
      .onDataAttributesNonInterleavedFinish();
  }

  @Test
  public void testAttributeBadIntegerUnsigned1()
    throws Exception
  {
    final BitSet state = new BitSet();

    final SMFTLineReaderType reader =
      SMFTLineReaderList.create(
        URI.create("urn:x"),
        List.of(
          "attribute x",
          "z",
          "end"),
        0);

    final SMFHeader.Builder header_b = SMFHeader.builder();
    final SMFAttribute attribute =
      SMFAttribute.of(
        SMFAttributeName.of("x"), ELEMENT_TYPE_INTEGER_UNSIGNED, 1, 32);
    header_b.addAttributesInOrder(attribute);
    header_b.setVertexCount(1L);
    final SMFHeader header = header_b.build();

    final SMFTV1BodySectionParserVerticesNonInterleaved cmd =
      new SMFTV1BodySectionParserVerticesNonInterleaved(
        () -> header, reader, state);

    Mockito.when(this.events.onAttributesNonInterleaved())
      .thenReturn(Optional.of(this.eventsData));
    Mockito.when(this.eventsData.onDataAttributeStart(attribute))
      .thenReturn(Optional.of(this.eventsValues));

    final SMFTParsingStatus r =
      cmd.parse(this.events, List.of("vertices", "noninterleaved"));
    Assertions.assertEquals(FAILURE, r);

    Mockito.verify(this.eventsValues).onError(this.captor.capture());
    Assertions.assertTrue(this.captor.getValue().message().contains(
      "Cannot parse"));
    Mockito.verify(this.eventsValues, new Times(1))
      .onDataAttributeValueFinish();
    Mockito.verify(this.eventsData, new Times(1))
      .onDataAttributesNonInterleavedFinish();
  }
}
