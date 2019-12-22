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
import com.io7m.smfj.processing.api.SMFFilterCommandContext;
import com.io7m.smfj.processing.api.SMFMemoryMesh;
import com.io7m.smfj.processing.api.SMFMemoryMeshFilterType;
import com.io7m.smfj.processing.api.SMFMemoryMeshProducer;
import com.io7m.smfj.processing.api.SMFMemoryMeshProducerType;
import com.io7m.smfj.processing.main.SMFMemoryMeshFilterEndiannessSet;
import java.io.OutputStream;
import java.nio.ByteOrder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.io7m.smfj.tests.processing.SMFMemoryMeshFilterTesting.WarningsAllowed.WARNINGS_DISALLOWED;

public final class SMFMemoryMeshFilterEndiannessSetTest
  extends SMFMemoryMeshFilterContract
{
  private static final Logger LOG =
    LoggerFactory.getLogger(SMFMemoryMeshFilterEndiannessSetTest.class);

  @Test
  public void testParseWrong1()
  {
    final SMFPartialLogged<SMFMemoryMeshFilterType> r =
      SMFMemoryMeshFilterEndiannessSet.parse(
        Optional.empty(),
        1,
        List.of());
    Assertions.assertTrue(r.isFailed());
  }

  @Test
  public void testParseWrong2()
  {
    final SMFPartialLogged<SMFMemoryMeshFilterType> r =
      SMFMemoryMeshFilterEndiannessSet.parse(
        Optional.empty(),
        1,
        List.of("x", "y"));
    Assertions.assertTrue(r.isFailed());
  }

  @Test
  public void testParseWrong3()
  {
    final SMFPartialLogged<SMFMemoryMeshFilterType> r =
      SMFMemoryMeshFilterEndiannessSet.parse(
        Optional.empty(),
        1,
        List.of("x", "y", "z", "w"));
    Assertions.assertTrue(r.isFailed());
  }

  @Test
  public void testParseWrong4()
  {
    final SMFPartialLogged<SMFMemoryMeshFilterType> r =
      SMFMemoryMeshFilterEndiannessSet.parse(
        Optional.empty(),
        1,
        List.of(
          "x",
          "0",
          "z"));
    Assertions.assertTrue(r.isFailed());
  }

  @Test
  public void testParseWrong5()
  {
    final SMFPartialLogged<SMFMemoryMeshFilterType> r =
      SMFMemoryMeshFilterEndiannessSet.parse(
        Optional.empty(),
        1,
        List.of(
          "0",
          "x",
          "z"));
    Assertions.assertTrue(r.isFailed());
  }

  @Test
  public void testParseWrong6()
  {
    final SMFPartialLogged<SMFMemoryMeshFilterType> r =
      SMFMemoryMeshFilterEndiannessSet.parse(
        Optional.empty(),
        1,
        List.of("x"));
    Assertions.assertTrue(r.isFailed());
  }

  @Test
  public void testParse0()
  {
    final SMFPartialLogged<SMFMemoryMeshFilterType> r =
      SMFMemoryMeshFilterEndiannessSet.parse(
        Optional.empty(),
        1,
        List.of("big"));
    Assertions.assertTrue(r.isSucceeded());
    final SMFMemoryMeshFilterType c = r.get();
    Assertions.assertEquals(c.name(), SMFMemoryMeshFilterEndiannessSet.NAME);
  }

  @Test
  public void testParse1()
  {
    final SMFPartialLogged<SMFMemoryMeshFilterType> r =
      SMFMemoryMeshFilterEndiannessSet.parse(
        Optional.empty(),
        1,
        List.of("little"));
    Assertions.assertTrue(r.isSucceeded());
    final SMFMemoryMeshFilterType c = r.get();
    Assertions.assertEquals(c.name(), SMFMemoryMeshFilterEndiannessSet.NAME);
  }

  @Test
  public void testLoadOK()
    throws Exception
  {
    final SMFMemoryMeshProducerType loader = SMFMemoryMeshProducer.create();

    try (var parser = SMFTestFiles.createParser(loader, "all.smft")) {
      SMFMemoryMeshFilterTesting.logEverything(LOG, loader, WARNINGS_DISALLOWED);
    }

    final Path path =
      this.filesystem.getPath("/data");

    final byte[] data = {(byte) 0x0, (byte) 0x1, (byte) 0x2, (byte) 0x3};

    Files.deleteIfExists(path);
    try (OutputStream out = Files.newOutputStream(path)) {
      out.write(data);
      out.flush();
    }

    final Path root =
      this.filesystem.getRootDirectories().iterator().next();
    final SMFFilterCommandContext context =
      SMFFilterCommandContext.of(root, root);

    final SMFMemoryMeshFilterType filter =
      SMFMemoryMeshFilterEndiannessSet.create(ByteOrder.LITTLE_ENDIAN);

    final SMFMemoryMesh mesh0 = loader.mesh();
    final SMFPartialLogged<SMFMemoryMesh> r = filter.filter(context, mesh0);
    Assertions.assertTrue(r.isSucceeded());

    final SMFMemoryMesh mesh1 = r.get();
    Assertions.assertEquals(mesh0.arrays(), mesh1.arrays());
    Assertions.assertEquals(mesh0.triangles(), mesh1.triangles());
    Assertions.assertEquals(ByteOrder.LITTLE_ENDIAN, mesh1.header().dataByteOrder());
  }
}
