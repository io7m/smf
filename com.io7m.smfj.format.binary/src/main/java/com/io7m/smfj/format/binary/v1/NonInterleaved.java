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

import java.util.Objects;
import com.io7m.junreachable.UnreachableCodeException;
import com.io7m.smfj.core.SMFAttribute;
import com.io7m.smfj.core.SMFAttributeName;
import com.io7m.smfj.core.SMFHeader;
import com.io7m.smfj.format.binary.SMFBAlignment;
import com.io7m.smfj.format.binary.SMFBDataStreamWriterType;
import com.io7m.smfj.format.binary.SMFBSection;
import com.io7m.smfj.serializer.api.SMFSerializerDataAttributesNonInterleavedType;
import com.io7m.smfj.serializer.api.SMFSerializerDataAttributesValuesType;
import javaslang.collection.SortedMap;

import java.io.IOException;

// Unavoidable class data abstraction coupling style issue: Too many classes referenced.
// CHECKSTYLE:OFF
final class NonInterleaved implements SMFSerializerDataAttributesNonInterleavedType
  // CHECKSTYLE:ON
{
  private final SMFBDataStreamWriterType writer;
  private final SMFHeader header;

  NonInterleaved(
    final SMFHeader in_header,
    final SMFBDataStreamWriterType in_writer)
  {
    this.header = Objects.requireNonNull(in_header, "Header");
    this.writer = Objects.requireNonNull(in_writer, "Writer");
  }

  private static SMFSerializerDataAttributesValuesType serializeFloat(
    final SMFBDataStreamWriterType writer,
    final SMFAttribute attribute)
  {
    switch (attribute.componentCount()) {
      case 1: {
        return serializeFloat1(writer, attribute);
      }
      case 2: {
        return serializeFloat2(writer, attribute);
      }
      case 3: {
        return serializeFloat3(writer, attribute);
      }
      case 4: {
        return serializeFloat4(writer, attribute);
      }
      default: {
        throw new UnreachableCodeException();
      }
    }
  }

  private static SMFSerializerDataAttributesValuesType serializeFloat1(
    final SMFBDataStreamWriterType writer,
    final SMFAttribute attribute)
  {
    switch (attribute.componentSizeBits()) {
      case 16: {
        return new Float1_16(writer, attribute);
      }
      case 32: {
        return new Float1_32(writer, attribute);
      }
      case 64: {
        return new Float1_64(writer, attribute);
      }
      default: {
        throw new UnreachableCodeException();
      }
    }
  }

  private static SMFSerializerDataAttributesValuesType serializeFloat2(
    final SMFBDataStreamWriterType writer,
    final SMFAttribute attribute)
  {
    switch (attribute.componentSizeBits()) {
      case 16: {
        return new Float2_16(writer, attribute);
      }
      case 32: {
        return new Float2_32(writer, attribute);
      }
      case 64: {
        return new Float2_64(writer, attribute);
      }
      default: {
        throw new UnreachableCodeException();
      }
    }
  }

  private static SMFSerializerDataAttributesValuesType serializeFloat3(
    final SMFBDataStreamWriterType writer,
    final SMFAttribute attribute)
  {
    switch (attribute.componentSizeBits()) {
      case 16: {
        return new Float3_16(writer, attribute);
      }
      case 32: {
        return new Float3_32(writer, attribute);
      }
      case 64: {
        return new Float3_64(writer, attribute);
      }
      default: {
        throw new UnreachableCodeException();
      }
    }
  }

  private static SMFSerializerDataAttributesValuesType serializeFloat4(
    final SMFBDataStreamWriterType writer,
    final SMFAttribute attribute)
  {
    switch (attribute.componentSizeBits()) {
      case 16: {
        return new Float4_16(writer, attribute);
      }
      case 32: {
        return new Float4_32(writer, attribute);
      }
      case 64: {
        return new Float4_64(writer, attribute);
      }
      default: {
        throw new UnreachableCodeException();
      }
    }
  }

  private static SMFSerializerDataAttributesValuesType serializeSigned(
    final SMFBDataStreamWriterType writer,
    final SMFAttribute attribute)
  {
    switch (attribute.componentCount()) {
      case 1: {
        return serializeSigned1(writer, attribute);
      }
      case 2: {
        return serializeSigned2(writer, attribute);
      }
      case 3: {
        return serializeSigned3(writer, attribute);
      }
      case 4: {
        return serializeSigned4(writer, attribute);
      }
      default: {
        throw new UnreachableCodeException();
      }
    }
  }

  private static SMFSerializerDataAttributesValuesType serializeSigned1(
    final SMFBDataStreamWriterType writer,
    final SMFAttribute attribute)
  {
    switch (attribute.componentSizeBits()) {
      case 8: {
        return new Signed1_8(writer, attribute);
      }
      case 16: {
        return new Signed1_16(writer, attribute);
      }
      case 32: {
        return new Signed1_32(writer, attribute);
      }
      case 64: {
        return new Signed1_64(writer, attribute);
      }
      default: {
        throw new UnreachableCodeException();
      }
    }
  }

  private static SMFSerializerDataAttributesValuesType serializeSigned2(
    final SMFBDataStreamWriterType writer,
    final SMFAttribute attribute)
  {
    switch (attribute.componentSizeBits()) {
      case 8: {
        return new Signed2_8(writer, attribute);
      }
      case 16: {
        return new Signed2_16(writer, attribute);
      }
      case 32: {
        return new Signed2_32(writer, attribute);
      }
      case 64: {
        return new Signed2_64(writer, attribute);
      }
      default: {
        throw new UnreachableCodeException();
      }
    }
  }

  private static SMFSerializerDataAttributesValuesType serializeSigned3(
    final SMFBDataStreamWriterType writer,
    final SMFAttribute attribute)
  {
    switch (attribute.componentSizeBits()) {
      case 8: {
        return new Signed3_8(writer, attribute);
      }
      case 16: {
        return new Signed3_16(writer, attribute);
      }
      case 32: {
        return new Signed3_32(writer, attribute);
      }
      case 64: {
        return new Signed3_64(writer, attribute);
      }
      default: {
        throw new UnreachableCodeException();
      }
    }
  }

  private static SMFSerializerDataAttributesValuesType serializeSigned4(
    final SMFBDataStreamWriterType writer,
    final SMFAttribute attribute)
  {
    switch (attribute.componentSizeBits()) {
      case 8: {
        return new Signed4_8(writer, attribute);
      }
      case 16: {
        return new Signed4_16(writer, attribute);
      }
      case 32: {
        return new Signed4_32(writer, attribute);
      }
      case 64: {
        return new Signed4_64(writer, attribute);
      }
      default: {
        throw new UnreachableCodeException();
      }
    }
  }

  private static SMFSerializerDataAttributesValuesType serializeUnsigned(
    final SMFBDataStreamWriterType writer,
    final SMFAttribute attribute)
  {
    switch (attribute.componentCount()) {
      case 1: {
        return serializeUnsigned1(writer, attribute);
      }
      case 2: {
        return serializeUnsigned2(writer, attribute);
      }
      case 3: {
        return serializeUnsigned3(writer, attribute);
      }
      case 4: {
        return serializeUnsigned4(writer, attribute);
      }
      default: {
        throw new UnreachableCodeException();
      }
    }
  }

  private static SMFSerializerDataAttributesValuesType serializeUnsigned1(
    final SMFBDataStreamWriterType writer,
    final SMFAttribute attribute)
  {
    switch (attribute.componentSizeBits()) {
      case 8: {
        return new Unsigned1_8(writer, attribute);
      }
      case 16: {
        return new Unsigned1_16(writer, attribute);
      }
      case 32: {
        return new Unsigned1_32(writer, attribute);
      }
      case 64: {
        return new Unsigned1_64(writer, attribute);
      }
      default: {
        throw new UnreachableCodeException();
      }
    }
  }

  private static SMFSerializerDataAttributesValuesType serializeUnsigned2(
    final SMFBDataStreamWriterType writer,
    final SMFAttribute attribute)
  {
    switch (attribute.componentSizeBits()) {
      case 8: {
        return new Unsigned2_8(writer, attribute);
      }
      case 16: {
        return new Unsigned2_16(writer, attribute);
      }
      case 32: {
        return new Unsigned2_32(writer, attribute);
      }
      case 64: {
        return new Unsigned2_64(writer, attribute);
      }
      default: {
        throw new UnreachableCodeException();
      }
    }
  }

  private static SMFSerializerDataAttributesValuesType serializeUnsigned3(
    final SMFBDataStreamWriterType writer,
    final SMFAttribute attribute)
  {
    switch (attribute.componentSizeBits()) {
      case 8: {
        return new Unsigned3_8(writer, attribute);
      }
      case 16: {
        return new Unsigned3_16(writer, attribute);
      }
      case 32: {
        return new Unsigned3_32(writer, attribute);
      }
      case 64: {
        return new Unsigned3_64(writer, attribute);
      }
      default: {
        throw new UnreachableCodeException();
      }
    }
  }

  private static SMFSerializerDataAttributesValuesType serializeUnsigned4(
    final SMFBDataStreamWriterType writer,
    final SMFAttribute attribute)
  {
    switch (attribute.componentSizeBits()) {
      case 8: {
        return new Unsigned4_8(writer, attribute);
      }
      case 16: {
        return new Unsigned4_16(writer, attribute);
      }
      case 32: {
        return new Unsigned4_32(writer, attribute);
      }
      case 64: {
        return new Unsigned4_64(writer, attribute);
      }
      default: {
        throw new UnreachableCodeException();
      }
    }
  }

  @Override
  public SMFSerializerDataAttributesValuesType serializeData(
    final SMFAttributeName name)
    throws IllegalArgumentException, IOException
  {
    this.writer.checkAlignment(SMFBSection.SECTION_ALIGNMENT);

    final SortedMap<SMFAttributeName, SMFAttribute> by_name =
      this.header.attributesByName();

    if (!by_name.containsKey(name)) {
      throw new IllegalArgumentException(
        "No such attribute: " + name.value());
    }

    final SMFAttribute attribute = by_name.get(name).get();
    switch (attribute.componentType()) {
      case ELEMENT_TYPE_INTEGER_SIGNED: {
        return serializeSigned(this.writer, attribute);
      }
      case ELEMENT_TYPE_INTEGER_UNSIGNED: {
        return serializeUnsigned(this.writer, attribute);
      }
      case ELEMENT_TYPE_FLOATING: {
        return serializeFloat(this.writer, attribute);
      }
    }

    throw new UnreachableCodeException();
  }

  @Override
  public void close()
    throws IOException
  {
    this.writer.padTo(
      SMFBAlignment.alignNext(this.writer.position(), 16),
      (byte) 0);
  }
}
