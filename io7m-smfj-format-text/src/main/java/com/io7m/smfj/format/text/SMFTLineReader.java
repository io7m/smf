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

import com.io7m.jlexing.core.LexicalPositionMutable;
import com.io7m.jnull.NullCheck;
import javaslang.collection.List;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.Optional;

final class SMFTLineReader
{
  private final BufferedReader reader;
  private final LexicalPositionMutable<Path> position;
  private final SMFLineLexer lexer;

  LexicalPositionMutable<Path> position()
  {
    return this.position;
  }

  SMFTLineReader(
    final Path in_path,
    final InputStream in_stream)
  {
    this.reader = new BufferedReader(
      new InputStreamReader(
        NullCheck.notNull(in_stream, "stream"), StandardCharsets.UTF_8));
    this.position = LexicalPositionMutable.create(0, 0, Optional.of(in_path));
    this.lexer = new SMFLineLexer();
  }

  public Optional<List<String>> line()
    throws Exception
  {
    final String line = this.reader.readLine();
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

    return Optional.of(this.lexer.lex(trimmed));
  }
}
