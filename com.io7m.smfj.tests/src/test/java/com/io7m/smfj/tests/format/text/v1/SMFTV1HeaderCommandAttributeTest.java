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

import com.io7m.jlexing.core.LexicalPosition;
import com.io7m.smfj.core.SMFAttribute;
import com.io7m.smfj.core.SMFAttributeName;
import com.io7m.smfj.core.SMFErrorType;
import com.io7m.smfj.core.SMFHeader;
import com.io7m.smfj.format.text.SMFTLineReaderType;
import com.io7m.smfj.format.text.SMFTParsingStatus;
import com.io7m.smfj.format.text.v1.SMFTV1HeaderCommandAttribute;
import com.io7m.smfj.parser.api.SMFParserEventsHeaderType;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import static com.io7m.smfj.format.text.SMFTParsingStatus.FAILURE;
import static com.io7m.smfj.format.text.SMFTParsingStatus.SUCCESS;

public final class SMFTV1HeaderCommandAttributeTest
{
  private SMFParserEventsHeaderType events;
  private SMFTLineReaderType reader;
  private ArgumentCaptor<SMFErrorType> captor;

  @BeforeEach
  public void testSetup()
  {
    this.events = Mockito.mock(SMFParserEventsHeaderType.class);
    this.reader = Mockito.mock(SMFTLineReaderType.class);
    this.captor = ArgumentCaptor.forClass(SMFErrorType.class);

    Mockito.when(this.reader.position())
      .thenReturn(LexicalPosition.of(0, 0, Optional.empty()));
  }

  @Test
  public void testOK_0()
    throws Exception
  {
    final SMFHeader.Builder header = SMFHeader.builder();
    final Map<SMFAttributeName, Integer> attributes_lines = new HashMap<>();
    final Collection<SMFAttribute> attributes_list = new ArrayList<>();
    final SMFTV1HeaderCommandAttribute cmd =
      new SMFTV1HeaderCommandAttribute(
        attributes_lines, attributes_list, this.reader, header);

    final SMFTParsingStatus r =
      cmd.parse(this.events, List.of("attribute", "position", "float", "3", "32"));
    Assertions.assertEquals(SUCCESS, r);

    final SMFHeader result = header.build();

    final SMFAttribute attr = result.attributesInOrder().get(0);
    Assertions.assertEquals("position", attr.name().value());
    Assertions.assertEquals("float", attr.componentType().getName());
    Assertions.assertEquals(3L, (long) attr.componentCount());
    Assertions.assertEquals(32L, (long) attr.componentSizeBits());
  }

  @Test
  public void testFailure_0()
    throws Exception
  {
    final SMFHeader.Builder header = SMFHeader.builder();
    final Map<SMFAttributeName, Integer> attributes_lines = new HashMap<>();
    final Collection<SMFAttribute> attributes_list = new ArrayList<>();
    final SMFTV1HeaderCommandAttribute cmd =
      new SMFTV1HeaderCommandAttribute(
        attributes_lines, attributes_list, this.reader, header);

    final SMFTParsingStatus r =
      cmd.parse(this.events, List.of("attribute"));
    Assertions.assertEquals(FAILURE, r);

    final SMFHeader result = header.build();
    Assertions.assertEquals(0L, result.vertexCount());

    Mockito.verify(this.events).onError(this.captor.capture());
    Assertions.assertTrue(this.captor.getValue().message().contains(
      SMFTV1HeaderCommandAttribute.SYNTAX));
  }

  @Test
  public void testFailure_1()
    throws Exception
  {
    final SMFHeader.Builder header = SMFHeader.builder();
    final Map<SMFAttributeName, Integer> attributes_lines = new HashMap<>();
    final Collection<SMFAttribute> attributes_list = new ArrayList<>();
    final SMFTV1HeaderCommandAttribute cmd =
      new SMFTV1HeaderCommandAttribute(
        attributes_lines, attributes_list, this.reader, header);

    final SMFTParsingStatus r =
      cmd.parse(this.events, List.of("attribute", "0", "1", "2", "3"));
    Assertions.assertEquals(FAILURE, r);

    final SMFHeader result = header.build();
    Assertions.assertEquals(0L, result.vertexCount());

    Mockito.verify(this.events).onError(this.captor.capture());
    Assertions.assertTrue(this.captor.getValue().message().contains(
      SMFTV1HeaderCommandAttribute.SYNTAX));
  }
}
