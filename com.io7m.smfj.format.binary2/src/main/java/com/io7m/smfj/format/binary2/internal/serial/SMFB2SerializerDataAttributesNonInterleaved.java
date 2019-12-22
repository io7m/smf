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

package com.io7m.smfj.format.binary2.internal.serial;

import com.io7m.jbssio.api.BSSWriterSequentialType;
import com.io7m.junreachable.UnreachableCodeException;
import com.io7m.smfj.core.SMFAttribute;
import com.io7m.smfj.core.SMFAttributeName;
import com.io7m.smfj.core.SMFHeader;
import com.io7m.smfj.format.binary2.SMFB2Section;
import com.io7m.smfj.format.binary2.internal.SMFB2Alignment;
import com.io7m.smfj.format.binary2.internal.SMFB2ParsingSectionVertexDataNI;
import com.io7m.smfj.format.binary2.internal.SMFB2WritingSectionHeader;
import com.io7m.smfj.serializer.api.SMFSerializerDataAttributesNonInterleavedType;
import com.io7m.smfj.serializer.api.SMFSerializerDataAttributesValuesType;
import java.io.IOException;
import java.util.Objects;
import java.util.SortedMap;

import static java.nio.ByteOrder.BIG_ENDIAN;
import static java.nio.ByteOrder.LITTLE_ENDIAN;

public final class SMFB2SerializerDataAttributesNonInterleaved
  implements SMFSerializerDataAttributesNonInterleavedType
{
  private final BSSWriterSequentialType writer;
  private final SMFHeader header;
  private final long vertexDataSize;
  private BSSWriterSequentialType dataWriter;
  private long dataStart;

  public SMFB2SerializerDataAttributesNonInterleaved(
    final SMFHeader inHeader,
    final BSSWriterSequentialType inWriter)
  {
    this.header =
      Objects.requireNonNull(inHeader, "Header");
    this.writer =
      Objects.requireNonNull(inWriter, "Writer");
    this.vertexDataSize =
      determineVertexDataSize(this.header);
  }

  private static long determineVertexDataSize(
    final SMFHeader header)
  {
    return header.attributesInOrder()
      .stream()
      .mapToLong(attr -> determineVertexDataSizeForAttribute(header, attr))
      .sum();
  }

  private static long determineVertexDataSizeForAttribute(
    final SMFHeader header,
    final SMFAttribute attribute)
  {
    final var sizeOfOne =
      (long) attribute.sizeOctets();
    final var sizeOfAll =
      sizeOfOne * header.vertexCount();
    return SMFB2Alignment.alignNext(sizeOfAll, 16);
  }

  public SMFB2SerializerDataAttributesNonInterleaved start()
    throws IOException
  {
    final var section =
      SMFB2Section.of(
        SMFB2ParsingSectionVertexDataNI.magic(),
        this.vertexDataSize,
        0L);

    new SMFB2WritingSectionHeader().write(this.writer, section);

    this.dataStart =
      this.writer.offsetCurrentRelative();
    this.dataWriter =
      this.writer.createSubWriterBounded("data", this.vertexDataSize);
    return this;
  }

  @Override
  public SMFSerializerDataAttributesValuesType serializeData(
    final SMFAttributeName name)
    throws IllegalArgumentException, IOException
  {
    final SortedMap<SMFAttributeName, SMFAttribute> by_name =
      this.header.attributesByName();

    if (!by_name.containsKey(name)) {
      throw new IllegalArgumentException(
        "No such attribute: " + name.value());
    }

    final SMFAttribute attribute = by_name.get(name);

    final var subWriter =
      this.dataWriter.createSubWriterBounded(
        attribute.name().value(),
        determineVertexDataSizeForAttribute(this.header, attribute));

    final var byteOrder = this.header.dataByteOrder();
    switch (attribute.componentType()) {
      case ELEMENT_TYPE_INTEGER_SIGNED: {
        if (Objects.equals(byteOrder, BIG_ENDIAN)) {
          return SMFB2SerializerDataAttributeNonInterleavedBE
            .serializeSignedBE(subWriter, attribute);
        }
        if (Objects.equals(byteOrder, LITTLE_ENDIAN)) {
          return SMFB2SerializerDataAttributeNonInterleavedLE
            .serializeSignedLE(subWriter, attribute);
        }
        throw new UnreachableCodeException();
      }
      case ELEMENT_TYPE_INTEGER_UNSIGNED: {
        if (Objects.equals(byteOrder, BIG_ENDIAN)) {
          return SMFB2SerializerDataAttributeNonInterleavedBE
            .serializeUnsignedBE(subWriter, attribute);
        }
        if (Objects.equals(byteOrder, LITTLE_ENDIAN)) {
          return SMFB2SerializerDataAttributeNonInterleavedLE
            .serializeUnsignedLE(subWriter, attribute);
        }
        throw new UnreachableCodeException();
      }
      case ELEMENT_TYPE_FLOATING: {
        if (Objects.equals(byteOrder, BIG_ENDIAN)) {
          return SMFB2SerializerDataAttributeNonInterleavedBE
            .serializeFloatBE(subWriter, attribute);
        }
        if (Objects.equals(byteOrder, LITTLE_ENDIAN)) {
          return SMFB2SerializerDataAttributeNonInterleavedLE
            .serializeFloatLE(subWriter, attribute);
        }
        throw new UnreachableCodeException();
      }
    }

    throw new UnreachableCodeException();
  }

  @Override
  public void close()
    throws IOException
  {
    this.dataWriter.close();

    final var end = this.dataStart + this.vertexDataSize;
    this.writer.padTo(end);
    this.writer.close();
  }
}
