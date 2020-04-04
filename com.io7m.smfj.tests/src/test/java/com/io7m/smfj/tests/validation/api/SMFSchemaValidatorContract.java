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

import com.io7m.jcoords.core.conversion.CAxisSystem;
import com.io7m.smfj.core.SMFAttribute;
import com.io7m.smfj.core.SMFAttributeName;
import com.io7m.smfj.core.SMFCoordinateSystem;
import com.io7m.smfj.core.SMFErrorType;
import com.io7m.smfj.core.SMFHeader;
import com.io7m.smfj.core.SMFSchemaIdentifier;
import com.io7m.smfj.core.SMFSchemaName;
import com.io7m.smfj.core.SMFTriangles;
import com.io7m.smfj.validation.api.SMFSchema;
import com.io7m.smfj.validation.api.SMFSchemaAttribute;
import com.io7m.smfj.validation.api.SMFSchemaValidatorType;
import java.util.List;
import java.util.Optional;
import java.util.OptionalInt;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.io7m.jcoords.core.conversion.CAxis.AXIS_NEGATIVE_Z;
import static com.io7m.jcoords.core.conversion.CAxis.AXIS_POSITIVE_X;
import static com.io7m.jcoords.core.conversion.CAxis.AXIS_POSITIVE_Y;
import static com.io7m.jcoords.core.conversion.CAxis.AXIS_POSITIVE_Z;
import static com.io7m.smfj.core.SMFComponentType.ELEMENT_TYPE_FLOATING;
import static com.io7m.smfj.core.SMFComponentType.ELEMENT_TYPE_INTEGER_SIGNED;
import static com.io7m.smfj.core.SMFFaceWindingOrder.FACE_WINDING_ORDER_COUNTER_CLOCKWISE;
import static com.io7m.smfj.validation.api.SMFSchemaAllowExtraAttributes.SMF_EXTRA_ATTRIBUTES_ALLOWED;
import static com.io7m.smfj.validation.api.SMFSchemaAllowExtraAttributes.SMF_EXTRA_ATTRIBUTES_DISALLOWED;
import static com.io7m.smfj.validation.api.SMFSchemaRequireTriangles.SMF_TRIANGLES_REQUIRED;
import static com.io7m.smfj.validation.api.SMFSchemaRequireVertices.SMF_VERTICES_REQUIRED;

public abstract class SMFSchemaValidatorContract
{
  private static final Logger LOG;

  static {
    LOG = LoggerFactory.getLogger(SMFSchemaValidatorContract.class);
  }

  protected abstract SMFSchemaValidatorType create();

  @Test
  public final void testRequiredAttributeMissing()
  {
    final SMFAttribute attr_0 =
      SMFAttribute.of(
        SMFAttributeName.of("x"),
        ELEMENT_TYPE_FLOATING,
        4,
        32);

    final SMFHeader header =
      SMFHeader.builder()
        .setTriangles(SMFTriangles.builder().build())
        .setCoordinateSystem(SMFCoordinateSystem.of(
          CAxisSystem.of(
            AXIS_POSITIVE_X,
            AXIS_POSITIVE_Y,
            AXIS_NEGATIVE_Z),
          FACE_WINDING_ORDER_COUNTER_CLOCKWISE))
        .setSchemaIdentifier(SMFSchemaIdentifier.of(SMFSchemaName.of(
          "com.io7m.schema"), 1, 0))
        .build();

    final SMFSchema schema =
      SMFSchema.builder()
        .setSchemaIdentifier(header.schemaIdentifier().get())
        .setAllowExtraAttributes(SMF_EXTRA_ATTRIBUTES_DISALLOWED)
        .setRequiredCoordinateSystem(Optional.empty())
        .putRequiredAttributes(
          attr_0.name(),
          SMFSchemaAttribute.builder()
            .setName(attr_0.name())
            .setRequiredComponentCount(OptionalInt.empty())
            .setRequiredComponentSize(OptionalInt.empty())
            .setRequiredComponentType(Optional.empty())
            .build())
        .build();

    final var r =
      this.create().validate(header, schema);

    Assertions.assertTrue(r.isFailed());

    final List<? extends SMFErrorType> errors = r.errors();
    errors.forEach(e -> LOG.error("{}", e));
    Assertions.assertTrue(errors.stream().anyMatch(e -> e.message().contains(
      "A required attribute is missing")));
  }

