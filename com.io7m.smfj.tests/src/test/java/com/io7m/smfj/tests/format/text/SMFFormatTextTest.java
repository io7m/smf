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
import com.io7m.jfsm.core.FSMTransitionException;
import com.io7m.smfj.core.SMFAttribute;
import com.io7m.smfj.core.SMFAttributeName;
import com.io7m.smfj.core.SMFComponentType;
import com.io7m.smfj.core.SMFCoordinateSystem;
import com.io7m.smfj.core.SMFFaceWindingOrder;
import com.io7m.smfj.core.SMFFormatVersion;
import com.io7m.smfj.core.SMFHeader;
import com.io7m.smfj.core.SMFSchemaIdentifier;
import com.io7m.smfj.core.SMFTriangles;
import com.io7m.smfj.format.text.SMFFormatText;
import com.io7m.smfj.parser.api.SMFParseError;
import com.io7m.smfj.parser.api.SMFParserEventsType;
import com.io7m.smfj.parser.api.SMFParserSequentialType;
import com.io7m.smfj.serializer.api.SMFSerializerType;
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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;

public final class SMFFormatTextTest
{
  private static final Logger LOG;
  private static final Path PATH = Paths.get("/example");

  static {
    LOG = LoggerFactory.getLogger(SMFFormatTextTest.class);
  }

  @Rule public final ExpectedException expected = ExpectedException.none();

  private static void runForText(
    final SMFParserEventsType events,
    final boolean data,
    final StringBuilder text)
  {
    try {
      final ByteArrayInputStream stream =
        new ByteArrayInputStream(text.toString().getBytes(StandardCharsets.UTF_8));

      final SMFFormatText format = new SMFFormatText();
      final SMFParserSequentialType parser =
        format.parserCreateSequential(events, PATH, stream);
      parser.parseHeader();
      if (data) {
        parser.parseData();
      }
      parser.close();
    } catch (final IOException e) {
      throw new UncheckedIOException(e);
    }
  }

  private static SMFHeader.Builder baseHeader(
    final List<SMFAttribute> attributes)
  {
    final SMFHeader.Builder header_b = SMFHeader.builder();
    header_b.setVertexCount(0L);
    header_b.setTriangles(baseTriangles());
    header_b.setAttributesInOrder(attributes);
    header_b.setSchemaIdentifier(
      SMFSchemaIdentifier.of(0x696F376D, 0xA0B0C0D0, 1, 2));
    header_b.setCoordinateSystem(SMFCoordinateSystem.of(
      CAxisSystem.of(
        CAxis.AXIS_POSITIVE_X,
        CAxis.AXIS_POSITIVE_Y,
        CAxis.AXIS_NEGATIVE_Z),
      SMFFaceWindingOrder.FACE_WINDING_ORDER_COUNTER_CLOCKWISE));
    return header_b;
  }

  private static SMFTriangles baseTriangles()
  {
    return SMFTriangles.of(0L, 16L);
  }

  @Test
  public void testEmpty(
    final @Mocked SMFParserEventsType events)
  {
    final StringBuilder s = new StringBuilder(128);

    new StrictExpectations()
    {{
      events.onStart();
      events.onError(this.withArgThat(
        new ParseErrorMessageStartsWith("Unexpected EOF")));
      events.onFinish();
    }};

    runForText(events, false, s);
  }

  @Test
  public void testBadVersion0(
    final @Mocked SMFParserEventsType events)
  {
    final StringBuilder s = new StringBuilder(128);
    s.append("gibberish");
    s.append(System.lineSeparator());

    new StrictExpectations()
    {{
      events.onStart();
      events.onError(this.withArgThat(
        new ParseErrorMessageStartsWith("Unrecognized command")));
      events.onFinish();
    }};

    runForText(events, false, s);
  }

  @Test
  public void testBadVersion1(
    final @Mocked SMFParserEventsType events)
  {
    final StringBuilder s = new StringBuilder(128);
    s.append("smf");
    s.append(System.lineSeparator());

    new StrictExpectations()
    {{
      events.onStart();
      events.onError(this.withArgThat(
        new ParseErrorMessageStartsWith("Incorrect number of arguments")));
      events.onFinish();
    }};

    runForText(events, false, s);
  }

  @Test
  public void testBadVersion2(
    final @Mocked SMFParserEventsType events)
  {
    final StringBuilder s = new StringBuilder(128);
    s.append("smf 1");
    s.append(System.lineSeparator());

    new StrictExpectations()
    {{
      events.onStart();
      events.onError(this.withArgThat(
        new ParseErrorMessageStartsWith("Incorrect number of arguments")));
      events.onFinish();
    }};

    runForText(events, false, s);
  }

  @Test
  public void testBadVersion3(
    final @Mocked SMFParserEventsType events)
  {
    final StringBuilder s = new StringBuilder(128);
    s.append("smf x");
    s.append(System.lineSeparator());

    new StrictExpectations()
    {{
      events.onStart();
      events.onError(this.withArgThat(
        new ParseErrorMessageStartsWith("Incorrect number of arguments")));
      events.onFinish();
    }};

    runForText(events, false, s);
  }

  @Test
  public void testBadVersion4(
    final @Mocked SMFParserEventsType events)
  {
    final StringBuilder s = new StringBuilder(128);
    s.append("smf 1 x");
    s.append(System.lineSeparator());

    new StrictExpectations()
    {{
      events.onStart();
      events.onError(this.withArgThat(
        new ParseErrorMessageStartsWith("Cannot parse number")));
      events.onFinish();
    }};

    runForText(events, false, s);
  }

  @Test
  public void testBadVersion5(
    final @Mocked SMFParserEventsType events)
  {
    final StringBuilder s = new StringBuilder(128);
    s.append("smf 1 0 x");
    s.append(System.lineSeparator());

    new StrictExpectations()
    {{
      events.onStart();
      events.onError(this.withArgThat(
        new ParseErrorMessageStartsWith("Incorrect number of arguments")));
      events.onFinish();
    }};

    runForText(events, false, s);
  }

  @Test
  public void testBadVersion6(
    final @Mocked SMFParserEventsType events)
  {
    final StringBuilder s = new StringBuilder(128);
    s.append("#");
    s.append(System.lineSeparator());

    new StrictExpectations()
    {{
      events.onStart();
      events.onError(this.withArgThat(
        new ParseErrorMessageStartsWith(
          "The first line must be a version declaration.")));
      events.onFinish();
    }};

    runForText(events, false, s);
  }

  @Test
  public void testBadVersion7(
    final @Mocked SMFParserEventsType events)
  {
    final StringBuilder s = new StringBuilder(128);
    s.append("");
    s.append(System.lineSeparator());

    new StrictExpectations()
    {{
      events.onStart();
      events.onError(this.withArgThat(
        new ParseErrorMessageStartsWith(
          "The first line must be a version declaration.")));
      events.onFinish();
    }};

    runForText(events, false, s);
  }

  @Test
  public void testBadVersionUnsupported(
    final @Mocked SMFParserEventsType events)
  {
    final StringBuilder s = new StringBuilder(128);
    s.append("smf 612367 0");
    s.append(System.lineSeparator());

    new StrictExpectations()
    {{
      events.onStart();
      events.onVersionReceived(SMFFormatVersion.of(612367, 0));
      events.onError(this.withArgThat(
        new ParseErrorMessageStartsWith("Unsupported version")));
      events.onFinish();
    }};

    runForText(events, false, s);
  }

  @Test
  public void testBadVertices0(
    final @Mocked SMFParserEventsType events)
  {
    final StringBuilder s = new StringBuilder(128);
    s.append("smf 1 0");
    s.append(System.lineSeparator());
    s.append("vertices");
    s.append(System.lineSeparator());

    new StrictExpectations()
    {{
      events.onStart();
      events.onVersionReceived(SMFFormatVersion.of(1, 0));
      events.onError(this.withArgThat(
        new ParseErrorMessageStartsWith("Incorrect number of arguments")));
      events.onError(this.withArgThat(
        new ParseErrorMessageStartsWith("Unexpected EOF")));
      events.onFinish();
    }};

    runForText(events, false, s);
  }

  @Test
  public void testBadVertices1(
    final @Mocked SMFParserEventsType events)
  {
    final StringBuilder s = new StringBuilder(128);
    s.append("smf 1 0");
    s.append(System.lineSeparator());
    s.append("vertices x");
    s.append(System.lineSeparator());

    new StrictExpectations()
    {{
      events.onStart();
      events.onVersionReceived(SMFFormatVersion.of(1, 0));
      events.onError(this.withArgThat(
        new ParseErrorMessageStartsWith("Cannot parse number")));
      events.onError(this.withArgThat(
        new ParseErrorMessageStartsWith("Unexpected EOF")));
      events.onFinish();
    }};

    runForText(events, false, s);
  }

  @Test
  public void testBadVertices2(
    final @Mocked SMFParserEventsType events)
  {
    final StringBuilder s = new StringBuilder(128);
    s.append("smf 1 0");
    s.append(System.lineSeparator());
    s.append("vertices 23 2");
    s.append(System.lineSeparator());

    new StrictExpectations()
    {{
      events.onStart();
      events.onVersionReceived(SMFFormatVersion.of(1, 0));
      events.onError(this.withArgThat(
        new ParseErrorMessageStartsWith("Incorrect number of arguments")));
      events.onError(this.withArgThat(
        new ParseErrorMessageStartsWith("Unexpected EOF")));
      events.onFinish();
    }};

    runForText(events, false, s);
  }

  @Test
  public void testBadTriangles0(
    final @Mocked SMFParserEventsType events)
  {
    final StringBuilder s = new StringBuilder(128);
    s.append("smf 1 0");
    s.append(System.lineSeparator());
    s.append("triangles");
    s.append(System.lineSeparator());

    new StrictExpectations()
    {{
      events.onStart();
      events.onVersionReceived(SMFFormatVersion.of(1, 0));
      events.onError(this.withArgThat(
        new ParseErrorMessageStartsWith("Incorrect number of arguments")));
      events.onError(this.withArgThat(
        new ParseErrorMessageStartsWith("Unexpected EOF")));
      events.onFinish();
    }};

    runForText(events, false, s);
  }

  @Test
  public void testBadTriangles1(
    final @Mocked SMFParserEventsType events)
  {
    final StringBuilder s = new StringBuilder(128);
    s.append("smf 1 0");
    s.append(System.lineSeparator());
    s.append("triangles x y");
    s.append(System.lineSeparator());

    new StrictExpectations()
    {{
      events.onStart();
      events.onVersionReceived(SMFFormatVersion.of(1, 0));
      events.onError(this.withArgThat(
        new ParseErrorMessageStartsWith("Cannot parse number")));
      events.onError(this.withArgThat(
        new ParseErrorMessageStartsWith("Unexpected EOF")));
      events.onFinish();
    }};

    runForText(events, false, s);
  }

  @Test
  public void testBadTriangles2(
    final @Mocked SMFParserEventsType events)
  {
    final StringBuilder s = new StringBuilder(128);
    s.append("smf 1 0");
    s.append(System.lineSeparator());
    s.append("triangles 1 y");
    s.append(System.lineSeparator());

    new StrictExpectations()
    {{
      events.onStart();
      events.onVersionReceived(SMFFormatVersion.of(1, 0));
      events.onError(this.withArgThat(
        new ParseErrorMessageStartsWith("Cannot parse number")));
      events.onError(this.withArgThat(
        new ParseErrorMessageStartsWith("Unexpected EOF")));
      events.onFinish();
    }};

    runForText(events, false, s);
  }

  @Test
  public void testBadTriangles3(
    final @Mocked SMFParserEventsType events)
  {
    final StringBuilder s = new StringBuilder(128);
    s.append("smf 1 0");
    s.append(System.lineSeparator());
    s.append("triangles 2 3 x");
    s.append(System.lineSeparator());

    new StrictExpectations()
    {{
      events.onStart();
      events.onVersionReceived(SMFFormatVersion.of(1, 0));
      events.onError(this.withArgThat(
        new ParseErrorMessageStartsWith("Incorrect number of arguments")));
      events.onError(this.withArgThat(
        new ParseErrorMessageStartsWith("Unexpected EOF")));
      events.onFinish();
    }};

    runForText(events, false, s);
  }

