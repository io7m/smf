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

package com.io7m.smfj.tests.bytebuffer;

import com.io7m.ieee754b16.Binary16;
import com.io7m.jintegers.Signed16;
import com.io7m.jintegers.Signed32;
import com.io7m.jintegers.Unsigned16;
import com.io7m.jintegers.Unsigned32;
import com.io7m.jintegers.Unsigned8;
import com.io7m.jpra.runtime.java.JPRACursor1DType;
import com.io7m.jtensors.VectorM2D;
import com.io7m.jtensors.VectorM2L;
import com.io7m.jtensors.VectorM3D;
import com.io7m.jtensors.VectorM3L;
import com.io7m.jtensors.VectorM4D;
import com.io7m.jtensors.VectorM4L;
import com.io7m.jtensors.VectorWritable3LType;
import com.io7m.junreachable.UnreachableCodeException;
import com.io7m.smfj.bytebuffer.SMFByteBufferCursors;
import com.io7m.smfj.bytebuffer.SMFByteBufferFloat1Type;
import com.io7m.smfj.bytebuffer.SMFByteBufferFloat2Type;
import com.io7m.smfj.bytebuffer.SMFByteBufferFloat3Type;
import com.io7m.smfj.bytebuffer.SMFByteBufferFloat4Type;
import com.io7m.smfj.bytebuffer.SMFByteBufferIntegerSigned1Type;
import com.io7m.smfj.bytebuffer.SMFByteBufferIntegerSigned2Type;
import com.io7m.smfj.bytebuffer.SMFByteBufferIntegerSigned3Type;
import com.io7m.smfj.bytebuffer.SMFByteBufferIntegerSigned4Type;
import com.io7m.smfj.bytebuffer.SMFByteBufferIntegerUnsigned1Type;
import com.io7m.smfj.bytebuffer.SMFByteBufferIntegerUnsigned2Type;
import com.io7m.smfj.bytebuffer.SMFByteBufferIntegerUnsigned3Type;
import com.io7m.smfj.bytebuffer.SMFByteBufferIntegerUnsigned4Type;
import com.io7m.smfj.bytebuffer.SMFByteBufferPackedMesh;
import com.io7m.smfj.bytebuffer.SMFByteBufferPackedMeshLoaderType;
import com.io7m.smfj.bytebuffer.SMFByteBufferPackedMeshes;
import com.io7m.smfj.bytebuffer.SMFByteBufferPackedTriangles;
import com.io7m.smfj.bytebuffer.SMFByteBufferPackerEventsType;
import com.io7m.smfj.bytebuffer.SMFByteBufferPackingConfiguration;
import com.io7m.smfj.core.SMFAttribute;
import com.io7m.smfj.core.SMFAttributeName;
import com.io7m.smfj.core.SMFComponentType;
import com.io7m.smfj.core.SMFErrorType;
import com.io7m.smfj.core.SMFHeader;
import com.io7m.smfj.format.text.SMFFormatText;
import com.io7m.smfj.parser.api.SMFParserEventsMeta;
import com.io7m.smfj.parser.api.SMFParserEventsType;
import com.io7m.smfj.parser.api.SMFParserProviderType;
import com.io7m.smfj.parser.api.SMFParserSequentialType;
import javaslang.Tuple;
import javaslang.collection.List;
import javaslang.collection.SortedMap;
import javaslang.collection.TreeMap;
import javaslang.control.Validation;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;

import static javaslang.control.Validation.valid;

public final class SMFByteBufferPackedMeshesTest
{
  private static SMFParserSequentialType createParser(
    final SMFParserEventsType loader,
    final String name)
    throws IOException
  {
    final String rpath = "/com/io7m/smfj/tests/bytebuffer/" + name;
    try (final InputStream stream =
           SMFByteBufferPackedMeshesTest.class.getResourceAsStream(rpath)) {
      final SMFParserProviderType fmt = new SMFFormatText();
      final Path path = Paths.get(rpath);
      final SMFParserSequentialType parser =
        fmt.parserCreateSequential(loader, path, stream);
      parser.parseHeader();
      parser.parseData();
      return parser;
    }
  }

  private static void checkTypeUnsigned8(
    final int component_count,
    final ByteBuffer rb)
  {
    switch (component_count) {
      case 1: {
        final JPRACursor1DType<SMFByteBufferIntegerUnsigned1Type> c =
          SMFByteBufferCursors.createUnsigned1Raw(rb, 8, 0, 1);
        final SMFByteBufferIntegerUnsigned1Type v = c.getElementView();

        {
          final long vx = (long) Unsigned8.unpackFromBuffer(rb, 0);
          final long vy = (long) Unsigned8.unpackFromBuffer(rb, 1);
          final long vz = (long) Unsigned8.unpackFromBuffer(rb, 2);
          Assert.assertEquals(0L, vx);
          Assert.assertEquals(128L, vy);
          Assert.assertEquals(255L, vz);

          c.setElementIndex(0);
          Assert.assertEquals(0L, vx);
          Assert.assertEquals(0L, v.get1UL());

          c.setElementIndex(1);
          Assert.assertEquals(128L, vy);
          Assert.assertEquals(128L, v.get1UL());

          c.setElementIndex(2);
          Assert.assertEquals(255L, vz);
          Assert.assertEquals(255L, v.get1UL());
        }
        break;
      }

      case 2: {
        final JPRACursor1DType<SMFByteBufferIntegerUnsigned2Type> c =
          SMFByteBufferCursors.createUnsigned2Raw(rb, 8, 0, 2);
        final SMFByteBufferIntegerUnsigned2Type v = c.getElementView();
        final VectorM2L o = new VectorM2L();

        {
          c.setElementIndex(0);
          final long vx = (long) Unsigned8.unpackFromBuffer(rb, 0);
          final long vy = (long) Unsigned8.unpackFromBuffer(rb, 1);
          Assert.assertEquals(0L, vx);
          Assert.assertEquals(255L, vy);

          v.get2UL(o);
          Assert.assertEquals(0L, o.getXL());
          Assert.assertEquals(255L, o.getYL());
        }

        {
          c.setElementIndex(1);
          final long vx = (long) Unsigned8.unpackFromBuffer(rb, 2);
          final long vy = (long) Unsigned8.unpackFromBuffer(rb, 3);
          Assert.assertEquals(0L, vx);
          Assert.assertEquals(255L, vy);

          v.get2UL(o);
          Assert.assertEquals(0L, o.getXL());
          Assert.assertEquals(255L, o.getYL());
        }

        {
          c.setElementIndex(2);
          final long vx = (long) Unsigned8.unpackFromBuffer(rb, 4);
          final long vy = (long) Unsigned8.unpackFromBuffer(rb, 5);
          Assert.assertEquals(0L, vx);
          Assert.assertEquals(255L, vy);

          v.get2UL(o);
          Assert.assertEquals(0L, o.getXL());
          Assert.assertEquals(255L, o.getYL());
        }
        break;
      }

      case 3: {
        final JPRACursor1DType<SMFByteBufferIntegerUnsigned3Type> c =
          SMFByteBufferCursors.createUnsigned3Raw(rb, 8, 0, 3);
        final SMFByteBufferIntegerUnsigned3Type v = c.getElementView();
        final VectorM3L o = new VectorM3L();

        {
          c.setElementIndex(0);
          final long vx = (long) Unsigned8.unpackFromBuffer(rb, 0);
          final long vy = (long) Unsigned8.unpackFromBuffer(rb, 1);
          final long vz = (long) Unsigned8.unpackFromBuffer(rb, 2);
          Assert.assertEquals(0L, vx);
          Assert.assertEquals(128L, vy);
          Assert.assertEquals(255L, vz);

          v.get3UL(o);
          Assert.assertEquals(0L, o.getXL());
          Assert.assertEquals(128L, o.getYL());
          Assert.assertEquals(255L, o.getZL());
        }

        {
          c.setElementIndex(1);
          final long vx = (long) Unsigned8.unpackFromBuffer(rb, 3);
          final long vy = (long) Unsigned8.unpackFromBuffer(rb, 4);
          final long vz = (long) Unsigned8.unpackFromBuffer(rb, 5);
          Assert.assertEquals(0L, vx);
          Assert.assertEquals(128L, vy);
          Assert.assertEquals(255L, vz);

          v.get3UL(o);
          Assert.assertEquals(0L, o.getXL());
          Assert.assertEquals(128L, o.getYL());
          Assert.assertEquals(255L, o.getZL());
        }

        {
          c.setElementIndex(2);
          final long vx = (long) Unsigned8.unpackFromBuffer(rb, 6);
          final long vy = (long) Unsigned8.unpackFromBuffer(rb, 7);
          final long vz = (long) Unsigned8.unpackFromBuffer(rb, 8);
          Assert.assertEquals(0L, vx);
          Assert.assertEquals(128L, vy);
          Assert.assertEquals(255L, vz);

          v.get3UL(o);
          Assert.assertEquals(0L, o.getXL());
          Assert.assertEquals(128L, o.getYL());
          Assert.assertEquals(255L, o.getZL());
        }
        break;
      }

      case 4: {
        final JPRACursor1DType<SMFByteBufferIntegerUnsigned4Type> c =
          SMFByteBufferCursors.createUnsigned4Raw(rb, 8, 0, 4);
        final SMFByteBufferIntegerUnsigned4Type v = c.getElementView();
        final VectorM4L o = new VectorM4L();

        {
          c.setElementIndex(0);
          final long vx = (long) Unsigned8.unpackFromBuffer(rb, 0);
          final long vy = (long) Unsigned8.unpackFromBuffer(rb, 1);
          final long vz = (long) Unsigned8.unpackFromBuffer(rb, 2);
          final long vw = (long) Unsigned8.unpackFromBuffer(rb, 3);
          Assert.assertEquals(0L, vx);
          Assert.assertEquals(128L, vy);
          Assert.assertEquals(255L, vz);
          Assert.assertEquals(128L, vw);

          v.get4UL(o);
          Assert.assertEquals(0L, o.getXL());
          Assert.assertEquals(128L, o.getYL());
          Assert.assertEquals(255L, o.getZL());
          Assert.assertEquals(128L, o.getWL());
        }

        {
          c.setElementIndex(1);
          final long vx = (long) Unsigned8.unpackFromBuffer(rb, 4);
          final long vy = (long) Unsigned8.unpackFromBuffer(rb, 5);
          final long vz = (long) Unsigned8.unpackFromBuffer(rb, 6);
          final long vw = (long) Unsigned8.unpackFromBuffer(rb, 7);
          Assert.assertEquals(0L, vx);
          Assert.assertEquals(128L, vy);
          Assert.assertEquals(255L, vz);
          Assert.assertEquals(128L, vw);

          v.get4UL(o);
          Assert.assertEquals(0L, o.getXL());
          Assert.assertEquals(128L, o.getYL());
          Assert.assertEquals(255L, o.getZL());
          Assert.assertEquals(128L, o.getWL());
        }

        {
          c.setElementIndex(2);
          final long vx = (long) Unsigned8.unpackFromBuffer(rb, 8);
          final long vy = (long) Unsigned8.unpackFromBuffer(rb, 9);
          final long vz = (long) Unsigned8.unpackFromBuffer(rb, 10);
          final long vw = (long) Unsigned8.unpackFromBuffer(rb, 11);
          Assert.assertEquals(0L, vx);
          Assert.assertEquals(128L, vy);
          Assert.assertEquals(255L, vz);
          Assert.assertEquals(128L, vw);

          v.get4UL(o);
          Assert.assertEquals(0L, o.getXL());
          Assert.assertEquals(128L, o.getYL());
          Assert.assertEquals(255L, o.getZL());
          Assert.assertEquals(128L, o.getWL());
        }
        break;
      }
      default: {
        throw new UnreachableCodeException();
      }
    }
  }

