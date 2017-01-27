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

import com.io7m.junreachable.UnreachableCodeException;
import com.io7m.smfj.core.SMFAttribute;
import com.io7m.smfj.core.SMFHeader;
import com.io7m.smfj.core.SMFSchemaIdentifier;
import com.io7m.smfj.core.SMFTriangles;
import com.io7m.smfj.format.binary.v1.SMFBV1HeaderReadableType;
import com.io7m.smfj.format.binary.v1.SMFBV1SchemaIDReadableType;
import javaslang.collection.List;

/**
 * Shared binary parser code.
 */

final class SMFBV1
{
  private SMFBV1()
  {
    throw new UnreachableCodeException();
  }

  public static SMFHeader header(
    final SMFBV1HeaderReadableType header_view,
    final List<SMFAttribute> attributes)
  {
    final SMFBV1SchemaIDReadableType schema_id_view =
      header_view.getSchemaReadable();

    final SMFSchemaIdentifier.Builder schema_b =
      SMFSchemaIdentifier.builder();
    schema_b.setVendorID(schema_id_view.getVendorId());
    schema_b.setSchemaID(schema_id_view.getSchemaId());
    schema_b.setSchemaMajorVersion(schema_id_view.getSchemaVersionMajor());
    schema_b.setSchemaMinorVersion(schema_id_view.getSchemaVersionMinor());

    final SMFTriangles triangles =
      SMFTriangles.of(
        header_view.getTriangleCount(),
        (long) header_view.getTriangleIndexSizeBits());

    final SMFHeader.Builder header_b = SMFHeader.builder();
    header_b.setVertexCount(
      header_view.getVertexCount());
    header_b.setTriangles(triangles);
    header_b.setAttributesInOrder(
      attributes);
    header_b.setSchemaIdentifier(
      schema_b.build());
    header_b.setCoordinateSystem(
      SMFBCoordinateSystems.unpack(
        header_view.getCoordinateSystemReadable()));
    header_b.setMetaCount(
      (long) header_view.getMetaCount());

    return header_b.build();
  }
}
