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

package com.io7m.smfj.tests.validation.api;

import com.io7m.jcoords.core.conversion.CAxis;
import com.io7m.jcoords.core.conversion.CAxisSystem;
import com.io7m.smfj.core.SMFAttribute;
import com.io7m.smfj.core.SMFAttributeName;
import com.io7m.smfj.core.SMFComponentType;
import com.io7m.smfj.core.SMFCoordinateSystem;
import com.io7m.smfj.core.SMFFaceWindingOrder;
import com.io7m.smfj.core.SMFHeader;
import com.io7m.smfj.core.SMFSchemaIdentifier;
import com.io7m.smfj.validation.api.SMFSchema;
import com.io7m.smfj.validation.api.SMFSchemaAttribute;
import com.io7m.smfj.validation.api.SMFSchemaValidationError;
import com.io7m.smfj.validation.api.SMFSchemaValidatorType;
import javaslang.collection.List;
import javaslang.control.Validation;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;
import java.util.OptionalInt;

public abstract class SMFSchemaValidatorContract
{
  private static final Logger LOG;

  static {
    LOG = LoggerFactory.getLogger(SMFSchemaValidatorContract.class);
  }

  protected abstract SMFSchemaValidatorType create();

  @Test
  public final void testAttributeMissing()
  {
    final SMFAttribute attr_0 =
      SMFAttribute.of(
        SMFAttributeName.of("x"),
        SMFComponentType.ELEMENT_TYPE_FLOATING,
        4,
        32);

    final SMFHeader header =
      SMFHeader.builder()
        .setCoordinateSystem(SMFCoordinateSystem.of(
          CAxisSystem.of(
            CAxis.AXIS_POSITIVE_X,
            CAxis.AXIS_POSITIVE_Y,
            CAxis.AXIS_NEGATIVE_Z),
          SMFFaceWindingOrder.FACE_WINDING_ORDER_COUNTER_CLOCKWISE))
        .setSchemaIdentifier(SMFSchemaIdentifier.of(0x494F374D, 0, 1, 0))
        .build();

    final SMFSchema schema =
      SMFSchema.builder()
        .setSchemaIdentifier(header.schemaIdentifier())
        .setAllowExtraAttributes(false)
        .setRequiredCoordinateSystem(Optional.empty())
        .putRequiredAttributes(
          attr_0.name(),
          SMFSchemaAttribute.builder()
            .setName(attr_0.name())
            .setRequiredComponentCount(OptionalInt.empty())
            .setRequiredComponentSize(OptionalInt.empty())
            .setRequiredType(Optional.empty())
            .build())
        .build();

    final Validation<List<SMFSchemaValidationError>, SMFHeader> r =
      this.create().validate(header, schema);

    Assert.assertTrue(r.isInvalid());

    final List<SMFSchemaValidationError> errors = r.getError();
    errors.forEach(e -> LOG.error("{}", e));
    Assert.assertTrue(errors.exists(e -> e.message().contains(
      "A required attribute is missing")));
  }

  @Test
  public final void testAttributeExtra()
  {
    final SMFAttribute attr_0 =
      SMFAttribute.of(
        SMFAttributeName.of("x"),
        SMFComponentType.ELEMENT_TYPE_FLOATING,
        4,
        32);

    final SMFHeader header =
      SMFHeader.builder()
        .addAttributesInOrder(attr_0)
        .setCoordinateSystem(SMFCoordinateSystem.of(
          CAxisSystem.of(
            CAxis.AXIS_POSITIVE_X,
            CAxis.AXIS_POSITIVE_Y,
            CAxis.AXIS_NEGATIVE_Z),
          SMFFaceWindingOrder.FACE_WINDING_ORDER_COUNTER_CLOCKWISE))
        .setSchemaIdentifier(SMFSchemaIdentifier.of(0x494F374D, 0, 1, 0))
        .build();

    final SMFSchema schema =
      SMFSchema.builder()
        .setSchemaIdentifier(header.schemaIdentifier())
        .setAllowExtraAttributes(false)
        .setRequiredCoordinateSystem(Optional.empty())
        .build();

    final Validation<List<SMFSchemaValidationError>, SMFHeader> r =
      this.create().validate(header, schema);

    Assert.assertTrue(r.isInvalid());

    final List<SMFSchemaValidationError> errors = r.getError();
    errors.forEach(e -> LOG.error("{}", e));
    Assert.assertTrue(errors.exists(e -> e.message().contains(
      "The mesh contains an extra attribute but the schema does not permit them")));
  }

