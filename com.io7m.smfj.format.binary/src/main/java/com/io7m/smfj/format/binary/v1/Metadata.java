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

package com.io7m.smfj.format.binary.v1;

import com.io7m.jnull.NullCheck;
import com.io7m.jpra.runtime.java.JPRACursor1DByteBufferedChecked;
import com.io7m.jpra.runtime.java.JPRAStringTruncation;
import com.io7m.smfj.core.SMFSchemaIdentifier;
import com.io7m.smfj.format.binary.SMFBAlignment;
import com.io7m.smfj.format.binary.SMFBDataStreamWriterType;
import com.io7m.smfj.format.binary.SMFBSection;
import com.io7m.smfj.format.binary.SMFBSectionMetadata;
import com.io7m.smfj.serializer.api.SMFSerializerDataMetaType;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

final class Metadata implements SMFSerializerDataMetaType
{
  private final SMFBDataStreamWriterType writer;

  Metadata(
    final SMFBDataStreamWriterType in_writer)
  {
    this.writer = NullCheck.notNull(in_writer, "Writer");
  }

  @Override
  public void close()
    throws IOException
  {
    final long position = this.writer.position();
    this.writer.padTo(
      SMFBAlignment.alignNext(position, SMFBSection.SECTION_ALIGNMENT),
      (byte) 0x0);
  }

  @Override
  public void serializeMetadata(
    final SMFSchemaIdentifier schema,
    final byte[] data)
    throws IOException
  {
    final byte[] buffer =
      new byte[SMFBv1MetadataIDByteBuffered.sizeInOctets()];

    final SMFBv1MetadataIDType view =
      JPRACursor1DByteBufferedChecked.newCursor(
        ByteBuffer.wrap(buffer).order(ByteOrder.BIG_ENDIAN),
        SMFBv1MetadataIDByteBuffered::newValueWithOffset)
        .getElementView();

    view.getMetaSchemaIdWritable()
      .setValue(schema.name().value(), JPRAStringTruncation.REJECT);
    view.setMetaSchemaVersionMajor(schema.versionMajor());
    view.setMetaSchemaVersionMinor(schema.versionMinor());
    view.setMetaSize(data.length);

    final long header_size =
      SMFBAlignment.alignNext(
        Integer.toUnsignedLong(buffer.length), SMFBSection.SECTION_ALIGNMENT);
    final long data_size =
      SMFBAlignment.alignNext(
        Integer.toUnsignedLong(data.length), SMFBSection.SECTION_ALIGNMENT);
    final long size_padded =
      Math.addExact(header_size, data_size);

    this.writer.putU64(SMFBSectionMetadata.MAGIC);
    this.writer.putU64(size_padded);
    this.writer.putBytes(buffer);

    this.writer.padTo(
      Math.addExact(this.writer.position(), header_size),
      (byte) 0x0);

    this.writer.putBytes(data);
  }
}
