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

import com.io7m.jlexing.core.LexicalPosition;
import com.io7m.smfj.core.SMFErrorType;
import com.io7m.smfj.core.SMFHeader;
import com.io7m.smfj.core.SMFSchemaIdentifier;
import com.io7m.smfj.core.SMFSchemaName;
import com.io7m.smfj.format.text.SMFTLineReaderList;
import com.io7m.smfj.format.text.SMFTLineReaderType;
import com.io7m.smfj.format.text.SMFTParsingStatus;
import com.io7m.smfj.format.text.v1.SMFTV1BodySectionParserMetadata;
import com.io7m.smfj.parser.api.SMFParserEventsBodyType;
import com.io7m.smfj.parser.api.SMFParserEventsDataMetaType;
import java.net.URI;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.mockito.internal.verification.Times;

import static com.io7m.smfj.format.text.SMFTParsingStatus.FAILURE;
import static com.io7m.smfj.format.text.SMFTParsingStatus.SUCCESS;

public final class SMFTV1BodyCommandMetadataTest
{
  private ArgumentCaptor<SMFErrorType> captor;
  private SMFParserEventsBodyType events;
  private SMFParserEventsDataMetaType eventsMeta;
  private SMFTLineReaderType reader;

  @BeforeEach
  public void testSetup()
  {
    this.events =
      Mockito.mock(SMFParserEventsBodyType.class);
    this.eventsMeta =
      Mockito.mock(SMFParserEventsDataMetaType.class);
    this.reader =
      Mockito.mock(SMFTLineReaderType.class);
    this.captor =
      ArgumentCaptor.forClass(SMFErrorType.class);

    Mockito.when(this.reader.position())
      .thenReturn(LexicalPosition.of(0, 0, Optional.empty()));
  }

  @Test
  public void testOK_IgnoreAll()
    throws Exception
  {
    final SMFTLineReaderType reader =
      SMFTLineReaderList.create(
        URI.create("urn:x"),
        List.of(
          "aGVsbG8taGVsbG8K",
          "end"),
        0);

    final SMFHeader.Builder header_b = SMFHeader.builder();
    final SMFHeader header = header_b.build();

    final SMFTV1BodySectionParserMetadata cmd =
      new SMFTV1BodySectionParserMetadata(() -> header, reader);

    final var schemaId =
      SMFSchemaIdentifier.of(
        SMFSchemaName.of("com.io7m.smf.example"), 1, 0);

    Mockito.when(this.events.onMeta(schemaId))
      .thenReturn(Optional.empty());

    final SMFTParsingStatus r =
      cmd.parse(
        this.events,
        List.of("metadata", "com.io7m.smf.example", "1", "0", "1"));
    Assertions.assertEquals(SUCCESS, r);
  }

  @Test
  public void testMalformedCommand_0()
    throws Exception
  {
    final SMFHeader.Builder header_b = SMFHeader.builder();
    final SMFHeader header = header_b.build();

    final SMFTV1BodySectionParserMetadata cmd =
      new SMFTV1BodySectionParserMetadata(() -> header, this.reader);

    final SMFTParsingStatus r = cmd.parse(
      this.events,
      List.of("metadata", "x"));
    Assertions.assertEquals(FAILURE, r);

    Mockito.verify(this.events).onError(this.captor.capture());
    Assertions.assertTrue(this.captor.getValue().message().contains(
      "Cannot parse"));
  }

  @Test
  public void testUnexpectedEOF()
    throws Exception
  {
    final SMFTLineReaderType reader =
      SMFTLineReaderList.create(
        URI.create("urn:x"),
        List.of(),
        0);

    final SMFHeader.Builder header_b = SMFHeader.builder();
    final SMFHeader header = header_b.build();

    final SMFTV1BodySectionParserMetadata cmd =
      new SMFTV1BodySectionParserMetadata(() -> header, reader);

    final var schemaIdentifier =
      SMFSchemaIdentifier.of(
        SMFSchemaName.of("com.io7m.smf.example"), 1, 0);

    Mockito.when(this.events.onMeta(schemaIdentifier))
      .thenReturn(Optional.of(this.eventsMeta));

    final SMFTParsingStatus r =
      cmd.parse(
        this.events,
        List.of("metadata", "com.io7m.smf.example", "1", "0", "1"));
    Assertions.assertEquals(FAILURE, r);

    Mockito.verify(this.eventsMeta).onError(this.captor.capture());
    Assertions.assertTrue(this.captor.getValue().message().contains(
      "Unexpected EOF"));
  }

