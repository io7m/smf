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

import com.io7m.jnull.NullCheck;
import com.io7m.smfj.core.SMFAttribute;
import com.io7m.smfj.core.SMFFormatVersion;
import com.io7m.smfj.core.SMFHeader;
import com.io7m.smfj.parser.api.SMFParseError;
import com.io7m.smfj.serializer.api.SMFSerializerType;
import javaslang.collection.List;

import java.io.IOException;
import java.io.UncheckedIOException;

/**
 * The default implementation of the {@link SMFFCopierType}.
 */

public final class SMFFCopier implements SMFFCopierType
{
  private final SMFSerializerType serializer;
  private List<SMFParseError> errors;

  private SMFFCopier(
    final SMFSerializerType in_serializer)
  {
    this.serializer = NullCheck.notNull(in_serializer, "Serializer");
    this.errors = List.empty();
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

  }

  @Override
  public void onVersionReceived(
    final SMFFormatVersion version)
  {

  }

  @Override
  public void onFinish()
  {

  }

  @Override
  public void onError(
    final SMFParseError e)
  {
    this.errors = this.errors.append(e);
  }

  @Override
  public void onHeaderParsed(
    final SMFHeader header)
  {
    this.serializer.serializeHeader(header);
  }

  @Override
  public void onDataAttributeStart(
    final SMFAttribute attribute)
  {
    try {
      this.serializer.serializeData(attribute.name());
    } catch (final IOException e) {
      throw new UncheckedIOException(e);
    }
  }

  @Override
  public void onDataAttributeValueIntegerSigned1(
    final long x)
  {
    try {
      this.serializer.serializeValueIntegerSigned1(x);
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
      this.serializer.serializeValueIntegerSigned2(x, y);
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
      this.serializer.serializeValueIntegerSigned3(x, y, z);
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
      this.serializer.serializeValueIntegerSigned4(x, y, z, w);
    } catch (final IOException e) {
      throw new UncheckedIOException(e);
    }
  }

  @Override
  public void onDataAttributeValueIntegerUnsigned1(
    final long x)
  {
    try {
      this.serializer.serializeValueIntegerUnsigned1(x);
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
      this.serializer.serializeValueIntegerUnsigned2(x, y);
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
      this.serializer.serializeValueIntegerUnsigned3(x, y, z);
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
      this.serializer.serializeValueIntegerUnsigned4(x, y, z, w);
    } catch (final IOException e) {
      throw new UncheckedIOException(e);
    }
  }

  @Override
  public void onDataAttributeValueFloat1(
    final double x)
  {
    try {
      this.serializer.serializeValueFloat1(x);
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
      this.serializer.serializeValueFloat2(x, y);
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
      this.serializer.serializeValueFloat3(x, y, z);
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
      this.serializer.serializeValueFloat4(x, y, z, w);
    } catch (final IOException e) {
      throw new UncheckedIOException(e);
    }
  }

  @Override
  public void onDataAttributeFinish(
    final SMFAttribute attribute)
  {

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
    try {
      this.serializer.serializeTriangle(v0, v1, v2);
    } catch (final IOException e) {
      throw new UncheckedIOException(e);
    }
  }

  @Override
  public void onDataTrianglesFinish()
  {

  }

  @Override
  public List<SMFParseError> errors()
  {
    return this.errors;
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
    try {
      this.serializer.serializeMetadata(vendor, schema, data);
    } catch (final IOException e) {
      throw new UncheckedIOException(e);
    }
  }
}
