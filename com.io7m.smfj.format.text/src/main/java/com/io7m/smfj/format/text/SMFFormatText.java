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
import java.util.Objects;
import com.io7m.smfj.core.SMFFormatDescription;
import com.io7m.smfj.core.SMFFormatVersion;
import com.io7m.smfj.format.text.v1.SMFTV1Parser;
import com.io7m.smfj.format.text.v1.SMFTV1Serializer;
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
import java.util.BitSet;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.io7m.smfj.format.text.implementation.Flags.TRIANGLES_RECEIVED;
import static com.io7m.smfj.format.text.implementation.Flags.TRIANGLES_REQUIRED;
import static com.io7m.smfj.format.text.implementation.Flags.VERTICES_RECEIVED;
import static com.io7m.smfj.format.text.implementation.Flags.VERTICES_REQUIRED;
import static com.io7m.smfj.parser.api.SMFParseErrors.errorException;
import static com.io7m.smfj.parser.api.SMFParseErrors.errorExpectedGot;
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
      b.setName("smf/t");
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
      return invalid(List.of(errorExpectedGot(
        "The first line must be a version declaration.",
        "smf <version-major> <version-minor>",
        line.toJavaStream().collect(Collectors.joining(" ")),
        position)));
    }

    switch (line.get(0)) {
      case "smf": {
        if (line.length() != 3) {
          return invalid(List.of(errorExpectedGot(
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
          return invalid(List.of(errorExpectedGot(
            "Cannot parse number: " + e.getMessage(),
            "smf <version-major> <version-minor>",
            line.toJavaStream().collect(Collectors.joining(" ")),
            position)));
        }
      }
      default: {
        return invalid(List.of(errorExpectedGot(
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
      "Version %s is not supported",
      version.toHumanString());
  }

  @Override
  public String toString()
  {
    return SMFFormatText.class.getCanonicalName();
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
    Objects.requireNonNull(in_events, "Events");
    Objects.requireNonNull(in_uri, "URI");
    Objects.requireNonNull(in_stream, "Stream");

    return new Parser(
      in_events, SMFTLineReaderStreamIO.create(in_uri, in_stream));
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
    Objects.requireNonNull(stream, "Stream");

    try (BufferedReader reader =
           new BufferedReader(new InputStreamReader(stream, UTF_8))) {

      final String line = reader.readLine();
      if (line != null) {
        final SMFTLineLexer lexer = new SMFTLineLexer();
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

  private static final class Parser implements SMFParserSequentialType
  {
    private final SMFParserEventsType events;
    private final SMFTLineReaderType reader;

    Parser(
      final SMFParserEventsType in_events,
      final SMFTLineReaderType in_reader)
    {
      this.events = Objects.requireNonNull(in_events, "Events");
      this.reader = Objects.requireNonNull(in_reader, "Reader");
    }

    @Override
    public void close()
      throws IOException
    {

    }

    @Override
    public void parse()
    {
      try {
        this.events.onStart();

        final Optional<List<String>> initial_opt = this.reader.line();
        if (!initial_opt.isPresent()) {
          this.events.onError(SMFParseError.of(
            this.reader.position(),
            "Unexpected EOF",
            Optional.empty()));
          return;
        }

        final List<String> initial = initial_opt.get();
        final Validation<List<SMFParseError>, SMFFormatVersion> result =
          parseSMFVersion(initial, LexicalPositions.zero());

        if (!result.isValid()) {
          result.getError().forEach(this.events::onError);
          return;
        }

        final BitSet state = new BitSet(8);
        final SMFFormatVersion version = result.get();
        switch (version.major()) {
          case 1: {
            try (SMFTV1Parser p =
                   new SMFTV1Parser(version, state, this.events, this.reader)) {
              p.parse();
            }
            break;
          }

          default: {
            this.events.onError(SMFParseError.of(
              this.reader.position(),
              notSupported(version),
              Optional.empty()));
            break;
          }
        }

        if (state.get(VERTICES_REQUIRED) && !state.get(VERTICES_RECEIVED)) {
          this.events.onError(SMFParseError.of(
            this.reader.position(),
            "A non-zero vertex count was specified, but no vertices were provided.",
            Optional.empty()));
        }

        if (state.get(TRIANGLES_REQUIRED) && !state.get(TRIANGLES_RECEIVED)) {
          this.events.onError(SMFParseError.of(
            this.reader.position(),
            "A non-zero triangle count was specified, but no triangles were provided.",
            Optional.empty()));
        }

      } catch (final Exception e) {
        this.events.onError(SMFParseError.of(
          this.reader.position(), e.getMessage(), Optional.of(e)));
      } finally {
        this.events.onFinish();
      }
    }
  }
}
