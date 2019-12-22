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

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;

/**
 * The result of evaluating a partial function, with log messages.
 *
 * @param <A> The type of result values.
 */

public final class SMFPartialLogged<A>
{
  private List<? extends SMFErrorType> errors;
  private List<? extends SMFWarningType> warnings;
  private final A value;

  private SMFPartialLogged(
    final List<? extends SMFErrorType> inErrors,
    final List<? extends SMFWarningType> inWarnings,
    final A inValue)
  {
    this.errors =
      List.copyOf(Objects.requireNonNull(inErrors, "inErrors"));
    this.warnings =
      List.copyOf(Objects.requireNonNull(inWarnings, "inWarnings"));
    this.value = inValue;
  }

  /**
   * Fail an operation.
   *
   * @param <A>        The type of result values
   * @param inErrors   The errors encountered
   * @param inWarnings The warnings encountered
   *
   * @return A failed operation
   */

  public static <A> SMFPartialLogged<A> failed(
    final List<? extends SMFErrorType> inErrors,
    final List<? extends SMFWarningType> inWarnings)
  {
    return new SMFPartialLogged<>(inErrors, inWarnings, null);
  }

  /**
   * Fail an operation.
   *
   * @param <A>      The type of result values
   * @param inErrors The errors encountered
   *
   * @return A failed operation
   */

  public static <A> SMFPartialLogged<A> failed(
    final List<? extends SMFErrorType> inErrors)
  {
    return new SMFPartialLogged<>(inErrors, List.of(), null);
  }

  /**
   * Fail an operation.
   *
   * @param <A>     The type of result values
   * @param inError The error encountered
   *
   * @return A failed operation
   */

  public static <A> SMFPartialLogged<A> failed(
    final SMFErrorType inError)
  {
    return new SMFPartialLogged<>(List.of(inError), List.of(), null);
  }

  /**
   * Succeed an operation.
   *
   * @param inErrors   The errors encountered
   * @param inWarnings The warnings encountered
   * @param inValue    The result of the operation
   * @param <A>        The type of result values
   *
   * @return A succeeded operation
   */

  public static <A> SMFPartialLogged<A> succeeded(
    final List<? extends SMFErrorType> inErrors,
    final List<? extends SMFWarningType> inWarnings,
    final A inValue)
  {
    return new SMFPartialLogged<>(
      inErrors,
      inWarnings,
      Objects.requireNonNull(inValue, "value"));
  }

  /**
   * Succeed an operation with nothing logged.
   *
   * @param inValue The result of the operation
   * @param <A>     The type of result values
   *
   * @return A succeeded operation
   */

  public static <A> SMFPartialLogged<A> succeeded(
    final A inValue)
  {
    return succeeded(
      List.of(),
      List.of(),
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

  public List<? extends SMFErrorType> errors()
  {
    return this.errors;
  }

  public List<? extends SMFWarningType> warnings()
  {
    return this.warnings;
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

  public <B> SMFPartialLogged<B> map(
    final Function<A, B> f)
  {
    if (this.isSucceeded()) {
      return succeeded(this.errors, this.warnings, f.apply(this.value));
    }
    return failed(this.errors, this.warnings);
  }

  /**
   * Monadic bind for partials.
   *
   * @param <B> The type of mapped values
   * @param f   A function to apply to the value
   *
   * @return {@code this >>= f}
   */

  public <B> SMFPartialLogged<B> flatMap(
    final Function<A, SMFPartialLogged<B>> f)
  {
    if (this.isSucceeded()) {
      final SMFPartialLogged<B> result = f.apply(this.get());
      final var newErrors = new ArrayList<SMFErrorType>();
      final var newWarns = new ArrayList<SMFWarningType>();
      newErrors.addAll(this.errors);
      newErrors.addAll(result.errors);
      newWarns.addAll(this.warnings);
      newWarns.addAll(result.warnings);
      if (result.isSucceeded()) {
        return succeeded(newErrors, newWarns, result.value);
      }
      return failed(newErrors, newWarns);
    }
    return failed(this.errors, this.warnings);
  }
}
