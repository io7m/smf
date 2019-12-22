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
import com.io7m.smfj.core.SMFSchemaName;
import com.io7m.smfj.core.SMFTriangles;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public final class SMFHeaderTest
{
  @Test
  public void testAll()
  {
    final SMFAttribute attr0 = SMFAttribute.of(
      SMFAttributeName.of("x"), SMFComponentType.ELEMENT_TYPE_FLOATING, 4, 32);

    final SMFHeader.Builder b = SMFHeader.builder();
    b.setTriangles(SMFTriangles.of(128L, 16));
    b.setVertexCount(256L);
    b.setSchemaIdentifier(SMFSchemaIdentifier.of(SMFSchemaName.of(
      "com.io7m.schema"), 1, 0));
    b.setAttributesInOrder(List.of(attr0));
    b.setCoordinateSystem(SMFCoordinateSystem.of(
      CAxisSystem.of(
        CAxis.AXIS_POSITIVE_X,
        CAxis.AXIS_POSITIVE_Y,
        CAxis.AXIS_NEGATIVE_Z),
      SMFFaceWindingOrder.FACE_WINDING_ORDER_COUNTER_CLOCKWISE));
    final SMFHeader h = b.build();

    Assertions.assertEquals(128L, h.triangles().triangleCount());
    Assertions.assertEquals(256L, h.vertexCount());
    Assertions.assertEquals(16L, h.triangles().triangleIndexSizeBits());
    Assertions.assertEquals(List.of(attr0), h.attributesInOrder());
    Assertions.assertEquals(
      Map.of(attr0.name(), attr0),
      h.attributesByName());
  }

  @Test
  public void testAttributeConsistency0()
  {
    final SMFAttribute attr0 = SMFAttribute.of(
      SMFAttributeName.of("x"), SMFComponentType.ELEMENT_TYPE_FLOATING, 4, 32);
    final SMFAttribute attr1 = SMFAttribute.of(
      SMFAttributeName.of("x"), SMFComponentType.ELEMENT_TYPE_FLOATING, 3, 32);

    final SMFHeader.Builder b = SMFHeader.builder();
    b.setTriangles(SMFTriangles.builder().build());
    b.setSchemaIdentifier(SMFSchemaIdentifier.of(SMFSchemaName.of(
      "com.io7m.schema"), 1, 0));
    b.setCoordinateSystem(SMFCoordinateSystem.of(
      CAxisSystem.of(
        CAxis.AXIS_POSITIVE_X,
        CAxis.AXIS_POSITIVE_Y,
        CAxis.AXIS_NEGATIVE_Z),
      SMFFaceWindingOrder.FACE_WINDING_ORDER_COUNTER_CLOCKWISE));
    b.setAttributesInOrder(List.of(attr0, attr1));

    Assertions.assertThrows(IllegalArgumentException.class, () -> {
      b.build();
    });
  }
}
