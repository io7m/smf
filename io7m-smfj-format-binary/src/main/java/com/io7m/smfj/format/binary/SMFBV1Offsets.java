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
import com.io7m.jnull.NullCheck;
import com.io7m.smfj.core.SMFAttribute;
import com.io7m.smfj.core.SMFAttributeName;
import com.io7m.smfj.core.SMFHeader;
import com.io7m.smfj.format.binary.v1.SMFBV1AttributeByteBuffered;
import javaslang.collection.HashMap;
import javaslang.collection.List;
import javaslang.collection.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Octet offsets for data within an SMFB 1.* file.
 */

public final class SMFBV1Offsets
{
  private static final long OFFSET_MAGIC_NUMBER;
  private static final long OFFSET_VERSION_MAJOR;
  private static final long OFFSET_VERSION_MINOR;
  private static final long OFFSET_HEADER;
  private static final long OFFSET_HEADER_VERTICES_COUNT;
  private static final long OFFSET_HEADER_TRIANGLES_COUNT;
  private static final long OFFSET_HEADER_TRIANGLES_SIZE;
  private static final long OFFSET_HEADER_ATTRIBUTES_COUNT;
  private static final long OFFSET_HEADER_ATTRIBUTES_DATA;
  private static final Logger LOG;

  static {
    LOG = LoggerFactory.getLogger(SMFBV1Offsets.class);

    OFFSET_MAGIC_NUMBER = 0L;
    OFFSET_VERSION_MAJOR = 8L;
    OFFSET_VERSION_MINOR = OFFSET_VERSION_MAJOR + 4L;

    Invariants.checkInvariant(
      SMFBV1AttributeByteBuffered.sizeInOctets() % 8 == 0,
      "Attribute size must be divisible by 8");

    OFFSET_HEADER = OFFSET_VERSION_MINOR + 4L;
    Invariants.checkInvariant(
      OFFSET_HEADER % 8L == 0L,
      "OFFSET_HEADER must be divisible by 8");

    OFFSET_HEADER_VERTICES_COUNT = OFFSET_HEADER;
    Invariants.checkInvariant(
      OFFSET_HEADER_VERTICES_COUNT % 8L == 0L,
      "OFFSET_HEADER_VERTICES_COUNT must be divisible by 8");

    OFFSET_HEADER_TRIANGLES_COUNT = OFFSET_HEADER_VERTICES_COUNT + 8L;
    Invariants.checkInvariant(
      OFFSET_HEADER_TRIANGLES_COUNT % 8L == 0L,
      "OFFSET_HEADER_TRIANGLES_COUNT must be divisible by 8");

    OFFSET_HEADER_TRIANGLES_SIZE = OFFSET_HEADER_TRIANGLES_COUNT + 8L;
    Invariants.checkInvariant(
      OFFSET_HEADER_TRIANGLES_SIZE % 4L == 0L,
      "OFFSET_HEADER_TRIANGLES_SIZE must be divisible by 4");

    OFFSET_HEADER_ATTRIBUTES_COUNT = OFFSET_HEADER_TRIANGLES_SIZE + 8L;
    Invariants.checkInvariant(
      OFFSET_HEADER_ATTRIBUTES_COUNT % 8L == 0L,
      "OFFSET_HEADER_ATTRIBUTES_COUNT must be divisible by 8");

    OFFSET_HEADER_ATTRIBUTES_DATA = OFFSET_HEADER_ATTRIBUTES_COUNT + 8L;
    Invariants.checkInvariant(
      OFFSET_HEADER_ATTRIBUTES_DATA % 8L == 0L,
      "OFFSET_HEADER_ATTRIBUTES_DATA must be divisible by 8");

    if (LOG.isTraceEnabled()) {
      LOG.trace(
        "OFFSET_HEADER:                  0x{}",
        Long.toUnsignedString(OFFSET_HEADER, 16));
      LOG.trace(
        "OFFSET_HEADER_VERTICES_COUNT:   0x{}",
        Long.toUnsignedString(OFFSET_HEADER_VERTICES_COUNT, 16));
      LOG.trace(
        "OFFSET_HEADER_TRIANGLES_COUNT:  0x{}",
        Long.toUnsignedString(OFFSET_HEADER_TRIANGLES_COUNT, 16));
      LOG.trace(
        "OFFSET_HEADER_TRIANGLES_SIZE:   0x{}",
        Long.toUnsignedString(OFFSET_HEADER_TRIANGLES_SIZE, 16));
      LOG.trace(
        "OFFSET_HEADER_ATTRIBUTES_COUNT: 0x{}",
        Long.toUnsignedString(OFFSET_HEADER_ATTRIBUTES_COUNT, 16));
      LOG.trace(
        "OFFSET_HEADER_ATTRIBUTES_DATA:  0x{}",
        Long.toUnsignedString(OFFSET_HEADER_ATTRIBUTES_DATA, 16));
    }
  }

