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

import java.util.Objects;
import com.io7m.jtensors.core.unparameterized.vectors.Vector2D;
import com.io7m.jtensors.core.unparameterized.vectors.Vector2L;
import com.io7m.jtensors.core.unparameterized.vectors.Vector3D;
import com.io7m.jtensors.core.unparameterized.vectors.Vector3L;
import com.io7m.jtensors.core.unparameterized.vectors.Vector4D;
import com.io7m.jtensors.core.unparameterized.vectors.Vector4L;
import com.io7m.junreachable.UnreachableCodeException;
import com.io7m.smfj.core.SMFFormatVersion;
import com.io7m.smfj.parser.api.SMFParserEventsBodyType;
import com.io7m.smfj.parser.api.SMFParserEventsDataAttributeValuesType;
import com.io7m.smfj.parser.api.SMFParserEventsDataAttributesNonInterleavedType;
import com.io7m.smfj.parser.api.SMFParserEventsDataMetaType;
import com.io7m.smfj.parser.api.SMFParserEventsDataTrianglesType;
import com.io7m.smfj.parser.api.SMFParserEventsHeaderType;
import com.io7m.smfj.parser.api.SMFParserEventsType;
import com.io7m.smfj.parser.api.SMFParserRandomAccessType;
import com.io7m.smfj.parser.api.SMFParserSequentialType;
import javaslang.collection.Vector;

import java.io.IOException;
import java.util.Optional;

/**
 * A parser that turns a memory mesh into a
 */

public final class SMFMemoryMeshParser
{
  private SMFMemoryMeshParser()
  {
    throw new UnreachableCodeException();
  }

  /**
   * Create a new random access parser from the given mesh.
   *
   * @param mesh   The source mesh
   * @param events The event receiver
   *
   * @return A parser
   */

  public static SMFParserRandomAccessType createRandomAccess(
    final SMFMemoryMesh mesh,
    final SMFParserEventsType events)
  {
    Objects.requireNonNull(mesh, "mesh");
    Objects.requireNonNull(events, "events");
    throw new UnreachableCodeException();
  }

  /**
   * Create a new sequential access parser from the given mesh.
   *
   * @param mesh   The source mesh
   * @param events The event receiver
   *
   * @return A parser
   */

  public static SMFParserSequentialType createSequential(
    final SMFMemoryMesh mesh,
    final SMFParserEventsType events)
  {
    Objects.requireNonNull(mesh, "mesh");
    Objects.requireNonNull(events, "events");
    return new Sequential(mesh, events);
  }

  private static final class Sequential implements SMFParserSequentialType
  {
    private final SMFMemoryMesh mesh;
    private final SMFParserEventsType events;

    Sequential(
      final SMFMemoryMesh in_mesh,
      final SMFParserEventsType in_events)
    {
      this.mesh = Objects.requireNonNull(in_mesh, "Mesh");
      this.events = Objects.requireNonNull(in_events, "Events");
    }

    @Override
    public void parse()
    {
      this.events.onStart();

      try {
        final Optional<SMFParserEventsHeaderType> r_opt =
          this.events.onVersionReceived(SMFFormatVersion.of(1, 0));

        if (r_opt.isPresent()) {
          final SMFParserEventsHeaderType r = r_opt.get();
          this.parseBody(r.onHeaderParsed(this.mesh.header()));
        }

      } finally {
        this.events.onFinish();
      }
    }

    private void parseBody(
      final Optional<SMFParserEventsBodyType> b_opt)
    {
      if (b_opt.isPresent()) {
        final SMFParserEventsBodyType b = b_opt.get();
        this.parseDataNonInterleaved(b);
        this.parseDataTriangles(b);
        this.parseDataMeta(b);
      }
    }

    private void parseDataMeta(
      final SMFParserEventsBodyType b)
    {
      final Vector<SMFMetadata> metas = this.mesh.metadata();
      for (int index = 0; index < metas.size(); ++index) {
        final SMFMetadata meta = metas.get(index);
        final Optional<SMFParserEventsDataMetaType> m_opt =
          b.onMeta(meta.schema());
        if (m_opt.isPresent()) {
          final SMFParserEventsDataMetaType m = m_opt.get();
          m.onMetaData(meta.schema(), meta.data());
        }
      }
    }

