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

package com.io7m.smfj.format.binary2.internal.serial;

import com.io7m.jaffirm.core.Invariants;
import com.io7m.jbssio.api.BSSWriterSequentialType;
import com.io7m.smfj.core.SMFAttribute;
import com.io7m.smfj.serializer.api.SMFSerializerDataAttributesValuesType;
import java.io.IOException;
import java.util.Objects;

public abstract class WriterBase implements
  SMFSerializerDataAttributesValuesType
{
  private final SMFAttribute attribute;
  private final BSSWriterSequentialType writer;

  protected WriterBase(
    final BSSWriterSequentialType inWriter,
    final SMFAttribute inAttribute)
  {
    this.writer =
      Objects.requireNonNull(inWriter, "Writer");
    this.attribute =
      Objects.requireNonNull(inAttribute, "Attribute");
  }

  @Override
  public final void close()
    throws IOException
  {
    final var remaining = this.writer.bytesRemaining();
    if (remaining.isEmpty()) {
      throw new IllegalStateException(
        "Misused BaseValues: writer must be bounded");
    }

    final var end =
      this.writer.offsetCurrentRelative() + remaining.getAsLong();

    this.writer.padTo(end);

    Invariants.checkInvariantL(
      this.writer.offsetCurrentAbsolute(),
      x -> x % 16L == 0L,
      x -> "Data must be aligned");

    this.writer.close();
  }

  protected final BSSWriterSequentialType writer()
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

  /**
   * Must be overridden for writers that accept values of this type.
   *
   * @param x The x value
   * @param y The y value
   * @param z The z value
   * @param w The w value
   *
   * @throws IOException              On I/O errors
   * @throws IllegalArgumentException On other errors
   */

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

  /**
   * Must be overridden for writers that accept values of this type.
   *
   * @param x The x value
   * @param y The y value
   * @param z The z value
   *
   * @throws IOException              On I/O errors
   * @throws IllegalArgumentException On other errors
   */

  @Override
  public void serializeValueFloat3(
    final double x,
    final double y,
    final double z)
    throws IOException, IllegalArgumentException
  {
    this.fail("A three element floating point vector");
  }

  /**
   * Must be overridden for writers that accept values of this type.
   *
   * @param x The x value
   * @param y The y value
   *
   * @throws IOException              On I/O errors
   * @throws IllegalArgumentException On other errors
   */

  @Override
  public void serializeValueFloat2(
    final double x,
    final double y)
    throws IOException, IllegalArgumentException
  {
    this.fail("A two element floating point vector");
  }

  /**
   * Must be overridden for writers that accept values of this type.
   *
   * @param x The x value
   *
   * @throws IOException              On I/O errors
   * @throws IllegalArgumentException On other errors
   */

  @Override
  public void serializeValueFloat1(
    final double x)
    throws IOException, IllegalArgumentException
  {
    this.fail("A one element floating point vector");
  }

  /**
   * Must be overridden for writers that accept values of this type.
   *
   * @param x The x value
   * @param y The y value
   * @param z The z value
   * @param w The w value
   *
   * @throws IOException              On I/O errors
   * @throws IllegalArgumentException On other errors
   */

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

  /**
   * Must be overridden for writers that accept values of this type.
   *
   * @param x The x value
   * @param y The y value
   * @param z The z value
   *
   * @throws IOException              On I/O errors
   * @throws IllegalArgumentException On other errors
   */

  @Override
  public void serializeValueIntegerSigned3(
    final long x,
    final long y,
    final long z)
    throws IOException, IllegalArgumentException
  {
    this.fail("A three element signed integer vector");
  }

  /**
   * Must be overridden for writers that accept values of this type.
   *
   * @param x The x value
   * @param y The y value
   *
   * @throws IOException              On I/O errors
   * @throws IllegalArgumentException On other errors
   */

  @Override
  public void serializeValueIntegerSigned2(
    final long x,
    final long y)
    throws IOException, IllegalArgumentException
  {
    this.fail("A two element signed integer vector");
  }

  /**
   * Must be overridden for writers that accept values of this type.
   *
   * @param x The x value
   *
   * @throws IOException              On I/O errors
   * @throws IllegalArgumentException On other errors
   */

  @Override
  public void serializeValueIntegerSigned1(
    final long x)
    throws IOException, IllegalArgumentException
  {
    this.fail("A one element signed integer vector");
  }

  /**
   * Must be overridden for writers that accept values of this type.
   *
   * @param x The x value
   * @param y The y value
   * @param z The z value
   * @param w The w value
   *
   * @throws IOException              On I/O errors
   * @throws IllegalArgumentException On other errors
   */

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

  /**
   * Must be overridden for writers that accept values of this type.
   *
   * @param x The x value
   * @param y The y value
   * @param z The z value
   *
   * @throws IOException              On I/O errors
   * @throws IllegalArgumentException On other errors
   */

  @Override
  public void serializeValueIntegerUnsigned3(
    final long x,
    final long y,
    final long z)
    throws IOException, IllegalArgumentException
  {
    this.fail("A three element unsigned integer vector");
  }

  /**
   * Must be overridden for writers that accept values of this type.
   *
   * @param x The x value
   * @param y The y value
   *
   * @throws IOException              On I/O errors
   * @throws IllegalArgumentException On other errors
   */

  @Override
  public void serializeValueIntegerUnsigned2(
    final long x,
    final long y)
    throws IOException, IllegalArgumentException
  {
    this.fail("A two element unsigned integer vector");
  }

  /**
   * Must be overridden for writers that accept values of this type.
   *
   * @param x The x value
   *
   * @throws IOException              On I/O errors
   * @throws IllegalArgumentException On other errors
   */

  @Override
  public void serializeValueIntegerUnsigned1(
    final long x)
    throws IOException, IllegalArgumentException
  {
    this.fail("A one element unsigned integer vector");
  }
}