  private static void checkTypeUnsigned16(
    final int component_count,
    final ByteBuffer rb)
  {
    switch (component_count) {

      case 1: {
        final JPRACursor1DType<SMFByteBufferIntegerUnsigned1Type> c =
          SMFByteBufferCursors.createUnsigned1Raw(rb, 16, 0, 1 * 2);
        final SMFByteBufferIntegerUnsigned1Type v = c.getElementView();

        {
          final long vx = (long) Unsigned16.unpackFromBuffer(rb, 0);
          final long vy = (long) Unsigned16.unpackFromBuffer(rb, 2);
          final long vz = (long) Unsigned16.unpackFromBuffer(rb, 4);

          Assert.assertEquals(0L, vx);
          c.setElementIndex(0);
          Assert.assertEquals(0L, v.get1UL());

          Assert.assertEquals(32768L, vy);
          c.setElementIndex(1);
          Assert.assertEquals(32768L, v.get1UL());

          Assert.assertEquals(65535L, vz);
          c.setElementIndex(2);
          Assert.assertEquals(65535L, v.get1UL());
        }
        break;
      }

      case 2: {
        final JPRACursor1DType<SMFByteBufferIntegerUnsigned2Type> c =
          SMFByteBufferCursors.createUnsigned2Raw(rb, 16, 0, 2 * 2);
        final SMFByteBufferIntegerUnsigned2Type v = c.getElementView();
        final VectorM2L o = new VectorM2L();

        {
          final long vx = (long) Unsigned16.unpackFromBuffer(rb, 0);
          final long vy = (long) Unsigned16.unpackFromBuffer(rb, 2);
          Assert.assertEquals(0L, vx);
          Assert.assertEquals(65535L, vy);

          c.setElementIndex(0);
          v.get2UL(o);
          Assert.assertEquals(0L, o.getXL());
          Assert.assertEquals(65535L, o.getYL());
        }

        {
          final long vx = (long) Unsigned16.unpackFromBuffer(rb, 4);
          final long vy = (long) Unsigned16.unpackFromBuffer(rb, 6);
          Assert.assertEquals(0L, vx);
          Assert.assertEquals(65535L, vy);

          c.setElementIndex(1);
          v.get2UL(o);
          Assert.assertEquals(0L, o.getXL());
          Assert.assertEquals(65535L, o.getYL());
        }

        {
          final long vx = (long) Unsigned16.unpackFromBuffer(rb, 8);
          final long vy = (long) Unsigned16.unpackFromBuffer(rb, 10);
          Assert.assertEquals(0L, vx);
          Assert.assertEquals(65535L, vy);

          c.setElementIndex(2);
          v.get2UL(o);
          Assert.assertEquals(0L, o.getXL());
          Assert.assertEquals(65535L, o.getYL());
        }
        break;
      }

      case 3: {
        final JPRACursor1DType<SMFByteBufferIntegerUnsigned3Type> c =
          SMFByteBufferCursors.createUnsigned3Raw(rb, 16, 0, 3 * 2);
        final SMFByteBufferIntegerUnsigned3Type v = c.getElementView();
        final VectorM3L o = new VectorM3L();

        {
          final long vx = (long) Unsigned16.unpackFromBuffer(rb, 0);
          final long vy = (long) Unsigned16.unpackFromBuffer(rb, 2);
          final long vz = (long) Unsigned16.unpackFromBuffer(rb, 4);
          Assert.assertEquals(0L, vx);
          Assert.assertEquals(32768L, vy);
          Assert.assertEquals(65535L, vz);

          c.setElementIndex(0);
          v.get3UL(o);
          Assert.assertEquals(0L, o.getXL());
          Assert.assertEquals(32768L, o.getYL());
          Assert.assertEquals(65535L, o.getZL());
        }

        {
          final long vx = (long) Unsigned16.unpackFromBuffer(rb, 6);
          final long vy = (long) Unsigned16.unpackFromBuffer(rb, 8);
          final long vz = (long) Unsigned16.unpackFromBuffer(rb, 10);
          Assert.assertEquals(0L, vx);
          Assert.assertEquals(32768L, vy);
          Assert.assertEquals(65535L, vz);

          c.setElementIndex(1);
          v.get3UL(o);
          Assert.assertEquals(0L, o.getXL());
          Assert.assertEquals(32768L, o.getYL());
          Assert.assertEquals(65535L, o.getZL());
        }

        {
          final long vx = (long) Unsigned16.unpackFromBuffer(rb, 12);
          final long vy = (long) Unsigned16.unpackFromBuffer(rb, 14);
          final long vz = (long) Unsigned16.unpackFromBuffer(rb, 16);
          Assert.assertEquals(0L, vx);
          Assert.assertEquals(32768L, vy);
          Assert.assertEquals(65535L, vz);

          c.setElementIndex(2);
          v.get3UL(o);
          Assert.assertEquals(0L, o.getXL());
          Assert.assertEquals(32768L, o.getYL());
          Assert.assertEquals(65535L, o.getZL());
        }
        break;
      }

      case 4: {
        final JPRACursor1DType<SMFByteBufferIntegerUnsigned4Type> c =
          SMFByteBufferCursors.createUnsigned4Raw(rb, 16, 0, 4 * 2);
        final SMFByteBufferIntegerUnsigned4Type v = c.getElementView();
        final VectorM4L o = new VectorM4L();

        {
          final long vx = (long) Unsigned16.unpackFromBuffer(rb, 0);
          final long vy = (long) Unsigned16.unpackFromBuffer(rb, 2);
          final long vz = (long) Unsigned16.unpackFromBuffer(rb, 4);
          final long vw = (long) Unsigned16.unpackFromBuffer(rb, 6);
          Assert.assertEquals(0L, vx);
          Assert.assertEquals(32768L, vy);
          Assert.assertEquals(65535L, vz);
          Assert.assertEquals(1L, vw);

          c.setElementIndex(0);
          v.get4UL(o);
          Assert.assertEquals(0L, o.getXL());
          Assert.assertEquals(32768L, o.getYL());
          Assert.assertEquals(65535L, o.getZL());
          Assert.assertEquals(1L, o.getWL());
        }

        {
          final long vx = (long) Unsigned16.unpackFromBuffer(rb, 8);
          final long vy = (long) Unsigned16.unpackFromBuffer(rb, 10);
          final long vz = (long) Unsigned16.unpackFromBuffer(rb, 12);
          final long vw = (long) Unsigned16.unpackFromBuffer(rb, 14);
          Assert.assertEquals(0L, vx);
          Assert.assertEquals(32768L, vy);
          Assert.assertEquals(65535L, vz);
          Assert.assertEquals(1L, vw);

          c.setElementIndex(1);
          v.get4UL(o);
          Assert.assertEquals(0L, o.getXL());
          Assert.assertEquals(32768L, o.getYL());
          Assert.assertEquals(65535L, o.getZL());
          Assert.assertEquals(1L, o.getWL());
        }

        {
          final long vx = (long) Unsigned16.unpackFromBuffer(rb, 16);
          final long vy = (long) Unsigned16.unpackFromBuffer(rb, 18);
          final long vz = (long) Unsigned16.unpackFromBuffer(rb, 20);
          final long vw = (long) Unsigned16.unpackFromBuffer(rb, 22);
          Assert.assertEquals(0L, vx);
          Assert.assertEquals(32768L, vy);
          Assert.assertEquals(65535L, vz);
          Assert.assertEquals(1L, vw);

          c.setElementIndex(2);
          v.get4UL(o);
          Assert.assertEquals(0L, o.getXL());
          Assert.assertEquals(32768L, o.getYL());
          Assert.assertEquals(65535L, o.getZL());
          Assert.assertEquals(1L, o.getWL());
        }
        break;
      }
      default: {
        throw new UnreachableCodeException();
      }
    }
  }

  private static void checkTypeUnsigned32(
    final int component_count,
    final ByteBuffer rb)
  {
    switch (component_count) {
      case 1: {
        final JPRACursor1DType<SMFByteBufferIntegerUnsigned1Type> c =
          SMFByteBufferCursors.createUnsigned1Raw(rb, 32, 0, 1 * 4);
        final SMFByteBufferIntegerUnsigned1Type v = c.getElementView();

        {
          final long vx = Unsigned32.unpackFromBuffer(rb, 0);
          final long vy = Unsigned32.unpackFromBuffer(rb, 4);
          final long vz = Unsigned32.unpackFromBuffer(rb, 8);

          Assert.assertEquals(0L, vx);
          c.setElementIndex(0);
          Assert.assertEquals(0L, v.get1UL());

          Assert.assertEquals(2147483648L, vy);
          c.setElementIndex(1);
          Assert.assertEquals(2147483648L, v.get1UL());

          Assert.assertEquals(4294967295L, vz);
          c.setElementIndex(2);
          Assert.assertEquals(4294967295L, v.get1UL());
        }
        break;
      }

      case 2: {
        final JPRACursor1DType<SMFByteBufferIntegerUnsigned2Type> c =
          SMFByteBufferCursors.createUnsigned2Raw(rb, 32, 0, 2 * 4);
        final SMFByteBufferIntegerUnsigned2Type v = c.getElementView();
        final VectorM2L o = new VectorM2L();

        {
          final long vx = Unsigned32.unpackFromBuffer(rb, 0);
          final long vy = Unsigned32.unpackFromBuffer(rb, 4);
          Assert.assertEquals(0L, vx);
          Assert.assertEquals(4294967295L, vy);

          c.setElementIndex(0);
          v.get2UL(o);
          Assert.assertEquals(0L, o.getXL());
          Assert.assertEquals(4294967295L, o.getYL());
        }

        {
          final long vx = Unsigned32.unpackFromBuffer(rb, 8);
          final long vy = Unsigned32.unpackFromBuffer(rb, 12);
          Assert.assertEquals(0L, vx);
          Assert.assertEquals(4294967295L, vy);

          c.setElementIndex(1);
          v.get2UL(o);
          Assert.assertEquals(0L, o.getXL());
          Assert.assertEquals(4294967295L, o.getYL());
        }

        {
          final long vx = Unsigned32.unpackFromBuffer(rb, 16);
          final long vy = Unsigned32.unpackFromBuffer(rb, 20);
          Assert.assertEquals(0L, vx);
          Assert.assertEquals(4294967295L, vy);

          c.setElementIndex(2);
          v.get2UL(o);
          Assert.assertEquals(0L, o.getXL());
          Assert.assertEquals(4294967295L, o.getYL());
        }
        break;
      }

      case 3: {
        final JPRACursor1DType<SMFByteBufferIntegerUnsigned3Type> c =
          SMFByteBufferCursors.createUnsigned3Raw(rb, 32, 0, 3 * 4);
        final SMFByteBufferIntegerUnsigned3Type v = c.getElementView();
        final VectorM3L o = new VectorM3L();

        {
          final long vx = Unsigned32.unpackFromBuffer(rb, 0);
          final long vy = Unsigned32.unpackFromBuffer(rb, 4);
          final long vz = Unsigned32.unpackFromBuffer(rb, 8);
          Assert.assertEquals(0L, vx);
          Assert.assertEquals(2147483648L, vy);
          Assert.assertEquals(4294967295L, vz);

          c.setElementIndex(0);
          v.get3UL(o);
          Assert.assertEquals(0L, o.getXL());
          Assert.assertEquals(2147483648L, o.getYL());
          Assert.assertEquals(4294967295L, o.getZL());
        }

        {
          final long vx = Unsigned32.unpackFromBuffer(rb, 12);
          final long vy = Unsigned32.unpackFromBuffer(rb, 16);
          final long vz = Unsigned32.unpackFromBuffer(rb, 20);
          Assert.assertEquals(0L, vx);
          Assert.assertEquals(2147483648L, vy);
          Assert.assertEquals(4294967295L, vz);

          c.setElementIndex(1);
          v.get3UL(o);
          Assert.assertEquals(0L, o.getXL());
          Assert.assertEquals(2147483648L, o.getYL());
          Assert.assertEquals(4294967295L, o.getZL());
        }

        {
          final long vx = Unsigned32.unpackFromBuffer(rb, 24);
          final long vy = Unsigned32.unpackFromBuffer(rb, 28);
          final long vz = Unsigned32.unpackFromBuffer(rb, 32);
          Assert.assertEquals(0L, vx);
          Assert.assertEquals(2147483648L, vy);
          Assert.assertEquals(4294967295L, vz);

          c.setElementIndex(2);
          v.get3UL(o);
          Assert.assertEquals(0L, o.getXL());
          Assert.assertEquals(2147483648L, o.getYL());
          Assert.assertEquals(4294967295L, o.getZL());
        }
        break;
      }

      case 4: {
        final JPRACursor1DType<SMFByteBufferIntegerUnsigned4Type> c =
          SMFByteBufferCursors.createUnsigned4Raw(rb, 32, 0, 4 * 4);
        final SMFByteBufferIntegerUnsigned4Type v = c.getElementView();
        final VectorM4L o = new VectorM4L();

        {
          final long vx = Unsigned32.unpackFromBuffer(rb, 0);
          final long vy = Unsigned32.unpackFromBuffer(rb, 4);
          final long vz = Unsigned32.unpackFromBuffer(rb, 8);
          final long vw = Unsigned32.unpackFromBuffer(rb, 12);
          Assert.assertEquals(0L, vx);
          Assert.assertEquals(2147483648L, vy);
          Assert.assertEquals(4294967295L, vz);
          Assert.assertEquals(1L, vw);

          c.setElementIndex(0);
          v.get4UL(o);
          Assert.assertEquals(0L, o.getXL());
          Assert.assertEquals(2147483648L, o.getYL());
          Assert.assertEquals(4294967295L, o.getZL());
          Assert.assertEquals(1L, o.getWL());
        }

        {
          final long vx = Unsigned32.unpackFromBuffer(rb, 16);
          final long vy = Unsigned32.unpackFromBuffer(rb, 20);
          final long vz = Unsigned32.unpackFromBuffer(rb, 24);
          final long vw = Unsigned32.unpackFromBuffer(rb, 28);
          Assert.assertEquals(0L, vx);
          Assert.assertEquals(2147483648L, vy);
          Assert.assertEquals(4294967295L, vz);
          Assert.assertEquals(1L, vw);

          c.setElementIndex(1);
          v.get4UL(o);
          Assert.assertEquals(0L, o.getXL());
          Assert.assertEquals(2147483648L, o.getYL());
          Assert.assertEquals(4294967295L, o.getZL());
          Assert.assertEquals(1L, o.getWL());
        }

        {
          final long vx = Unsigned32.unpackFromBuffer(rb, 32);
          final long vy = Unsigned32.unpackFromBuffer(rb, 36);
          final long vz = Unsigned32.unpackFromBuffer(rb, 40);
          final long vw = Unsigned32.unpackFromBuffer(rb, 44);
          Assert.assertEquals(0L, vx);
          Assert.assertEquals(2147483648L, vy);
          Assert.assertEquals(4294967295L, vz);
          Assert.assertEquals(1L, vw);

          c.setElementIndex(2);
          v.get4UL(o);
          Assert.assertEquals(0L, o.getXL());
          Assert.assertEquals(2147483648L, o.getYL());
          Assert.assertEquals(4294967295L, o.getZL());
          Assert.assertEquals(1L, o.getWL());
        }
        break;
      }
      default: {
        throw new UnreachableCodeException();
      }
    }
  }

