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

package com.io7m.smfj.tests.parser.api;

import com.io7m.smfj.core.SMFErrorType;
import com.io7m.smfj.core.SMFFormatVersion;
import com.io7m.smfj.core.SMFHeader;
import com.io7m.smfj.core.SMFTriangles;
import com.io7m.smfj.core.SMFWarningType;
import com.io7m.smfj.parser.api.SMFParserEventsBodyType;
import com.io7m.smfj.parser.api.SMFParserEventsHeaderType;
import com.io7m.smfj.parser.api.SMFParserEventsType;
import com.io7m.smfj.parser.api.SMFParserSequentialType;
import mockit.Delegate;
import mockit.Mocked;
import mockit.Expectations;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.NoSuchFileException;
import java.util.Optional;

public abstract class SMFParserSequentialTextContract
{
  private static URI uri(
    final String name)
    throws NoSuchFileException
  {
    final URL url = SMFParserSequentialTextContract.class.getResource(name);
    if (url == null) {
      throw new NoSuchFileException(name);
    }

    try {
      return url.toURI();
    } catch (final URISyntaxException e) {
      throw new IllegalArgumentException(e);
    }
  }

  protected abstract SMFParserSequentialType parser(
    SMFParserEventsType events,
    URI uri,
    InputStream stream);

  protected final SMFParserSequentialType parser(
    final SMFParserEventsType events,
    final String name)
    throws IOException
  {
    final URI uri = uri(name);
    final URL url = uri.toURL();
    return this.parser(events, uri, url.openStream());
  }

  @Test
  public void testEmpty(
    final @Mocked SMFParserEventsType events)
    throws Exception
  {
    new Expectations()
    {{
      events.onStart();
      events.onError(this.with(new Delegate<SMFErrorType>()
      {
        public boolean check(final SMFErrorType e)
        {
          return e.message().contains("Unexpected EOF");
        }
      }));
      events.onFinish();
    }};

    try (SMFParserSequentialType p =
           this.parser(events, "empty.smft")) {
      p.parse();
    }
  }

  @Test
  public void testMinimal(
    final @Mocked SMFParserEventsHeaderType events_header,
    final @Mocked SMFParserEventsType events)
    throws Exception
  {
    final SMFHeader.Builder header_builder = SMFHeader.builder();
    final SMFHeader header = header_builder.build();

    new Expectations()
    {{
      events.onStart();
      events.onVersionReceived(SMFFormatVersion.of(1, 0));
      this.result = Optional.of(events_header);
      events_header.onHeaderParsed(header);
      events.onFinish();
    }};

    try (SMFParserSequentialType p =
           this.parser(events, "minimal.smft")) {
      p.parse();
    }
  }

  @Test
  public void testHeaderEarlyEOF(
    final @Mocked SMFParserEventsHeaderType events_header,
    final @Mocked SMFParserEventsType events)
    throws Exception
  {
    new Expectations()
    {{
      events.onStart();
      events.onVersionReceived(SMFFormatVersion.of(1, 0));
      this.result = Optional.of(events_header);
      events_header.onError(this.with(new Delegate<SMFErrorType>()
      {
        public boolean check(final SMFErrorType e)
        {
          return e.message().contains("Unexpected EOF");
        }
      }));
      events.onFinish();
    }};

    try (SMFParserSequentialType p =
           this.parser(events, "header_early_eof.smft")) {
      p.parse();
    }
  }

  @Test
  public void testHeaderUnrecognized(
    final @Mocked SMFParserEventsHeaderType events_header,
    final @Mocked SMFParserEventsType events)
    throws Exception
  {
    final SMFHeader.Builder header_builder = SMFHeader.builder();
    final SMFHeader header = header_builder.build();

    new Expectations()
    {{
      events.onStart();
      events.onVersionReceived(SMFFormatVersion.of(1, 0));
      this.result = Optional.of(events_header);
      events_header.onWarning(this.with(new Delegate<SMFWarningType>()
      {
        boolean check(final SMFWarningType w)
        {
          return w.message().contains("Unrecognized");
        }
      }));
      events_header.onHeaderParsed(header);
      events.onFinish();
    }};

    try (SMFParserSequentialType p =
           this.parser(events, "header_unrecognized.smft")) {
      p.parse();
    }
  }

