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

public final class SMFFormatBinaryFloatingTest extends SMFBinaryTest
{
  private static final Logger LOG;

  static {
    LOG = LoggerFactory.getLogger(SMFFormatBinaryFloatingTest.class);
  }

  @Test
  public void testDataAttributesFloating64_4(
    final @Mocked SMFParserEventsType events)
  {
    final String name = "F64_4";
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
        SMFComponentType.ELEMENT_TYPE_FLOATING,
        (int) component_count,
        (int) component_size);
      events.onHeaderAttributeReceived(attribute);

      events.onHeaderVerticesCountReceived(3L);
      events.onHeaderTrianglesCountReceived(0L);
      events.onHeaderTrianglesIndexSizeReceived(32L);
      events.onHeaderFinish();

      events.onDataAttributeStart(attribute);
      events.onDataAttributeValueFloat4(0.0, 1.0, 2.0, 3.0);
      events.onDataAttributeValueFloat4(10.0, 11.0, 12.0, 13.0);
      events.onDataAttributeValueFloat4(20.0, 21.0, 22.0, 23.0);
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
      out.putU32((long) SMFComponentType.ELEMENT_TYPE_FLOATING.toInteger());
      out.putU32(component_count);
      out.putU32(component_size);

      for (int vertex = 0; vertex < 3; ++vertex) {
        for (int component = 0; (long) component < component_count; ++component) {
          final double value = (double) ((vertex * 10) + component);
          LOG.debug(
            "vertex {} component {}: {}",
            Integer.valueOf(vertex),
            Integer.valueOf(component),
            Double.valueOf(value));
          out.putF64(value);
        }
      }
    });

    p.parseHeader();
    p.parseAttributeData(SMFAttributeName.of(name));
  }

  @Test
  public void testDataAttributesFloating64_3(
    final @Mocked SMFParserEventsType events)
  {
    final String name = "F64_3";
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
        SMFComponentType.ELEMENT_TYPE_FLOATING,
        (int) component_count,
        (int) component_size);
      events.onHeaderAttributeReceived(attribute);

      events.onHeaderVerticesCountReceived(3L);
      events.onHeaderTrianglesCountReceived(0L);
      events.onHeaderTrianglesIndexSizeReceived(32L);
      events.onHeaderFinish();

      events.onDataAttributeStart(attribute);
      events.onDataAttributeValueFloat3(0.0, 1.0, 2.0);
      events.onDataAttributeValueFloat3(10.0, 11.0, 12.0);
      events.onDataAttributeValueFloat3(20.0, 21.0, 22.0);
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
      out.putU32((long) SMFComponentType.ELEMENT_TYPE_FLOATING.toInteger());
      out.putU32(component_count);
      out.putU32(component_size);

      for (int vertex = 0; vertex < 3; ++vertex) {
        for (int component = 0; (long) component < component_count; ++component) {
          final double value = (double) ((vertex * 10) + component);
          LOG.debug(
            "vertex {} component {}: {}",
            Integer.valueOf(vertex),
            Integer.valueOf(component),
            Double.valueOf(value));
          out.putF64(value);
        }
      }
    });

    p.parseHeader();
    p.parseAttributeData(SMFAttributeName.of(name));
  }

  @Test
  public void testDataAttributesFloating64_2(
    final @Mocked SMFParserEventsType events)
  {
    final String name = "F64_2";
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
        SMFComponentType.ELEMENT_TYPE_FLOATING,
        (int) component_count,
        (int) component_size);
      events.onHeaderAttributeReceived(attribute);

      events.onHeaderVerticesCountReceived(3L);
      events.onHeaderTrianglesCountReceived(0L);
      events.onHeaderTrianglesIndexSizeReceived(32L);
      events.onHeaderFinish();

      events.onDataAttributeStart(attribute);
      events.onDataAttributeValueFloat2(0.0, 1.0);
      events.onDataAttributeValueFloat2(10.0, 11.0);
      events.onDataAttributeValueFloat2(20.0, 21.0);
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
      out.putU32((long) SMFComponentType.ELEMENT_TYPE_FLOATING.toInteger());
      out.putU32(component_count);
      out.putU32(component_size);

      for (int vertex = 0; vertex < 3; ++vertex) {
        for (int component = 0; (long) component < component_count; ++component) {
          final double value = (double) ((vertex * 10) + component);
          LOG.debug(
            "vertex {} component {}: {}",
            Integer.valueOf(vertex),
            Integer.valueOf(component),
            Double.valueOf(value));
          out.putF64(value);
        }
      }
    });

    p.parseHeader();
    p.parseAttributeData(SMFAttributeName.of(name));
  }

  @Test
  public void testDataAttributesFloating64_1(
    final @Mocked SMFParserEventsType events)
  {
    final String name = "F64_1";
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
        SMFComponentType.ELEMENT_TYPE_FLOATING,
        (int) component_count,
        (int) component_size);
      events.onHeaderAttributeReceived(attribute);

      events.onHeaderVerticesCountReceived(3L);
      events.onHeaderTrianglesCountReceived(0L);
      events.onHeaderTrianglesIndexSizeReceived(32L);
      events.onHeaderFinish();

      events.onDataAttributeStart(attribute);
      events.onDataAttributeValueFloat1(0.0);
      events.onDataAttributeValueFloat1(10.0);
      events.onDataAttributeValueFloat1(20.0);
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
      out.putU32((long) SMFComponentType.ELEMENT_TYPE_FLOATING.toInteger());
      out.putU32(component_count);
      out.putU32(component_size);

      for (int vertex = 0; vertex < 3; ++vertex) {
        for (int component = 0; (long) component < component_count; ++component) {
          final double value = (double) ((vertex * 10) + component);
          LOG.debug(
            "vertex {} component {}: {}",
            Integer.valueOf(vertex),
            Integer.valueOf(component),
            Double.valueOf(value));
          out.putF64(value);
        }
      }
    });

    p.parseHeader();
    p.parseAttributeData(SMFAttributeName.of(name));
  }

  @Test
  public void testDataAttributesFloating32_4(
    final @Mocked SMFParserEventsType events)
  {
    final String name = "F32_4";
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
        SMFComponentType.ELEMENT_TYPE_FLOATING,
        (int) component_count,
        (int) component_size);
      events.onHeaderAttributeReceived(attribute);

      events.onHeaderVerticesCountReceived(3L);
      events.onHeaderTrianglesCountReceived(0L);
      events.onHeaderTrianglesIndexSizeReceived(32L);
      events.onHeaderFinish();

      events.onDataAttributeStart(attribute);
      events.onDataAttributeValueFloat4(0.0, 1.0, 2.0, 3.0);
      events.onDataAttributeValueFloat4(10.0, 11.0, 12.0, 13.0);
      events.onDataAttributeValueFloat4(20.0, 21.0, 22.0, 23.0);
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
      out.putU32((long) SMFComponentType.ELEMENT_TYPE_FLOATING.toInteger());
      out.putU32(component_count);
      out.putU32(component_size);

      for (int vertex = 0; vertex < 3; ++vertex) {
        for (int component = 0; (long) component < component_count; ++component) {
          final double value = (double) ((vertex * 10) + component);
          LOG.debug(
            "vertex {} component {}: {}",
            Integer.valueOf(vertex),
            Integer.valueOf(component),
            Double.valueOf(value));
          out.putF32(value);
        }
      }
    });

    p.parseHeader();
    p.parseAttributeData(SMFAttributeName.of(name));
  }

  @Test
  public void testDataAttributesFloating32_3(
    final @Mocked SMFParserEventsType events)
  {
    final String name = "F32_3";
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
        SMFComponentType.ELEMENT_TYPE_FLOATING,
        (int) component_count,
        (int) component_size);
      events.onHeaderAttributeReceived(attribute);

      events.onHeaderVerticesCountReceived(3L);
      events.onHeaderTrianglesCountReceived(0L);
      events.onHeaderTrianglesIndexSizeReceived(32L);
      events.onHeaderFinish();

      events.onDataAttributeStart(attribute);
      events.onDataAttributeValueFloat3(0.0, 1.0, 2.0);
      events.onDataAttributeValueFloat3(10.0, 11.0, 12.0);
      events.onDataAttributeValueFloat3(20.0, 21.0, 22.0);
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
      out.putU32((long) SMFComponentType.ELEMENT_TYPE_FLOATING.toInteger());
      out.putU32(component_count);
      out.putU32(component_size);

      for (int vertex = 0; vertex < 3; ++vertex) {
        for (int component = 0; (long) component < component_count; ++component) {
          final double value = (double) ((vertex * 10) + component);
          LOG.debug(
            "vertex {} component {}: {}",
            Integer.valueOf(vertex),
            Integer.valueOf(component),
            Double.valueOf(value));
          out.putF32(value);
        }
      }
    });

    p.parseHeader();
    p.parseAttributeData(SMFAttributeName.of(name));
  }

  @Test
  public void testDataAttributesFloating32_2(
    final @Mocked SMFParserEventsType events)
  {
    final String name = "F32_2";
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
        SMFComponentType.ELEMENT_TYPE_FLOATING,
        (int) component_count,
        (int) component_size);
      events.onHeaderAttributeReceived(attribute);

      events.onHeaderVerticesCountReceived(3L);
      events.onHeaderTrianglesCountReceived(0L);
      events.onHeaderTrianglesIndexSizeReceived(32L);
      events.onHeaderFinish();

      events.onDataAttributeStart(attribute);
      events.onDataAttributeValueFloat2(0.0, 1.0);
      events.onDataAttributeValueFloat2(10.0, 11.0);
      events.onDataAttributeValueFloat2(20.0, 21.0);
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
      out.putU32((long) SMFComponentType.ELEMENT_TYPE_FLOATING.toInteger());
      out.putU32(component_count);
      out.putU32(component_size);

      for (int vertex = 0; vertex < 3; ++vertex) {
        for (int component = 0; (long) component < component_count; ++component) {
          final double value = (double) ((vertex * 10) + component);
          LOG.debug(
            "vertex {} component {}: {}",
            Integer.valueOf(vertex),
            Integer.valueOf(component),
            Double.valueOf(value));
          out.putF32(value);
        }
      }
    });

    p.parseHeader();
    p.parseAttributeData(SMFAttributeName.of(name));
  }

  @Test
  public void testDataAttributesFloating32_1(
    final @Mocked SMFParserEventsType events)
  {
    final String name = "F32_1";
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
        SMFComponentType.ELEMENT_TYPE_FLOATING,
        (int) component_count,
        (int) component_size);
      events.onHeaderAttributeReceived(attribute);

      events.onHeaderVerticesCountReceived(3L);
      events.onHeaderTrianglesCountReceived(0L);
      events.onHeaderTrianglesIndexSizeReceived(32L);
      events.onHeaderFinish();

      events.onDataAttributeStart(attribute);
      events.onDataAttributeValueFloat1(0.0);
      events.onDataAttributeValueFloat1(10.0);
      events.onDataAttributeValueFloat1(20.0);
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
      out.putU32((long) SMFComponentType.ELEMENT_TYPE_FLOATING.toInteger());
      out.putU32(component_count);
      out.putU32(component_size);

      for (int vertex = 0; vertex < 3; ++vertex) {
        for (int component = 0; (long) component < component_count; ++component) {
          final double value = (double) ((vertex * 10) + component);
          LOG.debug(
            "vertex {} component {}: {}",
            Integer.valueOf(vertex),
            Integer.valueOf(component),
            Double.valueOf(value));
          out.putF32(value);
        }
      }
    });

    p.parseHeader();
    p.parseAttributeData(SMFAttributeName.of(name));
  }

  @Test
  public void testDataAttributesFloating16_4(
    final @Mocked SMFParserEventsType events)
  {
    final String name = "F16_4";
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
        SMFComponentType.ELEMENT_TYPE_FLOATING,
        (int) component_count,
        (int) component_size);
      events.onHeaderAttributeReceived(attribute);

      events.onHeaderVerticesCountReceived(3L);
      events.onHeaderTrianglesCountReceived(0L);
      events.onHeaderTrianglesIndexSizeReceived(32L);
      events.onHeaderFinish();

      events.onDataAttributeStart(attribute);
      events.onDataAttributeValueFloat4(0.0, 1.0, 2.0, 3.0);
      events.onDataAttributeValueFloat4(10.0, 11.0, 12.0, 13.0);
      events.onDataAttributeValueFloat4(20.0, 21.0, 22.0, 23.0);
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
      out.putU32((long) SMFComponentType.ELEMENT_TYPE_FLOATING.toInteger());
      out.putU32(component_count);
      out.putU32(component_size);

      for (int vertex = 0; vertex < 3; ++vertex) {
        for (int component = 0; (long) component < component_count; ++component) {
          final double value = (double) ((vertex * 10) + component);
          LOG.debug(
            "vertex {} component {}: {}",
            Integer.valueOf(vertex),
            Integer.valueOf(component),
            Double.valueOf(value));
          out.putF16(value);
        }
      }
    });

    p.parseHeader();
    p.parseAttributeData(SMFAttributeName.of(name));
  }

  @Test
  public void testDataAttributesFloating16_3(
    final @Mocked SMFParserEventsType events)
  {
    final String name = "F16_3";
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
        SMFComponentType.ELEMENT_TYPE_FLOATING,
        (int) component_count,
        (int) component_size);
      events.onHeaderAttributeReceived(attribute);

      events.onHeaderVerticesCountReceived(3L);
      events.onHeaderTrianglesCountReceived(0L);
      events.onHeaderTrianglesIndexSizeReceived(32L);
      events.onHeaderFinish();

      events.onDataAttributeStart(attribute);
      events.onDataAttributeValueFloat3(0.0, 1.0, 2.0);
      events.onDataAttributeValueFloat3(10.0, 11.0, 12.0);
      events.onDataAttributeValueFloat3(20.0, 21.0, 22.0);
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
      out.putU32((long) SMFComponentType.ELEMENT_TYPE_FLOATING.toInteger());
      out.putU32(component_count);
      out.putU32(component_size);

      for (int vertex = 0; vertex < 3; ++vertex) {
        for (int component = 0; (long) component < component_count; ++component) {
          final double value = (double) ((vertex * 10) + component);
          LOG.debug(
            "vertex {} component {}: {}",
            Integer.valueOf(vertex),
            Integer.valueOf(component),
            Double.valueOf(value));
          out.putF16(value);
        }
      }
    });

    p.parseHeader();
    p.parseAttributeData(SMFAttributeName.of(name));
  }

  @Test
  public void testDataAttributesFloating16_2(
    final @Mocked SMFParserEventsType events)
  {
    final String name = "F16_2";
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
        SMFComponentType.ELEMENT_TYPE_FLOATING,
        (int) component_count,
        (int) component_size);
      events.onHeaderAttributeReceived(attribute);

      events.onHeaderVerticesCountReceived(3L);
      events.onHeaderTrianglesCountReceived(0L);
      events.onHeaderTrianglesIndexSizeReceived(32L);
      events.onHeaderFinish();

      events.onDataAttributeStart(attribute);
      events.onDataAttributeValueFloat2(0.0, 1.0);
      events.onDataAttributeValueFloat2(10.0, 11.0);
      events.onDataAttributeValueFloat2(20.0, 21.0);
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
      out.putU32((long) SMFComponentType.ELEMENT_TYPE_FLOATING.toInteger());
      out.putU32(component_count);
      out.putU32(component_size);

      for (int vertex = 0; vertex < 3; ++vertex) {
        for (int component = 0; (long) component < component_count; ++component) {
          final double value = (double) ((vertex * 10) + component);
          LOG.debug(
            "vertex {} component {}: {}",
            Integer.valueOf(vertex),
            Integer.valueOf(component),
            Double.valueOf(value));
          out.putF16(value);
        }
      }
    });

    p.parseHeader();
    p.parseAttributeData(SMFAttributeName.of(name));
  }

  @Test
  public void testDataAttributesFloating16_1(
    final @Mocked SMFParserEventsType events)
  {
    final String name = "F16_1";
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
        SMFComponentType.ELEMENT_TYPE_FLOATING,
        (int) component_count,
        (int) component_size);
      events.onHeaderAttributeReceived(attribute);

      events.onHeaderVerticesCountReceived(3L);
      events.onHeaderTrianglesCountReceived(0L);
      events.onHeaderTrianglesIndexSizeReceived(32L);
      events.onHeaderFinish();

      events.onDataAttributeStart(attribute);
      events.onDataAttributeValueFloat1(0.0);
      events.onDataAttributeValueFloat1(10.0);
      events.onDataAttributeValueFloat1(20.0);
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
      out.putU32((long) SMFComponentType.ELEMENT_TYPE_FLOATING.toInteger());
      out.putU32(component_count);
      out.putU32(component_size);

      for (int vertex = 0; vertex < 3; ++vertex) {
        for (int component = 0; (long) component < component_count; ++component) {
          final double value = (double) ((vertex * 10) + component);
          LOG.debug(
            "vertex {} component {}: {}",
            Integer.valueOf(vertex),
            Integer.valueOf(component),
            Double.valueOf(value));
          out.putF16(value);
        }
      }
    });

    p.parseHeader();
    p.parseAttributeData(SMFAttributeName.of(name));
  }
}

