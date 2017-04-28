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

package com.io7m.smfj.serializer.api;

import com.io7m.jfsm.core.FSMTransitionException;
import com.io7m.smfj.core.SMFAttributeName;
import com.io7m.smfj.core.SMFHeader;

import java.io.Closeable;
import java.io.IOException;

/**
 * The type of serializers.
 */

public interface SMFSerializerType extends Closeable
{
  /**
   * <p>Serialize the header data.</p>
   *
   * @param header The header
   *
   * @throws FSMTransitionException If the header has already been serialized
   * @throws FSMTransitionException If the serializer has previously failed
   */

  void serializeHeader(
    SMFHeader header)
    throws FSMTransitionException;

  /**
   * <p>Start serializing data.</p>
   *
   * <p>This method must be called exactly once.</p>
   *
   * <p>If the method raises an exception, the serializer is considered to have
   * <i>failed</i> and all subsequent method calls will raise {@link
   * IllegalArgumentException}.</p>
   *
   * @throws FSMTransitionException If the header has not yet been serialized
   * @throws FSMTransitionException If the serializer has previously failed
   * @throws IOException            On I/O errors
   */

  void serializeDataStart()
    throws FSMTransitionException, IOException;

  /**
   * <p>Start serializing data for a single attribute.</p>
   *
   * <p>This method must be called once for each attribute in the header passed
   * to {@link #serializeHeader(SMFHeader)} in the order the attributes are
   * specified by {@link SMFHeader#attributesInOrder()}.</p>
   *
   * <p>If the method raises an exception, the serializer is considered to have
   * <i>failed</i> and all subsequent method calls will raise {@link
   * IllegalArgumentException}.</p>
   *
   * @param name The attribute name
   *
   * @throws IllegalArgumentException Iff the given attribute is not the next
   *                                  expected attribute
   * @throws FSMTransitionException   If too few values have been serialized for
   *                                  the attribute previously passed to this
   *                                  method
   * @throws FSMTransitionException   If the header has not yet been serialized
   * @throws FSMTransitionException   If the serializer has previously failed
   * @throws IOException              On I/O errors
   */

  void serializeData(
    SMFAttributeName name)
    throws IllegalArgumentException, FSMTransitionException, IOException;

  /**
   * Serialize a value.
   *
   * @param x The x value
   * @param y The y value
   * @param z The z value
   * @param w The w value
   *
   * @throws FSMTransitionException   If the header has not yet been serialized
   * @throws FSMTransitionException   If the serializer has previously failed
   * @throws IllegalArgumentException If the current attribute is not of a type
   *                                  appropriate to this method call
   * @throws IOException              On I/O errors
   */

  void serializeValueFloat4(
    double x,
    double y,
    double z,
    double w)
    throws IOException, IllegalArgumentException, FSMTransitionException;

  /**
   * Serialize a value.
   *
   * @param x The x value
   * @param y The y value
   * @param z The z value
   *
   * @throws FSMTransitionException   If the header has not yet been serialized
   * @throws FSMTransitionException   If the serializer has previously failed
   * @throws IllegalArgumentException If the current attribute is not of a type
   *                                  appropriate to this method call
   * @throws IOException              On I/O errors
   */

  void serializeValueFloat3(
    double x,
    double y,
    double z)
    throws IOException, IllegalArgumentException, FSMTransitionException;

  /**
   * Serialize a value.
   *
   * @param x The x value
   * @param y The y value
   *
   * @throws FSMTransitionException   If the header has not yet been serialized
   * @throws FSMTransitionException   If the serializer has previously failed
   * @throws IllegalArgumentException If the current attribute is not of a type
   *                                  appropriate to this method call
   * @throws IOException              On I/O errors
   */

  void serializeValueFloat2(
    double x,
    double y)
    throws IOException, IllegalArgumentException, FSMTransitionException;

  /**
   * Serialize a value.
   *
   * @param x The x value
   *
   * @throws FSMTransitionException   If the header has not yet been serialized
   * @throws FSMTransitionException   If the serializer has previously failed
   * @throws IllegalArgumentException If the current attribute is not of a type
   *                                  appropriate to this method call
   * @throws IOException              On I/O errors
   */

  void serializeValueFloat1(
    double x)
    throws IOException, IllegalArgumentException, FSMTransitionException;


  /**
   * Serialize a value.
   *
   * @param x The x value
   * @param y The y value
   * @param z The z value
   * @param w The w value
   *
   * @throws FSMTransitionException   If the header has not yet been serialized
   * @throws FSMTransitionException   If the serializer has previously failed
   * @throws IllegalArgumentException If the current attribute is not of a type
   *                                  appropriate to this method call
   * @throws IOException              On I/O errors
   */

  void serializeValueIntegerSigned4(
    long x,
    long y,
    long z,
    long w)
    throws IOException, IllegalArgumentException, FSMTransitionException;

  /**
   * Serialize a value.
   *
   * @param x The x value
   * @param y The y value
   * @param z The z value
   *
   * @throws FSMTransitionException   If the header has not yet been serialized
   * @throws FSMTransitionException   If the serializer has previously failed
   * @throws IllegalArgumentException If the current attribute is not of a type
   *                                  appropriate to this method call
   * @throws IOException              On I/O errors
   */

  void serializeValueIntegerSigned3(
    long x,
    long y,
    long z)
    throws IOException, IllegalArgumentException, FSMTransitionException;

