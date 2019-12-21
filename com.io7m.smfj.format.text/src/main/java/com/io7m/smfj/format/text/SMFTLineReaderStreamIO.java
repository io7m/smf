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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A line reader based on blocking stream IO.
 */

public final class SMFTLineReaderStreamIO extends SMFTLineReaderAbstract
{
  private static final Logger LOG;

  static {
    LOG = LoggerFactory.getLogger(SMFTLineReaderStreamIO.class);
  }

  private final BufferedReader reader;

  private SMFTLineReaderStreamIO(
    final URI in_uri,
    final InputStream in_stream)
  {
    super(in_uri, 1);
    this.reader = new BufferedReader(
      new InputStreamReader(
        Objects.requireNonNull(in_stream, "stream"), StandardCharsets.UTF_8));
  }

  /**
   * Construct a new line reader.
   *
   * @param in_uri    The file URI, for diagnostic messages
   * @param in_stream The input stream
   *
   * @return A line reader
   */

  public static SMFTLineReaderType create(
    final URI in_uri,
    final InputStream in_stream)
  {
    return new SMFTLineReaderStreamIO(in_uri, in_stream);
  }

  @Override
  protected Logger log()
  {
    return LOG;
  }

  @Override
  protected String lineNextRaw()
    throws IOException
  {
    return this.reader.readLine();
  }
}
