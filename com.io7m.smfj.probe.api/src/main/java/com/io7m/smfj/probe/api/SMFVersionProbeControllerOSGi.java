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
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.osgi.service.component.annotations.ReferencePolicyOption;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;

/**
 * An OSGi probe controller component.
 */

@Component(
  immediate = true,
  service = SMFVersionProbeControllerType.class)
public final class SMFVersionProbeControllerOSGi
  implements SMFVersionProbeControllerType
{
  private final ArrayList<SMFVersionProbeProviderType> probes;

  /**
   * Construct an OSGi probe controller component.
   */

  public SMFVersionProbeControllerOSGi()
  {
    this.probes = new ArrayList<>(8);
  }

  /**
   * Register a version probe provider.
   *
   * @param provider The provider
   */

  @Reference(
    unbind = "onProbeProviderRemove",
    cardinality = ReferenceCardinality.MULTIPLE,
    policy = ReferencePolicy.DYNAMIC,
    policyOption = ReferencePolicyOption.GREEDY)
  public void onProbeProviderAdd(
    final SMFVersionProbeProviderType provider)
  {
    synchronized (this.probes) {
      this.probes.add(Objects.requireNonNull(provider, "Provider"));
    }
  }

  /**
   * Unregister a version probe provider.
   *
   * @param provider The provider
   */

  public void onProbeProviderRemove(
    final SMFVersionProbeProviderType provider)
  {
    synchronized (this.probes) {
      this.probes.remove(Objects.requireNonNull(provider, "Provider"));
    }
  }

  @Override
  public Validation<Seq<SMFParseError>, SMFVersionProbed> probe(
    final Supplier<InputStream> streams)
  {
    final List<SMFVersionProbeProviderType> probes_current;
    synchronized (this.probes) {
      probes_current = new ArrayList<>(this.probes);
    }

    return SMFVersionProbeControllers.probe(streams, probes_current);
  }
}
