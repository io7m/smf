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
import java.io.IOException;
import java.util.Objects;

import static java.nio.charset.StandardCharsets.UTF_8;

public final class SMFB2WritingBoundedString implements SMFB2StructureWriterType<String>
{
  private final String name;
  private final int maxLength;

  public SMFB2WritingBoundedString(
    final String inName,
    final int inMaxLength)
  {
    this.name = Objects.requireNonNull(inName, "name");
    this.maxLength = inMaxLength;
  }

  @Override
  public void write(
    final BSSWriterSequentialType writer,
    final String value)
    throws IOException
  {
    try (var subWriter = writer.createSubWriter(this.name)) {
      final var dataInput = value.getBytes(UTF_8);
      final var dataWrite = new byte[this.maxLength];
      final var bound = Math.min(dataInput.length, this.maxLength);
      System.arraycopy(dataInput, 0, dataWrite, 0, bound);
      subWriter.writeU32BE(
        "length", Integer.toUnsignedLong(dataInput.length));
      subWriter.writeBytes(
        "data", dataWrite);
    }
  }
}
