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

package com.io7m.smfj.format.binary;

import com.io7m.jaffirm.core.Invariants;
import com.io7m.jaffirm.core.Preconditions;
import com.io7m.jnull.NullCheck;
import com.io7m.jpra.runtime.java.JPRACursor1DByteBufferedChecked;
import com.io7m.jpra.runtime.java.JPRACursor1DType;
import com.io7m.jpra.runtime.java.JPRAStringCursorType;
import com.io7m.jpra.runtime.java.JPRAStringTruncation;
import com.io7m.junreachable.UnreachableCodeException;
import com.io7m.smfj.core.SMFAttribute;
import com.io7m.smfj.core.SMFAttributeName;
import com.io7m.smfj.core.SMFComponentType;
import com.io7m.smfj.core.SMFFormatVersion;
import com.io7m.smfj.core.SMFHeader;
import com.io7m.smfj.core.SMFVendorSchemaIdentifier;
import com.io7m.smfj.format.binary.v1.SMFBV1AttributeByteBuffered;
import com.io7m.smfj.format.binary.v1.SMFBV1AttributeType;
import com.io7m.smfj.serializer.api.SMFSerializerType;
import javaslang.collection.List;
import javaslang.collection.Queue;
import javaslang.control.Option;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.file.Path;
import java.util.Arrays;

final class SMFBV1Serializer implements SMFSerializerType
{
  private static final Logger LOG;

  static {
    LOG = LoggerFactory.getLogger(SMFBV1Serializer.class);
  }

  private final SMFFormatVersion version;
  private final SMFBDataStreamWriterType writer;
  private final byte[] attribute_bytes;
  private final ByteBuffer attribute_buffer;
  private SerializerState state;
  private SMFHeader header;
  private Queue<SMFAttribute> attribute_queue;
  private long attribute_values_remaining;
  private SMFAttribute attribute_current;
  private long triangle_values_remaining;
  private SMFBV1Offsets offsets;

  SMFBV1Serializer(
    final SMFFormatVersion in_version,
    final Path in_path,
    final OutputStream in_stream)
  {
    this.version = NullCheck.notNull(in_version, "Version");
    Preconditions.checkPreconditionI(
      in_version.major(),
      in_version.major() == 1,
      v -> "Major version " + v + " must be 1");

    this.writer = SMFBDataStreamWriter.create(in_path, in_stream);
    this.attribute_bytes = new byte[SMFBV1AttributeByteBuffered.sizeInOctets()];
    this.attribute_buffer = ByteBuffer.wrap(this.attribute_bytes);
    this.attribute_buffer.order(ByteOrder.BIG_ENDIAN);
    this.state = SerializerState.STATE_INITIAL;
  }

