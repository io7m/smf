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

import com.io7m.smfj.core.SMFAttribute;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class SMFB2ParsingAttributeList implements SMFB2StructureParserType<List<SMFAttribute>>
{
  private static final Logger LOG =
    LoggerFactory.getLogger(SMFB2ParsingAttributeList.class);

  private final long attributeCount;

  SMFB2ParsingAttributeList(
    final long inAttributeCount)
  {
    this.attributeCount = inAttributeCount;
  }

  @Override
  public List<SMFAttribute> parse(final SMFB2ParsingContextType context)
    throws IOException
  {
    LOG.trace(
      "attempting to parse {} attributes",
      Long.valueOf(this.attributeCount));

    final var results = new ArrayList<SMFAttribute>(32);
    final var size = this.attributeCount * SMFB2ParsingAttribute.attributeSize();

    return context.withReader("attributes", size, reader -> {
      for (var attributeIndex = 0L;
           Long.compareUnsigned(attributeIndex, this.attributeCount) < 0;
           ++attributeIndex) {
        results.add(new SMFB2ParsingAttribute(attributeIndex).parse(context));
      }
      return results;
    });
  }
}
