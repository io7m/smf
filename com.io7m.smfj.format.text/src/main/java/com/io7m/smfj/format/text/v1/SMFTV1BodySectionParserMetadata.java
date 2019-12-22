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

package com.io7m.smfj.format.text.v1;

import com.io7m.junreachable.UnreachableCodeException;
import com.io7m.smfj.core.SMFErrorType;
import com.io7m.smfj.core.SMFHeader;
import com.io7m.smfj.core.SMFSchemaIdentifier;
import com.io7m.smfj.core.SMFSchemaName;
import com.io7m.smfj.core.SMFWarningType;
import com.io7m.smfj.format.text.SMFBase64Lines;
import com.io7m.smfj.format.text.SMFTBodySectionParserType;
import com.io7m.smfj.format.text.SMFTLineReaderType;
import com.io7m.smfj.format.text.SMFTParsingStatus;
import com.io7m.smfj.parser.api.SMFParseError;
import com.io7m.smfj.parser.api.SMFParserEventsBodyType;
import com.io7m.smfj.parser.api.SMFParserEventsDataMetaType;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Supplier;

import static com.io7m.smfj.format.text.SMFTParsingStatus.FAILURE;
import static com.io7m.smfj.format.text.SMFTParsingStatus.SUCCESS;

/**
 * A parser for the "metadata" body section.
 */

public final class SMFTV1BodySectionParserMetadata
  implements SMFTBodySectionParserType
{
  /**
   * The syntax for the command.
   */

  public static final String SYNTAX =
    "metadata <schema> <version-major> <version-minor> <line-count>";

  private final SMFTLineReaderType reader;
  private final Supplier<SMFHeader> header_get;

  /**
   * Construct a parser.
   *
   * @param in_header_get A function that yields a header
   * @param in_reader     A line reader
   */

  public SMFTV1BodySectionParserMetadata(
    final Supplier<SMFHeader> in_header_get,
    final SMFTLineReaderType in_reader)
  {
    this.header_get = Objects.requireNonNull(in_header_get, "Header");
    this.reader = Objects.requireNonNull(in_reader, "Reader");
  }

  private static SMFParserEventsDataMetaType makeMetadataReceiver(
    final SMFParserEventsBodyType receiver,
    final SMFSchemaIdentifier schema)
  {
    final Optional<SMFParserEventsDataMetaType> r_opt = receiver.onMeta(schema);
    return r_opt.orElseGet(() -> new IgnoringMetaReceiver(receiver));
  }

  @Override
  public String name()
  {
    return "metadata";
  }

  @Override
  public SMFTParsingStatus parse(
    final SMFParserEventsBodyType receiver,
    final List<String> line_start)
    throws IOException
  {
    return this.parseMeta(line_start, receiver);
  }

  private SMFTParsingStatus parseMeta(
    final List<String> line,
    final SMFParserEventsBodyType receiver)
    throws IOException
  {
    if (line.size() == 5) {
      try {
        final SMFSchemaName name = SMFSchemaName.of(line.get(1));
        final int major = Integer.parseUnsignedInt(line.get(2));
        final int minor = Integer.parseUnsignedInt(line.get(3));
        final int lines = Integer.parseUnsignedInt(line.get(4));
        final SMFSchemaIdentifier schema =
          SMFSchemaIdentifier.of(name, major, minor);

        final SMFParserEventsDataMetaType meta_receiver =
          makeMetadataReceiver(receiver, schema);

        switch (this.readDataLines(meta_receiver, schema, lines)) {
          case SUCCESS:
            return SUCCESS;
          case FAILURE:
            return FAILURE;
        }
        throw new UnreachableCodeException();
      } catch (final NumberFormatException e) {
        receiver.onError(SMFTErrors.errorExpectedGotWithException(
          "Cannot parse meta command: " + e.getMessage(),
          SYNTAX,
          line,
          this.reader.position(),
          e));
        return FAILURE;
      }
    }

    receiver.onError(SMFTErrors.errorExpectedGot(
      "Cannot parse meta command.",
      SYNTAX,
      line,
      this.reader.position()));
    return FAILURE;
  }

  private SMFTParsingStatus readDataLines(
    final SMFParserEventsDataMetaType receiver,
    final SMFSchemaIdentifier schema,
    final int lines)
    throws IOException
  {
    final ArrayList<String> lines_saved = new ArrayList<>();
    for (int index = 0; Integer.compareUnsigned(index, lines) < 0; ++index) {
      final Optional<List<String>> data_line_opt = this.reader.line();
      if (!data_line_opt.isPresent()) {
        receiver.onError(SMFParseError.of(
          this.reader.position(),
          "Unexpected EOF",
          Optional.empty()));
        return FAILURE;
      }

      final List<String> data_line = data_line_opt.get();
      if (data_line.size() == 1) {
        lines_saved.add(data_line.get(0));
      } else {
        receiver.onError(SMFTErrors.errorExpectedGot(
          "Cannot parse base64 encoded data.",
          "Base64 encoded data",
          data_line,
          this.reader.position()));
        return FAILURE;
      }
    }

    try {
      final byte[] data = SMFBase64Lines.fromBase64Lines(lines_saved);
      receiver.onMetaData(schema, data);
      return SUCCESS;
    } catch (final Exception e) {
      receiver.onError(SMFTErrors.errorExpectedGotWithException(
        "Cannot parse base64 encoded data.",
        "Base64 encoded data",
        List.copyOf(lines_saved),
        this.reader.position(),
        e));
      return FAILURE;
    }
  }

  private static final class IgnoringMetaReceiver
    implements SMFParserEventsDataMetaType
  {
    private final SMFParserEventsBodyType receiver;

    IgnoringMetaReceiver(
      final SMFParserEventsBodyType in_receiver)
    {
      this.receiver = Objects.requireNonNull(in_receiver, "Receiver");
    }

    @Override
    public void onError(final SMFErrorType e)
    {
      this.receiver.onError(e);
    }

    @Override
    public void onWarning(final SMFWarningType w)
    {
      this.receiver.onWarning(w);
    }

    @Override
    public void onMetaData(
      final SMFSchemaIdentifier schema,
      final byte[] data)
    {

    }
  }
}
