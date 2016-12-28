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
import com.io7m.smfj.processing.SMFAttributeArrayType;
import com.io7m.smfj.processing.SMFMemoryMesh;
import com.io7m.smfj.processing.SMFMemoryMeshFilterAttributeRemove;
import com.io7m.smfj.processing.SMFMemoryMeshFilterType;
import com.io7m.smfj.processing.SMFMemoryMeshProducer;
import com.io7m.smfj.processing.SMFMemoryMeshProducerType;
import com.io7m.smfj.processing.SMFProcessingError;
import javaslang.collection.List;
import javaslang.collection.Map;
import javaslang.control.Validation;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

public final class SMFMemoryMeshFilterAttributeRemoveTest
{
  private static final Logger LOG;

  static {
    LOG = LoggerFactory.getLogger(SMFMemoryMeshFilterAttributeRemoveTest.class);
  }

  @Test public void testParseWrong0()
  {
    final Validation<List<SMFParseError>, SMFMemoryMeshFilterType> r =
      SMFMemoryMeshFilterAttributeRemove.parse(Optional.empty(), 1, List.empty());
    Assert.assertTrue(r.isInvalid());
  }

  @Test public void testParseWrong1()
  {
    final Validation<List<SMFParseError>, SMFMemoryMeshFilterType> r =
      SMFMemoryMeshFilterAttributeRemove.parse(Optional.empty(), 1, List.of("remove"));
    Assert.assertTrue(r.isInvalid());
  }

  @Test public void testParseWrong2()
  {
    final Validation<List<SMFParseError>, SMFMemoryMeshFilterType> r =
      SMFMemoryMeshFilterAttributeRemove.parse(Optional.empty(), 1, List.of("remove", "<#@"));
    Assert.assertTrue(r.isInvalid());
  }

  @Test public void testParseWrong3()
  {
    final Validation<List<SMFParseError>, SMFMemoryMeshFilterType> r =
      SMFMemoryMeshFilterAttributeRemove.parse(Optional.empty(), 1, List.of("remove", "x", "y"));
    Assert.assertTrue(r.isInvalid());
  }

  @Test public void testParse()
  {
    final Validation<List<SMFParseError>, SMFMemoryMeshFilterType> r =
      SMFMemoryMeshFilterAttributeRemove.parse(Optional.empty(), 1, List.of("remove", "x"));
    Assert.assertTrue(r.isValid());
    final SMFMemoryMeshFilterType c = r.get();
    Assert.assertEquals(c.name(), "remove");
  }

  @Test
  public void testRemoveNonexistent()
    throws Exception
  {
    final SMFMemoryMeshProducerType loader = SMFMemoryMeshProducer.create();

    try (final SMFParserSequentialType parser =
           SMFTestFiles.createParser(loader, "all.smft")) {
      // Nothing
    }

    final SMFAttributeName name_source = SMFAttributeName.of("nonexistent");

    final SMFMemoryMeshFilterType filter =
      SMFMemoryMeshFilterAttributeRemove.create(name_source);

    final Validation<List<SMFProcessingError>, SMFMemoryMesh> r =
      filter.filter(loader.mesh());
    Assert.assertTrue(r.isInvalid());

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

    try (final SMFParserSequentialType parser =
           SMFTestFiles.createParser(loader, "all.smft")) {
      // Nothing
    }

    final SMFAttributeName name_source = SMFAttributeName.of("f16_4");

    final SMFMemoryMeshFilterType filter =
      SMFMemoryMeshFilterAttributeRemove.create(name_source);

    final SMFMemoryMesh mesh0 = loader.mesh();
    final SMFMemoryMesh mesh1 = filter.filter(mesh0).get();
    final Map<SMFAttributeName, SMFAttributeArrayType> arrays0 = mesh0.arrays();
    final Map<SMFAttributeName, SMFAttributeArrayType> arrays1 = mesh1.arrays();
    final SMFHeader header0 = mesh0.header();
    final SMFHeader header1 = mesh1.header();

    Assert.assertEquals(mesh0.triangles(), mesh1.triangles());
    Assert.assertEquals((long) arrays0.size() - 1L, (long) arrays1.size());
    Assert.assertEquals(
      (long) header0.attributesByName().size() - 1L,
      (long) header1.attributesByName().size());
    Assert.assertEquals(
      (long) header0.attributesInOrder().size() - 1L,
      (long) header1.attributesInOrder().size());
    Assert.assertEquals(
      header0.metaCount(),
      header1.metaCount());
    Assert.assertEquals(
      header0.coordinateSystem(),
      header1.coordinateSystem());
    Assert.assertEquals(
      header0.schemaIdentifier(),
      header1.schemaIdentifier());
    Assert.assertEquals(
      mesh0.metadata(),
      mesh1.metadata());

    final SMFAttribute attr =
      header0.attributesByName().get(name_source).get();
    Assert.assertEquals(
      header1.attributesInOrder(),
      header0.attributesInOrder().remove(attr));
    Assert.assertEquals(
      header0.attributesByName().remove(name_source),
      header1.attributesByName());
  }
}
