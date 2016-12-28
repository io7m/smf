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

package com.io7m.smfj.processing;

import com.io7m.jnull.NullCheck;
import com.io7m.smfj.core.SMFAttribute;
import com.io7m.smfj.core.SMFAttributeName;
import javaslang.collection.List;
import javaslang.collection.Map;
import javaslang.control.Validation;

/**
 * A filter that checks the existence and type of an attribute.
 */

public final class SMFMemoryMeshFilterCheckType implements
  SMFMemoryMeshFilterType
{
  /**
   * The command name.
   */

  public static final String NAME = "check";

  private final SMFMemoryMeshFilterCheckTypeConfiguration config;

  private SMFMemoryMeshFilterCheckType(
    final SMFMemoryMeshFilterCheckTypeConfiguration in_config)
  {
    this.config = NullCheck.notNull(in_config, "Config");
  }

  /**
   * Create a new filter.
   *
   * @param in_config The configuration
   *
   * @return A new filter
   */

  public static SMFMemoryMeshFilterType create(
    final SMFMemoryMeshFilterCheckTypeConfiguration in_config)
  {
    return new SMFMemoryMeshFilterCheckType(in_config);
  }

  @Override
  public String name()
  {
    return NAME;
  }

  @Override
  public Validation<List<SMFProcessingError>, SMFMemoryMesh> filter(
    final SMFMemoryMesh m)
  {
    NullCheck.notNull(m, "Mesh");

    final Map<SMFAttributeName, SMFAttribute> by_name =
      m.header().attributesByName();

    if (by_name.containsKey(this.config.name())) {
      final SMFAttribute attr = by_name.get(this.config.name()).get();

      boolean size_ok = true;
      if (this.config.componentSize().isPresent()) {
        size_ok = this.config.componentSize().getAsInt() == attr.componentSizeBits();
      }
      boolean count_ok = true;
      if (this.config.componentCount().isPresent()) {
        count_ok = this.config.componentCount().getAsInt() == attr.componentCount();
      }
      boolean type_ok = true;
      if (this.config.componentType().isPresent()) {
        type_ok = this.config.componentType().get() == attr.componentType();
      }

      if (size_ok && count_ok && type_ok) {
        return Validation.valid(m);
      }

      return Validation.invalid(this.expectedGot(
        String.format(
          "An attribute '%s' with %d components of type %s with size %d",
          attr.name().value(),
          Integer.valueOf(attr.componentCount()),
          attr.componentType().getName(),
          Integer.valueOf(attr.componentSizeBits()))));
    }

    return Validation.invalid(this.expectedGot("nothing"));
  }

  private List<SMFProcessingError> expectedGot(
    final String text)
  {
    final StringBuilder sb = new StringBuilder(128);
    sb.append("Type checking failed.");
    sb.append(System.lineSeparator());
    sb.append("  Expected: An attribute '");
    sb.append(this.config.name().value());
    sb.append("'");
    if (this.config.componentCount().isPresent()) {
      sb.append(" with ");
      sb.append(this.config.componentCount().getAsInt());
      sb.append(" components");
    } else {
      sb.append(" with any number of components");
    }
    if (this.config.componentType().isPresent()) {
      sb.append(" of type ");
      sb.append(this.config.componentType().get().getName());
    } else {
      sb.append(" of any type");
    }
    if (this.config.componentSize().isPresent()) {
      sb.append(" with size ");
      sb.append(this.config.componentSize().getAsInt());
    } else {
      sb.append(" with any size");
    }
    sb.append(System.lineSeparator());
    sb.append("  Received: ");
    sb.append(text);
    sb.append(System.lineSeparator());
    return List.of(SMFProcessingError.of(sb.toString()));
  }

}