  @Test
  public void testBadMeta0(
    final @Mocked SMFParserEventsType events)
  {
    final StringBuilder s = new StringBuilder(128);
    s.append("smf 1 0");
    s.append(System.lineSeparator());
    s.append("meta");
    s.append(System.lineSeparator());

    new StrictExpectations()
    {{
      events.onStart();
      events.onVersionReceived(SMFFormatVersion.of(1, 0));
      events.onError(this.withArgThat(
        new ParseErrorMessageStartsWith("Incorrect number of arguments")));
      events.onError(this.withArgThat(
        new ParseErrorMessageStartsWith("Unexpected EOF")));
      events.onFinish();
    }};

    runForText(events, false, s);
  }

  @Test
  public void testBadMeta1(
    final @Mocked SMFParserEventsType events)
  {
    final StringBuilder s = new StringBuilder(128);
    s.append("smf 1 0");
    s.append(System.lineSeparator());
    s.append("meta x");
    s.append(System.lineSeparator());

    new StrictExpectations()
    {{
      events.onStart();
      events.onVersionReceived(SMFFormatVersion.of(1, 0));
      events.onError(this.withArgThat(
        new ParseErrorMessageStartsWith("Cannot parse number")));
      events.onError(this.withArgThat(
        new ParseErrorMessageStartsWith("Unexpected EOF")));
      events.onFinish();
    }};

    runForText(events, false, s);
  }

  @Test
  public void testBadMeta2(
    final @Mocked SMFParserEventsType events)
  {
    final StringBuilder s = new StringBuilder(128);
    s.append("smf 1 0");
    s.append(System.lineSeparator());
    s.append("meta 1 2");
    s.append(System.lineSeparator());

    new StrictExpectations()
    {{
      events.onStart();
      events.onVersionReceived(SMFFormatVersion.of(1, 0));
      events.onError(this.withArgThat(
        new ParseErrorMessageStartsWith("Incorrect number of arguments")));
      events.onError(this.withArgThat(
        new ParseErrorMessageStartsWith("Unexpected EOF")));
      events.onFinish();
    }};

    runForText(events, false, s);
  }

  @Test
  public void testBadCoordinateSpace0(
    final @Mocked SMFParserEventsType events)
  {
    final StringBuilder s = new StringBuilder(128);
    s.append("smf 1 0");
    s.append(System.lineSeparator());
    s.append("vertices 4");
    s.append(System.lineSeparator());
    s.append("triangles 2 16");
    s.append(System.lineSeparator());
    s.append("coordinates");
    s.append(System.lineSeparator());
    s.append("data");
    s.append(System.lineSeparator());

    new StrictExpectations()
    {{
      events.onStart();
      events.onVersionReceived(SMFFormatVersion.of(1, 0));
      events.onError(this.withArgThat(
        new ParseErrorMessageStartsWith("Incorrect number of arguments")));
      events.onFinish();
    }};

    runForText(events, false, s);
  }

  @Test
  public void testBadCoordinateSpace1(
    final @Mocked SMFParserEventsType events)
  {
    final StringBuilder s = new StringBuilder(128);
    s.append("smf 1 0");
    s.append(System.lineSeparator());
    s.append("vertices 4");
    s.append(System.lineSeparator());
    s.append("triangles 2 16");
    s.append(System.lineSeparator());
    s.append("coordinates +x");
    s.append(System.lineSeparator());
    s.append("data");
    s.append(System.lineSeparator());

    new StrictExpectations()
    {{
      events.onStart();
      events.onVersionReceived(SMFFormatVersion.of(1, 0));
      events.onError(this.withArgThat(
        new ParseErrorMessageStartsWith("Incorrect number of arguments")));
      events.onFinish();
    }};

    runForText(events, false, s);
  }

  @Test
  public void testBadCoordinateSpace2(
    final @Mocked SMFParserEventsType events)
  {
    final StringBuilder s = new StringBuilder(128);
    s.append("smf 1 0");
    s.append(System.lineSeparator());
    s.append("vertices 4");
    s.append(System.lineSeparator());
    s.append("triangles 2 16");
    s.append(System.lineSeparator());
    s.append("coordinates +x +y");
    s.append(System.lineSeparator());
    s.append("data");
    s.append(System.lineSeparator());

    new StrictExpectations()
    {{
      events.onStart();
      events.onVersionReceived(SMFFormatVersion.of(1, 0));
      events.onError(this.withArgThat(
        new ParseErrorMessageStartsWith("Incorrect number of arguments")));
      events.onFinish();
    }};

    runForText(events, false, s);
  }

  @Test
  public void testBadCoordinateSpace3(
    final @Mocked SMFParserEventsType events)
  {
    final StringBuilder s = new StringBuilder(128);
    s.append("smf 1 0");
    s.append(System.lineSeparator());
    s.append("vertices 4");
    s.append(System.lineSeparator());
    s.append("triangles 2 16");
    s.append(System.lineSeparator());
    s.append("coordinates +x +y +z");
    s.append(System.lineSeparator());
    s.append("data");
    s.append(System.lineSeparator());

    new StrictExpectations()
    {{
      events.onStart();
      events.onVersionReceived(SMFFormatVersion.of(1, 0));
      events.onError(this.withArgThat(
        new ParseErrorMessageStartsWith("Incorrect number of arguments")));
      events.onFinish();
    }};

    runForText(events, false, s);
  }

  @Test
  public void testBadCoordinateSpace4(
    final @Mocked SMFParserEventsType events)
  {
    final StringBuilder s = new StringBuilder(128);
    s.append("smf 1 0");
    s.append(System.lineSeparator());
    s.append("vertices 4");
    s.append(System.lineSeparator());
    s.append("triangles 2 16");
    s.append(System.lineSeparator());
    s.append("coordinates +x +x +z clockwise");
    s.append(System.lineSeparator());
    s.append("data");
    s.append(System.lineSeparator());

    new StrictExpectations()
    {{
      events.onStart();
      events.onVersionReceived(SMFFormatVersion.of(1, 0));
      events.onError(this.withArgThat(
        new ParseErrorMessageStartsWith("Axes must be perpendicular")));
      events.onFinish();
    }};

    runForText(events, false, s);
  }

  @Test
  public void testBadCoordinateSpace5(
    final @Mocked SMFParserEventsType events)
  {
    final StringBuilder s = new StringBuilder(128);
    s.append("smf 1 0");
    s.append(System.lineSeparator());
    s.append("vertices 4");
    s.append(System.lineSeparator());
    s.append("triangles 2 16");
    s.append(System.lineSeparator());
    s.append("coordinates +x +y +z q");
    s.append(System.lineSeparator());
    s.append("data");
    s.append(System.lineSeparator());

    new StrictExpectations()
    {{
      events.onStart();
      events.onVersionReceived(SMFFormatVersion.of(1, 0));
      events.onError(this.withArgThat(
        new ParseErrorMessageStartsWith("Unrecognized winding order: q")));
      events.onFinish();
    }};

    runForText(events, false, s);
  }

  @Test
  public void testBadAttribute0(
    final @Mocked SMFParserEventsType events)
  {
    final StringBuilder s = new StringBuilder(128);
    s.append("smf 1 0");
    s.append(System.lineSeparator());
    s.append("attribute");
    s.append(System.lineSeparator());

    new StrictExpectations()
    {{
      events.onStart();
      events.onVersionReceived(SMFFormatVersion.of(1, 0));
      events.onError(this.withArgThat(
        new ParseErrorMessageStartsWith("Incorrect number of arguments")));
      events.onError(this.withArgThat(
        new ParseErrorMessageStartsWith("Unexpected EOF")));
      events.onFinish();
    }};

    runForText(events, false, s);
  }

  @Test
  public void testBadData(
    final @Mocked SMFParserEventsType events)
  {
    final StringBuilder s = new StringBuilder(128);
    s.append("smf 1 0");
    s.append(System.lineSeparator());
    s.append("vertices 4");
    s.append(System.lineSeparator());
    s.append("triangles 2 16");
    s.append(System.lineSeparator());
    s.append("data x");
    s.append(System.lineSeparator());

    new StrictExpectations()
    {{
      events.onStart();
      events.onVersionReceived(SMFFormatVersion.of(1, 0));
      events.onError(this.withArgThat(
        new ParseErrorMessageStartsWith("Incorrect number of arguments")));
      events.onFinish();
    }};

    runForText(events, false, s);
  }

  @Test
  public void testBadAttribute1(
    final @Mocked SMFParserEventsType events)
  {
    final StringBuilder s = new StringBuilder(128);
    s.append("smf 1 0");
    s.append(System.lineSeparator());
    s.append("attribute xyz unknown 4 32");
    s.append(System.lineSeparator());

    new StrictExpectations()
    {{
      events.onStart();
      events.onVersionReceived(SMFFormatVersion.of(1, 0));
      events.onError(this.withArgThat(
        new ParseErrorMessageStartsWith("Unrecognized type")));
      events.onError(this.withArgThat(
        new ParseErrorMessageStartsWith("Unexpected EOF")));
      events.onFinish();
    }};

    runForText(events, false, s);
  }

  @Test
  public void testBadAttribute2(
    final @Mocked SMFParserEventsType events)
  {
    final StringBuilder s = new StringBuilder(128);
    s.append("smf 1 0");
    s.append(System.lineSeparator());
    s.append("attribute ");
    for (int index = 0; index < 65; ++index) {
      s.append("x");
    }
    s.append(" float 4 32");
    s.append(System.lineSeparator());

    new StrictExpectations()
    {{
      events.onStart();
      events.onVersionReceived(SMFFormatVersion.of(1, 0));
      events.onError(this.withArgThat(
        new ParseErrorMessageStartsWith(
          "Attribute names must match the pattern")));
      events.onError(this.withArgThat(
        new ParseErrorMessageStartsWith("Unexpected EOF")));
      events.onFinish();
    }};

    runForText(events, false, s);
  }

