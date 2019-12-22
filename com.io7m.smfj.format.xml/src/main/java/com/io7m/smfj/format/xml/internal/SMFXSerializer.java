/*
 * Copyright © 2019 Mark Raynsford <code@io7m.com> http://io7m.com
 *
 * Permission to use, copy, modify, and/or distribute this software for
 * any purpose with or without fee is hereby granted, provided that the
 * above copyright notice and this permission notice appear in all copies.
 *
 * THE SOFTWARE IS PROVIDED "AS IS" AND THE AUTHOR DISCLAIMS ALL
 * WARRANTIES WITH REGARD TO THIS SOFTWARE INCLUDING ALL IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS. IN NO EVENT SHALL THE AUTHOR
 * BE LIABLE FOR ANY SPECIAL, DIRECT, INDIRECT, OR CONSEQUENTIAL DAMAGES
 * OR ANY DAMAGES WHATSOEVER RESULTING FROM LOSS OF USE, DATA OR PROFITS,
 * WHETHER IN AN ACTION OF CONTRACT, NEGLIGENCE OR OTHER TORTIOUS ACTION,
 * ARISING OUT OF OR IN CONNECTION WITH THE USE OR PERFORMANCE OF THIS
 * SOFTWARE.
 */


package com.io7m.smfj.format.xml.internal;

import com.io7m.smfj.core.SMFAttribute;
import com.io7m.smfj.core.SMFAttributeName;
import com.io7m.smfj.core.SMFCoordinateSystem;
import com.io7m.smfj.core.SMFHeader;
import com.io7m.smfj.core.SMFSchemaIdentifier;
import com.io7m.smfj.core.SMFTriangles;
import com.io7m.smfj.serializer.api.SMFSerializerDataAttributesNonInterleavedType;
import com.io7m.smfj.serializer.api.SMFSerializerDataAttributesValuesType;
import com.io7m.smfj.serializer.api.SMFSerializerDataTrianglesType;
import com.io7m.smfj.serializer.api.SMFSerializerType;
import java.io.IOException;
import java.util.Base64;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

public final class SMFXSerializer implements SMFSerializerType
{
  private final XMLStreamWriter writer;

  public SMFXSerializer(
    final XMLStreamWriter inWriter)
  {
    this.writer = Objects.requireNonNull(inWriter, "writer");
  }

  public SMFXSerializer start()
    throws XMLStreamException
  {
    final var namespaceURI = SMFX.namespaceURI2p0();

    this.writer.writeStartDocument("UTF-8", "1.0");
    this.writer.setPrefix("sx", namespaceURI);
    this.writer.writeStartElement(namespaceURI, "SMF");
    this.writer.writeNamespace("sx", namespaceURI);
    return this;
  }

  @Override
  public void serializeHeader(
    final SMFHeader header)
    throws IllegalStateException, IOException
  {
    try {
      final var namespaceURI = SMFX.namespaceURI2p0();
      this.writer.writeStartElement("sx", "Header", namespaceURI);
      this.writer.writeAttribute(
        "vertexCount",
        Long.toUnsignedString(header.vertexCount()));
      this.writer.writeAttribute(
        "endianness",
        header.dataByteOrder().toString());

      this.writeHeaderCoordinateSystem(namespaceURI, header.coordinateSystem());
      this.writeHeaderTriangles(namespaceURI, header.triangles());
      this.writeSchemaIdentifierOpt(namespaceURI, header.schemaIdentifier());
      this.writeHeaderAttributes(namespaceURI, header.attributesInOrder());

      this.writer.writeEndElement();
    } catch (final XMLStreamException e) {
      throw new IOException(e);
    }
  }

  private void writeHeaderAttributes(
    final String namespaceURI,
    final List<SMFAttribute> attributesInOrder)
    throws XMLStreamException
  {
    this.writer.writeStartElement(
      "sx", "Attributes", namespaceURI);

    for (final var attribute : attributesInOrder) {
      this.writer.writeStartElement(
        "sx", "Attribute", namespaceURI);

      this.writer.writeAttribute(
        "name",
        attribute.name().value());
      this.writer.writeAttribute(
        "componentKind",
        attribute.componentType().toString());
      this.writer.writeAttribute(
        "componentCount",
        Integer.toUnsignedString(attribute.componentCount()));
      this.writer.writeAttribute(
        "componentSizeBits",
        Integer.toUnsignedString(attribute.componentSizeBits()));

      this.writer.writeEndElement();
    }

    this.writer.writeEndElement();
  }

  private void writeSchemaIdentifierOpt(
    final String namespaceURI,
    final Optional<SMFSchemaIdentifier> schemaIdentifierOpt)
    throws XMLStreamException
  {
    if (schemaIdentifierOpt.isPresent()) {
      this.writeSchemaIdentifier(namespaceURI, schemaIdentifierOpt.get());
    }
  }

