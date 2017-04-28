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
import com.io7m.jcoords.core.conversion.CAxis;
import com.io7m.jcoords.core.conversion.CAxisSystem;
import com.io7m.jfsm.core.FSMEnumMutable;
import com.io7m.jfsm.core.FSMEnumMutableBuilderType;
import com.io7m.jnull.NullCheck;
import com.io7m.junreachable.UnreachableCodeException;
import com.io7m.smfj.core.SMFAttribute;
import com.io7m.smfj.core.SMFAttributeName;
import com.io7m.smfj.core.SMFComponentType;
import com.io7m.smfj.core.SMFCoordinateSystem;
import com.io7m.smfj.core.SMFFaceWindingOrder;
import com.io7m.smfj.core.SMFFormatVersion;
import com.io7m.smfj.core.SMFHeader;
import com.io7m.smfj.core.SMFSchemaIdentifier;
import com.io7m.smfj.core.SMFTriangles;
import com.io7m.smfj.serializer.api.SMFSerializerType;
import javaslang.collection.List;
import javaslang.collection.Queue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

final class SMFTV1Serializer implements SMFSerializerType
{
  private static final Logger LOG;

  static {
    LOG = LoggerFactory.getLogger(SMFTV1Serializer.class);
  }

  private final SMFFormatVersion version;
  private final BufferedWriter writer;
  private final FSMEnumMutable<SerializerState> state;
  private SMFHeader header;
  private Queue<SMFAttribute> attribute_queue;
  private long attribute_values_remaining;
  private SMFAttribute attribute_current;
  private long triangle_values_remaining;
  private long meta_values_remaining;

  SMFTV1Serializer(
    final SMFFormatVersion in_version,
    final URI in_uri,
    final OutputStream in_stream)
  {
    this.version = NullCheck.notNull(in_version, "Version");
    Preconditions.checkPreconditionI(
      in_version.major(),
      in_version.major() == 1,
      v -> "Major version " + v + " must be 1");

    this.writer = new BufferedWriter(
      new OutputStreamWriter(in_stream, StandardCharsets.UTF_8));

    final FSMEnumMutableBuilderType<SerializerState> builder =
      FSMEnumMutable.builder(SerializerState.STATE_INITIAL)
        .addTransition(
          SerializerState.STATE_INITIAL,
          SerializerState.STATE_HEADER)
        .addTransition(
          SerializerState.STATE_HEADER,
          SerializerState.STATE_DATA_ATTRIBUTES_START)
        .addTransition(
          SerializerState.STATE_DATA_ATTRIBUTES_START,
          SerializerState.STATE_DATA_ATTRIBUTES_IN_PROGRESS)
        .addTransition(
          SerializerState.STATE_DATA_ATTRIBUTES_IN_PROGRESS,
          SerializerState.STATE_DATA_ATTRIBUTES_FINISHED)
        .addTransition(
          SerializerState.STATE_DATA_ATTRIBUTES_FINISHED,
          SerializerState.STATE_DATA_TRIANGLES_START)
        .addTransition(
          SerializerState.STATE_DATA_TRIANGLES_START,
          SerializerState.STATE_DATA_TRIANGLES_IN_PROGRESS)
        .addTransition(
          SerializerState.STATE_DATA_TRIANGLES_IN_PROGRESS,
          SerializerState.STATE_DATA_TRIANGLES_FINISHED)
        .addTransition(
          SerializerState.STATE_DATA_TRIANGLES_FINISHED,
          SerializerState.STATE_DATA_METADATA_START)
        .addTransition(
          SerializerState.STATE_DATA_METADATA_START,
          SerializerState.STATE_DATA_METADATA_IN_PROGRESS)
        .addTransition(
          SerializerState.STATE_DATA_METADATA_IN_PROGRESS,
          SerializerState.STATE_DATA_METADATA_FINISHED)
        .addTransition(
          SerializerState.STATE_DATA_METADATA_FINISHED,
          SerializerState.STATE_FINISHED);

    builder.addTransition(
      SerializerState.STATE_DATA_ATTRIBUTES_IN_PROGRESS,
      SerializerState.STATE_DATA_ATTRIBUTES_IN_PROGRESS);

    builder.addTransition(
      SerializerState.STATE_DATA_ATTRIBUTES_IN_PROGRESS,
      SerializerState.STATE_DATA_ATTRIBUTES_IN_PROGRESS);

    builder.addTransition(
      SerializerState.STATE_DATA_TRIANGLES_IN_PROGRESS,
      SerializerState.STATE_DATA_TRIANGLES_IN_PROGRESS);

    builder.addTransition(
      SerializerState.STATE_DATA_METADATA_IN_PROGRESS,
      SerializerState.STATE_DATA_METADATA_IN_PROGRESS);

    for (final SerializerState source : SerializerState.values()) {
      builder.addTransition(source, SerializerState.STATE_FAILED);
    }

    this.state = builder.build();
  }

