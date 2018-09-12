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

package com.io7m.smfj.probe.api;

import com.io7m.smfj.parser.api.SMFParseError;
import io.vavr.collection.Seq;
import io.vavr.control.Validation;
import org.osgi.annotation.versioning.ProviderType;

import java.io.InputStream;
import java.util.function.Supplier;

/**
 * The type of version probe controllers. A version probe controller iterates
 * through a set of version probes and tries to find one that can support
 * the probed file.
 */

@ProviderType
public interface SMFVersionProbeControllerType
{
  /**
   * Probe the given file.
   *
   * @param stream A supplier that is capable of repeatedly re-opening a file
   *
   * @return A probed version or a set of errors
   */

  Validation<Seq<SMFParseError>, SMFVersionProbed> probe(
    Supplier<InputStream> stream);
}