  private void writeSchemaIdentifier(
    final String namespaceURI,
    final SMFSchemaIdentifier schemaIdentifier)
    throws XMLStreamException
  {
    this.writer.writeStartElement(
      "sx", "SchemaIdentifier", namespaceURI);
    this.writer.writeAttribute(
      "name", schemaIdentifier.name().value());
    this.writer.writeAttribute(
      "versionMajor",
      Integer.toUnsignedString(schemaIdentifier.versionMajor()));
    this.writer.writeAttribute(
      "versionMinor",
      Integer.toUnsignedString(schemaIdentifier.versionMinor()));
    this.writer.writeEndElement();
  }

  private void writeHeaderTriangles(
    final String namespaceURI,
    final SMFTriangles triangles)
    throws XMLStreamException
  {
    this.writer.writeStartElement(
      "sx", "TriangleSpecification", namespaceURI);
    this.writer.writeAttribute(
      "count",
      Long.toUnsignedString(triangles.triangleCount()));
    this.writer.writeAttribute(
      "sizeBits",
      Integer.toUnsignedString(triangles.triangleIndexSizeBits()));
    this.writer.writeEndElement();
  }

  private void writeHeaderCoordinateSystem(
    final String namespaceURI,
    final SMFCoordinateSystem coordinateSystem)
    throws XMLStreamException
  {
    final var axes = coordinateSystem.axes();
    this.writer.writeStartElement(
      "sx", "CoordinateSystem", namespaceURI);
    this.writer.writeAttribute(
      "right", axes.right().toString());
    this.writer.writeAttribute(
      "up", axes.up().toString());
    this.writer.writeAttribute(
      "forward", axes.forward().toString());
    this.writer.writeAttribute(
      "windingOrder", coordinateSystem.windingOrder().toString());
    this.writer.writeEndElement();
  }

  @Override
  public SMFSerializerDataAttributesNonInterleavedType serializeVertexDataNonInterleavedStart()
    throws IllegalStateException, IOException
  {
    try {
      return new DataAttributesNonInterleaved(this.writer).start();
    } catch (final Exception e) {
      throw new IOException(e);
    }
  }

  @Override
  public SMFSerializerDataTrianglesType serializeTrianglesStart()
    throws IllegalStateException, IOException
  {
    try {
      return new DataTriangles(this.writer).start();
    } catch (final Exception e) {
      throw new IOException(e);
    }
  }

  @Override
  public void serializeMetadata(
    final SMFSchemaIdentifier schema,
    final byte[] data)
    throws IllegalStateException, IOException
  {
    try {
      final var namespaceURI = SMFX.namespaceURI2p0();
      this.writer.writeStartElement("sx", "Metadata", namespaceURI);
      this.writeSchemaIdentifier(namespaceURI, schema);
      this.writer.writeStartElement("sx", "Base64Data", namespaceURI);
      this.writer.writeCData(Base64.getEncoder().encodeToString(data));
      this.writer.writeEndElement();
      this.writer.writeEndElement();
    } catch (final XMLStreamException e) {
      throw new IOException(e);
    }
  }

  @Override
  public void close()
    throws IOException
  {
    try {
      this.writer.flush();
      this.writer.writeEndElement();
      this.writer.writeEndDocument();
    } catch (final Exception e) {
      throw new IOException(e);
    }
  }

