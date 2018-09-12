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
import com.io7m.smfj.core.SMFSchemaIdentifier;
import com.io7m.smfj.core.SMFSchemaName;
import com.io7m.smfj.format.text.SMFTLineReaderList;
import com.io7m.smfj.format.text.SMFTLineReaderType;
import com.io7m.smfj.parser.api.SMFParseError;
import com.io7m.smfj.validation.api.SMFSchema;
import com.io7m.smfj.validation.api.SMFSchemaAllowExtraAttributes;
import com.io7m.smfj.validation.api.SMFSchemaAttribute;
import com.io7m.smfj.validation.api.SMFSchemaParserProviderType;
import com.io7m.smfj.validation.api.SMFSchemaParserType;
import com.io7m.smfj.validation.api.SMFSchemaRequireTriangles;
import com.io7m.smfj.validation.api.SMFSchemaRequireVertices;
import com.io7m.smfj.validation.api.SMFSchemaVersion;
import io.vavr.Tuple;
import io.vavr.Tuple2;
import io.vavr.collection.List;
import io.vavr.collection.Seq;
import io.vavr.collection.SortedSet;
import io.vavr.collection.TreeSet;
import io.vavr.control.Validation;
import org.apache.commons.io.IOUtils;
import org.osgi.service.component.annotations.Component;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.stream.Collectors;

import static com.io7m.smfj.validation.api.SMFSchemaRequireTriangles.SMF_TRIANGLES_NOT_REQUIRED;
import static com.io7m.smfj.validation.api.SMFSchemaRequireTriangles.SMF_TRIANGLES_REQUIRED;
import static com.io7m.smfj.validation.api.SMFSchemaRequireVertices.SMF_VERTICES_NOT_REQUIRED;
import static com.io7m.smfj.validation.api.SMFSchemaRequireVertices.SMF_VERTICES_REQUIRED;
import static io.vavr.control.Validation.invalid;
import static io.vavr.control.Validation.valid;

/**
 * The default implementation of the {@link SMFSchemaParserProviderType} interface.
 */

