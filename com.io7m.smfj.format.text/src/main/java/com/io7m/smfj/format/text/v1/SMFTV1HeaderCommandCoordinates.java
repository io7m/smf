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

package com.io7m.smfj.format.text.v1;

import com.io7m.jcoords.core.conversion.CAxis;
import com.io7m.jcoords.core.conversion.CAxisSystem;
import java.util.Objects;
import com.io7m.smfj.core.SMFCoordinateSystem;
import com.io7m.smfj.core.SMFFaceWindingOrder;
import com.io7m.smfj.core.SMFHeader;
import com.io7m.smfj.format.text.SMFTHeaderCommandParserType;
import com.io7m.smfj.format.text.SMFTLineReaderType;
import com.io7m.smfj.format.text.SMFTParsingStatus;
import com.io7m.smfj.parser.api.SMFParserEventsHeaderType;
import javaslang.collection.List;

import java.io.IOException;

import static com.io7m.smfj.format.text.SMFTParsingStatus.FAILURE;
import static com.io7m.smfj.format.text.SMFTParsingStatus.SUCCESS;
import static com.io7m.smfj.format.text.v1.SMFTErrors.errorCommandExpectedGotWithException;
import static com.io7m.smfj.format.text.v1.SMFTErrors.errorMalformedCommand;

/**
 * A parser for the "coordinates" command.
 */

public final class SMFTV1HeaderCommandCoordinates
  implements SMFTHeaderCommandParserType
{
  /**
   * The command syntax.
   */

  public static final String SYNTAX =
    "coordinates <axis> <axis> <axis> <winding-order>";

  private final SMFTLineReaderType reader;
  private final SMFHeader.Builder header;

  /**
   * Construct a parser.
   *
   * @param in_reader The current line reader
   * @param in_header A header builder
   */

  public SMFTV1HeaderCommandCoordinates(
    final SMFTLineReaderType in_reader,
    final SMFHeader.Builder in_header)
  {
    this.reader = Objects.requireNonNull(in_reader, "Reader");
    this.header = Objects.requireNonNull(in_header, "Header");
  }

  @Override
  public String name()
  {
    return "coordinates";
  }

  @Override
  public SMFTParsingStatus parse(
    final SMFParserEventsHeaderType receiver,
    final List<String> line)
    throws IOException
  {
    if (line.length() == 5) {
      try {
        final CAxis axis_right = CAxis.of(line.get(1));
        final CAxis axis_up = CAxis.of(line.get(2));
        final CAxis axis_forward = CAxis.of(line.get(3));
        final SMFFaceWindingOrder order =
          SMFFaceWindingOrder.fromName(line.get(4));

        boolean bad = false;
        bad = bad || axis_right.axis() == axis_up.axis();
        bad = bad || axis_up.axis() == axis_forward.axis();
        bad = bad || axis_forward.axis() == axis_right.axis();

        if (bad) {
          throw new IllegalArgumentException("Axes must be perpendicular");
        }

        this.header.setCoordinateSystem(
          SMFCoordinateSystem.of(
            CAxisSystem.of(axis_right, axis_up, axis_forward),
            order));
        return SUCCESS;
      } catch (final IllegalArgumentException e) {
        receiver.onError(errorCommandExpectedGotWithException(
          "coordinates", SYNTAX, line, this.reader.position(), e));
        return FAILURE;
      }
    }

    receiver.onError(errorMalformedCommand(
      "coordinates", SYNTAX, line, this.reader.position()));
    return FAILURE;
  }
}