  private static final class DataAttributeValues
    implements SMFSerializerDataAttributesValuesType
  {
    private final XMLStreamWriter writer;
    private final SMFAttributeName name;

    DataAttributeValues(
      final XMLStreamWriter inWriter,
      final SMFAttributeName inName)
    {
      this.writer = Objects.requireNonNull(inWriter, "writer");
      this.name = Objects.requireNonNull(inName, "inName");
    }

    DataAttributeValues start()
      throws IOException
    {
      try {
        final var namespaceURI = SMFX.namespaceURI2p0();
        this.writer.writeStartElement(
          "sx", "AttributeData", namespaceURI);
        this.writer.writeAttribute("name", this.name.value());
        return this;
      } catch (final Exception e) {
        throw new IOException(e);
      }
    }

    @Override
    public void serializeValueFloat4(
      final double x,
      final double y,
      final double z,
      final double w)
      throws IOException, IllegalArgumentException, IllegalStateException
    {
      try {
        final var namespaceURI = SMFX.namespaceURI2p0();
        this.writer.writeStartElement(
          "sx", "AttributeFloat4", namespaceURI);
        this.writer.writeAttribute("c0", Double.toString(x));
        this.writer.writeAttribute("c1", Double.toString(y));
        this.writer.writeAttribute("c2", Double.toString(z));
        this.writer.writeAttribute("c3", Double.toString(w));
        this.writer.writeEndElement();
      } catch (final Exception e) {
        throw new IOException(e);
      }
    }

    @Override
    public void serializeValueFloat3(
      final double x,
      final double y,
      final double z)
      throws IOException, IllegalArgumentException, IllegalStateException
    {
      try {
        final var namespaceURI = SMFX.namespaceURI2p0();
        this.writer.writeStartElement(
          "sx", "AttributeFloat3", namespaceURI);
        this.writer.writeAttribute("c0", Double.toString(x));
        this.writer.writeAttribute("c1", Double.toString(y));
        this.writer.writeAttribute("c2", Double.toString(z));
        this.writer.writeEndElement();
      } catch (final Exception e) {
        throw new IOException(e);
      }
    }

    @Override
    public void serializeValueFloat2(
      final double x,
      final double y)
      throws IOException, IllegalArgumentException, IllegalStateException
    {
      try {
        final var namespaceURI = SMFX.namespaceURI2p0();
        this.writer.writeStartElement(
          "sx", "AttributeFloat2", namespaceURI);
        this.writer.writeAttribute("c0", Double.toString(x));
        this.writer.writeAttribute("c1", Double.toString(y));
        this.writer.writeEndElement();
      } catch (final Exception e) {
        throw new IOException(e);
      }
    }

    @Override
    public void serializeValueFloat1(final double x)
      throws IOException, IllegalArgumentException, IllegalStateException
    {
      try {
        final var namespaceURI = SMFX.namespaceURI2p0();
        this.writer.writeStartElement(
          "sx", "AttributeFloat1", namespaceURI);
        this.writer.writeAttribute("c0", Double.toString(x));
        this.writer.writeEndElement();
      } catch (final Exception e) {
        throw new IOException(e);
      }
    }

    @Override
    public void serializeValueIntegerSigned4(
      final long x,
      final long y,
      final long z,
      final long w)
      throws IOException, IllegalArgumentException, IllegalStateException
    {
      try {
        final var namespaceURI = SMFX.namespaceURI2p0();
        this.writer.writeStartElement(
          "sx", "AttributeIntegerSigned4", namespaceURI);
        this.writer.writeAttribute("c0", Long.toString(x));
        this.writer.writeAttribute("c1", Long.toString(y));
        this.writer.writeAttribute("c2", Long.toString(z));
        this.writer.writeAttribute("c3", Long.toString(w));
        this.writer.writeEndElement();
      } catch (final Exception e) {
        throw new IOException(e);
      }
    }

    @Override
    public void serializeValueIntegerSigned3(
      final long x,
      final long y,
      final long z)
      throws IOException, IllegalArgumentException, IllegalStateException
    {
      try {
        final var namespaceURI = SMFX.namespaceURI2p0();
        this.writer.writeStartElement(
          "sx", "AttributeIntegerSigned3", namespaceURI);
        this.writer.writeAttribute("c0", Long.toString(x));
        this.writer.writeAttribute("c1", Long.toString(y));
        this.writer.writeAttribute("c2", Long.toString(z));
        this.writer.writeEndElement();
      } catch (final Exception e) {
        throw new IOException(e);
      }
    }

    @Override
    public void serializeValueIntegerSigned2(
      final long x,
      final long y)
      throws IOException, IllegalArgumentException, IllegalStateException
    {
      try {
        final var namespaceURI = SMFX.namespaceURI2p0();
        this.writer.writeStartElement(
          "sx", "AttributeIntegerSigned2", namespaceURI);
        this.writer.writeAttribute("c0", Long.toString(x));
        this.writer.writeAttribute("c1", Long.toString(y));
        this.writer.writeEndElement();
      } catch (final Exception e) {
        throw new IOException(e);
      }
    }

    @Override
    public void serializeValueIntegerSigned1(final long x)
      throws IOException, IllegalArgumentException, IllegalStateException
    {
      try {
        final var namespaceURI = SMFX.namespaceURI2p0();
        this.writer.writeStartElement(
          "sx", "AttributeIntegerSigned1", namespaceURI);
        this.writer.writeAttribute("c0", Long.toString(x));
        this.writer.writeEndElement();
      } catch (final Exception e) {
        throw new IOException(e);
      }
    }

    @Override
    public void serializeValueIntegerUnsigned4(
      final long x,
      final long y,
      final long z,
      final long w)
      throws IOException, IllegalArgumentException, IllegalStateException
    {
      try {
        final var namespaceURI = SMFX.namespaceURI2p0();
        this.writer.writeStartElement(
          "sx", "AttributeIntegerUnsigned4", namespaceURI);
        this.writer.writeAttribute("c0", Long.toUnsignedString(x));
        this.writer.writeAttribute("c1", Long.toUnsignedString(y));
        this.writer.writeAttribute("c2", Long.toUnsignedString(z));
        this.writer.writeAttribute("c3", Long.toUnsignedString(w));
        this.writer.writeEndElement();
      } catch (final Exception e) {
        throw new IOException(e);
      }
    }

    @Override
    public void serializeValueIntegerUnsigned3(
      final long x,
      final long y,
      final long z)
      throws IOException, IllegalArgumentException, IllegalStateException
    {
      try {
        final var namespaceURI = SMFX.namespaceURI2p0();
        this.writer.writeStartElement(
          "sx", "AttributeIntegerUnsigned3", namespaceURI);
        this.writer.writeAttribute("c0", Long.toUnsignedString(x));
        this.writer.writeAttribute("c1", Long.toUnsignedString(y));
        this.writer.writeAttribute("c2", Long.toUnsignedString(z));
        this.writer.writeEndElement();
      } catch (final Exception e) {
        throw new IOException(e);
      }
    }

    @Override
    public void serializeValueIntegerUnsigned2(
      final long x,
      final long y)
      throws IOException, IllegalArgumentException, IllegalStateException
    {
      try {
        final var namespaceURI = SMFX.namespaceURI2p0();
        this.writer.writeStartElement(
          "sx", "AttributeIntegerUnsigned2", namespaceURI);
        this.writer.writeAttribute("c0", Long.toUnsignedString(x));
        this.writer.writeAttribute("c1", Long.toUnsignedString(y));
        this.writer.writeEndElement();
      } catch (final Exception e) {
        throw new IOException(e);
      }
    }

    @Override
    public void serializeValueIntegerUnsigned1(final long x)
      throws IOException, IllegalArgumentException, IllegalStateException
    {
      try {
        final var namespaceURI = SMFX.namespaceURI2p0();
        this.writer.writeStartElement(
          "sx", "AttributeIntegerUnsigned1", namespaceURI);
        this.writer.writeAttribute("c0", Long.toUnsignedString(x));
        this.writer.writeEndElement();
      } catch (final Exception e) {
        throw new IOException(e);
      }
    }

    @Override
    public void close()
      throws IOException
    {
      try {
        this.writer.writeEndElement();
      } catch (final Exception e) {
        throw new IOException(e);
      }
    }
  }

