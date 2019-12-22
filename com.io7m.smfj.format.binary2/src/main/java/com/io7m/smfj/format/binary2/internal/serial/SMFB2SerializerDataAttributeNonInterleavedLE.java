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


package com.io7m.smfj.format.binary2.internal.serial;

import com.io7m.jbssio.api.BSSWriterSequentialType;
import com.io7m.junreachable.UnreachableCodeException;
import com.io7m.smfj.core.SMFAttribute;
import com.io7m.smfj.format.binary2.internal.serial.le.WriterLEFloat1_16;
import com.io7m.smfj.format.binary2.internal.serial.le.WriterLEFloat1_32;
import com.io7m.smfj.format.binary2.internal.serial.le.WriterLEFloat1_64;
import com.io7m.smfj.format.binary2.internal.serial.le.WriterLEFloat2_16;
import com.io7m.smfj.format.binary2.internal.serial.le.WriterLEFloat2_32;
import com.io7m.smfj.format.binary2.internal.serial.le.WriterLEFloat2_64;
import com.io7m.smfj.format.binary2.internal.serial.le.WriterLEFloat3_16;
import com.io7m.smfj.format.binary2.internal.serial.le.WriterLEFloat3_32;
import com.io7m.smfj.format.binary2.internal.serial.le.WriterLEFloat3_64;
import com.io7m.smfj.format.binary2.internal.serial.le.WriterLEFloat4_16;
import com.io7m.smfj.format.binary2.internal.serial.le.WriterLEFloat4_32;
import com.io7m.smfj.format.binary2.internal.serial.le.WriterLEFloat4_64;
import com.io7m.smfj.format.binary2.internal.serial.le.WriterLESigned1_16;
import com.io7m.smfj.format.binary2.internal.serial.le.WriterLESigned1_32;
import com.io7m.smfj.format.binary2.internal.serial.le.WriterLESigned1_64;
import com.io7m.smfj.format.binary2.internal.serial.le.WriterLESigned2_16;
import com.io7m.smfj.format.binary2.internal.serial.le.WriterLESigned2_32;
import com.io7m.smfj.format.binary2.internal.serial.le.WriterLESigned2_64;
import com.io7m.smfj.format.binary2.internal.serial.le.WriterLESigned3_16;
import com.io7m.smfj.format.binary2.internal.serial.le.WriterLESigned3_32;
import com.io7m.smfj.format.binary2.internal.serial.le.WriterLESigned3_64;
import com.io7m.smfj.format.binary2.internal.serial.le.WriterLESigned4_16;
import com.io7m.smfj.format.binary2.internal.serial.le.WriterLESigned4_32;
import com.io7m.smfj.format.binary2.internal.serial.le.WriterLESigned4_64;
import com.io7m.smfj.format.binary2.internal.serial.le.WriterLEUnsigned1_16;
import com.io7m.smfj.format.binary2.internal.serial.le.WriterLEUnsigned1_32;
import com.io7m.smfj.format.binary2.internal.serial.le.WriterLEUnsigned1_64;
import com.io7m.smfj.format.binary2.internal.serial.le.WriterLEUnsigned2_16;
import com.io7m.smfj.format.binary2.internal.serial.le.WriterLEUnsigned2_32;
import com.io7m.smfj.format.binary2.internal.serial.le.WriterLEUnsigned2_64;
import com.io7m.smfj.format.binary2.internal.serial.le.WriterLEUnsigned3_16;
import com.io7m.smfj.format.binary2.internal.serial.le.WriterLEUnsigned3_32;
import com.io7m.smfj.format.binary2.internal.serial.le.WriterLEUnsigned3_64;
import com.io7m.smfj.format.binary2.internal.serial.le.WriterLEUnsigned4_16;
import com.io7m.smfj.format.binary2.internal.serial.le.WriterLEUnsigned4_32;
import com.io7m.smfj.format.binary2.internal.serial.le.WriterLEUnsigned4_64;
import com.io7m.smfj.serializer.api.SMFSerializerDataAttributesValuesType;

// CHECKSTYLE:OFF
/*
 * Unavoidable class-data coupling; too many classes referenced.
 */
