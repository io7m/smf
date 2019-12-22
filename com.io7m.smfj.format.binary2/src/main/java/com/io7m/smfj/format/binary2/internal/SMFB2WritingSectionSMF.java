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

package com.io7m.smfj.format.binary2.internal;

import com.io7m.jbssio.api.BSSWriterSequentialType;
import com.io7m.junreachable.UnreachableCodeException;
import com.io7m.smfj.core.SMFAttribute;
import com.io7m.smfj.core.SMFHeader;
import java.io.IOException;
import java.nio.ByteOrder;
import java.util.Objects;

public final class SMFB2WritingSectionSMF
  implements SMFB2StructureWriterType<SMFHeader>
{
  public SMFB2WritingSectionSMF()
  {

  }

  private static void writeAttributesWithWriter(
    final BSSWriterSequentialType writer,
    final SMFHeader value)
    throws IOException
  {
    writer.checkNotClosed();

    for (final var attribute : value.attributesInOrder()) {
      final var name = String.format("attribute[%s]", attribute.name().value());
      try (var subWriter = writer.createSubWriterBounded(
        name, SMFB2ParsingAttribute.attributeSize())) {
        writeAttributeWithWriter(subWriter, attribute);
      }
    }
  }

  private static void writeAttributeWithWriter(
    final BSSWriterSequentialType writer,
    final SMFAttribute attribute)
    throws IOException
  {
    writer.checkNotClosed();

    new SMFB2WritingBoundedString("attributeName", 64)
      .write(writer, attribute.name().value());

    writer.writeU32BE(
      "kind",
      Integer.toUnsignedLong(attribute.componentType().toInteger()));
    writer.writeU32BE(
      "count",
      Integer.toUnsignedLong(attribute.componentCount()));
    writer.writeU32BE(
      "size",
      Integer.toUnsignedLong(attribute.componentSizeBits()));
  }

  private static void writeFieldsWithWriter(
    final BSSWriterSequentialType writer,
    final SMFHeader value,
    final DataSizes dataSizes)
    throws IOException
  {
    writer.checkNotClosed();

    writer.writeU32BE("fieldsSize", dataSizes.fieldsSizeAligned - 4L);

    final var schemaIdOpt = value.schemaIdentifier();
    if (schemaIdOpt.isPresent()) {
      final var schemaId = schemaIdOpt.get();
      new SMFB2WritingSchemaIdentifier().write(writer, schemaId);
    } else {
      writer.skip(SMFB2ParsingSchemaIdentifier.schemaIdentifierSize());
    }

    writer.writeU64BE("vertexCount", value.vertexCount());

    final var triangles = value.triangles();
    writer.writeU64BE(
      "triangleCount",
      triangles.triangleCount());
    writer.writeU32BE(
      "triangleSize",
      Integer.toUnsignedLong(triangles.triangleIndexSizeBits()));
    writer.writeU32BE(
      "attributeCount",
      Integer.toUnsignedLong(value.attributesInOrder().size()));

    new SMFB2WritingCoordinateSystem()
      .write(writer, value.coordinateSystem());

    final var byteOrder = value.dataByteOrder();
    if (Objects.equals(byteOrder, ByteOrder.BIG_ENDIAN)) {
      writer.writeU32BE("dataByteOrder", 0L);
    } else if (Objects.equals(byteOrder, ByteOrder.LITTLE_ENDIAN)) {
      writer.writeU32BE("dataByteOrder", 1L);
    } else {
      throw new UnreachableCodeException();
    }
  }

  private static DataSizes determineDataSize(
    final SMFHeader header)
  {
    final var fieldsBaseSize =
      SMFB2ParsingSectionSMF.baseFieldsSizeIn2_0();
    final var fieldsWithSize =
      fieldsBaseSize + 4L;
    final var fieldsSizeAligned =
      SMFB2Alignment.alignNext(fieldsWithSize, 16);

    final var attributeBaseSize =
      SMFB2ParsingAttribute.attributeSize();
    final var attributesSize =
      (long) header.attributesInOrder().size() * attributeBaseSize;
    final var attributesSizeAligned =
      SMFB2Alignment.alignNext(attributesSize, 16);

    return new DataSizes(fieldsSizeAligned, attributesSizeAligned);
  }

  @Override
  public void write(
    final BSSWriterSequentialType writer,
    final SMFHeader value)
    throws IOException
  {
    writer.checkNotClosed();

    final var dataSizes =
      determineDataSize(value);

    final var header =
      SMFB2Section.of(
        SMFB2ParsingSectionSMF.magic(), dataSizes.dataSize(), 0L);

    new SMFB2WritingSectionHeader().write(writer, header);
    try (var subWriter = writer.createSubWriterBounded(
      "smf",
      dataSizes.dataSize())) {
      this.writeWithWriter(subWriter, value, dataSizes);
    }
  }

  private void writeWithWriter(
    final BSSWriterSequentialType writer,
    final SMFHeader value,
    final DataSizes dataSizes)
    throws IOException
  {
    writer.checkNotClosed();

    final var fieldsStart = writer.offsetCurrentRelative();
    try (var fieldsWriter = writer.createSubWriterBounded(
      "fields", dataSizes.fieldsSizeAligned)) {
      writeFieldsWithWriter(fieldsWriter, value, dataSizes);
    }
    writer.padTo(fieldsStart + dataSizes.fieldsSizeAligned);

    final var attributesStart = writer.offsetCurrentRelative();
    try (var attributesWriter = writer.createSubWriterBounded(
      "attributes", dataSizes.attributesSizeAligned)) {
      writeAttributesWithWriter(attributesWriter, value);
    }
    writer.padTo(attributesStart + dataSizes.attributesSizeAligned);
  }

  private static final class DataSizes
  {
    private final long fieldsSizeAligned;
    private final long attributesSizeAligned;

    DataSizes(
      final long inFieldsSizeAligned,
      final long inAttributesSizeAligned)
    {
      this.fieldsSizeAligned = inFieldsSizeAligned;
      this.attributesSizeAligned = inAttributesSizeAligned;
    }

    long dataSize()
    {
      return this.attributesSizeAligned + this.fieldsSizeAligned;
    }
  }
}
