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

package com.io7m.smfj.processing.main;

import com.io7m.smfj.core.SMFAttributeName;
import com.io7m.smfj.core.SMFComponentType;
import java.util.Optional;
import java.util.OptionalInt;
import org.immutables.value.Value;

/**
 * A specification of how an attribute should be checked.
 */

@com.io7m.immutables.styles.ImmutablesStyleType
@Value.Immutable
public interface SMFMemoryMeshFilterCheckConfigurationType
{
  /**
   * @return The name of the attribute
   */

  @Value.Parameter
  SMFAttributeName name();

  /**
   * @return The type of components
   */

  @Value.Parameter
  Optional<SMFComponentType> componentType();

  /**
   * @return The number of components per element
   */

  @Value.Parameter
  OptionalInt componentCount();

  /**
   * @return The size of components in bits
   */

  @Value.Parameter
  OptionalInt componentSize();

}
