/*
 * Copyright © 2017 <code@io7m.com> http://io7m.com
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

package com.io7m.smfj.tests.bytebuffer;

import com.io7m.smfj.bytebuffer.SMFByteBufferPackingConfiguration;
import com.io7m.smfj.core.SMFAttribute;
import com.io7m.smfj.core.SMFAttributeName;
import com.io7m.smfj.core.SMFComponentType;
import io.vavr.collection.List;
import org.junit.Assert;
import org.junit.Test;

public final class SMFByteBufferPackingConfigurationTest
{
  @Test
  public void testSigned8_1()
  {
    final SMFComponentType type = SMFComponentType.ELEMENT_TYPE_INTEGER_SIGNED;
    final int component_count = 1;
    final int component_size = 8;
    this.checkSimpleOne(type, component_count, component_size);
  }

  @Test
  public void testSigned8_2()
  {
    final SMFComponentType type = SMFComponentType.ELEMENT_TYPE_INTEGER_SIGNED;
    final int component_count = 2;
    final int component_size = 8;
    this.checkSimpleOne(type, component_count, component_size);
  }

  @Test
  public void testSigned8_3()
  {
    final SMFComponentType type = SMFComponentType.ELEMENT_TYPE_INTEGER_SIGNED;
    final int component_count = 3;
    final int component_size = 8;
    this.checkSimpleOne(type, component_count, component_size);
  }

  @Test
  public void testSigned8_4()
  {
    final SMFComponentType type = SMFComponentType.ELEMENT_TYPE_INTEGER_SIGNED;
    final int component_count = 4;
    final int component_size = 8;
    this.checkSimpleOne(type, component_count, component_size);
  }

  @Test
  public void testSigned16_1()
  {
    final SMFComponentType type = SMFComponentType.ELEMENT_TYPE_INTEGER_SIGNED;
    final int component_count = 1;
    final int component_size = 16;
    this.checkSimpleOne(type, component_count, component_size);
  }

  @Test
  public void testSigned16_2()
  {
    final SMFComponentType type = SMFComponentType.ELEMENT_TYPE_INTEGER_SIGNED;
    final int component_count = 2;
    final int component_size = 16;
    this.checkSimpleOne(type, component_count, component_size);
  }

  @Test
  public void testSigned16_3()
  {
    final SMFComponentType type = SMFComponentType.ELEMENT_TYPE_INTEGER_SIGNED;
    final int component_count = 3;
    final int component_size = 16;
    this.checkSimpleOne(type, component_count, component_size);
  }

  @Test
  public void testSigned16_4()
  {
    final SMFComponentType type = SMFComponentType.ELEMENT_TYPE_INTEGER_SIGNED;
    final int component_count = 4;
    final int component_size = 16;
    this.checkSimpleOne(type, component_count, component_size);
  }

  @Test
  public void testSigned32_1()
  {
    final SMFComponentType type = SMFComponentType.ELEMENT_TYPE_INTEGER_SIGNED;
    final int component_count = 1;
    final int component_size = 32;
    this.checkSimpleOne(type, component_count, component_size);
  }

  @Test
  public void testSigned32_2()
  {
    final SMFComponentType type = SMFComponentType.ELEMENT_TYPE_INTEGER_SIGNED;
    final int component_count = 2;
    final int component_size = 32;
    this.checkSimpleOne(type, component_count, component_size);
  }

  @Test
  public void testSigned32_3()
  {
    final SMFComponentType type = SMFComponentType.ELEMENT_TYPE_INTEGER_SIGNED;
    final int component_count = 3;
    final int component_size = 32;
    this.checkSimpleOne(type, component_count, component_size);
  }

  @Test
  public void testSigned32_4()
  {
    final SMFComponentType type = SMFComponentType.ELEMENT_TYPE_INTEGER_SIGNED;
    final int component_count = 4;
    final int component_size = 32;
    this.checkSimpleOne(type, component_count, component_size);
  }

  @Test
  public void testSigned64_1()
  {
    final SMFComponentType type = SMFComponentType.ELEMENT_TYPE_INTEGER_SIGNED;
    final int component_count = 1;
    final int component_size = 64;
    this.checkSimpleOne(type, component_count, component_size);
  }

  @Test
  public void testSigned64_2()
  {
    final SMFComponentType type = SMFComponentType.ELEMENT_TYPE_INTEGER_SIGNED;
    final int component_count = 2;
    final int component_size = 64;
    this.checkSimpleOne(type, component_count, component_size);
  }

  @Test
  public void testSigned64_3()
  {
    final SMFComponentType type = SMFComponentType.ELEMENT_TYPE_INTEGER_SIGNED;
    final int component_count = 3;
    final int component_size = 64;
    this.checkSimpleOne(type, component_count, component_size);
  }

  @Test
  public void testSigned64_4()
  {
    final SMFComponentType type = SMFComponentType.ELEMENT_TYPE_INTEGER_SIGNED;
    final int component_count = 4;
    final int component_size = 64;
    this.checkSimpleOne(type, component_count, component_size);
  }

  @Test
  public void testUnsigned8_1()
  {
    final SMFComponentType type = SMFComponentType.ELEMENT_TYPE_INTEGER_UNSIGNED;
    final int component_count = 1;
    final int component_size = 8;
    this.checkSimpleOne(type, component_count, component_size);
  }

  @Test
  public void testUnsigned8_2()
  {
    final SMFComponentType type = SMFComponentType.ELEMENT_TYPE_INTEGER_UNSIGNED;
    final int component_count = 2;
    final int component_size = 8;
    this.checkSimpleOne(type, component_count, component_size);
  }

  @Test
  public void testUnsigned8_3()
  {
    final SMFComponentType type = SMFComponentType.ELEMENT_TYPE_INTEGER_UNSIGNED;
    final int component_count = 3;
    final int component_size = 8;
    this.checkSimpleOne(type, component_count, component_size);
  }

  @Test
  public void testUnsigned8_4()
  {
    final SMFComponentType type = SMFComponentType.ELEMENT_TYPE_INTEGER_UNSIGNED;
    final int component_count = 4;
    final int component_size = 8;
    this.checkSimpleOne(type, component_count, component_size);
  }

  @Test
  public void testUnsigned16_1()
  {
    final SMFComponentType type = SMFComponentType.ELEMENT_TYPE_INTEGER_UNSIGNED;
    final int component_count = 1;
    final int component_size = 16;
    this.checkSimpleOne(type, component_count, component_size);
  }

  @Test
  public void testUnsigned16_2()
  {
    final SMFComponentType type = SMFComponentType.ELEMENT_TYPE_INTEGER_UNSIGNED;
    final int component_count = 2;
    final int component_size = 16;
    this.checkSimpleOne(type, component_count, component_size);
  }

  @Test
  public void testUnsigned16_3()
  {
    final SMFComponentType type = SMFComponentType.ELEMENT_TYPE_INTEGER_UNSIGNED;
    final int component_count = 3;
    final int component_size = 16;
    this.checkSimpleOne(type, component_count, component_size);
  }

  @Test
  public void testUnsigned16_4()
  {
    final SMFComponentType type = SMFComponentType.ELEMENT_TYPE_INTEGER_UNSIGNED;
    final int component_count = 4;
    final int component_size = 16;
    this.checkSimpleOne(type, component_count, component_size);
  }

  @Test
  public void testUnsigned32_1()
  {
    final SMFComponentType type = SMFComponentType.ELEMENT_TYPE_INTEGER_UNSIGNED;
    final int component_count = 1;
    final int component_size = 32;
    this.checkSimpleOne(type, component_count, component_size);
  }

  @Test
  public void testUnsigned32_2()
  {
    final SMFComponentType type = SMFComponentType.ELEMENT_TYPE_INTEGER_UNSIGNED;
    final int component_count = 2;
    final int component_size = 32;
    this.checkSimpleOne(type, component_count, component_size);
  }

  @Test
  public void testUnsigned32_3()
  {
    final SMFComponentType type = SMFComponentType.ELEMENT_TYPE_INTEGER_UNSIGNED;
    final int component_count = 3;
    final int component_size = 32;
    this.checkSimpleOne(type, component_count, component_size);
  }

  @Test
  public void testUnsigned32_4()
  {
    final SMFComponentType type = SMFComponentType.ELEMENT_TYPE_INTEGER_UNSIGNED;
    final int component_count = 4;
    final int component_size = 32;
    this.checkSimpleOne(type, component_count, component_size);
  }


  @Test
  public void testUnsigned64_1()
  {
    final SMFComponentType type = SMFComponentType.ELEMENT_TYPE_INTEGER_UNSIGNED;
    final int component_count = 1;
    final int component_size = 64;
    this.checkSimpleOne(type, component_count, component_size);
  }

  @Test
  public void testUnsigned64_2()
  {
    final SMFComponentType type = SMFComponentType.ELEMENT_TYPE_INTEGER_UNSIGNED;
    final int component_count = 2;
    final int component_size = 64;
    this.checkSimpleOne(type, component_count, component_size);
  }

  @Test
  public void testUnsigned64_3()
  {
    final SMFComponentType type = SMFComponentType.ELEMENT_TYPE_INTEGER_UNSIGNED;
    final int component_count = 3;
    final int component_size = 64;
    this.checkSimpleOne(type, component_count, component_size);
  }

  @Test
  public void testUnsigned64_4()
  {
    final SMFComponentType type = SMFComponentType.ELEMENT_TYPE_INTEGER_UNSIGNED;
    final int component_count = 4;
    final int component_size = 64;
    this.checkSimpleOne(type, component_count, component_size);
  }


  @Test
  public void testFloat16_1()
  {
    final SMFComponentType type = SMFComponentType.ELEMENT_TYPE_FLOATING;
    final int component_count = 1;
    final int component_size = 16;
    this.checkSimpleOne(type, component_count, component_size);
  }

  @Test
  public void testFloat16_2()
  {
    final SMFComponentType type = SMFComponentType.ELEMENT_TYPE_FLOATING;
    final int component_count = 2;
    final int component_size = 16;
    this.checkSimpleOne(type, component_count, component_size);
  }

  @Test
  public void testFloat16_3()
  {
    final SMFComponentType type = SMFComponentType.ELEMENT_TYPE_FLOATING;
    final int component_count = 3;
    final int component_size = 16;
    this.checkSimpleOne(type, component_count, component_size);
  }

  @Test
  public void testFloat16_4()
  {
    final SMFComponentType type = SMFComponentType.ELEMENT_TYPE_FLOATING;
    final int component_count = 4;
    final int component_size = 16;
    this.checkSimpleOne(type, component_count, component_size);
  }

  @Test
  public void testFloat32_1()
  {
    final SMFComponentType type = SMFComponentType.ELEMENT_TYPE_FLOATING;
    final int component_count = 1;
    final int component_size = 32;
    this.checkSimpleOne(type, component_count, component_size);
  }

  @Test
  public void testFloat32_2()
  {
    final SMFComponentType type = SMFComponentType.ELEMENT_TYPE_FLOATING;
    final int component_count = 2;
    final int component_size = 32;
    this.checkSimpleOne(type, component_count, component_size);
  }

  @Test
  public void testFloat32_3()
  {
    final SMFComponentType type = SMFComponentType.ELEMENT_TYPE_FLOATING;
    final int component_count = 3;
    final int component_size = 32;
    this.checkSimpleOne(type, component_count, component_size);
  }

  @Test
  public void testFloat32_4()
  {
    final SMFComponentType type = SMFComponentType.ELEMENT_TYPE_FLOATING;
    final int component_count = 4;
    final int component_size = 32;
    this.checkSimpleOne(type, component_count, component_size);
  }

  @Test
  public void testFloat64_1()
  {
    final SMFComponentType type = SMFComponentType.ELEMENT_TYPE_FLOATING;
    final int component_count = 1;
    final int component_size = 64;
    this.checkSimpleOne(type, component_count, component_size);
  }

  @Test
  public void testFloat64_2()
  {
    final SMFComponentType type = SMFComponentType.ELEMENT_TYPE_FLOATING;
    final int component_count = 2;
    final int component_size = 64;
    this.checkSimpleOne(type, component_count, component_size);
  }

  @Test
  public void testFloat64_3()
  {
    final SMFComponentType type = SMFComponentType.ELEMENT_TYPE_FLOATING;
    final int component_count = 3;
    final int component_size = 64;
    this.checkSimpleOne(type, component_count, component_size);
  }

  @Test
  public void testFloat64_4()
  {
    final SMFComponentType type = SMFComponentType.ELEMENT_TYPE_FLOATING;
    final int component_count = 4;
    final int component_size = 64;
    this.checkSimpleOne(type, component_count, component_size);
  }

  private void checkSimpleOne(
    final SMFComponentType type,
    final int component_count,
    final int component_size)
  {
    final SMFAttributeName attr_name = SMFAttributeName.of("x");
    final SMFAttribute attr = SMFAttribute.of(
      attr_name,
      type,
      component_count,
      component_size);

    final SMFByteBufferPackingConfiguration c =
      SMFByteBufferPackingConfiguration.of(List.of(attr));
    Assert.assertEquals(
      (long) (component_count * (component_size / 8)),
      (long) c.vertexSizeOctets());
    Assert.assertEquals(1L, (long) c.attributesOrdered().size());
    Assert.assertTrue(c.attributesOrdered().contains(attr));
    Assert.assertEquals(1L, (long) c.packedAttributesByName().size());
    Assert.assertEquals(
      attr,
      c.packedAttributesByName().get(attr_name).get().attribute());
    Assert.assertEquals(
      attr,
      c.packedAttributesByOffset().get(Integer.valueOf(0)).get().attribute());
    Assert.assertEquals(0L, c.offsetOctetsForIndex(0, 0L));
    Assert.assertEquals(
      (long) c.vertexSizeOctets(), c.offsetOctetsForIndex(0, 1L));
    Assert.assertEquals(
      (long) (c.vertexSizeOctets() * 2), c.offsetOctetsForIndex(0, 2L));
  }
}
