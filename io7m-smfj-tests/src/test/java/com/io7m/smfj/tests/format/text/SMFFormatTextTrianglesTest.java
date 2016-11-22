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

package com.io7m.smfj.tests.format.text;

import com.io7m.smfj.core.SMFAttribute;
import com.io7m.smfj.core.SMFFormatVersion;
import com.io7m.smfj.core.SMFHeader;
import com.io7m.smfj.core.SMFVendorSchemaIdentifier;
import com.io7m.smfj.format.text.SMFFormatText;
import com.io7m.smfj.parser.api.SMFParserEventsType;
import com.io7m.smfj.parser.api.SMFParserSequentialType;
import com.io7m.smfj.serializer.api.SMFSerializerType;
import javaslang.Tuple;
import javaslang.collection.HashMap;
import javaslang.collection.List;
import mockit.Mocked;
import mockit.StrictExpectations;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

public final class SMFFormatTextTrianglesTest extends SMFTextTest
{
  private static final Logger LOG;

  static {
    LOG = LoggerFactory.getLogger(SMFFormatTextTrianglesTest.class);
  }

  @Rule public final ExpectedException expected = ExpectedException.none();

  @Test
  public void testTriangles8(
    final @Mocked SMFParserEventsType events)
  {
    final SMFHeader.Builder hb = SMFHeader.builder();
    hb.setAttributesInOrder(List.empty());
    hb.setAttributesByName(HashMap.empty());
    hb.setTriangleCount(2L);
    hb.setTriangleIndexSizeBits(8L);
    hb.setVertexCount(0L);
    hb.setSchemaIdentifier(
      SMFVendorSchemaIdentifier.of(0x696F376D, 0xA0B0C0D0, 1, 2));
    final SMFHeader h = hb.build();

    new StrictExpectations()
    {{
      events.onStart();
      events.onVersionReceived(SMFFormatVersion.of(1, 0));
      events.onHeaderParsed(h);

      events.onDataTrianglesStart();
      events.onDataTriangle(0L, 1L, 2L);
      events.onDataTriangle(0L, 2L, 3L);
      events.onDataTrianglesFinish();
    }};

    final SMFParserSequentialType p = this.parserFor(events, out -> {
      out.put("smf 1 0");
      out.put("schema 696F376D A0B0C0D0 1 2");
      out.put("vertices 0");
      out.put("triangles 2 8");
      out.put("data");

      out.put("triangles");
      out.put("0 1 2");
      out.put("0 2 3");
    });

    p.parse();
  }

  @Test
  public void testTriangles16(
    final @Mocked SMFParserEventsType events)
  {
    final SMFHeader.Builder hb = SMFHeader.builder();
    hb.setAttributesInOrder(List.empty());
    hb.setAttributesByName(HashMap.empty());
    hb.setTriangleCount(2L);
    hb.setTriangleIndexSizeBits(16L);
    hb.setVertexCount(0L);
    hb.setSchemaIdentifier(
      SMFVendorSchemaIdentifier.of(0x696F376D, 0xA0B0C0D0, 1, 2));
    final SMFHeader h = hb.build();

    new StrictExpectations()
    {{
      events.onStart();
      events.onVersionReceived(SMFFormatVersion.of(1, 0));
      events.onHeaderParsed(h);

      events.onDataTrianglesStart();
      events.onDataTriangle(0L, 1L, 2L);
      events.onDataTriangle(0L, 2L, 3L);
      events.onDataTrianglesFinish();
    }};

    final SMFParserSequentialType p = this.parserFor(events, out -> {
      out.put("smf 1 0");
      out.put("schema 696F376D A0B0C0D0 1 2");
      out.put("vertices 0");
      out.put("triangles 2 16");
      out.put("data");

      out.put("triangles");
      out.put("0 1 2");
      out.put("0 2 3");
    });

    p.parse();
  }

  @Test
  public void testTriangles32(
    final @Mocked SMFParserEventsType events)
  {
    final SMFHeader.Builder hb = SMFHeader.builder();
    hb.setAttributesInOrder(List.empty());
    hb.setAttributesByName(HashMap.empty());
    hb.setTriangleCount(2L);
    hb.setTriangleIndexSizeBits(32L);
    hb.setVertexCount(0L);
    hb.setSchemaIdentifier(
      SMFVendorSchemaIdentifier.of(0x696F376D, 0xA0B0C0D0, 1, 2));
    final SMFHeader h = hb.build();

    new StrictExpectations()
    {{
      events.onStart();
      events.onVersionReceived(SMFFormatVersion.of(1, 0));
      events.onHeaderParsed(h);

      events.onDataTrianglesStart();
      events.onDataTriangle(0L, 1L, 2L);
      events.onDataTriangle(0L, 2L, 3L);
      events.onDataTrianglesFinish();
    }};

    final SMFParserSequentialType p = this.parserFor(events, out -> {
      out.put("smf 1 0");
      out.put("schema 696F376D A0B0C0D0 1 2");
      out.put("vertices 0");
      out.put("triangles 2 32");
      out.put("data");

      out.put("triangles");
      out.put("0 1 2");
      out.put("0 2 3");
    });

    p.parse();
  }