  @Test
  public void testUnparseable0()
    throws Exception
  {
    final SMFTLineReaderType reader =
      SMFTLineReaderList.create(
        URI.create("urn:x"),
        List.of(
          "=",
          "end"),
        0);

    final SMFHeader.Builder header_b = SMFHeader.builder();
    final SMFHeader header = header_b.build();

    final SMFTV1BodySectionParserMetadata cmd =
      new SMFTV1BodySectionParserMetadata(() -> header, reader);

    final SMFSchemaIdentifier id =
      SMFSchemaIdentifier.of(
        SMFSchemaName.of("com.io7m.smf.example"), 1, 0);

    Mockito.when(this.events.onMeta(id))
      .thenReturn(Optional.of(this.eventsMeta));

    final SMFTParsingStatus r =
      cmd.parse(
        this.events,
        List.of("metadata", "com.io7m.smf.example", "1", "0", "1"));
    Assertions.assertEquals(FAILURE, r);

    Mockito.verify(this.eventsMeta).onError(this.captor.capture());
    Assertions.assertTrue(this.captor.getValue().message().contains(
      "Cannot parse base64 encoded data"));
  }

  @Test
  public void testUnparseable1()
    throws Exception
  {
    final SMFTLineReaderType reader =
      SMFTLineReaderList.create(
        URI.create("urn:x"),
        List.of(
          "",
          "end"),
        0);

    final SMFHeader.Builder header_b = SMFHeader.builder();
    final SMFHeader header = header_b.build();

    final SMFTV1BodySectionParserMetadata cmd =
      new SMFTV1BodySectionParserMetadata(() -> header, reader);

    final SMFSchemaIdentifier id =
      SMFSchemaIdentifier.of(
        SMFSchemaName.of("com.io7m.smf.example"), 1, 0);

    Mockito.when(this.events.onMeta(id))
      .thenReturn(Optional.of(this.eventsMeta));

    final SMFTParsingStatus r =
      cmd.parse(
        this.events,
        List.of("metadata", "com.io7m.smf.example", "1", "0", "1"));
    Assertions.assertEquals(FAILURE, r);

    Mockito.verify(this.eventsMeta).onError(this.captor.capture());
    Assertions.assertTrue(this.captor.getValue().message().contains(
      "Cannot parse base64 encoded data"));
  }

  @Test
  public void testUnparseable2()
    throws Exception
  {
    final SMFTLineReaderType reader =
      SMFTLineReaderList.create(
        URI.create("urn:x"),
        List.of(),
        0);

    final SMFHeader.Builder header_b = SMFHeader.builder();
    final SMFHeader header = header_b.build();

    final SMFTV1BodySectionParserMetadata cmd =
      new SMFTV1BodySectionParserMetadata(() -> header, reader);

    final SMFSchemaIdentifier id =
      SMFSchemaIdentifier.of(
        SMFSchemaName.of("com.io7m.smf.example"), 1, 0);

    Mockito.when(this.events.onMeta(id))
      .thenReturn(Optional.of(this.eventsMeta));

    final SMFTParsingStatus r =
      cmd.parse(
        this.events,
        List.of("metadata", "com.io7m.smf.example", "1", "0", "1"));
    Assertions.assertEquals(FAILURE, r);

    Mockito.verify(this.eventsMeta).onError(this.captor.capture());
    Assertions.assertTrue(this.captor.getValue().message().contains(
      "Unexpected EOF"));
  }

  @Test
  public void testUnparseable3()
    throws Exception
  {
    final SMFTLineReaderType reader =
      SMFTLineReaderList.create(
        URI.create("urn:x"),
        List.of(
          "metadata z com.io7m.smf.example 1"),
        0);

    final SMFHeader.Builder header_b = SMFHeader.builder();
    final SMFHeader header = header_b.build();

    final SMFTV1BodySectionParserMetadata cmd =
      new SMFTV1BodySectionParserMetadata(() -> header, reader);

    final SMFTParsingStatus r = cmd.parse(this.events, List.of("metadata"));
    Assertions.assertEquals(FAILURE, r);

    Mockito.verify(this.events).onError(this.captor.capture());
    Assertions.assertTrue(this.captor.getValue().message().contains(
      "Cannot parse meta command"));
  }

