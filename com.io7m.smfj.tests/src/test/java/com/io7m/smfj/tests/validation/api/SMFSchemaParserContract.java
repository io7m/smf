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
import com.io7m.jnull.NullCheck;
import com.io7m.smfj.core.SMFAttributeName;
import com.io7m.smfj.core.SMFComponentType;
import com.io7m.smfj.core.SMFCoordinateSystem;
import com.io7m.smfj.core.SMFErrorType;
import com.io7m.smfj.core.SMFFaceWindingOrder;
import com.io7m.smfj.core.SMFSchemaIdentifier;
import com.io7m.smfj.core.SMFSchemaName;
import com.io7m.smfj.validation.api.SMFSchema;
import com.io7m.smfj.validation.api.SMFSchemaAttribute;
import com.io7m.smfj.validation.api.SMFSchemaParserType;
import javaslang.collection.List;
import javaslang.collection.TreeMap;
import javaslang.control.Validation;
import org.apache.commons.io.input.BrokenInputStream;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.OptionalInt;

public abstract class SMFSchemaParserContract
{
  private static final Logger LOG;

  static {
    LOG = LoggerFactory.getLogger(SMFSchemaParserContract.class);
  }

  private static void showErrors(
    final Validation<List<SMFErrorType>, SMFSchema> r)
  {
    if (r.isInvalid()) {
      r.getError().forEach(e -> LOG.error("{}", e.fullMessage()));
    }
  }

  protected abstract SMFSchemaParserType create(
    Path path,
    InputStream stream);

  private SMFSchemaParserType resourceParser(
    final String name)
    throws Exception
  {
    final String path = "/com/io7m/smfj/tests/validation/api/" + name;
    final InputStream stream =
      NullCheck.notNull(
        SMFSchemaParserContract.class.getResourceAsStream(path),
        "Stream");
    return this.create(Paths.get(path), stream);
  }

  @Test
  public final void testIOError()
  {
    boolean caught = false;
    try (final SMFSchemaParserType parser = this.create(
      Paths.get("/invalid"),
      new BrokenInputStream())) {
      final Validation<List<SMFErrorType>, SMFSchema> r = parser.parseSchema();
      showErrors(r);
      Assert.assertFalse(r.isValid());
      Assert.assertTrue(r.getError().exists(e -> e.message().contains(
        "I/O error")));
    } catch (final IOException e) {
      LOG.debug("caught: ", e);
      caught = true;
    }
    Assert.assertTrue(caught);
  }

  @Test
  public final void testInvalidUnknown0()
    throws Exception
  {
    try (final SMFSchemaParserType parser = this.resourceParser(
      "invalid-unknown-0.smfs")) {
      final Validation<List<SMFErrorType>, SMFSchema> r = parser.parseSchema();
      showErrors(r);
      Assert.assertFalse(r.isValid());
      Assert.assertTrue(r.getError().exists(e -> e.message().contains(
        "Unrecognized schema statement")));
    }
  }

  @Test
  public final void testInvalidAttribute0()
    throws Exception
  {
    try (final SMFSchemaParserType parser = this.resourceParser(
      "invalid-attribute-0.smfs")) {
      final Validation<List<SMFErrorType>, SMFSchema> r = parser.parseSchema();
      showErrors(r);
      Assert.assertFalse(r.isValid());
      Assert.assertTrue(r.getError().exists(e -> e.message().contains(
        "Incorrect number of arguments")));
    }
  }

  @Test
  public final void testInvalidAttribute1()
    throws Exception
  {
    try (final SMFSchemaParserType parser = this.resourceParser(
      "invalid-attribute-1.smfs")) {
      final Validation<List<SMFErrorType>, SMFSchema> r = parser.parseSchema();
      showErrors(r);
      Assert.assertFalse(r.isValid());
      Assert.assertTrue(r.getError().exists(e -> e.message().contains(
        "Could not parse requirement")));
    }
  }

