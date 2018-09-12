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

package com.io7m.smfj.format.text;

import com.io7m.jlexing.core.LexicalPosition;
import com.io7m.jlexing.core.LexicalPositionMutable;
import io.vavr.collection.List;
import org.slf4j.Logger;

import java.io.IOException;
import java.net.URI;
import java.util.Optional;

/**
 * An abstract line reader implementation.
 */

abstract class SMFTLineReaderAbstract implements SMFTLineReaderType
{
  private final LexicalPositionMutable<URI> position;
  private final SMFTLineLexer lexer;

  SMFTLineReaderAbstract(
    final URI in_uri,
    final int in_start)
  {
    this.lexer = new SMFTLineLexer();
    this.position = LexicalPositionMutable.create(
      in_start - 1,
      0,
      Optional.of(in_uri));
  }

  @Override
  public final LexicalPosition<URI> position()
  {
    return LexicalPosition.copyOf(this.position);
  }

  @Override
  public final Optional<List<String>> line()
    throws IOException
  {
    final String line = this.lineNextRaw();
    this.position.setLine(Math.addExact(this.position.line(), 1));

    if (line == null) {
      return Optional.empty();
    }

    final String trimmed = line.trim();
    if (trimmed.isEmpty()) {
      return Optional.of(List.empty());
    }

    if (trimmed.startsWith("#")) {
      return Optional.of(List.empty());
    }

    if (this.log().isTraceEnabled()) {
      if (this.position.file().isPresent()) {
        final URI file = this.position.file().get();
        this.log().trace(
          "{}:{}: {}", file, Integer.valueOf(this.position.line()), trimmed);
      } else {
        this.log().trace(
          "{}: {}", Integer.valueOf(this.position.line()), trimmed);
      }
    }

    return Optional.of(this.lexer.lex(trimmed));
  }

  protected abstract Logger log();

  protected abstract String lineNextRaw()
    throws IOException;
}
