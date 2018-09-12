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

import com.io7m.smfj.core.SMFAttribute;
import com.io7m.smfj.core.SMFAttributeName;
import com.io7m.smfj.core.SMFErrorType;
import com.io7m.smfj.core.SMFHeader;
import com.io7m.smfj.format.text.SMFTLineReaderList;
import com.io7m.smfj.format.text.SMFTLineReaderType;
import com.io7m.smfj.format.text.SMFTParsingStatus;
import com.io7m.smfj.format.text.implementation.Flags;
import com.io7m.smfj.format.text.v1.SMFTV1BodySectionParserVerticesNonInterleaved;
import com.io7m.smfj.parser.api.SMFParserEventsBodyType;
import com.io7m.smfj.parser.api.SMFParserEventsDataAttributeValuesType;
import com.io7m.smfj.parser.api.SMFParserEventsDataAttributesNonInterleavedType;
import io.vavr.collection.List;
import mockit.Delegate;
import mockit.Expectations;
import mockit.Mocked;
import mockit.Expectations;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.util.BitSet;
import java.util.Optional;

import static com.io7m.smfj.core.SMFComponentType.ELEMENT_TYPE_FLOATING;
import static com.io7m.smfj.core.SMFComponentType.ELEMENT_TYPE_INTEGER_SIGNED;
import static com.io7m.smfj.core.SMFComponentType.ELEMENT_TYPE_INTEGER_UNSIGNED;
import static com.io7m.smfj.format.text.SMFTParsingStatus.FAILURE;
import static com.io7m.smfj.format.text.SMFTParsingStatus.SUCCESS;

public final class SMFTV1BodyCommandVerticesNonInterleavedTest
{
  @Test
  public void testOK_0(
    final @Mocked SMFParserEventsBodyType events,
    final @Mocked SMFTLineReaderType reader)
    throws Exception
  {
    final BitSet state = new BitSet();

    final SMFHeader.Builder header_b = SMFHeader.builder();
    final SMFHeader header = header_b.build();

    final SMFTV1BodySectionParserVerticesNonInterleaved cmd =
      new SMFTV1BodySectionParserVerticesNonInterleaved(() -> header, reader, state);

    new Expectations()
    {{
      reader.line();
      this.result = Optional.of(List.of("end"));
    }};

    final SMFTParsingStatus r =
      cmd.parse(events, List.of("vertices", "noninterleaved"));
    Assertions.assertEquals(SUCCESS, r);
    Assertions.assertTrue(state.get(Flags.VERTICES_RECEIVED));
  }

  @Test
  public void testUndeclaredAttribute(
    final @Mocked SMFParserEventsBodyType events,
    final @Mocked SMFTLineReaderType reader)
    throws Exception
  {
    final BitSet state = new BitSet();

    final SMFHeader.Builder header_b = SMFHeader.builder();
    final SMFHeader header = header_b.build();

    final SMFTV1BodySectionParserVerticesNonInterleaved cmd =
      new SMFTV1BodySectionParserVerticesNonInterleaved(() -> header, reader, state);

    new Expectations()
    {{
      reader.line();
      this.result = Optional.of(List.of("attribute", "x"));

      events.onError(this.with(new Delegate<SMFErrorType>()
      {
        boolean check(final SMFErrorType e)
        {
          return e.message().contains("Unknown attribute");
        }
      }));
    }};

    final SMFTParsingStatus r =
      cmd.parse(events, List.of("vertices", "noninterleaved"));
    Assertions.assertEquals(FAILURE, r);
  }

  @Test
  public void testDuplicateAttribute(
    final @Mocked SMFParserEventsBodyType events)
    throws Exception
  {
    final BitSet state = new BitSet();

    final SMFTLineReaderType reader =
      SMFTLineReaderList.create(
        URI.create("urn:x"),
        List.of(
          "attribute x",
          "attribute x"),
        0);

    final SMFHeader.Builder header_b = SMFHeader.builder();
    header_b.addAttributesInOrder(
      SMFAttribute.of(
        SMFAttributeName.of("x"),
        ELEMENT_TYPE_FLOATING,
        3,
        32));

    final SMFHeader header = header_b.build();

    final SMFTV1BodySectionParserVerticesNonInterleaved cmd =
      new SMFTV1BodySectionParserVerticesNonInterleaved(() -> header, reader, state);

    new Expectations()
    {{
      events.onError(this.with(new Delegate<SMFErrorType>()
      {
        boolean check(final SMFErrorType e)
        {
          return e.message().contains("Attribute already specified");
        }
      }));
    }};

    final SMFTParsingStatus r =
      cmd.parse(events, List.of("vertices", "noninterleaved"));
    Assertions.assertEquals(FAILURE, r);
  }

  @Test
  public void testBadAttribute(
    final @Mocked SMFParserEventsBodyType events)
    throws Exception
  {
    final BitSet state = new BitSet();

    final SMFTLineReaderType reader =
      SMFTLineReaderList.create(
        URI.create("urn:x"),
        List.of(
          "attribute"),
        0);

    final SMFHeader.Builder header_b = SMFHeader.builder();
    final SMFHeader header = header_b.build();

    final SMFTV1BodySectionParserVerticesNonInterleaved cmd =
      new SMFTV1BodySectionParserVerticesNonInterleaved(() -> header, reader, state);

    new Expectations()
    {{
      events.onError(this.with(new Delegate<SMFErrorType>()
      {
        boolean check(final SMFErrorType e)
        {
          return e.message().contains("attribute <name>");
        }
      }));
    }};

    final SMFTParsingStatus r =
      cmd.parse(events, List.of("vertices", "noninterleaved"));
    Assertions.assertEquals(FAILURE, r);
  }

