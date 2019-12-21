/*
 * Copyright © 2019 Mark Raynsford <code@io7m.com> http://io7m.com
 *
 * Permission to use, copy, modify, and/or distribute this software for
 * any purpose with or without fee is hereby granted, provided that the
 * above copyright notice and this permission notice appear in all copies.
 *
 * THE SOFTWARE IS PROVIDED "AS IS" AND THE AUTHOR DISCLAIMS ALL
 * WARRANTIES WITH REGARD TO THIS SOFTWARE INCLUDING ALL IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS. IN NO EVENT SHALL THE AUTHOR
 * BE LIABLE FOR ANY SPECIAL, DIRECT, INDIRECT, OR CONSEQUENTIAL DAMAGES
 * OR ANY DAMAGES WHATSOEVER RESULTING FROM LOSS OF USE, DATA OR PROFITS,
 * WHETHER IN AN ACTION OF CONTRACT, NEGLIGENCE OR OTHER TORTIOUS ACTION,
 * ARISING OUT OF OR IN CONNECTION WITH THE USE OR PERFORMANCE OF THIS
 * SOFTWARE.
 */

package com.io7m.smfj.format.support;

import com.io7m.jlexing.core.LexicalPosition;
import java.net.URI;
import java.util.Objects;

/**
 * A tracker of triangle counts and indices.
 */

public final class SMFTriangleTracker
{
  private final long expectedVertexCount;
  private final long expectedTriangleCount;
  private final ErrorReceiverType errors;
  private long triangleCount;

  /**
   * Construct a tracker.
   *
   * @param inErrors        A receiver of errors
   * @param inVertexCount   The expected number of vertices
   * @param inTriangleCount The expected number of triangles
   */

  public SMFTriangleTracker(
    final ErrorReceiverType inErrors,
    final long inVertexCount,
    final long inTriangleCount)
  {
    this.errors = Objects.requireNonNull(inErrors, "inErrors");
    this.expectedVertexCount = inVertexCount;
    this.expectedTriangleCount = inTriangleCount;
    this.triangleCount = 0L;
  }

  /**
   * Add a triangle.
   *
   * @param lexical The triangle declaration position
   * @param v0      The index of vertex 0
   * @param v1      The index of vertex 1
   * @param v2      The index of vertex 2
   */

  public void addTriangle(
    final LexicalPosition<URI> lexical,
    final long v0,
    final long v1,
    final long v2)
  {
    Objects.requireNonNull(lexical, "lexical");

    this.checkTriangleVertex(lexical, 0, v0);
    this.checkTriangleVertex(lexical, 1, v1);
    this.checkTriangleVertex(lexical, 2, v2);
    this.triangleCount = Math.addExact(this.triangleCount, 1L);
  }

  /**
   * Check that all invariants hold. If they do not, errors will be published
   * to the error receiver and {@code false} returned.
   *
   * @param lexical The lexical position
   *
   * @return {@code true} if all invariants hold
   */

  public boolean check(
    final LexicalPosition<URI> lexical)
  {
    if (this.triangleCount != this.expectedTriangleCount) {
      this.errors.onError(
        lexical,
        String.format(
          "Expected %s triangles, but %s were provided",
          Long.toUnsignedString(this.expectedTriangleCount),
          Long.toUnsignedString(this.triangleCount))
      );
      return false;
    }
    return true;
  }

  private void checkTriangleVertex(
    final LexicalPosition<URI> lexical,
    final int vertexIndex,
    final long vertexValue)
  {
    if (Long.compareUnsigned(vertexValue, this.expectedVertexCount) >= 0) {
      this.errors.onError(
        lexical,
        String.format(
          "Triangle %s, vertex %d specifies a vertex value (%s) greater than the specified vertex count (%s)",
          Long.toUnsignedString(this.triangleCount),
          Integer.valueOf(vertexIndex),
          Long.toUnsignedString(vertexValue),
          Long.toUnsignedString(this.expectedVertexCount))
      );
    }
  }

  /**
   * A receiver of error messages.
   */

  public interface ErrorReceiverType
  {
    /**
     * Receive an error.
     *
     * @param lexical The lexical position
     * @param message The error message
     */

    void onError(
      LexicalPosition<URI> lexical,
      String message);
  }
}
