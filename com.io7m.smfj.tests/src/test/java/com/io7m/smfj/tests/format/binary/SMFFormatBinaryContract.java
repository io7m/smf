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

package com.io7m.smfj.tests.format.binary;

import com.io7m.smfj.core.SMFErrorType;
import com.io7m.smfj.core.SMFFormatVersion;
import com.io7m.smfj.core.SMFHeader;
import com.io7m.smfj.core.SMFTriangles;
import com.io7m.smfj.parser.api.SMFParserEventsBodyType;
import com.io7m.smfj.parser.api.SMFParserEventsHeaderType;
import com.io7m.smfj.parser.api.SMFParserEventsType;
import com.io7m.smfj.parser.api.SMFParserSequentialType;
import mockit.Delegate;
import mockit.Mocked;
import mockit.StrictExpectations;
import org.junit.Test;

import java.util.Optional;

public abstract class SMFFormatBinaryContract
{
  protected abstract SMFParserSequentialType createParser(
    String name,
    SMFParserEventsType events)
    throws Exception;

  @Test
  public void testBadMagicNumber(
    final @Mocked SMFParserEventsType events)
    throws Exception
  {
    new StrictExpectations()
    {{
      events.onStart();
      events.onError(this.with(new Delegate<SMFErrorType>()
      {
        boolean check(final SMFErrorType e)
        {
          return e.message().contains("Bad magic number");
        }
      }));
      events.onFinish();
    }};

    try (SMFParserSequentialType p =
           this.createParser("bad-magic.smfb", events)) {
      p.parse();
    }
  }

  @Test
  public void testUnsupported(
    final @Mocked SMFParserEventsHeaderType events_header,
    final @Mocked SMFParserEventsType events)
    throws Exception
  {
    new StrictExpectations()
    {{
      events.onStart();
      events.onVersionReceived(SMFFormatVersion.of(0x7ffffffe, 0));
      this.result = Optional.of(events_header);
      events_header.onError(this.with(new Delegate<SMFErrorType>()
      {
        boolean check(final SMFErrorType e)
        {
          return e.message().contains("not supported");
        }
      }));
      events.onFinish();
    }};

    try (SMFParserSequentialType p =
           this.createParser("unsupported.smfb", events)) {
      p.parse();
    }
  }

  @Test
  public void testMissingTriangles(
    final @Mocked SMFParserEventsBodyType events_body,
    final @Mocked SMFParserEventsHeaderType events_header,
    final @Mocked SMFParserEventsType events)
    throws Exception
  {
    new StrictExpectations()
    {{
      events.onStart();
      events.onVersionReceived(SMFFormatVersion.of(1, 0));
      this.result = Optional.of(events_header);
      events_header.onHeaderParsed(
        SMFHeader.builder().setTriangles(SMFTriangles.of(1L, 32)).build());
      this.result = Optional.of(events_body);
      events.onError(this.with(new Delegate<SMFErrorType>()
      {
        boolean check(final SMFErrorType e)
        {
          return e.message().contains("A non-zero triangle count was specified, but no triangles were provided");
        }
      }));
      events.onFinish();
    }};

    try (SMFParserSequentialType p =
           this.createParser("missing_triangles.smfb", events)) {
      p.parse();
    }
  }

  @Test
  public void testMissingVertices(
    final @Mocked SMFParserEventsBodyType events_body,
    final @Mocked SMFParserEventsHeaderType events_header,
    final @Mocked SMFParserEventsType events)
    throws Exception
  {
    new StrictExpectations()
    {{
      events.onStart();
      events.onVersionReceived(SMFFormatVersion.of(1, 0));
      this.result = Optional.of(events_header);
      events_header.onHeaderParsed(
        SMFHeader.builder().setVertexCount(1L).build());
      this.result = Optional.of(events_body);
      events.onError(this.with(new Delegate<SMFErrorType>()
      {
        boolean check(final SMFErrorType e)
        {
          return e.message().contains("A non-zero vertex count was specified, but no vertices were provided");
        }
      }));
      events.onFinish();
    }};

    try (SMFParserSequentialType p =
           this.createParser("missing_vertices.smfb", events)) {
      p.parse();
    }
  }
}
