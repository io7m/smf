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

import com.io7m.jbssio.api.BSSReaderType;
import com.io7m.smfj.core.SMFAttribute;
import com.io7m.smfj.parser.api.SMFParserEventsDataAttributeValuesType;
import java.io.IOException;

final class SMFB2ParsingVertexDataLE
{
  private SMFB2ParsingVertexDataLE()
  {

  }

  static void parseAttributeWithReaderLE(
    final long vertexCount,
    final SMFAttribute attribute,
    final BSSReaderType reader,
    final SMFParserEventsDataAttributeValuesType values)
    throws IOException
  {
    switch (attribute.componentType()) {
      case ELEMENT_TYPE_INTEGER_SIGNED: {
        parseAttributeWithReaderIntegerSignedLE(
          vertexCount, attribute, reader, values);
        return;
      }
      case ELEMENT_TYPE_INTEGER_UNSIGNED: {
        parseAttributeWithReaderIntegerUnsignedLE(
          vertexCount, attribute, reader, values);
        return;
      }
      case ELEMENT_TYPE_FLOATING: {
        parseAttributeWithReaderFloatingLE(
          vertexCount, attribute, reader, values);
        return;
      }
    }
  }

  private static void parseAttributeWithReaderFloating64LE(
    final long vertexCount,
    final SMFAttribute attribute,
    final BSSReaderType reader,
    final SMFParserEventsDataAttributeValuesType values)
    throws IOException
  {
    switch (attribute.componentCount()) {
      case 1: {
        for (var index = 0L;
             Long.compareUnsigned(index, vertexCount) < 0;
             ++index) {
          final var c0 = reader.readD64LE("c0");
          values.onDataAttributeValueFloat1(c0);
        }
        return;
      }
      case 2: {
        for (var index = 0L;
             Long.compareUnsigned(index, vertexCount) < 0;
             ++index) {
          final var c0 = reader.readD64LE("c0");
          final var c1 = reader.readD64LE("c1");
          values.onDataAttributeValueFloat2(c0, c1);
        }
        return;
      }
      case 3: {
        for (var index = 0L;
             Long.compareUnsigned(index, vertexCount) < 0;
             ++index) {
          final var c0 = reader.readD64LE("c0");
          final var c1 = reader.readD64LE("c1");
          final var c2 = reader.readD64LE("c2");
          values.onDataAttributeValueFloat3(c0, c1, c2);
        }
        return;
      }
      case 4: {
        for (var index = 0L;
             Long.compareUnsigned(index, vertexCount) < 0;
             ++index) {
          final var c0 = reader.readD64LE("c0");
          final var c1 = reader.readD64LE("c1");
          final var c2 = reader.readD64LE("c2");
          final var c3 = reader.readD64LE("c3");
          values.onDataAttributeValueFloat4(c0, c1, c2, c3);
        }
        return;
      }
    }
  }

  private static void parseAttributeWithReaderFloating32LE(
    final long vertexCount,
    final SMFAttribute attribute,
    final BSSReaderType reader,
    final SMFParserEventsDataAttributeValuesType values)
    throws IOException
  {
    switch (attribute.componentCount()) {
      case 1: {
        for (var index = 0L;
             Long.compareUnsigned(index, vertexCount) < 0;
             ++index) {
          final var c0 = reader.readF32LE("c0");
          values.onDataAttributeValueFloat1(c0);
        }
        return;
      }
      case 2: {
        for (var index = 0L;
             Long.compareUnsigned(index, vertexCount) < 0;
             ++index) {
          final var c0 = reader.readF32LE("c0");
          final var c1 = reader.readF32LE("c1");
          values.onDataAttributeValueFloat2(c0, c1);
        }
        return;
      }
      case 3: {
        for (var index = 0L;
             Long.compareUnsigned(index, vertexCount) < 0;
             ++index) {
          final var c0 = reader.readF32LE("c0");
          final var c1 = reader.readF32LE("c1");
          final var c2 = reader.readF32LE("c2");
          values.onDataAttributeValueFloat3(c0, c1, c2);
        }
        return;
      }
      case 4: {
        for (var index = 0L;
             Long.compareUnsigned(index, vertexCount) < 0;
             ++index) {
          final var c0 = reader.readF32LE("c0");
          final var c1 = reader.readF32LE("c1");
          final var c2 = reader.readF32LE("c2");
          final var c3 = reader.readF32LE("c3");
          values.onDataAttributeValueFloat4(c0, c1, c2, c3);
        }
        return;
      }
    }
  }