  @Test
  public void testFull16(
    final @Mocked SMFParserEventsType events)
  {
    final StringBuilder s = new StringBuilder(128);
    s.append("smf 1 0");
    s.append(System.lineSeparator());
    s.append("schema 696F376D A0B0C0D0 1 2");
    s.append(System.lineSeparator());
    s.append("meta 1");
    s.append(System.lineSeparator());
    s.append("coordinates +x +y -z counter-clockwise");
    s.append(System.lineSeparator());
    s.append("attribute \"float-1-16\" float 1 16");
    s.append(System.lineSeparator());
    s.append("attribute \"float-2-16\" float 2 16");
    s.append(System.lineSeparator());
    s.append("attribute \"float-3-16\" float 3 16");
    s.append(System.lineSeparator());
    s.append("attribute \"float-4-16\" float 4 16");
    s.append(System.lineSeparator());

    s.append("attribute \"integer-unsigned-1-16\" integer-unsigned 1 16");
    s.append(System.lineSeparator());
    s.append("attribute \"integer-unsigned-2-16\" integer-unsigned 2 16");
    s.append(System.lineSeparator());
    s.append("attribute \"integer-unsigned-3-16\" integer-unsigned 3 16");
    s.append(System.lineSeparator());
    s.append("attribute \"integer-unsigned-4-16\" integer-unsigned 4 16");
    s.append(System.lineSeparator());

    s.append("attribute \"integer-signed-1-16\" integer-signed 1 16");
    s.append(System.lineSeparator());
    s.append("attribute \"integer-signed-2-16\" integer-signed 2 16");
    s.append(System.lineSeparator());
    s.append("attribute \"integer-signed-3-16\" integer-signed 3 16");
    s.append(System.lineSeparator());
    s.append("attribute \"integer-signed-4-16\" integer-signed 4 16");
    s.append(System.lineSeparator());

    s.append("vertices 4");
    s.append(System.lineSeparator());
    s.append("triangles 2 16");
    s.append(System.lineSeparator());
    s.append("data");
    s.append(System.lineSeparator());

    /*
     * Float
     */

    s.append("attribute float-1-16");
    s.append(System.lineSeparator());
    s.append("0.0");
    s.append(System.lineSeparator());
    s.append("0.0");
    s.append(System.lineSeparator());
    s.append("0.0");
    s.append(System.lineSeparator());
    s.append("0.0");
    s.append(System.lineSeparator());

    s.append("attribute float-2-16");
    s.append(System.lineSeparator());
    s.append("0.0 1.0");
    s.append(System.lineSeparator());
    s.append("0.0 1.0");
    s.append(System.lineSeparator());
    s.append("0.0 1.0");
    s.append(System.lineSeparator());
    s.append("0.0 1.0");
    s.append(System.lineSeparator());

    s.append("attribute float-3-16");
    s.append(System.lineSeparator());
    s.append("0.0 1.0 2.0");
    s.append(System.lineSeparator());
    s.append("0.0 1.0 2.0");
    s.append(System.lineSeparator());
    s.append("0.0 1.0 2.0");
    s.append(System.lineSeparator());
    s.append("0.0 1.0 2.0");
    s.append(System.lineSeparator());

    s.append("attribute float-4-16");
    s.append(System.lineSeparator());
    s.append("0.0 1.0 2.0 3.0");
    s.append(System.lineSeparator());
    s.append("0.0 1.0 2.0 3.0");
    s.append(System.lineSeparator());
    s.append("0.0 1.0 2.0 3.0");
    s.append(System.lineSeparator());
    s.append("0.0 1.0 2.0 3.0");
    s.append(System.lineSeparator());

    /*
     * Unsigned
     */

    s.append("attribute integer-unsigned-1-16");
    s.append(System.lineSeparator());
    s.append("0");
    s.append(System.lineSeparator());
    s.append("0");
    s.append(System.lineSeparator());
    s.append("0");
    s.append(System.lineSeparator());
    s.append("0");
    s.append(System.lineSeparator());

    s.append("attribute integer-unsigned-2-16");
    s.append(System.lineSeparator());
    s.append("0 1");
    s.append(System.lineSeparator());
    s.append("0 1");
    s.append(System.lineSeparator());
    s.append("0 1");
    s.append(System.lineSeparator());
    s.append("0 1");
    s.append(System.lineSeparator());

    s.append("attribute integer-unsigned-3-16");
    s.append(System.lineSeparator());
    s.append("0 1 2");
    s.append(System.lineSeparator());
    s.append("0 1 2");
    s.append(System.lineSeparator());
    s.append("0 1 2");
    s.append(System.lineSeparator());
    s.append("0 1 2");
    s.append(System.lineSeparator());

    s.append("attribute integer-unsigned-4-16");
    s.append(System.lineSeparator());
    s.append("0 1 2 3");
    s.append(System.lineSeparator());
    s.append("0 1 2 3");
    s.append(System.lineSeparator());
    s.append("0 1 2 3");
    s.append(System.lineSeparator());
    s.append("0 1 2 3");
    s.append(System.lineSeparator());

    /*
     * Signed
     */

    s.append("attribute integer-signed-1-16");
    s.append(System.lineSeparator());
    s.append("0");
    s.append(System.lineSeparator());
    s.append("0");
    s.append(System.lineSeparator());
    s.append("0");
    s.append(System.lineSeparator());
    s.append("0");
    s.append(System.lineSeparator());

    s.append("attribute integer-signed-2-16");
    s.append(System.lineSeparator());
    s.append("0 1");
    s.append(System.lineSeparator());
    s.append("0 1");
    s.append(System.lineSeparator());
    s.append("0 1");
    s.append(System.lineSeparator());
    s.append("0 1");
    s.append(System.lineSeparator());

    s.append("attribute integer-signed-3-16");
    s.append(System.lineSeparator());
    s.append("0 1 2");
    s.append(System.lineSeparator());
    s.append("0 1 2");
    s.append(System.lineSeparator());
    s.append("0 1 2");
    s.append(System.lineSeparator());
    s.append("0 1 2");
    s.append(System.lineSeparator());

    s.append("attribute integer-signed-4-16");
    s.append(System.lineSeparator());
    s.append("0 1 2 3");
    s.append(System.lineSeparator());
    s.append("0 1 2 3");
    s.append(System.lineSeparator());
    s.append("0 1 2 3");
    s.append(System.lineSeparator());
    s.append("0 1 2 3");
    s.append(System.lineSeparator());

    s.append("triangles");
    s.append(System.lineSeparator());
    s.append("0 1 2");
    s.append(System.lineSeparator());
    s.append("0 2 3");
    s.append(System.lineSeparator());

    final byte[] bytes = new byte[256];
    for (int index = 0; index < bytes.length; ++index) {
      bytes[index] = (byte) index;
    }

    s.append("metadata");
    s.append(System.lineSeparator());
    s.append("meta 696F376D 0A0B0C0D 5");
    s.append(System.lineSeparator());
    s.append(
      "AAECAwQFBgcICQoLDA0ODxAREhMUFRYXGBkaGxwdHh8gISIjJCUmJygpKissLS4vMDEyMzQ1");
    s.append(System.lineSeparator());
    s.append(
      "Njc4OTo7PD0-P0BBQkNERUZHSElKS0xNTk9QUVJTVFVWV1hZWltcXV5fYGFiY2RlZmdoaWpr");
    s.append(System.lineSeparator());
    s.append(
      "bG1ub3BxcnN0dXZ3eHl6e3x9fn-AgYKDhIWGh4iJiouMjY6PkJGSk5SVlpeYmZqbnJ2en6Ch");
    s.append(System.lineSeparator());
    s.append(
      "oqOkpaanqKmqq6ytrq-wsbKztLW2t7i5uru8vb6_wMHCw8TFxsfIycrLzM3Oz9DR0tPU1dbX");
    s.append(System.lineSeparator());
    s.append("2Nna29zd3t_g4eLj5OXm5-jp6uvs7e7v8PHy8_T19vf4-fr7_P3-_w==");
    s.append(System.lineSeparator());

    final SMFAttribute attr_f1_16 = SMFAttribute.of(
      SMFAttributeName.of("float-1-16"),
      SMFComponentType.ELEMENT_TYPE_FLOATING,
      1,
      16);
    final SMFAttribute attr_f2_16 = SMFAttribute.of(
      SMFAttributeName.of("float-2-16"),
      SMFComponentType.ELEMENT_TYPE_FLOATING,
      2,
      16);
    final SMFAttribute attr_f3_16 = SMFAttribute.of(
      SMFAttributeName.of("float-3-16"),
      SMFComponentType.ELEMENT_TYPE_FLOATING,
      3,
      16);
    final SMFAttribute attr_f4_16 = SMFAttribute.of(
      SMFAttributeName.of("float-4-16"),
      SMFComponentType.ELEMENT_TYPE_FLOATING,
      4,
      16);

    final SMFAttribute attr_u1_16 = SMFAttribute.of(
      SMFAttributeName.of("integer-unsigned-1-16"),
      SMFComponentType.ELEMENT_TYPE_INTEGER_UNSIGNED,
      1,
      16);
    final SMFAttribute attr_u2_16 = SMFAttribute.of(
      SMFAttributeName.of("integer-unsigned-2-16"),
      SMFComponentType.ELEMENT_TYPE_INTEGER_UNSIGNED,
      2,
      16);
    final SMFAttribute attr_u3_16 = SMFAttribute.of(
      SMFAttributeName.of("integer-unsigned-3-16"),
      SMFComponentType.ELEMENT_TYPE_INTEGER_UNSIGNED,
      3,
      16);
    final SMFAttribute attr_u4_16 = SMFAttribute.of(
      SMFAttributeName.of("integer-unsigned-4-16"),
      SMFComponentType.ELEMENT_TYPE_INTEGER_UNSIGNED,
      4,
      16);

    final SMFAttribute attr_s1_16 = SMFAttribute.of(
      SMFAttributeName.of("integer-signed-1-16"),
      SMFComponentType.ELEMENT_TYPE_INTEGER_SIGNED,
      1,
      16);
    final SMFAttribute attr_s2_16 = SMFAttribute.of(
      SMFAttributeName.of("integer-signed-2-16"),
      SMFComponentType.ELEMENT_TYPE_INTEGER_SIGNED,
      2,
      16);
    final SMFAttribute attr_s3_16 = SMFAttribute.of(
      SMFAttributeName.of("integer-signed-3-16"),
      SMFComponentType.ELEMENT_TYPE_INTEGER_SIGNED,
      3,
      16);
    final SMFAttribute attr_s4_16 = SMFAttribute.of(
      SMFAttributeName.of("integer-signed-4-16"),
      SMFComponentType.ELEMENT_TYPE_INTEGER_SIGNED,
      4,
      16);

    final List<SMFAttribute> attribs = List.of(
      attr_f1_16,
      attr_f2_16,
      attr_f3_16,
      attr_f4_16,
      attr_u1_16,
      attr_u2_16,
      attr_u3_16,
      attr_u4_16,
      attr_s1_16,
      attr_s2_16,
      attr_s3_16,
      attr_s4_16
    );

    final SMFHeader.Builder header_b = SMFHeader.builder();
    header_b.setAttributesInOrder(attribs);
    header_b.setTriangles(SMFTriangles.of(2L, 16L));
    header_b.setVertexCount(4L);
    header_b.setMetaCount(1L);
    header_b.setSchemaIdentifier(
      SMFSchemaIdentifier.of(0x696F376D, 0xA0B0C0D0, 1, 2));
    header_b.setCoordinateSystem(SMFCoordinateSystem.of(
      CAxisSystem.of(
        CAxis.AXIS_POSITIVE_X,
        CAxis.AXIS_POSITIVE_Y,
        CAxis.AXIS_NEGATIVE_Z),
      SMFFaceWindingOrder.FACE_WINDING_ORDER_COUNTER_CLOCKWISE));
    final SMFHeader h = header_b.build();

    new StrictExpectations()
    {{
      events.onStart();
      events.onVersionReceived(SMFFormatVersion.of(1, 0));
      events.onHeaderParsed(h);

      events.onDataAttributeStart(attr_f1_16);
      events.onDataAttributeValueFloat1(0.0);
      events.onDataAttributeValueFloat1(0.0);
      events.onDataAttributeValueFloat1(0.0);
      events.onDataAttributeValueFloat1(0.0);
      events.onDataAttributeFinish(attr_f1_16);

      events.onDataAttributeStart(attr_f2_16);
      events.onDataAttributeValueFloat2(0.0, 1.0);
      events.onDataAttributeValueFloat2(0.0, 1.0);
      events.onDataAttributeValueFloat2(0.0, 1.0);
      events.onDataAttributeValueFloat2(0.0, 1.0);
      events.onDataAttributeFinish(attr_f2_16);

      events.onDataAttributeStart(attr_f3_16);
      events.onDataAttributeValueFloat3(0.0, 1.0, 2.0);
      events.onDataAttributeValueFloat3(0.0, 1.0, 2.0);
      events.onDataAttributeValueFloat3(0.0, 1.0, 2.0);
      events.onDataAttributeValueFloat3(0.0, 1.0, 2.0);
      events.onDataAttributeFinish(attr_f3_16);

      events.onDataAttributeStart(attr_f4_16);
      events.onDataAttributeValueFloat4(0.0, 1.0, 2.0, 3.0);
      events.onDataAttributeValueFloat4(0.0, 1.0, 2.0, 3.0);
      events.onDataAttributeValueFloat4(0.0, 1.0, 2.0, 3.0);
      events.onDataAttributeValueFloat4(0.0, 1.0, 2.0, 3.0);
      events.onDataAttributeFinish(attr_f4_16);

      events.onDataAttributeStart(attr_u1_16);
      events.onDataAttributeValueIntegerUnsigned1(0L);
      events.onDataAttributeValueIntegerUnsigned1(0L);
      events.onDataAttributeValueIntegerUnsigned1(0L);
      events.onDataAttributeValueIntegerUnsigned1(0L);
      events.onDataAttributeFinish(attr_u1_16);

      events.onDataAttributeStart(attr_u2_16);
      events.onDataAttributeValueIntegerUnsigned2(0L, 1L);
      events.onDataAttributeValueIntegerUnsigned2(0L, 1L);
      events.onDataAttributeValueIntegerUnsigned2(0L, 1L);
      events.onDataAttributeValueIntegerUnsigned2(0L, 1L);
      events.onDataAttributeFinish(attr_u2_16);

      events.onDataAttributeStart(attr_u3_16);
      events.onDataAttributeValueIntegerUnsigned3(0L, 1L, 2L);
      events.onDataAttributeValueIntegerUnsigned3(0L, 1L, 2L);
      events.onDataAttributeValueIntegerUnsigned3(0L, 1L, 2L);
      events.onDataAttributeValueIntegerUnsigned3(0L, 1L, 2L);
      events.onDataAttributeFinish(attr_u3_16);

      events.onDataAttributeStart(attr_u4_16);
      events.onDataAttributeValueIntegerUnsigned4(0L, 1L, 2L, 3L);
      events.onDataAttributeValueIntegerUnsigned4(0L, 1L, 2L, 3L);
      events.onDataAttributeValueIntegerUnsigned4(0L, 1L, 2L, 3L);
      events.onDataAttributeValueIntegerUnsigned4(0L, 1L, 2L, 3L);
      events.onDataAttributeFinish(attr_u4_16);

      events.onDataAttributeStart(attr_s1_16);
      events.onDataAttributeValueIntegerSigned1(0L);
      events.onDataAttributeValueIntegerSigned1(0L);
      events.onDataAttributeValueIntegerSigned1(0L);
      events.onDataAttributeValueIntegerSigned1(0L);
      events.onDataAttributeFinish(attr_s1_16);

      events.onDataAttributeStart(attr_s2_16);
      events.onDataAttributeValueIntegerSigned2(0L, 1L);
      events.onDataAttributeValueIntegerSigned2(0L, 1L);
      events.onDataAttributeValueIntegerSigned2(0L, 1L);
      events.onDataAttributeValueIntegerSigned2(0L, 1L);
      events.onDataAttributeFinish(attr_s2_16);

      events.onDataAttributeStart(attr_s3_16);
      events.onDataAttributeValueIntegerSigned3(0L, 1L, 2L);
      events.onDataAttributeValueIntegerSigned3(0L, 1L, 2L);
      events.onDataAttributeValueIntegerSigned3(0L, 1L, 2L);
      events.onDataAttributeValueIntegerSigned3(0L, 1L, 2L);
      events.onDataAttributeFinish(attr_s3_16);

      events.onDataAttributeStart(attr_s4_16);
      events.onDataAttributeValueIntegerSigned4(0L, 1L, 2L, 3L);
      events.onDataAttributeValueIntegerSigned4(0L, 1L, 2L, 3L);
      events.onDataAttributeValueIntegerSigned4(0L, 1L, 2L, 3L);
      events.onDataAttributeValueIntegerSigned4(0L, 1L, 2L, 3L);
      events.onDataAttributeFinish(attr_s4_16);

      events.onDataTrianglesStart();
      events.onDataTriangle(0L, 1L, 2L);
      events.onDataTriangle(0L, 2L, 3L);
      events.onDataTrianglesFinish();

      events.onMeta(0x696F376DL, 0x0A0B0C0DL, 256L);
      this.result = Boolean.TRUE;
      events.onMetaData(0x696F376DL, 0x0A0B0C0DL, bytes);

      events.onFinish();
    }};

    runForText(events, true, s);
  }

