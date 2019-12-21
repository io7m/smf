/*
 * Copyright © 2019 Mark Raynsford <code@io7m.com> http://io7m.com
 *
 * Permission to use, copy, modify, and/or distribute this software for
 * any purpose with or without fee is hereby granted, provided that the
 * above copyright notice and this permission notice appear in all copies.
 *
 * THE SOFTWARE IS PROVIDED "AS IS" AND THE AUTHOR DISCLAIMS ALL
 * WARRANTIES WITH REGARD TO THIS SOFTWARE INCLUDING ALL IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS. IN NO EVENT SHALL THE AUTHOR
 * BE LIABLE FOR ANY SPECIAL, DIRECT, INDIRECT, OR CONSEQUENTIAL DAMAGES
 * OR ANY DAMAGES WHATSOEVER RESULTING FROM LOSS OF USE, DATA OR PROFITS,
 * WHETHER IN AN ACTION OF CONTRACT, NEGLIGENCE OR OTHER TORTIOUS ACTION,
 * ARISING OUT OF OR IN CONNECTION WITH THE USE OR PERFORMANCE OF THIS
 * SOFTWARE.
 */


package com.io7m.smfj.format.binary2.internal;

import com.io7m.jbssio.api.BSSWriterSequentialType;
import com.io7m.smfj.core.SMFMetadataValue;
import com.io7m.smfj.format.binary2.SMFB2Section;
import java.io.IOException;

public final class SMFB2WritingSectionMetadata
  implements SMFB2StructureWriterType<SMFMetadataValue>
{
  public SMFB2WritingSectionMetadata()
  {

  }

  @Override
  public void write(
    final BSSWriterSequentialType writer,
    final SMFMetadataValue value)
    throws IOException
  {
    writer.checkNotClosed();

    final var metaDataSize =
      Integer.toUnsignedLong(value.data().length);

    var sectionDataSize = 0L;
    sectionDataSize += SMFB2ParsingSchemaIdentifier.schemaIdentifierSize();
    sectionDataSize += 4L;
    sectionDataSize += metaDataSize;
    sectionDataSize = SMFB2Alignment.alignNext(sectionDataSize, 16);

    final var section =
      SMFB2Section.of(SMFB2ParsingSectionMetadata.magic(), sectionDataSize, 0L);

    new SMFB2WritingSectionHeader().write(writer, section);
    final long finalSectionDataSize = sectionDataSize;

    try (var subWriter =
           writer.createSubWriterBounded("metadata", sectionDataSize)) {
      new SMFB2WritingSchemaIdentifier().write(subWriter, value.schemaId());
      subWriter.writeU32BE("dataSize", metaDataSize);
      subWriter.writeBytes(value.data());
      subWriter.padTo(finalSectionDataSize);
    }
  }
}