public final class SMFB2SerializerDataAttributeNonInterleavedLE
  //CHECKSTYLE:ON
{
  private SMFB2SerializerDataAttributeNonInterleavedLE()
  {
    
  }

  static SMFSerializerDataAttributesValuesType serializeFloatLE(
    final BSSWriterSequentialType writer,
    final SMFAttribute attribute)
  {
    switch (attribute.componentCount()) {
      case 1:
        return serializeFloat1LE(writer, attribute);
      case 2:
        return serializeFloat2LE(writer, attribute);
      case 3:
        return serializeFloat3LE(writer, attribute);
      case 4:
        return serializeFloat4LE(writer, attribute);
      default:
        throw new UnreachableCodeException();
    }
  }

  private static SMFSerializerDataAttributesValuesType serializeFloat1LE(
    final BSSWriterSequentialType writer,
    final SMFAttribute attribute)
  {
    switch (attribute.componentSizeBits()) {
      case 16:
        return new WriterLEFloat1_16(writer, attribute);
      case 32:
        return new WriterLEFloat1_32(writer, attribute);
      case 64:
        return new WriterLEFloat1_64(writer, attribute);
      default:
        throw new UnreachableCodeException();
    }
  }

  private static SMFSerializerDataAttributesValuesType serializeFloat2LE(
    final BSSWriterSequentialType writer,
    final SMFAttribute attribute)
  {
    switch (attribute.componentSizeBits()) {
      case 16:
        return new WriterLEFloat2_16(writer, attribute);
      case 32:
        return new WriterLEFloat2_32(writer, attribute);
      case 64:
        return new WriterLEFloat2_64(writer, attribute);
      default:
        throw new UnreachableCodeException();
    }
  }

  private static SMFSerializerDataAttributesValuesType serializeFloat3LE(
    final BSSWriterSequentialType writer,
    final SMFAttribute attribute)
  {
    switch (attribute.componentSizeBits()) {
      case 16:
        return new WriterLEFloat3_16(writer, attribute);
      case 32:
        return new WriterLEFloat3_32(writer, attribute);
      case 64:
        return new WriterLEFloat3_64(writer, attribute);
      default:
        throw new UnreachableCodeException();
    }
  }

  private static SMFSerializerDataAttributesValuesType serializeFloat4LE(
    final BSSWriterSequentialType writer,
    final SMFAttribute attribute)
  {
    switch (attribute.componentSizeBits()) {
      case 16:
        return new WriterLEFloat4_16(writer, attribute);
      case 32:
        return new WriterLEFloat4_32(writer, attribute);
      case 64:
        return new WriterLEFloat4_64(writer, attribute);
      default:
        throw new UnreachableCodeException();
    }
  }

  static SMFSerializerDataAttributesValuesType serializeSignedLE(
    final BSSWriterSequentialType writer,
    final SMFAttribute attribute)
  {
    switch (attribute.componentCount()) {
      case 1:
        return serializeSigned1LE(writer, attribute);
      case 2:
        return serializeSigned2LE(writer, attribute);
      case 3:
        return serializeSigned3LE(writer, attribute);
      case 4:
        return serializeSigned4LE(writer, attribute);
      default:
        throw new UnreachableCodeException();
    }
  }

  private static SMFSerializerDataAttributesValuesType serializeSigned1LE(
    final BSSWriterSequentialType writer,
    final SMFAttribute attribute)
  {
    switch (attribute.componentSizeBits()) {
      case 8:
        return new Signed1_8(writer, attribute);
      case 16:
        return new WriterLESigned1_16(writer, attribute);
      case 32:
        return new WriterLESigned1_32(writer, attribute);
      case 64:
        return new WriterLESigned1_64(writer, attribute);
      default:
        throw new UnreachableCodeException();
    }
  }

  private static SMFSerializerDataAttributesValuesType serializeSigned2LE(
    final BSSWriterSequentialType writer,
    final SMFAttribute attribute)
  {
    switch (attribute.componentSizeBits()) {
      case 8:
        return new Signed2_8(writer, attribute);
      case 16:
        return new WriterLESigned2_16(writer, attribute);
      case 32:
        return new WriterLESigned2_32(writer, attribute);
      case 64:
        return new WriterLESigned2_64(writer, attribute);
      default:
        throw new UnreachableCodeException();
    }
  }

  private static SMFSerializerDataAttributesValuesType serializeSigned3LE(
    final BSSWriterSequentialType writer,
    final SMFAttribute attribute)
  {
    switch (attribute.componentSizeBits()) {
      case 8:
        return new Signed3_8(writer, attribute);
      case 16:
        return new WriterLESigned3_16(writer, attribute);
      case 32:
        return new WriterLESigned3_32(writer, attribute);
      case 64:
        return new WriterLESigned3_64(writer, attribute);
      default:
        throw new UnreachableCodeException();
    }
  }

  private static SMFSerializerDataAttributesValuesType serializeSigned4LE(
    final BSSWriterSequentialType writer,
    final SMFAttribute attribute)
  {
    switch (attribute.componentSizeBits()) {
      case 8:
        return new Signed4_8(writer, attribute);
      case 16:
        return new WriterLESigned4_16(writer, attribute);
      case 32:
        return new WriterLESigned4_32(writer, attribute);
      case 64:
        return new WriterLESigned4_64(writer, attribute);
      default:
        throw new UnreachableCodeException();
    }
  }

  static SMFSerializerDataAttributesValuesType serializeUnsignedLE(
    final BSSWriterSequentialType writer,
    final SMFAttribute attribute)
  {
    switch (attribute.componentCount()) {
      case 1:
        return serializeUnsigned1LE(writer, attribute);
      case 2:
        return serializeUnsigned2LE(writer, attribute);
      case 3:
        return serializeUnsigned3LE(writer, attribute);
      case 4:
        return serializeUnsigned4LE(writer, attribute);
      default:
        throw new UnreachableCodeException();
    }
  }

  private static SMFSerializerDataAttributesValuesType serializeUnsigned1LE(
    final BSSWriterSequentialType writer,
    final SMFAttribute attribute)
  {
    switch (attribute.componentSizeBits()) {
      case 8:
        return new Unsigned1_8(writer, attribute);
      case 16:
        return new WriterLEUnsigned1_16(writer, attribute);
      case 32:
        return new WriterLEUnsigned1_32(writer, attribute);
      case 64:
        return new WriterLEUnsigned1_64(writer, attribute);
      default:
        throw new UnreachableCodeException();
    }
  }

  private static SMFSerializerDataAttributesValuesType serializeUnsigned2LE(
    final BSSWriterSequentialType writer,
    final SMFAttribute attribute)
  {
    switch (attribute.componentSizeBits()) {
      case 8:
        return new Unsigned2_8(writer, attribute);
      case 16:
        return new WriterLEUnsigned2_16(writer, attribute);
      case 32:
        return new WriterLEUnsigned2_32(writer, attribute);
      case 64:
        return new WriterLEUnsigned2_64(writer, attribute);
      default:
        throw new UnreachableCodeException();
    }
  }

  private static SMFSerializerDataAttributesValuesType serializeUnsigned3LE(
    final BSSWriterSequentialType writer,
    final SMFAttribute attribute)
  {
    switch (attribute.componentSizeBits()) {
      case 8:
        return new Unsigned3_8(writer, attribute);
      case 16:
        return new WriterLEUnsigned3_16(writer, attribute);
      case 32:
        return new WriterLEUnsigned3_32(writer, attribute);
      case 64:
        return new WriterLEUnsigned3_64(writer, attribute);
      default:
        throw new UnreachableCodeException();
    }
  }

  private static SMFSerializerDataAttributesValuesType serializeUnsigned4LE(
    final BSSWriterSequentialType writer,
    final SMFAttribute attribute)
  {
    switch (attribute.componentSizeBits()) {
      case 8:
        return new Unsigned4_8(writer, attribute);
      case 16:
        return new WriterLEUnsigned4_16(writer, attribute);
      case 32:
        return new WriterLEUnsigned4_32(writer, attribute);
      case 64:
        return new WriterLEUnsigned4_64(writer, attribute);
      default:
        throw new UnreachableCodeException();
    }
  }
}
