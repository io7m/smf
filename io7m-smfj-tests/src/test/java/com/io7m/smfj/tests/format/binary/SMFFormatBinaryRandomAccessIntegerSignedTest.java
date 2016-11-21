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

import com.io7m.junreachable.UnreachableCodeException;
import com.io7m.smfj.core.SMFAttribute;
import com.io7m.smfj.core.SMFAttributeName;
import com.io7m.smfj.core.SMFAttributeNameType;
import com.io7m.smfj.core.SMFComponentType;
import com.io7m.smfj.core.SMFFormatVersion;
import com.io7m.smfj.core.SMFHeader;
import com.io7m.smfj.core.SMFVendorSchemaIdentifier;
import com.io7m.smfj.format.binary.SMFFormatBinary;
import com.io7m.smfj.parser.api.SMFParserEventsType;
import com.io7m.smfj.parser.api.SMFParserRandomAccessType;
import com.io7m.smfj.serializer.api.SMFSerializerType;
import javaslang.Tuple;
import javaslang.collection.List;
import mockit.Mocked;
import mockit.StrictExpectations;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

public final class SMFFormatBinaryRandomAccessIntegerSignedTest extends SMFBinaryTest
{
  private static final Logger LOG;

  static {
    LOG = LoggerFactory.getLogger(SMFFormatBinaryRandomAccessIntegerSignedTest.class);
  }

  @Rule public final ExpectedException expected = ExpectedException.none();

  private static SMFHeader header(
    final SMFAttribute attr)
  {
    final SMFHeader.Builder hb = SMFHeader.builder();
    hb.setAttributesInOrder(List.of(attr));
    hb.setAttributesByName(List.of(attr).toMap(a -> Tuple.of(a.name(), a)));
    hb.setVertexCount(3L);
    hb.setTriangleIndexSizeBits(32L);
    hb.setTriangleCount(0L);
    hb.setSchemaIdentifier(
      SMFVendorSchemaIdentifier.of(0x696F376D, 0xA0B0C0D0, 1, 2));
    return hb.build();
  }
  