  @Test
  public void testUnexpectedEOF(
    final @Mocked SMFParserEventsDataAttributeValuesType events_values,
    final @Mocked SMFParserEventsDataAttributesNonInterleavedType events_data,
    final @Mocked SMFParserEventsBodyType events)
    throws Exception
  {
    final BitSet state = new BitSet();

    final SMFTLineReaderType reader =
      SMFTLineReaderList.create(
        URI.create("urn:x"),
        List.of(),
        0);

    final SMFHeader.Builder header_b = SMFHeader.builder();
    final SMFHeader header = header_b.build();

    final SMFTV1BodySectionParserVerticesNonInterleaved cmd =
      new SMFTV1BodySectionParserVerticesNonInterleaved(() -> header, reader, state);

    new Expectations()
    {{
      events.onAttributesNonInterleaved();
      this.result = Optional.of(events_data);

      events_data.onError(this.with(new Delegate<SMFErrorType>()
      {
        boolean check(final SMFErrorType e)
        {
          return e.message().contains("Unexpected EOF");
        }
      }));
      events_data.onDataAttributesNonInterleavedFinish();
    }};

    final SMFTParsingStatus r =
      cmd.parse(events, List.of("vertices", "noninterleaved"));
    Assertions.assertEquals(FAILURE, r);
  }

  @Test
  public void testAttributeFloat4(
    final @Mocked SMFParserEventsDataAttributeValuesType events_values,
    final @Mocked SMFParserEventsDataAttributesNonInterleavedType events_data,
    final @Mocked SMFParserEventsBodyType events)
    throws Exception
  {
    final BitSet state = new BitSet();

    final SMFTLineReaderType reader =
      SMFTLineReaderList.create(
        URI.create("urn:x"),
        List.of(
          "attribute x",
          "0.0 1.0 2.0 3.0",
          "end"),
        0);

    final SMFHeader.Builder header_b = SMFHeader.builder();
    final SMFAttribute attribute =
      SMFAttribute.of(
        SMFAttributeName.of("x"), ELEMENT_TYPE_FLOATING, 4, 32);
    header_b.addAttributesInOrder(attribute);
    header_b.setVertexCount(1L);
    final SMFHeader header = header_b.build();

    final SMFTV1BodySectionParserVerticesNonInterleaved cmd =
      new SMFTV1BodySectionParserVerticesNonInterleaved(() -> header, reader, state);

    new Expectations()
    {{
      events.onAttributesNonInterleaved();
      this.result = Optional.of(events_data);

      events_data.onDataAttributeStart(attribute);
      this.result = Optional.of(events_values);

      events_values.onDataAttributeValueFloat4(0.0, 1.0, 2.0, 3.0);
      events_values.onDataAttributeValueFinish();

      events_data.onDataAttributesNonInterleavedFinish();
    }};

    final SMFTParsingStatus r =
      cmd.parse(events, List.of("vertices", "noninterleaved"));
    Assertions.assertEquals(SUCCESS, r);
  }

  @Test
  public void testAttributeFloat3(
    final @Mocked SMFParserEventsDataAttributeValuesType events_values,
    final @Mocked SMFParserEventsDataAttributesNonInterleavedType events_data,
    final @Mocked SMFParserEventsBodyType events)
    throws Exception
  {
    final BitSet state = new BitSet();

    final SMFTLineReaderType reader =
      SMFTLineReaderList.create(
        URI.create("urn:x"),
        List.of(
          "attribute x",
          "0.0 1.0 2.0",
          "end"),
        0);

    final SMFHeader.Builder header_b = SMFHeader.builder();
    final SMFAttribute attribute =
      SMFAttribute.of(
        SMFAttributeName.of("x"), ELEMENT_TYPE_FLOATING, 3, 32);
    header_b.addAttributesInOrder(attribute);
    header_b.setVertexCount(1L);
    final SMFHeader header = header_b.build();

    final SMFTV1BodySectionParserVerticesNonInterleaved cmd =
      new SMFTV1BodySectionParserVerticesNonInterleaved(() -> header, reader, state);

    new Expectations()
    {{
      events.onAttributesNonInterleaved();
      this.result = Optional.of(events_data);

      events_data.onDataAttributeStart(attribute);
      this.result = Optional.of(events_values);

      events_values.onDataAttributeValueFloat3(0.0, 1.0, 2.0);
      events_values.onDataAttributeValueFinish();

      events_data.onDataAttributesNonInterleavedFinish();
    }};

    final SMFTParsingStatus r =
      cmd.parse(events, List.of("vertices", "noninterleaved"));
    Assertions.assertEquals(SUCCESS, r);
  }

  @Test
  public void testAttributeFloat2(
    final @Mocked SMFParserEventsDataAttributeValuesType events_values,
    final @Mocked SMFParserEventsDataAttributesNonInterleavedType events_data,
    final @Mocked SMFParserEventsBodyType events)
    throws Exception
  {
    final BitSet state = new BitSet();

    final SMFTLineReaderType reader =
      SMFTLineReaderList.create(
        URI.create("urn:x"),
        List.of(
          "attribute x",
          "0.0 1.0",
          "end"),
        0);

    final SMFHeader.Builder header_b = SMFHeader.builder();
    final SMFAttribute attribute =
      SMFAttribute.of(
        SMFAttributeName.of("x"), ELEMENT_TYPE_FLOATING, 2, 32);
    header_b.addAttributesInOrder(attribute);
    header_b.setVertexCount(1L);
    final SMFHeader header = header_b.build();

    final SMFTV1BodySectionParserVerticesNonInterleaved cmd =
      new SMFTV1BodySectionParserVerticesNonInterleaved(() -> header, reader,
                                                        state);

    new Expectations()
    {{
      events.onAttributesNonInterleaved();
      this.result = Optional.of(events_data);

      events_data.onDataAttributeStart(attribute);
      this.result = Optional.of(events_values);

      events_values.onDataAttributeValueFloat2(0.0, 1.0);
      events_values.onDataAttributeValueFinish();

      events_data.onDataAttributesNonInterleavedFinish();
    }};

    final SMFTParsingStatus r =
      cmd.parse(events, List.of("vertices", "noninterleaved"));
    Assertions.assertEquals(SUCCESS, r);
  }

