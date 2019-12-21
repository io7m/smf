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

package com.io7m.smfj.format.text.v1;

import com.io7m.jlexing.core.LexicalPosition;
import com.io7m.junreachable.UnreachableCodeException;
import com.io7m.smfj.parser.api.SMFParseError;
import com.io7m.smfj.parser.api.SMFParseErrors;
import java.net.URI;
import java.util.List;

final class SMFTErrors
{
  private SMFTErrors()
  {
    throw new UnreachableCodeException();
  }

  static SMFParseError errorMalformedCommand(
    final String name,
    final String syntax,
    final List<String> line,
    final LexicalPosition<URI> position)
  {
    return SMFParseErrors.errorExpectedGot(
      String.format("Could not parse '%s' command.", name),
      syntax,
      String.join(" ", line),
      position);
  }

  static SMFParseError errorCommandExpectedGotWithException(
    final String name,
    final String syntax,
    final List<String> line,
    final LexicalPosition<URI> position,
    final Exception exception)
  {
    return errorExpectedGotWithException(
      String.format("Could not parse '%s' command.", name),
      syntax,
      line,
      position,
      exception);
  }

  static SMFParseError errorCommandExpectedGot(
    final String name,
    final String syntax,
    final List<String> line,
    final LexicalPosition<URI> position)
  {
    return errorExpectedGot(
      String.format("Could not parse '%s' command.", name),
      syntax,
      line,
      position);
  }

  static SMFParseError errorExpectedGotWithException(
    final String message,
    final String syntax,
    final List<String> line,
    final LexicalPosition<URI> position,
    final Exception exception)
  {
    return SMFParseErrors.errorExpectedGotWithException(
      message,
      syntax,
      String.join(" ", line),
      position,
      exception);
  }

  static SMFParseError errorExpectedGot(
    final String message,
    final String syntax,
    final List<String> line,
    final LexicalPosition<URI> position)
  {
    return SMFParseErrors.errorExpectedGot(
      message,
      syntax,
      String.join(" ", line),
      position);
  }
}