  @Test
  public final void testInvalidAttribute2()
    throws Exception
  {
    try (final SMFSchemaParserType parser = this.resourceParser(
      "invalid-attribute-2.smfs")) {
      final Validation<List<SMFErrorType>, SMFSchema> r = parser.parseSchema();
      showErrors(r);
      Assert.assertFalse(r.isValid());
      Assert.assertTrue(r.getError().exists(e -> e.message().contains(
        "Could not parse attribute name")));
    }
  }

  @Test
  public final void testInvalidAttribute3()
    throws Exception
  {
    try (final SMFSchemaParserType parser = this.resourceParser(
      "invalid-attribute-3.smfs")) {
      final Validation<List<SMFErrorType>, SMFSchema> r = parser.parseSchema();
      showErrors(r);
      Assert.assertFalse(r.isValid());
      Assert.assertTrue(r.getError().exists(e -> e.message().contains(
        "Unrecognized type")));
    }
  }

  @Test
  public final void testInvalidAttribute4()
    throws Exception
  {
    try (final SMFSchemaParserType parser = this.resourceParser(
      "invalid-attribute-4.smfs")) {
      final Validation<List<SMFErrorType>, SMFSchema> r = parser.parseSchema();
      showErrors(r);
      Assert.assertFalse(r.isValid());
      Assert.assertTrue(r.getError().exists(e -> e.message().contains(
        "Could not parse component count")));
    }
  }

  @Test
  public final void testInvalidAttribute5()
    throws Exception
  {
    try (final SMFSchemaParserType parser = this.resourceParser(
      "invalid-attribute-5.smfs")) {
      final Validation<List<SMFErrorType>, SMFSchema> r = parser.parseSchema();
      showErrors(r);
      Assert.assertFalse(r.isValid());
      Assert.assertTrue(r.getError().exists(e -> e.message().contains(
        "Could not parse component size")));
    }
  }

  @Test
  public final void testInvalidEmpty()
    throws Exception
  {
    try (final SMFSchemaParserType parser = this.resourceParser("empty.smfs")) {
      final Validation<List<SMFErrorType>, SMFSchema> r = parser.parseSchema();
      showErrors(r);
      Assert.assertFalse(r.isValid());
      Assert.assertTrue(r.getError().exists(e -> e.message().contains(
        "Empty file: Must begin with an smf-schema version declaration")));
    }
  }

  @Test
  public final void testInvalidMissingIdentifier()
    throws Exception
  {
    try (final SMFSchemaParserType parser = this.resourceParser(
      "missing-ident.smfs")) {
      final Validation<List<SMFErrorType>, SMFSchema> r = parser.parseSchema();
      showErrors(r);
      Assert.assertFalse(r.isValid());
      Assert.assertTrue(r.getError().exists(e -> e.message().contains(
        "Must specify a schema identifier")));
    }
  }

  @Test
  public final void testInvalidSchema0()
    throws Exception
  {
    try (final SMFSchemaParserType parser = this.resourceParser(
      "invalid-schema-0.smfs")) {
      final Validation<List<SMFErrorType>, SMFSchema> r = parser.parseSchema();
      showErrors(r);
      Assert.assertFalse(r.isValid());
      Assert.assertTrue(r.getError().exists(e -> e.message().contains(
        "Incorrect number of arguments")));
    }
  }

  @Test
  public final void testInvalidVersion0()
    throws Exception
  {
    try (final SMFSchemaParserType parser = this.resourceParser(
      "invalid-version-0.smfs")) {
      final Validation<List<SMFErrorType>, SMFSchema> r = parser.parseSchema();
      showErrors(r);
      Assert.assertFalse(r.isValid());
      Assert.assertTrue(r.getError().exists(e -> e.fullMessage().contains(
        "Unparseable version declaration")));
    }
  }

