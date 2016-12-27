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

package com.io7m.smfj.processing;

import com.io7m.jfunctional.PartialBiFunctionType;
import com.io7m.smfj.core.SMFAttributeName;
import org.immutables.value.Value;

/**
 * The type of filters.
 */

public interface SMFAttributeFilterType
{
  /**
   * Match on the type of filter.
   *
   * @param context   A contextual value passed to the given functions
   * @param on_remove Evaluated on values of {@link SMFAttributeFilterRemoveType}
   * @param on_rename Evaluated on values of {@link SMFAttributeFilterRenameType}
   * @param <A>       The type of contextual values
   * @param <B>       The type of return values
   * @param <E>       The type of raised exceptions
   *
   * @return A value of {@code B}
   *
   * @throws E If any of the given functions raise {@code E}
   */

  <A, B, E extends Exception>
  B matchFilter(
    A context,
    PartialBiFunctionType<A, SMFAttributeFilterRemoveType, B, E> on_remove,
    PartialBiFunctionType<A, SMFAttributeFilterRenameType, B, E> on_rename)
    throws E;

  /**
   * @return The source attribute name
   */

  @Value.Parameter
  SMFAttributeName source();
}
