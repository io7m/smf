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

import com.io7m.jlexing.core.LexicalPositions;
import com.io7m.junreachable.UnreachableCodeException;

import java.util.Optional;

/**
 * Convenient functions for constructing parse errors.
 */

public final class SMFParseErrors
{
  private SMFParseErrors()
  {
    throw new UnreachableCodeException();
  }

  /**
   * Construct a parse error for the given exception.
   *
   * @param e The exception
   *
   * @return A parse error
   */

  public static SMFParseError errorException(
    final Exception e)
  {
    return SMFParseError.of(
      LexicalPositions.zero(), e.getMessage(), Optional.of(e));
  }

  /**
   * Construct a parse error for the given exception.
   *
   * @param message The error message
   *
   * @return A parse error
   */

  public static SMFParseError errorWithMessage(
    final String message)
  {
    return SMFParseError.of(LexicalPositions.zero(), message, Optional.empty());
  }
}
