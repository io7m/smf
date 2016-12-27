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

import com.io7m.jtensors.VectorI2D;
import com.io7m.jtensors.VectorI2L;
import com.io7m.jtensors.VectorI3D;
import com.io7m.jtensors.VectorI3L;
import com.io7m.jtensors.VectorI4D;
import com.io7m.jtensors.VectorI4L;
import com.io7m.smfj.core.SMFAttributeName;
import com.io7m.smfj.format.text.SMFFormatText;
import com.io7m.smfj.parser.api.SMFParserEventsType;
import com.io7m.smfj.parser.api.SMFParserProviderType;
import com.io7m.smfj.parser.api.SMFParserSequentialType;
import com.io7m.smfj.processing.SMFAttributeArrayFloating1;
import com.io7m.smfj.processing.SMFAttributeArrayFloating2;
import com.io7m.smfj.processing.SMFAttributeArrayFloating3;
import com.io7m.smfj.processing.SMFAttributeArrayFloating4;
import com.io7m.smfj.processing.SMFAttributeArrayIntegerSigned1;
import com.io7m.smfj.processing.SMFAttributeArrayIntegerSigned2;
import com.io7m.smfj.processing.SMFAttributeArrayIntegerSigned3;
import com.io7m.smfj.processing.SMFAttributeArrayIntegerSigned4;
import com.io7m.smfj.processing.SMFAttributeArrayIntegerUnsigned1;
import com.io7m.smfj.processing.SMFAttributeArrayIntegerUnsigned2;
import com.io7m.smfj.processing.SMFAttributeArrayIntegerUnsigned3;
import com.io7m.smfj.processing.SMFAttributeArrayIntegerUnsigned4;
import com.io7m.smfj.processing.SMFAttributeArrayType;
import com.io7m.smfj.processing.SMFMemoryMesh;
import com.io7m.smfj.processing.SMFMemoryMeshProducer;
import com.io7m.smfj.processing.SMFMemoryMeshProducerType;
import com.io7m.smfj.processing.SMFMetadata;
import javaslang.collection.Map;
import javaslang.collection.Vector;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;

public final class SMFMemoryMeshProducerTest
{
  @Rule public ExpectedException expected = ExpectedException.none();

  private static SMFParserSequentialType createParser(
    final SMFParserEventsType loader,
    final String name)
    throws IOException
  {
    final String rpath = "/com/io7m/smfj/tests/processing/" + name;
    try (final InputStream stream =
           SMFMemoryMeshProducerTest.class.getResourceAsStream(rpath)) {
      final SMFParserProviderType fmt = new SMFFormatText();
      final Path path = Paths.get(rpath);
      final SMFParserSequentialType parser =
        fmt.parserCreateSequential(loader, path, stream);
      parser.parseHeader();
      parser.parseData();
      return parser;
    }
  }

  @Test
  public void testReuse()
    throws Exception
  {
    final SMFMemoryMeshProducerType loader = SMFMemoryMeshProducer.create();

    try (final SMFParserSequentialType parser = createParser(
      loader,
      "all.smft")) {
      // Nothing
    }

    this.expected.expect(IllegalStateException.class);

    try (final SMFParserSequentialType parser = createParser(
      loader,
      "all.smft")) {
      // Nothing
    }
  }

