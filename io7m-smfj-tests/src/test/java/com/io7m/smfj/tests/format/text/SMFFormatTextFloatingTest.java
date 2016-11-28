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

package com.io7m.smfj.tests.format.text;

import com.io7m.jcoords.core.conversion.CAxis;
import com.io7m.jcoords.core.conversion.CAxisSystem;
import com.io7m.junreachable.UnreachableCodeException;
import com.io7m.smfj.core.SMFAttribute;
import com.io7m.smfj.core.SMFAttributeName;
import com.io7m.smfj.core.SMFComponentType;
import com.io7m.smfj.core.SMFCoordinateSystem;
import com.io7m.smfj.core.SMFFaceWindingOrder;
import com.io7m.smfj.core.SMFFormatVersion;
import com.io7m.smfj.core.SMFHeader;
import com.io7m.smfj.core.SMFSchemaIdentifier;
import com.io7m.smfj.format.text.SMFFormatText;
import com.io7m.smfj.parser.api.SMFParserEventsType;
import com.io7m.smfj.parser.api.SMFParserSequentialType;
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

public final class SMFFormatTextFloatingTest extends SMFTextTest
{
  private static final Logger LOG;

  static {
    LOG = LoggerFactory.getLogger(SMFFormatTextFloatingTest.class);
  }

  @Rule public final ExpectedException expected = ExpectedException.none();

  private static SMFHeader header(
    final SMFAttribute attr)
  {
    final SMFHeader.Builder header_b = SMFHeader.builder();
    header_b.setAttributesInOrder(List.of(attr));
    header_b.setAttributesByName(List.of(attr).toMap(a -> Tuple.of(
      a.name(),
      a)));
    header_b.setVertexCount(3L);
    header_b.setTriangleIndexSizeBits(32L);
    header_b.setTriangleCount(0L);
    header_b.setSchemaIdentifier(
      SMFSchemaIdentifier.of(0x696F376D, 0xA0B0C0D0, 1, 2));
    header_b.setCoordinateSystem(SMFCoordinateSystem.of(
      CAxisSystem.of(
        CAxis.AXIS_POSITIVE_X,
        CAxis.AXIS_POSITIVE_Y,
        CAxis.AXIS_NEGATIVE_Z),
      SMFFaceWindingOrder.FACE_WINDING_ORDER_COUNTER_CLOCKWISE));
    return header_b.build();
  }

  @Test
  public void testDataAttributesFloating64_4(
    final @Mocked SMFParserEventsType events)
  {
    final String name = "F64_4";
    final long component_count = 4L;
    final long component_size = 64L;
    final long vertex_count = 3L;

    final SMFAttribute attr = SMFAttribute.of(
      SMFAttributeName.of(name),
      SMFComponentType.ELEMENT_TYPE_FLOATING,
      (int) component_count,
      (int) component_size);

    final SMFHeader h = header(attr);

    new StrictExpectations()
    {{
      events.onStart();
      events.onVersionReceived(SMFFormatVersion.of(1, 0));
      events.onHeaderParsed(h);

      events.onDataAttributeStart(attr);
      events.onDataAttributeValueFloat4(0.0, 1.0, 2.0, 3.0);
      events.onDataAttributeValueFloat4(10.0, 11.0, 12.0, 13.0);
      events.onDataAttributeValueFloat4(20.0, 21.0, 22.0, 23.0);
      events.onDataAttributeFinish(attr);
    }};

    final SMFParserSequentialType p = this.parserFor(events, out -> {
      out.put("smf 1 0");
      out.put("schema 696F376D A0B0C0D0 1 2");
      out.put("vertices 3");
      out.put("triangles 0 32");
      out.put("coordinates +x +y -z counter-clockwise");
      out.put(String.format(
        "attribute \"%s\" %s %d %d",
        name,
        SMFComponentType.ELEMENT_TYPE_FLOATING.getName(),
        Long.valueOf(component_count),
        Long.valueOf(component_size)));
      out.put("data");

      out.put(String.format("attribute \"%s\"", name));
      for (int vertex = 0; vertex < vertex_count; ++vertex) {
        final StringBuilder sb = new StringBuilder(128);
        for (int component = 0; (long) component < component_count; ++component) {
          final double value = (double) ((vertex * 10) + component);
          sb.append(value);
          sb.append(" ");
        }
        out.put(sb.toString());
      }
    });

    p.parse();
  }

