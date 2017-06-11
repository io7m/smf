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

import com.io7m.jaffirm.core.Invariants;
import com.io7m.jaffirm.core.Preconditions;
import com.io7m.jlexing.core.LexicalPosition;
import com.io7m.jnull.NullCheck;
import com.io7m.smfj.core.SMFAttribute;
import com.io7m.smfj.core.SMFAttributeName;
import com.io7m.smfj.core.SMFFormatVersion;
import com.io7m.smfj.core.SMFHeader;
import com.io7m.smfj.format.text.SMFTBodySectionParserType;
import com.io7m.smfj.format.text.SMFTHeaderCommandParserType;
import com.io7m.smfj.format.text.SMFTLineReaderType;
import com.io7m.smfj.format.text.SMFTParsingStatus;
import com.io7m.smfj.parser.api.SMFParseError;
import com.io7m.smfj.parser.api.SMFParseErrors;
import com.io7m.smfj.parser.api.SMFParseWarnings;
import com.io7m.smfj.parser.api.SMFParserEventsBodyType;
import com.io7m.smfj.parser.api.SMFParserEventsHeaderType;
import com.io7m.smfj.parser.api.SMFParserEventsType;
import com.io7m.smfj.parser.api.SMFParserSequentialType;
import javaslang.collection.List;

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Objects;
import java.util.Optional;
import java.util.TreeMap;
import java.util.stream.Collectors;

import static com.io7m.smfj.format.text.SMFTParsingStatus.FAILURE;
import static com.io7m.smfj.format.text.SMFTParsingStatus.SUCCESS;

/**
 * A parser for the 1.* format.
 */

public final class SMFTV1Parser implements SMFParserSequentialType
{
  private final SMFParserEventsType events;
  private final SMFTLineReaderType reader;
  private final SMFFormatVersion version;
  private final TreeMap<String, SMFTHeaderCommandParserType> header_commands;
  private final TreeMap<String, SMFTBodySectionParserType> body_commands;
  private final SMFHeader.Builder header_builder;
  private TreeMap<SMFAttributeName, Integer> attributes_lines;
  private Collection<SMFAttribute> attributes_list;
  private SMFHeader header;

  /**
   * Construct a parser.
   *
   * @param in_version The format version
   * @param in_events  An event receiver
   * @param in_reader  A line reader
   */

  public SMFTV1Parser(
    final SMFFormatVersion in_version,
    final SMFParserEventsType in_events,
    final SMFTLineReaderType in_reader)
  {
    this.events = NullCheck.notNull(in_events, "Events");
    this.reader = NullCheck.notNull(in_reader, "Reader");
    this.version = NullCheck.notNull(in_version, "Version");

    Preconditions.checkPreconditionI(
      in_version.major(),
      in_version.major() == 1,
      v -> "Major version " + v + " must be 1");

    this.attributes_lines = new TreeMap<>();
    this.attributes_list = new ArrayList<>(8);

    this.header_builder = SMFHeader.builder();

    this.header_commands = new TreeMap<>();
    this.registerHeaderCommand(
      new SMFTV1HeaderCommandAttribute(
        this.attributes_lines,
        this.attributes_list,
        this.reader,
        this.header_builder));
    this.registerHeaderCommand(
      new SMFTV1HeaderCommandCoordinates(this.reader, this.header_builder));
    this.registerHeaderCommand(
      new SMFTV1HeaderCommandMeta(this.reader, this.header_builder));
    this.registerHeaderCommand(
      new SMFTV1HeaderCommandSchema(this.reader, this.header_builder));
    this.registerHeaderCommand(
      new SMFTV1HeaderCommandTriangles(this.reader, this.header_builder));
    this.registerHeaderCommand(
      new SMFTV1HeaderCommandVertices(this.reader, this.header_builder));

    this.body_commands = new TreeMap<>();
    this.registerBodyCommand(
      new SMFTV1BodySectionParserVertices(() -> this.header, this.reader));
    this.registerBodyCommand(
      new SMFTV1BodySectionParserTriangles(() -> this.header, this.reader));
    this.registerBodyCommand(
      new SMFTV1BodySectionParserMetadata(() -> this.header, this.reader));
  }

  private void registerHeaderCommand(
    final SMFTHeaderCommandParserType cmd)
  {
    this.header_commands.put(cmd.name(), cmd);
  }

  private void registerBodyCommand(
    final SMFTBodySectionParserType cmd)
  {
    this.body_commands.put(cmd.name(), cmd);
  }

