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

// Unavoidable class data abstraction coupling style issue: Too many classes referenced.
// CHECKSTYLE:OFF
public final class SMFB2SerializerDataAttributesNonInterleaved
  implements SMFSerializerDataAttributesNonInterleavedType
  // CHECKSTYLE:ON
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

  private static SMFSerializerDataAttributesValuesType serializeFloat(
    final BSSWriterSequentialType writer,
    final SMFAttribute attribute)
  {
    switch (attribute.componentCount()) {
      case 1:
        return serializeFloat1(writer, attribute);
      case 2:
        return serializeFloat2(writer, attribute);
      case 3:
        return serializeFloat3(writer, attribute);
      case 4:
        return serializeFloat4(writer, attribute);
      default:
        throw new UnreachableCodeException();
    }
  }

  private static SMFSerializerDataAttributesValuesType serializeFloat1(
    final BSSWriterSequentialType writer,
    final SMFAttribute attribute)
  {
    switch (attribute.componentSizeBits()) {
      case 16:
        return new Float1_16(writer, attribute);
      case 32:
        return new Float1_32(writer, attribute);
      case 64:
        return new Float1_64(writer, attribute);
      default:
        throw new UnreachableCodeException();
    }
  }

  private static SMFSerializerDataAttributesValuesType serializeFloat2(
    final BSSWriterSequentialType writer,
    final SMFAttribute attribute)
  {
    switch (attribute.componentSizeBits()) {
      case 16:
        return new Float2_16(writer, attribute);
      case 32:
        return new Float2_32(writer, attribute);
      case 64:
        return new Float2_64(writer, attribute);
      default:
        throw new UnreachableCodeException();
    }
  }

  private static SMFSerializerDataAttributesValuesType serializeFloat3(
    final BSSWriterSequentialType writer,
    final SMFAttribute attribute)
  {
    switch (attribute.componentSizeBits()) {
      case 16:
        return new Float3_16(writer, attribute);
      case 32:
        return new Float3_32(writer, attribute);
      case 64:
        return new Float3_64(writer, attribute);
      default:
        throw new UnreachableCodeException();
    }
  }

  private static SMFSerializerDataAttributesValuesType serializeFloat4(
    final BSSWriterSequentialType writer,
    final SMFAttribute attribute)
  {
    switch (attribute.componentSizeBits()) {
      case 16:
        return new Float4_16(writer, attribute);
      case 32:
        return new Float4_32(writer, attribute);
      case 64:
        return new Float4_64(writer, attribute);
      default:
        throw new UnreachableCodeException();
    }
  }

  private static SMFSerializerDataAttributesValuesType serializeSigned(
    final BSSWriterSequentialType writer,
    final SMFAttribute attribute)
  {
    switch (attribute.componentCount()) {
      case 1:
        return serializeSigned1(writer, attribute);
      case 2:
        return serializeSigned2(writer, attribute);
      case 3:
        return serializeSigned3(writer, attribute);
      case 4:
        return serializeSigned4(writer, attribute);
      default:
        throw new UnreachableCodeException();
    }
  }

  private static SMFSerializerDataAttributesValuesType serializeSigned1(
    final BSSWriterSequentialType writer,
    final SMFAttribute attribute)
  {
    switch (attribute.componentSizeBits()) {
      case 8:
        return new Signed1_8(writer, attribute);
      case 16:
        return new Signed1_16(writer, attribute);
      case 32:
        return new Signed1_32(writer, attribute);
      case 64:
        return new Signed1_64(writer, attribute);
      default:
        throw new UnreachableCodeException();
    }
  }

  private static SMFSerializerDataAttributesValuesType serializeSigned2(
    final BSSWriterSequentialType writer,
    final SMFAttribute attribute)
  {
    switch (attribute.componentSizeBits()) {
      case 8:
        return new Signed2_8(writer, attribute);
      case 16:
        return new Signed2_16(writer, attribute);
      case 32:
        return new Signed2_32(writer, attribute);
      case 64:
        return new Signed2_64(writer, attribute);
      default:
        throw new UnreachableCodeException();
    }
  }

  private static SMFSerializerDataAttributesValuesType serializeSigned3(
    final BSSWriterSequentialType writer,
    final SMFAttribute attribute)
  {
    switch (attribute.componentSizeBits()) {
      case 8:
        return new Signed3_8(writer, attribute);
      case 16:
        return new Signed3_16(writer, attribute);
      case 32:
        return new Signed3_32(writer, attribute);
      case 64:
        return new Signed3_64(writer, attribute);
      default:
        throw new UnreachableCodeException();
    }
  }

  private static SMFSerializerDataAttributesValuesType serializeSigned4(
    final BSSWriterSequentialType writer,
    final SMFAttribute attribute)
  {
    switch (attribute.componentSizeBits()) {
      case 8:
        return new Signed4_8(writer, attribute);
      case 16:
        return new Signed4_16(writer, attribute);
      case 32:
        return new Signed4_32(writer, attribute);
      case 64:
        return new Signed4_64(writer, attribute);
      default:
        throw new UnreachableCodeException();
    }
  }

  private static SMFSerializerDataAttributesValuesType serializeUnsigned(
    final BSSWriterSequentialType writer,
    final SMFAttribute attribute)
  {
    switch (attribute.componentCount()) {
      case 1:
        return serializeUnsigned1(writer, attribute);
      case 2:
        return serializeUnsigned2(writer, attribute);
      case 3:
        return serializeUnsigned3(writer, attribute);
      case 4:
        return serializeUnsigned4(writer, attribute);
      default:
        throw new UnreachableCodeException();
    }
  }

  private static SMFSerializerDataAttributesValuesType serializeUnsigned1(
    final BSSWriterSequentialType writer,
    final SMFAttribute attribute)
  {
    switch (attribute.componentSizeBits()) {
      case 8:
        return new Unsigned1_8(writer, attribute);
      case 16:
        return new Unsigned1_16(writer, attribute);
      case 32:
        return new Unsigned1_32(writer, attribute);
      case 64:
        return new Unsigned1_64(writer, attribute);
      default:
        throw new UnreachableCodeException();
    }
  }

  private static SMFSerializerDataAttributesValuesType serializeUnsigned2(
    final BSSWriterSequentialType writer,
    final SMFAttribute attribute)
  {
    switch (attribute.componentSizeBits()) {
      case 8:
        return new Unsigned2_8(writer, attribute);
      case 16:
        return new Unsigned2_16(writer, attribute);
      case 32:
        return new Unsigned2_32(writer, attribute);
      case 64:
        return new Unsigned2_64(writer, attribute);
      default:
        throw new UnreachableCodeException();
    }
  }

  private static SMFSerializerDataAttributesValuesType serializeUnsigned3(
    final BSSWriterSequentialType writer,
    final SMFAttribute attribute)
  {
    switch (attribute.componentSizeBits()) {
      case 8:
        return new Unsigned3_8(writer, attribute);
      case 16:
        return new Unsigned3_16(writer, attribute);
      case 32:
        return new Unsigned3_32(writer, attribute);
      case 64:
        return new Unsigned3_64(writer, attribute);
      default:
        throw new UnreachableCodeException();
    }
  }

  private static SMFSerializerDataAttributesValuesType serializeUnsigned4(
    final BSSWriterSequentialType writer,
    final SMFAttribute attribute)
  {
    switch (attribute.componentSizeBits()) {
      case 8:
        return new Unsigned4_8(writer, attribute);
      case 16:
        return new Unsigned4_16(writer, attribute);
      case 32:
        return new Unsigned4_32(writer, attribute);
      case 64:
        return new Unsigned4_64(writer, attribute);
      default:
        throw new UnreachableCodeException();
    }
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

    switch (attribute.componentType()) {
      case ELEMENT_TYPE_INTEGER_SIGNED:
        return serializeSigned(subWriter, attribute);
      case ELEMENT_TYPE_INTEGER_UNSIGNED:
        return serializeUnsigned(subWriter, attribute);
      case ELEMENT_TYPE_FLOATING:
        return serializeFloat(subWriter, attribute);
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
