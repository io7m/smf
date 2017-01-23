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

import com.io7m.smfj.core.SMFFormatDescription;
import com.io7m.smfj.core.SMFFormatVersion;
import javaslang.collection.SortedSet;
import org.osgi.annotation.versioning.ProviderType;

import java.io.OutputStream;
import java.nio.file.Path;

/**
 * The type of serializer providers.
 */

@ProviderType
public interface SMFSerializerProviderType
{
  /**
   * @return The format that this provider supports
   */

  SMFFormatDescription serializerFormat();

  /**
   * @return The supported versions of the format
   */

  SortedSet<SMFFormatVersion> serializerSupportedVersions();

  /**
   * @param version The format version
   * @param path    The path referred to by the output stream, for diagnostic
   *                messages
   * @param stream  An output stream
   *
   * @return A new serializer for the format
   *
   * @throws UnsupportedOperationException If the given version is not
   *                                       supported
   */

  SMFSerializerType serializerCreate(
    SMFFormatVersion version,
    Path path,
    OutputStream stream)
    throws UnsupportedOperationException;
}
