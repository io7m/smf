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
import com.io7m.smfj.core.SMFErrorType;
import com.io7m.smfj.core.SMFSchemaName;
import com.io7m.smfj.parser.api.SMFParserSequentialType;
import com.io7m.smfj.processing.api.SMFAttributeArrayFloating1;
import com.io7m.smfj.processing.api.SMFAttributeArrayFloating2;
import com.io7m.smfj.processing.api.SMFAttributeArrayFloating3;
import com.io7m.smfj.processing.api.SMFAttributeArrayFloating4;
import com.io7m.smfj.processing.api.SMFAttributeArrayIntegerSigned1;
import com.io7m.smfj.processing.api.SMFAttributeArrayIntegerSigned2;
import com.io7m.smfj.processing.api.SMFAttributeArrayIntegerSigned3;
import com.io7m.smfj.processing.api.SMFAttributeArrayIntegerSigned4;
import com.io7m.smfj.processing.api.SMFAttributeArrayIntegerUnsigned1;
import com.io7m.smfj.processing.api.SMFAttributeArrayIntegerUnsigned2;
import com.io7m.smfj.processing.api.SMFAttributeArrayIntegerUnsigned3;
import com.io7m.smfj.processing.api.SMFAttributeArrayIntegerUnsigned4;
import com.io7m.smfj.processing.api.SMFAttributeArrayType;
import com.io7m.smfj.processing.api.SMFMemoryMesh;
import com.io7m.smfj.processing.api.SMFMemoryMeshProducer;
import com.io7m.smfj.processing.api.SMFMemoryMeshProducerType;
import com.io7m.smfj.processing.api.SMFMetadata;
import io.vavr.collection.Map;
import io.vavr.collection.Vector;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public final class SMFMemoryMeshProducerTest
{
  private static void checkVector4F(
    final Map<SMFAttributeName, SMFAttributeArrayType> arrays,
    final String name)
  {
    final SMFAttributeArrayFloating4 a = (SMFAttributeArrayFloating4)
      arrays.get(SMFAttributeName.of(name)).get();
    {
      final Vector4D v = a.values().get(0);
      Assertions.assertEquals(-127.0, v.x(), 0.0001);
      Assertions.assertEquals(0.0, v.y(), 0.0001);
      Assertions.assertEquals(1.0, v.z(), 0.0001);
      Assertions.assertEquals(127.0, v.w(), 0.0001);
    }
    {
      final Vector4D v = a.values().get(1);
      Assertions.assertEquals(-127.0, v.x(), 0.0001);
      Assertions.assertEquals(0.0, v.y(), 0.0001);
      Assertions.assertEquals(1.0, v.z(), 0.0001);
      Assertions.assertEquals(127.0, v.w(), 0.0001);
    }
    {
      final Vector4D v = a.values().get(2);
      Assertions.assertEquals(-127.0, v.x(), 0.0001);
      Assertions.assertEquals(0.0, v.y(), 0.0001);
      Assertions.assertEquals(1.0, v.z(), 0.0001);
      Assertions.assertEquals(127.0, v.w(), 0.0001);
    }
  }

  private static void checkVector3F(
    final Map<SMFAttributeName, SMFAttributeArrayType> arrays,
    final String name)
  {
    final SMFAttributeArrayFloating3 a = (SMFAttributeArrayFloating3)
      arrays.get(SMFAttributeName.of(name)).get();
    {
      final Vector3D v = a.values().get(0);
      Assertions.assertEquals(-127.0, v.x(), 0.0001);
      Assertions.assertEquals(0.0, v.y(), 0.0001);
      Assertions.assertEquals(127.0, v.z(), 0.0001);
    }
    {
      final Vector3D v = a.values().get(1);
      Assertions.assertEquals(-127.0, v.x(), 0.0001);
      Assertions.assertEquals(0.0, v.y(), 0.0001);
      Assertions.assertEquals(127.0, v.z(), 0.0001);
    }
    {
      final Vector3D v = a.values().get(2);
      Assertions.assertEquals(-127.0, v.x(), 0.0001);
      Assertions.assertEquals(0.0, v.y(), 0.0001);
      Assertions.assertEquals(127.0, v.z(), 0.0001);
    }
  }

  private static void checkVector2F(
    final Map<SMFAttributeName, SMFAttributeArrayType> arrays,
    final String name)
  {
    final SMFAttributeArrayFloating2 a = (SMFAttributeArrayFloating2)
      arrays.get(SMFAttributeName.of(name)).get();
    {
      final Vector2D v = a.values().get(0);
      Assertions.assertEquals(-127.0, v.x(), 0.0001);
      Assertions.assertEquals(127.0, v.y(), 0.0001);
    }
    {
      final Vector2D v = a.values().get(1);
      Assertions.assertEquals(-127.0, v.x(), 0.0001);
      Assertions.assertEquals(127.0, v.y(), 0.0001);
    }
    {
      final Vector2D v = a.values().get(2);
      Assertions.assertEquals(-127.0, v.x(), 0.0001);
      Assertions.assertEquals(127.0, v.y(), 0.0001);
    }
  }

  private static void checkVector1F(
    final Map<SMFAttributeName, SMFAttributeArrayType> arrays,
    final String name)
  {
    final SMFAttributeArrayFloating1 a = (SMFAttributeArrayFloating1)
      arrays.get(SMFAttributeName.of(name)).get();
    {
      final Double v = a.values().get(0);
      Assertions.assertEquals(127.0, v.doubleValue(), 0.0001);
    }
    {
      final Double v = a.values().get(1);
      Assertions.assertEquals(127.0, v.doubleValue(), 0.0001);
    }
    {
      final Double v = a.values().get(2);
      Assertions.assertEquals(127.0, v.doubleValue(), 0.0001);
    }
  }

  private static void checkVector4I(
    final Map<SMFAttributeName, SMFAttributeArrayType> arrays,
    final String name)
  {
    final SMFAttributeArrayIntegerSigned4 a = (SMFAttributeArrayIntegerSigned4)
      arrays.get(SMFAttributeName.of(name)).get();
    {
      final Vector4L v = a.values().get(0);
      Assertions.assertEquals(-127L, v.x());
      Assertions.assertEquals(0L, v.y());
      Assertions.assertEquals(1L, v.z());
      Assertions.assertEquals(127L, v.w());
    }
    {
      final Vector4L v = a.values().get(1);
      Assertions.assertEquals(-127L, v.x());
      Assertions.assertEquals(0L, v.y());
      Assertions.assertEquals(1L, v.z());
      Assertions.assertEquals(127L, v.w());
    }
    {
      final Vector4L v = a.values().get(2);
      Assertions.assertEquals(-127L, v.x());
      Assertions.assertEquals(0L, v.y());
      Assertions.assertEquals(1L, v.z());
      Assertions.assertEquals(127L, v.w());
    }
  }

  private static void checkVector3I(
    final Map<SMFAttributeName, SMFAttributeArrayType> arrays,
    final String name)
  {
    final SMFAttributeArrayIntegerSigned3 a = (SMFAttributeArrayIntegerSigned3)
      arrays.get(SMFAttributeName.of(name)).get();
    {
      final Vector3L v = a.values().get(0);
      Assertions.assertEquals(-127L, v.x());
      Assertions.assertEquals(0L, v.y());
      Assertions.assertEquals(127L, v.z());
    }
    {
      final Vector3L v = a.values().get(1);
      Assertions.assertEquals(-127L, v.x());
      Assertions.assertEquals(0L, v.y());
      Assertions.assertEquals(127L, v.z());
    }
    {
      final Vector3L v = a.values().get(2);
      Assertions.assertEquals(-127L, v.x());
      Assertions.assertEquals(0L, v.y());
      Assertions.assertEquals(127L, v.z());
    }
  }

  private static void checkVector2I(
    final Map<SMFAttributeName, SMFAttributeArrayType> arrays,
    final String name)
  {
    final SMFAttributeArrayIntegerSigned2 a = (SMFAttributeArrayIntegerSigned2)
      arrays.get(SMFAttributeName.of(name)).get();
    {
      final Vector2L v = a.values().get(0);
      Assertions.assertEquals(-127L, v.x());
      Assertions.assertEquals(127L, v.y());
    }
    {
      final Vector2L v = a.values().get(1);
      Assertions.assertEquals(-127L, v.x());
      Assertions.assertEquals(127L, v.y());
    }
    {
      final Vector2L v = a.values().get(2);
      Assertions.assertEquals(-127L, v.x());
      Assertions.assertEquals(127L, v.y());
    }
  }

  private static void checkVector1I(
    final Map<SMFAttributeName, SMFAttributeArrayType> arrays,
    final String name)
  {
    final SMFAttributeArrayIntegerSigned1 a = (SMFAttributeArrayIntegerSigned1)
      arrays.get(SMFAttributeName.of(name)).get();
    {
      final Long v = a.values().get(0);
      Assertions.assertEquals(127L, v.longValue());
    }
    {
      final Long v = a.values().get(1);
      Assertions.assertEquals(127L, v.longValue());
    }
    {
      final Long v = a.values().get(2);
      Assertions.assertEquals(127L, v.longValue());
    }
  }

  private static void checkVector4U(
    final Map<SMFAttributeName, SMFAttributeArrayType> arrays,
    final String name)
  {
    final SMFAttributeArrayIntegerUnsigned4 a = (SMFAttributeArrayIntegerUnsigned4)
      arrays.get(SMFAttributeName.of(name)).get();
    {
      final Vector4L v = a.values().get(0);
      Assertions.assertEquals(127L, v.x());
      Assertions.assertEquals(0L, v.y());
      Assertions.assertEquals(1L, v.z());
      Assertions.assertEquals(127L, v.w());
    }
    {
      final Vector4L v = a.values().get(1);
      Assertions.assertEquals(127L, v.x());
      Assertions.assertEquals(0L, v.y());
      Assertions.assertEquals(1L, v.z());
      Assertions.assertEquals(127L, v.w());
    }
    {
      final Vector4L v = a.values().get(2);
      Assertions.assertEquals(127L, v.x());
      Assertions.assertEquals(0L, v.y());
      Assertions.assertEquals(1L, v.z());
      Assertions.assertEquals(127L, v.w());
    }
  }

  private static void checkVector3U(
    final Map<SMFAttributeName, SMFAttributeArrayType> arrays,
    final String name)
  {
    final SMFAttributeArrayIntegerUnsigned3 a = (SMFAttributeArrayIntegerUnsigned3)
      arrays.get(SMFAttributeName.of(name)).get();
    {
      final Vector3L v = a.values().get(0);
      Assertions.assertEquals(127L, v.x());
      Assertions.assertEquals(0L, v.y());
      Assertions.assertEquals(127L, v.z());
    }
    {
      final Vector3L v = a.values().get(1);
      Assertions.assertEquals(127L, v.x());
      Assertions.assertEquals(0L, v.y());
      Assertions.assertEquals(127L, v.z());
    }
    {
      final Vector3L v = a.values().get(2);
      Assertions.assertEquals(127L, v.x());
      Assertions.assertEquals(0L, v.y());
      Assertions.assertEquals(127L, v.z());
    }
  }

  private static void checkVector2U(
    final Map<SMFAttributeName, SMFAttributeArrayType> arrays,
    final String name)
  {
    final SMFAttributeArrayIntegerUnsigned2 a = (SMFAttributeArrayIntegerUnsigned2)
      arrays.get(SMFAttributeName.of(name)).get();
    {
      final Vector2L v = a.values().get(0);
      Assertions.assertEquals(127L, v.x());
      Assertions.assertEquals(127L, v.y());
    }
    {
      final Vector2L v = a.values().get(1);
      Assertions.assertEquals(127L, v.x());
      Assertions.assertEquals(127L, v.y());
    }
    {
      final Vector2L v = a.values().get(2);
      Assertions.assertEquals(127L, v.x());
      Assertions.assertEquals(127L, v.y());
    }
  }

  private static void checkVector1U(
    final Map<SMFAttributeName, SMFAttributeArrayType> arrays,
    final String name)
  {
    final SMFAttributeArrayIntegerUnsigned1 a = (SMFAttributeArrayIntegerUnsigned1)
      arrays.get(SMFAttributeName.of(name)).get();
    {
      final Long v = a.values().get(0);
      Assertions.assertEquals(127L, v.longValue());
    }
    {
      final Long v = a.values().get(1);
      Assertions.assertEquals(127L, v.longValue());
    }
    {
      final Long v = a.values().get(2);
      Assertions.assertEquals(127L, v.longValue());
    }
  }

  @Test
  public void testReuse()
    throws Exception
  {
    final SMFMemoryMeshProducerType loader = SMFMemoryMeshProducer.create();

    try (SMFParserSequentialType parser =
           SMFTestFiles.createParser(loader, "all.smft")) {
      // Parse already called by SMFTestFiles.createParser
    }

    Assertions.assertEquals(0L, (long) loader.errors().size());

    try (SMFParserSequentialType parser =
           SMFTestFiles.createParser(loader, "all.smft")) {
      // Parse already called by SMFTestFiles.createParser
    }

    Assertions.assertEquals(1L, (long) loader.errors().size());

    final SMFErrorType e = loader.errors().get(0);
    Assertions.assertTrue(e.exception().get() instanceof IllegalStateException);
  }

  @Test
  public void testAll()
    throws Exception
  {
    final SMFMemoryMeshProducerType loader = SMFMemoryMeshProducer.create();

    try (SMFParserSequentialType parser =
           SMFTestFiles.createParser(loader, "all.smft")) {
      // Parse already called by SMFTestFiles.createParser
    }

    Assertions.assertTrue(loader.errors().isEmpty());

    final SMFMemoryMesh mesh = loader.mesh();
    final Map<SMFAttributeName, SMFAttributeArrayType> arrays = mesh.arrays();
    final Vector<Vector3L> triangles = mesh.triangles();
    final Vector<SMFMetadata> metas = mesh.metadata();

    {
      final SMFMetadata m = metas.get(0);
      Assertions.assertEquals(SMFSchemaName.of("com.io7m.smf.example"), m.schema().name());
      Assertions.assertEquals(0L, (long) m.schema().versionMajor());
      Assertions.assertEquals(0L, (long) m.schema().versionMinor());
    }

    {
      final SMFMetadata m = metas.get(1);
      Assertions.assertEquals(SMFSchemaName.of("com.io7m.smf.example"), m.schema().name());
      Assertions.assertEquals(1L, (long) m.schema().versionMajor());
      Assertions.assertEquals(0L, (long) m.schema().versionMinor());
    }

    {
      final SMFMetadata m = metas.get(2);
      Assertions.assertEquals(
        SMFSchemaName.of("com.io7m.smf.example.different"),
        m.schema().name());
      Assertions.assertEquals(1L, (long) m.schema().versionMajor());
      Assertions.assertEquals(0L, (long) m.schema().versionMinor());
    }

    {
      final SMFMetadata m = metas.get(3);
      Assertions.assertEquals(SMFSchemaName.of("com.io7m.smf.example"), m.schema().name());
      Assertions.assertEquals(2L, (long) m.schema().versionMajor());
      Assertions.assertEquals(0L, (long) m.schema().versionMinor());
    }

    Assertions.assertTrue(arrays.containsKey(SMFAttributeName.of("f16_4")));
    Assertions.assertTrue(arrays.containsKey(SMFAttributeName.of("f16_3")));
    Assertions.assertTrue(arrays.containsKey(SMFAttributeName.of("f16_2")));
    Assertions.assertTrue(arrays.containsKey(SMFAttributeName.of("f16_1")));

    Assertions.assertTrue(arrays.containsKey(SMFAttributeName.of("f32_4")));
    Assertions.assertTrue(arrays.containsKey(SMFAttributeName.of("f32_3")));
    Assertions.assertTrue(arrays.containsKey(SMFAttributeName.of("f32_2")));
    Assertions.assertTrue(arrays.containsKey(SMFAttributeName.of("f32_1")));

    Assertions.assertTrue(arrays.containsKey(SMFAttributeName.of("f64_4")));
    Assertions.assertTrue(arrays.containsKey(SMFAttributeName.of("f64_3")));
    Assertions.assertTrue(arrays.containsKey(SMFAttributeName.of("f64_2")));
    Assertions.assertTrue(arrays.containsKey(SMFAttributeName.of("f64_1")));

    Assertions.assertTrue(arrays.containsKey(SMFAttributeName.of("i64_4")));
    Assertions.assertTrue(arrays.containsKey(SMFAttributeName.of("i64_3")));
    Assertions.assertTrue(arrays.containsKey(SMFAttributeName.of("i64_2")));
    Assertions.assertTrue(arrays.containsKey(SMFAttributeName.of("i64_1")));

    Assertions.assertTrue(arrays.containsKey(SMFAttributeName.of("i32_4")));
    Assertions.assertTrue(arrays.containsKey(SMFAttributeName.of("i32_3")));
    Assertions.assertTrue(arrays.containsKey(SMFAttributeName.of("i32_2")));
    Assertions.assertTrue(arrays.containsKey(SMFAttributeName.of("i32_1")));

    Assertions.assertTrue(arrays.containsKey(SMFAttributeName.of("i16_4")));
    Assertions.assertTrue(arrays.containsKey(SMFAttributeName.of("i16_3")));
    Assertions.assertTrue(arrays.containsKey(SMFAttributeName.of("i16_2")));
    Assertions.assertTrue(arrays.containsKey(SMFAttributeName.of("i16_1")));

    Assertions.assertTrue(arrays.containsKey(SMFAttributeName.of("i8_4")));
    Assertions.assertTrue(arrays.containsKey(SMFAttributeName.of("i8_3")));
    Assertions.assertTrue(arrays.containsKey(SMFAttributeName.of("i8_2")));
    Assertions.assertTrue(arrays.containsKey(SMFAttributeName.of("i8_1")));

    Assertions.assertTrue(arrays.containsKey(SMFAttributeName.of("u64_4")));
    Assertions.assertTrue(arrays.containsKey(SMFAttributeName.of("u64_3")));
    Assertions.assertTrue(arrays.containsKey(SMFAttributeName.of("u64_2")));
    Assertions.assertTrue(arrays.containsKey(SMFAttributeName.of("u64_1")));

    Assertions.assertTrue(arrays.containsKey(SMFAttributeName.of("u32_4")));
    Assertions.assertTrue(arrays.containsKey(SMFAttributeName.of("u32_3")));
    Assertions.assertTrue(arrays.containsKey(SMFAttributeName.of("u32_2")));
    Assertions.assertTrue(arrays.containsKey(SMFAttributeName.of("u32_1")));

    Assertions.assertTrue(arrays.containsKey(SMFAttributeName.of("u16_4")));
    Assertions.assertTrue(arrays.containsKey(SMFAttributeName.of("u16_3")));
    Assertions.assertTrue(arrays.containsKey(SMFAttributeName.of("u16_2")));
    Assertions.assertTrue(arrays.containsKey(SMFAttributeName.of("u16_1")));

    Assertions.assertTrue(arrays.containsKey(SMFAttributeName.of("u8_4")));
    Assertions.assertTrue(arrays.containsKey(SMFAttributeName.of("u8_3")));
    Assertions.assertTrue(arrays.containsKey(SMFAttributeName.of("u8_2")));
    Assertions.assertTrue(arrays.containsKey(SMFAttributeName.of("u8_1")));

    Assertions.assertEquals(44L, (long) arrays.size());
    Assertions.assertEquals(1L, (long) triangles.size());

    checkVector4F(arrays, "f16_4");
    checkVector3F(arrays, "f16_3");
    checkVector2F(arrays, "f16_2");
    checkVector1F(arrays, "f16_1");

    checkVector4F(arrays, "f32_4");
    checkVector3F(arrays, "f32_3");
    checkVector2F(arrays, "f32_2");
    checkVector1F(arrays, "f32_1");

    checkVector4F(arrays, "f64_4");
    checkVector3F(arrays, "f64_3");
    checkVector2F(arrays, "f64_2");
    checkVector1F(arrays, "f64_1");

    checkVector4I(arrays, "i64_4");
    checkVector3I(arrays, "i64_3");
    checkVector2I(arrays, "i64_2");
    checkVector1I(arrays, "i64_1");

    checkVector4I(arrays, "i32_4");
    checkVector3I(arrays, "i32_3");
    checkVector2I(arrays, "i32_2");
    checkVector1I(arrays, "i32_1");

    checkVector4I(arrays, "i16_4");
    checkVector3I(arrays, "i16_3");
    checkVector2I(arrays, "i16_2");
    checkVector1I(arrays, "i16_1");

    checkVector4I(arrays, "i8_4");
    checkVector3I(arrays, "i8_3");
    checkVector2I(arrays, "i8_2");
    checkVector1I(arrays, "i8_1");

    checkVector4U(arrays, "u64_4");
    checkVector3U(arrays, "u64_3");
    checkVector2U(arrays, "u64_2");
    checkVector1U(arrays, "u64_1");

    checkVector4U(arrays, "u32_4");
    checkVector3U(arrays, "u32_3");
    checkVector2U(arrays, "u32_2");
    checkVector1U(arrays, "u32_1");

    checkVector4U(arrays, "u16_4");
    checkVector3U(arrays, "u16_3");
    checkVector2U(arrays, "u16_2");
    checkVector1U(arrays, "u16_1");

    checkVector4U(arrays, "u8_4");
    checkVector3U(arrays, "u8_3");
    checkVector2U(arrays, "u8_2");
    checkVector1U(arrays, "u8_1");
  }
}
