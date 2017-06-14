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

import com.io7m.smfj.core.SMFAttribute;

import java.util.Optional;

/**
 * A receiver of parse events for mesh attribute data. The expectation is that
 * the methods in this interface will be called by parsers after the call to
 * {@link SMFParserEventsHeaderType#onHeaderParsed(com.io7m.smfj.core.SMFHeader)}.
 */

public interface SMFParserEventsDataAttributesNonInterleavedType
  extends SMFParserEventsErrorType
{
  /**
   * <p>Parsing of data for the attribute has started.</p>
   *
   * <p>This method must return a receiver for the given attribute if it wants
   * to receive values, or {@link Optional#empty()} if it does not.</p>
   *
   * @param attribute The attribute
   *
   * @return A value receiver
   */

  Optional<SMFParserEventsDataAttributeValuesType> onDataAttributeStart(
    SMFAttribute attribute);

  /**
   * Called when parsing of all non-interleaved attribute data has finished.
   */

  void onDataAttributesNonInterleavedFinish();
}
