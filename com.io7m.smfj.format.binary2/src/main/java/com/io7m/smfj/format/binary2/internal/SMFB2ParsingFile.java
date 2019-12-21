/*
 * Copyright © 2019 Mark Raynsford <code@io7m.com> http://io7m.com
 *
 * Permission to use, copy, modify, and/or distribute this software for
 * any purpose with or without fee is hereby granted, provided that the
 * above copyright notice and this permission notice appear in all copies.
 *
 * THE SOFTWARE IS PROVIDED "AS IS" AND THE AUTHOR DISCLAIMS ALL
 * WARRANTIES WITH REGARD TO THIS SOFTWARE INCLUDING ALL IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS. IN NO EVENT SHALL THE AUTHOR
 * BE LIABLE FOR ANY SPECIAL, DIRECT, INDIRECT, OR CONSEQUENTIAL DAMAGES
 * OR ANY DAMAGES WHATSOEVER RESULTING FROM LOSS OF USE, DATA OR PROFITS,
 * WHETHER IN AN ACTION OF CONTRACT, NEGLIGENCE OR OTHER TORTIOUS ACTION,
 * ARISING OUT OF OR IN CONNECTION WITH THE USE OR PERFORMANCE OF THIS
 * SOFTWARE.
 */


package com.io7m.smfj.format.binary2.internal;

