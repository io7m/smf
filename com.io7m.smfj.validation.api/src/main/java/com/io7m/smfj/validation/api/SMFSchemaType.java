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

package com.io7m.smfj.validation.api;

import com.io7m.jaffirm.core.Preconditions;
import com.io7m.smfj.core.SMFAttributeName;
import com.io7m.smfj.core.SMFCoordinateSystem;
import com.io7m.smfj.core.SMFSchemaIdentifier;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import org.immutables.value.Value;

/**
 * The type of schemas.
 */

@com.io7m.immutables.styles.ImmutablesStyleType
@Value.Immutable
public interface SMFSchemaType
{
  /**
   * @return The unique schema identifier
   */

  @Value.Parameter
  SMFSchemaIdentifier schemaIdentifier();

  /**
   * A schema may define required attributes. An attribute is not allowed to be both required and
   * optional at the same time.
   *
   * @return The required attributes
   */

  @Value.Parameter
  Map<SMFAttributeName, SMFSchemaAttribute> requiredAttributes();

  /**
   * A schema may define optional attributes. An attribute is not allowed to be both required and
   * optional at the same time.
   *
   * @return The optional attributes
   */

  @Value.Parameter
  Map<SMFAttributeName, SMFSchemaAttribute> optionalAttributes();

  /**
   * @return The required coordinate system, if any
   */

  @Value.Parameter
  Optional<SMFCoordinateSystem> requiredCoordinateSystem();

  /**
   * @return {@link SMFSchemaAllowExtraAttributes#SMF_EXTRA_ATTRIBUTES_ALLOWED} if the mesh is
   * allowed to contain attributes that are not given in {@link #requiredAttributes()}
   */

  @Value.Parameter
  @Value.Default
  default SMFSchemaAllowExtraAttributes allowExtraAttributes()
  {
    return SMFSchemaAllowExtraAttributes.SMF_EXTRA_ATTRIBUTES_DISALLOWED;
  }

  /**
   * @return {@link SMFSchemaRequireTriangles#SMF_TRIANGLES_REQUIRED} if a non-zero triangle count
   * is required
   */

  @Value.Parameter
  @Value.Default
  default SMFSchemaRequireTriangles requireTriangles()
  {
    return SMFSchemaRequireTriangles.SMF_TRIANGLES_NOT_REQUIRED;
  }

  /**
   * @return {@link SMFSchemaRequireVertices#SMF_VERTICES_REQUIRED} if a non-zero vertex count is
   * required
   */

  @Value.Parameter
  @Value.Default
  default SMFSchemaRequireVertices requireVertices()
  {
    return SMFSchemaRequireVertices.SMF_VERTICES_NOT_REQUIRED;
  }

  /**
   * Check preconditions for the type.
   */

  @Value.Check
  default void checkPreconditions()
  {
    final var required = new HashSet<>(this.requiredAttributes().keySet());
    required.retainAll(this.optionalAttributes().keySet());

    Preconditions.checkPrecondition(
      required,
      required.isEmpty(),
      s -> "The intersection of the required and optional attributes must be empty");
  }
}
