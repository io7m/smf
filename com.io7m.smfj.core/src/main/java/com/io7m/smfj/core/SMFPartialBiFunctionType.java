/*
 * Copyright © 2018 <code@io7m.com> http://io7m.com
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

package com.io7m.smfj.core;

/**
 * The type of two-argument partial functions.
 *
 * @param <A> The type of the first argument
 * @param <B> The type of the second argument
 * @param <C> The type of the return value
 * @param <E> The type of exceptions raised
 */

public interface SMFPartialBiFunctionType<A, B, C, E extends Exception>
{
  /**
   * Apply the function.
   *
   * @param a The first argument
   * @param b The second argument
   *
   * @return A value of {@code C}
   *
   * @throws E If required
   */

  C apply(
    A a,
    B b)
    throws E;
}