  private static void parseAttributeWithReaderFloating16LE(
    final long vertexCount,
    final SMFAttribute attribute,
    final BSSReaderType reader,
    final SMFParserEventsDataAttributeValuesType values)
    throws IOException
  {
    switch (attribute.componentCount()) {
      case 1: {
        for (var index = 0L;
             Long.compareUnsigned(index, vertexCount) < 0;
             ++index) {
          final var c0 = reader.readF16LE("c0");
          values.onDataAttributeValueFloat1(c0);
        }
        return;
      }
      case 2: {
        for (var index = 0L;
             Long.compareUnsigned(index, vertexCount) < 0;
             ++index) {
          final var c0 = reader.readF16LE("c0");
          final var c1 = reader.readF16LE("c1");
          values.onDataAttributeValueFloat2(c0, c1);
        }
        return;
      }
      case 3: {
        for (var index = 0L;
             Long.compareUnsigned(index, vertexCount) < 0;
             ++index) {
          final var c0 = reader.readF16LE("c0");
          final var c1 = reader.readF16LE("c1");
          final var c2 = reader.readF16LE("c2");
          values.onDataAttributeValueFloat3(c0, c1, c2);
        }
        return;
      }
      case 4: {
        for (var index = 0L;
             Long.compareUnsigned(index, vertexCount) < 0;
             ++index) {
          final var c0 = reader.readF16LE("c0");
          final var c1 = reader.readF16LE("c1");
          final var c2 = reader.readF16LE("c2");
          final var c3 = reader.readF16LE("c3");
          values.onDataAttributeValueFloat4(c0, c1, c2, c3);
        }
        return;
      }
    }
  }

  private static void parseAttributeWithReaderFloatingLE(
    final long vertexCount,
    final SMFAttribute attribute,
    final BSSReaderType reader,
    final SMFParserEventsDataAttributeValuesType values)
    throws IOException
  {
    switch (attribute.componentSizeBits()) {
      case 16: {
        parseAttributeWithReaderFloating16LE(
          vertexCount, attribute, reader, values);
        return;
      }
      case 32: {
        parseAttributeWithReaderFloating32LE(
          vertexCount, attribute, reader, values);
        return;
      }
      case 64: {
        parseAttributeWithReaderFloating64LE(
          vertexCount, attribute, reader, values);
        return;
      }
    }
  }

  private static void parseAttributeWithReaderIntegerUnsigned16LE(
    final long vertexCount,
    final SMFAttribute attribute,
    final BSSReaderType reader,
    final SMFParserEventsDataAttributeValuesType values)
    throws IOException
  {
    switch (attribute.componentCount()) {
      case 1: {
        for (var index = 0L;
             Long.compareUnsigned(index, vertexCount) < 0;
             ++index) {
          final var c0 = reader.readU16LE("c0");
          values.onDataAttributeValueIntegerUnsigned1(c0);
        }
        return;
      }
      case 2: {
        for (var index = 0L;
             Long.compareUnsigned(index, vertexCount) < 0;
             ++index) {
          final var c0 = reader.readU16LE("c0");
          final var c1 = reader.readU16LE("c1");
          values.onDataAttributeValueIntegerUnsigned2(c0, c1);
        }
        return;
      }
      case 3: {
        for (var index = 0L;
             Long.compareUnsigned(index, vertexCount) < 0;
             ++index) {
          final var c0 = reader.readU16LE("c0");
          final var c1 = reader.readU16LE("c1");
          final var c2 = reader.readU16LE("c2");
          values.onDataAttributeValueIntegerUnsigned3(c0, c1, c2);
        }
        return;
      }
      case 4: {
        for (var index = 0L;
             Long.compareUnsigned(index, vertexCount) < 0;
             ++index) {
          final var c0 = reader.readU16LE("c0");
          final var c1 = reader.readU16LE("c1");
          final var c2 = reader.readU16LE("c2");
          final var c3 = reader.readU16LE("c3");
          values.onDataAttributeValueIntegerUnsigned4(c0, c1, c2, c3);
        }
        return;
      }
    }
  }

  private static void parseAttributeWithReaderIntegerUnsigned32LE(
    final long vertexCount,
    final SMFAttribute attribute,
    final BSSReaderType reader,
    final SMFParserEventsDataAttributeValuesType values)
    throws IOException
  {
    switch (attribute.componentCount()) {
      case 1: {
        for (var index = 0L;
             Long.compareUnsigned(index, vertexCount) < 0;
             ++index) {
          final var c0 = reader.readU32LE("c0");
          values.onDataAttributeValueIntegerUnsigned1(c0);
        }
        return;
      }
      case 2: {
        for (var index = 0L;
             Long.compareUnsigned(index, vertexCount) < 0;
             ++index) {
          final var c0 = reader.readU32LE("c0");
          final var c1 = reader.readU32LE("c1");
          values.onDataAttributeValueIntegerUnsigned2(c0, c1);
        }
        return;
      }
      case 3: {
        for (var index = 0L;
             Long.compareUnsigned(index, vertexCount) < 0;
             ++index) {
          final var c0 = reader.readU32LE("c0");
          final var c1 = reader.readU32LE("c1");
          final var c2 = reader.readU32LE("c2");
          values.onDataAttributeValueIntegerUnsigned3(c0, c1, c2);
        }
        return;
      }
      case 4: {
        for (var index = 0L;
             Long.compareUnsigned(index, vertexCount) < 0;
             ++index) {
          final var c0 = reader.readU32LE("c0");
          final var c1 = reader.readU32LE("c1");
          final var c2 = reader.readU32LE("c2");
          final var c3 = reader.readU32LE("c3");
          values.onDataAttributeValueIntegerUnsigned4(c0, c1, c2, c3);
        }
        return;
      }
    }
  }

