/*
 * Copyright © 2019 Mark Raynsford <code@io7m.com> http://io7m.com
 *
 * Permission to use, copy, modify, and/or distribute this software for
 * any purpose with or without fee is hereby granted, provided that the
 * above copyright notice and this permission notice appear in all copies.
 *
 * THE SOFTWARE IS PROVIDED "AS IS" AND THE AUTHOR DISCLAIMS ALL
 * WARRANTIES WITH REGARD TO THIS SOFTWARE INCLUDING ALL IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS. IN NO EVENT SHALL THE AUTHOR
 * BE LIABLE FOR ANY SPECIAL, DIRECT, INDIRECT, OR CONSEQUENTIAL DAMAGES
 * OR ANY DAMAGES WHATSOEVER RESULTING FROM LOSS OF USE, DATA OR PROFITS,
 * WHETHER IN AN ACTION OF CONTRACT, NEGLIGENCE OR OTHER TORTIOUS ACTION,
 * ARISING OUT OF OR IN CONNECTION WITH THE USE OR PERFORMANCE OF THIS
 * SOFTWARE.
 */


package com.io7m.smfj.format.binary2;

import com.io7m.jbssio.api.BSSWriterSequentialType;
import com.io7m.junreachable.UnreachableCodeException;
import com.io7m.smfj.core.SMFFormatVersion;
import com.io7m.smfj.core.SMFHeader;
import com.io7m.smfj.core.SMFSchemaIdentifier;
import com.io7m.smfj.core.SMFTriangles;
import com.io7m.smfj.core.SMFVoid;
import com.io7m.smfj.format.binary2.internal.SMFB2Alignment;
import com.io7m.smfj.format.binary2.internal.SMFB2Metadata;
import com.io7m.smfj.format.binary2.internal.SMFB2ParsingSectionTriangles;
import com.io7m.smfj.format.binary2.internal.SMFB2WritingFileHeader;
import com.io7m.smfj.format.binary2.internal.SMFB2WritingSectionEnd;
import com.io7m.smfj.format.binary2.internal.SMFB2WritingSectionHeader;
import com.io7m.smfj.format.binary2.internal.SMFB2WritingSectionMetadata;
import com.io7m.smfj.format.binary2.internal.SMFB2WritingSectionSMF;
import com.io7m.smfj.format.binary2.internal.serial.SMFB2SerializerDataAttributesNonInterleaved;
import com.io7m.smfj.format.binary2.internal.serial.Triangles16;
import com.io7m.smfj.format.binary2.internal.serial.Triangles32;
import com.io7m.smfj.format.binary2.internal.serial.Triangles64;
import com.io7m.smfj.format.binary2.internal.serial.Triangles8;
import com.io7m.smfj.serializer.api.SMFSerializerDataAttributesNonInterleavedType;
import com.io7m.smfj.serializer.api.SMFSerializerDataTrianglesType;
import com.io7m.smfj.serializer.api.SMFSerializerType;
import java.io.IOException;
import java.util.Objects;

final class SMFB2Serializer implements SMFSerializerType
{
  private final SMFFormatVersion version;
  private final BSSWriterSequentialType writer;
  private SMFHeader headerReceived;

  SMFB2Serializer(
    final SMFFormatVersion inVersion,
    final BSSWriterSequentialType inWriter)
  {
    this.version =
      Objects.requireNonNull(inVersion, "version");
    this.writer =
      Objects.requireNonNull(inWriter, "writer");
  }

  @Override
  public void serializeHeader(
    final SMFHeader header)
    throws IllegalStateException, IOException
  {
    this.writer.checkNotClosed();

    this.headerReceived = header;
    new SMFB2WritingFileHeader().write(this.writer, this.version);
    new SMFB2WritingSectionSMF().write(this.writer, header);
  }

  @Override
  public SMFSerializerDataAttributesNonInterleavedType serializeVertexDataNonInterleavedStart()
    throws IllegalStateException, IOException
  {
    this.writer.checkNotClosed();

    if (this.headerReceived == null) {
      throw new IllegalStateException("Must serialize header first!");
    }

    final var subWriter =
      this.writer.createSubWriter("vertexDataNonInterleaved");

    return new SMFB2SerializerDataAttributesNonInterleaved(
      this.headerReceived, subWriter)
      .start();
  }

  @Override
  public SMFSerializerDataTrianglesType serializeTrianglesStart()
    throws IllegalStateException, IOException
  {
    this.writer.checkNotClosed();

    if (this.headerReceived == null) {
      throw new IllegalStateException("Must serialize header first!");
    }

    final SMFTriangles triangles = this.headerReceived.triangles();
    final var sizeOfOne = triangles.triangleSizeOctets();
    final var sizeOfAll = sizeOfOne * triangles.triangleCount();
    final var sizeAlign = SMFB2Alignment.alignNext(sizeOfAll, 16);

    new SMFB2WritingSectionHeader()
      .write(
        this.writer,
        SMFB2Section.of(SMFB2ParsingSectionTriangles.magic(), sizeAlign, 0L));

    final var subWriter =
      this.writer.createSubWriterBounded("triangles", sizeAlign);

    switch (triangles.triangleIndexSizeBits()) {
      case 8:
        return new Triangles8(subWriter, this.headerReceived);
      case 16:
        return new Triangles16(subWriter, this.headerReceived);
      case 32:
        return new Triangles32(subWriter, this.headerReceived);
      case 64:
        return new Triangles64(subWriter, this.headerReceived);
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
    this.writer.checkNotClosed();

    new SMFB2WritingSectionMetadata()
      .write(this.writer, new SMFB2Metadata(schema, data));
  }

  @Override
  public void close()
    throws IOException
  {
    new SMFB2WritingSectionEnd().write(this.writer, SMFVoid.void_());
    this.writer.close();
  }
}
