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

import com.io7m.junreachable.UnreachableCodeException;
import java.util.Objects;

/**
 * The kind of components.
 */

public enum SMFComponentType
{
  /**
   * Signed integer values.
   */

  ELEMENT_TYPE_INTEGER_SIGNED("integer-signed"),

  /**
   * Unsigned integer values.
   */

  ELEMENT_TYPE_INTEGER_UNSIGNED("integer-unsigned"),

  /**
   * Floating point values.
   */

  ELEMENT_TYPE_FLOATING("float");

  private final String name;

  SMFComponentType(
    final String in_name)
  {
    this.name = Objects.requireNonNull(in_name, "Name");
  }

  /**
   * Return a component type for the given name.
   *
   * @param name The name
   *
   * @return A component type
   *
   * @throws IllegalArgumentException If the name does not refer to a recognized type
   */

  public static SMFComponentType of(
    final String name)
  {
    switch (name) {
      case "integer-signed": {
        return ELEMENT_TYPE_INTEGER_SIGNED;
      }
      case "integer-unsigned": {
        return ELEMENT_TYPE_INTEGER_UNSIGNED;
      }
      case "float": {
        return ELEMENT_TYPE_FLOATING;
      }
      default: {
        throw new IllegalArgumentException(
          "Unrecognized type: " + name);
      }
    }
  }

  /**
   * @return The unique name of the type
   */

  public String getName()
  {
    return this.name;
  }

  /**
   * @return An integer value for the current component type
   */

  public int toInteger()
  {
    switch (this) {
      case ELEMENT_TYPE_INTEGER_SIGNED:
        return 0;
      case ELEMENT_TYPE_INTEGER_UNSIGNED:
        return 1;
      case ELEMENT_TYPE_FLOATING:
        return 2;
    }

    throw new UnreachableCodeException();
  }
}
