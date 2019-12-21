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


package com.io7m.smfj.core;

import java.util.Objects;
import java.util.function.Function;

/**
 * The result of evaluating a partial function.
 *
 * @param <A> The type of result values.
 */

public final class SMFPartial<A>
{
  private final A value;

  private SMFPartial(
    final A inValue)
  {
    this.value = inValue;
  }

  @Override
  public boolean equals(final Object o)
  {
    if (this == o) {
      return true;
    }
    if (o == null || !Objects.equals(this.getClass(), o.getClass())) {
      return false;
    }
    final SMFPartial<?> that = (SMFPartial<?>) o;
    return Objects.equals(this.value, that.value);
  }

  @Override
  public int hashCode()
  {
    return Objects.hash(this.value);
  }

  /**
   * Fail an operation.
   *
   * @param <A> The type of result values
   *
   * @return A failed operation
   */

  public static <A> SMFPartial<A> failed()
  {
    return new SMFPartial<A>(null);
  }

  /**
   * Succeed an operation.
   *
   * @param inValue The result of the operation
   * @param <A>     The type of result values
   *
   * @return A succeeded operation
   */

  public static <A> SMFPartial<A> succeeded(
    final A inValue)
  {
    return new SMFPartial<>(
      Objects.requireNonNull(inValue, "value"));
  }

  /**
   * @return The result of the operation if it succeeded
   *
   * @throws IllegalStateException If the operation failed
   */

  public A get()
    throws IllegalStateException
  {
    if (this.isFailed()) {
      throw new IllegalStateException("Operation failed!");
    }
    return this.value;
  }

  /**
   * @return {@code true} if the operation failed
   */

  public boolean isFailed()
  {
    return this.value == null;
  }

  /**
   * @return {@code true} if the operation succeeded
   */

  public boolean isSucceeded()
  {
    return !this.isFailed();
  }

  /**
   * Functor map for partials.
   *
   * @param <B> The type of mapped values
   * @param f   A function to apply to the value
   *
   * @return {@code f(x)}
   */

  public <B> SMFPartial<B> map(
    final Function<A, B> f)
  {
    if (this.isSucceeded()) {
      return succeeded(f.apply(this.get()));
    }
    return failed();
  }

  /**
   * Monadic bind for partials.
   *
   * @param <B> The type of mapped values
   * @param f   A function to apply to the value
   *
   * @return {@code this >>= f}
   */

  public <B> SMFPartial<B> flatMap(
    final Function<A, SMFPartial<B>> f)
  {
    if (this.isSucceeded()) {
      return f.apply(this.get());
    }
    return failed();
  }
}
