/*
 * Copyright © 2019 Mark Raynsford <code@io7m.com> http://io7m.com
 *
 * Permission to use, copy, modify, and/or distribute this software for
 * any purpose with or without fee is hereby granted, provided that the
 * above copyright notice and this permission notice appear in all copies.
 *
 * THE SOFTWARE IS PROVIDED "AS IS" AND THE AUTHOR DISCLAIMS ALL
 * WARRANTIES WITH REGARD TO THIS SOFTWARE INCLUDING ALL IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS. IN NO EVENT SHALL THE AUTHOR
 * BE LIABLE FOR ANY SPECIAL, DIRECT, INDIRECT, OR CONSEQUENTIAL DAMAGES
 * OR ANY DAMAGES WHATSOEVER RESULTING FROM LOSS OF USE, DATA OR PROFITS,
 * WHETHER IN AN ACTION OF CONTRACT, NEGLIGENCE OR OTHER TORTIOUS ACTION,
 * ARISING OUT OF OR IN CONNECTION WITH THE USE OR PERFORMANCE OF THIS
 * SOFTWARE.
 */


package com.io7m.smfj.tests.format.binary2;

import com.io7m.smfj.format.binary2.SMFFormatBinary2;
import com.io7m.smfj.format.xml.SMFFormatXML;
import com.io7m.smfj.processing.api.SMFMemoryMeshProducer;
import com.io7m.smfj.processing.api.SMFMemoryMeshProducerType;
import com.io7m.smfj.tests.processing.SMFMemoryMeshTesting;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class SMFFormatBinary2Test
{
  private static final Logger LOG =
    LoggerFactory.getLogger(SMFFormatBinary2Test.class);

  private static final URI TEST = URI.create("urn:test");

  private static InputStream resource(
    final String name)
    throws IOException
  {
    final var path = String.format("/com/io7m/smfj/tests/format/binary2/%s", name);
    final var url = SMFFormatBinary2Test.class.getResource(path);
    if (url == null) {
      throw new FileNotFoundException(path);
    }
    return url.openStream();
  }

  private static void logEverything(final SMFMemoryMeshProducerType meshes)
  {
    meshes.errors()
      .forEach(e -> LOG.error("{}: ", e, e.exception().orElse(null)));
    meshes.warnings()
      .forEach(e -> LOG.warn("{}: ", e, e.exception().orElse(null)));
  }

  private static void checkFailure(final String file)
    throws IOException
  {
    final var format = new SMFFormatBinary2();
    final var meshes = SMFMemoryMeshProducer.create();

    try (var stream = resource(file)) {
      try (var parser = format.parserCreateSequential(meshes, TEST, stream)) {
        parser.parse();
      }
    }
    logEverything(meshes);

    Assertions.assertFalse(
      meshes.errors().isEmpty(),
      "At least one error must have been logged");
  }

  @Test
  public void testInvalid0()
    throws Exception
  {
    checkFailure("smfFull_invalidSMFHeader0.smfb");
  }

  @Test
  public void testInvalid1()
    throws Exception
  {
    checkFailure("smfFull_invalidSMFHeader1.smfb");
  }

  @Test
  public void testInvalid2()
    throws Exception
  {
    checkFailure("smfFull_invalidSMFHeader2.smfb");
  }

  @Test
  public void testInvalid3()
    throws Exception
  {
    checkFailure("smfFull_invalidTooFewTriangles0.smfb");
  }

  @Test
  public void testInvalid4()
    throws Exception
  {
    checkFailure("smfFull_invalidTrianglesOutOfRange0.smfb");
  }
}
