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
import com.io7m.jtensors.VectorI3L;
import com.io7m.smfj.parser.api.SMFParseError;
import com.io7m.smfj.parser.api.SMFParserSequentialType;
import com.io7m.smfj.processing.api.SMFFilterCommandContext;
import com.io7m.smfj.processing.api.SMFMemoryMesh;
import com.io7m.smfj.processing.api.SMFMemoryMeshFilterType;
import com.io7m.smfj.processing.api.SMFMemoryMeshProducer;
import com.io7m.smfj.processing.api.SMFMemoryMeshProducerType;
import com.io7m.smfj.processing.api.SMFProcessingError;
import com.io7m.smfj.processing.main.SMFMemoryMeshFilterTrianglesOptimize;
import com.io7m.smfj.processing.main.SMFMemoryMeshFilterTrianglesOptimizeConfiguration;
import javaslang.collection.List;
import javaslang.collection.Vector;
import javaslang.control.Validation;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.FileSystems;
import java.nio.file.Paths;
import java.util.Optional;

public final class SMFMemoryMeshFilterTrianglesOptimizeTest
{
  private static final Logger LOG;

  static {
    LOG = LoggerFactory.getLogger(SMFMemoryMeshFilterTrianglesOptimizeTest.class);
  }

  private static void checkMeshesSame(
    final SMFMemoryMesh mesh0,
    final SMFMemoryMesh mesh1)
  {
    Assert.assertEquals(
      mesh0.header().attributesByName(), mesh1.header().attributesByName());
    Assert.assertEquals(
      mesh0.header().attributesInOrder(), mesh1.header().attributesInOrder());
    Assert.assertEquals(
      mesh0.header().coordinateSystem(), mesh1.header().coordinateSystem());
    Assert.assertEquals(
      mesh0.header().schemaIdentifier(), mesh1.header().schemaIdentifier());
    Assert.assertEquals(
      mesh0.header().metaCount(), mesh1.header().metaCount());
    Assert.assertEquals(
      mesh0.metadata(), mesh1.metadata());
    Assert.assertEquals(
      mesh0.arrays(), mesh1.arrays());
    Assert.assertEquals(
      mesh0.triangles(), mesh1.triangles());
  }

  private static void checkTriangles(
    final SMFMemoryMesh mesh,
    final int bits)
  {
    final long max = (long) (Math.pow(2.0, (double) bits) - 1.0);

    final Vector<VectorI3L> triangles = mesh.triangles();
    for (int index = 0; index < triangles.size(); ++index) {
      final VectorI3L triangle = triangles.get(index);
      Assert.assertTrue(Long.compareUnsigned(triangle.getXL(), max) <= 0);
      Assert.assertTrue(Long.compareUnsigned(triangle.getYL(), max) <= 0);
      Assert.assertTrue(Long.compareUnsigned(triangle.getZL(), max) <= 0);
    }
  }

  @Test
  public void testParseWrong1()
  {
    final Validation<List<SMFParseError>, SMFMemoryMeshFilterType> r =
      SMFMemoryMeshFilterTrianglesOptimize.parse(
        Optional.empty(),
        1,
        List.of());
    Assert.assertTrue(r.isInvalid());
  }

  @Test
  public void testParseWrong2()
  {
    final Validation<List<SMFParseError>, SMFMemoryMeshFilterType> r =
      SMFMemoryMeshFilterTrianglesOptimize.parse(
        Optional.empty(),
        1,
        List.of("16"));
    Assert.assertTrue(r.isInvalid());
  }

  @Test
  public void testParseWrong3()
  {
    final Validation<List<SMFParseError>, SMFMemoryMeshFilterType> r =
      SMFMemoryMeshFilterTrianglesOptimize.parse(
        Optional.empty(),
        1,
        List.of("16", "x"));
    Assert.assertTrue(r.isInvalid());
  }

  @Test
  public void testParseWrong4()
  {
    final Validation<List<SMFParseError>, SMFMemoryMeshFilterType> r =
      SMFMemoryMeshFilterTrianglesOptimize.parse(
        Optional.empty(),
        1,
        List.of("16", "validate", "x"));
    Assert.assertTrue(r.isInvalid());
  }

  @Test
  public void testParseOk0()
  {
    final Validation<List<SMFParseError>, SMFMemoryMeshFilterType> r =
      SMFMemoryMeshFilterTrianglesOptimize.parse(
        Optional.empty(),
        1,
        List.of("16", "validate"));
    Assert.assertTrue(r.isValid());
  }

