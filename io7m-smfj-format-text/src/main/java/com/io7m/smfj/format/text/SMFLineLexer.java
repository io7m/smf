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

import com.io7m.jaffirm.core.Invariants;
import javaslang.collection.List;

import java.util.ArrayList;

/**
 * A mindlessly trivial line lexer.
 */

public final class SMFLineLexer
{
  private final java.util.List<String> tokens;
  private final StringBuilder buffer;

  /**
   * Create a new lexer.
   */

  public SMFLineLexer()
  {
    this.tokens = new ArrayList<>(8);
    this.buffer = new StringBuilder(32);
  }

  /**
   * Lex the given line into tokens.
   * @param line The line
   * @return A list of tokens
   */

  public List<String> lex(
    final String line)
  {
    try {
      State state = State.STATE_INITIAL;

      int index = 0;
      while (index < line.length()) {
        final int code = line.codePointAt(index);

        switch (state) {
          case STATE_INITIAL: {
            if (Character.isSpaceChar(code)) {
              Invariants.checkInvariant(
                this.buffer.length() == 0, "Buffer is empty");
              break;
            }

            if (code == '"') {
              Invariants.checkInvariant(
                this.buffer.length() == 0, "Buffer is empty");
              state = State.STATE_IN_QUOTE;
              break;
            }

            state = State.STATE_IN_WORD;
            this.buffer.appendCodePoint(code);
            break;
          }

          case STATE_IN_WORD: {
            if (Character.isSpaceChar(code)) {
              Invariants.checkInvariant(
                this.buffer.length() != 0,
                "Buffer is not empty");
              state = State.STATE_INITIAL;
              this.tokens.add(this.buffer.toString());
              this.buffer.setLength(0);
              break;
            }

            if (code == '"') {
              Invariants.checkInvariant(
                this.buffer.length() != 0,
                "Buffer is not empty");
              state = State.STATE_IN_QUOTE;
              this.tokens.add(this.buffer.toString());
              this.buffer.setLength(0);
              break;
            }

            this.buffer.appendCodePoint(code);
            break;
          }

          case STATE_IN_QUOTE: {
            if (code == '\\') {
              state = State.STATE_IN_QUOTE_ESCAPE;
              break;
            }

            if (code == '"') {
              state = State.STATE_INITIAL;
              this.tokens.add(this.buffer.toString());
              this.buffer.setLength(0);
              break;
            }

            this.buffer.appendCodePoint(code);
            break;
          }

          case STATE_IN_QUOTE_ESCAPE: {
            state = State.STATE_IN_QUOTE;
            this.buffer.appendCodePoint(code);
            break;
          }
        }

        index += Character.charCount(code);
      }

      if (this.buffer.length() > 0) {
        this.tokens.add(this.buffer.toString());
      }

      return List.ofAll(this.tokens);
    } finally {
      this.tokens.clear();
      this.buffer.setLength(0);
    }
  }

  private enum State
  {
    STATE_INITIAL,
    STATE_IN_WORD,
    STATE_IN_QUOTE,
    STATE_IN_QUOTE_ESCAPE
  }
}
