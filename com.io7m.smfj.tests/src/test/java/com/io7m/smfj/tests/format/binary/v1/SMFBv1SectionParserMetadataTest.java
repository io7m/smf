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

package com.io7m.smfj.tests.format.binary.v1;

import com.io7m.jpra.runtime.java.JPRACursor1DByteBufferedChecked;
import com.io7m.jpra.runtime.java.JPRAStringTruncation;
import com.io7m.smfj.core.SMFErrorType;
import com.io7m.smfj.core.SMFHeader;
import com.io7m.smfj.core.SMFSchemaIdentifier;
import com.io7m.smfj.core.SMFSchemaName;
import com.io7m.smfj.format.binary.SMFBDataStreamReader;
import com.io7m.smfj.format.binary.SMFBDataStreamReaderType;
import com.io7m.smfj.format.binary.SMFBSectionMetadata;
import com.io7m.smfj.format.binary.v1.SMFBv1MetadataIDByteBuffered;
import com.io7m.smfj.format.binary.v1.SMFBv1MetadataIDType;
import com.io7m.smfj.format.binary.v1.SMFBv1SectionParserMetadata;
import com.io7m.smfj.parser.api.SMFParserEventsBodyType;
import com.io7m.smfj.parser.api.SMFParserEventsDataMetaType;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URI;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Optional;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.mockito.internal.verification.Times;

public final class SMFBv1SectionParserMetadataTest
{
  private ArgumentCaptor<SMFErrorType> captor;
  private SMFParserEventsBodyType eventsBody;
  private SMFParserEventsDataMetaType eventsMeta;

  @BeforeEach
  public void testSetup()
  {
    this.eventsBody =
      Mockito.mock(SMFParserEventsBodyType.class);
    this.eventsMeta =
      Mockito.mock(SMFParserEventsDataMetaType.class);
    this.captor =
      ArgumentCaptor.forClass(SMFErrorType.class);
  }

  @Test
  public void testEmpty()
  {
    final SMFBDataStreamReaderType reader =
      SMFBDataStreamReader.create(
        URI.create("urn:test"),
        new ByteArrayInputStream(new byte[]{}));

    final SMFHeader header = SMFHeader.builder().build();

    final SMFBv1SectionParserMetadata p = new SMFBv1SectionParserMetadata();
    Assertions.assertEquals(SMFBSectionMetadata.MAGIC, p.magic());

    p.parse(header, this.eventsBody, reader);

    Mockito.verify(this.eventsBody, new Times(1))
      .onError(this.captor.capture());

    final var e = this.captor.getValue();
    Assertions.assertTrue(
      e.exception().isPresent()
        && e.exception().get() instanceof IOException
        && e.message().contains("Failed to read the required number of octets"));
  }

  @Test
  public void testSimple()
  {
    final int header_size =
      SMFBv1MetadataIDByteBuffered.sizeInOctets();
    final byte[] data =
      new byte[header_size + (4 * 16)];

    final SMFBv1MetadataIDType view =
      JPRACursor1DByteBufferedChecked.newCursor(
        ByteBuffer.wrap(data).order(ByteOrder.BIG_ENDIAN),
        SMFBv1MetadataIDByteBuffered::newValueWithOffset)
        .getElementView();

    view.setMetaSize(8);
    view.setMetaSchemaVersionMajor(1);
    view.setMetaSchemaVersionMinor(2);
    view.getMetaSchemaIdWritable().setValue(
      "com.io7m.smf.example", JPRAStringTruncation.REJECT);

    data[header_size + 0] = 'A';
    data[header_size + 1] = 'B';
    data[header_size + 2] = 'C';
    data[header_size + 3] = 'D';
    data[header_size + 4] = 'E';
    data[header_size + 5] = 'F';
    data[header_size + 6] = 'G';
    data[header_size + 7] = 'H';

    final SMFBDataStreamReaderType reader =
      SMFBDataStreamReader.create(
        URI.create("urn:test"),
        new ByteArrayInputStream(data));

    final SMFHeader header = SMFHeader.builder().build();

    final SMFBv1SectionParserMetadata p = new SMFBv1SectionParserMetadata();
    Assertions.assertEquals(SMFBSectionMetadata.MAGIC, p.magic());

    Mockito.when(this.eventsBody.onMeta(
      SMFSchemaIdentifier.of(
        SMFSchemaName.of("com.io7m.smf.example"),
        1,
        2)
    )).thenReturn(Optional.of(this.eventsMeta));

    p.parse(header, this.eventsBody, reader);

    Mockito.verify(this.eventsMeta).onMetaData(
      SMFSchemaIdentifier.of(
        SMFSchemaName.of("com.io7m.smf.example"),
        1,
        2), new byte[]{'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H'}
    );
  }

  @Test
  public void testSimpleIgnored()
  {
    final int header_size =
      SMFBv1MetadataIDByteBuffered.sizeInOctets();
    final byte[] data =
      new byte[header_size + (4 * 16)];

    final SMFBv1MetadataIDType view =
      JPRACursor1DByteBufferedChecked.newCursor(
        ByteBuffer.wrap(data).order(ByteOrder.BIG_ENDIAN),
        SMFBv1MetadataIDByteBuffered::newValueWithOffset)
        .getElementView();

    view.setMetaSize(8);
    view.setMetaSchemaVersionMajor(1);
    view.setMetaSchemaVersionMinor(2);
    view.getMetaSchemaIdWritable().setValue(
      "com.io7m.smf.example", JPRAStringTruncation.REJECT);

    data[header_size + 0] = 'A';
    data[header_size + 1] = 'B';
    data[header_size + 2] = 'C';
    data[header_size + 3] = 'D';
    data[header_size + 4] = 'E';
    data[header_size + 5] = 'F';
    data[header_size + 6] = 'G';
    data[header_size + 7] = 'H';

    final SMFBDataStreamReaderType reader =
      SMFBDataStreamReader.create(
        URI.create("urn:test"),
        new ByteArrayInputStream(data));

    final SMFHeader header = SMFHeader.builder().build();

    final SMFBv1SectionParserMetadata p = new SMFBv1SectionParserMetadata();
    Assertions.assertEquals(SMFBSectionMetadata.MAGIC, p.magic());

    Mockito.when(this.eventsBody.onMeta(
      SMFSchemaIdentifier.of(
        SMFSchemaName.of("com.io7m.smf.example"),
        1,
        2)
    )).thenReturn(Optional.empty());

    p.parse(header, this.eventsBody, reader);
  }
}
