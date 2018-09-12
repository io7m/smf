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

package com.io7m.smfj.format.obj;

import com.io7m.jaffirm.core.Preconditions;
import com.io7m.jcoords.core.conversion.CAxisSystem;
import com.io7m.jlexing.core.LexicalPosition;
import com.io7m.jlexing.core.LexicalPositionType;
import com.io7m.jlexing.core.LexicalPositions;
import com.io7m.jobj.core.JOParser;
import com.io7m.jobj.core.JOParserErrorCode;
import com.io7m.jobj.core.JOParserType;
import com.io7m.jtensors.core.unparameterized.vectors.Vector2D;
import com.io7m.jtensors.core.unparameterized.vectors.Vector3D;
import com.io7m.smfj.core.SMFAttribute;
import com.io7m.smfj.core.SMFAttributeName;
import com.io7m.smfj.core.SMFComponentType;
import com.io7m.smfj.core.SMFCoordinateSystem;
import com.io7m.smfj.core.SMFFormatVersion;
import com.io7m.smfj.core.SMFHeader;
import com.io7m.smfj.core.SMFSchemaIdentifier;
import com.io7m.smfj.core.SMFSchemaName;
import com.io7m.smfj.core.SMFTriangles;
import com.io7m.smfj.parser.api.SMFParseError;
import com.io7m.smfj.parser.api.SMFParseWarning;
import com.io7m.smfj.parser.api.SMFParserEventsBodyType;
import com.io7m.smfj.parser.api.SMFParserEventsDataAttributeValuesType;
import com.io7m.smfj.parser.api.SMFParserEventsDataAttributesNonInterleavedType;
import com.io7m.smfj.parser.api.SMFParserEventsDataTrianglesType;
import com.io7m.smfj.parser.api.SMFParserEventsHeaderType;
import com.io7m.smfj.parser.api.SMFParserEventsType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import static com.io7m.jcoords.core.conversion.CAxis.AXIS_NEGATIVE_Z;
import static com.io7m.jcoords.core.conversion.CAxis.AXIS_POSITIVE_X;
import static com.io7m.jcoords.core.conversion.CAxis.AXIS_POSITIVE_Y;
import static com.io7m.smfj.core.SMFFaceWindingOrder.FACE_WINDING_ORDER_COUNTER_CLOCKWISE;

/**
 * The default implementation of the {@link SMFOBJImporterType} interface.
 */

public final class SMFOBJImporter implements SMFOBJImporterType
{
  private static final Logger LOG;

  static {
    LOG = LoggerFactory.getLogger(SMFOBJImporter.class);
  }

  private final JOParserType parser;
  private final SMFParserEventsType events;
  private final List<Vector3D> positions;
  private final List<Vector3D> normals;
  private final List<Vector2D> uvs;
  private final List<Vertex> vertices;
  private final List<Triangle> triangles;
  private final Map<OriginalVertexIdentifier, Integer> vertex_mappings;
  private int triangle_v0;
  private int triangle_v1;
  private int triangle_v2;
  private TriangleState triangle_state = TriangleState.WANT_VERTEX_0;
  private SMFAttribute attrib_position;
  private SMFAttribute attrib_normal;
  private SMFAttribute attrib_uv;

  private SMFOBJImporter(
    final Optional<Path> in_path,
    final InputStream in_stream,
    final SMFParserEventsType in_events)
  {
    this.events = Objects.requireNonNull(in_events, "Events");
    this.parser = JOParser.newParserFromStream(in_path, in_stream, this);
    this.positions = new ArrayList<>(8);
    this.normals = new ArrayList<>(8);
    this.uvs = new ArrayList<>(8);
    this.vertices = new ArrayList<>(0);
    this.triangles = new ArrayList<>(0);
    this.vertex_mappings = new HashMap<>(16);
  }

  /**
   * Create a new OBJ importer.
   *
   * @param in_path   The path, if any
   * @param in_stream The input stream
   * @param in_events An event receiver
   *
   * @return A new importer
   */

  public static SMFOBJImporterType create(
    final Optional<Path> in_path,
    final InputStream in_stream,
    final SMFParserEventsType in_events)
  {
    return new SMFOBJImporter(in_path, in_stream, in_events);
  }

  @Override
  public void onFatalError(
    final LexicalPositionType<Path> p,
    final Optional<Throwable> e,
    final String message)
  {
    this.events.onError(SMFParseError.of(
      LexicalPosition.of(p.line(), p.column(), p.file().map(Path::toUri)),
      e + ": " + message,
      e.map(Exception::new)));
  }

