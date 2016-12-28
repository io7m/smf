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
import javaslang.collection.Seq;
import javaslang.control.Validation;

/**
 * A filter that renames a mesh attribute.
 */

public final class SMFMemoryMeshFilterAttributeRename implements
  SMFMemoryMeshFilterType
{
  private final SMFAttributeName source;
  private final SMFAttributeName target;

  private SMFMemoryMeshFilterAttributeRename(
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
    return new SMFMemoryMeshFilterAttributeRename(in_source, in_target);
  }

  private SMFProcessingError nonexistentAttribute(
    final Seq<SMFAttribute> ordered)
  {
    final StringBuilder sb = new StringBuilder(128);
    sb.append("Mesh does not contain the given attribute.");
    sb.append(System.lineSeparator());
    sb.append("  Attribute: ");
    sb.append(this.source.value());
    sb.append(System.lineSeparator());
    sb.append("  Existing:  ");
    sb.append(System.lineSeparator());

    for (int index = 0; index < ordered.size(); ++index) {
      final SMFAttribute attr = ordered.get(index);
      sb.append("    [");
      sb.append(index);
      sb.append("] ");
      sb.append(attr.name().value());
      sb.append(System.lineSeparator());
    }

    return SMFProcessingError.of(sb.toString());
  }

  private SMFProcessingError collidingAttribute(
    final Seq<SMFAttribute> ordered)
  {
    final StringBuilder sb = new StringBuilder(128);
    sb.append("Mesh already contains the target attribute.");
    sb.append(System.lineSeparator());
    sb.append("  Attribute: ");
    sb.append(this.target.value());
    sb.append(System.lineSeparator());
    sb.append("  Existing:  ");
    sb.append(System.lineSeparator());

    for (int index = 0; index < ordered.size(); ++index) {
      final SMFAttribute attr = ordered.get(index);
      sb.append("    [");
      sb.append(index);
      sb.append("] ");
      sb.append(attr.name().value());
      sb.append(System.lineSeparator());
    }

    return SMFProcessingError.of(sb.toString());
  }

  @Override
  public Validation<List<SMFProcessingError>, SMFMemoryMesh> filter(
    final SMFMemoryMesh m)
  {
    NullCheck.notNull(m, "Mesh");

    final Map<SMFAttributeName, SMFAttributeArrayType> arrays = m.arrays();
    if (!arrays.containsKey(this.source)) {
      return Validation.invalid(List.of(
        this.nonexistentAttribute(m.header().attributesInOrder())));
    }
    if (arrays.containsKey(this.target)) {
      return Validation.invalid(List.of(
        this.collidingAttribute(m.header().attributesInOrder())));
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
