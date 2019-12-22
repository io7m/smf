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
import com.io7m.junreachable.UnimplementedCodeException;
import com.io7m.smfj.core.SMFAttributeName;
import com.io7m.smfj.core.SMFHeader;
import com.io7m.smfj.core.SMFVoid;
import com.io7m.smfj.parser.api.SMFParserEventsDataAttributeValuesIgnoringReceiver;
import com.io7m.smfj.parser.api.SMFParserEventsDataAttributeValuesType;
import com.io7m.smfj.parser.api.SMFParserEventsDataAttributesNonInterleavedType;
import java.util.Map;
import java.util.Objects;
import org.xml.sax.Attributes;

public final class SMFXAttributeData
  implements BTElementHandlerType<Object, SMFVoid>
{
  private final SMFParserEventsDataAttributesNonInterleavedType events;
  private final SMFHeader header;
  private SMFParserEventsDataAttributeValuesType eventsValues;

  public SMFXAttributeData(
    final BTElementParsingContextType inContext,
    final SMFHeader inHeader,
    final SMFParserEventsDataAttributesNonInterleavedType inEvents)
  {
    this.header =
      Objects.requireNonNull(inHeader, "header");
    this.events =
      Objects.requireNonNull(inEvents, "events");
  }

  @Override
  public Map<BTQualifiedName, BTElementHandlerConstructorType<?, ?>> onChildHandlersRequested(
    final BTElementParsingContextType context)
  {
    return Map.ofEntries(
      Map.entry(
        BTQualifiedName.of(SMFX.namespaceURI2p0(), "AttributeFloat4"),
        c -> new SMFXAttributeFloat4(context, this.eventsValues)
      ),
      Map.entry(
        BTQualifiedName.of(SMFX.namespaceURI2p0(), "AttributeFloat3"),
        c -> new SMFXAttributeFloat3(context, this.eventsValues)
      ),
      Map.entry(
        BTQualifiedName.of(SMFX.namespaceURI2p0(), "AttributeFloat2"),
        c -> new SMFXAttributeFloat2(context, this.eventsValues)
      ),
      Map.entry(
        BTQualifiedName.of(SMFX.namespaceURI2p0(), "AttributeFloat1"),
        c -> new SMFXAttributeFloat1(context, this.eventsValues)
      ),
      Map.entry(
        BTQualifiedName.of(SMFX.namespaceURI2p0(), "AttributeIntegerUnsigned4"),
        c -> new SMFXAttributeIntegerUnsigned4(context, this.eventsValues)
      ),
      Map.entry(
        BTQualifiedName.of(SMFX.namespaceURI2p0(), "AttributeIntegerUnsigned3"),
        c -> new SMFXAttributeIntegerUnsigned3(context, this.eventsValues)
      ),
      Map.entry(
        BTQualifiedName.of(SMFX.namespaceURI2p0(), "AttributeIntegerUnsigned2"),
        c -> new SMFXAttributeIntegerUnsigned2(context, this.eventsValues)
      ),
      Map.entry(
        BTQualifiedName.of(SMFX.namespaceURI2p0(), "AttributeIntegerUnsigned1"),
        c -> new SMFXAttributeIntegerUnsigned1(context, this.eventsValues)
      ),
      Map.entry(
        BTQualifiedName.of(SMFX.namespaceURI2p0(), "AttributeIntegerSigned4"),
        c -> new SMFXAttributeIntegerSigned4(context, this.eventsValues)
      ),
      Map.entry(
        BTQualifiedName.of(SMFX.namespaceURI2p0(), "AttributeIntegerSigned3"),
        c -> new SMFXAttributeIntegerSigned3(context, this.eventsValues)
      ),
      Map.entry(
        BTQualifiedName.of(SMFX.namespaceURI2p0(), "AttributeIntegerSigned2"),
        c -> new SMFXAttributeIntegerSigned2(context, this.eventsValues)
      ),
      Map.entry(
        BTQualifiedName.of(SMFX.namespaceURI2p0(), "AttributeIntegerSigned1"),
        c -> new SMFXAttributeIntegerSigned1(context, this.eventsValues)
      )
    );
  }

  @Override
  public void onElementStart(
    final BTElementParsingContextType context,
    final Attributes attributes)
  {
    final var attributeName =
      SMFAttributeName.of(attributes.getValue("name"));
    final var attribute =
      this.header.attributesByName().get(attributeName);

    if (attribute == null) {
      throw new UnimplementedCodeException();
    }

    this.eventsValues = this.events.onDataAttributeStart(attribute)
      .orElse(new SMFParserEventsDataAttributeValuesIgnoringReceiver(this.events));
  }

  @Override
  public SMFVoid onElementFinished(
    final BTElementParsingContextType context)
  {
    this.eventsValues.onDataAttributeValueFinish();
    return SMFVoid.void_();
  }
}
