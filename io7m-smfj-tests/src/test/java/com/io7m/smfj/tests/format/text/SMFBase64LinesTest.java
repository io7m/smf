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

package com.io7m.smfj.tests.format.text;

import com.io7m.smfj.format.text.SMFBase64Lines;
import net.java.quickcheck.QuickCheck;
import net.java.quickcheck.characteristic.AbstractCharacteristic;
import net.java.quickcheck.generator.support.ByteArrayGenerator;
import org.junit.Assert;
import org.junit.Test;

import java.util.List;

public final class SMFBase64LinesTest
{
  @Test
  public void testRoundTrip()
  {
    QuickCheck.forAllVerbose(
      new ByteArrayGenerator(),
      new AbstractCharacteristic<byte[]>()
      {
        @Override
        protected void doSpecify(final byte[] data)
          throws Throwable
        {
          final List<String> lines = SMFBase64Lines.toBase64Lines(data);
          final byte[] r_data = SMFBase64Lines.fromBase64Lines(lines);
          Assert.assertArrayEquals(data, r_data);
        }
      });
  }

  public static void main(
    final String[] args)
  {
    final byte[] data = new byte[256];
    for (int index = 0; index < data.length; ++index) {
      data[index] = (byte) index;
    }

    SMFBase64Lines.toBase64Lines(data).forEach(System.out::println);
  }
}
