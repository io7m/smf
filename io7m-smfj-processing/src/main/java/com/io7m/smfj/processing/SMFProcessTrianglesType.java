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

import com.io7m.smfj.core.SMFImmutableStyleType;
import org.immutables.value.Value;

import java.util.OptionalInt;

/**
 * A specification of how triangles should be processed.
 */

@SMFImmutableStyleType
@Value.Immutable
public interface SMFProcessTrianglesType
{
  /**
   * A specification of whether triangles should be optimized. If a value is
   * provided, it represents the smallest size in bits that an implementation is
   * allowed to use for triangle indices. If no value is specified, optimization
   * is not performed.
   *
   * @return The minimum size of triangle indices, if any
   */

  @Value.Parameter
  OptionalInt optimize();

  /**
   * A specification of whether or not triangle indices should be validated. A
   * triangle index is valid iff there is an existing vertex with the same index
   * value.
   *
   * @return {@code true} iff triangle indices should be validated
   */

  @Value.Parameter
  boolean validate();
}