  @Test
  public final void testAttributeExtra()
  {
    final SMFAttribute attr_0 =
      SMFAttribute.of(
        SMFAttributeName.of("x"),
        ELEMENT_TYPE_FLOATING,
        4,
        32);

    final SMFHeader header =
      SMFHeader.builder()
        .addAttributesInOrder(attr_0)
        .setTriangles(SMFTriangles.builder().build())
        .setCoordinateSystem(SMFCoordinateSystem.of(
          CAxisSystem.of(
            AXIS_POSITIVE_X,
            AXIS_POSITIVE_Y,
            AXIS_NEGATIVE_Z),
          FACE_WINDING_ORDER_COUNTER_CLOCKWISE))
        .setSchemaIdentifier(SMFSchemaIdentifier.of(SMFSchemaName.of(
          "com.io7m.schema"), 1, 0))
        .build();

    final SMFSchema schema =
      SMFSchema.builder()
        .setSchemaIdentifier(header.schemaIdentifier().get())
        .setAllowExtraAttributes(SMF_EXTRA_ATTRIBUTES_DISALLOWED)
        .setRequiredCoordinateSystem(Optional.empty())
        .build();

    final var r =
      this.create().validate(header, schema);

    Assertions.assertTrue(r.isFailed());

    final List<? extends SMFErrorType> errors = r.errors();
    errors.forEach(e -> LOG.error("{}", e));
    Assertions.assertTrue(errors.stream().anyMatch(e -> e.message().contains(
      "The mesh contains an extra attribute but the schema does not permit them")));
  }

  @Test
  public final void testWrongCoordinateSystem()
  {
    final SMFAttribute attr_0 =
      SMFAttribute.of(
        SMFAttributeName.of("x"),
        ELEMENT_TYPE_FLOATING,
        4,
        32);

    final SMFCoordinateSystem coords_rec =
      SMFCoordinateSystem.of(
        CAxisSystem.of(
          AXIS_POSITIVE_X,
          AXIS_POSITIVE_Y,
          AXIS_NEGATIVE_Z),
        FACE_WINDING_ORDER_COUNTER_CLOCKWISE);

    final SMFCoordinateSystem coords_exp =
      SMFCoordinateSystem.of(
        CAxisSystem.of(
          AXIS_POSITIVE_X,
          AXIS_POSITIVE_Y,
          AXIS_POSITIVE_Z),
        FACE_WINDING_ORDER_COUNTER_CLOCKWISE);

    final SMFHeader header =
      SMFHeader.builder()
        .addAttributesInOrder(attr_0)
        .setCoordinateSystem(coords_rec)
        .setTriangles(SMFTriangles.builder().build())
        .setSchemaIdentifier(SMFSchemaIdentifier.of(SMFSchemaName.of(
          "com.io7m.schema"), 1, 0))
        .build();

    final SMFSchema schema =
      SMFSchema.builder()
        .setSchemaIdentifier(header.schemaIdentifier().get())
        .setAllowExtraAttributes(SMF_EXTRA_ATTRIBUTES_ALLOWED)
        .setRequiredCoordinateSystem(coords_exp)
        .build();

    final var r =
      this.create().validate(header, schema);

    Assertions.assertTrue(r.isFailed());

    final List<? extends SMFErrorType> errors = r.errors();
    errors.forEach(e -> LOG.error("{}", e));
    Assertions.assertTrue(errors.stream().anyMatch(e -> e.message().contains(
      "The mesh has an unexpected coordinate system")));
  }

  @Test
  public final void testCorrectCoordinateSystem()
  {
    final SMFAttribute attr_0 =
      SMFAttribute.of(
        SMFAttributeName.of("x"),
        ELEMENT_TYPE_FLOATING,
        4,
        32);

    final SMFCoordinateSystem coords_exp =
      SMFCoordinateSystem.of(
        CAxisSystem.of(
          AXIS_POSITIVE_X,
          AXIS_POSITIVE_Y,
          AXIS_POSITIVE_Z),
        FACE_WINDING_ORDER_COUNTER_CLOCKWISE);

    final SMFHeader header =
      SMFHeader.builder()
        .addAttributesInOrder(attr_0)
        .setTriangles(SMFTriangles.builder().build())
        .setCoordinateSystem(coords_exp)
        .setSchemaIdentifier(SMFSchemaIdentifier.of(SMFSchemaName.of(
          "com.io7m.schema"), 1, 0))
        .build();

    final SMFSchema schema =
      SMFSchema.builder()
        .setSchemaIdentifier(header.schemaIdentifier().get())
        .setAllowExtraAttributes(SMF_EXTRA_ATTRIBUTES_ALLOWED)
        .setRequiredCoordinateSystem(coords_exp)
        .build();

    final var r =
      this.create().validate(header, schema);

    Assertions.assertTrue(r.isSucceeded());
  }

