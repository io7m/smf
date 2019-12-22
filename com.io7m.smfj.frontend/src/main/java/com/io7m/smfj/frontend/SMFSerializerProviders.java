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

package com.io7m.smfj.frontend;

import com.io7m.smfj.serializer.api.SMFSerializerProviderType;
import java.util.List;
import java.util.Optional;
import java.util.ServiceLoader;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Convenience functions to locate serializer providers via {@link
 * ServiceLoader}.
 */

public final class SMFSerializerProviders
{
  private static final Logger LOG;

  static {
    LOG = LoggerFactory.getLogger(SMFSerializerProviders.class);
  }

  private SMFSerializerProviders()
  {

  }

  /**
   * Try to find a serializer provider. If a format name is not specified, the
   * format is inferred from the suffix of the given file.
   *
   * @param format_opt An optional format name.
   * @param file       A file name
   *
   * @return A serializer provider, if a suitable one is available
   */

  public static Optional<SMFSerializerProviderType> findSerializerProvider(
    final Optional<String> format_opt,
    final String file)
  {
    final ServiceLoader<SMFSerializerProviderType> loader =
      ServiceLoader.load(SMFSerializerProviderType.class);

    if (!format_opt.isPresent()) {
      LOG.debug("attempting to infer format from file suffix");
      final int index = file.lastIndexOf('.');
      if (index != -1) {
        final String suffix = file.substring(index + 1);

        final var providersWithFormat =
          loader.stream()
            .map(ServiceLoader.Provider::get)
            .filter(p -> p.serializerFormat().suffix().equals(suffix))
            .collect(Collectors.toList());

        return findBestProviderFromList(providersWithFormat);
      }

      LOG.error("File {} does not have a recognized suffix", file);
    } else {
      final String format = format_opt.get();
      LOG.debug("attempting to find provider for {}", format);

      final var providersWithFormat =
        loader.stream()
          .map(ServiceLoader.Provider::get)
          .filter(p -> p.serializerFormat().name().equals(format))
          .collect(Collectors.toList());

      return findBestProviderFromList(providersWithFormat);
    }

    return Optional.empty();
  }

  private static Optional<SMFSerializerProviderType> findBestProviderFromList(
    final List<SMFSerializerProviderType> providersWithFormat)
  {
    if (providersWithFormat.isEmpty()) {
      LOG.error("no provider has the specified format");
      return Optional.empty();
    }

    SMFSerializerProviderType providerBest = providersWithFormat.get(0);
    for (final var providerNow : providersWithFormat) {
      final var versionBest =
        providerBest.serializerSupportedVersions().last();
      final var versionNow =
        providerNow.serializerSupportedVersions().last();
      if (versionNow.compareTo(versionBest) > 0) {
        providerBest = providerNow;
      }
    }

    LOG.debug("using provider: {}", providerBest);
    return Optional.of(providerBest);
  }
}
