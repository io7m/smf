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

package com.io7m.smfj.format.binary2;

import com.io7m.jbssio.api.BSSReaderProviderType;
import com.io7m.jbssio.api.BSSWriterProviderType;
import com.io7m.jbssio.vanilla.BSSReaders;
import com.io7m.jbssio.vanilla.BSSWriters;
import com.io7m.smfj.core.SMFFormatDescription;
import com.io7m.smfj.core.SMFFormatVersion;
import com.io7m.smfj.core.SMFPartialLogged;
import com.io7m.smfj.format.binary2.internal.SMFB2ParsingContexts;
import com.io7m.smfj.format.binary2.internal.SMFB2ParsingFileHeader;
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
import java.util.SortedSet;
import java.util.TreeSet;
import org.osgi.service.component.annotations.Component;

/**
 * A provider for the binary2 format.
 */

@Component
public final class SMFFormatBinary2
  implements SMFParserProviderType,
  SMFSerializerProviderType,
  SMFVersionProbeProviderType
{
  private static final SMFFormatDescription FORMAT = makeFormat();
  private static final SortedSet<SMFFormatVersion> SUPPORTED = makeVersion();
  private final BSSReaderProviderType readers;
  private final BSSWriterProviderType writers;
  private final SMFB2ParsingContexts parserContexts;

  /**
   * Construct a binary format provider.
   *
   * @param inReaders A provider of readers
   * @param inWriters A provider of writers
   */

  public SMFFormatBinary2(
    final BSSReaderProviderType inReaders,
    final BSSWriterProviderType inWriters)
  {
    this.readers = Objects.requireNonNull(inReaders, "readers");
    this.writers = Objects.requireNonNull(inWriters, "writers");
    this.parserContexts = new SMFB2ParsingContexts(inReaders);
  }

  /**
   * Construct a binary format provider.
   */

  public SMFFormatBinary2()
  {
    this(new BSSReaders(), new BSSWriters());
  }

  private static SMFFormatDescription makeFormat()
  {
    final SMFFormatDescription.Builder b = SMFFormatDescription.builder();
    b.setDescription("A binary encoding of SMF data");
    b.setMimeType("application/vnd.io7m.smf");
    b.setName("smf/b");
    b.setRandomAccess(true);
    b.setSuffix("smfb");
    return b.build();
  }

  private static SortedSet<SMFFormatVersion> makeVersion()
  {
    final var supported = new TreeSet<SMFFormatVersion>();
    supported.add(SMFFormatVersion.of(2, 0));
    return Collections.unmodifiableSortedSet(supported);
  }

  @Override
  public String toString()
  {
    return SMFFormatBinary2.class.getCanonicalName();
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

    return new SMFB2ParserSequential(events, uri, stream, this.parserContexts);
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

    return new SMFB2ParserRandomAccess(events, uri, file, this.parserContexts);
  }

  @Override
  public SMFPartialLogged<SMFVersionProbed> probe(
    final InputStream stream)
  {
    try (var reader = this.readers.createReaderFromStream(
      URI.create("urn:input"), stream, "file")) {
      return SMFB2ParsingFileHeader.parseDirectly(reader)
        .map(formatVersion -> SMFVersionProbed.of(this, formatVersion));
    } catch (final Exception e) {
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

    return new SMFB2Serializer(
      version,
      this.writers.createWriterFromStream(uri, stream, "root"));
  }
}
