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

import com.io7m.junreachable.UnreachableCodeException;
import com.io7m.smfj.core.SMFAttribute;
import com.io7m.smfj.format.binary.SMFBDataStreamReaderType;
import com.io7m.smfj.parser.api.SMFParserEventsDataAttributeValuesType;

import java.io.IOException;
import java.util.Optional;

final class ReadUnsigned
{
  private ReadUnsigned()
  {
    throw new UnreachableCodeException();
  }

  static void parseAttributeDataUnsigned(
    final SMFBDataStreamReaderType reader,
    final long count,
    final SMFAttribute attribute,
    final SMFParserEventsDataAttributeValuesType events_values,
    final Optional<String> name_opt)
    throws IOException
  {
    switch (attribute.componentCount()) {
      case 1: {
        parseAttributeDataUnsigned1(
          reader, count, attribute, events_values, name_opt);
        return;
      }
      case 2: {
        parseAttributeDataUnsigned2(
          reader, count, attribute, events_values, name_opt);
        return;
      }
      case 3: {
        parseAttributeDataUnsigned3(
          reader, count, attribute, events_values, name_opt);
        return;
      }
      case 4: {
        parseAttributeDataUnsigned4(
          reader, count, attribute, events_values, name_opt);
        return;
      }
      default: {
        throw new UnreachableCodeException();
      }
    }
  }

  private static void parseAttributeDataUnsigned4(
    final SMFBDataStreamReaderType reader,
    final long count,
    final SMFAttribute attribute,
    final SMFParserEventsDataAttributeValuesType events_values,
    final Optional<String> name_opt)
    throws IOException
  {
    switch (attribute.componentSizeBits()) {

      case 8: {
        for (long index = 0L;
             Long.compareUnsigned(index, count) < 0;
             ++index) {
          final long x = reader.readU8(name_opt);
          final long y = reader.readU8(name_opt);
          final long z = reader.readU8(name_opt);
          final long w = reader.readU8(name_opt);
          events_values.onDataAttributeValueIntegerUnsigned4(x, y, z, w);
        }
        return;
      }

      case 16: {
        for (long index = 0L;
             Long.compareUnsigned(index, count) < 0;
             ++index) {
          final long x = reader.readU16(name_opt);
          final long y = reader.readU16(name_opt);
          final long z = reader.readU16(name_opt);
          final long w = reader.readU16(name_opt);
          events_values.onDataAttributeValueIntegerUnsigned4(x, y, z, w);
        }
        return;
      }

      case 32: {
        for (long index = 0L;
             Long.compareUnsigned(index, count) < 0;
             ++index) {
          final long x = reader.readU32(name_opt);
          final long y = reader.readU32(name_opt);
          final long z = reader.readU32(name_opt);
          final long w = reader.readU32(name_opt);
          events_values.onDataAttributeValueIntegerUnsigned4(x, y, z, w);
        }
        return;
      }

      case 64: {
        for (long index = 0L;
             Long.compareUnsigned(index, count) < 0;
             ++index) {
          final long x = reader.readU64(name_opt);
          final long y = reader.readU64(name_opt);
          final long z = reader.readU64(name_opt);
          final long w = reader.readU64(name_opt);
          events_values.onDataAttributeValueIntegerUnsigned4(x, y, z, w);
        }
        return;
      }

      default: {
        throw new UnreachableCodeException();
      }
    }
  }