  private static void parseAttributeWithReaderIntegerUnsigned64LE(
    final long vertexCount,
    final SMFAttribute attribute,
    final BSSReaderType reader,
    final SMFParserEventsDataAttributeValuesType values)
    throws IOException
  {
    switch (attribute.componentCount()) {
      case 1: {
        for (var index = 0L;
             Long.compareUnsigned(index, vertexCount) < 0;
             ++index) {
          final var c0 = reader.readU64LE("c0");
          values.onDataAttributeValueIntegerUnsigned1(c0);
        }
        return;
      }
      case 2: {
        for (var index = 0L;
             Long.compareUnsigned(index, vertexCount) < 0;
             ++index) {
          final var c0 = reader.readU64LE("c0");
          final var c1 = reader.readU64LE("c1");
          values.onDataAttributeValueIntegerUnsigned2(c0, c1);
        }
        return;
      }
      case 3: {
        for (var index = 0L;
             Long.compareUnsigned(index, vertexCount) < 0;
             ++index) {
          final var c0 = reader.readU64LE("c0");
          final var c1 = reader.readU64LE("c1");
          final var c2 = reader.readU64LE("c2");
          values.onDataAttributeValueIntegerUnsigned3(c0, c1, c2);
        }
        return;
      }
      case 4: {
        for (var index = 0L;
             Long.compareUnsigned(index, vertexCount) < 0;
             ++index) {
          final var c0 = reader.readU64LE("c0");
          final var c1 = reader.readU64LE("c1");
          final var c2 = reader.readU64LE("c2");
          final var c3 = reader.readU64LE("c3");
          values.onDataAttributeValueIntegerUnsigned4(c0, c1, c2, c3);
        }
        return;
      }
    }
  }

  private static void parseAttributeWithReaderIntegerUnsignedLE(
    final long vertexCount,
    final SMFAttribute attribute,
    final BSSReaderType reader,
    final SMFParserEventsDataAttributeValuesType values)
    throws IOException
  {
    switch (attribute.componentSizeBits()) {
      case 8: {
        SMFB2ParsingVertexData.parseAttributeWithReaderIntegerUnsigned8(
          vertexCount, attribute, reader, values);
        return;
      }
      case 16: {
        parseAttributeWithReaderIntegerUnsigned16LE(
          vertexCount, attribute, reader, values);
        return;
      }
      case 32: {
        parseAttributeWithReaderIntegerUnsigned32LE(
          vertexCount, attribute, reader, values);
        return;
      }
      case 64: {
        parseAttributeWithReaderIntegerUnsigned64LE(
          vertexCount, attribute, reader, values);
        return;
      }
    }
  }

  private static void parseAttributeWithReaderIntegerSigned16LE(
    final long vertexCount,
    final SMFAttribute attribute,
    final BSSReaderType reader,
    final SMFParserEventsDataAttributeValuesType values)
    throws IOException
  {
    switch (attribute.componentCount()) {
      case 1: {
        for (var index = 0L;
             Long.compareUnsigned(index, vertexCount) < 0;
             ++index) {
          final var c0 = reader.readS16LE("c0");
          values.onDataAttributeValueIntegerSigned1(c0);
        }
        return;
      }
      case 2: {
        for (var index = 0L;
             Long.compareUnsigned(index, vertexCount) < 0;
             ++index) {
          final var c0 = reader.readS16LE("c0");
          final var c1 = reader.readS16LE("c1");
          values.onDataAttributeValueIntegerSigned2(c0, c1);
        }
        return;
      }
      case 3: {
        for (var index = 0L;
             Long.compareUnsigned(index, vertexCount) < 0;
             ++index) {
          final var c0 = reader.readS16LE("c0");
          final var c1 = reader.readS16LE("c1");
          final var c2 = reader.readS16LE("c2");
          values.onDataAttributeValueIntegerSigned3(c0, c1, c2);
        }
        return;
      }
      case 4: {
        for (var index = 0L;
             Long.compareUnsigned(index, vertexCount) < 0;
             ++index) {
          final var c0 = reader.readS16LE("c0");
          final var c1 = reader.readS16LE("c1");
          final var c2 = reader.readS16LE("c2");
          final var c3 = reader.readS16LE("c3");
          values.onDataAttributeValueIntegerSigned4(c0, c1, c2, c3);
        }
        return;
      }
    }
  }

