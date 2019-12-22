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
import com.io7m.smfj.core.SMFSchemaIdentifier;
import com.io7m.smfj.core.SMFSchemaName;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

public final class SMFXHeaderSchemaIdentifier
  implements BTElementHandlerType<Object, SMFSchemaIdentifier>
{
  private final SMFSchemaIdentifier.Builder builder;

  public SMFXHeaderSchemaIdentifier(
    final BTElementParsingContextType context)
  {
    this.builder = SMFSchemaIdentifier.builder();
  }

  @Override
  public void onElementStart(
    final BTElementParsingContextType context,
    final Attributes attributes)
    throws SAXException
  {
    try {
      final var name =
        attributes.getValue("name");
      final var versionMajor =
        Integer.parseUnsignedInt(attributes.getValue("versionMajor"));
      final var versionMinor =
        Integer.parseUnsignedInt(attributes.getValue("versionMinor"));

      this.builder.setName(SMFSchemaName.of(name));
      this.builder.setVersionMajor(versionMajor);
      this.builder.setVersionMinor(versionMinor);
    } catch (final Exception e) {
      throw context.parseException(e);
    }
  }

  @Override
  public SMFSchemaIdentifier onElementFinished(
    final BTElementParsingContextType context)
  {
    return this.builder.build();
  }
}
