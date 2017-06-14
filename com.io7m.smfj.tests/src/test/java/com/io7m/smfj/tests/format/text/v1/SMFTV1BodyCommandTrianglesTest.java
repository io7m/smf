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

package com.io7m.smfj.tests.format.text.v1;

import com.io7m.smfj.core.SMFErrorType;
import com.io7m.smfj.core.SMFHeader;
import com.io7m.smfj.core.SMFTriangles;
import com.io7m.smfj.format.text.SMFTLineReaderList;
import com.io7m.smfj.format.text.SMFTLineReaderType;
import com.io7m.smfj.format.text.SMFTParsingStatus;
import com.io7m.smfj.format.text.v1.SMFTV1BodySectionParserTriangles;
import com.io7m.smfj.parser.api.SMFParserEventsDataTrianglesType;
import com.io7m.smfj.parser.api.SMFParserEventsBodyType;
import javaslang.collection.List;
import mockit.Delegate;
import mockit.Expectations;
import mockit.Mocked;
import mockit.StrictExpectations;
import org.junit.Assert;
import org.junit.Test;

import java.net.URI;
import java.util.Optional;

import static com.io7m.smfj.format.text.SMFTParsingStatus.FAILURE;
import static com.io7m.smfj.format.text.SMFTParsingStatus.SUCCESS;

public final class SMFTV1BodyCommandTrianglesTest
{
  @Test
  public void testOK_0(
    final @Mocked SMFParserEventsBodyType events,
    final @Mocked SMFTLineReaderType reader)
    throws Exception
  {
    final SMFHeader.Builder header_b = SMFHeader.builder();
    final SMFHeader header = header_b.build();

    final SMFTV1BodySectionParserTriangles cmd =
      new SMFTV1BodySectionParserTriangles(() -> header, reader);

    new StrictExpectations()
    {{
      reader.line();
      this.result = Optional.of(List.of("end"));
    }};

    final SMFTParsingStatus r =
      cmd.parse(events, List.of("triangles"));
    Assert.assertEquals(SUCCESS, r);
  }

  @Test
  public void testMalformedCommand(
    final @Mocked SMFParserEventsBodyType events)
    throws Exception
  {
    final SMFTLineReaderType reader =
      SMFTLineReaderList.create(
        URI.create("urn:x"),
        List.of(
          "0 1 2",
          "end"),
        0);

    final SMFHeader.Builder header_b = SMFHeader.builder();
    header_b.setTriangles(SMFTriangles.of(0L, 32));
    final SMFHeader header = header_b.build();

    final SMFTV1BodySectionParserTriangles cmd =
      new SMFTV1BodySectionParserTriangles(() -> header, reader);

    new Expectations()
    {{
      events.onError(this.with(new Delegate<SMFErrorType>()
      {
        boolean check(final SMFErrorType e)
        {
          return e.message().contains("Could not parse");
        }
      }));
    }};

    final SMFTParsingStatus r =
      cmd.parse(events, List.of("triangles", "what?"));
    Assert.assertEquals(FAILURE, r);
  }

  @Test
  public void testTooManyTriangles(
    final @Mocked SMFParserEventsDataTrianglesType events_triangles,
    final @Mocked SMFParserEventsBodyType events)
    throws Exception
  {
    final SMFTLineReaderType reader =
      SMFTLineReaderList.create(
        URI.create("urn:x"),
        List.of(
          "0 1 2",
          "end"),
        0);

    final SMFHeader.Builder header_b = SMFHeader.builder();
    header_b.setTriangles(SMFTriangles.of(0L, 32));
    final SMFHeader header = header_b.build();

    final SMFTV1BodySectionParserTriangles cmd =
      new SMFTV1BodySectionParserTriangles(() -> header, reader);

    new Expectations()
    {{
      events.onTriangles();
      this.result = Optional.of(events_triangles);

      events_triangles.onError(this.with(new Delegate<SMFErrorType>()
      {
        boolean check(final SMFErrorType e)
        {
          return e.message().contains("Too many triangles");
        }
      }));

      events_triangles.onDataTrianglesFinish();
    }};

    final SMFTParsingStatus r =
      cmd.parse(events, List.of("triangles"));
    Assert.assertEquals(FAILURE, r);
  }