  private boolean checkUniqueAttributeNames(
    final SMFParserEventsHeaderType receiver)
  {
    final Collection<SMFAttributeName> names =
      new HashSet<>(this.attributes_list.size());

    boolean result = true;

    for (final SMFAttribute attribute : this.attributes_list) {
      final SMFAttributeName name = attribute.name();
      if (names.contains(name)) {
        Invariants.checkInvariant(
          name,
          this.attributes_lines.containsKey(name),
          a_name -> "Attribute lines must contain " + a_name);

        final LexicalPosition<URI> position =
          LexicalPosition.of(
            this.attributes_lines.get(name).intValue(),
            0,
            this.reader.position().file());

        receiver.onError(SMFParseError.of(
          position,
          "Duplicate attribute name: " + name.value(),
          Optional.empty()
        ));

        result = false;
      }
      names.add(name);
    }

    return result;
  }

  private String knownHeaderCommands()
  {
    return this.header_commands.keySet()
      .stream()
      .collect(Collectors.joining(" | "));
  }

  private String knownBodyCommands()
  {
    return this.body_commands.keySet()
      .stream()
      .collect(Collectors.joining(" | "));
  }

  @Override
  public void parse()
  {
    try {
      final Optional<SMFParserEventsHeaderType> r_opt =
        this.events.onVersionReceived(this.version);

      if (!r_opt.isPresent()) {
        return;
      }

      this.parseHeader(r_opt.get());
    } catch (final Exception e) {
      String message = e.getMessage();
      if (message == null) {
        message = e.getClass().getCanonicalName();
      }

      this.events.onError(SMFParseError.of(
        this.reader.position(), message, Optional.of(e)));
    }
  }

  private void parseHeader(
    final SMFParserEventsHeaderType header_receiver)
    throws Exception
  {
    switch (this.parseHeaderCommands(header_receiver)) {
      case SUCCESS:
        break;
      case FAILURE:
        return;
    }

    if (!this.checkUniqueAttributeNames(header_receiver)) {
      return;
    }

    this.header = this.header_builder.build();
    final Optional<SMFParserEventsBodyType> r_opt =
      header_receiver.onHeaderParsed(this.header);

    if (!r_opt.isPresent()) {
      return;
    }

    final SMFParserEventsBodyType data_receiver = r_opt.get();
    switch (this.parseBodyCommands(data_receiver)) {
      case SUCCESS:
        break;
      case FAILURE:
        return;
    }
  }

  private SMFTParsingStatus parseBodyCommands(
    final SMFParserEventsBodyType receiver)
    throws Exception
  {
    while (true) {
      final Optional<List<String>> line_opt = this.reader.line();
      if (!line_opt.isPresent()) {
        return SUCCESS;
      }

      final List<String> line = line_opt.get();
      if (line.isEmpty()) {
        continue;
      }

      final String command_name = line.get(0);
      final SMFTBodySectionParserType command =
        this.body_commands.get(command_name);

      if (command == null) {
        receiver.onWarning(SMFParseWarnings.warningExpectedGot(
          String.format("Unrecognized command '%s'", command_name),
          "One of: " + this.knownBodyCommands(),
          line.collect(Collectors.joining(" ")),
          this.reader.position()));
        continue;
      }

      switch (command.parse(receiver, line)) {
        case SUCCESS:
          break;
        case FAILURE:
          switch (SMFTSections.skipUntilEnd(this.reader, receiver)) {
            case SUCCESS:
              continue;
            case FAILURE:
              return FAILURE;
          }
          break;
      }
    }
  }


  private SMFTParsingStatus parseHeaderCommands(
    final SMFParserEventsHeaderType receiver)
    throws Exception
  {
    while (true) {
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

      final String command_name = line.get(0);
      if (Objects.equals(command_name, "end")) {
        if (line.length() == 1) {
          return SUCCESS;
        }

        receiver.onError(SMFParseErrors.errorExpectedGot(
          "Malformed 'end' command",
          "One of: " + this.knownHeaderCommands(),
          line.collect(Collectors.joining(" ")),
          this.reader.position()));
        return FAILURE;
      }

      final SMFTHeaderCommandParserType command =
        this.header_commands.get(command_name);

      if (command == null) {
        receiver.onWarning(SMFParseWarnings.warningExpectedGot(
          String.format("Unrecognized command '%s'", command_name),
          "One of: " + this.knownHeaderCommands(),
          line.collect(Collectors.joining(" ")),
          this.reader.position()));
        continue;
      }

      switch (command.parse(receiver, line)) {
        case SUCCESS:
          continue;
        case FAILURE:
          return FAILURE;
      }
    }
  }

  @Override
  public void close()
    throws IOException
  {

  }

}
