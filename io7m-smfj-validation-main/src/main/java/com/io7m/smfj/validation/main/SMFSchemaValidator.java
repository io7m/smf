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

package com.io7m.smfj.validation.main;

import com.io7m.jnull.NullCheck;
import com.io7m.smfj.core.SMFAttribute;
import com.io7m.smfj.core.SMFAttributeName;
import com.io7m.smfj.core.SMFComponentType;
import com.io7m.smfj.core.SMFCoordinateSystem;
import com.io7m.smfj.core.SMFHeader;
import com.io7m.smfj.validation.api.SMFSchema;
import com.io7m.smfj.validation.api.SMFSchemaAttribute;
import com.io7m.smfj.validation.api.SMFSchemaValidationError;
import com.io7m.smfj.validation.api.SMFSchemaValidatorType;
import javaslang.Tuple2;
import javaslang.collection.List;
import javaslang.collection.SortedMap;
import javaslang.control.Validation;

import java.util.Objects;
import java.util.Optional;
import java.util.OptionalInt;

public final class SMFSchemaValidator implements SMFSchemaValidatorType
{
  /**
   * Construct a validator.
   */

  public SMFSchemaValidator()
  {

  }

  @Override
  public Validation<List<SMFSchemaValidationError>, SMFHeader> validate(
    final SMFHeader header,
    final SMFSchema schema)
  {
    NullCheck.notNull(header, "Header");
    NullCheck.notNull(schema, "Schema");

    List<SMFSchemaValidationError> errors = List.empty();

    final SortedMap<SMFAttributeName, SMFSchemaAttribute> required_by_name =
      schema.requiredAttributes();
    final SortedMap<SMFAttributeName, SMFAttribute> by_name =
      header.attributesByName();
    for (final Tuple2<SMFAttributeName, SMFSchemaAttribute> p : required_by_name) {
      final SMFAttributeName name = p._1;
      final SMFSchemaAttribute attr_schema = p._2;

      if (by_name.containsKey(name)) {
        final SMFAttribute attr = by_name.get(name).get();
        errors = checkComponentType(errors, name, attr_schema, attr);
        errors = checkComponentSize(errors, name, attr_schema, attr);
        errors = checkComponentCount(errors, name, attr_schema, attr);
      } else {
        errors = errors.append(errorMissingAttribute(name));
      }
    }

    if (!schema.allowExtraAttributes()) {
      final SortedMap<SMFAttributeName, SMFAttribute> extras =
        by_name.filterKeys(k -> !required_by_name.containsKey(k));
      if (!extras.isEmpty()) {
        errors = errors.appendAll(extras.keySet().toList().map(
          SMFSchemaValidator::errorExtraAttribute));
      }
    }

    final Optional<SMFCoordinateSystem> coords_opt =
      schema.requiredCoordinateSystem();
    if (coords_opt.isPresent()) {
      final SMFCoordinateSystem req_coords = coords_opt.get();
      if (!Objects.equals(req_coords, header.coordinateSystem())) {
        errors = errors.append(errorWrongCoordinateSystem(
          req_coords,
          header.coordinateSystem()));
      }
    }

    if (errors.isEmpty()) {
      return Validation.valid(header);
    }

    return Validation.invalid(errors);
  }

  private static SMFSchemaValidationError errorWrongCoordinateSystem(
    final SMFCoordinateSystem expected,
    final SMFCoordinateSystem received)
  {
    final StringBuilder sb = new StringBuilder(128);
    sb.append("The mesh has an unexpected coordinate system.");
    sb.append(System.lineSeparator());
    sb.append("  Expected: ");
    sb.append(expected.toHumanString());
    sb.append(System.lineSeparator());
    sb.append("  Received: ");
    sb.append(received.toHumanString());
    sb.append(System.lineSeparator());
    return SMFSchemaValidationError.of(sb.toString(), Optional.empty());
  }

  private static SMFSchemaValidationError errorExtraAttribute(
    final SMFAttributeName name)
  {
    final StringBuilder sb = new StringBuilder(128);
    sb.append(
      "The mesh contains an extra attribute but the schema does not permit them.");
    sb.append(System.lineSeparator());
    sb.append("  Attribute: ");
    sb.append(name.value());
    sb.append(System.lineSeparator());
    return SMFSchemaValidationError.of(sb.toString(), Optional.empty());
  }

