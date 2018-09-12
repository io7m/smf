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

import com.io7m.jcoords.core.conversion.CAxis;
import com.io7m.jcoords.core.conversion.CAxisSystem;
import com.io7m.jpra.runtime.java.JPRACursor1DByteBufferedChecked;
import com.io7m.jpra.runtime.java.JPRAStringTruncation;
import com.io7m.smfj.core.SMFAttribute;
import com.io7m.smfj.core.SMFAttributeName;
import com.io7m.smfj.core.SMFComponentType;
import com.io7m.smfj.core.SMFCoordinateSystem;
import com.io7m.smfj.core.SMFFaceWindingOrder;
import com.io7m.smfj.core.SMFFormatVersion;
import com.io7m.smfj.core.SMFHeader;
import com.io7m.smfj.core.SMFSchemaIdentifier;
import com.io7m.smfj.core.SMFSchemaName;
import com.io7m.smfj.core.SMFTriangles;
import com.io7m.smfj.format.binary.SMFBCoordinateSystems;
import com.io7m.smfj.format.binary.SMFBDataStreamReader;
import com.io7m.smfj.format.binary.SMFBDataStreamReaderType;
import com.io7m.smfj.format.binary.SMFBSection;
import com.io7m.smfj.format.binary.SMFBSectionHeader;
import com.io7m.smfj.format.binary.v1.SMFBv1AttributeByteBuffered;
import com.io7m.smfj.format.binary.v1.SMFBv1AttributeType;
import com.io7m.smfj.format.binary.v1.SMFBv1Headers;
import com.io7m.smfj.format.binary.v1.SMFBv1SchemaIDWritableType;
import com.io7m.smfj.format.binary.v1.SMFBv1_0HeaderByteBuffered;
import com.io7m.smfj.format.binary.v1.SMFBv1_0HeaderType;
import com.io7m.smfj.parser.api.SMFParseError;
import com.io7m.smfj.parser.api.SMFParserEventsBodyType;
import io.vavr.collection.List;
import io.vavr.control.Validation;
import mockit.Mocked;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.net.URI;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.file.Files;
import java.nio.file.Paths;

import static com.io7m.smfj.core.SMFComponentType.ELEMENT_TYPE_FLOATING;

public final class SMFBv1SectionParserHeaderTest
{
  private static final Logger LOG;

  static {
    LOG = LoggerFactory.getLogger(SMFBv1SectionParserHeaderTest.class);
  }

  @Test
  public void testEmpty(
    final @Mocked SMFParserEventsBodyType events_body)
  {
    final SMFBDataStreamReaderType reader =
      SMFBDataStreamReader.create(
        URI.create("urn:test"),
        new ByteArrayInputStream(new byte[]{}));

    final SMFBSection section =
      SMFBSection.of(SMFBSectionHeader.MAGIC, 0L, 0L);

    final Validation<List<SMFParseError>, SMFHeader> r =
      SMFBv1Headers.parse(
        SMFFormatVersion.of(1, 0), reader, section);

    dumpErrors(r);
    Assertions.assertTrue(r.isInvalid());
  }

  @Test
  public void testSizesAlignment()
  {
    Assertions.assertTrue(
      SMFBv1_0HeaderByteBuffered.metaFieldsSizeStaticOffsetFromType() % 4 == 0,
      "Fields size is 4 octet aligned");
    Assertions.assertTrue(
      SMFBv1_0HeaderByteBuffered.metaSchemaStaticOffsetFromType() % 4 == 0,
      "Schema is 4 octet aligned");
    Assertions.assertTrue(
      SMFBv1_0HeaderByteBuffered.metaVertexCountStaticOffsetFromType() % 8 == 0,
      "Vertex count is 8 octet aligned");
    Assertions.assertTrue(
      SMFBv1_0HeaderByteBuffered.metaTriangleCountStaticOffsetFromType() % 8 == 0,
      "Triangle count is 8 octet aligned");
    Assertions.assertTrue(
      SMFBv1_0HeaderByteBuffered.metaTriangleIndexSizeBitsStaticOffsetFromType() % 4 == 0,
      "Triangle size is 4 octet aligned");
    Assertions.assertTrue(
      SMFBv1_0HeaderByteBuffered.metaAttributeCountStaticOffsetFromType() % 4 == 0,
      "Attribute count is 4 octet aligned");
    Assertions.assertTrue(
      SMFBv1_0HeaderByteBuffered.metaCoordinateSystemStaticOffsetFromType() % 2 == 0,
      "Attribute count is 2 octet aligned");

    Assertions.assertTrue(
      SMFBv1_0HeaderByteBuffered.sizeInOctets() % 16 == 0,
      "Size of header is divisible by 16");
  }