  @Override
  public void serializeHeader(
    final SMFHeader in_header)
  {
    NullCheck.notNull(in_header, "Header");

    try {
      switch (this.state) {
        case STATE_INITIAL: {
          final List<SMFAttribute> attributes = in_header.attributesInOrder();
          final long attribute_count = (long) attributes.length();

          this.writer.putBytes(SMFFormatBinary.magicNumber());
          this.writer.putU32((long) this.version.major());
          this.writer.putU32((long) this.version.minor());

          final SMFVendorSchemaIdentifier schema_id =
            in_header.schemaIdentifier();
          this.writer.putU32((long) schema_id.vendorID());
          this.writer.putU32((long) schema_id.schemaID());
          this.writer.putU32((long) schema_id.schemaMajorVersion());
          this.writer.putU32((long) schema_id.schemaMinorVersion());

          this.writer.putU64(in_header.vertexCount());
          this.writer.putU64(in_header.triangleCount());
          this.writer.putU32(in_header.triangleIndexSizeBits());
          this.writer.putU32(0x78787878L);
          this.writer.putU32(attribute_count);
          this.writer.putU32(0x78787878L);

          final JPRACursor1DType<SMFBV1AttributeType> cursor =
            JPRACursor1DByteBufferedChecked.newCursor(
              this.attribute_buffer,
              SMFBV1AttributeByteBuffered::newValueWithOffset);
          final SMFBV1AttributeType view =
            cursor.getElementView();

          for (final SMFAttribute attribute : attributes) {
            Arrays.fill(this.attribute_bytes, (byte) 0);

            final JPRAStringCursorType name = view.getNameWritable();
            name.setValue(
              attribute.name().value(),
              JPRAStringTruncation.TRUNCATE);
            view.setComponentCount(attribute.componentCount());
            view.setComponentSize(attribute.componentSizeBits());
            view.setComponentKind(attribute.componentType().toInteger());
            this.writer.putBytes(this.attribute_bytes);
          }

          this.attribute_queue = Queue.ofAll(attributes);
          this.attribute_values_remaining = 0L;
          this.triangle_values_remaining = in_header.triangleCount();

          this.header = in_header;
          this.offsets = SMFBV1Offsets.fromHeader(in_header);

          /*
           * If there aren't any attributes, then the serialization of
           * attribute data is already complete.
           */

          if (attributes.isEmpty()) {
            this.state = SerializerState.STATE_ATTRIBUTE_DATA_SERIALIZED;
          } else {
            this.state = SerializerState.STATE_HEADER_SERIALIZED;
          }

          break;
        }

        case STATE_ATTRIBUTE_DATA_SERIALIZED:
        case STATE_HEADER_SERIALIZED: {
          throw new IllegalStateException("Header has already been serialized");
        }

        case STATE_FAILED: {
          throw new IllegalStateException("Serializer has already failed");
        }

        case STATE_FINISHED: {
          throw new IllegalStateException("Serializer has already finished");
        }
      }
    } catch (final IOException e) {
      LOG.debug("failure: ", e);
      this.state = SerializerState.STATE_FAILED;
    }
  }

  @Override
  public void serializeData(
    final SMFAttributeName name)
    throws IOException
  {
    NullCheck.notNull(name, "Name");

    switch (this.state) {
      case STATE_INITIAL: {
        throw new IllegalStateException("Header not yet serialized");
      }

      case STATE_HEADER_SERIALIZED: {
        if (this.attribute_values_remaining != 0L) {
          final StringBuilder sb = new StringBuilder(128);
          sb.append("Too few attribute values serialized.");
          sb.append(System.lineSeparator());
          sb.append("  Attribute: ");
          sb.append(this.attribute_current.name().value());
          sb.append(System.lineSeparator());
          sb.append("  Remaining: ");
          sb.append(this.attribute_values_remaining);
          sb.append(System.lineSeparator());
          this.state = SerializerState.STATE_FAILED;
          throw new IllegalStateException(sb.toString());
        }

        if (!this.attribute_queue.isEmpty()) {
          final SMFAttribute next = this.attribute_queue.head();
          if (name.equals(next.name())) {
            this.attribute_queue = this.attribute_queue.tail();
            this.attribute_values_remaining = this.header.vertexCount();
            this.attribute_current = next;
            this.serializeDataInsertAlignmentPadding(name);
            return;
          }
        }

        final StringBuilder sb = new StringBuilder(128);
        sb.append("Incorrect attribute specified for serialization.");
        sb.append(System.lineSeparator());
        sb.append("  Expected: ");
        if (!this.attribute_queue.isEmpty()) {
          sb.append(this.attribute_queue.head().name().value());
        } else {
          sb.append("(no attribute expected)");
        }
        sb.append(System.lineSeparator());
        sb.append("  Received: ");
        sb.append(name.value());
        sb.append(System.lineSeparator());
        this.state = SerializerState.STATE_FAILED;
        throw new IllegalArgumentException(sb.toString());
      }

      case STATE_ATTRIBUTE_DATA_SERIALIZED: {
        throw new IllegalStateException(
          "Attribute data has already been serialized");
      }

      case STATE_FAILED: {
        throw new IllegalStateException("Serializer has already failed");
      }

      case STATE_FINISHED: {
        throw new IllegalStateException("Serializer has already finished");
      }
    }

    throw new UnreachableCodeException();
  }

