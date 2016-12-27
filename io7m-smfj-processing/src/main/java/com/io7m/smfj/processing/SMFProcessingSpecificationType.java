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

import com.io7m.jaffirm.core.Preconditions;
import com.io7m.smfj.core.SMFAttributeName;
import com.io7m.smfj.core.SMFImmutableStyleType;
import javaslang.Tuple2;
import javaslang.collection.Map;
import org.immutables.value.Value;

import java.util.Objects;

/**
 * A processing specification.
 */

@SMFImmutableStyleType
@Value.Immutable
public interface SMFProcessingSpecificationType
{
  /**
   * @return A list of filters to be applied to attributes
   */

  @Value.Parameter
  Map<SMFAttributeName, SMFAttributeFilterType> filters();

  /**
   * @return A specification of how triangles should be processed
   */

  @Value.Parameter
  SMFProcessTriangles triangles();

  @Value.Check
  default void checkPreconditions()
  {
    for (final Tuple2<SMFAttributeName, SMFAttributeFilterType> entry : this.filters()) {
      Preconditions.checkPrecondition(
        entry._1,
        Objects.equals(entry._1, entry._2.source()),
        name -> "Entry name " + name + " must match filter source name");
    }
  }
}
