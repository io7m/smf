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

import com.io7m.smfj.core.SMFSchemaIdentifier;

import java.io.Closeable;
import java.io.IOException;

/**
 * Functions for serializing metadata.
 */

public interface SMFSerializerDataMetaType extends Closeable
{
  /**
   * <p>Serialize one item of metadata.</p>
   *
   * <p>Metadata is serialized after all other data in the file has been
   * serialized.</p>
   *
   * @param schema The schema ID
   * @param data   The data
   *
   * @throws IllegalStateException If the header has not yet been serialized
   * @throws IllegalStateException If the mesh data has not yet been
   *                                serialized
   * @throws IllegalStateException If the triangle data has not yet been
   *                                serialized
   * @throws IllegalStateException If no metadata was specified by the header
   * @throws IllegalStateException If the serializer has previously failed
   * @throws IOException            On I/O errors
   */

  void serializeMetadata(
    SMFSchemaIdentifier schema,
    byte[] data)
    throws IOException, IllegalStateException;
}