    private void parseDataTriangles(
      final SMFParserEventsBodyType b)
    {
      final Optional<SMFParserEventsDataTrianglesType> t_opt = b.onTriangles();
      if (t_opt.isPresent()) {
        final SMFParserEventsDataTrianglesType t = t_opt.get();
        try {
          final Vector<Vector3L> triangles = this.mesh.triangles();
          for (int index = 0; index < triangles.size(); ++index) {
            final Vector3L tri = triangles.get(index);
            t.onDataTriangle(tri.x(), tri.y(), tri.z());
          }
        } finally {
          t.onDataTrianglesFinish();
        }
      }
    }

    private void parseDataNonInterleaved(
      final SMFParserEventsBodyType b)
    {
      final Optional<SMFParserEventsDataAttributesNonInterleavedType> ni_opt =
        b.onAttributesNonInterleaved();

      if (ni_opt.isPresent()) {
        final SMFParserEventsDataAttributesNonInterleavedType ni = ni_opt.get();
        try {
          this.mesh.header().attributesInOrder().forEach(a -> {
            final Optional<SMFParserEventsDataAttributeValuesType> av_opt =
              ni.onDataAttributeStart(a);

            if (av_opt.isPresent()) {
              final SMFParserEventsDataAttributeValuesType av = av_opt.get();
              try {
                final SMFAttributeArrayType array =
                  this.mesh.arrays().get(a.name()).get();
                array.matchArray(
                  av,
                  Sequential::sendArray4D,
                  Sequential::sendArray3D,
                  Sequential::sendArray2D,
                  Sequential::sendArray1D,
                  Sequential::sendArray4UL,
                  Sequential::sendArray3UL,
                  Sequential::sendArray2UL,
                  Sequential::sendArray1UL,
                  Sequential::sendArray4L,
                  Sequential::sendArray3L,
                  Sequential::sendArray2L,
                  Sequential::sendArray1L);
              } finally {
                av.onDataAttributeValueFinish();
              }
            }
          });
        } finally {
          ni.onDataAttributesNonInterleavedFinish();
        }
      }
    }

    private static Boolean sendArray4D(
      final SMFParserEventsDataAttributeValuesType events,
      final SMFAttributeArrayFloating4Type array_4d)
    {
      final Vector<Vector4D> vv = array_4d.values();
      for (int index = 0; index < array_4d.size(); ++index) {
        final Vector4D v = vv.get(index);
        events.onDataAttributeValueFloat4(v.x(), v.y(), v.z(), v.w());
      }
      return Boolean.TRUE;
    }

    private static Boolean sendArray3D(
      final SMFParserEventsDataAttributeValuesType events,
      final SMFAttributeArrayFloating3Type array_3d)
    {
      final Vector<Vector3D> vv = array_3d.values();
      for (int index = 0; index < array_3d.size(); ++index) {
        final Vector3D v = vv.get(index);
        events.onDataAttributeValueFloat3(v.x(), v.y(), v.z());
      }
      return Boolean.TRUE;
    }

    private static Boolean sendArray2D(
      final SMFParserEventsDataAttributeValuesType events,
      final SMFAttributeArrayFloating2Type array_2d)
    {
      final Vector<Vector2D> vv = array_2d.values();
      for (int index = 0; index < array_2d.size(); ++index) {
        final Vector2D v = vv.get(index);
        events.onDataAttributeValueFloat2(v.x(), v.y());
      }
      return Boolean.TRUE;
    }

    private static Boolean sendArray1D(
      final SMFParserEventsDataAttributeValuesType events,
      final SMFAttributeArrayFloating1Type array_1d)
    {
      final Vector<Double> vv = array_1d.values();
      for (int index = 0; index < array_1d.size(); ++index) {
        final Double v = vv.get(index);
        events.onDataAttributeValueFloat1(v.doubleValue());
      }
      return Boolean.TRUE;
    }

