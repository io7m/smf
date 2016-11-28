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

package com.io7m.smfj.format.text;

import com.io7m.jaffirm.core.Preconditions;
import com.io7m.jnull.NullCheck;
import com.io7m.junreachable.UnreachableCodeException;
import com.io7m.smfj.core.SMFAttribute;
import com.io7m.smfj.core.SMFAttributeName;
import com.io7m.smfj.core.SMFComponentType;
import com.io7m.smfj.core.SMFFormatVersion;
import com.io7m.smfj.core.SMFHeader;
import com.io7m.smfj.core.SMFSchemaIdentifier;
import com.io7m.smfj.serializer.api.SMFSerializerType;
import javaslang.collection.List;
import javaslang.collection.Queue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;

final class SMFTV1Serializer implements SMFSerializerType
{
  private static final Logger LOG;

  static {
    LOG = LoggerFactory.getLogger(SMFTV1Serializer.class);
  }

  private final SMFFormatVersion version;
  private final BufferedWriter writer;
  private SerializerState state;
  private SMFHeader header;
  private Queue<SMFAttribute> attribute_queue;
  private long attribute_values_remaining;
  private SMFAttribute attribute_current;
  private long triangle_values_remaining;

  SMFTV1Serializer(
    final SMFFormatVersion in_version,
    final Path in_path,
    final OutputStream in_stream)
  {
    this.version = NullCheck.notNull(in_version, "Version");
    Preconditions.checkPreconditionI(
      in_version.major(),
      in_version.major() == 1,
      v -> "Major version " + v + " must be 1");

    this.writer = new BufferedWriter(
      new OutputStreamWriter(in_stream, StandardCharsets.UTF_8));
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
          this.header = in_header;
          this.attribute_queue = Queue.ofAll(attributes);
          this.triangle_values_remaining = in_header.triangleCount();

          this.writer.append(
            String.format(
              "smf %d %d",
              Integer.valueOf(this.version.major()),
              Integer.valueOf(this.version.minor())));
          this.writer.newLine();

          final SMFSchemaIdentifier schema_id =
            this.header.schemaIdentifier();
          if (schema_id.vendorID() != 0) {
            this.writer.append(
              String.format(
                "schema %8x %8x %d %d",
                Integer.valueOf(schema_id.vendorID()),
                Integer.valueOf(schema_id.schemaID()),
                Integer.valueOf(schema_id.schemaMajorVersion()),
                Integer.valueOf(schema_id.schemaMinorVersion())));
            this.writer.newLine();
          }

          this.writer.append(
            String.format(
              "vertices %s",
              Long.toUnsignedString(this.header.vertexCount())));
          this.writer.newLine();

          this.writer.append(
            String.format(
              "triangles %s %s",
              Long.toUnsignedString(this.header.triangleCount()),
              Long.toUnsignedString(this.header.triangleIndexSizeBits())));
          this.writer.newLine();

          for (final SMFAttribute attribute : attributes) {
            this.writer.append(
              String.format(
                "attribute \"%s\" %s %s %s",
                attribute.name().value(),
                attribute.componentType().getName(),
                Long.toUnsignedString((long) attribute.componentCount()),
                Long.toUnsignedString((long) attribute.componentSizeBits())));
            this.writer.newLine();
          }

          /*
           * If there aren't any attributes, then the serialization of
           * attribute data is already complete.
           */

          if (attributes.isEmpty()) {
            this.state = SerializerState.STATE_ATTRIBUTE_DATA_SERIALIZED;
          } else {
            this.writer.append("data");
            this.writer.newLine();
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

            this.writer.append(
              String.format(
                "attribute \"%s\"",
                this.attribute_current.name().value()));
            this.writer.newLine();
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
      this.writer.append(
        String.format(
          "%.15f %.15f %.15f %.15f",
          Double.valueOf(x),
          Double.valueOf(y),
          Double.valueOf(z),
          Double.valueOf(w)));
      this.writer.newLine();
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
      this.writer.append(
        String.format(
          "%.15f %.15f %.15f",
          Double.valueOf(x),
          Double.valueOf(y),
          Double.valueOf(z)));
      this.writer.newLine();
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
      this.writer.append(
        String.format(
          "%.15f %.15f",
          Double.valueOf(x),
          Double.valueOf(y)));
      this.writer.newLine();
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
      this.writer.append(
        String.format(
          "%.15f",
          Double.valueOf(x)));
      this.writer.newLine();
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
      this.writer.append(
        String.format(
          "%d %d %d %d",
          Long.valueOf(x),
          Long.valueOf(y),
          Long.valueOf(z),
          Long.valueOf(w)));
      this.writer.newLine();
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
      this.writer.append(
        String.format(
          "%d %d %d",
          Long.valueOf(x),
          Long.valueOf(y),
          Long.valueOf(z)));
      this.writer.newLine();
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
      this.writer.append(
        String.format(
          "%d %d",
          Long.valueOf(x),
          Long.valueOf(y)));
      this.writer.newLine();
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
      this.writer.append(
        String.format(
          "%d",
          Long.valueOf(x)));
      this.writer.newLine();
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
      this.writer.append(
        String.format(
          "%s %s %s %s",
          Long.toUnsignedString(x),
          Long.toUnsignedString(y),
          Long.toUnsignedString(z),
          Long.toUnsignedString(w)));
      this.writer.newLine();
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
      this.writer.append(
        String.format(
          "%s %s %s",
          Long.toUnsignedString(x),
          Long.toUnsignedString(y),
          Long.toUnsignedString(z)));
      this.writer.newLine();
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
      this.writer.append(
        String.format(
          "%s %s",
          Long.toUnsignedString(x),
          Long.toUnsignedString(y)));
      this.writer.newLine();
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
      this.writer.append(String.format("%s", Long.toUnsignedString(x)));
      this.writer.newLine();
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

      if (this.header.triangleCount() != 0L) {
        this.writer.append("triangles");
        this.writer.newLine();
      }
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
    this.writer.append(
      String.format(
        "%s %s %s",
        Long.toUnsignedString(v0),
        Long.toUnsignedString(v1),
        Long.toUnsignedString(v2)));
    this.writer.newLine();

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
    LOG.debug("closing serializer");

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
        this.writer.flush();
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
