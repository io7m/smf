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

import com.io7m.smfj.core.SMFAttribute;
import com.io7m.smfj.core.SMFAttributeName;
import com.io7m.smfj.core.SMFAttributeNameType;
import com.io7m.smfj.core.SMFComponentType;
import com.io7m.smfj.core.SMFFormatVersion;
import com.io7m.smfj.format.binary.SMFFormatBinary;
import com.io7m.smfj.parser.api.SMFParserEventsType;
import com.io7m.smfj.parser.api.SMFParserRandomAccessType;
import mockit.Mocked;
import mockit.StrictExpectations;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class SMFFormatBinaryIntegerUnsignedTest extends SMFBinaryTest
{
  private static final Logger LOG;

  static {
    LOG = LoggerFactory.getLogger(SMFFormatBinaryIntegerUnsignedTest.class);
  }

  @Test
  public void testDataAttributesIntegerUnsigned64_4(
    final @Mocked SMFParserEventsType events)
  {
    final String name = "Unsigned64_4";
    final long component_count = 4L;
    final long component_size = 64L;

    new StrictExpectations()
    {{
      events.onStart();
      events.onVersionReceived(SMFFormatVersion.of(1, 0));
      events.onHeaderStart();
      events.onHeaderAttributeCountReceived(1L);

      final SMFAttribute attribute = SMFAttribute.of(
        SMFAttributeName.of(name),
        SMFComponentType.ELEMENT_TYPE_INTEGER_UNSIGNED,
        (int) component_count,
        (int) component_size);
      events.onHeaderAttributeReceived(attribute);

      events.onHeaderVerticesCountReceived(3L);
      events.onHeaderTrianglesCountReceived(0L);
      events.onHeaderTrianglesIndexSizeReceived(32L);
      events.onHeaderFinish();

      events.onDataAttributeStart(attribute);
      events.onDataAttributeValueIntegerUnsigned4(0L, 1L, 2L, 3L);
      events.onDataAttributeValueIntegerUnsigned4(10L, 11L, 12L, 13L);
      events.onDataAttributeValueIntegerUnsigned4(20L, 21L, 22L, 23L);
      events.onDataAttributeFinish(attribute);
    }};

    final SMFParserRandomAccessType p = this.parserFor(events, out -> {
      out.putBytes(SMFFormatBinary.magicNumber());
      out.putU32(1L);
      out.putU32(0L);

      out.putU64(3L);
      out.putU64(0L);
      out.putU32(32L);
      out.putU32(0x7f7f7f7fL);
      out.putU32(1L);
      out.putU32(0x7f7f7f7fL);

      out.putStringPad(name, SMFAttributeNameType.MAXIMUM_CHARACTERS);
      out.putU32((long) SMFComponentType.ELEMENT_TYPE_INTEGER_UNSIGNED.toInteger());
      out.putU32(component_count);
      out.putU32(component_size);

      for (int vertex = 0; vertex < 3; ++vertex) {
        for (int component = 0; (long) component < component_count; ++component) {
          final long value = (long) ((vertex * 10) + component);
          LOG.debug(
            "vertex {} component {}: {}",
            Integer.valueOf(vertex),
            Integer.valueOf(component),
            Long.valueOf(value));
          out.putU64(value);
        }
      }
    });

    p.parseHeader();
    p.parseAttributeData(SMFAttributeName.of(name));
  }

  @Test
  public void testDataAttributesIntegerUnsigned64_3(
    final @Mocked SMFParserEventsType events)
  {
    final String name = "Unsigned64_3";
    final long component_count = 3L;
    final long component_size = 64L;

    new StrictExpectations()
    {{
      events.onStart();
      events.onVersionReceived(SMFFormatVersion.of(1, 0));
      events.onHeaderStart();
      events.onHeaderAttributeCountReceived(1L);

      final SMFAttribute attribute = SMFAttribute.of(
        SMFAttributeName.of(name),
        SMFComponentType.ELEMENT_TYPE_INTEGER_UNSIGNED,
        (int) component_count,
        (int) component_size);
      events.onHeaderAttributeReceived(attribute);

      events.onHeaderVerticesCountReceived(3L);
      events.onHeaderTrianglesCountReceived(0L);
      events.onHeaderTrianglesIndexSizeReceived(32L);
      events.onHeaderFinish();

      events.onDataAttributeStart(attribute);
      events.onDataAttributeValueIntegerUnsigned3(0L, 1L, 2L);
      events.onDataAttributeValueIntegerUnsigned3(10L, 11L, 12L);
      events.onDataAttributeValueIntegerUnsigned3(20L, 21L, 22L);
      events.onDataAttributeFinish(attribute);
    }};

    final SMFParserRandomAccessType p = this.parserFor(events, out -> {
      out.putBytes(SMFFormatBinary.magicNumber());
      out.putU32(1L);
      out.putU32(0L);

      out.putU64(3L);
      out.putU64(0L);
      out.putU32(32L);
      out.putU32(0x7f7f7f7fL);
      out.putU32(1L);
      out.putU32(0x7f7f7f7fL);

      out.putStringPad(name, SMFAttributeNameType.MAXIMUM_CHARACTERS);
      out.putU32((long) SMFComponentType.ELEMENT_TYPE_INTEGER_UNSIGNED.toInteger());
      out.putU32(component_count);
      out.putU32(component_size);

      for (int vertex = 0; vertex < 3; ++vertex) {
        for (int component = 0; (long) component < component_count; ++component) {
          final long value = (long) ((vertex * 10) + component);
          LOG.debug(
            "vertex {} component {}: {}",
            Integer.valueOf(vertex),
            Integer.valueOf(component),
            Long.valueOf(value));
          out.putU64(value);
        }
      }
    });

    p.parseHeader();
    p.parseAttributeData(SMFAttributeName.of(name));
  }

  @Test
  public void testDataAttributesIntegerUnsigned64_2(
    final @Mocked SMFParserEventsType events)
  {
    final String name = "Unsigned64_2";
    final long component_count = 2L;
    final long component_size = 64L;

    new StrictExpectations()
    {{
      events.onStart();
      events.onVersionReceived(SMFFormatVersion.of(1, 0));
      events.onHeaderStart();
      events.onHeaderAttributeCountReceived(1L);

      final SMFAttribute attribute = SMFAttribute.of(
        SMFAttributeName.of(name),
        SMFComponentType.ELEMENT_TYPE_INTEGER_UNSIGNED,
        (int) component_count,
        (int) component_size);
      events.onHeaderAttributeReceived(attribute);

      events.onHeaderVerticesCountReceived(3L);
      events.onHeaderTrianglesCountReceived(0L);
      events.onHeaderTrianglesIndexSizeReceived(32L);
      events.onHeaderFinish();

      events.onDataAttributeStart(attribute);
      events.onDataAttributeValueIntegerUnsigned2(0L, 1L);
      events.onDataAttributeValueIntegerUnsigned2(10L, 11L);
      events.onDataAttributeValueIntegerUnsigned2(20L, 21L);
      events.onDataAttributeFinish(attribute);
    }};

    final SMFParserRandomAccessType p = this.parserFor(events, out -> {
      out.putBytes(SMFFormatBinary.magicNumber());
      out.putU32(1L);
      out.putU32(0L);

      out.putU64(3L);
      out.putU64(0L);
      out.putU32(32L);
      out.putU32(0x7f7f7f7fL);
      out.putU32(1L);
      out.putU32(0x7f7f7f7fL);

      out.putStringPad(name, SMFAttributeNameType.MAXIMUM_CHARACTERS);
      out.putU32((long) SMFComponentType.ELEMENT_TYPE_INTEGER_UNSIGNED.toInteger());
      out.putU32(component_count);
      out.putU32(component_size);

      for (int vertex = 0; vertex < 3; ++vertex) {
        for (int component = 0; (long) component < component_count; ++component) {
          final long value = (long) ((vertex * 10) + component);
          LOG.debug(
            "vertex {} component {}: {}",
            Integer.valueOf(vertex),
            Integer.valueOf(component),
            Long.valueOf(value));
          out.putU64(value);
        }
      }
    });

    p.parseHeader();
    p.parseAttributeData(SMFAttributeName.of(name));
  }

  @Test
  public void testDataAttributesIntegerUnsigned64_1(
    final @Mocked SMFParserEventsType events)
  {
    final String name = "Unsigned64_1";
    final long component_count = 1L;
    final long component_size = 64L;

    new StrictExpectations()
    {{
      events.onStart();
      events.onVersionReceived(SMFFormatVersion.of(1, 0));
      events.onHeaderStart();
      events.onHeaderAttributeCountReceived(1L);

      final SMFAttribute attribute = SMFAttribute.of(
        SMFAttributeName.of(name),
        SMFComponentType.ELEMENT_TYPE_INTEGER_UNSIGNED,
        (int) component_count,
        (int) component_size);
      events.onHeaderAttributeReceived(attribute);

      events.onHeaderVerticesCountReceived(3L);
      events.onHeaderTrianglesCountReceived(0L);
      events.onHeaderTrianglesIndexSizeReceived(32L);
      events.onHeaderFinish();

      events.onDataAttributeStart(attribute);
      events.onDataAttributeValueIntegerUnsigned1(0L);
      events.onDataAttributeValueIntegerUnsigned1(10L);
      events.onDataAttributeValueIntegerUnsigned1(20L);
      events.onDataAttributeFinish(attribute);
    }};

    final SMFParserRandomAccessType p = this.parserFor(events, out -> {
      out.putBytes(SMFFormatBinary.magicNumber());
      out.putU32(1L);
      out.putU32(0L);

      out.putU64(3L);
      out.putU64(0L);
      out.putU32(32L);
      out.putU32(0x7f7f7f7fL);
      out.putU32(1L);
      out.putU32(0x7f7f7f7fL);

      out.putStringPad(name, SMFAttributeNameType.MAXIMUM_CHARACTERS);
      out.putU32((long) SMFComponentType.ELEMENT_TYPE_INTEGER_UNSIGNED.toInteger());
      out.putU32(component_count);
      out.putU32(component_size);

      for (int vertex = 0; vertex < 3; ++vertex) {
        for (int component = 0; (long) component < component_count; ++component) {
          final long value = (long) ((vertex * 10) + component);
          LOG.debug(
            "vertex {} component {}: {}",
            Integer.valueOf(vertex),
            Integer.valueOf(component),
            Long.valueOf(value));
          out.putU64(value);
        }
      }
    });

    p.parseHeader();
    p.parseAttributeData(SMFAttributeName.of(name));
  }

  @Test
  public void testDataAttributesIntegerUnsigned32_4(
    final @Mocked SMFParserEventsType events)
  {
    final String name = "Unsigned32_4";
    final long component_count = 4L;
    final long component_size = 32L;

    new StrictExpectations()
    {{
      events.onStart();
      events.onVersionReceived(SMFFormatVersion.of(1, 0));
      events.onHeaderStart();
      events.onHeaderAttributeCountReceived(1L);

      final SMFAttribute attribute = SMFAttribute.of(
        SMFAttributeName.of(name),
        SMFComponentType.ELEMENT_TYPE_INTEGER_UNSIGNED,
        (int) component_count,
        (int) component_size);
      events.onHeaderAttributeReceived(attribute);

      events.onHeaderVerticesCountReceived(3L);
      events.onHeaderTrianglesCountReceived(0L);
      events.onHeaderTrianglesIndexSizeReceived(32L);
      events.onHeaderFinish();

      events.onDataAttributeStart(attribute);
      events.onDataAttributeValueIntegerUnsigned4(0L, 1L, 2L, 3L);
      events.onDataAttributeValueIntegerUnsigned4(10L, 11L, 12L, 13L);
      events.onDataAttributeValueIntegerUnsigned4(20L, 21L, 22L, 23L);
      events.onDataAttributeFinish(attribute);
    }};

    final SMFParserRandomAccessType p = this.parserFor(events, out -> {
      out.putBytes(SMFFormatBinary.magicNumber());
      out.putU32(1L);
      out.putU32(0L);

      out.putU64(3L);
      out.putU64(0L);
      out.putU32(32L);
      out.putU32(0x7f7f7f7fL);
      out.putU32(1L);
      out.putU32(0x7f7f7f7fL);

      out.putStringPad(name, SMFAttributeNameType.MAXIMUM_CHARACTERS);
      out.putU32((long) SMFComponentType.ELEMENT_TYPE_INTEGER_UNSIGNED.toInteger());
      out.putU32(component_count);
      out.putU32(component_size);

      for (int vertex = 0; vertex < 3; ++vertex) {
        for (int component = 0; (long) component < component_count; ++component) {
          final long value = (long) ((vertex * 10) + component);
          LOG.debug(
            "vertex {} component {}: {}",
            Integer.valueOf(vertex),
            Integer.valueOf(component),
            Long.valueOf(value));
          out.putU32(value);
        }
      }
    });

    p.parseHeader();
    p.parseAttributeData(SMFAttributeName.of(name));
  }

  @Test
  public void testDataAttributesIntegerUnsigned32_3(
    final @Mocked SMFParserEventsType events)
  {
    final String name = "Unsigned32_3";
    final long component_count = 3L;
    final long component_size = 32L;

    new StrictExpectations()
    {{
      events.onStart();
      events.onVersionReceived(SMFFormatVersion.of(1, 0));
      events.onHeaderStart();
      events.onHeaderAttributeCountReceived(1L);

      final SMFAttribute attribute = SMFAttribute.of(
        SMFAttributeName.of(name),
        SMFComponentType.ELEMENT_TYPE_INTEGER_UNSIGNED,
        (int) component_count,
        (int) component_size);
      events.onHeaderAttributeReceived(attribute);

      events.onHeaderVerticesCountReceived(3L);
      events.onHeaderTrianglesCountReceived(0L);
      events.onHeaderTrianglesIndexSizeReceived(32L);
      events.onHeaderFinish();

      events.onDataAttributeStart(attribute);
      events.onDataAttributeValueIntegerUnsigned3(0L, 1L, 2L);
      events.onDataAttributeValueIntegerUnsigned3(10L, 11L, 12L);
      events.onDataAttributeValueIntegerUnsigned3(20L, 21L, 22L);
      events.onDataAttributeFinish(attribute);
    }};

    final SMFParserRandomAccessType p = this.parserFor(events, out -> {
      out.putBytes(SMFFormatBinary.magicNumber());
      out.putU32(1L);
      out.putU32(0L);

      out.putU64(3L);
      out.putU64(0L);
      out.putU32(32L);
      out.putU32(0x7f7f7f7fL);
      out.putU32(1L);
      out.putU32(0x7f7f7f7fL);

      out.putStringPad(name, SMFAttributeNameType.MAXIMUM_CHARACTERS);
      out.putU32((long) SMFComponentType.ELEMENT_TYPE_INTEGER_UNSIGNED.toInteger());
      out.putU32(component_count);
      out.putU32(component_size);

      for (int vertex = 0; vertex < 3; ++vertex) {
        for (int component = 0; (long) component < component_count; ++component) {
          final long value = (long) ((vertex * 10) + component);
          LOG.debug(
            "vertex {} component {}: {}",
            Integer.valueOf(vertex),
            Integer.valueOf(component),
            Long.valueOf(value));
          out.putU32(value);
        }
      }
    });

    p.parseHeader();
    p.parseAttributeData(SMFAttributeName.of(name));
  }

  @Test
  public void testDataAttributesIntegerUnsigned32_2(
    final @Mocked SMFParserEventsType events)
  {
    final String name = "Unsigned32_2";
    final long component_count = 2L;
    final long component_size = 32L;

    new StrictExpectations()
    {{
      events.onStart();
      events.onVersionReceived(SMFFormatVersion.of(1, 0));
      events.onHeaderStart();
      events.onHeaderAttributeCountReceived(1L);

      final SMFAttribute attribute = SMFAttribute.of(
        SMFAttributeName.of(name),
        SMFComponentType.ELEMENT_TYPE_INTEGER_UNSIGNED,
        (int) component_count,
        (int) component_size);
      events.onHeaderAttributeReceived(attribute);

      events.onHeaderVerticesCountReceived(3L);
      events.onHeaderTrianglesCountReceived(0L);
      events.onHeaderTrianglesIndexSizeReceived(32L);
      events.onHeaderFinish();

      events.onDataAttributeStart(attribute);
      events.onDataAttributeValueIntegerUnsigned2(0L, 1L);
      events.onDataAttributeValueIntegerUnsigned2(10L, 11L);
      events.onDataAttributeValueIntegerUnsigned2(20L, 21L);
      events.onDataAttributeFinish(attribute);
    }};

    final SMFParserRandomAccessType p = this.parserFor(events, out -> {
      out.putBytes(SMFFormatBinary.magicNumber());
      out.putU32(1L);
      out.putU32(0L);

      out.putU64(3L);
      out.putU64(0L);
      out.putU32(32L);
      out.putU32(0x7f7f7f7fL);
      out.putU32(1L);
      out.putU32(0x7f7f7f7fL);

      out.putStringPad(name, SMFAttributeNameType.MAXIMUM_CHARACTERS);
      out.putU32((long) SMFComponentType.ELEMENT_TYPE_INTEGER_UNSIGNED.toInteger());
      out.putU32(component_count);
      out.putU32(component_size);

      for (int vertex = 0; vertex < 3; ++vertex) {
        for (int component = 0; (long) component < component_count; ++component) {
          final long value = (long) ((vertex * 10) + component);
          LOG.debug(
            "vertex {} component {}: {}",
            Integer.valueOf(vertex),
            Integer.valueOf(component),
            Long.valueOf(value));
          out.putU32(value);
        }
      }
    });

    p.parseHeader();
    p.parseAttributeData(SMFAttributeName.of(name));
  }

  @Test
  public void testDataAttributesIntegerUnsigned32_1(
    final @Mocked SMFParserEventsType events)
  {
    final String name = "Unsigned32_1";
    final long component_count = 1L;
    final long component_size = 32L;

    new StrictExpectations()
    {{
      events.onStart();
      events.onVersionReceived(SMFFormatVersion.of(1, 0));
      events.onHeaderStart();
      events.onHeaderAttributeCountReceived(1L);

      final SMFAttribute attribute = SMFAttribute.of(
        SMFAttributeName.of(name),
        SMFComponentType.ELEMENT_TYPE_INTEGER_UNSIGNED,
        (int) component_count,
        (int) component_size);
      events.onHeaderAttributeReceived(attribute);

      events.onHeaderVerticesCountReceived(3L);
      events.onHeaderTrianglesCountReceived(0L);
      events.onHeaderTrianglesIndexSizeReceived(32L);
      events.onHeaderFinish();

      events.onDataAttributeStart(attribute);
      events.onDataAttributeValueIntegerUnsigned1(0L);
      events.onDataAttributeValueIntegerUnsigned1(10L);
      events.onDataAttributeValueIntegerUnsigned1(20L);
      events.onDataAttributeFinish(attribute);
    }};

    final SMFParserRandomAccessType p = this.parserFor(events, out -> {
      out.putBytes(SMFFormatBinary.magicNumber());
      out.putU32(1L);
      out.putU32(0L);

      out.putU64(3L);
      out.putU64(0L);
      out.putU32(32L);
      out.putU32(0x7f7f7f7fL);
      out.putU32(1L);
      out.putU32(0x7f7f7f7fL);

      out.putStringPad(name, SMFAttributeNameType.MAXIMUM_CHARACTERS);
      out.putU32((long) SMFComponentType.ELEMENT_TYPE_INTEGER_UNSIGNED.toInteger());
      out.putU32(component_count);
      out.putU32(component_size);

      for (int vertex = 0; vertex < 3; ++vertex) {
        for (int component = 0; (long) component < component_count; ++component) {
          final long value = (long) ((vertex * 10) + component);
          LOG.debug(
            "vertex {} component {}: {}",
            Integer.valueOf(vertex),
            Integer.valueOf(component),
            Long.valueOf(value));
          out.putU32(value);
        }
      }
    });

    p.parseHeader();
    p.parseAttributeData(SMFAttributeName.of(name));
  }

  @Test
  public void testDataAttributesIntegerUnsigned16_4(
    final @Mocked SMFParserEventsType events)
  {
    final String name = "Unsigned16_4";
    final long component_count = 4L;
    final long component_size = 16L;

    new StrictExpectations()
    {{
      events.onStart();
      events.onVersionReceived(SMFFormatVersion.of(1, 0));
      events.onHeaderStart();
      events.onHeaderAttributeCountReceived(1L);

      final SMFAttribute attribute = SMFAttribute.of(
        SMFAttributeName.of(name),
        SMFComponentType.ELEMENT_TYPE_INTEGER_UNSIGNED,
        (int) component_count,
        (int) component_size);
      events.onHeaderAttributeReceived(attribute);

      events.onHeaderVerticesCountReceived(3L);
      events.onHeaderTrianglesCountReceived(0L);
      events.onHeaderTrianglesIndexSizeReceived(32L);
      events.onHeaderFinish();

      events.onDataAttributeStart(attribute);
      events.onDataAttributeValueIntegerUnsigned4(0L, 1L, 2L, 3L);
      events.onDataAttributeValueIntegerUnsigned4(10L, 11L, 12L, 13L);
      events.onDataAttributeValueIntegerUnsigned4(20L, 21L, 22L, 23L);
      events.onDataAttributeFinish(attribute);
    }};

    final SMFParserRandomAccessType p = this.parserFor(events, out -> {
      out.putBytes(SMFFormatBinary.magicNumber());
      out.putU32(1L);
      out.putU32(0L);

      out.putU64(3L);
      out.putU64(0L);
      out.putU32(32L);
      out.putU32(0x7f7f7f7fL);
      out.putU32(1L);
      out.putU32(0x7f7f7f7fL);

      out.putStringPad(name, SMFAttributeNameType.MAXIMUM_CHARACTERS);
      out.putU32((long) SMFComponentType.ELEMENT_TYPE_INTEGER_UNSIGNED.toInteger());
      out.putU32(component_count);
      out.putU32(component_size);

      for (int vertex = 0; vertex < 3; ++vertex) {
        for (int component = 0; (long) component < component_count; ++component) {
          final long value = (long) ((vertex * 10) + component);
          LOG.debug(
            "vertex {} component {}: {}",
            Integer.valueOf(vertex),
            Integer.valueOf(component),
            Long.valueOf(value));
          out.putU16(value);
        }
      }
    });

    p.parseHeader();
    p.parseAttributeData(SMFAttributeName.of(name));
  }

  @Test
  public void testDataAttributesIntegerUnsigned16_3(
    final @Mocked SMFParserEventsType events)
  {
    final String name = "Unsigned16_3";
    final long component_count = 3L;
    final long component_size = 16L;

    new StrictExpectations()
    {{
      events.onStart();
      events.onVersionReceived(SMFFormatVersion.of(1, 0));
      events.onHeaderStart();
      events.onHeaderAttributeCountReceived(1L);

      final SMFAttribute attribute = SMFAttribute.of(
        SMFAttributeName.of(name),
        SMFComponentType.ELEMENT_TYPE_INTEGER_UNSIGNED,
        (int) component_count,
        (int) component_size);
      events.onHeaderAttributeReceived(attribute);

      events.onHeaderVerticesCountReceived(3L);
      events.onHeaderTrianglesCountReceived(0L);
      events.onHeaderTrianglesIndexSizeReceived(32L);
      events.onHeaderFinish();

      events.onDataAttributeStart(attribute);
      events.onDataAttributeValueIntegerUnsigned3(0L, 1L, 2L);
      events.onDataAttributeValueIntegerUnsigned3(10L, 11L, 12L);
      events.onDataAttributeValueIntegerUnsigned3(20L, 21L, 22L);
      events.onDataAttributeFinish(attribute);
    }};

    final SMFParserRandomAccessType p = this.parserFor(events, out -> {
      out.putBytes(SMFFormatBinary.magicNumber());
      out.putU32(1L);
      out.putU32(0L);

      out.putU64(3L);
      out.putU64(0L);
      out.putU32(32L);
      out.putU32(0x7f7f7f7fL);
      out.putU32(1L);
      out.putU32(0x7f7f7f7fL);

      out.putStringPad(name, SMFAttributeNameType.MAXIMUM_CHARACTERS);
      out.putU32((long) SMFComponentType.ELEMENT_TYPE_INTEGER_UNSIGNED.toInteger());
      out.putU32(component_count);
      out.putU32(component_size);

      for (int vertex = 0; vertex < 3; ++vertex) {
        for (int component = 0; (long) component < component_count; ++component) {
          final long value = (long) ((vertex * 10) + component);
          LOG.debug(
            "vertex {} component {}: {}",
            Integer.valueOf(vertex),
            Integer.valueOf(component),
            Long.valueOf(value));
          out.putU16(value);
        }
      }
    });

    p.parseHeader();
    p.parseAttributeData(SMFAttributeName.of(name));
  }

  @Test
  public void testDataAttributesIntegerUnsigned16_2(
    final @Mocked SMFParserEventsType events)
  {
    final String name = "Unsigned16_2";
    final long component_count = 2L;
    final long component_size = 16L;

    new StrictExpectations()
    {{
      events.onStart();
      events.onVersionReceived(SMFFormatVersion.of(1, 0));
      events.onHeaderStart();
      events.onHeaderAttributeCountReceived(1L);

      final SMFAttribute attribute = SMFAttribute.of(
        SMFAttributeName.of(name),
        SMFComponentType.ELEMENT_TYPE_INTEGER_UNSIGNED,
        (int) component_count,
        (int) component_size);
      events.onHeaderAttributeReceived(attribute);

      events.onHeaderVerticesCountReceived(3L);
      events.onHeaderTrianglesCountReceived(0L);
      events.onHeaderTrianglesIndexSizeReceived(32L);
      events.onHeaderFinish();

      events.onDataAttributeStart(attribute);
      events.onDataAttributeValueIntegerUnsigned2(0L, 1L);
      events.onDataAttributeValueIntegerUnsigned2(10L, 11L);
      events.onDataAttributeValueIntegerUnsigned2(20L, 21L);
      events.onDataAttributeFinish(attribute);
    }};

    final SMFParserRandomAccessType p = this.parserFor(events, out -> {
      out.putBytes(SMFFormatBinary.magicNumber());
      out.putU32(1L);
      out.putU32(0L);

      out.putU64(3L);
      out.putU64(0L);
      out.putU32(32L);
      out.putU32(0x7f7f7f7fL);
      out.putU32(1L);
      out.putU32(0x7f7f7f7fL);

      out.putStringPad(name, SMFAttributeNameType.MAXIMUM_CHARACTERS);
      out.putU32((long) SMFComponentType.ELEMENT_TYPE_INTEGER_UNSIGNED.toInteger());
      out.putU32(component_count);
      out.putU32(component_size);

      for (int vertex = 0; vertex < 3; ++vertex) {
        for (int component = 0; (long) component < component_count; ++component) {
          final long value = (long) ((vertex * 10) + component);
          LOG.debug(
            "vertex {} component {}: {}",
            Integer.valueOf(vertex),
            Integer.valueOf(component),
            Long.valueOf(value));
          out.putU16(value);
        }
      }
    });

    p.parseHeader();
    p.parseAttributeData(SMFAttributeName.of(name));
  }

  @Test
  public void testDataAttributesIntegerUnsigned16_1(
    final @Mocked SMFParserEventsType events)
  {
    final String name = "Unsigned16_1";
    final long component_count = 1L;
    final long component_size = 16L;

    new StrictExpectations()
    {{
      events.onStart();
      events.onVersionReceived(SMFFormatVersion.of(1, 0));
      events.onHeaderStart();
      events.onHeaderAttributeCountReceived(1L);

      final SMFAttribute attribute = SMFAttribute.of(
        SMFAttributeName.of(name),
        SMFComponentType.ELEMENT_TYPE_INTEGER_UNSIGNED,
        (int) component_count,
        (int) component_size);
      events.onHeaderAttributeReceived(attribute);

      events.onHeaderVerticesCountReceived(3L);
      events.onHeaderTrianglesCountReceived(0L);
      events.onHeaderTrianglesIndexSizeReceived(32L);
      events.onHeaderFinish();

      events.onDataAttributeStart(attribute);
      events.onDataAttributeValueIntegerUnsigned1(0L);
      events.onDataAttributeValueIntegerUnsigned1(10L);
      events.onDataAttributeValueIntegerUnsigned1(20L);
      events.onDataAttributeFinish(attribute);
    }};

    final SMFParserRandomAccessType p = this.parserFor(events, out -> {
      out.putBytes(SMFFormatBinary.magicNumber());
      out.putU32(1L);
      out.putU32(0L);

      out.putU64(3L);
      out.putU64(0L);
      out.putU32(32L);
      out.putU32(0x7f7f7f7fL);
      out.putU32(1L);
      out.putU32(0x7f7f7f7fL);

      out.putStringPad(name, SMFAttributeNameType.MAXIMUM_CHARACTERS);
      out.putU32((long) SMFComponentType.ELEMENT_TYPE_INTEGER_UNSIGNED.toInteger());
      out.putU32(component_count);
      out.putU32(component_size);

      for (int vertex = 0; vertex < 3; ++vertex) {
        for (int component = 0; (long) component < component_count; ++component) {
          final long value = (long) ((vertex * 10) + component);
          LOG.debug(
            "vertex {} component {}: {}",
            Integer.valueOf(vertex),
            Integer.valueOf(component),
            Long.valueOf(value));
          out.putU16(value);
        }
      }
    });

    p.parseHeader();
    p.parseAttributeData(SMFAttributeName.of(name));
  }


  @Test
  public void testDataAttributesIntegerUnsigned8_4(
    final @Mocked SMFParserEventsType events)
  {
    final String name = "Unsigned8_4";
    final long component_count = 4L;
    final long component_size = 8L;

    new StrictExpectations()
    {{
      events.onStart();
      events.onVersionReceived(SMFFormatVersion.of(1, 0));
      events.onHeaderStart();
      events.onHeaderAttributeCountReceived(1L);

      final SMFAttribute attribute = SMFAttribute.of(
        SMFAttributeName.of(name),
        SMFComponentType.ELEMENT_TYPE_INTEGER_UNSIGNED,
        (int) component_count,
        (int) component_size);
      events.onHeaderAttributeReceived(attribute);

      events.onHeaderVerticesCountReceived(3L);
      events.onHeaderTrianglesCountReceived(0L);
      events.onHeaderTrianglesIndexSizeReceived(32L);
      events.onHeaderFinish();

      events.onDataAttributeStart(attribute);
      events.onDataAttributeValueIntegerUnsigned4(0L, 1L, 2L, 3L);
      events.onDataAttributeValueIntegerUnsigned4(10L, 11L, 12L, 13L);
      events.onDataAttributeValueIntegerUnsigned4(20L, 21L, 22L, 23L);
      events.onDataAttributeFinish(attribute);
    }};

    final SMFParserRandomAccessType p = this.parserFor(events, out -> {
      out.putBytes(SMFFormatBinary.magicNumber());
      out.putU32(1L);
      out.putU32(0L);

      out.putU64(3L);
      out.putU64(0L);
      out.putU32(32L);
      out.putU32(0x7f7f7f7fL);
      out.putU32(1L);
      out.putU32(0x7f7f7f7fL);

      out.putStringPad(name, SMFAttributeNameType.MAXIMUM_CHARACTERS);
      out.putU32((long) SMFComponentType.ELEMENT_TYPE_INTEGER_UNSIGNED.toInteger());
      out.putU32(component_count);
      out.putU32(component_size);

      for (int vertex = 0; vertex < 3; ++vertex) {
        for (int component = 0; (long) component < component_count; ++component) {
          final long value = (long) ((vertex * 10) + component);
          LOG.debug(
            "vertex {} component {}: {}",
            Integer.valueOf(vertex),
            Integer.valueOf(component),
            Long.valueOf(value));
          out.putU8(value);
        }
      }
    });

    p.parseHeader();
    p.parseAttributeData(SMFAttributeName.of(name));
  }

  @Test
  public void testDataAttributesIntegerUnsigned8_3(
    final @Mocked SMFParserEventsType events)
  {
    final String name = "Unsigned8_3";
    final long component_count = 3L;
    final long component_size = 8L;

    new StrictExpectations()
    {{
      events.onStart();
      events.onVersionReceived(SMFFormatVersion.of(1, 0));
      events.onHeaderStart();
      events.onHeaderAttributeCountReceived(1L);

      final SMFAttribute attribute = SMFAttribute.of(
        SMFAttributeName.of(name),
        SMFComponentType.ELEMENT_TYPE_INTEGER_UNSIGNED,
        (int) component_count,
        (int) component_size);
      events.onHeaderAttributeReceived(attribute);

      events.onHeaderVerticesCountReceived(3L);
      events.onHeaderTrianglesCountReceived(0L);
      events.onHeaderTrianglesIndexSizeReceived(32L);
      events.onHeaderFinish();

      events.onDataAttributeStart(attribute);
      events.onDataAttributeValueIntegerUnsigned3(0L, 1L, 2L);
      events.onDataAttributeValueIntegerUnsigned3(10L, 11L, 12L);
      events.onDataAttributeValueIntegerUnsigned3(20L, 21L, 22L);
      events.onDataAttributeFinish(attribute);
    }};

    final SMFParserRandomAccessType p = this.parserFor(events, out -> {
      out.putBytes(SMFFormatBinary.magicNumber());
      out.putU32(1L);
      out.putU32(0L);

      out.putU64(3L);
      out.putU64(0L);
      out.putU32(32L);
      out.putU32(0x7f7f7f7fL);
      out.putU32(1L);
      out.putU32(0x7f7f7f7fL);

      out.putStringPad(name, SMFAttributeNameType.MAXIMUM_CHARACTERS);
      out.putU32((long) SMFComponentType.ELEMENT_TYPE_INTEGER_UNSIGNED.toInteger());
      out.putU32(component_count);
      out.putU32(component_size);

      for (int vertex = 0; vertex < 3; ++vertex) {
        for (int component = 0; (long) component < component_count; ++component) {
          final long value = (long) ((vertex * 10) + component);
          LOG.debug(
            "vertex {} component {}: {}",
            Integer.valueOf(vertex),
            Integer.valueOf(component),
            Long.valueOf(value));
          out.putU8(value);
        }
      }
    });

    p.parseHeader();
    p.parseAttributeData(SMFAttributeName.of(name));
  }

  @Test
  public void testDataAttributesIntegerUnsigned8_2(
    final @Mocked SMFParserEventsType events)
  {
    final String name = "Unsigned8_2";
    final long component_count = 2L;
    final long component_size = 8L;

    new StrictExpectations()
    {{
      events.onStart();
      events.onVersionReceived(SMFFormatVersion.of(1, 0));
      events.onHeaderStart();
      events.onHeaderAttributeCountReceived(1L);

      final SMFAttribute attribute = SMFAttribute.of(
        SMFAttributeName.of(name),
        SMFComponentType.ELEMENT_TYPE_INTEGER_UNSIGNED,
        (int) component_count,
        (int) component_size);
      events.onHeaderAttributeReceived(attribute);

      events.onHeaderVerticesCountReceived(3L);
      events.onHeaderTrianglesCountReceived(0L);
      events.onHeaderTrianglesIndexSizeReceived(32L);
      events.onHeaderFinish();

      events.onDataAttributeStart(attribute);
      events.onDataAttributeValueIntegerUnsigned2(0L, 1L);
      events.onDataAttributeValueIntegerUnsigned2(10L, 11L);
      events.onDataAttributeValueIntegerUnsigned2(20L, 21L);
      events.onDataAttributeFinish(attribute);
    }};

    final SMFParserRandomAccessType p = this.parserFor(events, out -> {
      out.putBytes(SMFFormatBinary.magicNumber());
      out.putU32(1L);
      out.putU32(0L);

      out.putU64(3L);
      out.putU64(0L);
      out.putU32(32L);
      out.putU32(0x7f7f7f7fL);
      out.putU32(1L);
      out.putU32(0x7f7f7f7fL);

      out.putStringPad(name, SMFAttributeNameType.MAXIMUM_CHARACTERS);
      out.putU32((long) SMFComponentType.ELEMENT_TYPE_INTEGER_UNSIGNED.toInteger());
      out.putU32(component_count);
      out.putU32(component_size);

      for (int vertex = 0; vertex < 3; ++vertex) {
        for (int component = 0; (long) component < component_count; ++component) {
          final long value = (long) ((vertex * 10) + component);
          LOG.debug(
            "vertex {} component {}: {}",
            Integer.valueOf(vertex),
            Integer.valueOf(component),
            Long.valueOf(value));
          out.putU8(value);
        }
      }
    });

    p.parseHeader();
    p.parseAttributeData(SMFAttributeName.of(name));
  }

  @Test
  public void testDataAttributesIntegerUnsigned8_1(
    final @Mocked SMFParserEventsType events)
  {
    final String name = "Unsigned8_1";
    final long component_count = 1L;
    final long component_size = 8L;

    new StrictExpectations()
    {{
      events.onStart();
      events.onVersionReceived(SMFFormatVersion.of(1, 0));
      events.onHeaderStart();
      events.onHeaderAttributeCountReceived(1L);

      final SMFAttribute attribute = SMFAttribute.of(
        SMFAttributeName.of(name),
        SMFComponentType.ELEMENT_TYPE_INTEGER_UNSIGNED,
        (int) component_count,
        (int) component_size);
      events.onHeaderAttributeReceived(attribute);

      events.onHeaderVerticesCountReceived(3L);
      events.onHeaderTrianglesCountReceived(0L);
      events.onHeaderTrianglesIndexSizeReceived(32L);
      events.onHeaderFinish();

      events.onDataAttributeStart(attribute);
      events.onDataAttributeValueIntegerUnsigned1(0L);
      events.onDataAttributeValueIntegerUnsigned1(10L);
      events.onDataAttributeValueIntegerUnsigned1(20L);
      events.onDataAttributeFinish(attribute);
    }};

    final SMFParserRandomAccessType p = this.parserFor(events, out -> {
      out.putBytes(SMFFormatBinary.magicNumber());
      out.putU32(1L);
      out.putU32(0L);

      out.putU64(3L);
      out.putU64(0L);
      out.putU32(32L);
      out.putU32(0x7f7f7f7fL);
      out.putU32(1L);
      out.putU32(0x7f7f7f7fL);

      out.putStringPad(name, SMFAttributeNameType.MAXIMUM_CHARACTERS);
      out.putU32((long) SMFComponentType.ELEMENT_TYPE_INTEGER_UNSIGNED.toInteger());
      out.putU32(component_count);
      out.putU32(component_size);

      for (int vertex = 0; vertex < 3; ++vertex) {
        for (int component = 0; (long) component < component_count; ++component) {
          final long value = (long) ((vertex * 10) + component);
          LOG.debug(
            "vertex {} component {}: {}",
            Integer.valueOf(vertex),
            Integer.valueOf(component),
            Long.valueOf(value));
          out.putU8(value);
        }
      }
    });

    p.parseHeader();
    p.parseAttributeData(SMFAttributeName.of(name));
  }
}