  @Test
  public void testDataAttributesIntegerSigned64_4(
    final @Mocked SMFParserEventsType events)
  {
    final String name = "Signed64_4";
    final long component_count = 4L;
    final long component_size = 64L;

    final SMFAttribute attr = SMFAttribute.of(
      SMFAttributeName.of(name),
      SMFComponentType.ELEMENT_TYPE_INTEGER_SIGNED,
      (int) component_count,
      (int) component_size);

    final SMFHeader h = header(attr);
    
    new StrictExpectations()
    {{
      events.onStart();
      events.onVersionReceived(SMFFormatVersion.of(1, 0));
      events.onHeaderParsed(h);

      events.onDataAttributeStart(attr);
      events.onDataAttributeValueIntegerSigned4(0L, 1L, 2L, 3L);
      events.onDataAttributeValueIntegerSigned4(10L, 11L, 12L, 13L);
      events.onDataAttributeValueIntegerSigned4(20L, 21L, 22L, 23L);
      events.onDataAttributeFinish(attr);
    }};

    final SMFParserRandomAccessType p = this.parserRandomFor(events, out -> {
      out.putBytes(SMFFormatBinary.magicNumber());
      out.putU32(1L);
      out.putU32(0L);

      out.putU32(0x696F376DL);
      out.putU32(0xA0B0C0D0L);
      out.putU32(1L);
      out.putU32(2L);

      out.putU64(3L);
      out.putU64(0L);
      out.putU32(32L);
      out.putU32(0x7f7f7f7fL);
      out.putU32(1L);
      out.putU32(0x7f7f7f7fL);

      out.putStringPadded(name, SMFAttributeNameType.MAXIMUM_CHARACTERS);
      out.putU32((long) SMFComponentType.ELEMENT_TYPE_INTEGER_SIGNED.toInteger());
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
          out.putS64(value);
        }
      }
    });

    p.parseHeader();
    p.parseAttributeData(SMFAttributeName.of(name));
  }

  @Test
  public void testDataAttributesIntegerSigned64_3(
    final @Mocked SMFParserEventsType events)
  {
    final String name = "Signed64_3";
    final long component_count = 3L;
    final long component_size = 64L;

    final SMFAttribute attr = SMFAttribute.of(
      SMFAttributeName.of(name),
      SMFComponentType.ELEMENT_TYPE_INTEGER_SIGNED,
      (int) component_count,
      (int) component_size);

    final SMFHeader h = header(attr);
    
    new StrictExpectations()
    {{
      events.onStart();
      events.onVersionReceived(SMFFormatVersion.of(1, 0));
      events.onHeaderParsed(h);

      events.onDataAttributeStart(attr);
      events.onDataAttributeValueIntegerSigned3(0L, 1L, 2L);
      events.onDataAttributeValueIntegerSigned3(10L, 11L, 12L);
      events.onDataAttributeValueIntegerSigned3(20L, 21L, 22L);
      events.onDataAttributeFinish(attr);
    }};

    final SMFParserRandomAccessType p = this.parserRandomFor(events, out -> {
      out.putBytes(SMFFormatBinary.magicNumber());
      out.putU32(1L);
      out.putU32(0L);

      out.putU32(0x696F376DL);
      out.putU32(0xA0B0C0D0L);
      out.putU32(1L);
      out.putU32(2L);

      out.putU64(3L);
      out.putU64(0L);
      out.putU32(32L);
      out.putU32(0x7f7f7f7fL);
      out.putU32(1L);
      out.putU32(0x7f7f7f7fL);

      out.putStringPadded(name, SMFAttributeNameType.MAXIMUM_CHARACTERS);
      out.putU32((long) SMFComponentType.ELEMENT_TYPE_INTEGER_SIGNED.toInteger());
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
          out.putS64(value);
        }
      }
    });

    p.parseHeader();
    p.parseAttributeData(SMFAttributeName.of(name));
  }

  @Test
  public void testDataAttributesIntegerSigned64_2(
    final @Mocked SMFParserEventsType events)
  {
    final String name = "Signed64_2";
    final long component_count = 2L;
    final long component_size = 64L;

    final SMFAttribute attr = SMFAttribute.of(
      SMFAttributeName.of(name),
      SMFComponentType.ELEMENT_TYPE_INTEGER_SIGNED,
      (int) component_count,
      (int) component_size);

    final SMFHeader h = header(attr);
    
    new StrictExpectations()
    {{
      events.onStart();
      events.onVersionReceived(SMFFormatVersion.of(1, 0));
      events.onHeaderParsed(h);

      events.onDataAttributeStart(attr);
      events.onDataAttributeValueIntegerSigned2(0L, 1L);
      events.onDataAttributeValueIntegerSigned2(10L, 11L);
      events.onDataAttributeValueIntegerSigned2(20L, 21L);
      events.onDataAttributeFinish(attr);
    }};

    final SMFParserRandomAccessType p = this.parserRandomFor(events, out -> {
      out.putBytes(SMFFormatBinary.magicNumber());
      out.putU32(1L);
      out.putU32(0L);

      out.putU32(0x696F376DL);
      out.putU32(0xA0B0C0D0L);
      out.putU32(1L);
      out.putU32(2L);

      out.putU64(3L);
      out.putU64(0L);
      out.putU32(32L);
      out.putU32(0x7f7f7f7fL);
      out.putU32(1L);
      out.putU32(0x7f7f7f7fL);

      out.putStringPadded(name, SMFAttributeNameType.MAXIMUM_CHARACTERS);
      out.putU32((long) SMFComponentType.ELEMENT_TYPE_INTEGER_SIGNED.toInteger());
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
          out.putS64(value);
        }
      }
    });

    p.parseHeader();
    p.parseAttributeData(SMFAttributeName.of(name));
  }

  @Test
  public void testDataAttributesIntegerSigned64_1(
    final @Mocked SMFParserEventsType events)
  {
    final String name = "Signed64_1";
    final long component_count = 1L;
    final long component_size = 64L;

    final SMFAttribute attr = SMFAttribute.of(
      SMFAttributeName.of(name),
      SMFComponentType.ELEMENT_TYPE_INTEGER_SIGNED,
      (int) component_count,
      (int) component_size);

    final SMFHeader h = header(attr);
    
    new StrictExpectations()
    {{
      events.onStart();
      events.onVersionReceived(SMFFormatVersion.of(1, 0));
      events.onHeaderParsed(h);

      events.onDataAttributeStart(attr);
      events.onDataAttributeValueIntegerSigned1(0L);
      events.onDataAttributeValueIntegerSigned1(10L);
      events.onDataAttributeValueIntegerSigned1(20L);
      events.onDataAttributeFinish(attr);
    }};

    final SMFParserRandomAccessType p = this.parserRandomFor(events, out -> {
      out.putBytes(SMFFormatBinary.magicNumber());
      out.putU32(1L);
      out.putU32(0L);

      out.putU32(0x696F376DL);
      out.putU32(0xA0B0C0D0L);
      out.putU32(1L);
      out.putU32(2L);

      out.putU64(3L);
      out.putU64(0L);
      out.putU32(32L);
      out.putU32(0x7f7f7f7fL);
      out.putU32(1L);
      out.putU32(0x7f7f7f7fL);

      out.putStringPadded(name, SMFAttributeNameType.MAXIMUM_CHARACTERS);
      out.putU32((long) SMFComponentType.ELEMENT_TYPE_INTEGER_SIGNED.toInteger());
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
          out.putS64(value);
        }
      }
    });

    p.parseHeader();
    p.parseAttributeData(SMFAttributeName.of(name));
  }

  @Test
  public void testDataAttributesIntegerSigned32_4(
    final @Mocked SMFParserEventsType events)
  {
    final String name = "Signed32_4";
    final long component_count = 4L;
    final long component_size = 32L;

    final SMFAttribute attr = SMFAttribute.of(
      SMFAttributeName.of(name),
      SMFComponentType.ELEMENT_TYPE_INTEGER_SIGNED,
      (int) component_count,
      (int) component_size);

    final SMFHeader h = header(attr);
    
    new StrictExpectations()
    {{
      events.onStart();
      events.onVersionReceived(SMFFormatVersion.of(1, 0));
      events.onHeaderParsed(h);

      events.onDataAttributeStart(attr);
      events.onDataAttributeValueIntegerSigned4(0L, 1L, 2L, 3L);
      events.onDataAttributeValueIntegerSigned4(10L, 11L, 12L, 13L);
      events.onDataAttributeValueIntegerSigned4(20L, 21L, 22L, 23L);
      events.onDataAttributeFinish(attr);
    }};

    final SMFParserRandomAccessType p = this.parserRandomFor(events, out -> {
      out.putBytes(SMFFormatBinary.magicNumber());
      out.putU32(1L);
      out.putU32(0L);

      out.putU32(0x696F376DL);
      out.putU32(0xA0B0C0D0L);
      out.putU32(1L);
      out.putU32(2L);

      out.putU64(3L);
      out.putU64(0L);
      out.putU32(32L);
      out.putU32(0x7f7f7f7fL);
      out.putU32(1L);
      out.putU32(0x7f7f7f7fL);

      out.putStringPadded(name, SMFAttributeNameType.MAXIMUM_CHARACTERS);
      out.putU32((long) SMFComponentType.ELEMENT_TYPE_INTEGER_SIGNED.toInteger());
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
          out.putS32(value);
        }
      }
    });

    p.parseHeader();
    p.parseAttributeData(SMFAttributeName.of(name));
  }

  @Test
  public void testDataAttributesIntegerSigned32_3(
    final @Mocked SMFParserEventsType events)
  {
    final String name = "Signed32_3";
    final long component_count = 3L;
    final long component_size = 32L;

    final SMFAttribute attr = SMFAttribute.of(
      SMFAttributeName.of(name),
      SMFComponentType.ELEMENT_TYPE_INTEGER_SIGNED,
      (int) component_count,
      (int) component_size);

    final SMFHeader h = header(attr);
    
    new StrictExpectations()
    {{
      events.onStart();
      events.onVersionReceived(SMFFormatVersion.of(1, 0));
      events.onHeaderParsed(h);

      events.onDataAttributeStart(attr);
      events.onDataAttributeValueIntegerSigned3(0L, 1L, 2L);
      events.onDataAttributeValueIntegerSigned3(10L, 11L, 12L);
      events.onDataAttributeValueIntegerSigned3(20L, 21L, 22L);
      events.onDataAttributeFinish(attr);
    }};

    final SMFParserRandomAccessType p = this.parserRandomFor(events, out -> {
      out.putBytes(SMFFormatBinary.magicNumber());
      out.putU32(1L);
      out.putU32(0L);

      out.putU32(0x696F376DL);
      out.putU32(0xA0B0C0D0L);
      out.putU32(1L);
      out.putU32(2L);

      out.putU64(3L);
      out.putU64(0L);
      out.putU32(32L);
      out.putU32(0x7f7f7f7fL);
      out.putU32(1L);
      out.putU32(0x7f7f7f7fL);

      out.putStringPadded(name, SMFAttributeNameType.MAXIMUM_CHARACTERS);
      out.putU32((long) SMFComponentType.ELEMENT_TYPE_INTEGER_SIGNED.toInteger());
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
          out.putS32(value);
        }
      }
    });

    p.parseHeader();
    p.parseAttributeData(SMFAttributeName.of(name));
  }

  @Test
  public void testDataAttributesIntegerSigned32_2(
    final @Mocked SMFParserEventsType events)
  {
    final String name = "Signed32_2";
    final long component_count = 2L;
    final long component_size = 32L;

    final SMFAttribute attr = SMFAttribute.of(
      SMFAttributeName.of(name),
      SMFComponentType.ELEMENT_TYPE_INTEGER_SIGNED,
      (int) component_count,
      (int) component_size);

    final SMFHeader h = header(attr);
    
    new StrictExpectations()
    {{
      events.onStart();
      events.onVersionReceived(SMFFormatVersion.of(1, 0));
      events.onHeaderParsed(h);

      events.onDataAttributeStart(attr);
      events.onDataAttributeValueIntegerSigned2(0L, 1L);
      events.onDataAttributeValueIntegerSigned2(10L, 11L);
      events.onDataAttributeValueIntegerSigned2(20L, 21L);
      events.onDataAttributeFinish(attr);
    }};

    final SMFParserRandomAccessType p = this.parserRandomFor(events, out -> {
      out.putBytes(SMFFormatBinary.magicNumber());
      out.putU32(1L);
      out.putU32(0L);

      out.putU32(0x696F376DL);
      out.putU32(0xA0B0C0D0L);
      out.putU32(1L);
      out.putU32(2L);

      out.putU64(3L);
      out.putU64(0L);
      out.putU32(32L);
      out.putU32(0x7f7f7f7fL);
      out.putU32(1L);
      out.putU32(0x7f7f7f7fL);

      out.putStringPadded(name, SMFAttributeNameType.MAXIMUM_CHARACTERS);
      out.putU32((long) SMFComponentType.ELEMENT_TYPE_INTEGER_SIGNED.toInteger());
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
          out.putS32(value);
        }
      }
    });

    p.parseHeader();
    p.parseAttributeData(SMFAttributeName.of(name));
  }

  @Test
  public void testDataAttributesIntegerSigned32_1(
    final @Mocked SMFParserEventsType events)
  {
    final String name = "Signed32_1";
    final long component_count = 1L;
    final long component_size = 32L;

    final SMFAttribute attr = SMFAttribute.of(
      SMFAttributeName.of(name),
      SMFComponentType.ELEMENT_TYPE_INTEGER_SIGNED,
      (int) component_count,
      (int) component_size);

    final SMFHeader h = header(attr);
    
    new StrictExpectations()
    {{
      events.onStart();
      events.onVersionReceived(SMFFormatVersion.of(1, 0));
      events.onHeaderParsed(h);

      events.onDataAttributeStart(attr);
      events.onDataAttributeValueIntegerSigned1(0L);
      events.onDataAttributeValueIntegerSigned1(10L);
      events.onDataAttributeValueIntegerSigned1(20L);
      events.onDataAttributeFinish(attr);
    }};

    final SMFParserRandomAccessType p = this.parserRandomFor(events, out -> {
      out.putBytes(SMFFormatBinary.magicNumber());
      out.putU32(1L);
      out.putU32(0L);

      out.putU32(0x696F376DL);
      out.putU32(0xA0B0C0D0L);
      out.putU32(1L);
      out.putU32(2L);

      out.putU64(3L);
      out.putU64(0L);
      out.putU32(32L);
      out.putU32(0x7f7f7f7fL);
      out.putU32(1L);
      out.putU32(0x7f7f7f7fL);

      out.putStringPadded(name, SMFAttributeNameType.MAXIMUM_CHARACTERS);
      out.putU32((long) SMFComponentType.ELEMENT_TYPE_INTEGER_SIGNED.toInteger());
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
          out.putS32(value);
        }
      }
    });

    p.parseHeader();
    p.parseAttributeData(SMFAttributeName.of(name));
  }

  @Test
  public void testDataAttributesIntegerSigned16_4(
    final @Mocked SMFParserEventsType events)
  {
    final String name = "Signed16_4";
    final long component_count = 4L;
    final long component_size = 16L;

    final SMFAttribute attr = SMFAttribute.of(
      SMFAttributeName.of(name),
      SMFComponentType.ELEMENT_TYPE_INTEGER_SIGNED,
      (int) component_count,
      (int) component_size);

    final SMFHeader h = header(attr);
    
    new StrictExpectations()
    {{
      events.onStart();
      events.onVersionReceived(SMFFormatVersion.of(1, 0));
      events.onHeaderParsed(h);

      events.onDataAttributeStart(attr);
      events.onDataAttributeValueIntegerSigned4(0L, 1L, 2L, 3L);
      events.onDataAttributeValueIntegerSigned4(10L, 11L, 12L, 13L);
      events.onDataAttributeValueIntegerSigned4(20L, 21L, 22L, 23L);
      events.onDataAttributeFinish(attr);
    }};

    final SMFParserRandomAccessType p = this.parserRandomFor(events, out -> {
      out.putBytes(SMFFormatBinary.magicNumber());
      out.putU32(1L);
      out.putU32(0L);

      out.putU32(0x696F376DL);
      out.putU32(0xA0B0C0D0L);
      out.putU32(1L);
      out.putU32(2L);

      out.putU64(3L);
      out.putU64(0L);
      out.putU32(32L);
      out.putU32(0x7f7f7f7fL);
      out.putU32(1L);
      out.putU32(0x7f7f7f7fL);

      out.putStringPadded(name, SMFAttributeNameType.MAXIMUM_CHARACTERS);
      out.putU32((long) SMFComponentType.ELEMENT_TYPE_INTEGER_SIGNED.toInteger());
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
          out.putS16(value);
        }
      }
    });

    p.parseHeader();
    p.parseAttributeData(SMFAttributeName.of(name));
  }

  @Test
  public void testDataAttributesIntegerSigned16_3(
    final @Mocked SMFParserEventsType events)
  {
    final String name = "Signed16_3";
    final long component_count = 3L;
    final long component_size = 16L;

    final SMFAttribute attr = SMFAttribute.of(
      SMFAttributeName.of(name),
      SMFComponentType.ELEMENT_TYPE_INTEGER_SIGNED,
      (int) component_count,
      (int) component_size);

    final SMFHeader h = header(attr);
    
    new StrictExpectations()
    {{
      events.onStart();
      events.onVersionReceived(SMFFormatVersion.of(1, 0));
      events.onHeaderParsed(h);

      events.onDataAttributeStart(attr);
      events.onDataAttributeValueIntegerSigned3(0L, 1L, 2L);
      events.onDataAttributeValueIntegerSigned3(10L, 11L, 12L);
      events.onDataAttributeValueIntegerSigned3(20L, 21L, 22L);
      events.onDataAttributeFinish(attr);
    }};

    final SMFParserRandomAccessType p = this.parserRandomFor(events, out -> {
      out.putBytes(SMFFormatBinary.magicNumber());
      out.putU32(1L);
      out.putU32(0L);

      out.putU32(0x696F376DL);
      out.putU32(0xA0B0C0D0L);
      out.putU32(1L);
      out.putU32(2L);

      out.putU64(3L);
      out.putU64(0L);
      out.putU32(32L);
      out.putU32(0x7f7f7f7fL);
      out.putU32(1L);
      out.putU32(0x7f7f7f7fL);

      out.putStringPadded(name, SMFAttributeNameType.MAXIMUM_CHARACTERS);
      out.putU32((long) SMFComponentType.ELEMENT_TYPE_INTEGER_SIGNED.toInteger());
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
          out.putS16(value);
        }
      }
    });

    p.parseHeader();
    p.parseAttributeData(SMFAttributeName.of(name));
  }

  @Test
  public void testDataAttributesIntegerSigned16_2(
    final @Mocked SMFParserEventsType events)
  {
    final String name = "Signed16_2";
    final long component_count = 2L;
    final long component_size = 16L;

    final SMFAttribute attr = SMFAttribute.of(
      SMFAttributeName.of(name),
      SMFComponentType.ELEMENT_TYPE_INTEGER_SIGNED,
      (int) component_count,
      (int) component_size);

    final SMFHeader h = header(attr);
    
    new StrictExpectations()
    {{
      events.onStart();
      events.onVersionReceived(SMFFormatVersion.of(1, 0));
      events.onHeaderParsed(h);

      events.onDataAttributeStart(attr);
      events.onDataAttributeValueIntegerSigned2(0L, 1L);
      events.onDataAttributeValueIntegerSigned2(10L, 11L);
      events.onDataAttributeValueIntegerSigned2(20L, 21L);
      events.onDataAttributeFinish(attr);
    }};

    final SMFParserRandomAccessType p = this.parserRandomFor(events, out -> {
      out.putBytes(SMFFormatBinary.magicNumber());
      out.putU32(1L);
      out.putU32(0L);

      out.putU32(0x696F376DL);
      out.putU32(0xA0B0C0D0L);
      out.putU32(1L);
      out.putU32(2L);

      out.putU64(3L);
      out.putU64(0L);
      out.putU32(32L);
      out.putU32(0x7f7f7f7fL);
      out.putU32(1L);
      out.putU32(0x7f7f7f7fL);

      out.putStringPadded(name, SMFAttributeNameType.MAXIMUM_CHARACTERS);
      out.putU32((long) SMFComponentType.ELEMENT_TYPE_INTEGER_SIGNED.toInteger());
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
          out.putS16(value);
        }
      }
    });

    p.parseHeader();
    p.parseAttributeData(SMFAttributeName.of(name));
  }

  @Test
  public void testDataAttributesIntegerSigned16_1(
    final @Mocked SMFParserEventsType events)
  {
    final String name = "Signed16_1";
    final long component_count = 1L;
    final long component_size = 16L;

    final SMFAttribute attr = SMFAttribute.of(
      SMFAttributeName.of(name),
      SMFComponentType.ELEMENT_TYPE_INTEGER_SIGNED,
      (int) component_count,
      (int) component_size);

    final SMFHeader h = header(attr);
    
    new StrictExpectations()
    {{
      events.onStart();
      events.onVersionReceived(SMFFormatVersion.of(1, 0));
      events.onHeaderParsed(h);

      events.onDataAttributeStart(attr);
      events.onDataAttributeValueIntegerSigned1(0L);
      events.onDataAttributeValueIntegerSigned1(10L);
      events.onDataAttributeValueIntegerSigned1(20L);
      events.onDataAttributeFinish(attr);
    }};

    final SMFParserRandomAccessType p = this.parserRandomFor(events, out -> {
      out.putBytes(SMFFormatBinary.magicNumber());
      out.putU32(1L);
      out.putU32(0L);

      out.putU32(0x696F376DL);
      out.putU32(0xA0B0C0D0L);
      out.putU32(1L);
      out.putU32(2L);

      out.putU64(3L);
      out.putU64(0L);
      out.putU32(32L);
      out.putU32(0x7f7f7f7fL);
      out.putU32(1L);
      out.putU32(0x7f7f7f7fL);

      out.putStringPadded(name, SMFAttributeNameType.MAXIMUM_CHARACTERS);
      out.putU32((long) SMFComponentType.ELEMENT_TYPE_INTEGER_SIGNED.toInteger());
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
          out.putS16(value);
        }
      }
    });

    p.parseHeader();
    p.parseAttributeData(SMFAttributeName.of(name));
  }

  @Test
  public void testDataAttributesIntegerSigned8_4(
    final @Mocked SMFParserEventsType events)
  {
    final String name = "Signed8_4";
    final long component_count = 4L;
    final long component_size = 8L;

    final SMFAttribute attr = SMFAttribute.of(
      SMFAttributeName.of(name),
      SMFComponentType.ELEMENT_TYPE_INTEGER_SIGNED,
      (int) component_count,
      (int) component_size);

    final SMFHeader h = header(attr);
    
    new StrictExpectations()
    {{
      events.onStart();
      events.onVersionReceived(SMFFormatVersion.of(1, 0));
      events.onHeaderParsed(h);

      events.onDataAttributeStart(attr);
      events.onDataAttributeValueIntegerSigned4(0L, 1L, 2L, 3L);
      events.onDataAttributeValueIntegerSigned4(10L, 11L, 12L, 13L);
      events.onDataAttributeValueIntegerSigned4(20L, 21L, 22L, 23L);
      events.onDataAttributeFinish(attr);
    }};

    final SMFParserRandomAccessType p = this.parserRandomFor(events, out -> {
      out.putBytes(SMFFormatBinary.magicNumber());
      out.putU32(1L);
      out.putU32(0L);

      out.putU32(0x696F376DL);
      out.putU32(0xA0B0C0D0L);
      out.putU32(1L);
      out.putU32(2L);

      out.putU64(3L);
      out.putU64(0L);
      out.putU32(32L);
      out.putU32(0x7f7f7f7fL);
      out.putU32(1L);
      out.putU32(0x7f7f7f7fL);

      out.putStringPadded(name, SMFAttributeNameType.MAXIMUM_CHARACTERS);
      out.putU32((long) SMFComponentType.ELEMENT_TYPE_INTEGER_SIGNED.toInteger());
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
          out.putS8(value);
        }
      }
    });

    p.parseHeader();
    p.parseAttributeData(SMFAttributeName.of(name));
  }

  @Test
  public void testDataAttributesIntegerSigned8_3(
    final @Mocked SMFParserEventsType events)
  {
    final String name = "Signed8_3";
    final long component_count = 3L;
    final long component_size = 8L;

    final SMFAttribute attr = SMFAttribute.of(
      SMFAttributeName.of(name),
      SMFComponentType.ELEMENT_TYPE_INTEGER_SIGNED,
      (int) component_count,
      (int) component_size);

    final SMFHeader h = header(attr);
    
    new StrictExpectations()
    {{
      events.onStart();
      events.onVersionReceived(SMFFormatVersion.of(1, 0));
      events.onHeaderParsed(h);

      events.onDataAttributeStart(attr);
      events.onDataAttributeValueIntegerSigned3(0L, 1L, 2L);
      events.onDataAttributeValueIntegerSigned3(10L, 11L, 12L);
      events.onDataAttributeValueIntegerSigned3(20L, 21L, 22L);
      events.onDataAttributeFinish(attr);
    }};

    final SMFParserRandomAccessType p = this.parserRandomFor(events, out -> {
      out.putBytes(SMFFormatBinary.magicNumber());
      out.putU32(1L);
      out.putU32(0L);

      out.putU32(0x696F376DL);
      out.putU32(0xA0B0C0D0L);
      out.putU32(1L);
      out.putU32(2L);

      out.putU64(3L);
      out.putU64(0L);
      out.putU32(32L);
      out.putU32(0x7f7f7f7fL);
      out.putU32(1L);
      out.putU32(0x7f7f7f7fL);

      out.putStringPadded(name, SMFAttributeNameType.MAXIMUM_CHARACTERS);
      out.putU32((long) SMFComponentType.ELEMENT_TYPE_INTEGER_SIGNED.toInteger());
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
          out.putS8(value);
        }
      }
    });

    p.parseHeader();
    p.parseAttributeData(SMFAttributeName.of(name));
  }

  @Test
  public void testDataAttributesIntegerSigned8_2(
    final @Mocked SMFParserEventsType events)
  {
    final String name = "Signed8_2";
    final long component_count = 2L;
    final long component_size = 8L;

    final SMFAttribute attr = SMFAttribute.of(
      SMFAttributeName.of(name),
      SMFComponentType.ELEMENT_TYPE_INTEGER_SIGNED,
      (int) component_count,
      (int) component_size);

    final SMFHeader h = header(attr);
    
    new StrictExpectations()
    {{
      events.onStart();
      events.onVersionReceived(SMFFormatVersion.of(1, 0));
      events.onHeaderParsed(h);

      events.onDataAttributeStart(attr);
      events.onDataAttributeValueIntegerSigned2(0L, 1L);
      events.onDataAttributeValueIntegerSigned2(10L, 11L);
      events.onDataAttributeValueIntegerSigned2(20L, 21L);
      events.onDataAttributeFinish(attr);
    }};

    final SMFParserRandomAccessType p = this.parserRandomFor(events, out -> {
      out.putBytes(SMFFormatBinary.magicNumber());
      out.putU32(1L);
      out.putU32(0L);

      out.putU32(0x696F376DL);
      out.putU32(0xA0B0C0D0L);
      out.putU32(1L);
      out.putU32(2L);

      out.putU64(3L);
      out.putU64(0L);
      out.putU32(32L);
      out.putU32(0x7f7f7f7fL);
      out.putU32(1L);
      out.putU32(0x7f7f7f7fL);

      out.putStringPadded(name, SMFAttributeNameType.MAXIMUM_CHARACTERS);
      out.putU32((long) SMFComponentType.ELEMENT_TYPE_INTEGER_SIGNED.toInteger());
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
          out.putS8(value);
        }
      }
    });

    p.parseHeader();
    p.parseAttributeData(SMFAttributeName.of(name));
  }

  @Test
  public void testDataAttributesIntegerSigned8_1(
    final @Mocked SMFParserEventsType events)
  {
    final String name = "Signed8_1";
    final long component_count = 1L;
    final long component_size = 8L;

    final SMFAttribute attr = SMFAttribute.of(
      SMFAttributeName.of(name),
      SMFComponentType.ELEMENT_TYPE_INTEGER_SIGNED,
      (int) component_count,
      (int) component_size);

    final SMFHeader h = header(attr);
    
    new StrictExpectations()
    {{
      events.onStart();
      events.onVersionReceived(SMFFormatVersion.of(1, 0));
      events.onHeaderParsed(h);

      events.onDataAttributeStart(attr);
      events.onDataAttributeValueIntegerSigned1(0L);
      events.onDataAttributeValueIntegerSigned1(10L);
      events.onDataAttributeValueIntegerSigned1(20L);
      events.onDataAttributeFinish(attr);
    }};

    final SMFParserRandomAccessType p = this.parserRandomFor(events, out -> {
      out.putBytes(SMFFormatBinary.magicNumber());
      out.putU32(1L);
      out.putU32(0L);

      out.putU32(0x696F376DL);
      out.putU32(0xA0B0C0D0L);
      out.putU32(1L);
      out.putU32(2L);

      out.putU64(3L);
      out.putU64(0L);
      out.putU32(32L);
      out.putU32(0x7f7f7f7fL);
      out.putU32(1L);
      out.putU32(0x7f7f7f7fL);

      out.putStringPadded(name, SMFAttributeNameType.MAXIMUM_CHARACTERS);
      out.putU32((long) SMFComponentType.ELEMENT_TYPE_INTEGER_SIGNED.toInteger());
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
          out.putS8(value);
        }
      }
    });

    p.parseHeader();
    p.parseAttributeData(SMFAttributeName.of(name));
  }

  @Test
  public void testSerializerAttributeTooFew()
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
        SMFComponentType.ELEMENT_TYPE_INTEGER_SIGNED,
        4,
        64),
      SMFAttribute.of(
        SMFAttributeName.of("y"),
        SMFComponentType.ELEMENT_TYPE_INTEGER_SIGNED,
        4,
        64));

    final SMFHeader.Builder header_b = SMFHeader.builder();
    header_b.setVertexCount(2L);
    header_b.setTriangleIndexSizeBits(16L);
    header_b.setTriangleCount(0L);
    header_b.setAttributesInOrder(attributes);
    header_b.setAttributesByName(attributes.toMap(a -> Tuple.of(a.name(), a)));
    header_b.setSchemaIdentifier(
      SMFVendorSchemaIdentifier.of(0x696F376D, 0xA0B0C0D0, 1, 2));
    final SMFHeader header = header_b.build();

    serializer.serializeHeader(header);
    serializer.serializeData(SMFAttributeName.of("x"));
    serializer.serializeValueIntegerSigned4(0L, 1L, 2L, 3L);

    this.expected.expect(IllegalStateException.class);
    serializer.serializeData(SMFAttributeName.of("y"));
  }

  @Test
  public void testSerializerAttributeWrongTypeF64_4()
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
        SMFComponentType.ELEMENT_TYPE_INTEGER_SIGNED,
        4,
        64));

    final SMFHeader.Builder header_b = SMFHeader.builder();
    header_b.setVertexCount(1L);
    header_b.setTriangleIndexSizeBits(16L);
    header_b.setTriangleCount(0L);
    header_b.setAttributesInOrder(attributes);
    header_b.setAttributesByName(attributes.toMap(a -> Tuple.of(a.name(), a)));
    header_b.setSchemaIdentifier(
      SMFVendorSchemaIdentifier.of(0x696F376D, 0xA0B0C0D0, 1, 2));
    final SMFHeader header = header_b.build();

    serializer.serializeHeader(header);
    serializer.serializeData(SMFAttributeName.of("x"));

    this.expected.expect(IllegalArgumentException.class);
    serializer.serializeValueIntegerSigned3(0L, 1L, 2L);
  }

  @Test
  public void testSerializerAttributeWrongTypeF64_3()
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
        SMFComponentType.ELEMENT_TYPE_INTEGER_SIGNED,
        3,
        64));

    final SMFHeader.Builder header_b = SMFHeader.builder();
    header_b.setVertexCount(1L);
    header_b.setTriangleIndexSizeBits(16L);
    header_b.setTriangleCount(0L);
    header_b.setAttributesInOrder(attributes);
    header_b.setAttributesByName(attributes.toMap(a -> Tuple.of(a.name(), a)));
    header_b.setSchemaIdentifier(
      SMFVendorSchemaIdentifier.of(0x696F376D, 0xA0B0C0D0, 1, 2));
    final SMFHeader header = header_b.build();

    serializer.serializeHeader(header);
    serializer.serializeData(SMFAttributeName.of("x"));

    this.expected.expect(IllegalArgumentException.class);
    serializer.serializeValueIntegerSigned4(0L, 1L, 2L, 3L);
  }

  @Test
  public void testSerializerAttributeWrongTypeF64_2()
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
        SMFComponentType.ELEMENT_TYPE_INTEGER_SIGNED,
        2,
        64));

    final SMFHeader.Builder header_b = SMFHeader.builder();
    header_b.setVertexCount(1L);
    header_b.setTriangleIndexSizeBits(16L);
    header_b.setTriangleCount(0L);
    header_b.setAttributesInOrder(attributes);
    header_b.setAttributesByName(attributes.toMap(a -> Tuple.of(a.name(), a)));
    header_b.setSchemaIdentifier(
      SMFVendorSchemaIdentifier.of(0x696F376D, 0xA0B0C0D0, 1, 2));
    final SMFHeader header = header_b.build();

    serializer.serializeHeader(header);
    serializer.serializeData(SMFAttributeName.of("x"));

    this.expected.expect(IllegalArgumentException.class);
    serializer.serializeValueIntegerSigned4(0L, 1L, 2L, 3L);
  }

  @Test
  public void testSerializerAttributeWrongTypeF64_1()
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
        SMFComponentType.ELEMENT_TYPE_INTEGER_SIGNED,
        1,
        64));

    final SMFHeader.Builder header_b = SMFHeader.builder();
    header_b.setVertexCount(1L);
    header_b.setTriangleIndexSizeBits(16L);
    header_b.setTriangleCount(0L);
    header_b.setAttributesInOrder(attributes);
    header_b.setAttributesByName(attributes.toMap(a -> Tuple.of(a.name(), a)));
    header_b.setSchemaIdentifier(
      SMFVendorSchemaIdentifier.of(0x696F376D, 0xA0B0C0D0, 1, 2));
    final SMFHeader header = header_b.build();

    serializer.serializeHeader(header);
    serializer.serializeData(SMFAttributeName.of("x"));

    this.expected.expect(IllegalArgumentException.class);
    serializer.serializeValueIntegerSigned4(0L, 1L, 2L, 3L);
  }

  @Test
  public void testSerializerAttributeWrongTypeF32_4()
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
        SMFComponentType.ELEMENT_TYPE_INTEGER_SIGNED,
        4,
        32));

    final SMFHeader.Builder header_b = SMFHeader.builder();
    header_b.setVertexCount(1L);
    header_b.setTriangleIndexSizeBits(16L);
    header_b.setTriangleCount(0L);
    header_b.setAttributesInOrder(attributes);
    header_b.setAttributesByName(attributes.toMap(a -> Tuple.of(a.name(), a)));
    header_b.setSchemaIdentifier(
      SMFVendorSchemaIdentifier.of(0x696F376D, 0xA0B0C0D0, 1, 2));
    final SMFHeader header = header_b.build();

    serializer.serializeHeader(header);
    serializer.serializeData(SMFAttributeName.of("x"));

    this.expected.expect(IllegalArgumentException.class);
    serializer.serializeValueIntegerSigned3(0L, 1L, 2L);
  }

  @Test
  public void testSerializerAttributeWrongTypeF32_3()
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
        SMFComponentType.ELEMENT_TYPE_INTEGER_SIGNED,
        3,
        32));

    final SMFHeader.Builder header_b = SMFHeader.builder();
    header_b.setVertexCount(1L);
    header_b.setTriangleIndexSizeBits(16L);
    header_b.setTriangleCount(0L);
    header_b.setAttributesInOrder(attributes);
    header_b.setAttributesByName(attributes.toMap(a -> Tuple.of(a.name(), a)));
    header_b.setSchemaIdentifier(
      SMFVendorSchemaIdentifier.of(0x696F376D, 0xA0B0C0D0, 1, 2));
    final SMFHeader header = header_b.build();

    serializer.serializeHeader(header);
    serializer.serializeData(SMFAttributeName.of("x"));

    this.expected.expect(IllegalArgumentException.class);
    serializer.serializeValueIntegerSigned4(0L, 1L, 2L, 3L);
  }

  @Test
  public void testSerializerAttributeWrongTypeF32_2()
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
        SMFComponentType.ELEMENT_TYPE_INTEGER_SIGNED,
        2,
        32));

    final SMFHeader.Builder header_b = SMFHeader.builder();
    header_b.setVertexCount(1L);
    header_b.setTriangleIndexSizeBits(16L);
    header_b.setTriangleCount(0L);
    header_b.setAttributesInOrder(attributes);
    header_b.setAttributesByName(attributes.toMap(a -> Tuple.of(a.name(), a)));
    header_b.setSchemaIdentifier(
      SMFVendorSchemaIdentifier.of(0x696F376D, 0xA0B0C0D0, 1, 2));
    final SMFHeader header = header_b.build();

    serializer.serializeHeader(header);
    serializer.serializeData(SMFAttributeName.of("x"));

    this.expected.expect(IllegalArgumentException.class);
    serializer.serializeValueIntegerSigned4(0L, 1L, 2L, 3L);
  }

  @Test
  public void testSerializerAttributeWrongTypeF32_1()
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
        SMFComponentType.ELEMENT_TYPE_INTEGER_SIGNED,
        1,
        32));

    final SMFHeader.Builder header_b = SMFHeader.builder();
    header_b.setVertexCount(1L);
    header_b.setTriangleIndexSizeBits(16L);
    header_b.setTriangleCount(0L);
    header_b.setAttributesInOrder(attributes);
    header_b.setAttributesByName(attributes.toMap(a -> Tuple.of(a.name(), a)));
    header_b.setSchemaIdentifier(
      SMFVendorSchemaIdentifier.of(0x696F376D, 0xA0B0C0D0, 1, 2));
    final SMFHeader header = header_b.build();

    serializer.serializeHeader(header);
    serializer.serializeData(SMFAttributeName.of("x"));

    this.expected.expect(IllegalArgumentException.class);
    serializer.serializeValueIntegerSigned4(0L, 1L, 2L, 3L);
  }


  @Test
  public void testSerializerAttributeWrongTypeF16_4()
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
        SMFComponentType.ELEMENT_TYPE_INTEGER_SIGNED,
        4,
        16));

    final SMFHeader.Builder header_b = SMFHeader.builder();
    header_b.setVertexCount(1L);
    header_b.setTriangleIndexSizeBits(16L);
    header_b.setTriangleCount(0L);
    header_b.setAttributesInOrder(attributes);
    header_b.setAttributesByName(attributes.toMap(a -> Tuple.of(a.name(), a)));
    header_b.setSchemaIdentifier(
      SMFVendorSchemaIdentifier.of(0x696F376D, 0xA0B0C0D0, 1, 2));
    final SMFHeader header = header_b.build();

    serializer.serializeHeader(header);
    serializer.serializeData(SMFAttributeName.of("x"));

    this.expected.expect(IllegalArgumentException.class);
    serializer.serializeValueIntegerSigned3(0L, 1L, 2L);
  }

  @Test
  public void testSerializerAttributeWrongTypeF16_3()
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
        SMFComponentType.ELEMENT_TYPE_INTEGER_SIGNED,
        3,
        16));

    final SMFHeader.Builder header_b = SMFHeader.builder();
    header_b.setVertexCount(1L);
    header_b.setTriangleIndexSizeBits(16L);
    header_b.setTriangleCount(0L);
    header_b.setAttributesInOrder(attributes);
    header_b.setAttributesByName(attributes.toMap(a -> Tuple.of(a.name(), a)));
    header_b.setSchemaIdentifier(
      SMFVendorSchemaIdentifier.of(0x696F376D, 0xA0B0C0D0, 1, 2));
    final SMFHeader header = header_b.build();

    serializer.serializeHeader(header);
    serializer.serializeData(SMFAttributeName.of("x"));

    this.expected.expect(IllegalArgumentException.class);
    serializer.serializeValueIntegerSigned4(0L, 1L, 2L, 3L);
  }

  @Test
  public void testSerializerAttributeWrongTypeF16_2()
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
        SMFComponentType.ELEMENT_TYPE_INTEGER_SIGNED,
        2,
        16));

    final SMFHeader.Builder header_b = SMFHeader.builder();
    header_b.setVertexCount(1L);
    header_b.setTriangleIndexSizeBits(16L);
    header_b.setTriangleCount(0L);
    header_b.setAttributesInOrder(attributes);
    header_b.setAttributesByName(attributes.toMap(a -> Tuple.of(a.name(), a)));
    header_b.setSchemaIdentifier(
      SMFVendorSchemaIdentifier.of(0x696F376D, 0xA0B0C0D0, 1, 2));
    final SMFHeader header = header_b.build();

    serializer.serializeHeader(header);
    serializer.serializeData(SMFAttributeName.of("x"));

    this.expected.expect(IllegalArgumentException.class);
    serializer.serializeValueIntegerSigned4(0L, 1L, 2L, 3L);
  }

  @Test
  public void testSerializerAttributeWrongTypeF16_1()
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
        SMFComponentType.ELEMENT_TYPE_INTEGER_SIGNED,
        1,
        16));

    final SMFHeader.Builder header_b = SMFHeader.builder();
    header_b.setVertexCount(1L);
    header_b.setTriangleIndexSizeBits(16L);
    header_b.setTriangleCount(0L);
    header_b.setAttributesInOrder(attributes);
    header_b.setAttributesByName(attributes.toMap(a -> Tuple.of(a.name(), a)));
    header_b.setSchemaIdentifier(
      SMFVendorSchemaIdentifier.of(0x696F376D, 0xA0B0C0D0, 1, 2));
    final SMFHeader header = header_b.build();

    serializer.serializeHeader(header);
    serializer.serializeData(SMFAttributeName.of("x"));

    this.expected.expect(IllegalArgumentException.class);
    serializer.serializeValueIntegerSigned4(0L, 1L, 2L, 3L);
  }


  @Test
  public void testSerializerAttributeAll()
    throws IOException
  {
    final ByteArrayOutputStream out = new ByteArrayOutputStream();
    final Path path = Paths.get("/data");
    final SMFFormatVersion version = SMFFormatVersion.of(1, 0);

    final SMFSerializerType serializer =
      new SMFFormatBinary().serializerCreate(version, path, out);

    List<SMFAttribute> attributes = List.empty();
    for (final int size : List.of(
      Integer.valueOf(16),
      Integer.valueOf(32),
      Integer.valueOf(64))) {
      for (final int count : List.of(
        Integer.valueOf(1),
        Integer.valueOf(2),
        Integer.valueOf(3),
        Integer.valueOf(4))) {
        attributes = attributes.append(SMFAttribute.of(
          SMFAttributeName.of(String.format(
            "%s-%d-%d", SMFComponentType.ELEMENT_TYPE_INTEGER_SIGNED.name(),
            Integer.valueOf(size),
            Integer.valueOf(count))),
          SMFComponentType.ELEMENT_TYPE_INTEGER_SIGNED,
          count,
          size));
      }
    }

    final SMFHeader.Builder header_b = SMFHeader.builder();
    header_b.setVertexCount(1L);
    header_b.setTriangleIndexSizeBits(16L);
    header_b.setTriangleCount(0L);
    header_b.setAttributesInOrder(attributes);
    header_b.setAttributesByName(attributes.toMap(a -> Tuple.of(a.name(), a)));
    header_b.setSchemaIdentifier(
      SMFVendorSchemaIdentifier.of(0x696F376D, 0xA0B0C0D0, 1, 2));
    final SMFHeader header = header_b.build();

    serializer.serializeHeader(header);
    for (final SMFAttribute attribute : attributes) {
      serializer.serializeData(attribute.name());
      switch (attribute.componentCount()) {
        case 1: {
          serializer.serializeValueIntegerSigned1(0L);
          break;
        }
        case 2: {
          serializer.serializeValueIntegerSigned2(0L, 1L);
          break;
        }
        case 3: {
          serializer.serializeValueIntegerSigned3(0L, 1L, 2L);
          break;
        }
        case 4: {
          serializer.serializeValueIntegerSigned4(0L, 1L, 2L, 3L);
          break;
        }
        default: {
          throw new UnreachableCodeException();
        }
      }
    }
  }
}