  @Test
  public void testBadHeaderCommand(
    final @Mocked SMFParserEventsType events)
  {
    final StringBuilder s = new StringBuilder(128);
    s.append("smf 1 0");
    s.append(System.lineSeparator());
    s.append("what 4");
    s.append(System.lineSeparator());

    new StrictExpectations()
    {{
      events.onStart();
      events.onVersionReceived(SMFFormatVersion.of(1, 0));
      events.onError(this.withArgThat(
        new ParseErrorMessageStartsWith("Unrecognized command")));
      events.onFinish();
    }};

    runForText(events, false, s);
  }

  @Test
  public void testBadAttributeCollision(
    final @Mocked SMFParserEventsType events)
  {
    final StringBuilder s = new StringBuilder(128);
    s.append("smf 1 0");
    s.append(System.lineSeparator());
    s.append("vertices 4");
    s.append(System.lineSeparator());
    s.append("triangles 2 16");
    s.append(System.lineSeparator());
    s.append("attribute x float 4 32");
    s.append(System.lineSeparator());
    s.append("attribute x float 4 32");
    s.append(System.lineSeparator());
    s.append("coordinates +x +y -z counter-clockwise");
    s.append(System.lineSeparator());
    s.append("data");
    s.append(System.lineSeparator());

    new StrictExpectations()
    {{
      events.onStart();
      events.onVersionReceived(SMFFormatVersion.of(1, 0));
      events.onError(this.withArgThat(
        new ParseErrorMessageStartsWith("Duplicate attribute name: x")));
      events.onFinish();
    }};

    runForText(events, false, s);
  }

  @Test
  public void testBadDataTooFewTriangles(
    final @Mocked SMFParserEventsType events)
  {
    final StringBuilder s = new StringBuilder(128);
    s.append("smf 1 0");
    s.append(System.lineSeparator());
    s.append("schema 696F376D A0B0C0D0 1 2");
    s.append(System.lineSeparator());
    s.append("coordinates +x +y -z counter-clockwise");
    s.append(System.lineSeparator());
    s.append("attribute a integer-unsigned 4 32");
    s.append(System.lineSeparator());
    s.append("vertices 3");
    s.append(System.lineSeparator());
    s.append("triangles 1 16");
    s.append(System.lineSeparator());
    s.append("data");
    s.append(System.lineSeparator());
    s.append("attribute a");
    s.append(System.lineSeparator());
    s.append("0 0 0 0");
    s.append(System.lineSeparator());
    s.append("0 0 0 0");
    s.append(System.lineSeparator());
    s.append("0 0 0 0");
    s.append(System.lineSeparator());

    final SMFAttribute attribute = SMFAttribute.of(
      SMFAttributeName.of("a"),
      SMFComponentType.ELEMENT_TYPE_INTEGER_UNSIGNED,
      4,
      32);

    final SMFHeader.Builder header_b = baseHeader(List.of(attribute));
    header_b.setTriangles(baseTriangles().withTriangleCount(1L));
    header_b.setVertexCount(3L);
    final SMFHeader h = header_b.build();

    new StrictExpectations()
    {{
      events.onStart();
      events.onVersionReceived(SMFFormatVersion.of(1, 0));
      events.onHeaderParsed(h);
      events.onDataAttributeStart(attribute);
      events.onDataAttributeValueIntegerUnsigned4(0L, 0L, 0L, 0L);
      events.onDataAttributeValueIntegerUnsigned4(0L, 0L, 0L, 0L);
      events.onDataAttributeValueIntegerUnsigned4(0L, 0L, 0L, 0L);
      events.onDataAttributeFinish(attribute);
      events.onError(this.withArgThat(
        new ParseErrorMessageStartsWith("Too few triangles specified")));
      events.onError(this.withArgThat(
        new ParseErrorMessageStartsWith("Unexpected EOF")));
      events.onFinish();
    }};

    runForText(events, true, s);
  }

  @Test
  public void testBadDataTooFewAttributes(
    final @Mocked SMFParserEventsType events)
  {
    final StringBuilder s = new StringBuilder(128);
    s.append("smf 1 0");
    s.append(System.lineSeparator());
    s.append("schema 696F376D A0B0C0D0 1 2");
    s.append(System.lineSeparator());
    s.append("coordinates +x +y -z counter-clockwise");
    s.append(System.lineSeparator());
    s.append("attribute a integer-unsigned 4 32");
    s.append(System.lineSeparator());
    s.append("vertices 3");
    s.append(System.lineSeparator());
    s.append("triangles 1 16");
    s.append(System.lineSeparator());
    s.append("data");
    s.append(System.lineSeparator());
    s.append("triangles");
    s.append(System.lineSeparator());
    s.append("0 1 2");
    s.append(System.lineSeparator());

    final SMFAttribute attribute = SMFAttribute.of(
      SMFAttributeName.of("a"),
      SMFComponentType.ELEMENT_TYPE_INTEGER_UNSIGNED,
      4,
      32);

    final SMFHeader.Builder header_b = baseHeader(List.of(attribute));
    header_b.setTriangles(baseTriangles().withTriangleCount(1L));
    header_b.setVertexCount(3L);
    final SMFHeader h = header_b.build();

    new StrictExpectations()
    {{
      events.onStart();
      events.onVersionReceived(SMFFormatVersion.of(1, 0));
      events.onHeaderParsed(h);
      events.onDataTrianglesStart();
      events.onDataTriangle(0L, 1L, 2L);
      events.onDataTrianglesFinish();
      events.onError(this.withArgThat(
        new ParseErrorMessageStartsWith("No data specified for attribute: a")));
      events.onError(this.withArgThat(
        new ParseErrorMessageStartsWith("Unexpected EOF")));
      events.onFinish();
    }};

    runForText(events, true, s);
  }

  @Test
  public void testBadDataTriangleMalformed(
    final @Mocked SMFParserEventsType events)
  {
    final StringBuilder s = new StringBuilder(128);
    s.append("smf 1 0");
    s.append(System.lineSeparator());
    s.append("schema 696F376D A0B0C0D0 1 2");
    s.append(System.lineSeparator());
    s.append("coordinates +x +y -z counter-clockwise");
    s.append(System.lineSeparator());
    s.append("attribute a integer-unsigned 4 32");
    s.append(System.lineSeparator());
    s.append("vertices 3");
    s.append(System.lineSeparator());
    s.append("triangles 1 16");
    s.append(System.lineSeparator());
    s.append("data");
    s.append(System.lineSeparator());
    s.append("triangles");
    s.append(System.lineSeparator());
    s.append("0 1");
    s.append(System.lineSeparator());

    final SMFAttribute attribute = SMFAttribute.of(
      SMFAttributeName.of("a"),
      SMFComponentType.ELEMENT_TYPE_INTEGER_UNSIGNED,
      4,
      32);

    final SMFHeader.Builder header_b = baseHeader(List.of(attribute));
    header_b.setTriangles(baseTriangles().withTriangleCount(1L));
    header_b.setVertexCount(3L);
    final SMFHeader h = header_b.build();

    new StrictExpectations()
    {{
      events.onStart();
      events.onVersionReceived(SMFFormatVersion.of(1, 0));
      events.onHeaderParsed(h);
      events.onDataTrianglesStart();
      events.onError(this.withArgThat(
        new ParseErrorMessageStartsWith("Incorrect number of arguments")));
      events.onDataTrianglesFinish();
      events.onError(this.withArgThat(
        new ParseErrorMessageStartsWith("No data specified for attribute: a")));
      events.onError(this.withArgThat(
        new ParseErrorMessageStartsWith("Too few triangles specified")));
      events.onError(this.withArgThat(
        new ParseErrorMessageStartsWith("Unexpected EOF")));
      events.onFinish();
    }};

    runForText(events, true, s);
  }

  @Test
  public void testBadDataTriangleMalformedNonNumeric(
    final @Mocked SMFParserEventsType events)
  {
    final StringBuilder s = new StringBuilder(128);
    s.append("smf 1 0");
    s.append(System.lineSeparator());
    s.append("schema 696F376D A0B0C0D0 1 2");
    s.append(System.lineSeparator());
    s.append("coordinates +x +y -z counter-clockwise");
    s.append(System.lineSeparator());
    s.append("attribute a integer-unsigned 4 32");
    s.append(System.lineSeparator());
    s.append("vertices 3");
    s.append(System.lineSeparator());
    s.append("triangles 1 16");
    s.append(System.lineSeparator());
    s.append("data");
    s.append(System.lineSeparator());
    s.append("triangles");
    s.append(System.lineSeparator());
    s.append("0 1 x");
    s.append(System.lineSeparator());

    final SMFAttribute attribute = SMFAttribute.of(
      SMFAttributeName.of("a"),
      SMFComponentType.ELEMENT_TYPE_INTEGER_UNSIGNED,
      4,
      32);

    final SMFHeader.Builder header_b = baseHeader(List.of(attribute));
    header_b.setTriangles(baseTriangles().withTriangleCount(1L));
    header_b.setVertexCount(3L);
    final SMFHeader h = header_b.build();

    new StrictExpectations()
    {{
      events.onStart();
      events.onVersionReceived(SMFFormatVersion.of(1, 0));
      events.onHeaderParsed(h);
      events.onDataTrianglesStart();
      events.onError(this.withArgThat(
        new ParseErrorMessageStartsWith("Cannot parse number")));
      events.onDataTrianglesFinish();
      events.onError(this.withArgThat(
        new ParseErrorMessageStartsWith("No data specified for attribute: a")));
      events.onError(this.withArgThat(
        new ParseErrorMessageStartsWith("Too few triangles specified")));
      events.onError(this.withArgThat(
        new ParseErrorMessageStartsWith("Unexpected EOF")));
      events.onFinish();
    }};

    runForText(events, true, s);
  }

