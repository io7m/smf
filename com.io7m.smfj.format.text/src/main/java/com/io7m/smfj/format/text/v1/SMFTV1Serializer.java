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

package com.io7m.smfj.format.text.v1;

import com.io7m.jaffirm.core.Preconditions;
import com.io7m.smfj.core.SMFAttribute;
import com.io7m.smfj.core.SMFAttributeName;
import com.io7m.smfj.core.SMFComponentType;
import com.io7m.smfj.core.SMFFormatVersion;
import com.io7m.smfj.core.SMFHeader;
import com.io7m.smfj.core.SMFSchemaIdentifier;
import com.io7m.smfj.core.SMFTriangles;
import com.io7m.smfj.format.text.SMFBase64Lines;
import com.io7m.smfj.serializer.api.SMFSerializerDataAttributesNonInterleavedType;
import com.io7m.smfj.serializer.api.SMFSerializerDataAttributesValuesType;
import com.io7m.smfj.serializer.api.SMFSerializerDataTrianglesType;
import com.io7m.smfj.serializer.api.SMFSerializerType;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UncheckedIOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static com.io7m.smfj.core.SMFComponentType.ELEMENT_TYPE_FLOATING;
import static com.io7m.smfj.core.SMFComponentType.ELEMENT_TYPE_INTEGER_SIGNED;
import static com.io7m.smfj.core.SMFComponentType.ELEMENT_TYPE_INTEGER_UNSIGNED;

/**
 * A serializer.
 */

public final class SMFTV1Serializer implements SMFSerializerType
{
  private final SMFFormatVersion version;
  private final BufferedWriter writer;
  private boolean done_header;
  private boolean done_vertices;
  private SMFHeader header;
  private boolean done_triangles;

  /**
   * Construct a serializer.
   *
   * @param in_version The format version
   * @param in_uri     The URI of the target, for diagnostic purposes
   * @param in_stream  An output stream
   */

  public SMFTV1Serializer(
    final SMFFormatVersion in_version,
    final URI in_uri,
    final OutputStream in_stream)
  {
    this.version = Objects.requireNonNull(in_version, "Version");

    Preconditions.checkPreconditionI(
      in_version.major(),
      in_version.major() == 1,
      v -> "Major version " + v + " must be 1");

    this.writer = new BufferedWriter(
      new OutputStreamWriter(in_stream, StandardCharsets.UTF_8));
  }

  @Override
  public void serializeHeader(
    final SMFHeader in_header)
    throws IOException
  {
    Objects.requireNonNull(in_header, "Header");

    if (this.done_header) {
      throw new IllegalStateException("Header has already been serialized");
    }

    try {
      this.serializeHeaderSMF();
      this.serializeHeaderSchema(in_header);
      this.serializeHeaderVertices(in_header);
      this.serializeHeaderTriangles(in_header);
      this.serializeHeaderCoordinates(in_header);
      this.serializeHeaderAttributes(in_header);
      this.serializeHeaderEnd();
    } finally {
      this.header = in_header;
      this.done_header = true;
    }
  }

  private void serializeHeaderEnd()
    throws IOException
  {
    this.writer.append("end");
    this.writer.newLine();
  }

  private void serializeHeaderAttributes(final SMFHeader in_header)
    throws IOException
  {
    try {
      in_header.attributesInOrder().forEach(a -> {
        try {
          this.writer.append("attribute ");
          this.writer.append('"');
          this.writer.append(a.name().value());
          this.writer.append('"');
          this.writer.append(" ");
          this.writer.append(a.componentType().getName());
          this.writer.append(" ");
          this.writer.append(Integer.toUnsignedString(a.componentCount()));
          this.writer.append(" ");
          this.writer.append(Integer.toUnsignedString(a.componentSizeBits()));
          this.writer.newLine();
        } catch (final IOException e) {
          throw new UncheckedIOException(e);
        }
      });
    } catch (final UncheckedIOException e) {
      throw e.getCause();
    }
  }

