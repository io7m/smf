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

package com.io7m.smfj.processing.api;

import com.io7m.smfj.core.SMFPartialBiFunctionType;

/**
 * The type of arrays.
 */

public interface SMFAttributeArrayType
{
  /**
   * Match on the type of array.
   *
   * @param context A contextual value
   * @param on_f4   A receiver function
   * @param on_f3   A receiver function
   * @param on_f2   A receiver function
   * @param on_f1   A receiver function
   * @param on_u4   A receiver function
   * @param on_u3   A receiver function
   * @param on_u2   A receiver function
   * @param on_u1   A receiver function
   * @param on_i4   A receiver function
   * @param on_i3   A receiver function
   * @param on_i2   A receiver function
   * @param on_i1   A receiver function
   * @param <A>     The type of contextual values
   * @param <B>     The type of returned values
   * @param <E>     The type of raised exceptions
   *
   * @return A value of {@code A}
   *
   * @throws E If any of the functions raise {@code E}
   */

  // Unavoidable "too many parameters" issue.
  // CHECKSTYLE:OFF
  <A, B, E extends Exception>
  B matchArray(
    A context,
    SMFPartialBiFunctionType<A, SMFAttributeArrayFloating4Type, B, E> on_f4,
    SMFPartialBiFunctionType<A, SMFAttributeArrayFloating3Type, B, E> on_f3,
    SMFPartialBiFunctionType<A, SMFAttributeArrayFloating2Type, B, E> on_f2,
    SMFPartialBiFunctionType<A, SMFAttributeArrayFloating1Type, B, E> on_f1,
    SMFPartialBiFunctionType<A, SMFAttributeArrayIntegerUnsigned4Type, B, E> on_u4,
    SMFPartialBiFunctionType<A, SMFAttributeArrayIntegerUnsigned3Type, B, E> on_u3,
    SMFPartialBiFunctionType<A, SMFAttributeArrayIntegerUnsigned2Type, B, E> on_u2,
    SMFPartialBiFunctionType<A, SMFAttributeArrayIntegerUnsigned1Type, B, E> on_u1,
    SMFPartialBiFunctionType<A, SMFAttributeArrayIntegerSigned4Type, B, E> on_i4,
    SMFPartialBiFunctionType<A, SMFAttributeArrayIntegerSigned3Type, B, E> on_i3,
    SMFPartialBiFunctionType<A, SMFAttributeArrayIntegerSigned2Type, B, E> on_i2,
    SMFPartialBiFunctionType<A, SMFAttributeArrayIntegerSigned1Type, B, E> on_i1)
    throws E;
  // CHECKSTYLE:ON

  /**
   * @return The size of the array
   */

  int size();
}