  @Test
  public final void testRequiredCorrectComponentType()
  {
    final SMFAttribute attr_0 =
      SMFAttribute.of(
        SMFAttributeName.of("x"),
        ELEMENT_TYPE_FLOATING,
        4,
        32);

    final SMFHeader header =
      SMFHeader.builder()
        .addAttributesInOrder(attr_0)
        .setTriangles(SMFTriangles.builder().build())
        .setCoordinateSystem(SMFCoordinateSystem.of(
          CAxisSystem.of(
            AXIS_POSITIVE_X,
            AXIS_POSITIVE_Y,
            AXIS_NEGATIVE_Z),
          FACE_WINDING_ORDER_COUNTER_CLOCKWISE))
        .setSchemaIdentifier(SMFSchemaIdentifier.of(SMFSchemaName.of(
          "com.io7m.schema"), 1, 0))
        .build();

    final SMFSchema schema =
      SMFSchema.builder()
        .setSchemaIdentifier(header.schemaIdentifier().get())
        .setAllowExtraAttributes(SMF_EXTRA_ATTRIBUTES_DISALLOWED)
        .setRequiredCoordinateSystem(Optional.empty())
        .putRequiredAttributes(
          attr_0.name(),
          SMFSchemaAttribute.builder()
            .setName(attr_0.name())
            .setRequiredComponentCount(OptionalInt.empty())
            .setRequiredComponentSize(OptionalInt.empty())
            .setRequiredComponentType(ELEMENT_TYPE_FLOATING)
            .build())
        .build();

    final var r =
      this.create().validate(header, schema);

    Assertions.assertTrue(r.isSucceeded());
  }

  @Test
  public final void testRequiredWrongComponentType()
  {
    final SMFAttribute attr_0 =
      SMFAttribute.of(
        SMFAttributeName.of("x"),
        ELEMENT_TYPE_FLOATING,
        4,
        32);

    final SMFHeader header =
      SMFHeader.builder()
        .addAttributesInOrder(attr_0)
        .setTriangles(SMFTriangles.builder().build())
        .setCoordinateSystem(SMFCoordinateSystem.of(
          CAxisSystem.of(
            AXIS_POSITIVE_X,
            AXIS_POSITIVE_Y,
            AXIS_NEGATIVE_Z),
          FACE_WINDING_ORDER_COUNTER_CLOCKWISE))
        .setSchemaIdentifier(SMFSchemaIdentifier.of(SMFSchemaName.of(
          "com.io7m.schema"), 1, 0))
        .build();

    final SMFSchema schema =
      SMFSchema.builder()
        .setSchemaIdentifier(header.schemaIdentifier().get())
        .setAllowExtraAttributes(SMF_EXTRA_ATTRIBUTES_DISALLOWED)
        .setRequiredCoordinateSystem(Optional.empty())
        .putRequiredAttributes(
          attr_0.name(),
          SMFSchemaAttribute.builder()
            .setName(attr_0.name())
            .setRequiredComponentCount(OptionalInt.empty())
            .setRequiredComponentSize(OptionalInt.empty())
            .setRequiredComponentType(ELEMENT_TYPE_INTEGER_SIGNED)
            .build())
        .build();

    final var r =
      this.create().validate(header, schema);

    Assertions.assertTrue(r.isFailed());

    final List<? extends SMFErrorType> errors = r.errors();
    errors.forEach(e -> LOG.error("{}", e));
    Assertions.assertTrue(errors.stream().anyMatch(e -> e.message().contains(
      "Attribute is not of the expected type")));
  }

