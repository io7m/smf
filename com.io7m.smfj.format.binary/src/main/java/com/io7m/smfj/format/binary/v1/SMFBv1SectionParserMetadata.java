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
import com.io7m.smfj.core.SMFHeader;
import com.io7m.smfj.core.SMFSchemaIdentifier;
import com.io7m.smfj.core.SMFSchemaName;
import com.io7m.smfj.format.binary.SMFBBodySectionParserType;
import com.io7m.smfj.format.binary.SMFBDataStreamReaderType;
import com.io7m.smfj.format.binary.SMFBSectionMetadata;
import com.io7m.smfj.parser.api.SMFParseError;
import com.io7m.smfj.parser.api.SMFParserEventsBodyType;
import com.io7m.smfj.parser.api.SMFParserEventsDataMetaType;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Objects;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A parser for metadata sections.
 */

public final class SMFBv1SectionParserMetadata
  implements SMFBBodySectionParserType
{
  private static final Logger LOG;

  static {
    LOG = LoggerFactory.getLogger(SMFBv1SectionParserMetadata.class);
  }

  /**
   * Construct a parser.
   */

  public SMFBv1SectionParserMetadata()
  {

  }

  @Override
  public long magic()
  {
    return SMFBSectionMetadata.MAGIC;
  }

  @Override
  public void parse(
    final SMFHeader header,
    final SMFParserEventsBodyType events,
    final SMFBDataStreamReaderType reader)
  {
    Objects.requireNonNull(header, "Header");
    Objects.requireNonNull(events, "Events");
    Objects.requireNonNull(reader, "Reader");

    try {
      final byte[] buffer =
        new byte[SMFBv1MetadataIDByteBuffered.sizeInOctets()];
      reader.readBytes(Optional.of("Metadata schema ID"), buffer);

      final SMFBv1MetadataIDType view =
        JPRACursor1DByteBufferedChecked.newCursor(
          ByteBuffer.wrap(buffer).order(ByteOrder.BIG_ENDIAN),
          SMFBv1MetadataIDByteBuffered::newValueWithOffset)
          .getElementView();

      final String name = view.getMetaSchemaIdReadable().getNewValue();
      final int major = view.getMetaSchemaVersionMajor();
      final int minor = view.getMetaSchemaVersionMinor();
      final int size = view.getMetaSize();

      if (LOG.isTraceEnabled()) {
        LOG.trace("name:  {}", name);
        LOG.trace("major: {}", Integer.valueOf(major));
        LOG.trace("minor: {}", Integer.valueOf(minor));
        LOG.trace("size:  {}", Integer.toUnsignedString(size));
      }

      final SMFSchemaIdentifier id =
        SMFSchemaIdentifier.of(SMFSchemaName.of(name), major, minor);

      final Optional<SMFParserEventsDataMetaType> meta_opt =
        events.onMeta(id);

      if (meta_opt.isPresent()) {
        final SMFParserEventsDataMetaType meta = meta_opt.get();
        final byte[] data = new byte[size];
        reader.readBytes(Optional.of("Metadata"), data);
        meta.onMetaData(id, data);
      }

    } catch (final IOException e) {
      events.onError(SMFParseError.of(
        reader.positionLexical(), e.getMessage(), Optional.of(e)));
    }
  }
}
