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

package com.io7m.smfj.processing.api;

import com.io7m.jfunctional.Unit;
import com.io7m.jlexing.core.LexicalPositions;
import com.io7m.jnull.NullCheck;
import com.io7m.jtensors.core.unparameterized.vectors.Vector2D;
import com.io7m.jtensors.core.unparameterized.vectors.Vector2L;
import com.io7m.jtensors.core.unparameterized.vectors.Vector3D;
import com.io7m.jtensors.core.unparameterized.vectors.Vector3L;
import com.io7m.jtensors.core.unparameterized.vectors.Vector4D;
import com.io7m.jtensors.core.unparameterized.vectors.Vector4L;
import com.io7m.junreachable.UnreachableCodeException;
import com.io7m.smfj.core.SMFAttribute;
import com.io7m.smfj.core.SMFAttributeName;
import com.io7m.smfj.core.SMFFormatVersion;
import com.io7m.smfj.parser.api.SMFParseError;
import com.io7m.smfj.parser.api.SMFParserEventsType;
import com.io7m.smfj.parser.api.SMFParserRandomAccessType;
import com.io7m.smfj.parser.api.SMFParserSequentialType;
import javaslang.collection.Map;

import java.io.IOException;
import java.util.NoSuchElementException;
import java.util.Optional;

/**
 * A parser that turns a memory mesh into a
 */

public final class SMFMemoryMeshParser
{
  private SMFMemoryMeshParser()
  {
    throw new UnreachableCodeException();
  }

  /**
   * Create a new random access parser from the given mesh.
   *
   * @param mesh   The source mesh
   * @param events The event receiver
   *
   * @return A parser
   */

  public static SMFParserRandomAccessType createRandomAccess(
    final SMFMemoryMesh mesh,
    final SMFParserEventsType events)
  {
    NullCheck.notNull(mesh, "mesh");
    NullCheck.notNull(events, "events");
    return new RandomAccess(mesh, events);
  }

  /**
   * Create a new sequential access parser from the given mesh.
   *
   * @param mesh   The source mesh
   * @param events The event receiver
   *
   * @return A parser
   */

  public static SMFParserSequentialType createSequential(
    final SMFMemoryMesh mesh,
    final SMFParserEventsType events)
  {
    NullCheck.notNull(mesh, "mesh");
    NullCheck.notNull(events, "events");
    return new Sequential(mesh, events);
  }

  private static final class RandomAccess extends AbstractParser implements
    SMFParserRandomAccessType
  {
    private final SMFMemoryMesh mesh;
    private boolean started;
    private boolean finished;

    private RandomAccess(
      final SMFMemoryMesh in_mesh,
      final SMFParserEventsType in_events)
    {
      super(in_events);
      this.mesh = NullCheck.notNull(in_mesh, "Mesh");
      this.started = false;
      this.finished = false;
    }

    @Override
    public void parseHeader()
    {
      if (this.parserHasFailed()) {
        throw new IllegalStateException("Parser has failed");
      }

      try {
        if (!this.started) {
          this.started = true;
          this.events.onStart();
          this.events.onVersionReceived(SMFFormatVersion.of(1, 0));
        }

        this.events.onHeaderParsed(this.mesh.header());
      } catch (final Exception e) {
        this.fail(e);
      }
    }

    @Override
    public void parseAttributeData(
      final SMFAttributeName name)
      throws IllegalStateException
    {
      if (this.parserHasFailed()) {
        throw new IllegalStateException("Parser has failed");
      }

      final Map<SMFAttributeName, SMFAttributeArrayType> arrays = this.mesh.arrays();
      if (!arrays.containsKey(name)) {
        throw new NoSuchElementException("No such attribute: " + name.value());
      }

      final SMFAttribute attr =
        this.mesh.header().attributesByName().get(name).get();
      final SMFAttributeArrayType array =
        this.mesh.arrays().get(name).get();

      try {
        try {
          this.events.onDataAttributeStart(attr);
          array.matchArray(
            this,
            AbstractParser::sendFloat4,
            AbstractParser::sendFloat3,
            AbstractParser::sendFloat2,
            AbstractParser::sendFloat1,
            AbstractParser::sendUnsigned4,
            AbstractParser::sendUnsigned3,
            AbstractParser::sendUnsigned2,
            AbstractParser::sendUnsigned1,
            AbstractParser::sendSigned4,
            AbstractParser::sendSigned3,
            AbstractParser::sendSigned2,
            AbstractParser::sendSigned1);
        } finally {
          this.events.onDataAttributeFinish(attr);
        }
      } catch (final Exception e) {
        this.fail(e);
      }
    }

    @Override
    public void parseTriangles()
      throws IllegalStateException
    {
      if (this.parserHasFailed()) {
        throw new IllegalStateException("Parser has failed");
      }

      try {
        try {
          this.events.onDataTrianglesStart();
          for (final Vector3L t : this.mesh.triangles()) {
            this.events.onDataTriangle(t.x(), t.y(), t.z());
          }
        } finally {
          this.events.onDataTrianglesFinish();
        }
      } catch (final Exception e) {
        this.fail(e);
      }
    }

    @Override
    public void parseMetadata()
      throws IllegalStateException
    {
      if (this.parserHasFailed()) {
        throw new IllegalStateException("Parser has failed");
      }

      try {
        for (final SMFMetadata m : this.mesh.metadata()) {
          if (this.events.onMeta(
            m.vendor(), m.schema(), (long) m.data().length)) {
            this.events.onMetaData(m.vendor(), m.schema(), m.data());
          }
        }
      } catch (final Exception e) {
        this.fail(e);
      }
    }

    @Override
    public void close()
      throws IOException
    {
      try {
        if (!this.finished) {
          this.finished = false;
          this.events.onFinish();
        }
      } catch (final Exception e) {
        this.fail(e);
      }
    }

    @Override
    public boolean parserHasFailed()
    {
      return super.failed;
    }
  }

