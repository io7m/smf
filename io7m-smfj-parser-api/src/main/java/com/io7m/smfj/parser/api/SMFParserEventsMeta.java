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

package com.io7m.smfj.parser.api;

import com.io7m.junreachable.UnreachableCodeException;

/**
 * Convenient metadata listener implementations.
 */

public final class SMFParserEventsMeta
{
  private SMFParserEventsMeta()
  {
    throw new UnreachableCodeException();
  }

  /**
   * @return A metadata listener that ignores all metadata
   */

  public static SMFParserEventsMetaType ignore()
  {
    return new Ignore();
  }

  private static final class Ignore implements SMFParserEventsMetaType
  {
    private Ignore()
    {
      // Nothing
    }

    @Override
    public boolean onMeta(
      final long vendor,
      final long schema,
      final long length)
    {
      return false;
    }

    @Override
    public void onMetaData(
      final long vendor,
      final long schema,
      final byte[] data)
    {
      throw new UnreachableCodeException();
    }
  }
}
