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
import com.io7m.smfj.core.SMFSchemaName;
import com.io7m.smfj.format.text.SMFTLineReaderList;
import com.io7m.smfj.format.text.SMFTLineReaderType;
import com.io7m.smfj.format.text.SMFTParsingStatus;
import com.io7m.smfj.format.text.v1.SMFTV1BodySectionParserMetadata;
import com.io7m.smfj.parser.api.SMFParserEventsBodyType;
import com.io7m.smfj.parser.api.SMFParserEventsDataMetaType;
import io.vavr.collection.List;
import mockit.Delegate;
import mockit.Mocked;
import mockit.Expectations;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.util.Optional;

import static com.io7m.smfj.format.text.SMFTParsingStatus.FAILURE;
import static com.io7m.smfj.format.text.SMFTParsingStatus.SUCCESS;

public final class SMFTV1BodyCommandMetadataTest
{
  @Test
  public void testOK_IgnoreAll(
    final @Mocked SMFParserEventsBodyType events)
    throws Exception
  {
    final SMFTLineReaderType reader =
      SMFTLineReaderList.create(
        URI.create("urn:x"),
        List.of(
          "aGVsbG8taGVsbG8K",
          "end"),
        0);

    final SMFHeader.Builder header_b = SMFHeader.builder();
    final SMFHeader header = header_b.build();

    final SMFTV1BodySectionParserMetadata cmd =
      new SMFTV1BodySectionParserMetadata(() -> header, reader);

    new Expectations()
    {{
      events.onMeta(SMFSchemaIdentifier.of(
        SMFSchemaName.of("com.io7m.smf.example"), 1, 0));
      this.result = Optional.empty();
    }};

    final SMFTParsingStatus r =
      cmd.parse(
        events, List.of("metadata", "com.io7m.smf.example", "1", "0", "1"));
    Assertions.assertEquals(SUCCESS, r);
  }

  @Test
  public void testMalformedCommand_0(
    final @Mocked SMFParserEventsBodyType events,
    final @Mocked SMFTLineReaderType reader)
    throws Exception
  {
    final SMFHeader.Builder header_b = SMFHeader.builder();
    final SMFHeader header = header_b.build();

    final SMFTV1BodySectionParserMetadata cmd =
      new SMFTV1BodySectionParserMetadata(() -> header, reader);

    new Expectations()
    {{
      events.onError(this.with(new Delegate<SMFErrorType>()
      {
        boolean check(final SMFErrorType e)
        {
          return e.message().contains("Cannot parse");
        }
      }));
    }};

    final SMFTParsingStatus r =
      cmd.parse(events, List.of("metadata", "x"));
    Assertions.assertEquals(FAILURE, r);
  }

  @Test
  public void testUnexpectedEOF(
    final @Mocked SMFParserEventsDataMetaType events_meta,
    final @Mocked SMFParserEventsBodyType events)
    throws Exception
  {
    final SMFTLineReaderType reader =
      SMFTLineReaderList.create(
        URI.create("urn:x"),
        List.of(),
        0);

    final SMFHeader.Builder header_b = SMFHeader.builder();
    final SMFHeader header = header_b.build();

    final SMFTV1BodySectionParserMetadata cmd =
      new SMFTV1BodySectionParserMetadata(() -> header, reader);

    new Expectations()
    {{
      events.onMeta(
        SMFSchemaIdentifier.of(
          SMFSchemaName.of("com.io7m.smf.example"), 1, 0));

      events.onError(this.with(new Delegate<SMFErrorType>()
      {
        boolean check(final SMFErrorType e)
        {
          return e.message().contains("Unexpected EOF");
        }
      }));
    }};

    final SMFTParsingStatus r =
      cmd.parse(
        events, List.of("metadata", "com.io7m.smf.example", "1", "0", "1"));
    Assertions.assertEquals(FAILURE, r);
  }

  @Test
  public void testUnparseable0(
    final @Mocked SMFParserEventsDataMetaType events_meta,
    final @Mocked SMFParserEventsBodyType events)
    throws Exception
  {
    final SMFTLineReaderType reader =
      SMFTLineReaderList.create(
        URI.create("urn:x"),
        List.of(
          "=",
          "end"),
        0);

    final SMFHeader.Builder header_b = SMFHeader.builder();
    final SMFHeader header = header_b.build();

    final SMFTV1BodySectionParserMetadata cmd =
      new SMFTV1BodySectionParserMetadata(() -> header, reader);

    final SMFSchemaIdentifier id =
      SMFSchemaIdentifier.of(
        SMFSchemaName.of("com.io7m.smf.example"), 1, 0);

    new Expectations()
    {{
      events.onMeta(id);
      this.result = Optional.of(events_meta);

      events_meta.onError(this.with(new Delegate<SMFErrorType>()
      {
        boolean check(final SMFErrorType e)
        {
          return e.message().contains("Cannot parse base64 encoded data");
        }
      }));
    }};

    final SMFTParsingStatus r =
      cmd.parse(
        events, List.of("metadata", "com.io7m.smf.example", "1", "0", "1"));
    Assertions.assertEquals(FAILURE, r);
  }

