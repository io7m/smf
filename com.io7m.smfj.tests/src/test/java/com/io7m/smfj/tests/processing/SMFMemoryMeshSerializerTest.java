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

import com.io7m.jtensors.core.unparameterized.vectors.Vector2D;
import com.io7m.jtensors.core.unparameterized.vectors.Vector2L;
import com.io7m.jtensors.core.unparameterized.vectors.Vector3D;
import com.io7m.jtensors.core.unparameterized.vectors.Vector3L;
import com.io7m.jtensors.core.unparameterized.vectors.Vector4D;
import com.io7m.jtensors.core.unparameterized.vectors.Vector4L;
import com.io7m.smfj.core.SMFAttributeName;
import com.io7m.smfj.core.SMFFormatVersion;
import com.io7m.smfj.core.SMFVoid;
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
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.io7m.smfj.core.SMFVoid.void_;
import static com.io7m.smfj.tests.processing.SMFMemoryMeshFilterTesting.WarningsAllowed.WARNINGS_DISALLOWED;
import static java.nio.file.StandardOpenOption.CREATE;
import static java.nio.file.StandardOpenOption.TRUNCATE_EXISTING;

public final class SMFMemoryMeshSerializerTest
{
  private static final Logger LOG;

  static {
    LOG = LoggerFactory.getLogger(SMFMemoryMeshSerializerTest.class);
  }

  private static SMFVoid checkFloat4(
    final SMFAttributeArrayFloating4Type a0,
    final SMFAttributeArrayFloating4Type a1)
  {
    final List<Vector4D> v0 = a0.values();
    final List<Vector4D> v1 = a1.values();

    for (int index = 0; index < v0.size(); ++index) {
      Assertions.assertEquals(v0.get(index), v1.get(index));
    }

    return void_();
  }

  private static SMFVoid checkFloat3(
    final SMFAttributeArrayFloating3Type a0,
    final SMFAttributeArrayFloating3Type a1)
  {
    final List<Vector3D> v0 = a0.values();
    final List<Vector3D> v1 = a1.values();

    for (int index = 0; index < v0.size(); ++index) {
      Assertions.assertEquals(v0.get(index), v1.get(index));
    }

    return void_();
  }

  private static SMFVoid checkFloat2(
    final SMFAttributeArrayFloating2Type a0,
    final SMFAttributeArrayFloating2Type a1)
  {
    final List<Vector2D> v0 = a0.values();
    final List<Vector2D> v1 = a1.values();

    for (int index = 0; index < v0.size(); ++index) {
      Assertions.assertEquals(v0.get(index), v1.get(index));
    }

    return void_();
  }

  private static SMFVoid checkFloat1(
    final SMFAttributeArrayFloating1Type a0,
    final SMFAttributeArrayFloating1Type a1)
  {
    final List<Double> v0 = a0.values();
    final List<Double> v1 = a1.values();

    for (int index = 0; index < v0.size(); ++index) {
      Assertions.assertEquals(v0.get(index), v1.get(index));
    }

    return void_();
  }

  private static SMFVoid checkUnsigned4(
    final SMFAttributeArrayIntegerUnsigned4Type a0,
    final SMFAttributeArrayIntegerUnsigned4Type a1)
  {
    final List<Vector4L> v0 = a0.values();
    final List<Vector4L> v1 = a1.values();

    for (int index = 0; index < v0.size(); ++index) {
      Assertions.assertEquals(v0.get(index), v1.get(index));
    }

    return void_();
  }

  private static SMFVoid checkUnsigned3(
    final SMFAttributeArrayIntegerUnsigned3Type a0,
    final SMFAttributeArrayIntegerUnsigned3Type a1)
  {
    final List<Vector3L> v0 = a0.values();
    final List<Vector3L> v1 = a1.values();

    for (int index = 0; index < v0.size(); ++index) {
      Assertions.assertEquals(v0.get(index), v1.get(index));
    }

    return void_();
  }

  private static SMFVoid checkUnsigned2(
    final SMFAttributeArrayIntegerUnsigned2Type a0,
    final SMFAttributeArrayIntegerUnsigned2Type a1)
  {
    final List<Vector2L> v0 = a0.values();
    final List<Vector2L> v1 = a1.values();

    for (int index = 0; index < v0.size(); ++index) {
      Assertions.assertEquals(v0.get(index), v1.get(index));
    }

    return void_();
  }

