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

import com.io7m.blackthorne.api.BTElementHandlerConstructorType;
import com.io7m.blackthorne.api.BTElementHandlerType;
import com.io7m.blackthorne.api.BTElementParsingContextType;
import com.io7m.blackthorne.api.BTQualifiedName;
import com.io7m.smfj.core.SMFVoid;
import com.io7m.smfj.format.support.SMFTriangleTracker;
import com.io7m.smfj.parser.api.SMFParserEventsBodyType;
import com.io7m.smfj.parser.api.SMFParserEventsDataTrianglesIgnoringReceiver;
import com.io7m.smfj.parser.api.SMFParserEventsDataTrianglesType;
import java.util.Map;
import java.util.Objects;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

public final class SMFXTriangles
  implements BTElementHandlerType<Object, SMFVoid>
{
  private final SMFParserEventsBodyType eventsBody;
  private final SMFTriangleTracker triangleTracker;
  private SMFParserEventsDataTrianglesType eventsTriangles;

  public SMFXTriangles(
    final BTElementParsingContextType context,
    final SMFTriangleTracker inTriangleTracker,
    final SMFParserEventsBodyType inBodyEvents)
  {
    this.triangleTracker =
      Objects.requireNonNull(inTriangleTracker, "triangleTracker");
    this.eventsBody =
      Objects.requireNonNull(inBodyEvents, "inBodyEvents");
  }

  @Override
  public Map<BTQualifiedName, BTElementHandlerConstructorType<?, ?>> onChildHandlersRequested(
    final BTElementParsingContextType context)
  {
    return Map.ofEntries(
      Map.entry(
        BTQualifiedName.of(SMFX.namespaceURI2p0(), "Triangle"),
        c -> new SMFXTriangle(
          context,
          this.triangleTracker,
          this.eventsTriangles)
      )
    );
  }

  @Override
  public void onElementStart(
    final BTElementParsingContextType context,
    final Attributes attributes)
    throws SAXException
  {
    this.eventsTriangles =
      this.eventsBody.onTriangles()
        .orElse(new SMFParserEventsDataTrianglesIgnoringReceiver(this.eventsBody));
  }

  @Override
  public SMFVoid onElementFinished(
    final BTElementParsingContextType context)
  {
    this.eventsTriangles.onDataTrianglesFinish();
    return SMFVoid.void_();
  }
}
