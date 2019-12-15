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

package com.io7m.smfj.cmdline;

import com.beust.jcommander.Parameters;
import com.io7m.smfj.core.SMFFormatDescription;
import com.io7m.smfj.core.SMFFormatVersion;
import com.io7m.smfj.parser.api.SMFParserProviderType;
import com.io7m.smfj.serializer.api.SMFSerializerProviderType;
import io.vavr.collection.SortedSet;
import java.util.Iterator;
import java.util.ServiceLoader;

@Parameters(commandDescription = "List supported formats")
final class CommandFormats extends CommandRoot
{
  CommandFormats()
  {

  }

  @Override
  public Integer call()
    throws Exception
  {
    super.call();

    final String fmt_string = "%-6s : %-6s : %-32s : %-10s : %-6s : %s\n";

    System.out.printf(
      fmt_string,
      "# Name",
      "Suffix",
      "Mime type",
      "Version",
      "R/W",
      "Description");

    final ServiceLoader<SMFParserProviderType> parser_loader =
      ServiceLoader.load(SMFParserProviderType.class);
    final Iterator<SMFParserProviderType> parser_providers =
      parser_loader.iterator();

    while (parser_providers.hasNext()) {
      final SMFParserProviderType provider = parser_providers.next();
      final SMFFormatDescription format = provider.parserFormat();
      final SortedSet<SMFFormatVersion> versions = provider.parserSupportedVersions();
      versions.forEach(
        version ->
          System.out.printf(
            fmt_string,
            format.name(),
            format.suffix(),
            format.mimeType(),
            String.format(
              "%d.%d",
              Integer.valueOf(version.major()),
              Integer.valueOf(version.minor())),
            "read",
            format.description()));
    }

    final ServiceLoader<SMFSerializerProviderType> serializer_loader =
      ServiceLoader.load(SMFSerializerProviderType.class);
    final Iterator<SMFSerializerProviderType> serializer_providers =
      serializer_loader.iterator();

    while (serializer_providers.hasNext()) {
      final SMFSerializerProviderType provider = serializer_providers.next();
      final SMFFormatDescription format = provider.serializerFormat();
      final SortedSet<SMFFormatVersion> versions = provider.serializerSupportedVersions();
      versions.forEach(
        version ->
          System.out.printf(
            fmt_string,
            format.name(),
            format.suffix(),
            format.mimeType(),
            String.format(
              "%d.%d",
              Integer.valueOf(version.major()),
              Integer.valueOf(version.minor())),
            "write",
            format.description()));
    }

    return Integer.valueOf(0);
  }
}
