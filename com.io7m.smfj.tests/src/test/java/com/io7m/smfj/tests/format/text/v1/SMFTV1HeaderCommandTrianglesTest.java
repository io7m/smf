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

package com.io7m.smfj.tests.format.text.v1;

import com.io7m.smfj.core.SMFErrorType;
import com.io7m.smfj.core.SMFHeader;
import com.io7m.smfj.format.text.SMFTLineReaderType;
import com.io7m.smfj.format.text.SMFTParsingStatus;
import com.io7m.smfj.format.text.v1.SMFTV1HeaderCommandTriangles;
import com.io7m.smfj.parser.api.SMFParserEventsHeaderType;
import io.vavr.collection.List;
import mockit.Delegate;
import mockit.Mocked;
import mockit.StrictExpectations;
import org.junit.Assert;
import org.junit.Test;

import static com.io7m.smfj.format.text.SMFTParsingStatus.FAILURE;
import static com.io7m.smfj.format.text.SMFTParsingStatus.SUCCESS;

public final class SMFTV1HeaderCommandTrianglesTest
{
  @Test
  public void testOK_0(
    final @Mocked SMFParserEventsHeaderType events,
    final @Mocked SMFTLineReaderType reader)
    throws Exception
  {
    final SMFHeader.Builder header = SMFHeader.builder();
    final SMFTV1HeaderCommandTriangles cmd =
      new SMFTV1HeaderCommandTriangles(reader, header);

    new StrictExpectations()
    {{

    }};

    final SMFTParsingStatus r =
      cmd.parse(events, List.of("triangles", "100", "32"));
    Assert.assertEquals(SUCCESS, r);

    final SMFHeader result = header.build();
    Assert.assertEquals(100L, result.triangles().triangleCount());
    Assert.assertEquals(32L, result.triangles().triangleIndexSizeBits());
  }

  @Test
  public void testFailure_0(
    final @Mocked SMFParserEventsHeaderType events,
    final @Mocked SMFTLineReaderType reader)
    throws Exception
  {
    final SMFHeader.Builder header = SMFHeader.builder();
    final SMFTV1HeaderCommandTriangles cmd =
      new SMFTV1HeaderCommandTriangles(reader, header);

    new StrictExpectations()
    {{
      events.onError(this.with(new Delegate<SMFErrorType>()
      {
        boolean check(final SMFErrorType e)
        {
          return e.message().contains(SMFTV1HeaderCommandTriangles.SYNTAX);
        }
      }));
    }};

    final SMFTParsingStatus r =
      cmd.parse(events, List.of("triangles"));
    Assert.assertEquals(FAILURE, r);
  }

  @Test
  public void testFailure_1(
    final @Mocked SMFParserEventsHeaderType events,
    final @Mocked SMFTLineReaderType reader)
    throws Exception
  {
    final SMFHeader.Builder header = SMFHeader.builder();
    final SMFTV1HeaderCommandTriangles cmd =
      new SMFTV1HeaderCommandTriangles(reader, header);

    new StrictExpectations()
    {{
      events.onError(this.with(new Delegate<SMFErrorType>()
      {
        boolean check(final SMFErrorType e)
        {
          return e.message().contains(SMFTV1HeaderCommandTriangles.SYNTAX);
        }
      }));
    }};

    final SMFTParsingStatus r =
      cmd.parse(events, List.of("triangles", "z", "32"));
    Assert.assertEquals(FAILURE, r);
  }

  @Test
  public void testFailure_2(
    final @Mocked SMFParserEventsHeaderType events,
    final @Mocked SMFTLineReaderType reader)
    throws Exception
  {
    final SMFHeader.Builder header = SMFHeader.builder();
    final SMFTV1HeaderCommandTriangles cmd =
      new SMFTV1HeaderCommandTriangles(reader, header);

    new StrictExpectations()
    {{
      events.onError(this.with(new Delegate<SMFErrorType>()
      {
        boolean check(final SMFErrorType e)
        {
          return e.message().contains(SMFTV1HeaderCommandTriangles.SYNTAX);
        }
      }));
    }};

    final SMFTParsingStatus r =
      cmd.parse(events, List.of("triangles", "100", "z"));
    Assert.assertEquals(FAILURE, r);
  }
}
