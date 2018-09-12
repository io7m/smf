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

import com.io7m.jcoords.core.conversion.CAxis;
import com.io7m.jcoords.core.conversion.CAxisSystem;
import com.io7m.jpra.runtime.java.JPRACursor1DByteBufferedChecked;
import com.io7m.jpra.runtime.java.JPRACursor1DType;
import com.io7m.jpra.runtime.java.JPRAStringTruncation;
import com.io7m.smfj.core.SMFCoordinateSystem;
import com.io7m.smfj.core.SMFFaceWindingOrder;
import com.io7m.smfj.core.SMFHeader;
import com.io7m.smfj.core.SMFSchemaIdentifier;
import com.io7m.smfj.core.SMFSchemaName;
import com.io7m.smfj.core.SMFTriangles;
import com.io7m.smfj.format.binary.SMFBCoordinateSystems;
import com.io7m.smfj.format.binary.SMFBDataStreamWriter;
import com.io7m.smfj.format.binary.SMFBDataStreamWriterType;
import com.io7m.smfj.format.binary.SMFFormatBinary;
import com.io7m.smfj.format.binary.v1.SMFBv1CoordinateSystemsWritableType;
import com.io7m.smfj.format.binary.v1.SMFBv1SchemaIDWritableType;
import com.io7m.smfj.format.binary.v1.SMFBv1_0HeaderByteBuffered;
import com.io7m.smfj.format.binary.v1.SMFBv1_0HeaderType;
import com.io7m.smfj.parser.api.SMFParserEventsType;
import com.io7m.smfj.parser.api.SMFParserRandomAccessType;
import com.io7m.smfj.parser.api.SMFParserSequentialType;
import javaslang.collection.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.bind.DatatypeConverter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;

public abstract class SMFBinaryTest implements SMFBinaryTestType
{
  private static final Logger LOG;

  static {
    LOG = LoggerFactory.getLogger(SMFBinaryTest.class);
  }

  @Override
  public SMFParserSequentialType parserSequentialFor(
    final SMFParserEventsType events,
    final IOConsumerType<SMFBDataStreamWriterType> o)
  {
    try {
      final Path dir = Files.createTempDirectory("smf-tests-");
      final Path target = dir.resolve("data");

      LOG.debug("path: {}", target);

      try (OutputStream os =
             Files.newOutputStream(
               target,
               StandardOpenOption.CREATE,
               StandardOpenOption.TRUNCATE_EXISTING)) {
        o.accept(SMFBDataStreamWriter.create(target.toUri(), os));
        os.flush();
      }

      LOG.debug("wrote {} octets", Long.valueOf(Files.size(target)));

      try (InputStream is = Files.newInputStream(target)) {
        final byte[] buffer = new byte[16];
        while (true) {
          final int r = is.read(buffer);
          if (r == -1) {
            break;
          }
          LOG.debug(
            "{}",
            DatatypeConverter.printHexBinary(Arrays.copyOf(buffer, r)));
        }
      }

      return new SMFFormatBinary().parserCreateSequential(
        events, target.toUri(), Files.newInputStream(target));
    } catch (final IOException e) {
      throw new UncheckedIOException(e);
    }
  }

  @Override
  public final SMFParserRandomAccessType parserRandomFor(
    final SMFParserEventsType events,
    final IOConsumerType<SMFBDataStreamWriterType> o)
  {
    try {
      final Path dir = Files.createTempDirectory("smf-tests-");
      final Path target = dir.resolve("data");

      LOG.debug("path: {}", target);

      try (OutputStream os =
             Files.newOutputStream(
               target,
               StandardOpenOption.CREATE,
               StandardOpenOption.TRUNCATE_EXISTING)) {
        o.accept(SMFBDataStreamWriter.create(target.toUri(), os));
        os.flush();
      }

      LOG.debug("wrote {} octets", Long.valueOf(Files.size(target)));

      try (InputStream is = Files.newInputStream(target)) {
        final byte[] buffer = new byte[16];
        while (true) {
          final int r = is.read(buffer);
          if (r == -1) {
            break;
          }
          LOG.debug(
            "{}",
            DatatypeConverter.printHexBinary(Arrays.copyOf(buffer, r)));
        }
      }

      final FileChannel channel = FileChannel.open(target);
      return new SMFFormatBinary()
        .parserCreateRandomAccess(events, target.toUri(), channel);
    } catch (final IOException e) {
      throw new UncheckedIOException(e);
    }
  }

  protected static final class SerializedHeader
  {
    private final SMFHeader.Builder header_builder;
    private final byte[] buffer;
    private final ByteBuffer wrap;
    private final JPRACursor1DType<SMFBv1_0HeaderType> cursor;
    private final SMFBv1_0HeaderType view;

    protected SerializedHeader()
    {
      this.header_builder = SMFHeader.builder();
      this.header_builder.setCoordinateSystem(
        SMFCoordinateSystem.of(
          CAxisSystem.of(
            CAxis.AXIS_POSITIVE_X,
            CAxis.AXIS_POSITIVE_Y,
            CAxis.AXIS_NEGATIVE_Z),
          SMFFaceWindingOrder.FACE_WINDING_ORDER_COUNTER_CLOCKWISE));
      this.header_builder.setTriangles(
        SMFTriangles.of(0L, 32));
      this.header_builder.setAttributesInOrder(List.empty());
      this.header_builder.setSchemaIdentifier(
        SMFSchemaIdentifier.of(SMFSchemaName.of("com.io7m.smf.example"), 1, 2));
      this.header_builder.setVertexCount(0L);

      this.buffer = new byte[SMFBv1_0HeaderByteBuffered.sizeInOctets()];
      this.wrap = ByteBuffer.wrap(this.buffer);

      this.cursor = JPRACursor1DByteBufferedChecked.newCursor(
        this.wrap, SMFBv1_0HeaderByteBuffered::newValueWithOffset);
      this.view = this.cursor.getElementView();
    }

    public SMFHeader.Builder headerBuilder()
    {
      return this.header_builder;
    }

    public byte[] buffer()
    {
      this.recalculate();
      return this.buffer;
    }

    private void recalculate()
    {
      final SMFHeader header = this.header_builder.build();

      final SMFBv1SchemaIDWritableType header_schema_id =
        this.view.getSchemaWritable();

      final SMFSchemaIdentifier schema_id = header.schemaIdentifier().get();
      header_schema_id.getSchemaIdWritable().setValue(
        schema_id.name().value(), JPRAStringTruncation.REJECT);
      header_schema_id.setSchemaVersionMajor(schema_id.versionMajor());
      header_schema_id.setSchemaVersionMinor(schema_id.versionMinor());

      this.view.setVertexCount(header.vertexCount());
      this.view.setTriangleCount(
        header.triangles().triangleCount());
      this.view.setTriangleIndexSizeBits(
        (int) header.triangles().triangleIndexSizeBits());
      this.view.setAttributeCount(header.attributesInOrder().size());

      final SMFBv1CoordinateSystemsWritableType coords =
        this.view.getCoordinateSystemWritable();

      SMFBCoordinateSystems.pack(header.coordinateSystem(), coords);
    }
  }
}
