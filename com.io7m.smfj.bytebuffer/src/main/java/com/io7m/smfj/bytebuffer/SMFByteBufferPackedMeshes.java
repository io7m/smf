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

import com.io7m.jaffirm.core.Invariants;
import com.io7m.jnull.NullCheck;
import com.io7m.smfj.core.SMFAttribute;
import com.io7m.smfj.core.SMFAttributeName;
import com.io7m.smfj.core.SMFErrorType;
import com.io7m.smfj.core.SMFFormatVersion;
import com.io7m.smfj.core.SMFHeader;
import com.io7m.smfj.core.SMFSchemaIdentifier;
import com.io7m.smfj.core.SMFTriangles;
import com.io7m.smfj.core.SMFWarningType;
import com.io7m.smfj.parser.api.SMFParserEventsBodyType;
import com.io7m.smfj.parser.api.SMFParserEventsDataAttributeValuesType;
import com.io7m.smfj.parser.api.SMFParserEventsDataAttributesNonInterleavedType;
import com.io7m.smfj.parser.api.SMFParserEventsDataMetaOptionalSupplierType;
import com.io7m.smfj.parser.api.SMFParserEventsDataMetaType;
import com.io7m.smfj.parser.api.SMFParserEventsDataTrianglesType;
import com.io7m.smfj.parser.api.SMFParserEventsHeaderType;
import javaslang.Tuple;
import javaslang.Tuple2;
import javaslang.collection.List;
import javaslang.collection.SortedMap;
import javaslang.collection.TreeMap;
import javaslang.control.Validation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Optional;
import java.util.function.LongFunction;

/**
 * Functions for loading and packing meshes.
 */