  @Test
  public void testParseOk1()
  {
    final Validation<List<SMFParseError>, SMFMemoryMeshFilterType> r =
      SMFMemoryMeshFilterTrianglesOptimize.parse(
        Optional.empty(),
        1,
        List.of("16", "no-validate"));
    Assert.assertTrue(r.isValid());
  }

  @Test
  public void testParseOk2()
  {
    final Validation<List<SMFParseError>, SMFMemoryMeshFilterType> r =
      SMFMemoryMeshFilterTrianglesOptimize.parse(
        Optional.empty(),
        1,
        List.of("-", "no-validate"));
    Assert.assertTrue(r.isValid());
  }

  @Test
  public void testValidateBadTriangle()
    throws Exception
  {
    final SMFMemoryMeshProducerType loader = SMFMemoryMeshProducer.create();

    try (final SMFParserSequentialType parser =
           SMFTestFiles.createParser(loader, "bad_triangle.smft")) {
      // Nothing
    }

    final SMFMemoryMeshFilterTrianglesOptimizeConfiguration config =
      SMFMemoryMeshFilterTrianglesOptimizeConfiguration.builder()
        .setValidate(true)
        .build();

    final SMFMemoryMeshFilterType filter =
      SMFMemoryMeshFilterTrianglesOptimize.create(config);

    final Validation<List<SMFProcessingError>, SMFMemoryMesh> r =
      filter.filter(SMFFilterCommandContext.of(Paths.get("")), loader.mesh());
    Assert.assertTrue(r.isInvalid());

    r.getError().map(e -> {
      LOG.error("error: {}", e);
      return Unit.unit();
    });
  }

  @Test
  public void testValidateSize8_8()
    throws Exception
  {
    final SMFMemoryMeshProducerType loader = SMFMemoryMeshProducer.create();

    try (final SMFParserSequentialType parser =
           SMFTestFiles.createParser(loader, "triangle8.smft")) {
      // Nothing
    }

    final SMFMemoryMeshFilterTrianglesOptimizeConfiguration config =
      SMFMemoryMeshFilterTrianglesOptimizeConfiguration.builder()
        .setValidate(false)
        .setOptimize(8)
        .build();

    final SMFMemoryMeshFilterType filter =
      SMFMemoryMeshFilterTrianglesOptimize.create(config);

    final Validation<List<SMFProcessingError>, SMFMemoryMesh> r =
      filter.filter(SMFFilterCommandContext.of(Paths.get("")), loader.mesh());
    Assert.assertTrue(r.isValid());

    final SMFMemoryMesh mesh0 = loader.mesh();
    final SMFMemoryMesh mesh1 = r.get();
    checkMeshesSame(mesh0, mesh1);

    Assert.assertEquals(8L, mesh0.header().triangleIndexSizeBits());
    Assert.assertEquals(8L, mesh1.header().triangleIndexSizeBits());

    checkTriangles(mesh1, 8);
  }

  @Test
  public void testValidateSize8_16()
    throws Exception
  {
    final SMFMemoryMeshProducerType loader = SMFMemoryMeshProducer.create();

    try (final SMFParserSequentialType parser =
           SMFTestFiles.createParser(loader, "triangle8.smft")) {
      // Nothing
    }

    final SMFMemoryMeshFilterTrianglesOptimizeConfiguration config =
      SMFMemoryMeshFilterTrianglesOptimizeConfiguration.builder()
        .setValidate(false)
        .setOptimize(16)
        .build();

    final SMFMemoryMeshFilterType filter =
      SMFMemoryMeshFilterTrianglesOptimize.create(config);

    final Validation<List<SMFProcessingError>, SMFMemoryMesh> r =
      filter.filter(SMFFilterCommandContext.of(Paths.get("")), loader.mesh());
    Assert.assertTrue(r.isValid());

    final SMFMemoryMesh mesh0 = loader.mesh();
    final SMFMemoryMesh mesh1 = r.get();
    checkMeshesSame(mesh0, mesh1);

    Assert.assertEquals(8L, mesh0.header().triangleIndexSizeBits());
    Assert.assertEquals(16L, mesh1.header().triangleIndexSizeBits());

    checkTriangles(mesh1, 16);
  }

