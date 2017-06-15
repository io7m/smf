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
import javaslang.collection.Seq;
import javaslang.control.Validation;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.ServiceLoader;
import java.util.function.Supplier;

/**
 * A probe controller that uses {@link ServiceLoader}.
 */

public final class SMFVersionProbeControllerServiceLoader
  implements SMFVersionProbeControllerType
{
  /**
   * Construct a new probe controller.
   */

  public SMFVersionProbeControllerServiceLoader()
  {

  }

  @Override
  public Validation<Seq<SMFParseError>, SMFVersionProbed> probe(
    final Supplier<InputStream> streams)
  {
    final ServiceLoader<SMFVersionProbeProviderType> loader =
      ServiceLoader.load(SMFVersionProbeProviderType.class);

    final Collection<SMFVersionProbeProviderType> probes =
      new ArrayList<>(4);
    final Iterator<SMFVersionProbeProviderType> iter = loader.iterator();
    while (iter.hasNext()) {
      probes.add(iter.next());
    }

    return SMFVersionProbeControllers.probe(streams, probes);
  }
}
