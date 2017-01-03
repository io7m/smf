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
import com.io7m.smfj.core.SMFAttributeName;
import com.io7m.smfj.core.SMFComponentType;
import com.io7m.smfj.parser.api.SMFParseError;
import com.io7m.smfj.parser.api.SMFParserSequentialType;
import com.io7m.smfj.processing.api.SMFMemoryMesh;
import com.io7m.smfj.processing.api.SMFMemoryMeshFilterType;
import com.io7m.smfj.processing.api.SMFMemoryMeshProducer;
import com.io7m.smfj.processing.api.SMFMemoryMeshProducerType;
import com.io7m.smfj.processing.api.SMFProcessingError;
import com.io7m.smfj.processing.main.SMFMemoryMeshFilterCheck;
import com.io7m.smfj.processing.main.SMFMemoryMeshFilterCheckConfiguration;
import javaslang.collection.List;
import javaslang.control.Validation;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;
import java.util.OptionalInt;

public final class SMFMemoryMeshFilterCheckTest
{
  private static final Logger LOG;

  static {
    LOG = LoggerFactory.getLogger(SMFMemoryMeshFilterCheckTest.class);
  }

  @Test
  public void testParseWrong1()
  {
    final Validation<List<SMFParseError>, SMFMemoryMeshFilterType> r =
      SMFMemoryMeshFilterCheck.parse(
        Optional.empty(),
        1,
        List.of());
    Assert.assertTrue(r.isInvalid());
  }

  @Test
  public void testParseWrong2()
  {
    final Validation<List<SMFParseError>, SMFMemoryMeshFilterType> r =
      SMFMemoryMeshFilterCheck.parse(
        Optional.empty(),
        1,
        List.of("x", "<#@"));
    Assert.assertTrue(r.isInvalid());
  }

  @Test
  public void testParseWrong3()
  {
    final Validation<List<SMFParseError>, SMFMemoryMeshFilterType> r =
      SMFMemoryMeshFilterCheck.parse(
        Optional.empty(),
        1,
        List.of("<#@", "y"));
    Assert.assertTrue(r.isInvalid());
  }

