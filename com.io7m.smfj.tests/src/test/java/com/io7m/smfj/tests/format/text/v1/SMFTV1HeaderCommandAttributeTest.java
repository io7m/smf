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
import com.io7m.smfj.format.text.SMFTLineReaderType;
import com.io7m.smfj.format.text.SMFTParsingStatus;
import com.io7m.smfj.format.text.v1.SMFTV1HeaderCommandAttribute;
import com.io7m.smfj.parser.api.SMFParserEventsHeaderType;
import io.vavr.collection.List;
import mockit.Delegate;
import mockit.Mocked;
import mockit.StrictExpectations;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import static com.io7m.smfj.format.text.SMFTParsingStatus.FAILURE;
import static com.io7m.smfj.format.text.SMFTParsingStatus.SUCCESS;

public final class SMFTV1HeaderCommandAttributeTest
{
  @Test
  public void testOK_0(
    final @Mocked SMFParserEventsHeaderType events,
    final @Mocked SMFTLineReaderType reader)
    throws Exception
  {
    final SMFHeader.Builder header = SMFHeader.builder();
    final Map<SMFAttributeName, Integer> attributes_lines = new HashMap<>();
    final Collection<SMFAttribute> attributes_list = new ArrayList<>();
    final SMFTV1HeaderCommandAttribute cmd =
      new SMFTV1HeaderCommandAttribute(
        attributes_lines, attributes_list, reader, header);

    new StrictExpectations()
    {{

    }};

    final SMFTParsingStatus r =
      cmd.parse(events, List.of("attribute", "position", "float", "3", "32"));
    Assert.assertEquals(SUCCESS, r);

    final SMFHeader result = header.build();

    final SMFAttribute attr = result.attributesInOrder().get(0);
    Assert.assertEquals("position", attr.name().value());
    Assert.assertEquals("float", attr.componentType().getName());
    Assert.assertEquals(3L, (long) attr.componentCount());
    Assert.assertEquals(32L, (long) attr.componentSizeBits());
  }

  @Test
  public void testFailure_0(
    final @Mocked SMFParserEventsHeaderType events,
    final @Mocked SMFTLineReaderType reader)
    throws Exception
  {
    final SMFHeader.Builder header = SMFHeader.builder();
    final Map<SMFAttributeName, Integer> attributes_lines = new HashMap<>();
    final Collection<SMFAttribute> attributes_list = new ArrayList<>();
    final SMFTV1HeaderCommandAttribute cmd =
      new SMFTV1HeaderCommandAttribute(
        attributes_lines, attributes_list, reader, header);

    new StrictExpectations()
    {{
      events.onError(this.with(new Delegate<SMFErrorType>()
      {
        boolean check(final SMFErrorType e)
        {
          return e.message().contains(SMFTV1HeaderCommandAttribute.SYNTAX);
        }
      }));
    }};

    final SMFTParsingStatus r =
      cmd.parse(events, List.of("attribute"));
    Assert.assertEquals(FAILURE, r);

    final SMFHeader result = header.build();
    Assert.assertEquals(0L, result.vertexCount());
  }

  @Test
  public void testFailure_1(
    final @Mocked SMFParserEventsHeaderType events,
    final @Mocked SMFTLineReaderType reader)
    throws Exception
  {
    final SMFHeader.Builder header = SMFHeader.builder();
    final Map<SMFAttributeName, Integer> attributes_lines = new HashMap<>();
    final Collection<SMFAttribute> attributes_list = new ArrayList<>();
    final SMFTV1HeaderCommandAttribute cmd =
      new SMFTV1HeaderCommandAttribute(
        attributes_lines, attributes_list, reader, header);

    new StrictExpectations()
    {{
      events.onError(this.with(new Delegate<SMFErrorType>()
      {
        boolean check(final SMFErrorType e)
        {
          return e.message().contains(SMFTV1HeaderCommandAttribute.SYNTAX);
        }
      }));
    }};

    final SMFTParsingStatus r =
      cmd.parse(events, List.of("attribute", "0", "1", "2", "3"));
    Assert.assertEquals(FAILURE, r);

    final SMFHeader result = header.build();
    Assert.assertEquals(0L, result.vertexCount());
  }
}