  private final long vertices_data_offset;
  private final long triangles_data_offset;
  private final Map<SMFAttributeName, Long> attributes_offsets;

  private SMFBV1Offsets(
    final long in_vertices_data_offset,
    final long in_triangles_data_offset,
    final Map<SMFAttributeName, Long> in_attributes_offsets)
  {
    this.vertices_data_offset =
      in_vertices_data_offset;
    this.triangles_data_offset =
      in_triangles_data_offset;
    this.attributes_offsets =
      NullCheck.notNull(in_attributes_offsets, "Offsets");
  }

  /**
   * @return The offset in octets of the file's magic number
   */

  public static long offsetMagicNumber()
  {
    return OFFSET_MAGIC_NUMBER;
  }

  /**
   * @return The offset in octets of the file's major version
   */

  public static long offsetVersionMajor()
  {
    return OFFSET_VERSION_MAJOR;
  }

  /**
   * @return The offset in octets of the file's minor version
   */

  public static long offsetVersionMinor()
  {
    return OFFSET_VERSION_MINOR;
  }

  /**
   * @return The offset in octets of the file's header
   */

  public static long offsetHeader()
  {
    return OFFSET_HEADER;
  }

  /**
   * @return The offset in octets of the number of vertices in the file
   */

  public static long offsetHeaderVerticesCount()
  {
    return OFFSET_HEADER_VERTICES_COUNT;
  }

  /**
   * @return The offset in octets of the number of triangles in the file
   */

  public static long offsetHeaderTrianglesCount()
  {
    return OFFSET_HEADER_TRIANGLES_COUNT;
  }

  /**
   * @return The offset in octets of the size of triangle indices in the file
   */

  public static long offsetHeaderTrianglesSize()
  {
    return OFFSET_HEADER_TRIANGLES_SIZE;
  }

  /**
   * @return The offset in octets of the number of attributes in the file
   */

  public static long offsetHeaderAttributesCount()
  {
    return OFFSET_HEADER_ATTRIBUTES_COUNT;
  }

  /**
   * @return The offset in octets of the start of the attribute definitions in
   * the file
   */

  public static long offsetHeaderAttributesData()
  {
    return OFFSET_HEADER_ATTRIBUTES_DATA;
  }

  /**
   * Calculate offsets from the given header.
   *
   * @param header The header
   *
   * @return A value containing offsets that are valid for the given header
   */

  public static SMFBV1Offsets fromHeader(
    final SMFHeader header)
  {
    NullCheck.notNull(header, "Header");

    final List<SMFAttribute> attributes =
      header.attributesInOrder();

    Map<SMFAttributeName, Long> attributes_offsets =
      HashMap.empty();

    final long attribute_definitions_size = Math.multiplyExact(
      (long) SMFBV1AttributeByteBuffered.sizeInOctets(),
      (long) attributes.length());

    final long vertices_data_offset =
      Math.addExact(offsetHeaderAttributesData(), attribute_definitions_size);

    if (LOG.isDebugEnabled()) {
      LOG.debug(
        "vertex data offset: {}",
        Long.valueOf(vertices_data_offset));
    }

    long off = vertices_data_offset;
    for (final SMFAttribute attribute : attributes) {
      Invariants.checkInvariantL(
        off,
        off % 8L == 0L,
        off_now -> "Offset " + off_now + " must be divisible by 8");

      attributes_offsets =
        attributes_offsets.put(attribute.name(), Long.valueOf(off));

      if (LOG.isDebugEnabled()) {
        LOG.debug(
          "attribute data offset: {} {}",
          attribute.name().value(),
          Long.valueOf(off));
      }

      final long data_size = Math.multiplyExact(
        (long) attribute.componentSizeOctets(),
        (long) attribute.componentCount());

      final long data_size_padded =
        Math.multiplyExact(
          Math.floorDiv(Math.addExact(data_size, 8L), 8L),
          8L);

      off = Math.addExact(off, data_size_padded);
    }

    Invariants.checkInvariantL(
      off,
      off % 8L == 0L,
      off_now -> "Offset " + off_now + " must be divisible by 8");

    final long triangles_data_offset = off;
    if (LOG.isDebugEnabled()) {
      LOG.debug(
        "triangles offset: {}",
        Long.valueOf(triangles_data_offset));
    }

    return new SMFBV1Offsets(
      vertices_data_offset,
      triangles_data_offset,
      attributes_offsets);
  }

  /**
   * @return The offset in octets of the start of the triangle data in the file
   */

  public long trianglesDataOffset()
  {
    return this.triangles_data_offset;
  }

  /**
   * @return The offset in octets of the start of the vertex data in the file
   */

  public long verticesDataOffset()
  {
    return this.vertices_data_offset;
  }

  /**
   * @return A map containing the offset in octets of the data in the file for
   * each named attribute
   */

  public Map<SMFAttributeName, Long> attributeOffsets()
  {
    return this.attributes_offsets;
  }
}