  @Test
  public void testHeaderMalformedEnd(
    final @Mocked SMFParserEventsHeaderType events_header,
    final @Mocked SMFParserEventsType events)
    throws Exception
  {
    final SMFHeader.Builder header_builder = SMFHeader.builder();
    final SMFHeader header = header_builder.build();

    new Expectations()
    {{
      events.onStart();
      events.onVersionReceived(SMFFormatVersion.of(1, 0));
      this.result = Optional.of(events_header);
      events_header.onError(this.with(new Delegate<SMFErrorType>()
      {
        boolean check(final SMFErrorType e)
        {
          return e.message().contains("Malformed");
        }
      }));
      events.onFinish();
    }};

    try (SMFParserSequentialType p =
           this.parser(events, "header_malformed_end.smft")) {
      p.parse();
    }
  }

  @Test
  public void testHeaderAttributeDuplicate(
    final @Mocked SMFParserEventsHeaderType events_header,
    final @Mocked SMFParserEventsType events)
    throws Exception
  {
    new Expectations()
    {{
      events.onStart();
      events.onVersionReceived(SMFFormatVersion.of(1, 0));
      this.result = Optional.of(events_header);
      events_header.onError(this.with(new Delegate<SMFErrorType>()
      {
        boolean check(final SMFErrorType e)
        {
          return e.message().contains("Duplicate attribute name");
        }
      }));
      events.onFinish();
    }};

    try (SMFParserSequentialType p =
           this.parser(events, "header_attribute_duplicate.smft")) {
      p.parse();
    }
  }

  @Test
  public void testHeaderUnsupportedVersion(
    final @Mocked SMFParserEventsHeaderType events_header,
    final @Mocked SMFParserEventsType events)
    throws Exception
  {
    new Expectations()
    {{
      events.onStart();
      events.onError(this.with(new Delegate<SMFErrorType>()
      {
        boolean check(final SMFErrorType e)
        {
          return e.message().contains("is not supported");
        }
      }));
      events.onFinish();
    }};

    try (SMFParserSequentialType p =
           this.parser(events, "header_version_unsupported.smft")) {
      p.parse();
    }
  }

  @Test
  public void testHeaderBrokenVersion0(
    final @Mocked SMFParserEventsHeaderType events_header,
    final @Mocked SMFParserEventsType events)
    throws Exception
  {
    new Expectations()
    {{
      events.onStart();
      events.onError(this.with(new Delegate<SMFErrorType>()
      {
        boolean check(final SMFErrorType e)
        {
          return e.message().contains("Cannot parse number");
        }
      }));
      events.onFinish();
    }};

    try (SMFParserSequentialType p =
           this.parser(events, "header_version_broken0.smft")) {
      p.parse();
    }
  }

  @Test
  public void testHeaderBrokenVersion1(
    final @Mocked SMFParserEventsHeaderType events_header,
    final @Mocked SMFParserEventsType events)
    throws Exception
  {
    new Expectations()
    {{
      events.onStart();
      events.onError(this.with(new Delegate<SMFErrorType>()
      {
        boolean check(final SMFErrorType e)
        {
          return e.message().contains("Incorrect number of arguments");
        }
      }));
      events.onFinish();
    }};

    try (SMFParserSequentialType p =
           this.parser(events, "header_version_broken1.smft")) {
      p.parse();
    }
  }

