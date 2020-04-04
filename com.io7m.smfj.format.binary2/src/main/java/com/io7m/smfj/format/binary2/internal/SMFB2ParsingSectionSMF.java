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
import com.io7m.smfj.core.SMFAttribute;
import com.io7m.smfj.core.SMFHeader;
import com.io7m.smfj.core.SMFPartial;
import com.io7m.smfj.core.SMFSchemaIdentifier;
import com.io7m.smfj.core.SMFTriangles;
import java.io.IOException;
import java.nio.ByteOrder;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.OptionalLong;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The 'smf' section.
 */

public final class SMFB2ParsingSectionSMF
  implements SMFB2StructureParserType<Optional<SMFHeader>>
{
  private static final Logger LOG =
    LoggerFactory.getLogger(SMFB2ParsingSectionSMF.class);

  private final SMFB2Section sectionHeader;

  public SMFB2ParsingSectionSMF(
    final SMFB2Section inSectionHeader)
  {
    this.sectionHeader =
      Objects.requireNonNull(inSectionHeader, "sectionHeader");
  }

  /**
   * @return The magic number identifying the section.
   */

  public static long magic()
  {
    return 0x534D_465F_4845_4144L;
  }

  static long baseFieldsSizeIn2_0()
  {
    var size = 0L;
    size += SMFB2ParsingSchemaIdentifier.schemaIdentifierSize();
    /* vertexCount */
    size += 8L;
    /* triangleCount */
    size += 8L;
    /* triangleSizeBits */
    size += 4L;
    /* attributeCount */
    size += 4L;
    /* coordinateSystem */
    size += 4L;
    /* dataByteOrder */
    size += 4L;
    return size;
  }

  private static void skipFieldsEnd(
    final BSSReaderType reader,
    final long fieldsSize,
    final long offsetThen)
    throws IOException
  {
    final var offsetNow = reader.offsetCurrentRelative();
    final var haveRead = offsetNow - offsetThen;
    final var toSkip = Math.max(0L, fieldsSize - haveRead);

    if (LOG.isTraceEnabled()) {
      LOG.trace("have read: {}", Long.valueOf(haveRead));
      LOG.trace("offset now: {}", Long.valueOf(offsetNow));
      LOG.trace("to skip: {}", Long.valueOf(toSkip));
    }

    reader.skip(toSkip);
  }

  private static OptionalLong parseFields(
    final SMFB2ParsingContextType context,
    final BSSReaderType reader,
    final SMFHeader.Builder builder)
    throws IOException
  {
    final var vertexCount =
      reader.readU64BE("vertexCount");
    final var triangleCount =
      reader.readU64BE("triangleCount");
    final var triangleSizeBits =
      reader.readU32BE("triangleSizeBits");
    final var attributeCount =
      reader.readU32BE("attributeCount");

    if (LOG.isTraceEnabled()) {
      LOG.trace(
        "parsed vertex count: {}",
        Long.toUnsignedString(vertexCount));
      LOG.trace(
        "parsed triangle count: {}",
        Long.toUnsignedString(triangleCount));
      LOG.trace(
        "parsed triangle size: {}",
        Long.toUnsignedString(triangleSizeBits));
      LOG.trace(
        "parsed attribute count: {}",
        Long.toUnsignedString(attributeCount));
    }

    builder.setVertexCount(vertexCount);

    try {
      builder.setTriangles(
        SMFTriangles.of(triangleCount, (int) triangleSizeBits));
    } catch (final UnsupportedOperationException e) {
      context.publishError(SMFB2ParseErrors.errorOfException(reader, e));
      return OptionalLong.empty();
    }

    final var coordinateOpt =
      new SMFB2ParsingCoordinateSystem().parse(context);

    if (coordinateOpt.isEmpty()) {
      return OptionalLong.empty();
    }
    builder.setCoordinateSystem(coordinateOpt.get());

    final var dataByteOrder =
      reader.readU32BE("dataByteOrder");

    final ByteOrder order;
    if (dataByteOrder == 0L) {
      order = ByteOrder.BIG_ENDIAN;
    } else if (dataByteOrder == 1L) {
      order = ByteOrder.LITTLE_ENDIAN;
    } else {
      context.publishError(SMFB2ParseErrors.errorOf(
        reader,
        "Unrecognized byte order: Expected big-endian (0x0) or little-endian (0x1), but received 0x%s",
        Long.toUnsignedString(dataByteOrder, 16)
      ));
      return OptionalLong.empty();
    }

    builder.setDataByteOrder(order);
    LOG.trace("parsed byte order: {}", order);
    return OptionalLong.of(attributeCount);
  }

  @Override
  public Optional<SMFHeader> parse(final SMFB2ParsingContextType context)
    throws IOException
  {
    return context.withReader(
      "smfData",
      this.sectionHeader.sizeOfData(),
      reader -> this.parseWithReader(context, reader));
  }

  private Optional<SMFHeader> parseWithReader(
    final SMFB2ParsingContextType context,
    final BSSReaderType reader)
    throws IOException
  {
    if (!SMFB2ParsingSectionHeader.checkHeader(
      context,
      reader,
      this.sectionHeader,
      magic(),
      "smf")) {
      return Optional.empty();
    }

    if (LOG.isDebugEnabled()) {
      LOG.debug(
        "section '{}' @ 0x{}",
        "smf",
        Long.toUnsignedString(reader.offsetCurrentAbsolute(), 16));
    }

    LOG.trace(
      "expecting to parse {} bytes of section data",
      Long.valueOf(this.sectionHeader.sizeOfData()));

    final var declaredFieldsSize = reader.readU32BE("fieldsSize");
    LOG.trace(
      "parsed fields size: {}",
      Long.valueOf(declaredFieldsSize));

    if (Long.compareUnsigned(
      declaredFieldsSize,
      baseFieldsSizeIn2_0()) < 0) {
      context.publishError(
        SMFB2ParseErrors.errorOf(
          reader,
          "Specified fields size %s is too small (must be at least %s)",
          Long.toUnsignedString(declaredFieldsSize),
          Long.toUnsignedString(baseFieldsSizeIn2_0())
        )
      );
      return Optional.empty();
    }

    final var offsetThen = reader.offsetCurrentRelative();
    final SMFPartial<Optional<SMFSchemaIdentifier>> identifierValidated =
      new SMFB2ParsingSchemaIdentifier().parse(context);

    if (identifierValidated.isFailed()) {
      return Optional.empty();
    }

    final var identifierOpt = identifierValidated.get();
    final SMFHeader.Builder builder = SMFHeader.builder();
    builder.setSchemaIdentifier(identifierOpt);

    final OptionalLong attributeCountOpt =
      parseFields(context, reader, builder);
    if (attributeCountOpt.isEmpty()) {
      return Optional.empty();
    }

    skipFieldsEnd(reader, declaredFieldsSize, offsetThen);
    final List<SMFAttribute> attributes =
      new SMFB2ParsingAttributeList(attributeCountOpt.getAsLong())
        .parse(context);

    builder.setAttributesInOrder(attributes);
    return Optional.of(builder.build());
  }
}
