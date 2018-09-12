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

package com.io7m.smfj.format.binary.v1;

import com.io7m.jaffirm.core.Preconditions;
import com.io7m.jpra.runtime.java.JPRACursor1DByteBufferedChecked;
import com.io7m.jpra.runtime.java.JPRAStringTruncation;
import com.io7m.junreachable.UnreachableCodeException;
import com.io7m.smfj.core.SMFAttribute;
import com.io7m.smfj.core.SMFCoordinateSystem;
import com.io7m.smfj.core.SMFFormatVersion;
import com.io7m.smfj.core.SMFHeader;
import com.io7m.smfj.core.SMFSchemaIdentifier;
import com.io7m.smfj.core.SMFTriangles;
import com.io7m.smfj.format.binary.SMFBAlignment;
import com.io7m.smfj.format.binary.SMFBCoordinateSystems;
import com.io7m.smfj.format.binary.SMFBDataStreamWriter;
import com.io7m.smfj.format.binary.SMFBDataStreamWriterType;
import com.io7m.smfj.format.binary.SMFBSection;
import com.io7m.smfj.format.binary.SMFBSectionEnd;
import com.io7m.smfj.format.binary.SMFBSectionHeader;
import com.io7m.smfj.format.binary.SMFBSectionTriangles;
import com.io7m.smfj.format.binary.SMFBSectionVerticesNonInterleaved;
import com.io7m.smfj.format.binary.SMFFormatBinary;
import com.io7m.smfj.serializer.api.SMFSerializerDataAttributesNonInterleavedType;
import com.io7m.smfj.serializer.api.SMFSerializerDataTrianglesType;
import com.io7m.smfj.serializer.api.SMFSerializerType;

import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Objects;

/**
 * A serializer for the 1.* format.
 */

public final class SMFBv1Serializer implements SMFSerializerType
{
  private final SMFFormatVersion version;
  private final URI uri;
  private final OutputStream stream;
  private final SMFBDataStreamWriterType writer;
  private SMFHeader header;

  /**
   * Construct a serializer.
   *
   * @param in_version The output format version
   * @param in_uri     The URI for diagnostics
   * @param in_stream  The output stream
   */

  public SMFBv1Serializer(
    final SMFFormatVersion in_version,
    final URI in_uri,
    final OutputStream in_stream)
  {
    this.version = Objects.requireNonNull(in_version, "Version");
    this.uri = Objects.requireNonNull(in_uri, "URI");
    this.stream = Objects.requireNonNull(in_stream, "Stream");
    this.writer = SMFBDataStreamWriter.create(this.uri, this.stream);
  }

  private static int headerSize(
    final SMFHeader header,
    final SMFFormatVersion version)
  {
    final int attributes =
      Math.multiplyExact(
        header.attributesInOrder().size(),
        SMFBv1AttributeByteBuffered.sizeInOctets());

    switch (version.minor()) {
      case 0: {
        return Math.addExact(
          SMFBv1_0HeaderByteBuffered.sizeInOctets(),
          attributes);
      }
      default: {
        throw new UnsupportedOperationException();
      }
    }
  }

  private static void checkAlignment(
    final SMFBDataStreamWriterType writer)
  {
    final long position = writer.position();
    Preconditions.checkPreconditionL(
      position,
      position % SMFBSection.SECTION_ALIGNMENT == 0L,
      x -> "Writer must be aligned to " + SMFBSection.SECTION_ALIGNMENT + " octet boundaries");
  }

