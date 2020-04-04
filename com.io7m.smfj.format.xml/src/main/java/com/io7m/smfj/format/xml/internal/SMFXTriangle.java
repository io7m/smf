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
import com.io7m.smfj.format.support.SMFTriangleTracker;
import com.io7m.smfj.parser.api.SMFParserEventsDataTrianglesType;
import java.util.Objects;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

public final class SMFXTriangle
  implements BTElementHandlerType<Object, SMFVoid>
{
  private final SMFTriangleTracker triangleTracker;
  private final SMFParserEventsDataTrianglesType triangles;

  public SMFXTriangle(
    final BTElementParsingContextType context,
    final SMFTriangleTracker inTriangleTracker,
    final SMFParserEventsDataTrianglesType inTriangles)
  {
    this.triangleTracker =
      Objects.requireNonNull(inTriangleTracker, "triangleTracker");
    this.triangles =
      Objects.requireNonNull(inTriangles, "triangles");
  }

  @Override
  public void onElementStart(
    final BTElementParsingContextType context,
    final Attributes attributes)
    throws SAXException
  {
    try {
      final var v0 =
        Long.parseUnsignedLong(attributes.getValue("v0"));
      final var v1 =
        Long.parseUnsignedLong(attributes.getValue("v1"));
      final var v2 =
        Long.parseUnsignedLong(attributes.getValue("v2"));
      this.triangleTracker.addTriangle(
        SMFXLexical.ofLocator(context.documentLocator()), v0, v1, v2);
      this.triangles.onDataTriangle(v0, v1, v2);
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
