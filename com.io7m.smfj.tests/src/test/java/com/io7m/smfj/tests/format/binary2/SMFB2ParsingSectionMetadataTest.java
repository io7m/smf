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

package com.io7m.smfj.tests.format.binary2;

import com.io7m.jbssio.vanilla.BSSReaders;
import com.io7m.smfj.core.SMFErrorType;
import com.io7m.smfj.core.SMFMetadataValue;
import com.io7m.smfj.core.SMFSchemaIdentifier;
import com.io7m.smfj.core.SMFSchemaName;
import com.io7m.smfj.core.SMFWarningType;
import com.io7m.smfj.format.binary2.internal.SMFB2ParsingContexts;
import com.io7m.smfj.format.binary2.internal.SMFB2ParsingSectionHeader;
import com.io7m.smfj.format.binary2.internal.SMFB2ParsingSectionMetadata;
import com.io7m.smfj.parser.api.SMFParserEventsDataMetaOptionalSupplierType;
import com.io7m.smfj.parser.api.SMFParserEventsDataMetaType;
import com.io7m.smfj.parser.api.SMFParserEventsErrorType;
import com.io7m.smfj.tests.TestDirectories;
import java.io.EOFException;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.channels.SeekableByteChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Optional;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class SMFB2ParsingSectionMetadataTest
  implements SMFParserEventsErrorType
{
  private static final Logger LOG =
    LoggerFactory.getLogger(SMFB2ParsingSectionMetadataTest.class);

  private Path directory;
  private BSSReaders readers;
  private ArrayList<SMFErrorType> errors;
  private ArrayList<SMFWarningType> warnings;
  private SMFB2ParsingContexts contexts;
  private SMFParserEventsDataMetaOptionalSupplierType acceptAll;
  private SMFParserEventsDataMetaType metaReceiver;

  private static InputStream resource(
    final String name)
    throws FileNotFoundException
  {
    final var stream = SMFB2ParsingSectionMetadataTest.class.getResourceAsStream(
      "/com/io7m/smfj/tests/format/binary2/" + name);
    if (stream == null) {
      throw new FileNotFoundException(name);
    }
    return stream;
  }

  private static SMFSchemaIdentifier schemaIdentifier()
  {
    return SMFSchemaIdentifier.of(
      SMFSchemaName.of(
        "ffffeeeeddddccccbbbbaaaa0000111122223333444455556666777788889999"),
      2,
      1);
  }

  private static void checkMeta(final SMFMetadataValue meta)
  {
    Assertions.assertEquals(schemaIdentifier(), meta.schemaId());
    Assertions.assertEquals(
      "Hello world.", new String(meta.data(), StandardCharsets.UTF_8));
  }

  @BeforeEach
  public void testSetup()
    throws IOException
  {
    this.readers = new BSSReaders();
    this.contexts = new SMFB2ParsingContexts(this.readers);
    this.directory = TestDirectories.temporaryDirectory();
    this.metaReceiver = Mockito.mock(SMFParserEventsDataMetaType.class);
    this.acceptAll = schema -> Optional.of(this.metaReceiver);
    this.errors = new ArrayList<>();
    this.warnings = new ArrayList<>();
  }

  /**
   * An empty file cannot be a metadata section.
   *
   * @throws Exception On errors
   */

  @Test
  public void testEmpty()
    throws Exception
  {
    final Path file = this.directory.resolve("out.smfb");
    Files.write(file, new byte[]{});

    try (var input = Files.newInputStream(file)) {
      try (var context = this.contexts.ofStream(
        URI.create("urn:file"), input, this)) {
        Assertions.assertThrows(EOFException.class, () -> {
          final var header =
            new SMFB2ParsingSectionHeader().parse(context);
          final var meta =
            new SMFB2ParsingSectionMetadata(this.acceptAll, header)
              .parse(context);

          Mockito.verify(this.metaReceiver, Mockito.times(0))
            .onMetaData(Mockito.any(), Mockito.any());
        });
      }
    }
  }

  /**
   * A metadata section is valid.
   *
   * @throws Exception On errors
   */

  @Test
  public void testOKStream()
    throws Exception
  {
    try (var input = resource("meta0.smfb")) {
      try (var context = this.contexts.ofStream(
        URI.create("urn:file"), input, this)) {
        final var header =
          new SMFB2ParsingSectionHeader().parse(context);
        final var meta =
          new SMFB2ParsingSectionMetadata(this.acceptAll, header)
            .parse(context);
        checkMeta(meta.get());

        Mockito.verify(this.metaReceiver, Mockito.times(1))
          .onMetaData(meta.get().schemaId(), meta.get().data());
      }
    }

    Assertions.assertEquals(0, this.errors.size());
  }

  /**
   * A metadata section is valid.
   *
   * @throws Exception On errors
   */

  @Test
  public void testOKChannel()
    throws Exception
  {
    try (var input = this.resourceChannel("meta0.smfb")) {
      try (var context = this.contexts.ofChannel(
        URI.create("urn:file"), input, this)) {
        final var header =
          new SMFB2ParsingSectionHeader().parse(context);
        final var meta =
          new SMFB2ParsingSectionMetadata(this.acceptAll, header)
            .parse(context);
        checkMeta(meta.get());

        Mockito.verify(this.metaReceiver, Mockito.times(1))
          .onMetaData(meta.get().schemaId(), meta.get().data());
      }
    }

    Assertions.assertEquals(0, this.errors.size());
  }

  /**
   * Invalid schema name.
   *
   * @throws Exception On errors
   */

  @Test
  public void testInvalidSchemaStream()
    throws Exception
  {
    try (var input = resource("meta_invalidSchema0.smfb")) {
      try (var context = this.contexts.ofStream(
        URI.create("urn:file"), input, this)) {
        final var header =
          new SMFB2ParsingSectionHeader().parse(context);
        final var meta =
          new SMFB2ParsingSectionMetadata(this.acceptAll, header)
            .parse(context);

        Mockito.verify(this.metaReceiver, Mockito.times(0))
          .onMetaData(Mockito.any(), Mockito.any());
      }
    }

    Assertions.assertEquals(2, this.errors.size());
    Assertions.assertTrue(this.errors.get(0).message().contains(
      "Schema name 'AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA' is not valid"));
    Assertions.assertTrue(this.errors.get(1).message().contains(
      "A valid schema identifier must be specified for metadata"));
  }

  /**
   * Invalid schema name.
   *
   * @throws Exception On errors
   */

  @Test
  public void testInvalidSchemaChannel()
    throws Exception
  {
    try (var input = this.resourceChannel("meta_invalidSchema0.smfb")) {
      try (var context = this.contexts.ofChannel(
        URI.create("urn:file"), input, this)) {
        final var header =
          new SMFB2ParsingSectionHeader().parse(context);
        final var meta =
          new SMFB2ParsingSectionMetadata(this.acceptAll, header)
            .parse(context);

        Mockito.verify(this.metaReceiver, Mockito.times(0))
          .onMetaData(Mockito.any(), Mockito.any());
      }
    }

    Assertions.assertEquals(2, this.errors.size());
    Assertions.assertTrue(this.errors.get(0).message().contains(
      "Schema name 'AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA' is not valid"));
    Assertions.assertTrue(this.errors.get(1).message().contains(
      "A valid schema identifier must be specified for metadata"));
  }

  /**
   * Invalid data size.
   *
   * @throws Exception On errors
   */

  @Test
  public void testInvalidDataSize0Stream()
    throws Exception
  {
    try (var input = resource("meta_tooLarge0.smfb")) {
      try (var context = this.contexts.ofStream(
        URI.create("urn:file"), input, this)) {
        final var header =
          new SMFB2ParsingSectionHeader().parse(context);
        final var meta =
          new SMFB2ParsingSectionMetadata(this.acceptAll, header)
            .parse(context);
      }
    }

    Assertions.assertEquals(1, this.errors.size());
    Assertions.assertTrue(this.errors.get(0).message().contains(
      "Metadata size is specified as 32 but only 16 bytes are remaining"));
  }

  /**
   * Invalid data size.
   *
   * @throws Exception On errors
   */

  @Test
  public void testInvalidDataSize0Channel()
    throws Exception
  {
    try (var input = this.resourceChannel("meta_tooLarge0.smfb")) {
      try (var context = this.contexts.ofChannel(
        URI.create("urn:file"), input, this)) {
        final var header =
          new SMFB2ParsingSectionHeader().parse(context);
        final var meta =
          new SMFB2ParsingSectionMetadata(this.acceptAll, header)
            .parse(context);
      }
    }

    Assertions.assertEquals(1, this.errors.size());
    Assertions.assertTrue(this.errors.get(0).message().contains(
      "Metadata size is specified as 32 but only 16 bytes are remaining"));
  }

  /**
   * Invalid data size.
   *
   * @throws Exception On errors
   */

  @Test
  public void testInvalidDataSize1Stream()
    throws Exception
  {
    try (var input = resource("meta_tooLarge1.smfb")) {
      try (var context = this.contexts.ofStream(
        URI.create("urn:file"), input, this)) {
        final var header =
          new SMFB2ParsingSectionHeader().parse(context);

        Assertions.assertThrows(IOException.class, () -> {
          final var meta =
            new SMFB2ParsingSectionMetadata(this.acceptAll, header)
              .parse(context);
        });
      }
    }

    Assertions.assertEquals(1, this.errors.size());
    Assertions.assertTrue(this.errors.get(0).message().contains(
      "Metadata size is specified as 2147483600 but only 2147483520 bytes are remaining"));
  }

  /**
   * Invalid data size.
   *
   * @throws Exception On errors
   */

  @Test
  public void testInvalidDataSize1Channel()
    throws Exception
  {
    try (var input = this.resourceChannel("meta_tooLarge1.smfb")) {
      try (var context = this.contexts.ofChannel(
        URI.create("urn:file"), input, this)) {
        final var header =
          new SMFB2ParsingSectionHeader().parse(context);

        Assertions.assertThrows(IllegalArgumentException.class, () -> {
          new SMFB2ParsingSectionMetadata(this.acceptAll, header)
            .parse(context);
        });
      }
    }
  }

  private SeekableByteChannel resourceChannel(
    final String name)
    throws IOException
  {
    final var outputFile = this.directory.resolve(name);
    Files.copy(resource(name), outputFile);
    return Files.newByteChannel(outputFile);
  }

  @Override
  public void onError(final SMFErrorType error)
  {
    LOG.debug("{}", error);
    this.errors.add(error);
  }

  @Override
  public void onWarning(final SMFWarningType warning)
  {
    LOG.debug("{}", warning);
    this.warnings.add(warning);
  }
}
