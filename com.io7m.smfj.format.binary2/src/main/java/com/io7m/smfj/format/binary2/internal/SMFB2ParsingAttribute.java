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
import com.io7m.smfj.core.SMFAttribute;
import com.io7m.smfj.core.SMFAttributeName;
import com.io7m.smfj.core.SMFComponentType;
import com.io7m.smfj.parser.api.SMFParseError;
import java.io.IOException;
import java.util.function.Consumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.io7m.smfj.core.SMFComponentType.ELEMENT_TYPE_FLOATING;
import static com.io7m.smfj.core.SMFComponentType.ELEMENT_TYPE_INTEGER_SIGNED;
import static com.io7m.smfj.core.SMFComponentType.ELEMENT_TYPE_INTEGER_UNSIGNED;
import static java.nio.charset.StandardCharsets.UTF_8;

public final class SMFB2ParsingAttribute implements SMFB2StructureParserType<SMFAttribute>
{
  private static final Logger LOG =
    LoggerFactory.getLogger(SMFB2ParsingAttribute.class);

  private final long index;

  public SMFB2ParsingAttribute(
    final long inIndex)
  {
    this.index = inIndex;
  }

  public static long attributeSize()
  {
    return 4L + 64L + 4L + 4L + 4L;
  }

  private static SMFComponentType componentOrdinal(
    final BSSReaderType reader,
    final Consumer<SMFParseError> errors,
    final int kind)
  {
    switch (kind) {
      case 0:
        return ELEMENT_TYPE_INTEGER_SIGNED;
      case 1:
        return ELEMENT_TYPE_INTEGER_UNSIGNED;
      case 2:
        return ELEMENT_TYPE_FLOATING;
      default: {
        errors.accept(
          SMFB2ParseErrors.errorOf(
            reader,
            "Invalid component type value: %d", Integer.valueOf(kind)
          )
        );
        return ELEMENT_TYPE_INTEGER_SIGNED;
      }
    }
  }

  @Override
  public SMFAttribute parse(
    final SMFB2ParsingContextType context)
    throws IOException
  {
    final String readerName =
      String.format("attribute[%s]", Long.toUnsignedString(this.index));

    return context.withReader(readerName, attributeSize(), reader -> {
      var nameLength = reader.readU32BE("nameLength");
      if (Long.compareUnsigned(nameLength, 64L) > 0) {
        context.publishError(
          SMFB2ParseErrors.errorOf(
            reader,
            "Name length %s is longer than 64",
            Long.toUnsignedString(nameLength)
          )
        );
        nameLength = 64L;
      }

      final var bytes = new byte[64];
      reader.readBytes(bytes);

      // CHECKSTYLE:OFF
      final var name =
        new String(bytes, 0, (int) nameLength, UTF_8);
      // CHECKSTYLE:ON

      final var kind =
        componentOrdinal(
          reader,
          context::publishError,
          (int) reader.readU32BE("kind"));
      final var count =
        reader.readU32BE("count");
      final var size =
        reader.readU32BE("size");

      if (LOG.isTraceEnabled()) {
        LOG.trace("parsed attribute name: {}", name);
        LOG.trace("parsed attribute component kind: {}", kind);
        LOG.trace("parsed attribute component count: {}", Long.valueOf(count));
        LOG.trace("parsed attribute component size: {}", Long.valueOf(count));
      }

      return SMFAttribute.of(
        SMFAttributeName.of(name),
        kind,
        (int) count,
        (int) size
      );
    });
  }
}
