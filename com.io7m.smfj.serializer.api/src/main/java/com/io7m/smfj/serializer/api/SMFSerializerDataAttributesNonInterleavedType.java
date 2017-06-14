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

import com.io7m.smfj.core.SMFAttributeName;

import java.io.Closeable;
import java.io.IOException;

/**
 * The type of serializers for non-interleaved vertex data.
 */

public interface SMFSerializerDataAttributesNonInterleavedType extends Closeable
{
  /**
   * <p>Start serializing data for a single attribute.</p>
   *
   * <p>This method must be called once for each attribute in the header passed
   * to {@link SMFSerializerType#serializeHeader(com.io7m.smfj.core.SMFHeader)}
   * in the order the attributes are specified by {@link
   * com.io7m.smfj.core.SMFHeader#attributesInOrder()}.</p>
   *
   * <p>If the method raises an exception, the serializer is considered to have
   * <i>failed</i> and all subsequent method calls will raise {@link
   * IllegalArgumentException}.</p>
   *
   * @param name The attribute name
   *
   * @return A serializer for the data values
   *
   * @throws IllegalArgumentException Iff the given attribute is not the next
   *                                  expected attribute
   * @throws IllegalStateException   If too few values have been serialized for
   *                                  the attribute previously passed to this
   *                                  method
   * @throws IllegalStateException   If the header has not yet been serialized
   * @throws IllegalStateException   If the serializer has previously failed
   * @throws IOException              On I/O errors
   */

  SMFSerializerDataAttributesValuesType serializeData(
    SMFAttributeName name)
    throws IllegalArgumentException, IllegalStateException, IOException;
}