  @Override
  public void onError(
    final LexicalPositionType<Path> p,
    final JOParserErrorCode e,
    final String message)
  {
    this.events.onError(SMFParseError.of(
      LexicalPosition.of(p.line(), p.column(), p.file().map(Path::toUri)),
      e + ": " + message,
      Optional.empty()));
  }

  @Override
  public void onLine(
    final LexicalPositionType<Path> p,
    final String line)
  {

  }

  @Override
  public void onEOF(
    final LexicalPositionType<Path> p)
  {
    this.deliverHeader();
  }

  private void deliverData(
    final SMFParserEventsBodyType events_data)
  {
    if (this.vertices.size() > 0) {
      final Optional<SMFParserEventsDataAttributesNonInterleavedType> events_noninterleaved_opt =
        events_data.onAttributesNonInterleaved();
      final SMFParserEventsDataAttributesNonInterleavedType events_noninterleaved =
        events_noninterleaved_opt.get();
      final Vertex vertex = this.vertices.get(0);

      this.deliverDataPosition(
        events_noninterleaved, this.attrib_position, vertex);
      this.deliverDataNormals(
        events_noninterleaved, this.attrib_normal, vertex);
      this.deliverDataUV(
        events_noninterleaved, this.attrib_uv, vertex);
    }

    if (this.triangles.size() > 0) {
      this.deliverDataTriangles(events_data);
    }
  }

  private void deliverHeader()
  {
    final SMFHeader.Builder header_b = SMFHeader.builder();
    header_b.setSchemaIdentifier(SMFSchemaIdentifier.of(
      SMFSchemaName.of("com.io7m.example"), 0, 0));

    javaslang.collection.List<SMFAttribute> attributes =
      javaslang.collection.List.empty();

    final SMFAttributeName name_position =
      SMFAttributeName.of("POSITION");
    final SMFAttributeName name_normal =
      SMFAttributeName.of("NORMAL");
    final SMFAttributeName name_uv =
      SMFAttributeName.of("UV:0");

    this.attrib_position = SMFAttribute.of(
      name_position, SMFComponentType.ELEMENT_TYPE_FLOATING, 3, 32);
    this.attrib_normal = SMFAttribute.of(
      name_normal, SMFComponentType.ELEMENT_TYPE_FLOATING, 3, 32);
    this.attrib_uv = SMFAttribute.of(
      name_uv, SMFComponentType.ELEMENT_TYPE_FLOATING, 2, 32);

    if (this.vertices.size() > 0) {
      header_b.setVertexCount((long) this.vertices.size());
      final Vertex vertex = this.vertices.get(0);
      if (vertex.position != null) {
        attributes = attributes.append(this.attrib_position);
      }
      if (vertex.normal != null) {
        attributes = attributes.append(this.attrib_normal);
      }
      if (vertex.uv != null) {
        attributes = attributes.append(this.attrib_uv);
      }
    }

    header_b.setAttributesInOrder(attributes);

    int triangle_bits = 32;
    if (this.vertices.size() < 65536) {
      triangle_bits = 16;
    }

    final SMFCoordinateSystem system =
      SMFCoordinateSystem.of(
        CAxisSystem.of(AXIS_POSITIVE_X, AXIS_POSITIVE_Y, AXIS_NEGATIVE_Z),
        FACE_WINDING_ORDER_COUNTER_CLOCKWISE);

    {
      final String text =
        new StringBuilder(128)
          .append("OBJ files do not contain coordinate system information.")
          .append(System.lineSeparator())
          .append(
            "A possibly incorrect default coordinate system has been assumed: ")
          .append(system.toHumanString())
          .append(System.lineSeparator())
          .toString();
      this.events.onWarning(
        SMFParseWarning.of(LexicalPositions.zero(), text, Optional.empty()));
    }

    header_b.setCoordinateSystem(system);
    header_b.setTriangles(
      SMFTriangles.of((long) this.triangles.size(), triangle_bits));
    final SMFHeader header = header_b.build();

    final Optional<SMFParserEventsHeaderType> events_header_opt =
      this.events.onVersionReceived(SMFFormatVersion.of(1, 0));

    if (events_header_opt.isPresent()) {
      final SMFParserEventsHeaderType events_header = events_header_opt.get();
      final Optional<SMFParserEventsBodyType> events_data_opt =
        events_header.onHeaderParsed(header);

      if (events_data_opt.isPresent()) {
        final SMFParserEventsBodyType events_data = events_data_opt.get();
        this.deliverData(events_data);
      }
    }
  }

