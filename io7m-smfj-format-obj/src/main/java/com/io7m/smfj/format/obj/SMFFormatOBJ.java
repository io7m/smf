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

package com.io7m.smfj.format.obj;

import com.io7m.smfj.core.SMFFormatDescription;
import com.io7m.smfj.core.SMFFormatVersion;
import com.io7m.smfj.parser.api.SMFParserEventsType;
import com.io7m.smfj.parser.api.SMFParserProviderType;
import com.io7m.smfj.parser.api.SMFParserRandomAccessType;
import com.io7m.smfj.parser.api.SMFParserSequentialType;
import javaslang.collection.SortedSet;
import javaslang.collection.TreeSet;

import java.io.InputStream;
import java.nio.channels.FileChannel;
import java.nio.file.Path;
import java.util.Optional;

/**
 * A provider for the Wavefront OBJ format.
 */

public final class SMFFormatOBJ implements SMFParserProviderType
{
  private static final SMFFormatDescription FORMAT;
  private static final SortedSet<SMFFormatVersion> SUPPORTED;

  static {

    {
      final SMFFormatDescription.Builder fb = SMFFormatDescription.builder();
      fb.setDescription("Wavefront OBJ");
      fb.setSuffix("obj");
      fb.setRandomAccess(false);
      fb.setName("obj");
      fb.setMimeType("application/wavefront-obj");
      FORMAT = fb.build();
    }

    {
      SUPPORTED = TreeSet.of(SMFFormatVersion.of(1, 0));
    }
  }

  /**
   * Construct an OBJ format provider.
   */

  public SMFFormatOBJ()
  {

  }

  @Override
  public SMFFormatDescription parserFormat()
  {
    return FORMAT;
  }

  @Override
  public SortedSet<SMFFormatVersion> parserSupportedVersions()
  {
    return SUPPORTED;
  }

  @Override
  public SMFParserSequentialType parserCreateSequential(
    final SMFParserEventsType events,
    final Path path,
    final InputStream stream)
    throws UnsupportedOperationException
  {
    return SMFOBJImporter.create(Optional.of(path), stream, events);
  }

  @Override
  public SMFParserRandomAccessType parserCreateRandomAccess(
    final SMFParserEventsType events,
    final Path path,
    final FileChannel file)
    throws UnsupportedOperationException
  {
    throw new UnsupportedOperationException();
  }
}