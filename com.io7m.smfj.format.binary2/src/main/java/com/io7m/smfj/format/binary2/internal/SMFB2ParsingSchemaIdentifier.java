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

import com.io7m.smfj.core.SMFPartial;
import com.io7m.smfj.core.SMFSchemaIdentifier;
import com.io7m.smfj.core.SMFSchemaName;
import com.io7m.smfj.core.SMFSchemaNames;
import java.io.IOException;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static java.nio.charset.StandardCharsets.UTF_8;

public final class SMFB2ParsingSchemaIdentifier
  implements SMFB2StructureParserType<SMFPartial<Optional<SMFSchemaIdentifier>>>
{
  private static final Logger LOG =
    LoggerFactory.getLogger(SMFB2ParsingSchemaIdentifier.class);

  SMFB2ParsingSchemaIdentifier()
  {

  }

  public static long schemaIdentifierSize()
  {
    return 4L + 64L + 4L + 4L;
  }

  private static void logSchema(
    final String name,
    final long versionMajor,
    final long versionMinor)
  {
    if (LOG.isTraceEnabled()) {
      LOG.trace("parsed schema name: {}", name);
      LOG.trace("parsed version major: {}", Long.valueOf(versionMajor));
      LOG.trace("parsed version minor: {}", Long.valueOf(versionMinor));
    }
  }

  @Override
  public SMFPartial<Optional<SMFSchemaIdentifier>> parse(
    final SMFB2ParsingContextType context)
    throws IOException
  {
    return context.withReader("schemaId", schemaIdentifierSize(), reader -> {
      final var nameLength = reader.readU32BE("nameLength");
      if (Long.compareUnsigned(nameLength, 64L) > 0) {
        context.publishError(
          SMFB2ParseErrors.errorOf(
            reader,
            "Name length %s is longer than 64",
            Long.toUnsignedString(nameLength)
          )
        );
        return SMFPartial.failed();
      }

      final var bytes = new byte[64];
      reader.readBytes(bytes);

      // CHECKSTYLE:OFF
      final var name =
        new String(bytes, 0, (int) nameLength, UTF_8);
      // CHECKSTYLE:ON

      final var versionMajor =
        reader.readU32BE("versionMajor");
      final var versionMinor =
        reader.readU32BE("versionMinor");

      logSchema(name, versionMajor, versionMinor);

      if (nameLength == 0L) {
        return SMFPartial.succeeded(Optional.empty());
      }

      if (!SMFSchemaNames.isValid(name)) {
        context.publishError(
          SMFB2ParseErrors.errorOf(
            reader,
            "Schema name '%s' is not valid; must match %s and have length <= 64",
            name,
            SMFSchemaNames.PATTERN.pattern()
          )
        );
        return SMFPartial.failed();
      }

      return SMFPartial.succeeded(
        Optional.of(SMFSchemaIdentifier.of(
          SMFSchemaName.of(name),
          (int) versionMajor,
          (int) versionMinor
        )));
    });
  }
}