  @Test
  public void testAttributeFloat1(
    final @Mocked SMFParserEventsDataAttributeValuesType events_values,
    final @Mocked SMFParserEventsDataAttributesNonInterleavedType events_data,
    final @Mocked SMFParserEventsBodyType events)
    throws Exception
  {
    final BitSet state = new BitSet();

    final SMFTLineReaderType reader =
      SMFTLineReaderList.create(
        URI.create("urn:x"),
        List.of(
          "attribute x",
          "0.0",
          "end"),
        0);

    final SMFHeader.Builder header_b = SMFHeader.builder();
    final SMFAttribute attribute =
      SMFAttribute.of(
        SMFAttributeName.of("x"), ELEMENT_TYPE_FLOATING, 1, 32);
    header_b.addAttributesInOrder(attribute);
    header_b.setVertexCount(1L);
    final SMFHeader header = header_b.build();

    final SMFTV1BodySectionParserVerticesNonInterleaved cmd =
      new SMFTV1BodySectionParserVerticesNonInterleaved(() -> header, reader,
                                                        state);

    new Expectations()
    {{
      events.onAttributesNonInterleaved();
      this.result = Optional.of(events_data);

      events_data.onDataAttributeStart(attribute);
      this.result = Optional.of(events_values);

      events_values.onDataAttributeValueFloat1(0.0);
      events_values.onDataAttributeValueFinish();

      events_data.onDataAttributesNonInterleavedFinish();
    }};

    final SMFTParsingStatus r =
      cmd.parse(events, List.of("vertices", "noninterleaved"));
    Assertions.assertEquals(SUCCESS, r);
  }

  @Test
  public void testAttributeBadFloat4(
    final @Mocked SMFParserEventsDataAttributeValuesType events_values,
    final @Mocked SMFParserEventsDataAttributesNonInterleavedType events_data,
    final @Mocked SMFParserEventsBodyType events)
    throws Exception
  {
    final BitSet state = new BitSet();

    final SMFTLineReaderType reader =
      SMFTLineReaderList.create(
        URI.create("urn:x"),
        List.of(
          "attribute x",
          "z 1.0 2.0 3.0",
          "end"),
        0);

    final SMFHeader.Builder header_b = SMFHeader.builder();
    final SMFAttribute attribute =
      SMFAttribute.of(
        SMFAttributeName.of("x"), ELEMENT_TYPE_FLOATING, 4, 32);
    header_b.addAttributesInOrder(attribute);
    header_b.setVertexCount(1L);
    final SMFHeader header = header_b.build();

    final SMFTV1BodySectionParserVerticesNonInterleaved cmd =
      new SMFTV1BodySectionParserVerticesNonInterleaved(() -> header, reader,
                                                        state);

    new Expectations()
    {{
      events.onAttributesNonInterleaved();
      this.result = Optional.of(events_data);

      events_data.onDataAttributeStart(attribute);
      this.result = Optional.of(events_values);

      events_values.onError(this.with(new Delegate<SMFErrorType>()
      {
        boolean check(final SMFErrorType e)
        {
          return e.message().contains("Cannot parse");
        }
      }));
      events_values.onDataAttributeValueFinish();

      events_data.onDataAttributesNonInterleavedFinish();
    }};

    final SMFTParsingStatus r =
      cmd.parse(events, List.of("vertices", "noninterleaved"));
    Assertions.assertEquals(FAILURE, r);
  }

  @Test
  public void testAttributeBadFloat3(
    final @Mocked SMFParserEventsDataAttributeValuesType events_values,
    final @Mocked SMFParserEventsDataAttributesNonInterleavedType events_data,
    final @Mocked SMFParserEventsBodyType events)
    throws Exception
  {
    final BitSet state = new BitSet();

    final SMFTLineReaderType reader =
      SMFTLineReaderList.create(
        URI.create("urn:x"),
        List.of(
          "attribute x",
          "z 1.0 2.0",
          "end"),
        0);

    final SMFHeader.Builder header_b = SMFHeader.builder();
    final SMFAttribute attribute =
      SMFAttribute.of(
        SMFAttributeName.of("x"), ELEMENT_TYPE_FLOATING, 3, 32);
    header_b.addAttributesInOrder(attribute);
    header_b.setVertexCount(1L);
    final SMFHeader header = header_b.build();

    final SMFTV1BodySectionParserVerticesNonInterleaved cmd =
      new SMFTV1BodySectionParserVerticesNonInterleaved(() -> header, reader,
                                                        state);

    new Expectations()
    {{
      events.onAttributesNonInterleaved();
      this.result = Optional.of(events_data);

      events_data.onDataAttributeStart(attribute);
      this.result = Optional.of(events_values);

      events_values.onError(this.with(new Delegate<SMFErrorType>()
      {
        boolean check(final SMFErrorType e)
        {
          return e.message().contains("Cannot parse");
        }
      }));
      events_values.onDataAttributeValueFinish();

      events_data.onDataAttributesNonInterleavedFinish();
    }};

    final SMFTParsingStatus r =
      cmd.parse(events, List.of("vertices", "noninterleaved"));
    Assertions.assertEquals(FAILURE, r);
  }

  @Test
  public void testAttributeBadFloat2(
    final @Mocked SMFParserEventsDataAttributeValuesType events_values,
    final @Mocked SMFParserEventsDataAttributesNonInterleavedType events_data,
    final @Mocked SMFParserEventsBodyType events)
    throws Exception
  {
    final BitSet state = new BitSet();

    final SMFTLineReaderType reader =
      SMFTLineReaderList.create(
        URI.create("urn:x"),
        List.of(
          "attribute x",
          "z 1.0",
          "end"),
        0);

    final SMFHeader.Builder header_b = SMFHeader.builder();
    final SMFAttribute attribute =
      SMFAttribute.of(
        SMFAttributeName.of("x"), ELEMENT_TYPE_FLOATING, 2, 32);
    header_b.addAttributesInOrder(attribute);
    header_b.setVertexCount(1L);
    final SMFHeader header = header_b.build();

    final SMFTV1BodySectionParserVerticesNonInterleaved cmd =
      new SMFTV1BodySectionParserVerticesNonInterleaved(() -> header, reader,
                                                        state);

    new Expectations()
    {{
      events.onAttributesNonInterleaved();
      this.result = Optional.of(events_data);

      events_data.onDataAttributeStart(attribute);
      this.result = Optional.of(events_values);

      events_values.onError(this.with(new Delegate<SMFErrorType>()
      {
        boolean check(final SMFErrorType e)
        {
          return e.message().contains("Cannot parse");
        }
      }));
      events_values.onDataAttributeValueFinish();

      events_data.onDataAttributesNonInterleavedFinish();
    }};

    final SMFTParsingStatus r =
      cmd.parse(events, List.of("vertices", "noninterleaved"));
    Assertions.assertEquals(FAILURE, r);
  }

