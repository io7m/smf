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

package com.io7m.smfj.tests.format.binary;

import com.io7m.smfj.format.binary.SMFFormatBinary;
import com.io7m.smfj.parser.api.SMFParserEventsType;
import com.io7m.smfj.parser.api.SMFParserSequentialType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.NoSuchFileException;

public final class SMFFormatBinaryTest extends SMFFormatBinaryContract
{
  private static URI uri(
    final String name)
    throws NoSuchFileException
  {
    final URL url = SMFFormatBinaryContract.class.getResource(name);
    if (url == null) {
      throw new NoSuchFileException(name);
    }

    try {
      return url.toURI();
    } catch (final URISyntaxException e) {
      throw new IllegalArgumentException(e);
    }
  }

  @Override
  protected Logger logger()
  {
    return LoggerFactory.getLogger(SMFFormatBinaryTest.class);
  }

  @Override
  protected SMFParserSequentialType createParser(
    final String name,
    final SMFParserEventsType events)
    throws Exception
  {
    final URI uri = uri(name);
    final InputStream stream = uri.toURL().openStream();
    return new SMFFormatBinary().parserCreateSequential(events, uri, stream);
  }
}
