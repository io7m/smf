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

import com.io7m.jlexing.core.LexicalPositions;
import com.io7m.junreachable.UnreachableCodeException;
import com.io7m.smfj.parser.api.SMFParseError;
import javaslang.collection.Seq;
import javaslang.collection.Vector;
import javaslang.control.Validation;

import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;
import java.util.function.Supplier;

import static com.io7m.smfj.parser.api.SMFParseErrors.errorWithMessage;

/**
 * Functions for implementing version probe controllers.
 */

final class SMFVersionProbeControllers
{
  private SMFVersionProbeControllers()
  {
    throw new UnreachableCodeException();
  }

  /**
   * Run the given version probes.
   *
   * @param streams A stream supplier
   * @param probes  A set of probe providers
   *
   * @return The result of probing
   */

  static Validation<Seq<SMFParseError>, SMFVersionProbed> probe(
    final Supplier<InputStream> streams,
    final Iterable<SMFVersionProbeProviderType> probes)
  {
    Vector<SMFParseError> errors = Vector.empty();
    for (SMFVersionProbeProviderType probe : probes) {
      try (InputStream stream = streams.get()) {
        final Validation<Seq<SMFParseError>, SMFVersionProbed> r =
          probe.probe(stream);
        if (r.isInvalid()) {
          errors = errors.appendAll(r.getError());
        } else {
          return r;
        }
      } catch (final IOException e) {
        errors = errors.append(SMFParseError.of(
          LexicalPositions.zero(), e.getMessage(), Optional.of(e)));
      }
    }

    if (errors.isEmpty()) {
      errors = errors.append(errorWithMessage("No format providers available."));
    }
    return Validation.invalid(errors);
  }
}
