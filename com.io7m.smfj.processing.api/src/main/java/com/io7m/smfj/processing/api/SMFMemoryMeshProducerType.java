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

package com.io7m.smfj.processing.api;

import com.io7m.smfj.core.SMFErrorType;
import com.io7m.smfj.core.SMFHeader;
import com.io7m.smfj.core.SMFWarningType;
import com.io7m.smfj.parser.api.SMFParserEventsType;
import javaslang.collection.List;

/**
 * The type of parser event listeners that produce values of type {@link SMFMemoryMeshType} as a
 * result.
 */

public interface SMFMemoryMeshProducerType extends SMFParserEventsType
{
  /**
   * @return The list of errors encountered, if any
   */

  List<SMFErrorType> errors();

  /**
   * @return The list of warnings encountered, if any
   */

  List<SMFWarningType> warnings();

  /**
   * The parsed header, if no parse errors were encountered.
   *
   * @return The parsed header
   *
   * @throws IllegalStateException If {@link #errors()} is non-empty
   */

  SMFHeader header()
    throws IllegalStateException;

  /**
   * The parsed mesh, if no parse errors were encountered.
   *
   * @return The parsed mesh
   *
   * @throws IllegalStateException If {@link #errors()} is non-empty
   */

  SMFMemoryMesh mesh()
    throws IllegalStateException;
}
