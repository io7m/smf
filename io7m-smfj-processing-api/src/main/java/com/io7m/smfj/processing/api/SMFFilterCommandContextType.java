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

import com.io7m.smfj.core.SMFImmutableStyleType;
import org.immutables.javaslang.encodings.JavaslangEncodingEnabled;
import org.immutables.value.Value;

import java.nio.file.Path;

/**
 * The context used during filtering.
 */

@Value.Immutable
@JavaslangEncodingEnabled
@SMFImmutableStyleType
public interface SMFFilterCommandContextType
{
  /**
   * The source root directory. If a filter causes files to be open, they must
   * be descendants of this directory.
   *
   * @return A source root directory
   */

  @Value.Parameter
  Path sourceRoot();

  /**
   * The current path. If a filter command specifies a relative path, the path
   * is resolved relative to the <i>current path</i>.
   *
   * @return The current path
   *
   * @see #resolvePath(Path)
   */

  @Value.Parameter
  Path currentPath();

  /**
   * Check preconditions for the type.
   */

  @Value.Check
  default void checkPreconditions()
  {
    if (!this.sourceRoot().isAbsolute()) {
      throw new IllegalArgumentException(
        "Source root must be an absolute path");
    }
  }

  /**
   * Resolve a given path.
   *
   * @param path The path
   *
   * @return A resolved path
   *
   * @throws IllegalArgumentException Iff the resolved path would fall outside
   *                                  of the source root
   */

  default Path resolvePath(
    final Path path)
    throws IllegalArgumentException
  {
    final Path result;
    if (path.isAbsolute()) {
      final Path relative = path.getRoot().relativize(path);
      result = this.sourceRoot().resolve(relative).toAbsolutePath();
    } else {
      result = this.currentPath().resolveSibling(path).toAbsolutePath();
    }

    if (result.startsWith(this.sourceRoot())) {
      return result;
    }

    final StringBuilder sb = new StringBuilder(128);
    sb.append("Resolved path is outside of the source root.");
    sb.append(System.lineSeparator());
    sb.append("  Source root:   ");
    sb.append(this.sourceRoot());
    sb.append(System.lineSeparator());
    sb.append("  Resolved path: ");
    sb.append(result);
    sb.append(System.lineSeparator());
    throw new IllegalArgumentException(sb.toString());
  }
}
