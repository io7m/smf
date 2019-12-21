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
import com.io7m.smfj.core.SMFComponentType;
import com.io7m.smfj.core.SMFErrorType;
import com.io7m.smfj.core.SMFHeader;
import com.io7m.smfj.core.SMFSchemaIdentifier;
import com.io7m.smfj.core.SMFSchemaName;
import com.io7m.smfj.core.SMFWarningType;
import com.io7m.smfj.format.binary2.internal.SMFB2ParsingContexts;
import com.io7m.smfj.format.binary2.internal.SMFB2ParsingSectionHeader;
import com.io7m.smfj.format.binary2.internal.SMFB2ParsingSectionSMF;
import com.io7m.smfj.parser.api.SMFParserEventsErrorType;
import com.io7m.smfj.tests.TestDirectories;
import java.io.EOFException;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.channels.SeekableByteChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Optional;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class SMFB2ParsingSectionSMFTest
  implements SMFParserEventsErrorType
{
  private static final Logger LOG =
    LoggerFactory.getLogger(SMFB2ParsingSectionSMFTest.class);

  private Path directory;
  private BSSReaders readers;
  private ArrayList<SMFErrorType> errors;
  private ArrayList<SMFWarningType> warnings;
  private SMFB2ParsingContexts contexts;

  private static void checkBasicSMF(
    final SMFHeader smf,
    final Optional<SMFSchemaIdentifier> schemaId)
  {
    Assertions.assertEquals(schemaId, smf.schemaIdentifier());
    Assertions.assertEquals(100L, smf.vertexCount());
    Assertions.assertEquals(32L, smf.triangles().triangleCount());
    Assertions.assertEquals(16, smf.triangles().triangleIndexSizeBits());

    Assertions.assertEquals(1, smf.attributesInOrder().size());

    {
      final var attr = smf.attributesInOrder().get(0);
      Assertions.assertEquals(
        "ffffeeeeddddccccbbbbaaaa9999888877776666555544443333222211110000",
        attr.name().value());
      Assertions.assertEquals(
        SMFComponentType.ELEMENT_TYPE_INTEGER_SIGNED,
        attr.componentType());
      Assertions.assertEquals(4, attr.componentCount());
      Assertions.assertEquals(32, attr.componentSizeBits());
    }
  }

  private static InputStream resource(
    final String name)
    throws FileNotFoundException
  {
    final var stream = SMFB2ParsingSectionSMFTest.class.getResourceAsStream(
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
        "aaaabbbbccccdddd0000111122223333444455556666777788889999eeeeffff"),
      2,
      1);
  }

  @BeforeEach
  public void testSetup()
    throws IOException
  {
    this.readers = new BSSReaders();
    this.contexts = new SMFB2ParsingContexts(this.readers);
    this.directory = TestDirectories.temporaryDirectory();
    this.errors = new ArrayList<>();
    this.warnings = new ArrayList<>();
  }

  /**
   * An empty file cannot be an smf section.
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
      try (var context =
             this.contexts.ofStream(URI.create("urn:file"), input, this)) {
        Assertions.assertThrows(EOFException.class, () -> {
          final var header = new SMFB2ParsingSectionHeader().parse(context);
          final var smf = new SMFB2ParsingSectionSMF(header).parse(context);
        });
      }
    }
  }

  /**
   * An smf section is valid.
   *
   * @throws Exception On errors
   */

  @Test
  public void testOKStream()
    throws Exception
  {
    try (var input = resource("smf_validBasic0.smfb")) {
      try (var context = this.contexts.ofStream(
        URI.create("urn:file"),
        input,
        this)) {
        final var header = new SMFB2ParsingSectionHeader().parse(context);
        final var smf = new SMFB2ParsingSectionSMF(header).parse(context);
        checkBasicSMF(smf.get(), Optional.of(schemaIdentifier()));
      }
    }

    Assertions.assertEquals(0, this.errors.size());
  }

  /**
   * An smf section is valid.
   *
   * @throws Exception On errors
   */

  @Test
  public void testOKChannel()
    throws Exception
  {
    try (var input = this.resourceChannel("smf_validBasic0.smfb")) {
      try (var context = this.contexts.ofChannel(
        URI.create("urn:file"),
        input,
        this)) {
        final var header = new SMFB2ParsingSectionHeader().parse(context);
        final var smf = new SMFB2ParsingSectionSMF(header).parse(context);
        checkBasicSMF(smf.get(), Optional.of(schemaIdentifier()));
      }
    }

    Assertions.assertEquals(0, this.errors.size());
  }

  /**
   * An smf section is valid.
   *
   * @throws Exception On errors
   */

  @Test
  public void testOKBigPadStream()
    throws Exception
  {
    try (var input = resource("smf_validBigPad0.smfb")) {
      try (var context = this.contexts.ofStream(
        URI.create("urn:file"),
        input,
        this)) {
        final var header = new SMFB2ParsingSectionHeader().parse(context);
        final var smf = new SMFB2ParsingSectionSMF(header).parse(context);
        checkBasicSMF(smf.get(), Optional.of(schemaIdentifier()));
      }
    }

    Assertions.assertEquals(0, this.errors.size());
  }

  /**
   * An smf section is valid.
   *
   * @throws Exception On errors
   */

  @Test
  public void testOKBigPadChannel()
    throws Exception
  {
    try (var input = this.resourceChannel("smf_validBigPad0.smfb")) {
      try (var context = this.contexts.ofChannel(
        URI.create("urn:file"),
        input,
        this)) {
        final var header = new SMFB2ParsingSectionHeader().parse(context);
        final var smf = new SMFB2ParsingSectionSMF(header).parse(context);
        checkBasicSMF(smf.get(), Optional.of(schemaIdentifier()));
      }
    }

    Assertions.assertEquals(0, this.errors.size());
  }

  /**
   * Declared fields size is too small.
   *
   * @throws Exception On errors
   */

  @Test
  public void testFieldShortStream()
    throws Exception
  {
    try (var input = resource("smf_invalidShortFields0.smfb")) {
      try (var context = this.contexts.ofStream(
        URI.create("urn:file"),
        input,
        this)) {
        final var header = new SMFB2ParsingSectionHeader().parse(context);
        final var smf = new SMFB2ParsingSectionSMF(header).parse(context);
        Assertions.assertEquals(Optional.empty(), smf);
      }
    }

    Assertions.assertEquals(1, this.errors.size());
    Assertions.assertTrue(this.errors.get(0).message().contains("too small"));
  }

  /**
   * Declared fields size is too small.
   *
   * @throws Exception On errors
   */

  @Test
  public void testFieldShortChannel()
    throws Exception
  {
    try (var input = this.resourceChannel("smf_invalidShortFields0.smfb")) {
      try (var context = this.contexts.ofChannel(
        URI.create("urn:file"),
        input,
        this)) {
        final var header = new SMFB2ParsingSectionHeader().parse(context);
        final var smf = new SMFB2ParsingSectionSMF(header).parse(context);
        Assertions.assertEquals(Optional.empty(), smf);
      }
    }

    Assertions.assertEquals(1, this.errors.size());
    Assertions.assertTrue(this.errors.get(0).message().contains("too small"));
  }

  /**
   * An smf section is valid.
   *
   * @throws Exception On errors
   */

  @Test
  public void testOKNoSchemaStream()
    throws Exception
  {
    try (var input = resource("smf_validNoSchema0.smfb")) {
      try (var context = this.contexts.ofStream(
        URI.create("urn:file"),
        input,
        this)) {
        final var header = new SMFB2ParsingSectionHeader().parse(context);
        final var smf = new SMFB2ParsingSectionSMF(header).parse(context);
        checkBasicSMF(smf.get(), Optional.empty());
      }
    }
  }

  /**
   * An smf section is valid.
   *
   * @throws Exception On errors
   */

  @Test
  public void testOKNoSchemaChannel()
    throws Exception
  {
    try (var input = this.resourceChannel("smf_validNoSchema0.smfb")) {
      try (var context = this.contexts.ofChannel(
        URI.create("urn:file"),
        input,
        this)) {
        final var header = new SMFB2ParsingSectionHeader().parse(context);
        final var smf = new SMFB2ParsingSectionSMF(header).parse(context);
        checkBasicSMF(smf.get(), Optional.empty());
      }
    }
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
    try (var input = resource("smf_invalidSchema0.smfb")) {
      try (var context = this.contexts.ofStream(
        URI.create("urn:file"),
        input,
        this)) {
        final var header = new SMFB2ParsingSectionHeader().parse(context);
        final var smf = new SMFB2ParsingSectionSMF(header).parse(context);
        Assertions.assertEquals(Optional.empty(), smf);
      }
    }

    Assertions.assertEquals(1, this.errors.size());
    Assertions.assertTrue(this.errors.get(0).message().contains(
      "Schema name 'AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA' is not valid"));
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
    try (var input = this.resourceChannel("smf_invalidSchema0.smfb")) {
      try (var context = this.contexts.ofChannel(
        URI.create("urn:file"),
        input,
        this)) {
        final var header = new SMFB2ParsingSectionHeader().parse(context);
        final var smf = new SMFB2ParsingSectionSMF(header).parse(context);
        Assertions.assertEquals(Optional.empty(), smf);
      }
    }

    Assertions.assertEquals(1, this.errors.size());
    Assertions.assertTrue(this.errors.get(0).message().contains(
      "Schema name 'AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA' is not valid"));
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