  @Test
  public void testDataAttributesFloating64_3(
    final @Mocked SMFParserEventsType events)
  {
    final String name = "F64_3";
    final long component_count = 3L;
    final long component_size = 64L;
    final long vertex_count = 3L;

    final SMFAttribute attr = SMFAttribute.of(
      SMFAttributeName.of(name),
      SMFComponentType.ELEMENT_TYPE_FLOATING,
      (int) component_count,
      (int) component_size);

    final SMFHeader h = header(attr);

    new StrictExpectations()
    {{
      events.onStart();
      events.onVersionReceived(SMFFormatVersion.of(1, 0));
      events.onHeaderParsed(h);

      events.onDataAttributeStart(attr);
      events.onDataAttributeValueFloat3(0.0, 1.0, 2.0);
      events.onDataAttributeValueFloat3(10.0, 11.0, 12.0);
      events.onDataAttributeValueFloat3(20.0, 21.0, 22.0);
      events.onDataAttributeFinish(attr);
    }};

    final SMFParserSequentialType p = this.parserFor(events, out -> {
      out.put("smf 1 0");
      out.put("schema 696F376D A0B0C0D0 1 2");
      out.put("vertices 3");
      out.put("triangles 0 32");
      out.put("coordinates +x +y -z counter-clockwise");
      out.put(String.format(
        "attribute \"%s\" %s %d %d",
        name,
        SMFComponentType.ELEMENT_TYPE_FLOATING.getName(),
        Long.valueOf(component_count),
        Long.valueOf(component_size)));
      out.put("data");

      out.put(String.format("attribute \"%s\"", name));
      for (int vertex = 0; vertex < vertex_count; ++vertex) {
        final StringBuilder sb = new StringBuilder(128);
        for (int component = 0; (long) component < component_count; ++component) {
          final double value = (double) ((vertex * 10) + component);
          sb.append(value);
          sb.append(" ");
        }
        out.put(sb.toString());
      }
    });

    p.parse();
  }

  @Test
  public void testDataAttributesFloating64_2(
    final @Mocked SMFParserEventsType events)
  {
    final String name = "F64_2";
    final long component_count = 2L;
    final long component_size = 64L;
    final long vertex_count = 3L;

    final SMFAttribute attr = SMFAttribute.of(
      SMFAttributeName.of(name),
      SMFComponentType.ELEMENT_TYPE_FLOATING,
      (int) component_count,
      (int) component_size);

    final SMFHeader h = header(attr);

    new StrictExpectations()
    {{
      events.onStart();
      events.onVersionReceived(SMFFormatVersion.of(1, 0));
      events.onHeaderParsed(h);

      events.onDataAttributeStart(attr);
      events.onDataAttributeValueFloat2(0.0, 1.0);
      events.onDataAttributeValueFloat2(10.0, 11.0);
      events.onDataAttributeValueFloat2(20.0, 21.0);
      events.onDataAttributeFinish(attr);
    }};

    final SMFParserSequentialType p = this.parserFor(events, out -> {
      out.put("smf 1 0");
      out.put("schema 696F376D A0B0C0D0 1 2");
      out.put("vertices 3");
      out.put("triangles 0 32");
      out.put("coordinates +x +y -z counter-clockwise");
      out.put(String.format(
        "attribute \"%s\" %s %d %d",
        name,
        SMFComponentType.ELEMENT_TYPE_FLOATING.getName(),
        Long.valueOf(component_count),
        Long.valueOf(component_size)));
      out.put("data");

      out.put(String.format("attribute \"%s\"", name));
      for (int vertex = 0; vertex < vertex_count; ++vertex) {
        final StringBuilder sb = new StringBuilder(128);
        for (int component = 0; (long) component < component_count; ++component) {
          final double value = (double) ((vertex * 10) + component);
          sb.append(value);
          sb.append(" ");
        }
        out.put(sb.toString());
      }
    });

    p.parse();
  }

  @Test
  public void testDataAttributesFloating64_1(
    final @Mocked SMFParserEventsType events)
  {
    final String name = "F64_1";
    final long component_count = 1L;
    final long component_size = 64L;
    final long vertex_count = 3L;

    final SMFAttribute attr = SMFAttribute.of(
      SMFAttributeName.of(name),
      SMFComponentType.ELEMENT_TYPE_FLOATING,
      (int) component_count,
      (int) component_size);

    final SMFHeader h = header(attr);

    new StrictExpectations()
    {{
      events.onStart();
      events.onVersionReceived(SMFFormatVersion.of(1, 0));
      events.onHeaderParsed(h);

      events.onDataAttributeStart(attr);
      events.onDataAttributeValueFloat1(0.0);
      events.onDataAttributeValueFloat1(10.0);
      events.onDataAttributeValueFloat1(20.0);
      events.onDataAttributeFinish(attr);
    }};

    final SMFParserSequentialType p = this.parserFor(events, out -> {
      out.put("smf 1 0");
      out.put("schema 696F376D A0B0C0D0 1 2");
      out.put("vertices 3");
      out.put("triangles 0 32");
      out.put("coordinates +x +y -z counter-clockwise");
      out.put(String.format(
        "attribute \"%s\" %s %d %d",
        name,
        SMFComponentType.ELEMENT_TYPE_FLOATING.getName(),
        Long.valueOf(component_count),
        Long.valueOf(component_size)));
      out.put("data");

      out.put(String.format("attribute \"%s\"", name));
      for (int vertex = 0; vertex < vertex_count; ++vertex) {
        final StringBuilder sb = new StringBuilder(128);
        for (int component = 0; (long) component < component_count; ++component) {
          final double value = (double) ((vertex * 10) + component);
          sb.append(value);
          sb.append(" ");
        }
        out.put(sb.toString());
      }
    });

    p.parse();
  }

