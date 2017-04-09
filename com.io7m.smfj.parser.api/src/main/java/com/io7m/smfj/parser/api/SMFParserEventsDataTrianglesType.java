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

/**
 * A receiver of parse events for triangle data. The expectation is that the
 * methods in this interface will be called by parsers after the call to {@link
 * SMFParserEventsHeaderType#onHeaderParsed(com.io7m.smfj.core.SMFHeader)}.
 */

public interface SMFParserEventsDataTrianglesType
{
  /**
   * Parsing of triangle data has started.
   *
   * <p>If this method is called, then {@link #onDataTrianglesFinish()}
   * is guaranteed to be called at some point in the
   * future, regardless of any errors encountered in the mean time.</p>
   */

  void onDataTrianglesStart();

  /**
   * A triangle has been parsed.
   *
   * @param v0 The index of the first vertex
   * @param v1 The index of the second vertex
   * @param v2 The index of the third vertex
   */

  void onDataTriangle(
    long v0,
    long v1,
    long v2);

  /**
   * Parsing of triangles has completed.
   */

  void onDataTrianglesFinish();
}
