/*
 * Copyright © 2017 <code@io7m.com> http://io7m.com
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

package com.io7m.smfj.bytebuffer;

import com.io7m.smfj.core.SMFAttribute;
import org.immutables.value.Value;

/**
 * A packed attribute.
 */

@com.io7m.immutables.styles.ImmutablesStyleType
@Value.Immutable
public interface SMFByteBufferPackedAttributeType extends
  Comparable<SMFByteBufferPackedAttributeType>
{
  /**
   * @return The real underlying attribute
   */

  @Value.Parameter
  SMFAttribute attribute();

  /**
   * @return The order in which the attribute appears
   */

  @Value.Parameter
  int order();

  /**
   * A specification of the offset in octets from the start of each vertex
   * at which this attribute's data will be written.
   *
   * @return The offset in octets
   */

  @Value.Parameter
  int offsetOctets();

  @Override
  default int compareTo(final SMFByteBufferPackedAttributeType o)
  {
    return Integer.compareUnsigned(this.order(), o.order());
  }
}