  @Test
  public final void testRequiredWrongComponentCount()
  {
    final SMFAttribute attr_0 =
      SMFAttribute.of(
        SMFAttributeName.of("x"),
        ELEMENT_TYPE_FLOATING,
        4,
        32);

    final SMFHeader header =
      SMFHeader.builder()
        .addAttributesInOrder(attr_0)
        .setTriangles(SMFTriangles.builder().build())
        .setCoordinateSystem(SMFCoordinateSystem.of(
          CAxisSystem.of(
            AXIS_POSITIVE_X,
            AXIS_POSITIVE_Y,
            AXIS_NEGATIVE_Z),
          FACE_WINDING_ORDER_COUNTER_CLOCKWISE))
        .setSchemaIdentifier(SMFSchemaIdentifier.of(SMFSchemaName.of(
          "com.io7m.schema"), 1, 0))
        .build();

    final SMFSchema schema =
      SMFSchema.builder()
        .setSchemaIdentifier(header.schemaIdentifier().get())
        .setAllowExtraAttributes(SMF_EXTRA_ATTRIBUTES_DISALLOWED)
        .setRequiredCoordinateSystem(Optional.empty())
        .putRequiredAttributes(
          attr_0.name(),
          SMFSchemaAttribute.builder()
            .setName(attr_0.name())
            .setRequiredComponentCount(3)
            .setRequiredComponentSize(OptionalInt.empty())
            .setRequiredComponentType(Optional.empty())
            .build())
        .build();

    final var r =
      this.create().validate(header, schema);

    Assertions.assertTrue(r.isFailed());

    final List<? extends SMFErrorType> errors = r.errors();
    errors.forEach(e -> LOG.error("{}", e));
    Assertions.assertTrue(errors.stream().anyMatch(e -> e.message().contains(
      "Attribute component count is not the expected count")));
  }

  @Test
  public final void testRequiredCorrectComponentCount()
  {
    final SMFAttribute attr_0 =
      SMFAttribute.of(
        SMFAttributeName.of("x"),
        ELEMENT_TYPE_FLOATING,
        4,
        32);

    final SMFHeader header =
      SMFHeader.builder()
        .addAttributesInOrder(attr_0)
        .setTriangles(SMFTriangles.builder().build())
        .setCoordinateSystem(SMFCoordinateSystem.of(
          CAxisSystem.of(
            AXIS_POSITIVE_X,
            AXIS_POSITIVE_Y,
            AXIS_NEGATIVE_Z),
          FACE_WINDING_ORDER_COUNTER_CLOCKWISE))
        .setSchemaIdentifier(SMFSchemaIdentifier.of(SMFSchemaName.of(
          "com.io7m.schema"), 1, 0))
        .build();

    final SMFSchema schema =
      SMFSchema.builder()
        .setSchemaIdentifier(header.schemaIdentifier().get())
        .setAllowExtraAttributes(SMF_EXTRA_ATTRIBUTES_DISALLOWED)
        .setRequiredCoordinateSystem(Optional.empty())
        .putRequiredAttributes(
          attr_0.name(),
          SMFSchemaAttribute.builder()
            .setName(attr_0.name())
            .setRequiredComponentCount(4)
            .setRequiredComponentSize(OptionalInt.empty())
            .setRequiredComponentType(Optional.empty())
            .build())
        .build();

    final var r =
      this.create().validate(header, schema);

    Assertions.assertTrue(r.isSucceeded());
  }

  @Test
  public final void testRequiredWrongComponentSize()
  {
    final SMFAttribute attr_0 =
      SMFAttribute.of(
        SMFAttributeName.of("x"),
        ELEMENT_TYPE_FLOATING,
        4,
        32);

    final SMFHeader header =
      SMFHeader.builder()
        .addAttributesInOrder(attr_0)
        .setTriangles(SMFTriangles.builder().build())
        .setCoordinateSystem(SMFCoordinateSystem.of(
          CAxisSystem.of(
            AXIS_POSITIVE_X,
            AXIS_POSITIVE_Y,
            AXIS_NEGATIVE_Z),
          FACE_WINDING_ORDER_COUNTER_CLOCKWISE))
        .setSchemaIdentifier(SMFSchemaIdentifier.of(SMFSchemaName.of(
          "com.io7m.schema"), 1, 0))
        .build();

    final SMFSchema schema =
      SMFSchema.builder()
        .setSchemaIdentifier(header.schemaIdentifier().get())
        .setAllowExtraAttributes(SMF_EXTRA_ATTRIBUTES_DISALLOWED)
        .setRequiredCoordinateSystem(Optional.empty())
        .putRequiredAttributes(
          attr_0.name(),
          SMFSchemaAttribute.builder()
            .setName(attr_0.name())
            .setRequiredComponentCount(OptionalInt.empty())
            .setRequiredComponentSize(16)
            .setRequiredComponentType(Optional.empty())
            .build())
        .build();

    final var r =
      this.create().validate(header, schema);

    Assertions.assertTrue(r.isFailed());

    final List<? extends SMFErrorType> errors = r.errors();
    errors.forEach(e -> LOG.error("{}", e));
    Assertions.assertTrue(errors.stream().anyMatch(e -> e.message().contains(
      "Attribute component size is not the expected size")));
  }

