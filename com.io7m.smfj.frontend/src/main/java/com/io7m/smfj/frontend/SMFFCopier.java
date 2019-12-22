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

package com.io7m.smfj.frontend;

import com.io7m.smfj.core.SMFAttribute;
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
import com.io7m.smfj.serializer.api.SMFSerializerDataAttributesNonInterleavedType;
import com.io7m.smfj.serializer.api.SMFSerializerDataAttributesValuesType;
import com.io7m.smfj.serializer.api.SMFSerializerDataTrianglesType;
import com.io7m.smfj.serializer.api.SMFSerializerType;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * The default implementation of the {@link SMFFCopierType}.
 */

public final class SMFFCopier
  implements SMFFCopierType,
  SMFParserEventsBodyType,
  SMFParserEventsHeaderType,
  SMFParserEventsDataAttributesNonInterleavedType,
  SMFParserEventsDataTrianglesType,
  SMFParserEventsDataMetaType,
  SMFParserEventsDataAttributeValuesType
{
  private final SMFSerializerType serializer;
  private List<SMFWarningType> warnings;
  private List<SMFErrorType> errors;
  private SMFSerializerDataAttributesNonInterleavedType serializer_data_noninterleaved;
  private SMFSerializerDataTrianglesType serializer_triangles;
  private SMFSerializerDataAttributesValuesType serializer_attribute;

  private SMFFCopier(
    final SMFSerializerType in_serializer)
  {
    this.serializer = Objects.requireNonNull(in_serializer, "Serializer");
    this.errors = new ArrayList<>();
    this.warnings = new ArrayList<>();
  }

  /**
   * Create a new copier.
   *
   * @param in_serializer The serializer
   *
   * @return A new copier
   */

  public static SMFFCopierType create(
    final SMFSerializerType in_serializer)
  {
    return new SMFFCopier(in_serializer);
  }

  @Override
  public void onStart()
  {
    this.errors.clear();
    this.warnings.clear();
  }

  @Override
  public Optional<SMFParserEventsHeaderType> onVersionReceived(
    final SMFFormatVersion in_version)
  {
    return Optional.of(this);
  }

  @Override
  public void onFinish()
  {
    try {
      this.serializer.close();
    } catch (final IOException e) {
      throw new UncheckedIOException(e);
    }
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
  public Optional<SMFParserEventsDataAttributeValuesType> onDataAttributeStart(
    final SMFAttribute attribute)
  {
    try {
      this.serializer_attribute =
        this.serializer_data_noninterleaved.serializeData(attribute.name());
    } catch (final IOException e) {
      throw new UncheckedIOException(e);
    }

    return Optional.of(this);
  }

  @Override
  public void onDataAttributesNonInterleavedFinish()
  {
    try {
      this.serializer_data_noninterleaved.close();
    } catch (final IOException e) {
      throw new UncheckedIOException(e);
    }
  }

  @Override
  public void onDataAttributeValueIntegerSigned1(
    final long x)
  {
    try {
      this.serializer_attribute.serializeValueIntegerSigned1(x);
    } catch (final IOException e) {
      throw new UncheckedIOException(e);
    }
  }

  @Override
  public void onDataAttributeValueIntegerSigned2(
    final long x,
    final long y)
  {
    try {
      this.serializer_attribute.serializeValueIntegerSigned2(x, y);
    } catch (final IOException e) {
      throw new UncheckedIOException(e);
    }
  }

  @Override
  public void onDataAttributeValueIntegerSigned3(
    final long x,
    final long y,
    final long z)
  {
    try {
      this.serializer_attribute.serializeValueIntegerSigned3(x, y, z);
    } catch (final IOException e) {
      throw new UncheckedIOException(e);
    }
  }

  @Override
  public void onDataAttributeValueIntegerSigned4(
    final long x,
    final long y,
    final long z,
    final long w)
  {
    try {
      this.serializer_attribute.serializeValueIntegerSigned4(x, y, z, w);
    } catch (final IOException e) {
      throw new UncheckedIOException(e);
    }
  }

  @Override
  public void onDataAttributeValueIntegerUnsigned1(
    final long x)
  {
    try {
      this.serializer_attribute.serializeValueIntegerUnsigned1(x);
    } catch (final IOException e) {
      throw new UncheckedIOException(e);
    }
  }

  @Override
  public void onDataAttributeValueIntegerUnsigned2(
    final long x,
    final long y)
  {
    try {
      this.serializer_attribute.serializeValueIntegerUnsigned2(x, y);
    } catch (final IOException e) {
      throw new UncheckedIOException(e);
    }
  }

  @Override
  public void onDataAttributeValueIntegerUnsigned3(
    final long x,
    final long y,
    final long z)
  {
    try {
      this.serializer_attribute.serializeValueIntegerUnsigned3(x, y, z);
    } catch (final IOException e) {
      throw new UncheckedIOException(e);
    }
  }

  @Override
  public void onDataAttributeValueIntegerUnsigned4(
    final long x,
    final long y,
    final long z,
    final long w)
  {
    try {
      this.serializer_attribute.serializeValueIntegerUnsigned4(x, y, z, w);
    } catch (final IOException e) {
      throw new UncheckedIOException(e);
    }
  }

  @Override
  public void onDataAttributeValueFloat1(
    final double x)
  {
    try {
      this.serializer_attribute.serializeValueFloat1(x);
    } catch (final IOException e) {
      throw new UncheckedIOException(e);
    }
  }

  @Override
  public void onDataAttributeValueFloat2(
    final double x,
    final double y)
  {
    try {
      this.serializer_attribute.serializeValueFloat2(x, y);
    } catch (final IOException e) {
      throw new UncheckedIOException(e);
    }
  }

  @Override
  public void onDataAttributeValueFloat3(
    final double x,
    final double y,
    final double z)
  {
    try {
      this.serializer_attribute.serializeValueFloat3(x, y, z);
    } catch (final IOException e) {
      throw new UncheckedIOException(e);
    }
  }

  @Override
  public void onDataAttributeValueFloat4(
    final double x,
    final double y,
    final double z,
    final double w)
  {
    try {
      this.serializer_attribute.serializeValueFloat4(x, y, z, w);
    } catch (final IOException e) {
      throw new UncheckedIOException(e);
    }
  }

  @Override
  public void onDataAttributeValueFinish()
  {
    try {
      this.serializer_attribute.close();
    } catch (final IOException e) {
      throw new UncheckedIOException(e);
    }
  }

  @Override
  public void onDataTriangle(
    final long v0,
    final long v1,
    final long v2)
  {
    try {
      this.serializer_triangles.serializeTriangle(v0, v1, v2);
    } catch (final IOException e) {
      throw new UncheckedIOException(e);
    }
  }

  @Override
  public void onDataTrianglesFinish()
  {
    try {
      this.serializer_triangles.close();
    } catch (final IOException e) {
      throw new UncheckedIOException(e);
    }
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
  public Optional<SMFParserEventsBodyType> onHeaderParsed(
    final SMFHeader header)
  {
    try {
      this.serializer.serializeHeader(header);
    } catch (final IOException e) {
      throw new UncheckedIOException(e);
    }

    return Optional.of(this);
  }

  @Override
  public Optional<SMFParserEventsDataAttributesNonInterleavedType> onAttributesNonInterleaved()
  {
    try {
      this.serializer_data_noninterleaved =
        this.serializer.serializeVertexDataNonInterleavedStart();
    } catch (final IOException e) {
      throw new UncheckedIOException(e);
    }

    return Optional.of(this);
  }

  @Override
  public Optional<SMFParserEventsDataTrianglesType> onTriangles()
  {
    try {
      this.serializer_triangles = this.serializer.serializeTrianglesStart();
    } catch (final IOException e) {
      throw new UncheckedIOException(e);
    }

    return Optional.of(this);
  }

  @Override
  public void onMetaData(
    final SMFSchemaIdentifier schema,
    final byte[] data)
  {
    try {
      this.serializer.serializeMetadata(schema, data);
    } catch (final IOException e) {
      throw new UncheckedIOException(e);
    }
  }

  @Override
  public Optional<SMFParserEventsDataMetaType> onMeta(
    final SMFSchemaIdentifier schema)
  {
    return Optional.of(this);
  }
}
