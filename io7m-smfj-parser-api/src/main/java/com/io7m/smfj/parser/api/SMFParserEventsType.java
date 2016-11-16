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
import com.io7m.smfj.core.SMFFormatVersion;

/**
 * A receiver of parse events.
 */

public interface SMFParserEventsType
{
  /**
   * Parsing has started.
   */

  void onStart();

  /**
   * An error has occurred. Parsing will continue but the file as a whole must
   * be considered invalid.
   *
   * @param e The error
   */

  void onError(
    SMFParseError e);

  /**
   * The file format version has been successfully parsed.
   *
   * @param version The file format version
   */

  void onVersionReceived(
    SMFFormatVersion version);

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

  /**
   * <p>Parsing of data for the attribute has started.</p>
   *
   * <p>If this method is called, then {@link #onDataAttributeFinish(SMFAttribute)}
   * is guaranteed to be called for the same attribute at some point in the
   * future, regardless of any errors encountered in the mean time.</p>
   *
   * @param attribute The attribute
   */

  void onDataAttributeStart(
    SMFAttribute attribute);

  /**
   * A data value has been received.
   *
   * @param x The x value
   */

  void onDataAttributeValueIntegerSigned1(
    long x);

  /**
   * A data value has been received.
   *
   * @param x The x value
   * @param y The y value
   */

  void onDataAttributeValueIntegerSigned2(
    long x,
    long y);

  /**
   * A data value has been received.
   *
   * @param x The x value
   * @param y The y value
   * @param z The z value
   */

  void onDataAttributeValueIntegerSigned3(
    long x,
    long y,
    long z);

  /**
   * A data value has been received.
   *
   * @param x The x value
   * @param y The y value
   * @param z The z value
   * @param w The w value
   */

  void onDataAttributeValueIntegerSigned4(
    long x,
    long y,
    long z,
    long w);

  /**
   * A data value has been received.
   *
   * @param x The x value
   */

  void onDataAttributeValueIntegerUnsigned1(
    long x);

  /**
   * A data value has been received.
   *
   * @param x The x value
   * @param y The y value
   */

  void onDataAttributeValueIntegerUnsigned2(
    long x,
    long y);

  /**
   * A data value has been received.
   *
   * @param x The x value
   * @param y The y value
   * @param z The z value
   */

  void onDataAttributeValueIntegerUnsigned3(
    long x,
    long y,
    long z);

  /**
   * A data value has been received.
   *
   * @param x The x value
   * @param y The y value
   * @param z The z value
   * @param w The w value
   */

  void onDataAttributeValueIntegerUnsigned4(
    long x,
    long y,
    long z,
    long w);

  /**
   * A data value has been received.
   *
   * @param x The x value
   */

  void onDataAttributeValueFloat1(
    double x);

  /**
   * A data value has been received.
   *
   * @param x The x value
   * @param y The y value
   */

  void onDataAttributeValueFloat2(
    double x,
    double y);

  /**
   * A data value has been received.
   *
   * @param x The x value
   * @param y The y value
   * @param z The z value
   */

  void onDataAttributeValueFloat3(
    double x,
    double y,
    double z);

  /**
   * A data value has been received.
   *
   * @param x The x value
   * @param y The y value
   * @param z The z value
   * @param w The w value
   */

  void onDataAttributeValueFloat4(
    double x,
    double y,
    double z,
    double w);

  /**
   * Parsing of data for the attribute has finished.
   *
   * @param attribute The attribute
   */

  void onDataAttributeFinish(
    SMFAttribute attribute);

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

  /**
   * Parsing has finished. This method will be called unconditionally at the
   * end of parsing, regardless of any errors encountered.
   */

  void onFinish();
}
