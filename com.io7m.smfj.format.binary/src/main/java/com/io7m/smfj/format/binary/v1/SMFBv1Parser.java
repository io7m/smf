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

import com.io7m.jaffirm.core.Preconditions;
import com.io7m.jlexing.core.LexicalPosition;
import com.io7m.smfj.core.SMFFormatVersion;
import com.io7m.smfj.core.SMFHeader;
import com.io7m.smfj.core.SMFPartialLogged;
import com.io7m.smfj.format.binary.SMFBBodySectionParserType;
import com.io7m.smfj.format.binary.SMFBDataStreamReaderType;
import com.io7m.smfj.format.binary.SMFBSection;
import com.io7m.smfj.format.binary.SMFBSectionEnd;
import com.io7m.smfj.format.binary.SMFBSectionHeader;
import com.io7m.smfj.format.binary.SMFBSectionMetadata;
import com.io7m.smfj.format.binary.SMFBSectionParser;
import com.io7m.smfj.format.binary.SMFBSectionParserType;
import com.io7m.smfj.format.binary.SMFBSectionTriangles;
import com.io7m.smfj.format.binary.SMFBSectionVerticesNonInterleaved;
import com.io7m.smfj.parser.api.SMFParseWarning;
import com.io7m.smfj.parser.api.SMFParserEventsBodyType;
import com.io7m.smfj.parser.api.SMFParserEventsHeaderType;
import com.io7m.smfj.parser.api.SMFParserSequentialType;
import java.io.IOException;
import java.net.URI;
import java.util.BitSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.io7m.smfj.format.binary.implementation.Flags.TRIANGLES_REQUIRED;
import static com.io7m.smfj.format.binary.implementation.Flags.VERTICES_REQUIRED;
import static com.io7m.smfj.parser.api.SMFParseErrors.errorExpectedGot;


/**
 * A parser for version 1.* files.
 */

public final class SMFBv1Parser implements SMFParserSequentialType
{
  private static final Logger LOG =
    LoggerFactory.getLogger(SMFBv1Parser.class);

  private final SMFBDataStreamReaderType reader;
  private final SMFParserEventsHeaderType events_header;
  private final SMFBSectionParserType sections;
  private final SMFFormatVersion version;
  private final Map<Long, SMFBBodySectionParserType> section_parsers;
  private final BitSet state;
  private SMFHeader header;

  /**
   * Construct a parser.
   *
   * @param in_version       The format version
   * @param in_state         The current parser state
   * @param in_reader        A data stream reader
   * @param in_events_header An event receiver
   */

  public SMFBv1Parser(
    final SMFFormatVersion in_version,
    final BitSet in_state,
    final SMFBDataStreamReaderType in_reader,
    final SMFParserEventsHeaderType in_events_header)
  {
    this.version =
      Objects.requireNonNull(in_version, "Version");
    this.state =
      Objects.requireNonNull(in_state, "State");
    this.reader =
      Objects.requireNonNull(in_reader, "Reader");
    this.events_header =
      Objects.requireNonNull(in_events_header, "Events Header");
    this.sections =
      new SMFBSectionParser(this.reader);

    this.section_parsers =
      Map.ofEntries(
        Map.entry(
          Long.valueOf(SMFBSectionTriangles.MAGIC),
          new SMFBv1SectionParserTriangles(this.state)),
        Map.entry(
          Long.valueOf(SMFBSectionMetadata.MAGIC),
          new SMFBv1SectionParserMetadata()),
        Map.entry(
          Long.valueOf(SMFBSectionVerticesNonInterleaved.MAGIC),
          new SMFBv1SectionParserVerticesNonInterleaved(this.state)));

    Preconditions.checkPreconditionI(
      this.version.major(),
      this.version.major() == 1,
      i -> "Major version must be 1");
  }

  @Override
  public void parse()
  {
    final SMFPartialLogged<Optional<SMFParserEventsBodyType>> header_r =
      this.parseHeader();
    if (header_r.isFailed()) {
      return;
    }

    final Optional<SMFParserEventsBodyType> events_body_opt = header_r.get();
    if (events_body_opt.isEmpty()) {
      return;
    }

    final SMFParserEventsBodyType events_body = events_body_opt.get();

    while (true) {
      final long current_position = this.reader.position();
      if (LOG.isTraceEnabled()) {
        LOG.trace("current: {}", Long.toUnsignedString(current_position));
      }

      final SMFPartialLogged<SMFBSection> result =
        this.sections.parse();
      if (result.isFailed()) {
        result.errors().forEach(events_body::onError);
        return;
      }

      final SMFBSection section = result.get();
      switch (this.parseBodySection(events_body, section)) {
        case CONTINUE:
          continue;
        case HALT:
          return;
      }
    }
  }

  private Continue parseBodySection(
    final SMFParserEventsBodyType events_body,
    final SMFBSection section)
  {
    final long magic = section.id();
    if (magic == SMFBSectionEnd.MAGIC) {
      return Continue.HALT;
    }

    final SMFBBodySectionParserType parser =
      this.section_parsers.get(Long.valueOf(magic));

    if (parser != null) {
      parser.parse(
        this.header, events_body, this.reader.withBounds(section.sizeOfData()));
      return Continue.CONTINUE;
    }

    final String text =
      new StringBuilder(128)
        .append("Unrecognized section type.")
        .append(System.lineSeparator())
        .append("  Section: ")
        .append(Long.toUnsignedString(magic, 16))
        .append(System.lineSeparator())
        .toString();

    events_body.onWarning(SMFParseWarning.of(
      this.reader.positionLexical(), text, Optional.empty()));
    return Continue.CONTINUE;
  }

  private SMFPartialLogged<Optional<SMFParserEventsBodyType>> parseHeader()
  {
    final SMFPartialLogged<SMFBSection> result = this.sections.parse();
    if (result.isFailed()) {
      result.errors().forEach(this.events_header::onError);
      return SMFPartialLogged.failed(List.of());
    }

    final LexicalPosition<URI> position = this.reader.positionLexical();
    final SMFBSection section = result.get();

    if (section.id() != SMFBSectionHeader.MAGIC) {
      this.events_header.onError(
        errorExpectedGot(
          "Files must begin with an SMF_HEAD section.",
          "Section " + Long.toUnsignedString(SMFBSectionHeader.MAGIC, 16),
          "Section " + Long.toUnsignedString(section.id(), 16),
          position));
      return SMFPartialLogged.failed(List.of());
    }

    if (LOG.isTraceEnabled()) {
      LOG.trace(
        "header section: {} octets",
        Long.toUnsignedString(section.sizeOfData()));
    }

    final SMFPartialLogged<SMFHeader> header_result =
      SMFBv1Headers.parse(
        this.version, this.reader.withBounds(section.sizeOfData()), section);

    header_result.errors().forEach(this.events_header::onError);
    header_result.warnings().forEach(this.events_header::onWarning);

    if (header_result.isFailed()) {
      return SMFPartialLogged.failed(List.of());
    }

    final SMFHeader result_header = header_result.get();
    this.header = result_header;

    this.state.set(
      VERTICES_REQUIRED, this.header.vertexCount() != 0L);
    this.state.set(
      TRIANGLES_REQUIRED, this.header.triangles().triangleCount() != 0L);

    return SMFPartialLogged.succeeded(
      this.events_header.onHeaderParsed(result_header));
  }

  @Override
  public void close()
    throws IOException
  {

  }

  private enum Continue
  {
    CONTINUE,
    HALT
  }
}