  @Test
  public final void testRequiredCorrectComponentSize()
  {
    final SMFAttribute attr_0 =
      SMFAttribute.of(
        SMFAttributeName.of("x"),
        ELEMENT_TYPE_FLOATING,
        4,
        32);

    final SMFHeader header =
      SMFHeader.builder()
        .addAttributesInOrder(attr_0)
        .setTriangles(SMFTriangles.builder().build())
        .setCoordinateSystem(SMFCoordinateSystem.of(
          CAxisSystem.of(
            AXIS_POSITIVE_X,
            AXIS_POSITIVE_Y,
            AXIS_NEGATIVE_Z),
          FACE_WINDING_ORDER_COUNTER_CLOCKWISE))
        .setSchemaIdentifier(SMFSchemaIdentifier.of(SMFSchemaName.of(
          "com.io7m.schema"), 1, 0))
        .build();

    final SMFSchema schema =
      SMFSchema.builder()
        .setSchemaIdentifier(header.schemaIdentifier().get())
        .setAllowExtraAttributes(SMF_EXTRA_ATTRIBUTES_DISALLOWED)
        .setRequiredCoordinateSystem(Optional.empty())
        .putRequiredAttributes(
          attr_0.name(),
          SMFSchemaAttribute.builder()
            .setName(attr_0.name())
            .setRequiredComponentCount(OptionalInt.empty())
            .setRequiredComponentSize(32)
            .setRequiredComponentType(Optional.empty())
            .build())
        .build();

    final var r =
      this.create().validate(header, schema);

    Assertions.assertTrue(r.isSucceeded());
  }

  @Test
  public final void testOptionalCorrectComponentType()
  {
    final SMFAttribute attr_0 =
      SMFAttribute.of(
        SMFAttributeName.of("x"),
        ELEMENT_TYPE_FLOATING,
        4,
        32);

    final SMFHeader header =
      SMFHeader.builder()
        .addAttributesInOrder(attr_0)
        .setTriangles(SMFTriangles.builder().build())
        .setCoordinateSystem(SMFCoordinateSystem.of(
          CAxisSystem.of(
            AXIS_POSITIVE_X,
            AXIS_POSITIVE_Y,
            AXIS_NEGATIVE_Z),
          FACE_WINDING_ORDER_COUNTER_CLOCKWISE))
        .setSchemaIdentifier(SMFSchemaIdentifier.of(SMFSchemaName.of(
          "com.io7m.schema"), 1, 0))
        .build();

    final SMFSchema schema =
      SMFSchema.builder()
        .setSchemaIdentifier(header.schemaIdentifier().get())
        .setAllowExtraAttributes(SMF_EXTRA_ATTRIBUTES_DISALLOWED)
        .setRequiredCoordinateSystem(Optional.empty())
        .putOptionalAttributes(
          attr_0.name(),
          SMFSchemaAttribute.builder()
            .setName(attr_0.name())
            .setRequiredComponentCount(OptionalInt.empty())
            .setRequiredComponentSize(OptionalInt.empty())
            .setRequiredComponentType(ELEMENT_TYPE_FLOATING)
            .build())
        .build();

    final var r =
      this.create().validate(header, schema);

    Assertions.assertTrue(r.isSucceeded());
  }

