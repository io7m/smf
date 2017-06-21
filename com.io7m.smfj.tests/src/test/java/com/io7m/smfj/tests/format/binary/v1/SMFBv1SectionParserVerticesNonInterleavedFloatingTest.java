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
import mockit.StrictExpectations;
import org.junit.Assert;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URI;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.BitSet;
import java.util.Optional;

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
    Assert.assertEquals(SMFBSectionVerticesNonInterleaved.MAGIC, p.magic());

    new StrictExpectations()
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
  public void testVertices_Float4_64(
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
    Assert.assertEquals(SMFBSectionVerticesNonInterleaved.MAGIC, p.magic());

    new StrictExpectations()
    {{
      events_body.onAttributesNonInterleaved();
      this.result = Optional.of(events_ni);

      events_ni.onDataAttributeStart(attr0);
      this.result = Optional.of(events_values);

      events_values.onDataAttributeValueFloat4(
        10.0, 20.0, 30.0, 40.0);
      events_values.onDataAttributeValueFloat4(
        11.0, 21.0, 31.0, 41.0);
      events_values.onDataAttributeValueFinish();

      events_ni.onDataAttributesNonInterleavedFinish();
    }};

    p.parse(header, events_body, reader);
  }

  @Test
  public void testVertices_Float3_64(
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
    Assert.assertEquals(SMFBSectionVerticesNonInterleaved.MAGIC, p.magic());

    new StrictExpectations()
    {{
      events_body.onAttributesNonInterleaved();
      this.result = Optional.of(events_ni);

      events_ni.onDataAttributeStart(attr0);
      this.result = Optional.of(events_values);

      events_values.onDataAttributeValueFloat3(
        10.0, 20.0, 30.0);
      events_values.onDataAttributeValueFloat3(
        11.0, 21.0, 31.0);
      events_values.onDataAttributeValueFinish();

      events_ni.onDataAttributesNonInterleavedFinish();
    }};

    p.parse(header, events_body, reader);
  }

  @Test
  public void testVertices_Float2_64(
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
    Assert.assertEquals(SMFBSectionVerticesNonInterleaved.MAGIC, p.magic());

    new StrictExpectations()
    {{
      events_body.onAttributesNonInterleaved();
      this.result = Optional.of(events_ni);

      events_ni.onDataAttributeStart(attr0);
      this.result = Optional.of(events_values);

      events_values.onDataAttributeValueFloat2(
        10.0, 20.0);
      events_values.onDataAttributeValueFloat2(
        11.0, 21.0);
      events_values.onDataAttributeValueFinish();

      events_ni.onDataAttributesNonInterleavedFinish();
    }};

    p.parse(header, events_body, reader);
  }

  @Test
  public void testVertices_Float1_64(
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
    Assert.assertEquals(SMFBSectionVerticesNonInterleaved.MAGIC, p.magic());

    new StrictExpectations()
    {{
      events_body.onAttributesNonInterleaved();
      this.result = Optional.of(events_ni);

      events_ni.onDataAttributeStart(attr0);
      this.result = Optional.of(events_values);

      events_values.onDataAttributeValueFloat1(
        10.0);
      events_values.onDataAttributeValueFloat1(
        11.0);
      events_values.onDataAttributeValueFinish();

      events_ni.onDataAttributesNonInterleavedFinish();
    }};

    p.parse(header, events_body, reader);
  }


  @Test
  public void testVertices_Float4_32(
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
    Assert.assertEquals(SMFBSectionVerticesNonInterleaved.MAGIC, p.magic());

    new StrictExpectations()
    {{
      events_body.onAttributesNonInterleaved();
      this.result = Optional.of(events_ni);

      events_ni.onDataAttributeStart(attr0);
      this.result = Optional.of(events_values);

      events_values.onDataAttributeValueFloat4(
        10.0, 20.0, 30.0, 40.0);
      events_values.onDataAttributeValueFloat4(
        11.0, 21.0, 31.0, 41.0);
      events_values.onDataAttributeValueFinish();

      events_ni.onDataAttributesNonInterleavedFinish();
    }};

    p.parse(header, events_body, reader);
  }

  @Test
  public void testVertices_Float3_32(
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
    Assert.assertEquals(SMFBSectionVerticesNonInterleaved.MAGIC, p.magic());

    new StrictExpectations()
    {{
      events_body.onAttributesNonInterleaved();
      this.result = Optional.of(events_ni);

      events_ni.onDataAttributeStart(attr0);
      this.result = Optional.of(events_values);

      events_values.onDataAttributeValueFloat3(
        10.0, 20.0, 30.0);
      events_values.onDataAttributeValueFloat3(
        11.0, 21.0, 31.0);
      events_values.onDataAttributeValueFinish();

      events_ni.onDataAttributesNonInterleavedFinish();
    }};

    p.parse(header, events_body, reader);
  }

  @Test
  public void testVertices_Float2_32(
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
    Assert.assertEquals(SMFBSectionVerticesNonInterleaved.MAGIC, p.magic());

    new StrictExpectations()
    {{
      events_body.onAttributesNonInterleaved();
      this.result = Optional.of(events_ni);

      events_ni.onDataAttributeStart(attr0);
      this.result = Optional.of(events_values);

      events_values.onDataAttributeValueFloat2(
        10.0, 20.0);
      events_values.onDataAttributeValueFloat2(
        11.0, 21.0);
      events_values.onDataAttributeValueFinish();

      events_ni.onDataAttributesNonInterleavedFinish();
    }};

    p.parse(header, events_body, reader);
  }

  @Test
  public void testVertices_Float1_32(
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
    Assert.assertEquals(SMFBSectionVerticesNonInterleaved.MAGIC, p.magic());

    new StrictExpectations()
    {{
      events_body.onAttributesNonInterleaved();
      this.result = Optional.of(events_ni);

      events_ni.onDataAttributeStart(attr0);
      this.result = Optional.of(events_values);

      events_values.onDataAttributeValueFloat1(
        10.0);
      events_values.onDataAttributeValueFloat1(
        11.0);
      events_values.onDataAttributeValueFinish();

      events_ni.onDataAttributesNonInterleavedFinish();
    }};

    p.parse(header, events_body, reader);
  }

  @Test
  public void testVertices_Float4_16(
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
    Assert.assertEquals(SMFBSectionVerticesNonInterleaved.MAGIC, p.magic());

    new StrictExpectations()
    {{
      events_body.onAttributesNonInterleaved();
      this.result = Optional.of(events_ni);

      events_ni.onDataAttributeStart(attr0);
      this.result = Optional.of(events_values);

      events_values.onDataAttributeValueFloat4(
        10.0, 20.0, 30.0, 40.0);
      events_values.onDataAttributeValueFloat4(
        11.0, 21.0, 31.0, 41.0);
      events_values.onDataAttributeValueFinish();

      events_ni.onDataAttributesNonInterleavedFinish();
    }};

    p.parse(header, events_body, reader);
  }

  @Test
  public void testVertices_Float3_16(
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
    Assert.assertEquals(SMFBSectionVerticesNonInterleaved.MAGIC, p.magic());

    new StrictExpectations()
    {{
      events_body.onAttributesNonInterleaved();
      this.result = Optional.of(events_ni);

      events_ni.onDataAttributeStart(attr0);
      this.result = Optional.of(events_values);

      events_values.onDataAttributeValueFloat3(
        10.0, 20.0, 30.0);
      events_values.onDataAttributeValueFloat3(
        11.0, 21.0, 31.0);
      events_values.onDataAttributeValueFinish();

      events_ni.onDataAttributesNonInterleavedFinish();
    }};

    p.parse(header, events_body, reader);
  }

  @Test
  public void testVertices_Float2_16(
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
    Assert.assertEquals(SMFBSectionVerticesNonInterleaved.MAGIC, p.magic());

    new StrictExpectations()
    {{
      events_body.onAttributesNonInterleaved();
      this.result = Optional.of(events_ni);

      events_ni.onDataAttributeStart(attr0);
      this.result = Optional.of(events_values);

      events_values.onDataAttributeValueFloat2(
        10.0, 20.0);
      events_values.onDataAttributeValueFloat2(
        11.0, 21.0);
      events_values.onDataAttributeValueFinish();

      events_ni.onDataAttributesNonInterleavedFinish();
    }};

    p.parse(header, events_body, reader);
  }

  @Test
  public void testVertices_Float1_16(
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
    Assert.assertEquals(SMFBSectionVerticesNonInterleaved.MAGIC, p.magic());

    new StrictExpectations()
    {{
      events_body.onAttributesNonInterleaved();
      this.result = Optional.of(events_ni);

      events_ni.onDataAttributeStart(attr0);
      this.result = Optional.of(events_values);

      events_values.onDataAttributeValueFloat1(
        10.0);
      events_values.onDataAttributeValueFloat1(
        11.0);
      events_values.onDataAttributeValueFinish();

      events_ni.onDataAttributesNonInterleavedFinish();
    }};

    p.parse(header, events_body, reader);
  }
}