  private void serializeDataInsertAlignmentPadding(
    final SMFAttributeName name)
    throws IOException
  {
    final Option<Long> opt = this.offsets.attributeOffsets().get(name);
    Invariants.checkInvariant(opt.isDefined(), "Offset is defined");
    final long off = opt.get().longValue();
    this.insertAlignmentPadding(off);
  }

  /**
   * Insert up to 8 octets of padding to guarantee the alignment of the
   * following data.
   */

  private void insertAlignmentPadding(
    final long off)
    throws IOException
  {
    final long diff = Math.subtractExact(off, this.writer.position());
    if (LOG.isTraceEnabled()) {
      LOG.trace("padding required: {} octets", Long.valueOf(diff));
    }

    Invariants.checkInvariantL(
      diff,
      diff >= 0L,
      d -> "Difference " + d + " must be non-negative");
    Invariants.checkInvariantL(
      diff,
      diff <= 8L,
      d -> "Difference " + d + " must be <= 8");

    for (long pad = 0L; pad < diff; ++pad) {
      this.writer.putU8(0L);
    }

    Invariants.checkInvariantL(
      this.writer.position(),
      this.writer.position() == off,
      p -> "Writer must be at position " + p);
  }

  private void checkType(
    final SMFComponentType type,
    final int count)
  {
    switch (this.state) {
      case STATE_INITIAL: {
        throw new IllegalStateException("Header not yet serialized");
      }

      case STATE_HEADER_SERIALIZED: {
        final SMFAttribute a = this.attribute_current;
        if (a != null) {
          if (a.componentType() == type && a.componentCount() == count) {
            return;
          }

          final StringBuilder sb = new StringBuilder(128);
          sb.append("Incorrect type for attribute.");
          sb.append(System.lineSeparator());
          sb.append("  Attribute:      ");
          sb.append(a.name().value());
          sb.append(System.lineSeparator());
          sb.append("  Attribute type: ");
          sb.append(a.componentCount());
          sb.append(" components of type ");
          sb.append(a.componentType().name());
          sb.append(System.lineSeparator());
          sb.append("  Received:       ");
          sb.append(count);
          sb.append(" components of type ");
          sb.append(type.name());
          sb.append(System.lineSeparator());
          this.state = SerializerState.STATE_FAILED;
          throw new IllegalArgumentException(sb.toString());
        }

        final StringBuilder sb = new StringBuilder(128);
        sb.append("No attribute data is currently being serialized.");
        sb.append(System.lineSeparator());
        this.state = SerializerState.STATE_FAILED;
        throw new IllegalStateException(sb.toString());
      }

      case STATE_ATTRIBUTE_DATA_SERIALIZED: {
        throw new IllegalStateException(
          "Attribute data has already been serialized");
      }

      case STATE_FAILED: {
        throw new IllegalStateException("Serializer has already failed");
      }

      case STATE_FINISHED: {
        throw new IllegalStateException("Serializer has already finished");
      }
    }
  }

  @Override
  public void serializeValueFloat4(
    final double x,
    final double y,
    final double z,
    final double w)
    throws IOException
  {
    this.checkType(SMFComponentType.ELEMENT_TYPE_FLOATING, 4);

    try {
      switch (this.attribute_current.componentSizeBits()) {
        case 16: {
          this.writer.putF16(x);
          this.writer.putF16(y);
          this.writer.putF16(z);
          this.writer.putF16(w);
          break;
        }
        case 32: {
          this.writer.putF32(x);
          this.writer.putF32(y);
          this.writer.putF32(z);
          this.writer.putF32(w);
          break;
        }
        case 64: {
          this.writer.putF64(x);
          this.writer.putF64(y);
          this.writer.putF64(z);
          this.writer.putF64(w);
          break;
        }
      }

      this.serializeValueUpdateRemaining();
    } catch (final IOException e) {
      this.state = SerializerState.STATE_FAILED;
      throw e;
    }
  }

