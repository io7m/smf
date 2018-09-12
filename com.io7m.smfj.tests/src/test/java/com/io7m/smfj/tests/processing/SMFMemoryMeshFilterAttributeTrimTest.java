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
import com.io7m.smfj.parser.api.SMFParseError;
import com.io7m.smfj.parser.api.SMFParserSequentialType;
import com.io7m.smfj.processing.api.SMFMemoryMesh;
import com.io7m.smfj.processing.api.SMFMemoryMeshFilterType;
import com.io7m.smfj.processing.api.SMFMemoryMeshProducer;
import com.io7m.smfj.processing.api.SMFMemoryMeshProducerType;
import com.io7m.smfj.processing.api.SMFProcessingError;
import com.io7m.smfj.processing.main.SMFMemoryMeshFilterAttributeTrim;
import io.vavr.collection.HashSet;
import io.vavr.collection.List;
import io.vavr.collection.Seq;
import io.vavr.control.Validation;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.FileSystem;
import java.util.Optional;

public final class SMFMemoryMeshFilterAttributeTrimTest extends
  SMFMemoryMeshFilterContract
{
  private static final Logger LOG;

  static {
    LOG = LoggerFactory.getLogger(SMFMemoryMeshFilterAttributeTrimTest.class);
  }

  private FileSystem filesystem;

  @Test
  public void testParseWrong0()
  {
    final Validation<Seq<SMFParseError>, SMFMemoryMeshFilterType> r =
      SMFMemoryMeshFilterAttributeTrim.parse(Optional.empty(), 1, List.empty());
    Assertions.assertTrue(r.isInvalid());
  }

  @Test
  public void testParseWrong1()
  {
    final Validation<Seq<SMFParseError>, SMFMemoryMeshFilterType> r =
      SMFMemoryMeshFilterAttributeTrim.parse(
        Optional.empty(),
        1,
        List.of());
    Assertions.assertTrue(r.isInvalid());
  }

  @Test
  public void testParseWrong2()
  {
    final Validation<Seq<SMFParseError>, SMFMemoryMeshFilterType> r =
      SMFMemoryMeshFilterAttributeTrim.parse(
        Optional.empty(),
        1,
        List.of("~#@"));
    Assertions.assertTrue(r.isInvalid());
  }

  @Test
  public void testParse0()
  {
    final Validation<Seq<SMFParseError>, SMFMemoryMeshFilterType> r =
      SMFMemoryMeshFilterAttributeTrim.parse(
        Optional.empty(),
        1,
        List.of("x"));
    Assertions.assertTrue(r.isValid());
    final SMFMemoryMeshFilterType c = r.get();
    Assertions.assertEquals(c.name(), "trim");
  }

  @Test
  public void testParse1()
  {
    final Validation<Seq<SMFParseError>, SMFMemoryMeshFilterType> r =
      SMFMemoryMeshFilterAttributeTrim.parse(
        Optional.empty(),
        1,
        List.of("x", "y", "z"));
    Assertions.assertTrue(r.isValid());
    final SMFMemoryMeshFilterType c = r.get();
    Assertions.assertEquals(c.name(), "trim");
  }

  @Test
  public void testTrimNonexistent()
    throws Exception
  {
    final SMFMemoryMeshProducerType loader = SMFMemoryMeshProducer.create();

    try (SMFParserSequentialType parser =
           SMFTestFiles.createParser(loader, "all.smft")) {
      // Nothing
    }

    final SMFAttributeName name_source = SMFAttributeName.of("nonexistent");

    final SMFMemoryMeshFilterType filter =
      SMFMemoryMeshFilterAttributeTrim.create(HashSet.of(name_source));

    final Validation<Seq<SMFProcessingError>, SMFMemoryMesh> r =
      filter.filter(this.createContext(), loader.mesh());
    Assertions.assertTrue(r.isInvalid());

    r.getError().map(e -> {
      LOG.error("error: {}", e.message());
      return Unit.unit();
    });
  }

  @Test
  public void testTrimOK()
    throws Exception
  {
    final SMFMemoryMeshProducerType loader = SMFMemoryMeshProducer.create();

    try (SMFParserSequentialType parser =
           SMFTestFiles.createParser(loader, "all.smft")) {
      // Nothing
    }

    final SMFAttributeName name_source =
      SMFAttributeName.of("f16_4");

    final SMFMemoryMeshFilterType filter =
      SMFMemoryMeshFilterAttributeTrim.create(HashSet.of(name_source));

    final SMFMemoryMesh mesh0 = loader.mesh();
    final Validation<Seq<SMFProcessingError>, SMFMemoryMesh> r =
      filter.filter(this.createContext(), mesh0);
    Assertions.assertTrue(r.isValid());

    final SMFMemoryMesh mesh1 = r.get();
    Assertions.assertEquals(1L, (long) mesh1.arrays().size());
    Assertions.assertEquals(1L, (long) mesh1.header().attributesInOrder().size());
    Assertions.assertEquals(1L, (long) mesh1.header().attributesByName().size());

    Assertions.assertTrue(
      mesh1.header().attributesByName().containsKey(name_source));

    Assertions.assertEquals(
      mesh0.arrays().get(name_source).get(),
      mesh1.arrays().get(name_source).get());
  }
}
