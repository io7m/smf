/*
 * Copyright © 2016 <code@io7m.com> http://io7m.com
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

package com.io7m.smfj.tests.format.binary;

import com.io7m.jfsm.core.FSMTransitionException;
import com.io7m.jpra.runtime.java.JPRACursor1DByteBufferedChecked;
import com.io7m.jpra.runtime.java.JPRACursor1DType;
import com.io7m.jpra.runtime.java.JPRAStringTruncation;
import com.io7m.smfj.core.SMFAttribute;
import com.io7m.smfj.core.SMFAttributeName;
import com.io7m.smfj.core.SMFAttributeNames;
import com.io7m.smfj.core.SMFComponentType;
import com.io7m.smfj.core.SMFFormatVersion;
import com.io7m.smfj.core.SMFHeader;
import com.io7m.smfj.format.binary.SMFFormatBinary;
import com.io7m.smfj.format.binary.v1.SMFBV1AttributeByteBuffered;
import com.io7m.smfj.format.binary.v1.SMFBV1AttributeType;
import com.io7m.smfj.format.binary.v1.SMFBV1HeaderByteBuffered;
import com.io7m.smfj.format.binary.v1.SMFBV1HeaderType;
import com.io7m.smfj.parser.api.SMFParseError;
import com.io7m.smfj.parser.api.SMFParserEventsType;
import com.io7m.smfj.parser.api.SMFParserRandomAccessType;
import com.io7m.smfj.serializer.api.SMFSerializerType;
import javaslang.Tuple;
import javaslang.collection.List;
import mockit.Mocked;
import mockit.StrictExpectations;
import org.hamcrest.Description;
import org.hamcrest.TypeSafeMatcher;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.Path;
import java.nio.file.Paths;

public final class SMFFormatBinaryRandomAccessTest extends SMFBinaryTest
{
  private static final Logger LOG;

  static {
    LOG = LoggerFactory.getLogger(SMFFormatBinaryRandomAccessTest.class);
  }

  @Rule public final ExpectedException expected = ExpectedException.none();

  @Test
  public void testEmpty(
    final @Mocked SMFParserEventsType events)
    throws IOException
  {
    new StrictExpectations()
    {{
      events.onStart();
      events.onError(this.withArgThat(
        new ParseErrorMessageStartsWith(
          "I/O error: Failed to read the required number of octets")));
      events.onFinish();
    }};

    final SMFParserRandomAccessType p = this.parserRandomFor(events, out -> {

    });

    p.parseHeader();
    p.close();
  }

  @Test
  public void testBadMagicNumber(
    final @Mocked SMFParserEventsType events)
    throws IOException
  {
    new StrictExpectations()
    {{
      events.onStart();
      events.onError(this.withArgThat(
        new ParseErrorMessageStartsWith("Bad magic number")));
      events.onFinish();
    }};

    final SMFParserRandomAccessType p = this.parserRandomFor(events, out -> {
      out.putBytes(new byte[]{
        (byte) 'N',
        (byte) 'O',
        (byte) 'T',
        (byte) 'G',
        (byte) 'O',
        (byte) 'O',
        (byte) 'D',
        (byte) 'X'});
    });

    p.parseHeader();
    p.close();
  }

  @Test
  public void testBadVersion(
    final @Mocked SMFParserEventsType events)
    throws IOException
  {
    new StrictExpectations()
    {{
      events.onStart();
      events.onVersionReceived(SMFFormatVersion.of(891237, 0));
      events.onError(this.withArgThat(
        new ParseErrorMessageStartsWith("Unsupported version")));
      events.onFinish();
    }};

    final SMFParserRandomAccessType p = this.parserRandomFor(events, out -> {
      out.putBytes(SMFFormatBinary.magicNumber());
      out.putU32(891237L);
      out.putU32(0L);
    });

    p.parseHeader();
    p.close();
  }

  @Test
  public void testNoData(
    final @Mocked SMFParserEventsType events)
    throws IOException
  {
    final SerializedHeader header_s = new SerializedHeader();
    final SMFHeader.Builder header_b = header_s.headerBuilder();
    final SMFHeader header = header_b.build();

    new StrictExpectations()
    {{
      events.onStart();
      events.onVersionReceived(SMFFormatVersion.of(1, 0));
      events.onHeaderParsed(header);
      events.onFinish();
    }};

    final SMFParserRandomAccessType p = this.parserRandomFor(events, out -> {
      out.putBytes(SMFFormatBinary.magicNumber());
      out.putU32(1L);
      out.putU32(0L);
      out.putBytes(header_s.buffer());
    });

    p.parseHeader();
    p.close();
  }

  @Test
  public void testBadTriangleSize(
    final @Mocked SMFParserEventsType events)
    throws IOException
  {
    final SerializedHeader header_s = new SerializedHeader();
    final byte[] buffer = header_s.buffer();
    final ByteBuffer wrap = ByteBuffer.wrap(buffer);
    final JPRACursor1DType<SMFBV1HeaderType> cursor =
      JPRACursor1DByteBufferedChecked.newCursor(
        wrap, SMFBV1HeaderByteBuffered::newValueWithOffset);
    final SMFBV1HeaderType view = cursor.getElementView();
    view.setTriangleIndexSizeBits(1000);

    new StrictExpectations()
    {{
      events.onStart();
      events.onVersionReceived(SMFFormatVersion.of(1, 0));
      events.onError(this.withArgThat(
        new ParseErrorMessageStartsWith("Invalid triangle index size")));
      events.onFinish();
    }};

    final SMFParserRandomAccessType p = this.parserRandomFor(events, out -> {
      out.putBytes(SMFFormatBinary.magicNumber());
      out.putU32(1L);
      out.putU32(0L);
      out.putBytes(buffer);
    });

    p.parseHeader();
    p.close();
  }

  @Test
  public void testHeaderAttributesFloating(
    final @Mocked SMFParserEventsType events)
    throws IOException
  {
    final SMFAttribute f64_4 = SMFAttribute.of(
      SMFAttributeName.of("F64_4"),
      SMFComponentType.ELEMENT_TYPE_FLOATING,
      4,
      64);
    final SMFAttribute f64_3 = SMFAttribute.of(
      SMFAttributeName.of("F64_3"),
      SMFComponentType.ELEMENT_TYPE_FLOATING,
      3,
      64);
    final SMFAttribute f64_2 = SMFAttribute.of(
      SMFAttributeName.of("F64_2"),
      SMFComponentType.ELEMENT_TYPE_FLOATING,
      2,
      64);
    final SMFAttribute f64_1 = SMFAttribute.of(
      SMFAttributeName.of("F64_1"),
      SMFComponentType.ELEMENT_TYPE_FLOATING,
      1,
      64);

    final SMFAttribute f32_4 = SMFAttribute.of(
      SMFAttributeName.of("F32_4"),
      SMFComponentType.ELEMENT_TYPE_FLOATING,
      4,
      32);
    final SMFAttribute f32_3 = SMFAttribute.of(
      SMFAttributeName.of("F32_3"),
      SMFComponentType.ELEMENT_TYPE_FLOATING,
      3,
      32);
    final SMFAttribute f32_2 = SMFAttribute.of(
      SMFAttributeName.of("F32_2"),
      SMFComponentType.ELEMENT_TYPE_FLOATING,
      2,
      32);
    final SMFAttribute f32_1 = SMFAttribute.of(
      SMFAttributeName.of("F32_1"),
      SMFComponentType.ELEMENT_TYPE_FLOATING,
      1,
      32);

    final SMFAttribute f16_4 = SMFAttribute.of(
      SMFAttributeName.of("F16_4"),
      SMFComponentType.ELEMENT_TYPE_FLOATING,
      4,
      16);
    final SMFAttribute f16_3 = SMFAttribute.of(
      SMFAttributeName.of("F16_3"),
      SMFComponentType.ELEMENT_TYPE_FLOATING,
      3,
      16);
    final SMFAttribute f16_2 = SMFAttribute.of(
      SMFAttributeName.of("F16_2"),
      SMFComponentType.ELEMENT_TYPE_FLOATING,
      2,
      16);
    final SMFAttribute f16_1 = SMFAttribute.of(
      SMFAttributeName.of("F16_1"),
      SMFComponentType.ELEMENT_TYPE_FLOATING,
      1,
      16);

    final List<SMFAttribute> xs = List.of(
      f64_4,
      f64_3,
      f64_2,
      f64_1,
      f32_4,
      f32_3,
      f32_2,
      f32_1,
      f16_4,
      f16_3,
      f16_2,
      f16_1
    );

    final SerializedHeader header_s = new SerializedHeader();
    final SMFHeader.Builder header_b = header_s.headerBuilder();
    header_b.setAttributesInOrder(xs);
    final SMFHeader header = header_b.build();

    new StrictExpectations()
    {{
      events.onStart();
      events.onVersionReceived(SMFFormatVersion.of(1, 0));
      events.onHeaderParsed(header);
      events.onFinish();
    }};

    final SMFParserRandomAccessType p = this.parserRandomFor(events, out -> {
      out.putBytes(SMFFormatBinary.magicNumber());
      out.putU32(1L);
      out.putU32(0L);
      out.putBytes(header_s.buffer());

      out.putStringPadded("F64_4", SMFAttributeNames.MAXIMUM_CHARACTERS);
      out.putU32((long) SMFComponentType.ELEMENT_TYPE_FLOATING.toInteger());
      out.putU32(4L);
      out.putU32(64L);

      out.putStringPadded("F64_3", SMFAttributeNames.MAXIMUM_CHARACTERS);
      out.putU32((long) SMFComponentType.ELEMENT_TYPE_FLOATING.toInteger());
      out.putU32(3L);
      out.putU32(64L);

      out.putStringPadded("F64_2", SMFAttributeNames.MAXIMUM_CHARACTERS);
      out.putU32((long) SMFComponentType.ELEMENT_TYPE_FLOATING.toInteger());
      out.putU32(2L);
      out.putU32(64L);

      out.putStringPadded("F64_1", SMFAttributeNames.MAXIMUM_CHARACTERS);
      out.putU32((long) SMFComponentType.ELEMENT_TYPE_FLOATING.toInteger());
      out.putU32(1L);
      out.putU32(64L);

      out.putStringPadded("F32_4", SMFAttributeNames.MAXIMUM_CHARACTERS);
      out.putU32((long) SMFComponentType.ELEMENT_TYPE_FLOATING.toInteger());
      out.putU32(4L);
      out.putU32(32L);

      out.putStringPadded("F32_3", SMFAttributeNames.MAXIMUM_CHARACTERS);
      out.putU32((long) SMFComponentType.ELEMENT_TYPE_FLOATING.toInteger());
      out.putU32(3L);
      out.putU32(32L);

      out.putStringPadded("F32_2", SMFAttributeNames.MAXIMUM_CHARACTERS);
      out.putU32((long) SMFComponentType.ELEMENT_TYPE_FLOATING.toInteger());
      out.putU32(2L);
      out.putU32(32L);

      out.putStringPadded("F32_1", SMFAttributeNames.MAXIMUM_CHARACTERS);
      out.putU32((long) SMFComponentType.ELEMENT_TYPE_FLOATING.toInteger());
      out.putU32(1L);
      out.putU32(32L);

      out.putStringPadded("F16_4", SMFAttributeNames.MAXIMUM_CHARACTERS);
      out.putU32((long) SMFComponentType.ELEMENT_TYPE_FLOATING.toInteger());
      out.putU32(4L);
      out.putU32(16L);

      out.putStringPadded("F16_3", SMFAttributeNames.MAXIMUM_CHARACTERS);
      out.putU32((long) SMFComponentType.ELEMENT_TYPE_FLOATING.toInteger());
      out.putU32(3L);
      out.putU32(16L);

      out.putStringPadded("F16_2", SMFAttributeNames.MAXIMUM_CHARACTERS);
      out.putU32((long) SMFComponentType.ELEMENT_TYPE_FLOATING.toInteger());
      out.putU32(2L);
      out.putU32(16L);

      out.putStringPadded("F16_1", SMFAttributeNames.MAXIMUM_CHARACTERS);
      out.putU32((long) SMFComponentType.ELEMENT_TYPE_FLOATING.toInteger());
      out.putU32(1L);
      out.putU32(16L);
    });

    p.parseHeader();
    p.close();
  }

  @Test
  public void testHeaderAttributesIntegerSigned(
    final @Mocked SMFParserEventsType events)
    throws IOException
  {
    final SMFAttribute f64_4 = SMFAttribute.of(
      SMFAttributeName.of("F64_4"),
      SMFComponentType.ELEMENT_TYPE_INTEGER_SIGNED,
      4,
      64);
    final SMFAttribute f64_3 = SMFAttribute.of(
      SMFAttributeName.of("F64_3"),
      SMFComponentType.ELEMENT_TYPE_INTEGER_SIGNED,
      3,
      64);
    final SMFAttribute f64_2 = SMFAttribute.of(
      SMFAttributeName.of("F64_2"),
      SMFComponentType.ELEMENT_TYPE_INTEGER_SIGNED,
      2,
      64);
    final SMFAttribute f64_1 = SMFAttribute.of(
      SMFAttributeName.of("F64_1"),
      SMFComponentType.ELEMENT_TYPE_INTEGER_SIGNED,
      1,
      64);

    final SMFAttribute f32_4 = SMFAttribute.of(
      SMFAttributeName.of("F32_4"),
      SMFComponentType.ELEMENT_TYPE_INTEGER_SIGNED,
      4,
      32);
    final SMFAttribute f32_3 = SMFAttribute.of(
      SMFAttributeName.of("F32_3"),
      SMFComponentType.ELEMENT_TYPE_INTEGER_SIGNED,
      3,
      32);
    final SMFAttribute f32_2 = SMFAttribute.of(
      SMFAttributeName.of("F32_2"),
      SMFComponentType.ELEMENT_TYPE_INTEGER_SIGNED,
      2,
      32);
    final SMFAttribute f32_1 = SMFAttribute.of(
      SMFAttributeName.of("F32_1"),
      SMFComponentType.ELEMENT_TYPE_INTEGER_SIGNED,
      1,
      32);

    final SMFAttribute f16_4 = SMFAttribute.of(
      SMFAttributeName.of("F16_4"),
      SMFComponentType.ELEMENT_TYPE_INTEGER_SIGNED,
      4,
      16);
    final SMFAttribute f16_3 = SMFAttribute.of(
      SMFAttributeName.of("F16_3"),
      SMFComponentType.ELEMENT_TYPE_INTEGER_SIGNED,
      3,
      16);
    final SMFAttribute f16_2 = SMFAttribute.of(
      SMFAttributeName.of("F16_2"),
      SMFComponentType.ELEMENT_TYPE_INTEGER_SIGNED,
      2,
      16);
    final SMFAttribute f16_1 = SMFAttribute.of(
      SMFAttributeName.of("F16_1"),
      SMFComponentType.ELEMENT_TYPE_INTEGER_SIGNED,
      1,
      16);

    final SMFAttribute f8_4 = SMFAttribute.of(
      SMFAttributeName.of("F8_4"),
      SMFComponentType.ELEMENT_TYPE_INTEGER_SIGNED,
      4,
      8);
    final SMFAttribute f8_3 = SMFAttribute.of(
      SMFAttributeName.of("F8_3"),
      SMFComponentType.ELEMENT_TYPE_INTEGER_SIGNED,
      3,
      8);
    final SMFAttribute f8_2 = SMFAttribute.of(
      SMFAttributeName.of("F8_2"),
      SMFComponentType.ELEMENT_TYPE_INTEGER_SIGNED,
      2,
      8);
    final SMFAttribute f8_1 = SMFAttribute.of(
      SMFAttributeName.of("F8_1"),
      SMFComponentType.ELEMENT_TYPE_INTEGER_SIGNED,
      1,
      8);

    final List<SMFAttribute> xs = List.of(
      f64_4,
      f64_3,
      f64_2,
      f64_1,
      f32_4,
      f32_3,
      f32_2,
      f32_1,
      f16_4,
      f16_3,
      f16_2,
      f16_1,
      f8_4,
      f8_3,
      f8_2,
      f8_1
    );

    final SerializedHeader header_s = new SerializedHeader();
    final SMFHeader.Builder header_b = header_s.headerBuilder();
    header_b.setAttributesInOrder(xs);
    final SMFHeader header = header_b.build();

    new StrictExpectations()
    {{
      events.onStart();
      events.onVersionReceived(SMFFormatVersion.of(1, 0));
      events.onHeaderParsed(header);
      events.onFinish();
    }};

    final SMFParserRandomAccessType p = this.parserRandomFor(events, out -> {
      out.putBytes(SMFFormatBinary.magicNumber());
      out.putU32(1L);
      out.putU32(0L);
      out.putBytes(header_s.buffer());

      out.putStringPadded("F64_4", SMFAttributeNames.MAXIMUM_CHARACTERS);
      out.putU32((long) SMFComponentType.ELEMENT_TYPE_INTEGER_SIGNED.toInteger());
      out.putU32(4L);
      out.putU32(64L);

      out.putStringPadded("F64_3", SMFAttributeNames.MAXIMUM_CHARACTERS);
      out.putU32((long) SMFComponentType.ELEMENT_TYPE_INTEGER_SIGNED.toInteger());
      out.putU32(3L);
      out.putU32(64L);

      out.putStringPadded("F64_2", SMFAttributeNames.MAXIMUM_CHARACTERS);
      out.putU32((long) SMFComponentType.ELEMENT_TYPE_INTEGER_SIGNED.toInteger());
      out.putU32(2L);
      out.putU32(64L);

      out.putStringPadded("F64_1", SMFAttributeNames.MAXIMUM_CHARACTERS);
      out.putU32((long) SMFComponentType.ELEMENT_TYPE_INTEGER_SIGNED.toInteger());
      out.putU32(1L);
      out.putU32(64L);

      out.putStringPadded("F32_4", SMFAttributeNames.MAXIMUM_CHARACTERS);
      out.putU32((long) SMFComponentType.ELEMENT_TYPE_INTEGER_SIGNED.toInteger());
      out.putU32(4L);
      out.putU32(32L);

      out.putStringPadded("F32_3", SMFAttributeNames.MAXIMUM_CHARACTERS);
      out.putU32((long) SMFComponentType.ELEMENT_TYPE_INTEGER_SIGNED.toInteger());
      out.putU32(3L);
      out.putU32(32L);

      out.putStringPadded("F32_2", SMFAttributeNames.MAXIMUM_CHARACTERS);
      out.putU32((long) SMFComponentType.ELEMENT_TYPE_INTEGER_SIGNED.toInteger());
      out.putU32(2L);
      out.putU32(32L);

      out.putStringPadded("F32_1", SMFAttributeNames.MAXIMUM_CHARACTERS);
      out.putU32((long) SMFComponentType.ELEMENT_TYPE_INTEGER_SIGNED.toInteger());
      out.putU32(1L);
      out.putU32(32L);

      out.putStringPadded("F16_4", SMFAttributeNames.MAXIMUM_CHARACTERS);
      out.putU32((long) SMFComponentType.ELEMENT_TYPE_INTEGER_SIGNED.toInteger());
      out.putU32(4L);
      out.putU32(16L);

      out.putStringPadded("F16_3", SMFAttributeNames.MAXIMUM_CHARACTERS);
      out.putU32((long) SMFComponentType.ELEMENT_TYPE_INTEGER_SIGNED.toInteger());
      out.putU32(3L);
      out.putU32(16L);

      out.putStringPadded("F16_2", SMFAttributeNames.MAXIMUM_CHARACTERS);
      out.putU32((long) SMFComponentType.ELEMENT_TYPE_INTEGER_SIGNED.toInteger());
      out.putU32(2L);
      out.putU32(16L);

      out.putStringPadded("F16_1", SMFAttributeNames.MAXIMUM_CHARACTERS);
      out.putU32((long) SMFComponentType.ELEMENT_TYPE_INTEGER_SIGNED.toInteger());
      out.putU32(1L);
      out.putU32(16L);

      out.putStringPadded("F8_4", SMFAttributeNames.MAXIMUM_CHARACTERS);
      out.putU32((long) SMFComponentType.ELEMENT_TYPE_INTEGER_SIGNED.toInteger());
      out.putU32(4L);
      out.putU32(8L);

      out.putStringPadded("F8_3", SMFAttributeNames.MAXIMUM_CHARACTERS);
      out.putU32((long) SMFComponentType.ELEMENT_TYPE_INTEGER_SIGNED.toInteger());
      out.putU32(3L);
      out.putU32(8L);

      out.putStringPadded("F8_2", SMFAttributeNames.MAXIMUM_CHARACTERS);
      out.putU32((long) SMFComponentType.ELEMENT_TYPE_INTEGER_SIGNED.toInteger());
      out.putU32(2L);
      out.putU32(8L);

      out.putStringPadded("F8_1", SMFAttributeNames.MAXIMUM_CHARACTERS);
      out.putU32((long) SMFComponentType.ELEMENT_TYPE_INTEGER_SIGNED.toInteger());
      out.putU32(1L);
      out.putU32(8L);
    });

    p.parseHeader();
    p.close();
  }

  @Test
  public void testHeaderAttributesIntegerUnsigned(
    final @Mocked SMFParserEventsType events)
    throws IOException
  {
    final SMFAttribute f64_4 = SMFAttribute.of(
      SMFAttributeName.of("F64_4"),
      SMFComponentType.ELEMENT_TYPE_INTEGER_UNSIGNED,
      4,
      64);
    final SMFAttribute f64_3 = SMFAttribute.of(
      SMFAttributeName.of("F64_3"),
      SMFComponentType.ELEMENT_TYPE_INTEGER_UNSIGNED,
      3,
      64);
    final SMFAttribute f64_2 = SMFAttribute.of(
      SMFAttributeName.of("F64_2"),
      SMFComponentType.ELEMENT_TYPE_INTEGER_UNSIGNED,
      2,
      64);
    final SMFAttribute f64_1 = SMFAttribute.of(
      SMFAttributeName.of("F64_1"),
      SMFComponentType.ELEMENT_TYPE_INTEGER_UNSIGNED,
      1,
      64);

    final SMFAttribute f32_4 = SMFAttribute.of(
      SMFAttributeName.of("F32_4"),
      SMFComponentType.ELEMENT_TYPE_INTEGER_UNSIGNED,
      4,
      32);
    final SMFAttribute f32_3 = SMFAttribute.of(
      SMFAttributeName.of("F32_3"),
      SMFComponentType.ELEMENT_TYPE_INTEGER_UNSIGNED,
      3,
      32);
    final SMFAttribute f32_2 = SMFAttribute.of(
      SMFAttributeName.of("F32_2"),
      SMFComponentType.ELEMENT_TYPE_INTEGER_UNSIGNED,
      2,
      32);
    final SMFAttribute f32_1 = SMFAttribute.of(
      SMFAttributeName.of("F32_1"),
      SMFComponentType.ELEMENT_TYPE_INTEGER_UNSIGNED,
      1,
      32);

    final SMFAttribute f16_4 = SMFAttribute.of(
      SMFAttributeName.of("F16_4"),
      SMFComponentType.ELEMENT_TYPE_INTEGER_UNSIGNED,
      4,
      16);
    final SMFAttribute f16_3 = SMFAttribute.of(
      SMFAttributeName.of("F16_3"),
      SMFComponentType.ELEMENT_TYPE_INTEGER_UNSIGNED,
      3,
      16);
    final SMFAttribute f16_2 = SMFAttribute.of(
      SMFAttributeName.of("F16_2"),
      SMFComponentType.ELEMENT_TYPE_INTEGER_UNSIGNED,
      2,
      16);
    final SMFAttribute f16_1 = SMFAttribute.of(
      SMFAttributeName.of("F16_1"),
      SMFComponentType.ELEMENT_TYPE_INTEGER_UNSIGNED,
      1,
      16);

    final SMFAttribute f8_4 = SMFAttribute.of(
      SMFAttributeName.of("F8_4"),
      SMFComponentType.ELEMENT_TYPE_INTEGER_UNSIGNED,
      4,
      8);
    final SMFAttribute f8_3 = SMFAttribute.of(
      SMFAttributeName.of("F8_3"),
      SMFComponentType.ELEMENT_TYPE_INTEGER_UNSIGNED,
      3,
      8);
    final SMFAttribute f8_2 = SMFAttribute.of(
      SMFAttributeName.of("F8_2"),
      SMFComponentType.ELEMENT_TYPE_INTEGER_UNSIGNED,
      2,
      8);
    final SMFAttribute f8_1 = SMFAttribute.of(
      SMFAttributeName.of("F8_1"),
      SMFComponentType.ELEMENT_TYPE_INTEGER_UNSIGNED,
      1,
      8);

    final List<SMFAttribute> xs = List.of(
      f64_4,
      f64_3,
      f64_2,
      f64_1,
      f32_4,
      f32_3,
      f32_2,
      f32_1,
      f16_4,
      f16_3,
      f16_2,
      f16_1,
      f8_4,
      f8_3,
      f8_2,
      f8_1
    );

    final SerializedHeader header_s = new SerializedHeader();
    final SMFHeader.Builder header_b = header_s.headerBuilder();
    header_b.setAttributesInOrder(xs);
    final SMFHeader header = header_b.build();

    new StrictExpectations()
    {{
      events.onStart();
      events.onVersionReceived(SMFFormatVersion.of(1, 0));
      events.onHeaderParsed(header);
      events.onFinish();
    }};

    final SMFParserRandomAccessType p = this.parserRandomFor(
      events,
      out -> {
        out.putBytes(SMFFormatBinary.magicNumber());
        out.putU32(1L);
        out.putU32(0L);
        out.putBytes(header_s.buffer());

        out.putStringPadded("F64_4", SMFAttributeNames.MAXIMUM_CHARACTERS);
        out.putU32((long) SMFComponentType.ELEMENT_TYPE_INTEGER_UNSIGNED.toInteger());
        out.putU32(4L);
        out.putU32(64L);

        out.putStringPadded("F64_3", SMFAttributeNames.MAXIMUM_CHARACTERS);
        out.putU32((long) SMFComponentType.ELEMENT_TYPE_INTEGER_UNSIGNED.toInteger());
        out.putU32(3L);
        out.putU32(64L);

        out.putStringPadded("F64_2", SMFAttributeNames.MAXIMUM_CHARACTERS);
        out.putU32((long) SMFComponentType.ELEMENT_TYPE_INTEGER_UNSIGNED.toInteger());
        out.putU32(2L);
        out.putU32(64L);

        out.putStringPadded("F64_1", SMFAttributeNames.MAXIMUM_CHARACTERS);
        out.putU32((long) SMFComponentType.ELEMENT_TYPE_INTEGER_UNSIGNED.toInteger());
        out.putU32(1L);
        out.putU32(64L);

        out.putStringPadded("F32_4", SMFAttributeNames.MAXIMUM_CHARACTERS);
        out.putU32((long) SMFComponentType.ELEMENT_TYPE_INTEGER_UNSIGNED.toInteger());
        out.putU32(4L);
        out.putU32(32L);

        out.putStringPadded("F32_3", SMFAttributeNames.MAXIMUM_CHARACTERS);
        out.putU32((long) SMFComponentType.ELEMENT_TYPE_INTEGER_UNSIGNED.toInteger());
        out.putU32(3L);
        out.putU32(32L);

        out.putStringPadded("F32_2", SMFAttributeNames.MAXIMUM_CHARACTERS);
        out.putU32((long) SMFComponentType.ELEMENT_TYPE_INTEGER_UNSIGNED.toInteger());
        out.putU32(2L);
        out.putU32(32L);

        out.putStringPadded("F32_1", SMFAttributeNames.MAXIMUM_CHARACTERS);
        out.putU32((long) SMFComponentType.ELEMENT_TYPE_INTEGER_UNSIGNED.toInteger());
        out.putU32(1L);
        out.putU32(32L);

        out.putStringPadded("F16_4", SMFAttributeNames.MAXIMUM_CHARACTERS);
        out.putU32((long) SMFComponentType.ELEMENT_TYPE_INTEGER_UNSIGNED.toInteger());
        out.putU32(4L);
        out.putU32(16L);

        out.putStringPadded("F16_3", SMFAttributeNames.MAXIMUM_CHARACTERS);
        out.putU32((long) SMFComponentType.ELEMENT_TYPE_INTEGER_UNSIGNED.toInteger());
        out.putU32(3L);
        out.putU32(16L);

        out.putStringPadded("F16_2", SMFAttributeNames.MAXIMUM_CHARACTERS);
        out.putU32((long) SMFComponentType.ELEMENT_TYPE_INTEGER_UNSIGNED.toInteger());
        out.putU32(2L);
        out.putU32(16L);

        out.putStringPadded("F16_1", SMFAttributeNames.MAXIMUM_CHARACTERS);
        out.putU32((long) SMFComponentType.ELEMENT_TYPE_INTEGER_UNSIGNED.toInteger());
        out.putU32(1L);
        out.putU32(16L);

        out.putStringPadded("F8_4", SMFAttributeNames.MAXIMUM_CHARACTERS);
        out.putU32((long) SMFComponentType.ELEMENT_TYPE_INTEGER_UNSIGNED.toInteger());
        out.putU32(4L);
        out.putU32(8L);

        out.putStringPadded("F8_3", SMFAttributeNames.MAXIMUM_CHARACTERS);
        out.putU32((long) SMFComponentType.ELEMENT_TYPE_INTEGER_UNSIGNED.toInteger());
        out.putU32(3L);
        out.putU32(8L);

        out.putStringPadded("F8_2", SMFAttributeNames.MAXIMUM_CHARACTERS);
        out.putU32((long) SMFComponentType.ELEMENT_TYPE_INTEGER_UNSIGNED.toInteger());
        out.putU32(2L);
        out.putU32(8L);

        out.putStringPadded("F8_1", SMFAttributeNames.MAXIMUM_CHARACTERS);
        out.putU32((long) SMFComponentType.ELEMENT_TYPE_INTEGER_UNSIGNED.toInteger());
        out.putU32(1L);
        out.putU32(8L);
      });

    p.parseHeader();
    p.close();
  }

  @Test
  public void testHeaderAttributeBadType(
    final @Mocked SMFParserEventsType events)
    throws IOException
  {
    new StrictExpectations()
    {{
      events.onStart();
      events.onVersionReceived(SMFFormatVersion.of(1, 0));
      events.onError(this.withArgThat(
        new ParseErrorMessageStartsWith("Unrecognized type for integer index")));
      events.onFinish();
    }};

    final List<SMFAttribute> attributes = List.of(
      SMFAttribute.of(
        SMFAttributeName.of("x"),
        SMFComponentType.ELEMENT_TYPE_FLOATING,
        4,
        32));

    final SerializedHeader header_s = new SerializedHeader();
    final SMFHeader.Builder header_b = header_s.headerBuilder();
    header_b.setAttributesInOrder(attributes);

    final byte[] attribute_buffer =
      new byte[SMFBV1AttributeByteBuffered.sizeInOctets()];
    final ByteBuffer attribute_wrap =
      ByteBuffer.wrap(attribute_buffer);
    final JPRACursor1DType<SMFBV1AttributeType> attribute_cursor =
      JPRACursor1DByteBufferedChecked.newCursor(
        attribute_wrap,
        SMFBV1AttributeByteBuffered::newValueWithOffset);
    final SMFBV1AttributeType attribute_view =
      attribute_cursor.getElementView();

    attribute_view.getNameWritable().setValue(
      "F64_4",
      JPRAStringTruncation.REJECT);
    attribute_view.setComponentCount(4);
    attribute_view.setComponentKind(1000);
    attribute_view.setComponentSize(64);

    final SMFParserRandomAccessType p = this.parserRandomFor(events, out -> {
      out.putBytes(SMFFormatBinary.magicNumber());
      out.putU32(1L);
      out.putU32(0L);
      out.putBytes(header_s.buffer());
      out.putBytes(attribute_buffer);
    });

    p.parseHeader();
    p.close();
  }

  @Test
  public void testHeaderAttributeBadComponentCount0(
    final @Mocked SMFParserEventsType events)
    throws IOException
  {
    new StrictExpectations()
    {{
      events.onStart();
      events.onVersionReceived(SMFFormatVersion.of(1, 0));
      events.onError(this.withArgThat(
        new ParseErrorMessageStartsWith(
          "Component count must be in the range [1, 4]")));
      events.onFinish();
    }};

    final List<SMFAttribute> attributes = List.of(
      SMFAttribute.of(
        SMFAttributeName.of("x"),
        SMFComponentType.ELEMENT_TYPE_FLOATING,
        4,
        32));

    final SerializedHeader header_s = new SerializedHeader();
    final SMFHeader.Builder header_b = header_s.headerBuilder();
    header_b.setAttributesInOrder(attributes);

    final byte[] attribute_buffer =
      new byte[SMFBV1AttributeByteBuffered.sizeInOctets()];
    final ByteBuffer attribute_wrap =
      ByteBuffer.wrap(attribute_buffer);
    final JPRACursor1DType<SMFBV1AttributeType> attribute_cursor =
      JPRACursor1DByteBufferedChecked.newCursor(
        attribute_wrap,
        SMFBV1AttributeByteBuffered::newValueWithOffset);
    final SMFBV1AttributeType attribute_view =
      attribute_cursor.getElementView();

    attribute_view.getNameWritable().setValue(
      "F64_4",
      JPRAStringTruncation.REJECT);
    attribute_view.setComponentCount(1000);
    attribute_view.setComponentKind(0);
    attribute_view.setComponentSize(64);

    final SMFParserRandomAccessType p = this.parserRandomFor(events, out -> {
      out.putBytes(SMFFormatBinary.magicNumber());
      out.putU32(1L);
      out.putU32(0L);
      out.putBytes(header_s.buffer());
      out.putBytes(attribute_buffer);
    });

    p.parseHeader();
    p.close();
  }

  @Test
  public void testHeaderAttributeBadComponentCount1(
    final @Mocked SMFParserEventsType events)
    throws IOException
  {
    new StrictExpectations()
    {{
      events.onStart();
      events.onVersionReceived(SMFFormatVersion.of(1, 0));
      events.onError(this.withArgThat(
        new ParseErrorMessageStartsWith(
          "Component count must be in the range [1, 4]")));
      events.onFinish();
    }};

    final List<SMFAttribute> attributes = List.of(
      SMFAttribute.of(
        SMFAttributeName.of("x"),
        SMFComponentType.ELEMENT_TYPE_FLOATING,
        4,
        32));

    final SerializedHeader header_s = new SerializedHeader();
    final SMFHeader.Builder header_b = header_s.headerBuilder();
    header_b.setAttributesInOrder(attributes);

    final byte[] attribute_buffer =
      new byte[SMFBV1AttributeByteBuffered.sizeInOctets()];
    final ByteBuffer attribute_wrap =
      ByteBuffer.wrap(attribute_buffer);
    final JPRACursor1DType<SMFBV1AttributeType> attribute_cursor =
      JPRACursor1DByteBufferedChecked.newCursor(
        attribute_wrap,
        SMFBV1AttributeByteBuffered::newValueWithOffset);
    final SMFBV1AttributeType attribute_view =
      attribute_cursor.getElementView();

    attribute_view.getNameWritable().setValue(
      "F64_4",
      JPRAStringTruncation.REJECT);
    attribute_view.setComponentCount(0);
    attribute_view.setComponentKind(0);
    attribute_view.setComponentSize(64);

    final SMFParserRandomAccessType p = this.parserRandomFor(events, out -> {
      out.putBytes(SMFFormatBinary.magicNumber());
      out.putU32(1L);
      out.putU32(0L);
      out.putBytes(header_s.buffer());
      out.putBytes(attribute_buffer);
    });

    p.parseHeader();
    p.close();
  }

  @Test
  public void testHeaderAttributeDuplicate(
    final @Mocked SMFParserEventsType events)
    throws IOException
  {
    new StrictExpectations()
    {{
      events.onStart();
      events.onVersionReceived(SMFFormatVersion.of(1, 0));
      events.onError(this.withArgThat(
        new ParseErrorMessageStartsWith("Duplicate attribute name: ATTRIBUTE")));
      events.onFinish();
    }};

    final List<SMFAttribute> attributes = List.of(
      SMFAttribute.of(
        SMFAttributeName.of("x"),
        SMFComponentType.ELEMENT_TYPE_FLOATING,
        4,
        32),
      SMFAttribute.of(
        SMFAttributeName.of("y"),
        SMFComponentType.ELEMENT_TYPE_FLOATING,
        4,
        32));

    final SerializedHeader header_s = new SerializedHeader();
    final SMFHeader.Builder header_b = header_s.headerBuilder();
    header_b.setAttributesInOrder(attributes);

    final byte[] attribute_buffer =
      new byte[SMFBV1AttributeByteBuffered.sizeInOctets()];
    final ByteBuffer attribute_wrap =
      ByteBuffer.wrap(attribute_buffer);
    final JPRACursor1DType<SMFBV1AttributeType> attribute_cursor =
      JPRACursor1DByteBufferedChecked.newCursor(
        attribute_wrap,
        SMFBV1AttributeByteBuffered::newValueWithOffset);
    final SMFBV1AttributeType attribute_view =
      attribute_cursor.getElementView();

    attribute_view.getNameWritable().setValue(
      "ATTRIBUTE",
      JPRAStringTruncation.REJECT);
    attribute_view.setComponentCount(4);
    attribute_view.setComponentKind(0);
    attribute_view.setComponentSize(64);

    final SMFParserRandomAccessType p = this.parserRandomFor(events, out -> {
      out.putBytes(SMFFormatBinary.magicNumber());
      out.putU32(1L);
      out.putU32(0L);
      out.putBytes(header_s.buffer());
      out.putBytes(attribute_buffer);
      out.putBytes(attribute_buffer);
    });
    p.parseHeader();
    p.close();
  }

  @Test
  public void testSerializerUnknownAttribute()
    throws IOException
  {
    final ByteArrayOutputStream out = new ByteArrayOutputStream();
    final Path path = Paths.get("/data");
    final SMFFormatVersion version = SMFFormatVersion.of(1, 0);

    final List<SMFAttribute> attributes = List.of(
      SMFAttribute.of(
        SMFAttributeName.of("x"),
        SMFComponentType.ELEMENT_TYPE_FLOATING,
        4,
        32));

    final SerializedHeader header_s = new SerializedHeader();
    final SMFHeader.Builder header_b = header_s.headerBuilder();
    header_b.setAttributesInOrder(attributes);
    final SMFHeader header = header_b.build();

    final SMFSerializerType serializer =
      new SMFFormatBinary().serializerCreate(version, path, out);

    serializer.serializeHeader(header);
    serializer.serializeDataStart();

    this.expected.expect(IllegalArgumentException.class);
    serializer.serializeData(SMFAttributeName.of("unknown"));
  }

  @Test
  public void testSerializerAttributeBeforeHeader()
    throws IOException
  {
    final ByteArrayOutputStream out = new ByteArrayOutputStream();
    final Path path = Paths.get("/data");
    final SMFFormatVersion version = SMFFormatVersion.of(1, 0);

    final SMFSerializerType serializer =
      new SMFFormatBinary().serializerCreate(version, path, out);

    this.expected.expect(FSMTransitionException.class);
    serializer.serializeData(SMFAttributeName.of("unknown"));
  }

  @Test
  public void testSerializerAttributeWrong()
    throws IOException
  {
    final ByteArrayOutputStream out = new ByteArrayOutputStream();
    final Path path = Paths.get("/data");
    final SMFFormatVersion version = SMFFormatVersion.of(1, 0);

    final SMFSerializerType serializer =
      new SMFFormatBinary().serializerCreate(version, path, out);

    final List<SMFAttribute> attributes = List.of(
      SMFAttribute.of(
        SMFAttributeName.of("x"),
        SMFComponentType.ELEMENT_TYPE_FLOATING,
        4,
        32),
      SMFAttribute.of(
        SMFAttributeName.of("y"),
        SMFComponentType.ELEMENT_TYPE_FLOATING,
        4,
        32));

    final SerializedHeader header_s = new SerializedHeader();
    final SMFHeader.Builder header_b = header_s.headerBuilder();
    header_b.setAttributesInOrder(attributes);
    final SMFHeader header = header_b.build();

    serializer.serializeHeader(header);
    serializer.serializeDataStart();
    serializer.serializeData(SMFAttributeName.of("x"));

    this.expected.expect(IllegalArgumentException.class);
    serializer.serializeData(SMFAttributeName.of("x"));
  }

  @Test
  public void testSerializerAttributeNoneExpected()
    throws IOException
  {
    final ByteArrayOutputStream out = new ByteArrayOutputStream();
    final Path path = Paths.get("/data");
    final SMFFormatVersion version = SMFFormatVersion.of(1, 0);

    final SMFSerializerType serializer =
      new SMFFormatBinary().serializerCreate(version, path, out);

    final List<SMFAttribute> attributes = List.of(
      SMFAttribute.of(
        SMFAttributeName.of("x"),
        SMFComponentType.ELEMENT_TYPE_FLOATING,
        4,
        32));

    final SerializedHeader header_s = new SerializedHeader();
    final SMFHeader.Builder header_b = header_s.headerBuilder();
    header_b.setAttributesInOrder(attributes);
    final SMFHeader header = header_b.build();

    serializer.serializeHeader(header);
    serializer.serializeDataStart();
    serializer.serializeData(SMFAttributeName.of("x"));

    this.expected.expect(IllegalArgumentException.class);
    serializer.serializeData(SMFAttributeName.of("x"));
  }

  @Test
  public void testSerializerAttributeNotFinishedSerializingPrevious()
    throws IOException
  {
    final ByteArrayOutputStream out = new ByteArrayOutputStream();
    final Path path = Paths.get("/data");
    final SMFFormatVersion version = SMFFormatVersion.of(1, 0);

    final SMFSerializerType serializer =
      new SMFFormatBinary().serializerCreate(version, path, out);

    final List<SMFAttribute> attributes = List.of(
      SMFAttribute.of(
        SMFAttributeName.of("x"),
        SMFComponentType.ELEMENT_TYPE_FLOATING,
        4,
        32),
      SMFAttribute.of(
        SMFAttributeName.of("y"),
        SMFComponentType.ELEMENT_TYPE_FLOATING,
        4,
        32));

    final SerializedHeader header_s = new SerializedHeader();
    final SMFHeader.Builder header_b = header_s.headerBuilder();
    header_b.setVertexCount(1L);
    header_b.setAttributesInOrder(attributes);
    final SMFHeader header = header_b.build();

    serializer.serializeHeader(header);
    serializer.serializeDataStart();
    serializer.serializeData(SMFAttributeName.of("x"));

    this.expected.expect(IllegalStateException.class);
    serializer.serializeData(SMFAttributeName.of("y"));
  }

  private static class ParseErrorMessageStartsWith extends TypeSafeMatcher<SMFParseError>
  {
    private final String message;

    ParseErrorMessageStartsWith(
      final String in_message)
    {
      this.message = in_message;
    }

    @Override
    protected final boolean matchesSafely(final SMFParseError item)
    {
      return item.message().startsWith(this.message);
    }

    @Override
    public final void describeTo(final Description description)
    {
      description.appendText(this.message);
    }
  }
}