  @Test
  public void testValidateSize8_32()
    throws Exception
  {
    final SMFMemoryMeshProducerType loader = SMFMemoryMeshProducer.create();

    try (final SMFParserSequentialType parser =
           SMFTestFiles.createParser(loader, "triangle8.smft")) {
      // Nothing
    }

    final SMFMemoryMeshFilterTrianglesOptimizeConfiguration config =
      SMFMemoryMeshFilterTrianglesOptimizeConfiguration.builder()
        .setValidate(false)
        .setOptimize(32)
        .build();

    final SMFMemoryMeshFilterType filter =
      SMFMemoryMeshFilterTrianglesOptimize.create(config);

    final Validation<List<SMFProcessingError>, SMFMemoryMesh> r =
      filter.filter(SMFFilterCommandContext.of(Paths.get("")), loader.mesh());
    Assert.assertTrue(r.isValid());

    final SMFMemoryMesh mesh0 = loader.mesh();
    final SMFMemoryMesh mesh1 = r.get();
    checkMeshesSame(mesh0, mesh1);

    Assert.assertEquals(8L, mesh0.header().triangleIndexSizeBits());
    Assert.assertEquals(32L, mesh1.header().triangleIndexSizeBits());

    checkTriangles(mesh1, 32);
  }

  @Test
  public void testValidateSize8_64()
    throws Exception
  {
    final SMFMemoryMeshProducerType loader = SMFMemoryMeshProducer.create();

    try (final SMFParserSequentialType parser =
           SMFTestFiles.createParser(loader, "triangle8.smft")) {
      // Nothing
    }

    final SMFMemoryMeshFilterTrianglesOptimizeConfiguration config =
      SMFMemoryMeshFilterTrianglesOptimizeConfiguration.builder()
        .setValidate(false)
        .setOptimize(64)
        .build();

    final SMFMemoryMeshFilterType filter =
      SMFMemoryMeshFilterTrianglesOptimize.create(config);

    final Validation<List<SMFProcessingError>, SMFMemoryMesh> r =
      filter.filter(SMFFilterCommandContext.of(Paths.get("")), loader.mesh());
    Assert.assertTrue(r.isValid());

    final SMFMemoryMesh mesh0 = loader.mesh();
    final SMFMemoryMesh mesh1 = r.get();
    checkMeshesSame(mesh0, mesh1);

    Assert.assertEquals(8L, mesh0.header().triangleIndexSizeBits());
    Assert.assertEquals(64L, mesh1.header().triangleIndexSizeBits());

    checkTriangles(mesh1, 64);
  }

  @Test
  public void testValidateSize16_8()
    throws Exception
  {
    final SMFMemoryMeshProducerType loader = SMFMemoryMeshProducer.create();

    try (final SMFParserSequentialType parser =
           SMFTestFiles.createParser(loader, "triangle16.smft")) {
      // Nothing
    }

    final SMFMemoryMeshFilterTrianglesOptimizeConfiguration config =
      SMFMemoryMeshFilterTrianglesOptimizeConfiguration.builder()
        .setValidate(false)
        .setOptimize(8)
        .build();

    final SMFMemoryMeshFilterType filter =
      SMFMemoryMeshFilterTrianglesOptimize.create(config);

    final Validation<List<SMFProcessingError>, SMFMemoryMesh> r =
      filter.filter(SMFFilterCommandContext.of(Paths.get("")), loader.mesh());
    Assert.assertTrue(r.isValid());

    final SMFMemoryMesh mesh0 = loader.mesh();
    final SMFMemoryMesh mesh1 = r.get();
    checkMeshesSame(mesh0, mesh1);

    Assert.assertEquals(16L, mesh0.header().triangleIndexSizeBits());
    Assert.assertEquals(8L, mesh1.header().triangleIndexSizeBits());

    checkTriangles(mesh1, 8);
  }

  @Test
  public void testValidateSize16_16()
    throws Exception
  {
    final SMFMemoryMeshProducerType loader = SMFMemoryMeshProducer.create();

    try (final SMFParserSequentialType parser =
           SMFTestFiles.createParser(loader, "triangle16.smft")) {
      // Nothing
    }

    final SMFMemoryMeshFilterTrianglesOptimizeConfiguration config =
      SMFMemoryMeshFilterTrianglesOptimizeConfiguration.builder()
        .setValidate(false)
        .setOptimize(16)
        .build();

    final SMFMemoryMeshFilterType filter =
      SMFMemoryMeshFilterTrianglesOptimize.create(config);

    final Validation<List<SMFProcessingError>, SMFMemoryMesh> r =
      filter.filter(SMFFilterCommandContext.of(Paths.get("")), loader.mesh());
    Assert.assertTrue(r.isValid());

    final SMFMemoryMesh mesh0 = loader.mesh();
    final SMFMemoryMesh mesh1 = r.get();
    checkMeshesSame(mesh0, mesh1);

    Assert.assertEquals(16L, mesh0.header().triangleIndexSizeBits());
    Assert.assertEquals(16L, mesh1.header().triangleIndexSizeBits());

    checkTriangles(mesh1, 16);
  }