  private static void parseAttributeWithReaderIntegerSigned32LE(
    final long vertexCount,
    final SMFAttribute attribute,
    final BSSReaderType reader,
    final SMFParserEventsDataAttributeValuesType values)
    throws IOException
  {
    switch (attribute.componentCount()) {
      case 1: {
        for (var index = 0L;
             Long.compareUnsigned(index, vertexCount) < 0;
             ++index) {
          final var c0 = reader.readS32LE("c0");
          values.onDataAttributeValueIntegerSigned1(c0);
        }
        return;
      }
      case 2: {
        for (var index = 0L;
             Long.compareUnsigned(index, vertexCount) < 0;
             ++index) {
          final var c0 = reader.readS32LE("c0");
          final var c1 = reader.readS32LE("c1");
          values.onDataAttributeValueIntegerSigned2(c0, c1);
        }
        return;
      }
      case 3: {
        for (var index = 0L;
             Long.compareUnsigned(index, vertexCount) < 0;
             ++index) {
          final var c0 = reader.readS32LE("c0");
          final var c1 = reader.readS32LE("c1");
          final var c2 = reader.readS32LE("c2");
          values.onDataAttributeValueIntegerSigned3(c0, c1, c2);
        }
        return;
      }
      case 4: {
        for (var index = 0L;
             Long.compareUnsigned(index, vertexCount) < 0;
             ++index) {
          final var c0 = reader.readS32LE("c0");
          final var c1 = reader.readS32LE("c1");
          final var c2 = reader.readS32LE("c2");
          final var c3 = reader.readS32LE("c3");
          values.onDataAttributeValueIntegerSigned4(c0, c1, c2, c3);
        }
        return;
      }
    }
  }

  private static void parseAttributeWithReaderIntegerSigned64LE(
    final long vertexCount,
    final SMFAttribute attribute,
    final BSSReaderType reader,
    final SMFParserEventsDataAttributeValuesType values)
    throws IOException
  {
    switch (attribute.componentCount()) {
      case 1: {
        for (var index = 0L;
             Long.compareUnsigned(index, vertexCount) < 0;
             ++index) {
          final var c0 = reader.readS64LE("c0");
          values.onDataAttributeValueIntegerSigned1(c0);
        }
        return;
      }
      case 2: {
        for (var index = 0L;
             Long.compareUnsigned(index, vertexCount) < 0;
             ++index) {
          final var c0 = reader.readS64LE("c0");
          final var c1 = reader.readS64LE("c1");
          values.onDataAttributeValueIntegerSigned2(c0, c1);
        }
        return;
      }
      case 3: {
        for (var index = 0L;
             Long.compareUnsigned(index, vertexCount) < 0;
             ++index) {
          final var c0 = reader.readS64LE("c0");
          final var c1 = reader.readS64LE("c1");
          final var c2 = reader.readS64LE("c2");
          values.onDataAttributeValueIntegerSigned3(c0, c1, c2);
        }
        return;
      }
      case 4: {
        for (var index = 0L;
             Long.compareUnsigned(index, vertexCount) < 0;
             ++index) {
          final var c0 = reader.readS64LE("c0");
          final var c1 = reader.readS64LE("c1");
          final var c2 = reader.readS64LE("c2");
          final var c3 = reader.readS64LE("c3");
          values.onDataAttributeValueIntegerSigned4(c0, c1, c2, c3);
        }
        return;
      }
    }
  }

  private static void parseAttributeWithReaderIntegerSignedLE(
    final long vertexCount,
    final SMFAttribute attribute,
    final BSSReaderType reader,
    final SMFParserEventsDataAttributeValuesType values)
    throws IOException
  {
    switch (attribute.componentSizeBits()) {
      case 8: {
        SMFB2ParsingVertexData.parseAttributeWithReaderIntegerSigned8(
          vertexCount, attribute, reader, values);
        return;
      }
      case 16: {
        parseAttributeWithReaderIntegerSigned16LE(
          vertexCount, attribute, reader, values);
        return;
      }
      case 32: {
        parseAttributeWithReaderIntegerSigned32LE(
          vertexCount, attribute, reader, values);
        return;
      }
      case 64: {
        parseAttributeWithReaderIntegerSigned64LE(
          vertexCount, attribute, reader, values);
        return;
      }
    }
  }
}