  private static void checkTypeUnsigned64(
    final int component_count,
    final ByteBuffer rb)
  {
    switch (component_count) {

      case 1: {
        final JPRACursor1DType<SMFByteBufferIntegerUnsigned1Type> c =
          SMFByteBufferCursors.createUnsigned1Raw(rb, 64, 0, 1 * 8);
        final SMFByteBufferIntegerUnsigned1Type v = c.getElementView();

        {
          Assert.assertEquals(0L, rb.getLong(0));
          c.setElementIndex(0);
          Assert.assertEquals(0L, v.get1UL());

          Assert.assertEquals(0x8000000000000000L, rb.getLong(8));
          c.setElementIndex(1);
          Assert.assertEquals(0x8000000000000000L, v.get1UL());

          Assert.assertEquals(0xffffffffffffffffL, rb.getLong(16));
          c.setElementIndex(2);
          Assert.assertEquals(0xffffffffffffffffL, v.get1UL());
        }
        break;
      }

      case 2: {
        final JPRACursor1DType<SMFByteBufferIntegerUnsigned2Type> c =
          SMFByteBufferCursors.createUnsigned2Raw(rb, 64, 0, 2 * 8);
        final SMFByteBufferIntegerUnsigned2Type v = c.getElementView();
        final VectorM2L o = new VectorM2L();

        {
          final long vx = rb.getLong(0);
          final long vy = rb.getLong(8);
          Assert.assertEquals(0L, vx);
          Assert.assertEquals(0xffffffffffffffffL, vy);

          c.setElementIndex(0);
          v.get2UL(o);
          Assert.assertEquals(0L, o.getXL());
          Assert.assertEquals(0xffffffffffffffffL, o.getYL());
        }

        {
          final long vx = rb.getLong(16);
          final long vy = rb.getLong(24);
          Assert.assertEquals(0L, vx);
          Assert.assertEquals(0xffffffffffffffffL, vy);

          c.setElementIndex(1);
          v.get2UL(o);
          Assert.assertEquals(0L, o.getXL());
          Assert.assertEquals(0xffffffffffffffffL, o.getYL());
        }

        {
          final long vx = rb.getLong(32);
          final long vy = rb.getLong(40);
          Assert.assertEquals(0L, vx);
          Assert.assertEquals(0xffffffffffffffffL, vy);

          c.setElementIndex(2);
          v.get2UL(o);
          Assert.assertEquals(0L, o.getXL());
          Assert.assertEquals(0xffffffffffffffffL, o.getYL());
        }
        break;
      }

      case 3: {
        final JPRACursor1DType<SMFByteBufferIntegerUnsigned3Type> c =
          SMFByteBufferCursors.createUnsigned3Raw(rb, 64, 0, 3 * 8);
        final SMFByteBufferIntegerUnsigned3Type v = c.getElementView();
        final VectorM3L o = new VectorM3L();

        {
          final long vx = rb.getLong(0);
          final long vy = rb.getLong(8);
          final long vz = rb.getLong(16);
          Assert.assertEquals(0L, vx);
          Assert.assertEquals(0x8000000000000000L, vy);
          Assert.assertEquals(0xffffffffffffffffL, vz);

          c.setElementIndex(0);
          v.get3UL(o);
          Assert.assertEquals(0L, o.getXL());
          Assert.assertEquals(0x8000000000000000L, o.getYL());
          Assert.assertEquals(0xffffffffffffffffL, o.getZL());
        }

        {
          final long vx = rb.getLong(24);
          final long vy = rb.getLong(32);
          final long vz = rb.getLong(40);
          Assert.assertEquals(0L, vx);
          Assert.assertEquals(0x8000000000000000L, vy);
          Assert.assertEquals(0xffffffffffffffffL, vz);

          c.setElementIndex(1);
          v.get3UL(o);
          Assert.assertEquals(0L, o.getXL());
          Assert.assertEquals(0x8000000000000000L, o.getYL());
          Assert.assertEquals(0xffffffffffffffffL, o.getZL());
        }

        {
          final long vx = rb.getLong(48);
          final long vy = rb.getLong(56);
          final long vz = rb.getLong(64);
          Assert.assertEquals(0L, vx);
          Assert.assertEquals(0x8000000000000000L, vy);
          Assert.assertEquals(0xffffffffffffffffL, vz);

          c.setElementIndex(2);
          v.get3UL(o);
          Assert.assertEquals(0L, o.getXL());
          Assert.assertEquals(0x8000000000000000L, o.getYL());
          Assert.assertEquals(0xffffffffffffffffL, o.getZL());
        }
        break;
      }

      case 4: {
        final JPRACursor1DType<SMFByteBufferIntegerUnsigned4Type> c =
          SMFByteBufferCursors.createUnsigned4Raw(rb, 64, 0, 4 * 8);
        final SMFByteBufferIntegerUnsigned4Type v = c.getElementView();
        final VectorM4L o = new VectorM4L();

        {
          final long vx = rb.getLong(0);
          final long vy = rb.getLong(8);
          final long vz = rb.getLong(16);
          final long vw = rb.getLong(24);
          Assert.assertEquals(0L, vx);
          Assert.assertEquals(0x8000000000000000L, vy);
          Assert.assertEquals(0xffffffffffffffffL, vz);
          Assert.assertEquals(1L, vw);

          c.setElementIndex(0);
          v.get4UL(o);
          Assert.assertEquals(0L, o.getXL());
          Assert.assertEquals(0x8000000000000000L, o.getYL());
          Assert.assertEquals(0xffffffffffffffffL, o.getZL());
          Assert.assertEquals(1L, o.getWL());
        }

        {
          final long vx = rb.getLong(32);
          final long vy = rb.getLong(40);
          final long vz = rb.getLong(48);
          final long vw = rb.getLong(56);
          Assert.assertEquals(0L, vx);
          Assert.assertEquals(0x8000000000000000L, vy);
          Assert.assertEquals(0xffffffffffffffffL, vz);
          Assert.assertEquals(1L, vw);

          c.setElementIndex(1);
          v.get4UL(o);
          Assert.assertEquals(0L, o.getXL());
          Assert.assertEquals(0x8000000000000000L, o.getYL());
          Assert.assertEquals(0xffffffffffffffffL, o.getZL());
          Assert.assertEquals(1L, o.getWL());
        }

        {
          final long vx = rb.getLong(64);
          final long vy = rb.getLong(72);
          final long vz = rb.getLong(80);
          final long vw = rb.getLong(88);
          Assert.assertEquals(0L, vx);
          Assert.assertEquals(0x8000000000000000L, vy);
          Assert.assertEquals(0xffffffffffffffffL, vz);
          Assert.assertEquals(1L, vw);

          c.setElementIndex(2);
          v.get4UL(o);
          Assert.assertEquals(0L, o.getXL());
          Assert.assertEquals(0x8000000000000000L, o.getYL());
          Assert.assertEquals(0xffffffffffffffffL, o.getZL());
          Assert.assertEquals(1L, o.getWL());
        }
        break;
      }

      default: {
        throw new UnreachableCodeException();
      }
    }
  }

  private static void checkTypeSigned8(
    final int component_count,
    final ByteBuffer rb)
  {
    switch (component_count) {

      case 1: {
        final JPRACursor1DType<SMFByteBufferIntegerSigned1Type> c =
          SMFByteBufferCursors.createSigned1Raw(rb, 8, 0, 1);
        final SMFByteBufferIntegerSigned1Type v = c.getElementView();

        c.setElementIndex(0);
        Assert.assertEquals(-127L, v.get1SL());
        Assert.assertEquals(-127L, (long) rb.get(0));

        c.setElementIndex(1);
        Assert.assertEquals(0L, v.get1SL());
        Assert.assertEquals(0L, (long) rb.get(1));

        c.setElementIndex(2);
        Assert.assertEquals(127L, v.get1SL());
        Assert.assertEquals(127L, (long) rb.get(2));
        break;
      }

      case 2: {
        final JPRACursor1DType<SMFByteBufferIntegerSigned2Type> c =
          SMFByteBufferCursors.createSigned2Raw(rb, 8, 0, 2);
        final SMFByteBufferIntegerSigned2Type v = c.getElementView();
        final VectorM2L o = new VectorM2L();

        c.setElementIndex(0);
        Assert.assertEquals(-127L, (long) rb.get(0));
        Assert.assertEquals(0L, (long) rb.get(1));
        v.get2SL(o);
        Assert.assertEquals(-127L, o.getXL());
        Assert.assertEquals(0L, o.getYL());

        c.setElementIndex(1);
        Assert.assertEquals(0L, (long) rb.get(2));
        Assert.assertEquals(0L, (long) rb.get(3));
        v.get2SL(o);
        Assert.assertEquals(0L, o.getXL());
        Assert.assertEquals(0L, o.getYL());

        c.setElementIndex(2);
        Assert.assertEquals(127L, (long) rb.get(4));
        Assert.assertEquals(0L, (long) rb.get(5));
        v.get2SL(o);
        Assert.assertEquals(127L, o.getXL());
        Assert.assertEquals(0L, o.getYL());
        break;
      }

      case 3: {
        final JPRACursor1DType<SMFByteBufferIntegerSigned3Type> c =
          SMFByteBufferCursors.createSigned3Raw(rb, 8, 0, 3);
        final SMFByteBufferIntegerSigned3Type v = c.getElementView();
        final VectorM3L o = new VectorM3L();

        c.setElementIndex(0);
        Assert.assertEquals(-127L, (long) rb.get(0));
        Assert.assertEquals(0L, (long) rb.get(1));
        Assert.assertEquals(127L, (long) rb.get(2));
        v.get3SL(o);
        Assert.assertEquals(-127L, o.getXL());
        Assert.assertEquals(0L, o.getYL());
        Assert.assertEquals(127L, o.getZL());

        c.setElementIndex(1);
        Assert.assertEquals(-127L, (long) rb.get(3));
        Assert.assertEquals(0L, (long) rb.get(4));
        Assert.assertEquals(127L, (long) rb.get(5));
        v.get3SL(o);
        Assert.assertEquals(-127L, o.getXL());
        Assert.assertEquals(0L, o.getYL());
        Assert.assertEquals(127L, o.getZL());

        c.setElementIndex(2);
        Assert.assertEquals(-127L, (long) rb.get(6));
        Assert.assertEquals(0L, (long) rb.get(7));
        Assert.assertEquals(127L, (long) rb.get(8));
        v.get3SL(o);
        Assert.assertEquals(-127L, o.getXL());
        Assert.assertEquals(0L, o.getYL());
        Assert.assertEquals(127L, o.getZL());
        break;
      }

      case 4: {
        final JPRACursor1DType<SMFByteBufferIntegerSigned4Type> c =
          SMFByteBufferCursors.createSigned4Raw(rb, 8, 0, 4);
        final SMFByteBufferIntegerSigned4Type v = c.getElementView();
        final VectorM4L o = new VectorM4L();

        c.setElementIndex(0);
        Assert.assertEquals(-127L, (long) rb.get(0));
        Assert.assertEquals(0L, (long) rb.get(1));
        Assert.assertEquals(127L, (long) rb.get(2));
        Assert.assertEquals(0L, (long) rb.get(3));
        v.get4SL(o);
        Assert.assertEquals(-127L, o.getXL());
        Assert.assertEquals(0L, o.getYL());
        Assert.assertEquals(127L, o.getZL());
        Assert.assertEquals(0L, o.getWL());

        c.setElementIndex(1);
        Assert.assertEquals(-127L, (long) rb.get(4));
        Assert.assertEquals(0L, (long) rb.get(5));
        Assert.assertEquals(127L, (long) rb.get(6));
        Assert.assertEquals(0L, (long) rb.get(7));
        v.get4SL(o);
        Assert.assertEquals(-127L, o.getXL());
        Assert.assertEquals(0L, o.getYL());
        Assert.assertEquals(127L, o.getZL());
        Assert.assertEquals(0L, o.getWL());

        c.setElementIndex(2);
        Assert.assertEquals(-127L, (long) rb.get(8));
        Assert.assertEquals(0L, (long) rb.get(9));
        Assert.assertEquals(127L, (long) rb.get(10));
        Assert.assertEquals(0L, (long) rb.get(11));
        v.get4SL(o);
        Assert.assertEquals(-127L, o.getXL());
        Assert.assertEquals(0L, o.getYL());
        Assert.assertEquals(127L, o.getZL());
        Assert.assertEquals(0L, o.getWL());

        break;
      }
      default: {
        throw new UnreachableCodeException();
      }
    }
  }