  private void deliverDataTriangles(
    final SMFParserEventsBodyType events_data)
  {
    final Optional<SMFParserEventsDataTrianglesType> events_tri_opt =
      events_data.onTriangles();

    if (events_tri_opt.isPresent()) {
      final SMFParserEventsDataTrianglesType events_tri = events_tri_opt.get();
      try {
        for (final Triangle t : this.triangles) {
          events_tri.onDataTriangle((long) t.v0, (long) t.v1, (long) t.v2);
        }
      } finally {
        events_tri.onDataTrianglesFinish();
      }
    }
  }

  private void deliverDataUV(
    final SMFParserEventsDataAttributesNonInterleavedType events_noninterleaved,
    final SMFAttribute in_attrib_uv,
    final Vertex vertex)
  {
    if (vertex.uv != null) {
      final Optional<SMFParserEventsDataAttributeValuesType> events_opt =
        events_noninterleaved.onDataAttributeStart(in_attrib_uv);

      if (events_opt.isPresent()) {
        final SMFParserEventsDataAttributeValuesType data_events = events_opt.get();
        try {
          for (final Vertex v : this.vertices) {
            data_events.onDataAttributeValueFloat2(
              v.uv.x(),
              v.uv.y());
          }
        } finally {
          data_events.onDataAttributeValueFinish();
        }
      }
    }
  }

  private void deliverDataNormals(
    final SMFParserEventsDataAttributesNonInterleavedType events_noninterleaved,
    final SMFAttribute in_attrib_normal,
    final Vertex vertex)
  {
    if (vertex.normal != null) {
      final Optional<SMFParserEventsDataAttributeValuesType> events_opt =
        events_noninterleaved.onDataAttributeStart(in_attrib_normal);

      if (events_opt.isPresent()) {
        final SMFParserEventsDataAttributeValuesType data_events = events_opt.get();
        try {
          for (final Vertex v : this.vertices) {
            data_events.onDataAttributeValueFloat3(
              v.normal.x(),
              v.normal.y(),
              v.normal.z());
          }
        } finally {
          data_events.onDataAttributeValueFinish();
        }
      }
    }
  }

  private void deliverDataPosition(
    final SMFParserEventsDataAttributesNonInterleavedType events_noninterleaved,
    final SMFAttribute in_attrib_position,
    final Vertex vertex)
  {
    if (vertex.position != null) {
      final Optional<SMFParserEventsDataAttributeValuesType> events_opt =
        events_noninterleaved.onDataAttributeStart(in_attrib_position);

      if (events_opt.isPresent()) {
        final SMFParserEventsDataAttributeValuesType data_events = events_opt.get();
        try {
          for (final Vertex v : this.vertices) {
            data_events.onDataAttributeValueFloat3(
              v.position.x(),
              v.position.y(),
              v.position.z());
          }
        } finally {
          data_events.onDataAttributeValueFinish();
        }
      }
    }
  }

  @Override
  public void onComment(
    final LexicalPositionType<Path> p,
    final String text)
  {

  }

  @Override
  public void onCommandUsemtl(
    final LexicalPositionType<Path> p,
    final String name)
  {

  }

  @Override
  public void onCommandMtllib(
    final LexicalPositionType<Path> p,
    final String name)
  {

  }

  @Override
  public void onCommandO(
    final LexicalPositionType<Path> p,
    final String name)
  {

  }

  @Override
  public void onCommandS(
    final LexicalPositionType<Path> p,
    final int group_number)
  {

  }

  @Override
  public void onCommandV(
    final LexicalPositionType<Path> p,
    final int index,
    final double x,
    final double y,
    final double z,
    final double w)
  {
    this.positions.add(Vector3D.of(x, y, z));
  }

  @Override
  public void onCommandVN(
    final LexicalPositionType<Path> p,
    final int index,
    final double x,
    final double y,
    final double z)
  {
    this.normals.add(Vector3D.of(x, y, z));
  }

  @Override
  public void onCommandVT(
    final LexicalPositionType<Path> p,
    final int index,
    final double x,
    final double y,
    final double z)
  {
    this.uvs.add(Vector2D.of(x, y));
  }