  @Test
  public void testAttributeBadFloat1(
    final @Mocked SMFParserEventsDataAttributeValuesType events_values,
    final @Mocked SMFParserEventsDataAttributesNonInterleavedType events_data,
    final @Mocked SMFParserEventsBodyType events)
    throws Exception
  {
    final BitSet state = new BitSet();

    final SMFTLineReaderType reader =
      SMFTLineReaderList.create(
        URI.create("urn:x"),
        List.of(
          "attribute x",
          "z",
          "end"),
        0);

    final SMFHeader.Builder header_b = SMFHeader.builder();
    final SMFAttribute attribute =
      SMFAttribute.of(
        SMFAttributeName.of("x"), ELEMENT_TYPE_FLOATING, 1, 32);
    header_b.addAttributesInOrder(attribute);
    header_b.setVertexCount(1L);
    final SMFHeader header = header_b.build();

    final SMFTV1BodySectionParserVerticesNonInterleaved cmd =
      new SMFTV1BodySectionParserVerticesNonInterleaved(() -> header, reader,
                                                        state);

    new Expectations()
    {{
      events.onAttributesNonInterleaved();
      this.result = Optional.of(events_data);

      events_data.onDataAttributeStart(attribute);
      this.result = Optional.of(events_values);

      events_values.onError(this.with(new Delegate<SMFErrorType>()
      {
        boolean check(final SMFErrorType e)
        {
          return e.message().contains("Cannot parse");
        }
      }));
      events_values.onDataAttributeValueFinish();

      events_data.onDataAttributesNonInterleavedFinish();
    }};

    final SMFTParsingStatus r =
      cmd.parse(events, List.of("vertices", "noninterleaved"));
    Assertions.assertEquals(FAILURE, r);
  }

  @Test
  public void testAttributeIntegerSigned4(
    final @Mocked SMFParserEventsDataAttributeValuesType events_values,
    final @Mocked SMFParserEventsDataAttributesNonInterleavedType events_data,
    final @Mocked SMFParserEventsBodyType events)
    throws Exception
  {
    final BitSet state = new BitSet();

    final SMFTLineReaderType reader =
      SMFTLineReaderList.create(
        URI.create("urn:x"),
        List.of(
          "attribute x",
          "0 1 2 3",
          "end"),
        0);

    final SMFHeader.Builder header_b = SMFHeader.builder();
    final SMFAttribute attribute =
      SMFAttribute.of(
        SMFAttributeName.of("x"), ELEMENT_TYPE_INTEGER_SIGNED, 4, 32);
    header_b.addAttributesInOrder(attribute);
    header_b.setVertexCount(1L);
    final SMFHeader header = header_b.build();

    final SMFTV1BodySectionParserVerticesNonInterleaved cmd =
      new SMFTV1BodySectionParserVerticesNonInterleaved(() -> header, reader,
                                                        state);

    new Expectations()
    {{
      events.onAttributesNonInterleaved();
      this.result = Optional.of(events_data);

      events_data.onDataAttributeStart(attribute);
      this.result = Optional.of(events_values);

      events_values.onDataAttributeValueIntegerSigned4(0, 1, 2, 3);
      events_values.onDataAttributeValueFinish();

      events_data.onDataAttributesNonInterleavedFinish();
    }};

    final SMFTParsingStatus r =
      cmd.parse(events, List.of("vertices", "noninterleaved"));
    Assertions.assertEquals(SUCCESS, r);
  }

  @Test
  public void testAttributeIntegerSigned3(
    final @Mocked SMFParserEventsDataAttributeValuesType events_values,
    final @Mocked SMFParserEventsDataAttributesNonInterleavedType events_data,
    final @Mocked SMFParserEventsBodyType events)
    throws Exception
  {
    final BitSet state = new BitSet();

    final SMFTLineReaderType reader =
      SMFTLineReaderList.create(
        URI.create("urn:x"),
        List.of(
          "attribute x",
          "0 1 2",
          "end"),
        0);

    final SMFHeader.Builder header_b = SMFHeader.builder();
    final SMFAttribute attribute =
      SMFAttribute.of(
        SMFAttributeName.of("x"), ELEMENT_TYPE_INTEGER_SIGNED, 3, 32);
    header_b.addAttributesInOrder(attribute);
    header_b.setVertexCount(1L);
    final SMFHeader header = header_b.build();

    final SMFTV1BodySectionParserVerticesNonInterleaved cmd =
      new SMFTV1BodySectionParserVerticesNonInterleaved(() -> header, reader,
                                                        state);

    new Expectations()
    {{
      events.onAttributesNonInterleaved();
      this.result = Optional.of(events_data);

      events_data.onDataAttributeStart(attribute);
      this.result = Optional.of(events_values);

      events_values.onDataAttributeValueIntegerSigned3(0, 1, 2);
      events_values.onDataAttributeValueFinish();

      events_data.onDataAttributesNonInterleavedFinish();
    }};

    final SMFTParsingStatus r =
      cmd.parse(events, List.of("vertices", "noninterleaved"));
    Assertions.assertEquals(SUCCESS, r);
  }

