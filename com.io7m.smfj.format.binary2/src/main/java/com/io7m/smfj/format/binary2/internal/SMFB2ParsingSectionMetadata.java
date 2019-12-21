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

package com.io7m.smfj.format.binary2.internal;

import com.io7m.jbssio.api.BSSReaderType;
import com.io7m.smfj.core.SMFMetadataValue;
import com.io7m.smfj.core.SMFSchemaIdentifier;
import com.io7m.smfj.format.binary2.SMFB2Section;
import com.io7m.smfj.parser.api.SMFParserEventsDataMetaOptionalSupplierType;
import com.io7m.smfj.parser.api.SMFParserEventsDataMetaType;
import java.io.IOException;
import java.util.Objects;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A 'metadata' section.
 */

public final class SMFB2ParsingSectionMetadata
  implements SMFB2StructureParserType<Optional<SMFMetadataValue>>
{
  private static final Logger LOG =
    LoggerFactory.getLogger(SMFB2ParsingSectionMetadata.class);

  private final SMFParserEventsDataMetaOptionalSupplierType metadataReceivers;
  private final SMFB2Section sectionHeader;

  /**
   * Construct a parser.
   *
   * @param inSectionHeader     The section header for this section
   * @param inMetadataReceivers A supplier of metadata receivers
   */

  public SMFB2ParsingSectionMetadata(
    final SMFParserEventsDataMetaOptionalSupplierType inMetadataReceivers,
    final SMFB2Section inSectionHeader)
  {
    this.metadataReceivers =
      Objects.requireNonNull(inMetadataReceivers, "metadataReceivers");
    this.sectionHeader =
      Objects.requireNonNull(inSectionHeader, "sectionHeader");
  }

  /**
   * @return The magic number identifying the section.
   */

  public static long magic()
  {
    return 0x534D_465F_4D45_5441L;
  }

  @Override
  public Optional<SMFMetadataValue> parse(
    final SMFB2ParsingContextType context)
    throws IOException
  {
    return context.withReader(
      "metadata",
      this.sectionHeader.sizeOfData(),
      reader -> this.parseWithReader(context, reader));
  }

  private Optional<SMFMetadataValue> parseWithReader(
    final SMFB2ParsingContextType context,
    final BSSReaderType reader)
    throws IOException
  {
    if (!SMFB2ParsingSectionHeader.checkHeader(
      context,
      reader,
      this.sectionHeader,
      magic(),
      "metadata")) {
      return Optional.empty();
    }

    if (LOG.isDebugEnabled()) {
      LOG.debug(
        "section '{}' @ 0x{}",
        "metadata",
        Long.toUnsignedString(reader.offsetCurrentAbsolute(), 16));
    }

    final var identifierValidated =
      new SMFB2ParsingSchemaIdentifier()
        .parse(context);

    final Optional<SMFSchemaIdentifier> identifierOpt;
    if (identifierValidated.isFailed()) {
      identifierOpt = Optional.empty();
    } else {
      identifierOpt = identifierValidated.get();
    }

    if (identifierOpt.isEmpty()) {
      context.publishError(
        SMFB2ParseErrors.errorOf(
          reader,
          "A valid schema identifier must be specified for metadata")
      );
      return Optional.empty();
    }

    final var identifier = identifierOpt.get();
    final var dataSize = this.readSize(context, reader);

    final Optional<SMFParserEventsDataMetaType> receiverOpt =
      this.metadataReceivers.onMeta(identifier);

    if (receiverOpt.isPresent()) {
      final var receiver = receiverOpt.get();
      final var data = new byte[Math.toIntExact(dataSize)];
      reader.readBytes(data);
      receiver.onMetaData(identifier, data);
      return Optional.of(SMFMetadataValue.of(identifier, data));
    }

    return Optional.empty();
  }

  private long readSize(
    final SMFB2ParsingContextType context,
    final BSSReaderType reader)
    throws IOException
  {
    final var metaSize =
      reader.readU32BE("metadataSize");
    final var remaining =
      reader.bytesRemaining().orElse(this.sectionHeader.sizeOfData());

    if (Long.compareUnsigned(metaSize, remaining) > 0) {
      context.publishError(
        SMFB2ParseErrors.errorOf(
          reader,
          "Metadata size is specified as %s but only %s bytes are remaining",
          Long.toUnsignedString(metaSize),
          Long.toUnsignedString(remaining))
      );
      return remaining;
    }
    return metaSize;
  }
}
