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

import com.io7m.smfj.core.SMFAttribute;
import com.io7m.smfj.core.SMFAttributeName;
import com.io7m.smfj.core.SMFComponentType;
import com.io7m.smfj.core.SMFErrorType;
import com.io7m.smfj.core.SMFHeader;
import com.io7m.smfj.core.SMFTriangles;
import com.io7m.smfj.format.binary.SMFBAlignment;
import com.io7m.smfj.format.binary.SMFBDataStreamReader;
import com.io7m.smfj.format.binary.SMFBDataStreamReaderType;
import com.io7m.smfj.format.binary.SMFBSectionVerticesNonInterleaved;
import com.io7m.smfj.format.binary.v1.SMFBv1SectionParserVerticesNonInterleaved;
import com.io7m.smfj.parser.api.SMFParserEventsBodyType;
import com.io7m.smfj.parser.api.SMFParserEventsDataAttributeValuesType;
import com.io7m.smfj.parser.api.SMFParserEventsDataAttributesNonInterleavedType;
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

import static com.io7m.ieee754b16.Binary16.packDouble;

public final class SMFBv1SectionParserVerticesNonInterleavedFloatingTest
{
  private static byte[] makeBytes(
    final int componentSizeOctets,
    final int componentCount,
    final int vertexCount)
  {
    final int buffer_size =
      (int) SMFBAlignment.alignNext(
        (long) (vertexCount * (componentCount * componentSizeOctets)),
        16);
    return new byte[buffer_size];
  }

  private ArgumentCaptor<SMFErrorType> captor0;
  private ArgumentCaptor<SMFErrorType> captor1;
  private SMFParserEventsBodyType eventsBody;
  private SMFParserEventsDataAttributeValuesType eventsValues;
  private SMFParserEventsDataAttributesNonInterleavedType eventsData;
  private SMFParserEventsDataTrianglesType eventsTriangles;

  @BeforeEach
  public void testSetup()
  {
    this.eventsBody =
      Mockito.mock(SMFParserEventsBodyType.class);
    this.eventsTriangles =
      Mockito.mock(SMFParserEventsDataTrianglesType.class);
    this.eventsData =
      Mockito.mock(SMFParserEventsDataAttributesNonInterleavedType.class);
    this.eventsValues =
      Mockito.mock(SMFParserEventsDataAttributeValuesType.class);
    this.captor0 =
      ArgumentCaptor.forClass(SMFErrorType.class);
    this.captor1 =
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

    final SMFAttribute attr0 =
      SMFAttribute.of(
        SMFAttributeName.of("attr0"),
        SMFComponentType.ELEMENT_TYPE_FLOATING,
        4,
        32);

    final SMFHeader header =
      SMFHeader.builder()
        .setVertexCount(1L)
        .addAttributesInOrder(attr0)
        .setTriangles(SMFTriangles.of(0L, 32))
        .build();

    final SMFBv1SectionParserVerticesNonInterleaved p =
      new SMFBv1SectionParserVerticesNonInterleaved(state);
    Assertions.assertEquals(SMFBSectionVerticesNonInterleaved.MAGIC, p.magic());

    Mockito.when(this.eventsBody.onAttributesNonInterleaved())
      .thenReturn(Optional.of(this.eventsData));
    Mockito.when(this.eventsData.onDataAttributeStart(attr0))
      .thenReturn(Optional.of(this.eventsValues));

    p.parse(header, this.eventsBody, reader);

    Mockito.verify(this.eventsValues, new Times(1))
      .onError(this.captor0.capture());
    Mockito.verify(this.eventsData, new Times(1))
      .onError(this.captor1.capture());
    Mockito.verify(this.eventsValues, new Times(1))
      .onDataAttributeValueFinish();
    Mockito.verify(this.eventsData, new Times(1))
      .onDataAttributesNonInterleavedFinish();

    {
      final var e = this.captor0.getValue();
      Assertions.assertTrue(
        e.exception().isPresent()
          && e.exception().get() instanceof IOException
          && e.message().contains("Failed to read the required number of octets"));
    }

    {
      final var e = this.captor1.getValue();
      Assertions.assertTrue(
        e.exception().isPresent()
          && e.exception().get() instanceof IOException
          && e.message().contains("Failed to read the required number of octets"));
    }
  }

