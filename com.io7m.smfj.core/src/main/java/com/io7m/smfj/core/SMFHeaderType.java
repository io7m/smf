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

package com.io7m.smfj.core;

import javaslang.collection.List;
import javaslang.collection.Map;
import javaslang.collection.SortedMap;
import javaslang.collection.TreeMap;
import javaslang.control.Option;
import org.immutables.javaslang.encodings.JavaslangEncodingEnabled;
import org.immutables.value.Value;

import java.util.Objects;
import java.util.Optional;

/**
 * Information about an SMF file.
 */

@Value.Immutable
@JavaslangEncodingEnabled
@SMFImmutableStyleType
public interface SMFHeaderType
{
  /**
   * @return The triangle information for the file
   */

  @Value.Default
  @Value.Parameter
  default SMFTriangles triangles()
  {
    return SMFTriangles.builder().build();
  }

  /**
   * @return The number of vertices in the file
   */

  @Value.Parameter
  @Value.Default
  default long vertexCount()
  {
    return 0L;
  }

  /**
   * @return The schema ID in the file
   */

  @Value.Parameter
  Optional<SMFSchemaIdentifier> schemaIdentifier();

  /**
   * @return The coordinate system of the mesh data
   */

  @Value.Default
  @Value.Parameter
  default SMFCoordinateSystem coordinateSystem()
  {
    return SMFCoordinateSystem.builder().build();
  }

  /**
   * @return The attributes in the order that they appeared in the file
   */

  @Value.Parameter
  List<SMFAttribute> attributesInOrder();

  /**
   * @return The attributes by name
   */

  @Value.Derived
  default SortedMap<SMFAttributeName, SMFAttribute> attributesByName()
  {
    SortedMap<SMFAttributeName, SMFAttribute> m = TreeMap.empty();
    final List<SMFAttribute> ordered = this.attributesInOrder();
    for (int index = 0; index < ordered.size(); ++index) {
      final SMFAttribute attr = ordered.get(index);
      if (m.containsKey(attr.name())) {
        final StringBuilder sb = new StringBuilder(128);
        sb.append("Duplicate attribute name.");
        sb.append(System.lineSeparator());
        sb.append("  Attribute: ");
        sb.append(attr.name().value());
        sb.append(System.lineSeparator());
        throw new IllegalArgumentException(sb.toString());
      }
      m = m.put(attr.name(), attr);
    }
    return m;
  }

  /**
   * Check preconditions for the type.
   */

  @Value.Check
  default void checkPreconditions()
  {
    final int order_size = this.attributesInOrder().size();
    final int named_size = this.attributesByName().size();
    if (order_size != named_size) {
      final StringBuilder sb = new StringBuilder(128);
      sb.append("The number of attributes by order and by name must match.");
      sb.append(System.lineSeparator());
      sb.append("  Ordered size: ");
      sb.append(order_size);
      sb.append(System.lineSeparator());
      sb.append("  Named size:   ");
      sb.append(named_size);
      sb.append(System.lineSeparator());
      throw new IllegalArgumentException(sb.toString());
    }

    final Map<SMFAttributeName, SMFAttribute> named = this.attributesByName();
    for (final SMFAttribute attribute : this.attributesInOrder()) {
      final Option<SMFAttribute> attribute_opt = named.get(attribute.name());
      if (attribute_opt.isDefined()) {
        if (!Objects.equals(attribute, attribute_opt.get())) {
          final StringBuilder sb = new StringBuilder(128);
          sb.append(
            "An attribute that appears in the ordered list does not match that in the named map.");
          sb.append(System.lineSeparator());
          sb.append("  Attribute: ");
          sb.append(attribute.name());
          sb.append(System.lineSeparator());
          throw new IllegalArgumentException(sb.toString());
        }
      } else {
        final StringBuilder sb = new StringBuilder(128);
        sb.append(
          "An attribute that appears in the ordered list does not exist in the named map.");
        sb.append(System.lineSeparator());
        sb.append("  Attribute: ");
        sb.append(attribute.name());
        sb.append(System.lineSeparator());
        throw new IllegalArgumentException(sb.toString());
      }
    }
  }
}