  @Test
  public void testUnparseable4()
    throws Exception
  {
    final SMFTLineReaderType reader =
      SMFTLineReaderList.create(
        URI.create("urn:x"),
        List.of(
          "metadata com.io7m.smf.example z 1"),
        0);

    final SMFHeader.Builder header_b = SMFHeader.builder();
    final SMFHeader header = header_b.build();

    final SMFTV1BodySectionParserMetadata cmd =
      new SMFTV1BodySectionParserMetadata(() -> header, reader);

    final SMFTParsingStatus r = cmd.parse(this.events, List.of("metadata"));
    Assertions.assertEquals(FAILURE, r);

    Mockito.verify(this.events).onError(this.captor.capture());
    Assertions.assertTrue(this.captor.getValue().message().contains(
      "Cannot parse meta command"));
  }

  @Test
  public void testUnparseable5()
    throws Exception
  {
    final SMFTLineReaderType reader =
      SMFTLineReaderList.create(
        URI.create("urn:x"),
        List.of(
          "metadata com.io7m.smf.example 1 z 1"),
        0);

    final SMFHeader.Builder header_b = SMFHeader.builder();
    final SMFHeader header = header_b.build();

    final SMFTV1BodySectionParserMetadata cmd =
      new SMFTV1BodySectionParserMetadata(() -> header, reader);

    final SMFTParsingStatus r = cmd.parse(this.events, List.of("metadata"));
    Assertions.assertEquals(FAILURE, r);

    Mockito.verify(this.events).onError(this.captor.capture());
    Assertions.assertTrue(this.captor.getValue().message().contains(
      "Cannot parse meta command"));
  }

  @Test
  public void testUnparseable6()
    throws Exception
  {
    final SMFTLineReaderType reader =
      SMFTLineReaderList.create(
        URI.create("urn:x"),
        List.of(
          "metadata com.io7m.smf.example 1 0 q"),
        0);

    final SMFHeader.Builder header_b = SMFHeader.builder();
    final SMFHeader header = header_b.build();

    final SMFTV1BodySectionParserMetadata cmd =
      new SMFTV1BodySectionParserMetadata(() -> header, reader);

    final SMFTParsingStatus r = cmd.parse(this.events, List.of("metadata"));
    Assertions.assertEquals(FAILURE, r);

    Mockito.verify(this.events).onError(this.captor.capture());
    Assertions.assertTrue(this.captor.getValue().message().contains(
      "Cannot parse meta command"));
  }

  @Test
  public void testUnparseable7()
    throws Exception
  {
    final SMFTLineReaderType reader =
      SMFTLineReaderList.create(
        URI.create("urn:x"),
        List.of(
          "metadata"),
        0);

    final SMFHeader.Builder header_b = SMFHeader.builder();
    final SMFHeader header = header_b.build();

    final SMFTV1BodySectionParserMetadata cmd =
      new SMFTV1BodySectionParserMetadata(() -> header, reader);

    final SMFTParsingStatus r = cmd.parse(this.events, List.of("metadata"));
    Assertions.assertEquals(FAILURE, r);

    Mockito.verify(this.events).onError(this.captor.capture());
    Assertions.assertTrue(this.captor.getValue().message().contains(
      "Cannot parse meta command"));
  }

  @Test
  public void testCorrect_0()
    throws Exception
  {
    final SMFTLineReaderType reader =
      SMFTLineReaderList.create(
        URI.create("urn:x"),
        List.of(
          "AA==",
          "end"),
        0);

    final SMFHeader.Builder header_b = SMFHeader.builder();
    final SMFHeader header = header_b.build();

    final SMFTV1BodySectionParserMetadata cmd =
      new SMFTV1BodySectionParserMetadata(() -> header, reader);

    final SMFSchemaIdentifier id0 =
      SMFSchemaIdentifier.of(
        SMFSchemaName.of("com.io7m.smf.example"), 1, 0);

    Mockito.when(this.events.onMeta(id0))
      .thenReturn(Optional.of(this.eventsMeta));

    final SMFTParsingStatus r =
      cmd.parse(
        this.events,
        List.of("metadata", "com.io7m.smf.example", "1", "0", "1"));
    Assertions.assertEquals(SUCCESS, r);

    Mockito.verify(this.eventsMeta, new Times(1))
      .onMetaData(id0, new byte[]{(byte) 0x0});
  }
}
