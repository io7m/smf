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

import com.io7m.smfj.format.binary.SMFFormatBinary;
import com.io7m.smfj.format.text.SMFFormatText;
import com.io7m.smfj.parser.api.SMFParseError;
import com.io7m.smfj.probe.api.SMFVersionProbeControllerOSGi;
import com.io7m.smfj.probe.api.SMFVersionProbed;
import io.vavr.collection.Seq;
import io.vavr.control.Validation;
import org.apache.commons.io.input.BrokenInputStream;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

public final class SMFVersionProbeControllerOSGiTest
{
  private static final Logger LOG;

  static {
    LOG = LoggerFactory.getLogger(SMFVersionProbeControllerOSGiTest.class);
  }

  private static InputStream resource(
    final String name)
  {
    return SMFVersionProbeControllerOSGiTest.class.getResourceAsStream(name);
  }

  private static void dumpValidation(
    final Validation<Seq<SMFParseError>, SMFVersionProbed> r)
  {
    if (r.isValid()) {
      LOG.debug("{}", r.get());
    } else {
      r.getError().forEach(c -> LOG.error("{}", c));
    }
  }

  @Test
  public void testEmptyFile()
  {
    final SMFVersionProbeControllerOSGi c =
      new SMFVersionProbeControllerOSGi();
    c.onProbeProviderAdd(new SMFFormatText());
    c.onProbeProviderAdd(new SMFFormatBinary());

    final Validation<Seq<SMFParseError>, SMFVersionProbed> r =
      c.probe(() -> new ByteArrayInputStream(new byte[0]));

    dumpValidation(r);
    Assert.assertTrue(r.isInvalid());
    Assert.assertTrue(r.getError().size() >= 1);
  }

  @Test
  public void testOneText()
  {
    final SMFVersionProbeControllerOSGi c =
      new SMFVersionProbeControllerOSGi();
    c.onProbeProviderAdd(new SMFFormatText());
    c.onProbeProviderAdd(new SMFFormatBinary());

    final Validation<Seq<SMFParseError>, SMFVersionProbed> r =
      c.probe(() -> resource("/com/io7m/smfj/tests/probe/one.smft"));

    dumpValidation(r);
    Assert.assertTrue(r.isValid());

    final SMFVersionProbed v = r.get();
    Assert.assertEquals(1L, (long) v.version().major());
    Assert.assertEquals(0L, (long) v.version().minor());
  }

  @Test
  public void testBadText()
  {
    final SMFVersionProbeControllerOSGi c =
      new SMFVersionProbeControllerOSGi();
    c.onProbeProviderAdd(new SMFFormatText());
    c.onProbeProviderAdd(new SMFFormatBinary());

    final Validation<Seq<SMFParseError>, SMFVersionProbed> r =
      c.probe(() -> resource("/com/io7m/smfj/tests/probe/bad.smft"));

    dumpValidation(r);
    Assert.assertTrue(r.isInvalid());
    Assert.assertTrue(r.getError().size() >= 1);
  }

  @Test
  public void testOneBinary()
  {
    final SMFVersionProbeControllerOSGi c =
      new SMFVersionProbeControllerOSGi();
    c.onProbeProviderAdd(new SMFFormatText());
    c.onProbeProviderAdd(new SMFFormatBinary());

    final Validation<Seq<SMFParseError>, SMFVersionProbed> r =
      c.probe(() -> resource("/com/io7m/smfj/tests/probe/one.smfb"));

    dumpValidation(r);
    Assert.assertTrue(r.isValid());

    final SMFVersionProbed v = r.get();
    Assert.assertEquals(1L, (long) v.version().major());
    Assert.assertEquals(0L, (long) v.version().minor());
  }

  @Test
  public void testBadBinary()
  {
    final SMFVersionProbeControllerOSGi c =
      new SMFVersionProbeControllerOSGi();
    c.onProbeProviderAdd(new SMFFormatText());
    c.onProbeProviderAdd(new SMFFormatBinary());

    final Validation<Seq<SMFParseError>, SMFVersionProbed> r =
      c.probe(() -> resource("/com/io7m/smfj/tests/probe/bad.smfb"));

    dumpValidation(r);
    Assert.assertTrue(r.isInvalid());
    Assert.assertTrue(r.getError().size() >= 1);
  }

  @Test
  public void testGarbage()
  {
    final SMFVersionProbeControllerOSGi c =
      new SMFVersionProbeControllerOSGi();
    c.onProbeProviderAdd(new SMFFormatText());
    c.onProbeProviderAdd(new SMFFormatBinary());

    final Validation<Seq<SMFParseError>, SMFVersionProbed> r =
      c.probe(() -> resource("/com/io7m/smfj/tests/probe/garbage.smfb"));

    dumpValidation(r);
    Assert.assertTrue(r.isInvalid());
    Assert.assertTrue(r.getError().size() >= 1);
  }

  @Test
  public void testFailingStreams()
  {
    final SMFVersionProbeControllerOSGi c =
      new SMFVersionProbeControllerOSGi();
    c.onProbeProviderAdd(new SMFFormatText());
    c.onProbeProviderAdd(new SMFFormatBinary());

    final Validation<Seq<SMFParseError>, SMFVersionProbed> r =
      c.probe(() -> new BrokenInputStream());

    dumpValidation(r);
    Assert.assertTrue(r.isInvalid());
    Assert.assertTrue(r.getError().size() >= 1);
  }
}
