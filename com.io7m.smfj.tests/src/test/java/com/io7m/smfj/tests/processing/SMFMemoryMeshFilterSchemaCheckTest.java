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

import com.io7m.smfj.core.SMFPartialLogged;
import com.io7m.smfj.core.SMFSchemaIdentifier;
import com.io7m.smfj.core.SMFSchemaName;
import com.io7m.smfj.parser.api.SMFParserSequentialType;
import com.io7m.smfj.processing.api.SMFMemoryMesh;
import com.io7m.smfj.processing.api.SMFMemoryMeshFilterType;
import com.io7m.smfj.processing.api.SMFMemoryMeshProducer;
import com.io7m.smfj.processing.api.SMFMemoryMeshProducerType;
import com.io7m.smfj.processing.main.SMFMemoryMeshFilterSchemaCheck;
import java.nio.file.FileSystem;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.io7m.smfj.tests.processing.SMFMemoryMeshFilterTesting.WarningsAllowed.WARNINGS_DISALLOWED;

public final class SMFMemoryMeshFilterSchemaCheckTest extends
  SMFMemoryMeshFilterContract
{
  private static final Logger LOG;

  static {
    LOG = LoggerFactory.getLogger(SMFMemoryMeshFilterSchemaCheckTest.class);
  }

  private FileSystem filesystem;

  @Test
  public void testParseWrong1()
  {
    final SMFPartialLogged<SMFMemoryMeshFilterType> r =
      SMFMemoryMeshFilterSchemaCheck.parse(
        Optional.empty(),
        1,
        List.of());
    Assertions.assertTrue(r.isFailed());
  }

  @Test
  public void testParseWrong2()
  {
    final SMFPartialLogged<SMFMemoryMeshFilterType> r =
      SMFMemoryMeshFilterSchemaCheck.parse(
        Optional.empty(),
        1,
        List.of("x", "<#@"));
    Assertions.assertTrue(r.isFailed());
  }

  @Test
  public void testParseWrong3()
  {
    final SMFPartialLogged<SMFMemoryMeshFilterType> r =
      SMFMemoryMeshFilterSchemaCheck.parse(
        Optional.empty(),
        1,
        List.of("<#@", "y"));
    Assertions.assertTrue(r.isFailed());
  }

  @Test
  public void testParseWrong4()
  {
    final SMFPartialLogged<SMFMemoryMeshFilterType> r =
      SMFMemoryMeshFilterSchemaCheck.parse(
        Optional.empty(),
        1,
        List.of(
          "x",
          "y",
          "z"));
    Assertions.assertTrue(r.isFailed());
  }

  @Test
  public void testParseWrong5()
  {
    final SMFPartialLogged<SMFMemoryMeshFilterType> r =
      SMFMemoryMeshFilterSchemaCheck.parse(
        Optional.empty(),
        1,
        List.of(
          "x",
          "float",
          "z",
          "32"));
    Assertions.assertTrue(r.isFailed());
  }

  @Test
  public void testParseOk0()
  {
    final SMFPartialLogged<SMFMemoryMeshFilterType> r =
      SMFMemoryMeshFilterSchemaCheck.parse(
        Optional.empty(),
        1,
        List.of("com.io7m.smf.example", "1", "2"));
    Assertions.assertTrue(r.isSucceeded());
    final SMFMemoryMeshFilterType c = r.get();
    Assertions.assertEquals(c.name(), "schema-check");
  }

  @Test
  public void testCheckOK()
    throws Exception
  {
    final SMFMemoryMeshProducerType loader = SMFMemoryMeshProducer.create();

    try (var parser =
           SMFTestFiles.createParser(loader, "all.smft")) {
      SMFMemoryMeshFilterTesting.logEverything(LOG, loader, WARNINGS_DISALLOWED);
    }

    final SMFSchemaIdentifier identifier =
      SMFSchemaIdentifier.builder()
        .setName(SMFSchemaName.of("com.io7m.smf.example"))
        .setVersionMajor(1)
        .setVersionMinor(2)
        .build();

    final SMFMemoryMeshFilterType filter =
      SMFMemoryMeshFilterSchemaCheck.create(identifier);

    final SMFPartialLogged<SMFMemoryMesh> r =
      filter.filter(this.createContext(), loader.mesh());

    this.dumpErrors(r);

    Assertions.assertTrue(r.isSucceeded());
    Assertions.assertEquals(loader.mesh(), r.get());
  }

  private void dumpErrors(
    final SMFPartialLogged<SMFMemoryMesh> r)
  {
    if (r.isFailed()) {
      r.errors().forEach(e -> LOG.error("parse: {}", e.fullMessage()));
    }
  }

  @Test
  public void testCheckFail0()
    throws Exception
  {
    final SMFMemoryMeshProducerType loader = SMFMemoryMeshProducer.create();

    try (var parser =
           SMFTestFiles.createParser(loader, "all.smft")) {
      SMFMemoryMeshFilterTesting.logEverything(LOG, loader, WARNINGS_DISALLOWED);
    }

    final SMFSchemaIdentifier identifier =
      SMFSchemaIdentifier.builder()
        .setName(SMFSchemaName.of("com.io7m.example"))
        .setVersionMajor(1)
        .setVersionMinor(2)
        .build();

    final SMFMemoryMeshFilterType filter =
      SMFMemoryMeshFilterSchemaCheck.create(identifier);

    final SMFPartialLogged<SMFMemoryMesh> r =
      filter.filter(this.createContext(), loader.mesh());
    Assertions.assertFalse(r.isSucceeded());

    r.errors().forEach(e -> {
      LOG.error("error: {}", e);
    });
  }

  @Test
  public void testCheckFail1()
    throws Exception
  {
    final SMFMemoryMeshProducerType loader = SMFMemoryMeshProducer.create();

    try (var parser =
           SMFTestFiles.createParser(loader, "all.smft")) {
      SMFMemoryMeshFilterTesting.logEverything(LOG, loader, WARNINGS_DISALLOWED);
    }

    final SMFSchemaIdentifier identifier =
      SMFSchemaIdentifier.builder()
        .setName(SMFSchemaName.of("com.io7m.example"))
        .setVersionMajor(1)
        .setVersionMinor(2)
        .build();

    final SMFMemoryMeshFilterType filter =
      SMFMemoryMeshFilterSchemaCheck.create(identifier);

    final SMFPartialLogged<SMFMemoryMesh> r =
      filter.filter(this.createContext(), loader.mesh());
    Assertions.assertFalse(r.isSucceeded());

    r.errors().forEach(e -> {
      LOG.error("error: {}", e);
    });
  }

  @Test
  public void testCheckFail2()
    throws Exception
  {
    final SMFMemoryMeshProducerType loader = SMFMemoryMeshProducer.create();

    try (var parser =
           SMFTestFiles.createParser(loader, "all.smft")) {
      SMFMemoryMeshFilterTesting.logEverything(LOG, loader, WARNINGS_DISALLOWED);
    }

    final SMFSchemaIdentifier identifier =
      SMFSchemaIdentifier.builder()
        .setName(SMFSchemaName.of("com.io7m.example"))
        .setVersionMajor(1)
        .setVersionMinor(2)
        .build();

    final SMFMemoryMeshFilterType filter =
      SMFMemoryMeshFilterSchemaCheck.create(identifier);

    final SMFPartialLogged<SMFMemoryMesh> r =
      filter.filter(this.createContext(), loader.mesh());
    Assertions.assertFalse(r.isSucceeded());

    r.errors().forEach(e -> {
      LOG.error("error: {}", e);
    });
  }

  @Test
  public void testCheckFail3()
    throws Exception
  {
    final SMFMemoryMeshProducerType loader = SMFMemoryMeshProducer.create();

    try (var parser =
           SMFTestFiles.createParser(loader, "all.smft")) {
      SMFMemoryMeshFilterTesting.logEverything(LOG, loader, WARNINGS_DISALLOWED);
    }

    final SMFSchemaIdentifier identifier =
      SMFSchemaIdentifier.builder()
        .setName(SMFSchemaName.of("com.io7m.example"))
        .setVersionMajor(1)
        .setVersionMinor(2)
        .build();

    final SMFMemoryMeshFilterType filter =
      SMFMemoryMeshFilterSchemaCheck.create(identifier);

    final SMFPartialLogged<SMFMemoryMesh> r =
      filter.filter(this.createContext(), loader.mesh());
    Assertions.assertFalse(r.isSucceeded());

    r.errors().forEach(e -> {
      LOG.error("error: {}", e);
    });
  }
}
