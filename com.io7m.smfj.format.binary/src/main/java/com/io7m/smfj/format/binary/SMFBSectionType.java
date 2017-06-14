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

package com.io7m.smfj.format.binary;

import com.io7m.smfj.core.SMFImmutableStyleType;
import org.immutables.value.Value;

/**
 * A parsed section.
 */

@SMFImmutableStyleType
@Value.Immutable
public interface SMFBSectionType
{
  /**
   * The required natural alignment of sections.
   */

  int SECTION_ALIGNMENT = 16;

  /**
   * @return The magic number identifying this section
   */

  @Value.Parameter
  long magic();

  /**
   * <p>The size of the section data, not including the magic number or this
   * size value.</p>
   *
   * <p>{@code sizeOfData() + 8 + 8} must be a multiple of {@link
   * #SECTION_ALIGNMENT}</p>
   *
   * @return The size of this section
   */

  @Value.Parameter
  long sizeOfData();

  /**
   * @return The absolute offset of the start of the section
   */

  @Value.Parameter
  long offset();

  /**
   * @return The total size of the section: The size of the data plus the header
   */

  @Value.Derived
  default long sizeTotal()
  {
    return Math.addExact(this.sizeOfData(), 16L);
  }

  /**
   * Check preconditions for the type.
   */

  @Value.Check
  default void checkPreconditions()
  {
    if (this.sizeTotal() % (long) SECTION_ALIGNMENT != 0L) {
      throw new IllegalArgumentException(
        String.format(
          "Total section size %s = %s + 16 is not a multiple of %s",
          Long.toUnsignedString(this.sizeTotal()),
          Long.toUnsignedString(this.sizeOfData()),
          Integer.toUnsignedString(SECTION_ALIGNMENT)));
    }
  }
}
