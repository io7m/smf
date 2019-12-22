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
import com.io7m.smfj.core.SMFFormatVersion;
import com.io7m.smfj.core.SMFHeader;
import com.io7m.smfj.core.SMFSchemaIdentifier;
import com.io7m.smfj.core.SMFWarningType;
import com.io7m.smfj.format.binary2.internal.SMFB2ParsingContexts;
import com.io7m.smfj.format.binary2.internal.SMFB2ParsingFile;
import com.io7m.smfj.parser.api.SMFParserEventsBodyType;
import com.io7m.smfj.parser.api.SMFParserEventsDataAttributesNonInterleavedType;
import com.io7m.smfj.parser.api.SMFParserEventsDataMetaType;
import com.io7m.smfj.parser.api.SMFParserEventsDataTrianglesType;
import com.io7m.smfj.parser.api.SMFParserEventsHeaderType;
import com.io7m.smfj.parser.api.SMFParserEventsType;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class SMFB2Demo
{
  private static final Logger LOG =
    LoggerFactory.getLogger(SMFB2Demo.class);

  private SMFB2Demo()
  {

  }

  public static void main(final String[] args)
    throws IOException
  {
    final var file = Paths.get(args[0]);
    final var contexts = new SMFB2ParsingContexts(new BSSReaders());

    final var bodyReceiver =
      new SMFParserEventsBodyType()
      {
        @Override
        public void onError(final SMFErrorType e)
        {
          LOG.error("error: {}", e.fullMessage());
        }

        @Override
        public void onWarning(final SMFWarningType w)
        {
          LOG.warn("warning: {}", w.fullMessage());
        }

        @Override
        public Optional<SMFParserEventsDataMetaType> onMeta(
          final SMFSchemaIdentifier schema)
        {
          return Optional.empty();
        }

        @Override
        public Optional<SMFParserEventsDataAttributesNonInterleavedType> onAttributesNonInterleaved()
        {
          return Optional.empty();
        }

        @Override
        public Optional<SMFParserEventsDataTrianglesType> onTriangles()
        {
          return Optional.empty();
        }
      };

    final var headerReceiver =
      new SMFParserEventsHeaderType()
      {
        @Override
        public void onError(final SMFErrorType e)
        {
          LOG.error("error: {}", e.fullMessage());
        }

        @Override
        public void onWarning(final SMFWarningType w)
        {
          LOG.warn("warning: {}", w.fullMessage());
        }

        @Override
        public Optional<SMFParserEventsBodyType> onHeaderParsed(
          final SMFHeader header)
        {
          return Optional.of(bodyReceiver);
        }
      };

    final var receiver =
      new SMFParserEventsType()
      {
        @Override
        public void onError(final SMFErrorType e)
        {
          LOG.error("error: {}", e.fullMessage());
        }

        @Override
        public void onWarning(final SMFWarningType w)
        {
          LOG.warn("warning: {}", w.fullMessage());
        }

        @Override
        public void onStart()
        {

        }

        @Override
        public Optional<SMFParserEventsHeaderType> onVersionReceived(
          final SMFFormatVersion version)
        {
          return Optional.of(headerReceiver);
        }

        @Override
        public void onFinish()
        {

        }
      };

    try (var input = Files.newByteChannel(file)) {
      try (var context = contexts.ofChannel(file.toUri(), input, receiver)) {
        new SMFB2ParsingFile(receiver).parse(context);
      }
    }
  }
}
