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
import javaslang.collection.List;
import javaslang.collection.SortedMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.file.Path;
import java.util.Optional;
import java.util.function.Function;
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

  private final Function<SMFHeader, SMFByteBufferPackingConfiguration> on_config;
  private final LongFunction<Optional<ByteBuffer>> on_allocate_attribute;
  private final LongFunction<Optional<ByteBuffer>> on_allocate_tri;
  private final SMFParserEventsMetaType meta;
  private List<SMFParseError> errors;
  private SMFByteBufferPackingConfiguration config;
  private Optional<ByteBuffer> buffer_attr;
  private Optional<ByteBuffer> buffer_tri;
  private SMFByteBufferPackedMesh mesh;
  private SMFByteBufferTrianglePacker packer_tri;
  private SMFByteBufferAttributePacker packer_attr;

  private SMFByteBufferPackedMeshes(
    final SMFParserEventsMetaType in_meta,
    final Function<SMFHeader, SMFByteBufferPackingConfiguration> in_on_config,
    final LongFunction<Optional<ByteBuffer>> in_on_allocate_attribute,
    final LongFunction<Optional<ByteBuffer>> in_on_allocate_index)
  {
    this.meta =
      NullCheck.notNull(in_meta, "Meta");
    this.on_config =
      NullCheck.notNull(in_on_config, "On config");
    this.on_allocate_attribute =
      NullCheck.notNull(in_on_allocate_attribute, "On allocate attribute");
    this.on_allocate_tri =
      NullCheck.notNull(in_on_allocate_index, "On allocate index");
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
   * @param in_meta               A metadata listener
   * @param on_config             A function that will produce a byte buffer
   *                              packing configuration when given a parsed
   *                              header
   * @param on_allocate_attribute A function that will allocate a byte buffer of
   *                              the given size for attribute data
   * @param on_allocate_triangles A function that will allocate a byte buffer of
   *                              the given size for triangle data
   *
   * @return A new mesh loader
   */

  public static SMFByteBufferPackedMeshLoaderType newLoader(
    final SMFParserEventsMetaType in_meta,
    final Function<SMFHeader, SMFByteBufferPackingConfiguration> on_config,
    final LongFunction<Optional<ByteBuffer>> on_allocate_attribute,
    final LongFunction<Optional<ByteBuffer>> on_allocate_triangles)
  {
    return new SMFByteBufferPackedMeshes(
      in_meta, on_config, on_allocate_attribute, on_allocate_triangles);
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
    this.config = null;
    this.buffer_attr = null;
    this.buffer_tri = null;
    this.mesh = null;
    this.packer_tri = null;
    this.packer_attr = null;
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
        this.config, this.buffer_attr, this.buffer_tri);
    }
  }

  @Override
  public void onHeaderParsed(
    final SMFHeader header)
  {
    this.config = this.on_config.apply(header);

    final long size_attr =
      Math.multiplyExact(
        (long) this.config.vertexSizeOctets(),
        header.vertexCount());

    final long size_tri =
      Math.multiplyExact(header.triangleSizeOctets(), header.triangleCount());

    this.buffer_attr = this.on_allocate_attribute.apply(size_attr);
    this.buffer_tri = this.on_allocate_tri.apply(size_tri);

    this.buffer_tri.ifPresent(
      buffer -> this.packer_tri =
        new SMFByteBufferTrianglePacker(
          buffer, Math.toIntExact(header.triangleIndexSizeBits())));
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
    final SortedMap<SMFAttributeName, SMFByteBufferPackedAttribute> by_name =
      this.config.packedAttributesByName();
    if (by_name.containsKey(attribute.name()) && this.buffer_attr.isPresent()) {
      final SMFByteBufferPackedAttribute packed_attr =
        by_name.get(attribute.name()).get();
      this.packer_attr =
        new SMFByteBufferAttributePacker(
          this.buffer_attr.get(),
          this.config,
          packed_attr);
    } else {
      this.packer_attr = null;
    }
  }

  @Override
  public void onDataAttributeValueIntegerSigned1(
    final long x)
  {
    if (this.packer_attr != null) {
      this.packer_attr.onDataAttributeValueIntegerSigned1(x);
    }
  }

  @Override
  public void onDataAttributeValueIntegerSigned2(
    final long x,
    final long y)
  {
    if (this.packer_attr != null) {
      this.packer_attr.onDataAttributeValueIntegerSigned2(x, y);
    }
  }

  @Override
  public void onDataAttributeValueIntegerSigned3(
    final long x,
    final long y,
    final long z)
  {
    if (this.packer_attr != null) {
      this.packer_attr.onDataAttributeValueIntegerSigned3(x, y, z);
    }
  }

  @Override
  public void onDataAttributeValueIntegerSigned4(
    final long x,
    final long y,
    final long z,
    final long w)
  {
    if (this.packer_attr != null) {
      this.packer_attr.onDataAttributeValueIntegerSigned4(x, y, z, w);
    }
  }

  @Override
  public void onDataAttributeValueIntegerUnsigned1(
    final long x)
  {
    if (this.packer_attr != null) {
      this.packer_attr.onDataAttributeValueIntegerUnsigned1(x);
    }
  }

  @Override
  public void onDataAttributeValueIntegerUnsigned2(
    final long x,
    final long y)
  {
    if (this.packer_attr != null) {
      this.packer_attr.onDataAttributeValueIntegerUnsigned2(x, y);
    }
  }

  @Override
  public void onDataAttributeValueIntegerUnsigned3(
    final long x,
    final long y,
    final long z)
  {
    if (this.packer_attr != null) {
      this.packer_attr.onDataAttributeValueIntegerUnsigned3(x, y, z);
    }
  }

  @Override
  public void onDataAttributeValueIntegerUnsigned4(
    final long x,
    final long y,
    final long z,
    final long w)
  {
    if (this.packer_attr != null) {
      this.packer_attr.onDataAttributeValueIntegerUnsigned4(x, y, z, w);
    }
  }

  @Override
  public void onDataAttributeValueFloat1(
    final double x)
  {
    if (this.packer_attr != null) {
      this.packer_attr.onDataAttributeValueFloat1(x);
    }
  }

  @Override
  public void onDataAttributeValueFloat2(
    final double x,
    final double y)
  {
    if (this.packer_attr != null) {
      this.packer_attr.onDataAttributeValueFloat2(x, y);
    }
  }

  @Override
  public void onDataAttributeValueFloat3(
    final double x,
    final double y,
    final double z)
  {
    if (this.packer_attr != null) {
      this.packer_attr.onDataAttributeValueFloat3(x, y, z);
    }
  }

  @Override
  public void onDataAttributeValueFloat4(
    final double x,
    final double y,
    final double z,
    final double w)
  {
    if (this.packer_attr != null) {
      this.packer_attr.onDataAttributeValueFloat4(x, y, z, w);
    }
  }

  @Override
  public void onDataAttributeFinish(
    final SMFAttribute attribute)
  {
    // Nothing to be done here
  }
}
