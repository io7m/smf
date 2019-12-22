/*
 * Copyright © 2019 Mark Raynsford <code@io7m.com> http://io7m.com
 *
 * Permission to use, copy, modify, and/or distribute this software for
 * any purpose with or without fee is hereby granted, provided that the
 * above copyright notice and this permission notice appear in all copies.
 *
 * THE SOFTWARE IS PROVIDED "AS IS" AND THE AUTHOR DISCLAIMS ALL
 * WARRANTIES WITH REGARD TO THIS SOFTWARE INCLUDING ALL IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS. IN NO EVENT SHALL THE AUTHOR
 * BE LIABLE FOR ANY SPECIAL, DIRECT, INDIRECT, OR CONSEQUENTIAL DAMAGES
 * OR ANY DAMAGES WHATSOEVER RESULTING FROM LOSS OF USE, DATA OR PROFITS,
 * WHETHER IN AN ACTION OF CONTRACT, NEGLIGENCE OR OTHER TORTIOUS ACTION,
 * ARISING OUT OF OR IN CONNECTION WITH THE USE OR PERFORMANCE OF THIS
 * SOFTWARE.
 */


package com.io7m.smfj.format.binary2.internal;

import com.io7m.jbssio.api.BSSWriterSequentialType;
import com.io7m.jcoords.core.conversion.CAxis;
import com.io7m.smfj.core.SMFCoordinateSystem;
import com.io7m.smfj.core.SMFFaceWindingOrder;
import java.io.IOException;

public final class SMFB2WritingCoordinateSystem
  implements SMFB2StructureWriterType<SMFCoordinateSystem>
{
  public SMFB2WritingCoordinateSystem()
  {

  }

  private static int windingToOrdinal(
    final SMFFaceWindingOrder windingOrder)
  {
    return windingOrder.index();
  }

  private static int axisToOrdinal(
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
      default: {
        return 0;
      }
    }
  }

  @Override
  public void write(
    final BSSWriterSequentialType writer,
    final SMFCoordinateSystem value)
    throws IOException
  {
    try (var subWriter = writer.createSubWriterBounded(
      "coordinateSystem",
      4L)) {
      final var axes = value.axes();
      subWriter.writeU8("right", axisToOrdinal(axes.right()));
      subWriter.writeU8("up", axisToOrdinal(axes.up()));
      subWriter.writeU8("forward", axisToOrdinal(axes.forward()));
      subWriter.writeU8("winding", windingToOrdinal(value.windingOrder()));
    }
  }
}