  @Test
  public final void testOptionalWrongComponentType()
  {
    final SMFAttribute attr_0 =
      SMFAttribute.of(
        SMFAttributeName.of("x"),
        ELEMENT_TYPE_FLOATING,
        4,
        32);

    final SMFHeader header =
      SMFHeader.builder()
        .addAttributesInOrder(attr_0)
        .setTriangles(SMFTriangles.builder().build())
        .setCoordinateSystem(SMFCoordinateSystem.of(
          CAxisSystem.of(
            AXIS_POSITIVE_X,
            AXIS_POSITIVE_Y,
            AXIS_NEGATIVE_Z),
          FACE_WINDING_ORDER_COUNTER_CLOCKWISE))
        .setSchemaIdentifier(SMFSchemaIdentifier.of(SMFSchemaName.of(
          "com.io7m.schema"), 1, 0))
        .build();

    final SMFSchema schema =
      SMFSchema.builder()
        .setSchemaIdentifier(header.schemaIdentifier().get())
        .setAllowExtraAttributes(SMF_EXTRA_ATTRIBUTES_DISALLOWED)
        .setRequiredCoordinateSystem(Optional.empty())
        .putOptionalAttributes(
          attr_0.name(),
          SMFSchemaAttribute.builder()
            .setName(attr_0.name())
            .setRequiredComponentCount(OptionalInt.empty())
            .setRequiredComponentSize(OptionalInt.empty())
            .setRequiredComponentType(ELEMENT_TYPE_INTEGER_SIGNED)
            .build())
        .build();

    final var r =
      this.create().validate(header, schema);

    Assertions.assertTrue(r.isFailed());

    final List<? extends SMFErrorType> errors = r.errors();
    errors.forEach(e -> LOG.error("{}", e));
    Assertions.assertTrue(errors.stream().anyMatch(e -> e.message().contains(
      "Attribute is not of the expected type")));
  }

  @Test
  public final void testOptionalWrongComponentCount()
  {
    final SMFAttribute attr_0 =
      SMFAttribute.of(
        SMFAttributeName.of("x"),
        ELEMENT_TYPE_FLOATING,
        4,
        32);

    final SMFHeader header =
      SMFHeader.builder()
        .addAttributesInOrder(attr_0)
        .setTriangles(SMFTriangles.builder().build())
        .setCoordinateSystem(SMFCoordinateSystem.of(
          CAxisSystem.of(
            AXIS_POSITIVE_X,
            AXIS_POSITIVE_Y,
            AXIS_NEGATIVE_Z),
          FACE_WINDING_ORDER_COUNTER_CLOCKWISE))
        .setSchemaIdentifier(SMFSchemaIdentifier.of(SMFSchemaName.of(
          "com.io7m.schema"), 1, 0))
        .build();

    final SMFSchema schema =
      SMFSchema.builder()
        .setSchemaIdentifier(header.schemaIdentifier().get())
        .setAllowExtraAttributes(SMF_EXTRA_ATTRIBUTES_DISALLOWED)
        .setRequiredCoordinateSystem(Optional.empty())
        .putOptionalAttributes(
          attr_0.name(),
          SMFSchemaAttribute.builder()
            .setName(attr_0.name())
            .setRequiredComponentCount(3)
            .setRequiredComponentSize(OptionalInt.empty())
            .setRequiredComponentType(Optional.empty())
            .build())
        .build();

    final var r =
      this.create().validate(header, schema);

    Assertions.assertTrue(r.isFailed());

    final List<? extends SMFErrorType> errors = r.errors();
    errors.forEach(e -> LOG.error("{}", e));
    Assertions.assertTrue(errors.stream().anyMatch(e -> e.message().contains(
      "Attribute component count is not the expected count")));
  }

  @Test
  public final void testOptionalCorrectComponentCount()
  {
    final SMFAttribute attr_0 =
      SMFAttribute.of(
        SMFAttributeName.of("x"),
        ELEMENT_TYPE_FLOATING,
        4,
        32);

    final SMFHeader header =
      SMFHeader.builder()
        .addAttributesInOrder(attr_0)
        .setTriangles(SMFTriangles.builder().build())
        .setCoordinateSystem(SMFCoordinateSystem.of(
          CAxisSystem.of(
            AXIS_POSITIVE_X,
            AXIS_POSITIVE_Y,
            AXIS_NEGATIVE_Z),
          FACE_WINDING_ORDER_COUNTER_CLOCKWISE))
        .setSchemaIdentifier(SMFSchemaIdentifier.of(SMFSchemaName.of(
          "com.io7m.schema"), 1, 0))
        .build();

    final SMFSchema schema =
      SMFSchema.builder()
        .setSchemaIdentifier(header.schemaIdentifier().get())
        .setAllowExtraAttributes(SMF_EXTRA_ATTRIBUTES_DISALLOWED)
        .setRequiredCoordinateSystem(Optional.empty())
        .putOptionalAttributes(
          attr_0.name(),
          SMFSchemaAttribute.builder()
            .setName(attr_0.name())
            .setRequiredComponentCount(4)
            .setRequiredComponentSize(OptionalInt.empty())
            .setRequiredComponentType(Optional.empty())
            .build())
        .build();

    final var r =
      this.create().validate(header, schema);

    Assertions.assertTrue(r.isSucceeded());
  }

