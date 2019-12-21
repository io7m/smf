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
import com.io7m.smfj.format.binary2.SMFB2Section;
import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A parser that can parse the section header for an arbitrary section.
 */

public final class SMFB2ParsingSectionHeader implements SMFB2StructureParserType<SMFB2Section>
{
  private static final Logger LOG =
    LoggerFactory.getLogger(SMFB2ParsingSectionHeader.class);

  /**
   * Construct a parser.
   */

  public SMFB2ParsingSectionHeader()
  {

  }

  /**
   * Check that {@code header} has the expected ID. Publish an error to {@code context} if it does not.
   *
   * @param context       The parsing context
   * @param reader        The current reader
   * @param header        The header
   * @param expectedMagic The expected magic number
   * @param name          The name of the header section
   *
   * @return {@code true} if the header has the expected value
   */

  public static boolean checkHeader(
    final SMFB2ParsingContextType context,
    final BSSReaderType reader,
    final SMFB2Section header,
    final long expectedMagic,
    final String name)
  {
    if (header.id() != expectedMagic) {
      context.publishError(
        SMFB2ParseErrors.errorOf(
          reader,
          "Expected an '%s' section (id 0x%s) but received a section with id 0x%s",
          name,
          Long.toUnsignedString(expectedMagic, 16),
          Long.toUnsignedString(header.id(), 16)
        )
      );
      return false;
    }
    return true;
  }

  @Override
  public SMFB2Section parse(final SMFB2ParsingContextType context)
    throws IOException
  {
    return context.withReader("sectionHeader", 16L, reader -> {
      final var id = reader.readU64BE();
      final var size = reader.readU64BE();
      if (LOG.isTraceEnabled()) {
        LOG.trace("parsed id: 0x{}", Long.toUnsignedString(id, 16));
        LOG.trace("parsed size: {}", Long.toUnsignedString(size));
      }
      return SMFB2Section.of(id, size, reader.offsetCurrentAbsolute());
    });
  }
}
