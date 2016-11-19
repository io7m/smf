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

import com.io7m.jaffirm.core.Invariants;
import com.io7m.jaffirm.core.Preconditions;
import com.io7m.jnull.NullCheck;
import com.io7m.junreachable.UnimplementedCodeException;
import com.io7m.smfj.core.SMFAttributeName;
import com.io7m.smfj.core.SMFFormatDescription;
import com.io7m.smfj.core.SMFFormatVersion;
import com.io7m.smfj.parser.api.SMFParserEventsType;
import com.io7m.smfj.parser.api.SMFParserProviderType;
import com.io7m.smfj.parser.api.SMFParserRandomAccessType;
import com.io7m.smfj.parser.api.SMFParserSequentialType;
import com.io7m.smfj.serializer.api.SMFSerializerProviderType;
import com.io7m.smfj.serializer.api.SMFSerializerType;
import javaslang.collection.SortedSet;
import javaslang.collection.TreeSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.bind.DatatypeConverter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.channels.FileChannel;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

/**
 * The implementation of the binary format.
 */

public final class SMFFormatBinary implements SMFParserProviderType,
  SMFSerializerProviderType
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
      b.setName("smfb");
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
    final Path in_path,
    final InputStream in_stream)
  {
    NullCheck.notNull(in_events, "Events");
    NullCheck.notNull(in_path, "Path");
    NullCheck.notNull(in_stream, "Stream");

    throw new UnimplementedCodeException();
  }

  @Override
  public SMFParserRandomAccessType parserCreateRandomAccess(
    final SMFParserEventsType in_events,
    final Path in_path,
    final FileChannel in_channel)
    throws UnsupportedOperationException
  {
    NullCheck.notNull(in_events, "Events");
    NullCheck.notNull(in_path, "Path");
    NullCheck.notNull(in_channel, "Channel");

    return new Parser(
      in_events,
      new SMFBDataReader(in_path, in_channel),
      new AtomicReference<>(SMFBAbstractParser.ParserState.STATE_INITIAL));
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
    final Path path,
    final OutputStream stream)
    throws UnsupportedOperationException
  {
    if (SUPPORTED_VERSIONS.contains(version)) {
      return new SMFBV1Serializer(version, path, stream);
    }

    throw new UnsupportedOperationException(
      String.format(
        "Version %d.%d is not supported",
        Integer.valueOf(version.major()),
        Integer.valueOf(version.minor())));
  }

  private static final class Parser extends SMFBAbstractParser
  {
    private Optional<SMFBAbstractParser> parser;

    Parser(
      final SMFParserEventsType in_events,
      final SMFBDataReader in_reader,
      final AtomicReference<ParserState> in_state)
    {
      super(in_events, in_reader, in_state);
      this.parser = Optional.empty();
    }

    @Override
    protected Logger log()
    {
      return LOG;
    }

    @Override
    public void parseHeader()
    {
      switch (super.state.get()) {
        case STATE_INITIAL: {
          super.events.onStart();

          try {
            this.parser = this.parseMagicNumberAndVersion();

            if (this.parser.isPresent()) {
              this.parser.get().parseHeader();
            } else {
              Invariants.checkInvariant(
                super.state.get(),
                super.state.get() == ParserState.STATE_FAILED,
                s -> String.format(
                  "State %s must be %s", s, ParserState.STATE_FAILED));
            }

          } catch (final Exception e) {
            super.fail(e.getMessage());
          } finally {
            if (super.state.get() == ParserState.STATE_FAILED) {
              super.events.onFinish();
            }
          }
          break;
        }
        case STATE_PARSED_HEADER: {
          throw new IllegalStateException("Header has already been parsed");
        }
        case STATE_FAILED: {
          throw new IllegalStateException("Parser has already failed");
        }
      }
    }

    @Override
    public void parseAttributeData(
      final SMFAttributeName name)
    {
      switch (super.state.get()) {
        case STATE_INITIAL: {
          throw new IllegalStateException("Header has not yet been parsed");
        }
        case STATE_PARSED_HEADER: {
          Preconditions.checkPrecondition(
            this.parser.isPresent(), "Parser must be present");

          this.parser.get().parseAttributeData(name);
          break;
        }
        case STATE_FAILED: {
          throw new IllegalStateException("Parser has already failed");
        }
      }
    }

    @Override
    public void parseTriangles()
    {
      switch (super.state.get()) {
        case STATE_INITIAL: {
          throw new IllegalStateException("Header has not yet been parsed");
        }
        case STATE_PARSED_HEADER: {
          Preconditions.checkPrecondition(
            this.parser.isPresent(), "Parser must be present");

          this.parser.get().parseTriangles();
          break;
        }
        case STATE_FAILED: {
          throw new IllegalStateException("Parser has already failed");
        }
      }
    }

    private Optional<SMFBAbstractParser> parseMagicNumberAndVersion()
    {
      try {
        final byte[] buffer8 = new byte[8];
        super.reader.readBytes(
          Optional.of("magic number"),
          buffer8,
          SMFBV1Offsets.offsetMagicNumber());
        if (magicNumberIsValid(buffer8)) {
          return this.parseVersion();
        }

        super.failExpectedGot(
          "Bad magic number.",
          DatatypeConverter.printHexBinary(MAGIC_NUMBER),
          DatatypeConverter.printHexBinary(buffer8));
        return Optional.empty();
      } catch (final IOException e) {
        super.fail("I/O error: " + e.getMessage());
        return Optional.empty();
      }
    }

    @Override
    public void close()
      throws IOException
    {
      LOG.debug("closing parser");
      super.events.onFinish();
    }

    private Optional<SMFBAbstractParser> parseVersion()
      throws IOException
    {
      final long major = super.reader.readUnsigned32(
        Optional.of("major version"), SMFBV1Offsets.offsetVersionMajor());
      final long minor = super.reader.readUnsigned32(
        Optional.of("minor version"), SMFBV1Offsets.offsetVersionMinor());

      final SMFFormatVersion version =
        SMFFormatVersion.of((int) major, (int) minor);

      super.events.onVersionReceived(version);

      switch ((int) major) {
        case 1: {
          LOG.debug("instantiating parser for 1.*");
          return Optional.of(
            new SMFBV1Parser(super.events, super.reader, super.state));
        }

        default: {
          LOG.debug("no parser for version {}", version);
          super.fail("Unsupported version");
          return Optional.empty();
        }
      }
    }
  }
}