  @Test
  public void testVertices_Float4_64()
  {
    final BitSet state = new BitSet();

    final int componentSizeBits = 64;
    final int componentSizeOctets = componentSizeBits / 8;
    final int componentCount = 4;
    final int vertexCount = 2;

    final byte[] buffer = makeBytes(
      componentSizeOctets,
      componentCount,
      vertexCount);
    final ByteBuffer wrap =
      ByteBuffer.wrap(buffer).order(ByteOrder.BIG_ENDIAN);

    wrap.putDouble(10.0);
    wrap.putDouble(20.0);
    wrap.putDouble(30.0);
    wrap.putDouble(40.0);

    wrap.putDouble(11.0);
    wrap.putDouble(21.0);
    wrap.putDouble(31.0);
    wrap.putDouble(41.0);

    final SMFBDataStreamReaderType reader =
      SMFBDataStreamReader.create(
        URI.create("urn:test"),
        new ByteArrayInputStream(buffer));

    final SMFAttribute attr0 =
      SMFAttribute.of(
        SMFAttributeName.of("attr0"),
        SMFComponentType.ELEMENT_TYPE_FLOATING,
        componentCount,
        componentSizeBits);

    final SMFHeader header =
      SMFHeader.builder()
        .setVertexCount(2L)
        .addAttributesInOrder(attr0)
        .build();

    final SMFBv1SectionParserVerticesNonInterleaved p =
      new SMFBv1SectionParserVerticesNonInterleaved(state);
    Assertions.assertEquals(SMFBSectionVerticesNonInterleaved.MAGIC, p.magic());

    Mockito.when(this.eventsBody.onAttributesNonInterleaved())
      .thenReturn(Optional.of(this.eventsData));
    Mockito.when(this.eventsData.onDataAttributeStart(attr0))
      .thenReturn(Optional.of(this.eventsValues));

    p.parse(header, this.eventsBody, reader);

    Mockito.verify(this.eventsValues, new Times(1))
      .onDataAttributeValueFloat4(10.0, 20.0, 30.0, 40.0);
    Mockito.verify(this.eventsValues, new Times(1))
      .onDataAttributeValueFloat4(11.0, 21.0, 31.0, 41.0);
    Mockito.verify(this.eventsValues, new Times(1))
      .onDataAttributeValueFinish();
    Mockito.verify(this.eventsData, new Times(1))
      .onDataAttributesNonInterleavedFinish();
  }

  @Test
  public void testVertices_Float3_64()
  {
    final BitSet state = new BitSet();

    final int componentSizeBits = 64;
    final int componentSizeOctets = componentSizeBits / 8;
    final int componentCount = 3;
    final int vertexCount = 2;

    final byte[] buffer =
      makeBytes(componentSizeOctets, componentCount, vertexCount);
    final ByteBuffer wrap =
      ByteBuffer.wrap(buffer).order(ByteOrder.BIG_ENDIAN);

    wrap.putDouble(10.0);
    wrap.putDouble(20.0);
    wrap.putDouble(30.0);

    wrap.putDouble(11.0);
    wrap.putDouble(21.0);
    wrap.putDouble(31.0);

    final SMFBDataStreamReaderType reader =
      SMFBDataStreamReader.create(
        URI.create("urn:test"),
        new ByteArrayInputStream(buffer));

    final SMFAttribute attr0 =
      SMFAttribute.of(
        SMFAttributeName.of("attr0"),
        SMFComponentType.ELEMENT_TYPE_FLOATING,
        componentCount,
        componentSizeBits);

    final SMFHeader header =
      SMFHeader.builder()
        .setVertexCount(2L)
        .addAttributesInOrder(attr0)
        .build();

    final SMFBv1SectionParserVerticesNonInterleaved p =
      new SMFBv1SectionParserVerticesNonInterleaved(state);
    Assertions.assertEquals(SMFBSectionVerticesNonInterleaved.MAGIC, p.magic());

    Mockito.when(this.eventsBody.onAttributesNonInterleaved())
      .thenReturn(Optional.of(this.eventsData));
    Mockito.when(this.eventsData.onDataAttributeStart(attr0))
      .thenReturn(Optional.of(this.eventsValues));

    p.parse(header, this.eventsBody, reader);

    Mockito.verify(this.eventsValues, new Times(1))
      .onDataAttributeValueFloat3(10.0, 20.0, 30.0);
    Mockito.verify(this.eventsValues, new Times(1))
      .onDataAttributeValueFloat3(11.0, 21.0, 31.0);
    Mockito.verify(this.eventsValues, new Times(1))
      .onDataAttributeValueFinish();
    Mockito.verify(this.eventsData, new Times(1))
      .onDataAttributesNonInterleavedFinish();
  }

