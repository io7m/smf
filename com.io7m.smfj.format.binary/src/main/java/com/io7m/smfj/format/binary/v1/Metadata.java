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

import com.io7m.jpra.runtime.java.JPRACursor1DByteBufferedChecked;
import com.io7m.jpra.runtime.java.JPRAStringTruncation;
import com.io7m.junreachable.UnreachableCodeException;
import com.io7m.smfj.core.SMFSchemaIdentifier;
import com.io7m.smfj.format.binary.SMFBAlignment;
import com.io7m.smfj.format.binary.SMFBDataStreamWriterType;
import com.io7m.smfj.format.binary.SMFBSection;
import com.io7m.smfj.format.binary.SMFBSectionMetadata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

final class Metadata
{
  private static final Logger LOG;

  static {
    LOG = LoggerFactory.getLogger(Metadata.class);
  }

  private Metadata()
  {
    throw new UnreachableCodeException();
  }

  public static void serializeMetadata(
    final SMFBDataStreamWriterType writer,
    final SMFSchemaIdentifier schema,
    final byte[] data)
    throws IOException
  {
    if (LOG.isDebugEnabled()) {
      LOG.debug(
        "serializeMetadata: {} ({} octets)",
        schema.toHumanString(),
        Integer.valueOf(data.length));
    }

    final byte[] meta_header_buffer =
      new byte[SMFBv1MetadataIDByteBuffered.sizeInOctets()];

    final SMFBv1MetadataIDType view =
      JPRACursor1DByteBufferedChecked.newCursor(
        ByteBuffer.wrap(meta_header_buffer).order(ByteOrder.BIG_ENDIAN),
        SMFBv1MetadataIDByteBuffered::newValueWithOffset)
        .getElementView();

    view.getMetaSchemaIdWritable()
      .setValue(schema.name().value(), JPRAStringTruncation.REJECT);
    view.setMetaSchemaVersionMajor(schema.versionMajor());
    view.setMetaSchemaVersionMinor(schema.versionMinor());
    view.setMetaSize(data.length);

    final long meta_header_size =
      SMFBAlignment.alignNext(
        Integer.toUnsignedLong(meta_header_buffer.length),
        SMFBSection.SECTION_ALIGNMENT);
    final long meta_data_size =
      SMFBAlignment.alignNext(
        Integer.toUnsignedLong(data.length), SMFBSection.SECTION_ALIGNMENT);
    final long size_total =
      Math.addExact(meta_header_size, meta_data_size);

    writer.putU64(SMFBSectionMetadata.MAGIC);
    writer.putU64(size_total);

    final long position_header = writer.position();
    writer.putBytes(meta_header_buffer);
    writer.padTo(
      Math.addExact(position_header, meta_header_size),
      (byte) 0);

    writer.putBytes(data);
    writer.padTo(
      SMFBAlignment.alignNext(writer.position(), SMFBSection.SECTION_ALIGNMENT),
      (byte) 0);
  }
}
