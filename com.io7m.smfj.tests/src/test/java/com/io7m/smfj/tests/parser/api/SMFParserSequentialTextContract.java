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

import com.io7m.jlexing.core.LexicalPosition;
import com.io7m.smfj.core.SMFErrorType;
import com.io7m.smfj.core.SMFFormatVersion;
import com.io7m.smfj.core.SMFHeader;
import com.io7m.smfj.core.SMFTriangles;
import com.io7m.smfj.core.SMFWarningType;
import com.io7m.smfj.format.text.SMFTLineReaderType;
import com.io7m.smfj.parser.api.SMFParserEventsBodyType;
import com.io7m.smfj.parser.api.SMFParserEventsHeaderType;
import com.io7m.smfj.parser.api.SMFParserEventsType;
import com.io7m.smfj.parser.api.SMFParserSequentialType;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.NoSuchFileException;
import java.util.Optional;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.mockito.internal.verification.Times;

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

  private String capturedError()
  {
    return this.errorCaptor.getValue().message();
  }

  private String capturedWarning()
  {
    return this.warningCaptor.getValue().message();
  }

  private ArgumentCaptor<SMFErrorType> errorCaptor;
  private ArgumentCaptor<SMFWarningType> warningCaptor;
  private SMFParserEventsType events;
  private SMFParserEventsHeaderType eventsHeader;
  private SMFParserEventsBodyType eventsData;
  private SMFTLineReaderType reader;

  @BeforeEach
  public final void testSetup()
  {
    this.events =
      Mockito.mock(SMFParserEventsType.class);
    this.eventsHeader =
      Mockito.mock(SMFParserEventsHeaderType.class);
    this.eventsData =
      Mockito.mock(SMFParserEventsBodyType.class);
    this.reader =
      Mockito.mock(SMFTLineReaderType.class);
    this.errorCaptor =
      ArgumentCaptor.forClass(SMFErrorType.class);
    this.warningCaptor =
      ArgumentCaptor.forClass(SMFWarningType.class);

    Mockito.when(this.reader.position())
      .thenReturn(LexicalPosition.of(0, 0, Optional.empty()));
  }

  @Test
  public void testEmpty()
    throws Exception
  {
    try (var p = this.parser(this.events, "empty.smft")) {
      p.parse();
    }

    Mockito.verify(this.events, new Times(1)).onStart();
    Mockito.verify(this.events).onError(this.errorCaptor.capture());
    Assertions.assertTrue(this.capturedError().contains("Unexpected EOF"));
    Mockito.verify(this.events, new Times(1)).onFinish();
  }

  @Test
  public void testMinimal()
    throws Exception
  {
    final SMFHeader.Builder header_builder = SMFHeader.builder();
    final SMFHeader header = header_builder.build();

    Mockito.when(this.events.onVersionReceived(SMFFormatVersion.of(1, 0)))
      .thenReturn(Optional.of(this.eventsHeader));

    try (var p = this.parser(this.events, "minimal.smft")) {
      p.parse();
    }

    Mockito.verify(this.events, new Times(1)).onStart();
    Mockito.verify(this.eventsHeader, new Times(1)).onHeaderParsed(header);
    Mockito.verify(this.events, new Times(1)).onFinish();
  }

  @Test
  public void testHeaderEarlyEOF()
    throws Exception
  {
    Mockito.when(this.events.onVersionReceived(SMFFormatVersion.of(1, 0)))
      .thenReturn(Optional.of(this.eventsHeader));

    try (var p = this.parser(this.events, "header_early_eof.smft")) {
      p.parse();
    }

    Mockito.verify(this.events, new Times(1)).onStart();
    Mockito.verify(this.eventsHeader).onError(this.errorCaptor.capture());
    Assertions.assertTrue(this.capturedError().contains("Unexpected EOF"));
    Mockito.verify(this.events, new Times(1)).onFinish();
  }

  @Test
  public void testHeaderUnrecognized()
    throws Exception
  {
    final SMFHeader.Builder header_builder = SMFHeader.builder();
    final SMFHeader header = header_builder.build();

    Mockito.when(this.events.onVersionReceived(SMFFormatVersion.of(1, 0)))
      .thenReturn(Optional.of(this.eventsHeader));

    try (var p = this.parser(this.events, "header_unrecognized.smft")) {
      p.parse();
    }

    Mockito.verify(this.events, new Times(1)).onStart();
    Mockito.verify(this.eventsHeader).onWarning(this.warningCaptor.capture());
    Assertions.assertTrue(this.capturedWarning().contains("Unrecognized"));
    Mockito.verify(this.eventsHeader, new Times(1)).onHeaderParsed(header);
    Mockito.verify(this.events, new Times(1)).onFinish();
  }

  @Test
  public void testHeaderMalformedEnd()
    throws Exception
  {
    Mockito.when(this.events.onVersionReceived(SMFFormatVersion.of(1, 0)))
      .thenReturn(Optional.of(this.eventsHeader));

    try (var p = this.parser(this.events, "header_malformed_end.smft")) {
      p.parse();
    }

    Mockito.verify(this.events, new Times(1)).onStart();
    Mockito.verify(this.eventsHeader).onError(this.errorCaptor.capture());
    Assertions.assertTrue(this.capturedError().contains("Malformed"));
    Mockito.verify(this.events, new Times(1)).onFinish();
  }

  @Test
  public void testHeaderAttributeDuplicate()
    throws Exception
  {
    Mockito.when(this.events.onVersionReceived(SMFFormatVersion.of(1, 0)))
      .thenReturn(Optional.of(this.eventsHeader));

    try (var p = this.parser(this.events, "header_attribute_duplicate.smft")) {
      p.parse();
    }

    Mockito.verify(this.events, new Times(1)).onStart();
    Mockito.verify(this.eventsHeader).onError(this.errorCaptor.capture());
    Assertions.assertTrue(this.capturedError().contains("Duplicate attribute name"));
    Mockito.verify(this.events, new Times(1)).onFinish();
  }

  @Test
  public void testHeaderUnsupportedVersion()
    throws Exception
  {
    try (var p = this.parser(this.events, "header_version_unsupported.smft")) {
      p.parse();
    }

    Mockito.verify(this.events, new Times(1)).onStart();
    Mockito.verify(this.events).onError(this.errorCaptor.capture());
    Assertions.assertTrue(this.capturedError().contains("is not supported"));
    Mockito.verify(this.events, new Times(1)).onFinish();
  }

  @Test
  public void testHeaderBrokenVersion0()
    throws Exception
  {
    try (var p = this.parser(this.events, "header_version_broken0.smft")) {
      p.parse();
    }

    Mockito.verify(this.events, new Times(1)).onStart();
    Mockito.verify(this.events).onError(this.errorCaptor.capture());
    Assertions.assertTrue(this.capturedError().contains("Cannot parse number"));
    Mockito.verify(this.events, new Times(1)).onFinish();
  }

  @Test
  public void testHeaderBrokenVersion1()
    throws Exception
  {
    try (var p = this.parser(this.events, "header_version_broken1.smft")) {
      p.parse();
    }

    Mockito.verify(this.events, new Times(1)).onStart();
    Mockito.verify(this.events).onError(this.errorCaptor.capture());
    Assertions.assertTrue(this.capturedError().contains("Incorrect number of arguments"));
    Mockito.verify(this.events, new Times(1)).onFinish();
  }

  @Test
  public void testHeaderBrokenVersion2()
    throws Exception
  {
    try (var p = this.parser(this.events, "header_version_broken2.smft")) {
      p.parse();
    }

    Mockito.verify(this.events, new Times(1)).onStart();
    Mockito.verify(this.events).onError(this.errorCaptor.capture());
    Assertions.assertTrue(this.capturedError().contains("The first line must be a version declaration"));
    Mockito.verify(this.events, new Times(1)).onFinish();
  }

  @Test
  public void testBodyUnrecognized0()
    throws Exception
  {
    final SMFHeader.Builder header_builder = SMFHeader.builder();
    final SMFHeader header = header_builder.build();

    Mockito.when(this.events.onVersionReceived(SMFFormatVersion.of(1, 0)))
      .thenReturn(Optional.of(this.eventsHeader));
    Mockito.when(this.eventsHeader.onHeaderParsed(header))
      .thenReturn(Optional.of(this.eventsData));

    try (var p = this.parser(this.events, "body_unrecognized0.smft")) {
      p.parse();
    }

    Mockito.verify(this.events, new Times(1)).onStart();
    Mockito.verify(this.eventsData).onWarning(this.warningCaptor.capture());
    Assertions.assertTrue(this.capturedWarning().contains("Unrecognized command"));
    Mockito.verify(this.events, new Times(1)).onFinish();
  }

  @Test
  public void testBodyUnrecognized1()
    throws Exception
  {
    final SMFHeader.Builder header_builder = SMFHeader.builder();
    final SMFHeader header = header_builder.build();

    Mockito.when(this.events.onVersionReceived(SMFFormatVersion.of(1, 0)))
      .thenReturn(Optional.of(this.eventsHeader));
    Mockito.when(this.eventsHeader.onHeaderParsed(header))
      .thenReturn(Optional.of(this.eventsData));

    try (var p = this.parser(this.events, "body_unrecognized1.smft")) {
      p.parse();
    }

    Mockito.verify(this.events, new Times(1)).onStart();
    Mockito.verify(this.eventsData).onWarning(this.warningCaptor.capture());
    Assertions.assertTrue(this.capturedWarning().contains("Unrecognized command"));
    Mockito.verify(this.eventsData, new Times(1)).onTriangles();
    Mockito.verify(this.events, new Times(1)).onFinish();
  }

  @Test
  public void testVerticesMissing()
    throws Exception
  {
    final SMFHeader.Builder header_builder = SMFHeader.builder();
    header_builder.setVertexCount(1L);
    final SMFHeader header = header_builder.build();

    Mockito.when(this.events.onVersionReceived(SMFFormatVersion.of(1, 0)))
      .thenReturn(Optional.of(this.eventsHeader));
    Mockito.when(this.eventsHeader.onHeaderParsed(header))
      .thenReturn(Optional.of(this.eventsData));

    try (var p = this.parser(this.events, "vertices_missing.smft")) {
      p.parse();
    }

    Mockito.verify(this.events, new Times(1)).onStart();
    Mockito.verify(this.events).onError(this.errorCaptor.capture());
    Assertions.assertTrue(this.capturedError().contains("A non-zero vertex count was specified, but no vertices were provided"));
    Mockito.verify(this.events, new Times(1)).onFinish();
  }

  @Test
  public void testTrianglesMissing()
    throws Exception
  {
    final SMFHeader.Builder header_builder = SMFHeader.builder();
    header_builder.setTriangles(SMFTriangles.of(1L, 32));
    final SMFHeader header = header_builder.build();

    Mockito.when(this.events.onVersionReceived(SMFFormatVersion.of(1, 0)))
      .thenReturn(Optional.of(this.eventsHeader));
    Mockito.when(this.eventsHeader.onHeaderParsed(header))
      .thenReturn(Optional.of(this.eventsData));

    try (var p = this.parser(this.events, "triangles_missing.smft")) {
      p.parse();
    }

    Mockito.verify(this.events, new Times(1)).onStart();
    Mockito.verify(this.events).onError(this.errorCaptor.capture());
    Assertions.assertTrue(this.capturedError().contains("A non-zero triangle count was specified, but no triangles were provided"));
    Mockito.verify(this.events, new Times(1)).onFinish();
  }
}