  @Test
  public void testVertices_Float2_64()
  {
    final BitSet state = new BitSet();

    final int componentSizeBits = 64;
    final int componentSizeOctets = componentSizeBits / 8;
    final int componentCount = 2;
    final int vertexCount = 2;

    final byte[] buffer =
      makeBytes(componentSizeOctets, componentCount, vertexCount);
    final ByteBuffer wrap =
      ByteBuffer.wrap(buffer).order(ByteOrder.BIG_ENDIAN);

    wrap.putDouble(10.0);
    wrap.putDouble(20.0);

    wrap.putDouble(11.0);
    wrap.putDouble(21.0);

    final SMFBDataStreamReaderType reader =
      SMFBDataStreamReader.create(
        URI.create("urn:test"),
        new ByteArrayInputStream(buffer));

    final SMFAttribute attr0 =
      SMFAttribute.of(
        SMFAttributeName.of("attr0"),
        SMFComponentType.ELEMENT_TYPE_FLOATING,
        componentCount,
        componentSizeBits);

    final SMFHeader header =
      SMFHeader.builder()
        .setVertexCount(2L)
        .addAttributesInOrder(attr0)
        .build();

    final SMFBv1SectionParserVerticesNonInterleaved p =
      new SMFBv1SectionParserVerticesNonInterleaved(state);
    Assertions.assertEquals(SMFBSectionVerticesNonInterleaved.MAGIC, p.magic());

    Mockito.when(this.eventsBody.onAttributesNonInterleaved())
      .thenReturn(Optional.of(this.eventsData));
    Mockito.when(this.eventsData.onDataAttributeStart(attr0))
      .thenReturn(Optional.of(this.eventsValues));

    p.parse(header, this.eventsBody, reader);

    Mockito.verify(this.eventsValues, new Times(1))
      .onDataAttributeValueFloat2(10.0, 20.0);
    Mockito.verify(this.eventsValues, new Times(1))
      .onDataAttributeValueFloat2(11.0, 21.0);
    Mockito.verify(this.eventsValues, new Times(1))
      .onDataAttributeValueFinish();
    Mockito.verify(this.eventsData, new Times(1))
      .onDataAttributesNonInterleavedFinish();
  }