  @Override
  public void onCommandFVertexV_VT_VN(
    final LexicalPositionType<Path> p,
    final int index,
    final int v,
    final int vt,
    final int vn)
  {
    final int v_index;
    final OriginalVertexIdentifier key =
      new OriginalVertexIdentifier(v, vn, vt);
    if (this.vertex_mappings.containsKey(key)) {
      v_index = this.vertex_mappings.get(key).intValue();
      if (LOG.isTraceEnabled()) {
        LOG.trace("reused vertex {}", Integer.valueOf(v_index));
      }
    } else {
      v_index = this.vertices.size();
      final Vertex vert =
        new Vertex(
          this.positions.get(v - 1),
          this.normals.get(vn - 1),
          this.uvs.get(vt - 1));
      this.vertices.add(vert);
      this.vertex_mappings.put(key, Integer.valueOf(v_index));

      if (LOG.isTraceEnabled()) {
        LOG.trace(
          "created vertex {} -> {}",
          key,
          Integer.valueOf(v_index));
      }
    }

    switch (this.triangle_state) {
      case WANT_VERTEX_0: {
        this.triangle_v0 = v_index;
        this.triangle_state = TriangleState.WANT_VERTEX_1;
        break;
      }
      case WANT_VERTEX_1: {
        this.triangle_v1 = v_index;
        this.triangle_state = TriangleState.WANT_VERTEX_2;
        break;
      }
      case WANT_VERTEX_2: {
        this.triangle_v2 = v_index;
        break;
      }
    }
  }

  @Override
  public void onCommandFVertexV_VT(
    final LexicalPositionType<Path> p,
    final int index,
    final int v,
    final int vt)
  {
    final int v_index;
    final OriginalVertexIdentifier key =
      new OriginalVertexIdentifier(v, -1, vt);
    if (this.vertex_mappings.containsKey(key)) {
      v_index = this.vertex_mappings.get(key).intValue();
      if (LOG.isTraceEnabled()) {
        LOG.trace("reused vertex {}", Integer.valueOf(v_index));
      }
    } else {
      v_index = this.vertices.size();
      final Vertex vert =
        new Vertex(
          this.positions.get(v - 1),
          null,
          this.uvs.get(vt - 1));
      this.vertices.add(vert);
      this.vertex_mappings.put(key, Integer.valueOf(v_index));

      if (LOG.isTraceEnabled()) {
        LOG.trace(
          "created vertex {} -> {}",
          key,
          Integer.valueOf(v_index));
      }
    }

    switch (this.triangle_state) {
      case WANT_VERTEX_0: {
        this.triangle_v0 = v_index;
        this.triangle_state = TriangleState.WANT_VERTEX_1;
        break;
      }
      case WANT_VERTEX_1: {
        this.triangle_v1 = v_index;
        this.triangle_state = TriangleState.WANT_VERTEX_2;
        break;
      }
      case WANT_VERTEX_2: {
        this.triangle_v2 = v_index;
        break;
      }
    }
  }

  @Override
  public void onCommandFVertexV_VN(
    final LexicalPositionType<Path> p,
    final int index,
    final int v,
    final int vn)
  {
    final int v_index;
    final OriginalVertexIdentifier key =
      new OriginalVertexIdentifier(v, vn, -1);
    if (this.vertex_mappings.containsKey(key)) {
      v_index = this.vertex_mappings.get(key).intValue();
      if (LOG.isTraceEnabled()) {
        LOG.trace("reused vertex {}", Integer.valueOf(v_index));
      }
    } else {
      v_index = this.vertices.size();
      final Vertex vert =
        new Vertex(
          this.positions.get(v - 1),
          this.normals.get(vn - 1),
          null);
      this.vertices.add(vert);
      this.vertex_mappings.put(key, Integer.valueOf(v_index));

      if (LOG.isTraceEnabled()) {
        LOG.trace(
          "created vertex {} -> {}",
          key,
          Integer.valueOf(v_index));
      }
    }

    switch (this.triangle_state) {
      case WANT_VERTEX_0: {
        this.triangle_v0 = v_index;
        this.triangle_state = TriangleState.WANT_VERTEX_1;
        break;
      }
      case WANT_VERTEX_1: {
        this.triangle_v1 = v_index;
        this.triangle_state = TriangleState.WANT_VERTEX_2;
        break;
      }
      case WANT_VERTEX_2: {
        this.triangle_v2 = v_index;
        break;
      }
    }
  }

