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

import com.io7m.smfj.core.SMFAttribute;

/**
 * Events related to the parsing of headers.
 */

public interface SMFParserEventsHeaderType extends SMFParserEventsErrorType
{
  /**
   * Parsing of the header information has started.
   *
   * <p>If this method is called, then {@link #onHeaderFinish()}
   * is guaranteed to be called at some point in the
   * future, regardless of any errors encountered in the mean time.</p>
   */

  void onHeaderStart();

  /**
   * The number of vertices has been received.
   *
   * @param count The vertex count
   */

  void onHeaderVerticesCountReceived(
    long count);

  /**
   * The number of triangles has been received.
   *
   * @param count The triangle count
   */

  void onHeaderTrianglesCountReceived(
    long count);

  /**
   * The size of each triangle index in bits has been received.
   *
   * @param bits The triangle index size in bits
   */

  void onHeaderTrianglesIndexSizeReceived(
    long bits);

  /**
   * The number of attributes present in the file has been received.
   *
   * @param count The number of attributes
   */

  void onHeaderAttributeCountReceived(
    long count);

  /**
   * The definition for an attribute has been received.
   *
   * @param attribute The attribute definition
   */

  void onHeaderAttributeReceived(
    SMFAttribute attribute);

  /**
   * Parsing of the header has completed.
   *
   * At this point, parsing will only continue beyond this method call
   * if the following invariants hold:
   *
   * <ul>
   * <li>All attributes are uniquely named; there are no duplicate names.</li>
   * <li>The number of triangles has been specified.</li>
   * <li>The number of vertices has been specified.</li>
   * </ul>
   */

  void onHeaderFinish();
}
