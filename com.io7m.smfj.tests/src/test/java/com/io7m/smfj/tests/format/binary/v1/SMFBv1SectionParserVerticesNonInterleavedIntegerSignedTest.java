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
import mockit.Delegate;
import mockit.Mocked;
import mockit.Expectations;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URI;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.BitSet;
import java.util.Optional;

public final class SMFBv1SectionParserVerticesNonInterleavedIntegerSignedTest
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

  @Test
  public void testEmpty(
    final @Mocked SMFParserEventsDataAttributesNonInterleavedType events_ni,
    final @Mocked SMFParserEventsDataAttributeValuesType events_values,
    final @Mocked SMFParserEventsBodyType events_body)
  {
    final BitSet state = new BitSet();

    final SMFBDataStreamReaderType reader =
      SMFBDataStreamReader.create(
        URI.create("urn:test"),
        new ByteArrayInputStream(new byte[]{}));

    final SMFAttribute attr0 =
      SMFAttribute.of(
        SMFAttributeName.of("attr0"),
        SMFComponentType.ELEMENT_TYPE_INTEGER_SIGNED,
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

    new Expectations()
    {{
      events_body.onAttributesNonInterleaved();
      this.result = Optional.of(events_ni);

      events_ni.onDataAttributeStart(attr0);
      this.result = Optional.of(events_values);

      events_values.onError(this.with(new Delegate<SMFErrorType>()
      {
        boolean check(final SMFErrorType e)
        {
          return e.exception().isPresent()
            && e.exception().get() instanceof IOException
            && e.message().contains(
            "Failed to read the required number of octets");
        }
      }));

      events_values.onDataAttributeValueFinish();

      events_ni.onError(this.with(new Delegate<SMFErrorType>()
      {
        boolean check(final SMFErrorType e)
        {
          return e.exception().isPresent()
            && e.exception().get() instanceof IOException
            && e.message().contains(
            "Failed to read the required number of octets");
        }
      }));

      events_ni.onDataAttributesNonInterleavedFinish();
    }};

    p.parse(header, events_body, reader);
  }

  @Test
  public void testVertices_IntegerSigned4_64(
    final @Mocked SMFParserEventsDataAttributesNonInterleavedType events_ni,
    final @Mocked SMFParserEventsDataAttributeValuesType events_values,
    final @Mocked SMFParserEventsBodyType events_body)
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

    wrap.putLong(10L);
    wrap.putLong(20L);
    wrap.putLong(30L);
    wrap.putLong(40L);

    wrap.putLong(11L);
    wrap.putLong(21L);
    wrap.putLong(31L);
    wrap.putLong(41L);

    final SMFBDataStreamReaderType reader =
      SMFBDataStreamReader.create(
        URI.create("urn:test"),
        new ByteArrayInputStream(buffer));

    final SMFAttribute attr0 =
      SMFAttribute.of(
        SMFAttributeName.of("attr0"),
        SMFComponentType.ELEMENT_TYPE_INTEGER_SIGNED,
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

    new Expectations()
    {{
      events_body.onAttributesNonInterleaved();
      this.result = Optional.of(events_ni);

      events_ni.onDataAttributeStart(attr0);
      this.result = Optional.of(events_values);

      events_values.onDataAttributeValueIntegerSigned4(
        10L, 20L, 30L, 40L);
      events_values.onDataAttributeValueIntegerSigned4(
        11L, 21L, 31L, 41L);
      events_values.onDataAttributeValueFinish();

      events_ni.onDataAttributesNonInterleavedFinish();
    }};

    p.parse(header, events_body, reader);
  }

  @Test
  public void testVertices_IntegerSigned3_64(
    final @Mocked SMFParserEventsDataAttributesNonInterleavedType events_ni,
    final @Mocked SMFParserEventsDataAttributeValuesType events_values,
    final @Mocked SMFParserEventsBodyType events_body)
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

    wrap.putLong(10L);
    wrap.putLong(20L);
    wrap.putLong(30L);

    wrap.putLong(11L);
    wrap.putLong(21L);
    wrap.putLong(31L);

    final SMFBDataStreamReaderType reader =
      SMFBDataStreamReader.create(
        URI.create("urn:test"),
        new ByteArrayInputStream(buffer));

    final SMFAttribute attr0 =
      SMFAttribute.of(
        SMFAttributeName.of("attr0"),
        SMFComponentType.ELEMENT_TYPE_INTEGER_SIGNED,
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

    new Expectations()
    {{
      events_body.onAttributesNonInterleaved();
      this.result = Optional.of(events_ni);

      events_ni.onDataAttributeStart(attr0);
      this.result = Optional.of(events_values);

      events_values.onDataAttributeValueIntegerSigned3(
        10L, 20L, 30L);
      events_values.onDataAttributeValueIntegerSigned3(
        11L, 21L, 31L);
      events_values.onDataAttributeValueFinish();

      events_ni.onDataAttributesNonInterleavedFinish();
    }};

    p.parse(header, events_body, reader);
  }

  @Test
  public void testVertices_IntegerSigned2_64(
    final @Mocked SMFParserEventsDataAttributesNonInterleavedType events_ni,
    final @Mocked SMFParserEventsDataAttributeValuesType events_values,
    final @Mocked SMFParserEventsBodyType events_body)
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

    wrap.putLong(10L);
    wrap.putLong(20L);

    wrap.putLong(11L);
    wrap.putLong(21L);

    final SMFBDataStreamReaderType reader =
      SMFBDataStreamReader.create(
        URI.create("urn:test"),
        new ByteArrayInputStream(buffer));

    final SMFAttribute attr0 =
      SMFAttribute.of(
        SMFAttributeName.of("attr0"),
        SMFComponentType.ELEMENT_TYPE_INTEGER_SIGNED,
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

    new Expectations()
    {{
      events_body.onAttributesNonInterleaved();
      this.result = Optional.of(events_ni);

      events_ni.onDataAttributeStart(attr0);
      this.result = Optional.of(events_values);

      events_values.onDataAttributeValueIntegerSigned2(
        10L, 20L);
      events_values.onDataAttributeValueIntegerSigned2(
        11L, 21L);
      events_values.onDataAttributeValueFinish();

      events_ni.onDataAttributesNonInterleavedFinish();
    }};

    p.parse(header, events_body, reader);
  }

  @Test
  public void testVertices_IntegerSigned1_64(
    final @Mocked SMFParserEventsDataAttributesNonInterleavedType events_ni,
    final @Mocked SMFParserEventsDataAttributeValuesType events_values,
    final @Mocked SMFParserEventsBodyType events_body)
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

    wrap.putLong(10L);

    wrap.putLong(11L);

    final SMFBDataStreamReaderType reader =
      SMFBDataStreamReader.create(
        URI.create("urn:test"),
        new ByteArrayInputStream(buffer));

    final SMFAttribute attr0 =
      SMFAttribute.of(
        SMFAttributeName.of("attr0"),
        SMFComponentType.ELEMENT_TYPE_INTEGER_SIGNED,
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

    new Expectations()
    {{
      events_body.onAttributesNonInterleaved();
      this.result = Optional.of(events_ni);

      events_ni.onDataAttributeStart(attr0);
      this.result = Optional.of(events_values);

      events_values.onDataAttributeValueIntegerSigned1(
        10L);
      events_values.onDataAttributeValueIntegerSigned1(
        11L);
      events_values.onDataAttributeValueFinish();

      events_ni.onDataAttributesNonInterleavedFinish();
    }};

    p.parse(header, events_body, reader);
  }


  @Test
  public void testVertices_IntegerSigned4_32(
    final @Mocked SMFParserEventsDataAttributesNonInterleavedType events_ni,
    final @Mocked SMFParserEventsDataAttributeValuesType events_values,
    final @Mocked SMFParserEventsBodyType events_body)
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

    wrap.putInt(10);
    wrap.putInt(20);
    wrap.putInt(30);
    wrap.putInt(40);

    wrap.putInt(11);
    wrap.putInt(21);
    wrap.putInt(31);
    wrap.putInt(41);

    final SMFBDataStreamReaderType reader =
      SMFBDataStreamReader.create(
        URI.create("urn:test"),
        new ByteArrayInputStream(buffer));

    final SMFAttribute attr0 =
      SMFAttribute.of(
        SMFAttributeName.of("attr0"),
        SMFComponentType.ELEMENT_TYPE_INTEGER_SIGNED,
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

    new Expectations()
    {{
      events_body.onAttributesNonInterleaved();
      this.result = Optional.of(events_ni);

      events_ni.onDataAttributeStart(attr0);
      this.result = Optional.of(events_values);

      events_values.onDataAttributeValueIntegerSigned4(
        10L, 20L, 30L, 40L);
      events_values.onDataAttributeValueIntegerSigned4(
        11L, 21L, 31L, 41L);
      events_values.onDataAttributeValueFinish();

      events_ni.onDataAttributesNonInterleavedFinish();
    }};

    p.parse(header, events_body, reader);
  }

  @Test
  public void testVertices_IntegerSigned3_32(
    final @Mocked SMFParserEventsDataAttributesNonInterleavedType events_ni,
    final @Mocked SMFParserEventsDataAttributeValuesType events_values,
    final @Mocked SMFParserEventsBodyType events_body)
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

    wrap.putInt(10);
    wrap.putInt(20);
    wrap.putInt(30);

    wrap.putInt(11);
    wrap.putInt(21);
    wrap.putInt(31);

    final SMFBDataStreamReaderType reader =
      SMFBDataStreamReader.create(
        URI.create("urn:test"),
        new ByteArrayInputStream(buffer));

    final SMFAttribute attr0 =
      SMFAttribute.of(
        SMFAttributeName.of("attr0"),
        SMFComponentType.ELEMENT_TYPE_INTEGER_SIGNED,
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

    new Expectations()
    {{
      events_body.onAttributesNonInterleaved();
      this.result = Optional.of(events_ni);

      events_ni.onDataAttributeStart(attr0);
      this.result = Optional.of(events_values);

      events_values.onDataAttributeValueIntegerSigned3(
        10L, 20L, 30L);
      events_values.onDataAttributeValueIntegerSigned3(
        11L, 21L, 31L);
      events_values.onDataAttributeValueFinish();

      events_ni.onDataAttributesNonInterleavedFinish();
    }};

    p.parse(header, events_body, reader);
  }

  @Test
  public void testVertices_IntegerSigned2_32(
    final @Mocked SMFParserEventsDataAttributesNonInterleavedType events_ni,
    final @Mocked SMFParserEventsDataAttributeValuesType events_values,
    final @Mocked SMFParserEventsBodyType events_body)
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

    wrap.putInt(10);
    wrap.putInt(20);

    wrap.putInt(11);
    wrap.putInt(21);

    final SMFBDataStreamReaderType reader =
      SMFBDataStreamReader.create(
        URI.create("urn:test"),
        new ByteArrayInputStream(buffer));

    final SMFAttribute attr0 =
      SMFAttribute.of(
        SMFAttributeName.of("attr0"),
        SMFComponentType.ELEMENT_TYPE_INTEGER_SIGNED,
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

    new Expectations()
    {{
      events_body.onAttributesNonInterleaved();
      this.result = Optional.of(events_ni);

      events_ni.onDataAttributeStart(attr0);
      this.result = Optional.of(events_values);

      events_values.onDataAttributeValueIntegerSigned2(
        10L, 20L);
      events_values.onDataAttributeValueIntegerSigned2(
        11L, 21L);
      events_values.onDataAttributeValueFinish();

      events_ni.onDataAttributesNonInterleavedFinish();
    }};

    p.parse(header, events_body, reader);
  }

  @Test
  public void testVertices_IntegerSigned1_32(
    final @Mocked SMFParserEventsDataAttributesNonInterleavedType events_ni,
    final @Mocked SMFParserEventsDataAttributeValuesType events_values,
    final @Mocked SMFParserEventsBodyType events_body)
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

    wrap.putInt(10);

    wrap.putInt(11);

    final SMFBDataStreamReaderType reader =
      SMFBDataStreamReader.create(
        URI.create("urn:test"),
        new ByteArrayInputStream(buffer));

    final SMFAttribute attr0 =
      SMFAttribute.of(
        SMFAttributeName.of("attr0"),
        SMFComponentType.ELEMENT_TYPE_INTEGER_SIGNED,
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

    new Expectations()
    {{
      events_body.onAttributesNonInterleaved();
      this.result = Optional.of(events_ni);

      events_ni.onDataAttributeStart(attr0);
      this.result = Optional.of(events_values);

      events_values.onDataAttributeValueIntegerSigned1(
        10L);
      events_values.onDataAttributeValueIntegerSigned1(
        11L);
      events_values.onDataAttributeValueFinish();

      events_ni.onDataAttributesNonInterleavedFinish();
    }};

    p.parse(header, events_body, reader);
  }

  @Test
  public void testVertices_IntegerSigned4_16(
    final @Mocked SMFParserEventsDataAttributesNonInterleavedType events_ni,
    final @Mocked SMFParserEventsDataAttributeValuesType events_values,
    final @Mocked SMFParserEventsBodyType events_body)
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

    wrap.putShort((short) 10);
    wrap.putShort((short) 20);
    wrap.putShort((short) (30));
    wrap.putShort((short) (40));

    wrap.putShort((short) (11));
    wrap.putShort((short) (21));
    wrap.putShort((short) (31));
    wrap.putShort((short) (41));

    final SMFBDataStreamReaderType reader =
      SMFBDataStreamReader.create(
        URI.create("urn:test"),
        new ByteArrayInputStream(buffer));

    final SMFAttribute attr0 =
      SMFAttribute.of(
        SMFAttributeName.of("attr0"),
        SMFComponentType.ELEMENT_TYPE_INTEGER_SIGNED,
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

    new Expectations()
    {{
      events_body.onAttributesNonInterleaved();
      this.result = Optional.of(events_ni);

      events_ni.onDataAttributeStart(attr0);
      this.result = Optional.of(events_values);

      events_values.onDataAttributeValueIntegerSigned4(
        10L, 20L, 30L, 40L);
      events_values.onDataAttributeValueIntegerSigned4(
        11L, 21L, 31L, 41L);
      events_values.onDataAttributeValueFinish();

      events_ni.onDataAttributesNonInterleavedFinish();
    }};

    p.parse(header, events_body, reader);
  }

  @Test
  public void testVertices_IntegerSigned3_16(
    final @Mocked SMFParserEventsDataAttributesNonInterleavedType events_ni,
    final @Mocked SMFParserEventsDataAttributeValuesType events_values,
    final @Mocked SMFParserEventsBodyType events_body)
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

    wrap.putShort((short) (10));
    wrap.putShort((short) (20));
    wrap.putShort((short) (30));

    wrap.putShort((short) (11));
    wrap.putShort((short) (21));
    wrap.putShort((short) (31));

    final SMFBDataStreamReaderType reader =
      SMFBDataStreamReader.create(
        URI.create("urn:test"),
        new ByteArrayInputStream(buffer));

    final SMFAttribute attr0 =
      SMFAttribute.of(
        SMFAttributeName.of("attr0"),
        SMFComponentType.ELEMENT_TYPE_INTEGER_SIGNED,
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

    new Expectations()
    {{
      events_body.onAttributesNonInterleaved();
      this.result = Optional.of(events_ni);

      events_ni.onDataAttributeStart(attr0);
      this.result = Optional.of(events_values);

      events_values.onDataAttributeValueIntegerSigned3(
        10L, 20L, 30L);
      events_values.onDataAttributeValueIntegerSigned3(
        11L, 21L, 31L);
      events_values.onDataAttributeValueFinish();

      events_ni.onDataAttributesNonInterleavedFinish();
    }};

    p.parse(header, events_body, reader);
  }

  @Test
  public void testVertices_IntegerSigned2_16(
    final @Mocked SMFParserEventsDataAttributesNonInterleavedType events_ni,
    final @Mocked SMFParserEventsDataAttributeValuesType events_values,
    final @Mocked SMFParserEventsBodyType events_body)
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

    wrap.putShort((short) (10));
    wrap.putShort((short) (20));

    wrap.putShort((short) (11));
    wrap.putShort((short) (21));

    final SMFBDataStreamReaderType reader =
      SMFBDataStreamReader.create(
        URI.create("urn:test"),
        new ByteArrayInputStream(buffer));

    final SMFAttribute attr0 =
      SMFAttribute.of(
        SMFAttributeName.of("attr0"),
        SMFComponentType.ELEMENT_TYPE_INTEGER_SIGNED,
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

    new Expectations()
    {{
      events_body.onAttributesNonInterleaved();
      this.result = Optional.of(events_ni);

      events_ni.onDataAttributeStart(attr0);
      this.result = Optional.of(events_values);

      events_values.onDataAttributeValueIntegerSigned2(
        10L, 20L);
      events_values.onDataAttributeValueIntegerSigned2(
        11L, 21L);
      events_values.onDataAttributeValueFinish();

      events_ni.onDataAttributesNonInterleavedFinish();
    }};

    p.parse(header, events_body, reader);
  }

  @Test
  public void testVertices_IntegerSigned1_16(
    final @Mocked SMFParserEventsDataAttributesNonInterleavedType events_ni,
    final @Mocked SMFParserEventsDataAttributeValuesType events_values,
    final @Mocked SMFParserEventsBodyType events_body)
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

    wrap.putShort((short) (10));

    wrap.putShort((short) (11));

    final SMFBDataStreamReaderType reader =
      SMFBDataStreamReader.create(
        URI.create("urn:test"),
        new ByteArrayInputStream(buffer));

    final SMFAttribute attr0 =
      SMFAttribute.of(
        SMFAttributeName.of("attr0"),
        SMFComponentType.ELEMENT_TYPE_INTEGER_SIGNED,
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

    new Expectations()
    {{
      events_body.onAttributesNonInterleaved();
      this.result = Optional.of(events_ni);

      events_ni.onDataAttributeStart(attr0);
      this.result = Optional.of(events_values);

      events_values.onDataAttributeValueIntegerSigned1(
        10L);
      events_values.onDataAttributeValueIntegerSigned1(
        11L);
      events_values.onDataAttributeValueFinish();

      events_ni.onDataAttributesNonInterleavedFinish();
    }};

    p.parse(header, events_body, reader);
  }

  @Test
  public void testVertices_IntegerSigned4_8(
    final @Mocked SMFParserEventsDataAttributesNonInterleavedType events_ni,
    final @Mocked SMFParserEventsDataAttributeValuesType events_values,
    final @Mocked SMFParserEventsBodyType events_body)
  {
    final BitSet state = new BitSet();

    final int componentSizeBits = 8;
    final int componentSizeOctets = componentSizeBits / 8;
    final int componentCount = 4;
    final int vertexCount = 2;

    final byte[] buffer =
      makeBytes(componentSizeOctets, componentCount, vertexCount);
    final ByteBuffer wrap =
      ByteBuffer.wrap(buffer).order(ByteOrder.BIG_ENDIAN);

    wrap.put((byte) 10);
    wrap.put((byte) 20);
    wrap.put((byte) (30));
    wrap.put((byte) (40));

    wrap.put((byte) (11));
    wrap.put((byte) (21));
    wrap.put((byte) (31));
    wrap.put((byte) (41));

    final SMFBDataStreamReaderType reader =
      SMFBDataStreamReader.create(
        URI.create("urn:test"),
        new ByteArrayInputStream(buffer));

    final SMFAttribute attr0 =
      SMFAttribute.of(
        SMFAttributeName.of("attr0"),
        SMFComponentType.ELEMENT_TYPE_INTEGER_SIGNED,
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

    new Expectations()
    {{
      events_body.onAttributesNonInterleaved();
      this.result = Optional.of(events_ni);

      events_ni.onDataAttributeStart(attr0);
      this.result = Optional.of(events_values);

      events_values.onDataAttributeValueIntegerSigned4(
        10L, 20L, 30L, 40L);
      events_values.onDataAttributeValueIntegerSigned4(
        11L, 21L, 31L, 41L);
      events_values.onDataAttributeValueFinish();

      events_ni.onDataAttributesNonInterleavedFinish();
    }};

    p.parse(header, events_body, reader);
  }

  @Test
  public void testVertices_IntegerSigned3_8(
    final @Mocked SMFParserEventsDataAttributesNonInterleavedType events_ni,
    final @Mocked SMFParserEventsDataAttributeValuesType events_values,
    final @Mocked SMFParserEventsBodyType events_body)
  {
    final BitSet state = new BitSet();

    final int componentSizeBits = 8;
    final int componentSizeOctets = componentSizeBits / 8;
    final int componentCount = 3;
    final int vertexCount = 2;

    final byte[] buffer =
      makeBytes(componentSizeOctets, componentCount, vertexCount);
    final ByteBuffer wrap =
      ByteBuffer.wrap(buffer).order(ByteOrder.BIG_ENDIAN);

    wrap.put((byte) (10));
    wrap.put((byte) (20));
    wrap.put((byte) (30));

    wrap.put((byte) (11));
    wrap.put((byte) (21));
    wrap.put((byte) (31));

    final SMFBDataStreamReaderType reader =
      SMFBDataStreamReader.create(
        URI.create("urn:test"),
        new ByteArrayInputStream(buffer));

    final SMFAttribute attr0 =
      SMFAttribute.of(
        SMFAttributeName.of("attr0"),
        SMFComponentType.ELEMENT_TYPE_INTEGER_SIGNED,
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

    new Expectations()
    {{
      events_body.onAttributesNonInterleaved();
      this.result = Optional.of(events_ni);

      events_ni.onDataAttributeStart(attr0);
      this.result = Optional.of(events_values);

      events_values.onDataAttributeValueIntegerSigned3(
        10L, 20L, 30L);
      events_values.onDataAttributeValueIntegerSigned3(
        11L, 21L, 31L);
      events_values.onDataAttributeValueFinish();

      events_ni.onDataAttributesNonInterleavedFinish();
    }};

    p.parse(header, events_body, reader);
  }

  @Test
  public void testVertices_IntegerSigned2_8(
    final @Mocked SMFParserEventsDataAttributesNonInterleavedType events_ni,
    final @Mocked SMFParserEventsDataAttributeValuesType events_values,
    final @Mocked SMFParserEventsBodyType events_body)
  {
    final BitSet state = new BitSet();

    final int componentSizeBits = 8;
    final int componentSizeOctets = componentSizeBits / 8;
    final int componentCount = 2;
    final int vertexCount = 2;

    final byte[] buffer =
      makeBytes(componentSizeOctets, componentCount, vertexCount);
    final ByteBuffer wrap =
      ByteBuffer.wrap(buffer).order(ByteOrder.BIG_ENDIAN);

    wrap.put((byte) (10));
    wrap.put((byte) (20));

    wrap.put((byte) (11));
    wrap.put((byte) (21));

    final SMFBDataStreamReaderType reader =
      SMFBDataStreamReader.create(
        URI.create("urn:test"),
        new ByteArrayInputStream(buffer));

    final SMFAttribute attr0 =
      SMFAttribute.of(
        SMFAttributeName.of("attr0"),
        SMFComponentType.ELEMENT_TYPE_INTEGER_SIGNED,
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

    new Expectations()
    {{
      events_body.onAttributesNonInterleaved();
      this.result = Optional.of(events_ni);

      events_ni.onDataAttributeStart(attr0);
      this.result = Optional.of(events_values);

      events_values.onDataAttributeValueIntegerSigned2(
        10L, 20L);
      events_values.onDataAttributeValueIntegerSigned2(
        11L, 21L);
      events_values.onDataAttributeValueFinish();

      events_ni.onDataAttributesNonInterleavedFinish();
    }};

    p.parse(header, events_body, reader);
  }

  @Test
  public void testVertices_IntegerSigned1_8(
    final @Mocked SMFParserEventsDataAttributesNonInterleavedType events_ni,
    final @Mocked SMFParserEventsDataAttributeValuesType events_values,
    final @Mocked SMFParserEventsBodyType events_body)
  {
    final BitSet state = new BitSet();

    final int componentSizeBits = 8;
    final int componentSizeOctets = componentSizeBits / 8;
    final int componentCount = 1;
    final int vertexCount = 2;

    final byte[] buffer =
      makeBytes(componentSizeOctets, componentCount, vertexCount);
    final ByteBuffer wrap =
      ByteBuffer.wrap(buffer).order(ByteOrder.BIG_ENDIAN);

    wrap.put((byte) (10));

    wrap.put((byte) (11));

    final SMFBDataStreamReaderType reader =
      SMFBDataStreamReader.create(
        URI.create("urn:test"),
        new ByteArrayInputStream(buffer));

    final SMFAttribute attr0 =
      SMFAttribute.of(
        SMFAttributeName.of("attr0"),
        SMFComponentType.ELEMENT_TYPE_INTEGER_SIGNED,
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

    new Expectations()
    {{
      events_body.onAttributesNonInterleaved();
      this.result = Optional.of(events_ni);

      events_ni.onDataAttributeStart(attr0);
      this.result = Optional.of(events_values);

      events_values.onDataAttributeValueIntegerSigned1(
        10L);
      events_values.onDataAttributeValueIntegerSigned1(
        11L);
      events_values.onDataAttributeValueFinish();

      events_ni.onDataAttributesNonInterleavedFinish();
    }};

    p.parse(header, events_body, reader);
  }
}
