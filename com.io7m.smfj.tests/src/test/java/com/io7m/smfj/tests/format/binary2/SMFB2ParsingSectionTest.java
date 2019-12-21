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
import com.io7m.smfj.core.SMFWarningType;
import com.io7m.smfj.format.binary2.internal.SMFB2ParsingContexts;
import com.io7m.smfj.format.binary2.internal.SMFB2ParsingSectionHeader;
import com.io7m.smfj.parser.api.SMFParserEventsErrorType;
import com.io7m.smfj.tests.TestDirectories;
import java.io.EOFException;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class SMFB2ParsingSectionTest
  implements SMFParserEventsErrorType
{
  private static final Logger LOG =
    LoggerFactory.getLogger(SMFB2ParsingSectionTest.class);

  private Path directory;
  private BSSReaders readers;
  private ArrayList<SMFErrorType> errors;
  private ArrayList<SMFWarningType> warnings;
  private SMFB2ParsingContexts contexts;

  private static InputStream resource(
    final String name)
    throws FileNotFoundException
  {
    final var stream = SMFB2ParsingSectionTest.class.getResourceAsStream(
      "/com/io7m/smfj/tests/format/binary2/" + name);
    if (stream == null) {
      throw new FileNotFoundException(name);
    }
    return stream;
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
   * An empty file cannot be a section.
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
      try (var context = this.contexts.ofStream(file.toUri(), input, this)) {
        Assertions.assertThrows(EOFException.class, () -> {
          new SMFB2ParsingSectionHeader().parse(context);
        });
      }
    }
  }

  /**
   * A section is valid.
   *
   * @throws Exception On errors
   */

  @Test
  public void testOK()
    throws Exception
  {
    try (var input = resource("end0.smfb")) {
      try (var context = this.contexts.ofStream(
        URI.create("urn:file"),
        input,
        this)) {
        new SMFB2ParsingSectionHeader().parse(context);
      }
    }
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
