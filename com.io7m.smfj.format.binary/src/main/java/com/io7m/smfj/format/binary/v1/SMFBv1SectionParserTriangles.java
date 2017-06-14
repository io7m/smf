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

package com.io7m.smfj.format.binary.v1;

import com.io7m.junreachable.UnreachableCodeException;
import com.io7m.smfj.core.SMFHeader;
import com.io7m.smfj.core.SMFTriangles;
import com.io7m.smfj.format.binary.SMFBBodySectionParserType;
import com.io7m.smfj.format.binary.SMFBDataStreamReaderType;
import com.io7m.smfj.format.binary.SMFBSectionTriangles;
import com.io7m.smfj.parser.api.SMFParseError;
import com.io7m.smfj.parser.api.SMFParserEventsBodyType;
import com.io7m.smfj.parser.api.SMFParserEventsDataTrianglesType;

import java.io.IOException;
import java.util.Optional;

import static com.io7m.jnull.NullCheck.notNull;

/**
 * A parser for triangle sections.
 */

public final class SMFBv1SectionParserTriangles
  implements SMFBBodySectionParserType
{
  /**
   * Construct a parser.
   */

  public SMFBv1SectionParserTriangles()
  {

  }

  @Override
  public long magic()
  {
    return SMFBSectionTriangles.MAGIC;
  }

  @Override
  public void parse(
    final SMFHeader header,
    final SMFParserEventsBodyType events,
    final SMFBDataStreamReaderType reader)
  {
    notNull(header, "Header");
    notNull(events, "Events");
    notNull(reader, "Reader");

    final Optional<SMFParserEventsDataTrianglesType> tri_opt = events.onTriangles();
    if (tri_opt.isPresent()) {
      final SMFParserEventsDataTrianglesType tri = tri_opt.get();

      try {
        final SMFTriangles triangles = header.triangles();
        final Optional<String> name = Optional.of("Triangle");
        switch (triangles.triangleIndexSizeBits()) {
          case 8: {
            for (long index = 0L;
                 Long.compareUnsigned(index, triangles.triangleCount()) < 0;
                 ++index) {
              final long v0 = reader.readU8(name);
              final long v1 = reader.readU8(name);
              final long v2 = reader.readU8(name);
              tri.onDataTriangle(v0, v1, v2);
            }
            break;
          }

          case 16: {
            for (long index = 0L;
                 Long.compareUnsigned(index, triangles.triangleCount()) < 0;
                 ++index) {
              final long v0 = reader.readU16(name);
              final long v1 = reader.readU16(name);
              final long v2 = reader.readU16(name);
              tri.onDataTriangle(v0, v1, v2);
            }
            break;
          }

          case 32: {
            for (long index = 0L;
                 Long.compareUnsigned(index, triangles.triangleCount()) < 0;
                 ++index) {
              final long v0 = reader.readU32(name);
              final long v1 = reader.readU32(name);
              final long v2 = reader.readU32(name);
              tri.onDataTriangle(v0, v1, v2);
            }
            break;
          }

          case 64: {
            for (long index = 0L;
                 Long.compareUnsigned(index, triangles.triangleCount()) < 0;
                 ++index) {
              final long v0 = reader.readU64(name);
              final long v1 = reader.readU64(name);
              final long v2 = reader.readU64(name);
              tri.onDataTriangle(v0, v1, v2);
            }
            break;
          }

          default: {
            throw new UnreachableCodeException();
          }
        }
      } catch (final IOException e) {
        tri.onError(SMFParseError.of(
          reader.positionLexical(), e.getMessage(), Optional.of(e)));
      } finally {
        tri.onDataTrianglesFinish();
      }
    }
  }
}