  @Test
  public final void testInvalidVersion1()
    throws Exception
  {
    try (final SMFSchemaParserType parser = this.resourceParser(
      "invalid-version-1.smfs")) {
      final Validation<List<SMFErrorType>, SMFSchema> r = parser.parseSchema();
      showErrors(r);
      Assert.assertFalse(r.isValid());
      Assert.assertTrue(r.getError().exists(e -> e.fullMessage().contains(
        "Unparseable version declaration")));
    }
  }

  @Test
  public final void testInvalidVersion2()
    throws Exception
  {
    try (final SMFSchemaParserType parser = this.resourceParser(
      "invalid-version-2.smfs")) {
      final Validation<List<SMFErrorType>, SMFSchema> r = parser.parseSchema();
      showErrors(r);
      Assert.assertFalse(r.isValid());
      Assert.assertTrue(r.getError().exists(e -> e.fullMessage().contains(
        "Unparseable version declaration")));
    }
  }

  @Test
  public final void testInvalidSchema1()
    throws Exception
  {
    try (final SMFSchemaParserType parser = this.resourceParser(
      "invalid-schema-1.smfs")) {
      final Validation<List<SMFErrorType>, SMFSchema> r = parser.parseSchema();
      showErrors(r);
      Assert.assertFalse(r.isValid());
      Assert.assertTrue(r.getError().exists(e -> e.fullMessage().contains(
        "NumberFormatException")));
    }
  }

  @Test
  public final void testInvalidBadCoordinateSystem0()
    throws Exception
  {
    try (final SMFSchemaParserType parser = this.resourceParser(
      "invalid-coords-0.smfs")) {
      final Validation<List<SMFErrorType>, SMFSchema> r = parser.parseSchema();
      showErrors(r);
      Assert.assertFalse(r.isValid());
      Assert.assertTrue(r.getError().exists(e -> e.message().contains(
        "Could not parse coordinate system")));
    }
  }

  @Test
  public final void testInvalidBadCoordinateSystem1()
    throws Exception
  {
    try (final SMFSchemaParserType parser = this.resourceParser(
      "invalid-coords-1.smfs")) {
      final Validation<List<SMFErrorType>, SMFSchema> r = parser.parseSchema();
      showErrors(r);
      Assert.assertFalse(r.isValid());
      Assert.assertTrue(r.getError().exists(e -> e.message().contains(
        "Could not parse coordinate system")));
    }
  }

  @Test
  public final void testInvalidBadCoordinateSystem2()
    throws Exception
  {
    try (final SMFSchemaParserType parser = this.resourceParser(
      "invalid-coords-2.smfs")) {
      final Validation<List<SMFErrorType>, SMFSchema> r = parser.parseSchema();
      showErrors(r);
      Assert.assertFalse(r.isValid());
      Assert.assertTrue(r.getError().exists(e -> e.message().contains(
        "Could not parse coordinate system")));
    }
  }

  @Test
  public final void testInvalidBadCoordinateSystem3()
    throws Exception
  {
    try (final SMFSchemaParserType parser = this.resourceParser(
      "invalid-coords-3.smfs")) {
      final Validation<List<SMFErrorType>, SMFSchema> r = parser.parseSchema();
      showErrors(r);
      Assert.assertFalse(r.isValid());
      Assert.assertTrue(r.getError().exists(e -> e.message().contains(
        "Could not parse coordinate system")));
    }
  }

  @Test
  public final void testValidMinimal()
    throws Exception
  {
    try (final SMFSchemaParserType parser = this.resourceParser(
      "valid-minimal.smfs")) {
      final Validation<List<SMFErrorType>, SMFSchema> r = parser.parseSchema();
      showErrors(r);
      Assert.assertTrue(r.isValid());
      final SMFSchema schema = r.get();
      Assert.assertEquals(
        SMFSchemaIdentifier.of(
          SMFSchemaName.of("com.io7m.smf.example"), 1, 2),
        schema.schemaIdentifier());
      Assert.assertEquals(Optional.empty(), schema.requiredCoordinateSystem());
      Assert.assertEquals(TreeMap.empty(), schema.requiredAttributes());
    }
  }

