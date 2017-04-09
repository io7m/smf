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
import com.io7m.jnull.NullCheck;
import com.io7m.jtensors.core.unparameterized.vectors.Vector2D;
import com.io7m.jtensors.core.unparameterized.vectors.Vector2L;
import com.io7m.jtensors.core.unparameterized.vectors.Vector3D;
import com.io7m.jtensors.core.unparameterized.vectors.Vector3L;
import com.io7m.jtensors.core.unparameterized.vectors.Vector4D;
import com.io7m.jtensors.core.unparameterized.vectors.Vector4L;
import com.io7m.junreachable.UnreachableCodeException;
import com.io7m.smfj.core.SMFAttribute;
import com.io7m.smfj.core.SMFAttributeName;
import com.io7m.smfj.core.SMFErrorType;
import com.io7m.smfj.core.SMFFormatVersion;
import com.io7m.smfj.core.SMFHeader;
import javaslang.collection.HashMap;
import javaslang.collection.List;
import javaslang.collection.Map;
import javaslang.collection.Vector;

/**
 * The default implementation of the {@link SMFMemoryMeshProducerType}
 * interface.
 */

public final class SMFMemoryMeshProducer implements SMFMemoryMeshProducerType
{
  private Vector<SMFMetadata> metadata;
  private boolean started;
  private List<SMFErrorType> errors;
  private SMFHeader header;
  private Vector<Object> elements;
  private Map<SMFAttributeName, SMFAttributeArrayType> arrays;
  private Vector<Vector3L> triangles;
  private SMFMemoryMesh mesh;
  private boolean finished;

  private SMFMemoryMeshProducer()
  {
    this.started = false;
    this.errors = List.empty();
    this.arrays = HashMap.empty();
    this.triangles = Vector.empty();
    this.metadata = Vector.empty();
  }

  /**
   * @return A new memory mesh producer
   */

  public static SMFMemoryMeshProducerType create()
  {
    return new SMFMemoryMeshProducer();
  }

  @Override
  public void onError(
    final SMFErrorType e)
  {
    this.errors = this.errors.append(e);
  }

  @Override
  public void onStart()
  {
    if (this.started) {
      throw new IllegalStateException("A mesh producer may not be reused");
    }
    this.started = true;
  }

  @Override
  public void onVersionReceived(
    final SMFFormatVersion version)
  {

  }

  @Override
  public void onFinish()
  {
    this.mesh =
      SMFMemoryMesh.builder()
        .setArrays(this.arrays)
        .setHeader(this.header)
        .setTriangles(this.triangles)
        .setMetadata(this.metadata)
        .build();

    this.finished = true;
  }

  @Override
  public void onHeaderParsed(
    final SMFHeader in_header)
  {
    this.header = NullCheck.notNull(in_header, "Header");
  }

  @Override
  public List<SMFErrorType> errors()
  {
    return this.errors;
  }

  @Override
  public SMFHeader header()
    throws IllegalStateException
  {
    if (this.header == null) {
      throw new IllegalStateException("Header has not been parsed");
    }
    return this.header;
  }

  @Override
  public SMFMemoryMesh mesh()
    throws IllegalStateException
  {
    Preconditions.checkPrecondition(
      this.finished, "Mesh parsing has not yet finished");

    if (this.errors.isEmpty()) {
      return this.mesh;
    }
    throw new IllegalStateException("Mesh parsing failed");
  }

  @Override
  public boolean onMeta(
    final long vendor,
    final long schema,
    final long length)
  {
    return true;
  }

  @Override
  public void onMetaData(
    final long vendor,
    final long schema,
    final byte[] data)
  {
    this.metadata = this.metadata.append(SMFMetadata.of(vendor, schema, data));
  }

  @Override
  public void onDataAttributeStart(
    final SMFAttribute attribute)
  {
    this.elements = Vector.empty();
  }

  @Override
  public void onDataAttributeValueIntegerSigned1(
    final long x)
  {
    this.elements = this.elements.append(Long.valueOf(x));
  }

  @Override
  public void onDataAttributeValueIntegerSigned2(
    final long x,
    final long y)
  {
    this.elements = this.elements.append(Vector2L.of(x, y));
  }

  @Override
  public void onDataAttributeValueIntegerSigned3(
    final long x,
    final long y,
    final long z)
  {
    this.elements = this.elements.append(Vector3L.of(x, y, z));
  }

  @Override
  public void onDataAttributeValueIntegerSigned4(
    final long x,
    final long y,
    final long z,
    final long w)
  {
    this.elements = this.elements.append(Vector4L.of(x, y, z, w));
  }

  @Override
  public void onDataAttributeValueIntegerUnsigned1(
    final long x)
  {
    this.elements = this.elements.append(Long.valueOf(x));
  }

  @Override
  public void onDataAttributeValueIntegerUnsigned2(
    final long x,
    final long y)
  {
    this.elements = this.elements.append(Vector2L.of(x, y));
  }

