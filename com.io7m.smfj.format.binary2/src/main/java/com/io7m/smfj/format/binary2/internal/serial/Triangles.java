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

package com.io7m.smfj.format.binary2.internal.serial;

import com.io7m.jaffirm.core.Invariants;
import com.io7m.jbssio.api.BSSWriterSequentialType;
import com.io7m.smfj.core.SMFHeader;
import com.io7m.smfj.serializer.api.SMFSerializerDataTrianglesType;
import java.io.IOException;
import java.util.Objects;

public abstract class Triangles implements SMFSerializerDataTrianglesType
{
  private final BSSWriterSequentialType writer;
  private final SMFHeader header;

  protected Triangles(
    final BSSWriterSequentialType inWriter,
    final SMFHeader inHeader)
  {
    this.writer =
      Objects.requireNonNull(inWriter, "Writer");
    this.header =
      Objects.requireNonNull(inHeader, "Header");
  }

  protected final BSSWriterSequentialType writer()
  {
    return this.writer;
  }

  @Override
  public final void close()
    throws IOException
  {
    final var remaining = this.writer.bytesRemaining();
    if (remaining.isEmpty()) {
      throw new IllegalStateException(
        "Misused BaseValues: writer must be bounded");
    }

    final var end =
      this.writer.offsetCurrentRelative() + remaining.getAsLong();

    this.writer.padTo(end);

    Invariants.checkInvariantL(
      this.writer.offsetCurrentAbsolute(),
      x -> x % 16L == 0L,
      x -> "Data must be aligned");

    this.writer.close();
  }
}
