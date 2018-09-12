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

import com.io7m.jaffirm.core.Preconditions;
import com.io7m.smfj.core.SMFAttribute;
import com.io7m.smfj.core.SMFAttributeName;
import io.vavr.Tuple;
import io.vavr.collection.HashMap;
import io.vavr.collection.Seq;
import io.vavr.collection.SortedMap;
import io.vavr.collection.TreeMap;
import org.immutables.value.Value;

/**
 * The byte buffer packing configuration.
 */

@com.io7m.immutables.styles.ImmutablesStyleType
@org.immutables.vavr.encodings.VavrEncodingEnabled
@Value.Immutable
public interface SMFByteBufferPackingConfigurationType
{
  /**
   * A specification of the sequence of attributes that will be packed into the
   * byte buffer. For each vertex in turn, the value of each attribute will be
   * packed (interleaved) into the buffer in the order given by this sequence.
   *
   * @return The sequence of attributes that will be packed into the byte buffer
   */

  @Value.Parameter
  Seq<SMFAttribute> attributesOrdered();

  /**
   * @return The packed attributes by octet offset
   */

  @Value.Derived
  default SortedMap<Integer, SMFByteBufferPackedAttribute> packedAttributesByOffset()
  {
    TreeMap<Integer, SMFByteBufferPackedAttribute> output = TreeMap.empty();
    final Seq<SMFAttribute> ordered = this.attributesOrdered();
    int offset = 0;
    for (int index = 0; index < ordered.size(); ++index) {
      final SMFAttribute attr = ordered.get(index);
      final SMFByteBufferPackedAttribute packed =
        SMFByteBufferPackedAttribute.of(attr, index, offset);
      output = output.put(Integer.valueOf(offset), packed);
      offset = Math.addExact(offset, attr.sizeOctets());
    }

    return output;
  }

  /**
   * @return The packed attributes in declaration order
   */

  @Value.Derived
  default Seq<SMFByteBufferPackedAttribute> packedAttributesByOrder()
  {
    return this.packedAttributesByOffset().values();
  }

  /**
   * @return The packed attributes by name
   */

  @Value.Derived
  default SortedMap<SMFAttributeName, SMFByteBufferPackedAttribute> packedAttributesByName()
  {
    return this.packedAttributesByOffset()
      .map((offset, attr) -> Tuple.of(attr.attribute().name(), attr));
  }

  /**
   * Determine the offset in octets of the attribute with order {@code index}
   * at vertex {@code vertex}.
   *
   * @param index  The attribute index
   * @param vertex The vertex index
   *
   * @return The offset in octets
   */

  default long offsetOctetsForIndex(
    final int index,
    final long vertex)
  {
    final Seq<SMFByteBufferPackedAttribute> packed =
      this.packedAttributesByOrder();

    Preconditions.checkPreconditionI(
      index,
      index < packed.size(),
      i -> "Index must point to valid attribute");

    final long base = Math.multiplyExact(
      (long) this.vertexSizeOctets(),
      vertex);
    return Math.addExact(base, (long) packed.get(index).offsetOctets());
  }

  /**
   * @return The size of one vertex in octets
   */

  @Value.Derived
  default int vertexSizeOctets()
  {
    int size = 0;
    for (final SMFAttribute a : this.attributesOrdered()) {
      size = Math.addExact(size, a.sizeOctets());
    }
    return size;
  }

  /**
   * Check preconditions for the type.
   */

  @Value.Check
  default void checkPreconditions()
  {
    HashMap<Integer, SMFByteBufferPackedAttribute> m = HashMap.empty();
    final SortedMap<Integer, SMFByteBufferPackedAttribute> by_offset =
      this.packedAttributesByOffset();
    for (final Integer offset : by_offset.keySet()) {
      Preconditions.checkPreconditionI(
        offset.intValue(),
        !m.containsKey(offset),
        i -> "All attribute offsets must be unique");
      m = m.put(offset, by_offset.get(offset).get());
    }
  }
}
