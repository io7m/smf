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

import com.io7m.jpra.runtime.java.JPRACursor1DByteBufferedChecked;
import com.io7m.jpra.runtime.java.JPRACursor1DType;
import com.io7m.jpra.runtime.java.JPRAStringCursorReadableType;
import com.io7m.jpra.runtime.java.JPRAValueByteBufferedConstructorType;
import com.io7m.jpra.runtime.java.JPRAValueType;
import com.io7m.junreachable.UnreachableCodeException;
import com.io7m.smfj.core.SMFAttribute;
import com.io7m.smfj.core.SMFAttributeName;
import com.io7m.smfj.core.SMFComponentType;
import com.io7m.smfj.core.SMFCoordinateSystem;
import com.io7m.smfj.core.SMFFormatVersion;
import com.io7m.smfj.core.SMFHeader;
import com.io7m.smfj.core.SMFPartialLogged;
import com.io7m.smfj.core.SMFSchemaIdentifier;
import com.io7m.smfj.core.SMFSchemaName;
import com.io7m.smfj.core.SMFTriangles;
import com.io7m.smfj.format.binary.SMFBCoordinateSystems;
import com.io7m.smfj.format.binary.SMFBDataStreamReaderType;
import com.io7m.smfj.format.binary.SMFBSection;
import com.io7m.smfj.parser.api.SMFParseError;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Functions to parse version 1.* headers.
 */

public final class SMFBv1Headers
{
  private static final Logger LOG =
    LoggerFactory.getLogger(SMFBv1Headers.class);

  private SMFBv1Headers()
  {
    throw new UnreachableCodeException();
  }

  private static SMFPartialLogged<List<SMFAttribute>> parseAttributes(
    final int attribute_count,
    final SMFBDataStreamReaderType reader)
  {
    try {
      if (LOG.isTraceEnabled()) {
        LOG.trace(
          "attempting to parse {} attributes",
          Integer.toUnsignedString(attribute_count));
      }

      final byte[] bytes = new byte[SMFBv1AttributeByteBuffered.sizeInOctets()];
      final ByteBuffer wrap = ByteBuffer.wrap(bytes).order(ByteOrder.BIG_ENDIAN);
      final JPRACursor1DType<SMFBv1AttributeType> cursor =
        JPRACursor1DByteBufferedChecked.newCursor(
          wrap, SMFBv1AttributeByteBuffered::newValueWithOffset);
      final SMFBv1AttributeType view = cursor.getElementView();

      final List<SMFAttribute> attributes = new ArrayList<>(attribute_count);
      for (int index = 0;
           Integer.compareUnsigned(index, attribute_count) < 0;
           ++index) {
        reader.readBytes(
          Optional.of("Attribute " + Integer.toUnsignedString(index)), bytes);

        final String name =
          view.getNameReadable().getNewValue();
        final SMFComponentType kind =
          SMFComponentType.ofInteger(view.getComponentKind());
        final int size = view.getComponentSize();
        final int count = view.getComponentCount();

        if (LOG.isTraceEnabled()) {
          LOG.trace("attribute: {}", name);
          LOG.trace("kind:      {}", kind);
          LOG.trace("size:      {}", Integer.valueOf(size));
          LOG.trace("count:     {}", Integer.valueOf(count));
        }

        attributes.add(
          SMFAttribute.of(SMFAttributeName.of(name), kind, count, size));
      }

      return SMFPartialLogged.succeeded(attributes);
    } catch (final IOException e) {
      return SMFPartialLogged.failed(SMFParseError.of(
        reader.positionLexical(), e.getMessage(), Optional.of(e)));
    }
  }

  private static <T extends JPRAValueType> JPRACursor1DType<T> readFixedHeader(
    final SMFBDataStreamReaderType reader,
    final JPRAValueByteBufferedConstructorType<T> constructor,
    final int size)
    throws IOException
  {
    final byte[] bytes = new byte[size];
    reader.readBytes(Optional.of("Header"), bytes);

    final ByteBuffer wrap =
      ByteBuffer.wrap(bytes).order(ByteOrder.BIG_ENDIAN);

    return JPRACursor1DByteBufferedChecked.newCursor(wrap, constructor);
  }

  /**
   * Parse a header.
   *
   * @param version The format version declared in the file
   * @param reader  A data stream reader
   * @param section The section
   *
   * @return A parsed header, or a list of parse errors
   */