  @Test
  public void testAttributeIntegerSigned2(
    final @Mocked SMFParserEventsDataAttributeValuesType events_values,
    final @Mocked SMFParserEventsDataAttributesNonInterleavedType events_data,
    final @Mocked SMFParserEventsBodyType events)
    throws Exception
  {
    final BitSet state = new BitSet();

    final SMFTLineReaderType reader =
      SMFTLineReaderList.create(
        URI.create("urn:x"),
        List.of(
          "attribute x",
          "0 1",
          "end"),
        0);

    final SMFHeader.Builder header_b = SMFHeader.builder();
    final SMFAttribute attribute =
      SMFAttribute.of(
        SMFAttributeName.of("x"), ELEMENT_TYPE_INTEGER_SIGNED, 2, 32);
    header_b.addAttributesInOrder(attribute);
    header_b.setVertexCount(1L);
    final SMFHeader header = header_b.build();

    final SMFTV1BodySectionParserVerticesNonInterleaved cmd =
      new SMFTV1BodySectionParserVerticesNonInterleaved(() -> header, reader,
                                                        state);

    new Expectations()
    {{
      events.onAttributesNonInterleaved();
      this.result = Optional.of(events_data);

      events_data.onDataAttributeStart(attribute);
      this.result = Optional.of(events_values);

      events_values.onDataAttributeValueIntegerSigned2(0, 1);
      events_values.onDataAttributeValueFinish();

      events_data.onDataAttributesNonInterleavedFinish();
    }};

    final SMFTParsingStatus r =
      cmd.parse(events, List.of("vertices", "noninterleaved"));
    Assertions.assertEquals(SUCCESS, r);
  }

  @Test
  public void testAttributeIntegerSigned1(
    final @Mocked SMFParserEventsDataAttributeValuesType events_values,
    final @Mocked SMFParserEventsDataAttributesNonInterleavedType events_data,
    final @Mocked SMFParserEventsBodyType events)
    throws Exception
  {
    final BitSet state = new BitSet();

    final SMFTLineReaderType reader =
      SMFTLineReaderList.create(
        URI.create("urn:x"),
        List.of(
          "attribute x",
          "0",
          "end"),
        0);

    final SMFHeader.Builder header_b = SMFHeader.builder();
    final SMFAttribute attribute =
      SMFAttribute.of(
        SMFAttributeName.of("x"), ELEMENT_TYPE_INTEGER_SIGNED, 1, 32);
    header_b.addAttributesInOrder(attribute);
    header_b.setVertexCount(1L);
    final SMFHeader header = header_b.build();

    final SMFTV1BodySectionParserVerticesNonInterleaved cmd =
      new SMFTV1BodySectionParserVerticesNonInterleaved(() -> header, reader,
                                                        state);

    new Expectations()
    {{
      events.onAttributesNonInterleaved();
      this.result = Optional.of(events_data);

      events_data.onDataAttributeStart(attribute);
      this.result = Optional.of(events_values);

      events_values.onDataAttributeValueIntegerSigned1(0);
      events_values.onDataAttributeValueFinish();

      events_data.onDataAttributesNonInterleavedFinish();
    }};

    final SMFTParsingStatus r =
      cmd.parse(events, List.of("vertices", "noninterleaved"));
    Assertions.assertEquals(SUCCESS, r);
  }

  @Test
  public void testAttributeBadIntegerSigned4(
    final @Mocked SMFParserEventsDataAttributeValuesType events_values,
    final @Mocked SMFParserEventsDataAttributesNonInterleavedType events_data,
    final @Mocked SMFParserEventsBodyType events)
    throws Exception
  {
    final BitSet state = new BitSet();

    final SMFTLineReaderType reader =
      SMFTLineReaderList.create(
        URI.create("urn:x"),
        List.of(
          "attribute x",
          "z 1 2 3",
          "end"),
        0);

    final SMFHeader.Builder header_b = SMFHeader.builder();
    final SMFAttribute attribute =
      SMFAttribute.of(
        SMFAttributeName.of("x"), ELEMENT_TYPE_INTEGER_SIGNED, 4, 32);
    header_b.addAttributesInOrder(attribute);
    header_b.setVertexCount(1L);
    final SMFHeader header = header_b.build();

    final SMFTV1BodySectionParserVerticesNonInterleaved cmd =
      new SMFTV1BodySectionParserVerticesNonInterleaved(() -> header, reader,
                                                        state);

    new Expectations()
    {{
      events.onAttributesNonInterleaved();
      this.result = Optional.of(events_data);

      events_data.onDataAttributeStart(attribute);
      this.result = Optional.of(events_values);

      events_values.onError(this.with(new Delegate<SMFErrorType>()
      {
        boolean check(final SMFErrorType e)
        {
          return e.message().contains("Cannot parse");
        }
      }));
      events_values.onDataAttributeValueFinish();

      events_data.onDataAttributesNonInterleavedFinish();
    }};

    final SMFTParsingStatus r =
      cmd.parse(events, List.of("vertices", "noninterleaved"));
    Assertions.assertEquals(FAILURE, r);
  }

  @Test
  public void testAttributeBadIntegerSigned3(
    final @Mocked SMFParserEventsDataAttributeValuesType events_values,
    final @Mocked SMFParserEventsDataAttributesNonInterleavedType events_data,
    final @Mocked SMFParserEventsBodyType events)
    throws Exception
  {
    final BitSet state = new BitSet();

    final SMFTLineReaderType reader =
      SMFTLineReaderList.create(
        URI.create("urn:x"),
        List.of(
          "attribute x",
          "z 1 2",
          "end"),
        0);

    final SMFHeader.Builder header_b = SMFHeader.builder();
    final SMFAttribute attribute =
      SMFAttribute.of(
        SMFAttributeName.of("x"), ELEMENT_TYPE_INTEGER_SIGNED, 3, 32);
    header_b.addAttributesInOrder(attribute);
    header_b.setVertexCount(1L);
    final SMFHeader header = header_b.build();

    final SMFTV1BodySectionParserVerticesNonInterleaved cmd =
      new SMFTV1BodySectionParserVerticesNonInterleaved(() -> header, reader,
                                                        state);

    new Expectations()
    {{
      events.onAttributesNonInterleaved();
      this.result = Optional.of(events_data);

      events_data.onDataAttributeStart(attribute);
      this.result = Optional.of(events_values);

      events_values.onError(this.with(new Delegate<SMFErrorType>()
      {
        boolean check(final SMFErrorType e)
        {
          return e.message().contains("Cannot parse");
        }
      }));
      events_values.onDataAttributeValueFinish();

      events_data.onDataAttributesNonInterleavedFinish();
    }};

    final SMFTParsingStatus r =
      cmd.parse(events, List.of("vertices", "noninterleaved"));
    Assertions.assertEquals(FAILURE, r);
  }