  private void serializeHeaderCoordinates(
    final SMFHeader in_header)
    throws IOException
  {
    this.writer.append("coordinates ");
    this.writer.append(in_header.coordinateSystem().toHumanString());
    this.writer.newLine();
  }

  private void serializeHeaderTriangles(
    final SMFHeader in_header)
    throws IOException
  {
    final SMFTriangles triangles = in_header.triangles();
    this.writer.append("triangles ");
    this.writer.append(Long.toUnsignedString(triangles.triangleCount()));
    this.writer.append(" ");
    this.writer.append(Long.toUnsignedString(triangles.triangleIndexSizeBits()));
    this.writer.newLine();
  }

  private void serializeHeaderVertices(
    final SMFHeader in_header)
    throws IOException
  {
    this.writer.append("vertices ");
    this.writer.append(Long.toUnsignedString(in_header.vertexCount()));
    this.writer.newLine();
  }

  private void serializeHeaderSchema(
    final SMFHeader in_header)
    throws IOException
  {
    try {
      in_header.schemaIdentifier().ifPresent(s -> {
        try {
          this.writer.append("schema ");
          this.writer.append(s.name().value());
          this.writer.append(" ");
          this.writer.append(Integer.toUnsignedString(s.versionMajor()));
          this.writer.append(" ");
          this.writer.append(Integer.toUnsignedString(s.versionMinor()));
          this.writer.newLine();
        } catch (final IOException e) {
          throw new UncheckedIOException(e);
        }
      });
    } catch (final UncheckedIOException e) {
      throw e.getCause();
    }
  }

  private void serializeHeaderSMF()
    throws IOException
  {
    this.writer.append("smf ");
    this.writer.append(Integer.toUnsignedString(this.version.major()));
    this.writer.append(" ");
    this.writer.append(Integer.toUnsignedString(this.version.minor()));
    this.writer.newLine();
  }

  @Override
  public SMFSerializerDataAttributesNonInterleavedType serializeVertexDataNonInterleavedStart()
    throws IOException
  {
    if (!this.done_header) {
      throw new IllegalStateException("Header has not yet been serialized");
    }
    if (this.done_vertices) {
      throw new IllegalStateException("Vertices have already been serialized");
    }

    try {
      this.writer.append("vertices-noninterleaved");
      this.writer.newLine();
      return new VertexDataNonInterleaved(this.writer, this.header);
    } finally {
      this.done_vertices = true;
    }
  }

  @Override
  public SMFSerializerDataTrianglesType serializeTrianglesStart()
    throws IOException
  {
    if (!this.done_header) {
      throw new IllegalStateException("Header has not yet been serialized");
    }
    if (this.done_triangles) {
      throw new IllegalStateException("Triangles have already been serialized");
    }

    try {
      this.writer.append("triangles");
      this.writer.newLine();
      return new Triangles(this.writer, this.header);
    } finally {
      this.done_triangles = true;
    }
  }

  @Override
  public void serializeMetadata(
    final SMFSchemaIdentifier schema,
    final byte[] data)
    throws IllegalStateException, IOException
  {
    if (!this.done_header) {
      throw new IllegalStateException("Header has not yet been serialized");
    }

    final List<String> lines = SMFBase64Lines.toBase64Lines(data);
    this.writer.append("metadata ");
    this.writer.append(schema.name().value());
    this.writer.append(" ");
    this.writer.append(Integer.toUnsignedString(schema.versionMajor()));
    this.writer.append(" ");
    this.writer.append(Integer.toUnsignedString(schema.versionMinor()));
    this.writer.append(" ");
    this.writer.append(Integer.toUnsignedString(lines.size()));
    this.writer.newLine();

    try {
      lines.forEach(line -> {
        try {
          this.writer.append(line);
          this.writer.newLine();
        } catch (final IOException e) {
          throw new UncheckedIOException(e);
        }
      });
    } catch (final UncheckedIOException e) {
      throw e.getCause();
    }

    this.writer.append("end");
    this.writer.newLine();
  }

  @Override
  public void close()
    throws IOException
  {
    this.writer.flush();
    this.writer.close();
  }