  @Test
  public void testBadDataTrianglesWrong(
    final @Mocked SMFParserEventsType events)
  {
    final StringBuilder s = new StringBuilder(128);
    s.append("smf 1 0");
    s.append(System.lineSeparator());
    s.append("schema 696F376D A0B0C0D0 1 2");
    s.append(System.lineSeparator());
    s.append("coordinates +x +y -z counter-clockwise");
    s.append(System.lineSeparator());
    s.append("attribute a integer-unsigned 4 32");
    s.append(System.lineSeparator());
    s.append("vertices 3");
    s.append(System.lineSeparator());
    s.append("triangles 1 16");
    s.append(System.lineSeparator());
    s.append("data");
    s.append(System.lineSeparator());
    s.append("triangles x");
    s.append(System.lineSeparator());

    final SMFAttribute attribute = SMFAttribute.of(
      SMFAttributeName.of("a"),
      SMFComponentType.ELEMENT_TYPE_INTEGER_UNSIGNED,
      4,
      32);

    final SMFHeader.Builder header_b = baseHeader(List.of(attribute));
    header_b.setTriangles(baseTriangles().withTriangleCount(1L));
    header_b.setVertexCount(3L);
    final SMFHeader h = header_b.build();

    new StrictExpectations()
    {{
      events.onStart();
      events.onVersionReceived(SMFFormatVersion.of(1, 0));
      events.onHeaderParsed(h);
      events.onError(this.withArgThat(
        new ParseErrorMessageStartsWith("Incorrect number of arguments")));
      events.onFinish();
    }};

    runForText(events, true, s);
  }

  @Test
  public void testBadDataAttributeWrong(
    final @Mocked SMFParserEventsType events)
  {
    final StringBuilder s = new StringBuilder(128);
    s.append("smf 1 0");
    s.append(System.lineSeparator());
    s.append("schema 696F376D A0B0C0D0 1 2");
    s.append(System.lineSeparator());
    s.append("coordinates +x +y -z counter-clockwise");
    s.append(System.lineSeparator());
    s.append("attribute a integer-unsigned 4 32");
    s.append(System.lineSeparator());
    s.append("vertices 3");
    s.append(System.lineSeparator());
    s.append("triangles 1 16");
    s.append(System.lineSeparator());
    s.append("data");
    s.append(System.lineSeparator());
    s.append("attribute");
    s.append(System.lineSeparator());

    final SMFAttribute attribute = SMFAttribute.of(
      SMFAttributeName.of("a"),
      SMFComponentType.ELEMENT_TYPE_INTEGER_UNSIGNED,
      4,
      32);

    final SMFHeader.Builder header_b = baseHeader(List.of(attribute));
    header_b.setTriangles(baseTriangles().withTriangleCount(1L));
    header_b.setVertexCount(3L);
    final SMFHeader h = header_b.build();

    new StrictExpectations()
    {{
      events.onStart();
      events.onVersionReceived(SMFFormatVersion.of(1, 0));
      events.onHeaderParsed(h);
      events.onError(this.withArgThat(
        new ParseErrorMessageStartsWith("Incorrect number of arguments")));
      events.onFinish();
    }};

    runForText(events, true, s);
  }

  @Test
  public void testBadDataAttributeName(
    final @Mocked SMFParserEventsType events)
  {
    final StringBuilder s = new StringBuilder(128);
    s.append("smf 1 0");
    s.append(System.lineSeparator());
    s.append("schema 696F376D A0B0C0D0 1 2");
    s.append(System.lineSeparator());
    s.append("coordinates +x +y -z counter-clockwise");
    s.append(System.lineSeparator());
    s.append("attribute a integer-unsigned 4 32");
    s.append(System.lineSeparator());
    s.append("vertices 3");
    s.append(System.lineSeparator());
    s.append("triangles 1 16");
    s.append(System.lineSeparator());
    s.append("data");
    s.append(System.lineSeparator());
    s.append("attribute ");
    for (int index = 0; index < 65; ++index) {
      s.append("x");
    }
    s.append(System.lineSeparator());

    final SMFAttribute attribute = SMFAttribute.of(
      SMFAttributeName.of("a"),
      SMFComponentType.ELEMENT_TYPE_INTEGER_UNSIGNED,
      4,
      32);

    final SMFHeader.Builder header_b = baseHeader(List.of(attribute));
    header_b.setTriangles(baseTriangles().withTriangleCount(1L));
    header_b.setVertexCount(3L);
    final SMFHeader h = header_b.build();

    new StrictExpectations()
    {{
      events.onStart();
      events.onVersionReceived(SMFFormatVersion.of(1, 0));
      events.onHeaderParsed(h);
      events.onError(this.withArgThat(
        new ParseErrorMessageStartsWith(
          "Attribute names must match the pattern")));
      events.onFinish();
    }};

    runForText(events, true, s);
  }

  @Test
  public void testBadDataAttributeMissedAttribute(
    final @Mocked SMFParserEventsType events)
  {
    final StringBuilder s = new StringBuilder(128);
    s.append("smf 1 0");
    s.append(System.lineSeparator());
    s.append("schema 696F376D A0B0C0D0 1 2");
    s.append(System.lineSeparator());
    s.append("attribute a integer-unsigned 4 32");
    s.append(System.lineSeparator());
    s.append("vertices 3");
    s.append(System.lineSeparator());
    s.append("triangles 1 16");
    s.append(System.lineSeparator());
    s.append("coordinates +x +y -z counter-clockwise");
    s.append(System.lineSeparator());
    s.append("data");
    s.append(System.lineSeparator());
    s.append("triangles");
    s.append(System.lineSeparator());
    s.append("0 1 2");
    s.append(System.lineSeparator());

    final SMFAttribute attribute = SMFAttribute.of(
      SMFAttributeName.of("a"),
      SMFComponentType.ELEMENT_TYPE_INTEGER_UNSIGNED,
      4,
      32);

    final SMFHeader.Builder header_b = baseHeader(List.of(attribute));
    header_b.setTriangles(baseTriangles().withTriangleCount(1L));
    header_b.setVertexCount(3L);
    final SMFHeader h = header_b.build();

    new StrictExpectations()
    {{
      events.onStart();
      events.onVersionReceived(SMFFormatVersion.of(1, 0));
      events.onHeaderParsed(h);
      events.onDataTrianglesStart();
      events.onDataTriangle(0L, 1L, 2L);
      events.onDataTrianglesFinish();
      events.onError(this.withArgThat(
        new ParseErrorMessageStartsWith("No data specified for attribute: a")));
      events.onError(this.withArgThat(
        new ParseErrorMessageStartsWith("Unexpected EOF")));
      events.onFinish();
    }};

    runForText(events, true, s);
  }

  @Test
  public void testBadDataAttributeTwiceAttribute(
    final @Mocked SMFParserEventsType events)
  {
    final StringBuilder s = new StringBuilder(128);
    s.append("smf 1 0");
    s.append(System.lineSeparator());
    s.append("schema 696F376D A0B0C0D0 1 2");
    s.append(System.lineSeparator());
    s.append("coordinates +x +y -z counter-clockwise");
    s.append(System.lineSeparator());
    s.append("attribute a integer-unsigned 4 32");
    s.append(System.lineSeparator());
    s.append("vertices 3");
    s.append(System.lineSeparator());
    s.append("triangles 1 16");
    s.append(System.lineSeparator());
    s.append("data");
    s.append(System.lineSeparator());
    s.append("triangles");
    s.append(System.lineSeparator());
    s.append("0 1 2");
    s.append(System.lineSeparator());
    s.append("attribute a");
    s.append(System.lineSeparator());
    s.append("0 1 2 3");
    s.append(System.lineSeparator());
    s.append("0 1 2 3");
    s.append(System.lineSeparator());
    s.append("0 1 2 3");
    s.append(System.lineSeparator());
    s.append("attribute a");
    s.append(System.lineSeparator());

    final SMFAttribute attribute = SMFAttribute.of(
      SMFAttributeName.of("a"),
      SMFComponentType.ELEMENT_TYPE_INTEGER_UNSIGNED,
      4,
      32);

    final SMFHeader.Builder header_b = baseHeader(List.of(attribute));
    header_b.setTriangles(baseTriangles().withTriangleCount(1L));
    header_b.setVertexCount(3L);
    final SMFHeader h = header_b.build();

    new StrictExpectations()
    {{
      events.onStart();
      events.onVersionReceived(SMFFormatVersion.of(1, 0));
      events.onHeaderParsed(h);
      events.onDataTrianglesStart();
      events.onDataTriangle(0L, 1L, 2L);
      events.onDataTrianglesFinish();
      events.onDataAttributeStart(attribute);
      events.onDataAttributeValueIntegerUnsigned4(0L, 1L, 2L, 3L);
      events.onDataAttributeValueIntegerUnsigned4(0L, 1L, 2L, 3L);
      events.onDataAttributeValueIntegerUnsigned4(0L, 1L, 2L, 3L);
      events.onDataAttributeFinish(attribute);
      events.onError(this.withArgThat(
        new ParseErrorMessageStartsWith(
          "An attempt has already been made to supply data for attribute a")));
      events.onFinish();
    }};

    runForText(events, true, s);
  }

  @Test
  public void testBadDataNonsense(
    final @Mocked SMFParserEventsType events)
  {
    final StringBuilder s = new StringBuilder(128);
    s.append("smf 1 0");
    s.append(System.lineSeparator());
    s.append("schema 696F376D A0B0C0D0 1 2");
    s.append(System.lineSeparator());
    s.append("coordinates +x +y -z counter-clockwise");
    s.append(System.lineSeparator());
    s.append("attribute a integer-unsigned 4 32");
    s.append(System.lineSeparator());
    s.append("vertices 3");
    s.append(System.lineSeparator());
    s.append("triangles 1 16");
    s.append(System.lineSeparator());
    s.append("data");
    s.append(System.lineSeparator());
    s.append("nonsense");
    s.append(System.lineSeparator());

    final SMFAttribute attribute = SMFAttribute.of(
      SMFAttributeName.of("a"),
      SMFComponentType.ELEMENT_TYPE_INTEGER_UNSIGNED,
      4,
      32);

    final SMFHeader.Builder header_b = baseHeader(List.of(attribute));
    header_b.setTriangles(baseTriangles().withTriangleCount(1L));
    header_b.setVertexCount(3L);
    final SMFHeader h = header_b.build();

    new StrictExpectations()
    {{
      events.onStart();
      events.onVersionReceived(SMFFormatVersion.of(1, 0));
      events.onHeaderParsed(h);
      events.onError(this.withArgThat(
        new ParseErrorMessageStartsWith("Unrecognized command")));
      events.onFinish();
    }};

    runForText(events, true, s);
  }

  @Test
  public void testBadDataAttributeValuesWrongFloat4(
    final @Mocked SMFParserEventsType events)
  {
    final StringBuilder s = new StringBuilder(128);
    s.append("smf 1 0");
    s.append(System.lineSeparator());
    s.append("schema 696F376D A0B0C0D0 1 2");
    s.append(System.lineSeparator());
    s.append("coordinates +x +y -z counter-clockwise");
    s.append(System.lineSeparator());
    s.append("attribute a float 4 32");
    s.append(System.lineSeparator());
    s.append("vertices 2");
    s.append(System.lineSeparator());
    s.append("triangles 1 16");
    s.append(System.lineSeparator());
    s.append("data");
    s.append(System.lineSeparator());
    s.append("triangles");
    s.append(System.lineSeparator());
    s.append("0 0 0");
    s.append(System.lineSeparator());
    s.append("attribute a");
    s.append(System.lineSeparator());
    s.append("0 0 0");
    s.append(System.lineSeparator());
    s.append("x 0 0 0");
    s.append(System.lineSeparator());

    final SMFAttribute attribute = SMFAttribute.of(
      SMFAttributeName.of("a"),
      SMFComponentType.ELEMENT_TYPE_FLOATING,
      4,
      32);

    final SMFHeader.Builder header_b = baseHeader(List.of(attribute));
    header_b.setTriangles(baseTriangles().withTriangleCount(1L));
    header_b.setVertexCount(2L);
    final SMFHeader h = header_b.build();

    new StrictExpectations()
    {{
      events.onStart();
      events.onVersionReceived(SMFFormatVersion.of(1, 0));
      events.onHeaderParsed(h);
      events.onDataTrianglesStart();
      events.onDataTriangle(0L, 0L, 0L);
      events.onDataTrianglesFinish();
      events.onDataAttributeStart(attribute);
      events.onError(this.withArgThat(
        new ParseErrorMessageStartsWith("Incorrect number of arguments")));
      events.onError(this.withArgThat(
        new ParseErrorMessageStartsWith("Cannot parse number")));
      events.onDataAttributeFinish(attribute);
      events.onFinish();
    }};

    runForText(events, true, s);
  }

