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

import com.io7m.smfj.core.SMFFormatDescription;
import com.io7m.smfj.core.SMFFormatVersion;
import javaslang.collection.SortedSet;
import org.osgi.annotation.versioning.ProviderType;

import java.io.InputStream;
import java.nio.channels.FileChannel;
import java.nio.file.Path;

/**
 * The type of parser providers.
 */

@ProviderType
public interface SMFParserProviderType
{
  /**
   * @return The format that this provider supports
   */

  SMFFormatDescription parserFormat();

  /**
   * @return The supported versions of the format
   */

  SortedSet<SMFFormatVersion> parserSupportedVersions();

  /**
   * @param events The event receiver
   * @param path   The path referred to by the input stream, for diagnostic
   *               messages
   * @param stream An input stream
   *
   * @return A new parser for the format
   *
   * @throws UnsupportedOperationException If sequential parsing is not
   *                                       supported
   * @see SMFFormatDescription#randomAccess()
   */

  SMFParserSequentialType parserCreateSequential(
    SMFParserEventsType events,
    Path path,
    InputStream stream)
    throws UnsupportedOperationException;

  /**
   * @param events The event receiver
   * @param path   The path referred to by the input stream, for diagnostic
   *               messages
   * @param file   A file channel
   *
   * @return A new parser for the format
   *
   * @throws UnsupportedOperationException If random-access parsing is not
   *                                       supported
   * @see SMFFormatDescription#randomAccess()
   */

  SMFParserRandomAccessType parserCreateRandomAccess(
    SMFParserEventsType events,
    Path path,
    FileChannel file)
    throws UnsupportedOperationException;
}
