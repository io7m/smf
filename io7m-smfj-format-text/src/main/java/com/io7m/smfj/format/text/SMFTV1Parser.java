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

package com.io7m.smfj.format.text;

import com.io7m.jnull.NullCheck;
import com.io7m.smfj.core.SMFFormatVersion;
import com.io7m.smfj.parser.api.SMFParserEventsType;
import com.io7m.smfj.parser.api.SMFParserSequentialType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

final class SMFTV1Parser extends SMFTAbstractParser
{
  private static final Logger LOG;

  static {
    LOG = LoggerFactory.getLogger(SMFTV1Parser.class);
  }

  private final SMFTAbstractParser parent;
  private final SMFFormatVersion version;
  private final SMFTV1HeaderParser header_parser;

  SMFTV1Parser(
    final SMFTAbstractParser in_parent,
    final SMFParserEventsType in_events,
    final SMFTLineReader in_reader,
    final SMFFormatVersion in_version)
  {
    super(in_events, in_reader, in_parent.state);
    this.parent = NullCheck.notNull(in_parent, "Parent");
    this.version = NullCheck.notNull(in_version, "Version");
    this.header_parser =
      new SMFTV1HeaderParser(this, in_events, in_reader, in_version);
  }

  @Override
  protected Logger log()
  {
    return LOG;
  }

  @Override
  public void parseHeader()
  {
    this.header_parser.parseHeader();
  }

  @Override
  public void parseData()
    throws IllegalStateException
  {
    if (super.state.get() != ParserState.STATE_HEADER_PARSED) {
      throw new IllegalStateException("Header has not been parsed");
    }

    final SMFParserSequentialType body_parser =
      new SMFTV1BodyParser(
        this,
        super.events,
        super.reader,
        this.version,
        this.header_parser.attributes,
        this.header_parser.vertex_count,
        this.header_parser.triangle_count,
        this.header_parser.triangle_size);
    body_parser.parseData();
  }
}