  @Test
  public void testBadDataAttributeValuesWrongFloat3(
    final @Mocked SMFParserEventsType events)
  {
    final StringBuilder s = new StringBuilder(128);
    s.append("smf 1 0");
    s.append(System.lineSeparator());
    s.append("schema 696F376D A0B0C0D0 1 2");
    s.append(System.lineSeparator());
    s.append("coordinates +x +y -z counter-clockwise");
    s.append(System.lineSeparator());
    s.append("attribute a float 3 32");
    s.append(System.lineSeparator());
    s.append("vertices 2");
    s.append(System.lineSeparator());
    s.append("triangles 1 16");
    s.append(System.lineSeparator());
    s.append("data");
    s.append(System.lineSeparator());
    s.append("triangles");
    s.append(System.lineSeparator());
    s.append("0 0 0");
    s.append(System.lineSeparator());
    s.append("attribute a");
    s.append(System.lineSeparator());
    s.append("0 0");
    s.append(System.lineSeparator());
    s.append("x 0 0");
    s.append(System.lineSeparator());

    final SMFAttribute attribute = SMFAttribute.of(
      SMFAttributeName.of("a"),
      SMFComponentType.ELEMENT_TYPE_FLOATING,
      3,
      32);

    final SMFHeader.Builder header_b = baseHeader(List.of(attribute));
    header_b.setTriangles(baseTriangles().withTriangleCount(1L));
    header_b.setVertexCount(2L);
    final SMFHeader h = header_b.build();

    new StrictExpectations()
    {{
      events.onStart();
      events.onVersionReceived(SMFFormatVersion.of(1, 0));
      events.onHeaderParsed(h);
      events.onDataTrianglesStart();
      events.onDataTriangle(0L, 0L, 0L);
      events.onDataTrianglesFinish();
      events.onDataAttributeStart(attribute);
      events.onError(this.withArgThat(
        new ParseErrorMessageStartsWith("Incorrect number of arguments")));
      events.onError(this.withArgThat(
        new ParseErrorMessageStartsWith("Cannot parse number")));
      events.onDataAttributeFinish(attribute);
      events.onFinish();
    }};

    runForText(events, true, s);
  }

  @Test
  public void testBadDataAttributeValuesWrongFloat2(
    final @Mocked SMFParserEventsType events)
  {
    final StringBuilder s = new StringBuilder(128);
    s.append("smf 1 0");
    s.append(System.lineSeparator());
    s.append("schema 696F376D A0B0C0D0 1 2");
    s.append(System.lineSeparator());
    s.append("coordinates +x +y -z counter-clockwise");
    s.append(System.lineSeparator());
    s.append("attribute a float 2 32");
    s.append(System.lineSeparator());
    s.append("vertices 2");
    s.append(System.lineSeparator());
    s.append("triangles 1 16");
    s.append(System.lineSeparator());
    s.append("data");
    s.append(System.lineSeparator());
    s.append("triangles");
    s.append(System.lineSeparator());
    s.append("0 0 0");
    s.append(System.lineSeparator());
    s.append("attribute a");
    s.append(System.lineSeparator());
    s.append("0");
    s.append(System.lineSeparator());
    s.append("x 0");
    s.append(System.lineSeparator());

    final SMFAttribute attribute = SMFAttribute.of(
      SMFAttributeName.of("a"),
      SMFComponentType.ELEMENT_TYPE_FLOATING,
      2,
      32);

    final SMFHeader.Builder header_b = baseHeader(List.of(attribute));
    header_b.setTriangles(baseTriangles().withTriangleCount(1L));
    header_b.setVertexCount(2L);
    final SMFHeader h = header_b.build();

    new StrictExpectations()
    {{
      events.onStart();
      events.onVersionReceived(SMFFormatVersion.of(1, 0));
      events.onHeaderParsed(h);
      events.onDataTrianglesStart();
      events.onDataTriangle(0L, 0L, 0L);
      events.onDataTrianglesFinish();
      events.onDataAttributeStart(attribute);
      events.onError(this.withArgThat(
        new ParseErrorMessageStartsWith("Incorrect number of arguments")));
      events.onError(this.withArgThat(
        new ParseErrorMessageStartsWith("Cannot parse number")));
      events.onDataAttributeFinish(attribute);
      events.onFinish();
    }};

    runForText(events, true, s);
  }

  @Test
  public void testBadDataAttributeValuesWrongFloat1(
    final @Mocked SMFParserEventsType events)
  {
    final StringBuilder s = new StringBuilder(128);
    s.append("smf 1 0");
    s.append(System.lineSeparator());
    s.append("schema 696F376D A0B0C0D0 1 2");
    s.append(System.lineSeparator());
    s.append("coordinates +x +y -z counter-clockwise");
    s.append(System.lineSeparator());
    s.append("attribute a float 1 32");
    s.append(System.lineSeparator());
    s.append("vertices 2");
    s.append(System.lineSeparator());
    s.append("triangles 1 16");
    s.append(System.lineSeparator());
    s.append("data");
    s.append(System.lineSeparator());
    s.append("triangles");
    s.append(System.lineSeparator());
    s.append("0 0 0");
    s.append(System.lineSeparator());
    s.append("attribute a");
    s.append(System.lineSeparator());
    s.append("x");
    s.append(System.lineSeparator());
    s.append("0 1");
    s.append(System.lineSeparator());

    final SMFAttribute attribute = SMFAttribute.of(
      SMFAttributeName.of("a"),
      SMFComponentType.ELEMENT_TYPE_FLOATING,
      1,
      32);

    final SMFHeader.Builder header_b = baseHeader(List.of(attribute));
    header_b.setTriangles(baseTriangles().withTriangleCount(1L));
    header_b.setVertexCount(2L);
    final SMFHeader h = header_b.build();

    new StrictExpectations()
    {{
      events.onStart();
      events.onVersionReceived(SMFFormatVersion.of(1, 0));
      events.onHeaderParsed(h);
      events.onDataTrianglesStart();
      events.onDataTriangle(0L, 0L, 0L);
      events.onDataTrianglesFinish();
      events.onDataAttributeStart(attribute);
      events.onError(this.withArgThat(
        new ParseErrorMessageStartsWith("Cannot parse number")));
      events.onError(this.withArgThat(
        new ParseErrorMessageStartsWith("Incorrect number of arguments")));
      events.onDataAttributeFinish(attribute);
      events.onFinish();
    }};

    runForText(events, true, s);
  }

  @Test
  public void testBadDataAttributeValuesWrongIntegerSigned4(
    final @Mocked SMFParserEventsType events)
  {
    final StringBuilder s = new StringBuilder(128);
    s.append("smf 1 0");
    s.append(System.lineSeparator());
    s.append("schema 696F376D A0B0C0D0 1 2");
    s.append(System.lineSeparator());
    s.append("coordinates +x +y -z counter-clockwise");
    s.append(System.lineSeparator());
    s.append("attribute a integer-signed 4 32");
    s.append(System.lineSeparator());
    s.append("vertices 2");
    s.append(System.lineSeparator());
    s.append("triangles 1 16");
    s.append(System.lineSeparator());
    s.append("data");
    s.append(System.lineSeparator());
    s.append("triangles");
    s.append(System.lineSeparator());
    s.append("0 0 0");
    s.append(System.lineSeparator());
    s.append("attribute a");
    s.append(System.lineSeparator());
    s.append("0 0 0");
    s.append(System.lineSeparator());
    s.append("x 0 0 0");
    s.append(System.lineSeparator());

    final SMFAttribute attribute = SMFAttribute.of(
      SMFAttributeName.of("a"),
      SMFComponentType.ELEMENT_TYPE_INTEGER_SIGNED,
      4,
      32);

    final SMFHeader.Builder header_b = baseHeader(List.of(attribute));
    header_b.setTriangles(baseTriangles().withTriangleCount(1L));
    header_b.setVertexCount(2L);
    final SMFHeader h = header_b.build();

    new StrictExpectations()
    {{
      events.onStart();
      events.onVersionReceived(SMFFormatVersion.of(1, 0));
      events.onHeaderParsed(h);
      events.onDataTrianglesStart();
      events.onDataTriangle(0L, 0L, 0L);
      events.onDataTrianglesFinish();
      events.onDataAttributeStart(attribute);
      events.onError(this.withArgThat(
        new ParseErrorMessageStartsWith("Incorrect number of arguments")));
      events.onError(this.withArgThat(
        new ParseErrorMessageStartsWith("Cannot parse number")));
      events.onDataAttributeFinish(attribute);
      events.onFinish();
    }};

    runForText(events, true, s);
  }

  @Test
  public void testBadDataAttributeValuesWrongIntegerSigned3(
    final @Mocked SMFParserEventsType events)
  {
    final StringBuilder s = new StringBuilder(128);
    s.append("smf 1 0");
    s.append(System.lineSeparator());
    s.append("schema 696F376D A0B0C0D0 1 2");
    s.append(System.lineSeparator());
    s.append("coordinates +x +y -z counter-clockwise");
    s.append(System.lineSeparator());
    s.append("attribute a integer-signed 3 32");
    s.append(System.lineSeparator());
    s.append("vertices 2");
    s.append(System.lineSeparator());
    s.append("triangles 1 16");
    s.append(System.lineSeparator());
    s.append("data");
    s.append(System.lineSeparator());
    s.append("triangles");
    s.append(System.lineSeparator());
    s.append("0 0 0");
    s.append(System.lineSeparator());
    s.append("attribute a");
    s.append(System.lineSeparator());
    s.append("0 0");
    s.append(System.lineSeparator());
    s.append("x 0 0");
    s.append(System.lineSeparator());

    final SMFAttribute attribute = SMFAttribute.of(
      SMFAttributeName.of("a"),
      SMFComponentType.ELEMENT_TYPE_INTEGER_SIGNED,
      3,
      32);

    final SMFHeader.Builder header_b = baseHeader(List.of(attribute));
    header_b.setTriangles(baseTriangles().withTriangleCount(1L));
    header_b.setVertexCount(2L);
    final SMFHeader h = header_b.build();

    new StrictExpectations()
    {{
      events.onStart();
      events.onVersionReceived(SMFFormatVersion.of(1, 0));
      events.onHeaderParsed(h);
      events.onDataTrianglesStart();
      events.onDataTriangle(0L, 0L, 0L);
      events.onDataTrianglesFinish();
      events.onDataAttributeStart(attribute);
      events.onError(this.withArgThat(
        new ParseErrorMessageStartsWith("Incorrect number of arguments")));
      events.onError(this.withArgThat(
        new ParseErrorMessageStartsWith("Cannot parse number")));
      events.onDataAttributeFinish(attribute);
      events.onFinish();
    }};

    runForText(events, true, s);
  }

  @Test
  public void testBadDataAttributeValuesWrongIntegerSigned2(
    final @Mocked SMFParserEventsType events)
  {
    final StringBuilder s = new StringBuilder(128);
    s.append("smf 1 0");
    s.append(System.lineSeparator());
    s.append("schema 696F376D A0B0C0D0 1 2");
    s.append(System.lineSeparator());
    s.append("coordinates +x +y -z counter-clockwise");
    s.append(System.lineSeparator());
    s.append("attribute a integer-signed 2 32");
    s.append(System.lineSeparator());
    s.append("vertices 2");
    s.append(System.lineSeparator());
    s.append("triangles 1 16");
    s.append(System.lineSeparator());
    s.append("data");
    s.append(System.lineSeparator());
    s.append("triangles");
    s.append(System.lineSeparator());
    s.append("0 0 0");
    s.append(System.lineSeparator());
    s.append("attribute a");
    s.append(System.lineSeparator());
    s.append("0");
    s.append(System.lineSeparator());
    s.append("x 0");
    s.append(System.lineSeparator());

    final SMFAttribute attribute = SMFAttribute.of(
      SMFAttributeName.of("a"),
      SMFComponentType.ELEMENT_TYPE_INTEGER_SIGNED,
      2,
      32);

    final SMFHeader.Builder header_b = baseHeader(List.of(attribute));
    header_b.setTriangles(baseTriangles().withTriangleCount(1L));
    header_b.setVertexCount(2L);
    final SMFHeader h = header_b.build();

    new StrictExpectations()
    {{
      events.onStart();
      events.onVersionReceived(SMFFormatVersion.of(1, 0));
      events.onHeaderParsed(h);
      events.onDataTrianglesStart();
      events.onDataTriangle(0L, 0L, 0L);
      events.onDataTrianglesFinish();
      events.onDataAttributeStart(attribute);
      events.onError(this.withArgThat(
        new ParseErrorMessageStartsWith("Incorrect number of arguments")));
      events.onError(this.withArgThat(
        new ParseErrorMessageStartsWith("Cannot parse number")));
      events.onDataAttributeFinish(attribute);
      events.onFinish();
    }};

    runForText(events, true, s);
  }