  @Test
  public final void testWrongCoordinateSystem()
  {
    final SMFAttribute attr_0 =
      SMFAttribute.of(
        SMFAttributeName.of("x"),
        SMFComponentType.ELEMENT_TYPE_FLOATING,
        4,
        32);

    final SMFCoordinateSystem coords_rec =
      SMFCoordinateSystem.of(
        CAxisSystem.of(
          CAxis.AXIS_POSITIVE_X,
          CAxis.AXIS_POSITIVE_Y,
          CAxis.AXIS_NEGATIVE_Z),
        SMFFaceWindingOrder.FACE_WINDING_ORDER_COUNTER_CLOCKWISE);

    final SMFCoordinateSystem coords_exp =
      SMFCoordinateSystem.of(
        CAxisSystem.of(
          CAxis.AXIS_POSITIVE_X,
          CAxis.AXIS_POSITIVE_Y,
          CAxis.AXIS_POSITIVE_Z),
        SMFFaceWindingOrder.FACE_WINDING_ORDER_COUNTER_CLOCKWISE);

    final SMFHeader header =
      SMFHeader.builder()
        .addAttributesInOrder(attr_0)
        .setCoordinateSystem(coords_rec)
        .setSchemaIdentifier(SMFSchemaIdentifier.of(0x494F374D, 0, 1, 0))
        .build();

    final SMFSchema schema =
      SMFSchema.builder()
        .setSchemaIdentifier(header.schemaIdentifier())
        .setAllowExtraAttributes(true)
        .setRequiredCoordinateSystem(coords_exp)
        .build();

    final Validation<List<SMFSchemaValidationError>, SMFHeader> r =
      this.create().validate(header, schema);

    Assert.assertTrue(r.isInvalid());

    final List<SMFSchemaValidationError> errors = r.getError();
    errors.forEach(e -> LOG.error("{}", e));
    Assert.assertTrue(errors.exists(e -> e.message().contains(
      "The mesh has an unexpected coordinate system")));
  }

  @Test
  public final void testCorrectCoordinateSystem()
  {
    final SMFAttribute attr_0 =
      SMFAttribute.of(
        SMFAttributeName.of("x"),
        SMFComponentType.ELEMENT_TYPE_FLOATING,
        4,
        32);

    final SMFCoordinateSystem coords_exp =
      SMFCoordinateSystem.of(
        CAxisSystem.of(
          CAxis.AXIS_POSITIVE_X,
          CAxis.AXIS_POSITIVE_Y,
          CAxis.AXIS_POSITIVE_Z),
        SMFFaceWindingOrder.FACE_WINDING_ORDER_COUNTER_CLOCKWISE);

    final SMFHeader header =
      SMFHeader.builder()
        .addAttributesInOrder(attr_0)
        .setCoordinateSystem(coords_exp)
        .setSchemaIdentifier(SMFSchemaIdentifier.of(0x494F374D, 0, 1, 0))
        .build();

    final SMFSchema schema =
      SMFSchema.builder()
        .setSchemaIdentifier(header.schemaIdentifier())
        .setAllowExtraAttributes(true)
        .setRequiredCoordinateSystem(coords_exp)
        .build();

    final Validation<List<SMFSchemaValidationError>, SMFHeader> r =
      this.create().validate(header, schema);

    Assert.assertTrue(r.isValid());
  }

  @Test
  public final void testCorrectComponentType()
  {
    final SMFAttribute attr_0 =
      SMFAttribute.of(
        SMFAttributeName.of("x"),
        SMFComponentType.ELEMENT_TYPE_FLOATING,
        4,
        32);

    final SMFHeader header =
      SMFHeader.builder()
        .addAttributesInOrder(attr_0)
        .setCoordinateSystem(SMFCoordinateSystem.of(
          CAxisSystem.of(
            CAxis.AXIS_POSITIVE_X,
            CAxis.AXIS_POSITIVE_Y,
            CAxis.AXIS_NEGATIVE_Z),
          SMFFaceWindingOrder.FACE_WINDING_ORDER_COUNTER_CLOCKWISE))
        .setSchemaIdentifier(SMFSchemaIdentifier.of(0x494F374D, 0, 1, 0))
        .build();

    final SMFSchema schema =
      SMFSchema.builder()
        .setSchemaIdentifier(header.schemaIdentifier())
        .setAllowExtraAttributes(false)
        .setRequiredCoordinateSystem(Optional.empty())
        .putRequiredAttributes(
          attr_0.name(),
          SMFSchemaAttribute.builder()
            .setName(attr_0.name())
            .setRequiredComponentCount(OptionalInt.empty())
            .setRequiredComponentSize(OptionalInt.empty())
            .setRequiredType(SMFComponentType.ELEMENT_TYPE_FLOATING)
            .build())
        .build();

    final Validation<List<SMFSchemaValidationError>, SMFHeader> r =
      this.create().validate(header, schema);

    Assert.assertTrue(r.isValid());
  }

