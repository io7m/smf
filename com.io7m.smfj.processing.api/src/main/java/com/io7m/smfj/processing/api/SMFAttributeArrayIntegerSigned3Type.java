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

import com.io7m.jtensors.core.unparameterized.vectors.Vector3L;
import com.io7m.smfj.core.SMFPartialBiFunctionType;
import io.vavr.collection.Vector;
import org.immutables.value.Value;

/**
 * The type of 3-element signed integer arrays.
 */

@com.io7m.immutables.styles.ImmutablesStyleType
@org.immutables.vavr.encodings.VavrEncodingEnabled
@Value.Immutable
public interface SMFAttributeArrayIntegerSigned3Type extends
  SMFAttributeArrayType
{
  // Unavoidable "too many parameters" issue.
  // CHECKSTYLE:OFF
  @Override
  default <A, B, E extends Exception> B matchArray(
    final A context,
    final SMFPartialBiFunctionType<A, SMFAttributeArrayFloating4Type, B, E> on_f4,
    final SMFPartialBiFunctionType<A, SMFAttributeArrayFloating3Type, B, E> on_f3,
    final SMFPartialBiFunctionType<A, SMFAttributeArrayFloating2Type, B, E> on_f2,
    final SMFPartialBiFunctionType<A, SMFAttributeArrayFloating1Type, B, E> on_f1,
    final SMFPartialBiFunctionType<A, SMFAttributeArrayIntegerUnsigned4Type, B, E> on_u4,
    final SMFPartialBiFunctionType<A, SMFAttributeArrayIntegerUnsigned3Type, B, E> on_u3,
    final SMFPartialBiFunctionType<A, SMFAttributeArrayIntegerUnsigned2Type, B, E> on_u2,
    final SMFPartialBiFunctionType<A, SMFAttributeArrayIntegerUnsigned1Type, B, E> on_u1,
    final SMFPartialBiFunctionType<A, SMFAttributeArrayIntegerSigned4Type, B, E> on_i4,
    final SMFPartialBiFunctionType<A, SMFAttributeArrayIntegerSigned3Type, B, E> on_i3,
    final SMFPartialBiFunctionType<A, SMFAttributeArrayIntegerSigned2Type, B, E> on_i2,
    final SMFPartialBiFunctionType<A, SMFAttributeArrayIntegerSigned1Type, B, E> on_i1)
    throws E
  // CHECKSTYLE:ON
  {
    return on_i3.apply(context, this);
  }

  @Override
  default int size()
  {
    return this.values().size();
  }

  /**
   * @return The array values
   */

  @Value.Parameter
  Vector<Vector3L> values();
}
