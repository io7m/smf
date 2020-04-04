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

package com.io7m.smfj.validation.main;

import com.io7m.jcoords.core.conversion.CAxis;
import com.io7m.jcoords.core.conversion.CAxisSystem;
import com.io7m.jlexing.core.LexicalPosition;
import com.io7m.junreachable.UnimplementedCodeException;
import com.io7m.smfj.core.SMFAttributeName;
import com.io7m.smfj.core.SMFComponentType;
import com.io7m.smfj.core.SMFCoordinateSystem;
import com.io7m.smfj.core.SMFErrorType;
import com.io7m.smfj.core.SMFFaceWindingOrder;
import com.io7m.smfj.core.SMFPartialLogged;
import com.io7m.smfj.core.SMFSchemaIdentifier;
import com.io7m.smfj.core.SMFSchemaName;
import com.io7m.smfj.core.SMFWarningType;
import com.io7m.smfj.format.text.SMFTLineReaderList;
import com.io7m.smfj.format.text.SMFTLineReaderType;
import com.io7m.smfj.parser.api.SMFParseError;
import com.io7m.smfj.validation.api.SMFSchema;
import com.io7m.smfj.validation.api.SMFSchemaAttribute;
import com.io7m.smfj.validation.api.SMFSchemaParserProviderType;
import com.io7m.smfj.validation.api.SMFSchemaParserType;
import com.io7m.smfj.validation.api.SMFSchemaRequireTriangles;
import com.io7m.smfj.validation.api.SMFSchemaRequireVertices;
import com.io7m.smfj.validation.api.SMFSchemaVersion;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.stream.Collectors;
import org.apache.commons.io.IOUtils;
import org.osgi.service.component.annotations.Component;

import static com.io7m.smfj.validation.api.SMFSchemaAllowExtraAttributes.SMF_EXTRA_ATTRIBUTES_DISALLOWED;
import static com.io7m.smfj.validation.api.SMFSchemaRequireTriangles.SMF_TRIANGLES_NOT_REQUIRED;
import static com.io7m.smfj.validation.api.SMFSchemaRequireTriangles.SMF_TRIANGLES_REQUIRED;
import static com.io7m.smfj.validation.api.SMFSchemaRequireVertices.SMF_VERTICES_NOT_REQUIRED;
import static com.io7m.smfj.validation.api.SMFSchemaRequireVertices.SMF_VERTICES_REQUIRED;

/**
 * The default implementation of the {@link SMFSchemaParserProviderType} interface.
 */