  private static String serializeAxis(
    final CAxis axis)
  {
    switch (axis) {
      case AXIS_POSITIVE_X:
        return "+x";
      case AXIS_POSITIVE_Y:
        return "+y";
      case AXIS_POSITIVE_Z:
        return "+z";
      case AXIS_NEGATIVE_X:
        return "-x";
      case AXIS_NEGATIVE_Y:
        return "-y";
      case AXIS_NEGATIVE_Z:
        return "-z";
    }
    throw new UnreachableCodeException();
  }

  private static String serializeWindingOrder(
    final SMFFaceWindingOrder order)
  {
    switch (order) {
      case FACE_WINDING_ORDER_CLOCKWISE:
        return "clockwise";
      case FACE_WINDING_ORDER_COUNTER_CLOCKWISE:
        return "counter-clockwise";
    }
    throw new UnreachableCodeException();
  }

  private static String serializeAxes(
    final SMFCoordinateSystem system)
  {
    final CAxisSystem axes = system.axes();
    return String.format(
      "coordinates %s %s %s %s",
      serializeAxis(axes.right()),
      serializeAxis(axes.up()),
      serializeAxis(axes.forward()),
      serializeWindingOrder(system.windingOrder()));
  }

  @Override
  public void serializeHeader(
    final SMFHeader in_header)
  {
    NullCheck.notNull(in_header, "Header");

    try {
      this.state.transition(SerializerState.STATE_HEADER);

      final List<SMFAttribute> attributes = in_header.attributesInOrder();
      this.header = in_header;
      this.attribute_queue = Queue.ofAll(attributes);
      final SMFTriangles triangles = in_header.triangles();
      this.triangle_values_remaining = triangles.triangleCount();
      this.meta_values_remaining = in_header.metaCount();

      this.writer.append(
        String.format(
          "smf %d %d",
          Integer.valueOf(this.version.major()),
          Integer.valueOf(this.version.minor())));
      this.writer.newLine();

      final SMFSchemaIdentifier schema_id =
        this.header.schemaIdentifier();
      if (schema_id.vendorID() != 0) {
        this.writer.append("schema ");
        this.writer.append(schema_id.toHumanString());
        this.writer.newLine();
      }

      this.writer.append(
        String.format(
          "meta %s",
          Long.toUnsignedString(this.header.metaCount())));
      this.writer.newLine();

      this.writer.append(
        String.format(
          "vertices %s",
          Long.toUnsignedString(this.header.vertexCount())));
      this.writer.newLine();

      this.writer.append(
        String.format(
          "triangles %s %s",
          Long.toUnsignedString(triangles.triangleCount()),
          Long.toUnsignedString(triangles.triangleIndexSizeBits())));
      this.writer.newLine();

      this.writer.append(serializeAxes(this.header.coordinateSystem()));
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

    } catch (final IOException e) {
      LOG.debug("failure: ", e);
      this.state.transition(SerializerState.STATE_FAILED);
    }
  }

