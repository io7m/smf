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
import java.util.Optional;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.mockito.internal.verification.Times;
import org.slf4j.Logger;

public abstract class SMFFormatBinaryContract
{
  protected abstract Logger logger();

  protected abstract SMFParserSequentialType createParser(
    String name,
    SMFParserEventsType events)
    throws Exception;

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
  public final void testBadMagicNumber()
    throws Exception
  {
    try (var p = this.createParser("bad-magic.smfb", this.events)) {
      p.parse();
    }

    Mockito.verify(this.events, new Times(1)).onStart();
    Mockito.verify(this.events).onError(this.errorCaptor.capture());
    Assertions.assertTrue(this.capturedError().contains("Bad magic number"));
    Mockito.verify(this.events, new Times(1)).onFinish();
  }

  @Test
  public final void testUnsupported()
    throws Exception
  {
    Mockito.when(this.events.onVersionReceived(SMFFormatVersion.of(0x7ffffffe, 0)))
      .thenReturn(Optional.of(this.eventsHeader));

    try (var p = this.createParser("unsupported.smfb", this.events)) {
      p.parse();
    }

    Mockito.verify(this.events, new Times(1)).onStart();
    Mockito.verify(this.events).onError(this.errorCaptor.capture());
    Assertions.assertTrue(this.capturedError().contains("not supported"));
    Mockito.verify(this.events, new Times(1)).onFinish();
  }

  @Test
  public final void testMissingTriangles()
    throws Exception
  {
    final SMFHeader header =
      SMFHeader.builder()
        .setTriangles(SMFTriangles.of(1L, 32))
        .build();

    Mockito.when(this.events.onVersionReceived(SMFFormatVersion.of(1, 0)))
      .thenReturn(Optional.of(this.eventsHeader));
    Mockito.when(this.eventsHeader.onHeaderParsed(header))
      .thenReturn(Optional.of(this.eventsData));

    try (var p = this.createParser("missing_triangles.smfb", this.events)) {
      p.parse();
    }

    Mockito.verify(this.events, new Times(1)).onStart();
    Mockito.verify(this.events).onError(this.errorCaptor.capture());
    Assertions.assertTrue(this.capturedError().contains("A non-zero triangle count was specified, but no triangles were provided"));
    Mockito.verify(this.events, new Times(1)).onFinish();
  }

  @Test
  public final void testMissingVertices()
    throws Exception
  {
    final SMFHeader header =
      SMFHeader.builder()
        .setVertexCount(1L)
        .build();

    Mockito.when(this.events.onVersionReceived(SMFFormatVersion.of(1, 0)))
      .thenReturn(Optional.of(this.eventsHeader));
    Mockito.when(this.eventsHeader.onHeaderParsed(header))
      .thenReturn(Optional.of(this.eventsData));

    try (var p = this.createParser("missing_vertices.smfb", this.events)) {
      p.parse();
    }

    Mockito.verify(this.events, new Times(1)).onStart();
    Mockito.verify(this.events).onError(this.errorCaptor.capture());
    Assertions.assertTrue(this.capturedError().contains("A non-zero vertex count was specified, but no vertices were provided"));
    Mockito.verify(this.events, new Times(1)).onFinish();
  }
}
