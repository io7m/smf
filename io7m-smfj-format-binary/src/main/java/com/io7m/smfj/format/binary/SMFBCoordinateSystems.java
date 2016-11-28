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


package com.io7m.smfj.format.binary;

import com.io7m.jcoords.core.conversion.CAxis;
import com.io7m.jcoords.core.conversion.CAxisSystem;
import com.io7m.jnull.NullCheck;
import com.io7m.junreachable.UnreachableCodeException;
import com.io7m.smfj.core.SMFCoordinateSystem;
import com.io7m.smfj.core.SMFFaceWindingOrder;
import com.io7m.smfj.format.binary.v1.SMFBV1CoordinateSystemsReadableType;
import com.io7m.smfj.format.binary.v1.SMFBV1CoordinateSystemsWritableType;

/**
 * Functions to pack and unpack coordinate system values.
 */

public final class SMFBCoordinateSystems
{
  private SMFBCoordinateSystems()
  {
    throw new UnreachableCodeException();
  }

  /**
   * Unpack a coordinate system from the given input.
   *
   * @param input An input value
   *
   * @return A coordinate system
   */

  public static SMFCoordinateSystem unpack(
    final SMFBV1CoordinateSystemsReadableType input)
  {
    NullCheck.notNull(input, "input");

    final SMFCoordinateSystem.Builder cb = SMFCoordinateSystem.builder();
    cb.setWindingOrder(SMFFaceWindingOrder.fromIndex(input.getWinding()));
    cb.setAxes(axesUnpack(input.getRight(), input.getUp(), input.getForward()));
    return cb.build();
  }

  private static CAxisSystem axesUnpack(
    final int right,
    final int up,
    final int forward)
  {
    return CAxisSystem.of(
      axisFromIndex(right),
      axisFromIndex(up),
      axisFromIndex(forward));
  }

  /**
   * Pack a coordinate system.
   *
   * @param system The coordinate system
   * @param out    The output value
   */

  public static void pack(
    final SMFCoordinateSystem system,
    final SMFBV1CoordinateSystemsWritableType out)
  {
    NullCheck.notNull(system, "system");
    NullCheck.notNull(out, "out");

    out.setRight(axisToIndex(system.axes().right()));
    out.setUp(axisToIndex(system.axes().up()));
    out.setForward(axisToIndex(system.axes().forward()));
    out.setWinding(system.windingOrder().index());
  }

  private static CAxis axisFromIndex(
    final int index)
  {
    switch (index) {
      case 0:
        return CAxis.AXIS_POSITIVE_X;
      case 1:
        return CAxis.AXIS_POSITIVE_Y;
      case 2:
        return CAxis.AXIS_POSITIVE_Z;
      case 3:
        return CAxis.AXIS_NEGATIVE_X;
      case 4:
        return CAxis.AXIS_NEGATIVE_Y;
      case 5:
        return CAxis.AXIS_NEGATIVE_Z;
      default: {
        throw new IllegalArgumentException(
          "Unrecognized axis value: " + index);
      }
    }
  }

  private static int axisToIndex(
    final CAxis axis)
  {
    switch (axis) {
      case AXIS_POSITIVE_X:
        return 0;
      case AXIS_POSITIVE_Y:
        return 1;
      case AXIS_POSITIVE_Z:
        return 2;
      case AXIS_NEGATIVE_X:
        return 3;
      case AXIS_NEGATIVE_Y:
        return 4;
      case AXIS_NEGATIVE_Z:
        return 5;
    }

    throw new UnreachableCodeException();
  }
}
