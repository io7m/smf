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

import com.io7m.smfj.core.SMFSchemaIdentifier;
import com.io7m.smfj.core.SMFSchemaName;
import com.io7m.smfj.parser.api.SMFParseError;
import com.io7m.smfj.parser.api.SMFParserSequentialType;
import com.io7m.smfj.processing.api.SMFMemoryMesh;
import com.io7m.smfj.processing.api.SMFMemoryMeshFilterType;
import com.io7m.smfj.processing.api.SMFMemoryMeshProducer;
import com.io7m.smfj.processing.api.SMFMemoryMeshProducerType;
import com.io7m.smfj.processing.api.SMFProcessingError;
import com.io7m.smfj.processing.main.SMFMemoryMeshFilterSchemaSet;
import io.vavr.collection.List;
import io.vavr.collection.Seq;
import io.vavr.control.Validation;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

public final class SMFMemoryMeshFilterSchemaSetTest extends
  SMFMemoryMeshFilterContract
{
  private static final Logger LOG;

  static {
    LOG = LoggerFactory.getLogger(SMFMemoryMeshFilterSchemaSetTest.class);
  }

  private static void checkMeshesSame(
    final SMFMemoryMesh mesh0,
    final SMFMemoryMesh mesh1)
  {
    Assertions.assertEquals(
      mesh0.header().attributesByName(), mesh1.header().attributesByName());
    Assertions.assertEquals(
      mesh0.header().attributesInOrder(), mesh1.header().attributesInOrder());
    Assertions.assertEquals(
      mesh0.header().coordinateSystem(), mesh1.header().coordinateSystem());
    Assertions.assertEquals(
      mesh0.metadata(), mesh1.metadata());
    Assertions.assertEquals(
      mesh0.arrays(), mesh1.arrays());
    Assertions.assertEquals(
      mesh0.triangles(), mesh1.triangles());
  }

  @Test
  public void testParseWrong1()
  {
    final Validation<Seq<SMFParseError>, SMFMemoryMeshFilterType> r =
      SMFMemoryMeshFilterSchemaSet.parse(
        Optional.empty(),
        1,
        List.of());
    Assertions.assertTrue(r.isInvalid());
  }

  @Test
  public void testParseWrong2()
  {
    final Validation<Seq<SMFParseError>, SMFMemoryMeshFilterType> r =
      SMFMemoryMeshFilterSchemaSet.parse(
        Optional.empty(),
        1,
        List.of("x", "<#@"));
    Assertions.assertTrue(r.isInvalid());
  }

  @Test
  public void testParseWrong3()
  {
    final Validation<Seq<SMFParseError>, SMFMemoryMeshFilterType> r =
      SMFMemoryMeshFilterSchemaSet.parse(
        Optional.empty(),
        1,
        List.of("<#@", "y"));
    Assertions.assertTrue(r.isInvalid());
  }

  @Test
  public void testParseWrong4()
  {
    final Validation<Seq<SMFParseError>, SMFMemoryMeshFilterType> r =
      SMFMemoryMeshFilterSchemaSet.parse(
        Optional.empty(),
        1,
        List.of(
          "x",
          "y",
          "z"));
    Assertions.assertTrue(r.isInvalid());
  }

  @Test
  public void testParseWrong5()
  {
    final Validation<Seq<SMFParseError>, SMFMemoryMeshFilterType> r =
      SMFMemoryMeshFilterSchemaSet.parse(
        Optional.empty(),
        1,
        List.of(
          "x",
          "float",
          "z",
          "32"));
    Assertions.assertTrue(r.isInvalid());
  }

  @Test
  public void testParseOk0()
  {
    final Validation<Seq<SMFParseError>, SMFMemoryMeshFilterType> r =
      SMFMemoryMeshFilterSchemaSet.parse(
        Optional.empty(),
        1,
        List.of("com.io7m.smf.example", "1", "2"));
    Assertions.assertTrue(r.isValid());
    final SMFMemoryMeshFilterType c = r.get();
    Assertions.assertEquals(c.name(), "schema-set");
  }

  @Test
  public void testSetOK()
    throws Exception
  {
    final SMFMemoryMeshProducerType loader = SMFMemoryMeshProducer.create();

    try (SMFParserSequentialType parser =
           SMFTestFiles.createParser(loader, "all.smft")) {
      // Nothing
    }

    final SMFSchemaIdentifier identifier =
      SMFSchemaIdentifier.builder()
        .setName(SMFSchemaName.of("com.io7m.schema"))
        .setVersionMajor(2)
        .setVersionMinor(3)
        .build();

    final SMFMemoryMeshFilterType filter =
      SMFMemoryMeshFilterSchemaSet.create(identifier);

    final Validation<Seq<SMFProcessingError>, SMFMemoryMesh> r =
      filter.filter(this.createContext(), loader.mesh());
    Assertions.assertTrue(r.isValid());
    Assertions.assertEquals(identifier, r.get().header().schemaIdentifier().get());

    checkMeshesSame(loader.mesh(), r.get());
  }
}
