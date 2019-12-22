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

package com.io7m.smfj.tests.probe;

import com.io7m.smfj.core.SMFPartialLogged;
import com.io7m.smfj.format.binary2.SMFFormatBinary2;
import com.io7m.smfj.format.text.SMFFormatText;
import com.io7m.smfj.probe.api.SMFVersionProbeControllerOSGi;
import com.io7m.smfj.probe.api.SMFVersionProbed;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import org.apache.commons.io.input.BrokenInputStream;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class SMFVersionProbeControllerOSGiTest
{
  private static final Logger LOG =
    LoggerFactory.getLogger(SMFVersionProbeControllerOSGiTest.class);

  private static InputStream resource(
    final String name)
  {
    return SMFVersionProbeControllerOSGiTest.class.getResourceAsStream(name);
  }

  private static void dumpValidation(
    final SMFPartialLogged<?> r)
  {
    r.errors().forEach(c -> LOG.error("{}", c));
    r.warnings().forEach(c -> LOG.warn("{}", c));

    if (r.isSucceeded()) {
      LOG.debug("{}", r.get());
    }
  }

  @Test
  public void testEmptyFile()
  {
    final SMFVersionProbeControllerOSGi c =
      new SMFVersionProbeControllerOSGi();
    c.onProbeProviderAdd(new SMFFormatText());
    c.onProbeProviderAdd(new SMFFormatBinary2());

    final var r = c.probe(() -> new ByteArrayInputStream(new byte[0]));

    dumpValidation(r);
    Assertions.assertTrue(r.isFailed());
    Assertions.assertTrue(r.errors().size() >= 1);
  }

  @Test
  public void testOneText()
  {
    final SMFVersionProbeControllerOSGi c =
      new SMFVersionProbeControllerOSGi();
    c.onProbeProviderAdd(new SMFFormatText());
    c.onProbeProviderAdd(new SMFFormatBinary2());

    final var r =
      c.probe(() -> resource("/com/io7m/smfj/tests/probe/one.smft"));

    dumpValidation(r);
    Assertions.assertTrue(r.isSucceeded());

    final SMFVersionProbed v = r.get();
    Assertions.assertEquals(1L, v.version().major());
    Assertions.assertEquals(0L, v.version().minor());
  }

  @Test
  public void testBadText()
  {
    final SMFVersionProbeControllerOSGi c =
      new SMFVersionProbeControllerOSGi();
    c.onProbeProviderAdd(new SMFFormatText());
    c.onProbeProviderAdd(new SMFFormatBinary2());

    final var r =
      c.probe(() -> resource("/com/io7m/smfj/tests/probe/bad.smft"));

    dumpValidation(r);
    Assertions.assertTrue(r.isFailed());
    Assertions.assertTrue(r.errors().size() >= 1);
  }

  @Test
  public void testOneBinary()
  {
    final SMFVersionProbeControllerOSGi c =
      new SMFVersionProbeControllerOSGi();
    c.onProbeProviderAdd(new SMFFormatText());
    c.onProbeProviderAdd(new SMFFormatBinary2());

    final var r =
      c.probe(() -> resource("/com/io7m/smfj/tests/probe/smfFull_validBasic0.smfb"));

    dumpValidation(r);
    Assertions.assertTrue(r.isSucceeded());

    final SMFVersionProbed v = r.get();
    Assertions.assertEquals(2L, v.version().major());
    Assertions.assertEquals(0L, v.version().minor());
  }

  @Test
  public void testBadBinary()
  {
    final SMFVersionProbeControllerOSGi c =
      new SMFVersionProbeControllerOSGi();
    c.onProbeProviderAdd(new SMFFormatText());
    c.onProbeProviderAdd(new SMFFormatBinary2());

    final var r =
      c.probe(() -> resource("/com/io7m/smfj/tests/probe/bad.smfb"));

    dumpValidation(r);
    Assertions.assertTrue(r.isFailed());
    Assertions.assertTrue(r.errors().size() >= 1);
  }

  @Test
  public void testGarbage()
  {
    final SMFVersionProbeControllerOSGi c =
      new SMFVersionProbeControllerOSGi();
    c.onProbeProviderAdd(new SMFFormatText());
    c.onProbeProviderAdd(new SMFFormatBinary2());

    final var r =
      c.probe(() -> resource("/com/io7m/smfj/tests/probe/garbage.smfb"));

    dumpValidation(r);
    Assertions.assertTrue(r.isFailed());
    Assertions.assertTrue(r.errors().size() >= 1);
  }

  @Test
  public void testFailingStreams()
  {
    final SMFVersionProbeControllerOSGi c =
      new SMFVersionProbeControllerOSGi();
    c.onProbeProviderAdd(new SMFFormatText());
    c.onProbeProviderAdd(new SMFFormatBinary2());

    final var r =
      c.probe(() -> new BrokenInputStream());

    dumpValidation(r);
    Assertions.assertTrue(r.isFailed());
    Assertions.assertTrue(r.errors().size() >= 1);
  }
}
