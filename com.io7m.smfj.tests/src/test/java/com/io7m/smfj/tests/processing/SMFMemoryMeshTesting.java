/*
 * Copyright © 2019 Mark Raynsford <code@io7m.com> http://io7m.com
 *
 * Permission to use, copy, modify, and/or distribute this software for
 * any purpose with or without fee is hereby granted, provided that the
 * above copyright notice and this permission notice appear in all copies.
 *
 * THE SOFTWARE IS PROVIDED "AS IS" AND THE AUTHOR DISCLAIMS ALL
 * WARRANTIES WITH REGARD TO THIS SOFTWARE INCLUDING ALL IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS. IN NO EVENT SHALL THE AUTHOR
 * BE LIABLE FOR ANY SPECIAL, DIRECT, INDIRECT, OR CONSEQUENTIAL DAMAGES
 * OR ANY DAMAGES WHATSOEVER RESULTING FROM LOSS OF USE, DATA OR PROFITS,
 * WHETHER IN AN ACTION OF CONTRACT, NEGLIGENCE OR OTHER TORTIOUS ACTION,
 * ARISING OUT OF OR IN CONNECTION WITH THE USE OR PERFORMANCE OF THIS
 * SOFTWARE.
 */


package com.io7m.smfj.tests.processing;

import com.io7m.jtensors.core.unparameterized.vectors.Vector2D;
import com.io7m.jtensors.core.unparameterized.vectors.Vector2L;
import com.io7m.jtensors.core.unparameterized.vectors.Vector3D;
import com.io7m.jtensors.core.unparameterized.vectors.Vector3L;
import com.io7m.jtensors.core.unparameterized.vectors.Vector4D;
import com.io7m.jtensors.core.unparameterized.vectors.Vector4L;
import com.io7m.smfj.core.SMFAttributeName;
import com.io7m.smfj.core.SMFSchemaName;
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
import com.io7m.smfj.processing.api.SMFMetadata;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Assertions;

public final class SMFMemoryMeshTesting
{
  private SMFMemoryMeshTesting()
  {

  }

  private static void checkVector4F(
    final Map<SMFAttributeName, SMFAttributeArrayType> arrays,
    final double delta,
    final String name)
  {
    final SMFAttributeArrayFloating4 a = (SMFAttributeArrayFloating4)
      arrays.get(SMFAttributeName.of(name));

    {
      final Vector4D v = a.values().get(0);
      Assertions.assertEquals(-127.0, v.x(), delta);
      Assertions.assertEquals(0.0, v.y(), delta);
      Assertions.assertEquals(1.0, v.z(), delta);
      Assertions.assertEquals(127.0, v.w(), delta);
    }
    {
      final Vector4D v = a.values().get(1);
      Assertions.assertEquals(-127.0, v.x(), delta);
      Assertions.assertEquals(0.0, v.y(), delta);
      Assertions.assertEquals(1.0, v.z(), delta);
      Assertions.assertEquals(127.0, v.w(), delta);
    }
    {
      final Vector4D v = a.values().get(2);
      Assertions.assertEquals(-127.0, v.x(), delta);
      Assertions.assertEquals(0.0, v.y(), delta);
      Assertions.assertEquals(1.0, v.z(), delta);
      Assertions.assertEquals(127.0, v.w(), delta);
    }
  }

  private static void checkVector3F(
    final Map<SMFAttributeName, SMFAttributeArrayType> arrays,
    final double delta,
    final String name)
  {
    final SMFAttributeArrayFloating3 a = (SMFAttributeArrayFloating3)
      arrays.get(SMFAttributeName.of(name));
    {
      final Vector3D v = a.values().get(0);
      Assertions.assertEquals(-127.0, v.x(), delta);
      Assertions.assertEquals(0.0, v.y(), delta);
      Assertions.assertEquals(127.0, v.z(), delta);
    }
    {
      final Vector3D v = a.values().get(1);
      Assertions.assertEquals(-127.0, v.x(), delta);
      Assertions.assertEquals(0.0, v.y(), delta);
      Assertions.assertEquals(127.0, v.z(), delta);
    }
    {
      final Vector3D v = a.values().get(2);
      Assertions.assertEquals(-127.0, v.x(), delta);
      Assertions.assertEquals(0.0, v.y(), delta);
      Assertions.assertEquals(127.0, v.z(), delta);
    }
  }

