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
import com.io7m.jcoords.core.conversion.CAxis;
import com.io7m.jcoords.core.conversion.CAxisSystem;
import com.io7m.jlexing.core.LexicalPosition;
import com.io7m.jlexing.core.LexicalPositionType;
import com.io7m.jnull.NullCheck;
import com.io7m.jnull.Nullable;
import com.io7m.jobj.core.JOParser;
import com.io7m.jobj.core.JOParserErrorCode;
import com.io7m.jobj.core.JOParserType;
import com.io7m.jtensors.VectorI2D;
import com.io7m.jtensors.VectorI3D;
import com.io7m.smfj.core.SMFAttribute;
import com.io7m.smfj.core.SMFAttributeName;
import com.io7m.smfj.core.SMFComponentType;
import com.io7m.smfj.core.SMFCoordinateSystem;
import com.io7m.smfj.core.SMFFaceWindingOrder;
import com.io7m.smfj.core.SMFHeader;
import com.io7m.smfj.core.SMFSchemaIdentifier;
import com.io7m.smfj.parser.api.SMFParseError;
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
import java.util.Optional;

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
  private final List<VectorI3D> positions;
  private final List<VectorI3D> normals;
  private final List<VectorI2D> uvs;
  private final List<Vertex> vertices;
  private final List<Triangle> triangles;
  private final Map<OriginalVertexIdentifier, Integer> vertex_mappings;
  private State state;
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
    this.events = NullCheck.notNull(in_events, "Events");
    this.parser = JOParser.newParserFromStream(in_path, in_stream, this);
    this.positions = new ArrayList<>(8);
    this.normals = new ArrayList<>(8);
    this.uvs = new ArrayList<>(8);
    this.vertices = new ArrayList<>(0);
    this.triangles = new ArrayList<>(0);
    this.vertex_mappings = new HashMap<>(16);
    this.state = State.STATE_INITIAL;
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
  public boolean parserHasFailed()
  {
    return this.state == State.STATE_FAILED;
  }

  @Override
  public void parseHeader()
  {
    if (this.state != State.STATE_INITIAL) {
      throw new IllegalStateException("Parser has already executed");
    }

    this.events.onStart();
    this.parser.run();

    if (this.state != State.STATE_FAILED) {
      this.state = State.STATE_HEADER_PARSED;
    }
  }

  @Override
  public void parseData()
    throws IllegalStateException
  {
    if (this.state != State.STATE_HEADER_PARSED) {
      throw new IllegalStateException("Header has not been parsed");
    }

    this.deliverData();

    if (this.state == State.STATE_HEADER_PARSED) {
      this.state = State.STATE_FINISHED;
    }
  }

  @Override
  public void onFatalError(
    final LexicalPositionType<Path> p,
    final Optional<Throwable> e,
    final String message)
  {
    this.state = State.STATE_FAILED;
    this.events.onError(SMFParseError.of(
      LexicalPosition.copyOf(p),
      e + ": " + message,
      e.map(Exception::new)));
  }

  @Override
  public void onError(
    final LexicalPositionType<Path> p,
    final JOParserErrorCode e,
    final String message)
  {
    this.state = State.STATE_FAILED;
    this.events.onError(SMFParseError.of(
      LexicalPosition.copyOf(p),
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

  private void deliverData()
  {
    if (this.vertices.size() > 0) {
      final Vertex vertex = this.vertices.get(0);
      this.deliverDataPosition(this.attrib_position, vertex);
      this.deliverDataNormals(this.attrib_normal, vertex);
      this.deliverDataUV(this.attrib_uv, vertex);
    }

    if (this.triangles.size() > 0) {
      this.deliverDataTriangles();
    }
  }

  private void deliverHeader()
  {
    final SMFHeader.Builder header_b = SMFHeader.builder();
    header_b.setSchemaIdentifier(SMFSchemaIdentifier.of(0, 0, 0, 0));

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

    LOG.warn("OBJ files do not contain coordinate system information.");
    LOG.warn(
      "A possibly incorrect default coordinate system has been assumed: Right +X, Up +Y, Forward -Z");

    header_b.setCoordinateSystem(SMFCoordinateSystem.of(
      CAxisSystem.of(
        CAxis.AXIS_POSITIVE_X,
        CAxis.AXIS_POSITIVE_Y,
        CAxis.AXIS_NEGATIVE_Z),
      SMFFaceWindingOrder.FACE_WINDING_ORDER_COUNTER_CLOCKWISE));

    header_b.setTriangleIndexSizeBits((long) triangle_bits);
    header_b.setTriangleCount((long) this.triangles.size());
    final SMFHeader header = header_b.build();
    this.events.onHeaderParsed(header);
  }

  private void deliverDataTriangles()
  {
    this.events.onDataTrianglesStart();
    for (final Triangle t : this.triangles) {
      this.events.onDataTriangle((long) t.v0, (long) t.v1, (long) t.v2);
    }
    this.events.onDataTrianglesFinish();
  }

  private void deliverDataUV(
    final SMFAttribute in_attrib_uv,
    final Vertex vertex)
  {
    if (vertex.uv != null) {
      this.events.onDataAttributeStart(in_attrib_uv);
      for (final Vertex v : this.vertices) {
        this.events.onDataAttributeValueFloat2(
          v.uv.getXD(),
          v.uv.getYD());
      }
      this.events.onDataAttributeFinish(in_attrib_uv);
    }
  }

  private void deliverDataNormals(
    final SMFAttribute in_attrib_normal,
    final Vertex vertex)
  {
    if (vertex.normal != null) {
      this.events.onDataAttributeStart(in_attrib_normal);
      for (final Vertex v : this.vertices) {
        this.events.onDataAttributeValueFloat3(
          v.normal.getXD(),
          v.normal.getYD(),
          v.normal.getZD());
      }
      this.events.onDataAttributeFinish(in_attrib_normal);
    }
  }

  private void deliverDataPosition(
    final SMFAttribute in_attrib_position,
    final Vertex vertex)
  {
    if (vertex.position != null) {
      this.events.onDataAttributeStart(in_attrib_position);
      for (final Vertex v : this.vertices) {
        this.events.onDataAttributeValueFloat3(
          v.position.getXD(),
          v.position.getYD(),
          v.position.getZD());
      }
      this.events.onDataAttributeFinish(in_attrib_position);
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
    this.positions.add(new VectorI3D(x, y, z));
  }

  @Override
  public void onCommandVN(
    final LexicalPositionType<Path> p,
    final int index,
    final double x,
    final double y,
    final double z)
  {
    this.normals.add(new VectorI3D(x, y, z));
  }

  @Override
  public void onCommandVT(
    final LexicalPositionType<Path> p,
    final int index,
    final double x,
    final double y,
    final double z)
  {
    this.uvs.add(new VectorI2D(x, y));
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
    try {
      if (this.state != State.STATE_CLOSED) {
        this.events.onFinish();
      }
    } finally {
      this.state = State.STATE_CLOSED;
    }
  }

  private enum State
  {
    STATE_INITIAL,
    STATE_FAILED,
    STATE_HEADER_PARSED,
    STATE_FINISHED,
    STATE_CLOSED
  }

  private enum TriangleState
  {
    WANT_VERTEX_0,
    WANT_VERTEX_1,
    WANT_VERTEX_2
  }

  private static final class OriginalVertexIdentifier
  {
    protected final int v;
    protected final int vn;
    protected final int vt;

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
    protected final @Nullable VectorI3D position;
    protected final @Nullable VectorI3D normal;
    protected final @Nullable VectorI2D uv;

    Vertex(
      final VectorI3D in_position,
      final VectorI3D in_normal,
      final VectorI2D in_uv)
    {
      this.position = in_position;
      this.normal = in_normal;
      this.uv = in_uv;
    }
  }

  private static final class Triangle
  {
    protected final int v0;
    protected final int v1;
    protected final int v2;

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
