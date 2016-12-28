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
import javaslang.collection.List;
import javaslang.collection.Map;
import javaslang.control.Validation;

/**
 * A filter that removes a mesh attribute.
 */

public final class SMFMemoryMeshAttributeRemove implements
  SMFMemoryMeshFilterType
{
  private final SMFAttributeName source;

  private SMFMemoryMeshAttributeRemove(
    final SMFAttributeName in_source)
  {
    this.source = NullCheck.notNull(in_source, "Source");
  }

  /**
   * Create a new filter.
   *
   * @param in_source The source attribute name
   *
   * @return A new filter
   */

  public static SMFMemoryMeshFilterType create(
    final SMFAttributeName in_source)
  {
    return new SMFMemoryMeshAttributeRemove(in_source);
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

    /*
     * Remove array.
     */

    final Map<SMFAttributeName, SMFAttributeArrayType> removed_arrays =
      arrays.remove(this.source);

    /*
     * Remove attribute.
     */

    final List<SMFAttribute> orig_ordered =
      m.header().attributesInOrder();
    final Map<SMFAttributeName, SMFAttribute> orig_by_name =
      m.header().attributesByName();
    final SMFAttribute orig_attrib =
      orig_by_name.get(this.source).get();

    final List<SMFAttribute> new_ordered =
      orig_ordered.remove(orig_attrib);
    final Map<SMFAttributeName, SMFAttribute> new_by_name =
      orig_by_name.remove(this.source);

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
        .setArrays(removed_arrays)
        .build());
  }
}