  private static void checkVector2F(
    final Map<SMFAttributeName, SMFAttributeArrayType> arrays,
    final double delta,
    final String name)
  {
    final SMFAttributeArrayFloating2 a = (SMFAttributeArrayFloating2)
      arrays.get(SMFAttributeName.of(name));
    {
      final Vector2D v = a.values().get(0);
      Assertions.assertEquals(-127.0, v.x(), delta);
      Assertions.assertEquals(127.0, v.y(), delta);
    }
    {
      final Vector2D v = a.values().get(1);
      Assertions.assertEquals(-127.0, v.x(), delta);
      Assertions.assertEquals(127.0, v.y(), delta);
    }
    {
      final Vector2D v = a.values().get(2);
      Assertions.assertEquals(-127.0, v.x(), delta);
      Assertions.assertEquals(127.0, v.y(), delta);
    }
  }

  private static void checkVector1F(
    final Map<SMFAttributeName, SMFAttributeArrayType> arrays,
    final double delta,
    final String name)
  {
    final SMFAttributeArrayFloating1 a = (SMFAttributeArrayFloating1)
      arrays.get(SMFAttributeName.of(name));
    {
      final Double v = a.values().get(0);
      Assertions.assertEquals(127.0, v.doubleValue(), delta);
    }
    {
      final Double v = a.values().get(1);
      Assertions.assertEquals(127.0, v.doubleValue(), delta);
    }
    {
      final Double v = a.values().get(2);
      Assertions.assertEquals(127.0, v.doubleValue(), delta);
    }
  }

  private static void checkVector4I(
    final Map<SMFAttributeName, SMFAttributeArrayType> arrays,
    final String name)
  {
    final SMFAttributeArrayIntegerSigned4 a = (SMFAttributeArrayIntegerSigned4)
      arrays.get(SMFAttributeName.of(name));
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
      arrays.get(SMFAttributeName.of(name));
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
      arrays.get(SMFAttributeName.of(name));
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
      arrays.get(SMFAttributeName.of(name));
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
      arrays.get(SMFAttributeName.of(name));
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
      arrays.get(SMFAttributeName.of(name));
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
      arrays.get(SMFAttributeName.of(name));
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
      arrays.get(SMFAttributeName.of(name));
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

  public static void checkStandardMesh(
    final SMFMemoryMesh mesh)
  {
    final Map<SMFAttributeName, SMFAttributeArrayType> arrays = mesh.arrays();
    final List<Vector3L> triangles = mesh.triangles();
    final List<SMFMetadata> metas = mesh.metadata();

    {
      final SMFMetadata m = metas.get(0);
      Assertions.assertEquals(
        SMFSchemaName.of("com.io7m.smf.example"),
        m.schema().name());
      Assertions.assertEquals(0L, m.schema().versionMajor());
      Assertions.assertEquals(0L, m.schema().versionMinor());
    }

    {
      final SMFMetadata m = metas.get(1);
      Assertions.assertEquals(
        SMFSchemaName.of("com.io7m.smf.example"),
        m.schema().name());
      Assertions.assertEquals(1L, m.schema().versionMajor());
      Assertions.assertEquals(0L, m.schema().versionMinor());
    }

    {
      final SMFMetadata m = metas.get(2);
      Assertions.assertEquals(
        SMFSchemaName.of("com.io7m.smf.example.different"),
        m.schema().name());
      Assertions.assertEquals(1L, m.schema().versionMajor());
      Assertions.assertEquals(0L, m.schema().versionMinor());
    }

    {
      final SMFMetadata m = metas.get(3);
      Assertions.assertEquals(
        SMFSchemaName.of("com.io7m.smf.example"),
        m.schema().name());
      Assertions.assertEquals(2L, m.schema().versionMajor());
      Assertions.assertEquals(0L, m.schema().versionMinor());
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

    Assertions.assertEquals(44L, arrays.size());
    Assertions.assertEquals(1L, triangles.size());

    checkVector4F(arrays, 0.001, "f16_4");
    checkVector3F(arrays, 0.001, "f16_3");
    checkVector2F(arrays, 0.001, "f16_2");
    checkVector1F(arrays, 0.001, "f16_1");

    checkVector4F(arrays, 0.00001, "f32_4");
    checkVector3F(arrays, 0.00001, "f32_3");
    checkVector2F(arrays, 0.00001, "f32_2");
    checkVector1F(arrays, 0.00001, "f32_1");

    checkVector4F(arrays, 0.000_000_001, "f64_4");
    checkVector3F(arrays, 0.000_000_001, "f64_3");
    checkVector2F(arrays, 0.000_000_001, "f64_2");
    checkVector1F(arrays, 0.000_000_001, "f64_1");

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