@Component
public final class SMFSchemaParserProvider
  implements SMFSchemaParserProviderType
{
  private static final SortedSet<SMFSchemaVersion> SUPPORTED = makeSupported();

  private static SortedSet<SMFSchemaVersion> makeSupported()
  {
    final var versions = new java.util.TreeSet<SMFSchemaVersion>();
    versions.add(SMFSchemaVersion.of(1, 0));
    return Collections.unmodifiableSortedSet(versions);
  }

  /**
   * Construct a schema parser provider.
   */

  public SMFSchemaParserProvider()
  {
    // Nothing required
  }

  @Override
  public SortedSet<SMFSchemaVersion> schemaSupportedVersions()
  {
    return SUPPORTED;
  }

  @Override
  public SMFSchemaParserType schemaParserCreate(
    final URI path,
    final InputStream stream)
  {
    return new Parser(path, stream);
  }

  private static final class ParserV1 implements SMFSchemaParserType
  {
    private final TreeMap<SMFAttributeName, SMFSchemaAttribute> requiredAttributes;
    private final TreeMap<SMFAttributeName, SMFSchemaAttribute> optionalAttributes;
    private SMFSchema.Builder builder;
    private boolean received_identifier;
    private final ArrayList<SMFErrorType> errors;
    private final ArrayList<SMFWarningType> warnings;
    private final SMFSchemaVersion version;
    private final SMFTLineReaderType reader;
    private final URI uri;

    ParserV1(
      final SMFSchemaVersion in_version,
      final SMFTLineReaderType in_reader,
      final URI in_uri)
    {
      this.version = in_version;
      this.reader = in_reader;
      this.uri = in_uri;
      this.received_identifier = false;
      this.errors = new ArrayList<>();
      this.warnings = new ArrayList<>();
      this.requiredAttributes = new TreeMap<>();
      this.optionalAttributes = new TreeMap<>();
      this.builder = SMFSchema.builder();
    }

    @Override
    public SMFPartialLogged<SMFSchema> parseSchema()
    {
      this.requiredAttributes.clear();
      this.optionalAttributes.clear();

      this.builder = SMFSchema.builder();
      this.builder.setAllowExtraAttributes(SMF_EXTRA_ATTRIBUTES_DISALLOWED);
      this.builder.setRequireTriangles(SMF_TRIANGLES_REQUIRED);
      this.builder.setRequireVertices(SMF_VERTICES_REQUIRED);

      try {
        while (true) {
          final Optional<List<String>> line_opt = this.reader.line();
          if (!line_opt.isPresent()) {
            break;
          }
          final List<String> line = line_opt.get();
          if (line.isEmpty()) {
            continue;
          }
          this.parseStatement(line);
        }
      } catch (final IOException e) {
        this.errors.add(
          SMFParseError.of(
            this.reader.position(),
            "I/O error",
            Optional.of(e)));
      }

      if (this.errors.isEmpty()) {
        if (!this.received_identifier) {
          this.errors.add(SMFParseError.of(
            this.reader.position(),
            "Must specify a schema identifier",
            Optional.empty()));
          return SMFPartialLogged.failed(this.errors);
        }

        this.builder.setRequiredAttributes(this.requiredAttributes);
        this.builder.setOptionalAttributes(this.optionalAttributes);
        return SMFPartialLogged.succeeded(this.builder.build());
      }
      return SMFPartialLogged.failed(this.errors);
    }

    private void parseStatement(
      final List<String> line)
    {
      final String name = line.get(0);
      switch (name) {
        case "schema": {
          final SMFPartialLogged<SMFSchemaIdentifier> result =
            this.parseStatementIdentifier(line);

          this.warnings.addAll(result.warnings());
          this.errors.addAll(result.errors());

          if (result.isSucceeded()) {
            final SMFSchemaIdentifier p = result.get();
            this.received_identifier = true;
            this.builder.setSchemaIdentifier(p);
          }
          return;
        }

        case "coordinates": {
          final SMFPartialLogged<SMFCoordinateSystem> result =
            this.parseStatementCoordinates(line);

          this.warnings.addAll(result.warnings());
          this.errors.addAll(result.errors());

          if (result.isSucceeded()) {
            this.builder.setRequiredCoordinateSystem(result.get());
          }
          return;
        }

        case "require-vertices": {
          final SMFPartialLogged<SMFSchemaRequireVertices> result =
            this.parseStatementRequireVertices(line);

          this.warnings.addAll(result.warnings());
          this.errors.addAll(result.errors());

          if (result.isSucceeded()) {
            this.builder.setRequireVertices(result.get());
          }
          return;
        }

        case "require-triangles": {
          final SMFPartialLogged<SMFSchemaRequireTriangles> result =
            this.parseStatementRequireTriangles(line);

          this.warnings.addAll(result.warnings());
          this.errors.addAll(result.errors());

          if (result.isSucceeded()) {
            this.builder.setRequireTriangles(result.get());
          }
          return;
        }

        case "attribute": {
          final SMFPartialLogged<AttributeRequirement> result =
            this.parseStatementAttribute(line);

          this.warnings.addAll(result.warnings());
          this.errors.addAll(result.errors());

          if (result.isSucceeded()) {
            final AttributeRequirement requirement = result.get();
            if (requirement.required) {
              this.requiredAttributes.put(
                requirement.attribute.name(), requirement.attribute);
            } else {
              this.optionalAttributes.put(
                requirement.attribute.name(), requirement.attribute);
            }
          }
          return;
        }

        default: {
          final SMFParseError error =
            SMFParseError.of(
              this.reader.position(),
              "Unrecognized schema statement: " + name,
              Optional.empty());
          this.errors.add(error);
        }
      }
    }

    private SMFPartialLogged<SMFSchemaRequireTriangles>
    parseStatementRequireTriangles(
      final List<String> line)
    {
      if (line.size() == 2) {
        final String text = line.get(1);
        switch (text) {
          case "true":
            return SMFPartialLogged.succeeded(SMF_TRIANGLES_REQUIRED);
          case "false":
            return SMFPartialLogged.succeeded(SMF_TRIANGLES_NOT_REQUIRED);
          default:
            break;
        }
      }

      final StringBuilder sb = new StringBuilder(128);
      sb.append("Could not parse triangle requirement.");
      sb.append(System.lineSeparator());
      sb.append("  Expected: require-triangles (true | false)");
      sb.append(System.lineSeparator());
      sb.append("  Received: ");
      sb.append(String.join(" ", line));
      sb.append(System.lineSeparator());
      return SMFPartialLogged.failed(SMFParseError.of(
        this.reader.position(),
        sb.toString(),
        Optional.empty()));
    }

    private SMFPartialLogged<SMFSchemaRequireVertices>
    parseStatementRequireVertices(
      final List<String> line)
    {
      if (line.size() == 2) {
        final String text = line.get(1);
        switch (text) {
          case "true":
            return SMFPartialLogged.succeeded(SMF_VERTICES_REQUIRED);
          case "false":
            return SMFPartialLogged.succeeded(SMF_VERTICES_NOT_REQUIRED);
          default:
            break;
        }
      }

      final StringBuilder sb = new StringBuilder(128);
      sb.append("Could not parse vertices requirement.");
      sb.append(System.lineSeparator());
      sb.append("  Expected: require-vertices (true | false)");
      sb.append(System.lineSeparator());
      sb.append("  Received: ");
      sb.append(String.join(" ", line));
      sb.append(System.lineSeparator());
      return SMFPartialLogged.failed(SMFParseError.of(
        this.reader.position(),
        sb.toString(),
        Optional.empty()));
    }

    private SMFPartialLogged<SMFCoordinateSystem> parseStatementCoordinates(
      final List<String> line)
    {
      if (line.size() == 5) {
        try {
          final CAxis axis_right = CAxis.of(line.get(1));
          final CAxis axis_up = CAxis.of(line.get(2));
          final CAxis axis_forward = CAxis.of(line.get(3));
          final SMFFaceWindingOrder order =
            SMFFaceWindingOrder.fromName(line.get(4));

          boolean bad = false;
          bad = bad || axis_right.axis() == axis_up.axis();
          bad = bad || axis_up.axis() == axis_forward.axis();
          bad = bad || axis_forward.axis() == axis_right.axis();

          if (bad) {
            throw new IllegalArgumentException("Axes must be perpendicular");
          }

          return SMFPartialLogged.succeeded(SMFCoordinateSystem.of(
            CAxisSystem.of(axis_right, axis_up, axis_forward),
            order));
        } catch (final IllegalArgumentException e) {
          final StringBuilder sb = new StringBuilder(128);
          sb.append("Could not parse coordinate system.");
          sb.append(System.lineSeparator());
          sb.append("  Error: ");
          sb.append(e.getMessage());
          sb.append(System.lineSeparator());
          sb.append(
            "  Expected: coordinates <axis> <axis> <axis> <winding-order>");
          sb.append(System.lineSeparator());
          sb.append("  Received: ");
          sb.append(String.join(" ", line));
          sb.append(System.lineSeparator());
          return SMFPartialLogged.failed(SMFParseError.of(
            this.reader.position(),
            sb.toString(),
            Optional.of(e)));
        }
      }

      final StringBuilder sb = new StringBuilder(128);
      sb.append("Could not parse coordinate system.");
      sb.append(System.lineSeparator());
      sb.append("  Expected: coordinates <axis> <axis> <axis> <winding-order>");
      sb.append(System.lineSeparator());
      sb.append("  Received: ");
      sb.append(line.stream().collect(Collectors.joining(" ")));
      sb.append(System.lineSeparator());
      return SMFPartialLogged.failed(SMFParseError.of(
        this.reader.position(),
        sb.toString(),
        Optional.empty()));
    }

    private SMFPartialLogged<Boolean>
    parseRequired(
      final String text)
    {
      if (Objects.equals("required", text)) {
        return SMFPartialLogged.succeeded(Boolean.TRUE);
      }
      if (Objects.equals("optional", text)) {
        return SMFPartialLogged.succeeded(Boolean.FALSE);
      }
      final StringBuilder sb = new StringBuilder(128);
      sb.append("Could not parse requirement.");
      sb.append(System.lineSeparator());
      sb.append("  Expected: required | optional");
      sb.append(System.lineSeparator());
      sb.append("  Received: ");
      sb.append(text);
      sb.append(System.lineSeparator());
      return SMFPartialLogged.failed(SMFParseError.of(
        this.reader.position(),
        sb.toString(),
        Optional.empty()));
    }

    private SMFPartialLogged<Optional<SMFComponentType>>
    parseComponentType(
      final String text)
    {
      if (Objects.equals(text, "-")) {
        return SMFPartialLogged.succeeded(Optional.empty());
      }

      try {
        return SMFPartialLogged.succeeded(
          Optional.of(SMFComponentType.of(text)));
      } catch (final IllegalArgumentException e) {
        return SMFPartialLogged.failed(SMFParseError.of(
          this.reader.position(),
          "Could not parse component type: " + e.getMessage(),
          Optional.of(e)));
      }
    }

    private SMFPartialLogged<OptionalInt>
    parseComponentCount(
      final String text)
    {
      if (Objects.equals(text, "-")) {
        return SMFPartialLogged.succeeded(OptionalInt.empty());
      }

      try {
        return SMFPartialLogged.succeeded(
          OptionalInt.of(Integer.parseUnsignedInt(text)));
      } catch (final NumberFormatException e) {
        return SMFPartialLogged.failed(SMFParseError.of(
          this.reader.position(),
          "Could not parse component count: " + e.getMessage(),
          Optional.of(e)));
      }
    }

    private SMFPartialLogged<OptionalInt>
    parseComponentSize(
      final String text)
    {
      if (Objects.equals(text, "-")) {
        return SMFPartialLogged.succeeded(OptionalInt.empty());
      }

      try {
        return SMFPartialLogged.succeeded(
          OptionalInt.of(Integer.parseUnsignedInt(text)));
      } catch (final NumberFormatException e) {
        return SMFPartialLogged.failed(SMFParseError.of(
          this.reader.position(),
          "Could not parse component size: " + e.getMessage(),
          Optional.of(e)));
      }
    }

    private SMFPartialLogged<SMFAttributeName>
    parseName(
      final String text)
    {
      try {
        return SMFPartialLogged.succeeded(SMFAttributeName.of(text));
      } catch (final IllegalArgumentException e) {
        return SMFPartialLogged.failed(SMFParseError.of(
          this.reader.position(),
          "Could not parse attribute name: " + e.getMessage(),
          Optional.of(e)));
      }
    }

    private SMFPartialLogged<SMFSchemaIdentifier>
    parseStatementIdentifier(
      final List<String> text)
    {
      if (text.size() == 4) {
        try {
          final SMFSchemaName schema = SMFSchemaName.of(text.get(1));
          final int major = Integer.parseUnsignedInt(text.get(2));
          final int minor = Integer.parseUnsignedInt(text.get(3));
          return SMFPartialLogged.succeeded(
            SMFSchemaIdentifier.of(schema, major, minor));
        } catch (final NumberFormatException e) {
          return SMFPartialLogged.failed(SMFParseError.of(
            this.reader.position(), e.getMessage(), Optional.of(e)));
        }
      }

      final StringBuilder sb = new StringBuilder(128);
      sb.append("Incorrect number of arguments.");
      sb.append(System.lineSeparator());
      sb.append(
        "  Expected: schema <schema> <version-major> <version-minor>");
      sb.append(System.lineSeparator());
      sb.append("  Received: ");
      sb.append(String.join(" ", text));
      sb.append(System.lineSeparator());
      return SMFPartialLogged.failed(SMFParseError.of(
        this.reader.position(),
        sb.toString(),
        Optional.empty()));
    }

    private static final class AttributeRequirement
    {
      private boolean required;
      private SMFSchemaAttribute attribute;

      AttributeRequirement(
        final boolean inRequired,
        final SMFSchemaAttribute inAttribute)
      {
        this.required = inRequired;
        this.attribute = Objects.requireNonNull(inAttribute, "attribute");
      }
    }

    private SMFPartialLogged<AttributeRequirement>
    parseStatementAttribute(
      final List<String> line)
    {
      if (line.size() == 6) {
        final String text_req = line.get(1);
        final String text_name = line.get(2);
        final String text_type = line.get(3);
        final String text_count = line.get(4);
        final String text_size = line.get(5);

        final SMFPartialLogged<Boolean> v_req =
          this.parseRequired(text_req);
        final SMFPartialLogged<SMFAttributeName> v_name =
          this.parseName(text_name);
        final SMFPartialLogged<Optional<SMFComponentType>> v_type =
          this.parseComponentType(text_type);
        final SMFPartialLogged<OptionalInt> v_count =
          this.parseComponentCount(text_count);
        final SMFPartialLogged<OptionalInt> v_size =
          this.parseComponentSize(text_size);

        return v_req.flatMap(required -> {
          return v_name.flatMap(name -> {
            return v_type.flatMap(type -> {
              return v_count.flatMap(count -> {
                return v_size.map(size -> {
                  return new AttributeRequirement(
                    required.booleanValue(),
                    SMFSchemaAttribute.of(name, type, count, size)
                  );
                });
              });
            });
          });
        });
      }

      final StringBuilder sb = new StringBuilder(128);
      sb.append("Incorrect number of arguments.");
      sb.append(System.lineSeparator());
      sb.append(
        "  Expected: attribute <requirement> <name> <type> <count> <size>");
      sb.append(System.lineSeparator());
      sb.append("  Received: ");
      sb.append(line.stream().collect(Collectors.joining(" ")));
      sb.append(System.lineSeparator());
      return SMFPartialLogged.failed(SMFParseError.of(
        this.reader.position(),
        sb.toString(),
        Optional.empty()));
    }

    @Override
    public void close()
      throws IOException
    {
      // Nothing required
    }
  }

  private static final class Parser implements SMFSchemaParserType
  {
    private final URI uri;
    private final InputStream stream;

    Parser(
      final URI in_uri,
      final InputStream in_stream)
    {
      this.uri = Objects.requireNonNull(in_uri, "uri");
      this.stream = Objects.requireNonNull(in_stream, "stream");
    }

    @Override
    public SMFPartialLogged<SMFSchema> parseSchema()
    {
      try {
        final List<String> lines =
          List.copyOf(IOUtils.readLines(this.stream, StandardCharsets.UTF_8));
        final SMFTLineReaderType reader =
          SMFTLineReaderList.create(this.uri, lines, 1);

        try {
          final Optional<List<String>> line = reader.line();
          if (line.isPresent()) {
            final SMFPartialLogged<SMFSchemaVersion> r_version =
              this.parseVersion(reader.position(), line.get());
            if (r_version.isFailed()) {
              return SMFPartialLogged.failed(
                r_version.errors(),
                r_version.warnings());
            }

            final SMFSchemaVersion version = r_version.get();
            if (version.major() == 1) {
              return new ParserV1(version, reader, this.uri).parseSchema();
            }

            throw new UnimplementedCodeException();
          }

          final SMFParseError error =
            SMFParseError.of(
              reader.position(),
              "Empty file: Must begin with an smf-schema version declaration",
              Optional.empty());
          return SMFPartialLogged.failed(error);
        } catch (final IOException e) {
          final SMFParseError error =
            SMFParseError.of(reader.position(), "I/O error", Optional.of(e));
          return SMFPartialLogged.failed(error);
        }
      } catch (final IOException e) {
        final SMFParseError error =
          SMFParseError.of(
            LexicalPosition.of(1, 0, Optional.of(this.uri)),
            "I/O error",
            Optional.of(e));
        return SMFPartialLogged.failed(error);
      }
    }

    private SMFPartialLogged<SMFSchemaVersion> parseVersion(
      final LexicalPosition<URI> position,
      final List<String> line)
    {
      if (line.size() == 3) {
        final String name = line.get(0);
        if (!Objects.equals(name, "smf-schema")) {
          return SMFPartialLogged.failed(
            this.unparseableVersionList(line, Optional.empty()));
        }

        try {
          final int major = Integer.parseUnsignedInt(line.get(1));
          final int minor = Integer.parseUnsignedInt(line.get(2));
          return SMFPartialLogged.succeeded(SMFSchemaVersion.of(major, minor));
        } catch (final NumberFormatException e) {
          return SMFPartialLogged.failed(
            this.unparseableVersionList(line, Optional.of(e)));
        }
      }

      return SMFPartialLogged.failed(
        this.unparseableVersionList(line, Optional.empty()));
    }

    private SMFParseError unparseableVersionList(
      final List<String> line,
      final Optional<Exception> exception)
    {
      return this.unparseableVersion(
        String.join(" ", line), exception);
    }

    private SMFParseError unparseableVersion(
      final String line,
      final Optional<Exception> exception)
    {
      final StringBuilder sb = new StringBuilder(128);
      sb.append("Unparseable version declaration.");
      sb.append(System.lineSeparator());
      sb.append("  Expected: smf-schema <integer-unsigned> <integer-unsigned>");
      sb.append(System.lineSeparator());
      sb.append("  Received: ");
      sb.append(line);
      sb.append(System.lineSeparator());
      return SMFParseError.of(
        LexicalPosition.of(1, 0, Optional.of(this.uri)),
        sb.toString(),
        exception);
    }

    @Override
    public void close()
      throws IOException
    {
      this.stream.close();
    }
  }
}
