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
import com.io7m.junreachable.UnimplementedCodeException;
import com.io7m.junreachable.UnreachableCodeException;
import com.io7m.smfj.bytebuffer.SMFByteBufferPackedMesh;
import com.io7m.smfj.bytebuffer.SMFByteBufferPackedMeshLoaderType;
import com.io7m.smfj.bytebuffer.SMFByteBufferPackedMeshes;
import com.io7m.smfj.bytebuffer.SMFByteBufferPackingConfiguration;
import com.io7m.smfj.core.SMFAttribute;
import com.io7m.smfj.core.SMFAttributeName;
import com.io7m.smfj.core.SMFComponentType;
import com.io7m.smfj.format.text.SMFFormatText;
import com.io7m.smfj.parser.api.SMFParseError;
import com.io7m.smfj.parser.api.SMFParserEventsMetaType;
import com.io7m.smfj.parser.api.SMFParserEventsType;
import com.io7m.smfj.parser.api.SMFParserProviderType;
import com.io7m.smfj.parser.api.SMFParserSequentialType;
import javaslang.collection.List;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;

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

  private static void checkTypeUnsigned16(
    final int component_count,
    final ByteBuffer rb)
  {
    switch (component_count) {
      case 1: {
        {
          final long vx = (long) Unsigned16.unpackFromBuffer(rb, 0);
          final long vy = (long) Unsigned16.unpackFromBuffer(rb, 2);
          final long vz = (long) Unsigned16.unpackFromBuffer(rb, 4);
          Assert.assertEquals(0L, vx);
          Assert.assertEquals(32768L, vy);
          Assert.assertEquals(65535L, vz);
        }
        break;
      }
      case 2: {
        {
          final long vx = (long) Unsigned16.unpackFromBuffer(rb, 0);
          final long vy = (long) Unsigned16.unpackFromBuffer(rb, 2);
          Assert.assertEquals(0L, vx);
          Assert.assertEquals(65535L, vy);
        }

        {
          final long vx = (long) Unsigned16.unpackFromBuffer(rb, 4);
          final long vy = (long) Unsigned16.unpackFromBuffer(rb, 6);
          Assert.assertEquals(0L, vx);
          Assert.assertEquals(65535L, vy);
        }

        {
          final long vx = (long) Unsigned16.unpackFromBuffer(rb, 8);
          final long vy = (long) Unsigned16.unpackFromBuffer(rb, 10);
          Assert.assertEquals(0L, vx);
          Assert.assertEquals(65535L, vy);
        }
        break;
      }
      case 3: {
        {
          final long vx = (long) Unsigned16.unpackFromBuffer(rb, 0);
          final long vy = (long) Unsigned16.unpackFromBuffer(rb, 2);
          final long vz = (long) Unsigned16.unpackFromBuffer(rb, 4);
          Assert.assertEquals(0L, vx);
          Assert.assertEquals(32768L, vy);
          Assert.assertEquals(65535L, vz);
        }

        {
          final long vx = (long) Unsigned16.unpackFromBuffer(rb, 6);
          final long vy = (long) Unsigned16.unpackFromBuffer(rb, 8);
          final long vz = (long) Unsigned16.unpackFromBuffer(rb, 10);
          Assert.assertEquals(0L, vx);
          Assert.assertEquals(32768L, vy);
          Assert.assertEquals(65535L, vz);
        }

        {
          final long vx = (long) Unsigned16.unpackFromBuffer(rb, 12);
          final long vy = (long) Unsigned16.unpackFromBuffer(rb, 14);
          final long vz = (long) Unsigned16.unpackFromBuffer(rb, 16);
          Assert.assertEquals(0L, vx);
          Assert.assertEquals(32768L, vy);
          Assert.assertEquals(65535L, vz);
        }
        break;
      }
      case 4: {
        {
          final long vx = (long) Unsigned16.unpackFromBuffer(rb, 0);
          final long vy = (long) Unsigned16.unpackFromBuffer(rb, 2);
          final long vz = (long) Unsigned16.unpackFromBuffer(rb, 4);
          final long vw = (long) Unsigned16.unpackFromBuffer(rb, 6);
          Assert.assertEquals(0L, vx);
          Assert.assertEquals(32768L, vy);
          Assert.assertEquals(65535L, vz);
          Assert.assertEquals(1L, vw);
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
        {
          Assert.assertEquals(0L, rb.getLong(0));
          Assert.assertEquals(0x8000000000000000L, rb.getLong(8));
          Assert.assertEquals(0xffffffffffffffffL, rb.getLong(16));
        }
        break;
      }
      case 2: {
        {
          final long vx = rb.getLong(0);
          final long vy = rb.getLong(8);
          Assert.assertEquals(0L, vx);
          Assert.assertEquals(0xffffffffffffffffL, vy);
        }

        {
          final long vx = rb.getLong(16);
          final long vy = rb.getLong(24);
          Assert.assertEquals(0L, vx);
          Assert.assertEquals(0xffffffffffffffffL, vy);
        }

        {
          final long vx = rb.getLong(32);
          final long vy = rb.getLong(40);
          Assert.assertEquals(0L, vx);
          Assert.assertEquals(0xffffffffffffffffL, vy);
        }
        break;
      }
      case 3: {
        {
          final long vx = rb.getLong(0);
          final long vy = rb.getLong(8);
          final long vz = rb.getLong(16);
          Assert.assertEquals(0L, vx);
          Assert.assertEquals(0x8000000000000000L, vy);
          Assert.assertEquals(0xffffffffffffffffL, vz);
        }

        {
          final long vx = rb.getLong(24);
          final long vy = rb.getLong(32);
          final long vz = rb.getLong(40);
          Assert.assertEquals(0L, vx);
          Assert.assertEquals(0x8000000000000000L, vy);
          Assert.assertEquals(0xffffffffffffffffL, vz);
        }

        {
          final long vx = rb.getLong(48);
          final long vy = rb.getLong(56);
          final long vz = rb.getLong(64);
          Assert.assertEquals(0L, vx);
          Assert.assertEquals(0x8000000000000000L, vy);
          Assert.assertEquals(0xffffffffffffffffL, vz);
        }
        break;
      }
      case 4: {
        {
          final long vx = rb.getLong(0);
          final long vy = rb.getLong(8);
          final long vz = rb.getLong(16);
          final long vw = rb.getLong(24);
          Assert.assertEquals(0L, vx);
          Assert.assertEquals(0x8000000000000000L, vy);
          Assert.assertEquals(0xffffffffffffffffL, vz);
          Assert.assertEquals(1L, vw);
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
        {
          final long vx = Unsigned32.unpackFromBuffer(rb, 0);
          final long vy = Unsigned32.unpackFromBuffer(rb, 4);
          final long vz = Unsigned32.unpackFromBuffer(rb, 8);
          Assert.assertEquals(0L, vx);
          Assert.assertEquals(2147483648L, vy);
          Assert.assertEquals(4294967295L, vz);
        }
        break;
      }
      case 2: {
        {
          final long vx = Unsigned32.unpackFromBuffer(rb, 0);
          final long vy = Unsigned32.unpackFromBuffer(rb, 4);
          Assert.assertEquals(0L, vx);
          Assert.assertEquals(4294967295L, vy);
        }

        {
          final long vx = Unsigned32.unpackFromBuffer(rb, 8);
          final long vy = Unsigned32.unpackFromBuffer(rb, 12);
          Assert.assertEquals(0L, vx);
          Assert.assertEquals(4294967295L, vy);
        }

        {
          final long vx = Unsigned32.unpackFromBuffer(rb, 16);
          final long vy = Unsigned32.unpackFromBuffer(rb, 20);
          Assert.assertEquals(0L, vx);
          Assert.assertEquals(4294967295L, vy);
        }
        break;
      }
      case 3: {
        {
          final long vx = Unsigned32.unpackFromBuffer(rb, 0);
          final long vy = Unsigned32.unpackFromBuffer(rb, 4);
          final long vz = Unsigned32.unpackFromBuffer(rb, 8);
          Assert.assertEquals(0L, vx);
          Assert.assertEquals(2147483648L, vy);
          Assert.assertEquals(4294967295L, vz);
        }

        {
          final long vx = Unsigned32.unpackFromBuffer(rb, 12);
          final long vy = Unsigned32.unpackFromBuffer(rb, 16);
          final long vz = Unsigned32.unpackFromBuffer(rb, 20);
          Assert.assertEquals(0L, vx);
          Assert.assertEquals(2147483648L, vy);
          Assert.assertEquals(4294967295L, vz);
        }

        {
          final long vx = Unsigned32.unpackFromBuffer(rb, 24);
          final long vy = Unsigned32.unpackFromBuffer(rb, 28);
          final long vz = Unsigned32.unpackFromBuffer(rb, 32);
          Assert.assertEquals(0L, vx);
          Assert.assertEquals(2147483648L, vy);
          Assert.assertEquals(4294967295L, vz);
        }
        break;
      }
      case 4: {
        {
          final long vx = Unsigned32.unpackFromBuffer(rb, 0);
          final long vy = Unsigned32.unpackFromBuffer(rb, 4);
          final long vz = Unsigned32.unpackFromBuffer(rb, 8);
          final long vw = Unsigned32.unpackFromBuffer(rb, 12);
          Assert.assertEquals(0L, vx);
          Assert.assertEquals(2147483648L, vy);
          Assert.assertEquals(4294967295L, vz);
          Assert.assertEquals(1L, vw);
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
        }
        break;
      }
      default: {
        throw new UnreachableCodeException();
      }
    }
  }

  private static void checkTypeUnsigned8(
    final int component_count,
    final ByteBuffer rb)
  {
    switch (component_count) {
      case 1: {
        {
          final long vx = (long) Unsigned8.unpackFromBuffer(rb, 0);
          final long vy = (long) Unsigned8.unpackFromBuffer(rb, 1);
          final long vz = (long) Unsigned8.unpackFromBuffer(rb, 2);
          Assert.assertEquals(0L, vx);
          Assert.assertEquals(128L, vy);
          Assert.assertEquals(255L, vz);
        }
        break;
      }
      case 2: {
        {
          final long vx = (long) Unsigned8.unpackFromBuffer(rb, 0);
          final long vy = (long) Unsigned8.unpackFromBuffer(rb, 1);
          Assert.assertEquals(0L, vx);
          Assert.assertEquals(255L, vy);
        }

        {
          final long vx = (long) Unsigned8.unpackFromBuffer(rb, 2);
          final long vy = (long) Unsigned8.unpackFromBuffer(rb, 3);
          Assert.assertEquals(0L, vx);
          Assert.assertEquals(255L, vy);
        }

        {
          final long vx = (long) Unsigned8.unpackFromBuffer(rb, 4);
          final long vy = (long) Unsigned8.unpackFromBuffer(rb, 5);
          Assert.assertEquals(0L, vx);
          Assert.assertEquals(255L, vy);
        }
        break;
      }
      case 3: {
        {
          final long vx = (long) Unsigned8.unpackFromBuffer(rb, 0);
          final long vy = (long) Unsigned8.unpackFromBuffer(rb, 1);
          final long vz = (long) Unsigned8.unpackFromBuffer(rb, 2);
          Assert.assertEquals(0L, vx);
          Assert.assertEquals(128L, vy);
          Assert.assertEquals(255L, vz);
        }

        {
          final long vx = (long) Unsigned8.unpackFromBuffer(rb, 3);
          final long vy = (long) Unsigned8.unpackFromBuffer(rb, 4);
          final long vz = (long) Unsigned8.unpackFromBuffer(rb, 5);
          Assert.assertEquals(0L, vx);
          Assert.assertEquals(128L, vy);
          Assert.assertEquals(255L, vz);
        }

        {
          final long vx = (long) Unsigned8.unpackFromBuffer(rb, 6);
          final long vy = (long) Unsigned8.unpackFromBuffer(rb, 7);
          final long vz = (long) Unsigned8.unpackFromBuffer(rb, 8);
          Assert.assertEquals(0L, vx);
          Assert.assertEquals(128L, vy);
          Assert.assertEquals(255L, vz);
        }
        break;
      }
      case 4: {
        {
          final long vx = (long) Unsigned8.unpackFromBuffer(rb, 0);
          final long vy = (long) Unsigned8.unpackFromBuffer(rb, 1);
          final long vz = (long) Unsigned8.unpackFromBuffer(rb, 2);
          final long vw = (long) Unsigned8.unpackFromBuffer(rb, 3);
          Assert.assertEquals(0L, vx);
          Assert.assertEquals(128L, vy);
          Assert.assertEquals(255L, vz);
          Assert.assertEquals(128L, vw);
        }

        {
          final long vx = (long) Unsigned8.unpackFromBuffer(rb, 4);
          final long vy = (long) Unsigned8.unpackFromBuffer(rb, 5);
          final long vz = (long) Unsigned8.unpackFromBuffer(rb, 6);
          final long vw = (long) Unsigned8.unpackFromBuffer(rb, 7);
          Assert.assertEquals(0L, vx);
          Assert.assertEquals(128L, vy);
          Assert.assertEquals(255L, vz);
          Assert.assertEquals(128L, vw);
        }

        {
          final long vx = (long) Unsigned8.unpackFromBuffer(rb, 8);
          final long vy = (long) Unsigned8.unpackFromBuffer(rb, 9);
          final long vz = (long) Unsigned8.unpackFromBuffer(rb, 10);
          final long vw = (long) Unsigned8.unpackFromBuffer(rb, 11);
          Assert.assertEquals(0L, vx);
          Assert.assertEquals(128L, vy);
          Assert.assertEquals(255L, vz);
          Assert.assertEquals(128L, vw);
        }
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
        {
          final long vx = (long) Signed16.unpackFromBuffer(rb, 0);
          final long vy = (long) Signed16.unpackFromBuffer(rb, 2);
          final long vz = (long) Signed16.unpackFromBuffer(rb, 4);
          Assert.assertEquals(-32767L, vx);
          Assert.assertEquals(0L, vy);
          Assert.assertEquals(32767L, vz);
        }
        break;
      }
      case 2: {
        {
          final long vx = (long) Signed16.unpackFromBuffer(rb, 0);
          final long vy = (long) Signed16.unpackFromBuffer(rb, 2);
          Assert.assertEquals(-32767L, vx);
          Assert.assertEquals(32767L, vy);
        }

        {
          final long vx = (long) Signed16.unpackFromBuffer(rb, 4);
          final long vy = (long) Signed16.unpackFromBuffer(rb, 6);
          Assert.assertEquals(-32767L, vx);
          Assert.assertEquals(32767L, vy);
        }

        {
          final long vx = (long) Signed16.unpackFromBuffer(rb, 8);
          final long vy = (long) Signed16.unpackFromBuffer(rb, 10);
          Assert.assertEquals(-32767L, vx);
          Assert.assertEquals(32767L, vy);
        }
        break;
      }
      case 3: {
        {
          final long vx = (long) Signed16.unpackFromBuffer(rb, 0);
          final long vy = (long) Signed16.unpackFromBuffer(rb, 2);
          final long vz = (long) Signed16.unpackFromBuffer(rb, 4);
          Assert.assertEquals(-32767L, vx);
          Assert.assertEquals(0L, vy);
          Assert.assertEquals(32767L, vz);
        }

        {
          final long vx = (long) Signed16.unpackFromBuffer(rb, 6);
          final long vy = (long) Signed16.unpackFromBuffer(rb, 8);
          final long vz = (long) Signed16.unpackFromBuffer(rb, 10);
          Assert.assertEquals(-32767L, vx);
          Assert.assertEquals(0L, vy);
          Assert.assertEquals(32767L, vz);
        }

        {
          final long vx = (long) Signed16.unpackFromBuffer(rb, 12);
          final long vy = (long) Signed16.unpackFromBuffer(rb, 14);
          final long vz = (long) Signed16.unpackFromBuffer(rb, 16);
          Assert.assertEquals(-32767L, vx);
          Assert.assertEquals(0L, vy);
          Assert.assertEquals(32767L, vz);
        }
        break;
      }
      case 4: {
        {
          final long vx = (long) Signed16.unpackFromBuffer(rb, 0);
          final long vy = (long) Signed16.unpackFromBuffer(rb, 2);
          final long vz = (long) Signed16.unpackFromBuffer(rb, 4);
          final long vw = (long) Signed16.unpackFromBuffer(rb, 6);
          Assert.assertEquals(-32767L, vx);
          Assert.assertEquals(0L, vy);
          Assert.assertEquals(32767L, vz);
          Assert.assertEquals(1L, vw);
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
        {
          Assert.assertEquals(-2147483648L, (long) rb.getInt(0));
          Assert.assertEquals(0L, (long) rb.getInt(4));
          Assert.assertEquals(2147483647L, (long) rb.getInt(8));
        }
        break;
      }
      case 2: {
        {
          final long vx = (long) Signed32.unpackFromBuffer(rb, 0);
          final long vy = (long) Signed32.unpackFromBuffer(rb, 4);
          Assert.assertEquals(-2147483648L, vx);
          Assert.assertEquals(2147483647L, vy);
        }

        {
          final long vx = (long) Signed32.unpackFromBuffer(rb, 8);
          final long vy = (long) Signed32.unpackFromBuffer(rb, 12);
          Assert.assertEquals(-2147483648L, vx);
          Assert.assertEquals(2147483647L, vy);
        }

        {
          final long vx = (long) Signed32.unpackFromBuffer(rb, 16);
          final long vy = (long) Signed32.unpackFromBuffer(rb, 20);
          Assert.assertEquals(-2147483648L, vx);
          Assert.assertEquals(2147483647L, vy);
        }
        break;
      }
      case 3: {
        {
          final long vx = (long) Signed32.unpackFromBuffer(rb, 0);
          final long vy = (long) Signed32.unpackFromBuffer(rb, 4);
          final long vz = (long) Signed32.unpackFromBuffer(rb, 8);
          Assert.assertEquals(-2147483648L, vx);
          Assert.assertEquals(0L, vy);
          Assert.assertEquals(2147483647L, vz);
        }

        {
          final long vx = (long) Signed32.unpackFromBuffer(rb, 12);
          final long vy = (long) Signed32.unpackFromBuffer(rb, 16);
          final long vz = (long) Signed32.unpackFromBuffer(rb, 20);
          Assert.assertEquals(-2147483648L, vx);
          Assert.assertEquals(0L, vy);
          Assert.assertEquals(2147483647L, vz);
        }

        {
          final long vx = (long) Signed32.unpackFromBuffer(rb, 24);
          final long vy = (long) Signed32.unpackFromBuffer(rb, 28);
          final long vz = (long) Signed32.unpackFromBuffer(rb, 32);
          Assert.assertEquals(-2147483648L, vx);
          Assert.assertEquals(0L, vy);
          Assert.assertEquals(2147483647L, vz);
        }
        break;
      }
      case 4: {
        {
          final long vx = (long) Signed32.unpackFromBuffer(rb, 0);
          final long vy = (long) Signed32.unpackFromBuffer(rb, 4);
          final long vz = (long) Signed32.unpackFromBuffer(rb, 8);
          final long vw = (long) Signed32.unpackFromBuffer(rb, 12);
          Assert.assertEquals(-2147483648L, vx);
          Assert.assertEquals(0L, vy);
          Assert.assertEquals(2147483647L, vz);
          Assert.assertEquals(1L, vw);
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
        {
          Assert.assertEquals(-9223372036854775808L, rb.getLong(0));
          Assert.assertEquals(0L, rb.getLong(8));
          Assert.assertEquals(9223372036854775807L, rb.getLong(16));
        }
        break;
      }
      case 2: {
        {
          final long vx = rb.getLong(0);
          final long vy = rb.getLong(8);
          Assert.assertEquals(-9223372036854775808L, vx);
          Assert.assertEquals(9223372036854775807L, vy);
        }

        {
          final long vx = rb.getLong(16);
          final long vy = rb.getLong(24);
          Assert.assertEquals(-9223372036854775808L, vx);
          Assert.assertEquals(9223372036854775807L, vy);
        }

        {
          final long vx = rb.getLong(32);
          final long vy = rb.getLong(40);
          Assert.assertEquals(-9223372036854775808L, vx);
          Assert.assertEquals(9223372036854775807L, vy);
        }
        break;
      }
      case 3: {
        {
          final long vx = rb.getLong(0);
          final long vy = rb.getLong(8);
          final long vz = rb.getLong(16);
          Assert.assertEquals(-9223372036854775808L, vx);
          Assert.assertEquals(0L, vy);
          Assert.assertEquals(9223372036854775807L, vz);
        }

        {
          final long vx = rb.getLong(24);
          final long vy = rb.getLong(32);
          final long vz = rb.getLong(40);
          Assert.assertEquals(-9223372036854775808L, vx);
          Assert.assertEquals(0L, vy);
          Assert.assertEquals(9223372036854775807L, vz);
        }

        {
          final long vx = rb.getLong(48);
          final long vy = rb.getLong(56);
          final long vz = rb.getLong(64);
          Assert.assertEquals(-9223372036854775808L, vx);
          Assert.assertEquals(0L, vy);
          Assert.assertEquals(9223372036854775807L, vz);
        }
        break;
      }
      case 4: {
        {
          final long vx = rb.getLong(0);
          final long vy = rb.getLong(8);
          final long vz = rb.getLong(16);
          final long vw = rb.getLong(24);
          Assert.assertEquals(-9223372036854775808L, vx);
          Assert.assertEquals(0L, vy);
          Assert.assertEquals(9223372036854775807L, vz);
          Assert.assertEquals(1L, vw);
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
        Assert.assertEquals(-1000.0f, rb.getFloat(0), 0.0f);
        Assert.assertEquals(0.0f, rb.getFloat(4), 0.0f);
        Assert.assertEquals(1000.0f, rb.getFloat(8), 0.0f);
        break;
      }
      case 2: {
        {
          final double vx = (double) rb.getFloat(0);
          final double vy = (double) rb.getFloat(4);
          Assert.assertEquals(vx, -1000.0, 0.0);
          Assert.assertEquals(vy, 1000.0, 0.0);
        }

        {
          final double vx = (double) rb.getFloat(8);
          final double vy = (double) rb.getFloat(12);
          Assert.assertEquals(vx, -1000.0, 0.0);
          Assert.assertEquals(vy, 1000.0, 0.0);
        }

        {
          final double vx = (double) rb.getFloat(16);
          final double vy = (double) rb.getFloat(20);
          Assert.assertEquals(vx, -1000.0, 0.0);
          Assert.assertEquals(vy, 1000.0, 0.0);
        }
        break;
      }
      case 3: {
        {
          final double vx = (double) rb.getFloat(0);
          final double vy = (double) rb.getFloat(4);
          final double vz = (double) rb.getFloat(8);
          Assert.assertEquals(vx, -1000.0, 0.0);
          Assert.assertEquals(vy, 0.0, 0.0);
          Assert.assertEquals(vz, 1000.0, 0.0);
        }

        {
          final double vx = (double) rb.getFloat(12);
          final double vy = (double) rb.getFloat(16);
          final double vz = (double) rb.getFloat(20);
          Assert.assertEquals(vx, -1000.0, 0.0);
          Assert.assertEquals(vy, 0.0, 0.0);
          Assert.assertEquals(vz, 1000.0, 0.0);
        }

        {
          final double vx = (double) rb.getFloat(24);
          final double vy = (double) rb.getFloat(28);
          final double vz = (double) rb.getFloat(32);
          Assert.assertEquals(vx, -1000.0, 0.0);
          Assert.assertEquals(vy, 0.0, 0.0);
          Assert.assertEquals(vz, 1000.0, 0.0);
        }
        break;
      }
      case 4: {
        {
          final double vx = (double) rb.getFloat(0);
          final double vy = (double) rb.getFloat(4);
          final double vz = (double) rb.getFloat(8);
          final double vw = (double) rb.getFloat(12);
          Assert.assertEquals(vx, -1000.0, 0.0);
          Assert.assertEquals(vy, 0.0, 0.0);
          Assert.assertEquals(vz, 1.0, 0.0);
          Assert.assertEquals(vw, 1000.0, 0.0);
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
        Assert.assertEquals(-1000.0, rb.getDouble(0), 0.0);
        Assert.assertEquals(0.0, rb.getDouble(8), 0.0);
        Assert.assertEquals(1000.0, rb.getDouble(16), 0.0);
        break;
      }
      case 2: {
        {
          final double vx = rb.getDouble(0);
          final double vy = rb.getDouble(8);
          Assert.assertEquals(vx, -1000.0, 0.0);
          Assert.assertEquals(vy, 1000.0, 0.0);
        }

        {
          final double vx = rb.getDouble(16);
          final double vy = rb.getDouble(24);
          Assert.assertEquals(vx, -1000.0, 0.0);
          Assert.assertEquals(vy, 1000.0, 0.0);
        }

        {
          final double vx = rb.getDouble(32);
          final double vy = rb.getDouble(40);
          Assert.assertEquals(vx, -1000.0, 0.0);
          Assert.assertEquals(vy, 1000.0, 0.0);
        }
        break;
      }
      case 3: {
        {
          final double vx = rb.getDouble(0);
          final double vy = rb.getDouble(8);
          final double vz = rb.getDouble(16);
          Assert.assertEquals(vx, -1000.0, 0.0);
          Assert.assertEquals(vy, 0.0, 0.0);
          Assert.assertEquals(vz, 1000.0, 0.0);
        }

        {
          final double vx = rb.getDouble(24);
          final double vy = rb.getDouble(32);
          final double vz = rb.getDouble(40);
          Assert.assertEquals(vx, -1000.0, 0.0);
          Assert.assertEquals(vy, 0.0, 0.0);
          Assert.assertEquals(vz, 1000.0, 0.0);
        }

        {
          final double vx = rb.getDouble(48);
          final double vy = rb.getDouble(56);
          final double vz = rb.getDouble(64);
          Assert.assertEquals(vx, -1000.0, 0.0);
          Assert.assertEquals(vy, 0.0, 0.0);
          Assert.assertEquals(vz, 1000.0, 0.0);
        }
        break;
      }
      case 4: {
        {
          final double vx = rb.getDouble(0);
          final double vy = rb.getDouble(8);
          final double vz = rb.getDouble(16);
          final double vw = rb.getDouble(24);
          Assert.assertEquals(vx, -1000.0, 0.0);
          Assert.assertEquals(vy, 0.0, 0.0);
          Assert.assertEquals(vz, 1.0, 0.0);
          Assert.assertEquals(vw, 1000.0, 0.0);
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
        Assert.assertEquals(
          -1000.0, Binary16.unpackDouble(rb.getChar(0)), 0.0);
        Assert.assertEquals(
          0.0, Binary16.unpackDouble(rb.getChar(2)), 0.0);
        Assert.assertEquals(
          1000.0, Binary16.unpackDouble(rb.getChar(4)), 0.0);
        break;
      }
      case 2: {
        {
          final double vx = Binary16.unpackDouble(rb.getChar(0));
          final double vy = Binary16.unpackDouble(rb.getChar(2));
          Assert.assertEquals(vx, -1000.0, 0.0);
          Assert.assertEquals(vy, 1000.0, 0.0);
        }

        {
          final double vx = Binary16.unpackDouble(rb.getChar(4));
          final double vy = Binary16.unpackDouble(rb.getChar(6));
          Assert.assertEquals(vx, -1000.0, 0.0);
          Assert.assertEquals(vy, 1000.0, 0.0);
        }

        {
          final double vx = Binary16.unpackDouble(rb.getChar(8));
          final double vy = Binary16.unpackDouble(rb.getChar(10));
          Assert.assertEquals(vx, -1000.0, 0.0);
          Assert.assertEquals(vy, 1000.0, 0.0);
        }
        break;
      }
      case 3: {
        {
          final double vx = Binary16.unpackDouble(rb.getChar(0));
          final double vy = Binary16.unpackDouble(rb.getChar(2));
          final double vz = Binary16.unpackDouble(rb.getChar(4));
          Assert.assertEquals(vx, -1000.0, 0.0);
          Assert.assertEquals(vy, 0.0, 0.0);
          Assert.assertEquals(vz, 1000.0, 0.0);
        }

        {
          final double vx = Binary16.unpackDouble(rb.getChar(6));
          final double vy = Binary16.unpackDouble(rb.getChar(8));
          final double vz = Binary16.unpackDouble(rb.getChar(10));
          Assert.assertEquals(vx, -1000.0, 0.0);
          Assert.assertEquals(vy, 0.0, 0.0);
          Assert.assertEquals(vz, 1000.0, 0.0);
        }

        {
          final double vx = Binary16.unpackDouble(rb.getChar(12));
          final double vy = Binary16.unpackDouble(rb.getChar(14));
          final double vz = Binary16.unpackDouble(rb.getChar(16));
          Assert.assertEquals(vx, -1000.0, 0.0);
          Assert.assertEquals(vy, 0.0, 0.0);
          Assert.assertEquals(vz, 1000.0, 0.0);
        }
        break;
      }
      case 4: {
        {
          final double vx = Binary16.unpackDouble(rb.getChar(0));
          final double vy = Binary16.unpackDouble(rb.getChar(2));
          final double vz = Binary16.unpackDouble(rb.getChar(4));
          final double vw = Binary16.unpackDouble(rb.getChar(6));
          Assert.assertEquals(vx, -1000.0, 0.0);
          Assert.assertEquals(vy, 0.0, 0.0);
          Assert.assertEquals(vz, 1.0, 0.0);
          Assert.assertEquals(vw, 1000.0, 0.0);
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
        Assert.assertEquals(-127L, (long) rb.get(0));
        Assert.assertEquals(0L, (long) rb.get(1));
        Assert.assertEquals(127L, (long) rb.get(2));
        break;
      }
      case 2: {
        Assert.assertEquals(-127L, (long) rb.get(0));
        Assert.assertEquals(0L, (long) rb.get(1));
        Assert.assertEquals(0L, (long) rb.get(2));
        Assert.assertEquals(0L, (long) rb.get(3));
        Assert.assertEquals(127L, (long) rb.get(4));
        Assert.assertEquals(0L, (long) rb.get(5));
        break;
      }
      case 3: {
        Assert.assertEquals(-127L, (long) rb.get(0));
        Assert.assertEquals(0L, (long) rb.get(1));
        Assert.assertEquals(127L, (long) rb.get(2));

        Assert.assertEquals(-127L, (long) rb.get(3));
        Assert.assertEquals(0L, (long) rb.get(4));
        Assert.assertEquals(127L, (long) rb.get(5));

        Assert.assertEquals(-127L, (long) rb.get(6));
        Assert.assertEquals(0L, (long) rb.get(7));
        Assert.assertEquals(127L, (long) rb.get(8));
        break;
      }
      case 4: {
        Assert.assertEquals(-127L, (long) rb.get(0));
        Assert.assertEquals(0L, (long) rb.get(1));
        Assert.assertEquals(127L, (long) rb.get(2));
        Assert.assertEquals(0L, (long) rb.get(3));

        Assert.assertEquals(-127L, (long) rb.get(4));
        Assert.assertEquals(0L, (long) rb.get(5));
        Assert.assertEquals(127L, (long) rb.get(6));
        Assert.assertEquals(0L, (long) rb.get(7));

        Assert.assertEquals(-127L, (long) rb.get(8));
        Assert.assertEquals(0L, (long) rb.get(9));
        Assert.assertEquals(127L, (long) rb.get(10));
        Assert.assertEquals(0L, (long) rb.get(11));
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
          case 16:
          {
            checkTypeFloat16(component_count, rb);
            break;
          }
          case 32:
          {
            checkTypeFloat32(component_count, rb);
            break;
          }
          case 64:
          {
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
    checkType(type, component_count, component_size, mesh.attributeData());
    
    Assert.assertEquals(0L, (long) mesh.triangleData().get(0));
    Assert.assertEquals(1L, (long) mesh.triangleData().get(1));
    Assert.assertEquals(2L, (long) mesh.triangleData().get(2));
  }

  private SMFByteBufferPackedMesh parseMesh(
    final String name)
    throws IOException
  {
    final SMFAttributeName attr_name = SMFAttributeName.of("x");

    final SMFByteBufferPackedMeshLoaderType loader =
      SMFByteBufferPackedMeshes.newLoader(
        new Meta(),
        header -> {
          final List<SMFAttribute> ordered =
            header.attributesInOrder();
          final List<SMFAttribute> filtered =
            ordered.filter(a -> Objects.equals(a.name(), attr_name));
          final SMFByteBufferPackingConfiguration config =
            SMFByteBufferPackingConfiguration.of(filtered);
          return config;
        },
        SMFByteBufferPackedMeshes.allocateByteBufferHeap(),
        SMFByteBufferPackedMeshes.allocateByteBufferHeap());

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
    Assert.assertEquals(0L, (long) mesh.triangleData().get(0));
    Assert.assertEquals(1L, (long) mesh.triangleData().get(1));
    Assert.assertEquals(2L, (long) mesh.triangleData().get(2));
  }

  @Test
  public void testTriangle16()
    throws Exception
  {
    final SMFByteBufferPackedMesh mesh = this.parseMesh("triangle16.smft");
    Assert.assertEquals(0L, (long) mesh.triangleData().getChar(0));
    Assert.assertEquals(1L, (long) mesh.triangleData().getChar(2));
    Assert.assertEquals(2L, (long) mesh.triangleData().getChar(4));
  }

  @Test
  public void testTriangle32()
    throws Exception
  {
    final SMFByteBufferPackedMesh mesh = this.parseMesh("triangle32.smft");
    Assert.assertEquals(0L, (long) mesh.triangleData().getInt(0));
    Assert.assertEquals(1L, (long) mesh.triangleData().getInt(4));
    Assert.assertEquals(2L, (long) mesh.triangleData().getInt(8));
  }

  @Test
  public void testTriangle64()
    throws Exception
  {
    final SMFByteBufferPackedMesh mesh = this.parseMesh("triangle64.smft");
    Assert.assertEquals(0L, (long) mesh.triangleData().getLong(0));
    Assert.assertEquals(1L, (long) mesh.triangleData().getLong(8));
    Assert.assertEquals(2L, (long) mesh.triangleData().getLong(16));
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
  
  private final class Meta implements SMFParserEventsMetaType
  {
    @Override
    public boolean onMeta(
      final long vendor,
      final long schema,
      final long length)
    {
      throw new UnreachableCodeException();
    }

    @Override
    public void onMetaData(
      final long vendor,
      final long schema,
      final byte[] data)
    {
      throw new UnreachableCodeException();
    }

    @Override
    public void onError(
      final SMFParseError e)
    {
      throw new UnreachableCodeException();
    }
  }
}