  private static void checkTypeSigned16(
    final int component_count,
    final ByteBuffer rb)
  {
    switch (component_count) {
      case 1: {
        final JPRACursor1DType<SMFByteBufferIntegerSigned1Type> c =
          SMFByteBufferCursors.createSigned1Raw(rb, 16, 0, 1 * 2);
        final SMFByteBufferIntegerSigned1Type v = c.getElementView();

        {
          final long vx = (long) Signed16.unpackFromBuffer(rb, 0);
          final long vy = (long) Signed16.unpackFromBuffer(rb, 2);
          final long vz = (long) Signed16.unpackFromBuffer(rb, 4);

          Assert.assertEquals(-32767L, vx);
          c.setElementIndex(0);
          Assert.assertEquals(-32767L, v.get1SL());

          Assert.assertEquals(0L, vy);
          c.setElementIndex(1);
          Assert.assertEquals(0L, v.get1SL());

          Assert.assertEquals(32767L, vz);
          c.setElementIndex(2);
          Assert.assertEquals(32767L, v.get1SL());
        }
        break;
      }

      case 2: {
        final JPRACursor1DType<SMFByteBufferIntegerSigned2Type> c =
          SMFByteBufferCursors.createSigned2Raw(rb, 16, 0, 2 * 2);
        final SMFByteBufferIntegerSigned2Type v = c.getElementView();
        final VectorM2L o = new VectorM2L();

        {
          final long vx = (long) Signed16.unpackFromBuffer(rb, 0);
          final long vy = (long) Signed16.unpackFromBuffer(rb, 2);
          Assert.assertEquals(-32767L, vx);
          Assert.assertEquals(32767L, vy);

          c.setElementIndex(0);
          v.get2SL(o);
          Assert.assertEquals(-32767L, o.getXL());
          Assert.assertEquals(32767L, o.getYL());
        }

        {
          final long vx = (long) Signed16.unpackFromBuffer(rb, 4);
          final long vy = (long) Signed16.unpackFromBuffer(rb, 6);
          Assert.assertEquals(-32767L, vx);
          Assert.assertEquals(32767L, vy);

          c.setElementIndex(1);
          v.get2SL(o);
          Assert.assertEquals(-32767L, o.getXL());
          Assert.assertEquals(32767L, o.getYL());
        }

        {
          final long vx = (long) Signed16.unpackFromBuffer(rb, 8);
          final long vy = (long) Signed16.unpackFromBuffer(rb, 10);
          Assert.assertEquals(-32767L, vx);
          Assert.assertEquals(32767L, vy);

          c.setElementIndex(2);
          v.get2SL(o);
          Assert.assertEquals(-32767L, o.getXL());
          Assert.assertEquals(32767L, o.getYL());
        }
        break;
      }

      case 3: {
        final JPRACursor1DType<SMFByteBufferIntegerSigned3Type> c =
          SMFByteBufferCursors.createSigned3Raw(rb, 16, 0, 3 * 2);
        final SMFByteBufferIntegerSigned3Type v = c.getElementView();
        final VectorM3L o = new VectorM3L();

        {
          final long vx = (long) Signed16.unpackFromBuffer(rb, 0);
          final long vy = (long) Signed16.unpackFromBuffer(rb, 2);
          final long vz = (long) Signed16.unpackFromBuffer(rb, 4);
          Assert.assertEquals(-32767L, vx);
          Assert.assertEquals(0L, vy);
          Assert.assertEquals(32767L, vz);

          c.setElementIndex(0);
          v.get3SL(o);
          Assert.assertEquals(-32767L, o.getXL());
          Assert.assertEquals(0L, o.getYL());
          Assert.assertEquals(32767L, o.getZL());
        }

        {
          final long vx = (long) Signed16.unpackFromBuffer(rb, 6);
          final long vy = (long) Signed16.unpackFromBuffer(rb, 8);
          final long vz = (long) Signed16.unpackFromBuffer(rb, 10);
          Assert.assertEquals(-32767L, vx);
          Assert.assertEquals(0L, vy);
          Assert.assertEquals(32767L, vz);

          c.setElementIndex(1);
          v.get3SL(o);
          Assert.assertEquals(-32767L, o.getXL());
          Assert.assertEquals(0L, o.getYL());
          Assert.assertEquals(32767L, o.getZL());
        }

        {
          final long vx = (long) Signed16.unpackFromBuffer(rb, 12);
          final long vy = (long) Signed16.unpackFromBuffer(rb, 14);
          final long vz = (long) Signed16.unpackFromBuffer(rb, 16);
          Assert.assertEquals(-32767L, vx);
          Assert.assertEquals(0L, vy);
          Assert.assertEquals(32767L, vz);

          c.setElementIndex(2);
          v.get3SL(o);
          Assert.assertEquals(-32767L, o.getXL());
          Assert.assertEquals(0L, o.getYL());
          Assert.assertEquals(32767L, o.getZL());
        }
        break;
      }

      case 4: {
        final JPRACursor1DType<SMFByteBufferIntegerSigned4Type> c =
          SMFByteBufferCursors.createSigned4Raw(rb, 16, 0, 4 * 2);
        final SMFByteBufferIntegerSigned4Type v = c.getElementView();
        final VectorM4L o = new VectorM4L();

        {
          final long vx = (long) Signed16.unpackFromBuffer(rb, 0);
          final long vy = (long) Signed16.unpackFromBuffer(rb, 2);
          final long vz = (long) Signed16.unpackFromBuffer(rb, 4);
          final long vw = (long) Signed16.unpackFromBuffer(rb, 6);
          Assert.assertEquals(-32767L, vx);
          Assert.assertEquals(0L, vy);
          Assert.assertEquals(32767L, vz);
          Assert.assertEquals(1L, vw);

          c.setElementIndex(0);
          v.get4SL(o);
          Assert.assertEquals(-32767L, o.getXL());
          Assert.assertEquals(0L, o.getYL());
          Assert.assertEquals(32767L, o.getZL());
          Assert.assertEquals(1L, o.getWL());
        }

        {
          final long vx = (long) Signed16.unpackFromBuffer(rb, 8);
          final long vy = (long) Signed16.unpackFromBuffer(rb, 10);
          final long vz = (long) Signed16.unpackFromBuffer(rb, 12);
          final long vw = (long) Signed16.unpackFromBuffer(rb, 14);
          Assert.assertEquals(-32767L, vx);
          Assert.assertEquals(0L, vy);
          Assert.assertEquals(32767L, vz);
          Assert.assertEquals(1L, vw);

          c.setElementIndex(1);
          v.get4SL(o);
          Assert.assertEquals(-32767L, o.getXL());
          Assert.assertEquals(0L, o.getYL());
          Assert.assertEquals(32767L, o.getZL());
          Assert.assertEquals(1L, o.getWL());
        }

        {
          final long vx = (long) Signed16.unpackFromBuffer(rb, 16);
          final long vy = (long) Signed16.unpackFromBuffer(rb, 18);
          final long vz = (long) Signed16.unpackFromBuffer(rb, 20);
          final long vw = (long) Signed16.unpackFromBuffer(rb, 22);
          Assert.assertEquals(-32767L, vx);
          Assert.assertEquals(0L, vy);
          Assert.assertEquals(32767L, vz);
          Assert.assertEquals(1L, vw);

          c.setElementIndex(2);
          v.get4SL(o);
          Assert.assertEquals(-32767L, o.getXL());
          Assert.assertEquals(0L, o.getYL());
          Assert.assertEquals(32767L, o.getZL());
          Assert.assertEquals(1L, o.getWL());
        }
        break;
      }
      default: {
        throw new UnreachableCodeException();
      }
    }
  }

  private static void checkTypeSigned32(
    final int component_count,
    final ByteBuffer rb)
  {
    switch (component_count) {
      case 1: {
        final JPRACursor1DType<SMFByteBufferIntegerSigned1Type> c =
          SMFByteBufferCursors.createSigned1Raw(rb, 32, 0, 1 * 4);
        final SMFByteBufferIntegerSigned1Type v = c.getElementView();

        {
          Assert.assertEquals(-2147483648L, (long) rb.getInt(0));
          c.setElementIndex(0);
          Assert.assertEquals(-2147483648L, v.get1SL());

          Assert.assertEquals(0L, (long) rb.getInt(4));
          c.setElementIndex(1);
          Assert.assertEquals(0L, v.get1SL());

          Assert.assertEquals(2147483647L, (long) rb.getInt(8));
          c.setElementIndex(2);
          Assert.assertEquals(2147483647L, v.get1SL());
        }
        break;
      }

      case 2: {
        final JPRACursor1DType<SMFByteBufferIntegerSigned2Type> c =
          SMFByteBufferCursors.createSigned2Raw(rb, 32, 0, 2 * 4);
        final SMFByteBufferIntegerSigned2Type v = c.getElementView();
        final VectorM2L o = new VectorM2L();

        {
          final long vx = (long) Signed32.unpackFromBuffer(rb, 0);
          final long vy = (long) Signed32.unpackFromBuffer(rb, 4);
          Assert.assertEquals(-2147483648L, vx);
          Assert.assertEquals(2147483647L, vy);

          c.setElementIndex(0);
          v.get2SL(o);
          Assert.assertEquals(-2147483648L, o.getXL());
          Assert.assertEquals(2147483647L, o.getYL());
        }

        {
          final long vx = (long) Signed32.unpackFromBuffer(rb, 8);
          final long vy = (long) Signed32.unpackFromBuffer(rb, 12);
          Assert.assertEquals(-2147483648L, vx);
          Assert.assertEquals(2147483647L, vy);

          c.setElementIndex(1);
          v.get2SL(o);
          Assert.assertEquals(-2147483648L, o.getXL());
          Assert.assertEquals(2147483647L, o.getYL());
        }

        {
          final long vx = (long) Signed32.unpackFromBuffer(rb, 16);
          final long vy = (long) Signed32.unpackFromBuffer(rb, 20);
          Assert.assertEquals(-2147483648L, vx);
          Assert.assertEquals(2147483647L, vy);

          c.setElementIndex(2);
          v.get2SL(o);
          Assert.assertEquals(-2147483648L, o.getXL());
          Assert.assertEquals(2147483647L, o.getYL());
        }
        break;
      }

      case 3: {
        final JPRACursor1DType<SMFByteBufferIntegerSigned3Type> c =
          SMFByteBufferCursors.createSigned3Raw(rb, 32, 0, 3 * 4);
        final SMFByteBufferIntegerSigned3Type v = c.getElementView();
        final VectorM3L o = new VectorM3L();

        {
          final long vx = (long) Signed32.unpackFromBuffer(rb, 0);
          final long vy = (long) Signed32.unpackFromBuffer(rb, 4);
          final long vz = (long) Signed32.unpackFromBuffer(rb, 8);
          Assert.assertEquals(-2147483648L, vx);
          Assert.assertEquals(0L, vy);
          Assert.assertEquals(2147483647L, vz);

          c.setElementIndex(0);
          v.get3SL(o);
          Assert.assertEquals(-2147483648L, o.getXL());
          Assert.assertEquals(0L, o.getYL());
          Assert.assertEquals(2147483647L, o.getZL());
        }

        {
          final long vx = (long) Signed32.unpackFromBuffer(rb, 12);
          final long vy = (long) Signed32.unpackFromBuffer(rb, 16);
          final long vz = (long) Signed32.unpackFromBuffer(rb, 20);
          Assert.assertEquals(-2147483648L, vx);
          Assert.assertEquals(0L, vy);
          Assert.assertEquals(2147483647L, vz);

          c.setElementIndex(0);
          v.get3SL(o);
          Assert.assertEquals(-2147483648L, o.getXL());
          Assert.assertEquals(0L, o.getYL());
          Assert.assertEquals(2147483647L, o.getZL());
        }

        {
          final long vx = (long) Signed32.unpackFromBuffer(rb, 24);
          final long vy = (long) Signed32.unpackFromBuffer(rb, 28);
          final long vz = (long) Signed32.unpackFromBuffer(rb, 32);
          Assert.assertEquals(-2147483648L, vx);
          Assert.assertEquals(0L, vy);
          Assert.assertEquals(2147483647L, vz);

          c.setElementIndex(0);
          v.get3SL(o);
          Assert.assertEquals(-2147483648L, o.getXL());
          Assert.assertEquals(0L, o.getYL());
          Assert.assertEquals(2147483647L, o.getZL());
        }
        break;
      }

      case 4: {
        final JPRACursor1DType<SMFByteBufferIntegerSigned4Type> c =
          SMFByteBufferCursors.createSigned4Raw(rb, 32, 0, 4 * 4);
        final SMFByteBufferIntegerSigned4Type v = c.getElementView();
        final VectorM4L o = new VectorM4L();

        {
          final long vx = (long) Signed32.unpackFromBuffer(rb, 0);
          final long vy = (long) Signed32.unpackFromBuffer(rb, 4);
          final long vz = (long) Signed32.unpackFromBuffer(rb, 8);
          final long vw = (long) Signed32.unpackFromBuffer(rb, 12);
          Assert.assertEquals(-2147483648L, vx);
          Assert.assertEquals(0L, vy);
          Assert.assertEquals(2147483647L, vz);
          Assert.assertEquals(1L, vw);

          c.setElementIndex(0);
          v.get4SL(o);
          Assert.assertEquals(-2147483648L, o.getXL());
          Assert.assertEquals(0L, o.getYL());
          Assert.assertEquals(2147483647L, o.getZL());
          Assert.assertEquals(1L, o.getWL());
        }

        {
          final long vx = (long) Signed32.unpackFromBuffer(rb, 16);
          final long vy = (long) Signed32.unpackFromBuffer(rb, 20);
          final long vz = (long) Signed32.unpackFromBuffer(rb, 24);
          final long vw = (long) Signed32.unpackFromBuffer(rb, 28);
          Assert.assertEquals(-2147483648L, vx);
          Assert.assertEquals(0L, vy);
          Assert.assertEquals(2147483647L, vz);
          Assert.assertEquals(1L, vw);

          c.setElementIndex(1);
          v.get4SL(o);
          Assert.assertEquals(-2147483648L, o.getXL());
          Assert.assertEquals(0L, o.getYL());
          Assert.assertEquals(2147483647L, o.getZL());
          Assert.assertEquals(1L, o.getWL());
        }

        {
          final long vx = (long) Signed32.unpackFromBuffer(rb, 32);
          final long vy = (long) Signed32.unpackFromBuffer(rb, 36);
          final long vz = (long) Signed32.unpackFromBuffer(rb, 40);
          final long vw = (long) Signed32.unpackFromBuffer(rb, 44);
          Assert.assertEquals(-2147483648L, vx);
          Assert.assertEquals(0L, vy);
          Assert.assertEquals(2147483647L, vz);
          Assert.assertEquals(1L, vw);

          c.setElementIndex(2);
          v.get4SL(o);
          Assert.assertEquals(-2147483648L, o.getXL());
          Assert.assertEquals(0L, o.getYL());
          Assert.assertEquals(2147483647L, o.getZL());
          Assert.assertEquals(1L, o.getWL());
        }
        break;
      }
      default: {
        throw new UnreachableCodeException();
      }
    }
  }