  private static SMFSchemaValidationError errorMissingAttribute(
    final SMFAttributeName name)
  {
    final StringBuilder sb = new StringBuilder(128);
    sb.append("A required attribute is missing.");
    sb.append(System.lineSeparator());
    sb.append("  Attribute: ");
    sb.append(name.value());
    sb.append(System.lineSeparator());
    return SMFSchemaValidationError.of(sb.toString(), Optional.empty());
  }

  private static List<SMFSchemaValidationError> checkComponentCount(
    final List<SMFSchemaValidationError> errors,
    final SMFAttributeName name,
    final SMFSchemaAttribute attr_schema,
    final SMFAttribute attr)
  {
    final OptionalInt req_count_opt = attr_schema.requiredComponentCount();
    if (req_count_opt.isPresent()) {
      final int req_count = req_count_opt.getAsInt();
      if (attr.componentCount() != req_count) {
        return errors.append(errorWrongComponentCount(
          name,
          req_count,
          attr.componentCount()));
      }
    }
    return errors;
  }

  private static List<SMFSchemaValidationError> checkComponentSize(
    final List<SMFSchemaValidationError> errors,
    final SMFAttributeName name,
    final SMFSchemaAttribute attr_schema,
    final SMFAttribute attr)
  {
    final OptionalInt req_size_opt = attr_schema.requiredComponentSize();
    if (req_size_opt.isPresent()) {
      final int req_size = req_size_opt.getAsInt();
      if (attr.componentSizeBits() != req_size) {
        return errors.append(errorWrongComponentSize(
          name,
          req_size,
          attr.componentSizeBits()));
      }
    }
    return errors;
  }

  private static List<SMFSchemaValidationError> checkComponentType(
    final List<SMFSchemaValidationError> errors,
    final SMFAttributeName name,
    final SMFSchemaAttribute attr_schema,
    final SMFAttribute attr)
  {
    final Optional<SMFComponentType> req_type_opt = attr_schema.requiredType();
    if (req_type_opt.isPresent()) {
      final SMFComponentType req_type = req_type_opt.get();
      if (attr.componentType() != req_type) {
        return errors.append(errorWrongComponentType(
          name,
          req_type,
          attr.componentType()));
      }
    }
    return errors;
  }

  private static SMFSchemaValidationError errorWrongComponentCount(
    final SMFAttributeName name,
    final int expected,
    final int received)
  {
    final StringBuilder sb = new StringBuilder(128);
    sb.append("Attribute component count is not the expected count.");
    sb.append(System.lineSeparator());
    sb.append("  Attribute: ");
    sb.append(name.value());
    sb.append(System.lineSeparator());
    sb.append("  Expected:  ");
    sb.append(expected);
    sb.append(System.lineSeparator());
    sb.append("  Received:  ");
    sb.append(received);
    sb.append(System.lineSeparator());
    return SMFSchemaValidationError.of(sb.toString(), Optional.empty());
  }

  private static SMFSchemaValidationError errorWrongComponentSize(
    final SMFAttributeName name,
    final int expected,
    final int received)
  {
    final StringBuilder sb = new StringBuilder(128);
    sb.append("Attribute component size is not the expected size.");
    sb.append(System.lineSeparator());
    sb.append("  Attribute: ");
    sb.append(name.value());
    sb.append(System.lineSeparator());
    sb.append("  Expected:  ");
    sb.append(expected);
    sb.append(System.lineSeparator());
    sb.append("  Received:  ");
    sb.append(received);
    sb.append(System.lineSeparator());
    return SMFSchemaValidationError.of(sb.toString(), Optional.empty());
  }

  private static SMFSchemaValidationError errorWrongComponentType(
    final SMFAttributeName name,
    final SMFComponentType expected,
    final SMFComponentType received)
  {
    final StringBuilder sb = new StringBuilder(128);
    sb.append("Attribute is not of the expected type.");
    sb.append(System.lineSeparator());
    sb.append("  Attribute: ");
    sb.append(name.value());
    sb.append(System.lineSeparator());
    sb.append("  Expected:  ");
    sb.append(expected.getName());
    sb.append(System.lineSeparator());
    sb.append("  Received:  ");
    sb.append(received.getName());
    sb.append(System.lineSeparator());
    return SMFSchemaValidationError.of(sb.toString(), Optional.empty());
  }
}
