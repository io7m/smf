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
import com.io7m.smfj.format.binary2.internal.serial.be.WriterBEFloat1_16;
import com.io7m.smfj.format.binary2.internal.serial.be.WriterBEFloat1_32;
import com.io7m.smfj.format.binary2.internal.serial.be.WriterBEFloat1_64;
import com.io7m.smfj.format.binary2.internal.serial.be.WriterBEFloat2_16;
import com.io7m.smfj.format.binary2.internal.serial.be.WriterBEFloat2_32;
import com.io7m.smfj.format.binary2.internal.serial.be.WriterBEFloat2_64;
import com.io7m.smfj.format.binary2.internal.serial.be.WriterBEFloat3_16;
import com.io7m.smfj.format.binary2.internal.serial.be.WriterBEFloat3_32;
import com.io7m.smfj.format.binary2.internal.serial.be.WriterBEFloat3_64;
import com.io7m.smfj.format.binary2.internal.serial.be.WriterBEFloat4_16;
import com.io7m.smfj.format.binary2.internal.serial.be.WriterBEFloat4_32;
import com.io7m.smfj.format.binary2.internal.serial.be.WriterBEFloat4_64;
import com.io7m.smfj.format.binary2.internal.serial.be.WriterBESigned1_16;
import com.io7m.smfj.format.binary2.internal.serial.be.WriterBESigned1_32;
import com.io7m.smfj.format.binary2.internal.serial.be.WriterBESigned1_64;
import com.io7m.smfj.format.binary2.internal.serial.be.WriterBESigned2_16;
import com.io7m.smfj.format.binary2.internal.serial.be.WriterBESigned2_32;
import com.io7m.smfj.format.binary2.internal.serial.be.WriterBESigned2_64;
import com.io7m.smfj.format.binary2.internal.serial.be.WriterBESigned3_16;
import com.io7m.smfj.format.binary2.internal.serial.be.WriterBESigned3_32;
import com.io7m.smfj.format.binary2.internal.serial.be.WriterBESigned3_64;
import com.io7m.smfj.format.binary2.internal.serial.be.WriterBESigned4_16;
import com.io7m.smfj.format.binary2.internal.serial.be.WriterBESigned4_32;
import com.io7m.smfj.format.binary2.internal.serial.be.WriterBESigned4_64;
import com.io7m.smfj.format.binary2.internal.serial.be.WriterBEUnsigned1_16;
import com.io7m.smfj.format.binary2.internal.serial.be.WriterBEUnsigned1_32;
import com.io7m.smfj.format.binary2.internal.serial.be.WriterBEUnsigned1_64;
import com.io7m.smfj.format.binary2.internal.serial.be.WriterBEUnsigned2_16;
import com.io7m.smfj.format.binary2.internal.serial.be.WriterBEUnsigned2_32;
import com.io7m.smfj.format.binary2.internal.serial.be.WriterBEUnsigned2_64;
import com.io7m.smfj.format.binary2.internal.serial.be.WriterBEUnsigned3_16;
import com.io7m.smfj.format.binary2.internal.serial.be.WriterBEUnsigned3_32;
import com.io7m.smfj.format.binary2.internal.serial.be.WriterBEUnsigned3_64;
import com.io7m.smfj.format.binary2.internal.serial.be.WriterBEUnsigned4_16;
import com.io7m.smfj.format.binary2.internal.serial.be.WriterBEUnsigned4_32;
import com.io7m.smfj.format.binary2.internal.serial.be.WriterBEUnsigned4_64;
import com.io7m.smfj.serializer.api.SMFSerializerDataAttributesValuesType;

// CHECKSTYLE:OFF
/*
 * Unavoidable class-data coupling; too many classes referenced.
 */