  @Test
  public void testBadDataAttributeValuesWrongIntegerSigned1(
    final @Mocked SMFParserEventsType events)
  {
    final StringBuilder s = new StringBuilder(128);
    s.append("smf 1 0");
    s.append(System.lineSeparator());
    s.append("schema 696F376D A0B0C0D0 1 2");
    s.append(System.lineSeparator());
    s.append("coordinates +x +y -z counter-clockwise");
    s.append(System.lineSeparator());
    s.append("attribute a integer-signed 1 32");
    s.append(System.lineSeparator());
    s.append("vertices 2");
    s.append(System.lineSeparator());
    s.append("triangles 1 16");
    s.append(System.lineSeparator());
    s.append("data");
    s.append(System.lineSeparator());
    s.append("triangles");
    s.append(System.lineSeparator());
    s.append("0 0 0");
    s.append(System.lineSeparator());
    s.append("attribute a");
    s.append(System.lineSeparator());
    s.append("x");
    s.append(System.lineSeparator());
    s.append("0 0");
    s.append(System.lineSeparator());

    final SMFAttribute attribute = SMFAttribute.of(
      SMFAttributeName.of("a"),
      SMFComponentType.ELEMENT_TYPE_INTEGER_SIGNED,
      1,
      32);

    final SMFHeader.Builder header_b = baseHeader(List.of(attribute));
    header_b.setTriangles(baseTriangles().withTriangleCount(1L));
    header_b.setVertexCount(2L);
    final SMFHeader h = header_b.build();

    new StrictExpectations()
    {{
      events.onStart();
      events.onVersionReceived(SMFFormatVersion.of(1, 0));
      events.onHeaderParsed(h);
      events.onDataTrianglesStart();
      events.onDataTriangle(0L, 0L, 0L);
      events.onDataTrianglesFinish();
      events.onDataAttributeStart(attribute);
      events.onError(this.withArgThat(
        new ParseErrorMessageStartsWith("Cannot parse number")));
      events.onError(this.withArgThat(
        new ParseErrorMessageStartsWith("Incorrect number of arguments")));
      events.onDataAttributeFinish(attribute);
      events.onFinish();
    }};

    runForText(events, true, s);
  }

  @Test
  public void testBadDataAttributeValuesWrongIntegerUnsigned4(
    final @Mocked SMFParserEventsType events)
  {
    final StringBuilder s = new StringBuilder(128);
    s.append("smf 1 0");
    s.append(System.lineSeparator());
    s.append("schema 696F376D A0B0C0D0 1 2");
    s.append(System.lineSeparator());
    s.append("coordinates +x +y -z counter-clockwise");
    s.append(System.lineSeparator());
    s.append("attribute a integer-unsigned 4 32");
    s.append(System.lineSeparator());
    s.append("vertices 2");
    s.append(System.lineSeparator());
    s.append("triangles 1 16");
    s.append(System.lineSeparator());
    s.append("data");
    s.append(System.lineSeparator());
    s.append("triangles");
    s.append(System.lineSeparator());
    s.append("0 0 0");
    s.append(System.lineSeparator());
    s.append("attribute a");
    s.append(System.lineSeparator());
    s.append("0 0 0");
    s.append(System.lineSeparator());
    s.append("x 0 0 0");
    s.append(System.lineSeparator());

    final SMFAttribute attribute = SMFAttribute.of(
      SMFAttributeName.of("a"),
      SMFComponentType.ELEMENT_TYPE_INTEGER_UNSIGNED,
      4,
      32);

    final SMFHeader.Builder header_b = baseHeader(List.of(attribute));
    header_b.setTriangles(baseTriangles().withTriangleCount(1L));
    header_b.setVertexCount(2L);
    final SMFHeader h = header_b.build();

    new StrictExpectations()
    {{
      events.onStart();
      events.onVersionReceived(SMFFormatVersion.of(1, 0));
      events.onHeaderParsed(h);
      events.onDataTrianglesStart();
      events.onDataTriangle(0L, 0L, 0L);
      events.onDataTrianglesFinish();
      events.onDataAttributeStart(attribute);
      events.onError(this.withArgThat(
        new ParseErrorMessageStartsWith("Incorrect number of arguments")));
      events.onError(this.withArgThat(
        new ParseErrorMessageStartsWith("Cannot parse number")));
      events.onDataAttributeFinish(attribute);
      events.onFinish();
    }};

    runForText(events, true, s);
  }

  @Test
  public void testBadDataAttributeValuesWrongIntegerUnsigned3(
    final @Mocked SMFParserEventsType events)
  {
    final StringBuilder s = new StringBuilder(128);
    s.append("smf 1 0");
    s.append(System.lineSeparator());
    s.append("schema 696F376D A0B0C0D0 1 2");
    s.append(System.lineSeparator());
    s.append("coordinates +x +y -z counter-clockwise");
    s.append(System.lineSeparator());
    s.append("attribute a integer-unsigned 3 32");
    s.append(System.lineSeparator());
    s.append("vertices 2");
    s.append(System.lineSeparator());
    s.append("triangles 1 16");
    s.append(System.lineSeparator());
    s.append("data");
    s.append(System.lineSeparator());
    s.append("triangles");
    s.append(System.lineSeparator());
    s.append("0 0 0");
    s.append(System.lineSeparator());
    s.append("attribute a");
    s.append(System.lineSeparator());
    s.append("0 0");
    s.append(System.lineSeparator());
    s.append("x 0 0");
    s.append(System.lineSeparator());

    final SMFAttribute attribute = SMFAttribute.of(
      SMFAttributeName.of("a"),
      SMFComponentType.ELEMENT_TYPE_INTEGER_UNSIGNED,
      3,
      32);

    final SMFHeader.Builder header_b = baseHeader(List.of(attribute));
    header_b.setTriangles(baseTriangles().withTriangleCount(1L));
    header_b.setVertexCount(2L);
    final SMFHeader h = header_b.build();

    new StrictExpectations()
    {{
      events.onStart();
      events.onVersionReceived(SMFFormatVersion.of(1, 0));
      events.onHeaderParsed(h);
      events.onDataTrianglesStart();
      events.onDataTriangle(0L, 0L, 0L);
      events.onDataTrianglesFinish();
      events.onDataAttributeStart(attribute);
      events.onError(this.withArgThat(
        new ParseErrorMessageStartsWith("Incorrect number of arguments")));
      events.onError(this.withArgThat(
        new ParseErrorMessageStartsWith("Cannot parse number")));
      events.onDataAttributeFinish(attribute);
      events.onFinish();
    }};

    runForText(events, true, s);
  }

  @Test
  public void testBadDataAttributeValuesWrongIntegerUnsigned2(
    final @Mocked SMFParserEventsType events)
  {
    final StringBuilder s = new StringBuilder(128);
    s.append("smf 1 0");
    s.append(System.lineSeparator());
    s.append("schema 696F376D A0B0C0D0 1 2");
    s.append(System.lineSeparator());
    s.append("coordinates +x +y -z counter-clockwise");
    s.append(System.lineSeparator());
    s.append("attribute a integer-unsigned 2 32");
    s.append(System.lineSeparator());
    s.append("vertices 2");
    s.append(System.lineSeparator());
    s.append("triangles 1 16");
    s.append(System.lineSeparator());
    s.append("data");
    s.append(System.lineSeparator());
    s.append("triangles");
    s.append(System.lineSeparator());
    s.append("0 0 0");
    s.append(System.lineSeparator());
    s.append("attribute a");
    s.append(System.lineSeparator());
    s.append("0");
    s.append(System.lineSeparator());
    s.append("x 0");
    s.append(System.lineSeparator());

    final SMFAttribute attribute = SMFAttribute.of(
      SMFAttributeName.of("a"),
      SMFComponentType.ELEMENT_TYPE_INTEGER_UNSIGNED,
      2,
      32);

    final SMFHeader.Builder header_b = baseHeader(List.of(attribute));
    header_b.setTriangles(baseTriangles().withTriangleCount(1L));
    header_b.setVertexCount(2L);
    final SMFHeader h = header_b.build();

    new StrictExpectations()
    {{
      events.onStart();
      events.onVersionReceived(SMFFormatVersion.of(1, 0));
      events.onHeaderParsed(h);
      events.onDataTrianglesStart();
      events.onDataTriangle(0L, 0L, 0L);
      events.onDataTrianglesFinish();
      events.onDataAttributeStart(attribute);
      events.onError(this.withArgThat(
        new ParseErrorMessageStartsWith("Incorrect number of arguments")));
      events.onError(this.withArgThat(
        new ParseErrorMessageStartsWith("Cannot parse number")));
      events.onDataAttributeFinish(attribute);
      events.onFinish();
    }};

    runForText(events, true, s);
  }

  @Test
  public void testBadDataAttributeValuesWrongIntegerUnsigned1(
    final @Mocked SMFParserEventsType events)
  {
    final StringBuilder s = new StringBuilder(128);
    s.append("smf 1 0");
    s.append(System.lineSeparator());
    s.append("schema 696F376D A0B0C0D0 1 2");
    s.append(System.lineSeparator());
    s.append("coordinates +x +y -z counter-clockwise");
    s.append(System.lineSeparator());
    s.append("attribute a integer-unsigned 1 32");
    s.append(System.lineSeparator());
    s.append("vertices 2");
    s.append(System.lineSeparator());
    s.append("triangles 1 16");
    s.append(System.lineSeparator());
    s.append("data");
    s.append(System.lineSeparator());
    s.append("triangles");
    s.append(System.lineSeparator());
    s.append("0 0 0");
    s.append(System.lineSeparator());
    s.append("attribute a");
    s.append(System.lineSeparator());
    s.append("x");
    s.append(System.lineSeparator());
    s.append("0 0");
    s.append(System.lineSeparator());

    final SMFAttribute attribute = SMFAttribute.of(
      SMFAttributeName.of("a"),
      SMFComponentType.ELEMENT_TYPE_INTEGER_UNSIGNED,
      1,
      32);

    final SMFHeader.Builder header_b = baseHeader(List.of(attribute));
    header_b.setTriangles(baseTriangles().withTriangleCount(1L));
    header_b.setVertexCount(2L);
    final SMFHeader h = header_b.build();

    new StrictExpectations()
    {{
      events.onStart();
      events.onVersionReceived(SMFFormatVersion.of(1, 0));
      events.onHeaderParsed(h);
      events.onDataTrianglesStart();
      events.onDataTriangle(0L, 0L, 0L);
      events.onDataTrianglesFinish();
      events.onDataAttributeStart(attribute);
      events.onError(this.withArgThat(
        new ParseErrorMessageStartsWith("Cannot parse number")));
      events.onError(this.withArgThat(
        new ParseErrorMessageStartsWith("Incorrect number of arguments")));
      events.onDataAttributeFinish(attribute);
      events.onFinish();
    }};

    runForText(events, true, s);
  }

  @Test
  public void testBadVendor0(
    final @Mocked SMFParserEventsType events)
  {
    final StringBuilder s = new StringBuilder(128);
    s.append("smf 1 0");
    s.append(System.lineSeparator());
    s.append("schema");
    s.append(System.lineSeparator());

    new StrictExpectations()
    {{
      events.onStart();
      events.onVersionReceived(SMFFormatVersion.of(1, 0));
      events.onError(this.withArgThat(
        new ParseErrorMessageStartsWith("Incorrect number of arguments")));
      events.onError(this.withArgThat(
        new ParseErrorMessageStartsWith("Unexpected EOF")));
      events.onFinish();
    }};

    runForText(events, false, s);
  }

  @Test
  public void testBadVendor1(
    final @Mocked SMFParserEventsType events)
  {
    final StringBuilder s = new StringBuilder(128);
    s.append("smf 1 0");
    s.append(System.lineSeparator());
    s.append("schema x 0 0 0");
    s.append(System.lineSeparator());

    new StrictExpectations()
    {{
      events.onStart();
      events.onVersionReceived(SMFFormatVersion.of(1, 0));
      events.onError(this.withArgThat(
        new ParseErrorMessageStartsWith("Could not parse number")));
      events.onError(this.withArgThat(
        new ParseErrorMessageStartsWith("Unexpected EOF")));
      events.onFinish();
    }};

    runForText(events, false, s);
  }