  @Test
  public void testValidateSize16_32()
    throws Exception
  {
    final SMFMemoryMeshProducerType loader = SMFMemoryMeshProducer.create();

    try (final SMFParserSequentialType parser =
           SMFTestFiles.createParser(loader, "triangle16.smft")) {
      // Nothing
    }

    final SMFMemoryMeshFilterTrianglesOptimizeConfiguration config =
      SMFMemoryMeshFilterTrianglesOptimizeConfiguration.builder()
        .setValidate(false)
        .setOptimize(32)
        .build();

    final SMFMemoryMeshFilterType filter =
      SMFMemoryMeshFilterTrianglesOptimize.create(config);

    final Validation<List<SMFProcessingError>, SMFMemoryMesh> r =
      filter.filter(SMFFilterCommandContext.of(Paths.get("")), loader.mesh());
    Assert.assertTrue(r.isValid());

    final SMFMemoryMesh mesh0 = loader.mesh();
    final SMFMemoryMesh mesh1 = r.get();
    checkMeshesSame(mesh0, mesh1);

    Assert.assertEquals(16L, mesh0.header().triangleIndexSizeBits());
    Assert.assertEquals(32L, mesh1.header().triangleIndexSizeBits());

    checkTriangles(mesh1, 32);
  }

  @Test
  public void testValidateSize16_64()
    throws Exception
  {
    final SMFMemoryMeshProducerType loader = SMFMemoryMeshProducer.create();

    try (final SMFParserSequentialType parser =
           SMFTestFiles.createParser(loader, "triangle16.smft")) {
      // Nothing
    }

    final SMFMemoryMeshFilterTrianglesOptimizeConfiguration config =
      SMFMemoryMeshFilterTrianglesOptimizeConfiguration.builder()
        .setValidate(false)
        .setOptimize(64)
        .build();

    final SMFMemoryMeshFilterType filter =
      SMFMemoryMeshFilterTrianglesOptimize.create(config);

    final Validation<List<SMFProcessingError>, SMFMemoryMesh> r =
      filter.filter(SMFFilterCommandContext.of(Paths.get("")), loader.mesh());
    Assert.assertTrue(r.isValid());

    final SMFMemoryMesh mesh0 = loader.mesh();
    final SMFMemoryMesh mesh1 = r.get();
    checkMeshesSame(mesh0, mesh1);

    Assert.assertEquals(16L, mesh0.header().triangleIndexSizeBits());
    Assert.assertEquals(64L, mesh1.header().triangleIndexSizeBits());

    checkTriangles(mesh1, 64);
  }

  @Test
  public void testValidateSize32_8()
    throws Exception
  {
    final SMFMemoryMeshProducerType loader = SMFMemoryMeshProducer.create();

    try (final SMFParserSequentialType parser =
           SMFTestFiles.createParser(loader, "triangle32.smft")) {
      // Nothing
    }

    final SMFMemoryMeshFilterTrianglesOptimizeConfiguration config =
      SMFMemoryMeshFilterTrianglesOptimizeConfiguration.builder()
        .setValidate(false)
        .setOptimize(8)
        .build();

    final SMFMemoryMeshFilterType filter =
      SMFMemoryMeshFilterTrianglesOptimize.create(config);

    final Validation<List<SMFProcessingError>, SMFMemoryMesh> r =
      filter.filter(SMFFilterCommandContext.of(Paths.get("")), loader.mesh());
    Assert.assertTrue(r.isValid());

    final SMFMemoryMesh mesh0 = loader.mesh();
    final SMFMemoryMesh mesh1 = r.get();
    checkMeshesSame(mesh0, mesh1);

    Assert.assertEquals(32L, mesh0.header().triangleIndexSizeBits());
    Assert.assertEquals(8L, mesh1.header().triangleIndexSizeBits());

    checkTriangles(mesh1, 8);
  }