  /**
   * Serialize a value.
   *
   * @param x The x value
   * @param y The y value
   *
   * @throws FSMTransitionException   If the header has not yet been serialized
   * @throws FSMTransitionException   If the serializer has previously failed
   * @throws IllegalArgumentException If the current attribute is not of a type
   *                                  appropriate to this method call
   * @throws IOException              On I/O errors
   */

  void serializeValueIntegerSigned2(
    long x,
    long y)
    throws IOException, IllegalArgumentException, FSMTransitionException;

  /**
   * Serialize a value.
   *
   * @param x The x value
   *
   * @throws FSMTransitionException   If the header has not yet been serialized
   * @throws FSMTransitionException   If the serializer has previously failed
   * @throws IllegalArgumentException If the current attribute is not of a type
   *                                  appropriate to this method call
   * @throws IOException              On I/O errors
   */

  void serializeValueIntegerSigned1(
    long x)
    throws IOException, IllegalArgumentException, FSMTransitionException;

  /**
   * Serialize a value.
   *
   * @param x The x value
   * @param y The y value
   * @param z The z value
   * @param w The w value
   *
   * @throws FSMTransitionException   If the header has not yet been serialized
   * @throws FSMTransitionException   If the serializer has previously failed
   * @throws IllegalArgumentException If the current attribute is not of a type
   *                                  appropriate to this method call
   * @throws IOException              On I/O errors
   */

  void serializeValueIntegerUnsigned4(
    long x,
    long y,
    long z,
    long w)
    throws IOException, IllegalArgumentException, FSMTransitionException;

  /**
   * Serialize a value.
   *
   * @param x The x value
   * @param y The y value
   * @param z The z value
   *
   * @throws FSMTransitionException   If the header has not yet been serialized
   * @throws FSMTransitionException   If the serializer has previously failed
   * @throws IllegalArgumentException If the current attribute is not of a type
   *                                  appropriate to this method call
   * @throws IOException              On I/O errors
   */

  void serializeValueIntegerUnsigned3(
    long x,
    long y,
    long z)
    throws IOException, IllegalArgumentException, FSMTransitionException;

  /**
   * Serialize a value.
   *
   * @param x The x value
   * @param y The y value
   *
   * @throws FSMTransitionException   If the header has not yet been serialized
   * @throws FSMTransitionException   If the serializer has previously failed
   * @throws IllegalArgumentException If the current attribute is not of a type
   *                                  appropriate to this method call
   * @throws IOException              On I/O errors
   */

  void serializeValueIntegerUnsigned2(
    long x,
    long y)
    throws IOException, IllegalArgumentException, FSMTransitionException;

  /**
   * Serialize a value.
   *
   * @param x The x value
   *
   * @throws FSMTransitionException   If the header has not yet been serialized
   * @throws FSMTransitionException   If the serializer has previously failed
   * @throws IllegalArgumentException If the current attribute is not of a type
   *                                  appropriate to this method call
   * @throws IOException              On I/O errors
   */

  void serializeValueIntegerUnsigned1(
    long x)
    throws IOException, IllegalArgumentException, FSMTransitionException;

  /**
   * <p>Start serializing triangles.</p>
   *
   * <p>This method must be called exactly once.</p>
   *
   * <p>If the method raises an exception, the serializer is considered to have
   * <i>failed</i> and all subsequent method calls will raise {@link
   * FSMTransitionException}.</p>
   *
   * @throws FSMTransitionException If the attribute data has not yet been
   *                                serialized
   * @throws FSMTransitionException If the serializer has previously failed
   * @throws IOException            On I/O errors
   */

  void serializeTrianglesStart()
    throws FSMTransitionException, IOException;

  /**
   * Serialize a triangle.
   *
   * @param v0 The first vertex index
   * @param v1 The second vertex index
   * @param v2 The third vertex index
   *
   * @throws FSMTransitionException If the header has not yet been serialized
   * @throws FSMTransitionException If the attribute has not yet been
   *                                serialized
   * @throws FSMTransitionException If the required number of triangles have
   *                                already been serialized
   * @throws FSMTransitionException If the serializer has previously failed
   * @throws IOException            On I/O errors
   */

  void serializeTriangle(
    long v0,
    long v1,
    long v2)
    throws IOException, FSMTransitionException;

  /**
   * <p>Start serializing metadata.</p>
   *
   * <p>This method must be called exactly once.</p>
   *
   * <p>If the method raises an exception, the serializer is considered to have
   * <i>failed</i> and all subsequent method calls will raise {@link
   * IllegalArgumentException}.</p>
   *
   * @throws FSMTransitionException If the triangle data has not yet been
   *                                serialized
   * @throws FSMTransitionException If the serializer has previously failed
   * @throws IOException            On I/O errors
   */

  void serializeMetadataStart()
    throws FSMTransitionException, IOException;

  /**
   * <p>Serialize one item of metadata.</p>
   *
   * <p>Metadata is serialized after all other data in the file has been
   * serialized.</p>
   *
   * @param vendor The vendor ID
   * @param schema The schema ID
   * @param data   The data
   *
   * @throws FSMTransitionException If the header has not yet been serialized
   * @throws FSMTransitionException If the mesh data has not yet been
   *                                serialized
   * @throws FSMTransitionException If the triangle data has not yet been
   *                                serialized
   * @throws FSMTransitionException If no metadata was specified by the header
   * @throws FSMTransitionException If the serializer has previously failed
   * @throws IOException            On I/O errors
   */

  void serializeMetadata(
    long vendor,
    long schema,
    byte[] data)
    throws IOException, FSMTransitionException;
}