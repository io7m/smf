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

package com.io7m.smfj.tests.core;

import com.io7m.jcoords.core.conversion.CAxis;
import com.io7m.jcoords.core.conversion.CAxisSystem;
import com.io7m.smfj.core.SMFAttribute;
import com.io7m.smfj.core.SMFAttributeName;
import com.io7m.smfj.core.SMFComponentType;
import com.io7m.smfj.core.SMFCoordinateSystem;
import com.io7m.smfj.core.SMFFaceWindingOrder;
import com.io7m.smfj.core.SMFHeader;
import com.io7m.smfj.core.SMFSchemaIdentifier;
import javaslang.collection.HashMap;
import javaslang.collection.List;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public final class SMFHeaderTest
{
  @Rule public ExpectedException expected = ExpectedException.none();

  @Test
  public void testAll()
  {
    final SMFAttribute attr0 = SMFAttribute.of(
      SMFAttributeName.of("x"), SMFComponentType.ELEMENT_TYPE_FLOATING, 4, 32);

    final SMFHeader.Builder b = SMFHeader.builder();
    b.setTriangleCount(128L);
    b.setTriangleIndexSizeBits(16L);
    b.setVertexCount(256L);
    b.setSchemaIdentifier(SMFSchemaIdentifier.of(0x696F376D, 0, 1, 0));
    b.setAttributesInOrder(List.of(attr0));
    b.setAttributesByName(HashMap.of(attr0.name(), attr0));
    b.setCoordinateSystem(SMFCoordinateSystem.of(
      CAxisSystem.of(
        CAxis.AXIS_POSITIVE_X,
        CAxis.AXIS_POSITIVE_Y,
        CAxis.AXIS_NEGATIVE_Z),
      SMFFaceWindingOrder.FACE_WINDING_ORDER_COUNTER_CLOCKWISE));
    final SMFHeader h = b.build();

    Assert.assertEquals(128L, h.triangleCount());
    Assert.assertEquals(256L, h.vertexCount());
    Assert.assertEquals(16L, h.triangleIndexSizeBits());
    Assert.assertEquals(List.of(attr0), h.attributesInOrder());
    Assert.assertEquals(HashMap.of(attr0.name(), attr0), h.attributesByName());
  }

  @Test
  public void testAttributeConsistency0()
  {
    final SMFAttribute attr0 = SMFAttribute.of(
      SMFAttributeName.of("x"), SMFComponentType.ELEMENT_TYPE_FLOATING, 4, 32);
    final SMFAttribute attr1 = SMFAttribute.of(
      SMFAttributeName.of("x"), SMFComponentType.ELEMENT_TYPE_FLOATING, 3, 32);

    final SMFHeader.Builder b = SMFHeader.builder();
    b.setSchemaIdentifier(SMFSchemaIdentifier.of(0x696F376D, 0, 1, 0));
    b.setCoordinateSystem(SMFCoordinateSystem.of(
      CAxisSystem.of(CAxis.AXIS_POSITIVE_X, CAxis.AXIS_POSITIVE_Y, CAxis.AXIS_NEGATIVE_Z),
      SMFFaceWindingOrder.FACE_WINDING_ORDER_COUNTER_CLOCKWISE));
    b.setAttributesInOrder(List.of(attr0));
    b.setAttributesByName(HashMap.of(attr1.name(), attr1));

    this.expected.expect(IllegalArgumentException.class);
    b.build();
  }

  @Test
  public void testAttributeConsistency1()
  {
    final SMFAttribute attr0 = SMFAttribute.of(
      SMFAttributeName.of("x"), SMFComponentType.ELEMENT_TYPE_FLOATING, 4, 32);
    final SMFAttribute attr1 = SMFAttribute.of(
      SMFAttributeName.of("y"), SMFComponentType.ELEMENT_TYPE_FLOATING, 4, 32);

    final SMFHeader.Builder b = SMFHeader.builder();
    b.setSchemaIdentifier(SMFSchemaIdentifier.of(0x696F376D, 0, 1, 0));
    b.setCoordinateSystem(SMFCoordinateSystem.of(
      CAxisSystem.of(CAxis.AXIS_POSITIVE_X, CAxis.AXIS_POSITIVE_Y, CAxis.AXIS_NEGATIVE_Z),
      SMFFaceWindingOrder.FACE_WINDING_ORDER_COUNTER_CLOCKWISE));
    b.setAttributesInOrder(List.of(attr0));
    b.setAttributesByName(HashMap.of(attr1.name(), attr1));

    this.expected.expect(IllegalArgumentException.class);
    b.build();
  }

  @Test
  public void testAttributeConsistency2()
  {
    final SMFAttribute attr0 = SMFAttribute.of(
      SMFAttributeName.of("x"), SMFComponentType.ELEMENT_TYPE_FLOATING, 4, 32);

    final SMFHeader.Builder b = SMFHeader.builder();
    b.setSchemaIdentifier(SMFSchemaIdentifier.of(0x696F376D, 0, 1, 0));
    b.setCoordinateSystem(SMFCoordinateSystem.of(
      CAxisSystem.of(CAxis.AXIS_POSITIVE_X, CAxis.AXIS_POSITIVE_Y, CAxis.AXIS_NEGATIVE_Z),
      SMFFaceWindingOrder.FACE_WINDING_ORDER_COUNTER_CLOCKWISE));
    b.setAttributesInOrder(List.empty());
    b.setAttributesByName(HashMap.of(attr0.name(), attr0));

    this.expected.expect(IllegalArgumentException.class);
    b.build();
  }

  @Test
  public void testAttributeConsistency3()
  {
    final SMFAttribute attr0 = SMFAttribute.of(
      SMFAttributeName.of("x"), SMFComponentType.ELEMENT_TYPE_FLOATING, 4, 32);

    final SMFHeader.Builder b = SMFHeader.builder();
    b.setSchemaIdentifier(SMFSchemaIdentifier.of(0x696F376D, 0, 1, 0));
    b.setCoordinateSystem(SMFCoordinateSystem.of(
      CAxisSystem.of(CAxis.AXIS_POSITIVE_X, CAxis.AXIS_POSITIVE_Y, CAxis.AXIS_NEGATIVE_Z),
      SMFFaceWindingOrder.FACE_WINDING_ORDER_COUNTER_CLOCKWISE));
    b.setAttributesInOrder(List.of(attr0));
    b.setAttributesByName(HashMap.empty());

    this.expected.expect(IllegalArgumentException.class);
    b.build();
  }
}