  @Test
  public void testAttributeBadIntegerSigned2(
    final @Mocked SMFParserEventsDataAttributeValuesType events_values,
    final @Mocked SMFParserEventsDataAttributesNonInterleavedType events_data,
    final @Mocked SMFParserEventsBodyType events)
    throws Exception
  {
    final BitSet state = new BitSet();

    final SMFTLineReaderType reader =
      SMFTLineReaderList.create(
        URI.create("urn:x"),
        List.of(
          "attribute x",
          "z 1",
          "end"),
        0);

    final SMFHeader.Builder header_b = SMFHeader.builder();
    final SMFAttribute attribute =
      SMFAttribute.of(
        SMFAttributeName.of("x"), ELEMENT_TYPE_INTEGER_SIGNED, 2, 32);
    header_b.addAttributesInOrder(attribute);
    header_b.setVertexCount(1L);
    final SMFHeader header = header_b.build();

    final SMFTV1BodySectionParserVerticesNonInterleaved cmd =
      new SMFTV1BodySectionParserVerticesNonInterleaved(() -> header, reader,
                                                        state);

    new Expectations()
    {{
      events.onAttributesNonInterleaved();
      this.result = Optional.of(events_data);

      events_data.onDataAttributeStart(attribute);
      this.result = Optional.of(events_values);

      events_values.onError(this.with(new Delegate<SMFErrorType>()
      {
        boolean check(final SMFErrorType e)
        {
          return e.message().contains("Cannot parse");
        }
      }));
      events_values.onDataAttributeValueFinish();

      events_data.onDataAttributesNonInterleavedFinish();
    }};

    final SMFTParsingStatus r =
      cmd.parse(events, List.of("vertices", "noninterleaved"));
    Assertions.assertEquals(FAILURE, r);
  }

  @Test
  public void testAttributeBadIntegerSigned1(
    final @Mocked SMFParserEventsDataAttributeValuesType events_values,
    final @Mocked SMFParserEventsDataAttributesNonInterleavedType events_data,
    final @Mocked SMFParserEventsBodyType events)
    throws Exception
  {
    final BitSet state = new BitSet();

    final SMFTLineReaderType reader =
      SMFTLineReaderList.create(
        URI.create("urn:x"),
        List.of(
          "attribute x",
          "z",
          "end"),
        0);

    final SMFHeader.Builder header_b = SMFHeader.builder();
    final SMFAttribute attribute =
      SMFAttribute.of(
        SMFAttributeName.of("x"), ELEMENT_TYPE_INTEGER_SIGNED, 1, 32);
    header_b.addAttributesInOrder(attribute);
    header_b.setVertexCount(1L);
    final SMFHeader header = header_b.build();

    final SMFTV1BodySectionParserVerticesNonInterleaved cmd =
      new SMFTV1BodySectionParserVerticesNonInterleaved(() -> header, reader,
                                                        state);

    new Expectations()
    {{
      events.onAttributesNonInterleaved();
      this.result = Optional.of(events_data);

      events_data.onDataAttributeStart(attribute);
      this.result = Optional.of(events_values);

      events_values.onError(this.with(new Delegate<SMFErrorType>()
      {
        boolean check(final SMFErrorType e)
        {
          return e.message().contains("Cannot parse");
        }
      }));
      events_values.onDataAttributeValueFinish();

      events_data.onDataAttributesNonInterleavedFinish();
    }};

    final SMFTParsingStatus r =
      cmd.parse(events, List.of("vertices", "noninterleaved"));
    Assertions.assertEquals(FAILURE, r);
  }

  @Test
  public void testAttributeIntegerUnsigned4(
    final @Mocked SMFParserEventsDataAttributeValuesType events_values,
    final @Mocked SMFParserEventsDataAttributesNonInterleavedType events_data,
    final @Mocked SMFParserEventsBodyType events)
    throws Exception
  {
    final BitSet state = new BitSet();

    final SMFTLineReaderType reader =
      SMFTLineReaderList.create(
        URI.create("urn:x"),
        List.of(
          "attribute x",
          "0 1 2 3",
          "end"),
        0);

    final SMFHeader.Builder header_b = SMFHeader.builder();
    final SMFAttribute attribute =
      SMFAttribute.of(
        SMFAttributeName.of("x"), ELEMENT_TYPE_INTEGER_UNSIGNED, 4, 32);
    header_b.addAttributesInOrder(attribute);
    header_b.setVertexCount(1L);
    final SMFHeader header = header_b.build();

    final SMFTV1BodySectionParserVerticesNonInterleaved cmd =
      new SMFTV1BodySectionParserVerticesNonInterleaved(() -> header, reader,
                                                        state);

    new Expectations()
    {{
      events.onAttributesNonInterleaved();
      this.result = Optional.of(events_data);

      events_data.onDataAttributeStart(attribute);
      this.result = Optional.of(events_values);

      events_values.onDataAttributeValueIntegerUnsigned4(0, 1, 2, 3);
      events_values.onDataAttributeValueFinish();

      events_data.onDataAttributesNonInterleavedFinish();
    }};

    final SMFTParsingStatus r =
      cmd.parse(events, List.of("vertices", "noninterleaved"));
    Assertions.assertEquals(SUCCESS, r);
  }

