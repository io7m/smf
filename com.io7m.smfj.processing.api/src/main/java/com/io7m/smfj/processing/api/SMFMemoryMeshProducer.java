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
import com.io7m.smfj.core.SMFSchemaIdentifier;
import com.io7m.smfj.core.SMFWarningType;
import com.io7m.smfj.parser.api.SMFParserEventsBodyType;
import com.io7m.smfj.parser.api.SMFParserEventsDataAttributeValuesType;
import com.io7m.smfj.parser.api.SMFParserEventsDataAttributesNonInterleavedType;
import com.io7m.smfj.parser.api.SMFParserEventsDataMetaType;
import com.io7m.smfj.parser.api.SMFParserEventsDataTrianglesType;
import com.io7m.smfj.parser.api.SMFParserEventsHeaderType;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The default implementation of the {@link SMFMemoryMeshProducerType} interface.
 */

public final class SMFMemoryMeshProducer
  implements SMFMemoryMeshProducerType,
  SMFParserEventsHeaderType,
  SMFParserEventsBodyType,
  SMFParserEventsDataTrianglesType,
  SMFParserEventsDataAttributesNonInterleavedType,
  SMFParserEventsDataMetaType,
  SMFParserEventsDataAttributeValuesType
{
  private static final Logger LOG =
    LoggerFactory.getLogger(SMFMemoryMeshProducer.class);

  private final List<Object> elements;
  private final List<SMFErrorType> errors;
  private final List<SMFMetadata> metadata;
  private final List<SMFWarningType> warnings;
  private final List<Vector3L> triangles;
  private final Map<SMFAttributeName, SMFAttributeArrayType> arrays;
  private SMFAttribute attribute_current;
  private SMFHeader header;
  private SMFMemoryMesh mesh;
  private boolean finished;
  private boolean started;

  private SMFMemoryMeshProducer()
  {
    this.started = false;
    this.errors = new ArrayList<>();
    this.warnings = new ArrayList<>();
    this.arrays = new HashMap<>();
    this.triangles = new ArrayList<>();
    this.metadata = new ArrayList<>();
    this.elements = new ArrayList<>();
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
    this.errors.add(e);
  }

  @Override
  public void onWarning(
    final SMFWarningType w)
  {
    this.warnings.add(w);
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
  public Optional<SMFParserEventsHeaderType> onVersionReceived(
    final SMFFormatVersion version)
  {
    Objects.requireNonNull(version, "Version");
    return Optional.of(this);
  }

  @Override
  public void onFinish()
  {
    if (this.errors.isEmpty()) {
      for (final var arrayEntry : this.arrays.entrySet()) {
        final var attributeName = arrayEntry.getKey();
        if (!this.header.attributesByName().containsKey(attributeName)) {
          throw new IllegalStateException(String.format(
            "Attribute %s is not specified in the header",
            attributeName.value()));
        }
      }

      for (final var attribute : this.header.attributesInOrder()) {
        if (!this.arrays.containsKey(attribute.name())) {
          throw new IllegalStateException(String.format(
            "Attribute %s is specified in the header but is not present in the mesh",
            attribute.name().value()));
        }
      }

      this.mesh =
        SMFMemoryMesh.builder()
          .setArrays(this.arrays)
          .setHeader(this.header)
          .setTriangles(this.triangles)
          .setMetadata(this.metadata)
          .build();
    }

    this.finished = true;
  }

  @Override
  public List<SMFErrorType> errors()
  {
    return List.copyOf(this.errors);
  }

  @Override
  public List<SMFWarningType> warnings()
  {
    return List.copyOf(this.warnings);
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
  public Optional<SMFParserEventsDataAttributesNonInterleavedType> onAttributesNonInterleaved()
  {
    return Optional.of(this);
  }

  @Override
  public Optional<SMFParserEventsDataTrianglesType> onTriangles()
  {
    return Optional.of(this);
  }

  @Override
  public void onDataAttributeValueIntegerSigned1(
    final long x)
  {
    this.elements.add(Long.valueOf(x));
  }

  @Override
  public void onDataAttributeValueIntegerSigned2(
    final long x,
    final long y)
  {
    this.elements.add(Vector2L.of(x, y));
  }

  @Override
  public void onDataAttributeValueIntegerSigned3(
    final long x,
    final long y,
    final long z)
  {
    this.elements.add(Vector3L.of(x, y, z));
  }

  @Override
  public void onDataAttributeValueIntegerSigned4(
    final long x,
    final long y,
    final long z,
    final long w)
  {
    this.elements.add(Vector4L.of(x, y, z, w));
  }

  @Override
  public void onDataAttributeValueIntegerUnsigned1(
    final long x)
  {
    this.elements.add(Long.valueOf(x));
  }

  @Override
  public void onDataAttributeValueIntegerUnsigned2(
    final long x,
    final long y)
  {
    this.elements.add(Vector2L.of(x, y));
  }

  @Override
  public void onDataAttributeValueIntegerUnsigned3(
    final long x,
    final long y,
    final long z)
  {
    this.elements.add(Vector3L.of(x, y, z));
  }

  @Override
  public void onDataAttributeValueIntegerUnsigned4(
    final long x,
    final long y,
    final long z,
    final long w)
  {
    this.elements.add(Vector4L.of(x, y, z, w));
  }

  @Override
  public void onDataAttributeValueFloat1(
    final double x)
  {
    this.elements.add(Double.valueOf(x));
  }

  @Override
  public void onDataAttributeValueFloat2(
    final double x,
    final double y)
  {
    this.elements.add(Vector2D.of(x, y));
  }

  @Override
  public void onDataAttributeValueFloat3(
    final double x,
    final double y,
    final double z)
  {
    this.elements.add(Vector3D.of(x, y, z));
  }

  @Override
  public void onDataAttributeValueFloat4(
    final double x,
    final double y,
    final double z,
    final double w)
  {
    this.elements.add(Vector4D.of(x, y, z, w));
  }

  @Override
  public void onDataAttributeValueFinish()
  {
    LOG.debug("finished attribute {}", this.attribute_current.name().value());

    switch (this.attribute_current.componentType()) {
      case ELEMENT_TYPE_INTEGER_SIGNED: {
        this.finishIntegerSignedAttribute();
        break;
      }

      case ELEMENT_TYPE_INTEGER_UNSIGNED: {
        this.finishIntegerUnsignedAttribute();
        break;
      }

      case ELEMENT_TYPE_FLOATING: {
        this.finishFloatingAttribute();
        break;
      }
    }
  }

  @SuppressWarnings("unchecked")
  private static <A, B extends A> List<B> unsafeListCast(final List<A> xs)
  {
    return (List<B>) xs;
  }

  private void finishFloatingAttribute()
  {
    switch (this.attribute_current.componentCount()) {
      case 4: {
        this.arrays.put(
          this.attribute_current.name(),
          SMFAttributeArrayFloating4.builder()
            .setValues(unsafeListCast(this.elements))
            .build());
        break;
      }
      case 3: {
        this.arrays.put(
          this.attribute_current.name(),
          SMFAttributeArrayFloating3.builder()
            .setValues(unsafeListCast(this.elements))
            .build());
        break;
      }
      case 2: {
        this.arrays.put(
          this.attribute_current.name(),
          SMFAttributeArrayFloating2.builder()
            .setValues(unsafeListCast(this.elements))
            .build());
        break;
      }
      case 1: {
        this.arrays.put(
          this.attribute_current.name(),
          SMFAttributeArrayFloating1.builder()
            .setValues(unsafeListCast(this.elements))
            .build());
        break;
      }
      default: {
        throw new UnreachableCodeException();
      }
    }
  }

  private void finishIntegerUnsignedAttribute()
  {
    switch (this.attribute_current.componentCount()) {
      case 4: {
        this.arrays.put(
          this.attribute_current.name(),
          SMFAttributeArrayIntegerUnsigned4.builder()
            .setValues(unsafeListCast(this.elements))
            .build());
        break;
      }
      case 3: {
        this.arrays.put(
          this.attribute_current.name(),
          SMFAttributeArrayIntegerUnsigned3.builder()
            .setValues(unsafeListCast(this.elements))
            .build());
        break;
      }
      case 2: {
        this.arrays.put(
          this.attribute_current.name(),
          SMFAttributeArrayIntegerUnsigned2.builder()
            .setValues(unsafeListCast(this.elements))
            .build());
        break;
      }
      case 1: {
        this.arrays.put(
          this.attribute_current.name(),
          SMFAttributeArrayIntegerUnsigned1.builder()
            .setValues(unsafeListCast(this.elements))
            .build());
        break;
      }
      default: {
        throw new UnreachableCodeException();
      }
    }
  }

  private void finishIntegerSignedAttribute()
  {
    switch (this.attribute_current.componentCount()) {
      case 4: {
        this.arrays.put(
          this.attribute_current.name(),
          SMFAttributeArrayIntegerSigned4.builder()
            .setValues(unsafeListCast(this.elements))
            .build());
        break;
      }
      case 3: {
        this.arrays.put(
          this.attribute_current.name(),
          SMFAttributeArrayIntegerSigned3.builder()
            .setValues(unsafeListCast(this.elements))
            .build());
        break;
      }
      case 2: {
        this.arrays.put(
          this.attribute_current.name(),
          SMFAttributeArrayIntegerSigned2.builder()
            .setValues(unsafeListCast(this.elements))
            .build());
        break;
      }
      case 1: {
        this.arrays.put(
          this.attribute_current.name(),
          SMFAttributeArrayIntegerSigned1.builder()
            .setValues(unsafeListCast(this.elements))
            .build());
        break;
      }
      default: {
        throw new UnreachableCodeException();
      }
    }
  }

  @Override
  public void onDataTriangle(
    final long v0,
    final long v1,
    final long v2)
  {
    this.triangles.add(Vector3L.of(v0, v1, v2));
  }

  @Override
  public void onDataTrianglesFinish()
  {

  }

  @Override
  public Optional<SMFParserEventsBodyType> onHeaderParsed(
    final SMFHeader in_header)
  {
    this.header = Objects.requireNonNull(in_header, "Header");
    return Optional.of(this);
  }

  @Override
  public Optional<SMFParserEventsDataAttributeValuesType> onDataAttributeStart(
    final SMFAttribute attribute)
  {
    Objects.requireNonNull(attribute, "Attribute");
    this.attribute_current = attribute;
    this.elements.clear();
    return Optional.of(this);
  }

  @Override
  public void onDataAttributesNonInterleavedFinish()
  {

  }

  @Override
  public void onMetaData(
    final SMFSchemaIdentifier schema,
    final byte[] data)
  {
    Objects.requireNonNull(schema, "Schema");
    Objects.requireNonNull(data, "Data");
    this.metadata.add(SMFMetadata.of(schema, data));
  }

  @Override
  public Optional<SMFParserEventsDataMetaType> onMeta(
    final SMFSchemaIdentifier schema)
  {
    Objects.requireNonNull(schema, "Schema");
    return Optional.of(this);
  }
}