  @Test
  public final void testOptionalWrongComponentSize()
  {
    final SMFAttribute attr_0 =
      SMFAttribute.of(
        SMFAttributeName.of("x"),
        ELEMENT_TYPE_FLOATING,
        4,
        32);

    final SMFHeader header =
      SMFHeader.builder()
        .addAttributesInOrder(attr_0)
        .setTriangles(SMFTriangles.builder().build())
        .setCoordinateSystem(SMFCoordinateSystem.of(
          CAxisSystem.of(
            AXIS_POSITIVE_X,
            AXIS_POSITIVE_Y,
            AXIS_NEGATIVE_Z),
          FACE_WINDING_ORDER_COUNTER_CLOCKWISE))
        .setSchemaIdentifier(SMFSchemaIdentifier.of(SMFSchemaName.of(
          "com.io7m.schema"), 1, 0))
        .build();

    final SMFSchema schema =
      SMFSchema.builder()
        .setSchemaIdentifier(header.schemaIdentifier().get())
        .setAllowExtraAttributes(SMF_EXTRA_ATTRIBUTES_DISALLOWED)
        .setRequiredCoordinateSystem(Optional.empty())
        .putOptionalAttributes(
          attr_0.name(),
          SMFSchemaAttribute.builder()
            .setName(attr_0.name())
            .setRequiredComponentCount(OptionalInt.empty())
            .setRequiredComponentSize(16)
            .setRequiredComponentType(Optional.empty())
            .build())
        .build();

    final var r =
      this.create().validate(header, schema);

    Assertions.assertTrue(r.isFailed());

    final List<? extends SMFErrorType> errors = r.errors();
    errors.forEach(e -> LOG.error("{}", e));
    Assertions.assertTrue(errors.stream().anyMatch(e -> e.message().contains(
      "Attribute component size is not the expected size")));
  }

  @Test
  public final void testOptionalCorrectComponentSize()
  {
    final SMFAttribute attr_0 =
      SMFAttribute.of(
        SMFAttributeName.of("x"),
        ELEMENT_TYPE_FLOATING,
        4,
        32);

    final SMFHeader header =
      SMFHeader.builder()
        .addAttributesInOrder(attr_0)
        .setTriangles(SMFTriangles.builder().build())
        .setCoordinateSystem(SMFCoordinateSystem.of(
          CAxisSystem.of(
            AXIS_POSITIVE_X,
            AXIS_POSITIVE_Y,
            AXIS_NEGATIVE_Z),
          FACE_WINDING_ORDER_COUNTER_CLOCKWISE))
        .setSchemaIdentifier(SMFSchemaIdentifier.of(SMFSchemaName.of(
          "com.io7m.schema"), 1, 0))
        .build();

    final SMFSchema schema =
      SMFSchema.builder()
        .setSchemaIdentifier(header.schemaIdentifier().get())
        .setAllowExtraAttributes(SMF_EXTRA_ATTRIBUTES_DISALLOWED)
        .setRequiredCoordinateSystem(Optional.empty())
        .putOptionalAttributes(
          attr_0.name(),
          SMFSchemaAttribute.builder()
            .setName(attr_0.name())
            .setRequiredComponentCount(OptionalInt.empty())
            .setRequiredComponentSize(32)
            .setRequiredComponentType(Optional.empty())
            .build())
        .build();

    final var r =
      this.create().validate(header, schema);

    Assertions.assertTrue(r.isSucceeded());
  }

