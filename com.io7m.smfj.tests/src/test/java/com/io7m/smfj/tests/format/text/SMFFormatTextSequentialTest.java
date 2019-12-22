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

package com.io7m.smfj.tests.format.text;

import com.io7m.smfj.format.text.SMFFormatText;
import com.io7m.smfj.parser.api.SMFParserEventsType;
import com.io7m.smfj.parser.api.SMFParserSequentialType;
import com.io7m.smfj.processing.api.SMFMemoryMeshProducer;
import com.io7m.smfj.tests.parser.api.SMFParserSequentialTextContract;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class SMFFormatTextSequentialTest
  extends SMFParserSequentialTextContract
{
  private static final Logger LOG =
    LoggerFactory.getLogger(SMFFormatTextSequentialTest.class);

  @Override
  protected SMFParserSequentialType parser(
    final SMFParserEventsType events,
    final URI uri,
    final InputStream stream)
  {
    return new SMFFormatText().parserCreateSequential(events, uri, stream);
  }

  @Test
  public void testNoWarningsEnd()
    throws Exception
  {
    final var memoryMesh = SMFMemoryMeshProducer.create();

    try (var parser = this.parser(
      memoryMesh,
      URI.create("urn:file"),
      resource("no_warnings.smft"))) {
      parser.parse();

      memoryMesh.warnings().forEach(
        e -> LOG.warn("{}: ", e.fullMessage(), e.exception().orElse(null)));
      memoryMesh.errors().forEach(
        e -> LOG.error("{}: ", e.fullMessage(), e.exception().orElse(null)));
    }
  }

  private static InputStream resource(
    final String name)
    throws IOException
  {
    final var path = String.format("/com/io7m/smfj/tests/format/text/%s", name);
    final var url = SMFFormatTextSequentialTest.class.getResource(path);
    if (url == null) {
      throw new FileNotFoundException(path);
    }
    return url.openStream();
  }
}
