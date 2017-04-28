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

package com.io7m.smfj.format.text;

import com.io7m.jlexing.core.LexicalPosition;
import com.io7m.jlexing.core.LexicalPositions;
import com.io7m.jnull.NullCheck;
import com.io7m.smfj.core.SMFFormatDescription;
import com.io7m.smfj.core.SMFFormatVersion;
import com.io7m.smfj.parser.api.SMFParseError;
import com.io7m.smfj.parser.api.SMFParserEventsType;
import com.io7m.smfj.parser.api.SMFParserProviderType;
import com.io7m.smfj.parser.api.SMFParserRandomAccessType;
import com.io7m.smfj.parser.api.SMFParserSequentialType;
import com.io7m.smfj.probe.api.SMFVersionProbeProviderType;
import com.io7m.smfj.probe.api.SMFVersionProbed;
import com.io7m.smfj.serializer.api.SMFSerializerProviderType;
import com.io7m.smfj.serializer.api.SMFSerializerType;
import javaslang.collection.List;
import javaslang.collection.Seq;
import javaslang.collection.SortedSet;
import javaslang.collection.TreeSet;
import javaslang.collection.Vector;
import javaslang.control.Validation;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.URI;
import java.nio.channels.FileChannel;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

import static com.io7m.smfj.parser.api.SMFParseErrors.errorException;
import static com.io7m.smfj.parser.api.SMFParseErrors.errorWithMessage;
import static java.nio.charset.StandardCharsets.UTF_8;
import static javaslang.control.Validation.invalid;
import static javaslang.control.Validation.valid;

/**
 * The implementation of the text format.
 */

