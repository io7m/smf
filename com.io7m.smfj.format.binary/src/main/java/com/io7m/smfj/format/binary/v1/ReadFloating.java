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

final class ReadFloating
{
  private ReadFloating()
  {
    throw new UnreachableCodeException();
  }

  static void parseAttributeDataFloating(
    final SMFBDataStreamReaderType reader,
    final long count,
    final SMFAttribute attribute,
    final SMFParserEventsDataAttributeValuesType events_values,
    final Optional<String> name_opt)
    throws IOException
  {
    switch (attribute.componentCount()) {
      case 1: {
        parseAttributeDataFloating1(
          reader, count, attribute, events_values, name_opt);
        return;
      }
      case 2: {
        parseAttributeDataFloating2(
          reader, count, attribute, events_values, name_opt);
        return;
      }
      case 3: {
        parseAttributeDataFloating3(
          reader, count, attribute, events_values, name_opt);
        return;
      }
      case 4: {
        parseAttributeDataFloating4(
          reader, count, attribute, events_values, name_opt);
        return;
      }
      default: {
        throw new UnreachableCodeException();
      }
    }
  }

  private static void parseAttributeDataFloating4(
    final SMFBDataStreamReaderType reader,
    final long count,
    final SMFAttribute attribute,
    final SMFParserEventsDataAttributeValuesType events_values,
    final Optional<String> name_opt)
    throws IOException
  {
    switch (attribute.componentSizeBits()) {
      case 16: {
        for (long index = 0L;
             Long.compareUnsigned(index, count) < 0;
             ++index) {
          final double x = reader.readF16(name_opt);
          final double y = reader.readF16(name_opt);
          final double z = reader.readF16(name_opt);
          final double w = reader.readF16(name_opt);
          events_values.onDataAttributeValueFloat4(x, y, z, w);
        }
        return;
      }
      case 32: {
        for (long index = 0L;
             Long.compareUnsigned(index, count) < 0;
             ++index) {
          final double x = reader.readF32(name_opt);
          final double y = reader.readF32(name_opt);
          final double z = reader.readF32(name_opt);
          final double w = reader.readF32(name_opt);
          events_values.onDataAttributeValueFloat4(x, y, z, w);
        }
        return;
      }
      case 64: {
        for (long index = 0L;
             Long.compareUnsigned(index, count) < 0;
             ++index) {
          final double x = reader.readF64(name_opt);
          final double y = reader.readF64(name_opt);
          final double z = reader.readF64(name_opt);
          final double w = reader.readF64(name_opt);
          events_values.onDataAttributeValueFloat4(x, y, z, w);
        }
        return;
      }
      default: {
        throw new UnreachableCodeException();
      }
    }
  }

  private static void parseAttributeDataFloating3(
    final SMFBDataStreamReaderType reader,
    final long count,
    final SMFAttribute attribute,
    final SMFParserEventsDataAttributeValuesType events_values,
    final Optional<String> name_opt)
    throws IOException
  {
    switch (attribute.componentSizeBits()) {
      case 16: {
        for (long index = 0L;
             Long.compareUnsigned(index, count) < 0;
             ++index) {
          final double x = reader.readF16(name_opt);
          final double y = reader.readF16(name_opt);
          final double z = reader.readF16(name_opt);
          events_values.onDataAttributeValueFloat3(x, y, z);
        }
        return;
      }
      case 32: {
        for (long index = 0L;
             Long.compareUnsigned(index, count) < 0;
             ++index) {
          final double x = reader.readF32(name_opt);
          final double y = reader.readF32(name_opt);
          final double z = reader.readF32(name_opt);
          events_values.onDataAttributeValueFloat3(x, y, z);
        }
        return;
      }
      case 64: {
        for (long index = 0L;
             Long.compareUnsigned(index, count) < 0;
             ++index) {
          final double x = reader.readF64(name_opt);
          final double y = reader.readF64(name_opt);
          final double z = reader.readF64(name_opt);
          events_values.onDataAttributeValueFloat3(x, y, z);
        }
        return;
      }
      default: {
        throw new UnreachableCodeException();
      }
    }
  }

  private static void parseAttributeDataFloating2(
    final SMFBDataStreamReaderType reader,
    final long count,
    final SMFAttribute attribute,
    final SMFParserEventsDataAttributeValuesType events_values,
    final Optional<String> name_opt)
    throws IOException
  {
    switch (attribute.componentSizeBits()) {
      case 16: {
        for (long index = 0L;
             Long.compareUnsigned(index, count) < 0;
             ++index) {
          final double x = reader.readF16(name_opt);
          final double y = reader.readF16(name_opt);
          events_values.onDataAttributeValueFloat2(x, y);
        }
        return;
      }
      case 32: {
        for (long index = 0L;
             Long.compareUnsigned(index, count) < 0;
             ++index) {
          final double x = reader.readF32(name_opt);
          final double y = reader.readF32(name_opt);
          events_values.onDataAttributeValueFloat2(x, y);
        }
        return;
      }
      case 64: {
        for (long index = 0L;
             Long.compareUnsigned(index, count) < 0;
             ++index) {
          final double x = reader.readF64(name_opt);
          final double y = reader.readF64(name_opt);
          events_values.onDataAttributeValueFloat2(x, y);
        }
        return;
      }
      default: {
        throw new UnreachableCodeException();
      }
    }
  }

  private static void parseAttributeDataFloating1(
    final SMFBDataStreamReaderType reader,
    final long count,
    final SMFAttribute attribute,
    final SMFParserEventsDataAttributeValuesType events_values,
    final Optional<String> name_opt)
    throws IOException
  {
    switch (attribute.componentSizeBits()) {
      case 16: {
        for (long index = 0L;
             Long.compareUnsigned(index, count) < 0;
             ++index) {
          final double x = reader.readF16(name_opt);
          events_values.onDataAttributeValueFloat1(x);
        }
        return;
      }
      case 32: {
        for (long index = 0L;
             Long.compareUnsigned(index, count) < 0;
             ++index) {
          final double x = reader.readF32(name_opt);
          events_values.onDataAttributeValueFloat1(x);
        }
        return;
      }
      case 64: {
        for (long index = 0L;
             Long.compareUnsigned(index, count) < 0;
             ++index) {
          final double x = reader.readF64(name_opt);
          events_values.onDataAttributeValueFloat1(x);
        }
        return;
      }
      default: {
        throw new UnreachableCodeException();
      }
    }
  }
}
