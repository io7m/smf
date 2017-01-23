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

import com.io7m.smfj.parser.api.SMFParseError;
import com.io7m.smfj.parser.api.SMFParserSequentialType;
import com.io7m.smfj.processing.api.SMFFilterCommandContext;
import com.io7m.smfj.processing.api.SMFMemoryMesh;
import com.io7m.smfj.processing.api.SMFMemoryMeshFilterType;
import com.io7m.smfj.processing.api.SMFMemoryMeshProducer;
import com.io7m.smfj.processing.api.SMFMemoryMeshProducerType;
import com.io7m.smfj.processing.api.SMFMetadata;
import com.io7m.smfj.processing.api.SMFProcessingError;
import com.io7m.smfj.processing.main.SMFMemoryMeshFilterMetadataRemove;
import javaslang.collection.List;
import javaslang.control.Validation;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import java.util.OptionalLong;

public final class SMFMemoryMeshFilterMetadataRemoveTest extends
  SMFMemoryMeshFilterContract
{
  private static final Logger LOG;

  static {
    LOG = LoggerFactory.getLogger(SMFMemoryMeshFilterMetadataRemoveTest.class);
  }

  @Test
  public void testParseWrong1()
  {
    final Validation<List<SMFParseError>, SMFMemoryMeshFilterType> r =
      SMFMemoryMeshFilterMetadataRemove.parse(
        Optional.empty(),
        1,
        List.of());
    Assert.assertTrue(r.isInvalid());
  }

  @Test
  public void testParseWrong2()
  {
    final Validation<List<SMFParseError>, SMFMemoryMeshFilterType> r =
      SMFMemoryMeshFilterMetadataRemove.parse(
        Optional.empty(),
        1,
        List.of("x", "y"));
    Assert.assertTrue(r.isInvalid());
  }

  @Test
  public void testParseWrong3()
  {
    final Validation<List<SMFParseError>, SMFMemoryMeshFilterType> r =
      SMFMemoryMeshFilterMetadataRemove.parse(
        Optional.empty(),
        1,
        List.of("x", "y", "z", "w"));
    Assert.assertTrue(r.isInvalid());
  }

  @Test
  public void testParseWrong4()
  {
    final Validation<List<SMFParseError>, SMFMemoryMeshFilterType> r =
      SMFMemoryMeshFilterMetadataRemove.parse(
        Optional.empty(),
        1,
        List.of(
          "x",
          "0",
          "z"));
    Assert.assertTrue(r.isInvalid());
  }

  @Test
  public void testParseWrong5()
  {
    final Validation<List<SMFParseError>, SMFMemoryMeshFilterType> r =
      SMFMemoryMeshFilterMetadataRemove.parse(
        Optional.empty(),
        1,
        List.of(
          "0",
          "x",
          "z"));
    Assert.assertTrue(r.isInvalid());
  }

  @Test
  public void testParseOK0()
  {
    final Validation<List<SMFParseError>, SMFMemoryMeshFilterType> r =
      SMFMemoryMeshFilterMetadataRemove.parse(
        Optional.empty(),
        1,
        List.of("1", "2"));
    Assert.assertTrue(r.isValid());
    final SMFMemoryMeshFilterType c = r.get();
    Assert.assertEquals(c.name(), SMFMemoryMeshFilterMetadataRemove.NAME);
  }

  @Test
  public void testParseOK1()
  {
    final Validation<List<SMFParseError>, SMFMemoryMeshFilterType> r =
      SMFMemoryMeshFilterMetadataRemove.parse(
        Optional.empty(),
        1,
        List.of("-", "2"));
    Assert.assertTrue(r.isValid());
    final SMFMemoryMeshFilterType c = r.get();
    Assert.assertEquals(c.name(), SMFMemoryMeshFilterMetadataRemove.NAME);
  }

  @Test
  public void testParseOK2()
  {
    final Validation<List<SMFParseError>, SMFMemoryMeshFilterType> r =
      SMFMemoryMeshFilterMetadataRemove.parse(
        Optional.empty(),
        1,
        List.of("1", "-"));
    Assert.assertTrue(r.isValid());
    final SMFMemoryMeshFilterType c = r.get();
    Assert.assertEquals(c.name(), SMFMemoryMeshFilterMetadataRemove.NAME);
  }

  @Test
  public void testParseOK3()
  {
    final Validation<List<SMFParseError>, SMFMemoryMeshFilterType> r =
      SMFMemoryMeshFilterMetadataRemove.parse(
        Optional.empty(),
        1,
        List.of("-", "-"));
    Assert.assertTrue(r.isValid());
    final SMFMemoryMeshFilterType c = r.get();
    Assert.assertEquals(c.name(), SMFMemoryMeshFilterMetadataRemove.NAME);
  }

  @Test
  public void testRemoveOK0()
    throws Exception
  {
    final SMFMemoryMeshProducerType loader = SMFMemoryMeshProducer.create();

    try (final SMFParserSequentialType parser =
           SMFTestFiles.createParser(loader, "all.smft")) {
      // Nothing
    }

    final Path root =
      this.filesystem.getRootDirectories().iterator().next();
    final SMFFilterCommandContext context =
      SMFFilterCommandContext.of(root, root);

    final SMFMemoryMeshFilterType filter =
      SMFMemoryMeshFilterMetadataRemove.create(
        OptionalLong.empty(), OptionalLong.empty());

    final SMFMemoryMesh mesh0 = loader.mesh();
    final Validation<List<SMFProcessingError>, SMFMemoryMesh> r =
      filter.filter(context, mesh0);

    Assert.assertTrue(r.isValid());

    final SMFMemoryMesh mesh1 = r.get();
    Assert.assertEquals(mesh0.arrays(), mesh1.arrays());
    Assert.assertEquals(mesh0.triangles(), mesh1.triangles());
    Assert.assertEquals(0L, (long) mesh1.metadata().size());

    Assert.assertEquals(
      0L,
      mesh1.header().metaCount());
  }

  @Test
  public void testRemoveOK1()
    throws Exception
  {
    final SMFMemoryMeshProducerType loader = SMFMemoryMeshProducer.create();

    try (final SMFParserSequentialType parser =
           SMFTestFiles.createParser(loader, "all.smft")) {
      // Nothing
    }

    final Path root =
      this.filesystem.getRootDirectories().iterator().next();
    final SMFFilterCommandContext context =
      SMFFilterCommandContext.of(root, root);

    final SMFMemoryMeshFilterType filter =
      SMFMemoryMeshFilterMetadataRemove.create(
        OptionalLong.of(0x696f376dL), OptionalLong.empty());

    final SMFMemoryMesh mesh0 = loader.mesh();
    final Validation<List<SMFProcessingError>, SMFMemoryMesh> r =
      filter.filter(context, mesh0);

    Assert.assertTrue(r.isValid());

    final SMFMemoryMesh mesh1 = r.get();
    Assert.assertEquals(mesh0.arrays(), mesh1.arrays());
    Assert.assertEquals(mesh0.triangles(), mesh1.triangles());
    Assert.assertEquals(0L, (long) mesh1.metadata().size());

    Assert.assertEquals(
      0L,
      mesh1.header().metaCount());
  }

  @Test
  public void testRemoveOK2()
    throws Exception
  {
    final SMFMemoryMeshProducerType loader = SMFMemoryMeshProducer.create();

    try (final SMFParserSequentialType parser =
           SMFTestFiles.createParser(loader, "all.smft")) {
      // Nothing
    }

    final Path root =
      this.filesystem.getRootDirectories().iterator().next();
    final SMFFilterCommandContext context =
      SMFFilterCommandContext.of(root, root);

    final SMFMemoryMeshFilterType filter =
      SMFMemoryMeshFilterMetadataRemove.create(
        OptionalLong.empty(), OptionalLong.of(0L));

    final SMFMemoryMesh mesh0 = loader.mesh();
    final Validation<List<SMFProcessingError>, SMFMemoryMesh> r =
      filter.filter(context, mesh0);

    Assert.assertTrue(r.isValid());

    final SMFMemoryMesh mesh1 = r.get();
    Assert.assertEquals(mesh0.arrays(), mesh1.arrays());
    Assert.assertEquals(mesh0.triangles(), mesh1.triangles());
    Assert.assertEquals(2L, (long) mesh1.metadata().size());

    Assert.assertEquals(
      2L,
      mesh1.header().metaCount());
  }

  @Test
  public void testRemoveOK3()
    throws Exception
  {
    final SMFMemoryMeshProducerType loader = SMFMemoryMeshProducer.create();

    try (final SMFParserSequentialType parser =
           SMFTestFiles.createParser(loader, "all.smft")) {
      // Nothing
    }

    final Path root =
      this.filesystem.getRootDirectories().iterator().next();
    final SMFFilterCommandContext context =
      SMFFilterCommandContext.of(root, root);

    final SMFMemoryMeshFilterType filter =
      SMFMemoryMeshFilterMetadataRemove.create(
        OptionalLong.of(0x696f376dL), OptionalLong.of(0L));

    final SMFMemoryMesh mesh0 = loader.mesh();
    final Validation<List<SMFProcessingError>, SMFMemoryMesh> r =
      filter.filter(context, mesh0);

    Assert.assertTrue(r.isValid());

    final SMFMemoryMesh mesh1 = r.get();
    Assert.assertEquals(mesh0.arrays(), mesh1.arrays());
    Assert.assertEquals(mesh0.triangles(), mesh1.triangles());
    Assert.assertEquals(2L, (long) mesh1.metadata().size());

    Assert.assertEquals(
      2L,
      mesh1.header().metaCount());
  }
}