  @Test
  public void testTooFewTriangles(
    final @Mocked SMFParserEventsDataTrianglesType events_triangles,
    final @Mocked SMFParserEventsBodyType events)
    throws Exception
  {
    final SMFTLineReaderType reader =
      SMFTLineReaderList.create(
        URI.create("urn:x"),
        List.of(
          "end"),
        0);

    final SMFHeader.Builder header_b = SMFHeader.builder();
    header_b.setTriangles(SMFTriangles.of(1L, 32));
    final SMFHeader header = header_b.build();

    final SMFTV1BodySectionParserTriangles cmd =
      new SMFTV1BodySectionParserTriangles(() -> header, reader);

    new Expectations()
    {{
      events.onTriangles();
      this.result = Optional.of(events_triangles);

      events_triangles.onError(this.with(new Delegate<SMFErrorType>()
      {
        boolean check(final SMFErrorType e)
        {
          return e.message().contains("Too few triangles");
        }
      }));

      events_triangles.onDataTrianglesFinish();
    }};

    final SMFTParsingStatus r =
      cmd.parse(events, List.of("triangles"));
    Assert.assertEquals(FAILURE, r);
  }

  @Test
  public void testBadTriangles0(
    final @Mocked SMFParserEventsDataTrianglesType events_triangles,
    final @Mocked SMFParserEventsBodyType events)
    throws Exception
  {
    final SMFTLineReaderType reader =
      SMFTLineReaderList.create(
        URI.create("urn:x"),
        List.of(
          "z 1 2",
          "end"),
        0);

    final SMFHeader.Builder header_b = SMFHeader.builder();
    header_b.setTriangles(SMFTriangles.of(1L, 32));
    final SMFHeader header = header_b.build();

    final SMFTV1BodySectionParserTriangles cmd =
      new SMFTV1BodySectionParserTriangles(() -> header, reader);

    new Expectations()
    {{
      events.onTriangles();
      this.result = Optional.of(events_triangles);

      events_triangles.onError(this.with(new Delegate<SMFErrorType>()
      {
        boolean check(final SMFErrorType e)
        {
          return e.message().contains("Cannot parse triangle");
        }
      }));

      events_triangles.onDataTrianglesFinish();
    }};

    final SMFTParsingStatus r =
      cmd.parse(events, List.of("triangles"));
    Assert.assertEquals(FAILURE, r);
  }

  @Test
  public void testBadTriangles1(
    final @Mocked SMFParserEventsDataTrianglesType events_triangles,
    final @Mocked SMFParserEventsBodyType events)
    throws Exception
  {
    final SMFTLineReaderType reader =
      SMFTLineReaderList.create(
        URI.create("urn:x"),
        List.of(
          "1 2 3 4",
          "end"),
        0);

    final SMFHeader.Builder header_b = SMFHeader.builder();
    header_b.setTriangles(SMFTriangles.of(1L, 32));
    final SMFHeader header = header_b.build();

    final SMFTV1BodySectionParserTriangles cmd =
      new SMFTV1BodySectionParserTriangles(() -> header, reader);

    new Expectations()
    {{
      events.onTriangles();
      this.result = Optional.of(events_triangles);

      events_triangles.onError(this.with(new Delegate<SMFErrorType>()
      {
        boolean check(final SMFErrorType e)
        {
          return e.message().contains("Cannot parse triangle");
        }
      }));

      events_triangles.onDataTrianglesFinish();
    }};

    final SMFTParsingStatus r =
      cmd.parse(events, List.of("triangles"));
    Assert.assertEquals(FAILURE, r);
  }

  @Test
  public void testTriangles(
    final @Mocked SMFParserEventsDataTrianglesType events_triangles,
    final @Mocked SMFParserEventsBodyType events)
    throws Exception
  {
    final SMFTLineReaderType reader =
      SMFTLineReaderList.create(
        URI.create("urn:x"),
        List.of(
          "1 2 3",
          "end"),
        0);

    final SMFHeader.Builder header_b = SMFHeader.builder();
    header_b.setTriangles(SMFTriangles.of(1L, 32));
    final SMFHeader header = header_b.build();

    final SMFTV1BodySectionParserTriangles cmd =
      new SMFTV1BodySectionParserTriangles(() -> header, reader);

    new Expectations()
    {{
      events.onTriangles();
      this.result = Optional.of(events_triangles);
      events_triangles.onDataTriangle(1L, 2L, 3L);
      events_triangles.onDataTrianglesFinish();
    }};

    final SMFTParsingStatus r =
      cmd.parse(events, List.of("triangles"));
    Assert.assertEquals(SUCCESS, r);
  }
}