  @Override
  public void serializeValueFloat3(
    final double x,
    final double y,
    final double z)
    throws IOException
  {
    this.checkType(SMFComponentType.ELEMENT_TYPE_FLOATING, 3);

    try {
      switch (this.attribute_current.componentSizeBits()) {
        case 16: {
          this.writer.putF16(x);
          this.writer.putF16(y);
          this.writer.putF16(z);
          break;
        }
        case 32: {
          this.writer.putF32(x);
          this.writer.putF32(y);
          this.writer.putF32(z);
          break;
        }
        case 64: {
          this.writer.putF64(x);
          this.writer.putF64(y);
          this.writer.putF64(z);
          break;
        }
      }

      this.serializeValueUpdateRemaining();
    } catch (final IOException e) {
      this.state = SerializerState.STATE_FAILED;
      throw e;
    }
  }

  @Override
  public void serializeValueFloat2(
    final double x,
    final double y)
    throws IOException
  {
    this.checkType(SMFComponentType.ELEMENT_TYPE_FLOATING, 2);

    try {
      switch (this.attribute_current.componentSizeBits()) {
        case 16: {
          this.writer.putF16(x);
          this.writer.putF16(y);
          break;
        }
        case 32: {
          this.writer.putF32(x);
          this.writer.putF32(y);
          break;
        }
        case 64: {
          this.writer.putF64(x);
          this.writer.putF64(y);
          break;
        }
      }

      this.serializeValueUpdateRemaining();
    } catch (final IOException e) {
      this.state = SerializerState.STATE_FAILED;
      throw e;
    }
  }

  @Override
  public void serializeValueFloat1(
    final double x)
    throws IOException
  {
    this.checkType(SMFComponentType.ELEMENT_TYPE_FLOATING, 1);

    try {
      switch (this.attribute_current.componentSizeBits()) {
        case 16: {
          this.writer.putF16(x);
          break;
        }
        case 32: {
          this.writer.putF32(x);
          break;
        }
        case 64: {
          this.writer.putF64(x);
          break;
        }
      }

      this.serializeValueUpdateRemaining();
    } catch (final IOException e) {
      this.state = SerializerState.STATE_FAILED;
      throw e;
    }
  }

  @Override
  public void serializeValueIntegerSigned4(
    final long x,
    final long y,
    final long z,
    final long w)
    throws IOException
  {
    this.checkType(SMFComponentType.ELEMENT_TYPE_INTEGER_SIGNED, 4);

    try {
      switch (this.attribute_current.componentSizeBits()) {
        case 16: {
          this.writer.putS16(x);
          this.writer.putS16(y);
          this.writer.putS16(z);
          this.writer.putS16(w);
          break;
        }
        case 32: {
          this.writer.putS32(x);
          this.writer.putS32(y);
          this.writer.putS32(z);
          this.writer.putS32(w);
          break;
        }
        case 64: {
          this.writer.putS64(x);
          this.writer.putS64(y);
          this.writer.putS64(z);
          this.writer.putS64(w);
          break;
        }
      }

      this.serializeValueUpdateRemaining();
    } catch (final IOException e) {
      this.state = SerializerState.STATE_FAILED;
      throw e;
    }
  }

  @Override
  public void serializeValueIntegerSigned3(
    final long x,
    final long y,
    final long z)
    throws IOException
  {
    this.checkType(SMFComponentType.ELEMENT_TYPE_INTEGER_SIGNED, 3);

    try {
      switch (this.attribute_current.componentSizeBits()) {
        case 16: {
          this.writer.putS16(x);
          this.writer.putS16(y);
          this.writer.putS16(z);
          break;
        }
        case 32: {
          this.writer.putS32(x);
          this.writer.putS32(y);
          this.writer.putS32(z);
          break;
        }
        case 64: {
          this.writer.putS64(x);
          this.writer.putS64(y);
          this.writer.putS64(z);
          break;
        }
      }

      this.serializeValueUpdateRemaining();
    } catch (final IOException e) {
      this.state = SerializerState.STATE_FAILED;
      throw e;
    }
  }

