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

package com.io7m.smfj.processing.api;

import com.io7m.jnull.NullCheck;
import javaslang.collection.SortedMap;
import javaslang.collection.TreeMap;

/**
 * An abstract implementation of the {@link SMFFilterCommandModuleProviderType}
 * interface.
 */

public abstract class SMFFilterCommandModuleProviderAbstract
  implements SMFFilterCommandModuleProviderType
{
  private final SortedMap<String, SMFFilterCommandModuleType> available;

  protected SMFFilterCommandModuleProviderAbstract(
    final SMFFilterCommandModuleType... in_modules)
  {
    NullCheck.notNull(in_modules, "Modules");

    SortedMap<String, SMFFilterCommandModuleType> m = TreeMap.empty();

    for (int index = 0; index < in_modules.length; ++index) {
      final SMFFilterCommandModuleType module = in_modules[index];
      if (m.containsKey(module.name())) {
        throw new IllegalArgumentException(
          "Duplicate published module: " + module.name());
      }
      m = m.put(module.name(), module);
    }

    this.available = m;
  }

  @Override
  public final SortedMap<String, SMFFilterCommandModuleType> available()
  {
    return this.available;
  }
}