  @Test
  public void testDataAttributesFloating32_4(
    final @Mocked SMFParserEventsType events)
  {
    final String name = "F32_4";
    final long component_count = 4L;
    final long component_size = 32L;
    final long vertex_count = 3L;

    final SMFAttribute attr = SMFAttribute.of(
      SMFAttributeName.of(name),
      SMFComponentType.ELEMENT_TYPE_FLOATING,
      (int) component_count,
      (int) component_size);

    final SMFHeader h = header(attr);

    new StrictExpectations()
    {{
      events.onStart();
      events.onVersionReceived(SMFFormatVersion.of(1, 0));
      events.onHeaderParsed(h);

      events.onDataAttributeStart(attr);
      events.onDataAttributeValueFloat4(0.0, 1.0, 2.0, 3.0);
      events.onDataAttributeValueFloat4(10.0, 11.0, 12.0, 13.0);
      events.onDataAttributeValueFloat4(20.0, 21.0, 22.0, 23.0);
      events.onDataAttributeFinish(attr);
    }};

    final SMFParserSequentialType p = this.parserFor(events, out -> {
      out.put("smf 1 0");
      out.put("schema 696F376D A0B0C0D0 1 2");
      out.put("vertices 3");
      out.put("triangles 0 32");
      out.put("coordinates +x +y -z counter-clockwise");
      out.put(String.format(
        "attribute \"%s\" %s %d %d",
        name,
        SMFComponentType.ELEMENT_TYPE_FLOATING.getName(),
        Long.valueOf(component_count),
        Long.valueOf(component_size)));
      out.put("data");

      out.put(String.format("attribute \"%s\"", name));
      for (int vertex = 0; vertex < vertex_count; ++vertex) {
        final StringBuilder sb = new StringBuilder(128);
        for (int component = 0; (long) component < component_count; ++component) {
          final double value = (double) ((vertex * 10) + component);
          sb.append(value);
          sb.append(" ");
        }
        out.put(sb.toString());
      }
    });

    p.parse();
  }

  @Test
  public void testDataAttributesFloating32_3(
    final @Mocked SMFParserEventsType events)
  {
    final String name = "F32_3";
    final long component_count = 3L;
    final long component_size = 32L;
    final long vertex_count = 3L;

    final SMFAttribute attr = SMFAttribute.of(
      SMFAttributeName.of(name),
      SMFComponentType.ELEMENT_TYPE_FLOATING,
      (int) component_count,
      (int) component_size);

    final SMFHeader h = header(attr);

    new StrictExpectations()
    {{
      events.onStart();
      events.onVersionReceived(SMFFormatVersion.of(1, 0));
      events.onHeaderParsed(h);

      events.onDataAttributeStart(attr);
      events.onDataAttributeValueFloat3(0.0, 1.0, 2.0);
      events.onDataAttributeValueFloat3(10.0, 11.0, 12.0);
      events.onDataAttributeValueFloat3(20.0, 21.0, 22.0);
      events.onDataAttributeFinish(attr);
    }};

    final SMFParserSequentialType p = this.parserFor(events, out -> {
      out.put("smf 1 0");
      out.put("schema 696F376D A0B0C0D0 1 2");
      out.put("vertices 3");
      out.put("triangles 0 32");
      out.put("coordinates +x +y -z counter-clockwise");
      out.put(String.format(
        "attribute \"%s\" %s %d %d",
        name,
        SMFComponentType.ELEMENT_TYPE_FLOATING.getName(),
        Long.valueOf(component_count),
        Long.valueOf(component_size)));
      out.put("data");

      out.put(String.format("attribute \"%s\"", name));
      for (int vertex = 0; vertex < vertex_count; ++vertex) {
        final StringBuilder sb = new StringBuilder(128);
        for (int component = 0; (long) component < component_count; ++component) {
          final double value = (double) ((vertex * 10) + component);
          sb.append(value);
          sb.append(" ");
        }
        out.put(sb.toString());
      }
    });

    p.parse();
  }