  private static final class Sequential extends AbstractParser implements
    SMFParserSequentialType
  {
    private final SMFMemoryMesh mesh;
    private final SMFParserEventsType events;
    private boolean started;
    private boolean finished;

    private Sequential(
      final SMFMemoryMesh in_mesh,
      final SMFParserEventsType in_events)
    {
      super(in_events);

      this.mesh = NullCheck.notNull(in_mesh, "Mesh");
      this.events = NullCheck.notNull(in_events, "Events");
      this.started = false;
      this.finished = false;
    }

    @Override
    public void parseHeader()
    {
      if (this.parserHasFailed()) {
        throw new IllegalStateException("Parser has failed");
      }

      try {
        if (!this.started) {
          this.started = true;
          this.events.onStart();
          this.events.onVersionReceived(SMFFormatVersion.of(1, 0));
        }

        this.events.onHeaderParsed(this.mesh.header());
      } catch (final Exception e) {
        this.fail(e);
      }
    }

    @Override
    public void parseData()
      throws IllegalStateException
    {
      if (this.parserHasFailed()) {
        throw new IllegalStateException("Parser has failed");
      }

      try {
        final Map<SMFAttributeName, SMFAttributeArrayType> arrays = this.mesh.arrays();
        for (final SMFAttribute attr : this.mesh.header().attributesInOrder()) {
          final SMFAttributeArrayType array = arrays.get(attr.name()).get();

          try {
            this.events.onDataAttributeStart(attr);
            array.matchArray(
              this,
              AbstractParser::sendFloat4,
              AbstractParser::sendFloat3,
              AbstractParser::sendFloat2,
              AbstractParser::sendFloat1,
              AbstractParser::sendUnsigned4,
              AbstractParser::sendUnsigned3,
              AbstractParser::sendUnsigned2,
              AbstractParser::sendUnsigned1,
              AbstractParser::sendSigned4,
              AbstractParser::sendSigned3,
              AbstractParser::sendSigned2,
              AbstractParser::sendSigned1);
          } finally {
            this.events.onDataAttributeFinish(attr);
          }
        }

        try {
          this.events.onDataTrianglesStart();
          for (final Vector3L t : this.mesh.triangles()) {
            this.events.onDataTriangle(t.x(), t.y(), t.z());
          }
        } finally {
          this.events.onDataTrianglesFinish();
        }

        for (final SMFMetadata m : this.mesh.metadata()) {
          if (this.events.onMeta(
            m.vendor(), m.schema(), (long) m.data().length)) {
            this.events.onMetaData(m.vendor(), m.schema(), m.data());
          }
        }

      } catch (final Exception e) {
        this.fail(e);
      }
    }

    @Override
    public boolean parserHasFailed()
    {
      return super.failed;
    }

    @Override
    public void close()
      throws IOException
    {
      try {
        if (!this.finished) {
          this.finished = false;
          this.events.onFinish();
        }
      } catch (final Exception e) {
        super.fail(e);
      }
    }
  }