  @Test
  public void testUnparseable1(
    final @Mocked SMFParserEventsDataMetaType events_meta,
    final @Mocked SMFParserEventsBodyType events)
    throws Exception
  {
    final SMFTLineReaderType reader =
      SMFTLineReaderList.create(
        URI.create("urn:x"),
        List.of(
          "",
          "end"),
        0);

    final SMFHeader.Builder header_b = SMFHeader.builder();
    final SMFHeader header = header_b.build();

    final SMFTV1BodySectionParserMetadata cmd =
      new SMFTV1BodySectionParserMetadata(() -> header, reader);

    final SMFSchemaIdentifier id =
      SMFSchemaIdentifier.of(
        SMFSchemaName.of("com.io7m.smf.example"), 1, 0);

    new Expectations()
    {{
      events.onMeta(id);
      this.result = Optional.of(events_meta);

      events_meta.onError(this.with(new Delegate<SMFErrorType>()
      {
        boolean check(final SMFErrorType e)
        {
          return e.message().contains("Cannot parse base64 encoded data");
        }
      }));
    }};

    final SMFTParsingStatus r =
      cmd.parse(
        events, List.of("metadata", "com.io7m.smf.example", "1", "0", "1"));
    Assertions.assertEquals(FAILURE, r);
  }

  @Test
  public void testUnparseable2(
    final @Mocked SMFParserEventsDataMetaType events_meta,
    final @Mocked SMFParserEventsBodyType events)
    throws Exception
  {
    final SMFTLineReaderType reader =
      SMFTLineReaderList.create(
        URI.create("urn:x"),
        List.of(),
        0);

    final SMFHeader.Builder header_b = SMFHeader.builder();
    final SMFHeader header = header_b.build();

    final SMFTV1BodySectionParserMetadata cmd =
      new SMFTV1BodySectionParserMetadata(() -> header, reader);

    final SMFSchemaIdentifier id =
      SMFSchemaIdentifier.of(
        SMFSchemaName.of("com.io7m.smf.example"), 1, 0);

    new Expectations()
    {{
      events.onMeta(id);
      this.result = Optional.of(events_meta);

      events_meta.onError(this.with(new Delegate<SMFErrorType>()
      {
        boolean check(final SMFErrorType e)
        {
          return e.message().contains("Unexpected EOF");
        }
      }));
    }};

    final SMFTParsingStatus r =
      cmd.parse(
        events, List.of("metadata", "com.io7m.smf.example", "1", "0", "1"));
    Assertions.assertEquals(FAILURE, r);
  }

  @Test
  public void testUnparseable3(
    final @Mocked SMFParserEventsBodyType events)
    throws Exception
  {
    final SMFTLineReaderType reader =
      SMFTLineReaderList.create(
        URI.create("urn:x"),
        List.of(
          "metadata z com.io7m.smf.example 1"),
        0);

    final SMFHeader.Builder header_b = SMFHeader.builder();
    final SMFHeader header = header_b.build();

    final SMFTV1BodySectionParserMetadata cmd =
      new SMFTV1BodySectionParserMetadata(() -> header, reader);

    new Expectations()
    {{
      events.onError(this.with(new Delegate<SMFErrorType>()
      {
        boolean check(final SMFErrorType e)
        {
          return e.message().contains("Cannot parse meta command");
        }
      }));
    }};

    final SMFTParsingStatus r =
      cmd.parse(events, List.of("metadata"));
    Assertions.assertEquals(FAILURE, r);
  }