import com.io7m.jbssio.api.BSSReaderType;
import com.io7m.smfj.core.SMFHeader;
import com.io7m.smfj.core.SMFVoid;
import com.io7m.smfj.format.binary2.SMFB2Section;
import com.io7m.smfj.parser.api.SMFParserEventsBodyType;
import com.io7m.smfj.parser.api.SMFParserEventsType;
import java.io.IOException;
import java.util.HashMap;
import java.util.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class SMFB2ParsingFile
  implements SMFB2StructureParserType<SMFVoid>
{
  private static final Logger LOG =
    LoggerFactory.getLogger(SMFB2ParsingFile.class);

  private final SMFParserEventsType events;
  private final HashMap<Long, BodyParseHandlerType> handlers;

  public SMFB2ParsingFile(
    final SMFParserEventsType inEvents)
  {
    this.events = Objects.requireNonNull(inEvents, "events");

    this.handlers = new HashMap<>();

    this.handlers.put(
      Long.valueOf(SMFB2ParsingSectionMetadata.magic()),
      SMFB2ParsingFile::handleMetadata);
    this.handlers.put(
      Long.valueOf(SMFB2ParsingSectionVertexDataNI.magic()),
      SMFB2ParsingFile::handleVertexDataNI);
    this.handlers.put(
      Long.valueOf(SMFB2ParsingSectionTriangles.magic()),
      SMFB2ParsingFile::handleTriangles);
    this.handlers.put(
      Long.valueOf(SMFB2ParsingSectionEnd.magic()),
      SMFB2ParsingFile::handleEnd);
  }

  private static boolean handleTriangles(
    final SMFB2ParsingContextType context,
    final BSSReaderType reader,
    final SMFParserEventsBodyType bodyEvents,
    final SMFB2Section section,
    final SMFHeader smf)
    throws IOException
  {
    final var eventHandlerOpt = bodyEvents.onTriangles();
    if (eventHandlerOpt.isPresent()) {
      final var eventHandler = eventHandlerOpt.get();
      new SMFB2ParsingSectionTriangles(section, smf, eventHandler)
        .parse(context);
    }
    return true;
  }

  private static boolean handleVertexDataNI(
    final SMFB2ParsingContextType context,
    final BSSReaderType reader,
    final SMFParserEventsBodyType bodyEvents,
    final SMFB2Section section,
    final SMFHeader smf)
    throws IOException
  {
    final var eventHandlerOpt = bodyEvents.onAttributesNonInterleaved();
    if (eventHandlerOpt.isPresent()) {
      final var eventHandler = eventHandlerOpt.get();
      new SMFB2ParsingSectionVertexDataNI(section, smf, eventHandler)
        .parse(context);
    }
    return true;
  }

  private static boolean handleEnd(
    final SMFB2ParsingContextType context,
    final BSSReaderType reader,
    final SMFParserEventsBodyType bodyEvents,
    final SMFB2Section section,
    final SMFHeader smf)
    throws IOException
  {
    final var result = new SMFB2ParsingSectionEnd(section).parse(context);
    return !result.isSucceeded();
  }

  private static boolean handleMetadata(
    final SMFB2ParsingContextType context,
    final BSSReaderType reader,
    final SMFParserEventsBodyType bodyEvents,
    final SMFB2Section section,
    final SMFHeader smf)
    throws IOException
  {
    new SMFB2ParsingSectionMetadata(bodyEvents, section).parse(context);
    return true;
  }

  @Override
  public SMFVoid parse(final SMFB2ParsingContextType context)
    throws IOException
  {
    return context.withReader("file", reader -> {
      this.parseWithReader(context, reader);
      return SMFVoid.void_();
    });
  }

  private void parseWithReader(
    final SMFB2ParsingContextType context,
    final BSSReaderType reader)
    throws IOException
  {
    LOG.trace("parsing file header");
    final var formatVersionOpt = new SMFB2ParsingFileHeader().parse(context);
    if (formatVersionOpt.isEmpty()) {
      LOG.trace("no valid file header");
      return;
    }

    final var headerEventsOpt =
      this.events.onVersionReceived(formatVersionOpt.get());
    if (headerEventsOpt.isEmpty()) {
      LOG.trace("no header events requested");
      return;
    }

    final var headerEvents = headerEventsOpt.get();
    final var smfSectionHeader =
      new SMFB2ParsingSectionHeader().parse(context);
    final var smfOpt =
      new SMFB2ParsingSectionSMF(smfSectionHeader).parse(context);

    if (smfOpt.isEmpty()) {
      LOG.trace("no valid smf section");
      return;
    }

    final var smf = smfOpt.get();
    final var bodyEventsOpt = headerEvents.onHeaderParsed(smf);
    if (bodyEventsOpt.isEmpty()) {
      LOG.trace("no body events requested");
      return;
    }

    final var bodyEvents = bodyEventsOpt.get();
    while (true) {
      if (LOG.isTraceEnabled()) {
        LOG.trace(
          "parsing section at (absolute) offset 0x{}",
          Long.toUnsignedString(reader.offsetCurrentAbsolute(), 16));
      }

      final SMFB2Section sectionHeader =
        new SMFB2ParsingSectionHeader()
          .parse(context);

      if (!this.handleSectionHeader(
        context,
        reader,
        bodyEvents,
        sectionHeader,
        smf)) {
        break;
      }
    }
  }

  private boolean handleSectionHeader(
    final SMFB2ParsingContextType context,
    final BSSReaderType reader,
    final SMFParserEventsBodyType bodyEvents,
    final SMFB2Section sectionHeader,
    final SMFHeader smf)
    throws IOException
  {
    final var handler = this.handlers.get(Long.valueOf(sectionHeader.id()));
    if (handler == null) {
      context.publishWarning(
        SMFB2ParseErrors.warningOf(
          reader,
          "Unrecognized section with id 0x%s; skipping it",
          Long.toUnsignedString(sectionHeader.id(), 16)
        )
      );
      reader.skip(sectionHeader.sizeOfData());
      return true;
    }

    return handler.parse(context, reader, bodyEvents, sectionHeader, smf);
  }

  interface BodyParseHandlerType
  {
    boolean parse(
      SMFB2ParsingContextType context,
      BSSReaderType reader,
      SMFParserEventsBodyType bodyEvents,
      SMFB2Section sectionHeader,
      SMFHeader smf)
      throws IOException;
  }
}
