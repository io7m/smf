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

package com.io7m.smfj.tests.format.binary;

import com.io7m.smfj.core.SMFAttribute;
import com.io7m.smfj.core.SMFErrorType;
import com.io7m.smfj.core.SMFFormatVersion;
import com.io7m.smfj.core.SMFHeader;
import com.io7m.smfj.core.SMFSchemaIdentifier;
import com.io7m.smfj.core.SMFWarningType;
import com.io7m.smfj.format.binary.SMFFormatBinary;
import com.io7m.smfj.parser.api.SMFParserEventsBodyType;
import com.io7m.smfj.parser.api.SMFParserEventsDataAttributeValuesType;
import com.io7m.smfj.parser.api.SMFParserEventsDataAttributesNonInterleavedType;
import com.io7m.smfj.parser.api.SMFParserEventsDataMetaType;
import com.io7m.smfj.parser.api.SMFParserEventsDataTrianglesType;
import com.io7m.smfj.parser.api.SMFParserEventsHeaderType;
import com.io7m.smfj.parser.api.SMFParserEventsType;
import com.io7m.smfj.parser.api.SMFParserSequentialType;
import com.io7m.smfj.serializer.api.SMFSerializerDataAttributesNonInterleavedType;
import com.io7m.smfj.serializer.api.SMFSerializerDataAttributesValuesType;
import com.io7m.smfj.serializer.api.SMFSerializerDataTrianglesType;
import com.io7m.smfj.serializer.api.SMFSerializerType;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.URI;
import java.util.Objects;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class SMFB
  implements SMFParserEventsType,
  SMFParserEventsHeaderType,
  SMFParserEventsBodyType,
  SMFParserEventsDataTrianglesType,
  SMFParserEventsDataAttributesNonInterleavedType,
  SMFParserEventsDataAttributeValuesType,
  SMFParserEventsDataMetaType
{
  private static final Logger LOG;

  static {
    LOG = LoggerFactory.getLogger(SMFB.class);
  }

  private final String[] args;
  private SMFSerializerType serial;
  private SMFSerializerDataAttributesNonInterleavedType serial_ni;
  private SMFSerializerDataAttributesValuesType serial_values;
  private SMFSerializerDataTrianglesType serial_tri;
  private long meta_vendor;
  private long meta_schema;

  private SMFB(
    final String[] in_args)
  {
    this.args = Objects.requireNonNull(in_args, "args");
  }

  public static void main(
    final String[] args)
    throws Exception
  {
    new SMFB(args).run();
  }

  public void run()
    throws Exception
  {
    final SMFFormatBinary fmt = new SMFFormatBinary();
    try (FileOutputStream out = new FileOutputStream("/tmp/x.bin")) {
      try (SMFSerializerType serial = fmt.serializerCreate(
        SMFFormatVersion.of(1, 0),
        URI.create("urn:out"),
        out)) {
        this.serial = serial;
        try (FileInputStream stream = new FileInputStream(this.args[0])) {
          try (SMFParserSequentialType parser =
                 fmt.parserCreateSequential(
                   this,
                   URI.create("urn:file:" + this.args[0]),
                   stream)) {
            parser.parse();
          }
        }
      }
    }
  }

  @Override
  public void onStart()
  {
    LOG.debug("started");
  }

  @Override
  public Optional<SMFParserEventsHeaderType> onVersionReceived(
    final SMFFormatVersion version)
  {
    LOG.debug(
      "received format version: {} {}",
      Integer.valueOf(version.major()),
      Integer.valueOf(version.minor()));
    return Optional.of(this);
  }

  @Override
  public void onFinish()
  {
    LOG.debug("finished");
  }

  @Override
  public void onError(
    final SMFErrorType e)
  {
    LOG.error("{}", e.fullMessage());
    e.exception().ifPresent(ex -> LOG.error("exception: ", ex));
  }

  @Override
  public void onWarning(
    final SMFWarningType w)
  {
    LOG.warn("{}", w.fullMessage());
    w.exception().ifPresent(ex -> LOG.warn("exception: ", ex));
  }

  @Override
  public Optional<SMFParserEventsBodyType> onHeaderParsed(
    final SMFHeader header)
  {
    LOG.debug("received header");

    LOG.debug(
      "vertex count:   {}",
      Long.toUnsignedString(header.vertexCount()));
    LOG.debug(
      "triangle count: {}",
      Long.toUnsignedString(header.triangles().triangleCount()));
    LOG.debug(
      "triangle size:  {}",
      Integer.toUnsignedString(header.triangles().triangleIndexSizeBits()));
    LOG.debug(
      "coordinates:    {}",
      header.coordinateSystem().toHumanString());

    try {
      this.serial.serializeHeader(header);
    } catch (final IOException e) {
      throw new UncheckedIOException(e);
    }

    return Optional.of(this);
  }

  @Override
  public Optional<SMFParserEventsDataAttributesNonInterleavedType> onAttributesNonInterleaved()
  {
    try {
      this.serial_ni = this.serial.serializeVertexDataNonInterleavedStart();
    } catch (final IOException e) {
      throw new UncheckedIOException(e);
    }

    return Optional.of(this);
  }

  @Override
  public Optional<SMFParserEventsDataTrianglesType> onTriangles()
  {
    try {
      this.serial_tri = this.serial.serializeTrianglesStart();
    } catch (final IOException e) {
      throw new UncheckedIOException(e);
    }

    return Optional.of(this);
  }

  @Override
  public void onDataTriangle(
    final long v0,
    final long v1,
    final long v2)
  {
    LOG.debug(
      "triangle: {} {} {}",
      Long.toUnsignedString(v0),
      Long.toUnsignedString(v1),
      Long.toUnsignedString(v2));

    try {
      this.serial_tri.serializeTriangle(v0, v1, v2);
    } catch (final IOException e) {
      throw new UncheckedIOException(e);
    }
  }

  @Override
  public void onDataTrianglesFinish()
  {
    LOG.debug("finished triangles");

    try {
      this.serial_tri.close();
    } catch (final IOException e) {
      throw new UncheckedIOException(e);
    }
  }

  @Override
  public Optional<SMFParserEventsDataAttributeValuesType> onDataAttributeStart(
    final SMFAttribute attribute)
  {
    try {
      this.serial_values = this.serial_ni.serializeData(attribute.name());
    } catch (final IOException e) {
      throw new UncheckedIOException(e);
    }

    return Optional.of(this);
  }

  @Override
  public void onDataAttributesNonInterleavedFinish()
  {
    LOG.debug("non-interleaved data finished");

    try {
      this.serial_ni.close();
    } catch (final IOException e) {
      throw new UncheckedIOException(e);
    }
  }

  @Override
  public void onDataAttributeValueIntegerSigned1(
    final long x)
  {
    LOG.debug("{}", Long.valueOf(x));
  }

  @Override
  public void onDataAttributeValueIntegerSigned2(
    final long x,
    final long y)
  {
    LOG.debug(
      "{} {}",
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
      "{} {} {}",
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
      "{} {} {} {}",
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
      "{}",
      Long.toUnsignedString(x));
  }

  @Override
  public void onDataAttributeValueIntegerUnsigned2(
    final long x,
    final long y)
  {
    LOG.debug(
      "{} {}",
      Long.toUnsignedString(x),
      Long.toUnsignedString(y));
  }

  @Override
  public void onDataAttributeValueIntegerUnsigned3(
    final long x,
    final long y,
    final long z)
  {
    LOG.debug(
      "{} {} {}",
      Long.toUnsignedString(x),
      Long.toUnsignedString(y),
      Long.toUnsignedString(z));
  }

  @Override
  public void onDataAttributeValueIntegerUnsigned4(
    final long x,
    final long y,
    final long z,
    final long w)
  {
    LOG.debug(
      "{} {} {} {}",
      Long.toUnsignedString(x),
      Long.toUnsignedString(y),
      Long.toUnsignedString(z),
      Long.toUnsignedString(w));
  }

  @Override
  public void onDataAttributeValueFloat1(
    final double x)
  {
    LOG.debug(
      "{}",
      x);
  }

  @Override
  public void onDataAttributeValueFloat2(
    final double x,
    final double y)
  {
    LOG.debug(
      "{} {}",
      x,
      y);
  }

  @Override
  public void onDataAttributeValueFloat3(
    final double x,
    final double y,
    final double z)
  {
    LOG.debug(
      "{} {} {}",
      x,
      y,
      z);
  }

  @Override
  public void onDataAttributeValueFloat4(
    final double x,
    final double y,
    final double z,
    final double w)
  {
    LOG.debug(
      "{} {} {} {}",
      x,
      y,
      z,
      w);

    try {
      this.serial_values.serializeValueFloat4(x, y, z, w);
    } catch (final IOException e) {
      throw new UncheckedIOException(e);
    }
  }

  @Override
  public void onDataAttributeValueFinish()
  {
    LOG.debug("attribute finished");
    try {
      this.serial_values.close();
    } catch (final IOException e) {
      throw new UncheckedIOException(e);
    }
  }

  @Override
  public void onMetaData(
    final SMFSchemaIdentifier schema,
    final byte[] data)
  {
    try {
      this.serial.serializeMetadata(schema, data);
    } catch (final IOException e) {
      throw new UncheckedIOException(e);
    }
  }

  @Override
  public Optional<SMFParserEventsDataMetaType> onMeta(
    final SMFSchemaIdentifier schema)
  {
    return Optional.of(this);
  }
}