  @Test
  public final void testValidAttribute0()
    throws Exception
  {
    try (final SMFSchemaParserType parser = this.resourceParser(
      "valid-attribute-0.smfs")) {
      final Validation<List<SMFErrorType>, SMFSchema> r = parser.parseSchema();
      showErrors(r);
      Assert.assertTrue(r.isValid());
      final SMFSchema schema = r.get();

      final SMFAttributeName a_name = SMFAttributeName.of("x");
      Assert.assertEquals(1L, schema.requiredAttributes().size());
      final SMFSchemaAttribute a = schema.requiredAttributes().get(a_name).get();
      Assert.assertEquals(a_name, a.name());
      Assert.assertEquals(
        SMFComponentType.ELEMENT_TYPE_FLOATING,
        a.requiredComponentType().get());
      Assert.assertEquals(4L, (long) a.requiredComponentCount().getAsInt());
      Assert.assertEquals(32L, (long) a.requiredComponentSize().getAsInt());
    }
  }

  @Test
  public final void testValidAttribute1()
    throws Exception
  {
    try (final SMFSchemaParserType parser = this.resourceParser(
      "valid-attribute-1.smfs")) {
      final Validation<List<SMFErrorType>, SMFSchema> r = parser.parseSchema();
      showErrors(r);
      Assert.assertTrue(r.isValid());
      final SMFSchema schema = r.get();

      final SMFAttributeName a_name = SMFAttributeName.of("x");
      Assert.assertEquals(1L, schema.requiredAttributes().size());
      final SMFSchemaAttribute a = schema.requiredAttributes().get(a_name).get();
      Assert.assertEquals(a_name, a.name());
      Assert.assertEquals(Optional.empty(), a.requiredComponentType());
      Assert.assertEquals(OptionalInt.empty(), a.requiredComponentCount());
      Assert.assertEquals(OptionalInt.empty(), a.requiredComponentSize());
    }
  }

  @Test
  public final void testValidAttribute2()
    throws Exception
  {
    try (final SMFSchemaParserType parser = this.resourceParser(
      "valid-attribute-2.smfs")) {
      final Validation<List<SMFErrorType>, SMFSchema> r = parser.parseSchema();
      showErrors(r);
      Assert.assertTrue(r.isValid());
      final SMFSchema schema = r.get();

      final SMFAttributeName a_name = SMFAttributeName.of("x");
      Assert.assertEquals(1L, schema.optionalAttributes().size());
      final SMFSchemaAttribute a = schema.optionalAttributes().get(a_name).get();
      Assert.assertEquals(a_name, a.name());
      Assert.assertEquals(
        SMFComponentType.ELEMENT_TYPE_FLOATING,
        a.requiredComponentType().get());
      Assert.assertEquals(4L, (long) a.requiredComponentCount().getAsInt());
      Assert.assertEquals(32L, (long) a.requiredComponentSize().getAsInt());
    }
  }

  @Test
  public final void testValidCoords0()
    throws Exception
  {
    try (final SMFSchemaParserType parser = this.resourceParser(
      "valid-coords-0.smfs")) {
      final Validation<List<SMFErrorType>, SMFSchema> r = parser.parseSchema();
      showErrors(r);
      Assert.assertTrue(r.isValid());
      final SMFSchema schema = r.get();
      Assert.assertEquals(
        SMFSchemaIdentifier.of(
          SMFSchemaName.of("com.io7m.smf.example"), 1, 2),
        schema.schemaIdentifier());
      Assert.assertEquals(
        Optional.of(SMFCoordinateSystem.of(
          CAxisSystem.of(
            CAxis.AXIS_POSITIVE_X,
            CAxis.AXIS_POSITIVE_Y,
            CAxis.AXIS_NEGATIVE_Z),
          SMFFaceWindingOrder.FACE_WINDING_ORDER_COUNTER_CLOCKWISE)),
        schema.requiredCoordinateSystem());
      Assert.assertEquals(TreeMap.empty(), schema.requiredAttributes());
    }
  }
}