  @Test
  public void testSerializeTriangles8()
    throws IOException
  {
    final ByteArrayOutputStream out = new ByteArrayOutputStream();
    final Path path = Paths.get("/data");
    final SMFFormatVersion version = SMFFormatVersion.of(1, 0);

    final SMFSerializerType serializer =
      new SMFFormatText().serializerCreate(version, path, out);

    final List<SMFAttribute> attributes = List.empty();
    final SMFHeader.Builder header_b = SMFHeader.builder();
    header_b.setVertexCount(0L);
    header_b.setTriangleIndexSizeBits(8L);
    header_b.setTriangleCount(1L);
    header_b.setAttributesInOrder(attributes);
    header_b.setAttributesByName(attributes.toMap(a -> Tuple.of(a.name(), a)));
    header_b.setSchemaIdentifier(
      SMFVendorSchemaIdentifier.of(0x696F376D, 0xA0B0C0D0, 1, 2));
    final SMFHeader header = header_b.build();

    serializer.serializeHeader(header);
    serializer.serializeTriangle(0L, 1L, 2L);
  }

  @Test
  public void testSerializeTriangles16()
    throws IOException
  {
    final ByteArrayOutputStream out = new ByteArrayOutputStream();
    final Path path = Paths.get("/data");
    final SMFFormatVersion version = SMFFormatVersion.of(1, 0);

    final SMFSerializerType serializer =
      new SMFFormatText().serializerCreate(version, path, out);

    final List<SMFAttribute> attributes = List.empty();
    final SMFHeader.Builder header_b = SMFHeader.builder();
    header_b.setVertexCount(0L);
    header_b.setTriangleIndexSizeBits(16L);
    header_b.setTriangleCount(1L);
    header_b.setAttributesInOrder(attributes);
    header_b.setAttributesByName(attributes.toMap(a -> Tuple.of(a.name(), a)));
    header_b.setSchemaIdentifier(
      SMFVendorSchemaIdentifier.of(0x696F376D, 0xA0B0C0D0, 1, 2));
    final SMFHeader header = header_b.build();

    serializer.serializeHeader(header);
    serializer.serializeTriangle(0L, 1L, 2L);
  }

  @Test
  public void testSerializeTriangles32()
    throws IOException
  {
    final ByteArrayOutputStream out = new ByteArrayOutputStream();
    final Path path = Paths.get("/data");
    final SMFFormatVersion version = SMFFormatVersion.of(1, 0);

    final SMFSerializerType serializer =
      new SMFFormatText().serializerCreate(version, path, out);

    final List<SMFAttribute> attributes = List.empty();
    final SMFHeader.Builder header_b = SMFHeader.builder();
    header_b.setVertexCount(0L);
    header_b.setTriangleIndexSizeBits(32L);
    header_b.setTriangleCount(1L);
    header_b.setAttributesInOrder(attributes);
    header_b.setAttributesByName(attributes.toMap(a -> Tuple.of(a.name(), a)));
    header_b.setSchemaIdentifier(
      SMFVendorSchemaIdentifier.of(0x696F376D, 0xA0B0C0D0, 1, 2));
    final SMFHeader header = header_b.build();

    serializer.serializeHeader(header);
    serializer.serializeTriangle(0L, 1L, 2L);
  }

  @Test
  public void testSerializeTriangles64()
    throws IOException
  {
    final ByteArrayOutputStream out = new ByteArrayOutputStream();
    final Path path = Paths.get("/data");
    final SMFFormatVersion version = SMFFormatVersion.of(1, 0);

    final SMFSerializerType serializer =
      new SMFFormatText().serializerCreate(version, path, out);

    final List<SMFAttribute> attributes = List.empty();
    final SMFHeader.Builder header_b = SMFHeader.builder();
    header_b.setVertexCount(0L);
    header_b.setTriangleIndexSizeBits(64L);
    header_b.setTriangleCount(1L);
    header_b.setAttributesInOrder(attributes);
    header_b.setAttributesByName(attributes.toMap(a -> Tuple.of(a.name(), a)));
    header_b.setSchemaIdentifier(
      SMFVendorSchemaIdentifier.of(0x696F376D, 0xA0B0C0D0, 1, 2));
    final SMFHeader header = header_b.build();

    serializer.serializeHeader(header);
    serializer.serializeTriangle(0L, 1L, 2L);
  }

  @Test
  public void testSerializeTrianglesTooMany()
    throws IOException
  {
    final ByteArrayOutputStream out = new ByteArrayOutputStream();
    final Path path = Paths.get("/data");
    final SMFFormatVersion version = SMFFormatVersion.of(1, 0);

    final SMFSerializerType serializer =
      new SMFFormatText().serializerCreate(version, path, out);

    final List<SMFAttribute> attributes = List.empty();
    final SMFHeader.Builder header_b = SMFHeader.builder();
    header_b.setVertexCount(0L);
    header_b.setTriangleIndexSizeBits(8L);
    header_b.setTriangleCount(1L);
    header_b.setAttributesInOrder(attributes);
    header_b.setAttributesByName(attributes.toMap(a -> Tuple.of(a.name(), a)));
    header_b.setSchemaIdentifier(
      SMFVendorSchemaIdentifier.of(0x696F376D, 0xA0B0C0D0, 1, 2));
    final SMFHeader header = header_b.build();

    serializer.serializeHeader(header);
    serializer.serializeTriangle(0L, 1L, 2L);

    this.expected.expect(IllegalStateException.class);
    serializer.serializeTriangle(0L, 1L, 2L);
  }
}
