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

package com.io7m.smfj.format.binary;

import com.io7m.jaffirm.core.Preconditions;
import com.io7m.jlexing.core.LexicalPosition;
import com.io7m.jnull.NullCheck;
import com.io7m.junreachable.UnimplementedCodeException;
import com.io7m.junreachable.UnreachableCodeException;
import com.io7m.smfj.core.SMFFormatDescription;
import com.io7m.smfj.core.SMFFormatVersion;
import com.io7m.smfj.format.binary.v1.SMFBv1Parser;
import com.io7m.smfj.format.binary.v1.SMFBv1Serializer;
import com.io7m.smfj.parser.api.SMFParseError;
import com.io7m.smfj.parser.api.SMFParserEventsHeaderType;
import com.io7m.smfj.parser.api.SMFParserEventsType;
import com.io7m.smfj.parser.api.SMFParserProviderType;
import com.io7m.smfj.parser.api.SMFParserRandomAccessType;
import com.io7m.smfj.parser.api.SMFParserSequentialType;
import com.io7m.smfj.probe.api.SMFVersionProbeProviderType;
import com.io7m.smfj.probe.api.SMFVersionProbed;
import com.io7m.smfj.serializer.api.SMFSerializerProviderType;
import com.io7m.smfj.serializer.api.SMFSerializerType;
import javaslang.collection.Seq;
import javaslang.collection.SortedSet;
import javaslang.collection.TreeSet;
import javaslang.collection.Vector;
import javaslang.control.Validation;
import org.apache.commons.io.IOUtils;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.bind.DatatypeConverter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.FileChannel;
import java.util.Arrays;
import java.util.BitSet;
import java.util.Optional;

import static com.io7m.smfj.format.binary.implementation.Flags.TRIANGLES_RECEIVED;
import static com.io7m.smfj.format.binary.implementation.Flags.TRIANGLES_REQUIRED;
import static com.io7m.smfj.format.binary.implementation.Flags.VERTICES_RECEIVED;
import static com.io7m.smfj.format.binary.implementation.Flags.VERTICES_REQUIRED;
import static com.io7m.smfj.parser.api.SMFParseErrors.errorException;
import static com.io7m.smfj.parser.api.SMFParseErrors.errorWithMessage;
import static javaslang.control.Validation.invalid;
import static javaslang.control.Validation.valid;

/**
 * The implementation of the binary format.
 */

