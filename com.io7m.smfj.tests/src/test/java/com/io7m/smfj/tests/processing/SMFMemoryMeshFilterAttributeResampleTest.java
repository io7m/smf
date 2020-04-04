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

import com.io7m.smfj.core.SMFAttribute;
import com.io7m.smfj.core.SMFAttributeName;
import com.io7m.smfj.core.SMFHeader;
import com.io7m.smfj.core.SMFPartialLogged;
import com.io7m.smfj.processing.api.SMFAttributeArrayType;
import com.io7m.smfj.processing.api.SMFMemoryMesh;
import com.io7m.smfj.processing.api.SMFMemoryMeshFilterType;
import com.io7m.smfj.processing.api.SMFMemoryMeshProducer;
import com.io7m.smfj.processing.api.SMFMemoryMeshProducerType;
import com.io7m.smfj.processing.main.SMFMemoryMeshFilterAttributeResample;
import java.nio.file.FileSystem;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.io7m.smfj.tests.processing.SMFMemoryMeshFilterTesting.WarningsAllowed.*;

public final class SMFMemoryMeshFilterAttributeResampleTest extends
  SMFMemoryMeshFilterContract
{
  private static final Logger LOG =
    LoggerFactory.getLogger(SMFMemoryMeshFilterAttributeResampleTest.class);

  private FileSystem filesystem;

  @Test
  public void testParseWrong1()
  {
    final SMFPartialLogged<SMFMemoryMeshFilterType> r =
      SMFMemoryMeshFilterAttributeResample.parse(
        Optional.empty(),
        1,
        List.of());
    Assertions.assertTrue(r.isFailed());
  }

  @Test
  public void testParseWrong2()
  {
    final SMFPartialLogged<SMFMemoryMeshFilterType> r =
      SMFMemoryMeshFilterAttributeResample.parse(
        Optional.empty(),
        1,
        List.of("x", "<#@"));
    Assertions.assertTrue(r.isFailed());
  }

  @Test
  public void testParseWrong3()
  {
    final SMFPartialLogged<SMFMemoryMeshFilterType> r =
      SMFMemoryMeshFilterAttributeResample.parse(
        Optional.empty(),
        1,
        List.of("<#@", "y"));
    Assertions.assertTrue(r.isFailed());
  }

  @Test
  public void testParseWrong4()
  {
    final SMFPartialLogged<SMFMemoryMeshFilterType> r =
      SMFMemoryMeshFilterAttributeResample.parse(
        Optional.empty(),
        1,
        List.of(
          "x",
          "y",
          "z"));
    Assertions.assertTrue(r.isFailed());
  }

  @Test
  public void testParse()
  {
    final SMFPartialLogged<SMFMemoryMeshFilterType> r =
      SMFMemoryMeshFilterAttributeResample.parse(
        Optional.empty(),
        1,
        List.of("x", "32"));
    Assertions.assertTrue(r.isSucceeded());
    final SMFMemoryMeshFilterType c = r.get();
    Assertions.assertEquals(c.name(), "resample");
  }

  @Test
  public void testResampleNonexistent()
    throws Exception
  {
    final SMFMemoryMeshProducerType loader = SMFMemoryMeshProducer.create();

    try (var parser = SMFTestFiles.createParser(loader, "all.smft")) {
      SMFMemoryMeshFilterTesting.logEverything(LOG, loader, WARNINGS_DISALLOWED);
    }

    final SMFAttributeName name_source = SMFAttributeName.of("nonexistent");

    final SMFMemoryMeshFilterType filter =
      SMFMemoryMeshFilterAttributeResample.create(name_source, 32);

    final SMFPartialLogged<SMFMemoryMesh> r =
      filter.filter(this.createContext(), loader.mesh());
    Assertions.assertTrue(r.isFailed());

    r.errors().forEach(e -> {
      LOG.error("error: {}", e.message());
    });
  }



  @Test
  public void testResampleUnsupported()
    throws Exception
  {
    final SMFMemoryMeshProducerType loader = SMFMemoryMeshProducer.create();

    try (var parser = SMFTestFiles.createParser(loader, "all.smft")) {
      SMFMemoryMeshFilterTesting.logEverything(LOG, loader, WARNINGS_DISALLOWED);
    }

    final SMFAttributeName name_source = SMFAttributeName.of("f16_4");

    final SMFMemoryMeshFilterType filter =
      SMFMemoryMeshFilterAttributeResample.create(name_source, 23);

    final SMFPartialLogged<SMFMemoryMesh> r =
      filter.filter(this.createContext(), loader.mesh());
    Assertions.assertTrue(r.isFailed());

    r.errors().forEach(e -> {
      LOG.error("error: {}", e.message());
    });
  }

  @Test
  public void testResampleOK()
    throws Exception
  {
    final SMFMemoryMeshProducerType loader = SMFMemoryMeshProducer.create();

    try (var parser = SMFTestFiles.createParser(loader, "all.smft")) {
      SMFMemoryMeshFilterTesting.logEverything(LOG, loader, WARNINGS_DISALLOWED);
    }

    final SMFAttributeName name_source = SMFAttributeName.of("f16_4");

    final SMFMemoryMeshFilterType filter =
      SMFMemoryMeshFilterAttributeResample.create(name_source, 32);

    final SMFMemoryMesh mesh0 = loader.mesh();
    final SMFMemoryMesh mesh1 =
      filter.filter(this.createContext(), mesh0).get();
    final Map<SMFAttributeName, SMFAttributeArrayType> arrays0 = mesh0.arrays();
    final Map<SMFAttributeName, SMFAttributeArrayType> arrays1 = mesh1.arrays();
    final SMFHeader header0 = mesh0.header();
    final SMFHeader header1 = mesh1.header();

    Assertions.assertEquals(mesh0.triangles(), mesh1.triangles());
    Assertions.assertEquals(arrays0.size(), arrays1.size());
    Assertions.assertEquals(
      header0.attributesByName().size(),
      header1.attributesByName().size());
    Assertions.assertEquals(
      header0.attributesInOrder().size(),
      header1.attributesInOrder().size());
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
      if (Objects.equals(attr0.name(), name_source)) {
        Assertions.assertEquals(16L, attr0.componentSizeBits());
        Assertions.assertEquals(32L, attr1.componentSizeBits());
      } else {
        Assertions.assertEquals(attr0, attr1);
      }
    }
  }
}