  @Test
  public void testAttributeIntegerUnsigned3(
    final @Mocked SMFParserEventsDataAttributeValuesType events_values,
    final @Mocked SMFParserEventsDataAttributesNonInterleavedType events_data,
    final @Mocked SMFParserEventsBodyType events)
    throws Exception
  {
    final BitSet state = new BitSet();

    final SMFTLineReaderType reader =
      SMFTLineReaderList.create(
        URI.create("urn:x"),
        List.of(
          "attribute x",
          "0 1 2",
          "end"),
        0);

    final SMFHeader.Builder header_b = SMFHeader.builder();
    final SMFAttribute attribute =
      SMFAttribute.of(
        SMFAttributeName.of("x"), ELEMENT_TYPE_INTEGER_UNSIGNED, 3, 32);
    header_b.addAttributesInOrder(attribute);
    header_b.setVertexCount(1L);
    final SMFHeader header = header_b.build();

    final SMFTV1BodySectionParserVerticesNonInterleaved cmd =
      new SMFTV1BodySectionParserVerticesNonInterleaved(() -> header, reader,
                                                        state);

    new Expectations()
    {{
      events.onAttributesNonInterleaved();
      this.result = Optional.of(events_data);

      events_data.onDataAttributeStart(attribute);
      this.result = Optional.of(events_values);

      events_values.onDataAttributeValueIntegerUnsigned3(0, 1, 2);
      events_values.onDataAttributeValueFinish();

      events_data.onDataAttributesNonInterleavedFinish();
    }};

    final SMFTParsingStatus r =
      cmd.parse(events, List.of("vertices", "noninterleaved"));
    Assertions.assertEquals(SUCCESS, r);
  }

  @Test
  public void testAttributeIntegerUnsigned2(
    final @Mocked SMFParserEventsDataAttributeValuesType events_values,
    final @Mocked SMFParserEventsDataAttributesNonInterleavedType events_data,
    final @Mocked SMFParserEventsBodyType events)
    throws Exception
  {
    final BitSet state = new BitSet();

    final SMFTLineReaderType reader =
      SMFTLineReaderList.create(
        URI.create("urn:x"),
        List.of(
          "attribute x",
          "0 1",
          "end"),
        0);

    final SMFHeader.Builder header_b = SMFHeader.builder();
    final SMFAttribute attribute =
      SMFAttribute.of(
        SMFAttributeName.of("x"), ELEMENT_TYPE_INTEGER_UNSIGNED, 2, 32);
    header_b.addAttributesInOrder(attribute);
    header_b.setVertexCount(1L);
    final SMFHeader header = header_b.build();

    final SMFTV1BodySectionParserVerticesNonInterleaved cmd =
      new SMFTV1BodySectionParserVerticesNonInterleaved(() -> header, reader,
                                                        state);

    new Expectations()
    {{
      events.onAttributesNonInterleaved();
      this.result = Optional.of(events_data);

      events_data.onDataAttributeStart(attribute);
      this.result = Optional.of(events_values);

      events_values.onDataAttributeValueIntegerUnsigned2(0, 1);
      events_values.onDataAttributeValueFinish();

      events_data.onDataAttributesNonInterleavedFinish();
    }};

    final SMFTParsingStatus r =
      cmd.parse(events, List.of("vertices", "noninterleaved"));
    Assertions.assertEquals(SUCCESS, r);
  }

  @Test
  public void testAttributeIntegerUnsigned1(
    final @Mocked SMFParserEventsDataAttributeValuesType events_values,
    final @Mocked SMFParserEventsDataAttributesNonInterleavedType events_data,
    final @Mocked SMFParserEventsBodyType events)
    throws Exception
  {
    final BitSet state = new BitSet();

    final SMFTLineReaderType reader =
      SMFTLineReaderList.create(
        URI.create("urn:x"),
        List.of(
          "attribute x",
          "0",
          "end"),
        0);

    final SMFHeader.Builder header_b = SMFHeader.builder();
    final SMFAttribute attribute =
      SMFAttribute.of(
        SMFAttributeName.of("x"), ELEMENT_TYPE_INTEGER_UNSIGNED, 1, 32);
    header_b.addAttributesInOrder(attribute);
    header_b.setVertexCount(1L);
    final SMFHeader header = header_b.build();

    final SMFTV1BodySectionParserVerticesNonInterleaved cmd =
      new SMFTV1BodySectionParserVerticesNonInterleaved(() -> header, reader,
                                                        state);

    new Expectations()
    {{
      events.onAttributesNonInterleaved();
      this.result = Optional.of(events_data);

      events_data.onDataAttributeStart(attribute);
      this.result = Optional.of(events_values);

      events_values.onDataAttributeValueIntegerUnsigned1(0);
      events_values.onDataAttributeValueFinish();

      events_data.onDataAttributesNonInterleavedFinish();
    }};

    final SMFTParsingStatus r =
      cmd.parse(events, List.of("vertices", "noninterleaved"));
    Assertions.assertEquals(SUCCESS, r);
  }

  @Test
  public void testAttributeBadIntegerUnsigned4(
    final @Mocked SMFParserEventsDataAttributeValuesType events_values,
    final @Mocked SMFParserEventsDataAttributesNonInterleavedType events_data,
    final @Mocked SMFParserEventsBodyType events)
    throws Exception
  {
    final BitSet state = new BitSet();

    final SMFTLineReaderType reader =
      SMFTLineReaderList.create(
        URI.create("urn:x"),
        List.of(
          "attribute x",
          "z 1 2 3",
          "end"),
        0);

    final SMFHeader.Builder header_b = SMFHeader.builder();
    final SMFAttribute attribute =
      SMFAttribute.of(
        SMFAttributeName.of("x"), ELEMENT_TYPE_INTEGER_UNSIGNED, 4, 32);
    header_b.addAttributesInOrder(attribute);
    header_b.setVertexCount(1L);
    final SMFHeader header = header_b.build();

    final SMFTV1BodySectionParserVerticesNonInterleaved cmd =
      new SMFTV1BodySectionParserVerticesNonInterleaved(() -> header, reader,
                                                        state);

    new Expectations()
    {{
      events.onAttributesNonInterleaved();
      this.result = Optional.of(events_data);

      events_data.onDataAttributeStart(attribute);
      this.result = Optional.of(events_values);

      events_values.onError(this.with(new Delegate<SMFErrorType>()
      {
        boolean check(final SMFErrorType e)
        {
          return e.message().contains("Cannot parse");
        }
      }));
      events_values.onDataAttributeValueFinish();

      events_data.onDataAttributesNonInterleavedFinish();
    }};

    final SMFTParsingStatus r =
      cmd.parse(events, List.of("vertices", "noninterleaved"));
    Assertions.assertEquals(FAILURE, r);
  }

