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

package com.io7m.smfj.format.xml.internal;

import com.io7m.blackthorne.api.BTElementHandlerType;
import com.io7m.blackthorne.api.BTElementParsingContextType;
import com.io7m.smfj.core.SMFVoid;
import com.io7m.smfj.parser.api.SMFParserEventsDataAttributeValuesType;
import java.util.Objects;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

public final class SMFXAttributeFloat4
  implements BTElementHandlerType<Object, SMFVoid>
{
  private final SMFParserEventsDataAttributeValuesType eventsValues;

  public SMFXAttributeFloat4(
    final BTElementParsingContextType context,
    final SMFParserEventsDataAttributeValuesType inEventsValues)
  {
    this.eventsValues =
      Objects.requireNonNull(inEventsValues, "eventsValues");
  }

  @Override
  public void onElementStart(
    final BTElementParsingContextType context,
    final Attributes attributes)
    throws SAXException
  {
    try {
      this.eventsValues.onDataAttributeValueFloat4(
        Double.parseDouble(attributes.getValue("c0")),
        Double.parseDouble(attributes.getValue("c1")),
        Double.parseDouble(attributes.getValue("c2")),
        Double.parseDouble(attributes.getValue("c3"))
      );
    } catch (final Exception e) {
      throw context.parseException(e);
    }
  }

  @Override
  public SMFVoid onElementFinished(
    final BTElementParsingContextType context)
  {
    return SMFVoid.void_();
  }
}