  @Test
  public void testAll()
    throws Exception
  {
    final SMFMemoryMeshProducerType loader = SMFMemoryMeshProducer.create();

    try (final SMFParserSequentialType parser = createParser(
      loader,
      "all.smft")) {
      // Nothing
    }

    Assert.assertTrue(loader.errors().isEmpty());

    final SMFMemoryMesh mesh = loader.mesh();
    final Map<SMFAttributeName, SMFAttributeArrayType> arrays = mesh.arrays();
    final Vector<VectorI3L> triangles = mesh.triangles();
    final Vector<SMFMetadata> metas = mesh.metadata();

    {
      final SMFMetadata m = metas.get(0);
      Assert.assertEquals(0x696f376dL, (long) m.vendor());
      Assert.assertEquals(0L, (long) m.schema());
    }

    {
      final SMFMetadata m = metas.get(1);
      Assert.assertEquals(0x696f376dL, (long) m.vendor());
      Assert.assertEquals(1L, (long) m.schema());
    }

    {
      final SMFMetadata m = metas.get(2);
      Assert.assertEquals(0x696f376dL, (long) m.vendor());
      Assert.assertEquals(2L, (long) m.schema());
    }

    Assert.assertTrue(arrays.containsKey(SMFAttributeName.of("f16_4")));
    Assert.assertTrue(arrays.containsKey(SMFAttributeName.of("f16_3")));
    Assert.assertTrue(arrays.containsKey(SMFAttributeName.of("f16_2")));
    Assert.assertTrue(arrays.containsKey(SMFAttributeName.of("f16_1")));

    Assert.assertTrue(arrays.containsKey(SMFAttributeName.of("f32_4")));
    Assert.assertTrue(arrays.containsKey(SMFAttributeName.of("f32_3")));
    Assert.assertTrue(arrays.containsKey(SMFAttributeName.of("f32_2")));
    Assert.assertTrue(arrays.containsKey(SMFAttributeName.of("f32_1")));

    Assert.assertTrue(arrays.containsKey(SMFAttributeName.of("f64_4")));
    Assert.assertTrue(arrays.containsKey(SMFAttributeName.of("f64_3")));
    Assert.assertTrue(arrays.containsKey(SMFAttributeName.of("f64_2")));
    Assert.assertTrue(arrays.containsKey(SMFAttributeName.of("f64_1")));

    Assert.assertTrue(arrays.containsKey(SMFAttributeName.of("i64_4")));
    Assert.assertTrue(arrays.containsKey(SMFAttributeName.of("i64_3")));
    Assert.assertTrue(arrays.containsKey(SMFAttributeName.of("i64_2")));
    Assert.assertTrue(arrays.containsKey(SMFAttributeName.of("i64_1")));

    Assert.assertTrue(arrays.containsKey(SMFAttributeName.of("i32_4")));
    Assert.assertTrue(arrays.containsKey(SMFAttributeName.of("i32_3")));
    Assert.assertTrue(arrays.containsKey(SMFAttributeName.of("i32_2")));
    Assert.assertTrue(arrays.containsKey(SMFAttributeName.of("i32_1")));

    Assert.assertTrue(arrays.containsKey(SMFAttributeName.of("i16_4")));
    Assert.assertTrue(arrays.containsKey(SMFAttributeName.of("i16_3")));
    Assert.assertTrue(arrays.containsKey(SMFAttributeName.of("i16_2")));
    Assert.assertTrue(arrays.containsKey(SMFAttributeName.of("i16_1")));

    Assert.assertTrue(arrays.containsKey(SMFAttributeName.of("i8_4")));
    Assert.assertTrue(arrays.containsKey(SMFAttributeName.of("i8_3")));
    Assert.assertTrue(arrays.containsKey(SMFAttributeName.of("i8_2")));
    Assert.assertTrue(arrays.containsKey(SMFAttributeName.of("i8_1")));

    Assert.assertTrue(arrays.containsKey(SMFAttributeName.of("u64_4")));
    Assert.assertTrue(arrays.containsKey(SMFAttributeName.of("u64_3")));
    Assert.assertTrue(arrays.containsKey(SMFAttributeName.of("u64_2")));
    Assert.assertTrue(arrays.containsKey(SMFAttributeName.of("u64_1")));

    Assert.assertTrue(arrays.containsKey(SMFAttributeName.of("u32_4")));
    Assert.assertTrue(arrays.containsKey(SMFAttributeName.of("u32_3")));
    Assert.assertTrue(arrays.containsKey(SMFAttributeName.of("u32_2")));
    Assert.assertTrue(arrays.containsKey(SMFAttributeName.of("u32_1")));

    Assert.assertTrue(arrays.containsKey(SMFAttributeName.of("u16_4")));
    Assert.assertTrue(arrays.containsKey(SMFAttributeName.of("u16_3")));
    Assert.assertTrue(arrays.containsKey(SMFAttributeName.of("u16_2")));
    Assert.assertTrue(arrays.containsKey(SMFAttributeName.of("u16_1")));

    Assert.assertTrue(arrays.containsKey(SMFAttributeName.of("u8_4")));
    Assert.assertTrue(arrays.containsKey(SMFAttributeName.of("u8_3")));
    Assert.assertTrue(arrays.containsKey(SMFAttributeName.of("u8_2")));
    Assert.assertTrue(arrays.containsKey(SMFAttributeName.of("u8_1")));

    Assert.assertEquals(44L, (long) arrays.size());
    Assert.assertEquals(1L, (long) triangles.size());

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

  private static void checkVector4F(
    final Map<SMFAttributeName, SMFAttributeArrayType> arrays,
    final String name)
  {
    final SMFAttributeArrayFloating4 a = (SMFAttributeArrayFloating4)
      arrays.get(SMFAttributeName.of(name)).get();
    {
      final VectorI4D v = a.values().get(0);
      Assert.assertEquals(-127.0, v.getXD(), 0.0);
      Assert.assertEquals(0.0, v.getYD(), 0.0);
      Assert.assertEquals(1.0, v.getZD(), 0.0);
      Assert.assertEquals(127.0, v.getWD(), 0.0);
    }
    {
      final VectorI4D v = a.values().get(1);
      Assert.assertEquals(-127.0, v.getXD(), 0.0);
      Assert.assertEquals(0.0, v.getYD(), 0.0);
      Assert.assertEquals(1.0, v.getZD(), 0.0);
      Assert.assertEquals(127.0, v.getWD(), 0.0);
    }
    {
      final VectorI4D v = a.values().get(2);
      Assert.assertEquals(-127.0, v.getXD(), 0.0);
      Assert.assertEquals(0.0, v.getYD(), 0.0);
      Assert.assertEquals(1.0, v.getZD(), 0.0);
      Assert.assertEquals(127.0, v.getWD(), 0.0);
    }
  }

  private static void checkVector3F(
    final Map<SMFAttributeName, SMFAttributeArrayType> arrays,
    final String name)
  {
    final SMFAttributeArrayFloating3 a = (SMFAttributeArrayFloating3)
      arrays.get(SMFAttributeName.of(name)).get();
    {
      final VectorI3D v = a.values().get(0);
      Assert.assertEquals(-127.0, v.getXD(), 0.0);
      Assert.assertEquals(0.0, v.getYD(), 0.0);
      Assert.assertEquals(127.0, v.getZD(), 0.0);
    }
    {
      final VectorI3D v = a.values().get(1);
      Assert.assertEquals(-127.0, v.getXD(), 0.0);
      Assert.assertEquals(0.0, v.getYD(), 0.0);
      Assert.assertEquals(127.0, v.getZD(), 0.0);
    }
    {
      final VectorI3D v = a.values().get(2);
      Assert.assertEquals(-127.0, v.getXD(), 0.0);
      Assert.assertEquals(0.0, v.getYD(), 0.0);
      Assert.assertEquals(127.0, v.getZD(), 0.0);
    }
  }

  private static void checkVector2F(
    final Map<SMFAttributeName, SMFAttributeArrayType> arrays,
    final String name)
  {
    final SMFAttributeArrayFloating2 a = (SMFAttributeArrayFloating2)
      arrays.get(SMFAttributeName.of(name)).get();
    {
      final VectorI2D v = a.values().get(0);
      Assert.assertEquals(-127.0, v.getXD(), 0.0);
      Assert.assertEquals(127.0, v.getYD(), 0.0);
    }
    {
      final VectorI2D v = a.values().get(1);
      Assert.assertEquals(-127.0, v.getXD(), 0.0);
      Assert.assertEquals(127.0, v.getYD(), 0.0);
    }
    {
      final VectorI2D v = a.values().get(2);
      Assert.assertEquals(-127.0, v.getXD(), 0.0);
      Assert.assertEquals(127.0, v.getYD(), 0.0);
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
      Assert.assertEquals(127.0, v.doubleValue(), 0.0);
    }
    {
      final Double v = a.values().get(1);
      Assert.assertEquals(127.0, v.doubleValue(), 0.0);
    }
    {
      final Double v = a.values().get(2);
      Assert.assertEquals(127.0, v.doubleValue(), 0.0);
    }
  }


  private static void checkVector4I(
    final Map<SMFAttributeName, SMFAttributeArrayType> arrays,
    final String name)
  {
    final SMFAttributeArrayIntegerSigned4 a = (SMFAttributeArrayIntegerSigned4)
      arrays.get(SMFAttributeName.of(name)).get();
    {
      final VectorI4L v = a.values().get(0);
      Assert.assertEquals(-127L, v.getXL());
      Assert.assertEquals(0L, v.getYL());
      Assert.assertEquals(1L, v.getZL());
      Assert.assertEquals(127L, v.getWL());
    }
    {
      final VectorI4L v = a.values().get(1);
      Assert.assertEquals(-127L, v.getXL());
      Assert.assertEquals(0L, v.getYL());
      Assert.assertEquals(1L, v.getZL());
      Assert.assertEquals(127L, v.getWL());
    }
    {
      final VectorI4L v = a.values().get(2);
      Assert.assertEquals(-127L, v.getXL());
      Assert.assertEquals(0L, v.getYL());
      Assert.assertEquals(1L, v.getZL());
      Assert.assertEquals(127L, v.getWL());
    }
  }

  private static void checkVector3I(
    final Map<SMFAttributeName, SMFAttributeArrayType> arrays,
    final String name)
  {
    final SMFAttributeArrayIntegerSigned3 a = (SMFAttributeArrayIntegerSigned3)
      arrays.get(SMFAttributeName.of(name)).get();
    {
      final VectorI3L v = a.values().get(0);
      Assert.assertEquals(-127L, v.getXL());
      Assert.assertEquals(0L, v.getYL());
      Assert.assertEquals(127L, v.getZL());
    }
    {
      final VectorI3L v = a.values().get(1);
      Assert.assertEquals(-127L, v.getXL());
      Assert.assertEquals(0L, v.getYL());
      Assert.assertEquals(127L, v.getZL());
    }
    {
      final VectorI3L v = a.values().get(2);
      Assert.assertEquals(-127L, v.getXL());
      Assert.assertEquals(0L, v.getYL());
      Assert.assertEquals(127L, v.getZL());
    }
  }

  private static void checkVector2I(
    final Map<SMFAttributeName, SMFAttributeArrayType> arrays,
    final String name)
  {
    final SMFAttributeArrayIntegerSigned2 a = (SMFAttributeArrayIntegerSigned2)
      arrays.get(SMFAttributeName.of(name)).get();
    {
      final VectorI2L v = a.values().get(0);
      Assert.assertEquals(-127L, v.getXL());
      Assert.assertEquals(127L, v.getYL());
    }
    {
      final VectorI2L v = a.values().get(1);
      Assert.assertEquals(-127L, v.getXL());
      Assert.assertEquals(127L, v.getYL());
    }
    {
      final VectorI2L v = a.values().get(2);
      Assert.assertEquals(-127L, v.getXL());
      Assert.assertEquals(127L, v.getYL());
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
      Assert.assertEquals(127L, v.longValue());
    }
    {
      final Long v = a.values().get(1);
      Assert.assertEquals(127L, v.longValue());
    }
    {
      final Long v = a.values().get(2);
      Assert.assertEquals(127L, v.longValue());
    }
  }


  private static void checkVector4U(
    final Map<SMFAttributeName, SMFAttributeArrayType> arrays,
    final String name)
  {
    final SMFAttributeArrayIntegerUnsigned4 a = (SMFAttributeArrayIntegerUnsigned4)
      arrays.get(SMFAttributeName.of(name)).get();
    {
      final VectorI4L v = a.values().get(0);
      Assert.assertEquals(127L, v.getXL());
      Assert.assertEquals(0L, v.getYL());
      Assert.assertEquals(1L, v.getZL());
      Assert.assertEquals(127L, v.getWL());
    }
    {
      final VectorI4L v = a.values().get(1);
      Assert.assertEquals(127L, v.getXL());
      Assert.assertEquals(0L, v.getYL());
      Assert.assertEquals(1L, v.getZL());
      Assert.assertEquals(127L, v.getWL());
    }
    {
      final VectorI4L v = a.values().get(2);
      Assert.assertEquals(127L, v.getXL());
      Assert.assertEquals(0L, v.getYL());
      Assert.assertEquals(1L, v.getZL());
      Assert.assertEquals(127L, v.getWL());
    }
  }

  private static void checkVector3U(
    final Map<SMFAttributeName, SMFAttributeArrayType> arrays,
    final String name)
  {
    final SMFAttributeArrayIntegerUnsigned3 a = (SMFAttributeArrayIntegerUnsigned3)
      arrays.get(SMFAttributeName.of(name)).get();
    {
      final VectorI3L v = a.values().get(0);
      Assert.assertEquals(127L, v.getXL());
      Assert.assertEquals(0L, v.getYL());
      Assert.assertEquals(127L, v.getZL());
    }
    {
      final VectorI3L v = a.values().get(1);
      Assert.assertEquals(127L, v.getXL());
      Assert.assertEquals(0L, v.getYL());
      Assert.assertEquals(127L, v.getZL());
    }
    {
      final VectorI3L v = a.values().get(2);
      Assert.assertEquals(127L, v.getXL());
      Assert.assertEquals(0L, v.getYL());
      Assert.assertEquals(127L, v.getZL());
    }
  }

  private static void checkVector2U(
    final Map<SMFAttributeName, SMFAttributeArrayType> arrays,
    final String name)
  {
    final SMFAttributeArrayIntegerUnsigned2 a = (SMFAttributeArrayIntegerUnsigned2)
      arrays.get(SMFAttributeName.of(name)).get();
    {
      final VectorI2L v = a.values().get(0);
      Assert.assertEquals(127L, v.getXL());
      Assert.assertEquals(127L, v.getYL());
    }
    {
      final VectorI2L v = a.values().get(1);
      Assert.assertEquals(127L, v.getXL());
      Assert.assertEquals(127L, v.getYL());
    }
    {
      final VectorI2L v = a.values().get(2);
      Assert.assertEquals(127L, v.getXL());
      Assert.assertEquals(127L, v.getYL());
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
      Assert.assertEquals(127L, v.longValue());
    }
    {
      final Long v = a.values().get(1);
      Assert.assertEquals(127L, v.longValue());
    }
    {
      final Long v = a.values().get(2);
      Assert.assertEquals(127L, v.longValue());
    }
  }
}
