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

package com.io7m.smfj.processing;

import com.io7m.jnull.NullCheck;
import com.io7m.smfj.core.SMFAttribute;
import com.io7m.smfj.core.SMFAttributeName;
import com.io7m.smfj.core.SMFHeader;
import javaslang.Tuple;
import javaslang.collection.List;
import javaslang.collection.Map;
import javaslang.control.Validation;

/**
 * A filter that renames a mesh attribute.
 */

public final class SMFMemoryMeshAttributeRename implements
  SMFMemoryMeshFilterType
{
  private final SMFAttributeName source;
  private final SMFAttributeName target;

  private SMFMemoryMeshAttributeRename(
    final SMFAttributeName in_source,
    final SMFAttributeName in_target)
  {
    this.source = NullCheck.notNull(in_source, "Source");
    this.target = NullCheck.notNull(in_target, "Target");
  }

  /**
   * Create a new filter.
   *
   * @param in_source The source attribute name
   * @param in_target The target attribute name
   *
   * @return A new filter
   */

  public static SMFMemoryMeshFilterType create(
    final SMFAttributeName in_source,
    final SMFAttributeName in_target)
  {
    return new SMFMemoryMeshAttributeRename(in_source, in_target);
  }

  private static SMFProcessingError error(
    final String format,
    final Object... params)
  {
    return SMFProcessingError.of(String.format(format, params));
  }

  @Override
  public Validation<List<SMFProcessingError>, SMFMemoryMesh> filter(
    final SMFMemoryMesh m)
  {
    NullCheck.notNull(m, "Mesh");

    final Map<SMFAttributeName, SMFAttributeArrayType> arrays = m.arrays();
    if (!arrays.containsKey(this.source)) {
      return Validation.invalid(
        List.of(error(
          "Mesh does not contain an attribute named \"%s\"",
          this.source.value())));
    }
    if (arrays.containsKey(this.target)) {
      return Validation.invalid(
        List.of(error(
          "Mesh already contains an attribute named \"%s\"",
          this.target.value())));
    }

    /*
     * Rename array.
     */

    final SMFAttributeArrayType array = arrays.get(this.source).get();
    final Map<SMFAttributeName, SMFAttributeArrayType> renamed_arrays =
      arrays.replace(
        Tuple.of(this.source, array),
        Tuple.of(this.target, array));

    /*
     * Rename attribute.
     */

    final List<SMFAttribute> orig_ordered =
      m.header().attributesInOrder();
    final Map<SMFAttributeName, SMFAttribute> orig_by_name =
      m.header().attributesByName();
    final SMFAttribute orig_attrib =
      orig_by_name.get(this.source).get();

    final SMFAttribute new_attrib =
      orig_attrib.withName(this.target);

    final List<SMFAttribute> new_ordered =
      orig_ordered.replace(orig_attrib, new_attrib);
    final Map<SMFAttributeName, SMFAttribute> new_by_name =
      orig_by_name.replace(
        Tuple.of(this.source, orig_attrib),
        Tuple.of(this.target, new_attrib));

    final SMFHeader new_header =
      SMFHeader.builder()
        .from(m.header())
        .setAttributesInOrder(new_ordered)
        .setAttributesByName(new_by_name)
        .build();

    return Validation.valid(
      SMFMemoryMesh.builder()
        .from(m)
        .setHeader(new_header)
        .setArrays(renamed_arrays)
        .build());
  }
}
