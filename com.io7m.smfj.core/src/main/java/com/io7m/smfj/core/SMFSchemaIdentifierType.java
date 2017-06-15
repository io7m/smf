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
 * A schema identifier.
 */

@Value.Immutable
@SMFImmutableStyleType
public interface SMFSchemaIdentifierType
{
  /**
   * @return The schema name
   */

  @Value.Parameter
  SMFSchemaName name();

  /**
   * @return The schema major version
   */

  @Value.Parameter
  @Value.Default
  default int versionMajor()
  {
    return 0x0;
  }

  /**
   * @return The schema minor version
   */

  @Value.Parameter
  @Value.Default
  default int versionMinor()
  {
    return 0x0;
  }

  /**
   * @return A humanly-readable string describing the schema identifier
   */

  @Value.Lazy
  default String toHumanString()
  {
    return String.format(
      "%s %s.%s",
      this.name().value(),
      Integer.toUnsignedString(this.versionMajor()),
      Integer.toUnsignedString(this.versionMinor()));
  }
}