  @Test
  public void testDataAttributesFloating32_2(
    final @Mocked SMFParserEventsType events)
  {
    final String name = "F32_2";
    final long component_count = 2L;
    final long component_size = 32L;
    final long vertex_count = 3L;

    final SMFAttribute attr = SMFAttribute.of(
      SMFAttributeName.of(name),
      SMFComponentType.ELEMENT_TYPE_FLOATING,
      (int) component_count,
      (int) component_size);

    final SMFHeader h = header(attr);

    new StrictExpectations()
    {{
      events.onStart();
      events.onVersionReceived(SMFFormatVersion.of(1, 0));
      events.onHeaderParsed(h);

      events.onDataAttributeStart(attr);
      events.onDataAttributeValueFloat2(0.0, 1.0);
      events.onDataAttributeValueFloat2(10.0, 11.0);
      events.onDataAttributeValueFloat2(20.0, 21.0);
      events.onDataAttributeFinish(attr);
    }};

    final SMFParserSequentialType p = this.parserFor(events, out -> {
      out.put("smf 1 0");
      out.put("schema 696F376D A0B0C0D0 1 2");
      out.put("vertices 3");
      out.put("triangles 0 32");
      out.put("coordinates +x +y -z counter-clockwise");
      out.put(String.format(
        "attribute \"%s\" %s %d %d",
        name,
        SMFComponentType.ELEMENT_TYPE_FLOATING.getName(),
        Long.valueOf(component_count),
        Long.valueOf(component_size)));
      out.put("data");

      out.put(String.format("attribute \"%s\"", name));
      for (int vertex = 0; vertex < vertex_count; ++vertex) {
        final StringBuilder sb = new StringBuilder(128);
        for (int component = 0; (long) component < component_count; ++component) {
          final double value = (double) ((vertex * 10) + component);
          sb.append(value);
          sb.append(" ");
        }
        out.put(sb.toString());
      }
    });

    p.parse();
  }

  @Test
  public void testDataAttributesFloating32_1(
    final @Mocked SMFParserEventsType events)
  {
    final String name = "F32_1";
    final long component_count = 1L;
    final long component_size = 32L;
    final long vertex_count = 3L;

    final SMFAttribute attr = SMFAttribute.of(
      SMFAttributeName.of(name),
      SMFComponentType.ELEMENT_TYPE_FLOATING,
      (int) component_count,
      (int) component_size);

    final SMFHeader h = header(attr);

    new StrictExpectations()
    {{
      events.onStart();
      events.onVersionReceived(SMFFormatVersion.of(1, 0));
      events.onHeaderParsed(h);

      events.onDataAttributeStart(attr);
      events.onDataAttributeValueFloat1(0.0);
      events.onDataAttributeValueFloat1(10.0);
      events.onDataAttributeValueFloat1(20.0);
      events.onDataAttributeFinish(attr);
    }};

    final SMFParserSequentialType p = this.parserFor(events, out -> {
      out.put("smf 1 0");
      out.put("schema 696F376D A0B0C0D0 1 2");
      out.put("vertices 3");
      out.put("triangles 0 32");
      out.put("coordinates +x +y -z counter-clockwise");
      out.put(String.format(
        "attribute \"%s\" %s %d %d",
        name,
        SMFComponentType.ELEMENT_TYPE_FLOATING.getName(),
        Long.valueOf(component_count),
        Long.valueOf(component_size)));
      out.put("data");

      out.put(String.format("attribute \"%s\"", name));
      for (int vertex = 0; vertex < vertex_count; ++vertex) {
        final StringBuilder sb = new StringBuilder(128);
        for (int component = 0; (long) component < component_count; ++component) {
          final double value = (double) ((vertex * 10) + component);
          sb.append(value);
          sb.append(" ");
        }
        out.put(sb.toString());
      }
    });

    p.parse();
  }

  @Test
  public void testDataAttributesFloating16_4(
    final @Mocked SMFParserEventsType events)
  {
    final String name = "F16_4";
    final long component_count = 4L;
    final long component_size = 16L;
    final long vertex_count = 3L;

    final SMFAttribute attr = SMFAttribute.of(
      SMFAttributeName.of(name),
      SMFComponentType.ELEMENT_TYPE_FLOATING,
      (int) component_count,
      (int) component_size);

    final SMFHeader h = header(attr);

    new StrictExpectations()
    {{
      events.onStart();
      events.onVersionReceived(SMFFormatVersion.of(1, 0));
      events.onHeaderParsed(h);

      events.onDataAttributeStart(attr);
      events.onDataAttributeValueFloat4(0.0, 1.0, 2.0, 3.0);
      events.onDataAttributeValueFloat4(10.0, 11.0, 12.0, 13.0);
      events.onDataAttributeValueFloat4(20.0, 21.0, 22.0, 23.0);
      events.onDataAttributeFinish(attr);
    }};

    final SMFParserSequentialType p = this.parserFor(events, out -> {
      out.put("smf 1 0");
      out.put("schema 696F376D A0B0C0D0 1 2");
      out.put("vertices 3");
      out.put("triangles 0 32");
      out.put("coordinates +x +y -z counter-clockwise");
      out.put(String.format(
        "attribute \"%s\" %s %d %d",
        name,
        SMFComponentType.ELEMENT_TYPE_FLOATING.getName(),
        Long.valueOf(component_count),
        Long.valueOf(component_size)));
      out.put("data");

      out.put(String.format("attribute \"%s\"", name));
      for (int vertex = 0; vertex < vertex_count; ++vertex) {
        final StringBuilder sb = new StringBuilder(128);
        for (int component = 0; (long) component < component_count; ++component) {
          final double value = (double) ((vertex * 10) + component);
          sb.append(value);
          sb.append(" ");
        }
        out.put(sb.toString());
      }
    });

    p.parse();
  }

