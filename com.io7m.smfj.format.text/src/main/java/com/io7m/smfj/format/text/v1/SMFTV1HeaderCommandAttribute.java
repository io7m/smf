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

import java.util.Objects;
import com.io7m.smfj.core.SMFAttribute;
import com.io7m.smfj.core.SMFAttributeName;
import com.io7m.smfj.core.SMFComponentType;
import com.io7m.smfj.core.SMFHeader;
import com.io7m.smfj.format.text.SMFTHeaderCommandParserType;
import com.io7m.smfj.format.text.SMFTLineReaderType;
import com.io7m.smfj.format.text.SMFTParsingStatus;
import com.io7m.smfj.parser.api.SMFParserEventsHeaderType;
import javaslang.collection.List;

import java.io.IOException;
import java.util.Collection;
import java.util.Map;

import static com.io7m.smfj.format.text.SMFTParsingStatus.FAILURE;
import static com.io7m.smfj.format.text.SMFTParsingStatus.SUCCESS;
import static com.io7m.smfj.format.text.v1.SMFTErrors.errorCommandExpectedGotWithException;
import static com.io7m.smfj.format.text.v1.SMFTErrors.errorMalformedCommand;

/**
 * A parser for the "attribute" command.
 */

public final class SMFTV1HeaderCommandAttribute
  implements SMFTHeaderCommandParserType
{
  /**
   * The command syntax.
   */

  public static final String SYNTAX =
    "attribute <attribute-name> <component-type> <component-count> <component-size>";

  private final SMFTLineReaderType reader;
  private final SMFHeader.Builder header;
  private final Map<SMFAttributeName, Integer> attributes_lines;
  private final Collection<SMFAttribute> attributes_list;

  /**
   * Construct a parser.
   *
   * @param in_attributes_lines A map that keeps track of the line numbers of
   *                            other attribute commands
   * @param in_attributes_list  The current attributes in declaration order
   * @param in_reader           A line reader
   * @param in_header           A header builder
   */

  public SMFTV1HeaderCommandAttribute(
    final Map<SMFAttributeName, Integer> in_attributes_lines,
    final Collection<SMFAttribute> in_attributes_list,
    final SMFTLineReaderType in_reader,
    final SMFHeader.Builder in_header)
  {
    this.attributes_lines =
      Objects.requireNonNull(in_attributes_lines, "Attribute Lines");
    this.attributes_list =
      Objects.requireNonNull(in_attributes_list, "Attributes List");
    this.reader =
      Objects.requireNonNull(in_reader, "Reader");
    this.header =
      Objects.requireNonNull(in_header, "Header");
  }

  @Override
  public String name()
  {
    return "attribute";
  }

  @Override
  public SMFTParsingStatus parse(
    final SMFParserEventsHeaderType receiver,
    final List<String> line)
    throws IOException
  {
    if (line.length() == 5) {
      try {
        final SMFAttributeName name =
          SMFAttributeName.of(line.get(1));
        final SMFComponentType type =
          SMFComponentType.of(line.get(2));
        final int count =
          Integer.parseUnsignedInt(line.get(3));
        final int size =
          Integer.parseUnsignedInt(line.get(4));
        final SMFAttribute attr =
          SMFAttribute.of(name, type, count, size);

        this.attributes_lines.put(
          name, Integer.valueOf(this.reader.position().line()));
        this.attributes_list.add(attr);

        this.header.addAttributesInOrder(attr);
        return SUCCESS;
      } catch (final IllegalArgumentException e) {
        receiver.onError(errorCommandExpectedGotWithException(
          "attribute",
          SYNTAX,
          line,
          this.reader.position(),
          e));
        return FAILURE;
      }
    }

    receiver.onError(errorMalformedCommand(
      "attribute",
      SYNTAX,
      line,
      this.reader.position()));
    return FAILURE;
  }
}