  @Test
  public void testVertices_Float1_64()
  {
    final BitSet state = new BitSet();

    final int componentSizeBits = 64;
    final int componentSizeOctets = componentSizeBits / 8;
    final int componentCount = 1;
    final int vertexCount = 2;

    final byte[] buffer =
      makeBytes(componentSizeOctets, componentCount, vertexCount);
    final ByteBuffer wrap =
      ByteBuffer.wrap(buffer).order(ByteOrder.BIG_ENDIAN);

    wrap.putDouble(10.0);

    wrap.putDouble(11.0);

    final SMFBDataStreamReaderType reader =
      SMFBDataStreamReader.create(
        URI.create("urn:test"),
        new ByteArrayInputStream(buffer));

    final SMFAttribute attr0 =
      SMFAttribute.of(
        SMFAttributeName.of("attr0"),
        SMFComponentType.ELEMENT_TYPE_FLOATING,
        componentCount,
        componentSizeBits);

    final SMFHeader header =
      SMFHeader.builder()
        .setVertexCount(2L)
        .addAttributesInOrder(attr0)
        .build();

    final SMFBv1SectionParserVerticesNonInterleaved p =
      new SMFBv1SectionParserVerticesNonInterleaved(state);
    Assertions.assertEquals(SMFBSectionVerticesNonInterleaved.MAGIC, p.magic());

    Mockito.when(this.eventsBody.onAttributesNonInterleaved())
      .thenReturn(Optional.of(this.eventsData));
    Mockito.when(this.eventsData.onDataAttributeStart(attr0))
      .thenReturn(Optional.of(this.eventsValues));

    p.parse(header, this.eventsBody, reader);

    Mockito.verify(this.eventsValues, new Times(1))
      .onDataAttributeValueFloat1(10.0);
    Mockito.verify(this.eventsValues, new Times(1))
      .onDataAttributeValueFloat1(11.0);
    Mockito.verify(this.eventsValues, new Times(1))
      .onDataAttributeValueFinish();
    Mockito.verify(this.eventsData, new Times(1))
      .onDataAttributesNonInterleavedFinish();
  }

  @Test
  public void testVertices_Float4_32()
  {
    final BitSet state = new BitSet();

    final int componentSizeBits = 32;
    final int componentSizeOctets = componentSizeBits / 8;
    final int componentCount = 4;
    final int vertexCount = 2;

    final byte[] buffer =
      makeBytes(componentSizeOctets, componentCount, vertexCount);
    final ByteBuffer wrap =
      ByteBuffer.wrap(buffer).order(ByteOrder.BIG_ENDIAN);

    wrap.putFloat(10.0f);
    wrap.putFloat(20.0f);
    wrap.putFloat(30.0f);
    wrap.putFloat(40.0f);

    wrap.putFloat(11.0f);
    wrap.putFloat(21.0f);
    wrap.putFloat(31.0f);
    wrap.putFloat(41.0f);

    final SMFBDataStreamReaderType reader =
      SMFBDataStreamReader.create(
        URI.create("urn:test"),
        new ByteArrayInputStream(buffer));

    final SMFAttribute attr0 =
      SMFAttribute.of(
        SMFAttributeName.of("attr0"),
        SMFComponentType.ELEMENT_TYPE_FLOATING,
        componentCount,
        componentSizeBits);

    final SMFHeader header =
      SMFHeader.builder()
        .setVertexCount(2L)
        .addAttributesInOrder(attr0)
        .build();

    final SMFBv1SectionParserVerticesNonInterleaved p =
      new SMFBv1SectionParserVerticesNonInterleaved(state);
    Assertions.assertEquals(SMFBSectionVerticesNonInterleaved.MAGIC, p.magic());

    Mockito.when(this.eventsBody.onAttributesNonInterleaved())
      .thenReturn(Optional.of(this.eventsData));
    Mockito.when(this.eventsData.onDataAttributeStart(attr0))
      .thenReturn(Optional.of(this.eventsValues));

    p.parse(header, this.eventsBody, reader);

    Mockito.verify(this.eventsValues, new Times(1))
      .onDataAttributeValueFloat4(10.0, 20.0, 30.0, 40.0);
    Mockito.verify(this.eventsValues, new Times(1))
      .onDataAttributeValueFloat4(11.0, 21.0, 31.0, 41.0);
    Mockito.verify(this.eventsValues, new Times(1))
      .onDataAttributeValueFinish();
    Mockito.verify(this.eventsData, new Times(1))
      .onDataAttributesNonInterleavedFinish();
  }