  @Test
  public void testDataAttributesFloating16_3(
    final @Mocked SMFParserEventsType events)
  {
    final String name = "F16_3";
    final long component_count = 3L;
    final long component_size = 16L;
    final long vertex_count = 3L;

    final SMFAttribute attr = SMFAttribute.of(
      SMFAttributeName.of(name),
      SMFComponentType.ELEMENT_TYPE_FLOATING,
      (int) component_count,
      (int) component_size);

    final SMFHeader h = header(attr);

    new StrictExpectations()
    {{
      events.onStart();
      events.onVersionReceived(SMFFormatVersion.of(1, 0));
      events.onHeaderParsed(h);

      events.onDataAttributeStart(attr);
      events.onDataAttributeValueFloat3(0.0, 1.0, 2.0);
      events.onDataAttributeValueFloat3(10.0, 11.0, 12.0);
      events.onDataAttributeValueFloat3(20.0, 21.0, 22.0);
      events.onDataAttributeFinish(attr);
    }};

    final SMFParserSequentialType p = this.parserFor(events, out -> {
      out.put("smf 1 0");
      out.put("schema 696F376D A0B0C0D0 1 2");
      out.put("vertices 3");
      out.put("triangles 0 32");
      out.put("coordinates +x +y -z counter-clockwise");
      out.put(String.format(
        "attribute \"%s\" %s %d %d",
        name,
        SMFComponentType.ELEMENT_TYPE_FLOATING.getName(),
        Long.valueOf(component_count),
        Long.valueOf(component_size)));
      out.put("data");

      out.put(String.format("attribute \"%s\"", name));
      for (int vertex = 0; vertex < vertex_count; ++vertex) {
        final StringBuilder sb = new StringBuilder(128);
        for (int component = 0; (long) component < component_count; ++component) {
          final double value = (double) ((vertex * 10) + component);
          sb.append(value);
          sb.append(" ");
        }
        out.put(sb.toString());
      }
    });

    p.parse();
  }

  @Test
  public void testDataAttributesFloating16_2(
    final @Mocked SMFParserEventsType events)
  {
    final String name = "F16_2";
    final long component_count = 2L;
    final long component_size = 16L;
    final long vertex_count = 3L;

    final SMFAttribute attr = SMFAttribute.of(
      SMFAttributeName.of(name),
      SMFComponentType.ELEMENT_TYPE_FLOATING,
      (int) component_count,
      (int) component_size);

    final SMFHeader h = header(attr);

    new StrictExpectations()
    {{
      events.onStart();
      events.onVersionReceived(SMFFormatVersion.of(1, 0));
      events.onHeaderParsed(h);

      events.onDataAttributeStart(attr);
      events.onDataAttributeValueFloat2(0.0, 1.0);
      events.onDataAttributeValueFloat2(10.0, 11.0);
      events.onDataAttributeValueFloat2(20.0, 21.0);
      events.onDataAttributeFinish(attr);
    }};

    final SMFParserSequentialType p = this.parserFor(events, out -> {
      out.put("smf 1 0");
      out.put("schema 696F376D A0B0C0D0 1 2");
      out.put("vertices 3");
      out.put("triangles 0 32");
      out.put("coordinates +x +y -z counter-clockwise");
      out.put(String.format(
        "attribute \"%s\" %s %d %d",
        name,
        SMFComponentType.ELEMENT_TYPE_FLOATING.getName(),
        Long.valueOf(component_count),
        Long.valueOf(component_size)));
      out.put("data");

      out.put(String.format("attribute \"%s\"", name));
      for (int vertex = 0; vertex < vertex_count; ++vertex) {
        final StringBuilder sb = new StringBuilder(128);
        for (int component = 0; (long) component < component_count; ++component) {
          final double value = (double) ((vertex * 10) + component);
          sb.append(value);
          sb.append(" ");
        }
        out.put(sb.toString());
      }
    });

    p.parse();
  }