  @Override
  public void serializeHeader(
    final SMFHeader in_header)
    throws IllegalStateException, IOException
  {
    this.header = Objects.requireNonNull(in_header, "Header");

    checkAlignment(this.writer);

    this.writer.putBytes(SMFFormatBinary.magicNumber());
    this.writer.putU32(Integer.toUnsignedLong(this.version.major()));
    this.writer.putU32(Integer.toUnsignedLong(this.version.minor()));

    final int size = headerSize(in_header, this.version);
    this.writer.putU64(SMFBSectionHeader.MAGIC);
    this.writer.putU64(Integer.toUnsignedLong(size));

    switch (this.version.minor()) {
      case 0: {
        final byte[] buffer =
          new byte[SMFBv1_0HeaderByteBuffered.sizeInOctets()];

        final SMFBv1_0HeaderType view =
          JPRACursor1DByteBufferedChecked.newCursor(
            ByteBuffer.wrap(buffer).order(ByteOrder.BIG_ENDIAN),
            SMFBv1_0HeaderByteBuffered::newValueWithOffset).getElementView();

        final SMFTriangles triangles = in_header.triangles();
        view.setAttributeCount(in_header.attributesInOrder().size());
        view.setFieldsSize(buffer.length);
        view.setTriangleCount(triangles.triangleCount());
        view.setTriangleIndexSizeBits(triangles.triangleIndexSizeBits());
        view.setVertexCount(in_header.vertexCount());

        final SMFCoordinateSystem system = in_header.coordinateSystem();
        SMFBCoordinateSystems.pack(system, view.getCoordinateSystemWritable());

        this.writer.putBytes(buffer);
        break;
      }
      default: {
        throw new UnsupportedOperationException();
      }
    }

    final byte[] zero_buffer =
      new byte[SMFBv1AttributeByteBuffered.sizeInOctets()];
    final byte[] buffer =
      new byte[SMFBv1AttributeByteBuffered.sizeInOctets()];
    final SMFBv1AttributeType view =
      JPRACursor1DByteBufferedChecked.newCursor(
        ByteBuffer.wrap(buffer).order(ByteOrder.BIG_ENDIAN),
        SMFBv1AttributeByteBuffered::newValueWithOffset).getElementView();

    for (final SMFAttribute attribute : in_header.attributesInOrder()) {
      System.arraycopy(zero_buffer, 0, buffer, 0, buffer.length);

      view.setComponentCount(attribute.componentCount());
      view.setComponentKind(attribute.componentType().toInteger());
      view.setComponentSize(attribute.componentSizeBits());
      view.getNameWritable().setValue(
        attribute.name().value(), JPRAStringTruncation.REJECT);
      this.writer.putBytes(buffer);
    }

    this.writer.padTo(
      SMFBAlignment.alignNext(this.writer.position(), 16),
      (byte) 0);
  }

  @Override
  public SMFSerializerDataAttributesNonInterleavedType serializeVertexDataNonInterleavedStart()
    throws IllegalStateException, IOException
  {
    checkAlignment(this.writer);

    this.writer.putU64(SMFBSectionVerticesNonInterleaved.MAGIC);

    long size = 0L;
    for (final SMFAttribute attribute : this.header.attributesInOrder()) {
      final long attribute_size =
        SMFBAlignment.alignNext(
          Math.multiplyExact(
            (long) attribute.sizeOctets(),
            this.header.vertexCount()),
          SMFBSection.SECTION_ALIGNMENT);
      size = Math.addExact(size, attribute_size);
    }

    this.writer.putU64(size);
    return new NonInterleaved(this.header, this.writer);
  }

  @Override
  public SMFSerializerDataTrianglesType serializeTrianglesStart()
    throws IllegalStateException, IOException
  {
    checkAlignment(this.writer);

    final SMFTriangles triangles = this.header.triangles();
    final long size =
      Math.multiplyExact(
        triangles.triangleCount(),
        Integer.toUnsignedLong(triangles.triangleSizeOctets()));

    final long size_padded =
      SMFBAlignment.alignNext(size, SMFBSection.SECTION_ALIGNMENT);

    this.writer.putU64(SMFBSectionTriangles.MAGIC);
    this.writer.putU64(size_padded);

    switch (triangles.triangleIndexSizeBits()) {
      case 8:
        return new Triangles8(this.writer, this.header);
      case 16:
        return new Triangles16(this.writer, this.header);
      case 32:
        return new Triangles32(this.writer, this.header);
      case 64:
        return new Triangles64(this.writer, this.header);
      default:
        throw new UnreachableCodeException();
    }
  }

  @Override
  public void serializeMetadata(
    final SMFSchemaIdentifier schema,
    final byte[] data)
    throws IllegalStateException, IOException
  {
    checkAlignment(this.writer);
    Metadata.serializeMetadata(this.writer, schema, data);
  }

  @Override
  public void close()
    throws IOException
  {
    checkAlignment(this.writer);

    this.writer.putU64(SMFBSectionEnd.MAGIC);
    this.writer.putU64(0L);
    this.stream.flush();
  }
}
