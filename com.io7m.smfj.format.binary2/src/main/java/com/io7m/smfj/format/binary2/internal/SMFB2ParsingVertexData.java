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

final class SMFB2ParsingVertexData
{
  private SMFB2ParsingVertexData()
  {

  }

  static void parseAttributeWithReaderIntegerSigned8(
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
          final var c0 = reader.readS8("c0");
          values.onDataAttributeValueIntegerSigned1(c0);
        }
        return;
      }
      case 2: {
        for (var index = 0L;
             Long.compareUnsigned(index, vertexCount) < 0;
             ++index) {
          final var c0 = reader.readS8("c0");
          final var c1 = reader.readS8("c1");
          values.onDataAttributeValueIntegerSigned2(c0, c1);
        }
        return;
      }
      case 3: {
        for (var index = 0L;
             Long.compareUnsigned(index, vertexCount) < 0;
             ++index) {
          final var c0 = reader.readS8("c0");
          final var c1 = reader.readS8("c1");
          final var c2 = reader.readS8("c2");
          values.onDataAttributeValueIntegerSigned3(c0, c1, c2);
        }
        return;
      }
      case 4: {
        for (var index = 0L;
             Long.compareUnsigned(index, vertexCount) < 0;
             ++index) {
          final var c0 = reader.readS8("c0");
          final var c1 = reader.readS8("c1");
          final var c2 = reader.readS8("c2");
          final var c3 = reader.readS8("c3");
          values.onDataAttributeValueIntegerSigned4(c0, c1, c2, c3);
        }
        return;
      }
    }
  }

  static void parseAttributeWithReaderIntegerUnsigned8(
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
          final var c0 = reader.readU8("c0");
          values.onDataAttributeValueIntegerUnsigned1(c0);
        }
        return;
      }
      case 2: {
        for (var index = 0L;
             Long.compareUnsigned(index, vertexCount) < 0;
             ++index) {
          final var c0 = reader.readU8("c0");
          final var c1 = reader.readU8("c1");
          values.onDataAttributeValueIntegerUnsigned2(c0, c1);
        }
        return;
      }
      case 3: {
        for (var index = 0L;
             Long.compareUnsigned(index, vertexCount) < 0;
             ++index) {
          final var c0 = reader.readU8("c0");
          final var c1 = reader.readU8("c1");
          final var c2 = reader.readU8("c2");
          values.onDataAttributeValueIntegerUnsigned3(c0, c1, c2);
        }
        return;
      }
      case 4: {
        for (var index = 0L;
             Long.compareUnsigned(index, vertexCount) < 0;
             ++index) {
          final var c0 = reader.readU8("c0");
          final var c1 = reader.readU8("c1");
          final var c2 = reader.readU8("c2");
          final var c3 = reader.readU8("c3");
          values.onDataAttributeValueIntegerUnsigned4(c0, c1, c2, c3);
        }
        return;
      }
    }
  }
}