  @Override
  public void onDataAttributeValueIntegerUnsigned3(
    final long x,
    final long y,
    final long z)
  {
    this.elements = this.elements.append(Vector3L.of(x, y, z));
  }

  @Override
  public void onDataAttributeValueIntegerUnsigned4(
    final long x,
    final long y,
    final long z,
    final long w)
  {
    this.elements = this.elements.append(Vector4L.of(x, y, z, w));
  }

  @Override
  public void onDataAttributeValueFloat1(
    final double x)
  {
    this.elements = this.elements.append(Double.valueOf(x));
  }

  @Override
  public void onDataAttributeValueFloat2(
    final double x,
    final double y)
  {
    this.elements = this.elements.append(Vector2D.of(x, y));
  }

  @Override
  public void onDataAttributeValueFloat3(
    final double x,
    final double y,
    final double z)
  {
    this.elements = this.elements.append(Vector3D.of(x, y, z));
  }

  @Override
  public void onDataAttributeValueFloat4(
    final double x,
    final double y,
    final double z,
    final double w)
  {
    this.elements = this.elements.append(Vector4D.of(x, y, z, w));
  }

  @Override
  public void onDataAttributeFinish(
    final SMFAttribute attribute)
  {
    switch (attribute.componentType()) {
      case ELEMENT_TYPE_INTEGER_SIGNED: {
        switch (attribute.componentCount()) {
          case 4: {
            this.arrays = this.arrays.put(
              attribute.name(),
              SMFAttributeArrayIntegerSigned4.builder()
                .setValues(this.elements.map(x -> (Vector4L) x))
                .build());
            break;
          }
          case 3: {
            this.arrays = this.arrays.put(
              attribute.name(),
              SMFAttributeArrayIntegerSigned3.builder()
                .setValues(this.elements.map(x -> (Vector3L) x))
                .build());
            break;
          }
          case 2: {
            this.arrays = this.arrays.put(
              attribute.name(),
              SMFAttributeArrayIntegerSigned2.builder()
                .setValues(this.elements.map(x -> (Vector2L) x))
                .build());
            break;
          }
          case 1: {
            this.arrays = this.arrays.put(
              attribute.name(),
              SMFAttributeArrayIntegerSigned1.builder()
                .setValues(this.elements.map(x -> (Long) x))
                .build());
            break;
          }
          default: {
            throw new UnreachableCodeException();
          }
        }
        break;
      }

      case ELEMENT_TYPE_INTEGER_UNSIGNED: {
        switch (attribute.componentCount()) {
          case 4: {
            this.arrays = this.arrays.put(
              attribute.name(),
              SMFAttributeArrayIntegerUnsigned4.builder()
                .setValues(this.elements.map(x -> (Vector4L) x))
                .build());
            break;
          }
          case 3: {
            this.arrays = this.arrays.put(
              attribute.name(),
              SMFAttributeArrayIntegerUnsigned3.builder()
                .setValues(this.elements.map(x -> (Vector3L) x))
                .build());
            break;
          }
          case 2: {
            this.arrays = this.arrays.put(
              attribute.name(),
              SMFAttributeArrayIntegerUnsigned2.builder()
                .setValues(this.elements.map(x -> (Vector2L) x))
                .build());
            break;
          }
          case 1: {
            this.arrays = this.arrays.put(
              attribute.name(),
              SMFAttributeArrayIntegerUnsigned1.builder()
                .setValues(this.elements.map(x -> (Long) x))
                .build());
            break;
          }
          default: {
            throw new UnreachableCodeException();
          }
        }
        break;
      }

      case ELEMENT_TYPE_FLOATING: {
        switch (attribute.componentCount()) {
          case 4: {
            this.arrays = this.arrays.put(
              attribute.name(),
              SMFAttributeArrayFloating4.builder()
                .setValues(this.elements.map(x -> (Vector4D) x))
                .build());
            break;
          }
          case 3: {
            this.arrays = this.arrays.put(
              attribute.name(),
              SMFAttributeArrayFloating3.builder()
                .setValues(this.elements.map(x -> (Vector3D) x))
                .build());
            break;
          }
          case 2: {
            this.arrays = this.arrays.put(
              attribute.name(),
              SMFAttributeArrayFloating2.builder()
                .setValues(this.elements.map(x -> (Vector2D) x))
                .build());
            break;
          }
          case 1: {
            this.arrays = this.arrays.put(
              attribute.name(),
              SMFAttributeArrayFloating1.builder()
                .setValues(this.elements.map(x -> (Double) x))
                .build());
            break;
          }
          default: {
            throw new UnreachableCodeException();
          }
        }
        break;
      }
    }
  }

  @Override
  public void onDataTrianglesStart()
  {

  }

  @Override
  public void onDataTriangle(
    final long v0,
    final long v1,
    final long v2)
  {
    this.triangles = this.triangles.append(Vector3L.of(v0, v1, v2));
  }

  @Override
  public void onDataTrianglesFinish()
  {

  }
}