  private static void checkTypeSigned64(
    final int component_count,
    final ByteBuffer rb)
  {
    switch (component_count) {
      case 1: {
        final JPRACursor1DType<SMFByteBufferIntegerSigned1Type> c =
          SMFByteBufferCursors.createSigned1Raw(rb, 64, 0, 1 * 8);
        final SMFByteBufferIntegerSigned1Type v = c.getElementView();

        {
          Assert.assertEquals(-9223372036854775808L, rb.getLong(0));
          c.setElementIndex(0);
          Assert.assertEquals(-9223372036854775808L, v.get1SL());

          Assert.assertEquals(0L, rb.getLong(8));
          c.setElementIndex(1);
          Assert.assertEquals(0L, v.get1SL());

          Assert.assertEquals(9223372036854775807L, rb.getLong(16));
          c.setElementIndex(2);
          Assert.assertEquals(9223372036854775807L, v.get1SL());
        }
        break;
      }

      case 2: {
        final JPRACursor1DType<SMFByteBufferIntegerSigned2Type> c =
          SMFByteBufferCursors.createSigned2Raw(rb, 64, 0, 2 * 8);
        final SMFByteBufferIntegerSigned2Type v = c.getElementView();
        final VectorM2L o = new VectorM2L();

        {
          final long vx = rb.getLong(0);
          final long vy = rb.getLong(8);
          Assert.assertEquals(-9223372036854775808L, vx);
          Assert.assertEquals(9223372036854775807L, vy);

          c.setElementIndex(0);
          v.get2SL(o);
          Assert.assertEquals(-9223372036854775808L, o.getXL());
          Assert.assertEquals(9223372036854775807L, o.getYL());
        }

        {
          final long vx = rb.getLong(16);
          final long vy = rb.getLong(24);
          Assert.assertEquals(-9223372036854775808L, vx);
          Assert.assertEquals(9223372036854775807L, vy);

          c.setElementIndex(1);
          v.get2SL(o);
          Assert.assertEquals(-9223372036854775808L, o.getXL());
          Assert.assertEquals(9223372036854775807L, o.getYL());
        }

        {
          final long vx = rb.getLong(32);
          final long vy = rb.getLong(40);
          Assert.assertEquals(-9223372036854775808L, vx);
          Assert.assertEquals(9223372036854775807L, vy);

          c.setElementIndex(2);
          v.get2SL(o);
          Assert.assertEquals(-9223372036854775808L, o.getXL());
          Assert.assertEquals(9223372036854775807L, o.getYL());
        }
        break;
      }

      case 3: {
        final JPRACursor1DType<SMFByteBufferIntegerSigned3Type> c =
          SMFByteBufferCursors.createSigned3Raw(rb, 64, 0, 3 * 8);
        final SMFByteBufferIntegerSigned3Type v = c.getElementView();
        final VectorM3L o = new VectorM3L();

        {
          final long vx = rb.getLong(0);
          final long vy = rb.getLong(8);
          final long vz = rb.getLong(16);
          Assert.assertEquals(-9223372036854775808L, vx);
          Assert.assertEquals(0L, vy);
          Assert.assertEquals(9223372036854775807L, vz);

          c.setElementIndex(0);
          v.get3SL(o);
          Assert.assertEquals(-9223372036854775808L, o.getXL());
          Assert.assertEquals(0L, o.getYL());
          Assert.assertEquals(9223372036854775807L, o.getZL());
        }

        {
          final long vx = rb.getLong(24);
          final long vy = rb.getLong(32);
          final long vz = rb.getLong(40);
          Assert.assertEquals(-9223372036854775808L, vx);
          Assert.assertEquals(0L, vy);
          Assert.assertEquals(9223372036854775807L, vz);

          c.setElementIndex(1);
          v.get3SL(o);
          Assert.assertEquals(-9223372036854775808L, o.getXL());
          Assert.assertEquals(0L, o.getYL());
          Assert.assertEquals(9223372036854775807L, o.getZL());
        }

        {
          final long vx = rb.getLong(48);
          final long vy = rb.getLong(56);
          final long vz = rb.getLong(64);
          Assert.assertEquals(-9223372036854775808L, vx);
          Assert.assertEquals(0L, vy);
          Assert.assertEquals(9223372036854775807L, vz);

          c.setElementIndex(2);
          v.get3SL(o);
          Assert.assertEquals(-9223372036854775808L, o.getXL());
          Assert.assertEquals(0L, o.getYL());
          Assert.assertEquals(9223372036854775807L, o.getZL());
        }
        break;
      }

      case 4: {
        final JPRACursor1DType<SMFByteBufferIntegerSigned4Type> c =
          SMFByteBufferCursors.createSigned4Raw(rb, 64, 0, 4 * 8);
        final SMFByteBufferIntegerSigned4Type v = c.getElementView();
        final VectorM4L o = new VectorM4L();

        {
          final long vx = rb.getLong(0);
          final long vy = rb.getLong(8);
          final long vz = rb.getLong(16);
          final long vw = rb.getLong(24);
          Assert.assertEquals(-9223372036854775808L, vx);
          Assert.assertEquals(0L, vy);
          Assert.assertEquals(9223372036854775807L, vz);
          Assert.assertEquals(1L, vw);

          c.setElementIndex(0);
          v.get4SL(o);
          Assert.assertEquals(-9223372036854775808L, o.getXL());
          Assert.assertEquals(0L, o.getYL());
          Assert.assertEquals(9223372036854775807L, o.getZL());
          Assert.assertEquals(1L, o.getWL());
        }

        {
          final long vx = rb.getLong(32);
          final long vy = rb.getLong(40);
          final long vz = rb.getLong(48);
          final long vw = rb.getLong(56);
          Assert.assertEquals(-9223372036854775808L, vx);
          Assert.assertEquals(0L, vy);
          Assert.assertEquals(9223372036854775807L, vz);
          Assert.assertEquals(1L, vw);

          c.setElementIndex(1);
          v.get4SL(o);
          Assert.assertEquals(-9223372036854775808L, o.getXL());
          Assert.assertEquals(0L, o.getYL());
          Assert.assertEquals(9223372036854775807L, o.getZL());
          Assert.assertEquals(1L, o.getWL());
        }

        {
          final long vx = rb.getLong(64);
          final long vy = rb.getLong(72);
          final long vz = rb.getLong(80);
          final long vw = rb.getLong(88);
          Assert.assertEquals(-9223372036854775808L, vx);
          Assert.assertEquals(0L, vy);
          Assert.assertEquals(9223372036854775807L, vz);
          Assert.assertEquals(1L, vw);

          c.setElementIndex(2);
          v.get4SL(o);
          Assert.assertEquals(-9223372036854775808L, o.getXL());
          Assert.assertEquals(0L, o.getYL());
          Assert.assertEquals(9223372036854775807L, o.getZL());
          Assert.assertEquals(1L, o.getWL());
        }
        break;
      }
      default: {
        throw new UnreachableCodeException();
      }
    }
  }

  private static void checkTypeFloat16(
    final int component_count,
    final ByteBuffer rb)
  {
    switch (component_count) {

      case 1: {
        final JPRACursor1DType<SMFByteBufferFloat1Type> c =
          SMFByteBufferCursors.createFloat1Raw(rb, 16, 0, 1 * 2);
        final SMFByteBufferFloat1Type v = c.getElementView();

        Assert.assertEquals(
          -1000.0, Binary16.unpackDouble(rb.getChar(0)), 0.0);
        c.setElementIndex(0);
        Assert.assertEquals(
          -1000.0, v.get1D(), 0.0);

        Assert.assertEquals(
          0.0, Binary16.unpackDouble(rb.getChar(2)), 0.0);
        c.setElementIndex(1);
        Assert.assertEquals(
          0.0, v.get1D(), 0.0);

        Assert.assertEquals(
          1000.0, Binary16.unpackDouble(rb.getChar(4)), 0.0);
        c.setElementIndex(2);
        Assert.assertEquals(
          1000.0, v.get1D(), 0.0);
        break;
      }

      case 2: {
        final JPRACursor1DType<SMFByteBufferFloat2Type> c =
          SMFByteBufferCursors.createFloat2Raw(rb, 16, 0, 2 * 2);
        final SMFByteBufferFloat2Type v = c.getElementView();
        final VectorM2D o = new VectorM2D();

        {
          final double vx = Binary16.unpackDouble(rb.getChar(0));
          final double vy = Binary16.unpackDouble(rb.getChar(2));
          Assert.assertEquals(vx, -1000.0, 0.0);
          Assert.assertEquals(vy, 1000.0, 0.0);

          c.setElementIndex(0);
          v.get2D(o);
          Assert.assertEquals(o.getXD(), -1000.0, 0.0);
          Assert.assertEquals(o.getYD(), 1000.0, 0.0);
        }

        {
          final double vx = Binary16.unpackDouble(rb.getChar(4));
          final double vy = Binary16.unpackDouble(rb.getChar(6));
          Assert.assertEquals(vx, -1000.0, 0.0);
          Assert.assertEquals(vy, 1000.0, 0.0);

          c.setElementIndex(1);
          v.get2D(o);
          Assert.assertEquals(o.getXD(), -1000.0, 0.0);
          Assert.assertEquals(o.getYD(), 1000.0, 0.0);
        }

        {
          final double vx = Binary16.unpackDouble(rb.getChar(8));
          final double vy = Binary16.unpackDouble(rb.getChar(10));
          Assert.assertEquals(vx, -1000.0, 0.0);
          Assert.assertEquals(vy, 1000.0, 0.0);

          c.setElementIndex(2);
          v.get2D(o);
          Assert.assertEquals(o.getXD(), -1000.0, 0.0);
          Assert.assertEquals(o.getYD(), 1000.0, 0.0);
        }
        break;
      }

      case 3: {
        final JPRACursor1DType<SMFByteBufferFloat3Type> c =
          SMFByteBufferCursors.createFloat3Raw(rb, 16, 0, 3 * 2);
        final SMFByteBufferFloat3Type v = c.getElementView();
        final VectorM3D o = new VectorM3D();

        {
          final double vx = Binary16.unpackDouble(rb.getChar(0));
          final double vy = Binary16.unpackDouble(rb.getChar(2));
          final double vz = Binary16.unpackDouble(rb.getChar(4));
          Assert.assertEquals(vx, -1000.0, 0.0);
          Assert.assertEquals(vy, 0.0, 0.0);
          Assert.assertEquals(vz, 1000.0, 0.0);

          c.setElementIndex(0);
          v.get3D(o);
          Assert.assertEquals(o.getXD(), -1000.0, 0.0);
          Assert.assertEquals(o.getYD(), 0.0, 0.0);
          Assert.assertEquals(o.getZD(), 1000.0, 0.0);
        }

        {
          final double vx = Binary16.unpackDouble(rb.getChar(6));
          final double vy = Binary16.unpackDouble(rb.getChar(8));
          final double vz = Binary16.unpackDouble(rb.getChar(10));
          Assert.assertEquals(vx, -1000.0, 0.0);
          Assert.assertEquals(vy, 0.0, 0.0);
          Assert.assertEquals(vz, 1000.0, 0.0);

          c.setElementIndex(1);
          v.get3D(o);
          Assert.assertEquals(o.getXD(), -1000.0, 0.0);
          Assert.assertEquals(o.getYD(), 0.0, 0.0);
          Assert.assertEquals(o.getZD(), 1000.0, 0.0);
        }

        {
          final double vx = Binary16.unpackDouble(rb.getChar(12));
          final double vy = Binary16.unpackDouble(rb.getChar(14));
          final double vz = Binary16.unpackDouble(rb.getChar(16));
          Assert.assertEquals(vx, -1000.0, 0.0);
          Assert.assertEquals(vy, 0.0, 0.0);
          Assert.assertEquals(vz, 1000.0, 0.0);

          c.setElementIndex(2);
          v.get3D(o);
          Assert.assertEquals(o.getXD(), -1000.0, 0.0);
          Assert.assertEquals(o.getYD(), 0.0, 0.0);
          Assert.assertEquals(o.getZD(), 1000.0, 0.0);
        }
        break;
      }

      case 4: {
        final JPRACursor1DType<SMFByteBufferFloat4Type> c =
          SMFByteBufferCursors.createFloat4Raw(rb, 16, 0, 4 * 2);
        final SMFByteBufferFloat4Type v = c.getElementView();
        final VectorM4D o = new VectorM4D();

        {
          final double vx = Binary16.unpackDouble(rb.getChar(0));
          final double vy = Binary16.unpackDouble(rb.getChar(2));
          final double vz = Binary16.unpackDouble(rb.getChar(4));
          final double vw = Binary16.unpackDouble(rb.getChar(6));
          Assert.assertEquals(vx, -1000.0, 0.0);
          Assert.assertEquals(vy, 0.0, 0.0);
          Assert.assertEquals(vz, 1.0, 0.0);
          Assert.assertEquals(vw, 1000.0, 0.0);

          c.setElementIndex(0);
          v.get4D(o);
          Assert.assertEquals(o.getXD(), -1000.0, 0.0);
          Assert.assertEquals(o.getYD(), 0.0, 0.0);
          Assert.assertEquals(o.getZD(), 1.0, 0.0);
          Assert.assertEquals(o.getWD(), 1000.0, 0.0);
        }

        {
          final double vx = Binary16.unpackDouble(rb.getChar(8));
          final double vy = Binary16.unpackDouble(rb.getChar(10));
          final double vz = Binary16.unpackDouble(rb.getChar(12));
          final double vw = Binary16.unpackDouble(rb.getChar(14));
          Assert.assertEquals(vx, -1000.0, 0.0);
          Assert.assertEquals(vy, 0.0, 0.0);
          Assert.assertEquals(vz, 1.0, 0.0);
          Assert.assertEquals(vw, 1000.0, 0.0);

          c.setElementIndex(1);
          v.get4D(o);
          Assert.assertEquals(o.getXD(), -1000.0, 0.0);
          Assert.assertEquals(o.getYD(), 0.0, 0.0);
          Assert.assertEquals(o.getZD(), 1.0, 0.0);
          Assert.assertEquals(o.getWD(), 1000.0, 0.0);
        }

        {
          final double vx = Binary16.unpackDouble(rb.getChar(16));
          final double vy = Binary16.unpackDouble(rb.getChar(18));
          final double vz = Binary16.unpackDouble(rb.getChar(20));
          final double vw = Binary16.unpackDouble(rb.getChar(22));
          Assert.assertEquals(vx, -1000.0, 0.0);
          Assert.assertEquals(vy, 0.0, 0.0);
          Assert.assertEquals(vz, 1.0, 0.0);
          Assert.assertEquals(vw, 1000.0, 0.0);

          c.setElementIndex(2);
          v.get4D(o);
          Assert.assertEquals(o.getXD(), -1000.0, 0.0);
          Assert.assertEquals(o.getYD(), 0.0, 0.0);
          Assert.assertEquals(o.getZD(), 1.0, 0.0);
          Assert.assertEquals(o.getWD(), 1000.0, 0.0);
        }
        break;
      }

      default: {
        throw new UnreachableCodeException();
      }
    }
  }

