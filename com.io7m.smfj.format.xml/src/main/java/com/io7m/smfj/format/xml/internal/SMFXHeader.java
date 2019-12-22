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
import com.io7m.junreachable.UnreachableCodeException;
import com.io7m.smfj.core.SMFAttribute;
import com.io7m.smfj.core.SMFCoordinateSystem;
import com.io7m.smfj.core.SMFHeader;
import com.io7m.smfj.core.SMFSchemaIdentifier;
import com.io7m.smfj.core.SMFTriangles;
import com.io7m.smfj.parser.api.SMFParserEventsErrorType;
import java.nio.ByteOrder;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

public final class SMFXHeader
  implements BTElementHandlerType<Object, SMFHeader>
{
  private final SMFHeader.Builder builder;
  private final SMFParserEventsErrorType errors;

  public SMFXHeader(
    final BTElementParsingContextType context,
    final SMFParserEventsErrorType inErrors)
  {
    this.errors = Objects.requireNonNull(inErrors, "errors");
    this.builder = SMFHeader.builder();
  }

  @Override
  public Map<BTQualifiedName, BTElementHandlerConstructorType<?, ?>> onChildHandlersRequested(
    final BTElementParsingContextType context)
  {
    return Map.ofEntries(
      Map.entry(
        BTQualifiedName.of(SMFX.namespaceURI2p0(), "CoordinateSystem"),
        SMFXHeaderCoordinateSystem::new
      ),
      Map.entry(
        BTQualifiedName.of(SMFX.namespaceURI2p0(), "TriangleSpecification"),
        c -> new SMFXHeaderTriangles(this.errors, c)
      ),
      Map.entry(
        BTQualifiedName.of(SMFX.namespaceURI2p0(), "SchemaIdentifier"),
        SMFXHeaderSchemaIdentifier::new
      ),
      Map.entry(
        BTQualifiedName.of(SMFX.namespaceURI2p0(), "Attributes"),
        SMFXHeaderAttributes::new
      )
    );
  }

  @Override
  public void onElementStart(
    final BTElementParsingContextType context,
    final Attributes attributes)
    throws SAXException
  {
    try {
      this.builder.setVertexCount(
        Long.parseUnsignedLong(attributes.getValue("vertexCount")));

      final var endianness = attributes.getValue("endianness");
      if (endianness == null) {
        throw new IllegalArgumentException("No endianness specified");
      }

      switch (endianness) {
        case "BIG_ENDIAN": {
          this.builder.setDataByteOrder(ByteOrder.BIG_ENDIAN);
          break;
        }
        case "LITTLE_ENDIAN": {
          this.builder.setDataByteOrder(ByteOrder.LITTLE_ENDIAN);
          break;
        }
        default: {
          throw new IllegalArgumentException(
            String.format("Unrecognized endianness: %s", endianness));
        }
      }
    } catch (final Exception e) {
      throw context.parseException(e);
    }
  }

  @Override
  public void onChildValueProduced(
    final BTElementParsingContextType context,
    final Object result)
  {
    if (result instanceof SMFSchemaIdentifier) {
      this.builder.setSchemaIdentifier((SMFSchemaIdentifier) result);
    } else if (result instanceof SMFTriangles) {
      this.builder.setTriangles((SMFTriangles) result);
    } else if (result instanceof List) {
      this.builder.setAttributesInOrder((Iterable<? extends SMFAttribute>) result);
    } else if (result instanceof SMFCoordinateSystem) {
      this.builder.setCoordinateSystem((SMFCoordinateSystem) result);
    } else {
      throw new UnreachableCodeException();
    }
  }

  @Override
  public SMFHeader onElementFinished(
    final BTElementParsingContextType context)
  {
    return this.builder.build();
  }
}