  @Test
  public void testDataAttributesFloating16_1(
    final @Mocked SMFParserEventsType events)
  {
    final String name = "F16_1";
    final long component_count = 1L;
    final long component_size = 16L;
    final long vertex_count = 3L;

    final SMFAttribute attr = SMFAttribute.of(
      SMFAttributeName.of(name),
      SMFComponentType.ELEMENT_TYPE_FLOATING,
      (int) component_count,
      (int) component_size);

    final SMFHeader h = header(attr);

    new StrictExpectations()
    {{
      events.onStart();
      events.onVersionReceived(SMFFormatVersion.of(1, 0));
      events.onHeaderParsed(h);

      events.onDataAttributeStart(attr);
      events.onDataAttributeValueFloat1(0.0);
      events.onDataAttributeValueFloat1(10.0);
      events.onDataAttributeValueFloat1(20.0);
      events.onDataAttributeFinish(attr);
    }};

    final SMFParserSequentialType p = this.parserFor(events, out -> {
      out.put("smf 1 0");
      out.put("schema 696F376D A0B0C0D0 1 2");
      out.put("vertices 3");
      out.put("triangles 0 32");
      out.put("coordinates +x +y -z counter-clockwise");
      out.put(String.format(
        "attribute \"%s\" %s %d %d",
        name,
        SMFComponentType.ELEMENT_TYPE_FLOATING.getName(),
        Long.valueOf(component_count),
        Long.valueOf(component_size)));
      out.put("data");

      out.put(String.format("attribute \"%s\"", name));
      for (int vertex = 0; vertex < vertex_count; ++vertex) {
        final StringBuilder sb = new StringBuilder(128);
        for (int component = 0; (long) component < component_count; ++component) {
          final double value = (double) ((vertex * 10) + component);
          sb.append(value);
          sb.append(" ");
        }
        out.put(sb.toString());
      }
    });

    p.parse();
  }

  @Test
  public void testSerializerAttributeTooFew()
    throws IOException
  {
    final ByteArrayOutputStream out = new ByteArrayOutputStream();
    final Path path = Paths.get("/data");
    final SMFFormatVersion version = SMFFormatVersion.of(1, 0);

    final SMFSerializerType serializer =
      new SMFFormatText().serializerCreate(version, path, out);

    final List<SMFAttribute> attributes = List.of(
      SMFAttribute.of(
        SMFAttributeName.of("x"),
        SMFComponentType.ELEMENT_TYPE_FLOATING,
        4,
        64),
      SMFAttribute.of(
        SMFAttributeName.of("y"),
        SMFComponentType.ELEMENT_TYPE_FLOATING,
        4,
        64));

    final SMFHeader.Builder header_b = SMFHeader.builder();
    header_b.setVertexCount(2L);
    header_b.setTriangleIndexSizeBits(16L);
    header_b.setTriangleCount(0L);
    header_b.setAttributesInOrder(attributes);
    header_b.setAttributesByName(attributes.toMap(a -> Tuple.of(a.name(), a)));
    header_b.setSchemaIdentifier(
      SMFSchemaIdentifier.of(0x696F376D, 0xA0B0C0D0, 1, 2));
    header_b.setCoordinateSystem(SMFCoordinateSystem.of(
      CAxisSystem.of(
        CAxis.AXIS_POSITIVE_X,
        CAxis.AXIS_POSITIVE_Y,
        CAxis.AXIS_NEGATIVE_Z),
      SMFFaceWindingOrder.FACE_WINDING_ORDER_COUNTER_CLOCKWISE));
    final SMFHeader header = header_b.build();

    serializer.serializeHeader(header);
    serializer.serializeData(SMFAttributeName.of("x"));
    serializer.serializeValueFloat4(0.0, 1.0, 2.0, 3.0);

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
      new SMFFormatText().serializerCreate(version, path, out);

    final SMFHeader header = header(SMFAttribute.of(
      SMFAttributeName.of("x"),
      SMFComponentType.ELEMENT_TYPE_FLOATING,
      4,
      64));

    serializer.serializeHeader(header);
    serializer.serializeData(SMFAttributeName.of("x"));

    this.expected.expect(IllegalArgumentException.class);
    serializer.serializeValueFloat3(0.0, 1.0, 2.0);
  }

  @Test
  public void testSerializerAttributeWrongTypeF64_3()
    throws IOException
  {
    final ByteArrayOutputStream out = new ByteArrayOutputStream();
    final Path path = Paths.get("/data");
    final SMFFormatVersion version = SMFFormatVersion.of(1, 0);

    final SMFSerializerType serializer =
      new SMFFormatText().serializerCreate(version, path, out);

    final SMFHeader header = header(SMFAttribute.of(
      SMFAttributeName.of("x"),
      SMFComponentType.ELEMENT_TYPE_FLOATING,
      3,
      64));

    serializer.serializeHeader(header);
    serializer.serializeData(SMFAttributeName.of("x"));

    this.expected.expect(IllegalArgumentException.class);
    serializer.serializeValueFloat4(0.0, 1.0, 2.0, 3.0);
  }

  @Test
  public void testSerializerAttributeWrongTypeF64_2()
    throws IOException
  {
    final ByteArrayOutputStream out = new ByteArrayOutputStream();
    final Path path = Paths.get("/data");
    final SMFFormatVersion version = SMFFormatVersion.of(1, 0);

    final SMFSerializerType serializer =
      new SMFFormatText().serializerCreate(version, path, out);

    final SMFHeader header = header(SMFAttribute.of(
      SMFAttributeName.of("x"),
      SMFComponentType.ELEMENT_TYPE_FLOATING,
      2,
      64));

    serializer.serializeHeader(header);
    serializer.serializeData(SMFAttributeName.of("x"));

    this.expected.expect(IllegalArgumentException.class);
    serializer.serializeValueFloat4(0.0, 1.0, 2.0, 3.0);
  }

