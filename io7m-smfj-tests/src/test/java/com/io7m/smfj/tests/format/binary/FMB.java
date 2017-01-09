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

package com.io7m.smfj.tests.format.binary;

import com.io7m.jlexing.core.LexicalPosition;
import com.io7m.smfj.core.SMFAttribute;
import com.io7m.smfj.core.SMFAttributeName;
import com.io7m.smfj.core.SMFFormatVersion;
import com.io7m.smfj.core.SMFHeader;
import com.io7m.smfj.format.binary.SMFFormatBinary;
import com.io7m.smfj.parser.api.SMFParseError;
import com.io7m.smfj.parser.api.SMFParserEventsType;
import com.io7m.smfj.parser.api.SMFParserRandomAccessType;
import com.io7m.smfj.serializer.api.SMFSerializerType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.channels.FileChannel;
import java.nio.channels.SeekableByteChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

public final class FMB
{
  private static final Logger LOG;

  static {
    LOG = LoggerFactory.getLogger(FMB.class);
  }

  private FMB()
  {

  }

  public static void main(final String[] args)
    throws IOException
  {
    final SMFFormatBinary fmt = new SMFFormatBinary();
    final Path path = Paths.get("mesh-binary.smfb");
    try (final SeekableByteChannel channel =
           Files.newByteChannel(path, StandardOpenOption.READ)) {

      final Map<SMFAttributeName, SMFAttribute> attributes = new HashMap<>(8);
      final List<SMFAttribute> attributes_ordered = new ArrayList<>(8);

      final AtomicLong vertex_count = new AtomicLong();
      final AtomicLong triangle_count = new AtomicLong();
      final AtomicLong triangle_size = new AtomicLong();

      final FileChannel file_channel = (FileChannel) channel;
      final SMFParserRandomAccessType p =
        fmt.parserCreateRandomAccess(new SMFParserEventsType()
        {
          @Override
          public boolean onMeta(
            final long vendor,
            final long schema,
            final long length)
          {
            return true;
          }

          @Override
          public void onMetaData(
            final long vendor,
            final long schema,
            final byte[] data)
          {
            LOG.debug(
              "metadata: {} {} {}",
              Long.toUnsignedString(vendor, 16),
              Long.toUnsignedString(schema, 16),
              Base64.getUrlEncoder().encodeToString(data));
          }

          @Override
          public void onStart()
          {
            LOG.debug("parser started");
          }

          @Override
          public void onError(
            final SMFParseError e)
          {
            final LexicalPosition<Path> lexical = e.lexical();
            final Optional<Path> opt = lexical.file();
            if (opt.isPresent()) {
              LOG.error(
                "{}:{}:{}: {}",
                opt.get(),
                Integer.valueOf(lexical.line()),
                Integer.valueOf(lexical.column()),
                e.message());
            } else {
              LOG.error(
                "{}:{}: {}",
                Integer.valueOf(lexical.line()),
                Integer.valueOf(lexical.column()),
                e.message());
            }
          }

          @Override
          public void onVersionReceived(
            final SMFFormatVersion version)
          {
            LOG.info("version: {}", version);
          }

          @Override
          public void onHeaderParsed(final SMFHeader header)
          {
            LOG.debug("header parsed: {}", header);
          }

          @Override
          public void onDataAttributeStart(
            final SMFAttribute attribute)
          {
            LOG.debug("data start: {}", attribute);
          }

          @Override
          public void onDataAttributeValueIntegerSigned1(
            final long x)
          {
            LOG.debug(
              "data signed 1: {}",
              Long.valueOf(x));
          }

          @Override
          public void onDataAttributeValueIntegerSigned2(
            final long x,
            final long y)
          {
            LOG.debug(
              "data signed 2: {} {}",
              Long.valueOf(x),
              Long.valueOf(y));
          }

          @Override
          public void onDataAttributeValueIntegerSigned3(
            final long x,
            final long y,
            final long z)
          {
            LOG.debug(
              "data signed 3: {} {} {}",
              Long.valueOf(x),
              Long.valueOf(y),
              Long.valueOf(z));
          }

          @Override
          public void onDataAttributeValueIntegerSigned4(
            final long x,
            final long y,
            final long z,
            final long w)
          {
            LOG.debug(
              "data signed 4: {} {} {} {}",
              Long.valueOf(x),
              Long.valueOf(y),
              Long.valueOf(z),
              Long.valueOf(w));
          }

          @Override
          public void onDataAttributeValueIntegerUnsigned1(
            final long x)
          {
            LOG.debug(
              "data unsigned 1: {}",
              Long.valueOf(x));
          }

          @Override
          public void onDataAttributeValueIntegerUnsigned2(
            final long x,
            final long y)
          {
            LOG.debug(
              "data unsigned 2: {} {}",
              Long.valueOf(x),
              Long.valueOf(y));
          }

          @Override
          public void onDataAttributeValueIntegerUnsigned3(
            final long x,
            final long y,
            final long z)
          {
            LOG.debug(
              "data unsigned 3: {} {} {}",
              Long.valueOf(x),
              Long.valueOf(y),
              Long.valueOf(z));
          }

          @Override
          public void onDataAttributeValueIntegerUnsigned4(
            final long x,
            final long y,
            final long z,
            final long w)
          {
            LOG.debug(
              "data unsigned 4: {} {} {} {}",
              Long.valueOf(x),
              Long.valueOf(y),
              Long.valueOf(z),
              Long.valueOf(w));
          }

          @Override
          public void onDataAttributeValueFloat1(
            final double x)
          {
            LOG.debug(
              "data float 1: {}",
              Double.valueOf(x));
          }

          @Override
          public void onDataAttributeValueFloat2(
            final double x,
            final double y)
          {
            LOG.debug(
              "data float 2: {} {}",
              Double.valueOf(x),
              Double.valueOf(y));
          }

          @Override
          public void onDataAttributeValueFloat3(
            final double x,
            final double y,
            final double z)
          {
            LOG.debug(
              "data float 3: {} {} {}",
              Double.valueOf(x),
              Double.valueOf(y),
              Double.valueOf(z));
          }

          @Override
          public void onDataAttributeValueFloat4(
            final double x,
            final double y,
            final double z,
            final double w)
          {
            LOG.debug(
              "data float 4: {} {} {} {}",
              Double.valueOf(x),
              Double.valueOf(y),
              Double.valueOf(z),
              Double.valueOf(w));
          }

          @Override
          public void onDataAttributeFinish(
            final SMFAttribute attribute)
          {
            LOG.debug("data finish: {}", attribute);
          }

          @Override
          public void onDataTrianglesStart()
          {
            LOG.debug("triangles start");
          }

          @Override
          public void onDataTriangle(
            final long v0,
            final long v1,
            final long v2)
          {
            LOG.debug(
              "triangle: {} {} {}",
              Long.valueOf(v0),
              Long.valueOf(v1),
              Long.valueOf(v2));
          }

          @Override
          public void onDataTrianglesFinish()
          {
            LOG.debug("triangles finish");
          }

          @Override
          public void onFinish()
          {
            LOG.debug("parser finished");
          }
        }, path, file_channel);

      p.parseHeader();

      for (final SMFAttributeName name : attributes.keySet()) {
        p.parseAttributeData(name);
      }

      p.parseTriangles();

      final Path out_path = Files.createTempFile("fmb-", ".smfb");
      LOG.debug("output: {}", out_path);

      try (final OutputStream out = Files.newOutputStream(out_path)) {
        final SMFSerializerType serial =
          fmt.serializerCreate(
            SMFFormatVersion.of(1, 0),
            out_path,
            out);

        final SMFHeader.Builder hb = SMFHeader.builder();
        hb.setVertexCount(vertex_count.get());
        hb.setTriangleIndexSizeBits(triangle_size.get());
        hb.setTriangleCount(triangle_count.get());
        hb.setAttributesInOrder(
          javaslang.collection.List.ofAll(attributes_ordered));
        serial.serializeHeader(hb.build());
      }
    }
  }
}
