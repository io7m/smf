/*
 * Copyright © 2017 <code@io7m.com> http://io7m.com
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

package com.io7m.smfj.tests.format.binary;

import com.io7m.smfj.format.binary.SMFBDataStreamReader;
import com.io7m.smfj.format.binary.SMFBDataStreamReaderType;
import com.io7m.smfj.format.binary.SMFBSectionParser;
import com.io7m.smfj.format.binary.SMFBSectionParserType;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;

public final class SMFBSectionParserTest extends SMFBSectionParserContract
{
  @Override
  protected SMFBSectionParserType parser(
    final String name)
    throws IOException
  {
    final URL url = SMFBSectionParserContract.class.getResource(name);
    final InputStream stream = url.openStream();
    final SMFBDataStreamReaderType reader =
      SMFBDataStreamReader.create(
        URI.create("urn:" + name),
        stream);
    return new SMFBSectionParser(reader);
  }
}
