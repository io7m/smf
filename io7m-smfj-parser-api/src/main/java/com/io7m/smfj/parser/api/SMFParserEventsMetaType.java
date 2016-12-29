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

package com.io7m.smfj.parser.api;

/**
 * Events related to the parsing of metadata.
 */

public interface SMFParserEventsMetaType extends SMFParserEventsErrorType
{
  /**
   * Metadata was encountered.
   *
   * @param vendor The vendor ID
   * @param schema The schema ID
   * @param length The length in octets of the data
   *
   * @return {@code true} iff the data should be delivered via the {@link
   * #onMetaData(long, long, byte[])} method
   */

  boolean onMeta(
    long vendor,
    long schema,
    long length);

  /**
   * Metadata is ready for delivery.
   *
   * @param vendor The vendor ID
   * @param schema The schema ID
   * @param data   The data
   */

  void onMetaData(
    long vendor,
    long schema,
    byte[] data);
}