  @Test
  public final void testWrongComponentType()
  {
    final SMFAttribute attr_0 =
      SMFAttribute.of(
        SMFAttributeName.of("x"),
        SMFComponentType.ELEMENT_TYPE_FLOATING,
        4,
        32);

    final SMFHeader header =
      SMFHeader.builder()
        .addAttributesInOrder(attr_0)
        .setCoordinateSystem(SMFCoordinateSystem.of(
          CAxisSystem.of(
            CAxis.AXIS_POSITIVE_X,
            CAxis.AXIS_POSITIVE_Y,
            CAxis.AXIS_NEGATIVE_Z),
          SMFFaceWindingOrder.FACE_WINDING_ORDER_COUNTER_CLOCKWISE))
        .setSchemaIdentifier(SMFSchemaIdentifier.of(0x494F374D, 0, 1, 0))
        .build();

    final SMFSchema schema =
      SMFSchema.builder()
        .setSchemaIdentifier(header.schemaIdentifier())
        .setAllowExtraAttributes(false)
        .setRequiredCoordinateSystem(Optional.empty())
        .putRequiredAttributes(
          attr_0.name(),
          SMFSchemaAttribute.builder()
            .setName(attr_0.name())
            .setRequiredComponentCount(OptionalInt.empty())
            .setRequiredComponentSize(OptionalInt.empty())
            .setRequiredType(SMFComponentType.ELEMENT_TYPE_INTEGER_SIGNED)
            .build())
        .build();

    final Validation<List<SMFSchemaValidationError>, SMFHeader> r =
      this.create().validate(header, schema);

    Assert.assertTrue(r.isInvalid());

    final List<SMFSchemaValidationError> errors = r.getError();
    errors.forEach(e -> LOG.error("{}", e));
    Assert.assertTrue(errors.exists(e -> e.message().contains(
      "Attribute is not of the expected type")));
  }

  @Test
  public final void testWrongComponentCount()
  {
    final SMFAttribute attr_0 =
      SMFAttribute.of(
        SMFAttributeName.of("x"),
        SMFComponentType.ELEMENT_TYPE_FLOATING,
        4,
        32);

    final SMFHeader header =
      SMFHeader.builder()
        .addAttributesInOrder(attr_0)
        .setCoordinateSystem(SMFCoordinateSystem.of(
          CAxisSystem.of(
            CAxis.AXIS_POSITIVE_X,
            CAxis.AXIS_POSITIVE_Y,
            CAxis.AXIS_NEGATIVE_Z),
          SMFFaceWindingOrder.FACE_WINDING_ORDER_COUNTER_CLOCKWISE))
        .setSchemaIdentifier(SMFSchemaIdentifier.of(0x494F374D, 0, 1, 0))
        .build();

    final SMFSchema schema =
      SMFSchema.builder()
        .setSchemaIdentifier(header.schemaIdentifier())
        .setAllowExtraAttributes(false)
        .setRequiredCoordinateSystem(Optional.empty())
        .putRequiredAttributes(
          attr_0.name(),
          SMFSchemaAttribute.builder()
            .setName(attr_0.name())
            .setRequiredComponentCount(3)
            .setRequiredComponentSize(OptionalInt.empty())
            .setRequiredType(Optional.empty())
            .build())
        .build();

    final Validation<List<SMFSchemaValidationError>, SMFHeader> r =
      this.create().validate(header, schema);

    Assert.assertTrue(r.isInvalid());

    final List<SMFSchemaValidationError> errors = r.getError();
    errors.forEach(e -> LOG.error("{}", e));
    Assert.assertTrue(errors.exists(e -> e.message().contains(
      "Attribute component count is not the expected count")));
  }

