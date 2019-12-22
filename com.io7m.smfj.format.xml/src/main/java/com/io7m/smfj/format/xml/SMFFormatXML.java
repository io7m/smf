/*
 * Copyright © 2019 Mark Raynsford <code@io7m.com> http://io7m.com
 *
 * Permission to use, copy, modify, and/or distribute this software for
 * any purpose with or without fee is hereby granted, provided that the
 * above copyright notice and this permission notice appear in all copies.
 *
 * THE SOFTWARE IS PROVIDED "AS IS" AND THE AUTHOR DISCLAIMS ALL
 * WARRANTIES WITH REGARD TO THIS SOFTWARE INCLUDING ALL IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS. IN NO EVENT SHALL THE AUTHOR
 * BE LIABLE FOR ANY SPECIAL, DIRECT, INDIRECT, OR CONSEQUENTIAL DAMAGES
 * OR ANY DAMAGES WHATSOEVER RESULTING FROM LOSS OF USE, DATA OR PROFITS,
 * WHETHER IN AN ACTION OF CONTRACT, NEGLIGENCE OR OTHER TORTIOUS ACTION,
 * ARISING OUT OF OR IN CONNECTION WITH THE USE OR PERFORMANCE OF THIS
 * SOFTWARE.
 */

package com.io7m.smfj.format.xml;

import com.io7m.jxe.core.JXEHardenedSAXParsers;
import com.io7m.jxe.core.JXESchemaDefinition;
import com.io7m.jxe.core.JXESchemaResolutionMappings;
import com.io7m.smfj.core.SMFFormatDescription;
import com.io7m.smfj.core.SMFFormatVersion;
import com.io7m.smfj.core.SMFPartialLogged;
import com.io7m.smfj.format.xml.internal.SMFXParser;
import com.io7m.smfj.format.xml.internal.SMFXProbe;
import com.io7m.smfj.format.xml.internal.SMFXSerializer;
import com.io7m.smfj.parser.api.SMFParseErrors;
import com.io7m.smfj.parser.api.SMFParserEventsType;
import com.io7m.smfj.parser.api.SMFParserProviderType;
import com.io7m.smfj.parser.api.SMFParserRandomAccessType;
import com.io7m.smfj.parser.api.SMFParserSequentialType;
import com.io7m.smfj.probe.api.SMFVersionProbeProviderType;
import com.io7m.smfj.probe.api.SMFVersionProbed;
import com.io7m.smfj.serializer.api.SMFSerializerProviderType;
import com.io7m.smfj.serializer.api.SMFSerializerType;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.nio.channels.FileChannel;
import java.util.Collections;
import java.util.Objects;
import java.util.Optional;
import java.util.SortedSet;
import java.util.TreeSet;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import org.osgi.service.component.annotations.Component;
import org.xml.sax.SAXException;

import static com.io7m.jxe.core.JXEXInclude.XINCLUDE_DISABLED;

/**
 * A format provider for the XML format.
 */

@Component
public final class SMFFormatXML
  implements SMFParserProviderType,
  SMFSerializerProviderType,
  SMFVersionProbeProviderType
{
  private static final JXESchemaDefinition SCHEMA_M2P0 =
    JXESchemaDefinition.of(
      URI.create("urn:com.io7m.smf:xml:2:0"),
      "schema-2.0.xsd",
      SMFFormatXML.class.getResource("/com/io7m/smfj/format/xml/schema-2.0.xsd")
    );

  private static final JXESchemaResolutionMappings SCHEMAS =
    JXESchemaResolutionMappings.builder()
      .putMappings(URI.create("urn:com.io7m.smf:xml:2:0"), SCHEMA_M2P0)
      .build();

  private static final SMFFormatDescription FORMAT = makeFormat();
  private static final SortedSet<SMFFormatVersion> SUPPORTED = makeVersion();
  private final JXEHardenedSAXParsers parsers;
  private final XMLOutputFactory writers;

  /**
   * Construct a format provider.
   */

  public SMFFormatXML()
  {
    this.parsers = new JXEHardenedSAXParsers();
    this.writers = XMLOutputFactory.newInstance();
  }

  private static SMFFormatDescription makeFormat()
  {
    final SMFFormatDescription.Builder b = SMFFormatDescription.builder();
    b.setDescription("An XML encoding of SMF data");
    b.setMimeType("application/vnd.io7m.smf+xml");
    b.setName("smf/x");
    b.setRandomAccess(false);
    b.setSuffix("smfx");
    return b.build();
  }

  private static SortedSet<SMFFormatVersion> makeVersion()
  {
    final var supported = new TreeSet<SMFFormatVersion>();
    supported.add(SMFFormatVersion.of(2, 0));
    return Collections.unmodifiableSortedSet(supported);
  }

  @Override
  public SMFFormatDescription parserFormat()
  {
    return FORMAT;
  }

  @Override
  public SortedSet<SMFFormatVersion> parserSupportedVersions()
  {
    return SUPPORTED;
  }

  @Override
  public SMFParserSequentialType parserCreateSequential(
    final SMFParserEventsType events,
    final URI uri,
    final InputStream stream)
    throws UnsupportedOperationException
  {
    Objects.requireNonNull(events, "events");
    Objects.requireNonNull(uri, "uri");
    Objects.requireNonNull(stream, "stream");

    try {
      final var reader =
        this.parsers.createXMLReader(
          Optional.empty(),
          XINCLUDE_DISABLED,
          SCHEMAS);
      return new SMFXParser(events, uri, reader, stream);
    } catch (final Exception e) {
      events.onError(SMFParseErrors.errorException(e));
      throw new UnsupportedOperationException(e);
    }
  }

  @Override
  public SMFParserRandomAccessType parserCreateRandomAccess(
    final SMFParserEventsType events,
    final URI uri,
    final FileChannel file)
    throws UnsupportedOperationException
  {
    Objects.requireNonNull(events, "events");
    Objects.requireNonNull(uri, "uri");
    Objects.requireNonNull(file, "file");

    throw new UnsupportedOperationException();
  }

  @Override
  public SMFPartialLogged<SMFVersionProbed> probe(
    final InputStream stream)
  {
    try {
      final var reader =
        this.parsers.createXMLReader(
          Optional.empty(),
          XINCLUDE_DISABLED,
          SCHEMAS);
      return new SMFXProbe(
        this,
        URI.create("urn:probe-input"),
        reader,
        stream)
        .execute();
    } catch (final ParserConfigurationException | SAXException e) {
      return SMFPartialLogged.failed(SMFParseErrors.errorException(e));
    }
  }

  @Override
  public SMFFormatDescription serializerFormat()
  {
    return FORMAT;
  }

  @Override
  public SortedSet<SMFFormatVersion> serializerSupportedVersions()
  {
    return SUPPORTED;
  }

  @Override
  public SMFSerializerType serializerCreate(
    final SMFFormatVersion version,
    final URI uri,
    final OutputStream stream)
    throws UnsupportedOperationException, IOException
  {
    Objects.requireNonNull(version, "version");
    Objects.requireNonNull(uri, "uri");
    Objects.requireNonNull(stream, "stream");

    try {
      return new SMFXSerializer(
        this.writers.createXMLStreamWriter(stream, "UTF-8"))
        .start();
    } catch (final XMLStreamException e) {
      throw new IOException(e);
    }
  }
}