  @Override
  public void serializeValueIntegerSigned2(
    final long x,
    final long y)
    throws IOException
  {
    this.checkType(SMFComponentType.ELEMENT_TYPE_INTEGER_SIGNED, 2);

    try {
      switch (this.attribute_current.componentSizeBits()) {
        case 16: {
          this.writer.putS16(x);
          this.writer.putS16(y);
          break;
        }
        case 32: {
          this.writer.putS32(x);
          this.writer.putS32(y);
          break;
        }
        case 64: {
          this.writer.putS64(x);
          this.writer.putS64(y);
          break;
        }
      }

      this.serializeValueUpdateRemaining();
    } catch (final IOException e) {
      this.state = SerializerState.STATE_FAILED;
      throw e;
    }
  }

  @Override
  public void serializeValueIntegerSigned1(
    final long x)
    throws IOException
  {
    this.checkType(SMFComponentType.ELEMENT_TYPE_INTEGER_SIGNED, 1);

    try {
      switch (this.attribute_current.componentSizeBits()) {
        case 16: {
          this.writer.putS16(x);
          break;
        }
        case 32: {
          this.writer.putS32(x);
          break;
        }
        case 64: {
          this.writer.putS64(x);
          break;
        }
      }

      this.serializeValueUpdateRemaining();
    } catch (final IOException e) {
      this.state = SerializerState.STATE_FAILED;
      throw e;
    }
  }

  @Override
  public void serializeValueIntegerUnsigned4(
    final long x,
    final long y,
    final long z,
    final long w)
    throws IOException
  {
    this.checkType(SMFComponentType.ELEMENT_TYPE_INTEGER_UNSIGNED, 4);

    try {
      switch (this.attribute_current.componentSizeBits()) {
        case 16: {
          this.writer.putU16(x);
          this.writer.putU16(y);
          this.writer.putU16(z);
          this.writer.putU16(w);
          break;
        }
        case 32: {
          this.writer.putU32(x);
          this.writer.putU32(y);
          this.writer.putU32(z);
          this.writer.putU32(w);
          break;
        }
        case 64: {
          this.writer.putU64(x);
          this.writer.putU64(y);
          this.writer.putU64(z);
          this.writer.putU64(w);
          break;
        }
      }

      this.serializeValueUpdateRemaining();
    } catch (final IOException e) {
      this.state = SerializerState.STATE_FAILED;
      throw e;
    }
  }

  @Override
  public void serializeValueIntegerUnsigned3(
    final long x,
    final long y,
    final long z)
    throws IOException
  {
    this.checkType(SMFComponentType.ELEMENT_TYPE_INTEGER_UNSIGNED, 3);

    try {
      switch (this.attribute_current.componentSizeBits()) {
        case 16: {
          this.writer.putU16(x);
          this.writer.putU16(y);
          this.writer.putU16(z);
          break;
        }
        case 32: {
          this.writer.putU32(x);
          this.writer.putU32(y);
          this.writer.putU32(z);
          break;
        }
        case 64: {
          this.writer.putU64(x);
          this.writer.putU64(y);
          this.writer.putU64(z);
          break;
        }
      }

      this.serializeValueUpdateRemaining();
    } catch (final IOException e) {
      this.state = SerializerState.STATE_FAILED;
      throw e;
    }
  }

  @Override
  public void serializeValueIntegerUnsigned2(
    final long x,
    final long y)
    throws IOException
  {
    this.checkType(SMFComponentType.ELEMENT_TYPE_INTEGER_UNSIGNED, 2);

    try {
      switch (this.attribute_current.componentSizeBits()) {
        case 16: {
          this.writer.putU16(x);
          this.writer.putU16(y);
          break;
        }
        case 32: {
          this.writer.putU32(x);
          this.writer.putU32(y);
          break;
        }
        case 64: {
          this.writer.putU64(x);
          this.writer.putU64(y);
          break;
        }
      }

      this.serializeValueUpdateRemaining();
    } catch (final IOException e) {
      this.state = SerializerState.STATE_FAILED;
      throw e;
    }
  }