  @Test
  public final void testCorrectComponentCount()
  {
    final SMFAttribute attr_0 =
      SMFAttribute.of(
        SMFAttributeName.of("x"),
        SMFComponentType.ELEMENT_TYPE_FLOATING,
        4,
        32);

    final SMFHeader header =
      SMFHeader.builder()
        .addAttributesInOrder(attr_0)
        .setCoordinateSystem(SMFCoordinateSystem.of(
          CAxisSystem.of(
            CAxis.AXIS_POSITIVE_X,
            CAxis.AXIS_POSITIVE_Y,
            CAxis.AXIS_NEGATIVE_Z),
          SMFFaceWindingOrder.FACE_WINDING_ORDER_COUNTER_CLOCKWISE))
        .setSchemaIdentifier(SMFSchemaIdentifier.of(0x494F374D, 0, 1, 0))
        .build();

    final SMFSchema schema =
      SMFSchema.builder()
        .setSchemaIdentifier(header.schemaIdentifier())
        .setAllowExtraAttributes(false)
        .setRequiredCoordinateSystem(Optional.empty())
        .putRequiredAttributes(
          attr_0.name(),
          SMFSchemaAttribute.builder()
            .setName(attr_0.name())
            .setRequiredComponentCount(4)
            .setRequiredComponentSize(OptionalInt.empty())
            .setRequiredType(Optional.empty())
            .build())
        .build();

    final Validation<List<SMFSchemaValidationError>, SMFHeader> r =
      this.create().validate(header, schema);

    Assert.assertTrue(r.isValid());
  }

  @Test
  public final void testWrongComponentSize()
  {
    final SMFAttribute attr_0 =
      SMFAttribute.of(
        SMFAttributeName.of("x"),
        SMFComponentType.ELEMENT_TYPE_FLOATING,
        4,
        32);

    final SMFHeader header =
      SMFHeader.builder()
        .addAttributesInOrder(attr_0)
        .setCoordinateSystem(SMFCoordinateSystem.of(
          CAxisSystem.of(
            CAxis.AXIS_POSITIVE_X,
            CAxis.AXIS_POSITIVE_Y,
            CAxis.AXIS_NEGATIVE_Z),
          SMFFaceWindingOrder.FACE_WINDING_ORDER_COUNTER_CLOCKWISE))
        .setSchemaIdentifier(SMFSchemaIdentifier.of(0x494F374D, 0, 1, 0))
        .build();

    final SMFSchema schema =
      SMFSchema.builder()
        .setSchemaIdentifier(header.schemaIdentifier())
        .setAllowExtraAttributes(false)
        .setRequiredCoordinateSystem(Optional.empty())
        .putRequiredAttributes(
          attr_0.name(),
          SMFSchemaAttribute.builder()
            .setName(attr_0.name())
            .setRequiredComponentCount(OptionalInt.empty())
            .setRequiredComponentSize(16)
            .setRequiredType(Optional.empty())
            .build())
        .build();

    final Validation<List<SMFSchemaValidationError>, SMFHeader> r =
      this.create().validate(header, schema);

    Assert.assertTrue(r.isInvalid());

    final List<SMFSchemaValidationError> errors = r.getError();
    errors.forEach(e -> LOG.error("{}", e));
    Assert.assertTrue(errors.exists(e -> e.message().contains(
      "Attribute component size is not the expected size")));
  }

  @Test
  public final void testCorrectComponentSize()
  {
    final SMFAttribute attr_0 =
      SMFAttribute.of(
        SMFAttributeName.of("x"),
        SMFComponentType.ELEMENT_TYPE_FLOATING,
        4,
        32);

    final SMFHeader header =
      SMFHeader.builder()
        .addAttributesInOrder(attr_0)
        .setCoordinateSystem(SMFCoordinateSystem.of(
          CAxisSystem.of(
            CAxis.AXIS_POSITIVE_X,
            CAxis.AXIS_POSITIVE_Y,
            CAxis.AXIS_NEGATIVE_Z),
          SMFFaceWindingOrder.FACE_WINDING_ORDER_COUNTER_CLOCKWISE))
        .setSchemaIdentifier(SMFSchemaIdentifier.of(0x494F374D, 0, 1, 0))
        .build();

    final SMFSchema schema =
      SMFSchema.builder()
        .setSchemaIdentifier(header.schemaIdentifier())
        .setAllowExtraAttributes(false)
        .setRequiredCoordinateSystem(Optional.empty())
        .putRequiredAttributes(
          attr_0.name(),
          SMFSchemaAttribute.builder()
            .setName(attr_0.name())
            .setRequiredComponentCount(OptionalInt.empty())
            .setRequiredComponentSize(32)
            .setRequiredType(Optional.empty())
            .build())
        .build();

    final Validation<List<SMFSchemaValidationError>, SMFHeader> r =
      this.create().validate(header, schema);

    Assert.assertTrue(r.isValid());
  }
}
