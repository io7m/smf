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

package com.io7m.smfj.format.xml.internal;

import com.io7m.blackthorne.api.BTContentHandler;
import com.io7m.blackthorne.api.BTParseError;
import com.io7m.blackthorne.api.BTQualifiedName;
import com.io7m.jlexing.core.LexicalPosition;
import com.io7m.smfj.parser.api.SMFParseError;
import com.io7m.smfj.parser.api.SMFParseErrors;
import com.io7m.smfj.parser.api.SMFParseWarning;
import com.io7m.smfj.parser.api.SMFParserEventsType;
import com.io7m.smfj.parser.api.SMFParserSequentialType;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.XMLReader;

public final class SMFXParser implements SMFParserSequentialType
{
  private static final Logger LOG =
    LoggerFactory.getLogger(SMFXParser.class);

  private final XMLReader reader;
  private final InputStream stream;
  private final SMFParserEventsType events;
  private final URI source;

  public SMFXParser(
    final SMFParserEventsType inEvents,
    final URI inSource,
    final XMLReader inReader,
    final InputStream inStream)
  {
    this.events =
      Objects.requireNonNull(inEvents, "events");
    this.source =
      Objects.requireNonNull(inSource, "inSource");
    this.reader =
      Objects.requireNonNull(inReader, "stream");
    this.stream =
      Objects.requireNonNull(inStream, "stream");
  }

  @Override
  public void parse()
  {
    final var contentHandler =
      new BTContentHandler<>(
        this.source,
        this::onError,
        Map.ofEntries(
          Map.entry(
            BTQualifiedName.of(SMFX.namespaceURI2p0(), "SMF"),
            c -> new SMFX(c, this.events)
          )
        )
      );

    this.reader.setContentHandler(contentHandler);
    this.reader.setErrorHandler(contentHandler);

    final var inputSource = new InputSource(this.stream);
    inputSource.setPublicId(this.source.toString());

    try {
      this.reader.parse(inputSource);
      LOG.debug("parsing completed");
    } catch (final SAXParseException e) {
      this.events.onError(SMFParseError.of(
        LexicalPosition.of(
          e.getLineNumber(),
          e.getColumnNumber(),
          Optional.of(this.source)),
        e.getMessage(),
        Optional.of(e)
      ));
    } catch (final IOException | SAXException e) {
      this.events.onError(SMFParseErrors.errorException(e));
    }
  }

  private void onError(
    final BTParseError e)
  {
    switch (e.severity()) {
      case WARNING: {
        this.events.onWarning(
          SMFParseWarning.of(e.lexical(), e.message(), e.exception()));
        break;
      }
      case ERROR: {
        this.events.onError(
          SMFParseError.of(e.lexical(), e.message(), e.exception()));
        break;
      }
    }
  }

  @Override
  public void close()
    throws IOException
  {
    this.stream.close();
  }
}
