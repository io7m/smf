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
import com.io7m.smfj.core.SMFAttribute;
import com.io7m.smfj.core.SMFAttributeName;
import com.io7m.smfj.core.SMFHeader;
import com.io7m.smfj.parser.api.SMFParseError;
import com.io7m.smfj.parser.api.SMFParserSequentialType;
import com.io7m.smfj.processing.api.SMFAttributeArrayType;
import com.io7m.smfj.processing.api.SMFMemoryMesh;
import com.io7m.smfj.processing.api.SMFMemoryMeshFilterType;
import com.io7m.smfj.processing.api.SMFMemoryMeshProducer;
import com.io7m.smfj.processing.api.SMFMemoryMeshProducerType;
import com.io7m.smfj.processing.api.SMFProcessingError;
import com.io7m.smfj.processing.main.SMFMemoryMeshFilterAttributeRename;
import io.vavr.Tuple2;
import io.vavr.collection.List;
import io.vavr.collection.Map;
import io.vavr.collection.Seq;
import io.vavr.control.Validation;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.FileSystem;
import java.util.Objects;
import java.util.Optional;

public final class SMFMemoryMeshFilterAttributeRenameTest extends
  SMFMemoryMeshFilterContract
{
  private static final Logger LOG;

  static {
    LOG = LoggerFactory.getLogger(SMFMemoryMeshFilterAttributeRenameTest.class);
  }

  private FileSystem filesystem;

  @Test
  public void testParseWrong1()
  {
    final Validation<Seq<SMFParseError>, SMFMemoryMeshFilterType> r =
      SMFMemoryMeshFilterAttributeRename.parse(
        Optional.empty(),
        1,
        List.of());
    Assertions.assertTrue(r.isInvalid());
  }

  @Test
  public void testParseWrong2()
  {
    final Validation<Seq<SMFParseError>, SMFMemoryMeshFilterType> r =
      SMFMemoryMeshFilterAttributeRename.parse(
        Optional.empty(),
        1,
        List.of("x", "<#@"));
    Assertions.assertTrue(r.isInvalid());
  }

  @Test
  public void testParseWrong3()
  {
    final Validation<Seq<SMFParseError>, SMFMemoryMeshFilterType> r =
      SMFMemoryMeshFilterAttributeRename.parse(
        Optional.empty(),
        1,
        List.of("<#@", "y"));
    Assertions.assertTrue(r.isInvalid());
  }

  @Test
  public void testParseWrong4()
  {
    final Validation<Seq<SMFParseError>, SMFMemoryMeshFilterType> r =
      SMFMemoryMeshFilterAttributeRename.parse(
        Optional.empty(),
        1,
        List.of(
          "x",
          "y",
          "z"));
    Assertions.assertTrue(r.isInvalid());
  }

  @Test
  public void testParse()
  {
    final Validation<Seq<SMFParseError>, SMFMemoryMeshFilterType> r =
      SMFMemoryMeshFilterAttributeRename.parse(
        Optional.empty(),
        1,
        List.of("x", "y"));
    Assertions.assertTrue(r.isValid());
    final SMFMemoryMeshFilterType c = r.get();
    Assertions.assertEquals(c.name(), "rename");
  }

  @Test
  public void testRenameNonexistent()
    throws Exception
  {
    final SMFMemoryMeshProducerType loader = SMFMemoryMeshProducer.create();

    try (SMFParserSequentialType parser =
           SMFTestFiles.createParser(loader, "all.smft")) {
      // Nothing
    }

    final SMFAttributeName name_target = SMFAttributeName.of("RENAMED");
    final SMFAttributeName name_source = SMFAttributeName.of("nonexistent");

    final SMFMemoryMeshFilterType filter =
      SMFMemoryMeshFilterAttributeRename.create(name_source, name_target);

    final Validation<Seq<SMFProcessingError>, SMFMemoryMesh> r =
      filter.filter(this.createContext(), loader.mesh());
    Assertions.assertTrue(r.isInvalid());

    r.getError().map(e -> {
      LOG.error("error: {}", e.message());
      return Unit.unit();
    });
  }

  @Test
  public void testRenameCollision()
    throws Exception
  {
    final SMFMemoryMeshProducerType loader = SMFMemoryMeshProducer.create();

    try (SMFParserSequentialType parser =
           SMFTestFiles.createParser(loader, "all.smft")) {
      // Nothing
    }

    final SMFAttributeName name_target = SMFAttributeName.of("f16_3");
    final SMFAttributeName name_source = SMFAttributeName.of("f16_4");

    final SMFMemoryMeshFilterType filter =
      SMFMemoryMeshFilterAttributeRename.create(name_source, name_target);

    final Validation<Seq<SMFProcessingError>, SMFMemoryMesh> r =
      filter.filter(this.createContext(), loader.mesh());
    Assertions.assertTrue(r.isInvalid());

    r.getError().map(e -> {
      LOG.error("error: {}", e.message());
      return Unit.unit();
    });
  }

  @Test
  public void testRenameOK()
    throws Exception
  {
    final SMFMemoryMeshProducerType loader = SMFMemoryMeshProducer.create();

    try (SMFParserSequentialType parser =
           SMFTestFiles.createParser(loader, "all.smft")) {
      // Nothing
    }

    final SMFAttributeName name_target = SMFAttributeName.of("RENAMED");
    final SMFAttributeName name_source = SMFAttributeName.of("f16_4");

    final SMFMemoryMeshFilterType filter =
      SMFMemoryMeshFilterAttributeRename.create(name_source, name_target);

    final SMFMemoryMesh mesh0 = loader.mesh();
    final SMFMemoryMesh mesh1 =
      filter.filter(this.createContext(), mesh0).get();
    final Map<SMFAttributeName, SMFAttributeArrayType> arrays0 = mesh0.arrays();
    final Map<SMFAttributeName, SMFAttributeArrayType> arrays1 = mesh1.arrays();
    final SMFHeader header0 = mesh0.header();
    final SMFHeader header1 = mesh1.header();

    Assertions.assertEquals(mesh0.triangles(), mesh1.triangles());
    Assertions.assertEquals((long) arrays0.size(), (long) arrays1.size());
    Assertions.assertEquals(
      (long) header0.attributesByName().size(),
      (long) header1.attributesByName().size());
    Assertions.assertEquals(
      (long) header0.attributesInOrder().size(),
      (long) header1.attributesInOrder().size());
    Assertions.assertEquals(
      header0.coordinateSystem(),
      header1.coordinateSystem());
    Assertions.assertEquals(
      header0.schemaIdentifier(),
      header1.schemaIdentifier());
    Assertions.assertEquals(
      mesh0.metadata(),
      mesh1.metadata());

    for (int index = 0; index < header0.attributesInOrder().size(); ++index) {
      final SMFAttribute attr0 = header0.attributesInOrder().get(index);
      final SMFAttribute attr1 = header1.attributesInOrder().get(index);
      if (!Objects.equals(attr0.name(), name_source)) {
        Assertions.assertEquals(attr0, attr1);
      }
    }

    for (final Tuple2<SMFAttributeName, SMFAttribute> pair0 : header0.attributesByName()) {
      final SMFAttributeName name0 = pair0._1;
      final SMFAttributeName name1;

      if (Objects.equals("f16_4", name0.value())) {
        name1 = name_target;
      } else {
        name1 = name0;
      }

      final SMFAttributeArrayType array0 = arrays0.get(name0).get();
      final SMFAttributeArrayType array1 = arrays1.get(name1).get();
      Assertions.assertEquals(array0, array1);
    }
  }
}
