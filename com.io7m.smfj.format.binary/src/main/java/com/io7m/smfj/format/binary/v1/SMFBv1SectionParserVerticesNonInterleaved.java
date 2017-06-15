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

import com.io7m.smfj.core.SMFAttribute;
import com.io7m.smfj.core.SMFHeader;
import com.io7m.smfj.format.binary.SMFBAlignment;
import com.io7m.smfj.format.binary.SMFBBodySectionParserType;
import com.io7m.smfj.format.binary.SMFBDataStreamReaderType;
import com.io7m.smfj.format.binary.SMFBSectionType;
import com.io7m.smfj.format.binary.SMFBSectionVerticesNonInterleaved;
import com.io7m.smfj.parser.api.SMFParseError;
import com.io7m.smfj.parser.api.SMFParserEventsBodyType;
import com.io7m.smfj.parser.api.SMFParserEventsDataAttributeValuesType;
import com.io7m.smfj.parser.api.SMFParserEventsDataAttributesNonInterleavedType;

import java.io.IOException;
import java.util.Optional;

import static com.io7m.jnull.NullCheck.notNull;

/**
 * A parser for non-interleaved vertex data.
 */

public final class SMFBv1SectionParserVerticesNonInterleaved
  implements SMFBBodySectionParserType
{
  /**
   * Construct a parser.
   */

  public SMFBv1SectionParserVerticesNonInterleaved()
  {

  }

  private static void processAttributes(
    final SMFHeader header,
    final SMFBDataStreamReaderType reader,
    final SMFParserEventsDataAttributesNonInterleavedType events_vni)
  {
    for (final SMFAttribute attribute : header.attributesInOrder()) {
      final Optional<SMFParserEventsDataAttributeValuesType> events_values_opt =
        events_vni.onDataAttributeStart(attribute);

      final long vertices = header.vertexCount();
      final long end_offset =
        SMFBAlignment.alignNext(
          Math.multiplyExact(vertices, (long) attribute.sizeOctets()),
          SMFBSectionType.SECTION_ALIGNMENT);

      final long position_current =
        reader.position();
      final long position_end =
        Math.addExact(position_current, end_offset);

      if (events_values_opt.isPresent()) {
        final SMFParserEventsDataAttributeValuesType events_values =
          events_values_opt.get();

        try {
          switch (attribute.componentType()) {
            case ELEMENT_TYPE_INTEGER_SIGNED:
              ReadSigned.parseAttributeDataSigned(
                reader,
                vertices,
                attribute,
                events_values,
                Optional.of(attribute.name().value()));
              break;

            case ELEMENT_TYPE_INTEGER_UNSIGNED:
              ReadUnsigned.parseAttributeDataUnsigned(
                reader,
                vertices,
                attribute,
                events_values,
                Optional.of(attribute.name().value()));
              break;

            case ELEMENT_TYPE_FLOATING:
              ReadFloating.parseAttributeDataFloating(
                reader,
                vertices,
                attribute,
                events_values,
                Optional.of(attribute.name().value()));
              break;
          }
        } catch (final IOException e) {
          events_values.onError(SMFParseError.of(
            reader.positionLexical(), e.getMessage(), Optional.of(e)));
        } finally {
          events_values.onDataAttributeValueFinish();
        }
      }

      try {
        reader.skipTo(position_end);
      } catch (final IOException e) {
        events_vni.onError(SMFParseError.of(
          reader.positionLexical(), e.getMessage(), Optional.of(e)));
        return;
      }
    }
  }

  @Override
  public long magic()
  {
    return SMFBSectionVerticesNonInterleaved.MAGIC;
  }

  @Override
  public void parse(
    final SMFHeader header,
    final SMFParserEventsBodyType events,
    final SMFBDataStreamReaderType reader)
  {
    notNull(header, "Header");
    notNull(events, "Events");
    notNull(reader, "Reader");

    final Optional<SMFParserEventsDataAttributesNonInterleavedType> events_vni_opt =
      events.onAttributesNonInterleaved();

    if (events_vni_opt.isPresent()) {
      final SMFParserEventsDataAttributesNonInterleavedType events_vni =
        events_vni_opt.get();

      try {
        processAttributes(header, reader, events_vni);
      } finally {
        events_vni.onDataAttributesNonInterleavedFinish();
      }
    }
  }
}