  public static SMFPartialLogged<SMFHeader> parse(
    final SMFFormatVersion version,
    final SMFBDataStreamReaderType reader,
    final SMFBSection section)
  {
    final HeaderParserType parser = new HeaderV1_0();
    if (Long.compareUnsigned(section.sizeOfData(), parser.sizeRequired()) < 0) {
      final String text =
        new StringBuilder(128)
          .append("Section is too small to contain a version 1.")
          .append(parser.version())
          .append(" header.")
          .append(System.lineSeparator())
          .append("  Section: ")
          .append(Long.toUnsignedString(section.id(), 16))
          .append(System.lineSeparator())
          .append("  Required size: ")
          .append(parser.sizeRequired())
          .append(System.lineSeparator())
          .append("  Received size: ")
          .append(section.sizeOfData())
          .append(System.lineSeparator())
          .toString();
      return SMFPartialLogged.failed(
        SMFParseError.of(reader.positionLexical(), text, Optional.empty()));
    }

    return parser.parse(reader, section);
  }

  private interface HeaderParserType
  {
    SMFPartialLogged<SMFHeader> parse(
      SMFBDataStreamReaderType reader,
      SMFBSection section);

    long sizeRequired();

    int version();
  }

  private static final class HeaderV1_0 implements HeaderParserType
  {
    HeaderV1_0()
    {

    }

    private static void skipAheadToAttributes(
      final long position_start,
      final SMFBDataStreamReaderType reader,
      final SMFBv1_0HeaderType view)
      throws IOException
    {
      final long position_now =
        reader.position();
      final long attribute_offset =
        Math.addExact(position_start, view.getFieldsSize());
      final long seek =
        Math.subtractExact(attribute_offset, position_now);

      if (LOG.isTraceEnabled()) {
        LOG.trace("position start: {}", Long.valueOf(position_start));
        LOG.trace("position now:   {}", Long.valueOf(position_now));
        LOG.trace("position attr:  {}", Long.valueOf(attribute_offset));
        LOG.trace("seek:           {}", Long.valueOf(seek));
      }

      reader.skip(seek);
    }

    @Override
    public SMFPartialLogged<SMFHeader> parse(
      final SMFBDataStreamReaderType reader,
      final SMFBSection section)
    {
      try {
        final long current = reader.position();

        final JPRACursor1DType<SMFBv1_0HeaderType> cursor =
          readFixedHeader(
            reader,
            SMFBv1_0HeaderByteBuffered::newValueWithOffset,
            Math.toIntExact(this.sizeRequired()));

        final SMFBv1_0HeaderType view = cursor.getElementView();

        final SMFCoordinateSystem system =
          SMFBCoordinateSystems.unpack(view.getCoordinateSystemReadable());

        final SMFTriangles triangles =
          SMFTriangles.of(
            view.getTriangleCount(),
            view.getTriangleIndexSizeBits());

        final SMFBv1SchemaIDReadableType schema =
          view.getSchemaReadable();

        final JPRAStringCursorReadableType schema_id_readable =
          schema.getSchemaIdReadable();

        final Optional<SMFSchemaIdentifier> schema_id;
        if (schema_id_readable.getUsedLength() > 0) {
          schema_id =
            Optional.of(
              SMFSchemaIdentifier.builder()
                .setName(SMFSchemaName.of(schema_id_readable.getNewValue()))
                .setVersionMinor(schema.getSchemaVersionMinor())
                .setVersionMajor(schema.getSchemaVersionMajor())
                .build());
        } else {
          schema_id = Optional.empty();
        }

        final SMFHeader.Builder header_b = SMFHeader.builder();
        header_b.setCoordinateSystem(system);
        header_b.setVertexCount(view.getVertexCount());
        header_b.setTriangles(triangles);
        header_b.setSchemaIdentifier(schema_id);

        skipAheadToAttributes(current, reader, view);

        return parseAttributes(view.getAttributeCount(), reader)
          .flatMap(attributes -> {
            header_b.addAllAttributesInOrder(attributes);
            return SMFPartialLogged.succeeded(header_b.build());
          });
      } catch (final IOException e) {
        return SMFPartialLogged.failed(
          SMFParseError.of(
            reader.positionLexical(), e.getMessage(), Optional.of(e)));
      }
    }

    @Override
    public long sizeRequired()
    {
      return Integer.toUnsignedLong(SMFBv1_0HeaderByteBuffered.sizeInOctets());
    }

    @Override
    public int version()
    {
      return 0;
    }
  }
}
