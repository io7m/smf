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
import com.io7m.smfj.core.SMFImmutableStyleType;
import org.immutables.value.Value;

/**
 * A specification that states that the attribute named by {@link #source()}
 * should be renamed to {@link #target()}.
 */

@SMFImmutableStyleType
@Value.Immutable
public interface SMFAttributeFilterRenameType extends SMFAttributeFilterType
{
  @Override
  default <A, B, E extends Exception> B matchFilter(
    final A context,
    final PartialBiFunctionType<A, SMFAttributeFilterRemoveType, B, E> on_remove,
    final PartialBiFunctionType<A, SMFAttributeFilterRenameType, B, E> on_rename)
    throws E
  {
    return on_rename.call(context, this);
  }

  @Override
  @Value.Parameter
  SMFAttributeName source();

  /**
   * @return The target name of the renamed attribute
   */

  @Value.Parameter
  SMFAttributeName target();
}
