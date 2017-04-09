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
import com.io7m.jtensors.core.unparameterized.vectors.Vector2D;
import com.io7m.jtensors.core.unparameterized.vectors.Vector2L;
import com.io7m.jtensors.core.unparameterized.vectors.Vector3D;
import com.io7m.jtensors.core.unparameterized.vectors.Vector3L;
import com.io7m.jtensors.core.unparameterized.vectors.Vector4D;
import com.io7m.jtensors.core.unparameterized.vectors.Vector4L;
import com.io7m.smfj.core.SMFAttributeName;
import com.io7m.smfj.core.SMFFormatVersion;
import com.io7m.smfj.format.text.SMFFormatText;
import com.io7m.smfj.parser.api.SMFParserSequentialType;
import com.io7m.smfj.processing.api.SMFAttributeArrayFloating1Type;
import com.io7m.smfj.processing.api.SMFAttributeArrayFloating2Type;
import com.io7m.smfj.processing.api.SMFAttributeArrayFloating3Type;
import com.io7m.smfj.processing.api.SMFAttributeArrayFloating4Type;
import com.io7m.smfj.processing.api.SMFAttributeArrayIntegerSigned1Type;
import com.io7m.smfj.processing.api.SMFAttributeArrayIntegerSigned2Type;
import com.io7m.smfj.processing.api.SMFAttributeArrayIntegerSigned3Type;
import com.io7m.smfj.processing.api.SMFAttributeArrayIntegerSigned4Type;
import com.io7m.smfj.processing.api.SMFAttributeArrayIntegerUnsigned1Type;
import com.io7m.smfj.processing.api.SMFAttributeArrayIntegerUnsigned2Type;
import com.io7m.smfj.processing.api.SMFAttributeArrayIntegerUnsigned3Type;
import com.io7m.smfj.processing.api.SMFAttributeArrayIntegerUnsigned4Type;
import com.io7m.smfj.processing.api.SMFAttributeArrayType;
import com.io7m.smfj.processing.api.SMFMemoryMesh;
import com.io7m.smfj.processing.api.SMFMemoryMeshProducer;
import com.io7m.smfj.processing.api.SMFMemoryMeshProducerType;
import com.io7m.smfj.processing.api.SMFMemoryMeshSerializer;
import com.io7m.smfj.serializer.api.SMFSerializerType;
import javaslang.Tuple2;
import javaslang.collection.Vector;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

import static com.io7m.jfunctional.Unit.unit;

public final class SMFMemoryMeshSerializerTest
{
  private static final Logger LOG;

  static {
    LOG = LoggerFactory.getLogger(SMFMemoryMeshSerializerTest.class);
  }

  private static Unit checkFloat4(
    final SMFAttributeArrayFloating4Type a0,
    final SMFAttributeArrayFloating4Type a1)
  {
    final Vector<Vector4D> v0 = a0.values();
    final Vector<Vector4D> v1 = a1.values();

    for (int index = 0; index < v0.length(); ++index) {
      Assert.assertEquals(v0.get(index), v1.get(index));
    }

    return unit();
  }

  private static Unit checkFloat3(
    final SMFAttributeArrayFloating3Type a0,
    final SMFAttributeArrayFloating3Type a1)
  {
    final Vector<Vector3D> v0 = a0.values();
    final Vector<Vector3D> v1 = a1.values();

    for (int index = 0; index < v0.length(); ++index) {
      Assert.assertEquals(v0.get(index), v1.get(index));
    }

    return unit();
  }

  private static Unit checkFloat2(
    final SMFAttributeArrayFloating2Type a0,
    final SMFAttributeArrayFloating2Type a1)
  {
    final Vector<Vector2D> v0 = a0.values();
    final Vector<Vector2D> v1 = a1.values();

    for (int index = 0; index < v0.length(); ++index) {
      Assert.assertEquals(v0.get(index), v1.get(index));
    }

    return unit();
  }

  private static Unit checkFloat1(
    final SMFAttributeArrayFloating1Type a0,
    final SMFAttributeArrayFloating1Type a1)
  {
    final Vector<Double> v0 = a0.values();
    final Vector<Double> v1 = a1.values();

    for (int index = 0; index < v0.length(); ++index) {
      Assert.assertEquals(v0.get(index), v1.get(index));
    }

    return unit();
  }

  private static Unit checkUnsigned4(
    final SMFAttributeArrayIntegerUnsigned4Type a0,
    final SMFAttributeArrayIntegerUnsigned4Type a1)
  {
    final Vector<Vector4L> v0 = a0.values();
    final Vector<Vector4L> v1 = a1.values();

    for (int index = 0; index < v0.length(); ++index) {
      Assert.assertEquals(v0.get(index), v1.get(index));
    }

    return unit();
  }

  private static Unit checkUnsigned3(
    final SMFAttributeArrayIntegerUnsigned3Type a0,
    final SMFAttributeArrayIntegerUnsigned3Type a1)
  {
    final Vector<Vector3L> v0 = a0.values();
    final Vector<Vector3L> v1 = a1.values();

    for (int index = 0; index < v0.length(); ++index) {
      Assert.assertEquals(v0.get(index), v1.get(index));
    }

    return unit();
  }

  private static Unit checkUnsigned2(
    final SMFAttributeArrayIntegerUnsigned2Type a0,
    final SMFAttributeArrayIntegerUnsigned2Type a1)
  {
    final Vector<Vector2L> v0 = a0.values();
    final Vector<Vector2L> v1 = a1.values();

    for (int index = 0; index < v0.length(); ++index) {
      Assert.assertEquals(v0.get(index), v1.get(index));
    }

    return unit();
  }

