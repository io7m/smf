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

package com.io7m.smfj.format.binary2.internal;

import com.io7m.junreachable.UnreachableCodeException;

/**
 * Functions to calculate alignment.
 */

public final class SMFB2Alignment
{
  private SMFB2Alignment()
  {
    throw new UnreachableCodeException();
  }

  /**
   * Align the given value to the next multiple of {@code alignment}.
   *
   * @param value     The current value
   * @param alignment The alignment
   *
   * @return An aligned value
   */

  public static long alignNext(
    final long value,
    final int alignment)
  {
    return Math.multiplyExact(
      (long) Math.ceil((double) value / (double) alignment),
      (long) alignment);
  }
}
