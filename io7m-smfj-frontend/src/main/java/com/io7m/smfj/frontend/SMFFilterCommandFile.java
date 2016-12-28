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

package com.io7m.smfj.frontend;

import com.io7m.jlexing.core.LexicalPosition;
import com.io7m.jnull.NullCheck;
import com.io7m.smfj.format.text.SMFLineLexer;
import com.io7m.smfj.parser.api.SMFParseError;
import com.io7m.smfj.processing.SMFFilterCommands;
import com.io7m.smfj.processing.SMFMemoryMeshFilterType;
import javaslang.collection.List;
import javaslang.control.Validation;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.Optional;

/**
 * Convenient functions to parse sequences of filter commands from files.
 */

public final class SMFFilterCommandFile
{
  private SMFFilterCommandFile()
  {

  }

  /**
   * Parse a command file.
   *
   * @param path_opt The path for error reporting
   * @param stream   An input stream
   *
   * @return A sequence of filters, or a list of errors encountered during
   * parsing
   *
   * @throws IOException On I/O errors
   */

  public static Validation<List<SMFParseError>, List<SMFMemoryMeshFilterType>> parseFromStream(
    final Optional<Path> path_opt,
    final InputStream stream)
    throws IOException
  {
    NullCheck.notNull(path_opt, "Path");
    NullCheck.notNull(stream, "Stream");

    final SMFLineLexer lexer = new SMFLineLexer();

    List<SMFParseError> errors = List.empty();
    List<SMFMemoryMeshFilterType> filters = List.empty();
    LexicalPosition<Path> position = LexicalPosition.of(1, 0, path_opt);

    try (final BufferedReader reader =
           new BufferedReader(new InputStreamReader(
             stream,
             StandardCharsets.UTF_8))) {
      while (true) {
        final String line = reader.readLine();
        if (line == null) {
          break;
        }
        final List<String> text = lexer.lex(line);
        if (text.isEmpty()) {
          continue;
        }

        final Validation<List<SMFParseError>, SMFMemoryMeshFilterType> result =
          SMFFilterCommands.parse(position.file(), position.line(), text);

        if (result.isValid()) {
          filters = filters.append(result.get());
        } else {
          errors = errors.appendAll(result.getError());
        }
        position = position.withLine(position.line() + 1).withColumn(0);
      }
    }

    if (errors.isEmpty()) {
      return Validation.valid(filters);
    }
    return Validation.invalid(errors);
  }
}
