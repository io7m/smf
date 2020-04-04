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
import com.io7m.smfj.core.SMFFormatVersion;
import com.io7m.smfj.core.SMFHeader;
import com.io7m.smfj.core.SMFVoid;
import com.io7m.smfj.format.support.SMFTriangleTracker;
import com.io7m.smfj.parser.api.SMFParseError;
import com.io7m.smfj.parser.api.SMFParserEventsBodyIgnoringReceiver;
import com.io7m.smfj.parser.api.SMFParserEventsBodyType;
import com.io7m.smfj.parser.api.SMFParserEventsHeaderIgnoringReceiver;
import com.io7m.smfj.parser.api.SMFParserEventsHeaderType;
import com.io7m.smfj.parser.api.SMFParserEventsType;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import org.xml.sax.Attributes;

public final class SMFX implements BTElementHandlerType<Object, Object>
{
  private final SMFParserEventsType events;
  private SMFHeader header;
  private SMFParserEventsHeaderType eventsHeader;
  private SMFParserEventsBodyType eventsBody;
  private SMFTriangleTracker triangleTracker;

  public SMFX(
    final BTElementParsingContextType context,
    final SMFParserEventsType inEvents)
  {
    this.events = Objects.requireNonNull(inEvents, "inEvents");
  }

  public static String namespaceURI2p0()
  {
    return "urn:com.io7m.smf:xml:2:0";
  }

  @Override
  public Map<BTQualifiedName, BTElementHandlerConstructorType<?, ?>> onChildHandlersRequested(
    final BTElementParsingContextType context)
  {
    return Map.ofEntries(
      Map.entry(
        BTQualifiedName.of(namespaceURI2p0(), "Header"),
        c -> new SMFXHeader(c, this.events)
      ),
      Map.entry(
        BTQualifiedName.of(namespaceURI2p0(), "VertexDataNonInterleaved"),
        c -> new SMFXVertexDataNonInterleaved(c, this.header, this.eventsBody)
      ),
      Map.entry(
        BTQualifiedName.of(namespaceURI2p0(), "Triangles"),
        c -> new SMFXTriangles(c, this.triangleTracker, this.eventsBody)
      ),
      Map.entry(
        BTQualifiedName.of(namespaceURI2p0(), "Metadata"),
        c -> new SMFXMetadata(c, this.eventsBody)
      )
    );
  }

  @Override
  public void onElementStart(
    final BTElementParsingContextType context,
    final Attributes attributes)
  {
    this.events.onStart();
    this.eventsHeader =
      this.events.onVersionReceived(SMFFormatVersion.of(2, 0))
        .orElse(new SMFParserEventsHeaderIgnoringReceiver(this.events));
  }

  @Override
  public SMFVoid onElementFinished(
    final BTElementParsingContextType context)
  {
    if (this.triangleTracker != null) {
      final var locator = context.documentLocator();
      final var lexical = SMFXLexical.ofLocator(locator);
      this.triangleTracker.check(lexical);
    }

    this.events.onFinish();
    return SMFVoid.void_();
  }

  @Override
  public void onChildValueProduced(
    final BTElementParsingContextType context,
    final Object result)
  {
    if (result instanceof SMFHeader) {
      this.header = (SMFHeader) result;
      this.triangleTracker =
        new SMFTriangleTracker(
          (lexical, message) -> {
            this.events.onError(
              SMFParseError.of(lexical, message, Optional.empty()));
          },
          this.header.vertexCount(),
          this.header.triangles().triangleCount());

      this.eventsBody =
        this.eventsHeader.onHeaderParsed(this.header)
          .orElse(new SMFParserEventsBodyIgnoringReceiver(this.events));
    }
  }
}
