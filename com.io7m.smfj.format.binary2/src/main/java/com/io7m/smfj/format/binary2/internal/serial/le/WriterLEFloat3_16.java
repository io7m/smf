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

package com.io7m.smfj.format.binary2.internal.serial.le;

import com.io7m.jbssio.api.BSSWriterSequentialType;
import com.io7m.smfj.core.SMFAttribute;
import com.io7m.smfj.format.binary2.internal.serial.WriterBase;
import java.io.IOException;

public final class WriterLEFloat3_16 extends WriterBase
{
  public WriterLEFloat3_16(
    final BSSWriterSequentialType in_writer,
    final SMFAttribute in_attribute)
  {
    super(in_writer, in_attribute);
  }

  @Override
  public void serializeValueFloat3(
    final double x,
    final double y,
    final double z)
    throws IOException
  {
    super.writer().writeF16LE(x);
    super.writer().writeF16LE(y);
    super.writer().writeF16LE(z);
  }
}
