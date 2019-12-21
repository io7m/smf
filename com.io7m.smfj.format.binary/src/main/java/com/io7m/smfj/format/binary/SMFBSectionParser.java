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

package com.io7m.smfj.format.binary;

import com.io7m.smfj.core.SMFPartialLogged;
import com.io7m.smfj.parser.api.SMFParseError;
import java.io.IOException;
import java.util.Objects;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The default implementation of the {@link SMFBSectionParserType} interface.
 */

public final class SMFBSectionParser implements SMFBSectionParserType
{
  private static final Logger LOG;
  private static final Optional<String> SECTION_MAGIC_NUMBER =
    Optional.of("Section magic number");
  private static final Optional<String> SECTION_SIZE =
    Optional.of("Section size");

  static {
    LOG = LoggerFactory.getLogger(SMFBSectionParser.class);
  }

  private final SMFBDataStreamReaderType reader;
  private long section_next;

  /**
   * Construct a parser.
   *
   * @param in_reader A data stream reader
   */

  public SMFBSectionParser(
    final SMFBDataStreamReaderType in_reader)
  {
    this.reader = Objects.requireNonNull(in_reader, "Reader");
    this.section_next = 0L;
  }

  @Override
  public SMFPartialLogged<SMFBSection> parse()
  {
    try {
      this.seekToNextSection();

      final long position = this.reader.position();
      final long magic = this.reader.readU64(SECTION_MAGIC_NUMBER);
      final long size = this.reader.readU64(SECTION_SIZE);
      final long size_max = Math.addExact(size, 16L);
      this.section_next = Math.addExact(position, size_max);

      if (size_max % (long) SMFBSectionType.SECTION_ALIGNMENT != 0L) {
        final String text =
          new StringBuilder(128)
            .append("Section sizes must be multiples of ")
            .append(SMFBSectionType.SECTION_ALIGNMENT)
            .append(System.lineSeparator())
            .append("  Section:  ")
            .append(Long.toUnsignedString(magic, 16))
            .append(System.lineSeparator())
            .append("  Size:     ")
            .append(Long.toUnsignedString(size_max))
            .append(System.lineSeparator())
            .append("  Position: ")
            .append(Long.toUnsignedString(position))
            .append(System.lineSeparator())
            .toString();
        return SMFPartialLogged.failed(SMFParseError.of(
          this.reader.positionLexical(), text, Optional.empty()));
      }

      final SMFBSection section = SMFBSection.of(magic, size, position);
      if (LOG.isTraceEnabled()) {
        LOG.trace(
          "section: {} of size {}/{} at {}",
          Long.toUnsignedString(magic, 16),
          Long.toUnsignedString(size),
          Long.toUnsignedString(size_max),
          Long.toUnsignedString(position));
      }

      return SMFPartialLogged.succeeded(section);
    } catch (final IOException e) {
      return SMFPartialLogged.failed(SMFParseError.of(
        this.reader.positionLexical(), e.getMessage(), Optional.of(e)));
    }
  }

  private void seekToNextSection()
    throws IOException
  {
    final long position = this.reader.position();
    if (Long.compareUnsigned(position, this.section_next) < 0) {
      final long seek = Math.subtractExact(this.section_next, position);
      if (LOG.isTraceEnabled()) {
        LOG.trace(
          "seeking {} octets to next section",
          Long.toUnsignedString(seek));
      }
      this.reader.skip(seek);
    }
  }
}
