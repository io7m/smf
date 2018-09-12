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

import java.util.Objects;
import com.io7m.smfj.core.SMFErrorType;
import com.io7m.smfj.core.SMFWarningType;

/**
 * A convenient implementation of the {@link SMFParserEventsDataAttributeValuesType}
 * interface that delegates warnings and errors but ignores data.
 */

public final class SMFParserEventsDataAttributeValuesIgnoringReceiver
  implements SMFParserEventsDataAttributeValuesType
{
  private final SMFParserEventsErrorType receiver;

  /**
   * Construct a receiver.
   *
   * @param in_receiver The error/warning receiver
   */

  public SMFParserEventsDataAttributeValuesIgnoringReceiver(
    final SMFParserEventsErrorType in_receiver)
  {
    this.receiver = Objects.requireNonNull(in_receiver, "Receiver");
  }

  @Override
  public void onDataAttributeValueIntegerSigned1(
    final long x)
  {

  }

  @Override
  public void onDataAttributeValueIntegerSigned2(
    final long x,
    final long y)
  {

  }

  @Override
  public void onDataAttributeValueIntegerSigned3(
    final long x,
    final long y,
    final long z)
  {

  }

  @Override
  public void onDataAttributeValueIntegerSigned4(
    final long x,
    final long y,
    final long z,
    final long w)
  {

  }

  @Override
  public void onDataAttributeValueIntegerUnsigned1(
    final long x)
  {

  }

  @Override
  public void onDataAttributeValueIntegerUnsigned2(
    final long x,
    final long y)
  {

  }

  @Override
  public void onDataAttributeValueIntegerUnsigned3(
    final long x,
    final long y,
    final long z)
  {

  }

  @Override
  public void onDataAttributeValueIntegerUnsigned4(
    final long x,
    final long y,
    final long z,
    final long w)
  {

  }

  @Override
  public void onDataAttributeValueFloat1(
    final double x)
  {

  }

  @Override
  public void onDataAttributeValueFloat2(
    final double x,
    final double y)
  {

  }

  @Override
  public void onDataAttributeValueFloat3(
    final double x,
    final double y,
    final double z)
  {

  }

  @Override
  public void onDataAttributeValueFloat4(
    final double x,
    final double y,
    final double z,
    final double w)
  {

  }

  @Override
  public void onDataAttributeValueFinish()
  {

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
}
