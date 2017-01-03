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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Iterator;
import java.util.ServiceLoader;

/**
 * <p>The default implementation of the {@link SMFFilterCommandModuleResolverType}
 * interface.</p>
 *
 * <p>The module uses {@link ServiceLoader} to locate module provider
 * implementations.</p>
 */

public final class SMFFilterCommandModuleResolver implements
  SMFFilterCommandModuleResolverType
{
  private static final Logger LOG;

  static {
    LOG = LoggerFactory.getLogger(SMFFilterCommandModuleResolver.class);
  }

  private final SortedMap<String, SMFFilterCommandModuleType> modules;

  private SMFFilterCommandModuleResolver(
    final SortedMap<String, SMFFilterCommandModuleType> in_modules)
  {
    this.modules = NullCheck.notNull(in_modules, "Modules");
  }

  /**
   * @return A module resolver
   */

  public static SMFFilterCommandModuleResolverType create()
  {
    SortedMap<String, SMFFilterCommandModuleType> modules =
      TreeMap.empty();

    final ServiceLoader<SMFFilterCommandModuleProviderType> providers =
      ServiceLoader.load(SMFFilterCommandModuleProviderType.class);

    final Iterator<SMFFilterCommandModuleProviderType> iter = providers.iterator();
    while (iter.hasNext()) {
      final SMFFilterCommandModuleProviderType provider = iter.next();
      final SortedMap<String, SMFFilterCommandModuleType> available =
        provider.available();

      for (final String name : available.keySet()) {
        final SMFFilterCommandModuleType module = available.get(name).get();
        if (modules.containsKey(name)) {
          LOG.warn("multiple modules with the same name: {}", name);
        }

        if (LOG.isDebugEnabled()) {
          LOG.debug("registered module {} via provider {}", name, provider);
        }
        modules = modules.put(name, module);
      }
    }

    return new SMFFilterCommandModuleResolver(modules);
  }

  @Override
  public SortedMap<String, SMFFilterCommandModuleType> available()
  {
    return this.modules;
  }
}
