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

package com.io7m.smfj.parser.api;

import com.io7m.smfj.core.SMFAttribute;
import com.io7m.smfj.core.SMFErrorType;
import com.io7m.smfj.core.SMFWarningType;
import java.util.Objects;
import java.util.Optional;

/**
 * A convenient implementation of the {@link SMFParserEventsDataAttributeValuesType}
 * interface that delegates warnings and errors but ignores data.
 */

public final class SMFParserEventsDataAttributesNonInterleavedIgnoringReceiver
  implements SMFParserEventsDataAttributesNonInterleavedType
{
  private final SMFParserEventsErrorType receiver;

  /**
   * Construct a receiver.
   *
   * @param in_receiver The error/warning receiver
   */

  public SMFParserEventsDataAttributesNonInterleavedIgnoringReceiver(
    final SMFParserEventsErrorType in_receiver)
  {
    this.receiver = Objects.requireNonNull(in_receiver, "Receiver");
  }

  @Override
  public void onWarning(
    final SMFWarningType w)
  {
    this.receiver.onWarning(w);
  }

  @Override
  public void onError(
    final SMFErrorType e)
  {
    this.receiver.onError(e);
  }

  @Override
  public Optional<SMFParserEventsDataAttributeValuesType> onDataAttributeStart(
    final SMFAttribute attribute)
  {
    return Optional.of(
      new SMFParserEventsDataAttributeValuesIgnoringReceiver(this.receiver));
  }

  @Override
  public void onDataAttributesNonInterleavedFinish()
  {

  }
}
