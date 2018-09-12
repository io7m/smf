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

package com.io7m.smfj.processing.main;

import com.io7m.jtensors.core.unparameterized.vectors.Vector3L;
import com.io7m.smfj.core.SMFTriangles;
import com.io7m.smfj.parser.api.SMFParseError;
import com.io7m.smfj.processing.api.SMFFilterCommandContext;
import com.io7m.smfj.processing.api.SMFMemoryMesh;
import com.io7m.smfj.processing.api.SMFMemoryMeshFilterType;
import com.io7m.smfj.processing.api.SMFProcessingError;
import io.vavr.collection.List;
import io.vavr.collection.Seq;
import io.vavr.collection.Vector;
import io.vavr.control.Validation;

import java.net.URI;
import java.util.Objects;
import java.util.Optional;
import java.util.OptionalInt;

import static com.io7m.smfj.processing.api.SMFFilterCommandParsing.errorExpectedGotValidation;

/**
 * A filter that optimizes and/or validates triangles.
 */

public final class SMFMemoryMeshFilterTrianglesOptimize implements
  SMFMemoryMeshFilterType
{
  /**
   * The command name.
   */

  public static final String NAME = "triangles-optimize";

  private static final String SYNTAX =
    "(<size> | '-') ('validate' | 'no-validate')";

  private final SMFMemoryMeshFilterTrianglesOptimizeConfiguration config;

  private SMFMemoryMeshFilterTrianglesOptimize(
    final SMFMemoryMeshFilterTrianglesOptimizeConfiguration in_config)
  {
    this.config = Objects.requireNonNull(in_config, "Config");
  }

  private static SMFProcessingError error(
    final String format,
    final Object... params)
  {
    return SMFProcessingError.of(
      String.format(format, params), Optional.empty());
  }

  /**
   * Create a new filter.
   *
   * @param in_config The configuration
   *
   * @return A new filter
   */

  public static SMFMemoryMeshFilterType create(
    final SMFMemoryMeshFilterTrianglesOptimizeConfiguration in_config)
  {
    return new SMFMemoryMeshFilterTrianglesOptimize(in_config);
  }

  private static SMFProcessingError nonexistentVertex(
    final int triangle,
    final long vertex)
  {
    return error(
      "Triangle %d points to nonexistent vertex %d",
      Integer.valueOf(triangle),
      Long.valueOf(vertex));
  }

  /**
   * Attempt to parse a command.
   *
   * @param file The file, if any
   * @param line The line
   * @param text The text
   *
   * @return A parsed command or a list of parse errors
   */

  public static Validation<Seq<SMFParseError>, SMFMemoryMeshFilterType> parse(
    final Optional<URI> file,
    final int line,
    final List<String> text)
  {
    Objects.requireNonNull(file, "file");
    Objects.requireNonNull(text, "text");

    if (text.length() == 2) {
      try {
        final int size;
        final String size_text = text.get(0);
        if (Objects.equals(size_text, "-")) {
          size = 0;
        } else {
          size = Integer.parseInt(size_text);
        }

        final String validate = text.get(1);
        final SMFMemoryMeshFilterTrianglesOptimizeConfiguration.Builder builder =
          SMFMemoryMeshFilterTrianglesOptimizeConfiguration.builder();
        if (size > 0) {
          builder.setOptimize(size);
        }

        if (Objects.equals(validate, "validate")) {
          builder.setValidate(true);
        } else if (Objects.equals(validate, "no-validate")) {
          builder.setValidate(false);
        } else {
          throw new IllegalArgumentException(
            "Could not parse validation value: Must be 'validate' | 'no-validate'");
        }

        return Validation.valid(
          create(builder.build()));
      } catch (final IllegalArgumentException e) {
        return errorExpectedGotValidation(file, line, makeSyntax(), text);
      }
    }

    return errorExpectedGotValidation(file, line, makeSyntax(), text);
  }

  private static String makeSyntax()
  {
    return NAME + " " + SYNTAX;
  }

  private static int optimize(
    final SMFMemoryMesh m,
    final OptionalInt optimize_opt,
    final long max)
  {
    int triangle_size = m.header().triangles().triangleIndexSizeBits();
    if (optimize_opt.isPresent()) {
      if (max < (long) (StrictMath.pow(2.0, 64.0) - 1.0)) {
        triangle_size = 64;
      }
      if (max < (long) (StrictMath.pow(2.0, 32.0) - 1.0)) {
        triangle_size = 32;
      }
      if (max < (long) (StrictMath.pow(2.0, 16.0) - 1.0)) {
        triangle_size = 16;
      }
      if (max < (long) (StrictMath.pow(2.0, 8.0) - 1.0)) {
        triangle_size = 8;
      }

      final int smallest_allowed = optimize_opt.getAsInt();
      triangle_size = Math.max(triangle_size, smallest_allowed);
    }
    return triangle_size;
  }

  @Override
  public String name()
  {
    return NAME;
  }

  @Override
  public String syntax()
  {
    return makeSyntax();
  }

  @Override
  public Validation<Seq<SMFProcessingError>, SMFMemoryMesh> filter(
    final SMFFilterCommandContext context,
    final SMFMemoryMesh m)
  {
    Objects.requireNonNull(context, "Context");
    Objects.requireNonNull(m, "Mesh");

    List<SMFProcessingError> errors = List.empty();
    final long vertices = m.header().vertexCount();
    final Vector<Vector3L> triangles = m.triangles();
    final OptionalInt optimize_opt = this.config.optimize();

    long max = 0L;
    for (int index = 0; index < triangles.size(); ++index) {
      final Vector3L triangle = triangles.get(index);

      final long v0 = triangle.x();
      final long v1 = triangle.y();
      final long v2 = triangle.z();

      if (this.config.validate()) {
        if (Long.compareUnsigned(v0, vertices) >= 0) {
          errors = errors.append(nonexistentVertex(index, v0));
        }
        if (Long.compareUnsigned(v1, vertices) >= 0) {
          errors = errors.append(nonexistentVertex(index, v1));
        }
        if (Long.compareUnsigned(v2, vertices) >= 0) {
          errors = errors.append(nonexistentVertex(index, v2));
        }
      }

      max = Math.max(max, v0);
      max = Math.max(max, v1);
      max = Math.max(max, v2);
    }

    final int triangle_size = optimize(m, optimize_opt, max);
    if (errors.isEmpty()) {
      final SMFTriangles new_triangles =
        m.header().triangles().withTriangleIndexSizeBits(triangle_size);
      return Validation.valid(
        m.withHeader(m.header().withTriangles(new_triangles)));
    }
    return Validation.invalid(errors);
  }
}