  @Test
  public void testAttributeBadIntegerUnsigned3(
    final @Mocked SMFParserEventsDataAttributeValuesType events_values,
    final @Mocked SMFParserEventsDataAttributesNonInterleavedType events_data,
    final @Mocked SMFParserEventsBodyType events)
    throws Exception
  {
    final BitSet state = new BitSet();

    final SMFTLineReaderType reader =
      SMFTLineReaderList.create(
        URI.create("urn:x"),
        List.of(
          "attribute x",
          "z 1 2",
          "end"),
        0);

    final SMFHeader.Builder header_b = SMFHeader.builder();
    final SMFAttribute attribute =
      SMFAttribute.of(
        SMFAttributeName.of("x"), ELEMENT_TYPE_INTEGER_UNSIGNED, 3, 32);
    header_b.addAttributesInOrder(attribute);
    header_b.setVertexCount(1L);
    final SMFHeader header = header_b.build();

    final SMFTV1BodySectionParserVerticesNonInterleaved cmd =
      new SMFTV1BodySectionParserVerticesNonInterleaved(() -> header, reader,
                                                        state);

    new Expectations()
    {{
      events.onAttributesNonInterleaved();
      this.result = Optional.of(events_data);

      events_data.onDataAttributeStart(attribute);
      this.result = Optional.of(events_values);

      events_values.onError(this.with(new Delegate<SMFErrorType>()
      {
        boolean check(final SMFErrorType e)
        {
          return e.message().contains("Cannot parse");
        }
      }));
      events_values.onDataAttributeValueFinish();

      events_data.onDataAttributesNonInterleavedFinish();
    }};

    final SMFTParsingStatus r =
      cmd.parse(events, List.of("vertices", "noninterleaved"));
    Assertions.assertEquals(FAILURE, r);
  }

  @Test
  public void testAttributeBadIntegerUnsigned2(
    final @Mocked SMFParserEventsDataAttributeValuesType events_values,
    final @Mocked SMFParserEventsDataAttributesNonInterleavedType events_data,
    final @Mocked SMFParserEventsBodyType events)
    throws Exception
  {
    final BitSet state = new BitSet();

    final SMFTLineReaderType reader =
      SMFTLineReaderList.create(
        URI.create("urn:x"),
        List.of(
          "attribute x",
          "z 1",
          "end"),
        0);

    final SMFHeader.Builder header_b = SMFHeader.builder();
    final SMFAttribute attribute =
      SMFAttribute.of(
        SMFAttributeName.of("x"), ELEMENT_TYPE_INTEGER_UNSIGNED, 2, 32);
    header_b.addAttributesInOrder(attribute);
    header_b.setVertexCount(1L);
    final SMFHeader header = header_b.build();

    final SMFTV1BodySectionParserVerticesNonInterleaved cmd =
      new SMFTV1BodySectionParserVerticesNonInterleaved(() -> header, reader,
                                                        state);

    new Expectations()
    {{
      events.onAttributesNonInterleaved();
      this.result = Optional.of(events_data);

      events_data.onDataAttributeStart(attribute);
      this.result = Optional.of(events_values);

      events_values.onError(this.with(new Delegate<SMFErrorType>()
      {
        boolean check(final SMFErrorType e)
        {
          return e.message().contains("Cannot parse");
        }
      }));
      events_values.onDataAttributeValueFinish();

      events_data.onDataAttributesNonInterleavedFinish();
    }};

    final SMFTParsingStatus r =
      cmd.parse(events, List.of("vertices", "noninterleaved"));
    Assertions.assertEquals(FAILURE, r);
  }

  @Test
  public void testAttributeBadIntegerUnsigned1(
    final @Mocked SMFParserEventsDataAttributeValuesType events_values,
    final @Mocked SMFParserEventsDataAttributesNonInterleavedType events_data,
    final @Mocked SMFParserEventsBodyType events)
    throws Exception
  {
    final BitSet state = new BitSet();

    final SMFTLineReaderType reader =
      SMFTLineReaderList.create(
        URI.create("urn:x"),
        List.of(
          "attribute x",
          "z",
          "end"),
        0);

    final SMFHeader.Builder header_b = SMFHeader.builder();
    final SMFAttribute attribute =
      SMFAttribute.of(
        SMFAttributeName.of("x"), ELEMENT_TYPE_INTEGER_UNSIGNED, 1, 32);
    header_b.addAttributesInOrder(attribute);
    header_b.setVertexCount(1L);
    final SMFHeader header = header_b.build();

    final SMFTV1BodySectionParserVerticesNonInterleaved cmd =
      new SMFTV1BodySectionParserVerticesNonInterleaved(() -> header, reader,
                                                        state);

    new Expectations()
    {{
      events.onAttributesNonInterleaved();
      this.result = Optional.of(events_data);

      events_data.onDataAttributeStart(attribute);
      this.result = Optional.of(events_values);

      events_values.onError(this.with(new Delegate<SMFErrorType>()
      {
        boolean check(final SMFErrorType e)
        {
          return e.message().contains("Cannot parse");
        }
      }));
      events_values.onDataAttributeValueFinish();

      events_data.onDataAttributesNonInterleavedFinish();
    }};

    final SMFTParsingStatus r =
      cmd.parse(events, List.of("vertices", "noninterleaved"));
    Assertions.assertEquals(FAILURE, r);
  }
}
