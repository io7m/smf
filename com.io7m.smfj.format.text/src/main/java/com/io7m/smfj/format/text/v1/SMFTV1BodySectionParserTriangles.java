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

import com.io7m.smfj.core.SMFHeader;
import com.io7m.smfj.format.text.SMFTBodySectionParserType;
import com.io7m.smfj.format.text.SMFTLineReaderType;
import com.io7m.smfj.format.text.SMFTParsingStatus;
import com.io7m.smfj.format.text.implementation.Flags;
import com.io7m.smfj.parser.api.SMFParseError;
import com.io7m.smfj.parser.api.SMFParserEventsBodyType;
import com.io7m.smfj.parser.api.SMFParserEventsDataTrianglesIgnoringReceiver;
import com.io7m.smfj.parser.api.SMFParserEventsDataTrianglesType;
import io.vavr.collection.List;

import java.io.IOException;
import java.util.BitSet;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Supplier;

import static com.io7m.smfj.format.text.SMFTParsingStatus.FAILURE;
import static com.io7m.smfj.format.text.SMFTParsingStatus.SUCCESS;
import static com.io7m.smfj.format.text.v1.SMFTErrors.errorMalformedCommand;
import static com.io7m.smfj.parser.api.SMFParseErrors.errorExpectedGot;

/**
 * A parser for the "triangles" body section.
 */

public final class SMFTV1BodySectionParserTriangles implements
  SMFTBodySectionParserType
{
  /**
   * The command syntax.
   */

  public static final String SYNTAX =
    "<integer-unsigned> <integer-unsigned> <integer-unsigned>";

  private final SMFTLineReaderType reader;
  private final Supplier<SMFHeader> header_get;
  private final BitSet state;

  /**
   * Construct a parser.
   *
   * @param in_header_get A function that yields a header
   * @param in_reader     A line reader
   * @param in_state      The current state
   */

  public SMFTV1BodySectionParserTriangles(
    final Supplier<SMFHeader> in_header_get,
    final SMFTLineReaderType in_reader,
    final BitSet in_state)
  {
    this.header_get = Objects.requireNonNull(in_header_get, "Header");
    this.reader = Objects.requireNonNull(in_reader, "Reader");
    this.state = Objects.requireNonNull(in_state, "State");
  }

  private static SMFParserEventsDataTrianglesType makeTriangleReceiver(
    final SMFParserEventsBodyType receiver,
    final Optional<SMFParserEventsDataTrianglesType> data_receiver_opt)
  {
    return data_receiver_opt.orElseGet(
      () -> new SMFParserEventsDataTrianglesIgnoringReceiver(receiver));
  }

  private static boolean checkTrianglesAreCorrect(
    final SMFTLineReaderType reader,
    final SMFParserEventsDataTrianglesType tri_receiver,
    final long triangle_count,
    final long triangles_remaining)
  {
    if (triangles_remaining < 0L) {
      tri_receiver.onError(
        tooManyTriangles(reader, triangle_count, triangles_remaining));
      return false;
    }

    if (triangles_remaining > 0L) {
      tri_receiver.onError(
        tooFewTriangles(reader, triangle_count, triangles_remaining));
      return false;
    }

    return true;
  }

  private static SMFParseError tooFewTriangles(
    final SMFTLineReaderType reader,
    final long triangle_count,
    final long triangles_remaining)
  {
    return errorExpectedGot(
      "Too few triangles were provided.",
      triangle_count + " triangles",
      (triangle_count - triangles_remaining) + " triangles",
      reader.position());
  }

  private static SMFParseError tooManyTriangles(
    final SMFTLineReaderType reader,
    final long triangle_count,
    final long triangles_remaining)
  {
    return errorExpectedGot(
      "Too many triangles were provided.",
      triangle_count + " triangles",
      (triangle_count - triangles_remaining) + " triangles",
      reader.position());
  }

  private static SMFParseError unexpectedEOF(final SMFTLineReaderType reader)
  {
    return SMFParseError.of(
      reader.position(),
      "Unexpected EOF",
      Optional.empty());
  }

  @Override
  public String name()
  {
    return "triangles";
  }

  @Override
  public SMFTParsingStatus parse(
    final SMFParserEventsBodyType receiver,
    final List<String> line_start)
    throws IOException
  {
    if (line_start.size() != 1) {
      receiver.onError(errorMalformedCommand(
        "triangles",
        "triangles",
        line_start,
        this.reader.position()));
      return FAILURE;
    }

    final Optional<SMFParserEventsDataTrianglesType> tri_receiver_opt =
      receiver.onTriangles();
    final SMFParserEventsDataTrianglesType tri_receiver =
      makeTriangleReceiver(receiver, tri_receiver_opt);

    try {
      final SMFHeader header = this.header_get.get();
      final long triangle_count = header.triangles().triangleCount();
      long triangles_remaining = triangle_count;
      boolean encountered_end = false;
      while (!encountered_end) {
        final Optional<List<String>> line_opt = this.reader.line();
        if (!line_opt.isPresent()) {
          receiver.onError(unexpectedEOF(this.reader));
          return FAILURE;
        }

        final List<String> line = line_opt.get();
        if (line.isEmpty()) {
          continue;
        }

        switch (line.get(0)) {
          case "end": {
            encountered_end = true;
            break;
          }
          default: {
            switch (this.parseAttributeElementUnsigned3(tri_receiver, line)) {
              case SUCCESS:
                triangles_remaining =
                  Math.subtractExact(triangles_remaining, 1L);
                break;
              case FAILURE:
                return FAILURE;
            }
          }
        }
      }

      if (!checkTrianglesAreCorrect(
        this.reader, tri_receiver, triangle_count, triangles_remaining)) {
        return FAILURE;
      }

      this.state.set(Flags.TRIANGLES_RECEIVED, true);
      return SUCCESS;
    } finally {
      tri_receiver.onDataTrianglesFinish();
    }
  }

  private SMFTParsingStatus parseAttributeElementUnsigned3(
    final SMFParserEventsDataTrianglesType receiver,
    final List<String> line)
  {
    if (line.length() == 3) {
      try {
        final long v0 = Long.parseUnsignedLong(line.get(0));
        final long v1 = Long.parseUnsignedLong(line.get(1));
        final long v2 = Long.parseUnsignedLong(line.get(2));
        receiver.onDataTriangle(v0, v1, v2);
        return SUCCESS;
      } catch (final NumberFormatException e) {
        receiver.onError(SMFTErrors.errorExpectedGotWithException(
          "Cannot parse triangle: " + e.getMessage(),
          SYNTAX,
          line,
          this.reader.position(),
          e));
        return FAILURE;
      }
    }
    receiver.onError(SMFTErrors.errorExpectedGot(
      "Cannot parse triangle.",
      SYNTAX,
      line,
      this.reader.position()));
    return FAILURE;
  }

}