  @Test
  public void testHeaderBrokenVersion2(
    final @Mocked SMFParserEventsHeaderType events_header,
    final @Mocked SMFParserEventsType events)
    throws Exception
  {
    new Expectations()
    {{
      events.onStart();
      events.onError(this.with(new Delegate<SMFErrorType>()
      {
        boolean check(final SMFErrorType e)
        {
          return e.message().contains("The first line must be a version declaration");
        }
      }));
      events.onFinish();
    }};

    try (SMFParserSequentialType p =
           this.parser(events, "header_version_broken2.smft")) {
      p.parse();
    }
  }

  @Test
  public void testBodyUnrecognized0(
    final @Mocked SMFParserEventsBodyType events_data,
    final @Mocked SMFParserEventsHeaderType events_header,
    final @Mocked SMFParserEventsType events)
    throws Exception
  {
    final SMFHeader.Builder header_builder = SMFHeader.builder();
    final SMFHeader header = header_builder.build();

    new Expectations()
    {{
      events.onStart();
      events.onVersionReceived(SMFFormatVersion.of(1, 0));
      this.result = Optional.of(events_header);
      events_header.onHeaderParsed(header);
      this.result = Optional.of(events_data);
      events_data.onWarning(this.with(new Delegate<SMFWarningType>()
      {
        boolean check(final SMFWarningType w)
        {
          return w.message().contains("Unrecognized command");
        }
      }));
      events.onFinish();
    }};

    try (SMFParserSequentialType p =
           this.parser(events, "body_unrecognized0.smft")) {
      p.parse();
    }
  }

  @Test
  public void testBodyUnrecognized1(
    final @Mocked SMFParserEventsBodyType events_data,
    final @Mocked SMFParserEventsHeaderType events_header,
    final @Mocked SMFParserEventsType events)
    throws Exception
  {
    final SMFHeader.Builder header_builder = SMFHeader.builder();
    final SMFHeader header = header_builder.build();

    new Expectations()
    {{
      events.onStart();
      events.onVersionReceived(SMFFormatVersion.of(1, 0));
      this.result = Optional.of(events_header);
      events_header.onHeaderParsed(header);
      this.result = Optional.of(events_data);
      events_data.onWarning(this.with(new Delegate<SMFWarningType>()
      {
        boolean check(final SMFWarningType w)
        {
          return w.message().contains("Unrecognized command");
        }
      }));
      events_data.onTriangles();
      events.onFinish();
    }};

    try (SMFParserSequentialType p =
           this.parser(events, "body_unrecognized1.smft")) {
      p.parse();
    }
  }

  @Test
  public void testVerticesMissing(
    final @Mocked SMFParserEventsBodyType events_data,
    final @Mocked SMFParserEventsHeaderType events_header,
    final @Mocked SMFParserEventsType events)
    throws Exception
  {
    final SMFHeader.Builder header_builder = SMFHeader.builder();
    header_builder.setVertexCount(1L);
    final SMFHeader header = header_builder.build();

    new Expectations()
    {{
      events.onStart();
      events.onVersionReceived(SMFFormatVersion.of(1, 0));
      this.result = Optional.of(events_header);
      events_header.onHeaderParsed(header);
      this.result = Optional.of(events_data);
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
           this.parser(events, "vertices_missing.smft")) {
      p.parse();
    }
  }

  @Test
  public void testTrianglesMissing(
    final @Mocked SMFParserEventsBodyType events_data,
    final @Mocked SMFParserEventsHeaderType events_header,
    final @Mocked SMFParserEventsType events)
    throws Exception
  {
    final SMFHeader.Builder header_builder = SMFHeader.builder();
    header_builder.setTriangles(SMFTriangles.of(1L, 32));
    final SMFHeader header = header_builder.build();

    new Expectations()
    {{
      events.onStart();
      events.onVersionReceived(SMFFormatVersion.of(1, 0));
      this.result = Optional.of(events_header);
      events_header.onHeaderParsed(header);
      this.result = Optional.of(events_data);
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
           this.parser(events, "triangles_missing.smft")) {
      p.parse();
    }
  }
}
