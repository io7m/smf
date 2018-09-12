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

import com.io7m.jaffirm.core.Preconditions;
import com.io7m.jpra.runtime.java.JPRACursor1DByteBufferedChecked;
import com.io7m.jpra.runtime.java.JPRACursor1DType;
import com.io7m.junreachable.UnreachableCodeException;
import com.io7m.smfj.core.SMFAttribute;
import com.io7m.smfj.core.SMFComponentType;

import java.nio.ByteBuffer;

import static com.io7m.smfj.bytebuffer.SMFByteBufferCursorsFloat.Float1b16;
import static com.io7m.smfj.bytebuffer.SMFByteBufferCursorsFloat.Float1b32;
import static com.io7m.smfj.bytebuffer.SMFByteBufferCursorsFloat.Float1b64;
import static com.io7m.smfj.bytebuffer.SMFByteBufferCursorsFloat.Float2b16;
import static com.io7m.smfj.bytebuffer.SMFByteBufferCursorsFloat.Float2b32;
import static com.io7m.smfj.bytebuffer.SMFByteBufferCursorsFloat.Float2b64;
import static com.io7m.smfj.bytebuffer.SMFByteBufferCursorsFloat.Float3b16;
import static com.io7m.smfj.bytebuffer.SMFByteBufferCursorsFloat.Float3b32;
import static com.io7m.smfj.bytebuffer.SMFByteBufferCursorsFloat.Float3b64;
import static com.io7m.smfj.bytebuffer.SMFByteBufferCursorsFloat.Float4b16;
import static com.io7m.smfj.bytebuffer.SMFByteBufferCursorsFloat.Float4b32;
import static com.io7m.smfj.bytebuffer.SMFByteBufferCursorsFloat.Float4b64;
import static com.io7m.smfj.bytebuffer.SMFByteBufferCursorsSigned.Signed1b16;
import static com.io7m.smfj.bytebuffer.SMFByteBufferCursorsSigned.Signed1b32;
import static com.io7m.smfj.bytebuffer.SMFByteBufferCursorsSigned.Signed1b64;
import static com.io7m.smfj.bytebuffer.SMFByteBufferCursorsSigned.Signed1b8;
import static com.io7m.smfj.bytebuffer.SMFByteBufferCursorsSigned.Signed2b16;
import static com.io7m.smfj.bytebuffer.SMFByteBufferCursorsSigned.Signed2b32;
import static com.io7m.smfj.bytebuffer.SMFByteBufferCursorsSigned.Signed2b64;
import static com.io7m.smfj.bytebuffer.SMFByteBufferCursorsSigned.Signed2b8;
import static com.io7m.smfj.bytebuffer.SMFByteBufferCursorsSigned.Signed3b16;
import static com.io7m.smfj.bytebuffer.SMFByteBufferCursorsSigned.Signed3b32;
import static com.io7m.smfj.bytebuffer.SMFByteBufferCursorsSigned.Signed3b64;
import static com.io7m.smfj.bytebuffer.SMFByteBufferCursorsSigned.Signed3b8;
import static com.io7m.smfj.bytebuffer.SMFByteBufferCursorsSigned.Signed4b16;
import static com.io7m.smfj.bytebuffer.SMFByteBufferCursorsSigned.Signed4b32;
import static com.io7m.smfj.bytebuffer.SMFByteBufferCursorsSigned.Signed4b64;
import static com.io7m.smfj.bytebuffer.SMFByteBufferCursorsSigned.Signed4b8;
import static com.io7m.smfj.bytebuffer.SMFByteBufferCursorsUnsigned.Unsigned1b16;
import static com.io7m.smfj.bytebuffer.SMFByteBufferCursorsUnsigned.Unsigned1b32;
import static com.io7m.smfj.bytebuffer.SMFByteBufferCursorsUnsigned.Unsigned1b64;
import static com.io7m.smfj.bytebuffer.SMFByteBufferCursorsUnsigned.Unsigned1b8;
import static com.io7m.smfj.bytebuffer.SMFByteBufferCursorsUnsigned.Unsigned2b16;
import static com.io7m.smfj.bytebuffer.SMFByteBufferCursorsUnsigned.Unsigned2b32;
import static com.io7m.smfj.bytebuffer.SMFByteBufferCursorsUnsigned.Unsigned2b64;
import static com.io7m.smfj.bytebuffer.SMFByteBufferCursorsUnsigned.Unsigned2b8;
import static com.io7m.smfj.bytebuffer.SMFByteBufferCursorsUnsigned.Unsigned3b16;
import static com.io7m.smfj.bytebuffer.SMFByteBufferCursorsUnsigned.Unsigned3b32;
import static com.io7m.smfj.bytebuffer.SMFByteBufferCursorsUnsigned.Unsigned3b64;
import static com.io7m.smfj.bytebuffer.SMFByteBufferCursorsUnsigned.Unsigned3b8;
import static com.io7m.smfj.bytebuffer.SMFByteBufferCursorsUnsigned.Unsigned4b16;
import static com.io7m.smfj.bytebuffer.SMFByteBufferCursorsUnsigned.Unsigned4b32;
import static com.io7m.smfj.bytebuffer.SMFByteBufferCursorsUnsigned.Unsigned4b64;
import static com.io7m.smfj.bytebuffer.SMFByteBufferCursorsUnsigned.Unsigned4b8;