  @Test
  public void testUnparseable4(
    final @Mocked SMFParserEventsBodyType events)
    throws Exception
  {
    final SMFTLineReaderType reader =
      SMFTLineReaderList.create(
        URI.create("urn:x"),
        List.of(
          "metadata com.io7m.smf.example z 1"),
        0);

    final SMFHeader.Builder header_b = SMFHeader.builder();
    final SMFHeader header = header_b.build();

    final SMFTV1BodySectionParserMetadata cmd =
      new SMFTV1BodySectionParserMetadata(() -> header, reader);

    new Expectations()
    {{
      events.onError(this.with(new Delegate<SMFErrorType>()
      {
        boolean check(final SMFErrorType e)
        {
          return e.message().contains("Cannot parse meta command");
        }
      }));
    }};

    final SMFTParsingStatus r =
      cmd.parse(events, List.of("metadata"));
    Assertions.assertEquals(FAILURE, r);
  }

  @Test
  public void testUnparseable5(
    final @Mocked SMFParserEventsBodyType events)
    throws Exception
  {
    final SMFTLineReaderType reader =
      SMFTLineReaderList.create(
        URI.create("urn:x"),
        List.of(
          "metadata com.io7m.smf.example 1 z 1"),
        0);

    final SMFHeader.Builder header_b = SMFHeader.builder();
    final SMFHeader header = header_b.build();

    final SMFTV1BodySectionParserMetadata cmd =
      new SMFTV1BodySectionParserMetadata(() -> header, reader);

    new Expectations()
    {{
      events.onError(this.with(new Delegate<SMFErrorType>()
      {
        boolean check(final SMFErrorType e)
        {
          return e.message().contains("Cannot parse meta command");
        }
      }));
    }};

    final SMFTParsingStatus r =
      cmd.parse(events, List.of("metadata"));
    Assertions.assertEquals(FAILURE, r);
  }

  @Test
  public void testUnparseable6(
    final @Mocked SMFParserEventsBodyType events)
    throws Exception
  {
    final SMFTLineReaderType reader =
      SMFTLineReaderList.create(
        URI.create("urn:x"),
        List.of(
          "metadata com.io7m.smf.example 1 0 q"),
        0);

    final SMFHeader.Builder header_b = SMFHeader.builder();
    final SMFHeader header = header_b.build();

    final SMFTV1BodySectionParserMetadata cmd =
      new SMFTV1BodySectionParserMetadata(() -> header, reader);

    new Expectations()
    {{
      events.onError(this.with(new Delegate<SMFErrorType>()
      {
        boolean check(final SMFErrorType e)
        {
          return e.message().contains("Cannot parse meta command");
        }
      }));
    }};

    final SMFTParsingStatus r =
      cmd.parse(events, List.of("metadata"));
    Assertions.assertEquals(FAILURE, r);
  }

  @Test
  public void testUnparseable7(
    final @Mocked SMFParserEventsBodyType events)
    throws Exception
  {
    final SMFTLineReaderType reader =
      SMFTLineReaderList.create(
        URI.create("urn:x"),
        List.of(
          "metadata"),
        0);

    final SMFHeader.Builder header_b = SMFHeader.builder();
    final SMFHeader header = header_b.build();

    final SMFTV1BodySectionParserMetadata cmd =
      new SMFTV1BodySectionParserMetadata(() -> header, reader);

    new Expectations()
    {{
      events.onError(this.with(new Delegate<SMFErrorType>()
      {
        boolean check(final SMFErrorType e)
        {
          return e.message().contains("Cannot parse meta command");
        }
      }));
    }};

    final SMFTParsingStatus r =
      cmd.parse(events, List.of("metadata"));
    Assertions.assertEquals(FAILURE, r);
  }

  @Test
  public void testCorrect_0(
    final @Mocked SMFParserEventsDataMetaType events_meta,
    final @Mocked SMFParserEventsBodyType events)
    throws Exception
  {
    final SMFTLineReaderType reader =
      SMFTLineReaderList.create(
        URI.create("urn:x"),
        List.of(
          "AA==",
          "end"),
        0);

    final SMFHeader.Builder header_b = SMFHeader.builder();
    final SMFHeader header = header_b.build();

    final SMFTV1BodySectionParserMetadata cmd =
      new SMFTV1BodySectionParserMetadata(() -> header, reader);

    final SMFSchemaIdentifier id0 =
      SMFSchemaIdentifier.of(
        SMFSchemaName.of("com.io7m.smf.example"), 1, 0);

    new Expectations()
    {{
      events.onMeta(id0);
      this.result = Optional.of(events_meta);
      events_meta.onMetaData(id0, new byte[]{(byte) 0x0});
    }};

    final SMFTParsingStatus r =
      cmd.parse(events, List.of("metadata", "com.io7m.smf.example", "1", "0", "1"));
    Assertions.assertEquals(SUCCESS, r);
  }
}
