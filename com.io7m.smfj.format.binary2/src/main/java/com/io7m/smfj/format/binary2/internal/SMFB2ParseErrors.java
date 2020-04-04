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
import com.io7m.jlexing.core.LexicalPosition;
import com.io7m.smfj.parser.api.SMFParseError;
import com.io7m.smfj.parser.api.SMFParseWarning;
import java.util.Optional;

public final class SMFB2ParseErrors
{
  private SMFB2ParseErrors()
  {

  }

  public static SMFParseError errorOf(
    final BSSReaderType reader,
    final String format,
    final Object... arguments)
  {
    final var lexical =
      LexicalPosition.of(
        (int) reader.offsetCurrentAbsolute(),
        0,
        Optional.of(reader.uri())
      );

    return SMFParseError.builder()
      .setLexical(lexical)
      .setMessage(String.format(format, arguments))
      .build();
  }

  public static SMFParseWarning warningOf(
    final BSSReaderType reader,
    final String format,
    final Object... arguments)
  {
    final var lexical =
      LexicalPosition.of(
        (int) reader.offsetCurrentAbsolute(),
        0,
        Optional.of(reader.uri())
      );

    return SMFParseWarning.builder()
      .setLexical(lexical)
      .setMessage(String.format(format, arguments))
      .build();
  }

  public static SMFParseError errorOfException(
    final BSSReaderType reader,
    final Exception e)
  {
    final var lexical =
      LexicalPosition.of(
        (int) reader.offsetCurrentAbsolute(),
        0,
        Optional.of(reader.uri())
      );

    return SMFParseError.builder()
      .setLexical(lexical)
      .setMessage(e.getMessage())
      .setException(e)
      .build();
  }
}