@Component
public final class SMFFormatBinary
  implements SMFParserProviderType,
  SMFSerializerProviderType,
  SMFVersionProbeProviderType
{
  private static final Logger LOG;
  private static final SMFFormatDescription FORMAT;
  private static final byte[] MAGIC_NUMBER;
  private static final SortedSet<SMFFormatVersion> SUPPORTED_VERSIONS;

  static {
    LOG = LoggerFactory.getLogger(SMFFormatBinary.class);

    {
      final SMFFormatDescription.Builder b = SMFFormatDescription.builder();
      b.setDescription("A binary encoding of SMF data");
      b.setMimeType("application/vnd.io7m.smf");
      b.setName("smf/b");
      b.setRandomAccess(true);
      b.setSuffix("smfb");
      FORMAT = b.build();
    }

    {
      SUPPORTED_VERSIONS = TreeSet.of(SMFFormatVersion.of(1, 0));
    }

    {
      MAGIC_NUMBER = new byte[8];
      MAGIC_NUMBER[0] = (byte) (0x89 & 0xff);
      MAGIC_NUMBER[1] = (byte) 'S';
      MAGIC_NUMBER[2] = (byte) 'M';
      MAGIC_NUMBER[3] = (byte) 'F';
      MAGIC_NUMBER[4] = (byte) 0x0D;
      MAGIC_NUMBER[5] = (byte) 0x0A;
      MAGIC_NUMBER[6] = (byte) 0x1A;
      MAGIC_NUMBER[7] = (byte) 0x0A;
    }
  }

  /**
   * Construct a binary format provider.
   */

  public SMFFormatBinary()
  {

  }

  /**
   * @return The magic bytes at the start of every SMF file
   */

  public static byte[] magicNumber()
  {
    return MAGIC_NUMBER.clone();
  }

  private static boolean magicNumberIsValid(
    final byte[] data)
  {
    Preconditions.checkPreconditionI(
      data.length,
      data.length == 8,
      len -> String.format("Length %d must be 8 bytes", Integer.valueOf(len)));
    return Arrays.equals(MAGIC_NUMBER, data);
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
    return SMFFormatBinary.class.getCanonicalName();
  }

  @Override
  public SMFFormatDescription parserFormat()
  {
    return FORMAT;
  }

  @Override
  public SortedSet<SMFFormatVersion> parserSupportedVersions()
  {
    return SUPPORTED_VERSIONS;
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

    return new ParserSequential(in_events, in_stream, in_uri);
  }

  @Override
  public SMFParserRandomAccessType parserCreateRandomAccess(
    final SMFParserEventsType in_events,
    final URI in_uri,
    final FileChannel in_channel)
    throws UnsupportedOperationException
  {
    NullCheck.notNull(in_events, "Events");
    NullCheck.notNull(in_uri, "URI");
    NullCheck.notNull(in_channel, "Channel");

    throw new UnimplementedCodeException();
  }

  @Override
  public SMFFormatDescription serializerFormat()
  {
    return FORMAT;
  }

  @Override
  public SortedSet<SMFFormatVersion> serializerSupportedVersions()
  {
    return TreeSet.of(SMFFormatVersion.of(1, 0));
  }

  @Override
  public SMFSerializerType serializerCreate(
    final SMFFormatVersion version,
    final URI uri,
    final OutputStream stream)
    throws UnsupportedOperationException
  {
    if (!SUPPORTED_VERSIONS.contains(version)) {
      throw new UnsupportedOperationException(notSupported(version));
    }

    switch (version.major()) {
      case 1: {
        return new SMFBv1Serializer(version, uri, stream);
      }
      default: {
        throw new UnreachableCodeException();
      }
    }
  }

  @Override
  public Validation<Seq<SMFParseError>, SMFVersionProbed> probe(
    final InputStream stream)
  {
    NullCheck.notNull(stream, "stream");

    try {
      final byte[] data =
        IOUtils.toByteArray(stream, MAGIC_NUMBER.length + 4 + 4);

      final byte[] magic = Arrays.copyOf(data, 8);
      if (magicNumberIsValid(magic)) {
        final ByteBuffer data_major =
          ByteBuffer.wrap(Arrays.copyOfRange(data, 8, 8 + 4))
            .order(ByteOrder.BIG_ENDIAN);
        final ByteBuffer data_minor =
          ByteBuffer.wrap(Arrays.copyOfRange(data, 8 + 4, 8 + 4 + 4))
            .order(ByteOrder.BIG_ENDIAN);

        final long major =
          (long) data_major.getInt(0) & 0xFFFFFFFFL;
        final long minor =
          (long) data_minor.getInt(0) & 0xFFFFFFFFL;
        final SMFFormatVersion version =
          SMFFormatVersion.of((int) major, (int) minor);

        if (SUPPORTED_VERSIONS.contains(version)) {
          return valid(SMFVersionProbed.of(this, version));
        }

        return invalid(
          Vector.of(errorWithMessage(notSupported(version))));
      }

      final StringBuilder sb = new StringBuilder(128);
      sb.append("Bad magic number.");
      sb.append(System.lineSeparator());
      sb.append("  Expected: ");
      sb.append(DatatypeConverter.printHexBinary(MAGIC_NUMBER));
      sb.append(System.lineSeparator());
      sb.append("  Received: ");
      sb.append(DatatypeConverter.printHexBinary(magic));
      sb.append(System.lineSeparator());
      return invalid(Vector.of(errorWithMessage(sb.toString())));
    } catch (final Exception e) {
      return invalid(Vector.of(errorException(e)));
    }
  }

  private static final class ParserSequential implements SMFParserSequentialType
  {
    private final SMFParserEventsType events;
    private final URI uri;
    private final SMFBDataStreamReaderType reader;
    private final InputStream stream;
    private SMFParserSequentialType parser;

    ParserSequential(
      final SMFParserEventsType in_events,
      final InputStream in_stream,
      final URI in_uri)
    {
      this.events = NullCheck.notNull(in_events, "Events");
      this.uri = NullCheck.notNull(in_uri, "URI");
      this.stream = NullCheck.notNull(in_stream, "Stream");
      this.reader = SMFBDataStreamReader.create(in_uri, this.stream);
    }

    @Override
    public void parse()
    {
      try {
        this.events.onStart();

        final byte[] magic = new byte[MAGIC_NUMBER.length];
        this.reader.readBytes(Optional.of("Magic number"), magic);

        if (!magicNumberIsValid(magic)) {
          final String text =
            new StringBuilder(128)
              .append("Bad magic number.")
              .append(System.lineSeparator())
              .append("  Expected: ")
              .append(DatatypeConverter.printHexBinary(MAGIC_NUMBER))
              .append(System.lineSeparator())
              .append("  Received: ")
              .append(DatatypeConverter.printHexBinary(magic))
              .append(System.lineSeparator())
              .toString();
          this.events.onError(SMFParseError.of(
            LexicalPosition.of(0, 0, Optional.of(this.uri)),
            text,
            Optional.empty()));
          return;
        }

        final int major =
          Math.toIntExact(this.reader.readU32(Optional.of("Major version")));
        final int minor =
          Math.toIntExact(this.reader.readU32(Optional.of("Minor version")));
        final SMFFormatVersion version =
          SMFFormatVersion.of(major, minor);
        final Optional<SMFParserEventsHeaderType> events_header_opt =
          this.events.onVersionReceived(version);

        final BitSet state = new BitSet(8);
        if (events_header_opt.isPresent()) {
          final SMFParserEventsHeaderType events_header =
            events_header_opt.get();

          switch (major) {
            case 1: {
              this.parser =
                new SMFBv1Parser(
                  version,
                  state,
                  SMFBDataStreamReader.create(this.uri, this.stream),
                  events_header);
              this.parser.parse();
              break;
            }
            default: {
              throw new UnsupportedOperationException(notSupported(version));
            }
          }
        }

        if (state.get(VERTICES_REQUIRED) && !state.get(VERTICES_RECEIVED)) {
          this.events.onError(SMFParseError.of(
            LexicalPosition.of(0, 0, Optional.of(this.uri)),
            "A non-zero vertex count was specified, but no vertices were provided.",
            Optional.empty()));
        }

        if (state.get(TRIANGLES_REQUIRED) && !state.get(TRIANGLES_RECEIVED)) {
          this.events.onError(SMFParseError.of(
            LexicalPosition.of(0, 0, Optional.of(this.uri)),
            "A non-zero triangle count was specified, but no triangles were provided.",
            Optional.empty()));
        }

      } catch (final Exception e) {
        this.events.onError(SMFParseError.of(
          LexicalPosition.of(0, 0, Optional.of(this.uri)),
          e.getMessage(),
          Optional.of(e)));
      } finally {
        this.events.onFinish();
      }
    }

    @Override
    public void close()
      throws IOException
    {
      final SMFParserSequentialType p = this.parser;
      if (p != null) {
        this.parser = null;
        p.close();
      }
    }
  }
}
