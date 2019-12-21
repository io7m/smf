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

import com.io7m.jbssio.api.BSSReaderType;
import com.io7m.jcoords.core.conversion.CAxis;
import com.io7m.jcoords.core.conversion.CAxisSystem;
import com.io7m.smfj.core.SMFCoordinateSystem;
import com.io7m.smfj.core.SMFFaceWindingOrder;
import com.io7m.smfj.parser.api.SMFParseError;
import java.io.IOException;
import java.util.Optional;
import java.util.function.Consumer;

public final class SMFB2ParsingCoordinateSystem
  implements SMFB2StructureParserType<Optional<SMFCoordinateSystem>>
{
  SMFB2ParsingCoordinateSystem()
  {

  }

  private static Optional<CAxis> axisOfOrdinal(
    final BSSReaderType reader,
    final Consumer<SMFParseError> errors,
    final int axis)
  {
    switch (axis) {
      case 0:
        return Optional.of(CAxis.AXIS_POSITIVE_X);
      case 1:
        return Optional.of(CAxis.AXIS_POSITIVE_Y);
      case 2:
        return Optional.of(CAxis.AXIS_POSITIVE_Z);
      case 3:
        return Optional.of(CAxis.AXIS_NEGATIVE_X);
      case 4:
        return Optional.of(CAxis.AXIS_NEGATIVE_Y);
      case 5:
        return Optional.of(CAxis.AXIS_NEGATIVE_Z);
      default: {
        errors.accept(
          SMFB2ParseErrors.errorOf(
            reader,
            "Invalid axis value: %d", Integer.valueOf(axis)
          )
        );
        return Optional.empty();
      }
    }
  }

  private static Optional<SMFFaceWindingOrder> windingOfOrdinal(
    final BSSReaderType reader,
    final Consumer<SMFParseError> errors,
    final int winding)
  {
    switch (winding) {
      case 0:
        return Optional.of(SMFFaceWindingOrder.FACE_WINDING_ORDER_CLOCKWISE);
      case 1:
        return Optional.of(SMFFaceWindingOrder.FACE_WINDING_ORDER_COUNTER_CLOCKWISE);
      default: {
        errors.accept(
          SMFB2ParseErrors.errorOf(
            reader,
            "Invalid winding order value: %d", Integer.valueOf(winding)
          )
        );
        return Optional.empty();
      }
    }
  }

  private static Optional<SMFCoordinateSystem> parseWithReader(
    final SMFB2ParsingContextType context,
    final BSSReaderType reader)
    throws IOException
  {
    final var rightOpt =
      axisOfOrdinal(
        reader,
        context::publishError,
        reader.readU8("right"));
    final var upOpt =
      axisOfOrdinal(
        reader,
        context::publishError,
        reader.readU8("up"));
    final var forwardOpt =
      axisOfOrdinal(
        reader,
        context::publishError,
        reader.readU8("forward"));
    final var windingOpt =
      windingOfOrdinal(
        reader,
        context::publishError,
        reader.readU8("winding"));

    if (rightOpt.isPresent()
      && upOpt.isPresent()
      && forwardOpt.isPresent()
      && windingOpt.isPresent()) {
      try {
        return Optional.of(
          SMFCoordinateSystem.of(
            CAxisSystem.of(rightOpt.get(), upOpt.get(), forwardOpt.get()),
            windingOpt.get()));
      } catch (final Exception e) {
        context.publishError(SMFB2ParseErrors.errorOfException(reader, e));
        return Optional.empty();
      }
    }

    return Optional.empty();
  }

  @Override
  public Optional<SMFCoordinateSystem> parse(
    final SMFB2ParsingContextType context)
    throws IOException
  {
    return context.withReader(
      "coordinateSystem",
      4L,
      reader -> parseWithReader(context, reader));
  }
}