    private static Boolean sendArray4L(
      final SMFParserEventsDataAttributeValuesType events,
      final SMFAttributeArrayIntegerSigned4Type array_4d)
    {
      final Vector<Vector4L> vv = array_4d.values();
      for (int index = 0; index < array_4d.size(); ++index) {
        final Vector4L v = vv.get(index);
        events.onDataAttributeValueIntegerSigned4(v.x(), v.y(), v.z(), v.w());
      }
      return Boolean.TRUE;
    }

    private static Boolean sendArray3L(
      final SMFParserEventsDataAttributeValuesType events,
      final SMFAttributeArrayIntegerSigned3Type array_3d)
    {
      final Vector<Vector3L> vv = array_3d.values();
      for (int index = 0; index < array_3d.size(); ++index) {
        final Vector3L v = vv.get(index);
        events.onDataAttributeValueIntegerSigned3(v.x(), v.y(), v.z());
      }
      return Boolean.TRUE;
    }

    private static Boolean sendArray2L(
      final SMFParserEventsDataAttributeValuesType events,
      final SMFAttributeArrayIntegerSigned2Type array_2d)
    {
      final Vector<Vector2L> vv = array_2d.values();
      for (int index = 0; index < array_2d.size(); ++index) {
        final Vector2L v = vv.get(index);
        events.onDataAttributeValueIntegerSigned2(v.x(), v.y());
      }
      return Boolean.TRUE;
    }

    private static Boolean sendArray1L(
      final SMFParserEventsDataAttributeValuesType events,
      final SMFAttributeArrayIntegerSigned1Type array_1d)
    {
      final Vector<Long> vv = array_1d.values();
      for (int index = 0; index < array_1d.size(); ++index) {
        final Long v = vv.get(index);
        events.onDataAttributeValueIntegerSigned1(v.longValue());
      }
      return Boolean.TRUE;
    }

    private static Boolean sendArray4UL(
      final SMFParserEventsDataAttributeValuesType events,
      final SMFAttributeArrayIntegerUnsigned4Type array_4d)
    {
      final Vector<Vector4L> vv = array_4d.values();
      for (int index = 0; index < array_4d.size(); ++index) {
        final Vector4L v = vv.get(index);
        events.onDataAttributeValueIntegerUnsigned4(v.x(), v.y(), v.z(), v.w());
      }
      return Boolean.TRUE;
    }

    private static Boolean sendArray3UL(
      final SMFParserEventsDataAttributeValuesType events,
      final SMFAttributeArrayIntegerUnsigned3Type array_3d)
    {
      final Vector<Vector3L> vv = array_3d.values();
      for (int index = 0; index < array_3d.size(); ++index) {
        final Vector3L v = vv.get(index);
        events.onDataAttributeValueIntegerUnsigned3(v.x(), v.y(), v.z());
      }
      return Boolean.TRUE;
    }

    private static Boolean sendArray2UL(
      final SMFParserEventsDataAttributeValuesType events,
      final SMFAttributeArrayIntegerUnsigned2Type array_2d)
    {
      final Vector<Vector2L> vv = array_2d.values();
      for (int index = 0; index < array_2d.size(); ++index) {
        final Vector2L v = vv.get(index);
        events.onDataAttributeValueIntegerUnsigned2(v.x(), v.y());
      }
      return Boolean.TRUE;
    }

    private static Boolean sendArray1UL(
      final SMFParserEventsDataAttributeValuesType events,
      final SMFAttributeArrayIntegerUnsigned1Type array_1d)
    {
      final Vector<Long> vv = array_1d.values();
      for (int index = 0; index < array_1d.size(); ++index) {
        final Long v = vv.get(index);
        events.onDataAttributeValueIntegerUnsigned1(v.longValue());
      }
      return Boolean.TRUE;
    }

    @Override
    public void close()
      throws IOException
    {

    }
  }
}