  @Test
  public void testValidateSize32_16()
    throws Exception
  {
    final SMFMemoryMeshProducerType loader = SMFMemoryMeshProducer.create();

    try (final SMFParserSequentialType parser =
           SMFTestFiles.createParser(loader, "triangle32.smft")) {
      // Nothing
    }

    final SMFMemoryMeshFilterTrianglesOptimizeConfiguration config =
      SMFMemoryMeshFilterTrianglesOptimizeConfiguration.builder()
        .setValidate(false)
        .setOptimize(16)
        .build();

    final SMFMemoryMeshFilterType filter =
      SMFMemoryMeshFilterTrianglesOptimize.create(config);

    final Validation<List<SMFProcessingError>, SMFMemoryMesh> r =
      filter.filter(SMFFilterCommandContext.of(Paths.get("")), loader.mesh());
    Assert.assertTrue(r.isValid());

    final SMFMemoryMesh mesh0 = loader.mesh();
    final SMFMemoryMesh mesh1 = r.get();
    checkMeshesSame(mesh0, mesh1);

    Assert.assertEquals(32L, mesh0.header().triangleIndexSizeBits());
    Assert.assertEquals(16L, mesh1.header().triangleIndexSizeBits());

    checkTriangles(mesh1, 16);
  }

  @Test
  public void testValidateSize32_32()
    throws Exception
  {
    final SMFMemoryMeshProducerType loader = SMFMemoryMeshProducer.create();

    try (final SMFParserSequentialType parser =
           SMFTestFiles.createParser(loader, "triangle32.smft")) {
      // Nothing
    }

    final SMFMemoryMeshFilterTrianglesOptimizeConfiguration config =
      SMFMemoryMeshFilterTrianglesOptimizeConfiguration.builder()
        .setValidate(false)
        .setOptimize(32)
        .build();

    final SMFMemoryMeshFilterType filter =
      SMFMemoryMeshFilterTrianglesOptimize.create(config);

    final Validation<List<SMFProcessingError>, SMFMemoryMesh> r =
      filter.filter(SMFFilterCommandContext.of(Paths.get("")), loader.mesh());
    Assert.assertTrue(r.isValid());

    final SMFMemoryMesh mesh0 = loader.mesh();
    final SMFMemoryMesh mesh1 = r.get();
    checkMeshesSame(mesh0, mesh1);

    Assert.assertEquals(32L, mesh0.header().triangleIndexSizeBits());
    Assert.assertEquals(32L, mesh1.header().triangleIndexSizeBits());

    checkTriangles(mesh1, 32);
  }

  @Test
  public void testValidateSize32_64()
    throws Exception
  {
    final SMFMemoryMeshProducerType loader = SMFMemoryMeshProducer.create();

    try (final SMFParserSequentialType parser =
           SMFTestFiles.createParser(loader, "triangle32.smft")) {
      // Nothing
    }

    final SMFMemoryMeshFilterTrianglesOptimizeConfiguration config =
      SMFMemoryMeshFilterTrianglesOptimizeConfiguration.builder()
        .setValidate(false)
        .setOptimize(64)
        .build();

    final SMFMemoryMeshFilterType filter =
      SMFMemoryMeshFilterTrianglesOptimize.create(config);

    final Validation<List<SMFProcessingError>, SMFMemoryMesh> r =
      filter.filter(SMFFilterCommandContext.of(Paths.get("")), loader.mesh());
    Assert.assertTrue(r.isValid());

    final SMFMemoryMesh mesh0 = loader.mesh();
    final SMFMemoryMesh mesh1 = r.get();
    checkMeshesSame(mesh0, mesh1);

    Assert.assertEquals(32L, mesh0.header().triangleIndexSizeBits());
    Assert.assertEquals(64L, mesh1.header().triangleIndexSizeBits());

    checkTriangles(mesh1, 64);
  }

  @Test
  public void testValidateSize64_8()
    throws Exception
  {
    final SMFMemoryMeshProducerType loader = SMFMemoryMeshProducer.create();

    try (final SMFParserSequentialType parser =
           SMFTestFiles.createParser(loader, "triangle64.smft")) {
      // Nothing
    }

    final SMFMemoryMeshFilterTrianglesOptimizeConfiguration config =
      SMFMemoryMeshFilterTrianglesOptimizeConfiguration.builder()
        .setValidate(false)
        .setOptimize(8)
        .build();

    final SMFMemoryMeshFilterType filter =
      SMFMemoryMeshFilterTrianglesOptimize.create(config);

    final Validation<List<SMFProcessingError>, SMFMemoryMesh> r =
      filter.filter(SMFFilterCommandContext.of(Paths.get("")), loader.mesh());
    Assert.assertTrue(r.isValid());

    final SMFMemoryMesh mesh0 = loader.mesh();
    final SMFMemoryMesh mesh1 = r.get();
    checkMeshesSame(mesh0, mesh1);

    Assert.assertEquals(64L, mesh0.header().triangleIndexSizeBits());
    Assert.assertEquals(8L, mesh1.header().triangleIndexSizeBits());

    checkTriangles(mesh1, 8);
  }