  @Override
  public void onCommandFVertexV(
    final LexicalPositionType<Path> p,
    final int index,
    final int v)
  {
    final int v_index;
    final OriginalVertexIdentifier key =
      new OriginalVertexIdentifier(v, -1, -1);
    if (this.vertex_mappings.containsKey(key)) {
      v_index = this.vertex_mappings.get(key).intValue();
      if (LOG.isTraceEnabled()) {
        LOG.trace("reused vertex {}", Integer.valueOf(v_index));
      }
    } else {
      v_index = this.vertices.size();
      final Vertex vert =
        new Vertex(
          this.positions.get(v - 1),
          null,
          null);
      this.vertices.add(vert);

      if (LOG.isTraceEnabled()) {
        LOG.trace("created vertex {}", Integer.valueOf(v_index));
      }
    }

    switch (this.triangle_state) {
      case WANT_VERTEX_0: {
        this.triangle_v0 = v_index;
        this.triangle_state = TriangleState.WANT_VERTEX_1;
        break;
      }
      case WANT_VERTEX_1: {
        this.triangle_v1 = v_index;
        this.triangle_state = TriangleState.WANT_VERTEX_2;
        break;
      }
      case WANT_VERTEX_2: {
        this.triangle_v2 = v_index;
        break;
      }
    }
  }

  @Override
  public void onCommandFStarted(
    final LexicalPositionType<Path> p,
    final int index)
  {
    this.triangle_v0 = -1;
    this.triangle_v1 = -1;
    this.triangle_v2 = -1;
    this.triangle_state = TriangleState.WANT_VERTEX_0;
  }

  @Override
  public void onCommandFFinished(
    final LexicalPositionType<Path> p,
    final int index)
  {
    Preconditions.checkPrecondition(
      this.triangle_state == TriangleState.WANT_VERTEX_2,
      "Must have received three triangle vertices");
    Preconditions.checkPreconditionI(
      this.triangle_v0,
      this.triangle_v0 != -1,
      i -> "Triangle vertex 0 must have been set");
    Preconditions.checkPreconditionI(
      this.triangle_v1,
      this.triangle_v1 != -1,
      i -> "Triangle vertex 1 must have been set");
    Preconditions.checkPreconditionI(
      this.triangle_v2,
      this.triangle_v2 != -1,
      i -> "Triangle vertex 2 must have been set");

    final int t_index = this.triangles.size();
    final Triangle t =
      new Triangle(this.triangle_v0, this.triangle_v1, this.triangle_v2);
    this.triangles.add(t);

    if (LOG.isTraceEnabled()) {
      LOG.trace("created triangle {} -> {}", Integer.valueOf(t_index), t);
    }
  }

  @Override
  public void close()
    throws IOException
  {

  }

  @Override
  public void parse()
  {
    this.parser.run();
  }

  private enum TriangleState
  {
    WANT_VERTEX_0,
    WANT_VERTEX_1,
    WANT_VERTEX_2
  }

  private static final class OriginalVertexIdentifier
  {
    private final int v;
    private final int vn;
    private final int vt;

    OriginalVertexIdentifier(
      final int in_v,
      final int in_vn,
      final int in_vt)
    {
      this.v = in_v;
      this.vn = in_vn;
      this.vt = in_vt;
    }

    @Override
    public String toString()
    {
      final StringBuilder sb = new StringBuilder(16);
      sb.append(this.v);
      sb.append("/");
      sb.append(this.vn);
      sb.append("/");
      sb.append(this.vt);
      return sb.toString();
    }

    @Override
    public boolean equals(final Object o)
    {
      if (this == o) {
        return true;
      }
      if (o == null || this.getClass() != o.getClass()) {
        return false;
      }

      final OriginalVertexIdentifier that = (OriginalVertexIdentifier) o;
      return this.v == that.v && this.vn == that.vn && this.vt == that.vt;
    }

    @Override
    public int hashCode()
    {
      int result = this.v;
      result = 31 * result + this.vn;
      result = 31 * result + this.vt;
      return result;
    }
  }

  private static final class Vertex
  {
    private final Vector3D position;
    private final Vector3D normal;
    private final Vector2D uv;

    Vertex(
      final Vector3D in_position,
      final Vector3D in_normal,
      final Vector2D in_uv)
    {
      this.position = in_position;
      this.normal = in_normal;
      this.uv = in_uv;
    }
  }

  private static final class Triangle
  {
    private final int v0;
    private final int v1;
    private final int v2;

    Triangle(
      final int in_v0,
      final int in_v1,
      final int in_v2)
    {
      this.v0 = in_v0;
      this.v1 = in_v1;
      this.v2 = in_v2;
    }

    @Override
    public String toString()
    {
      final StringBuilder sb = new StringBuilder(16);
      sb.append(this.v0);
      sb.append(" ");
      sb.append(this.v1);
      sb.append(" ");
      sb.append(this.v2);
      return sb.toString();
    }
  }
}
