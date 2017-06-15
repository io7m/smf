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
import com.io7m.smfj.core.SMFSchemaIdentifier;
import com.io7m.smfj.format.text.SMFTLineReaderType;
import com.io7m.smfj.format.text.SMFTParsingStatus;
import com.io7m.smfj.format.text.v1.SMFTV1HeaderCommandSchema;
import com.io7m.smfj.parser.api.SMFParserEventsHeaderType;
import javaslang.collection.List;
import mockit.Delegate;
import mockit.Mocked;
import mockit.StrictExpectations;
import org.junit.Assert;
import org.junit.Test;

import static com.io7m.smfj.format.text.SMFTParsingStatus.FAILURE;
import static com.io7m.smfj.format.text.SMFTParsingStatus.SUCCESS;

public final class SMFTV1HeaderCommandSchemaTest
{
  @Test
  public void testOK_0(
    final @Mocked SMFParserEventsHeaderType events,
    final @Mocked SMFTLineReaderType reader)
    throws Exception
  {
    final SMFHeader.Builder header = SMFHeader.builder();
    final SMFTV1HeaderCommandSchema cmd =
      new SMFTV1HeaderCommandSchema(reader, header);

    new StrictExpectations()
    {{

    }};

    final SMFTParsingStatus r =
      cmd.parse(events, List.of("schema", "com.io7m.example", "1", "2"));
    Assert.assertEquals(SUCCESS, r);

    final SMFHeader result = header.build();
    final SMFSchemaIdentifier schema = result.schemaIdentifier().get();

    Assert.assertEquals("com.io7m.example", schema.name().value());
    Assert.assertEquals(1L, (long) schema.versionMajor());
    Assert.assertEquals(2L, (long) schema.versionMinor());
  }

  @Test
  public void testFailure_0(
    final @Mocked SMFParserEventsHeaderType events,
    final @Mocked SMFTLineReaderType reader)
    throws Exception
  {
    final SMFHeader.Builder header = SMFHeader.builder();
    final SMFTV1HeaderCommandSchema cmd =
      new SMFTV1HeaderCommandSchema(reader, header);

    new StrictExpectations()
    {{
      events.onError(this.with(new Delegate<SMFErrorType>()
      {
        boolean check(final SMFErrorType e)
        {
          return e.message().contains(SMFTV1HeaderCommandSchema.SYNTAX);
        }
      }));
    }};

    final SMFTParsingStatus r =
      cmd.parse(events, List.of("schema"));
    Assert.assertEquals(FAILURE, r);
  }

  @Test
  public void testFailure_1(
    final @Mocked SMFParserEventsHeaderType events,
    final @Mocked SMFTLineReaderType reader)
    throws Exception
  {
    final SMFHeader.Builder header = SMFHeader.builder();
    final SMFTV1HeaderCommandSchema cmd =
      new SMFTV1HeaderCommandSchema(reader, header);

    new StrictExpectations()
    {{
      events.onError(this.with(new Delegate<SMFErrorType>()
      {
        boolean check(final SMFErrorType e)
        {
          return e.message().contains(SMFTV1HeaderCommandSchema.SYNTAX);
        }
      }));
    }};

    final SMFTParsingStatus r =
      cmd.parse(events, List.of("schema", "0"));
    Assert.assertEquals(FAILURE, r);
  }

  @Test
  public void testFailure_2(
    final @Mocked SMFParserEventsHeaderType events,
    final @Mocked SMFTLineReaderType reader)
    throws Exception
  {
    final SMFHeader.Builder header = SMFHeader.builder();
    final SMFTV1HeaderCommandSchema cmd =
      new SMFTV1HeaderCommandSchema(reader, header);

    new StrictExpectations()
    {{
      events.onError(this.with(new Delegate<SMFErrorType>()
      {
        boolean check(final SMFErrorType e)
        {
          return e.message().contains(SMFTV1HeaderCommandSchema.SYNTAX);
        }
      }));
    }};

    final SMFTParsingStatus r =
      cmd.parse(events, List.of("schema", "0", "1"));
    Assert.assertEquals(FAILURE, r);
  }

  @Test
  public void testFailure_3(
    final @Mocked SMFParserEventsHeaderType events,
    final @Mocked SMFTLineReaderType reader)
    throws Exception
  {
    final SMFHeader.Builder header = SMFHeader.builder();
    final SMFTV1HeaderCommandSchema cmd =
      new SMFTV1HeaderCommandSchema(reader, header);

    new StrictExpectations()
    {{
      events.onError(this.with(new Delegate<SMFErrorType>()
      {
        boolean check(final SMFErrorType e)
        {
          return e.message().contains(SMFTV1HeaderCommandSchema.SYNTAX);
        }
      }));
    }};

    final SMFTParsingStatus r =
      cmd.parse(events, List.of("schema", "0", "1", "2"));
    Assert.assertEquals(FAILURE, r);
  }

  @Test
  public void testFailure_4(
    final @Mocked SMFParserEventsHeaderType events,
    final @Mocked SMFTLineReaderType reader)
    throws Exception
  {
    final SMFHeader.Builder header = SMFHeader.builder();
    final SMFTV1HeaderCommandSchema cmd =
      new SMFTV1HeaderCommandSchema(reader, header);

    new StrictExpectations()
    {{
      events.onError(this.with(new Delegate<SMFErrorType>()
      {
        boolean check(final SMFErrorType e)
        {
          return e.message().contains(SMFTV1HeaderCommandSchema.SYNTAX);
        }
      }));
    }};

    final SMFTParsingStatus r =
      cmd.parse(events, List.of("schema", "x", "1", "2", "3"));
    Assert.assertEquals(FAILURE, r);
  }

  @Test
  public void testFailure_5(
    final @Mocked SMFParserEventsHeaderType events,
    final @Mocked SMFTLineReaderType reader)
    throws Exception
  {
    final SMFHeader.Builder header = SMFHeader.builder();
    final SMFTV1HeaderCommandSchema cmd =
      new SMFTV1HeaderCommandSchema(reader, header);

    new StrictExpectations()
    {{
      events.onError(this.with(new Delegate<SMFErrorType>()
      {
        boolean check(final SMFErrorType e)
        {
          return e.message().contains(SMFTV1HeaderCommandSchema.SYNTAX);
        }
      }));
    }};

    final SMFTParsingStatus r =
      cmd.parse(events, List.of("schema", "0", "1", "2", "3", "4"));
    Assert.assertEquals(FAILURE, r);
  }
}