  @Test
  public void testSerializerAttributeWrongTypeF64_1()
    throws IOException
  {
    final ByteArrayOutputStream out = new ByteArrayOutputStream();
    final Path path = Paths.get("/data");
    final SMFFormatVersion version = SMFFormatVersion.of(1, 0);

    final SMFSerializerType serializer =
      new SMFFormatText().serializerCreate(version, path, out);

    final SMFHeader header = header(SMFAttribute.of(
      SMFAttributeName.of("x"),
      SMFComponentType.ELEMENT_TYPE_FLOATING,
      1,
      64));

    serializer.serializeHeader(header);
    serializer.serializeData(SMFAttributeName.of("x"));

    this.expected.expect(IllegalArgumentException.class);
    serializer.serializeValueFloat4(0.0, 1.0, 2.0, 3.0);
  }

  @Test
  public void testSerializerAttributeWrongTypeF32_4()
    throws IOException
  {
    final ByteArrayOutputStream out = new ByteArrayOutputStream();
    final Path path = Paths.get("/data");
    final SMFFormatVersion version = SMFFormatVersion.of(1, 0);

    final SMFSerializerType serializer =
      new SMFFormatText().serializerCreate(version, path, out);

    final SMFHeader header = header(SMFAttribute.of(
      SMFAttributeName.of("x"),
      SMFComponentType.ELEMENT_TYPE_FLOATING,
      4,
      32));

    serializer.serializeHeader(header);
    serializer.serializeData(SMFAttributeName.of("x"));

    this.expected.expect(IllegalArgumentException.class);
    serializer.serializeValueFloat3(0.0, 1.0, 2.0);
  }

  @Test
  public void testSerializerAttributeWrongTypeF32_3()
    throws IOException
  {
    final ByteArrayOutputStream out = new ByteArrayOutputStream();
    final Path path = Paths.get("/data");
    final SMFFormatVersion version = SMFFormatVersion.of(1, 0);

    final SMFSerializerType serializer =
      new SMFFormatText().serializerCreate(version, path, out);

    final SMFHeader header = header(SMFAttribute.of(
      SMFAttributeName.of("x"),
      SMFComponentType.ELEMENT_TYPE_FLOATING,
      3,
      32));

    serializer.serializeHeader(header);
    serializer.serializeData(SMFAttributeName.of("x"));

    this.expected.expect(IllegalArgumentException.class);
    serializer.serializeValueFloat4(0.0, 1.0, 2.0, 3.0);
  }

  @Test
  public void testSerializerAttributeWrongTypeF32_2()
    throws IOException
  {
    final ByteArrayOutputStream out = new ByteArrayOutputStream();
    final Path path = Paths.get("/data");
    final SMFFormatVersion version = SMFFormatVersion.of(1, 0);

    final SMFSerializerType serializer =
      new SMFFormatText().serializerCreate(version, path, out);

    final SMFHeader header = header(SMFAttribute.of(
      SMFAttributeName.of("x"),
      SMFComponentType.ELEMENT_TYPE_FLOATING,
      2,
      32));

    serializer.serializeHeader(header);
    serializer.serializeData(SMFAttributeName.of("x"));

    this.expected.expect(IllegalArgumentException.class);
    serializer.serializeValueFloat4(0.0, 1.0, 2.0, 3.0);
  }

  @Test
  public void testSerializerAttributeWrongTypeF32_1()
    throws IOException
  {
    final ByteArrayOutputStream out = new ByteArrayOutputStream();
    final Path path = Paths.get("/data");
    final SMFFormatVersion version = SMFFormatVersion.of(1, 0);

    final SMFSerializerType serializer =
      new SMFFormatText().serializerCreate(version, path, out);

    final SMFHeader header = header(SMFAttribute.of(
      SMFAttributeName.of("x"),
      SMFComponentType.ELEMENT_TYPE_FLOATING,
      1,
      32));

    serializer.serializeHeader(header);
    serializer.serializeData(SMFAttributeName.of("x"));

    this.expected.expect(IllegalArgumentException.class);
    serializer.serializeValueFloat4(0.0, 1.0, 2.0, 3.0);
  }


  @Test
  public void testSerializerAttributeWrongTypeF16_4()
    throws IOException
  {
    final ByteArrayOutputStream out = new ByteArrayOutputStream();
    final Path path = Paths.get("/data");
    final SMFFormatVersion version = SMFFormatVersion.of(1, 0);

    final SMFSerializerType serializer =
      new SMFFormatText().serializerCreate(version, path, out);

    final SMFHeader header = header(SMFAttribute.of(
      SMFAttributeName.of("x"),
      SMFComponentType.ELEMENT_TYPE_FLOATING,
      4,
      16));

    serializer.serializeHeader(header);
    serializer.serializeData(SMFAttributeName.of("x"));

    this.expected.expect(IllegalArgumentException.class);
    serializer.serializeValueFloat3(0.0, 1.0, 2.0);
  }