  private static void checkTypeFloat32(
    final int component_count,
    final ByteBuffer rb)
  {
    switch (component_count) {

      case 1: {
        final JPRACursor1DType<SMFByteBufferFloat1Type> c =
          SMFByteBufferCursors.createFloat1Raw(rb, 32, 0, 1 * 4);
        final SMFByteBufferFloat1Type v = c.getElementView();

        Assert.assertEquals(-1000.0f, rb.getFloat(0), 0.0f);
        c.setElementIndex(0);
        Assert.assertEquals(-1000.0f, v.get1D(), 0.0f);

        Assert.assertEquals(0.0f, rb.getFloat(4), 0.0f);
        c.setElementIndex(1);
        Assert.assertEquals(0.0f, v.get1D(), 0.0f);

        Assert.assertEquals(1000.0f, rb.getFloat(8), 0.0f);
        c.setElementIndex(2);
        Assert.assertEquals(1000.0f, v.get1D(), 0.0f);
        break;
      }

      case 2: {
        final JPRACursor1DType<SMFByteBufferFloat2Type> c =
          SMFByteBufferCursors.createFloat2Raw(rb, 32, 0, 2 * 4);
        final SMFByteBufferFloat2Type v = c.getElementView();
        final VectorM2D o = new VectorM2D();

        {
          final double vx = (double) rb.getFloat(0);
          final double vy = (double) rb.getFloat(4);
          Assert.assertEquals(vx, -1000.0, 0.0);
          Assert.assertEquals(vy, 1000.0, 0.0);

          c.setElementIndex(0);
          v.get2D(o);
          Assert.assertEquals(o.getXD(), -1000.0, 0.0);
          Assert.assertEquals(o.getYD(), 1000.0, 0.0);
        }

        {
          final double vx = (double) rb.getFloat(8);
          final double vy = (double) rb.getFloat(12);
          Assert.assertEquals(vx, -1000.0, 0.0);
          Assert.assertEquals(vy, 1000.0, 0.0);

          c.setElementIndex(1);
          v.get2D(o);
          Assert.assertEquals(o.getXD(), -1000.0, 0.0);
          Assert.assertEquals(o.getYD(), 1000.0, 0.0);
        }

        {
          final double vx = (double) rb.getFloat(16);
          final double vy = (double) rb.getFloat(20);
          Assert.assertEquals(vx, -1000.0, 0.0);
          Assert.assertEquals(vy, 1000.0, 0.0);

          c.setElementIndex(2);
          v.get2D(o);
          Assert.assertEquals(o.getXD(), -1000.0, 0.0);
          Assert.assertEquals(o.getYD(), 1000.0, 0.0);
        }
        break;
      }

      case 3: {
        final JPRACursor1DType<SMFByteBufferFloat3Type> c =
          SMFByteBufferCursors.createFloat3Raw(rb, 32, 0, 3 * 4);
        final SMFByteBufferFloat3Type v = c.getElementView();
        final VectorM3D o = new VectorM3D();

        {
          final double vx = (double) rb.getFloat(0);
          final double vy = (double) rb.getFloat(4);
          final double vz = (double) rb.getFloat(8);
          Assert.assertEquals(vx, -1000.0, 0.0);
          Assert.assertEquals(vy, 0.0, 0.0);
          Assert.assertEquals(vz, 1000.0, 0.0);

          c.setElementIndex(0);
          v.get3D(o);
          Assert.assertEquals(o.getXD(), -1000.0, 0.0);
          Assert.assertEquals(o.getYD(), 0.0, 0.0);
          Assert.assertEquals(o.getZD(), 1000.0, 0.0);
        }

        {
          final double vx = (double) rb.getFloat(12);
          final double vy = (double) rb.getFloat(16);
          final double vz = (double) rb.getFloat(20);
          Assert.assertEquals(vx, -1000.0, 0.0);
          Assert.assertEquals(vy, 0.0, 0.0);
          Assert.assertEquals(vz, 1000.0, 0.0);

          c.setElementIndex(1);
          v.get3D(o);
          Assert.assertEquals(o.getXD(), -1000.0, 0.0);
          Assert.assertEquals(o.getYD(), 0.0, 0.0);
          Assert.assertEquals(o.getZD(), 1000.0, 0.0);
        }

        {
          final double vx = (double) rb.getFloat(24);
          final double vy = (double) rb.getFloat(28);
          final double vz = (double) rb.getFloat(32);
          Assert.assertEquals(vx, -1000.0, 0.0);
          Assert.assertEquals(vy, 0.0, 0.0);
          Assert.assertEquals(vz, 1000.0, 0.0);

          c.setElementIndex(2);
          v.get3D(o);
          Assert.assertEquals(o.getXD(), -1000.0, 0.0);
          Assert.assertEquals(o.getYD(), 0.0, 0.0);
          Assert.assertEquals(o.getZD(), 1000.0, 0.0);
        }
        break;
      }

      case 4: {
        final JPRACursor1DType<SMFByteBufferFloat4Type> c =
          SMFByteBufferCursors.createFloat4Raw(rb, 32, 0, 4 * 4);
        final SMFByteBufferFloat4Type v = c.getElementView();
        final VectorM4D o = new VectorM4D();

        {
          final double vx = (double) rb.getFloat(0);
          final double vy = (double) rb.getFloat(4);
          final double vz = (double) rb.getFloat(8);
          final double vw = (double) rb.getFloat(12);
          Assert.assertEquals(vx, -1000.0, 0.0);
          Assert.assertEquals(vy, 0.0, 0.0);
          Assert.assertEquals(vz, 1.0, 0.0);
          Assert.assertEquals(vw, 1000.0, 0.0);

          c.setElementIndex(0);
          v.get4D(o);
          Assert.assertEquals(o.getXD(), -1000.0, 0.0);
          Assert.assertEquals(o.getYD(), 0.0, 0.0);
          Assert.assertEquals(o.getZD(), 1.0, 0.0);
          Assert.assertEquals(o.getWD(), 1000.0, 0.0);
        }

        {
          final double vx = (double) rb.getFloat(16);
          final double vy = (double) rb.getFloat(20);
          final double vz = (double) rb.getFloat(24);
          final double vw = (double) rb.getFloat(28);
          Assert.assertEquals(vx, -1000.0, 0.0);
          Assert.assertEquals(vy, 0.0, 0.0);
          Assert.assertEquals(vz, 1.0, 0.0);
          Assert.assertEquals(vw, 1000.0, 0.0);

          c.setElementIndex(1);
          v.get4D(o);
          Assert.assertEquals(o.getXD(), -1000.0, 0.0);
          Assert.assertEquals(o.getYD(), 0.0, 0.0);
          Assert.assertEquals(o.getZD(), 1.0, 0.0);
          Assert.assertEquals(o.getWD(), 1000.0, 0.0);
        }

        {
          final double vx = (double) rb.getFloat(32);
          final double vy = (double) rb.getFloat(36);
          final double vz = (double) rb.getFloat(40);
          final double vw = (double) rb.getFloat(44);
          Assert.assertEquals(vx, -1000.0, 0.0);
          Assert.assertEquals(vy, 0.0, 0.0);
          Assert.assertEquals(vz, 1.0, 0.0);
          Assert.assertEquals(vw, 1000.0, 0.0);

          c.setElementIndex(2);
          v.get4D(o);
          Assert.assertEquals(o.getXD(), -1000.0, 0.0);
          Assert.assertEquals(o.getYD(), 0.0, 0.0);
          Assert.assertEquals(o.getZD(), 1.0, 0.0);
          Assert.assertEquals(o.getWD(), 1000.0, 0.0);
        }
        break;
      }

      default: {
        throw new UnreachableCodeException();
      }
    }
  }

  private static void checkTypeFloat64(
    final int component_count,
    final ByteBuffer rb)
  {
    switch (component_count) {

      case 1: {
        final JPRACursor1DType<SMFByteBufferFloat1Type> c =
          SMFByteBufferCursors.createFloat1Raw(rb, 64, 0, 1 * 8);
        final SMFByteBufferFloat1Type v = c.getElementView();

        Assert.assertEquals(-1000.0, rb.getDouble(0), 0.0);
        c.setElementIndex(0);
        Assert.assertEquals(-1000.0, v.get1D(), 0.0);

        Assert.assertEquals(0.0, rb.getDouble(8), 0.0);
        c.setElementIndex(1);
        Assert.assertEquals(0.0, v.get1D(), 0.0);

        Assert.assertEquals(1000.0, rb.getDouble(16), 0.0);
        c.setElementIndex(2);
        Assert.assertEquals(1000.0, v.get1D(), 0.0);
        break;
      }

      case 2: {

        final JPRACursor1DType<SMFByteBufferFloat2Type> c =
          SMFByteBufferCursors.createFloat2Raw(rb, 64, 0, 2 * 8);
        final SMFByteBufferFloat2Type v = c.getElementView();
        final VectorM2D o = new VectorM2D();

        {
          final double vx = rb.getDouble(0);
          final double vy = rb.getDouble(8);
          Assert.assertEquals(vx, -1000.0, 0.0);
          Assert.assertEquals(vy, 1000.0, 0.0);

          c.setElementIndex(0);
          v.get2D(o);
          Assert.assertEquals(o.getXD(), -1000.0, 0.0);
          Assert.assertEquals(o.getYD(), 1000.0, 0.0);
        }

        {
          final double vx = rb.getDouble(16);
          final double vy = rb.getDouble(24);
          Assert.assertEquals(vx, -1000.0, 0.0);
          Assert.assertEquals(vy, 1000.0, 0.0);

          c.setElementIndex(1);
          v.get2D(o);
          Assert.assertEquals(o.getXD(), -1000.0, 0.0);
          Assert.assertEquals(o.getYD(), 1000.0, 0.0);
        }

        {
          final double vx = rb.getDouble(32);
          final double vy = rb.getDouble(40);
          Assert.assertEquals(vx, -1000.0, 0.0);
          Assert.assertEquals(vy, 1000.0, 0.0);

          c.setElementIndex(2);
          v.get2D(o);
          Assert.assertEquals(o.getXD(), -1000.0, 0.0);
          Assert.assertEquals(o.getYD(), 1000.0, 0.0);
        }
        break;
      }

      case 3: {

        final JPRACursor1DType<SMFByteBufferFloat3Type> c =
          SMFByteBufferCursors.createFloat3Raw(rb, 64, 0, 3 * 8);
        final SMFByteBufferFloat3Type v = c.getElementView();
        final VectorM3D o = new VectorM3D();

        {
          final double vx = rb.getDouble(0);
          final double vy = rb.getDouble(8);
          final double vz = rb.getDouble(16);
          Assert.assertEquals(vx, -1000.0, 0.0);
          Assert.assertEquals(vy, 0.0, 0.0);
          Assert.assertEquals(vz, 1000.0, 0.0);

          c.setElementIndex(0);
          v.get3D(o);
          Assert.assertEquals(o.getXD(), -1000.0, 0.0);
          Assert.assertEquals(o.getYD(), 0.0, 0.0);
          Assert.assertEquals(o.getZD(), 1000.0, 0.0);
        }

        {
          final double vx = rb.getDouble(24);
          final double vy = rb.getDouble(32);
          final double vz = rb.getDouble(40);
          Assert.assertEquals(vx, -1000.0, 0.0);
          Assert.assertEquals(vy, 0.0, 0.0);
          Assert.assertEquals(vz, 1000.0, 0.0);

          c.setElementIndex(1);
          v.get3D(o);
          Assert.assertEquals(o.getXD(), -1000.0, 0.0);
          Assert.assertEquals(o.getYD(), 0.0, 0.0);
          Assert.assertEquals(o.getZD(), 1000.0, 0.0);
        }

        {
          final double vx = rb.getDouble(48);
          final double vy = rb.getDouble(56);
          final double vz = rb.getDouble(64);
          Assert.assertEquals(vx, -1000.0, 0.0);
          Assert.assertEquals(vy, 0.0, 0.0);
          Assert.assertEquals(vz, 1000.0, 0.0);

          c.setElementIndex(2);
          v.get3D(o);
          Assert.assertEquals(o.getXD(), -1000.0, 0.0);
          Assert.assertEquals(o.getYD(), 0.0, 0.0);
          Assert.assertEquals(o.getZD(), 1000.0, 0.0);
        }
        break;
      }

      case 4: {

        final JPRACursor1DType<SMFByteBufferFloat4Type> c =
          SMFByteBufferCursors.createFloat4Raw(rb, 64, 0, 4 * 8);
        final SMFByteBufferFloat4Type v = c.getElementView();
        final VectorM4D o = new VectorM4D();

        {
          final double vx = rb.getDouble(0);
          final double vy = rb.getDouble(8);
          final double vz = rb.getDouble(16);
          final double vw = rb.getDouble(24);
          Assert.assertEquals(vx, -1000.0, 0.0);
          Assert.assertEquals(vy, 0.0, 0.0);
          Assert.assertEquals(vz, 1.0, 0.0);
          Assert.assertEquals(vw, 1000.0, 0.0);

          c.setElementIndex(0);
          v.get4D(o);
          Assert.assertEquals(o.getXD(), -1000.0, 0.0);
          Assert.assertEquals(o.getYD(), 0.0, 0.0);
          Assert.assertEquals(o.getZD(), 1.0, 0.0);
          Assert.assertEquals(o.getWD(), 1000.0, 0.0);
        }

        {
          final double vx = rb.getDouble(32);
          final double vy = rb.getDouble(40);
          final double vz = rb.getDouble(48);
          final double vw = rb.getDouble(56);
          Assert.assertEquals(vx, -1000.0, 0.0);
          Assert.assertEquals(vy, 0.0, 0.0);
          Assert.assertEquals(vz, 1.0, 0.0);
          Assert.assertEquals(vw, 1000.0, 0.0);

          c.setElementIndex(1);
          v.get4D(o);
          Assert.assertEquals(o.getXD(), -1000.0, 0.0);
          Assert.assertEquals(o.getYD(), 0.0, 0.0);
          Assert.assertEquals(o.getZD(), 1.0, 0.0);
          Assert.assertEquals(o.getWD(), 1000.0, 0.0);
        }

        {
          final double vx = rb.getDouble(64);
          final double vy = rb.getDouble(72);
          final double vz = rb.getDouble(80);
          final double vw = rb.getDouble(88);
          Assert.assertEquals(vx, -1000.0, 0.0);
          Assert.assertEquals(vy, 0.0, 0.0);
          Assert.assertEquals(vz, 1.0, 0.0);
          Assert.assertEquals(vw, 1000.0, 0.0);

          c.setElementIndex(2);
          v.get4D(o);
          Assert.assertEquals(o.getXD(), -1000.0, 0.0);
          Assert.assertEquals(o.getYD(), 0.0, 0.0);
          Assert.assertEquals(o.getZD(), 1.0, 0.0);
          Assert.assertEquals(o.getWD(), 1000.0, 0.0);
        }
        break;
      }

      default: {
        throw new UnreachableCodeException();
      }
    }
  }

  private static void checkType(
    final SMFComponentType type,
    final int component_count,
    final int component_size,
    final ByteBuffer rb)
  {
    switch (type) {
      case ELEMENT_TYPE_INTEGER_SIGNED: {
        switch (component_size) {
          case 8: {
            checkTypeSigned8(component_count, rb);
            break;
          }
          case 16: {
            checkTypeSigned16(component_count, rb);
            break;
          }
          case 32: {
            checkTypeSigned32(component_count, rb);
            break;
          }
          case 64: {
            checkTypeSigned64(component_count, rb);
            break;
          }
          default: {
            throw new UnreachableCodeException();
          }
        }
        break;
      }
      case ELEMENT_TYPE_INTEGER_UNSIGNED: {
        switch (component_size) {
          case 8: {
            checkTypeUnsigned8(component_count, rb);
            break;
          }
          case 16: {
            checkTypeUnsigned16(component_count, rb);
            break;
          }
          case 32: {
            checkTypeUnsigned32(component_count, rb);
            break;
          }
          case 64: {
            checkTypeUnsigned64(component_count, rb);
            break;
          }
          default: {
            throw new UnreachableCodeException();
          }
        }
        break;
      }
      case ELEMENT_TYPE_FLOATING: {
        switch (component_size) {
          case 16: {
            checkTypeFloat16(component_count, rb);
            break;
          }
          case 32: {
            checkTypeFloat32(component_count, rb);
            break;
          }
          case 64: {
            checkTypeFloat64(component_count, rb);
            break;
          }
          default: {
            throw new UnreachableCodeException();
          }
        }
        break;
      }
    }
  }