  @Test
  public void testV1_0(
    final @Mocked SMFParserEventsBodyType events_body)
    throws Exception
  {
    final int header_size = SMFBv1_0HeaderByteBuffered.sizeInOctets();
    final int attr_size = SMFBv1AttributeByteBuffered.sizeInOctets();

    final byte[] header_buffer = new byte[header_size + attr_size];
    final byte[] attr_buffer = new byte[attr_size];

    final SMFBv1AttributeType attr_view =
      JPRACursor1DByteBufferedChecked.newCursor(
        ByteBuffer.wrap(attr_buffer).order(ByteOrder.BIG_ENDIAN),
        SMFBv1AttributeByteBuffered::newValueWithOffset)
        .getElementView();

    attr_view.getNameWritable().setValue("attr0", JPRAStringTruncation.REJECT);
    attr_view.setComponentSize(32);
    attr_view.setComponentKind(ELEMENT_TYPE_FLOATING.toInteger());
    attr_view.setComponentCount(4);

    for (int index = 0; index < attr_buffer.length; ++index) {
      header_buffer[index + header_size] = attr_buffer[index];
    }

    final SMFBv1_0HeaderType header_view =
      JPRACursor1DByteBufferedChecked.newCursor(
        ByteBuffer.wrap(header_buffer).order(ByteOrder.BIG_ENDIAN),
        SMFBv1_0HeaderByteBuffered::newValueWithOffset)
        .getElementView();

    final SMFSchemaIdentifier schema_id =
      SMFSchemaIdentifier.of(
        SMFSchemaName.of(
          "AAAAAAAABBBBBBBBCCCCCCCCDDDDDDDDEEEEEEEEFFFFFFFFGGGGGGGGHHHHHHHH"),
        1,
        2);

    final SMFCoordinateSystem system =
      SMFCoordinateSystem.of(
        CAxisSystem.of(
          CAxis.AXIS_POSITIVE_X,
          CAxis.AXIS_POSITIVE_Y,
          CAxis.AXIS_NEGATIVE_Z),
        SMFFaceWindingOrder.FACE_WINDING_ORDER_COUNTER_CLOCKWISE);
    SMFBCoordinateSystems.pack(
      system,
      header_view.getCoordinateSystemWritable());

    header_view.setVertexCount(23L);
    header_view.setTriangleIndexSizeBits(32);
    header_view.setTriangleCount(17L);
    header_view.setAttributeCount(1);
    header_view.setFieldsSize(header_size);

    final SMFBv1SchemaIDWritableType schema = header_view.getSchemaWritable();
    schema.setSchemaVersionMajor(schema_id.versionMajor());
    schema.setSchemaVersionMinor(schema_id.versionMinor());
    schema.getSchemaIdWritable().setValue(
      schema_id.name().value(), JPRAStringTruncation.REJECT);

    Files.write(Paths.get("/tmp/test-out.bin"), header_buffer);

    final SMFBDataStreamReaderType reader =
      SMFBDataStreamReader.create(
        URI.create("urn:test"),
        new ByteArrayInputStream(header_buffer));

    final SMFBSection section =
      SMFBSection.of(SMFBSectionHeader.MAGIC, header_buffer.length, 0L);

    final Validation<List<SMFParseError>, SMFHeader> r =
      SMFBv1Headers.parse(
        SMFFormatVersion.of(1, 0), reader, section);

    dumpErrors(r);
    Assertions.assertTrue(r.isValid());

    final SMFHeader header = r.get();
    Assertions.assertEquals(23L, header.vertexCount());
    Assertions.assertEquals(SMFTriangles.of(17L, 32), header.triangles());
    Assertions.assertEquals(system, header.coordinateSystem());
    Assertions.assertEquals(List.of(SMFAttribute.of(
      SMFAttributeName.of("attr0"),
      SMFComponentType.ELEMENT_TYPE_FLOATING,
      4,
      32
    )), header.attributesInOrder());
    Assertions.assertEquals(schema_id, header.schemaIdentifier().get());
  }

  private static void dumpErrors(
    final Validation<List<SMFParseError>, SMFHeader> r)
  {
    if (r.isInvalid()) {
      r.getError().forEach(e -> LOG.error("parse: {}", e.fullMessage()));
    }
  }
}
