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
import com.io7m.blackthorne.api.BTScalarElementHandler;
import com.io7m.junreachable.UnreachableCodeException;
import com.io7m.smfj.core.SMFMetadataValue;
import com.io7m.smfj.core.SMFSchemaIdentifier;
import com.io7m.smfj.parser.api.SMFParserEventsBodyType;
import com.io7m.smfj.parser.api.SMFParserEventsDataMetaType;
import java.util.Base64;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

public final class SMFXMetadata
  implements BTElementHandlerType<Object, SMFMetadataValue>
{
  private final SMFParserEventsBodyType events;
  private SMFSchemaIdentifier schemaId;
  private byte[] data;
  private Optional<SMFParserEventsDataMetaType> eventsMeta;

  public SMFXMetadata(
    final BTElementParsingContextType context,
    final SMFParserEventsBodyType inEvents)
  {
    this.events = Objects.requireNonNull(inEvents, "events");
    this.data = new byte[0];
  }

  private static byte[] decodeBase64(
    final BTElementParsingContextType ignored,
    final char[] characters,
    final int offset,
    final int length)
  {
    // CHECKSTYLE:OFF
    final var text = new String(characters, offset, length);
    // CHECKSTYLE:ON
    return Base64.getDecoder().decode(text);
  }

  @Override
  public Map<BTQualifiedName, BTElementHandlerConstructorType<?, ?>> onChildHandlersRequested(
    final BTElementParsingContextType context)
  {
    final BTQualifiedName base64 =
      BTQualifiedName.of(SMFX.namespaceURI2p0(), "Base64Data");

    return Map.ofEntries(
      Map.entry(
        BTQualifiedName.of(SMFX.namespaceURI2p0(), "SchemaIdentifier"),
        SMFXHeaderSchemaIdentifier::new
      ),
      Map.entry(
        base64,
        c -> new BTScalarElementHandler<>(base64, SMFXMetadata::decodeBase64)
      )
    );
  }

  @Override
  public void onChildValueProduced(
    final BTElementParsingContextType context,
    final Object result)
  {
    if (result instanceof SMFSchemaIdentifier) {
      this.schemaId = (SMFSchemaIdentifier) result;
      this.eventsMeta = this.events.onMeta(this.schemaId);
    } else if (result instanceof byte[]) {
      this.data = (byte[]) result;
      this.eventsMeta.ifPresent(e -> e.onMetaData(this.schemaId, this.data));
    } else {
      throw new UnreachableCodeException();
    }
  }

  @Override
  public SMFMetadataValue onElementFinished(
    final BTElementParsingContextType context)
  {
    return SMFMetadataValue.of(this.schemaId, this.data);
  }
}
