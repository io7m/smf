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
import com.io7m.jcoords.core.conversion.CAxis;
import com.io7m.jcoords.core.conversion.CAxisSystem;
import com.io7m.smfj.core.SMFCoordinateSystem;
import com.io7m.smfj.core.SMFFaceWindingOrder;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

public final class SMFXHeaderCoordinateSystem
  implements BTElementHandlerType<Object, SMFCoordinateSystem>
{
  private final SMFCoordinateSystem.Builder builder;

  public SMFXHeaderCoordinateSystem(
    final BTElementParsingContextType context)
  {
    this.builder = SMFCoordinateSystem.builder();
  }

  @Override
  public void onElementStart(
    final BTElementParsingContextType context,
    final Attributes attributes)
    throws SAXException
  {
    try {
      final var right =
        CAxis.valueOf(attributes.getValue("right"));
      final var up =
        CAxis.valueOf(attributes.getValue("up"));
      final var forward =
        CAxis.valueOf(attributes.getValue("forward"));
      final var winding =
        SMFFaceWindingOrder.valueOf(attributes.getValue("windingOrder"));

      this.builder.setAxes(CAxisSystem.of(right, up, forward));
      this.builder.setWindingOrder(winding);
    } catch (final Exception e) {
      throw context.parseException(e);
    }
  }

  @Override
  public SMFCoordinateSystem onElementFinished(
    final BTElementParsingContextType context)
  {
    return this.builder.build();
  }
}