  @Test
  public void testVertices_Float3_32()
  {
    final BitSet state = new BitSet();

    final int componentSizeBits = 32;
    final int componentSizeOctets = componentSizeBits / 8;
    final int componentCount = 3;
    final int vertexCount = 2;

    final byte[] buffer =
      makeBytes(componentSizeOctets, componentCount, vertexCount);
    final ByteBuffer wrap =
      ByteBuffer.wrap(buffer).order(ByteOrder.BIG_ENDIAN);

    wrap.putFloat(10.0f);
    wrap.putFloat(20.0f);
    wrap.putFloat(30.0f);

    wrap.putFloat(11.0f);
    wrap.putFloat(21.0f);
    wrap.putFloat(31.0f);

    final SMFBDataStreamReaderType reader =
      SMFBDataStreamReader.create(
        URI.create("urn:test"),
        new ByteArrayInputStream(buffer));

    final SMFAttribute attr0 =
      SMFAttribute.of(
        SMFAttributeName.of("attr0"),
        SMFComponentType.ELEMENT_TYPE_FLOATING,
        componentCount,
        componentSizeBits);

    final SMFHeader header =
      SMFHeader.builder()
        .setVertexCount(2L)
        .addAttributesInOrder(attr0)
        .build();

    final SMFBv1SectionParserVerticesNonInterleaved p =
      new SMFBv1SectionParserVerticesNonInterleaved(state);
    Assertions.assertEquals(SMFBSectionVerticesNonInterleaved.MAGIC, p.magic());

    Mockito.when(this.eventsBody.onAttributesNonInterleaved())
      .thenReturn(Optional.of(this.eventsData));
    Mockito.when(this.eventsData.onDataAttributeStart(attr0))
      .thenReturn(Optional.of(this.eventsValues));

    p.parse(header, this.eventsBody, reader);

    Mockito.verify(this.eventsValues, new Times(1))
      .onDataAttributeValueFloat3(10.0, 20.0, 30.0);
    Mockito.verify(this.eventsValues, new Times(1))
      .onDataAttributeValueFloat3(11.0, 21.0, 31.0);
    Mockito.verify(this.eventsValues, new Times(1))
      .onDataAttributeValueFinish();
    Mockito.verify(this.eventsData, new Times(1))
      .onDataAttributesNonInterleavedFinish();
  }

  @Test
  public void testVertices_Float2_32()
  {
    final BitSet state = new BitSet();

    final int componentSizeBits = 32;
    final int componentSizeOctets = componentSizeBits / 8;
    final int componentCount = 2;
    final int vertexCount = 2;

    final byte[] buffer =
      makeBytes(componentSizeOctets, componentCount, vertexCount);
    final ByteBuffer wrap =
      ByteBuffer.wrap(buffer).order(ByteOrder.BIG_ENDIAN);

    wrap.putFloat(10.0f);
    wrap.putFloat(20.0f);

    wrap.putFloat(11.0f);
    wrap.putFloat(21.0f);

    final SMFBDataStreamReaderType reader =
      SMFBDataStreamReader.create(
        URI.create("urn:test"),
        new ByteArrayInputStream(buffer));

    final SMFAttribute attr0 =
      SMFAttribute.of(
        SMFAttributeName.of("attr0"),
        SMFComponentType.ELEMENT_TYPE_FLOATING,
        componentCount,
        componentSizeBits);

    final SMFHeader header =
      SMFHeader.builder()
        .setVertexCount(2L)
        .addAttributesInOrder(attr0)
        .build();

    final SMFBv1SectionParserVerticesNonInterleaved p =
      new SMFBv1SectionParserVerticesNonInterleaved(state);
    Assertions.assertEquals(SMFBSectionVerticesNonInterleaved.MAGIC, p.magic());

    Mockito.when(this.eventsBody.onAttributesNonInterleaved())
      .thenReturn(Optional.of(this.eventsData));
    Mockito.when(this.eventsData.onDataAttributeStart(attr0))
      .thenReturn(Optional.of(this.eventsValues));

    p.parse(header, this.eventsBody, reader);

    Mockito.verify(this.eventsValues, new Times(1))
      .onDataAttributeValueFloat2(10.0, 20.0);
    Mockito.verify(this.eventsValues, new Times(1))
      .onDataAttributeValueFloat2(11.0, 21.0);
    Mockito.verify(this.eventsValues, new Times(1))
      .onDataAttributeValueFinish();
    Mockito.verify(this.eventsData, new Times(1))
      .onDataAttributesNonInterleavedFinish();
  }

