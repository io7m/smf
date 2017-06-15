/*
 * Copyright © 2016 <code@io7m.com> http://io7m.com
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

import com.io7m.smfj.core.SMFFormatVersion;

import java.util.Optional;

/**
 * A receiver of parse events.
 */

public interface SMFParserEventsType extends SMFParserEventsErrorType
{
  /**
   * Parsing has started.
   */

  void onStart();

  /**
   * The file format version has been successfully parsed. The functions should
   * return a receiver for the header information if parsing should continue, or
   * {@link Optional#empty()} if parsing should stop.
   *
   * @param version The file format version
   *
   * @return A receiver for the header, if any
   */

  Optional<SMFParserEventsHeaderType> onVersionReceived(
    SMFFormatVersion version);

  /**
   * Parsing has finished. This method will be called unconditionally when the
   * parser is closed, regardless of any errors encountered.
   */

  void onFinish();
}
