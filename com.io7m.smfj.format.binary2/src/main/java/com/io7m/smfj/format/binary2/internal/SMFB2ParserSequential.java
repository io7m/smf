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

import com.io7m.smfj.parser.api.SMFParseErrors;
import com.io7m.smfj.parser.api.SMFParserEventsType;
import com.io7m.smfj.parser.api.SMFParserSequentialType;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.Objects;

public final class SMFB2ParserSequential implements SMFParserSequentialType
{
  private final SMFParserEventsType events;
  private final URI uri;
  private final InputStream stream;
  private final SMFB2ParsingContexts parserContexts;

  public SMFB2ParserSequential(
    final SMFParserEventsType inEvents,
    final URI inUri,
    final InputStream inStream,
    final SMFB2ParsingContexts inParserContexts)
  {
    this.events =
      Objects.requireNonNull(inEvents, "inEvents");
    this.uri =
      Objects.requireNonNull(inUri, "inUri");
    this.stream =
      Objects.requireNonNull(inStream, "inStream");
    this.parserContexts =
      Objects.requireNonNull(inParserContexts, "inParserContexts");
  }

  @Override
  public void parse()
  {
    try (var context =
           this.parserContexts.ofStream(this.uri, this.stream, this.events)) {
      new SMFB2ParsingFile(this.events).parse(context);
    } catch (final IOException e) {
      this.events.onError(SMFParseErrors.errorException(e));
    }
  }

  @Override
  public void close()
  {
    this.events.onFinish();
  }
}