  @Test
  public void testVertices_Float1_32()
  {
    final BitSet state = new BitSet();

    final int componentSizeBits = 32;
    final int componentSizeOctets = componentSizeBits / 8;
    final int componentCount = 1;
    final int vertexCount = 2;

    final byte[] buffer =
      makeBytes(componentSizeOctets, componentCount, vertexCount);
    final ByteBuffer wrap =
      ByteBuffer.wrap(buffer).order(ByteOrder.BIG_ENDIAN);

    wrap.putFloat(10.0f);

    wrap.putFloat(11.0f);

    final SMFBDataStreamReaderType reader =
      SMFBDataStreamReader.create(
        URI.create("urn:test"),
        new ByteArrayInputStream(buffer));

    final SMFAttribute attr0 =
      SMFAttribute.of(
        SMFAttributeName.of("attr0"),
        SMFComponentType.ELEMENT_TYPE_FLOATING,
        componentCount,
        componentSizeBits);

    final SMFHeader header =
      SMFHeader.builder()
        .setVertexCount(2L)
        .addAttributesInOrder(attr0)
        .build();

    final SMFBv1SectionParserVerticesNonInterleaved p =
      new SMFBv1SectionParserVerticesNonInterleaved(state);
    Assertions.assertEquals(SMFBSectionVerticesNonInterleaved.MAGIC, p.magic());

    Mockito.when(this.eventsBody.onAttributesNonInterleaved())
      .thenReturn(Optional.of(this.eventsData));
    Mockito.when(this.eventsData.onDataAttributeStart(attr0))
      .thenReturn(Optional.of(this.eventsValues));

    p.parse(header, this.eventsBody, reader);

    Mockito.verify(this.eventsValues, new Times(1))
      .onDataAttributeValueFloat1(10.0);
    Mockito.verify(this.eventsValues, new Times(1))
      .onDataAttributeValueFloat1(11.0);
    Mockito.verify(this.eventsValues, new Times(1))
      .onDataAttributeValueFinish();
    Mockito.verify(this.eventsData, new Times(1))
      .onDataAttributesNonInterleavedFinish();
  }

  @Test
  public void testVertices_Float4_16()
  {
    final BitSet state = new BitSet();

    final int componentSizeBits = 16;
    final int componentSizeOctets = componentSizeBits / 8;
    final int componentCount = 4;
    final int vertexCount = 2;

    final byte[] buffer =
      makeBytes(componentSizeOctets, componentCount, vertexCount);
    final ByteBuffer wrap =
      ByteBuffer.wrap(buffer).order(ByteOrder.BIG_ENDIAN);

    wrap.putChar(packDouble(10.0));
    wrap.putChar(packDouble(20.0));
    wrap.putChar(packDouble(30.0));
    wrap.putChar(packDouble(40.0));

    wrap.putChar(packDouble(11.0));
    wrap.putChar(packDouble(21.0));
    wrap.putChar(packDouble(31.0));
    wrap.putChar(packDouble(41.0));

    final SMFBDataStreamReaderType reader =
      SMFBDataStreamReader.create(
        URI.create("urn:test"),
        new ByteArrayInputStream(buffer));

    final SMFAttribute attr0 =
      SMFAttribute.of(
        SMFAttributeName.of("attr0"),
        SMFComponentType.ELEMENT_TYPE_FLOATING,
        componentCount,
        componentSizeBits);

    final SMFHeader header =
      SMFHeader.builder()
        .setVertexCount(2L)
        .addAttributesInOrder(attr0)
        .build();

    final SMFBv1SectionParserVerticesNonInterleaved p =
      new SMFBv1SectionParserVerticesNonInterleaved(state);
    Assertions.assertEquals(SMFBSectionVerticesNonInterleaved.MAGIC, p.magic());

    Mockito.when(this.eventsBody.onAttributesNonInterleaved())
      .thenReturn(Optional.of(this.eventsData));
    Mockito.when(this.eventsData.onDataAttributeStart(attr0))
      .thenReturn(Optional.of(this.eventsValues));

    p.parse(header, this.eventsBody, reader);

    Mockito.verify(this.eventsValues, new Times(1))
      .onDataAttributeValueFloat4(10.0, 20.0, 30.0, 40.0);
    Mockito.verify(this.eventsValues, new Times(1))
      .onDataAttributeValueFloat4(11.0, 21.0, 31.0, 41.0);
    Mockito.verify(this.eventsValues, new Times(1))
      .onDataAttributeValueFinish();
    Mockito.verify(this.eventsData, new Times(1))
      .onDataAttributesNonInterleavedFinish();
  }

