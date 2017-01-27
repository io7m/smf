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

package com.io7m.smfj.processing.api;

import com.io7m.jaffirm.core.Preconditions;
import com.io7m.jtensors.VectorI3L;
import com.io7m.smfj.core.SMFAttributeName;
import com.io7m.smfj.core.SMFHeader;
import com.io7m.smfj.core.SMFImmutableStyleType;
import javaslang.collection.Map;
import javaslang.collection.Vector;
import org.immutables.value.Value;

/**
 * An immutable in-memory copy of a mesh.
 */

@SMFImmutableStyleType
@Value.Immutable
public interface SMFMemoryMeshType
{
  /**
   * @return The mesh header
   */

  @Value.Parameter
  SMFHeader header();

  /**
   * @return The mesh attribute arrays
   */

  @Value.Parameter
  Map<SMFAttributeName, SMFAttributeArrayType> arrays();

  /**
   * @return The parsed metadata
   */

  @Value.Parameter
  Vector<SMFMetadata> metadata();

  /**
   * @return The triangles
   */

  @Value.Parameter
  Vector<VectorI3L> triangles();

  /**
   * Check preconditions for the type.
   */

  @Value.Check
  default void checkPreconditions()
  {
    {
      final long tri_lsize = (long) this.triangles().size();
      final long tri_hcount = this.header().triangles().triangleCount();
      Preconditions.checkPreconditionL(
        tri_lsize,
        tri_lsize == tri_hcount,
        x -> "Triangle list size must match header count");
    }

    {
      final long meta_lsize = (long) this.metadata().size();
      final long meta_hcount = this.header().metaCount();
      Preconditions.checkPreconditionL(
        meta_lsize,
        meta_lsize == meta_hcount,
        x -> "Metadata list size must match header count");
    }

    {
      this.arrays().forEach(p -> {
        final long array_lsize = (long) p._2.size();
        final long array_hcount = this.header().vertexCount();
        Preconditions.checkPreconditionL(
          array_lsize,
          array_lsize == array_hcount,
          x -> "Attribute array size must match header count");
      });
    }
  }
}
