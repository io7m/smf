/*
 * Copyright © 2019 Mark Raynsford <code@io7m.com> http://io7m.com
 *
 * Permission to use, copy, modify, and/or distribute this software for
 * any purpose with or without fee is hereby granted, provided that the
 * above copyright notice and this permission notice appear in all copies.
 *
 * THE SOFTWARE IS PROVIDED "AS IS" AND THE AUTHOR DISCLAIMS ALL
 * WARRANTIES WITH REGARD TO THIS SOFTWARE INCLUDING ALL IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS. IN NO EVENT SHALL THE AUTHOR
 * BE LIABLE FOR ANY SPECIAL, DIRECT, INDIRECT, OR CONSEQUENTIAL DAMAGES
 * OR ANY DAMAGES WHATSOEVER RESULTING FROM LOSS OF USE, DATA OR PROFITS,
 * WHETHER IN AN ACTION OF CONTRACT, NEGLIGENCE OR OTHER TORTIOUS ACTION,
 * ARISING OUT OF OR IN CONNECTION WITH THE USE OR PERFORMANCE OF THIS
 * SOFTWARE.
 */


package com.io7m.smfj.format.binary2.internal;

import com.io7m.jbssio.api.BSSWriterSequentialType;
import com.io7m.smfj.format.binary2.SMFB2Section;
import java.io.IOException;

public final class SMFB2WritingSectionHeader implements SMFB2StructureWriterType<SMFB2Section>
{
  public SMFB2WritingSectionHeader()
  {

  }

  @Override
  public void write(
    final BSSWriterSequentialType writer,
    final SMFB2Section value)
    throws IOException
  {
    writer.checkNotClosed();

    try (var subWriter = writer.createSubWriterBounded("sectionHeader", 16L)) {
      subWriter.writeU64BE("id", value.id());
      subWriter.writeU64BE("size", value.sizeOfData());
    }
  }
}