  @Test
  public void testParseWrong4()
  {
    final Validation<List<SMFParseError>, SMFMemoryMeshFilterType> r =
      SMFMemoryMeshFilterCheck.parse(
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
      SMFMemoryMeshFilterCheck.parse(
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
      SMFMemoryMeshFilterCheck.parse(
        Optional.empty(),
        1,
        List.of("x", "float", "4", "32"));
    Assert.assertTrue(r.isValid());
    final SMFMemoryMeshFilterType c = r.get();
    Assert.assertEquals(c.name(), "check");
  }

  @Test
  public void testParseOk1()
  {
    final Validation<List<SMFParseError>, SMFMemoryMeshFilterType> r =
      SMFMemoryMeshFilterCheck.parse(
        Optional.empty(),
        1,
        List.of("x", "-", "-", "-"));
    Assert.assertTrue(r.isValid());
    final SMFMemoryMeshFilterType c = r.get();
    Assert.assertEquals(c.name(), "check");
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

    final SMFMemoryMeshFilterType filter =
      SMFMemoryMeshFilterCheck.create(
        SMFMemoryMeshFilterCheckConfiguration.builder()
          .setName(SMFAttributeName.of("f16_4"))
          .setComponentType(SMFComponentType.ELEMENT_TYPE_FLOATING)
          .setComponentSize(16)
          .setComponentCount(4)
          .build());

    final Validation<List<SMFProcessingError>, SMFMemoryMesh> r =
      filter.filter(loader.mesh());
    Assert.assertTrue(r.isValid());
    Assert.assertEquals(loader.mesh(), r.get());
  }

  @Test
  public void testCheckSizeWrong()
    throws Exception
  {
    final SMFMemoryMeshProducerType loader = SMFMemoryMeshProducer.create();

    try (final SMFParserSequentialType parser =
           SMFTestFiles.createParser(loader, "all.smft")) {
      // Nothing
    }

    final SMFMemoryMeshFilterType filter =
      SMFMemoryMeshFilterCheck.create(
        SMFMemoryMeshFilterCheckConfiguration.builder()
          .setName(SMFAttributeName.of("f16_4"))
          .setComponentType(SMFComponentType.ELEMENT_TYPE_FLOATING)
          .setComponentSize(8)
          .setComponentCount(4)
          .build());

    final Validation<List<SMFProcessingError>, SMFMemoryMesh> r =
      filter.filter(loader.mesh());
    Assert.assertTrue(r.isInvalid());

    r.getError().map(e -> {
      LOG.error("error: {}", e.message());
      return Unit.unit();
    });
  }

  @Test
  public void testCheckCountWrong()
    throws Exception
  {
    final SMFMemoryMeshProducerType loader = SMFMemoryMeshProducer.create();

    try (final SMFParserSequentialType parser =
           SMFTestFiles.createParser(loader, "all.smft")) {
      // Nothing
    }

    final SMFMemoryMeshFilterType filter =
      SMFMemoryMeshFilterCheck.create(
        SMFMemoryMeshFilterCheckConfiguration.builder()
          .setName(SMFAttributeName.of("f16_4"))
          .setComponentType(SMFComponentType.ELEMENT_TYPE_FLOATING)
          .setComponentSize(16)
          .setComponentCount(3)
          .build());

    final Validation<List<SMFProcessingError>, SMFMemoryMesh> r =
      filter.filter(loader.mesh());
    Assert.assertTrue(r.isInvalid());

    r.getError().map(e -> {
      LOG.error("error: {}", e.message());
      return Unit.unit();
    });
  }

  @Test
  public void testCheckTypeWrong()
    throws Exception
  {
    final SMFMemoryMeshProducerType loader = SMFMemoryMeshProducer.create();

    try (final SMFParserSequentialType parser =
           SMFTestFiles.createParser(loader, "all.smft")) {
      // Nothing
    }

    final SMFMemoryMeshFilterType filter =
      SMFMemoryMeshFilterCheck.create(
        SMFMemoryMeshFilterCheckConfiguration.builder()
          .setName(SMFAttributeName.of("f16_4"))
          .setComponentType(SMFComponentType.ELEMENT_TYPE_INTEGER_SIGNED)
          .setComponentSize(16)
          .setComponentCount(4)
          .build());

    final Validation<List<SMFProcessingError>, SMFMemoryMesh> r =
      filter.filter(loader.mesh());
    Assert.assertTrue(r.isInvalid());

    r.getError().map(e -> {
      LOG.error("error: {}", e.message());
      return Unit.unit();
    });
  }

  @Test
  public void testCheckExists()
    throws Exception
  {
    final SMFMemoryMeshProducerType loader = SMFMemoryMeshProducer.create();

    try (final SMFParserSequentialType parser =
           SMFTestFiles.createParser(loader, "all.smft")) {
      // Nothing
    }

    final SMFMemoryMeshFilterType filter =
      SMFMemoryMeshFilterCheck.create(
        SMFMemoryMeshFilterCheckConfiguration.builder()
          .setName(SMFAttributeName.of("f16_4"))
          .setComponentType(Optional.empty())
          .setComponentSize(OptionalInt.empty())
          .setComponentCount(OptionalInt.empty())
          .build());

    final Validation<List<SMFProcessingError>, SMFMemoryMesh> r =
      filter.filter(loader.mesh());
    Assert.assertTrue(r.isValid());
  }

  @Test
  public void testCheckNonexistent()
    throws Exception
  {
    final SMFMemoryMeshProducerType loader = SMFMemoryMeshProducer.create();

    try (final SMFParserSequentialType parser =
           SMFTestFiles.createParser(loader, "all.smft")) {
      // Nothing
    }

    final SMFMemoryMeshFilterType filter =
      SMFMemoryMeshFilterCheck.create(
        SMFMemoryMeshFilterCheckConfiguration.builder()
          .setName(SMFAttributeName.of("nonexistent"))
          .setComponentType(Optional.empty())
          .setComponentSize(OptionalInt.empty())
          .setComponentCount(OptionalInt.empty())
          .build());

    final Validation<List<SMFProcessingError>, SMFMemoryMesh> r =
      filter.filter(loader.mesh());
    Assert.assertTrue(r.isInvalid());

    r.getError().map(e -> {
      LOG.error("error: {}", e.message());
      return Unit.unit();
    });
  }
}