@Component
public final class SMFFormatText
  implements SMFParserProviderType,
  SMFSerializerProviderType,
  SMFVersionProbeProviderType
{
  private static final Logger LOG;
  private static final SMFFormatDescription FORMAT;
  private static final SortedSet<SMFFormatVersion> SUPPORTED;

  static {
    LOG = LoggerFactory.getLogger(SMFFormatText.class);

    {
      final SMFFormatDescription.Builder b = SMFFormatDescription.builder();
      b.setDescription("A plain text encoding of SMF data");
      b.setMimeType("text/vnd.io7m.smf");
      b.setName("smft");
      b.setRandomAccess(false);
      b.setSuffix("smft");
      FORMAT = b.build();
    }

    SUPPORTED = TreeSet.of(SMFFormatVersion.of(1, 0));
  }

  /**
   * Construct a text format provider.
   */

  public SMFFormatText()
  {

  }

  private static Validation<List<SMFParseError>, SMFFormatVersion> parseSMFVersion(
    final List<String> line,
    final LexicalPosition<URI> position)
  {
    if (line.isEmpty()) {
      return invalid(List.of(SMFTAbstractParser.makeErrorExpectedGot(
        "The first line must be a version declaration.",
        "smf <version-major> <version-minor>",
        line.toJavaStream().collect(Collectors.joining(" ")),
        position)));
    }

    switch (line.get(0)) {
      case "smf": {
        if (line.length() != 3) {
          return invalid(List.of(SMFTAbstractParser.makeErrorExpectedGot(
            "Incorrect number of arguments.",
            "smf <version-major> <version-minor>",
            line.toJavaStream().collect(Collectors.joining(" ")),
            position)));
        }

        try {
          final int major = Integer.parseUnsignedInt(line.get(1));
          final int minor = Integer.parseUnsignedInt(line.get(2));
          return valid(SMFFormatVersion.of(major, minor));
        } catch (final NumberFormatException e) {
          return invalid(List.of(SMFTAbstractParser.makeErrorExpectedGot(
            "Cannot parse number: " + e.getMessage(),
            "smf <version-major> <version-minor>",
            line.toJavaStream().collect(Collectors.joining(" ")),
            position)));
        }
      }
      default: {
        return invalid(List.of(SMFTAbstractParser.makeErrorExpectedGot(
          "Unrecognized command.",
          "smf <version-major> <version-minor>",
          line.toJavaStream().collect(Collectors.joining(" ")),
          position)));
      }
    }
  }

  private static String notSupported(
    final SMFFormatVersion version)
  {
    return String.format(
      "Version %d.%d is not supported",
      Integer.valueOf(version.major()),
      Integer.valueOf(version.minor()));
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
    final SMFParserEventsType in_events,
    final URI in_uri,
    final InputStream in_stream)
  {
    NullCheck.notNull(in_events, "Events");
    NullCheck.notNull(in_uri, "URI");
    NullCheck.notNull(in_stream, "Stream");
    return new Parser(
      in_events,
      SMFTLineReaderStreamIO.create(in_uri, in_stream),
      new AtomicReference<>(SMFTAbstractParser.ParserState.STATE_INITIAL));
  }

  @Override
  public SMFParserRandomAccessType parserCreateRandomAccess(
    final SMFParserEventsType events,
    final URI uri,
    final FileChannel file)
    throws UnsupportedOperationException
  {
    throw new UnsupportedOperationException(
      "Random access parsing is not supported");
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
    throws UnsupportedOperationException
  {
    if (SUPPORTED.contains(version)) {
      return new SMFTV1Serializer(version, uri, stream);
    }

    throw new UnsupportedOperationException(notSupported(version));
  }

  @Override
  public Validation<Seq<SMFParseError>, SMFVersionProbed> probe(
    final InputStream stream)
  {
    NullCheck.notNull(stream, "Stream");

    try (final BufferedReader reader =
           new BufferedReader(new InputStreamReader(stream, UTF_8))) {

      final String line = reader.readLine();
      if (line != null) {
        final SMFLineLexer lexer = new SMFLineLexer();
        final List<String> line_tokens = lexer.lex(line);

        final Validation<List<SMFParseError>, SMFFormatVersion> result =
          parseSMFVersion(line_tokens, LexicalPositions.zero());

        return result.flatMap(v -> {
          if (SUPPORTED.contains(v)) {
            return valid(SMFVersionProbed.of(this, v));
          }
          return invalid(List.of(errorWithMessage(notSupported(v))));
        }).mapError(Vector::ofAll);
      }

      return invalid(Vector.of(
        errorWithMessage("Could not read first line of file.")));
    } catch (final Exception e) {
      return invalid(Vector.of(errorException(e)));
    }
  }

  private static final class Parser extends SMFTAbstractParser
  {
    private SMFTV1Parser v1;

    Parser(
      final SMFParserEventsType in_events,
      final SMFTLineReaderType in_reader,
      final AtomicReference<ParserState> in_state)
    {
      super(in_events, in_reader, in_state);
    }

    @Override
    protected Logger log()
    {
      return LOG;
    }

    private Validation<List<SMFParseError>, SMFFormatVersion> onParseVersion(
      final List<String> line)
    {
      return parseSMFVersion(line, this.reader.position());
    }

    @Override
    public void close()
      throws IOException
    {
      LOG.debug("closing parser");
      super.events.onFinish();
    }

    @Override
    public void parseHeader()
    {
      if (super.state.get() != ParserState.STATE_INITIAL) {
        throw new IllegalStateException("Parser has already executed");
      }

      try {
        super.events.onStart();

        final Optional<List<String>> line = super.reader.line();
        if (line.isPresent()) {
          final Validation<List<SMFParseError>, SMFFormatVersion> v_version =
            this.onParseVersion(line.get());

          if (v_version.isValid()) {
            final SMFFormatVersion version = v_version.get();
            super.events.onVersionReceived(version);
            switch (version.major()) {
              case 1: {
                LOG.debug("instantiating 1.* parser");
                this.v1 =
                  new SMFTV1Parser(this, super.events, super.reader, version);
                this.v1.parseHeader();
                return;
              }
              default: {
                LOG.debug("no parser for version {}", version);
                this.fail("Unsupported version", Optional.empty());
                return;
              }
            }
          }

          this.failErrors(v_version.getError());
          return;
        }
        this.fail("Unexpected EOF", Optional.empty());
      } catch (final Exception e) {
        this.fail(e.getMessage(), Optional.of(e));
      }
    }

    @Override
    public void parseData()
      throws IllegalStateException
    {
      if (super.state.get() != ParserState.STATE_HEADER_PARSED) {
        throw new IllegalStateException("Header has not been parsed");
      }

      this.v1.parseData();
    }
  }
}