  @Test
  public void testValidateSize64_16()
    throws Exception
  {
    final SMFMemoryMeshProducerType loader = SMFMemoryMeshProducer.create();

    try (final SMFParserSequentialType parser =
           SMFTestFiles.createParser(loader, "triangle64.smft")) {
      // Nothing
    }

    final SMFMemoryMeshFilterTrianglesOptimizeConfiguration config =
      SMFMemoryMeshFilterTrianglesOptimizeConfiguration.builder()
        .setValidate(false)
        .setOptimize(16)
        .build();

    final SMFMemoryMeshFilterType filter =
      SMFMemoryMeshFilterTrianglesOptimize.create(config);

    final Validation<List<SMFProcessingError>, SMFMemoryMesh> r =
      filter.filter(SMFFilterCommandContext.of(Paths.get("")), loader.mesh());
    Assert.assertTrue(r.isValid());

    final SMFMemoryMesh mesh0 = loader.mesh();
    final SMFMemoryMesh mesh1 = r.get();
    checkMeshesSame(mesh0, mesh1);

    Assert.assertEquals(64L, mesh0.header().triangleIndexSizeBits());
    Assert.assertEquals(16L, mesh1.header().triangleIndexSizeBits());

    checkTriangles(mesh1, 16);
  }

  @Test
  public void testValidateSize64_32()
    throws Exception
  {
    final SMFMemoryMeshProducerType loader = SMFMemoryMeshProducer.create();

    try (final SMFParserSequentialType parser =
           SMFTestFiles.createParser(loader, "triangle64.smft")) {
      // Nothing
    }

    final SMFMemoryMeshFilterTrianglesOptimizeConfiguration config =
      SMFMemoryMeshFilterTrianglesOptimizeConfiguration.builder()
        .setValidate(false)
        .setOptimize(32)
        .build();

    final SMFMemoryMeshFilterType filter =
      SMFMemoryMeshFilterTrianglesOptimize.create(config);

    final Validation<List<SMFProcessingError>, SMFMemoryMesh> r =
      filter.filter(SMFFilterCommandContext.of(Paths.get("")), loader.mesh());
    Assert.assertTrue(r.isValid());

    final SMFMemoryMesh mesh0 = loader.mesh();
    final SMFMemoryMesh mesh1 = r.get();
    checkMeshesSame(mesh0, mesh1);

    Assert.assertEquals(64L, mesh0.header().triangleIndexSizeBits());
    Assert.assertEquals(32L, mesh1.header().triangleIndexSizeBits());

    checkTriangles(mesh1, 32);
  }

  @Test
  public void testValidateSize64_64()
    throws Exception
  {
    final SMFMemoryMeshProducerType loader = SMFMemoryMeshProducer.create();

    try (final SMFParserSequentialType parser =
           SMFTestFiles.createParser(loader, "triangle64.smft")) {
      // Nothing
    }

    final SMFMemoryMeshFilterTrianglesOptimizeConfiguration config =
      SMFMemoryMeshFilterTrianglesOptimizeConfiguration.builder()
        .setValidate(false)
        .setOptimize(64)
        .build();

    final SMFMemoryMeshFilterType filter =
      SMFMemoryMeshFilterTrianglesOptimize.create(config);

    final Validation<List<SMFProcessingError>, SMFMemoryMesh> r =
      filter.filter(SMFFilterCommandContext.of(Paths.get("")), loader.mesh());
    Assert.assertTrue(r.isValid());

    final SMFMemoryMesh mesh0 = loader.mesh();
    final SMFMemoryMesh mesh1 = r.get();
    checkMeshesSame(mesh0, mesh1);

    Assert.assertEquals(64L, mesh0.header().triangleIndexSizeBits());
    Assert.assertEquals(64L, mesh1.header().triangleIndexSizeBits());

    checkTriangles(mesh1, 64);
  }
}
