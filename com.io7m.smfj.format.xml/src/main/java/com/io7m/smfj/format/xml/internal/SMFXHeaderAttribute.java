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
import com.io7m.smfj.core.SMFAttribute;
import com.io7m.smfj.core.SMFAttributeName;
import com.io7m.smfj.core.SMFComponentType;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

public final class SMFXHeaderAttribute
  implements BTElementHandlerType<Object, SMFAttribute>
{
  private final SMFAttribute.Builder builder;

  public SMFXHeaderAttribute(
    final BTElementParsingContextType context)
  {
    this.builder = SMFAttribute.builder();
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
      final var componentKind =
        SMFComponentType.valueOf(attributes.getValue("componentKind"));
      final var componentCount =
        Integer.parseUnsignedInt(attributes.getValue("componentCount"));
      final var componentSizeBits =
        Integer.parseUnsignedInt(attributes.getValue("componentSizeBits"));

      this.builder.setName(SMFAttributeName.of(name));
      this.builder.setComponentCount(componentCount);
      this.builder.setComponentSizeBits(componentSizeBits);
      this.builder.setComponentType(componentKind);
    } catch (final Exception e) {
      throw context.parseException(e);
    }
  }

  @Override
  public SMFAttribute onElementFinished(
    final BTElementParsingContextType context)
  {
    return this.builder.build();
  }
}
