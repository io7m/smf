/*
 * Copyright © 2016 <code@io7m.com> http://io7m.com
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

package com.io7m.smfj.format.binary;

import com.io7m.jnull.NullCheck;
import com.io7m.smfj.core.SMFFormatVersion;
import com.io7m.smfj.serializer.api.SMFSerializerType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.OutputStream;
import java.nio.file.Path;

final class SMFBV1Serializer implements SMFSerializerType
{
  private static final Logger LOG;

  static {
    LOG = LoggerFactory.getLogger(SMFBV1Serializer.class);
  }

  private final SMFFormatVersion version;
  private final Path path;
  private final OutputStream stream;

  SMFBV1Serializer(
    final SMFFormatVersion in_version,
    final Path in_path,
    final OutputStream in_stream)
  {
    this.version = NullCheck.notNull(in_version, "Version");
    this.path = NullCheck.notNull(in_path, "Path");
    this.stream = NullCheck.notNull(in_stream, "Stream");
  }
}
