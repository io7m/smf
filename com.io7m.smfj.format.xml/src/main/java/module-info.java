/*
 * Copyright © 2020 Mark Raynsford <code@io7m.com> http://io7m.com
 *
 * Permission to use, copy, modify, and/or distribute this software for any
 * purpose with or without fee is hereby granted, provided that the above
 * copyright notice and this permission notice appear in all copies.
 *
 * THE SOFTWARE IS PROVIDED "AS IS" AND THE AUTHOR DISCLAIMS ALL WARRANTIES
 * WITH REGARD TO THIS SOFTWARE INCLUDING ALL IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS. IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY
 * SPECIAL, DIRECT, INDIRECT, OR CONSEQUENTIAL DAMAGES OR ANY DAMAGES
 * WHATSOEVER RESULTING FROM LOSS OF USE, DATA OR PROFITS, WHETHER IN AN
 * ACTION OF CONTRACT, NEGLIGENCE OR OTHER TORTIOUS ACTION, ARISING OUT OF OR
 * IN CONNECTION WITH THE USE OR PERFORMANCE OF THIS SOFTWARE.
 */

import com.io7m.smfj.format.xml.SMFFormatXML;

/**
 * The XML format.
 */

module com.io7m.smfj.format.xml
{
  requires static org.osgi.annotation.bundle;
  requires static org.osgi.annotation.versioning;
  requires static org.osgi.service.component.annotations;

  requires com.io7m.blackthorne.api;
  requires com.io7m.jaffirm.core;
  requires com.io7m.jcoords.core;
  requires com.io7m.jlexing.core;
  requires com.io7m.junreachable.core;
  requires com.io7m.jxe.core;
  requires com.io7m.smfj.core;
  requires com.io7m.smfj.format.support;
  requires com.io7m.smfj.parser.api;
  requires com.io7m.smfj.probe.api;
  requires com.io7m.smfj.serializer.api;
  requires java.xml;
  requires org.slf4j;

  provides com.io7m.smfj.parser.api.SMFParserProviderType
    with SMFFormatXML;
  provides com.io7m.smfj.probe.api.SMFVersionProbeProviderType
    with SMFFormatXML;
  provides com.io7m.smfj.serializer.api.SMFSerializerProviderType
    with SMFFormatXML;

  exports com.io7m.smfj.format.xml;
}