  @Test
  public void testVertices_Float3_16()
  {
    final BitSet state = new BitSet();

    final int componentSizeBits = 16;
    final int componentSizeOctets = componentSizeBits / 8;
    final int componentCount = 3;
    final int vertexCount = 2;

    final byte[] buffer =
      makeBytes(componentSizeOctets, componentCount, vertexCount);
    final ByteBuffer wrap =
      ByteBuffer.wrap(buffer).order(ByteOrder.BIG_ENDIAN);

    wrap.putChar(packDouble(10.0));
    wrap.putChar(packDouble(20.0));
    wrap.putChar(packDouble(30.0));

    wrap.putChar(packDouble(11.0));
    wrap.putChar(packDouble(21.0));
    wrap.putChar(packDouble(31.0));

    final SMFBDataStreamReaderType reader =
      SMFBDataStreamReader.create(
        URI.create("urn:test"),
        new ByteArrayInputStream(buffer));

    final SMFAttribute attr0 =
      SMFAttribute.of(
        SMFAttributeName.of("attr0"),
        SMFComponentType.ELEMENT_TYPE_FLOATING,
        componentCount,
        componentSizeBits);

    final SMFHeader header =
      SMFHeader.builder()
        .setVertexCount(2L)
        .addAttributesInOrder(attr0)
        .build();

    final SMFBv1SectionParserVerticesNonInterleaved p =
      new SMFBv1SectionParserVerticesNonInterleaved(state);
    Assertions.assertEquals(SMFBSectionVerticesNonInterleaved.MAGIC, p.magic());

    Mockito.when(this.eventsBody.onAttributesNonInterleaved())
      .thenReturn(Optional.of(this.eventsData));
    Mockito.when(this.eventsData.onDataAttributeStart(attr0))
      .thenReturn(Optional.of(this.eventsValues));

    p.parse(header, this.eventsBody, reader);

    Mockito.verify(this.eventsValues, new Times(1))
      .onDataAttributeValueFloat3(10.0, 20.0, 30.0);
    Mockito.verify(this.eventsValues, new Times(1))
      .onDataAttributeValueFloat3(11.0, 21.0, 31.0);
    Mockito.verify(this.eventsValues, new Times(1))
      .onDataAttributeValueFinish();
    Mockito.verify(this.eventsData, new Times(1))
      .onDataAttributesNonInterleavedFinish();
  }

