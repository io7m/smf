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

import com.io7m.jfunctional.Unit;
import com.io7m.smfj.core.SMFSchemaIdentifier;
import com.io7m.smfj.parser.api.SMFParseError;
import com.io7m.smfj.parser.api.SMFParserSequentialType;
import com.io7m.smfj.processing.api.SMFMemoryMesh;
import com.io7m.smfj.processing.api.SMFMemoryMeshFilterType;
import com.io7m.smfj.processing.api.SMFMemoryMeshProducer;
import com.io7m.smfj.processing.api.SMFMemoryMeshProducerType;
import com.io7m.smfj.processing.api.SMFProcessingError;
import com.io7m.smfj.processing.main.SMFMemoryMeshFilterSchemaCheck;
import javaslang.collection.List;
import javaslang.control.Validation;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.FileSystem;
import java.util.Optional;

public final class SMFMemoryMeshFilterSchemaCheckTest extends
  SMFMemoryMeshFilterContract
{
  private static final Logger LOG;

  static {
    LOG = LoggerFactory.getLogger(SMFMemoryMeshFilterSchemaCheckTest.class);
  }

  private FileSystem filesystem;

  @Test
  public void testParseWrong1()
  {
    final Validation<List<SMFParseError>, SMFMemoryMeshFilterType> r =
      SMFMemoryMeshFilterSchemaCheck.parse(
        Optional.empty(),
        1,
        List.of());
    Assert.assertTrue(r.isInvalid());
  }

  @Test
  public void testParseWrong2()
  {
    final Validation<List<SMFParseError>, SMFMemoryMeshFilterType> r =
      SMFMemoryMeshFilterSchemaCheck.parse(
        Optional.empty(),
        1,
        List.of("x", "<#@"));
    Assert.assertTrue(r.isInvalid());
  }

  @Test
  public void testParseWrong3()
  {
    final Validation<List<SMFParseError>, SMFMemoryMeshFilterType> r =
      SMFMemoryMeshFilterSchemaCheck.parse(
        Optional.empty(),
        1,
        List.of("<#@", "y"));
    Assert.assertTrue(r.isInvalid());
  }

  @Test
  public void testParseWrong4()
  {
    final Validation<List<SMFParseError>, SMFMemoryMeshFilterType> r =
      SMFMemoryMeshFilterSchemaCheck.parse(
        Optional.empty(),
        1,
        List.of(
          "x",
          "y",
          "z"));
    Assert.assertTrue(r.isInvalid());
  }

  @Test
  public void testParseWrong5()
  {
    final Validation<List<SMFParseError>, SMFMemoryMeshFilterType> r =
      SMFMemoryMeshFilterSchemaCheck.parse(
        Optional.empty(),
        1,
        List.of(
          "x",
          "float",
          "z",
          "32"));
    Assert.assertTrue(r.isInvalid());
  }

  @Test
  public void testParseOk0()
  {
    final Validation<List<SMFParseError>, SMFMemoryMeshFilterType> r =
      SMFMemoryMeshFilterSchemaCheck.parse(
        Optional.empty(),
        1,
        List.of("696f376d", "0", "1", "2"));
    Assert.assertTrue(r.isValid());
    final SMFMemoryMeshFilterType c = r.get();
    Assert.assertEquals(c.name(), "schema-check");
  }

  @Test
  public void testCheckOK()
    throws Exception
  {
    final SMFMemoryMeshProducerType loader = SMFMemoryMeshProducer.create();

    try (final SMFParserSequentialType parser =
           SMFTestFiles.createParser(loader, "all.smft")) {
      // Nothing
    }

    final SMFSchemaIdentifier identifier =
      SMFSchemaIdentifier.builder()
        .setVendorID(0x696f376d)
        .setSchemaID(0)
        .setSchemaMajorVersion(1)
        .setSchemaMinorVersion(2)
        .build();

    final SMFMemoryMeshFilterType filter =
      SMFMemoryMeshFilterSchemaCheck.create(identifier);

    final Validation<List<SMFProcessingError>, SMFMemoryMesh> r =
      filter.filter(this.createContext(), loader.mesh());
    Assert.assertTrue(r.isValid());
    Assert.assertEquals(loader.mesh(), r.get());
  }

  @Test
  public void testCheckFail0()
    throws Exception
  {
    final SMFMemoryMeshProducerType loader = SMFMemoryMeshProducer.create();

    try (final SMFParserSequentialType parser =
           SMFTestFiles.createParser(loader, "all.smft")) {
      // Nothing
    }

    final SMFSchemaIdentifier identifier =
      SMFSchemaIdentifier.builder()
        .setVendorID(0x696f376e)
        .setSchemaID(0)
        .setSchemaMajorVersion(1)
        .setSchemaMinorVersion(2)
        .build();

    final SMFMemoryMeshFilterType filter =
      SMFMemoryMeshFilterSchemaCheck.create(identifier);

    final Validation<List<SMFProcessingError>, SMFMemoryMesh> r =
      filter.filter(this.createContext(), loader.mesh());
    Assert.assertFalse(r.isValid());

    r.getError().map(e -> {
      LOG.error("error: {}", e);
      return Unit.unit();
    });
  }

  @Test
  public void testCheckFail1()
    throws Exception
  {
    final SMFMemoryMeshProducerType loader = SMFMemoryMeshProducer.create();

    try (final SMFParserSequentialType parser =
           SMFTestFiles.createParser(loader, "all.smft")) {
      // Nothing
    }

    final SMFSchemaIdentifier identifier =
      SMFSchemaIdentifier.builder()
        .setVendorID(0x696f376d)
        .setSchemaID(1)
        .setSchemaMajorVersion(1)
        .setSchemaMinorVersion(2)
        .build();

    final SMFMemoryMeshFilterType filter =
      SMFMemoryMeshFilterSchemaCheck.create(identifier);

    final Validation<List<SMFProcessingError>, SMFMemoryMesh> r =
      filter.filter(this.createContext(), loader.mesh());
    Assert.assertFalse(r.isValid());

    r.getError().map(e -> {
      LOG.error("error: {}", e);
      return Unit.unit();
    });
  }

  @Test
  public void testCheckFail2()
    throws Exception
  {
    final SMFMemoryMeshProducerType loader = SMFMemoryMeshProducer.create();

    try (final SMFParserSequentialType parser =
           SMFTestFiles.createParser(loader, "all.smft")) {
      // Nothing
    }

    final SMFSchemaIdentifier identifier =
      SMFSchemaIdentifier.builder()
        .setVendorID(0x696f376d)
        .setSchemaID(0)
        .setSchemaMajorVersion(2)
        .setSchemaMinorVersion(2)
        .build();

    final SMFMemoryMeshFilterType filter =
      SMFMemoryMeshFilterSchemaCheck.create(identifier);

    final Validation<List<SMFProcessingError>, SMFMemoryMesh> r =
      filter.filter(this.createContext(), loader.mesh());
    Assert.assertFalse(r.isValid());

    r.getError().map(e -> {
      LOG.error("error: {}", e);
      return Unit.unit();
    });
  }

  @Test
  public void testCheckFail3()
    throws Exception
  {
    final SMFMemoryMeshProducerType loader = SMFMemoryMeshProducer.create();

    try (final SMFParserSequentialType parser =
           SMFTestFiles.createParser(loader, "all.smft")) {
      // Nothing
    }

    final SMFSchemaIdentifier identifier =
      SMFSchemaIdentifier.builder()
        .setVendorID(0x696f376d)
        .setSchemaID(0)
        .setSchemaMajorVersion(1)
        .setSchemaMinorVersion(3)
        .build();

    final SMFMemoryMeshFilterType filter =
      SMFMemoryMeshFilterSchemaCheck.create(identifier);

    final Validation<List<SMFProcessingError>, SMFMemoryMesh> r =
      filter.filter(this.createContext(), loader.mesh());
    Assert.assertFalse(r.isValid());

    r.getError().map(e -> {
      LOG.error("error: {}", e);
      return Unit.unit();
    });
  }
}