  @Override
  public void serializeValueIntegerUnsigned1(
    final long x)
    throws IOException
  {
    this.checkType(SMFComponentType.ELEMENT_TYPE_INTEGER_UNSIGNED, 1);

    try {
      switch (this.attribute_current.componentSizeBits()) {
        case 16: {
          this.writer.putU16(x);
          break;
        }
        case 32: {
          this.writer.putU32(x);
          break;
        }
        case 64: {
          this.writer.putU64(x);
          break;
        }
      }

      this.serializeValueUpdateRemaining();

    } catch (final IOException e) {
      this.state = SerializerState.STATE_FAILED;
      throw e;
    }
  }

  private void serializeValueUpdateRemaining()
    throws IOException
  {
    Preconditions.checkPrecondition(
      this.state,
      this.state == SerializerState.STATE_HEADER_SERIALIZED,
      s -> s + " must be " + SerializerState.STATE_HEADER_SERIALIZED);

    this.attribute_values_remaining =
      Math.subtractExact(this.attribute_values_remaining, 1L);

    if (this.attribute_values_remaining == 0L && this.attribute_queue.isEmpty()) {
      this.state = SerializerState.STATE_ATTRIBUTE_DATA_SERIALIZED;
      this.insertAlignmentPadding(this.offsets.trianglesDataOffset());
    }
  }

  @Override
  public void serializeTriangle(
    final long v0,
    final long v1,
    final long v2)
    throws IOException, IllegalStateException
  {
    switch (this.state) {
      case STATE_INITIAL: {
        throw new IllegalStateException(
          "Header has not yet been serialized");
      }
      case STATE_HEADER_SERIALIZED: {
        throw new IllegalStateException(
          "Attribute data has not yet been serialized");
      }
      case STATE_ATTRIBUTE_DATA_SERIALIZED: {
        if (this.triangle_values_remaining != 0L) {
          this.serializeTriangleWrite(v0, v1, v2);
          return;
        }
        throw new IllegalStateException("No triangles are required");
      }
      case STATE_FAILED: {
        throw new IllegalStateException("Serializer has already failed");
      }
      case STATE_FINISHED: {
        throw new IllegalStateException("Serializer has already finished");
      }
    }
  }

  private void serializeTriangleWrite(
    final long v0,
    final long v1,
    final long v2)
    throws IOException
  {
    switch ((int) this.header.triangleIndexSizeBits()) {
      case 8: {
        this.writer.putU8(v0);
        this.writer.putU8(v1);
        this.writer.putU8(v2);
        break;
      }
      case 16: {
        this.writer.putU16(v0);
        this.writer.putU16(v1);
        this.writer.putU16(v2);
        break;
      }
      case 32: {
        this.writer.putU32(v0);
        this.writer.putU32(v1);
        this.writer.putU32(v2);
        break;
      }
      case 64: {
        this.writer.putU64(v0);
        this.writer.putU64(v1);
        this.writer.putU64(v2);
        break;
      }
      default: {
        throw new UnreachableCodeException();
      }
    }

    this.triangle_values_remaining =
      Math.subtractExact(this.triangle_values_remaining, 1L);

    if (this.triangle_values_remaining == 0L) {
      this.state = SerializerState.STATE_FINISHED;
    }
  }

  @Override
  public void close()
    throws IOException
  {
    switch (this.state) {
      case STATE_INITIAL:
      case STATE_HEADER_SERIALIZED:
      case STATE_ATTRIBUTE_DATA_SERIALIZED: {
        throw new IllegalStateException(
          "Closed a serializer without finishing it");
      }
      case STATE_FAILED: {
        break;
      }
      case STATE_FINISHED: {
        break;
      }
    }
  }

  private enum SerializerState
  {
    STATE_INITIAL,
    STATE_HEADER_SERIALIZED,
    STATE_ATTRIBUTE_DATA_SERIALIZED,
    STATE_FAILED,
    STATE_FINISHED
  }
}
