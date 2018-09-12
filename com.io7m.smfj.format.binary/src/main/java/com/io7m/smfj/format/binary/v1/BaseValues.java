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

package com.io7m.smfj.format.binary.v1;

import java.util.Objects;
import com.io7m.smfj.core.SMFAttribute;
import com.io7m.smfj.format.binary.SMFBAlignment;
import com.io7m.smfj.format.binary.SMFBDataStreamWriterType;
import com.io7m.smfj.format.binary.SMFBSection;
import com.io7m.smfj.serializer.api.SMFSerializerDataAttributesValuesType;

import java.io.IOException;

abstract class BaseValues
  implements SMFSerializerDataAttributesValuesType
{
  private final SMFAttribute attribute;
  private final SMFBDataStreamWriterType writer;

  BaseValues(
    final SMFBDataStreamWriterType in_writer,
    final SMFAttribute in_attribute)
  {
    this.writer = Objects.requireNonNull(in_writer, "Writer");
    this.attribute = Objects.requireNonNull(in_attribute, "Attribute");
  }

  @Override
  public final void close()
    throws IOException
  {
    final long position = this.writer.position();
    this.writer.padTo(
      SMFBAlignment.alignNext(position, SMFBSection.SECTION_ALIGNMENT),
      (byte) 0x0);
  }

  protected final SMFBDataStreamWriterType writer()
  {
    return this.writer;
  }

  private void fail(
    final String received)
  {
    final String text =
      new StringBuilder(128)
        .append("Incorrect type.")
        .append(System.lineSeparator())
        .append("  Expected: ")
        .append(this.attribute.componentType().getName())
        .append(" ")
        .append(this.attribute.componentCount())
        .append(" ")
        .append(this.attribute.componentSizeBits())
        .append(System.lineSeparator())
        .append("  Received: ")
        .append(received)
        .append(System.lineSeparator())
        .toString();
    throw new IllegalArgumentException(text);
  }

  @Override
  public void serializeValueFloat4(
    final double x,
    final double y,
    final double z,
    final double w)
    throws IOException, IllegalArgumentException
  {
    this.fail("A four element floating point vector");
  }

  @Override
  public void serializeValueFloat3(
    final double x,
    final double y,
    final double z)
    throws IOException, IllegalArgumentException
  {
    this.fail("A three element floating point vector");
  }

  @Override
  public void serializeValueFloat2(
    final double x,
    final double y)
    throws IOException, IllegalArgumentException
  {
    this.fail("A two element floating point vector");
  }

  @Override
  public void serializeValueFloat1(
    final double x)
    throws IOException, IllegalArgumentException
  {
    this.fail("A one element floating point vector");
  }

  @Override
  public void serializeValueIntegerSigned4(
    final long x,
    final long y,
    final long z,
    final long w)
    throws IOException, IllegalArgumentException
  {
    this.fail("A four element signed integer vector");
  }

  @Override
  public void serializeValueIntegerSigned3(
    final long x,
    final long y,
    final long z)
    throws IOException, IllegalArgumentException
  {
    this.fail("A three element signed integer vector");
  }

  @Override
  public void serializeValueIntegerSigned2(
    final long x,
    final long y)
    throws IOException, IllegalArgumentException
  {
    this.fail("A two element signed integer vector");
  }

  @Override
  public void serializeValueIntegerSigned1(
    final long x)
    throws IOException, IllegalArgumentException
  {
    this.fail("A one element signed integer vector");
  }

  @Override
  public void serializeValueIntegerUnsigned4(
    final long x,
    final long y,
    final long z,
    final long w)
    throws IOException, IllegalArgumentException
  {
    this.fail("A four element unsigned integer vector");
  }

  @Override
  public void serializeValueIntegerUnsigned3(
    final long x,
    final long y,
    final long z)
    throws IOException, IllegalArgumentException
  {
    this.fail("A three element unsigned integer vector");
  }

  @Override
  public void serializeValueIntegerUnsigned2(
    final long x,
    final long y)
    throws IOException, IllegalArgumentException
  {
    this.fail("A two element unsigned integer vector");
  }

  @Override
  public void serializeValueIntegerUnsigned1(
    final long x)
    throws IOException, IllegalArgumentException
  {
    this.fail("A one element unsigned integer vector");
  }
}
