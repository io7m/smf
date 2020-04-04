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

package com.io7m.smfj.tests.parser.api;

import com.io7m.jlexing.core.LexicalPosition;
import com.io7m.smfj.parser.api.SMFParseError;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.Optional;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class SMFParseErrorTest
{
  private static final Logger LOG;

  static {
    LOG = LoggerFactory.getLogger(SMFParseErrorTest.class);
  }

  @Test
  public void testFullMessage0()
  {
    final SMFParseError e =
      SMFParseError.of(
        LexicalPosition.of(
          23,
          127,
          Optional.of(Paths.get("/x/y.txt").toUri())),
        "Failed",
        Optional.empty());

    LOG.error(e.fullMessage());
    Assertions.assertEquals("file:///x/y.txt:23:127: Failed", e.fullMessage());
  }

  @Test
  public void testFullMessage1()
  {
    final SMFParseError e =
      SMFParseError.of(
        LexicalPosition.of(
          23,
          127,
          Optional.of(Paths.get("/x/y.txt").toUri())),
        "Failed",
        Optional.of(new IOException("Printer on fire")));

    LOG.error(e.fullMessage());
    Assertions.assertEquals(
      "file:///x/y.txt:23:127: Failed (java.io.IOException: Printer on fire)",
      e.fullMessage());
  }

  @Test
  public void testFullMessage2()
  {
    final SMFParseError e =
      SMFParseError.of(
        LexicalPosition.of(
          23,
          127,
          Optional.empty()),
        "Failed",
        Optional.of(new IOException("Printer on fire")));

    LOG.error(e.fullMessage());
    Assertions.assertEquals(
      "23:127: Failed (java.io.IOException: Printer on fire)",
      e.fullMessage());
  }

  @Test
  public void testFullMessage3()
  {
    final SMFParseError e =
      SMFParseError.of(
        LexicalPosition.of(
          23,
          127,
          Optional.empty()),
        "Failed",
        Optional.empty());

    LOG.error(e.fullMessage());
    Assertions.assertEquals("23:127: Failed", e.fullMessage());
  }
}