  @Test
  public final void testOptionalNotPresent()
  {
    final SMFAttribute attr_0 =
      SMFAttribute.of(
        SMFAttributeName.of("x"),
        ELEMENT_TYPE_FLOATING,
        4,
        32);

    final SMFHeader header =
      SMFHeader.builder()
        .setCoordinateSystem(SMFCoordinateSystem.of(
          CAxisSystem.of(
            AXIS_POSITIVE_X,
            AXIS_POSITIVE_Y,
            AXIS_NEGATIVE_Z),
          FACE_WINDING_ORDER_COUNTER_CLOCKWISE))
        .setTriangles(SMFTriangles.builder().build())
        .setSchemaIdentifier(SMFSchemaIdentifier.of(SMFSchemaName.of(
          "com.io7m.schema"), 1, 0))
        .build();

    final SMFSchema schema =
      SMFSchema.builder()
        .setSchemaIdentifier(header.schemaIdentifier().get())
        .setAllowExtraAttributes(SMF_EXTRA_ATTRIBUTES_DISALLOWED)
        .setRequiredCoordinateSystem(Optional.empty())
        .putOptionalAttributes(
          attr_0.name(),
          SMFSchemaAttribute.builder()
            .setName(attr_0.name())
            .setRequiredComponentCount(OptionalInt.empty())
            .setRequiredComponentSize(32)
            .setRequiredComponentType(Optional.empty())
            .build())
        .build();

    final var r =
      this.create().validate(header, schema);

    Assertions.assertTrue(r.isSucceeded());
  }

  @Test
  public final void testWrongSchemaIdentifier()
  {
    final SMFHeader header =
      SMFHeader.builder()
        .setTriangles(SMFTriangles.builder().build())
        .setCoordinateSystem(SMFCoordinateSystem.of(
          CAxisSystem.of(
            AXIS_POSITIVE_X,
            AXIS_POSITIVE_Y,
            AXIS_NEGATIVE_Z),
          FACE_WINDING_ORDER_COUNTER_CLOCKWISE))
        .setSchemaIdentifier(
          SMFSchemaIdentifier.of(
            SMFSchemaName.of("com.io7m.schema"),
            1,
            0))
        .build();

    final SMFSchema schema =
      SMFSchema.builder()
        .setSchemaIdentifier(SMFSchemaIdentifier.of(
          SMFSchemaName.of("com.io7m.schema.different"),
          1,
          0))
        .setAllowExtraAttributes(SMF_EXTRA_ATTRIBUTES_ALLOWED)
        .build();

    final var r =
      this.create().validate(header, schema);

    Assertions.assertTrue(r.isFailed());

    final List<? extends SMFErrorType> errors = r.errors();
    errors.forEach(e -> LOG.error("{}", e));
    Assertions.assertTrue(errors.stream().anyMatch(e -> e.message().contains(
      "The mesh schema identifier does not match the identifier in the schema")));
  }

  @Test
  public final void testRequireTriangles()
  {
    final SMFCoordinateSystem coords_exp =
      SMFCoordinateSystem.of(
        CAxisSystem.of(
          AXIS_POSITIVE_X,
          AXIS_POSITIVE_Y,
          AXIS_POSITIVE_Z),
        FACE_WINDING_ORDER_COUNTER_CLOCKWISE);

    final SMFHeader header =
      SMFHeader.builder()
        .setTriangles(SMFTriangles.builder().build())
        .setCoordinateSystem(coords_exp)
        .setSchemaIdentifier(SMFSchemaIdentifier.of(
          SMFSchemaName.of("com.io7m.schema"), 1, 0))
        .build();

    final SMFSchema schema =
      SMFSchema.builder()
        .setSchemaIdentifier(header.schemaIdentifier().get())
        .setRequireTriangles(SMF_TRIANGLES_REQUIRED)
        .build();

    final var r =
      this.create().validate(header, schema);

    Assertions.assertFalse(r.isSucceeded());
  }

  @Test
  public final void testRequireVertices()
  {
    final SMFCoordinateSystem coords_exp =
      SMFCoordinateSystem.of(
        CAxisSystem.of(
          AXIS_POSITIVE_X,
          AXIS_POSITIVE_Y,
          AXIS_POSITIVE_Z),
        FACE_WINDING_ORDER_COUNTER_CLOCKWISE);

    final SMFHeader header =
      SMFHeader.builder()
        .setTriangles(SMFTriangles.builder().build())
        .setCoordinateSystem(coords_exp)
        .setSchemaIdentifier(SMFSchemaIdentifier.of(
          SMFSchemaName.of("com.io7m.schema"), 1, 0))
        .build();

    final SMFSchema schema =
      SMFSchema.builder()
        .setSchemaIdentifier(header.schemaIdentifier().get())
        .setRequireVertices(SMF_VERTICES_REQUIRED)
        .build();

    final var r =
      this.create().validate(header, schema);

    Assertions.assertFalse(r.isSucceeded());
  }
}
