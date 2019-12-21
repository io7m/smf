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

import com.io7m.smfj.core.SMFSchemaIdentifier;
import java.util.Objects;

/**
 * Parsed metadata.
 */

public final class SMFB2Metadata
{
  private final SMFSchemaIdentifier identifier;
  private final byte[] data;

  /**
   * Construct metadata.
   *
   * @param inIdentifier The schema identifier
   * @param inData       The data
   */

  public SMFB2Metadata(
    final SMFSchemaIdentifier inIdentifier,
    final byte[] inData)
  {
    this.identifier =
      Objects.requireNonNull(inIdentifier, "identifier");
    this.data =
      Objects.requireNonNull(inData, "data");
  }

  /**
   * @return The schema identifier
   */

  public SMFSchemaIdentifier identifier()
  {
    return this.identifier;
  }

  /**
   * @return The metadata
   */

  public byte[] data()
  {
    return this.data;
  }
}
