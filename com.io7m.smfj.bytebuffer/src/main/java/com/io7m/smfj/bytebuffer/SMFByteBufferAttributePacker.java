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

import com.io7m.jnull.NullCheck;
import com.io7m.jpra.runtime.java.JPRACursor1DType;
import com.io7m.junreachable.UnreachableCodeException;
import com.io7m.smfj.core.SMFAttribute;
import com.io7m.smfj.core.SMFErrorType;
import com.io7m.smfj.core.SMFWarningType;
import com.io7m.smfj.parser.api.SMFParserEventsDataAttributeValuesType;
import com.io7m.smfj.parser.api.SMFParserEventsErrorType;

import java.nio.ByteBuffer;

/**
 * An event listener that packs data into a given {@link ByteBuffer}.
 */

public final class SMFByteBufferAttributePacker
  implements SMFParserEventsDataAttributeValuesType
{
  private final long vertices;
  private final SMFParserEventsErrorType errors;
  private JPRACursor1DType<SMFByteBufferFloat1Type> cursor_float1;
  private JPRACursor1DType<SMFByteBufferFloat2Type> cursor_float2;
  private JPRACursor1DType<SMFByteBufferFloat3Type> cursor_float3;
  private JPRACursor1DType<SMFByteBufferFloat4Type> cursor_float4;
  private JPRACursor1DType<SMFByteBufferIntegerUnsigned1Type> cursor_unsigned1;
  private JPRACursor1DType<SMFByteBufferIntegerUnsigned2Type> cursor_unsigned2;
  private JPRACursor1DType<SMFByteBufferIntegerUnsigned3Type> cursor_unsigned3;
  private JPRACursor1DType<SMFByteBufferIntegerUnsigned4Type> cursor_unsigned4;
  private JPRACursor1DType<SMFByteBufferIntegerSigned1Type> cursor_signed1;
  private JPRACursor1DType<SMFByteBufferIntegerSigned2Type> cursor_signed2;
  private JPRACursor1DType<SMFByteBufferIntegerSigned3Type> cursor_signed3;
  private JPRACursor1DType<SMFByteBufferIntegerSigned4Type> cursor_signed4;

  /**
   * Construct a packer.
   *
   * @param in_errors           An error listener
   * @param in_attribute_buffer The byte buffer that will contain attribute
   *                            data
   * @param in_packed_config    The packing configuration
   * @param in_packed_attribute The attribute to be packed
   * @param in_vertices         The number of vertices that will be packed
   */

  public SMFByteBufferAttributePacker(
    final SMFParserEventsErrorType in_errors,
    final ByteBuffer in_attribute_buffer,
    final SMFByteBufferPackingConfiguration in_packed_config,
    final SMFByteBufferPackedAttribute in_packed_attribute,
    final long in_vertices)
  {
    this.errors = NullCheck.notNull(in_errors, "Errors");
    NullCheck.notNull(in_attribute_buffer, "Attribute buffer");
    NullCheck.notNull(in_packed_config, "Packed config");
    NullCheck.notNull(in_packed_attribute, "Packed attribute");

    final SMFAttribute attribute = in_packed_attribute.attribute();
    this.vertices = in_vertices;
    if (Long.compareUnsigned(in_vertices, 0L) > 0) {
      switch (attribute.componentType()) {
        case ELEMENT_TYPE_INTEGER_SIGNED: {
          switch (attribute.componentCount()) {
            case 1: {
              this.cursor_signed1 = SMFByteBufferCursors.createSigned1(
                in_packed_config, in_packed_attribute, in_attribute_buffer);
              break;
            }
            case 2: {
              this.cursor_signed2 = SMFByteBufferCursors.createSigned2(
                in_packed_config, in_packed_attribute, in_attribute_buffer);
              break;
            }
            case 3: {
              this.cursor_signed3 = SMFByteBufferCursors.createSigned3(
                in_packed_config, in_packed_attribute, in_attribute_buffer);
              break;
            }
            case 4: {
              this.cursor_signed4 = SMFByteBufferCursors.createSigned4(
                in_packed_config, in_packed_attribute, in_attribute_buffer);
              break;
            }
            default:
              throw new UnreachableCodeException();
          }
          break;
        }
        case ELEMENT_TYPE_INTEGER_UNSIGNED: {
          switch (attribute.componentCount()) {
            case 1: {
              this.cursor_unsigned1 = SMFByteBufferCursors.createUnsigned1(
                in_packed_config, in_packed_attribute, in_attribute_buffer);
              break;
            }
            case 2: {
              this.cursor_unsigned2 = SMFByteBufferCursors.createUnsigned2(
                in_packed_config, in_packed_attribute, in_attribute_buffer);
              break;
            }
            case 3: {
              this.cursor_unsigned3 = SMFByteBufferCursors.createUnsigned3(
                in_packed_config, in_packed_attribute, in_attribute_buffer);
              break;
            }
            case 4: {
              this.cursor_unsigned4 = SMFByteBufferCursors.createUnsigned4(
                in_packed_config, in_packed_attribute, in_attribute_buffer);
              break;
            }
            default:
              throw new UnreachableCodeException();
          }
          break;
        }
        case ELEMENT_TYPE_FLOATING: {
          switch (attribute.componentCount()) {
            case 1: {
              this.cursor_float1 = SMFByteBufferCursors.createFloat1(
                in_packed_config, in_packed_attribute, in_attribute_buffer);
              break;
            }
            case 2: {
              this.cursor_float2 = SMFByteBufferCursors.createFloat2(
                in_packed_config, in_packed_attribute, in_attribute_buffer);
              break;
            }
            case 3: {
              this.cursor_float3 = SMFByteBufferCursors.createFloat3(
                in_packed_config, in_packed_attribute, in_attribute_buffer);
              break;
            }
            case 4: {
              this.cursor_float4 = SMFByteBufferCursors.createFloat4(
                in_packed_config, in_packed_attribute, in_attribute_buffer);
              break;
            }
            default:
              throw new UnreachableCodeException();
          }
          break;
        }
      }
    }
  }

  private <T> void cursorNext(
    final JPRACursor1DType<T> c)
  {
    final int next = Math.addExact(c.getElementIndex(), 1);
    final long next_u = Integer.toUnsignedLong(next);
    if (Long.compareUnsigned(next_u, this.vertices) < 0) {
      c.setElementIndex(next);
    }
  }

  @Override
  public void onDataAttributeValueIntegerSigned1(
    final long x)
  {
    this.cursor_signed1.getElementView().set1SL(x);
    this.cursorNext(this.cursor_signed1);
  }

  @Override
  public void onDataAttributeValueIntegerSigned2(
    final long x,
    final long y)
  {
    this.cursor_signed2.getElementView().set2SL(x, y);
    this.cursorNext(this.cursor_signed2);
  }

  @Override
  public void onDataAttributeValueIntegerSigned3(
    final long x,
    final long y,
    final long z)
  {
    this.cursor_signed3.getElementView().set3SL(x, y, z);
    this.cursorNext(this.cursor_signed3);
  }

  @Override
  public void onDataAttributeValueIntegerSigned4(
    final long x,
    final long y,
    final long z,
    final long w)
  {
    this.cursor_signed4.getElementView().set4SL(x, y, z, w);
    this.cursorNext(this.cursor_signed4);
  }

  @Override
  public void onDataAttributeValueIntegerUnsigned1(
    final long x)
  {
    this.cursor_unsigned1.getElementView().set1UL(x);
    this.cursorNext(this.cursor_unsigned1);
  }

  @Override
  public void onDataAttributeValueIntegerUnsigned2(
    final long x,
    final long y)
  {
    this.cursor_unsigned2.getElementView().set2UL(x, y);
    this.cursorNext(this.cursor_unsigned2);
  }

  @Override
  public void onDataAttributeValueIntegerUnsigned3(
    final long x,
    final long y,
    final long z)
  {
    this.cursor_unsigned3.getElementView().set3UL(x, y, z);
    this.cursorNext(this.cursor_unsigned3);
  }

  @Override
  public void onDataAttributeValueIntegerUnsigned4(
    final long x,
    final long y,
    final long z,
    final long w)
  {
    this.cursor_unsigned4.getElementView().set4UL(x, y, z, w);
    this.cursorNext(this.cursor_unsigned4);
  }

  @Override
  public void onDataAttributeValueFloat1(
    final double x)
  {
    this.cursor_float1.getElementView().set1D(x);
    this.cursorNext(this.cursor_float1);
  }

  @Override
  public void onDataAttributeValueFloat2(
    final double x,
    final double y)
  {
    this.cursor_float2.getElementView().set2D(x, y);
    this.cursorNext(this.cursor_float2);
  }

  @Override
  public void onDataAttributeValueFloat3(
    final double x,
    final double y,
    final double z)
  {
    this.cursor_float3.getElementView().set3D(x, y, z);
    this.cursorNext(this.cursor_float3);
  }

  @Override
  public void onDataAttributeValueFloat4(
    final double x,
    final double y,
    final double z,
    final double w)
  {
    this.cursor_float4.getElementView().set4D(x, y, z, w);
    this.cursorNext(this.cursor_float4);
  }

  @Override
  public void onDataAttributeValueFinish()
  {

  }

  @Override
  public void onError(final SMFErrorType e)
  {
    this.errors.onError(e);
  }

  @Override
  public void onWarning(final SMFWarningType w)
  {
    this.errors.onWarning(w);
  }
}