  private static Unit checkUnsigned1(
    final SMFAttributeArrayIntegerUnsigned1Type a0,
    final SMFAttributeArrayIntegerUnsigned1Type a1)
  {
    final Vector<Long> v0 = a0.values();
    final Vector<Long> v1 = a1.values();

    for (int index = 0; index < v0.length(); ++index) {
      Assert.assertEquals(v0.get(index), v1.get(index));
    }

    return unit();
  }

  private static Unit checkSigned4(
    final SMFAttributeArrayIntegerSigned4Type a0,
    final SMFAttributeArrayIntegerSigned4Type a1)
  {
    final Vector<Vector4L> v0 = a0.values();
    final Vector<Vector4L> v1 = a1.values();

    for (int index = 0; index < v0.length(); ++index) {
      Assert.assertEquals(v0.get(index), v1.get(index));
    }

    return unit();
  }

  private static Unit checkSigned3(
    final SMFAttributeArrayIntegerSigned3Type a0,
    final SMFAttributeArrayIntegerSigned3Type a1)
  {
    final Vector<Vector3L> v0 = a0.values();
    final Vector<Vector3L> v1 = a1.values();

    for (int index = 0; index < v0.length(); ++index) {
      Assert.assertEquals(v0.get(index), v1.get(index));
    }

    return unit();
  }

  private static Unit checkSigned2(
    final SMFAttributeArrayIntegerSigned2Type a0,
    final SMFAttributeArrayIntegerSigned2Type a1)
  {
    final Vector<Vector2L> v0 = a0.values();
    final Vector<Vector2L> v1 = a1.values();

    for (int index = 0; index < v0.length(); ++index) {
      Assert.assertEquals(v0.get(index), v1.get(index));
    }

    return unit();
  }

  private static Unit checkSigned1(
    final SMFAttributeArrayIntegerSigned1Type a0,
    final SMFAttributeArrayIntegerSigned1Type a1)
  {
    final Vector<Long> v0 = a0.values();
    final Vector<Long> v1 = a1.values();

    for (int index = 0; index < v0.length(); ++index) {
      Assert.assertEquals(v0.get(index), v1.get(index));
    }

    return unit();
  }

  @Test
  public void testAll()
    throws Exception
  {
    final SMFMemoryMeshProducerType loader0 = SMFMemoryMeshProducer.create();

    try (final SMFParserSequentialType parser0 =
           SMFTestFiles.createParser(loader0, "all.smft")) {
      // Nothing
    }

    Assert.assertTrue(loader0.errors().isEmpty());

    final SMFMemoryMesh mesh0 = loader0.mesh();
    final SMFFormatText fmt = new SMFFormatText();
    final Path tmp = Files.createTempFile("smf-memory-", ".smft");
    try (final OutputStream stream =
           Files.newOutputStream(
             tmp,
             StandardOpenOption.CREATE,
             StandardOpenOption.TRUNCATE_EXISTING)) {
      try (final SMFSerializerType serial =
             fmt.serializerCreate(SMFFormatVersion.of(1, 0), tmp, stream)) {
        SMFMemoryMeshSerializer.serialize(mesh0, serial);
      }
    }

    final SMFMemoryMeshProducerType loader1 = SMFMemoryMeshProducer.create();
    try (final InputStream stream = Files.newInputStream(tmp)) {
      try (final SMFParserSequentialType parser1 =
             fmt.parserCreateSequential(loader1, tmp, stream)) {
        parser1.parseHeader();
        parser1.parseData();
      }
    }

    final SMFMemoryMesh mesh1 = loader1.mesh();

    LOG.debug("mesh0: {} ", mesh0);
    LOG.debug("mesh1: {} ", mesh1);

    Assert.assertEquals(mesh0.header(), mesh1.header());
    Assert.assertEquals(mesh0.triangles(), mesh1.triangles());

    for (final Tuple2<SMFAttributeName, SMFAttributeArrayType> pair : mesh0.arrays()) {
      final SMFAttributeName name = pair._1;
      final SMFAttributeArrayType a0 = pair._2;
      final SMFAttributeArrayType a1 = mesh1.arrays().get(name).get();

      a0.matchArray(
        unit(),
        (x, y) -> checkFloat4(y, (SMFAttributeArrayFloating4Type) a1),
        (x, y) -> checkFloat3(y, (SMFAttributeArrayFloating3Type) a1),
        (x, y) -> checkFloat2(y, (SMFAttributeArrayFloating2Type) a1),
        (x, y) -> checkFloat1(y, (SMFAttributeArrayFloating1Type) a1),
        (x, y) -> checkUnsigned4(y, (SMFAttributeArrayIntegerUnsigned4Type) a1),
        (x, y) -> checkUnsigned3(y, (SMFAttributeArrayIntegerUnsigned3Type) a1),
        (x, y) -> checkUnsigned2(y, (SMFAttributeArrayIntegerUnsigned2Type) a1),
        (x, y) -> checkUnsigned1(y, (SMFAttributeArrayIntegerUnsigned1Type) a1),
        (x, y) -> checkSigned4(y, (SMFAttributeArrayIntegerSigned4Type) a1),
        (x, y) -> checkSigned3(y, (SMFAttributeArrayIntegerSigned3Type) a1),
        (x, y) -> checkSigned2(y, (SMFAttributeArrayIntegerSigned2Type) a1),
        (x, y) -> checkSigned1(y, (SMFAttributeArrayIntegerSigned1Type) a1));
    }
  }
}
