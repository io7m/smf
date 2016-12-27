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
import com.io7m.smfj.core.SMFImmutableStyleType;
import javaslang.collection.Vector;
import org.immutables.value.Value;

/**
 * The type of 1-element floating point arrays.
 */

@SMFImmutableStyleType
@Value.Immutable
public interface SMFAttributeArrayFloating1Type extends SMFAttributeArrayType
{
  @Override
  default <A, B, E extends Exception> B matchArray(
    final A context,
    final PartialBiFunctionType<A, SMFAttributeArrayFloating4Type, B, E> on_f4,
    final PartialBiFunctionType<A, SMFAttributeArrayFloating3Type, B, E> on_f3,
    final PartialBiFunctionType<A, SMFAttributeArrayFloating2Type, B, E> on_f2,
    final PartialBiFunctionType<A, SMFAttributeArrayFloating1Type, B, E> on_f1,
    final PartialBiFunctionType<A, SMFAttributeArrayIntegerUnsigned4Type, B, E> on_u4,
    final PartialBiFunctionType<A, SMFAttributeArrayIntegerUnsigned3Type, B, E> on_u3,
    final PartialBiFunctionType<A, SMFAttributeArrayIntegerUnsigned2Type, B, E> on_u2,
    final PartialBiFunctionType<A, SMFAttributeArrayIntegerUnsigned1Type, B, E> on_u1,
    final PartialBiFunctionType<A, SMFAttributeArrayIntegerSigned4Type, B, E> on_i4,
    final PartialBiFunctionType<A, SMFAttributeArrayIntegerSigned3Type, B, E> on_i3,
    final PartialBiFunctionType<A, SMFAttributeArrayIntegerSigned2Type, B, E> on_i2,
    final PartialBiFunctionType<A, SMFAttributeArrayIntegerSigned1Type, B, E> on_i1)
    throws E
  {
    return on_f1.call(context, this);
  }

  /**
   * @return The array values
   */

  @Value.Parameter
  Vector<Double> values();
}
