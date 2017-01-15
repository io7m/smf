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

import com.io7m.ieee754b16.Binary16;
import com.io7m.jintegers.Unsigned16;
import com.io7m.jintegers.Unsigned32;
import com.io7m.jintegers.Unsigned8;
import com.io7m.jnull.NullCheck;
import com.io7m.junreachable.UnreachableCodeException;
import com.io7m.smfj.core.SMFAttribute;
import com.io7m.smfj.core.SMFComponentType;
import com.io7m.smfj.parser.api.SMFParserEventsDataAttributesType;

import java.nio.ByteBuffer;

/**
 * An event listener that packs data into a given {@link ByteBuffer}.
 */

public final class SMFByteBufferAttributePacker implements
  SMFParserEventsDataAttributesType
{
  private final ByteBuffer attr_buffer;
  private final SMFComponentType type;
  private final int size;
  private final int stride;
  private int index;

  /**
   * Construct a packer.
   *
   * @param in_attribute_buffer The byte buffer that will contain attribute
   *                            data
   * @param in_packed_config    The packing configuration
   * @param in_packed_attribute The attribute to be packed
   */

  public SMFByteBufferAttributePacker(
    final ByteBuffer in_attribute_buffer,
    final SMFByteBufferPackingConfiguration in_packed_config,
    final SMFByteBufferPackedAttribute in_packed_attribute)
  {
    this.attr_buffer =
      NullCheck.notNull(in_attribute_buffer, "Attribute Buffer");

    final SMFAttribute attribute = in_packed_attribute.attribute();
    this.type = attribute.componentType();
    this.size = attribute.componentSizeBits();
    this.stride = in_packed_config.vertexSizeOctets();
  }

  private void next()
  {
    this.index = Math.addExact(this.index, this.stride);
  }

  @Override
  public void onDataAttributeStart(
    final SMFAttribute attribute)
  {

  }

  @Override
  public void onDataAttributeValueIntegerSigned1(
    final long x)
  {
    switch (this.type) {
      case ELEMENT_TYPE_INTEGER_SIGNED: {
        switch (this.size) {
          case 8: {
            final byte cx = (byte) x;
            this.attr_buffer.put(this.index, cx);
            break;
          }
          case 16: {
            final short cx = (short) x;
            this.attr_buffer.putShort(this.index, cx);
            break;
          }
          case 32: {
            final int cx = (int) x;
            this.attr_buffer.putInt(this.index, cx);
            break;
          }
          case 64: {
            this.attr_buffer.putLong(this.index, x);
            break;
          }
          default: {
            throw new UnreachableCodeException();
          }
        }
        break;
      }
      case ELEMENT_TYPE_INTEGER_UNSIGNED:
      case ELEMENT_TYPE_FLOATING:
        throw new UnreachableCodeException();
    }

    this.next();
  }

  @Override
  public void onDataAttributeValueIntegerSigned2(
    final long x,
    final long y)
  {
    switch (this.type) {
      case ELEMENT_TYPE_INTEGER_SIGNED: {
        switch (this.size) {
          case 8: {
            final byte cx = (byte) x;
            final byte cy = (byte) y;
            this.attr_buffer.put(this.index, cx);
            this.attr_buffer.put(this.index + 1, cy);
            break;
          }
          case 16: {
            final short cx = (short) x;
            final short cy = (short) y;
            this.attr_buffer.putShort(this.index, cx);
            this.attr_buffer.putShort(this.index + 2, cy);
            break;
          }
          case 32: {
            final int cx = (int) x;
            final int cy = (int) y;
            this.attr_buffer.putInt(this.index, cx);
            this.attr_buffer.putInt(this.index + 4, cy);
            break;
          }
          case 64: {
            this.attr_buffer.putLong(this.index, x);
            this.attr_buffer.putLong(this.index + 8, y);
            break;
          }
          default: {
            throw new UnreachableCodeException();
          }
        }

        break;
      }
      case ELEMENT_TYPE_INTEGER_UNSIGNED:
      case ELEMENT_TYPE_FLOATING:
        throw new UnreachableCodeException();
    }

    this.next();
  }

  @Override
  public void onDataAttributeValueIntegerSigned3(
    final long x,
    final long y,
    final long z)
  {
    switch (this.type) {
      case ELEMENT_TYPE_INTEGER_SIGNED: {
        switch (this.size) {
          case 8: {
            final byte cx = (byte) x;
            final byte cy = (byte) y;
            final byte cz = (byte) z;
            this.attr_buffer.put(this.index, cx);
            this.attr_buffer.put(this.index + 1, cy);
            this.attr_buffer.put(this.index + 2, cz);
            break;
          }
          case 16: {
            final short cx = (short) x;
            final short cy = (short) y;
            final short cz = (short) z;
            this.attr_buffer.putShort(this.index, cx);
            this.attr_buffer.putShort(this.index + 2, cy);
            this.attr_buffer.putShort(this.index + 4, cz);
            break;
          }
          case 32: {
            final int cx = (int) x;
            final int cy = (int) y;
            final int cz = (int) z;
            this.attr_buffer.putInt(this.index, cx);
            this.attr_buffer.putInt(this.index + 4, cy);
            this.attr_buffer.putInt(this.index + 8, cz);
            break;
          }
          case 64: {
            this.attr_buffer.putLong(this.index, x);
            this.attr_buffer.putLong(this.index + 8, y);
            this.attr_buffer.putLong(this.index + 16, z);
            break;
          }
          default: {
            throw new UnreachableCodeException();
          }
        }

        break;
      }
      case ELEMENT_TYPE_INTEGER_UNSIGNED:
      case ELEMENT_TYPE_FLOATING:
        throw new UnreachableCodeException();
    }

    this.next();
  }

  @Override
  public void onDataAttributeValueIntegerSigned4(
    final long x,
    final long y,
    final long z,
    final long w)
  {
    switch (this.type) {
      case ELEMENT_TYPE_INTEGER_SIGNED: {
        switch (this.size) {
          case 8: {
            final byte cx = (byte) x;
            final byte cy = (byte) y;
            final byte cz = (byte) z;
            final byte cw = (byte) w;
            this.attr_buffer.put(this.index, cx);
            this.attr_buffer.put(this.index + 1, cy);
            this.attr_buffer.put(this.index + 2, cz);
            this.attr_buffer.put(this.index + 3, cw);
            break;
          }
          case 16: {
            final short cx = (short) x;
            final short cy = (short) y;
            final short cz = (short) z;
            final short cw = (short) w;
            this.attr_buffer.putShort(this.index, cx);
            this.attr_buffer.putShort(this.index + 2, cy);
            this.attr_buffer.putShort(this.index + 4, cz);
            this.attr_buffer.putShort(this.index + 6, cw);
            break;
          }
          case 32: {
            final int cx = (int) x;
            final int cy = (int) y;
            final int cz = (int) z;
            final int cw = (int) w;
            this.attr_buffer.putInt(this.index, cx);
            this.attr_buffer.putInt(this.index + 4, cy);
            this.attr_buffer.putInt(this.index + 8, cz);
            this.attr_buffer.putInt(this.index + 12, cw);
            break;
          }
          case 64: {
            this.attr_buffer.putLong(this.index, x);
            this.attr_buffer.putLong(this.index + 8, y);
            this.attr_buffer.putLong(this.index + 16, z);
            this.attr_buffer.putLong(this.index + 24, w);
            break;
          }
          default: {
            throw new UnreachableCodeException();
          }
        }

        break;
      }
      case ELEMENT_TYPE_INTEGER_UNSIGNED:
      case ELEMENT_TYPE_FLOATING:
        throw new UnreachableCodeException();
    }

    this.next();
  }

  @Override
  public void onDataAttributeValueIntegerUnsigned1(
    final long x)
  {
    switch (this.type) {
      case ELEMENT_TYPE_INTEGER_UNSIGNED: {
        switch (this.size) {
          case 8: {
            Unsigned8.packToBuffer((int) x, this.attr_buffer, this.index);
            break;
          }
          case 16: {
            Unsigned16.packToBuffer((int) x, this.attr_buffer, this.index);
            break;
          }
          case 32: {
            Unsigned32.packToBuffer(x, this.attr_buffer, this.index);
            break;
          }
          case 64: {
            this.attr_buffer.putLong(this.index, x);
            break;
          }
          default: {
            throw new UnreachableCodeException();
          }
        }
        break;
      }
      case ELEMENT_TYPE_INTEGER_SIGNED:
      case ELEMENT_TYPE_FLOATING:
        throw new UnreachableCodeException();
    }

    this.next();
  }

  @Override
  public void onDataAttributeValueIntegerUnsigned2(
    final long x,
    final long y)
  {
    switch (this.type) {
      case ELEMENT_TYPE_INTEGER_UNSIGNED: {
        switch (this.size) {
          case 8: {
            Unsigned8.packToBuffer((int) x, this.attr_buffer, this.index);
            Unsigned8.packToBuffer((int) y, this.attr_buffer, this.index + 1);
            break;
          }
          case 16: {
            Unsigned16.packToBuffer((int) x, this.attr_buffer, this.index);
            Unsigned16.packToBuffer((int) y, this.attr_buffer, this.index + 2);
            break;
          }
          case 32: {
            Unsigned32.packToBuffer(x, this.attr_buffer, this.index);
            Unsigned32.packToBuffer(y, this.attr_buffer, this.index + 4);
            break;
          }
          case 64: {
            this.attr_buffer.putLong(this.index, x);
            this.attr_buffer.putLong(this.index + 8, y);
            break;
          }
          default: {
            throw new UnreachableCodeException();
          }
        }
        break;
      }
      case ELEMENT_TYPE_INTEGER_SIGNED:
      case ELEMENT_TYPE_FLOATING:
        throw new UnreachableCodeException();
    }

    this.next();
  }

  @Override
  public void onDataAttributeValueIntegerUnsigned3(
    final long x,
    final long y,
    final long z)
  {
    switch (this.type) {
      case ELEMENT_TYPE_INTEGER_UNSIGNED: {
        switch (this.size) {
          case 8: {
            Unsigned8.packToBuffer((int) x, this.attr_buffer, this.index);
            Unsigned8.packToBuffer((int) y, this.attr_buffer, this.index + 1);
            Unsigned8.packToBuffer((int) z, this.attr_buffer, this.index + 2);
            break;
          }
          case 16: {
            Unsigned16.packToBuffer((int) x, this.attr_buffer, this.index);
            Unsigned16.packToBuffer((int) y, this.attr_buffer, this.index + 2);
            Unsigned16.packToBuffer((int) z, this.attr_buffer, this.index + 4);
            break;
          }
          case 32: {
            Unsigned32.packToBuffer(x, this.attr_buffer, this.index);
            Unsigned32.packToBuffer(y, this.attr_buffer, this.index + 4);
            Unsigned32.packToBuffer(z, this.attr_buffer, this.index + 8);
            break;
          }
          case 64: {
            this.attr_buffer.putLong(this.index, x);
            this.attr_buffer.putLong(this.index + 8, y);
            this.attr_buffer.putLong(this.index + 16, z);
            break;
          }
          default: {
            throw new UnreachableCodeException();
          }
        }
        break;
      }
      case ELEMENT_TYPE_INTEGER_SIGNED:
      case ELEMENT_TYPE_FLOATING:
        throw new UnreachableCodeException();
    }

    this.next();
  }

  @Override
  public void onDataAttributeValueIntegerUnsigned4(
    final long x,
    final long y,
    final long z,
    final long w)
  {
    switch (this.type) {
      case ELEMENT_TYPE_INTEGER_UNSIGNED: {
        switch (this.size) {
          case 8: {
            Unsigned8.packToBuffer((int) x, this.attr_buffer, this.index);
            Unsigned8.packToBuffer((int) y, this.attr_buffer, this.index + 1);
            Unsigned8.packToBuffer((int) z, this.attr_buffer, this.index + 2);
            Unsigned8.packToBuffer((int) w, this.attr_buffer, this.index + 3);
            break;
          }
          case 16: {
            Unsigned16.packToBuffer((int) x, this.attr_buffer, this.index);
            Unsigned16.packToBuffer((int) y, this.attr_buffer, this.index + 2);
            Unsigned16.packToBuffer((int) z, this.attr_buffer, this.index + 4);
            Unsigned16.packToBuffer((int) w, this.attr_buffer, this.index + 6);
            break;
          }
          case 32: {
            Unsigned32.packToBuffer(x, this.attr_buffer, this.index);
            Unsigned32.packToBuffer(y, this.attr_buffer, this.index + 4);
            Unsigned32.packToBuffer(z, this.attr_buffer, this.index + 8);
            Unsigned32.packToBuffer(w, this.attr_buffer, this.index + 12);
            break;
          }
          case 64: {
            this.attr_buffer.putLong(this.index, x);
            this.attr_buffer.putLong(this.index + 8, y);
            this.attr_buffer.putLong(this.index + 16, z);
            this.attr_buffer.putLong(this.index + 24, w);
            break;
          }
          default: {
            throw new UnreachableCodeException();
          }
        }
        break;
      }
      case ELEMENT_TYPE_INTEGER_SIGNED:
      case ELEMENT_TYPE_FLOATING:
        throw new UnreachableCodeException();
    }

    this.next();
  }

  @Override
  public void onDataAttributeValueFloat1(
    final double x)
  {
    switch (this.type) {
      case ELEMENT_TYPE_INTEGER_UNSIGNED:
      case ELEMENT_TYPE_INTEGER_SIGNED: {
        throw new UnreachableCodeException();
      }
      case ELEMENT_TYPE_FLOATING: {
        switch (this.size) {
          case 64: {
            this.attr_buffer.putDouble(this.index, x);
            break;
          }
          case 32: {
            this.attr_buffer.putFloat(this.index, (float) x);
            break;
          }
          case 16: {
            this.attr_buffer.putChar(this.index, Binary16.packDouble(x));
            break;
          }
          default: {
            throw new UnreachableCodeException();
          }
        }
      }
    }

    this.next();
  }

  @Override
  public void onDataAttributeValueFloat2(
    final double x,
    final double y)
  {
    switch (this.type) {
      case ELEMENT_TYPE_INTEGER_UNSIGNED:
      case ELEMENT_TYPE_INTEGER_SIGNED: {
        throw new UnreachableCodeException();
      }
      case ELEMENT_TYPE_FLOATING: {
        switch (this.size) {
          case 64: {
            this.attr_buffer.putDouble(this.index, x);
            this.attr_buffer.putDouble(this.index + 8, y);
            break;
          }
          case 32: {
            this.attr_buffer.putFloat(this.index, (float) x);
            this.attr_buffer.putFloat(this.index + 4, (float) y);
            break;
          }
          case 16: {
            this.attr_buffer.putChar(this.index, Binary16.packDouble(x));
            this.attr_buffer.putChar(this.index + 2, Binary16.packDouble(y));
            break;
          }
          default: {
            throw new UnreachableCodeException();
          }
        }
      }
    }

    this.next();
  }

  @Override
  public void onDataAttributeValueFloat3(
    final double x,
    final double y,
    final double z)
  {
    switch (this.type) {
      case ELEMENT_TYPE_INTEGER_UNSIGNED:
      case ELEMENT_TYPE_INTEGER_SIGNED: {
        throw new UnreachableCodeException();
      }
      case ELEMENT_TYPE_FLOATING: {
        switch (this.size) {
          case 64: {
            this.attr_buffer.putDouble(this.index, x);
            this.attr_buffer.putDouble(this.index + 8, y);
            this.attr_buffer.putDouble(this.index + 16, z);
            break;
          }
          case 32: {
            this.attr_buffer.putFloat(this.index, (float) x);
            this.attr_buffer.putFloat(this.index + 4, (float) y);
            this.attr_buffer.putFloat(this.index + 8, (float) z);
            break;
          }
          case 16: {
            this.attr_buffer.putChar(this.index, Binary16.packDouble(x));
            this.attr_buffer.putChar(this.index + 2, Binary16.packDouble(y));
            this.attr_buffer.putChar(this.index + 4, Binary16.packDouble(z));
            break;
          }
          default: {
            throw new UnreachableCodeException();
          }
        }
      }
    }

    this.next();
  }

  @Override
  public void onDataAttributeValueFloat4(
    final double x,
    final double y,
    final double z,
    final double w)
  {
    switch (this.type) {
      case ELEMENT_TYPE_INTEGER_UNSIGNED:
      case ELEMENT_TYPE_INTEGER_SIGNED: {
        throw new UnreachableCodeException();
      }
      case ELEMENT_TYPE_FLOATING: {
        switch (this.size) {
          case 64: {
            this.attr_buffer.putDouble(this.index, x);
            this.attr_buffer.putDouble(this.index + 8, y);
            this.attr_buffer.putDouble(this.index + 16, z);
            this.attr_buffer.putDouble(this.index + 24, w);
            break;
          }
          case 32: {
            this.attr_buffer.putFloat(this.index, (float) x);
            this.attr_buffer.putFloat(this.index + 4, (float) y);
            this.attr_buffer.putFloat(this.index + 8, (float) z);
            this.attr_buffer.putFloat(this.index + 12, (float) w);
            break;
          }
          case 16: {
            this.attr_buffer.putChar(this.index, Binary16.packDouble(x));
            this.attr_buffer.putChar(this.index + 2, Binary16.packDouble(y));
            this.attr_buffer.putChar(this.index + 4, Binary16.packDouble(z));
            this.attr_buffer.putChar(this.index + 6, Binary16.packDouble(w));
            break;
          }
          default: {
            throw new UnreachableCodeException();
          }
        }
      }
    }

    this.next();
  }

  @Override
  public void onDataAttributeFinish(
    final SMFAttribute attribute)
  {

  }
}