  private static abstract class AbstractParser
  {
    protected final SMFParserEventsType events;
    protected boolean failed;

    protected AbstractParser(
      final SMFParserEventsType in_events)
    {
      this.events = NullCheck.notNull(in_events, "Events");
      this.failed = false;
    }

    protected final void fail(
      final Exception e)
    {
      this.failed = true;
      this.events.onError(SMFParseError.of(
        LexicalPositions.zero(), e.getMessage(), Optional.of(e)));
    }

    protected final Unit sendSigned1(
      final SMFAttributeArrayIntegerSigned1Type y)
      throws IOException
    {
      for (final Long v : y.values()) {
        this.events.onDataAttributeValueIntegerSigned1(v.longValue());
      }
      return Unit.unit();
    }

    protected final Unit sendSigned2(
      final SMFAttributeArrayIntegerSigned2Type y)
      throws IOException
    {
      for (final Vector2L v : y.values()) {
        this.events.onDataAttributeValueIntegerSigned2(v.x(), v.y());
      }
      return Unit.unit();
    }

    protected final Unit sendSigned3(
      final SMFAttributeArrayIntegerSigned3Type y)
      throws IOException
    {
      for (final Vector3L v : y.values()) {
        this.events.onDataAttributeValueIntegerSigned3(v.x(), v.y(), v.z());
      }
      return Unit.unit();
    }

    protected final Unit sendSigned4(
      final SMFAttributeArrayIntegerSigned4Type y)
      throws IOException
    {
      for (final Vector4L v : y.values()) {
        this.events.onDataAttributeValueIntegerSigned4(
          v.x(), v.y(), v.z(), v.w());
      }
      return Unit.unit();
    }

    protected final Unit sendUnsigned1(
      final SMFAttributeArrayIntegerUnsigned1Type y)
      throws IOException
    {
      for (final Long v : y.values()) {
        this.events.onDataAttributeValueIntegerUnsigned1(
          v.longValue());
      }
      return Unit.unit();
    }

    protected final Unit sendUnsigned2(
      final SMFAttributeArrayIntegerUnsigned2Type y)
      throws IOException
    {
      for (final Vector2L v : y.values()) {
        this.events.onDataAttributeValueIntegerUnsigned2(v.x(), v.y());
      }
      return Unit.unit();
    }

    protected final Unit sendUnsigned3(
      final SMFAttributeArrayIntegerUnsigned3Type y)
      throws IOException
    {
      for (final Vector3L v : y.values()) {
        this.events.onDataAttributeValueIntegerUnsigned3(v.x(), v.y(), v.z());
      }
      return Unit.unit();
    }

    protected final Unit sendUnsigned4(
      final SMFAttributeArrayIntegerUnsigned4Type y)
      throws IOException
    {
      for (final Vector4L v : y.values()) {
        this.events.onDataAttributeValueIntegerUnsigned4(
          v.x(), v.y(), v.z(), v.w());
      }
      return Unit.unit();
    }

    protected final Unit sendFloat1(
      final SMFAttributeArrayFloating1Type y)
      throws IOException
    {
      for (final Double v : y.values()) {
        this.events.onDataAttributeValueFloat1(
          v.doubleValue());
      }
      return Unit.unit();
    }

    protected final Unit sendFloat2(
      final SMFAttributeArrayFloating2Type y)
      throws IOException
    {
      for (final Vector2D v : y.values()) {
        this.events.onDataAttributeValueFloat2(v.x(), v.y());
      }
      return Unit.unit();
    }

    protected final Unit sendFloat3(
      final SMFAttributeArrayFloating3Type y)
      throws IOException
    {
      for (final Vector3D v : y.values()) {
        this.events.onDataAttributeValueFloat3(v.x(), v.y(), v.z());
      }
      return Unit.unit();
    }

    protected final Unit sendFloat4(
      final SMFAttributeArrayFloating4Type y)
      throws IOException
    {
      for (final Vector4D v : y.values()) {
        this.events.onDataAttributeValueFloat4(v.x(), v.y(), v.z(), v.w());
      }
      return Unit.unit();
    }
  }
}