  private void check(
    final SMFComponentType type,
    final int component_count,
    final int component_size,
    final int vertex_count,
    final String name)
    throws IOException
  {
    final SMFByteBufferPackedMesh mesh = this.parseMesh(name);

    Assert.assertEquals(1L, mesh.attributeSets().size());

    final ByteBuffer ab = mesh.attributeSets().iterator().next().byteBuffer();
    checkType(type, component_count, component_size, ab);

    final SMFByteBufferPackedTriangles t = mesh.triangles().get();
    final ByteBuffer tb = t.byteBuffer();
    Assert.assertEquals(0L, (long) tb.get(0));
    Assert.assertEquals(1L, (long) tb.get(1));
    Assert.assertEquals(2L, (long) tb.get(2));
  }

  private SMFByteBufferPackedMesh parseMesh(
    final String name)
    throws IOException
  {
    final SMFAttributeName attr_name = SMFAttributeName.of("x");

    final SMFByteBufferPackedMeshLoaderType loader =
      SMFByteBufferPackedMeshes.newLoader(
        SMFParserEventsMeta.ignore(),
        new SMFByteBufferPackerEventsType()
        {
          @Override
          public Validation<List<SMFErrorType>, SortedMap<Integer, SMFByteBufferPackingConfiguration>> onHeader(
            final SMFHeader header)
          {
            final List<SMFAttribute> ordered =
              header.attributesInOrder();
            final List<SMFAttribute> filtered =
              ordered.filter(a -> Objects.equals(a.name(), attr_name));
            final SMFByteBufferPackingConfiguration config =
              SMFByteBufferPackingConfiguration.of(filtered);
            return valid(TreeMap.of(Tuple.of(Integer.valueOf(0), config)));
          }

          @Override
          public boolean onShouldPackTriangles()
          {
            return true;
          }

          @Override
          public ByteBuffer onAllocateTriangleBuffer(
            final long size)
          {
            return ByteBuffer
              .allocate(Math.toIntExact(size))
              .order(ByteOrder.nativeOrder());
          }

          @Override
          public ByteBuffer onAllocateAttributeBuffer(
            final Integer id,
            final SMFByteBufferPackingConfiguration config,
            final long size)
          {
            return ByteBuffer
              .allocate(Math.toIntExact(size))
              .order(ByteOrder.nativeOrder());
          }
        });

    try (final SMFParserSequentialType parser = createParser(loader, name)) {
      // Nothing
    }

    Assert.assertTrue(loader.errors().isEmpty());
    return loader.mesh();
  }

  @Test
  public void testTriangle8()
    throws Exception
  {
    final SMFByteBufferPackedMesh mesh = this.parseMesh("triangle8.smft");
    final SMFByteBufferPackedTriangles t = mesh.triangles().get();
    final ByteBuffer b = t.byteBuffer();
    Assert.assertEquals(0L, (long) b.get(0));
    Assert.assertEquals(1L, (long) b.get(1));
    Assert.assertEquals(2L, (long) b.get(2));

    final JPRACursor1DType<SMFByteBufferIntegerUnsigned3Type> c = t.cursor();
    final VectorM3L out = new VectorM3L();
    c.getElementView().get3UL(out);
    Assert.assertEquals(0L, out.getXL());
    Assert.assertEquals(1L, out.getYL());
    Assert.assertEquals(2L, out.getZL());
  }

  @Test
  public void testTriangle16()
    throws Exception
  {
    final SMFByteBufferPackedMesh mesh = this.parseMesh("triangle16.smft");
    final SMFByteBufferPackedTriangles t = mesh.triangles().get();
    final ByteBuffer b = t.byteBuffer();
    Assert.assertEquals(0L, (long) b.getChar(0));
    Assert.assertEquals(1L, (long) b.getChar(2));
    Assert.assertEquals(2L, (long) b.getChar(4));

    final JPRACursor1DType<SMFByteBufferIntegerUnsigned3Type> c = t.cursor();
    final VectorM3L out = new VectorM3L();
    c.getElementView().get3UL(out);
    Assert.assertEquals(0L, out.getXL());
    Assert.assertEquals(1L, out.getYL());
    Assert.assertEquals(2L, out.getZL());
  }

  @Test
  public void testTriangle32()
    throws Exception
  {
    final SMFByteBufferPackedMesh mesh = this.parseMesh("triangle32.smft");
    final SMFByteBufferPackedTriangles t = mesh.triangles().get();
    final ByteBuffer b = t.byteBuffer();
    Assert.assertEquals(0L, (long) b.getInt(0));
    Assert.assertEquals(1L, (long) b.getInt(4));
    Assert.assertEquals(2L, (long) b.getInt(8));

    final JPRACursor1DType<SMFByteBufferIntegerUnsigned3Type> c = t.cursor();
    final VectorM3L out = new VectorM3L();
    c.getElementView().get3UL(out);
    Assert.assertEquals(0L, out.getXL());
    Assert.assertEquals(1L, out.getYL());
    Assert.assertEquals(2L, out.getZL());
  }

  @Test
  public void testTriangle64()
    throws Exception
  {
    final SMFByteBufferPackedMesh mesh = this.parseMesh("triangle64.smft");
    final SMFByteBufferPackedTriangles t = mesh.triangles().get();
    final ByteBuffer b = t.byteBuffer();
    Assert.assertEquals(0L, b.getLong(0));
    Assert.assertEquals(1L, b.getLong(8));
    Assert.assertEquals(2L, b.getLong(16));

    final JPRACursor1DType<SMFByteBufferIntegerUnsigned3Type> c = t.cursor();
    final VectorM3L out = new VectorM3L();
    c.getElementView().get3UL(out);
    Assert.assertEquals(0L, out.getXL());
    Assert.assertEquals(1L, out.getYL());
    Assert.assertEquals(2L, out.getZL());
  }

  @Test
  public void testLoadIntegerSigned8_1()
    throws Exception
  {
    final SMFComponentType type = SMFComponentType.ELEMENT_TYPE_INTEGER_SIGNED;
    final int component_count = 1;
    final int component_size = 8;
    final int vertex_count = 3;
    final String name = "integer8_1.smft";

    this.check(type, component_count, component_size, vertex_count, name);
  }

  @Test
  public void testLoadIntegerSigned8_2()
    throws Exception
  {
    final SMFComponentType type = SMFComponentType.ELEMENT_TYPE_INTEGER_SIGNED;
    final int component_count = 2;
    final int component_size = 8;
    final int vertex_count = 3;
    final String name = "integer8_2.smft";

    this.check(type, component_count, component_size, vertex_count, name);
  }

  @Test
  public void testLoadIntegerSigned8_3()
    throws Exception
  {
    final SMFComponentType type = SMFComponentType.ELEMENT_TYPE_INTEGER_SIGNED;
    final int component_count = 3;
    final int component_size = 8;
    final int vertex_count = 3;
    final String name = "integer8_3.smft";

    this.check(type, component_count, component_size, vertex_count, name);
  }

  @Test
  public void testLoadIntegerSigned8_4()
    throws Exception
  {
    final SMFComponentType type = SMFComponentType.ELEMENT_TYPE_INTEGER_SIGNED;
    final int component_count = 4;
    final int component_size = 8;
    final int vertex_count = 3;
    final String name = "integer8_4.smft";

    this.check(type, component_count, component_size, vertex_count, name);
  }

  @Test
  public void testLoadIntegerSigned16_1()
    throws Exception
  {
    final SMFComponentType type = SMFComponentType.ELEMENT_TYPE_INTEGER_SIGNED;
    final int component_count = 1;
    final int component_size = 16;
    final int vertex_count = 3;
    final String name = "integer16_1.smft";

    this.check(type, component_count, component_size, vertex_count, name);
  }

  @Test
  public void testLoadIntegerSigned16_2()
    throws Exception
  {
    final SMFComponentType type = SMFComponentType.ELEMENT_TYPE_INTEGER_SIGNED;
    final int component_count = 2;
    final int component_size = 16;
    final int vertex_count = 3;
    final String name = "integer16_2.smft";

    this.check(type, component_count, component_size, vertex_count, name);
  }

  @Test
  public void testLoadIntegerSigned16_3()
    throws Exception
  {
    final SMFComponentType type = SMFComponentType.ELEMENT_TYPE_INTEGER_SIGNED;
    final int component_count = 3;
    final int component_size = 16;
    final int vertex_count = 3;
    final String name = "integer16_3.smft";

    this.check(type, component_count, component_size, vertex_count, name);
  }

  @Test
  public void testLoadIntegerSigned16_4()
    throws Exception
  {
    final SMFComponentType type = SMFComponentType.ELEMENT_TYPE_INTEGER_SIGNED;
    final int component_count = 4;
    final int component_size = 16;
    final int vertex_count = 3;
    final String name = "integer16_4.smft";

    this.check(type, component_count, component_size, vertex_count, name);
  }

  @Test
  public void testLoadIntegerSigned32_1()
    throws Exception
  {
    final SMFComponentType type = SMFComponentType.ELEMENT_TYPE_INTEGER_SIGNED;
    final int component_count = 1;
    final int component_size = 32;
    final int vertex_count = 3;
    final String name = "integer32_1.smft";

    this.check(type, component_count, component_size, vertex_count, name);
  }

  @Test
  public void testLoadIntegerSigned32_2()
    throws Exception
  {
    final SMFComponentType type = SMFComponentType.ELEMENT_TYPE_INTEGER_SIGNED;
    final int component_count = 2;
    final int component_size = 32;
    final int vertex_count = 3;
    final String name = "integer32_2.smft";

    this.check(type, component_count, component_size, vertex_count, name);
  }

  @Test
  public void testLoadIntegerSigned32_3()
    throws Exception
  {
    final SMFComponentType type = SMFComponentType.ELEMENT_TYPE_INTEGER_SIGNED;
    final int component_count = 3;
    final int component_size = 32;
    final int vertex_count = 3;
    final String name = "integer32_3.smft";

    this.check(type, component_count, component_size, vertex_count, name);
  }

  @Test
  public void testLoadIntegerSigned32_4()
    throws Exception
  {
    final SMFComponentType type = SMFComponentType.ELEMENT_TYPE_INTEGER_SIGNED;
    final int component_count = 4;
    final int component_size = 32;
    final int vertex_count = 3;
    final String name = "integer32_4.smft";

    this.check(type, component_count, component_size, vertex_count, name);
  }

  @Test
  public void testLoadIntegerSigned64_1()
    throws Exception
  {
    final SMFComponentType type = SMFComponentType.ELEMENT_TYPE_INTEGER_SIGNED;
    final int component_count = 1;
    final int component_size = 64;
    final int vertex_count = 3;
    final String name = "integer64_1.smft";

    this.check(type, component_count, component_size, vertex_count, name);
  }

  @Test
  public void testLoadIntegerSigned64_2()
    throws Exception
  {
    final SMFComponentType type = SMFComponentType.ELEMENT_TYPE_INTEGER_SIGNED;
    final int component_count = 2;
    final int component_size = 64;
    final int vertex_count = 3;
    final String name = "integer64_2.smft";

    this.check(type, component_count, component_size, vertex_count, name);
  }

  @Test
  public void testLoadIntegerSigned64_3()
    throws Exception
  {
    final SMFComponentType type = SMFComponentType.ELEMENT_TYPE_INTEGER_SIGNED;
    final int component_count = 3;
    final int component_size = 64;
    final int vertex_count = 3;
    final String name = "integer64_3.smft";

    this.check(type, component_count, component_size, vertex_count, name);
  }

  @Test
  public void testLoadIntegerSigned64_4()
    throws Exception
  {
    final SMFComponentType type = SMFComponentType.ELEMENT_TYPE_INTEGER_SIGNED;
    final int component_count = 4;
    final int component_size = 64;
    final int vertex_count = 3;
    final String name = "integer64_4.smft";

    this.check(type, component_count, component_size, vertex_count, name);
  }

  @Test
  public void testLoadIntegerUnsigned8_1()
    throws Exception
  {
    final SMFComponentType type = SMFComponentType.ELEMENT_TYPE_INTEGER_UNSIGNED;
    final int component_count = 1;
    final int component_size = 8;
    final int vertex_count = 3;
    final String name = "unsigned8_1.smft";

    this.check(type, component_count, component_size, vertex_count, name);
  }

  @Test
  public void testLoadIntegerUnsigned8_2()
    throws Exception
  {
    final SMFComponentType type = SMFComponentType.ELEMENT_TYPE_INTEGER_UNSIGNED;
    final int component_count = 2;
    final int component_size = 8;
    final int vertex_count = 3;
    final String name = "unsigned8_2.smft";

    this.check(type, component_count, component_size, vertex_count, name);
  }

  @Test
  public void testLoadIntegerUnsigned8_3()
    throws Exception
  {
    final SMFComponentType type = SMFComponentType.ELEMENT_TYPE_INTEGER_UNSIGNED;
    final int component_count = 3;
    final int component_size = 8;
    final int vertex_count = 3;
    final String name = "unsigned8_3.smft";

    this.check(type, component_count, component_size, vertex_count, name);
  }

  @Test
  public void testLoadIntegerUnsigned8_4()
    throws Exception
  {
    final SMFComponentType type = SMFComponentType.ELEMENT_TYPE_INTEGER_UNSIGNED;
    final int component_count = 4;
    final int component_size = 8;
    final int vertex_count = 3;
    final String name = "unsigned8_4.smft";

    this.check(type, component_count, component_size, vertex_count, name);
  }

  @Test
  public void testLoadIntegerUnsigned16_1()
    throws Exception
  {
    final SMFComponentType type = SMFComponentType.ELEMENT_TYPE_INTEGER_UNSIGNED;
    final int component_count = 1;
    final int component_size = 16;
    final int vertex_count = 3;
    final String name = "unsigned16_1.smft";

    this.check(type, component_count, component_size, vertex_count, name);
  }

  @Test
  public void testLoadIntegerUnsigned16_2()
    throws Exception
  {
    final SMFComponentType type = SMFComponentType.ELEMENT_TYPE_INTEGER_UNSIGNED;
    final int component_count = 2;
    final int component_size = 16;
    final int vertex_count = 3;
    final String name = "unsigned16_2.smft";

    this.check(type, component_count, component_size, vertex_count, name);
  }

  @Test
  public void testLoadIntegerUnsigned16_3()
    throws Exception
  {
    final SMFComponentType type = SMFComponentType.ELEMENT_TYPE_INTEGER_UNSIGNED;
    final int component_count = 3;
    final int component_size = 16;
    final int vertex_count = 3;
    final String name = "unsigned16_3.smft";

    this.check(type, component_count, component_size, vertex_count, name);
  }

