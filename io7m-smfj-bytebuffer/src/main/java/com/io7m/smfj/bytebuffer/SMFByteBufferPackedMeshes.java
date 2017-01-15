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

import com.io7m.jlexing.core.LexicalPosition;
import com.io7m.jnull.NullCheck;
import com.io7m.smfj.core.SMFAttribute;
import com.io7m.smfj.core.SMFAttributeName;
import com.io7m.smfj.core.SMFFormatVersion;
import com.io7m.smfj.core.SMFHeader;
import com.io7m.smfj.parser.api.SMFParseError;
import com.io7m.smfj.parser.api.SMFParserEventsMetaType;
import javaslang.Tuple;
import javaslang.Tuple2;
import javaslang.collection.List;
import javaslang.collection.SortedMap;
import javaslang.collection.TreeMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.file.Path;
import java.util.Optional;
import java.util.function.LongFunction;

/**
 * Functions for loading and packing meshes.
 */

public final class SMFByteBufferPackedMeshes implements
  SMFByteBufferPackedMeshLoaderType
{
  private static final Logger LOG;

  static {
    LOG = LoggerFactory.getLogger(SMFByteBufferPackedMeshes.class);
  }

  private final SMFByteBufferPackerEventsType events;
  private final SMFParserEventsMetaType meta;

  private List<SMFParseError> errors;
  private SortedMap<Integer, ByteBuffer> buffers_attr;
  private ByteBuffer buffer_tri;
  private SMFByteBufferPackedMesh mesh;
  private SMFByteBufferTrianglePacker packer_tri;
  private SortedMap<Integer, SMFByteBufferPackingConfiguration> config;
  private SortedMap<Integer, SMFByteBufferAttributePacker> attr_packers;

  private SMFByteBufferPackedMeshes(
    final SMFParserEventsMetaType in_meta,
    final SMFByteBufferPackerEventsType in_events)
  {
    this.meta = NullCheck.notNull(in_meta, "Meta");
    this.events = NullCheck.notNull(in_events, "Events");
  }

  /**
   * A convenient function for allocating a heap-based byte buffer with native
   * byte ordering
   *
   * @return A function that will allocate a heap-based byte buffer
   */

  public static LongFunction<Optional<ByteBuffer>> allocateByteBufferHeap()
  {
    return size -> {
      if (LOG.isDebugEnabled()) {
        LOG.debug("allocating {} octets", Long.valueOf(size));
      }
      final ByteBuffer b =
        ByteBuffer.allocate(Math.toIntExact(size))
          .order(ByteOrder.nativeOrder());
      return Optional.of(b);
    };
  }

  /**
   * Create a new mesh loader.
   *
   * @param in_meta   A metadata listener
   * @param in_events A buffer packing event listener
   *
   * @return A new mesh loader
   */

  public static SMFByteBufferPackedMeshLoaderType newLoader(
    final SMFParserEventsMetaType in_meta,
    final SMFByteBufferPackerEventsType in_events)
  {
    return new SMFByteBufferPackedMeshes(in_meta, in_events);
  }

  @Override
  public void onError(
    final SMFParseError e)
  {
    final LexicalPosition<Path> lex = e.lexical();
    LOG.error(
      "parse error: {}:{}:{}: {}",
      lex.file(),
      Integer.valueOf(lex.line()),
      Integer.valueOf(lex.column()),
      e.message());
    this.errors = this.errors.append(e);
  }

  @Override
  public void onStart()
  {
    this.errors = List.empty();
    this.buffers_attr = TreeMap.empty();
    this.attr_packers = TreeMap.empty();
    this.buffer_tri = null;
    this.mesh = null;
    this.packer_tri = null;
  }

  @Override
  public void onVersionReceived(
    final SMFFormatVersion version)
  {
    // Nothing to be done here
  }

  @Override
  public void onFinish()
  {
    if (this.errors.isEmpty()) {
      this.mesh = SMFByteBufferPackedMesh.of(
        this.config,
        this.buffers_attr,
        Optional.ofNullable(this.buffer_tri));
    }
  }

  @Override
  public void onHeaderParsed(
    final SMFHeader header)
  {
    this.config = this.events.onHeader(header);

    final boolean want_triangles = this.events.onShouldPackTriangles();
    if (want_triangles) {
      final long size_tri = Math.multiplyExact(
        header.triangleSizeOctets(), header.triangleCount());
      LOG.debug("allocating triangle buffer");
      this.buffer_tri = this.events.onAllocateTriangleBuffer(size_tri);
      this.packer_tri = new SMFByteBufferTrianglePacker(
        this.buffer_tri,
        Math.toIntExact(header.triangleIndexSizeBits()));
    } else {
      LOG.debug("no triangle buffer required");
    }

    this.buffers_attr = this.config.map((id, buffer_config) -> {
      final long size_attr =
        Math.multiplyExact(
          (long) buffer_config.vertexSizeOctets(),
          header.vertexCount());

      LOG.debug("allocating attribute buffer for {}", id);
      final ByteBuffer buffer =
        this.events.onAllocateAttributeBuffer(id, buffer_config, size_attr);
      return Tuple.of(id, buffer);
    });
  }

  @Override
  public List<SMFParseError> errors()
  {
    return this.errors;
  }

  @Override
  public SMFByteBufferPackedMesh mesh()
    throws IllegalStateException
  {
    if (this.errors.isEmpty()) {
      return this.mesh;
    }

    throw new IllegalStateException("Buffer packer has failed");
  }

  @Override
  public boolean onMeta(
    final long vendor,
    final long schema,
    final long length)
  {
    return this.meta.onMeta(vendor, schema, length);
  }

  @Override
  public void onMetaData(
    final long vendor,
    final long schema,
    final byte[] data)
  {
    this.meta.onMetaData(vendor, schema, data);
  }

  @Override
  public void onDataTrianglesStart()
  {
    if (this.packer_tri != null) {
      this.packer_tri.onDataTrianglesStart();
    }
  }

  @Override
  public void onDataTriangle(
    final long v0,
    final long v1,
    final long v2)
  {
    if (this.packer_tri != null) {
      this.packer_tri.onDataTriangle(v0, v1, v2);
    }
  }

  @Override
  public void onDataTrianglesFinish()
  {
    if (this.packer_tri != null) {
      this.packer_tri.onDataTrianglesFinish();
    }
  }

  @Override
  public void onDataAttributeStart(
    final SMFAttribute attribute)
  {
    TreeMap<Integer, SMFByteBufferAttributePacker> packers = TreeMap.empty();

    final SMFAttributeName name = attribute.name();
    for (final Tuple2<Integer, SMFByteBufferPackingConfiguration> p : this.config) {
      final Integer id = p._1;
      final SMFByteBufferPackingConfiguration bc = p._2;
      final SortedMap<SMFAttributeName, SMFByteBufferPackedAttribute> by_name =
        bc.packedAttributesByName();
      if (by_name.containsKey(name)) {
        final ByteBuffer b = this.buffers_attr.get(id).get();
        final SMFByteBufferPackedAttribute pa = by_name.get(name).get();
        final SMFByteBufferAttributePacker packer =
          new SMFByteBufferAttributePacker(b, bc, pa);
        packers = packers.put(id, packer);
      }
    }

    this.attr_packers = packers;
  }

  @Override
  public void onDataAttributeValueIntegerSigned1(
    final long x)
  {
    for (final SMFByteBufferAttributePacker p : this.attr_packers.values()) {
      p.onDataAttributeValueIntegerSigned1(x);
    }
  }

  @Override
  public void onDataAttributeValueIntegerSigned2(
    final long x,
    final long y)
  {
    for (final SMFByteBufferAttributePacker p : this.attr_packers.values()) {
      p.onDataAttributeValueIntegerSigned2(x, y);
    }
  }

  @Override
  public void onDataAttributeValueIntegerSigned3(
    final long x,
    final long y,
    final long z)
  {
    for (final SMFByteBufferAttributePacker p : this.attr_packers.values()) {
      p.onDataAttributeValueIntegerSigned3(x, y, z);
    }
  }

  @Override
  public void onDataAttributeValueIntegerSigned4(
    final long x,
    final long y,
    final long z,
    final long w)
  {
    for (final SMFByteBufferAttributePacker p : this.attr_packers.values()) {
      p.onDataAttributeValueIntegerSigned4(x, y, z, w);
    }
  }

  @Override
  public void onDataAttributeValueIntegerUnsigned1(
    final long x)
  {
    for (final SMFByteBufferAttributePacker p : this.attr_packers.values()) {
      p.onDataAttributeValueIntegerUnsigned1(x);
    }
  }

  @Override
  public void onDataAttributeValueIntegerUnsigned2(
    final long x,
    final long y)
  {
    for (final SMFByteBufferAttributePacker p : this.attr_packers.values()) {
      p.onDataAttributeValueIntegerUnsigned2(x, y);
    }
  }

  @Override
  public void onDataAttributeValueIntegerUnsigned3(
    final long x,
    final long y,
    final long z)
  {
    for (final SMFByteBufferAttributePacker p : this.attr_packers.values()) {
      p.onDataAttributeValueIntegerUnsigned3(x, y, z);
    }
  }

  @Override
  public void onDataAttributeValueIntegerUnsigned4(
    final long x,
    final long y,
    final long z,
    final long w)
  {
    for (final SMFByteBufferAttributePacker p : this.attr_packers.values()) {
      p.onDataAttributeValueIntegerUnsigned4(x, y, z, w);
    }
  }

  @Override
  public void onDataAttributeValueFloat1(
    final double x)
  {
    for (final SMFByteBufferAttributePacker p : this.attr_packers.values()) {
      p.onDataAttributeValueFloat1(x);
    }
  }

  @Override
  public void onDataAttributeValueFloat2(
    final double x,
    final double y)
  {
    for (final SMFByteBufferAttributePacker p : this.attr_packers.values()) {
      p.onDataAttributeValueFloat2(x, y);
    }
  }

  @Override
  public void onDataAttributeValueFloat3(
    final double x,
    final double y,
    final double z)
  {
    for (final SMFByteBufferAttributePacker p : this.attr_packers.values()) {
      p.onDataAttributeValueFloat3(x, y, z);
    }
  }

  @Override
  public void onDataAttributeValueFloat4(
    final double x,
    final double y,
    final double z,
    final double w)
  {
    for (final SMFByteBufferAttributePacker p : this.attr_packers.values()) {
      p.onDataAttributeValueFloat4(x, y, z, w);
    }
  }

  @Override
  public void onDataAttributeFinish(
    final SMFAttribute attribute)
  {
    // Nothing to be done here
  }
}
