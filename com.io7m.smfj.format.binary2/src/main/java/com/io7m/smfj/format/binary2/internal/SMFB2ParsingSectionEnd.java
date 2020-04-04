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

import com.io7m.jbssio.api.BSSWriterType;
import com.io7m.smfj.core.SMFPartial;
import com.io7m.smfj.core.SMFVoid;
import java.io.IOException;
import java.util.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * An 'end' section.
 */

public final class SMFB2ParsingSectionEnd
  implements SMFB2StructureParserType<SMFPartial<SMFVoid>>
{
  private static final Logger LOG =
    LoggerFactory.getLogger(SMFB2ParsingSectionEnd.class);

  private final SMFB2Section sectionHeader;

  /**
   * Construct a parser.
   *
   * @param inSectionHeader The section header for this section
   */

  public SMFB2ParsingSectionEnd(
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
    return 0x534D_465F_454E_4421L;
  }

  public static void write(
    final BSSWriterType writer)
    throws IOException
  {
    writer.writeU64BE(magic());
    writer.writeU64BE(0L);
  }

  @Override
  public SMFPartial<SMFVoid> parse(final SMFB2ParsingContextType context)
    throws IOException
  {
    return context.withReader("end", 0L, reader -> {
      if (!SMFB2ParsingSectionHeader.checkHeader(
        context,
        reader,
        this.sectionHeader,
        magic(),
        "end")) {
        return SMFPartial.failed();
      }

      if (LOG.isDebugEnabled()) {
        LOG.debug(
          "section '{}' @ 0x{}",
          "end",
          Long.toUnsignedString(reader.offsetCurrentAbsolute(), 16));
      }

      return SMFPartial.succeeded(SMFVoid.void_());
    });
  }
}