/**
 * Cursors for reading/writing byte buffers.
 */

// Unavoidable class data abstraction problem: Too many references to other classes.
// CHECKSTYLE:OFF
public final class SMFByteBufferCursors
  // CHECKSTYLE:ON
{
  private SMFByteBufferCursors()
  {
    throw new UnreachableCodeException();
  }

  /**
   * Create a floating point cursor.
   *
   * @param config      The byte buffer packing configuration
   * @param packed_attr An attribute from the packing configuration
   * @param buffer      A byte buffer
   *
   * @return A new cursor
   */

  public static JPRACursor1DType<SMFByteBufferFloat4Type>
  createFloat4(
    final SMFByteBufferPackingConfiguration config,
    final SMFByteBufferPackedAttribute packed_attr,
    final ByteBuffer buffer)
  {
    final SMFAttribute attr = packed_attr.attribute();
    Preconditions.checkPrecondition(
      packed_attr,
      config.packedAttributesByOrder().contains(packed_attr),
      a -> "Packed attribute must exist in configuration");
    Preconditions.checkPreconditionI(
      attr.componentCount(),
      attr.componentCount() == 4,
      n -> "Attribute must have 4 components");
    Preconditions.checkPrecondition(
      attr.componentType(),
      attr.componentType() == SMFComponentType.ELEMENT_TYPE_FLOATING,
      n -> "Attribute must be of a floating point type");

    return createFloat4Raw(
      buffer,
      packed_attr.attribute().componentSizeBits(),
      packed_attr.offsetOctets(),
      config.vertexSizeOctets());
  }

  /**
   * Create a floating point cursor.
   *
   * @param buffer         The byte buffer
   * @param component_size The component size in bits
   * @param offset         The offset of each element
   * @param vertex_size    The size in octets of a single vertex
   *
   * @return A new cursor
   */

  public static JPRACursor1DType<SMFByteBufferFloat4Type> createFloat4Raw(
    final ByteBuffer buffer,
    final int component_size,
    final int offset,
    final int vertex_size)
  {
    return JPRACursor1DByteBufferedChecked.newCursor(
      buffer, (b, c, o) -> {
        switch (component_size) {
          case 16:
            return new Float4b16(b, c, vertex_size, offset);
          case 32:
            return new Float4b32(b, c, vertex_size, offset);
          case 64:
            return new Float4b64(b, c, vertex_size, offset);
          default:
            throw new UnreachableCodeException();
        }
      });
  }

  /**
   * Create a floating point cursor.
   *
   * @param config      The byte buffer packing configuration
   * @param packed_attr An attribute from the packing configuration
   * @param buffer      A byte buffer
   *
   * @return A new cursor
   */

  public static JPRACursor1DType<SMFByteBufferFloat3Type>
  createFloat3(
    final SMFByteBufferPackingConfiguration config,
    final SMFByteBufferPackedAttribute packed_attr,
    final ByteBuffer buffer)
  {
    final SMFAttribute attr = packed_attr.attribute();
    Preconditions.checkPrecondition(
      packed_attr,
      config.packedAttributesByOrder().contains(packed_attr),
      a -> "Packed attribute must exist in configuration");
    Preconditions.checkPreconditionI(
      attr.componentCount(),
      attr.componentCount() == 3,
      n -> "Attribute must have 3 components");
    Preconditions.checkPrecondition(
      attr.componentType(),
      attr.componentType() == SMFComponentType.ELEMENT_TYPE_FLOATING,
      n -> "Attribute must be of a floating point type");

    return createFloat3Raw(
      buffer,
      packed_attr.attribute().componentSizeBits(),
      packed_attr.offsetOctets(),
      config.vertexSizeOctets());
  }

  /**
   * Create a floating point cursor.
   *
   * @param buffer         The byte buffer
   * @param component_size The component size in bits
   * @param offset         The offset of each element
   * @param vertex_size    The size in octets of a single vertex
   *
   * @return A new cursor
   */

  public static JPRACursor1DType<SMFByteBufferFloat3Type> createFloat3Raw(
    final ByteBuffer buffer,
    final int component_size,
    final int offset,
    final int vertex_size)
  {
    return JPRACursor1DByteBufferedChecked.newCursor(
      buffer, (b, c, o) -> {
        switch (component_size) {
          case 16:
            return new Float3b16(b, c, vertex_size, offset);
          case 32:
            return new Float3b32(b, c, vertex_size, offset);
          case 64:
            return new Float3b64(b, c, vertex_size, offset);
          default:
            throw new UnreachableCodeException();
        }
      });
  }

  /**
   * Create a floating point cursor.
   *
   * @param config      The byte buffer packing configuration
   * @param packed_attr An attribute from the packing configuration
   * @param buffer      A byte buffer
   *
   * @return A new cursor
   */

  public static JPRACursor1DType<SMFByteBufferFloat2Type>
  createFloat2(
    final SMFByteBufferPackingConfiguration config,
    final SMFByteBufferPackedAttribute packed_attr,
    final ByteBuffer buffer)
  {
    final SMFAttribute attr = packed_attr.attribute();
    Preconditions.checkPrecondition(
      packed_attr,
      config.packedAttributesByOrder().contains(packed_attr),
      a -> "Packed attribute must exist in configuration");
    Preconditions.checkPreconditionI(
      attr.componentCount(),
      attr.componentCount() == 2,
      n -> "Attribute must have 2 components");
    Preconditions.checkPrecondition(
      attr.componentType(),
      attr.componentType() == SMFComponentType.ELEMENT_TYPE_FLOATING,
      n -> "Attribute must be of a floating point type");

    return createFloat2Raw(
      buffer,
      packed_attr.attribute().componentSizeBits(),
      packed_attr.offsetOctets(),
      config.vertexSizeOctets());
  }

  /**
   * Create a floating point cursor.
   *
   * @param buffer         The byte buffer
   * @param component_size The component size in bits
   * @param offset         The offset of each element
   * @param vertex_size    The size in octets of a single vertex
   *
   * @return A new cursor
   */

  public static JPRACursor1DType<SMFByteBufferFloat2Type> createFloat2Raw(
    final ByteBuffer buffer,
    final int component_size,
    final int offset,
    final int vertex_size)
  {
    return JPRACursor1DByteBufferedChecked.newCursor(
      buffer, (b, c, o) -> {
        switch (component_size) {
          case 16:
            return new Float2b16(b, c, vertex_size, offset);
          case 32:
            return new Float2b32(b, c, vertex_size, offset);
          case 64:
            return new Float2b64(b, c, vertex_size, offset);
          default:
            throw new UnreachableCodeException();
        }
      });
  }

  /**
   * Create a floating point cursor.
   *
   * @param config      The byte buffer packing configuration
   * @param packed_attr An attribute from the packing configuration
   * @param buffer      A byte buffer
   *
   * @return A new cursor
   */

  public static JPRACursor1DType<SMFByteBufferFloat1Type>
  createFloat1(
    final SMFByteBufferPackingConfiguration config,
    final SMFByteBufferPackedAttribute packed_attr,
    final ByteBuffer buffer)
  {
    final SMFAttribute attr = packed_attr.attribute();
    Preconditions.checkPrecondition(
      packed_attr,
      config.packedAttributesByOrder().contains(packed_attr),
      a -> "Packed attribute must exist in configuration");
    Preconditions.checkPreconditionI(
      attr.componentCount(),
      attr.componentCount() == 1,
      n -> "Attribute must have 1 components");
    Preconditions.checkPrecondition(
      attr.componentType(),
      attr.componentType() == SMFComponentType.ELEMENT_TYPE_FLOATING,
      n -> "Attribute must be of a floating point type");

    return createFloat1Raw(
      buffer,
      packed_attr.attribute().componentSizeBits(),
      packed_attr.offsetOctets(),
      config.vertexSizeOctets());
  }

  /**
   * Create a floating point cursor.
   *
   * @param buffer         The byte buffer
   * @param component_size The component size in bits
   * @param offset         The offset of each element
   * @param vertex_size    The size in octets of a single vertex
   *
   * @return A new cursor
   */

  public static JPRACursor1DType<SMFByteBufferFloat1Type> createFloat1Raw(
    final ByteBuffer buffer,
    final int component_size,
    final int offset,
    final int vertex_size)
  {
    return JPRACursor1DByteBufferedChecked.newCursor(
      buffer, (b, c, o) -> {
        switch (component_size) {
          case 16:
            return new Float1b16(b, c, vertex_size, offset);
          case 32:
            return new Float1b32(b, c, vertex_size, offset);
          case 64:
            return new Float1b64(b, c, vertex_size, offset);
          default:
            throw new UnreachableCodeException();
        }
      });
  }

  /**
   * Create a signed integer cursor.
   *
   * @param config      The byte buffer packing configuration
   * @param packed_attr An attribute from the packing configuration
   * @param buffer      A byte buffer
   *
   * @return A new cursor
   */

  public static JPRACursor1DType<SMFByteBufferIntegerSigned4Type>
  createSigned4(
    final SMFByteBufferPackingConfiguration config,
    final SMFByteBufferPackedAttribute packed_attr,
    final ByteBuffer buffer)
  {
    final SMFAttribute attr = packed_attr.attribute();
    Preconditions.checkPrecondition(
      packed_attr,
      config.packedAttributesByOrder().contains(packed_attr),
      a -> "Packed attribute must exist in configuration");
    Preconditions.checkPreconditionI(
      attr.componentCount(),
      attr.componentCount() == 4,
      n -> "Attribute must have 4 components");
    Preconditions.checkPrecondition(
      attr.componentType(),
      attr.componentType() == SMFComponentType.ELEMENT_TYPE_INTEGER_SIGNED,
      n -> "Attribute must be of a signed integer type");

    return createSigned4Raw(
      buffer,
      packed_attr.attribute().componentSizeBits(),
      packed_attr.offsetOctets(),
      config.vertexSizeOctets());
  }

  /**
   * Create a signed integer cursor.
   *
   * @param buffer         The byte buffer
   * @param component_size The component size in bits
   * @param offset         The offset of each element
   * @param vertex_size    The size in octets of a single vertex
   *
   * @return A new cursor
   */

  public static JPRACursor1DType<SMFByteBufferIntegerSigned4Type>
  createSigned4Raw(
    final ByteBuffer buffer,
    final int component_size,
    final int offset,
    final int vertex_size)
  {
    return JPRACursor1DByteBufferedChecked.newCursor(
      buffer, (b, c, o) -> {
        switch (component_size) {
          case 64:
            return new Signed4b64(b, c, vertex_size, offset);
          case 32:
            return new Signed4b32(b, c, vertex_size, offset);
          case 16:
            return new Signed4b16(b, c, vertex_size, offset);
          case 8:
            return new Signed4b8(b, c, vertex_size, offset);
          default:
            throw new UnreachableCodeException();
        }
      });
  }

  /**
   * Create a signed integer cursor.
   *
   * @param config      The byte buffer packing configuration
   * @param packed_attr An attribute from the packing configuration
   * @param buffer      A byte buffer
   *
   * @return A new cursor
   */

  public static JPRACursor1DType<SMFByteBufferIntegerSigned3Type>
  createSigned3(
    final SMFByteBufferPackingConfiguration config,
    final SMFByteBufferPackedAttribute packed_attr,
    final ByteBuffer buffer)
  {
    final SMFAttribute attr = packed_attr.attribute();
    Preconditions.checkPrecondition(
      packed_attr,
      config.packedAttributesByOrder().contains(packed_attr),
      a -> "Packed attribute must exist in configuration");
    Preconditions.checkPreconditionI(
      attr.componentCount(),
      attr.componentCount() == 3,
      n -> "Attribute must have 3 components");
    Preconditions.checkPrecondition(
      attr.componentType(),
      attr.componentType() == SMFComponentType.ELEMENT_TYPE_INTEGER_SIGNED,
      n -> "Attribute must be of a signed integer type");

    return createSigned3Raw(
      buffer,
      packed_attr.attribute().componentSizeBits(),
      packed_attr.offsetOctets(),
      config.vertexSizeOctets());
  }

  /**
   * Create a signed integer cursor.
   *
   * @param buffer         The byte buffer
   * @param component_size The component size in bits
   * @param offset         The offset of each element
   * @param vertex_size    The size in octets of a single vertex
   *
   * @return A new cursor
   */

  public static JPRACursor1DType<SMFByteBufferIntegerSigned3Type> createSigned3Raw(
    final ByteBuffer buffer,
    final int component_size,
    final int offset,
    final int vertex_size)
  {
    return JPRACursor1DByteBufferedChecked.newCursor(
      buffer, (b, c, o) -> {
        switch (component_size) {
          case 64:
            return new Signed3b64(b, c, vertex_size, offset);
          case 32:
            return new Signed3b32(b, c, vertex_size, offset);
          case 16:
            return new Signed3b16(b, c, vertex_size, offset);
          case 8:
            return new Signed3b8(b, c, vertex_size, offset);
          default:
            throw new UnreachableCodeException();
        }
      });
  }

  /**
   * Create a signed integer cursor.
   *
   * @param config      The byte buffer packing configuration
   * @param packed_attr An attribute from the packing configuration
   * @param buffer      A byte buffer
   *
   * @return A new cursor
   */

  public static JPRACursor1DType<SMFByteBufferIntegerSigned2Type>
  createSigned2(
    final SMFByteBufferPackingConfiguration config,
    final SMFByteBufferPackedAttribute packed_attr,
    final ByteBuffer buffer)
  {
    final SMFAttribute attr = packed_attr.attribute();
    Preconditions.checkPrecondition(
      packed_attr,
      config.packedAttributesByOrder().contains(packed_attr),
      a -> "Packed attribute must exist in configuration");
    Preconditions.checkPreconditionI(
      attr.componentCount(),
      attr.componentCount() == 2,
      n -> "Attribute must have 2 components");
    Preconditions.checkPrecondition(
      attr.componentType(),
      attr.componentType() == SMFComponentType.ELEMENT_TYPE_INTEGER_SIGNED,
      n -> "Attribute must be of a signed integer type");

    return createSigned2Raw(
      buffer,
      packed_attr.attribute().componentSizeBits(),
      packed_attr.offsetOctets(),
      config.vertexSizeOctets());
  }

  /**
   * Create a signed integer cursor.
   *
   * @param buffer         The byte buffer
   * @param component_size The component size in bits
   * @param offset         The offset of each element
   * @param vertex_size    The size in octets of a single vertex
   *
   * @return A new cursor
   */

  public static JPRACursor1DType<SMFByteBufferIntegerSigned2Type>
  createSigned2Raw(
    final ByteBuffer buffer,
    final int component_size,
    final int offset,
    final int vertex_size)
  {
    return JPRACursor1DByteBufferedChecked.newCursor(
      buffer, (b, c, o) -> {
        switch (component_size) {
          case 64:
            return new Signed2b64(b, c, vertex_size, offset);
          case 32:
            return new Signed2b32(b, c, vertex_size, offset);
          case 16:
            return new Signed2b16(b, c, vertex_size, offset);
          case 8:
            return new Signed2b8(b, c, vertex_size, offset);
          default:
            throw new UnreachableCodeException();
        }
      });
  }

  /**
   * Create a signed integer cursor.
   *
   * @param config      The byte buffer packing configuration
   * @param packed_attr An attribute from the packing configuration
   * @param buffer      A byte buffer
   *
   * @return A new cursor
   */

  public static JPRACursor1DType<SMFByteBufferIntegerSigned1Type>
  createSigned1(
    final SMFByteBufferPackingConfiguration config,
    final SMFByteBufferPackedAttribute packed_attr,
    final ByteBuffer buffer)
  {
    final SMFAttribute attr = packed_attr.attribute();
    Preconditions.checkPrecondition(
      packed_attr,
      config.packedAttributesByOrder().contains(packed_attr),
      a -> "Packed attribute must exist in configuration");
    Preconditions.checkPreconditionI(
      attr.componentCount(),
      attr.componentCount() == 1,
      n -> "Attribute must have 1 components");
    Preconditions.checkPrecondition(
      attr.componentType(),
      attr.componentType() == SMFComponentType.ELEMENT_TYPE_INTEGER_SIGNED,
      n -> "Attribute must be of a signed integer type");

    return createSigned1Raw(
      buffer,
      packed_attr.attribute().componentSizeBits(),
      packed_attr.offsetOctets(),
      config.vertexSizeOctets());
  }

  /**
   * Create a signed integer cursor.
   *
   * @param buffer         The byte buffer
   * @param component_size The component size in bits
   * @param offset         The offset of each element
   * @param vertex_size    The size in octets of a single vertex
   *
   * @return A new cursor
   */

  public static JPRACursor1DType<SMFByteBufferIntegerSigned1Type>
  createSigned1Raw(
    final ByteBuffer buffer,
    final int component_size,
    final int offset,
    final int vertex_size)
  {
    return JPRACursor1DByteBufferedChecked.newCursor(
      buffer, (b, c, o) -> {
        switch (component_size) {
          case 64:
            return new Signed1b64(b, c, vertex_size, offset);
          case 32:
            return new Signed1b32(b, c, vertex_size, offset);
          case 16:
            return new Signed1b16(b, c, vertex_size, offset);
          case 8:
            return new Signed1b8(b, c, vertex_size, offset);
          default:
            throw new UnreachableCodeException();
        }
      });
  }

  /**
   * Create an unsigned integer cursor.
   *
   * @param config      The byte buffer packing configuration
   * @param packed_attr An attribute from the packing configuration
   * @param buffer      A byte buffer
   *
   * @return A new cursor
   */

  public static JPRACursor1DType<SMFByteBufferIntegerUnsigned4Type>
  createUnsigned4(
    final SMFByteBufferPackingConfiguration config,
    final SMFByteBufferPackedAttribute packed_attr,
    final ByteBuffer buffer)
  {
    final SMFAttribute attr = packed_attr.attribute();
    Preconditions.checkPrecondition(
      packed_attr,
      config.packedAttributesByOrder().contains(packed_attr),
      a -> "Packed attribute must exist in configuration");
    Preconditions.checkPreconditionI(
      attr.componentCount(),
      attr.componentCount() == 4,
      n -> "Attribute must have 4 components");
    Preconditions.checkPrecondition(
      attr.componentType(),
      attr.componentType() == SMFComponentType.ELEMENT_TYPE_INTEGER_UNSIGNED,
      n -> "Attribute must be of an unsigned integer type");

    return createUnsigned4Raw(
      buffer,
      packed_attr.attribute().componentSizeBits(),
      packed_attr.offsetOctets(),
      config.vertexSizeOctets());
  }

  /**
   * Create an unsigned integer cursor.
   *
   * @param buffer         The byte buffer
   * @param component_size The component size in bits
   * @param offset         The offset of each element
   * @param vertex_size    The size in octets of a single vertex
   *
   * @return A new cursor
   */

  public static JPRACursor1DType<SMFByteBufferIntegerUnsigned4Type>
  createUnsigned4Raw(
    final ByteBuffer buffer,
    final int component_size,
    final int offset,
    final int vertex_size)
  {
    return JPRACursor1DByteBufferedChecked.newCursor(
      buffer, (b, c, o) -> {
        switch (component_size) {
          case 64:
            return new Unsigned4b64(b, c, vertex_size, offset);
          case 32:
            return new Unsigned4b32(b, c, vertex_size, offset);
          case 16:
            return new Unsigned4b16(b, c, vertex_size, offset);
          case 8:
            return new Unsigned4b8(b, c, vertex_size, offset);
          default:
            throw new UnreachableCodeException();
        }
      });
  }

  /**
   * Create an unsigned integer cursor.
   *
   * @param config      The byte buffer packing configuration
   * @param packed_attr An attribute from the packing configuration
   * @param buffer      A byte buffer
   *
   * @return A new cursor
   */

  public static JPRACursor1DType<SMFByteBufferIntegerUnsigned3Type>
  createUnsigned3(
    final SMFByteBufferPackingConfiguration config,
    final SMFByteBufferPackedAttribute packed_attr,
    final ByteBuffer buffer)
  {
    final SMFAttribute attr = packed_attr.attribute();
    Preconditions.checkPrecondition(
      packed_attr,
      config.packedAttributesByOrder().contains(packed_attr),
      a -> "Packed attribute must exist in configuration");
    Preconditions.checkPreconditionI(
      attr.componentCount(),
      attr.componentCount() == 3,
      n -> "Attribute must have 3 components");
    Preconditions.checkPrecondition(
      attr.componentType(),
      attr.componentType() == SMFComponentType.ELEMENT_TYPE_INTEGER_UNSIGNED,
      n -> "Attribute must be of an unsigned integer type");

    final int size = packed_attr.attribute().componentSizeBits();
    final int offset = packed_attr.offsetOctets();
    final int vertex_size = config.vertexSizeOctets();

    return createUnsigned3Raw(buffer, size, offset, vertex_size);
  }

  /**
   * Create an unsigned integer cursor.
   *
   * @param buffer         The byte buffer
   * @param component_size The component size in bits
   * @param offset         The offset of each element
   * @param vertex_size    The size in octets of a single vertex
   *
   * @return A new cursor
   */

  public static JPRACursor1DType<SMFByteBufferIntegerUnsigned3Type>
  createUnsigned3Raw(
    final ByteBuffer buffer,
    final int component_size,
    final int offset,
    final int vertex_size)
  {
    return JPRACursor1DByteBufferedChecked.newCursor(
      buffer, (b, c, o) -> {
        switch (component_size) {
          case 64:
            return new Unsigned3b64(b, c, vertex_size, offset);
          case 32:
            return new Unsigned3b32(b, c, vertex_size, offset);
          case 16:
            return new Unsigned3b16(b, c, vertex_size, offset);
          case 8:
            return new Unsigned3b8(b, c, vertex_size, offset);
          default:
            throw new UnreachableCodeException();
        }
      });
  }

  /**
   * Create an unsigned integer cursor.
   *
   * @param config      The byte buffer packing configuration
   * @param packed_attr An attribute from the packing configuration
   * @param buffer      A byte buffer
   *
   * @return A new cursor
   */

  public static JPRACursor1DType<SMFByteBufferIntegerUnsigned2Type>
  createUnsigned2(
    final SMFByteBufferPackingConfiguration config,
    final SMFByteBufferPackedAttribute packed_attr,
    final ByteBuffer buffer)
  {
    final SMFAttribute attr = packed_attr.attribute();
    Preconditions.checkPrecondition(
      packed_attr,
      config.packedAttributesByOrder().contains(packed_attr),
      a -> "Packed attribute must exist in configuration");
    Preconditions.checkPreconditionI(
      attr.componentCount(),
      attr.componentCount() == 2,
      n -> "Attribute must have 2 components");
    Preconditions.checkPrecondition(
      attr.componentType(),
      attr.componentType() == SMFComponentType.ELEMENT_TYPE_INTEGER_UNSIGNED,
      n -> "Attribute must be of an unsigned integer type");

    return createUnsigned2Raw(
      buffer,
      packed_attr.attribute().componentSizeBits(),
      packed_attr.offsetOctets(),
      config.vertexSizeOctets());
  }

  /**
   * Create an unsigned integer cursor.
   *
   * @param buffer         The byte buffer
   * @param component_size The component size in bits
   * @param offset         The offset of each element
   * @param vertex_size    The size in octets of a single vertex
   *
   * @return A new cursor
   */

  public static JPRACursor1DType<SMFByteBufferIntegerUnsigned2Type>
  createUnsigned2Raw(
    final ByteBuffer buffer,
    final int component_size,
    final int offset,
    final int vertex_size)
  {
    return JPRACursor1DByteBufferedChecked.newCursor(
      buffer, (b, c, o) -> {
        switch (component_size) {
          case 64:
            return new Unsigned2b64(b, c, vertex_size, offset);
          case 32:
            return new Unsigned2b32(b, c, vertex_size, offset);
          case 16:
            return new Unsigned2b16(b, c, vertex_size, offset);
          case 8:
            return new Unsigned2b8(b, c, vertex_size, offset);
          default:
            throw new UnreachableCodeException();
        }
      });
  }

  /**
   * Create an unsigned integer cursor.
   *
   * @param config      The byte buffer packing configuration
   * @param packed_attr An attribute from the packing configuration
   * @param buffer      A byte buffer
   *
   * @return A new cursor
   */

  public static JPRACursor1DType<SMFByteBufferIntegerUnsigned1Type>
  createUnsigned1(
    final SMFByteBufferPackingConfiguration config,
    final SMFByteBufferPackedAttribute packed_attr,
    final ByteBuffer buffer)
  {
    final SMFAttribute attr = packed_attr.attribute();
    Preconditions.checkPrecondition(
      packed_attr,
      config.packedAttributesByOrder().contains(packed_attr),
      a -> "Packed attribute must exist in configuration");
    Preconditions.checkPreconditionI(
      attr.componentCount(),
      attr.componentCount() == 1,
      n -> "Attribute must have 1 component");
    Preconditions.checkPrecondition(
      attr.componentType(),
      attr.componentType() == SMFComponentType.ELEMENT_TYPE_INTEGER_UNSIGNED,
      n -> "Attribute must be of an unsigned integer type");

    final int size = packed_attr.attribute().componentSizeBits();
    final int offset = packed_attr.offsetOctets();
    final int vsize = config.vertexSizeOctets();
    return createUnsigned1Raw(buffer, size, offset, vsize);
  }

  /**
   * Create an unsigned integer cursor.
   *
   * @param buffer         The byte buffer
   * @param component_size The component size in bits
   * @param offset         The offset of each element
   * @param vertex_size    The size in octets of a single vertex
   *
   * @return A new cursor
   */

  public static JPRACursor1DType<SMFByteBufferIntegerUnsigned1Type>
  createUnsigned1Raw(
    final ByteBuffer buffer,
    final int component_size,
    final int offset,
    final int vertex_size)
  {
    return JPRACursor1DByteBufferedChecked.newCursor(
      buffer, (b, c, o) -> {
        switch (component_size) {
          case 64:
            return new Unsigned1b64(b, c, vertex_size, offset);
          case 32:
            return new Unsigned1b32(b, c, vertex_size, offset);
          case 16:
            return new Unsigned1b16(b, c, vertex_size, offset);
          case 8:
            return new Unsigned1b8(b, c, vertex_size, offset);
          default:
            throw new UnreachableCodeException();
        }
      });
  }
}