  @Test
  public void testSerializerAttributeWrongTypeF16_3()
    throws IOException
  {
    final ByteArrayOutputStream out = new ByteArrayOutputStream();
    final Path path = Paths.get("/data");
    final SMFFormatVersion version = SMFFormatVersion.of(1, 0);

    final SMFSerializerType serializer =
      new SMFFormatText().serializerCreate(version, path, out);

    final SMFHeader header = header(SMFAttribute.of(
      SMFAttributeName.of("x"),
      SMFComponentType.ELEMENT_TYPE_FLOATING,
      3,
      16));

    serializer.serializeHeader(header);
    serializer.serializeData(SMFAttributeName.of("x"));

    this.expected.expect(IllegalArgumentException.class);
    serializer.serializeValueFloat4(0.0, 1.0, 2.0, 3.0);
  }

  @Test
  public void testSerializerAttributeWrongTypeF16_2()
    throws IOException
  {
    final ByteArrayOutputStream out = new ByteArrayOutputStream();
    final Path path = Paths.get("/data");
    final SMFFormatVersion version = SMFFormatVersion.of(1, 0);

    final SMFSerializerType serializer =
      new SMFFormatText().serializerCreate(version, path, out);

    final SMFHeader header = header(SMFAttribute.of(
      SMFAttributeName.of("x"),
      SMFComponentType.ELEMENT_TYPE_FLOATING,
      2,
      16));

    serializer.serializeHeader(header);
    serializer.serializeData(SMFAttributeName.of("x"));

    this.expected.expect(IllegalArgumentException.class);
    serializer.serializeValueFloat4(0.0, 1.0, 2.0, 3.0);
  }

  @Test
  public void testSerializerAttributeWrongTypeF16_1()
    throws IOException
  {
    final ByteArrayOutputStream out = new ByteArrayOutputStream();
    final Path path = Paths.get("/data");
    final SMFFormatVersion version = SMFFormatVersion.of(1, 0);

    final SMFSerializerType serializer =
      new SMFFormatText().serializerCreate(version, path, out);

    final SMFHeader header = header(SMFAttribute.of(
      SMFAttributeName.of("x"),
      SMFComponentType.ELEMENT_TYPE_FLOATING,
      1,
      16));

    serializer.serializeHeader(header);
    serializer.serializeData(SMFAttributeName.of("x"));

    this.expected.expect(IllegalArgumentException.class);
    serializer.serializeValueFloat4(0.0, 1.0, 2.0, 3.0);
  }

  @Test
  public void testSerializerAttributeAll()
    throws IOException
  {
    final ByteArrayOutputStream out = new ByteArrayOutputStream();
    final Path path = Paths.get("/data");
    final SMFFormatVersion version = SMFFormatVersion.of(1, 0);

    final SMFSerializerType serializer =
      new SMFFormatText().serializerCreate(version, path, out);

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
            "%s-%d-%d", SMFComponentType.ELEMENT_TYPE_FLOATING.name(),
            Integer.valueOf(size),
            Integer.valueOf(count))),
          SMFComponentType.ELEMENT_TYPE_FLOATING,
          count,
          size));
      }
    }

    final SMFHeader.Builder header_b = SMFHeader.builder();
    header_b.setAttributesInOrder(attributes);
    header_b.setAttributesByName(attributes.toMap(a -> Tuple.of(a.name(), a)));
    header_b.setVertexCount(1L);
    header_b.setTriangleIndexSizeBits(32L);
    header_b.setTriangleCount(0L);
    header_b.setSchemaIdentifier(
      SMFSchemaIdentifier.of(0x696F376D, 0xA0B0C0D0, 1, 2));
    header_b.setCoordinateSystem(SMFCoordinateSystem.of(
      CAxisSystem.of(
        CAxis.AXIS_POSITIVE_X,
        CAxis.AXIS_POSITIVE_Y,
        CAxis.AXIS_NEGATIVE_Z),
      SMFFaceWindingOrder.FACE_WINDING_ORDER_COUNTER_CLOCKWISE));
    final SMFHeader header = header_b.build();

    serializer.serializeHeader(header);
    for (final SMFAttribute attribute : attributes) {
      serializer.serializeData(attribute.name());
      switch (attribute.componentCount()) {
        case 1: {
          serializer.serializeValueFloat1(0.0);
          break;
        }
        case 2: {
          serializer.serializeValueFloat2(0.0, 1.0);
          break;
        }
        case 3: {
          serializer.serializeValueFloat3(0.0, 1.0, 2.0);
          break;
        }
        case 4: {
          serializer.serializeValueFloat4(0.0, 1.0, 2.0, 3.0);
          break;
        }
        default: {
          throw new UnreachableCodeException();
        }
      }
    }
  }
}