public final class SMFByteBufferPackedMeshes
  implements SMFByteBufferPackedMeshLoaderType,
  SMFParserEventsHeaderType,
  SMFParserEventsBodyType,
  SMFParserEventsDataAttributesNonInterleavedType,
  SMFParserEventsDataTrianglesType,
  SMFParserEventsDataAttributeValuesType
{
  private static final Logger LOG;

  static {
    LOG = LoggerFactory.getLogger(SMFByteBufferPackedMeshes.class);
  }

  private final SMFByteBufferPackerEventsType events;
  private final SMFParserEventsDataMetaOptionalSupplierType meta;
  private List<SMFWarningType> warnings;
  private List<SMFErrorType> errors;
  private ByteBuffer tri_buffer;
  private SMFByteBufferPackedMesh mesh;
  private SMFByteBufferTrianglePacker tri_packer;
  private SortedMap<Integer, SMFByteBufferPackingConfiguration> config;
  private SortedMap<Integer, SMFByteBufferAttributePacker> attr_packers;
  private SortedMap<Integer, ByteBuffer> attr_buffers;
  private SMFHeader header;

  private SMFByteBufferPackedMeshes(
    final SMFParserEventsDataMetaOptionalSupplierType in_meta,
    final SMFByteBufferPackerEventsType in_events)
  {
    this.meta = NullCheck.notNull(in_meta, "Meta");
    this.events = NullCheck.notNull(in_events, "Events");
    this.errors = List.empty();
    this.warnings = List.empty();
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
   * @param in_meta   A metadata supplier
   * @param in_events A buffer packing event listener
   *
   * @return A new mesh loader
   */

  public static SMFByteBufferPackedMeshLoaderType
  newLoader(
    final SMFParserEventsDataMetaOptionalSupplierType in_meta,
    final SMFByteBufferPackerEventsType in_events)
  {
    return new SMFByteBufferPackedMeshes(in_meta, in_events);
  }

  @Override
  public void onError(
    final SMFErrorType e)
  {
    LOG.error("parse error: {}", e.fullMessage());
    this.errors = this.errors.append(e);
  }

  @Override
  public void onWarning(
    final SMFWarningType w)
  {
    LOG.warn("parse warning: {}", w.fullMessage());
    this.warnings = this.warnings.append(w);
  }

  @Override
  public void onStart()
  {
    this.errors = List.empty();
    this.attr_buffers = TreeMap.empty();
    this.attr_packers = TreeMap.empty();
    this.mesh = null;
    this.header = null;
    this.tri_buffer = null;
    this.tri_packer = null;
  }

  @Override
  public Optional<SMFParserEventsHeaderType> onVersionReceived(
    final SMFFormatVersion version)
  {
    return Optional.of(this);
  }

  @Override
  public void onFinish()
  {
    if (this.errors.isEmpty()) {
      Invariants.checkInvariantI(
        this.attr_buffers.size(),
        this.attr_buffers.size() == this.config.size(),
        x -> "Must have correct number of buffers");

      final SMFByteBufferPackedMesh.Builder mb =
        SMFByteBufferPackedMesh.builder();
      for (final Tuple2<Integer, ByteBuffer> p : this.attr_buffers) {
        mb.addAttributeSets(SMFByteBufferPackedAttributeSet.of(
          p._1.intValue(),
          this.config.get(p._1).get(),
          p._2));
      }

      if (this.tri_buffer != null) {
        final SMFTriangles triangles = this.header.triangles();
        mb.setTriangles(SMFByteBufferPackedTriangles.of(
          this.tri_buffer,
          triangles.triangleCount(),
          Math.toIntExact(triangles.triangleIndexSizeBits())));
      }

      mb.setHeader(this.header);
      this.mesh = mb.build();
    }
  }

  @Override
  public List<SMFErrorType> errors()
  {
    return this.errors;
  }

  @Override
  public List<SMFWarningType> warnings()
  {
    return this.warnings;
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
  public void onDataTriangle(
    final long v0,
    final long v1,
    final long v2)
  {
    if (this.tri_packer != null) {
      this.tri_packer.onDataTriangle(v0, v1, v2);
    }
  }

  @Override
  public void onDataTrianglesFinish()
  {
    if (this.tri_packer != null) {
      this.tri_packer.onDataTrianglesFinish();
    }
  }

  @Override
  public Optional<SMFParserEventsDataAttributeValuesType> onDataAttributeStart(
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
        final ByteBuffer b = this.attr_buffers.get(id).get();
        final SMFByteBufferPackedAttribute pa = by_name.get(name).get();
        final SMFByteBufferAttributePacker packer =
          new SMFByteBufferAttributePacker(
            this,
            b,
            bc,
            pa,
            this.header.vertexCount());
        packers = packers.put(id, packer);
      }
    }

    this.attr_packers = packers;
    return Optional.of(this);
  }

  @Override
  public void onDataAttributesNonInterleavedFinish()
  {
    // Nothing
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
  public void onDataAttributeValueFinish()
  {
    // Nothing
  }

  @Override
  public Optional<SMFParserEventsBodyType> onHeaderParsed(
    final SMFHeader in_header)
  {
    this.header = NullCheck.notNull(in_header, "Header");

    final Validation<List<SMFErrorType>, SortedMap<Integer, SMFByteBufferPackingConfiguration>> result =
      this.events.onHeader(this.header);
    if (result.isInvalid()) {
      this.errors = this.errors.appendAll(result.getError());
      return Optional.empty();
    }

    this.config = result.get();

    final boolean want_triangles = this.events.onShouldPackTriangles();
    if (want_triangles) {
      final SMFTriangles triangles = this.header.triangles();
      final long size_tri =
        Math.multiplyExact(
          triangles.triangleSizeOctets(),
          triangles.triangleCount());

      LOG.debug("allocating triangle buffer");
      this.tri_buffer =
        this.events.onAllocateTriangleBuffer(triangles, size_tri);

      Invariants.checkInvariantL(
        (long) this.tri_buffer.capacity(),
        (long) this.tri_buffer.capacity() == size_tri,
        x -> "Triangle buffer size must be " + size_tri);

      this.tri_packer = new SMFByteBufferTrianglePacker(
        this,
        this.tri_buffer,
        Math.toIntExact(triangles.triangleIndexSizeBits()),
        triangles.triangleCount());
    } else {
      LOG.debug("no triangle buffer required");
    }

    this.attr_buffers = this.config.map((id, buffer_config) -> {
      final long size_attr =
        Math.multiplyExact(
          (long) buffer_config.vertexSizeOctets(),
          this.header.vertexCount());

      LOG.debug("allocating attribute buffer for {}", id);
      final ByteBuffer buffer =
        this.events.onAllocateAttributeBuffer(id, buffer_config, size_attr);

      Invariants.checkInvariantL(
        (long) buffer.capacity(),
        (long) buffer.capacity() == size_attr,
        x -> "Attribute buffer size must be " + size_attr);

      return Tuple.of(id, buffer);
    });

    return Optional.of(this);
  }

  @Override
  public Optional<SMFParserEventsDataAttributesNonInterleavedType> onAttributesNonInterleaved()
  {
    return Optional.of(this);
  }

  @Override
  public Optional<SMFParserEventsDataTrianglesType> onTriangles()
  {
    if (this.tri_packer != null) {
      return Optional.of(this);
    }

    return Optional.empty();
  }

  @Override
  public Optional<SMFParserEventsDataMetaType> onMeta(
    final SMFSchemaIdentifier schema)
  {
    return this.meta.onMeta(schema);
  }
}
