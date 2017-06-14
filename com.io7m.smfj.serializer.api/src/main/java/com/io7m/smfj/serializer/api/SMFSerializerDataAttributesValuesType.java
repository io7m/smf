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

package com.io7m.smfj.serializer.api;

import java.io.Closeable;
import java.io.IOException;

/**
 * Functions for serializing values.
 */

public interface SMFSerializerDataAttributesValuesType extends Closeable
{
  /**
   * Serialize a value.
   *
   * @param x The x value
   * @param y The y value
   * @param z The z value
   * @param w The w value
   *
   * @throws IllegalStateException   If the header has not yet been serialized
   * @throws IllegalStateException   If the serializer has previously failed
   * @throws IllegalArgumentException If the current attribute is not of a type
   *                                  appropriate to this method call
   * @throws IOException              On I/O errors
   */

  void serializeValueFloat4(
    double x,
    double y,
    double z,
    double w)
    throws IOException, IllegalArgumentException, IllegalStateException;

  /**
   * Serialize a value.
   *
   * @param x The x value
   * @param y The y value
   * @param z The z value
   *
   * @throws IllegalStateException   If the header has not yet been serialized
   * @throws IllegalStateException   If the serializer has previously failed
   * @throws IllegalArgumentException If the current attribute is not of a type
   *                                  appropriate to this method call
   * @throws IOException              On I/O errors
   */

  void serializeValueFloat3(
    double x,
    double y,
    double z)
    throws IOException, IllegalArgumentException, IllegalStateException;

  /**
   * Serialize a value.
   *
   * @param x The x value
   * @param y The y value
   *
   * @throws IllegalStateException   If the header has not yet been serialized
   * @throws IllegalStateException   If the serializer has previously failed
   * @throws IllegalArgumentException If the current attribute is not of a type
   *                                  appropriate to this method call
   * @throws IOException              On I/O errors
   */

  void serializeValueFloat2(
    double x,
    double y)
    throws IOException, IllegalArgumentException, IllegalStateException;

  /**
   * Serialize a value.
   *
   * @param x The x value
   *
   * @throws IllegalStateException   If the header has not yet been serialized
   * @throws IllegalStateException   If the serializer has previously failed
   * @throws IllegalArgumentException If the current attribute is not of a type
   *                                  appropriate to this method call
   * @throws IOException              On I/O errors
   */

  void serializeValueFloat1(
    double x)
    throws IOException, IllegalArgumentException, IllegalStateException;

  /**
   * Serialize a value.
   *
   * @param x The x value
   * @param y The y value
   * @param z The z value
   * @param w The w value
   *
   * @throws IllegalStateException   If the header has not yet been serialized
   * @throws IllegalStateException   If the serializer has previously failed
   * @throws IllegalArgumentException If the current attribute is not of a type
   *                                  appropriate to this method call
   * @throws IOException              On I/O errors
   */

  void serializeValueIntegerSigned4(
    long x,
    long y,
    long z,
    long w)
    throws IOException, IllegalArgumentException, IllegalStateException;

  /**
   * Serialize a value.
   *
   * @param x The x value
   * @param y The y value
   * @param z The z value
   *
   * @throws IllegalStateException   If the header has not yet been serialized
   * @throws IllegalStateException   If the serializer has previously failed
   * @throws IllegalArgumentException If the current attribute is not of a type
   *                                  appropriate to this method call
   * @throws IOException              On I/O errors
   */

  void serializeValueIntegerSigned3(
    long x,
    long y,
    long z)
    throws IOException, IllegalArgumentException, IllegalStateException;

  /**
   * Serialize a value.
   *
   * @param x The x value
   * @param y The y value
   *
   * @throws IllegalStateException   If the header has not yet been serialized
   * @throws IllegalStateException   If the serializer has previously failed
   * @throws IllegalArgumentException If the current attribute is not of a type
   *                                  appropriate to this method call
   * @throws IOException              On I/O errors
   */

  void serializeValueIntegerSigned2(
    long x,
    long y)
    throws IOException, IllegalArgumentException, IllegalStateException;

  /**
   * Serialize a value.
   *
   * @param x The x value
   *
   * @throws IllegalStateException   If the header has not yet been serialized
   * @throws IllegalStateException   If the serializer has previously failed
   * @throws IllegalArgumentException If the current attribute is not of a type
   *                                  appropriate to this method call
   * @throws IOException              On I/O errors
   */

  void serializeValueIntegerSigned1(
    long x)
    throws IOException, IllegalArgumentException, IllegalStateException;

  /**
   * Serialize a value.
   *
   * @param x The x value
   * @param y The y value
   * @param z The z value
   * @param w The w value
   *
   * @throws IllegalStateException   If the header has not yet been serialized
   * @throws IllegalStateException   If the serializer has previously failed
   * @throws IllegalArgumentException If the current attribute is not of a type
   *                                  appropriate to this method call
   * @throws IOException              On I/O errors
   */

  void serializeValueIntegerUnsigned4(
    long x,
    long y,
    long z,
    long w)
    throws IOException, IllegalArgumentException, IllegalStateException;

  /**
   * Serialize a value.
   *
   * @param x The x value
   * @param y The y value
   * @param z The z value
   *
   * @throws IllegalStateException   If the header has not yet been serialized
   * @throws IllegalStateException   If the serializer has previously failed
   * @throws IllegalArgumentException If the current attribute is not of a type
   *                                  appropriate to this method call
   * @throws IOException              On I/O errors
   */

  void serializeValueIntegerUnsigned3(
    long x,
    long y,
    long z)
    throws IOException, IllegalArgumentException, IllegalStateException;

  /**
   * Serialize a value.
   *
   * @param x The x value
   * @param y The y value
   *
   * @throws IllegalStateException   If the header has not yet been serialized
   * @throws IllegalStateException   If the serializer has previously failed
   * @throws IllegalArgumentException If the current attribute is not of a type
   *                                  appropriate to this method call
   * @throws IOException              On I/O errors
   */

  void serializeValueIntegerUnsigned2(
    long x,
    long y)
    throws IOException, IllegalArgumentException, IllegalStateException;

  /**
   * Serialize a value.
   *
   * @param x The x value
   *
   * @throws IllegalStateException   If the header has not yet been serialized
   * @throws IllegalStateException   If the serializer has previously failed
   * @throws IllegalArgumentException If the current attribute is not of a type
   *                                  appropriate to this method call
   * @throws IOException              On I/O errors
   */

  void serializeValueIntegerUnsigned1(
    long x)
    throws IOException, IllegalArgumentException, IllegalStateException;
}
