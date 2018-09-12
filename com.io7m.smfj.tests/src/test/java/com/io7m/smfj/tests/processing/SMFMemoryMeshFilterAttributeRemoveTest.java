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
import com.io7m.smfj.processing.main.SMFMemoryMeshFilterAttributeRemove;
import io.vavr.collection.List;
import io.vavr.collection.Map;
import io.vavr.collection.Seq;
import io.vavr.control.Validation;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

public final class SMFMemoryMeshFilterAttributeRemoveTest extends
  SMFMemoryMeshFilterContract
{
  private static final Logger LOG;

  static {
    LOG = LoggerFactory.getLogger(SMFMemoryMeshFilterAttributeRemoveTest.class);
  }

  @Test
  public void testParseWrong1()
  {
    final Validation<Seq<SMFParseError>, SMFMemoryMeshFilterType> r =
      SMFMemoryMeshFilterAttributeRemove.parse(
        Optional.empty(),
        1,
        List.of());
    Assertions.assertTrue(r.isInvalid());
  }

  @Test
  public void testParseWrong2()
  {
    final Validation<Seq<SMFParseError>, SMFMemoryMeshFilterType> r =
      SMFMemoryMeshFilterAttributeRemove.parse(
        Optional.empty(),
        1,
        List.of("<#@"));
    Assertions.assertTrue(r.isInvalid());
  }

  @Test
  public void testParseWrong3()
  {
    final Validation<Seq<SMFParseError>, SMFMemoryMeshFilterType> r =
      SMFMemoryMeshFilterAttributeRemove.parse(
        Optional.empty(),
        1,
        List.of("x", "y"));
    Assertions.assertTrue(r.isInvalid());
  }

  @Test
  public void testParse()
  {
    final Validation<Seq<SMFParseError>, SMFMemoryMeshFilterType> r =
      SMFMemoryMeshFilterAttributeRemove.parse(
        Optional.empty(),
        1,
        List.of("x"));
    Assertions.assertTrue(r.isValid());
    final SMFMemoryMeshFilterType c = r.get();
    Assertions.assertEquals(c.name(), "remove");
  }

  @Test
  public void testRemoveNonexistent()
    throws Exception
  {
    final SMFMemoryMeshProducerType loader = SMFMemoryMeshProducer.create();

    try (SMFParserSequentialType parser =
           SMFTestFiles.createParser(loader, "all.smft")) {
      // Nothing
    }

    final SMFAttributeName name_source = SMFAttributeName.of("nonexistent");

    final SMFMemoryMeshFilterType filter =
      SMFMemoryMeshFilterAttributeRemove.create(name_source);

    final Validation<Seq<SMFProcessingError>, SMFMemoryMesh> r =
      filter.filter(this.createContext(), loader.mesh());
    Assertions.assertTrue(r.isInvalid());

    r.getError().map(e -> {
      LOG.error("error: {}", e.message());
      return Unit.unit();
    });
  }

  @Test
  public void testRemoveOK()
    throws Exception
  {
    final SMFMemoryMeshProducerType loader = SMFMemoryMeshProducer.create();

    try (SMFParserSequentialType parser =
           SMFTestFiles.createParser(loader, "all.smft")) {
      // Nothing
    }

    final SMFAttributeName name_source = SMFAttributeName.of("f16_4");

    final SMFMemoryMeshFilterType filter =
      SMFMemoryMeshFilterAttributeRemove.create(name_source);

    final SMFMemoryMesh mesh0 = loader.mesh();
    final SMFMemoryMesh mesh1 =
      filter.filter(this.createContext(), mesh0).get();
    final Map<SMFAttributeName, SMFAttributeArrayType> arrays0 = mesh0.arrays();
    final Map<SMFAttributeName, SMFAttributeArrayType> arrays1 = mesh1.arrays();
    final SMFHeader header0 = mesh0.header();
    final SMFHeader header1 = mesh1.header();

    Assertions.assertEquals(mesh0.triangles(), mesh1.triangles());
    Assertions.assertEquals((long) arrays0.size() - 1L, (long) arrays1.size());
    Assertions.assertEquals(
      (long) header0.attributesByName().size() - 1L,
      (long) header1.attributesByName().size());
    Assertions.assertEquals(
      (long) header0.attributesInOrder().size() - 1L,
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

    final SMFAttribute attr =
      header0.attributesByName().get(name_source).get();
    Assertions.assertEquals(
      header1.attributesInOrder(),
      header0.attributesInOrder().remove(attr));
    Assertions.assertEquals(
      header0.attributesByName().remove(name_source),
      header1.attributesByName());
  }
}
