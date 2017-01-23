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

/**
 * The face winding order for triangles.
 */

public enum SMFFaceWindingOrder
{
  /**
   * The vertices of triangles are specified in clockwise winding order.
   */

  FACE_WINDING_ORDER_CLOCKWISE(0),

  /**
   * The vertices of triangles are specified in counter-clockwise winding order.
   */

  FACE_WINDING_ORDER_COUNTER_CLOCKWISE(1);

  /**
   * @return The integer index of the value
   */

  public int index()
  {
    return this.index;
  }

  private final int index;

  SMFFaceWindingOrder(
    final int in_index)
  {
    this.index = in_index;
  }

  /**
   * @return The face winding order as a humanly readable name such as
   * "counter-clockwise"
   */

  public String toName()
  {
    switch (this) {
      case FACE_WINDING_ORDER_CLOCKWISE:
        return "clockwise";
      case FACE_WINDING_ORDER_COUNTER_CLOCKWISE:
        return "counter-clockwise";
    }

    throw new UnreachableCodeException();
  }

  /**
   * @param name An order name such as "clockwise" or "counter-clockwise"
   *
   * @return A face winding order for the given name
   *
   * @throws IllegalArgumentException On unrecognized names
   */

  public static SMFFaceWindingOrder fromName(
    final String name)
    throws IllegalArgumentException
  {
    switch (name) {
      case "clockwise":
        return FACE_WINDING_ORDER_CLOCKWISE;
      case "counter-clockwise":
        return FACE_WINDING_ORDER_COUNTER_CLOCKWISE;
      default: {
        throw new IllegalArgumentException("Unrecognized winding order: " + name);
      }
    }
  }

  /**
   * @param in_index An order index
   *
   * @return A face winding order for the given index
   *
   * @throws IllegalArgumentException On unrecognized indices
   */

  public static SMFFaceWindingOrder fromIndex(
    final int in_index)
    throws IllegalArgumentException
  {
    switch (in_index) {
      case 0:
        return FACE_WINDING_ORDER_CLOCKWISE;
      case 1:
        return FACE_WINDING_ORDER_COUNTER_CLOCKWISE;
      default: {
        throw new IllegalArgumentException(
          "Unrecognized face winding order: " + in_index);
      }
    }
  }
}
