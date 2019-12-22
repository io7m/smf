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


package com.io7m.smfj.format.binary2.internal;

import com.io7m.jbssio.api.BSSReaderProviderType;
import com.io7m.jbssio.api.BSSReaderRandomAccessType;
import com.io7m.jbssio.api.BSSReaderSequentialType;
import com.io7m.smfj.core.SMFErrorType;
import com.io7m.smfj.core.SMFWarningType;
import com.io7m.smfj.parser.api.SMFParserEventsErrorType;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.channels.SeekableByteChannel;
import java.util.ArrayDeque;
import java.util.Objects;
import java.util.OptionalLong;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class SMFB2ParsingContexts
{
  private static final Logger LOG =
    LoggerFactory.getLogger(SMFB2ParsingContexts.class);

  private final BSSReaderProviderType readers;

  public SMFB2ParsingContexts(
    final BSSReaderProviderType inReaders)
  {
    this.readers = Objects.requireNonNull(inReaders, "readers");
  }

  public SMFB2ParsingContextType ofStream(
    final URI uri,
    final InputStream stream,
    final SMFParserEventsErrorType errors)
    throws IOException
  {
    return ContextOfStream.create(this.readers, uri, stream, errors);
  }

  public SMFB2ParsingContextType ofChannel(
    final URI uri,
    final SeekableByteChannel channel,
    final SMFParserEventsErrorType errors)
    throws IOException
  {
    return ContextOfChannel.create(this.readers, uri, channel, errors);
  }

  private static final class ContextOfChannel implements SMFB2ParsingContextType
  {
    private final SMFParserEventsErrorType errors;
    private final ArrayDeque<BSSReaderRandomAccessType> readerStack;

    private ContextOfChannel(
      final SMFParserEventsErrorType inErrors)
    {
      this.errors = Objects.requireNonNull(inErrors, "errors");
      this.readerStack = new ArrayDeque<>();
    }

    static ContextOfChannel create(
      final BSSReaderProviderType readers,
      final URI uri,
      final SeekableByteChannel channel,
      final SMFParserEventsErrorType inErrors)
      throws IOException
    {
      final var context = new ContextOfChannel(inErrors);

      final var reader =
        readers.createReaderFromChannel(
          uri,
          channel,
          "root",
          OptionalLong.of(channel.size()));

      context.pushReader(reader);
      return context;
    }

    private void pushReader(
      final BSSReaderRandomAccessType reader)
    {
      this.readerStack.push(reader);
    }

    @Override
    public void publishError(
      final SMFErrorType error)
    {
      this.errors.onError(error);
    }

    @Override
    public void publishWarning(
      final SMFWarningType warning)
    {
      this.errors.onWarning(warning);
    }

    @Override
    public <T> T withReader(
      final String name,
      final long size,
      final WithReaderFunctionType<T> receiver)
      throws IOException
    {
      Objects.requireNonNull(name, "name");
      Objects.requireNonNull(receiver, "receiver");

      final BSSReaderRandomAccessType current = this.readerStack.peek();
      try (var newReader = current.createSubReaderAtBounded(name, 0L, size)) {
        this.readerStack.push(newReader);
        LOG.trace("created reader: {}", newReader);
        final var result = receiver.execute(newReader);
        return result;
      } finally {
        LOG.trace("skipping {}", Long.valueOf(size));
        this.readerStack.pop();
        final var last = this.readerStack.peek();
        if (last != null) {
          last.skip(size);
        }
      }
    }

    @Override
    public <T> T withReader(
      final String name,
      final WithReaderFunctionType<T> receiver)
      throws IOException
    {
      Objects.requireNonNull(name, "name");
      Objects.requireNonNull(receiver, "receiver");

      final BSSReaderRandomAccessType current = this.readerStack.peek();
      try (var newReader = current.createSubReaderAt(name, 0L)) {
        this.readerStack.push(newReader);
        LOG.trace("created reader: {}", newReader);
        final var result = receiver.execute(newReader);
        return result;
      } finally {
        this.readerStack.pop();
      }
    }

    @Override
    public void close()
    {

    }
  }

  private static final class ContextOfStream implements SMFB2ParsingContextType
  {
    private final SMFParserEventsErrorType errors;
    private final ArrayDeque<BSSReaderSequentialType> readerStack;

    private ContextOfStream(
      final SMFParserEventsErrorType inErrors)
    {
      this.errors = Objects.requireNonNull(inErrors, "errors");
      this.readerStack = new ArrayDeque<>();
    }

    static ContextOfStream create(
      final BSSReaderProviderType readers,
      final URI uri,
      final InputStream stream,
      final SMFParserEventsErrorType inErrors)
      throws IOException
    {
      final var context = new ContextOfStream(inErrors);

      final var reader =
        readers.createReaderFromStream(uri, stream, "root");

      context.pushReader(reader);
      return context;
    }

    private void pushReader(
      final BSSReaderSequentialType reader)
    {
      this.readerStack.push(reader);
    }

    @Override
    public void publishError(
      final SMFErrorType error)
    {
      this.errors.onError(error);
    }

    @Override
    public void publishWarning(
      final SMFWarningType warning)
    {
      this.errors.onWarning(warning);
    }

    @Override
    public <T> T withReader(
      final String name,
      final long size,
      final WithReaderFunctionType<T> receiver)
      throws IOException
    {
      Objects.requireNonNull(name, "name");
      Objects.requireNonNull(receiver, "receiver");

      final BSSReaderSequentialType current = this.readerStack.peek();
      final var start = current.offsetCurrentRelative();

      try (var newReader = current.createSubReaderBounded(name, size)) {
        this.readerStack.push(newReader);
        final var result = receiver.execute(newReader);
        final var expectedEnd = start + size;
        final var remaining =
          Math.max(0L, expectedEnd - current.offsetCurrentRelative());
        current.skip(remaining);
        return result;
      } finally {
        this.readerStack.pop();
      }
    }

    @Override
    public <T> T withReader(
      final String name,
      final WithReaderFunctionType<T> receiver)
      throws IOException
    {
      Objects.requireNonNull(name, "name");
      Objects.requireNonNull(receiver, "receiver");

      final BSSReaderSequentialType current = this.readerStack.peek();
      try (var newReader = current.createSubReader(name)) {
        this.readerStack.push(newReader);
        return receiver.execute(newReader);
      } finally {
        this.readerStack.pop();
      }
    }

    @Override
    public void close()
      throws IOException
    {

    }
  }
}