public final class SMFB2SerializerDataAttributeNonInterleavedBE
  // CHECKSTYLE:ON
{
  private SMFB2SerializerDataAttributeNonInterleavedBE()
  {

  }

  static SMFSerializerDataAttributesValuesType serializeFloatBE(
    final BSSWriterSequentialType writer,
    final SMFAttribute attribute)
  {
    switch (attribute.componentCount()) {
      case 1:
        return serializeFloat1BE(writer, attribute);
      case 2:
        return serializeFloat2BE(writer, attribute);
      case 3:
        return serializeFloat3BE(writer, attribute);
      case 4:
        return serializeFloat4BE(writer, attribute);
      default:
        throw new UnreachableCodeException();
    }
  }

  private static SMFSerializerDataAttributesValuesType serializeFloat1BE(
    final BSSWriterSequentialType writer,
    final SMFAttribute attribute)
  {
    switch (attribute.componentSizeBits()) {
      case 16:
        return new WriterBEFloat1_16(writer, attribute);
      case 32:
        return new WriterBEFloat1_32(writer, attribute);
      case 64:
        return new WriterBEFloat1_64(writer, attribute);
      default:
        throw new UnreachableCodeException();
    }
  }

  private static SMFSerializerDataAttributesValuesType serializeFloat2BE(
    final BSSWriterSequentialType writer,
    final SMFAttribute attribute)
  {
    switch (attribute.componentSizeBits()) {
      case 16:
        return new WriterBEFloat2_16(writer, attribute);
      case 32:
        return new WriterBEFloat2_32(writer, attribute);
      case 64:
        return new WriterBEFloat2_64(writer, attribute);
      default:
        throw new UnreachableCodeException();
    }
  }

  private static SMFSerializerDataAttributesValuesType serializeFloat3BE(
    final BSSWriterSequentialType writer,
    final SMFAttribute attribute)
  {
    switch (attribute.componentSizeBits()) {
      case 16:
        return new WriterBEFloat3_16(writer, attribute);
      case 32:
        return new WriterBEFloat3_32(writer, attribute);
      case 64:
        return new WriterBEFloat3_64(writer, attribute);
      default:
        throw new UnreachableCodeException();
    }
  }

  private static SMFSerializerDataAttributesValuesType serializeFloat4BE(
    final BSSWriterSequentialType writer,
    final SMFAttribute attribute)
  {
    switch (attribute.componentSizeBits()) {
      case 16:
        return new WriterBEFloat4_16(writer, attribute);
      case 32:
        return new WriterBEFloat4_32(writer, attribute);
      case 64:
        return new WriterBEFloat4_64(writer, attribute);
      default:
        throw new UnreachableCodeException();
    }
  }

  static SMFSerializerDataAttributesValuesType serializeSignedBE(
    final BSSWriterSequentialType writer,
    final SMFAttribute attribute)
  {
    switch (attribute.componentCount()) {
      case 1:
        return serializeSigned1BE(writer, attribute);
      case 2:
        return serializeSigned2BE(writer, attribute);
      case 3:
        return serializeSigned3BE(writer, attribute);
      case 4:
        return serializeSigned4BE(writer, attribute);
      default:
        throw new UnreachableCodeException();
    }
  }

  private static SMFSerializerDataAttributesValuesType serializeSigned1BE(
    final BSSWriterSequentialType writer,
    final SMFAttribute attribute)
  {
    switch (attribute.componentSizeBits()) {
      case 8:
        return new Signed1_8(writer, attribute);
      case 16:
        return new WriterBESigned1_16(writer, attribute);
      case 32:
        return new WriterBESigned1_32(writer, attribute);
      case 64:
        return new WriterBESigned1_64(writer, attribute);
      default:
        throw new UnreachableCodeException();
    }
  }

  private static SMFSerializerDataAttributesValuesType serializeSigned2BE(
    final BSSWriterSequentialType writer,
    final SMFAttribute attribute)
  {
    switch (attribute.componentSizeBits()) {
      case 8:
        return new Signed2_8(writer, attribute);
      case 16:
        return new WriterBESigned2_16(writer, attribute);
      case 32:
        return new WriterBESigned2_32(writer, attribute);
      case 64:
        return new WriterBESigned2_64(writer, attribute);
      default:
        throw new UnreachableCodeException();
    }
  }

  private static SMFSerializerDataAttributesValuesType serializeSigned3BE(
    final BSSWriterSequentialType writer,
    final SMFAttribute attribute)
  {
    switch (attribute.componentSizeBits()) {
      case 8:
        return new Signed3_8(writer, attribute);
      case 16:
        return new WriterBESigned3_16(writer, attribute);
      case 32:
        return new WriterBESigned3_32(writer, attribute);
      case 64:
        return new WriterBESigned3_64(writer, attribute);
      default:
        throw new UnreachableCodeException();
    }
  }

  private static SMFSerializerDataAttributesValuesType serializeSigned4BE(
    final BSSWriterSequentialType writer,
    final SMFAttribute attribute)
  {
    switch (attribute.componentSizeBits()) {
      case 8:
        return new Signed4_8(writer, attribute);
      case 16:
        return new WriterBESigned4_16(writer, attribute);
      case 32:
        return new WriterBESigned4_32(writer, attribute);
      case 64:
        return new WriterBESigned4_64(writer, attribute);
      default:
        throw new UnreachableCodeException();
    }
  }

  static SMFSerializerDataAttributesValuesType serializeUnsignedBE(
    final BSSWriterSequentialType writer,
    final SMFAttribute attribute)
  {
    switch (attribute.componentCount()) {
      case 1:
        return serializeUnsigned1BE(writer, attribute);
      case 2:
        return serializeUnsigned2BE(writer, attribute);
      case 3:
        return serializeUnsigned3BE(writer, attribute);
      case 4:
        return serializeUnsigned4BE(writer, attribute);
      default:
        throw new UnreachableCodeException();
    }
  }

  private static SMFSerializerDataAttributesValuesType serializeUnsigned1BE(
    final BSSWriterSequentialType writer,
    final SMFAttribute attribute)
  {
    switch (attribute.componentSizeBits()) {
      case 8:
        return new Unsigned1_8(writer, attribute);
      case 16:
        return new WriterBEUnsigned1_16(writer, attribute);
      case 32:
        return new WriterBEUnsigned1_32(writer, attribute);
      case 64:
        return new WriterBEUnsigned1_64(writer, attribute);
      default:
        throw new UnreachableCodeException();
    }
  }

  private static SMFSerializerDataAttributesValuesType serializeUnsigned2BE(
    final BSSWriterSequentialType writer,
    final SMFAttribute attribute)
  {
    switch (attribute.componentSizeBits()) {
      case 8:
        return new Unsigned2_8(writer, attribute);
      case 16:
        return new WriterBEUnsigned2_16(writer, attribute);
      case 32:
        return new WriterBEUnsigned2_32(writer, attribute);
      case 64:
        return new WriterBEUnsigned2_64(writer, attribute);
      default:
        throw new UnreachableCodeException();
    }
  }

  private static SMFSerializerDataAttributesValuesType serializeUnsigned3BE(
    final BSSWriterSequentialType writer,
    final SMFAttribute attribute)
  {
    switch (attribute.componentSizeBits()) {
      case 8:
        return new Unsigned3_8(writer, attribute);
      case 16:
        return new WriterBEUnsigned3_16(writer, attribute);
      case 32:
        return new WriterBEUnsigned3_32(writer, attribute);
      case 64:
        return new WriterBEUnsigned3_64(writer, attribute);
      default:
        throw new UnreachableCodeException();
    }
  }

  private static SMFSerializerDataAttributesValuesType serializeUnsigned4BE(
    final BSSWriterSequentialType writer,
    final SMFAttribute attribute)
  {
    switch (attribute.componentSizeBits()) {
      case 8:
        return new Unsigned4_8(writer, attribute);
      case 16:
        return new WriterBEUnsigned4_16(writer, attribute);
      case 32:
        return new WriterBEUnsigned4_32(writer, attribute);
      case 64:
        return new WriterBEUnsigned4_64(writer, attribute);
      default:
        throw new UnreachableCodeException();
    }
  }
}
