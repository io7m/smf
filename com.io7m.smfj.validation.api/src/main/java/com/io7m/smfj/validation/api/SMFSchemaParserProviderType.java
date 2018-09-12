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

package com.io7m.smfj.validation.api;

import io.vavr.collection.SortedSet;
import org.osgi.annotation.versioning.ProviderType;

import java.io.InputStream;
import java.net.URI;

/**
 * The type of schema parser providers.
 */

@ProviderType
public interface SMFSchemaParserProviderType
{
  /**
   * @return The set of supported schema language versions
   */

  SortedSet<SMFSchemaVersion> schemaSupportedVersions();

  /**
   * @param uri    The URI referred to by the input stream, for diagnostic messages
   * @param stream An input stream
   *
   * @return A new parser for the schema
   */

  SMFSchemaParserType schemaParserCreate(
    URI uri,
    InputStream stream);
}