@Component
public final class SMFSchemaParserProvider
  implements SMFSchemaParserProviderType
{
  private static final SortedSet<SMFSchemaVersion> SUPPORTED;

  static {
    SUPPORTED = TreeSet.of(SMFSchemaVersion.of(1, 0));
  }

  /**
   * Construct a schema parser provider.
   */

  public SMFSchemaParserProvider()
  {
    // Nothing required
  }

  private static <E, T> Validation<Seq<E>, T> flatten(
    final Validation<Seq<Seq<E>>, T> v)
  {
    return v.mapError(xs -> xs.fold(List.empty(), Seq::appendAll));
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
    private final SMFSchemaVersion version;
    private final SMFTLineReaderType reader;
    private final URI uri;
    private boolean received_identifier;

    ParserV1(
      final SMFSchemaVersion in_version,
      final SMFTLineReaderType in_reader,
      final URI in_uri)
    {
      this.version = in_version;
      this.reader = in_reader;
      this.uri = in_uri;
      this.received_identifier = false;
    }

    @Override
    public Validation<Seq<SMFErrorType>, SMFSchema> parseSchema()
    {
      final SMFSchema.Builder builder = SMFSchema.builder();
      builder.setAllowExtraAttributes(
        SMFSchemaAllowExtraAttributes.SMF_EXTRA_ATTRIBUTES_DISALLOWED);
      builder.setRequireTriangles(
        SMF_TRIANGLES_REQUIRED);
      builder.setRequireVertices(
        SMF_VERTICES_REQUIRED);

      List<SMFErrorType> errors = List.empty();

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
          errors = this.parseStatement(builder, errors, line);
        }
      } catch (final IOException e) {
        errors = errors.append(
          SMFParseError.of(
            this.reader.position(),
            "I/O error",
            Optional.of(e)));
      }

      if (errors.isEmpty()) {
        if (!this.received_identifier) {
          return invalid(errors.append(SMFParseError.of(
            this.reader.position(),
            "Must specify a schema identifier",
            Optional.empty())));
        }

        return valid(builder.build());
      }
      return invalid(errors);
    }

    private List<SMFErrorType> parseStatement(
      final SMFSchema.Builder builder,
      final List<SMFErrorType> errors,
      final List<String> line)
    {
      final String name = line.get(0);
      switch (name) {

        case "schema": {
          final Validation<Seq<SMFParseError>, SMFSchemaIdentifier> result =
            this.parseStatementIdentifier(line);
          if (result.isValid()) {
            final SMFSchemaIdentifier p = result.get();
            this.received_identifier = true;
            builder.setSchemaIdentifier(p);
            return errors;
          }

          return errors.appendAll(result.getError());
        }

        case "coordinates": {
          final Validation<Seq<SMFParseError>, SMFCoordinateSystem> result =
            this.parseStatementCoordinates(line);
          if (result.isValid()) {
            builder.setRequiredCoordinateSystem(result.get());
            return errors;
          }

          return errors.appendAll(result.getError());
        }

        case "require-vertices": {
          final Validation<Seq<SMFParseError>, SMFSchemaRequireVertices> result =
            this.parseStatementRequireVertices(line);
          if (result.isValid()) {
            builder.setRequireVertices(result.get());
            return errors;
          }

          return errors.appendAll(result.getError());
        }

        case "require-triangles": {
          final Validation<Seq<SMFParseError>, SMFSchemaRequireTriangles> result =
            this.parseStatementRequireTriangles(line);
          if (result.isValid()) {
            builder.setRequireTriangles(result.get());
            return errors;
          }

          return errors.appendAll(result.getError());
        }

        case "attribute": {
          final Validation<Seq<SMFParseError>, Tuple2<Boolean, SMFSchemaAttribute>> result =
            this.parseStatementAttribute(line);
          if (result.isValid()) {
            final Tuple2<Boolean, SMFSchemaAttribute> p = result.get();
            final boolean required = p._1.booleanValue();
            if (required) {
              builder.putRequiredAttributes(p._2.name(), p._2);
              return errors;
            }
            builder.putOptionalAttributes(p._2.name(), p._2);
            return errors;
          }

          return errors.appendAll(result.getError());
        }

        default: {
          final SMFParseError error = SMFParseError.of(
            this.reader.position(),
            "Unrecognized schema statement: " + name,
            Optional.empty());
          return errors.append(error);
        }
      }
    }

    private Validation<Seq<SMFParseError>, SMFSchemaRequireTriangles>
    parseStatementRequireTriangles(
      final List<String> line)
    {
      if (line.size() == 2) {
        final String text = line.get(1);
        switch (text) {
          case "true":
            return valid(SMF_TRIANGLES_REQUIRED);
          case "false":
            return valid(SMF_TRIANGLES_NOT_REQUIRED);
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
      sb.append(line.toJavaStream().collect(Collectors.joining(" ")));
      sb.append(System.lineSeparator());
      return invalid(List.of(SMFParseError.of(
        this.reader.position(),
        sb.toString(),
        Optional.empty())));
    }

    private Validation<Seq<SMFParseError>, SMFSchemaRequireVertices>
    parseStatementRequireVertices(
      final List<String> line)
    {
      if (line.size() == 2) {
        final String text = line.get(1);
        switch (text) {
          case "true":
            return valid(SMF_VERTICES_REQUIRED);
          case "false":
            return valid(SMF_VERTICES_NOT_REQUIRED);
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
      sb.append(line.toJavaStream().collect(Collectors.joining(" ")));
      sb.append(System.lineSeparator());
      return invalid(List.of(SMFParseError.of(
        this.reader.position(),
        sb.toString(),
        Optional.empty())));
    }

    private Validation<Seq<SMFParseError>, SMFCoordinateSystem> parseStatementCoordinates(
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

          return valid(SMFCoordinateSystem.of(
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
          sb.append(line.toJavaStream().collect(Collectors.joining(" ")));
          sb.append(System.lineSeparator());
          return invalid(List.of(SMFParseError.of(
            this.reader.position(),
            sb.toString(),
            Optional.of(e))));
        }
      }

      final StringBuilder sb = new StringBuilder(128);
      sb.append("Could not parse coordinate system.");
      sb.append(System.lineSeparator());
      sb.append("  Expected: coordinates <axis> <axis> <axis> <winding-order>");
      sb.append(System.lineSeparator());
      sb.append("  Received: ");
      sb.append(line.toJavaStream().collect(Collectors.joining(" ")));
      sb.append(System.lineSeparator());
      return invalid(List.of(SMFParseError.of(
        this.reader.position(),
        sb.toString(),
        Optional.empty())));
    }

    private Validation<Seq<SMFParseError>, Boolean>
    parseRequired(
      final String text)
    {
      if (Objects.equals("required", text)) {
        return valid(Boolean.TRUE);
      }
      if (Objects.equals("optional", text)) {
        return valid(Boolean.FALSE);
      }
      final StringBuilder sb = new StringBuilder(128);
      sb.append("Could not parse requirement.");
      sb.append(System.lineSeparator());
      sb.append("  Expected: required | optional");
      sb.append(System.lineSeparator());
      sb.append("  Received: ");
      sb.append(text);
      sb.append(System.lineSeparator());
      return invalid(List.of(SMFParseError.of(
        this.reader.position(),
        sb.toString(),
        Optional.empty())));
    }

    private Validation<Seq<SMFParseError>, Optional<SMFComponentType>>
    parseComponentType(
      final String text)
    {
      if (Objects.equals(text, "-")) {
        return valid(Optional.empty());
      }

      try {
        return valid(Optional.of(SMFComponentType.of(text)));
      } catch (final IllegalArgumentException e) {
        return invalid(List.of(SMFParseError.of(
          this.reader.position(),
          "Could not parse component type: " + e.getMessage(),
          Optional.of(e))));
      }
    }

    private Validation<Seq<SMFParseError>, OptionalInt>
    parseComponentCount(
      final String text)
    {
      if (Objects.equals(text, "-")) {
        return valid(OptionalInt.empty());
      }

      try {
        return valid(OptionalInt.of(Integer.parseUnsignedInt(text)));
      } catch (final NumberFormatException e) {
        return invalid(List.of(SMFParseError.of(
          this.reader.position(),
          "Could not parse component count: " + e.getMessage(),
          Optional.of(e))));
      }
    }

    private Validation<Seq<SMFParseError>, OptionalInt>
    parseComponentSize(
      final String text)
    {
      if (Objects.equals(text, "-")) {
        return valid(OptionalInt.empty());
      }

      try {
        return valid(OptionalInt.of(Integer.parseUnsignedInt(text)));
      } catch (final NumberFormatException e) {
        return invalid(List.of(SMFParseError.of(
          this.reader.position(),
          "Could not parse component size: " + e.getMessage(),
          Optional.of(e))));
      }
    }

    private Validation<Seq<SMFParseError>, SMFAttributeName>
    parseName(
      final String text)
    {
      try {
        return valid(SMFAttributeName.of(text));
      } catch (final IllegalArgumentException e) {
        return invalid(List.of(SMFParseError.of(
          this.reader.position(),
          "Could not parse attribute name: " + e.getMessage(),
          Optional.of(e))));
      }
    }

    private Validation<Seq<SMFParseError>, SMFSchemaIdentifier>
    parseStatementIdentifier(
      final List<String> text)
    {
      if (text.length() == 4) {
        try {
          final SMFSchemaName schema = SMFSchemaName.of(text.get(1));
          final int major = Integer.parseUnsignedInt(text.get(2));
          final int minor = Integer.parseUnsignedInt(text.get(3));
          return valid(SMFSchemaIdentifier.of(schema, major, minor));
        } catch (final NumberFormatException e) {
          return invalid(List.of(SMFParseError.of(
            this.reader.position(), e.getMessage(), Optional.of(e))));
        }
      }

      final StringBuilder sb = new StringBuilder(128);
      sb.append("Incorrect number of arguments.");
      sb.append(System.lineSeparator());
      sb.append(
        "  Expected: schema <schema> <version-major> <version-minor>");
      sb.append(System.lineSeparator());
      sb.append("  Received: ");
      sb.append(text.toJavaStream().collect(Collectors.joining(" ")));
      sb.append(System.lineSeparator());
      return invalid(List.of(SMFParseError.of(
        this.reader.position(),
        sb.toString(),
        Optional.empty())));
    }

    private Validation<Seq<SMFParseError>, Tuple2<Boolean, SMFSchemaAttribute>>
    parseStatementAttribute(
      final List<String> line)
    {
      if (line.length() == 6) {
        final String text_req = line.get(1);
        final String text_name = line.get(2);
        final String text_type = line.get(3);
        final String text_count = line.get(4);
        final String text_size = line.get(5);

        final Validation<Seq<SMFParseError>, Boolean> v_req =
          this.parseRequired(text_req);
        final Validation<Seq<SMFParseError>, SMFAttributeName> v_name =
          this.parseName(text_name);
        final Validation<Seq<SMFParseError>, Optional<SMFComponentType>> v_type =
          this.parseComponentType(text_type);
        final Validation<Seq<SMFParseError>, OptionalInt> v_count =
          this.parseComponentCount(text_count);
        final Validation<Seq<SMFParseError>, OptionalInt> v_size =
          this.parseComponentSize(text_size);

        return flatten(
          Validation.combine(v_req, v_name, v_type, v_count, v_size)
            .ap((req, name, type, count, size) ->
                  Tuple.of(
                    req,
                    SMFSchemaAttribute.of(name, type, count, size))));
      }

      final StringBuilder sb = new StringBuilder(128);
      sb.append("Incorrect number of arguments.");
      sb.append(System.lineSeparator());
      sb.append(
        "  Expected: attribute <requirement> <name> <type> <count> <size>");
      sb.append(System.lineSeparator());
      sb.append("  Received: ");
      sb.append(line.toJavaStream().collect(Collectors.joining(" ")));
      sb.append(System.lineSeparator());
      return invalid(List.of(SMFParseError.of(
        this.reader.position(),
        sb.toString(),
        Optional.empty())));
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
    public Validation<Seq<SMFErrorType>, SMFSchema> parseSchema()
    {
      try {
        final List<String> lines =
          List.ofAll(IOUtils.readLines(this.stream, StandardCharsets.UTF_8));
        final SMFTLineReaderType reader =
          SMFTLineReaderList.create(this.uri, lines, 1);

        try {
          final Optional<List<String>> line = reader.line();
          if (line.isPresent()) {
            final Validation<Seq<SMFErrorType>, SMFSchemaVersion> r_version =
              this.parseVersion(reader.position(), line.get());
            if (r_version.isInvalid()) {
              return invalid(r_version.getError());
            }

            final SMFSchemaVersion version = r_version.get();
            if (version.major() == 1) {
              return new ParserV1(version, reader, this.uri).parseSchema();
            }

            throw new UnimplementedCodeException();
          }
          final SMFParseError error = SMFParseError.of(
            reader.position(),
            "Empty file: Must begin with an smf-schema version declaration",
            Optional.empty());
          return invalid(List.of(error));
        } catch (final IOException e) {
          final SMFParseError error =
            SMFParseError.of(reader.position(), "I/O error", Optional.of(e));
          return invalid(List.of(error));
        }
      } catch (final IOException e) {
        final SMFParseError error =
          SMFParseError.of(
            LexicalPosition.of(1, 0, Optional.of(this.uri)),
            "I/O error",
            Optional.of(e));
        return invalid(List.of(error));
      }
    }

    private Validation<Seq<SMFErrorType>, SMFSchemaVersion> parseVersion(
      final LexicalPosition<URI> position,
      final List<String> line)
    {
      if (line.size() == 3) {
        final String name = line.get(0);
        if (!Objects.equals(name, "smf-schema")) {
          return invalid(List.of(
            this.unparseableVersionList(line, Optional.empty())));
        }

        try {
          final int major = Integer.parseUnsignedInt(line.get(1));
          final int minor = Integer.parseUnsignedInt(line.get(2));
          return valid(SMFSchemaVersion.of(major, minor));
        } catch (final NumberFormatException e) {
          return invalid(List.of(
            this.unparseableVersionList(line, Optional.of(e))));
        }
      }

      return invalid(List.of(
        this.unparseableVersionList(line, Optional.empty())));
    }

    private SMFParseError unparseableVersionList(
      final List<String> line,
      final Optional<Exception> exception)
    {
      return this.unparseableVersion(
        line.toJavaStream().collect(Collectors.joining(" ")),
        exception);
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