  @Test
  public void testVertices_Float2_16()
  {
    final BitSet state = new BitSet();

    final int componentSizeBits = 16;
    final int componentSizeOctets = componentSizeBits / 8;
    final int componentCount = 2;
    final int vertexCount = 2;

    final byte[] buffer =
      makeBytes(componentSizeOctets, componentCount, vertexCount);
    final ByteBuffer wrap =
      ByteBuffer.wrap(buffer).order(ByteOrder.BIG_ENDIAN);

    wrap.putChar(packDouble(10.0));
    wrap.putChar(packDouble(20.0));

    wrap.putChar(packDouble(11.0));
    wrap.putChar(packDouble(21.0));

    final SMFBDataStreamReaderType reader =
      SMFBDataStreamReader.create(
        URI.create("urn:test"),
        new ByteArrayInputStream(buffer));

    final SMFAttribute attr0 =
      SMFAttribute.of(
        SMFAttributeName.of("attr0"),
        SMFComponentType.ELEMENT_TYPE_FLOATING,
        componentCount,
        componentSizeBits);

    final SMFHeader header =
      SMFHeader.builder()
        .setVertexCount(2L)
        .addAttributesInOrder(attr0)
        .build();

    final SMFBv1SectionParserVerticesNonInterleaved p =
      new SMFBv1SectionParserVerticesNonInterleaved(state);
    Assertions.assertEquals(SMFBSectionVerticesNonInterleaved.MAGIC, p.magic());

    Mockito.when(this.eventsBody.onAttributesNonInterleaved())
      .thenReturn(Optional.of(this.eventsData));
    Mockito.when(this.eventsData.onDataAttributeStart(attr0))
      .thenReturn(Optional.of(this.eventsValues));

    p.parse(header, this.eventsBody, reader);

    Mockito.verify(this.eventsValues, new Times(1))
      .onDataAttributeValueFloat2(10.0, 20.0);
    Mockito.verify(this.eventsValues, new Times(1))
      .onDataAttributeValueFloat2(11.0, 21.0);
    Mockito.verify(this.eventsValues, new Times(1))
      .onDataAttributeValueFinish();
    Mockito.verify(this.eventsData, new Times(1))
      .onDataAttributesNonInterleavedFinish();
  }

  @Test
  public void testVertices_Float1_16()
  {
    final BitSet state = new BitSet();

    final int componentSizeBits = 16;
    final int componentSizeOctets = componentSizeBits / 8;
    final int componentCount = 1;
    final int vertexCount = 2;

    final byte[] buffer =
      makeBytes(componentSizeOctets, componentCount, vertexCount);
    final ByteBuffer wrap =
      ByteBuffer.wrap(buffer).order(ByteOrder.BIG_ENDIAN);

    wrap.putChar(packDouble(10.0));

    wrap.putChar(packDouble(11.0));

    final SMFBDataStreamReaderType reader =
      SMFBDataStreamReader.create(
        URI.create("urn:test"),
        new ByteArrayInputStream(buffer));

    final SMFAttribute attr0 =
      SMFAttribute.of(
        SMFAttributeName.of("attr0"),
        SMFComponentType.ELEMENT_TYPE_FLOATING,
        componentCount,
        componentSizeBits);

    final SMFHeader header =
      SMFHeader.builder()
        .setVertexCount(2L)
        .addAttributesInOrder(attr0)
        .build();

    final SMFBv1SectionParserVerticesNonInterleaved p =
      new SMFBv1SectionParserVerticesNonInterleaved(state);
    Assertions.assertEquals(SMFBSectionVerticesNonInterleaved.MAGIC, p.magic());

    Mockito.when(this.eventsBody.onAttributesNonInterleaved())
      .thenReturn(Optional.of(this.eventsData));
    Mockito.when(this.eventsData.onDataAttributeStart(attr0))
      .thenReturn(Optional.of(this.eventsValues));

    p.parse(header, this.eventsBody, reader);

    Mockito.verify(this.eventsValues, new Times(1))
      .onDataAttributeValueFloat1(10.0);
    Mockito.verify(this.eventsValues, new Times(1))
      .onDataAttributeValueFloat1(11.0);
    Mockito.verify(this.eventsValues, new Times(1))
      .onDataAttributeValueFinish();
    Mockito.verify(this.eventsData, new Times(1))
      .onDataAttributesNonInterleavedFinish();
  }
}