  private static final class VertexDataNonInterleaved
    implements SMFSerializerDataAttributesNonInterleavedType
  {
    private final SMFHeader header;
    private final Deque<SMFAttribute> queue;
    private final BufferedWriter writer;

    VertexDataNonInterleaved(
      final BufferedWriter in_writer,
      final SMFHeader in_header)
    {
      this.writer = Objects.requireNonNull(in_writer, "Writer");
      this.header = Objects.requireNonNull(in_header, "Header");
      this.queue = new LinkedList<>();
      this.header.attributesInOrder().forEach(this.queue::add);
    }

    @Override
    public SMFSerializerDataAttributesValuesType serializeData(
      final SMFAttributeName name)
      throws IllegalArgumentException, IOException
    {
      Objects.requireNonNull(name, "Name");

      if (this.queue.isEmpty()) {
        throw new IllegalStateException("No more attributes to serialize.");
      }

      final SMFAttribute head = this.queue.peek();
      if (!Objects.equals(head.name(), name)) {
        final String text =
          new StringBuilder(128)
            .append("Attempted to serialize attributes in the wrong order.")
            .append(System.lineSeparator())
            .append("  Expected: ")
            .append(head.name().value())
            .append(System.lineSeparator())
            .append("  Received: ")
            .append(name.value())
            .append(System.lineSeparator())
            .toString();
        throw new IllegalStateException(text);
      }

      this.writer.append("attribute ");
      this.writer.append('"');
      this.writer.append(head.name().value());
      this.writer.append('"');
      this.writer.newLine();

      return new ValuesNonInterleaved(
        this.writer,
        this.header.vertexCount(),
        this.queue.poll());
    }

    @Override
    public void close()
      throws IOException
    {
      if (!this.queue.isEmpty()) {
        final StringBuilder sb = new StringBuilder(128);
        sb.append("Some attributes were not serialized.");
        sb.append(System.lineSeparator());
        sb.append("  Missed attributes: ");
        sb.append(this.queue.stream().map(a -> a.name().value()).collect(
          Collectors.joining(" ")));
        sb.append(System.lineSeparator());
        throw new IllegalStateException(sb.toString());
      }

      this.writer.append("end");
      this.writer.newLine();
    }
  }

