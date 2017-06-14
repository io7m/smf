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

import com.io7m.smfj.core.SMFHeader;
import com.io7m.smfj.core.SMFSchemaIdentifier;

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
   * @throws IOException           On I/O errors
   * @throws IllegalStateException If the header has already been serialized
   */

  void serializeHeader(
    SMFHeader header)
    throws IllegalStateException, IOException;

  /**
   * <p>Start serializing non-interleaved vertex data.</p>
   *
   * <p>If the method raises an exception, the serializer is considered to have
   * <i>failed</i> and all subsequent method calls will raise {@link
   * IllegalArgumentException}.</p>
   *
   * @return A serializer for non-interleaved vertex data values
   *
   * @throws IllegalStateException If the header has not yet been serialized
   * @throws IOException           On I/O errors
   */

  SMFSerializerDataAttributesNonInterleavedType serializeVertexDataNonInterleavedStart()
    throws IllegalStateException, IOException;

  /**
   * <p>Start serializing triangles.</p>
   *
   * @return A serializer for triangles
   *
   * @throws IllegalStateException If the header has not yet been serialized
   * @throws IOException           On I/O errors
   */

  SMFSerializerDataTrianglesType serializeTrianglesStart()
    throws IllegalStateException, IOException;

  /**
   * <p>Serialize one item of metadata.</p>
   *
   * @param schema The schema ID
   * @param data   The data
   *
   * @throws IllegalStateException If the header has not yet been serialized
   * @throws IOException           On I/O errors
   */

  void serializeMetadata(
    SMFSchemaIdentifier schema,
    byte[] data)
    throws IllegalStateException, IOException;
}
