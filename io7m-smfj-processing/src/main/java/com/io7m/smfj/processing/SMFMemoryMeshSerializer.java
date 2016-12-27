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

import com.io7m.jfunctional.Unit;
import com.io7m.jnull.NullCheck;
import com.io7m.jtensors.VectorI2D;
import com.io7m.jtensors.VectorI2L;
import com.io7m.jtensors.VectorI3D;
import com.io7m.jtensors.VectorI3L;
import com.io7m.jtensors.VectorI4D;
import com.io7m.jtensors.VectorI4L;
import com.io7m.junreachable.UnreachableCodeException;
import com.io7m.smfj.core.SMFAttribute;
import com.io7m.smfj.core.SMFAttributeName;
import com.io7m.smfj.core.SMFHeader;
import com.io7m.smfj.serializer.api.SMFSerializerType;

import java.io.IOException;

/**
 * A memory mesh serializer.
 */

public final class SMFMemoryMeshSerializer
{
  private SMFMemoryMeshSerializer()
  {
    throw new UnreachableCodeException();
  }

  /**
   * Serialize the given mesh to the given serializer.
   *
   * @param mesh A mesh
   * @param s    A serializer
   *
   * @throws IOException On I/O errors
   */

  public static void serialize(
    final SMFMemoryMesh mesh,
    final SMFSerializerType s)
    throws IOException
  {
    NullCheck.notNull(mesh, "Mesh");
    NullCheck.notNull(s, "Serial");

    final SMFHeader header = mesh.header();
    s.serializeHeader(header);

    for (final SMFAttribute attribute : header.attributesInOrder()) {
      final SMFAttributeName name = attribute.name();
      final SMFAttributeArrayType array = mesh.arrays().get(name).get();
      s.serializeData(name);
      array.matchArray(
        Unit.unit(),
        (x, y) -> serializeFloat4(s, y),
        (x, y) -> serializeFloat3(s, y),
        (x, y) -> serializeFloat2(s, y),
        (x, y) -> serializeFloat1(s, y),
        (x, y) -> serializeUnsigned4(s, y),
        (x, y) -> serializeUnsigned3(s, y),
        (x, y) -> serializeUnsigned2(s, y),
        (x, y) -> serializeUnsigned1(s, y),
        (x, y) -> serializeSigned4(s, y),
        (x, y) -> serializeSigned3(s, y),
        (x, y) -> serializeSigned2(s, y),
        (x, y) -> serializeSigned1(s, y));
    }

    for (final VectorI3L triangle : mesh.triangles()) {
      s.serializeTriangle(triangle.getXL(), triangle.getYL(), triangle.getZL());
    }

    for (final SMFMetadata meta : mesh.metadata()) {
      s.serializeMetadata(meta.vendor(), meta.schema(), meta.data());
    }
  }

  private static Unit serializeSigned1(
    final SMFSerializerType s,
    final SMFAttributeArrayIntegerSigned1Type y)
    throws IOException
  {
    for (final Long v : y.values()) {
      s.serializeValueIntegerSigned1(v.longValue());
    }
    return Unit.unit();
  }

  private static Unit serializeSigned2(
    final SMFSerializerType s,
    final SMFAttributeArrayIntegerSigned2Type y)
    throws IOException
  {
    for (final VectorI2L v : y.values()) {
      s.serializeValueIntegerSigned2(v.getXL(), v.getYL());
    }
    return Unit.unit();
  }

  private static Unit serializeSigned3(
    final SMFSerializerType s,
    final SMFAttributeArrayIntegerSigned3Type y)
    throws IOException
  {
    for (final VectorI3L v : y.values()) {
      s.serializeValueIntegerSigned3(v.getXL(), v.getYL(), v.getZL());
    }
    return Unit.unit();
  }

  private static Unit serializeSigned4(
    final SMFSerializerType s,
    final SMFAttributeArrayIntegerSigned4Type y)
    throws IOException
  {
    for (final VectorI4L v : y.values()) {
      s.serializeValueIntegerSigned4(
        v.getXL(),
        v.getYL(),
        v.getZL(),
        v.getWL());
    }
    return Unit.unit();
  }

  private static Unit serializeUnsigned1(
    final SMFSerializerType s,
    final SMFAttributeArrayIntegerUnsigned1Type y)
    throws IOException
  {
    for (final Long v : y.values()) {
      s.serializeValueIntegerUnsigned1(v.longValue());
    }
    return Unit.unit();
  }

  private static Unit serializeUnsigned2(
    final SMFSerializerType s,
    final SMFAttributeArrayIntegerUnsigned2Type y)
    throws IOException
  {
    for (final VectorI2L v : y.values()) {
      s.serializeValueIntegerUnsigned2(v.getXL(), v.getYL());
    }
    return Unit.unit();
  }

  private static Unit serializeUnsigned3(
    final SMFSerializerType s,
    final SMFAttributeArrayIntegerUnsigned3Type y)
    throws IOException
  {
    for (final VectorI3L v : y.values()) {
      s.serializeValueIntegerUnsigned3(v.getXL(), v.getYL(), v.getZL());
    }
    return Unit.unit();
  }

  private static Unit serializeUnsigned4(
    final SMFSerializerType s,
    final SMFAttributeArrayIntegerUnsigned4Type y)
    throws IOException
  {
    for (final VectorI4L v : y.values()) {
      s.serializeValueIntegerUnsigned4(
        v.getXL(),
        v.getYL(),
        v.getZL(),
        v.getWL());
    }
    return Unit.unit();
  }

  private static Unit serializeFloat1(
    final SMFSerializerType s,
    final SMFAttributeArrayFloating1Type y)
    throws IOException
  {
    for (final Double v : y.values()) {
      s.serializeValueFloat1(v.doubleValue());
    }
    return Unit.unit();
  }

  private static Unit serializeFloat2(
    final SMFSerializerType s,
    final SMFAttributeArrayFloating2Type y)
    throws IOException
  {
    for (final VectorI2D v : y.values()) {
      s.serializeValueFloat2(v.getXD(), v.getYD());
    }
    return Unit.unit();
  }

  private static Unit serializeFloat3(
    final SMFSerializerType s,
    final SMFAttributeArrayFloating3Type y)
    throws IOException
  {
    for (final VectorI3D v : y.values()) {
      s.serializeValueFloat3(v.getXD(), v.getYD(), v.getZD());
    }
    return Unit.unit();
  }

  private static Unit serializeFloat4(
    final SMFSerializerType s,
    final SMFAttributeArrayFloating4Type y)
    throws IOException
  {
    for (final VectorI4D v : y.values()) {
      s.serializeValueFloat4(v.getXD(), v.getYD(), v.getZD(), v.getWD());
    }
    return Unit.unit();
  }
}
