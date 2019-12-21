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

import com.io7m.smfj.core.SMFHeader;
import com.io7m.smfj.core.SMFPartialLogged;
import com.io7m.smfj.processing.api.SMFFilterCommandContext;
import com.io7m.smfj.processing.api.SMFMemoryMesh;
import com.io7m.smfj.processing.api.SMFMemoryMeshFilterType;
import com.io7m.smfj.processing.api.SMFProcessingError;
import com.io7m.smfj.validation.api.SMFSchema;
import com.io7m.smfj.validation.api.SMFSchemaParserProviderType;
import com.io7m.smfj.validation.api.SMFSchemaParserType;
import com.io7m.smfj.validation.api.SMFSchemaValidatorType;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.ServiceLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.io7m.smfj.processing.api.SMFFilterCommandParsing.errorExpectedGotValidation;

/**
 * A filter that validates the current mesh against a schema.
 */

public final class SMFMemoryMeshFilterSchemaValidate implements
  SMFMemoryMeshFilterType
{
  private static final Logger LOG =
    LoggerFactory.getLogger(SMFMemoryMeshFilterSchemaValidate.class);

  /**
   * The command name.
   */

  public static final String NAME = "schema-validate";
  private static final String SYNTAX = "<file>";

  private final Path schema_file;

  private SMFMemoryMeshFilterSchemaValidate(
    final Path in_file)
  {
    this.schema_file = Objects.requireNonNull(in_file, "File");
  }

  /**
   * Create a new filter.
   *
   * @param in_file The schema file
   *
   * @return A new filter
   */

  public static SMFMemoryMeshFilterType create(
    final Path in_file)
  {
    return new SMFMemoryMeshFilterSchemaValidate(in_file);
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

  public static SMFPartialLogged<SMFMemoryMeshFilterType> parse(
    final Optional<URI> file,
    final int line,
    final List<String> text)
  {
    Objects.requireNonNull(file, "file");
    Objects.requireNonNull(text, "text");

    if (text.size() == 1) {
      try {
        return SMFPartialLogged.succeeded(create(Paths.get(text.get(0))));
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

  private static SMFSchemaValidatorType findValidator()
  {
    final ServiceLoader<SMFSchemaValidatorType> loader =
      ServiceLoader.load(SMFSchemaValidatorType.class);
    final Iterator<SMFSchemaValidatorType> providers =
      loader.iterator();
    if (providers.hasNext()) {
      return providers.next();
    }

    throw new UnsupportedOperationException("No available validator");
  }

  private static SMFSchemaParserProviderType findParserProvider()
  {
    final ServiceLoader<SMFSchemaParserProviderType> loader =
      ServiceLoader.load(SMFSchemaParserProviderType.class);
    final Iterator<SMFSchemaParserProviderType> providers =
      loader.iterator();
    if (providers.hasNext()) {
      return providers.next();
    }

    throw new UnsupportedOperationException("No available schema parser");
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
  public SMFPartialLogged<SMFMemoryMesh> filter(
    final SMFFilterCommandContext context,
    final SMFMemoryMesh m)
  {
    Objects.requireNonNull(context, "Context");
    Objects.requireNonNull(m, "Mesh");

    final Path file = context.resolvePath(this.schema_file);
    LOG.debug("resolved schema file: {}", file);

    final SMFSchemaParserProviderType parser_provider =
      findParserProvider();
    final SMFSchemaValidatorType validator =
      findValidator();

    try (InputStream stream = Files.newInputStream(file)) {
      try (SMFSchemaParserType parser =
             parser_provider.schemaParserCreate(file.toUri(), stream)) {

        final SMFPartialLogged<SMFSchema> resultSchema = parser.parseSchema();
        if (resultSchema.isFailed()) {
          return SMFPartialLogged.failed(
            resultSchema.errors(),
            resultSchema.warnings());
        }

        final SMFSchema schema = resultSchema.get();
        final SMFPartialLogged<SMFHeader> validationResult =
          validator.validate(m.header(), schema);

        if (validationResult.isFailed()) {
          return resultSchema.flatMap(x -> {
            return SMFPartialLogged.failed(
              resultSchema.errors(),
              resultSchema.warnings());
          });
        }

        return resultSchema.flatMap(ignored0 -> {
          return validationResult.flatMap(ignored1 -> {
            return SMFPartialLogged.succeeded(m);
          });
        });
      }
    } catch (final IOException e) {
      return SMFPartialLogged.failed(
        SMFProcessingError.of(e.getMessage(), Optional.of(e)));
    }
  }
}
