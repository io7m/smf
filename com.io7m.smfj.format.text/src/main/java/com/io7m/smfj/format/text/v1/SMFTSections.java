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

package com.io7m.smfj.format.text.v1;

import com.io7m.junreachable.UnreachableCodeException;
import com.io7m.smfj.format.text.SMFTLineReaderType;
import com.io7m.smfj.format.text.SMFTParsingStatus;
import com.io7m.smfj.parser.api.SMFParseError;
import com.io7m.smfj.parser.api.SMFParserEventsErrorType;
import java.io.IOException;
import java.util.List;
import java.util.Optional;

import static com.io7m.smfj.format.text.SMFTParsingStatus.FAILURE;
import static com.io7m.smfj.format.text.SMFTParsingStatus.SUCCESS;

final class SMFTSections
{
  private SMFTSections()
  {
    throw new UnreachableCodeException();
  }

  static SMFTParsingStatus skipUntilEnd(
    final SMFTLineReaderType reader,
    final SMFParserEventsErrorType receiver)
    throws IOException
  {
    while (true) {
      final Optional<List<String>> line_opt = reader.line();
      if (!line_opt.isPresent()) {
        receiver.onError(SMFParseError.of(
          reader.position(),
          "Unexpected EOF",
          Optional.empty()));
        return FAILURE;
      }

      final List<String> line = line_opt.get();
      if (line.isEmpty()) {
        continue;
      }

      final String command_name = line.get(0);
      switch (command_name) {
        case "end": {
          if (line.size() == 1) {
            return SUCCESS;
          }

          receiver.onError(SMFTErrors.errorMalformedCommand(
            "end", "end", line, reader.position()));
          return FAILURE;
        }
        default: {
          continue;
        }
      }
    }
  }
}
