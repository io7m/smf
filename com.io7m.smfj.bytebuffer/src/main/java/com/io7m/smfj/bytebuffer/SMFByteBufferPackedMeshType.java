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

package com.io7m.smfj.bytebuffer;

import com.io7m.smfj.core.SMFHeader;
import io.vavr.collection.Seq;
import io.vavr.collection.SortedMap;
import io.vavr.collection.TreeMap;
import org.immutables.value.Value;

import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * A mesh that has been packed into a set of byte buffers.
 */

@com.io7m.immutables.styles.ImmutablesStyleType
@org.immutables.vavr.encodings.VavrEncodingEnabled
@Value.Immutable
public interface SMFByteBufferPackedMeshType
{
  /**
   * @return The sets of packed attributes
   */

  @Value.Parameter
  Seq<SMFByteBufferPackedAttributeSet> attributeSets();

  /**
   * @return The packed triangles, if any
   */

  @Value.Parameter
  Optional<SMFByteBufferPackedTriangles> triangles();

  /**
   * @return The parsed header
   */

  @Value.Parameter
  SMFHeader header();

  /**
   * @return The sets of packed attributes, grouped by ID
   */

  @Value.Derived
  default SortedMap<Integer, SMFByteBufferPackedAttributeSet> attributeSetsByID()
  {
    return TreeMap.ofAll(
      this.attributeSets().toJavaStream().collect(
        Collectors.toMap(
          attr -> Integer.valueOf(attr.id()),
          Function.identity(),
          (set0, set1) -> {
            final StringBuilder sb = new StringBuilder(128);
            sb.append("Duplicate packing set ID.");
            sb.append(System.lineSeparator());
            sb.append("  ID: ");
            sb.append(set0.id());
            sb.append(System.lineSeparator());
            throw new IllegalArgumentException(sb.toString());
          })));
  }
}
