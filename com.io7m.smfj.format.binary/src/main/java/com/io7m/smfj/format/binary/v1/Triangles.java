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

package com.io7m.smfj.format.binary.v1;

import com.io7m.smfj.core.SMFHeader;
import com.io7m.smfj.format.binary.SMFBAlignment;
import com.io7m.smfj.format.binary.SMFBDataStreamWriterType;
import com.io7m.smfj.format.binary.SMFBSection;
import com.io7m.smfj.serializer.api.SMFSerializerDataTrianglesType;

import java.io.IOException;
import java.util.Objects;

abstract class Triangles implements SMFSerializerDataTrianglesType
{
  private final SMFBDataStreamWriterType writer;
  private final SMFHeader header;

  Triangles(
    final SMFBDataStreamWriterType in_writer,
    final SMFHeader in_header)
  {
    this.writer = Objects.requireNonNull(in_writer, "Writer");
    this.header = Objects.requireNonNull(in_header, "Header");
  }

  protected final SMFBDataStreamWriterType writer()
  {
    return this.writer;
  }

  @Override
  public final void close()
    throws IOException
  {
    final long position = this.writer.position();
    this.writer.padTo(
      SMFBAlignment.alignNext(position, SMFBSection.SECTION_ALIGNMENT),
      (byte) 0);
  }
}