  private static SMFVoid checkUnsigned1(
    final SMFAttributeArrayIntegerUnsigned1Type a0,
    final SMFAttributeArrayIntegerUnsigned1Type a1)
  {
    final List<Long> v0 = a0.values();
    final List<Long> v1 = a1.values();

    for (int index = 0; index < v0.size(); ++index) {
      Assertions.assertEquals(v0.get(index), v1.get(index));
    }

    return void_();
  }

  private static SMFVoid checkSigned4(
    final SMFAttributeArrayIntegerSigned4Type a0,
    final SMFAttributeArrayIntegerSigned4Type a1)
  {
    final List<Vector4L> v0 = a0.values();
    final List<Vector4L> v1 = a1.values();

    for (int index = 0; index < v0.size(); ++index) {
      Assertions.assertEquals(v0.get(index), v1.get(index));
    }

    return void_();
  }

  private static SMFVoid checkSigned3(
    final SMFAttributeArrayIntegerSigned3Type a0,
    final SMFAttributeArrayIntegerSigned3Type a1)
  {
    final List<Vector3L> v0 = a0.values();
    final List<Vector3L> v1 = a1.values();

    for (int index = 0; index < v0.size(); ++index) {
      Assertions.assertEquals(v0.get(index), v1.get(index));
    }

    return void_();
  }

  private static SMFVoid checkSigned2(
    final SMFAttributeArrayIntegerSigned2Type a0,
    final SMFAttributeArrayIntegerSigned2Type a1)
  {
    final List<Vector2L> v0 = a0.values();
    final List<Vector2L> v1 = a1.values();

    for (int index = 0; index < v0.size(); ++index) {
      Assertions.assertEquals(v0.get(index), v1.get(index));
    }

    return void_();
  }

  private static SMFVoid checkSigned1(
    final SMFAttributeArrayIntegerSigned1Type a0,
    final SMFAttributeArrayIntegerSigned1Type a1)
  {
    final List<Long> v0 = a0.values();
    final List<Long> v1 = a1.values();

    for (int index = 0; index < v0.size(); ++index) {
      Assertions.assertEquals(v0.get(index), v1.get(index));
    }

    return void_();
  }

  @Test
  public void testAll()
    throws Exception
  {
    final SMFMemoryMeshProducerType loader0 = SMFMemoryMeshProducer.create();

    try (SMFParserSequentialType parser0 =
           SMFTestFiles.createParser(loader0, "all.smft")) {
      SMFMemoryMeshFilterTesting.logEverything(LOG, loader0, WARNINGS_DISALLOWED);
    }

    Assertions.assertTrue(loader0.errors().isEmpty());

    final SMFMemoryMesh mesh0 = loader0.mesh();
    final SMFFormatText fmt = new SMFFormatText();
    final Path tmp = Files.createTempFile("smf-memory-", ".smft");
    try (OutputStream stream =
           Files.newOutputStream(tmp, CREATE, TRUNCATE_EXISTING)) {
      final SMFFormatVersion version = SMFFormatVersion.of(1, 0);
      try (SMFSerializerType serial =
             fmt.serializerCreate(version, tmp.toUri(), stream)) {
        SMFMemoryMeshSerializer.serialize(mesh0, serial);
      }
    }

    final SMFMemoryMeshProducerType loader1 = SMFMemoryMeshProducer.create();
    try (InputStream stream = Files.newInputStream(tmp)) {
      try (SMFParserSequentialType parser1 =
             fmt.parserCreateSequential(loader1, tmp.toUri(), stream)) {
        parser1.parse();
      }
    }

    final SMFMemoryMesh mesh1 = loader1.mesh();

    LOG.debug("mesh0: {} ", mesh0);
    LOG.debug("mesh1: {} ", mesh1);

    Assertions.assertEquals(mesh0.header(), mesh1.header());
    Assertions.assertEquals(mesh0.triangles(), mesh1.triangles());

    for (final Map.Entry<SMFAttributeName, SMFAttributeArrayType> pair : mesh0.arrays().entrySet()) {
      final SMFAttributeName name = pair.getKey();
      final SMFAttributeArrayType a0 = pair.getValue();
      final SMFAttributeArrayType a1 = mesh1.arrays().get(name);

      a0.matchArray(
        void_(),
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