  private static void parseAttributeDataUnsigned3(
    final SMFBDataStreamReaderType reader,
    final long count,
    final SMFAttribute attribute,
    final SMFParserEventsDataAttributeValuesType events_values,
    final Optional<String> name_opt)
    throws IOException
  {
    switch (attribute.componentSizeBits()) {

      case 8: {
        for (long index = 0L;
             Long.compareUnsigned(index, count) < 0;
             ++index) {
          final long x = reader.readU8(name_opt);
          final long y = reader.readU8(name_opt);
          final long z = reader.readU8(name_opt);
          events_values.onDataAttributeValueIntegerUnsigned3(x, y, z);
        }
        return;
      }

      case 16: {
        for (long index = 0L;
             Long.compareUnsigned(index, count) < 0;
             ++index) {
          final long x = reader.readU16(name_opt);
          final long y = reader.readU16(name_opt);
          final long z = reader.readU16(name_opt);
          events_values.onDataAttributeValueIntegerUnsigned3(x, y, z);
        }
        return;
      }

      case 32: {
        for (long index = 0L;
             Long.compareUnsigned(index, count) < 0;
             ++index) {
          final long x = reader.readU32(name_opt);
          final long y = reader.readU32(name_opt);
          final long z = reader.readU32(name_opt);
          events_values.onDataAttributeValueIntegerUnsigned3(x, y, z);
        }
        return;
      }

      case 64: {
        for (long index = 0L;
             Long.compareUnsigned(index, count) < 0;
             ++index) {
          final long x = reader.readU64(name_opt);
          final long y = reader.readU64(name_opt);
          final long z = reader.readU64(name_opt);
          events_values.onDataAttributeValueIntegerUnsigned3(x, y, z);
        }
        return;
      }

      default: {
        throw new UnreachableCodeException();
      }
    }
  }

  private static void parseAttributeDataUnsigned2(
    final SMFBDataStreamReaderType reader,
    final long count,
    final SMFAttribute attribute,
    final SMFParserEventsDataAttributeValuesType events_values,
    final Optional<String> name_opt)
    throws IOException
  {
    switch (attribute.componentSizeBits()) {

      case 8: {
        for (long index = 0L;
             Long.compareUnsigned(index, count) < 0;
             ++index) {
          final long x = reader.readU8(name_opt);
          final long y = reader.readU8(name_opt);
          events_values.onDataAttributeValueIntegerUnsigned2(x, y);
        }
        return;
      }

      case 16: {
        for (long index = 0L;
             Long.compareUnsigned(index, count) < 0;
             ++index) {
          final long x = reader.readU16(name_opt);
          final long y = reader.readU16(name_opt);
          events_values.onDataAttributeValueIntegerUnsigned2(x, y);
        }
        return;
      }

      case 32: {
        for (long index = 0L;
             Long.compareUnsigned(index, count) < 0;
             ++index) {
          final long x = reader.readU32(name_opt);
          final long y = reader.readU32(name_opt);
          events_values.onDataAttributeValueIntegerUnsigned2(x, y);
        }
        return;
      }

      case 64: {
        for (long index = 0L;
             Long.compareUnsigned(index, count) < 0;
             ++index) {
          final long x = reader.readU64(name_opt);
          final long y = reader.readU64(name_opt);
          events_values.onDataAttributeValueIntegerUnsigned2(x, y);
        }
        return;
      }

      default: {
        throw new UnreachableCodeException();
      }
    }
  }

  private static void parseAttributeDataUnsigned1(
    final SMFBDataStreamReaderType reader,
    final long count,
    final SMFAttribute attribute,
    final SMFParserEventsDataAttributeValuesType events_values,
    final Optional<String> name_opt)
    throws IOException
  {
    switch (attribute.componentSizeBits()) {

      case 8: {
        for (long index = 0L;
             Long.compareUnsigned(index, count) < 0;
             ++index) {
          final long x = reader.readU8(name_opt);
          events_values.onDataAttributeValueIntegerUnsigned1(x);
        }
        return;
      }

      case 16: {
        for (long index = 0L;
             Long.compareUnsigned(index, count) < 0;
             ++index) {
          final long x = reader.readU16(name_opt);
          events_values.onDataAttributeValueIntegerUnsigned1(x);
        }
        return;
      }

      case 32: {
        for (long index = 0L;
             Long.compareUnsigned(index, count) < 0;
             ++index) {
          final long x = reader.readU32(name_opt);
          events_values.onDataAttributeValueIntegerUnsigned1(x);
        }
        return;
      }

      case 64: {
        for (long index = 0L;
             Long.compareUnsigned(index, count) < 0;
             ++index) {
          final long x = reader.readU64(name_opt);
          events_values.onDataAttributeValueIntegerUnsigned1(x);
        }
        return;
      }

      default: {
        throw new UnreachableCodeException();
      }
    }
  }
}
