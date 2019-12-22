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

import com.io7m.jbssio.api.BSSReaderType;
import com.io7m.junreachable.UnreachableCodeException;
import com.io7m.smfj.core.SMFAttribute;
import com.io7m.smfj.core.SMFHeader;
import com.io7m.smfj.core.SMFVoid;
import com.io7m.smfj.format.binary2.SMFB2Section;
import com.io7m.smfj.parser.api.SMFParserEventsDataAttributeValuesType;
import com.io7m.smfj.parser.api.SMFParserEventsDataAttributesNonInterleavedType;
import java.io.IOException;
import java.nio.ByteOrder;
import java.util.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class SMFB2ParsingSectionVertexDataNI
  implements SMFB2StructureParserType<SMFVoid>
{
  private static final Logger LOG =
    LoggerFactory.getLogger(SMFB2ParsingSectionVertexDataNI.class);

  private final SMFParserEventsDataAttributesNonInterleavedType nonInterleaved;
  private final SMFB2Section sectionHeader;
  private final SMFHeader smfHeader;

  public SMFB2ParsingSectionVertexDataNI(
    final SMFB2Section inSectionHeader,
    final SMFHeader inSMFHeader,
    final SMFParserEventsDataAttributesNonInterleavedType inNonInterleaved)
  {
    this.sectionHeader =
      Objects.requireNonNull(inSectionHeader, "sectionHeader");
    this.smfHeader =
      Objects.requireNonNull(inSMFHeader, "smfHeader");
    this.nonInterleaved =
      Objects.requireNonNull(inNonInterleaved, "nonInterleaved");
  }

  /**
   * @return The magic number identifying the section.
   */

  public static long magic()
  {
    return 0x534D_465F_5644_4E49L;
  }

  private static void parseAttributeWithReader(
    final long vertexCount,
    final ByteOrder byteOrder,
    final SMFAttribute attribute,
    final BSSReaderType dataReader,
    final SMFParserEventsDataAttributeValuesType values)
    throws IOException
  {
    if (Objects.equals(byteOrder, ByteOrder.BIG_ENDIAN)) {
      SMFB2ParsingVertexDataBE.parseAttributeWithReaderBE(
        vertexCount, attribute, dataReader, values);
    } else if (Objects.equals(byteOrder, ByteOrder.LITTLE_ENDIAN)) {
      SMFB2ParsingVertexDataLE.parseAttributeWithReaderLE(
        vertexCount, attribute, dataReader, values);
    } else {
      throw new UnreachableCodeException();
    }
  }

  @Override
  public SMFVoid parse(final SMFB2ParsingContextType context)
    throws IOException
  {
    return context.withReader(
      "vertexData",
      this.sectionHeader.sizeOfData(),
      reader -> {
        this.parseWithReader(context, reader);
        return SMFVoid.void_();
      });
  }

  private void parseWithReader(
    final SMFB2ParsingContextType context,
    final BSSReaderType reader)
    throws IOException
  {
    try {
      if (!SMFB2ParsingSectionHeader.checkHeader(
        context,
        reader,
        this.sectionHeader,
        magic(),
        "vertex-data-non-interleaved")) {
        return;
      }

      if (LOG.isDebugEnabled()) {
        LOG.debug(
          "section '{}' @ 0x{}",
          "vertex-data-non-interleaved",
          Long.toUnsignedString(reader.offsetCurrentAbsolute(), 16));
      }

      for (final var attribute : this.smfHeader.attributesInOrder()) {
        final var sizeOfOne =
          Integer.toUnsignedLong(attribute.sizeOctets());
        final var sizeOfAll =
          sizeOfOne * this.smfHeader.vertexCount();
        final var sizeAligned =
          SMFB2Alignment.alignNext(sizeOfAll, 16);

        final var valuesOpt =
          this.nonInterleaved.onDataAttributeStart(attribute);

        if (valuesOpt.isPresent()) {
          final var values = valuesOpt.get();
          context.withReader(
            attribute.name().value(),
            sizeAligned,
            dataReader -> {
              try {
                parseAttributeWithReader(
                  this.smfHeader.vertexCount(),
                  this.smfHeader.dataByteOrder(),
                  attribute,
                  dataReader,
                  values);
                return SMFVoid.void_();
              } finally {
                values.onDataAttributeValueFinish();
              }
            });
        }
      }
    } finally {
      this.nonInterleaved.onDataAttributesNonInterleavedFinish();
    }
  }
}
