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

import com.io7m.junreachable.UnimplementedCodeException;
import com.io7m.junreachable.UnreachableCodeException;
import com.io7m.smfj.core.SMFAttribute;
import com.io7m.smfj.core.SMFAttributeName;
import com.io7m.smfj.core.SMFErrorType;
import com.io7m.smfj.core.SMFHeader;
import com.io7m.smfj.core.SMFWarningType;
import com.io7m.smfj.format.text.SMFTBodySectionParserType;
import com.io7m.smfj.format.text.SMFTLineReaderType;
import com.io7m.smfj.format.text.SMFTParsingStatus;
import com.io7m.smfj.format.text.implementation.Flags;
import com.io7m.smfj.parser.api.SMFParseError;
import com.io7m.smfj.parser.api.SMFParserEventsBodyType;
import com.io7m.smfj.parser.api.SMFParserEventsDataAttributeValuesIgnoringReceiver;
import com.io7m.smfj.parser.api.SMFParserEventsDataAttributeValuesType;
import com.io7m.smfj.parser.api.SMFParserEventsDataAttributesNonInterleavedType;
import io.vavr.collection.List;
import io.vavr.collection.SortedMap;
import io.vavr.collection.SortedSet;

import java.io.IOException;
import java.util.BitSet;
import java.util.HashSet;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static com.io7m.smfj.format.text.SMFTParsingStatus.FAILURE;
import static com.io7m.smfj.format.text.SMFTParsingStatus.SUCCESS;
import static com.io7m.smfj.format.text.v1.SMFTErrors.errorMalformedCommand;
import static com.io7m.smfj.parser.api.SMFParseErrors.errorExpectedGot;

/**
 * A parser for the "vertices" body section.
 */