  private static final class ValuesNonInterleaved
    implements SMFSerializerDataAttributesValuesType
  {
    private final SMFAttribute attribute;
    private final BufferedWriter writer;
    private long vertices;

    ValuesNonInterleaved(
      final BufferedWriter in_writer,
      final long in_vertices,
      final SMFAttribute in_attribute)
    {
      this.writer = Objects.requireNonNull(in_writer, "Writer");
      this.vertices = in_vertices;
      this.attribute = Objects.requireNonNull(in_attribute, "Attribute");
    }

    @Override
    public void serializeValueFloat4(
      final double x,
      final double y,
      final double z,
      final double w)
      throws IOException, IllegalArgumentException
    {
      this.checkType(ELEMENT_TYPE_FLOATING, 4);
      this.checkVerticesRemaining();

      this.writer.append(Double.toString(x));
      this.writer.append(" ");
      this.writer.append(Double.toString(y));
      this.writer.append(" ");
      this.writer.append(Double.toString(z));
      this.writer.append(" ");
      this.writer.append(Double.toString(w));
      this.writer.newLine();
    }

    private void checkType(
      final SMFComponentType tried_component_type,
      final int tried_component_count)
    {
      if (this.attribute.componentType() == tried_component_type
        && this.attribute.componentCount() == tried_component_count) {
        return;
      }

      final String text =
        new StringBuilder(128)
          .append("Incorrect value type.")
          .append(System.lineSeparator())
          .append("  Attribute name: ")
          .append(this.attribute.name().value())
          .append(System.lineSeparator())
          .append("  Attribute type: ")
          .append(this.attribute.componentType().getName())
          .append(" ")
          .append(this.attribute.componentCount())
          .append(System.lineSeparator())
          .append("  Received type: ")
          .append(tried_component_type.getName())
          .append(" ")
          .append(tried_component_type)
          .append(System.lineSeparator())
          .toString();
      throw new IllegalArgumentException(text);
    }

    private void checkVerticesRemaining()
    {
      if (Long.compareUnsigned(this.vertices, 0L) <= 0) {
        final String text =
          new StringBuilder(128)
            .append("Attempted to serialize too many vertices.")
            .append(System.lineSeparator())
            .append("  Attribute name: ")
            .append(this.attribute.name().value())
            .append(System.lineSeparator())
            .append("  Remaining vertices: ")
            .append(this.vertices)
            .append(System.lineSeparator())
            .toString();
        throw new IllegalStateException(text);
      }

      this.vertices = Math.subtractExact(this.vertices, 1L);
    }

    @Override
    public void serializeValueFloat3(
      final double x,
      final double y,
      final double z)
      throws IOException, IllegalArgumentException
    {
      this.checkType(ELEMENT_TYPE_FLOATING, 3);
      this.checkVerticesRemaining();

      this.writer.append(Double.toString(x));
      this.writer.append(" ");
      this.writer.append(Double.toString(y));
      this.writer.append(" ");
      this.writer.append(Double.toString(z));
      this.writer.newLine();
    }

    @Override
    public void serializeValueFloat2(
      final double x,
      final double y)
      throws IOException, IllegalArgumentException
    {
      this.checkType(ELEMENT_TYPE_FLOATING, 2);
      this.checkVerticesRemaining();

      this.writer.append(Double.toString(x));
      this.writer.append(" ");
      this.writer.append(Double.toString(y));
      this.writer.newLine();
    }

    @Override
    public void serializeValueFloat1(
      final double x)
      throws IOException, IllegalArgumentException
    {
      this.checkType(ELEMENT_TYPE_FLOATING, 1);
      this.checkVerticesRemaining();

      this.writer.append(Double.toString(x));
      this.writer.newLine();
    }

    @Override
    public void serializeValueIntegerSigned4(
      final long x,
      final long y,
      final long z,
      final long w)
      throws IOException, IllegalArgumentException
    {
      this.checkType(ELEMENT_TYPE_INTEGER_SIGNED, 4);
      this.checkVerticesRemaining();

      this.writer.append(Long.toString(x));
      this.writer.append(" ");
      this.writer.append(Long.toString(y));
      this.writer.append(" ");
      this.writer.append(Long.toString(z));
      this.writer.append(" ");
      this.writer.append(Long.toString(w));
      this.writer.newLine();
    }

    @Override
    public void serializeValueIntegerSigned3(
      final long x,
      final long y,
      final long z)
      throws IOException, IllegalArgumentException
    {
      this.checkType(ELEMENT_TYPE_INTEGER_SIGNED, 3);
      this.checkVerticesRemaining();

      this.writer.append(Long.toString(x));
      this.writer.append(" ");
      this.writer.append(Long.toString(y));
      this.writer.append(" ");
      this.writer.append(Long.toString(z));
      this.writer.newLine();
    }

    @Override
    public void serializeValueIntegerSigned2(
      final long x,
      final long y)
      throws IOException, IllegalArgumentException
    {
      this.checkType(ELEMENT_TYPE_INTEGER_SIGNED, 2);
      this.checkVerticesRemaining();

      this.writer.append(Long.toString(x));
      this.writer.append(" ");
      this.writer.append(Long.toString(y));
      this.writer.newLine();
    }

    @Override
    public void serializeValueIntegerSigned1(
      final long x)
      throws IOException, IllegalArgumentException
    {
      this.checkType(ELEMENT_TYPE_INTEGER_SIGNED, 1);
      this.checkVerticesRemaining();

      this.writer.append(Long.toString(x));
      this.writer.newLine();
    }

    @Override
    public void serializeValueIntegerUnsigned4(
      final long x,
      final long y,
      final long z,
      final long w)
      throws IOException, IllegalArgumentException
    {
      this.checkType(ELEMENT_TYPE_INTEGER_UNSIGNED, 4);
      this.checkVerticesRemaining();

      this.writer.append(Long.toUnsignedString(x));
      this.writer.append(" ");
      this.writer.append(Long.toUnsignedString(y));
      this.writer.append(" ");
      this.writer.append(Long.toUnsignedString(z));
      this.writer.append(" ");
      this.writer.append(Long.toUnsignedString(w));
      this.writer.newLine();
    }

    @Override
    public void serializeValueIntegerUnsigned3(
      final long x,
      final long y,
      final long z)
      throws IOException, IllegalArgumentException
    {
      this.checkType(ELEMENT_TYPE_INTEGER_UNSIGNED, 3);
      this.checkVerticesRemaining();

      this.writer.append(Long.toUnsignedString(x));
      this.writer.append(" ");
      this.writer.append(Long.toUnsignedString(y));
      this.writer.append(" ");
      this.writer.append(Long.toUnsignedString(z));
      this.writer.newLine();
    }

    @Override
    public void serializeValueIntegerUnsigned2(
      final long x,
      final long y)
      throws IOException, IllegalArgumentException
    {
      this.checkType(ELEMENT_TYPE_INTEGER_UNSIGNED, 2);
      this.checkVerticesRemaining();

      this.writer.append(Long.toUnsignedString(x));
      this.writer.append(" ");
      this.writer.append(Long.toUnsignedString(y));
      this.writer.newLine();
    }

    @Override
    public void serializeValueIntegerUnsigned1(
      final long x)
      throws IOException, IllegalArgumentException
    {
      this.checkType(ELEMENT_TYPE_INTEGER_UNSIGNED, 1);
      this.checkVerticesRemaining();

      this.writer.append(Long.toUnsignedString(x));
      this.writer.newLine();
    }

    @Override
    public void close()
      throws IOException
    {
      if (this.vertices > 0L) {
        final String text =
          new StringBuilder(128)
            .append("Failed to serialize the correct number of vertices.")
            .append(System.lineSeparator())
            .append("  Attribute name: ")
            .append(this.attribute.name().value())
            .append(System.lineSeparator())
            .append("  Remaining vertices: ")
            .append(this.vertices)
            .append(System.lineSeparator())
            .toString();
        throw new IllegalStateException(text);
      }
    }
  }