  @Test
  public void testLoadIntegerUnsigned16_4()
    throws Exception
  {
    final SMFComponentType type = SMFComponentType.ELEMENT_TYPE_INTEGER_UNSIGNED;
    final int component_count = 4;
    final int component_size = 16;
    final int vertex_count = 3;
    final String name = "unsigned16_4.smft";

    this.check(type, component_count, component_size, vertex_count, name);
  }

  @Test
  public void testLoadIntegerUnsigned32_1()
    throws Exception
  {
    final SMFComponentType type = SMFComponentType.ELEMENT_TYPE_INTEGER_UNSIGNED;
    final int component_count = 1;
    final int component_size = 32;
    final int vertex_count = 3;
    final String name = "unsigned32_1.smft";

    this.check(type, component_count, component_size, vertex_count, name);
  }

  @Test
  public void testLoadIntegerUnsigned32_2()
    throws Exception
  {
    final SMFComponentType type = SMFComponentType.ELEMENT_TYPE_INTEGER_UNSIGNED;
    final int component_count = 2;
    final int component_size = 32;
    final int vertex_count = 3;
    final String name = "unsigned32_2.smft";

    this.check(type, component_count, component_size, vertex_count, name);
  }

  @Test
  public void testLoadIntegerUnsigned32_3()
    throws Exception
  {
    final SMFComponentType type = SMFComponentType.ELEMENT_TYPE_INTEGER_UNSIGNED;
    final int component_count = 3;
    final int component_size = 32;
    final int vertex_count = 3;
    final String name = "unsigned32_3.smft";

    this.check(type, component_count, component_size, vertex_count, name);
  }

  @Test
  public void testLoadIntegerUnsigned32_4()
    throws Exception
  {
    final SMFComponentType type = SMFComponentType.ELEMENT_TYPE_INTEGER_UNSIGNED;
    final int component_count = 4;
    final int component_size = 32;
    final int vertex_count = 3;
    final String name = "unsigned32_4.smft";

    this.check(type, component_count, component_size, vertex_count, name);
  }

  @Test
  public void testLoadIntegerUnsigned64_1()
    throws Exception
  {
    final SMFComponentType type = SMFComponentType.ELEMENT_TYPE_INTEGER_UNSIGNED;
    final int component_count = 1;
    final int component_size = 64;
    final int vertex_count = 3;
    final String name = "unsigned64_1.smft";

    this.check(type, component_count, component_size, vertex_count, name);
  }

  @Test
  public void testLoadIntegerUnsigned64_2()
    throws Exception
  {
    final SMFComponentType type = SMFComponentType.ELEMENT_TYPE_INTEGER_UNSIGNED;
    final int component_count = 2;
    final int component_size = 64;
    final int vertex_count = 3;
    final String name = "unsigned64_2.smft";

    this.check(type, component_count, component_size, vertex_count, name);
  }

  @Test
  public void testLoadIntegerUnsigned64_3()
    throws Exception
  {
    final SMFComponentType type = SMFComponentType.ELEMENT_TYPE_INTEGER_UNSIGNED;
    final int component_count = 3;
    final int component_size = 64;
    final int vertex_count = 3;
    final String name = "unsigned64_3.smft";

    this.check(type, component_count, component_size, vertex_count, name);
  }

  @Test
  public void testLoadIntegerUnsigned64_4()
    throws Exception
  {
    final SMFComponentType type = SMFComponentType.ELEMENT_TYPE_INTEGER_UNSIGNED;
    final int component_count = 4;
    final int component_size = 64;
    final int vertex_count = 3;
    final String name = "unsigned64_4.smft";

    this.check(type, component_count, component_size, vertex_count, name);
  }

  @Test
  public void testLoadFloat64_1()
    throws Exception
  {
    final SMFComponentType type = SMFComponentType.ELEMENT_TYPE_FLOATING;
    final int component_count = 1;
    final int component_size = 64;
    final int vertex_count = 3;
    final String name = "float64_1.smft";

    this.check(type, component_count, component_size, vertex_count, name);
  }

  @Test
  public void testLoadFloat64_2()
    throws Exception
  {
    final SMFComponentType type = SMFComponentType.ELEMENT_TYPE_FLOATING;
    final int component_count = 2;
    final int component_size = 64;
    final int vertex_count = 3;
    final String name = "float64_2.smft";

    this.check(type, component_count, component_size, vertex_count, name);
  }

  @Test
  public void testLoadFloat64_3()
    throws Exception
  {
    final SMFComponentType type = SMFComponentType.ELEMENT_TYPE_FLOATING;
    final int component_count = 3;
    final int component_size = 64;
    final int vertex_count = 3;
    final String name = "float64_3.smft";

    this.check(type, component_count, component_size, vertex_count, name);
  }

  @Test
  public void testLoadFloat64_4()
    throws Exception
  {
    final SMFComponentType type = SMFComponentType.ELEMENT_TYPE_FLOATING;
    final int component_count = 4;
    final int component_size = 64;
    final int vertex_count = 3;
    final String name = "float64_4.smft";

    this.check(type, component_count, component_size, vertex_count, name);
  }

  @Test
  public void testLoadFloat32_1()
    throws Exception
  {
    final SMFComponentType type = SMFComponentType.ELEMENT_TYPE_FLOATING;
    final int component_count = 1;
    final int component_size = 32;
    final int vertex_count = 3;
    final String name = "float32_1.smft";

    this.check(type, component_count, component_size, vertex_count, name);
  }

  @Test
  public void testLoadFloat32_2()
    throws Exception
  {
    final SMFComponentType type = SMFComponentType.ELEMENT_TYPE_FLOATING;
    final int component_count = 2;
    final int component_size = 32;
    final int vertex_count = 3;
    final String name = "float32_2.smft";

    this.check(type, component_count, component_size, vertex_count, name);
  }

  @Test
  public void testLoadFloat32_3()
    throws Exception
  {
    final SMFComponentType type = SMFComponentType.ELEMENT_TYPE_FLOATING;
    final int component_count = 3;
    final int component_size = 32;
    final int vertex_count = 3;
    final String name = "float32_3.smft";

    this.check(type, component_count, component_size, vertex_count, name);
  }

  @Test
  public void testLoadFloat32_4()
    throws Exception
  {
    final SMFComponentType type = SMFComponentType.ELEMENT_TYPE_FLOATING;
    final int component_count = 4;
    final int component_size = 32;
    final int vertex_count = 3;
    final String name = "float32_4.smft";

    this.check(type, component_count, component_size, vertex_count, name);
  }

  @Test
  public void testLoadFloat16_1()
    throws Exception
  {
    final SMFComponentType type = SMFComponentType.ELEMENT_TYPE_FLOATING;
    final int component_count = 1;
    final int component_size = 16;
    final int vertex_count = 3;
    final String name = "float16_1.smft";

    this.check(type, component_count, component_size, vertex_count, name);
  }

  @Test
  public void testLoadFloat16_2()
    throws Exception
  {
    final SMFComponentType type = SMFComponentType.ELEMENT_TYPE_FLOATING;
    final int component_count = 2;
    final int component_size = 16;
    final int vertex_count = 3;
    final String name = "float16_2.smft";

    this.check(type, component_count, component_size, vertex_count, name);
  }

  @Test
  public void testLoadFloat16_3()
    throws Exception
  {
    final SMFComponentType type = SMFComponentType.ELEMENT_TYPE_FLOATING;
    final int component_count = 3;
    final int component_size = 16;
    final int vertex_count = 3;
    final String name = "float16_3.smft";

    this.check(type, component_count, component_size, vertex_count, name);
  }

  @Test
  public void testLoadFloat16_4()
    throws Exception
  {
    final SMFComponentType type = SMFComponentType.ELEMENT_TYPE_FLOATING;
    final int component_count = 4;
    final int component_size = 16;
    final int vertex_count = 3;
    final String name = "float16_4.smft";

    this.check(type, component_count, component_size, vertex_count, name);
  }

  @Test
  public void testLoadNoAttributesNoTriangles()
    throws Exception
  {
    final SMFByteBufferPackedMeshLoaderType loader =
      SMFByteBufferPackedMeshes.newLoader(
        SMFParserEventsMeta.ignore(),
        new SMFByteBufferPackerEventsType()
        {
          @Override
          public Validation<List<SMFErrorType>, SortedMap<Integer, SMFByteBufferPackingConfiguration>> onHeader(
            final SMFHeader header)
          {
            return valid(TreeMap.empty());
          }

          @Override
          public boolean onShouldPackTriangles()
          {
            return false;
          }

          @Override
          public ByteBuffer onAllocateTriangleBuffer(
            final long size)
          {
            return ByteBuffer
              .allocate(Math.toIntExact(size))
              .order(ByteOrder.nativeOrder());
          }

          @Override
          public ByteBuffer onAllocateAttributeBuffer(
            final Integer id,
            final SMFByteBufferPackingConfiguration config,
            final long size)
          {
            return ByteBuffer
              .allocate(Math.toIntExact(size))
              .order(ByteOrder.nativeOrder());
          }
        });

    try (final SMFParserSequentialType parser =
           createParser(loader, "float64_4.smft")) {
      // Nothing
    }

    Assert.assertTrue(loader.errors().isEmpty());

    final SMFByteBufferPackedMesh mesh = loader.mesh();
    Assert.assertTrue(mesh.attributeSets().isEmpty());
    Assert.assertFalse(mesh.triangles().isPresent());
  }

  @Test
  public void testLoadNoTriangles()
    throws Exception
  {
    final SMFAttributeName attr_name = SMFAttributeName.of("x");

    final SMFByteBufferPackedMeshLoaderType loader =
      SMFByteBufferPackedMeshes.newLoader(
        SMFParserEventsMeta.ignore(),
        new SMFByteBufferPackerEventsType()
        {
          @Override
          public Validation<List<SMFErrorType>, SortedMap<Integer, SMFByteBufferPackingConfiguration>> onHeader(
            final SMFHeader header)
          {
            final List<SMFAttribute> ordered =
              header.attributesInOrder();
            final List<SMFAttribute> filtered =
              ordered.filter(a -> Objects.equals(a.name(), attr_name));
            final SMFByteBufferPackingConfiguration config =
              SMFByteBufferPackingConfiguration.of(filtered);
            return valid(TreeMap.of(Tuple.of(Integer.valueOf(0), config)));
          }

          @Override
          public boolean onShouldPackTriangles()
          {
            return false;
          }

          @Override
          public ByteBuffer onAllocateTriangleBuffer(
            final long size)
          {
            return ByteBuffer
              .allocate(Math.toIntExact(size))
              .order(ByteOrder.nativeOrder());
          }

          @Override
          public ByteBuffer onAllocateAttributeBuffer(
            final Integer id,
            final SMFByteBufferPackingConfiguration config,
            final long size)
          {
            return ByteBuffer
              .allocate(Math.toIntExact(size))
              .order(ByteOrder.nativeOrder());
          }
        });

    try (final SMFParserSequentialType parser =
           createParser(loader, "float64_4.smft")) {
      // Nothing
    }

    Assert.assertTrue(loader.errors().isEmpty());

    final SMFByteBufferPackedMesh mesh = loader.mesh();
    Assert.assertFalse(mesh.attributeSets().isEmpty());
    Assert.assertFalse(mesh.triangles().isPresent());
  }

  @Test
  public void testLoadNoAttributes()
    throws Exception
  {
    final SMFByteBufferPackedMeshLoaderType loader =
      SMFByteBufferPackedMeshes.newLoader(
        SMFParserEventsMeta.ignore(),
        new SMFByteBufferPackerEventsType()
        {
          @Override
          public Validation<List<SMFErrorType>, SortedMap<Integer, SMFByteBufferPackingConfiguration>> onHeader(
            final SMFHeader header)
          {
            return valid(TreeMap.empty());
          }

          @Override
          public boolean onShouldPackTriangles()
          {
            return true;
          }

          @Override
          public ByteBuffer onAllocateTriangleBuffer(
            final long size)
          {
            return ByteBuffer
              .allocate(Math.toIntExact(size))
              .order(ByteOrder.nativeOrder());
          }

          @Override
          public ByteBuffer onAllocateAttributeBuffer(
            final Integer id,
            final SMFByteBufferPackingConfiguration config,
            final long size)
          {
            return ByteBuffer
              .allocate(Math.toIntExact(size))
              .order(ByteOrder.nativeOrder());
          }
        });

    try (final SMFParserSequentialType parser =
           createParser(loader, "float64_4.smft")) {
      // Nothing
    }

    Assert.assertTrue(loader.errors().isEmpty());

    final SMFByteBufferPackedMesh mesh = loader.mesh();
    Assert.assertTrue(mesh.attributeSets().isEmpty());
    Assert.assertTrue(mesh.triangles().isPresent());
  }

  @Rule public ExpectedException expected = ExpectedException.none();

  @Test
  public void testLoadError()
    throws Exception
  {
    final SMFAttributeName attr_name = SMFAttributeName.of("x");

    final SMFByteBufferPackedMeshLoaderType loader =
      SMFByteBufferPackedMeshes.newLoader(
        SMFParserEventsMeta.ignore(),
        new SMFByteBufferPackerEventsType()
        {
          @Override
          public Validation<List<SMFErrorType>, SortedMap<Integer, SMFByteBufferPackingConfiguration>> onHeader(
            final SMFHeader header)
          {
            final List<SMFAttribute> ordered =
              header.attributesInOrder();
            final List<SMFAttribute> filtered =
              ordered.filter(a -> Objects.equals(a.name(), attr_name));
            final SMFByteBufferPackingConfiguration config =
              SMFByteBufferPackingConfiguration.of(filtered);
            return valid(TreeMap.of(Tuple.of(Integer.valueOf(0), config)));
          }

          @Override
          public boolean onShouldPackTriangles()
          {
            return true;
          }

          @Override
          public ByteBuffer onAllocateTriangleBuffer(
            final long size)
          {
            return ByteBuffer
              .allocate(Math.toIntExact(size))
              .order(ByteOrder.nativeOrder());
          }

          @Override
          public ByteBuffer onAllocateAttributeBuffer(
            final Integer id,
            final SMFByteBufferPackingConfiguration config,
            final long size)
          {
            return ByteBuffer
              .allocate(Math.toIntExact(size))
              .order(ByteOrder.nativeOrder());
          }
        });

    try (final SMFParserSequentialType parser =
           createParser(loader, "broken.smft")) {
      // Nothing
    }

    Assert.assertFalse(loader.errors().isEmpty());

    this.expected.expect(IllegalStateException.class);
    loader.mesh();
  }
}
