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
import com.io7m.smfj.core.SMFErrorType;
import com.io7m.smfj.core.SMFFormatVersion;
import com.io7m.smfj.core.SMFPartialLogged;
import com.io7m.smfj.core.SMFWarningType;
import com.io7m.smfj.parser.api.SMFParseError;
import com.io7m.smfj.parser.api.SMFParseErrors;
import com.io7m.smfj.parser.api.SMFParseWarning;
import com.io7m.smfj.parser.api.SMFParserProviderType;
import com.io7m.smfj.probe.api.SMFVersionProbed;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.XMLReader;

public final class SMFXProbe
{
  private static final Logger LOG = LoggerFactory.getLogger(SMFXProbe.class);
  private final XMLReader reader;
  private final InputStream stream;
  private final SMFParserProviderType parsers;
  private final URI source;

  public SMFXProbe(
    final SMFParserProviderType inParsers,
    final URI inSource,
    final XMLReader inReader,
    final InputStream inStream)
  {
    this.parsers =
      Objects.requireNonNull(inParsers, "parsers");
    this.source =
      Objects.requireNonNull(inSource, "inSource");
    this.reader =
      Objects.requireNonNull(inReader, "stream");
    this.stream =
      Objects.requireNonNull(inStream, "stream");
  }

  private static void onError(
    final ArrayList<SMFErrorType> errors,
    final ArrayList<SMFWarningType> warnings,
    final BTParseError error)
  {
    switch (error.severity()) {
      case WARNING: {
        warnings.add(SMFParseWarning.of(
          error.lexical(),
          error.message(),
          error.exception()
        ));
        break;
      }
      case ERROR: {
        errors.add(SMFParseError.of(
          error.lexical(),
          error.message(),
          error.exception()
        ));
        break;
      }
    }
  }

  public SMFPartialLogged<SMFVersionProbed> execute()
  {
    final var errors = new ArrayList<SMFErrorType>();
    final var warnings = new ArrayList<SMFWarningType>();

    final var contentHandler =
      new BTContentHandler<>(
        this.source,
        error -> onError(errors, warnings, error),
        Map.ofEntries(
          Map.entry(
            BTQualifiedName.of(SMFX.namespaceURI2p0(), "SMF"),
            SMFXIgnoring::new
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
      if (!errors.isEmpty()) {
        return SMFPartialLogged.failed(errors, warnings);
      }
      return SMFPartialLogged.succeeded(SMFVersionProbed.of(
        this.parsers,
        SMFFormatVersion.of(2, 0)
      ));
    } catch (final SAXParseException e) {
      final SMFParseError error =
        SMFParseError.of(
          LexicalPosition.of(
            e.getLineNumber(),
            e.getColumnNumber(),
            Optional.of(this.source)),
          e.getMessage(),
          Optional.of(e)
        );
      errors.add(error);
      return SMFPartialLogged.failed(errors, warnings);
    } catch (final IOException | SAXException e) {
      errors.add(SMFParseErrors.errorException(e));
      return SMFPartialLogged.failed(errors, warnings);
    }
  }
}