  private static final class Triangles implements SMFSerializerDataTrianglesType
  {
    private final BufferedWriter writer;
    private final SMFHeader header;
    private long remaining;

    Triangles(
      final BufferedWriter in_writer,
      final SMFHeader in_header)
    {
      this.writer = Objects.requireNonNull(in_writer, "Writer");
      this.header = Objects.requireNonNull(in_header, "Header");
      this.remaining = this.header.triangles().triangleCount();
    }

    @Override
    public void serializeTriangle(
      final long v0,
      final long v1,
      final long v2)
      throws IOException, IllegalStateException
    {
      if (Long.compareUnsigned(this.remaining, 0L) <= 0) {
        final String text =
          new StringBuilder(128)
            .append("Attempted to serialize too many triangles.")
            .append(System.lineSeparator())
            .append("  Remaining triangles: ")
            .append(this.remaining)
            .append(System.lineSeparator())
            .toString();
        throw new IllegalStateException(text);
      }

      this.writer.append(Long.toUnsignedString(v0));
      this.writer.append(" ");
      this.writer.append(Long.toUnsignedString(v1));
      this.writer.append(" ");
      this.writer.append(Long.toUnsignedString(v2));
      this.writer.newLine();

      this.remaining = Math.subtractExact(this.remaining, 1L);
    }

    @Override
    public void close()
      throws IOException
    {
      if (Long.compareUnsigned(this.remaining, 0L) > 0) {
        final String text =
          new StringBuilder(128)
            .append("Attempted to serialize too few triangles.")
            .append(System.lineSeparator())
            .append("  Remaining triangles: ")
            .append(this.remaining)
            .append(System.lineSeparator())
            .toString();
        throw new IllegalStateException(text);
      }

      this.writer.append("end");
      this.writer.newLine();
    }
  }
}