  private static final class DataTriangles
    implements SMFSerializerDataTrianglesType
  {
    private final XMLStreamWriter writer;

    DataTriangles(
      final XMLStreamWriter inWriter)
    {
      this.writer = Objects.requireNonNull(inWriter, "writer");
    }

    DataTriangles start()
      throws IOException
    {
      try {
        final var namespaceURI = SMFX.namespaceURI2p0();
        this.writer.writeStartElement(
          "sx", "Triangles", namespaceURI);
        return this;
      } catch (final Exception e) {
        throw new IOException(e);
      }
    }

    @Override
    public void serializeTriangle(
      final long v0,
      final long v1,
      final long v2)
      throws IOException, IllegalStateException
    {
      try {
        final var namespaceURI = SMFX.namespaceURI2p0();
        this.writer.writeStartElement(
          "sx", "Triangle", namespaceURI);
        this.writer.writeAttribute("v0", Long.toUnsignedString(v0));
        this.writer.writeAttribute("v1", Long.toUnsignedString(v1));
        this.writer.writeAttribute("v2", Long.toUnsignedString(v2));
        this.writer.writeEndElement();
      } catch (final Exception e) {
        throw new IOException(e);
      }
    }

    @Override
    public void close()
      throws IOException
    {
      try {
        this.writer.writeEndElement();
      } catch (final Exception e) {
        throw new IOException(e);
      }
    }
  }

  private static final class DataAttributesNonInterleaved
    implements SMFSerializerDataAttributesNonInterleavedType
  {
    private final XMLStreamWriter writer;

    DataAttributesNonInterleaved(
      final XMLStreamWriter inWriter)
    {
      this.writer = Objects.requireNonNull(inWriter, "writer");
    }

    DataAttributesNonInterleaved start()
      throws IOException
    {
      try {
        final var namespaceURI = SMFX.namespaceURI2p0();
        this.writer.writeStartElement(
          "sx", "VertexDataNonInterleaved", namespaceURI);
        return this;
      } catch (final Exception e) {
        throw new IOException(e);
      }
    }

    @Override
    public SMFSerializerDataAttributesValuesType serializeData(
      final SMFAttributeName name)
      throws IllegalArgumentException, IllegalStateException, IOException
    {
      try {
        return new DataAttributeValues(this.writer, name).start();
      } catch (final Exception e) {
        throw new IOException(e);
      }
    }

    @Override
    public void close()
      throws IOException
    {
      try {
        this.writer.writeEndElement();
      } catch (final Exception e) {
        throw new IOException(e);
      }
    }
  }
}