public final class SMFTV1BodySectionParserVerticesNonInterleaved implements
  SMFTBodySectionParserType
{
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

  public SMFTV1BodySectionParserVerticesNonInterleaved(
    final Supplier<SMFHeader> in_header_get,
    final SMFTLineReaderType in_reader,
    final BitSet in_state)
  {
    this.header_get = Objects.requireNonNull(in_header_get, "Header");
    this.reader = Objects.requireNonNull(in_reader, "Reader");
    this.state = Objects.requireNonNull(in_state, "State");
  }

  private static String remainingAttributes(
    final SortedSet<SMFAttributeName> all_attributes,
    final Set<String> specified_attributes)
  {
    return all_attributes
      .map(SMFAttributeName::value)
      .filter(a -> !specified_attributes.contains(a))
      .collect(Collectors.joining(" "));
  }

  private static SMFParserEventsDataAttributeValuesType makeValueReceiver(
    final SMFParserEventsDataAttributesNonInterleavedType data_receiver,
    final SMFAttribute attr)
  {
    return data_receiver.onDataAttributeStart(attr).orElseGet(
      () -> new SMFParserEventsDataAttributeValuesIgnoringReceiver(data_receiver));
  }

  private static SMFParserEventsDataAttributesNonInterleavedType makeDataReceiver(
    final SMFParserEventsBodyType receiver,
    final Optional<SMFParserEventsDataAttributesNonInterleavedType> data_receiver_opt)
  {
    return data_receiver_opt.orElseGet(() -> new IgnoringDataReceiver(receiver));
  }

  private static String knownAttributes(
    final SortedSet<SMFAttributeName> names)
  {
    return names
      .map(SMFAttributeName::value)
      .collect(Collectors.joining(" "));
  }

  @Override
  public String name()
  {
    return "vertices-noninterleaved";
  }

  @Override
  public SMFTParsingStatus parse(
    final SMFParserEventsBodyType receiver,
    final List<String> line_start)
    throws IOException
  {
    final HashSet<String> attributes_done = new HashSet<>();

    final SMFParserEventsDataAttributesNonInterleavedType data_receiver =
      makeDataReceiver(receiver, receiver.onAttributesNonInterleaved());

    try {
      final SMFHeader header = this.header_get.get();

      boolean vertices_done = false;
      while (!vertices_done) {
        final Optional<List<String>> line_opt = this.reader.line();
        if (!line_opt.isPresent()) {
          data_receiver.onError(SMFParseError.of(
            this.reader.position(),
            "Unexpected EOF",
            Optional.empty()));
          return FAILURE;
        }

        final List<String> line = line_opt.get();
        if (line.isEmpty()) {
          continue;
        }

        final String command_name = line.get(0);
        switch (command_name) {
          case "attribute": {
            switch (this.parseAttribute(
              header, attributes_done, data_receiver, line)) {
              case SUCCESS:
                break;
              case FAILURE:
                return FAILURE;
            }
            break;
          }
          case "end": {
            vertices_done = true;
            break;
          }
          default: {
            throw new UnimplementedCodeException();
          }
        }
      }

      if (attributes_done.size() != header.attributesByName().size()) {
        header.attributesByName().keySet().forEach(
          name -> data_receiver.onError(SMFParseError.of(
            this.reader.position(),
            String.format("No data specified for attribute '%s'", name.value()),
            Optional.empty()
          )));
        return FAILURE;
      }

      this.state.set(Flags.VERTICES_RECEIVED, true);
      return SUCCESS;
    } finally {
      data_receiver.onDataAttributesNonInterleavedFinish();
    }
  }

  private SMFTParsingStatus parseAttribute(
    final SMFHeader header,
    final Set<String> attributes,
    final SMFParserEventsDataAttributesNonInterleavedType receiver,
    final List<String> line)
    throws IOException
  {
    if (line.length() == 2) {
      final SMFAttributeName name = SMFAttributeName.of(line.get(1));
      final SortedMap<SMFAttributeName, SMFAttribute> by_name =
        header.attributesByName();

      if (!by_name.containsKey(name)) {
        receiver.onError(errorExpectedGot(
          "Unknown attribute.",
          "One of " + knownAttributes(by_name.keySet()),
          line.collect(Collectors.joining(" ")),
          this.reader.position()
        ));
        return FAILURE;
      }

      if (attributes.contains(name.value())) {
        receiver.onError(errorExpectedGot(
          "Attribute already specified.",
          "One of " + remainingAttributes(by_name.keySet(), attributes),
          line.collect(Collectors.joining(" ")),
          this.reader.position()
        ));
        return FAILURE;
      }

      final SMFAttribute attr = by_name.get(name).get();
      attributes.add(name.value());

      final SMFParserEventsDataAttributeValuesType new_receiver =
        makeValueReceiver(receiver, attr);
      return this.parseAttributeNonInterleavedValues(
        header, new_receiver, attr);
    }

    receiver.onError(errorMalformedCommand(
      "attribute", "attribute <name>", line, this.reader.position()));
    return FAILURE;
  }

  private SMFTParsingStatus parseAttributeNonInterleavedValues(
    final SMFHeader header,
    final SMFParserEventsDataAttributeValuesType receiver,
    final SMFAttribute attribute)
    throws IOException
  {
    try {
      long vertices_remaining = header.vertexCount();
      while (vertices_remaining > 0L) {
        final Optional<List<String>> line_opt = this.reader.line();
        if (!line_opt.isPresent()) {
          receiver.onError(SMFParseError.of(
            this.reader.position(),
            "Unexpected EOF",
            Optional.empty()));
          return FAILURE;
        }

        final List<String> line = line_opt.get();
        if (line.isEmpty()) {
          continue;
        }

        switch (this.parseAttributeElement(receiver, attribute, line)) {
          case SUCCESS:
            vertices_remaining = Math.subtractExact(vertices_remaining, 1L);
            continue;
          case FAILURE:
            return FAILURE;
        }
      }

      return SUCCESS;
    } finally {
      receiver.onDataAttributeValueFinish();
    }
  }

  private SMFTParsingStatus parseAttributeElement(
    final SMFParserEventsDataAttributeValuesType receiver,
    final SMFAttribute attribute,
    final List<String> line)
  {
    switch (attribute.componentType()) {
      case ELEMENT_TYPE_INTEGER_SIGNED: {
        return this.parseAttributeElementIntegerSigned(receiver, attribute, line);
      }

      case ELEMENT_TYPE_INTEGER_UNSIGNED: {
        return this.parseAttributeElementIntegerUnsigned(receiver, attribute, line);
      }

      case ELEMENT_TYPE_FLOATING: {
        return this.parseAttributeElementFloating(receiver, attribute, line);
      }
    }

    throw new UnreachableCodeException();
  }

  private SMFTParsingStatus parseAttributeElementFloating(
    final SMFParserEventsDataAttributeValuesType receiver,
    final SMFAttribute attribute,
    final List<String> line)
  {
    switch (attribute.componentCount()) {
      case 1: {
        return this.parseAttributeElementFloat1(receiver, line);
      }

      case 2: {
        return this.parseAttributeElementFloat2(receiver, line);
      }

      case 3: {
        return this.parseAttributeElementFloat3(receiver, line);
      }

      case 4: {
        return this.parseAttributeElementFloat4(receiver, line);
      }

      default: {
        throw new UnreachableCodeException();
      }
    }
  }

  private SMFTParsingStatus parseAttributeElementIntegerUnsigned(
    final SMFParserEventsDataAttributeValuesType receiver,
    final SMFAttribute attribute,
    final List<String> line)
  {
    switch (attribute.componentCount()) {
      case 1: {
        return this.parseAttributeElementUnsigned1(receiver, line);
      }

      case 2: {
        return this.parseAttributeElementUnsigned2(receiver, line);
      }

      case 3: {
        return this.parseAttributeElementUnsigned3(receiver, line);
      }

      case 4: {
        return this.parseAttributeElementUnsigned4(receiver, line);
      }

      default: {
        throw new UnreachableCodeException();
      }
    }
  }

  private SMFTParsingStatus parseAttributeElementIntegerSigned(
    final SMFParserEventsDataAttributeValuesType receiver,
    final SMFAttribute attribute,
    final List<String> line)
  {
    switch (attribute.componentCount()) {
      case 1: {
        return this.parseAttributeElementSigned1(receiver, line);
      }

      case 2: {
        return this.parseAttributeElementSigned2(receiver, line);
      }

      case 3: {
        return this.parseAttributeElementSigned3(receiver, line);
      }

      case 4: {
        return this.parseAttributeElementSigned4(receiver, line);
      }

      default: {
        throw new UnreachableCodeException();
      }
    }
  }

  private SMFTParsingStatus parseAttributeElementUnsigned4(
    final SMFParserEventsDataAttributeValuesType receiver,
    final List<String> line)
  {
    if (line.length() == 4) {
      try {
        final long x = Long.parseUnsignedLong(line.get(0));
        final long y = Long.parseUnsignedLong(line.get(1));
        final long z = Long.parseUnsignedLong(line.get(2));
        final long w = Long.parseUnsignedLong(line.get(3));
        receiver.onDataAttributeValueIntegerUnsigned4(x, y, z, w);
        return SUCCESS;
      } catch (final NumberFormatException e) {
        receiver.onError(SMFTErrors.errorExpectedGotWithException(
          "Cannot parse four element vector: " + e.getMessage(),
          "<integer-unsigned> <integer-unsigned> <integer-unsigned> <integer-unsigned>",
          line,
          this.reader.position(),
          e));
        return FAILURE;
      }
    }
    receiver.onError(SMFTErrors.errorExpectedGot(
      "Cannot parse four element vector.",
      "<integer-unsigned> <integer-unsigned> <integer-unsigned> <integer-unsigned>",
      line,
      this.reader.position()));
    return FAILURE;
  }

  private SMFTParsingStatus parseAttributeElementUnsigned3(
    final SMFParserEventsDataAttributeValuesType receiver,
    final List<String> line)
  {
    if (line.length() == 3) {
      try {
        final long x = Long.parseUnsignedLong(line.get(0));
        final long y = Long.parseUnsignedLong(line.get(1));
        final long z = Long.parseUnsignedLong(line.get(2));
        receiver.onDataAttributeValueIntegerUnsigned3(x, y, z);
        return SUCCESS;
      } catch (final NumberFormatException e) {
        receiver.onError(SMFTErrors.errorExpectedGotWithException(
          "Cannot parse three element vector: " + e.getMessage(),
          SMFTV1BodySectionParserTriangles.SYNTAX,
          line,
          this.reader.position(),
          e));
        return FAILURE;
      }
    }
    receiver.onError(SMFTErrors.errorExpectedGot(
      "Cannot parse three element vector.",
      SMFTV1BodySectionParserTriangles.SYNTAX,
      line,
      this.reader.position()));
    return FAILURE;
  }

  private SMFTParsingStatus parseAttributeElementUnsigned2(
    final SMFParserEventsDataAttributeValuesType receiver,
    final List<String> line)
  {
    if (line.length() == 2) {
      try {
        final long x = Long.parseUnsignedLong(line.get(0));
        final long y = Long.parseUnsignedLong(line.get(1));
        receiver.onDataAttributeValueIntegerUnsigned2(x, y);
        return SUCCESS;
      } catch (final NumberFormatException e) {
        receiver.onError(SMFTErrors.errorExpectedGotWithException(
          "Cannot parse two element vector: " + e.getMessage(),
          "<integer-unsigned> <integer-unsigned>",
          line,
          this.reader.position(),
          e));
        return FAILURE;
      }
    }
    receiver.onError(SMFTErrors.errorExpectedGot(
      "Cannot parse two element vector.",
      "<integer-unsigned> <integer-unsigned>",
      line,
      this.reader.position()));
    return FAILURE;
  }

  private SMFTParsingStatus parseAttributeElementUnsigned1(
    final SMFParserEventsDataAttributeValuesType receiver,
    final List<String> line)
  {
    if (line.length() == 1) {
      try {
        final long x = Long.parseUnsignedLong(line.get(0));
        receiver.onDataAttributeValueIntegerUnsigned1(x);
        return SUCCESS;
      } catch (final NumberFormatException e) {
        receiver.onError(SMFTErrors.errorExpectedGotWithException(
          "Cannot parse unsigned integer: " + e.getMessage(),
          "<integer-unsigned>",
          line,
          this.reader.position(),
          e));
        return FAILURE;
      }
    }
    receiver.onError(SMFTErrors.errorExpectedGot(
      "Cannot parse unsigned integer.",
      "<integer-unsigned>",
      line,
      this.reader.position()));
    return FAILURE;
  }

  private SMFTParsingStatus parseAttributeElementSigned4(
    final SMFParserEventsDataAttributeValuesType receiver,
    final List<String> line)
  {
    if (line.length() == 4) {
      try {
        final long x = Long.parseLong(line.get(0));
        final long y = Long.parseLong(line.get(1));
        final long z = Long.parseLong(line.get(2));
        final long w = Long.parseLong(line.get(3));
        receiver.onDataAttributeValueIntegerSigned4(x, y, z, w);
        return SUCCESS;
      } catch (final NumberFormatException e) {
        receiver.onError(SMFTErrors.errorExpectedGotWithException(
          "Cannot parse four element vector: " + e.getMessage(),
          "<integer-signed> <integer-signed> <integer-signed> <integer-signed>",
          line,
          this.reader.position(),
          e));
        return FAILURE;
      }
    }
    receiver.onError(SMFTErrors.errorExpectedGot(
      "Cannot parse four element vector.",
      "<integer-signed> <integer-signed> <integer-signed> <integer-signed>",
      line,
      this.reader.position()));
    return FAILURE;
  }

  private SMFTParsingStatus parseAttributeElementSigned3(
    final SMFParserEventsDataAttributeValuesType receiver,
    final List<String> line)
  {
    if (line.length() == 3) {
      try {
        final long x = Long.parseLong(line.get(0));
        final long y = Long.parseLong(line.get(1));
        final long z = Long.parseLong(line.get(2));
        receiver.onDataAttributeValueIntegerSigned3(x, y, z);
        return SUCCESS;
      } catch (final NumberFormatException e) {
        receiver.onError(SMFTErrors.errorExpectedGotWithException(
          "Cannot parse three element vector: " + e.getMessage(),
          "<integer-signed> <integer-signed> <integer-signed>",
          line,
          this.reader.position(),
          e));
        return FAILURE;
      }
    }
    receiver.onError(SMFTErrors.errorExpectedGot(
      "Cannot parse three element vector.",
      "<integer-signed> <integer-signed> <integer-signed>",
      line,
      this.reader.position()));
    return FAILURE;
  }

  private SMFTParsingStatus parseAttributeElementSigned2(
    final SMFParserEventsDataAttributeValuesType receiver,
    final List<String> line)
  {
    if (line.length() == 2) {
      try {
        final long x = Long.parseLong(line.get(0));
        final long y = Long.parseLong(line.get(1));
        receiver.onDataAttributeValueIntegerSigned2(x, y);
        return SUCCESS;
      } catch (final NumberFormatException e) {
        receiver.onError(SMFTErrors.errorExpectedGotWithException(
          "Cannot parse two element vector: " + e.getMessage(),
          "<integer-signed> <integer-signed>",
          line,
          this.reader.position(),
          e));
        return FAILURE;
      }
    }
    receiver.onError(SMFTErrors.errorExpectedGot(
      "Cannot parse two element vector.",
      "<integer-signed> <integer-signed>",
      line,
      this.reader.position()));
    return FAILURE;
  }

  private SMFTParsingStatus parseAttributeElementSigned1(
    final SMFParserEventsDataAttributeValuesType receiver,
    final List<String> line)
  {
    if (line.length() == 1) {
      try {
        final long x = Long.parseLong(line.get(0));
        receiver.onDataAttributeValueIntegerSigned1(x);
        return SUCCESS;
      } catch (final NumberFormatException e) {
        receiver.onError(SMFTErrors.errorExpectedGotWithException(
          "Cannot parse signed integer: " + e.getMessage(),
          "<integer-signed>",
          line,
          this.reader.position(),
          e));
        return FAILURE;
      }
    }
    receiver.onError(SMFTErrors.errorExpectedGot(
      "Cannot parse signed integer.",
      "<integer-signed>",
      line,
      this.reader.position()));
    return FAILURE;
  }

  private SMFTParsingStatus parseAttributeElementFloat4(
    final SMFParserEventsDataAttributeValuesType receiver,
    final List<String> line)
  {
    if (line.length() == 4) {
      try {
        final double x = Double.parseDouble(line.get(0));
        final double y = Double.parseDouble(line.get(1));
        final double z = Double.parseDouble(line.get(2));
        final double w = Double.parseDouble(line.get(3));
        receiver.onDataAttributeValueFloat4(x, y, z, w);
        return SUCCESS;
      } catch (final NumberFormatException e) {
        receiver.onError(SMFTErrors.errorExpectedGotWithException(
          "Cannot parse four element vector: " + e.getMessage(),
          "<float> <float> <float> <float>",
          line,
          this.reader.position(),
          e));
        return FAILURE;
      }
    }
    receiver.onError(SMFTErrors.errorExpectedGot(
      "Cannot parse four element vector.",
      "<float> <float> <float> <float>",
      line,
      this.reader.position()));
    return FAILURE;
  }

  private SMFTParsingStatus parseAttributeElementFloat3(
    final SMFParserEventsDataAttributeValuesType receiver,
    final List<String> line)
  {
    if (line.length() == 3) {
      try {
        final double x = Double.parseDouble(line.get(0));
        final double y = Double.parseDouble(line.get(1));
        final double z = Double.parseDouble(line.get(2));
        receiver.onDataAttributeValueFloat3(x, y, z);
        return SUCCESS;
      } catch (final NumberFormatException e) {
        receiver.onError(SMFTErrors.errorExpectedGotWithException(
          "Cannot parse three element vector: " + e.getMessage(),
          "<float> <float> <float>",
          line,
          this.reader.position(),
          e));
        return FAILURE;
      }
    }
    receiver.onError(SMFTErrors.errorExpectedGot(
      "Cannot parse three element vector.",
      "<float> <float> <float>",
      line,
      this.reader.position()));
    return FAILURE;
  }

  private SMFTParsingStatus parseAttributeElementFloat2(
    final SMFParserEventsDataAttributeValuesType receiver,
    final List<String> line)
  {
    if (line.length() == 2) {
      try {
        final double x = Double.parseDouble(line.get(0));
        final double y = Double.parseDouble(line.get(1));
        receiver.onDataAttributeValueFloat2(x, y);
        return SUCCESS;
      } catch (final NumberFormatException e) {
        receiver.onError(SMFTErrors.errorExpectedGotWithException(
          "Cannot parse two element vector: " + e.getMessage(),
          "<float> <float>",
          line,
          this.reader.position(),
          e));
        return FAILURE;
      }
    }
    receiver.onError(SMFTErrors.errorExpectedGot(
      "Cannot parse two element vector.",
      "<float> <float>",
      line,
      this.reader.position()));
    return FAILURE;
  }

  private SMFTParsingStatus parseAttributeElementFloat1(
    final SMFParserEventsDataAttributeValuesType receiver,
    final List<String> line)
  {
    if (line.length() == 1) {
      try {
        final double x = Double.parseDouble(line.get(0));
        receiver.onDataAttributeValueFloat1(x);
        return SUCCESS;
      } catch (final NumberFormatException e) {
        receiver.onError(SMFTErrors.errorExpectedGotWithException(
          "Cannot parse float: " + e.getMessage(),
          "<float>",
          line,
          this.reader.position(),
          e));
        return FAILURE;
      }
    }
    receiver.onError(SMFTErrors.errorExpectedGot(
      "Cannot parse float.",
      "<float>",
      line,
      this.reader.position()));
    return FAILURE;
  }

  private static final class IgnoringDataReceiver
    implements SMFParserEventsDataAttributesNonInterleavedType
  {
    private final SMFParserEventsBodyType receiver;

    IgnoringDataReceiver(
      final SMFParserEventsBodyType in_receiver)
    {
      this.receiver = Objects.requireNonNull(in_receiver, "Receiver");
    }

    @Override
    public Optional<SMFParserEventsDataAttributeValuesType> onDataAttributeStart(
      final SMFAttribute attribute)
    {
      return Optional.empty();
    }

    @Override
    public void onDataAttributesNonInterleavedFinish()
    {

    }

    @Override
    public void onError(
      final SMFErrorType e)
    {
      this.receiver.onError(e);
    }

    @Override
    public void onWarning(
      final SMFWarningType w)
    {
      this.receiver.onWarning(w);
    }
  }
}