  @Test
  public void testBadVendor2(
    final @Mocked SMFParserEventsType events)
  {
    final StringBuilder s = new StringBuilder(128);
    s.append("smf 1 0");
    s.append(System.lineSeparator());
    s.append("schema 0 x 0 0");
    s.append(System.lineSeparator());

    new StrictExpectations()
    {{
      events.onStart();
      events.onVersionReceived(SMFFormatVersion.of(1, 0));
      events.onError(this.withArgThat(
        new ParseErrorMessageStartsWith("Could not parse number")));
      events.onError(this.withArgThat(
        new ParseErrorMessageStartsWith("Unexpected EOF")));
      events.onFinish();
    }};

    runForText(events, false, s);
  }

  @Test
  public void testBadVendor3(
    final @Mocked SMFParserEventsType events)
  {
    final StringBuilder s = new StringBuilder(128);
    s.append("smf 1 0");
    s.append(System.lineSeparator());
    s.append("schema 0 0 x 0");
    s.append(System.lineSeparator());

    new StrictExpectations()
    {{
      events.onStart();
      events.onVersionReceived(SMFFormatVersion.of(1, 0));
      events.onError(this.withArgThat(
        new ParseErrorMessageStartsWith("Could not parse number")));
      events.onError(this.withArgThat(
        new ParseErrorMessageStartsWith("Unexpected EOF")));
      events.onFinish();
    }};

    runForText(events, false, s);
  }

  @Test
  public void testBadVendor4(
    final @Mocked SMFParserEventsType events)
  {
    final StringBuilder s = new StringBuilder(128);
    s.append("smf 1 0");
    s.append(System.lineSeparator());
    s.append("schema 0 0 0 x");
    s.append(System.lineSeparator());

    new StrictExpectations()
    {{
      events.onStart();
      events.onVersionReceived(SMFFormatVersion.of(1, 0));
      events.onError(this.withArgThat(
        new ParseErrorMessageStartsWith("Could not parse number")));
      events.onError(this.withArgThat(
        new ParseErrorMessageStartsWith("Unexpected EOF")));
      events.onFinish();
    }};

    runForText(events, false, s);
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

    final SMFHeader.Builder header_b = baseHeader(attributes);
    final SMFHeader header = header_b.build();

    final SMFSerializerType serializer =
      new SMFFormatText().serializerCreate(version, path, out);

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
      new SMFFormatText().serializerCreate(version, path, out);

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
      new SMFFormatText().serializerCreate(version, path, out);

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

    final SMFHeader.Builder header_b = baseHeader(attributes);
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
      new SMFFormatText().serializerCreate(version, path, out);

    final List<SMFAttribute> attributes = List.of(
      SMFAttribute.of(
        SMFAttributeName.of("x"),
        SMFComponentType.ELEMENT_TYPE_FLOATING,
        4,
        32));

    final SMFHeader.Builder header_b = baseHeader(attributes);
    header_b.setVertexCount(1L);
    final SMFHeader header = header_b.build();

    serializer.serializeHeader(header);
    serializer.serializeDataStart();
    serializer.serializeData(SMFAttributeName.of("x"));

    this.expected.expect(IllegalStateException.class);
    serializer.serializeData(SMFAttributeName.of("x"));
  }

  @Test
  public void testNoTriangles(
    final @Mocked SMFParserEventsType events)
  {
    final StringBuilder s = new StringBuilder(128);
    s.append("smf 1 0");
    s.append(System.lineSeparator());
    s.append("data");
    s.append(System.lineSeparator());

    final SMFHeader.Builder header_b = baseHeader(List.empty());
    final SMFHeader h = header_b.build();

    new StrictExpectations()
    {{
      events.onStart();
      events.onVersionReceived(SMFFormatVersion.of(1, 0));
      events.onError(this.withArgThat(
        new ParseErrorMessageStartsWith("No triangle count was specified")));
      events.onFinish();
    }};

    runForText(events, false, s);
  }

  @Test
  public void testNoVertices(
    final @Mocked SMFParserEventsType events)
  {
    final StringBuilder s = new StringBuilder(128);
    s.append("smf 1 0");
    s.append(System.lineSeparator());
    s.append("triangles 0 16");
    s.append(System.lineSeparator());
    s.append("data");
    s.append(System.lineSeparator());

    final SMFHeader.Builder header_b = baseHeader(List.empty());
    final SMFHeader h = header_b.build();

    new StrictExpectations()
    {{
      events.onStart();
      events.onVersionReceived(SMFFormatVersion.of(1, 0));
      events.onError(this.withArgThat(
        new ParseErrorMessageStartsWith("No vertex count was specified")));
      events.onFinish();
    }};

    runForText(events, false, s);
  }

  @Test
  public void testNoCoordinateSpace(
    final @Mocked SMFParserEventsType events)
  {
    final StringBuilder s = new StringBuilder(128);
    s.append("smf 1 0");
    s.append(System.lineSeparator());
    s.append("triangles 0 16");
    s.append(System.lineSeparator());
    s.append("vertices 0");
    s.append(System.lineSeparator());
    s.append("data");
    s.append(System.lineSeparator());

    final SMFHeader.Builder header_b = baseHeader(List.empty());
    final SMFHeader h = header_b.build();

    new StrictExpectations()
    {{
      events.onStart();
      events.onVersionReceived(SMFFormatVersion.of(1, 0));
      events.onError(this.withArgThat(
        new ParseErrorMessageStartsWith("No coordinate system was specified")));
      events.onFinish();
    }};

    runForText(events, false, s);
  }

  @Test
  public void testSerializerAttributeNotFinishedSerializingPrevious()
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
        32),
      SMFAttribute.of(
        SMFAttributeName.of("y"),
        SMFComponentType.ELEMENT_TYPE_FLOATING,
        4,
        32));

    final SMFHeader.Builder header_b = SMFHeader.builder();
    header_b.setVertexCount(1L);
    header_b.setTriangles(SMFTriangles.of(0L, 16L));
    header_b.setAttributesInOrder(attributes);
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
    serializer.serializeDataStart();
    serializer.serializeData(SMFAttributeName.of("x"));

    this.expected.expect(IllegalStateException.class);
    serializer.serializeData(SMFAttributeName.of("y"));
  }

  @Test
  public void testMetaNoneNotOmitted(
    final @Mocked SMFParserEventsType events)
  {
    final StringBuilder s = new StringBuilder(128);
    s.append("smf 1 0");
    s.append(System.lineSeparator());
    s.append("schema 696F376D A0B0C0D0 1 2");
    s.append(System.lineSeparator());
    s.append("coordinates +x +y -z counter-clockwise");
    s.append(System.lineSeparator());
    s.append("attribute a integer-unsigned 1 32");
    s.append(System.lineSeparator());
    s.append("vertices 1");
    s.append(System.lineSeparator());
    s.append("triangles 1 16");
    s.append(System.lineSeparator());
    s.append("data");
    s.append(System.lineSeparator());
    s.append("attribute a");
    s.append(System.lineSeparator());
    s.append("0");
    s.append(System.lineSeparator());
    s.append("triangles");
    s.append(System.lineSeparator());
    s.append("0 0 0");
    s.append(System.lineSeparator());
    s.append("metadata");
    s.append(System.lineSeparator());

    final SMFAttribute attribute = SMFAttribute.of(
      SMFAttributeName.of("a"),
      SMFComponentType.ELEMENT_TYPE_INTEGER_UNSIGNED,
      1,
      32);

    final SMFHeader.Builder header_b = baseHeader(List.of(attribute));
    header_b.setTriangles(baseTriangles().withTriangleCount(1L));
    header_b.setVertexCount(1L);
    final SMFHeader h = header_b.build();

    new StrictExpectations()
    {{
      events.onStart();
      events.onVersionReceived(SMFFormatVersion.of(1, 0));
      events.onHeaderParsed(h);
      events.onDataAttributeStart(attribute);
      events.onDataAttributeValueIntegerUnsigned1(0L);
      events.onDataAttributeFinish(attribute);
      events.onDataTrianglesStart();
      events.onDataTriangle(0L, 0L, 0L);
      events.onDataTrianglesFinish();
      events.onFinish();
    }};

    runForText(events, true, s);
  }

  @Test
  public void testMetaNoneOmitted(
    final @Mocked SMFParserEventsType events)
  {
    final StringBuilder s = new StringBuilder(128);
    s.append("smf 1 0");
    s.append(System.lineSeparator());
    s.append("schema 696F376D A0B0C0D0 1 2");
    s.append(System.lineSeparator());
    s.append("coordinates +x +y -z counter-clockwise");
    s.append(System.lineSeparator());
    s.append("attribute a integer-unsigned 1 32");
    s.append(System.lineSeparator());
    s.append("vertices 1");
    s.append(System.lineSeparator());
    s.append("triangles 1 16");
    s.append(System.lineSeparator());
    s.append("data");
    s.append(System.lineSeparator());
    s.append("attribute a");
    s.append(System.lineSeparator());
    s.append("0");
    s.append(System.lineSeparator());
    s.append("triangles");
    s.append(System.lineSeparator());
    s.append("0 0 0");
    s.append(System.lineSeparator());

    final SMFAttribute attribute = SMFAttribute.of(
      SMFAttributeName.of("a"),
      SMFComponentType.ELEMENT_TYPE_INTEGER_UNSIGNED,
      1,
      32);

    final SMFHeader.Builder header_b = baseHeader(List.of(attribute));
    header_b.setTriangles(baseTriangles().withTriangleCount(1L));
    header_b.setVertexCount(1L);
    final SMFHeader h = header_b.build();

    new StrictExpectations()
    {{
      events.onStart();
      events.onVersionReceived(SMFFormatVersion.of(1, 0));
      events.onHeaderParsed(h);
      events.onDataAttributeStart(attribute);
      events.onDataAttributeValueIntegerUnsigned1(0L);
      events.onDataAttributeFinish(attribute);
      events.onDataTrianglesStart();
      events.onDataTriangle(0L, 0L, 0L);
      events.onDataTrianglesFinish();
      events.onFinish();
    }};

    runForText(events, true, s);
  }

  @Test
  public void testMetaTwice(
    final @Mocked SMFParserEventsType events)
  {
    final StringBuilder s = new StringBuilder(128);
    s.append("smf 1 0");
    s.append(System.lineSeparator());
    s.append("schema 696F376D A0B0C0D0 1 2");
    s.append(System.lineSeparator());
    s.append("coordinates +x +y -z counter-clockwise");
    s.append(System.lineSeparator());
    s.append("attribute a integer-unsigned 1 32");
    s.append(System.lineSeparator());
    s.append("vertices 1");
    s.append(System.lineSeparator());
    s.append("triangles 1 16");
    s.append(System.lineSeparator());
    s.append("data");
    s.append(System.lineSeparator());
    s.append("attribute a");
    s.append(System.lineSeparator());
    s.append("0");
    s.append(System.lineSeparator());
    s.append("triangles");
    s.append(System.lineSeparator());
    s.append("0 0 0");
    s.append(System.lineSeparator());
    s.append("metadata");
    s.append(System.lineSeparator());
    s.append("metadata");
    s.append(System.lineSeparator());

    final SMFAttribute attribute = SMFAttribute.of(
      SMFAttributeName.of("a"),
      SMFComponentType.ELEMENT_TYPE_INTEGER_UNSIGNED,
      1,
      32);

    final SMFHeader.Builder header_b = baseHeader(List.of(attribute));
    header_b.setTriangles(baseTriangles().withTriangleCount(1L));
    header_b.setVertexCount(1L);
    final SMFHeader h = header_b.build();

    new StrictExpectations()
    {{
      events.onStart();
      events.onVersionReceived(SMFFormatVersion.of(1, 0));
      events.onHeaderParsed(h);
      events.onDataAttributeStart(attribute);
      events.onDataAttributeValueIntegerUnsigned1(0L);
      events.onDataAttributeFinish(attribute);
      events.onDataTrianglesStart();
      events.onDataTriangle(0L, 0L, 0L);
      events.onDataTrianglesFinish();
      events.onError(this.withArgThat(
        new ParseErrorMessageStartsWith(
          "A metadata command has already been specified.")));
      events.onFinish();
    }};

    runForText(events, true, s);
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
