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
import com.io7m.smfj.processing.api.SMFProcessingError;
import com.io7m.smfj.processing.main.SMFMemoryMeshFilterSchemaValidate;
import io.vavr.collection.List;
import io.vavr.collection.Seq;
import io.vavr.control.Validation;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

public final class SMFMemoryMeshFilterSchemaValidateTest extends
  SMFMemoryMeshFilterContract
{
  private static final Logger LOG;

  static {
    LOG = LoggerFactory.getLogger(SMFMemoryMeshFilterSchemaValidateTest.class);
  }

  @Test
  public void testParseWrong1()
  {
    final Validation<Seq<SMFParseError>, SMFMemoryMeshFilterType> r =
      SMFMemoryMeshFilterSchemaValidate.parse(
        Optional.empty(),
        1,
        List.of());
    Assertions.assertTrue(r.isInvalid());
  }

  @Test
  public void testParseWrong3()
  {
    final Validation<Seq<SMFParseError>, SMFMemoryMeshFilterType> r =
      SMFMemoryMeshFilterSchemaValidate.parse(
        Optional.empty(),
        1,
        List.of("<#@", "y"));
    Assertions.assertTrue(r.isInvalid());
  }

  @Test
  public void testParseOk0()
  {
    final Validation<Seq<SMFParseError>, SMFMemoryMeshFilterType> r =
      SMFMemoryMeshFilterSchemaValidate.parse(
        Optional.empty(),
        1,
        List.of("file.txt"));
    Assertions.assertTrue(r.isValid());
    final SMFMemoryMeshFilterType c = r.get();
    Assertions.assertEquals(c.name(), "schema-validate");
  }

  @Test
  public void testCheckOK()
    throws Exception
  {
    final SMFMemoryMeshProducerType loader = SMFMemoryMeshProducer.create();

    try (SMFParserSequentialType parser =
           SMFTestFiles.createParser(loader, "all.smft")) {
      // Nothing
    }

    final Path file = this.filesystem.getPath("/schema.smfs");
    Files.deleteIfExists(file);

    try (InputStream in = SMFTestFiles.resourceStream("all.smfs")) {
      try (OutputStream out = Files.newOutputStream(file)) {
        IOUtils.copy(in, out);
        out.flush();
      }
    }

    final Path root = this.filesystem.getRootDirectories().iterator().next();
    final SMFFilterCommandContext context = SMFFilterCommandContext.of(
      root,
      root);

    final SMFMemoryMeshFilterType filter =
      SMFMemoryMeshFilterSchemaValidate.create(file);

    final Validation<Seq<SMFProcessingError>, SMFMemoryMesh> r =
      filter.filter(context, loader.mesh());

    if (r.isInvalid()) {
      r.getError().forEach(e -> LOG.error(e.message()));
    }

    Assertions.assertTrue(r.isValid());
    Assertions.assertEquals(loader.mesh(), r.get());
  }

  @Test
  public void testCheckNotOK()
    throws Exception
  {
    final SMFMemoryMeshProducerType loader = SMFMemoryMeshProducer.create();

    try (SMFParserSequentialType parser =
           SMFTestFiles.createParser(loader, "all.smft")) {
      // Nothing
    }

    final Path file = this.filesystem.getPath("/schema.smfs");
    Files.deleteIfExists(file);

    try (InputStream in = SMFTestFiles.resourceStream("empty.smfs")) {
      try (OutputStream out = Files.newOutputStream(file)) {
        IOUtils.copy(in, out);
        out.flush();
      }
    }

    final Path root = this.filesystem.getRootDirectories().iterator().next();
    final SMFFilterCommandContext context = SMFFilterCommandContext.of(
      root,
      root);

    final SMFMemoryMeshFilterType filter =
      SMFMemoryMeshFilterSchemaValidate.create(file);

    final Validation<Seq<SMFProcessingError>, SMFMemoryMesh> r =
      filter.filter(context, loader.mesh());

    if (r.isInvalid()) {
      r.getError().forEach(e -> LOG.error(e.message()));
    }

    Assertions.assertFalse(r.isValid());
  }

  @Test
  public void testCheckNonexistent()
    throws Exception
  {
    final SMFMemoryMeshProducerType loader = SMFMemoryMeshProducer.create();

    try (SMFParserSequentialType parser =
           SMFTestFiles.createParser(loader, "all.smft")) {
      // Nothing
    }

    final Path file = this.filesystem.getPath("/schema.smfs");
    Files.deleteIfExists(file);

    final Path root = this.filesystem.getRootDirectories().iterator().next();
    final SMFFilterCommandContext context = SMFFilterCommandContext.of(
      root,
      root);

    final SMFMemoryMeshFilterType filter =
      SMFMemoryMeshFilterSchemaValidate.create(file);

    final Validation<Seq<SMFProcessingError>, SMFMemoryMesh> r =
      filter.filter(context, loader.mesh());

    if (r.isInvalid()) {
      r.getError().forEach(e -> LOG.error(e.message()));
    }

    Assertions.assertFalse(r.isValid());
  }
}
