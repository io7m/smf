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

package com.io7m.smfj.tests.processing;

import com.io7m.smfj.format.text.SMFFormatText;
import com.io7m.smfj.parser.api.SMFParserEventsType;
import com.io7m.smfj.parser.api.SMFParserProviderType;
import com.io7m.smfj.parser.api.SMFParserSequentialType;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;

public final class SMFTestFiles
{
  private SMFTestFiles()
  {

  }

  static InputStream resourceStream(
    final String name)
  {
    final String rpath = "/com/io7m/smfj/tests/processing/" + name;
    return SMFMemoryMeshProducerTest.class.getResourceAsStream(rpath);
  }

  static SMFParserSequentialType createParser(
    final SMFParserEventsType loader,
    final String name)
    throws IOException
  {
    try (var stream = resourceStream(name)) {
      final SMFParserProviderType fmt = new SMFFormatText();
      final String rpath = "/com/io7m/smfj/tests/processing/" + name;
      final Path path = Paths.get(rpath);
      final SMFParserSequentialType parser =
        fmt.parserCreateSequential(loader, path.toUri(), stream);
      parser.parse();
      return parser;
    }
  }
}
