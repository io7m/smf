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
import com.io7m.smfj.core.SMFFormatVersion;
import com.io7m.smfj.core.SMFPartialLogged;
import java.io.IOException;
import java.util.Optional;

public final class SMFB2ParsingFileHeader
  implements SMFB2StructureParserType<Optional<SMFFormatVersion>>
{
  public SMFB2ParsingFileHeader()
  {

  }

  /**
   * @return The magic number used to identify SMF files.
   */

  public static long magic()
  {
    return 0x8953_4D46_0D0A_1A0AL;
  }

  /**
   * Parse a file header from the given reader.
   *
   * @param reader The reader
   *
   * @return The result of parsing
   */

  public static SMFPartialLogged<SMFFormatVersion> parseDirectly(
    final BSSReaderType reader)
  {
    try {
      final var magic = reader.readU64BE("magic");
      if (magic != magic()) {
        return SMFPartialLogged.failed(
          SMFB2ParseErrors.errorOf(
            reader,
            "File does not seem to be an SMF file: Expected magic number 0x%s but received 0x%s",
            Long.toUnsignedString(magic(), 16),
            Long.toUnsignedString(magic, 16))
        );
      }

      final var major = reader.readU32BE("versionMajor");
      if (major != 2L) {
        return SMFPartialLogged.failed(
          SMFB2ParseErrors.errorOf(
            reader,
            "Unsupported major version %s",
            Long.toUnsignedString(major, 10))
        );
      }

      final var minor = reader.readU32BE("versionMinor");
      return SMFPartialLogged.succeeded(
        SMFFormatVersion.of((int) major, (int) minor));
    } catch (final IOException e) {
      return SMFPartialLogged.failed(
        SMFB2ParseErrors.errorOfException(reader, e)
      );
    }
  }

  private static Optional<SMFFormatVersion> parseWithReader(
    final SMFB2ParsingContextType context,
    final BSSReaderType reader)
  {
    final var parsed = parseDirectly(reader);

    for (final var warning : parsed.warnings()) {
      context.publishWarning(warning);
    }
    for (final var error : parsed.errors()) {
      context.publishError(error);
    }

    if (parsed.isSucceeded()) {
      return Optional.of(parsed.get());
    }
    return Optional.empty();
  }

  @Override
  public Optional<SMFFormatVersion> parse(
    final SMFB2ParsingContextType context)
    throws IOException
  {
    return context.withReader("fileHeader", 16L, reader -> {
      return parseWithReader(context, reader);
    });
  }
}