  @Override
  public void serializeDataStart()
    throws IllegalStateException, IOException
  {
    this.state.transition(SerializerState.STATE_DATA_ATTRIBUTES_START);
    this.writer.append("data");
    this.writer.newLine();

    if (this.attribute_queue.isEmpty()) {
      this.state.transition(SerializerState.STATE_DATA_ATTRIBUTES_IN_PROGRESS);
      this.state.transition(SerializerState.STATE_DATA_ATTRIBUTES_FINISHED);
    }
  }

  @Override
  public void serializeData(
    final SMFAttributeName name)
    throws IOException
  {
    NullCheck.notNull(name, "Name");

    this.state.transition(SerializerState.STATE_DATA_ATTRIBUTES_IN_PROGRESS);

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
      this.state.transition(SerializerState.STATE_FAILED);
      throw new IllegalStateException(sb.toString());
    }

    if (!this.attribute_queue.isEmpty()) {
      final SMFAttribute next = this.attribute_queue.head();
      if (Objects.equals(name, next.name())) {
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
    this.state.transition(SerializerState.STATE_FAILED);
    throw new IllegalArgumentException(sb.toString());
  }

  private void checkType(
    final SMFComponentType type,
    final int count)
  {
    this.state.transition(SerializerState.STATE_DATA_ATTRIBUTES_IN_PROGRESS);

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
      this.state.transition(SerializerState.STATE_FAILED);
      throw new IllegalArgumentException(sb.toString());
    }

    final StringBuilder sb = new StringBuilder(128);
    sb.append("No attribute data is currently being serialized.");
    sb.append(System.lineSeparator());
    this.state.transition(SerializerState.STATE_FAILED);
    throw new IllegalStateException(sb.toString());
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
      this.state.transition(SerializerState.STATE_FAILED);
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
      this.state.transition(SerializerState.STATE_FAILED);
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
      this.state.transition(SerializerState.STATE_FAILED);
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
      this.state.transition(SerializerState.STATE_FAILED);
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
      this.state.transition(SerializerState.STATE_FAILED);
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
      this.state.transition(SerializerState.STATE_FAILED);
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
      this.state.transition(SerializerState.STATE_FAILED);
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
      this.state.transition(SerializerState.STATE_FAILED);
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
      this.state.transition(SerializerState.STATE_FAILED);
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
      this.state.transition(SerializerState.STATE_FAILED);
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
      this.state.transition(SerializerState.STATE_FAILED);
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
      this.state.transition(SerializerState.STATE_FAILED);
      throw e;
    }
  }

  @Override
  public void serializeTrianglesStart()
    throws IllegalStateException, IOException
  {
    this.state.transition(SerializerState.STATE_DATA_TRIANGLES_START);
    this.writer.append("triangles");
    this.writer.newLine();

    if (this.triangle_values_remaining == 0L) {
      this.state.transition(SerializerState.STATE_DATA_TRIANGLES_IN_PROGRESS);
      this.state.transition(SerializerState.STATE_DATA_TRIANGLES_FINISHED);
    }
  }

  private void serializeValueUpdateRemaining()
    throws IOException
  {
    Preconditions.checkPrecondition(
      this.state.current(),
      this.state.current() == SerializerState.STATE_DATA_ATTRIBUTES_IN_PROGRESS,
      s -> s + " must be " + SerializerState.STATE_DATA_ATTRIBUTES_IN_PROGRESS);

    this.attribute_values_remaining =
      Math.subtractExact(this.attribute_values_remaining, 1L);

    if (this.attribute_values_remaining == 0L) {
      if (this.attribute_queue.isEmpty()) {
        this.state.transition(SerializerState.STATE_DATA_ATTRIBUTES_FINISHED);
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
    this.state.transition(SerializerState.STATE_DATA_TRIANGLES_IN_PROGRESS);

    if (this.triangle_values_remaining != 0L) {
      this.serializeTriangleWrite(v0, v1, v2);
      return;
    }

    throw new IllegalStateException("No triangles are required");
  }

  @Override
  public void serializeMetadataStart()
    throws IllegalStateException, IOException
  {
    this.state.transition(SerializerState.STATE_DATA_METADATA_START);
    this.writer.append("metadata");
    this.writer.newLine();

    if (this.meta_values_remaining == 0L) {
      this.state.transition(SerializerState.STATE_DATA_METADATA_IN_PROGRESS);
      this.state.transition(SerializerState.STATE_DATA_METADATA_FINISHED);
    }
  }

  @Override
  public void serializeMetadata(
    final long vendor,
    final long schema,
    final byte[] data)
    throws IOException, IllegalStateException
  {
    NullCheck.notNull(data, "Data");

    this.state.transition(SerializerState.STATE_DATA_METADATA_IN_PROGRESS);

    if (this.meta_values_remaining != 0L) {
      final java.util.List<String> lines =
        SMFBase64Lines.toBase64Lines(data);

      this.writer.append(
        String.format(
          "meta %s %s %d",
          Long.toUnsignedString(vendor, 16),
          Long.toUnsignedString(schema, 16),
          Integer.valueOf(lines.size())));
      this.writer.newLine();

      for (final String line : lines) {
        this.writer.append(line);
        this.writer.newLine();
      }

      this.meta_values_remaining =
        Math.subtractExact(this.meta_values_remaining, 1L);

      if (this.meta_values_remaining == 0L) {
        this.state.transition(SerializerState.STATE_DATA_METADATA_FINISHED);
      }
      return;
    }

    throw new IllegalStateException("No metadata is required");
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
      this.state.transition(SerializerState.STATE_DATA_TRIANGLES_FINISHED);
    }
  }

  @Override
  public void close()
    throws IOException
  {
    LOG.debug("closing serializer");

    switch (this.state.current()) {
      case STATE_HEADER:
      case STATE_INITIAL:
      case STATE_DATA_ATTRIBUTES_START:
      case STATE_DATA_ATTRIBUTES_IN_PROGRESS:
      case STATE_DATA_ATTRIBUTES_FINISHED:
      case STATE_DATA_TRIANGLES_START:
      case STATE_DATA_TRIANGLES_IN_PROGRESS:
      case STATE_DATA_TRIANGLES_FINISHED:
      case STATE_DATA_METADATA_START:
      case STATE_DATA_METADATA_IN_PROGRESS:
      case STATE_DATA_METADATA_FINISHED:
      case STATE_FINISHED: {
        if (!this.attribute_queue.isEmpty()) {
          throw new IllegalStateException(
            "Closed a serializer with "
              + this.attribute_queue.size()
              + " attributes remaining");
        }
        if (this.triangle_values_remaining != 0L) {
          throw new IllegalStateException(
            "Closed a serializer with "
              + this.triangle_values_remaining
              + " triangles remaining");
        }
        if (this.meta_values_remaining != 0L) {
          throw new IllegalStateException(
            "Closed a serializer with "
              + this.meta_values_remaining
              + " metadata values remaining");
        }
        this.writer.flush();
        break;
      }
      case STATE_FAILED:
        break;
    }
  }

  private enum SerializerState
  {
    STATE_INITIAL,
    STATE_HEADER,
    STATE_DATA_ATTRIBUTES_START,
    STATE_DATA_ATTRIBUTES_IN_PROGRESS,
    STATE_DATA_ATTRIBUTES_FINISHED,
    STATE_DATA_TRIANGLES_START,
    STATE_DATA_TRIANGLES_IN_PROGRESS,
    STATE_DATA_TRIANGLES_FINISHED,
    STATE_DATA_METADATA_START,
    STATE_DATA_METADATA_IN_PROGRESS,
    STATE_DATA_METADATA_FINISHED,
    STATE_FINISHED,
    STATE_FAILED
  }
}
