/*
 * Copyright © 2016 <code@io7m.com> http://io7m.com
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

package com.io7m.smfj.format.binary;

import com.io7m.jlexing.core.LexicalPosition;
import com.io7m.jnull.NullCheck;
import com.io7m.smfj.parser.api.SMFParseError;
import com.io7m.smfj.parser.api.SMFParserEventsType;
import com.io7m.smfj.parser.api.SMFParserSequentialType;
import org.slf4j.Logger;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

abstract class SMFBAbstractParserSequential implements
  SMFParserSequentialType
{
  protected final SMFParserEventsType events;
  protected final AtomicReference<ParserState> state;
  protected final SMFBDataStreamReaderType reader;

  SMFBAbstractParserSequential(
    final SMFParserEventsType in_events,
    final SMFBDataStreamReaderType in_reader,
    final AtomicReference<ParserState> in_state)
  {
    this.events = NullCheck.notNull(in_events, "Events");
    this.reader = NullCheck.notNull(in_reader, "Reader");
    this.state = NullCheck.notNull(in_state, "State");
  }

  protected abstract Logger log();

  protected final String onFailure(
    final String message)
  {
    NullCheck.notNull(message, "message");
    this.log().debug("onFailure: {}", message);
    this.state.set(ParserState.STATE_FAILED);
    this.events.onError(SMFParseError.of(
      LexicalPosition.of(-1, -1, Optional.of(this.reader.path())), message));
    return message;
  }

  protected final String fail(
    final String message)
  {
    return this.onFailure(message);
  }

  protected final String failExpectedGot(
    final String message,
    final String expected,
    final String received)
  {
    final StringBuilder sb = new StringBuilder(128);
    sb.append(message);
    sb.append(System.lineSeparator());
    sb.append("  Expected: ");
    sb.append(expected);
    sb.append(System.lineSeparator());
    sb.append("  Received: ");
    sb.append(received);
    sb.append(System.lineSeparator());
    return this.onFailure(sb.toString());
  }

  enum ParserState
  {
    STATE_INITIAL,
    STATE_PARSED_HEADER,
    STATE_FAILED
  }
}
