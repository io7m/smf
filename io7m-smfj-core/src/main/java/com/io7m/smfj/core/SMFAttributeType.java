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

package com.io7m.smfj.core;

import org.immutables.value.Value;

/**
 * An attribute.
 */

@Value.Immutable
@ImmutableStyleType
public interface SMFAttributeType
{
  /**
   * @return The name of the attribute
   */

  @Value.Parameter
  SMFAttributeName name();

  /**
   * @return The kind of components that make up the attribute
   */

  @Value.Parameter
  SMFComponentType componentType();

  /**
   * @return The number of components in the attribute
   */

  @Value.Parameter
  int componentCount();

  /**
   * @return The size of a single component in bits
   */

  @Value.Parameter
  int componentSizeBits();

  /**
   * @return The size of a single component in octets
   */

  default int componentSizeOctets()
  {
    return (int) Math.ceil((double) this.componentSizeBits() / 8.0);
  }

  /**
   * @return The size of the attribute in octets
   */

  default int sizeOctets()
  {
    return Math.multiplyExact(
      this.componentSizeOctets(),
      this.componentCount());
  }

  /**
   * Check preconditions for the type.
   */

  @Value.Check
  default void checkPreconditions()
  {
    if (Integer.compareUnsigned(this.componentCount(), 1) < 0) {
      throw new IllegalArgumentException(
        "Component count must be in the range [1, 4]");
    }
    if (Integer.compareUnsigned(this.componentCount(), 4) > 0) {
      throw new IllegalArgumentException(
        "Component count must be in the range [1, 4]");
    }

    switch (this.componentSizeBits()) {
      case 8:
      case 16:
      case 32:
      case 64: {
        break;
      }
      default: {
        throw new IllegalArgumentException(
          "Component size must be {8, 16, 32, 64}");
      }
    }
  }
}
