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

package com.io7m.smfj.format.text;

import com.io7m.jaffirm.core.Invariants;
import com.io7m.jlexing.core.LexicalPositionMutable;
import com.io7m.jnull.NullCheck;
import com.io7m.junreachable.UnreachableCodeException;
import com.io7m.smfj.core.SMFAttribute;
import com.io7m.smfj.core.SMFAttributeName;
import com.io7m.smfj.core.SMFAttributeType;
import com.io7m.smfj.core.SMFComponentType;
import com.io7m.smfj.core.SMFFormatDescription;
import com.io7m.smfj.core.SMFFormatVersion;
import com.io7m.smfj.parser.api.SMFParseError;
import com.io7m.smfj.parser.api.SMFParserEventsType;
import com.io7m.smfj.parser.api.SMFParserProviderType;
import com.io7m.smfj.parser.api.SMFParserRandomAccessType;
import com.io7m.smfj.parser.api.SMFParserSequentialType;
import javaslang.collection.HashMap;
import javaslang.collection.List;
import javaslang.collection.Map;
import javaslang.collection.Seq;
import javaslang.collection.Set;
import javaslang.collection.SortedSet;
import javaslang.collection.TreeSet;
import javaslang.control.Option;
import javaslang.control.Validation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.Collection;
import java.util.HashSet;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

import static javaslang.control.Validation.invalid;
import static javaslang.control.Validation.valid;

/**
 * The implementation of the text format.
 */

public final class SMFFormatText implements SMFParserProviderType
{
  private static final Logger LOG;
  private static final SMFFormatDescription FORMAT;

  static {
    LOG = LoggerFactory.getLogger(SMFFormatText.class);

    {
      final SMFFormatDescription.Builder b = SMFFormatDescription.builder();
      b.setDescription("A plain text encoding of SMF data");
      b.setMimeType("text/vnd.io7m.smf");
      b.setName("smft");
      b.setRandomAccess(false);
      b.setSuffix("smft");
      FORMAT = b.build();
    }

  }

  /**
   * Construct a text format provider.
   */

  public SMFFormatText()
  {

  }

  @Override
  public SMFFormatDescription parserFormat()
  {
    return FORMAT;
  }

  @Override
  public SortedSet<SMFFormatVersion> parserSupportedVersions()
  {
    return TreeSet.of(SMFFormatVersion.of(1, 0));
  }

  @Override
  public SMFParserSequentialType parserCreateSequential(
    final SMFParserEventsType in_events,
    final Path in_path,
    final InputStream in_stream)
  {
    NullCheck.notNull(in_events, "Events");
    NullCheck.notNull(in_path, "Path");
    NullCheck.notNull(in_stream, "Stream");
    return new Parser(
      in_events,
      new LineReader(in_path, in_stream),
      new AtomicReference<>(ParserState.STATE_INITIAL));
  }

  @Override
  public SMFParserRandomAccessType parserCreateRandomAccess(
    final SMFParserEventsType events,
    final Path path,
    final FileChannel file)
    throws UnsupportedOperationException
  {
    throw new UnsupportedOperationException(
      "Random access parsing is not supported");
  }

  private enum ParserState
  {
    STATE_INITIAL,
    STATE_PARSING,
    STATE_FINISHED
  }

  private interface LineParserType
  {
    void onLine(
      List<String> line);
  }

  private static final class LineReader
  {
    private final BufferedReader reader;
    private final LexicalPositionMutable<Path> position;
    private final SMFLineLexer lexer;

    LineReader(
      final Path in_path,
      final InputStream in_stream)
    {
      this.reader = new BufferedReader(
        new InputStreamReader(
          NullCheck.notNull(in_stream, "stream"), StandardCharsets.UTF_8));
      this.position = LexicalPositionMutable.create(0, 0, Optional.of(in_path));
      this.lexer = new SMFLineLexer();
    }

    public Optional<List<String>> line()
      throws Exception
    {
      final String line = this.reader.readLine();
      this.position.setLine(Math.addExact(this.position.line(), 1));

      if (line == null) {
        return Optional.empty();
      }

      final String trimmed = line.trim();
      if (trimmed.isEmpty()) {
        return Optional.of(List.empty());
      }

      if (trimmed.startsWith("#")) {
        return Optional.of(List.empty());
      }

      return Optional.of(this.lexer.lex(trimmed));
    }
  }

  private static final class V1HeaderParser extends AbstractParser
  {
    private final AbstractParser parent;
    private final SMFFormatVersion version;
    private boolean ok_vertices;
    private boolean ok_triangles;
    private Map<SMFAttributeName, SMFAttribute> attributes;
    private Map<SMFAttributeName, Integer> attribute_lines;
    private List<SMFAttribute> attributes_list;
    private long vertex_count;
    private long triangle_count;
    private long triangle_size;

    V1HeaderParser(
      final AbstractParser in_parent,
      final SMFParserEventsType in_events,
      final LineReader in_reader,
      final SMFFormatVersion in_version)
    {
      super(in_events, in_reader, in_parent.state);
      this.parent = NullCheck.notNull(in_parent, "Parent");
      this.version = NullCheck.notNull(in_version, "Version");
      this.attribute_lines = HashMap.empty();
      this.attributes_list = List.empty();
      this.attributes = HashMap.empty();
      this.ok_vertices = false;
      this.ok_triangles = false;
    }

    @Override
    public void parse()
    {
      LOG.debug("parsing header");

      try {
        super.events.onHeaderStart();

        this.parseHeaderCommands();

        if (super.state.get() != ParserState.STATE_FINISHED) {
          this.parseHeaderCheckUniqueAttributeNames();
        }

        if (super.state.get() != ParserState.STATE_FINISHED) {
          super.events.onHeaderAttributeCountReceived(
            (long) this.attributes_list.size());

          for (final SMFAttribute attribute : this.attributes_list) {
            super.events.onHeaderAttributeReceived(attribute);
          }

          if (this.ok_vertices) {
            super.events.onHeaderVerticesCountReceived(this.vertex_count);
          }

          if (this.ok_triangles) {
            super.events.onHeaderTrianglesCountReceived(this.triangle_count);
            super.events.onHeaderTrianglesIndexSizeReceived(this.triangle_size);
          }
        }

      } catch (final Exception e) {
        this.fail(e.getMessage());
      } finally {
        super.events.onHeaderFinish();
      }
    }

    private void parseHeaderCommands()
      throws Exception
    {
      while (true) {
        final Optional<List<String>> line_opt = super.reader.line();
        if (!line_opt.isPresent()) {
          this.fail("Unexpected EOF");
          return;
        }

        LOG.debug("line: {}", line_opt.get());
        final List<String> line = line_opt.get();
        if (line.isEmpty()) {
          continue;
        }

        switch (line.get(0)) {
          case "data": {
            if (line.size() == 1) {
              return;
            }

            super.failExpectedGot(
              "Incorrect number of arguments",
              "data",
              line.toJavaStream().collect(Collectors.joining(" ")));
            return;
          }

          case "vertices": {
            this.parseHeaderCommandVertices(line);
            break;
          }

          case "triangles": {
            this.parseHeaderCommandTriangles(line);
            break;
          }

          case "attribute": {
            this.parseHeaderCommandAttribute(line);
            break;
          }

          default: {
            super.failExpectedGot(
              "Unrecognized command.",
              "attribute | triangles | vertices | data",
              line.toJavaStream().collect(Collectors.joining(" ")));
            return;
          }
        }
      }
    }

    private void parseHeaderCommandAttribute(
      final Seq<String> line)
    {
      if (line.size() == 5) {
        try {
          final SMFAttributeName name =
            SMFAttributeName.of(line.get(1));
          final SMFComponentType type =
            SMFComponentType.of(line.get(2));
          final int count =
            Integer.parseUnsignedInt(line.get(3));
          final int size =
            Integer.parseUnsignedInt(line.get(4));
          final SMFAttribute attr =
            SMFAttribute.of(name, type, count, size);

          this.attribute_lines = this.attribute_lines.put(
            name, Integer.valueOf(super.reader.position.line()));
          this.attributes_list = this.attributes_list.append(attr);
        } catch (final IllegalArgumentException e) {
          super.failExpectedGot(
            e.getMessage(),
            "attribute <attribute-name> <component-type> <component-count> <component-size>",
            line.toJavaStream().collect(Collectors.joining(" ")));
        }
      } else {
        super.failExpectedGot(
          "Incorrect number of arguments",
          "attribute <attribute-name> <component-type> <component-count> <component-size>",
          line.toJavaStream().collect(Collectors.joining(" ")));
      }
    }

    private void parseHeaderCommandTriangles(
      final Seq<String> line)
    {
      if (line.size() == 3) {
        try {
          this.triangle_count = Long.parseUnsignedLong(line.get(1));
          this.triangle_size = Long.parseUnsignedLong(line.get(2));
          this.ok_triangles = true;
        } catch (final NumberFormatException e) {
          super.failExpectedGot(
            "Cannot parse number: " + e.getMessage(),
            "triangles <triangle-count> <triangle-index-size>",
            line.toJavaStream().collect(Collectors.joining(" ")));
        }
      } else {
        super.failExpectedGot(
          "Incorrect number of arguments",
          "triangles <triangle-count> <triangle-index-size>",
          line.toJavaStream().collect(Collectors.joining(" ")));
      }
    }

    private void parseHeaderCommandVertices(
      final Seq<String> line)
    {
      if (line.size() == 2) {
        try {
          this.vertex_count = Long.parseUnsignedLong(line.get(1));
          this.ok_vertices = true;
        } catch (final NumberFormatException e) {
          super.failExpectedGot(
            "Cannot parse number: " + e.getMessage(),
            "vertices <vertex-count>",
            line.toJavaStream().collect(Collectors.joining(" ")));
        }
      } else {
        super.failExpectedGot(
          "Incorrect number of arguments",
          "vertices <vertex-count>",
          line.toJavaStream().collect(Collectors.joining(" ")));
      }
    }

    private void parseHeaderCheckUniqueAttributeNames()
    {
      final Collection<SMFAttributeName> names =
        new HashSet<>(this.attributes_list.size());

      for (final SMFAttribute attribute : this.attributes_list) {
        final SMFAttributeName name = attribute.name();
        if (names.contains(name)) {
          Invariants.checkInvariant(
            name,
            this.attribute_lines.containsKey(name),
            a_name -> "Attribute lines must contain " + a_name);

          this.failWithLineNumber(
            this.attribute_lines.get(name).get().intValue(),
            "Duplicate attribute name: " + name.value());
        } else {
          this.attributes = this.attributes.put(name, attribute);
        }
        names.add(name);
      }
    }
  }

  private static abstract class AbstractParser implements
    SMFParserSequentialType
  {
    private final LineReader reader;
    private final SMFParserEventsType events;
    private final AtomicReference<ParserState> state;

    AbstractParser(
      final SMFParserEventsType in_events,
      final LineReader in_reader,
      final AtomicReference<ParserState> in_state)
    {
      this.events = NullCheck.notNull(in_events, "Events");
      this.reader = NullCheck.notNull(in_reader, "Reader");
      this.state = NullCheck.notNull(in_state, "state");
    }

    protected final SMFParseError makeErrorExpectedGot(
      final String message,
      final String expected,
      final String received)
    {
      final StringBuilder sb = new StringBuilder(128);
      sb.append(message);
      sb.append(System.lineSeparator());
      sb.append("  Expected: ");
      sb.append(expected);
      sb.append(System.lineSeparator());
      sb.append("  Received: ");
      sb.append(received);
      sb.append(System.lineSeparator());
      return this.makeError(sb.toString());
    }

    private SMFParseError makeError(
      final String message)
    {
      return SMFParseError.of(this.reader.position.toImmutable(), message);
    }


    private SMFParseError makeErrorWithLine(
      final int line,
      final String message)
    {
      return SMFParseError.of(
        this.reader.position.toImmutable().withLine(line), message);
    }

    protected final void fail(
      final String message)
    {
      this.onFailure(this.makeError(message));
    }

    private void failExpectedGot(
      final String message,
      final String expected,
      final String received)
    {
      this.onFailure(this.makeErrorExpectedGot(message, expected, received));
    }

    protected final void failErrors(
      final Iterable<SMFParseError> errors)
    {
      errors.forEach(this::onFailure);
    }

    private void onFailure(
      final SMFParseError error)
    {
      LOG.trace("failure: {}", error);
      this.state.set(ParserState.STATE_FINISHED);
      this.events.onError(error);
    }

    protected final void failWithLineNumber(
      final int line,
      final String message)
    {
      this.onFailure(this.makeErrorWithLine(line, message));
    }

    @Override
    public void close()
      throws IOException
    {

    }
  }

  private static final class V1BodyParser extends AbstractParser
  {
    private final AbstractParser parent;
    private final SMFFormatVersion version;
    private final Map<SMFAttributeName, SMFAttribute> attributes;
    private final long vertex_count;
    private final long triangle_count;
    private final long triangle_size;
    private Map<SMFAttributeName, Boolean> attributes_ok;
    private Map<SMFAttributeName, Boolean> attributes_attempted;
    private boolean ok_triangles;

    V1BodyParser(
      final AbstractParser in_parent,
      final SMFParserEventsType in_events,
      final LineReader in_reader,
      final SMFFormatVersion in_version,
      final Map<SMFAttributeName, SMFAttribute> in_attributes,
      final long in_vertex_count,
      final long in_triangle_count,
      final long in_triangle_size)
    {
      super(in_events, in_reader, in_parent.state);
      this.parent = NullCheck.notNull(in_parent, "Parent");
      this.version = NullCheck.notNull(in_version, "Version");
      this.attributes = NullCheck.notNull(in_attributes, "attributes");
      this.attributes_ok = HashMap.empty();
      this.attributes_attempted = HashMap.empty();
      this.vertex_count = in_vertex_count;
      this.triangle_count = in_triangle_count;
      this.triangle_size = in_triangle_size;
    }

    @Override
    public void parse()
    {
      LOG.debug("parsing body");

      try {
        while (true) {
          final Optional<List<String>> line_opt = super.reader.line();
          if (!line_opt.isPresent()) {
            this.onEOF();
            return;
          }

          LOG.debug("line: {}", line_opt.get());
          final List<String> line = line_opt.get();
          if (line.isEmpty()) {
            continue;
          }

          switch (line.get(0)) {
            case "triangles": {
              if (line.size() == 1) {
                this.parseTriangles();
              } else {
                super.failExpectedGot(
                  "Incorrect number of arguments",
                  "triangles",
                  line.toJavaStream().collect(Collectors.joining(" ")));
                return;
              }
              break;
            }

            case "attribute": {
              if (line.size() == 2) {
                SMFAttributeName name = null;

                try {
                  name = SMFAttributeName.of(line.get(1));
                } catch (final IllegalArgumentException e) {
                  super.failExpectedGot(
                    e.getMessage(),
                    "attribute",
                    line.toJavaStream().collect(Collectors.joining(" ")));
                  return;
                }

                this.parseAttribute(name);
              } else {
                super.failExpectedGot(
                  "Incorrect number of arguments",
                  "attribute <attribute-name>",
                  line.toJavaStream().collect(Collectors.joining(" ")));
                return;
              }
              break;
            }

            default: {
              super.failExpectedGot(
                "Unrecognized command.",
                "attribute | triangles",
                line.toJavaStream().collect(Collectors.joining(" ")));
              return;
            }
          }
        }
      } catch (final Exception e) {
        this.fail(e.getMessage());
      }
    }

    private void parseAttribute(
      final SMFAttributeName name)
      throws Exception
    {
      final Option<SMFAttribute> attribute_opt = this.attributes.get(name);
      if (attribute_opt.isDefined()) {
        final SMFAttribute attribute = attribute_opt.get();

        if (this.attributes_attempted.containsKey(name)) {
          super.fail(
            "An attempt has already been made to supply data for attribute " + name.value());
          return;
        }

        this.attributes_attempted =
          this.attributes_attempted.put(name, Boolean.TRUE);

        try {
          super.events.onDataAttributeStart(attribute);

          long parsed_vertices = 0L;
          while (parsed_vertices != this.vertex_count) {
            final Optional<List<String>> line_opt = super.reader.line();
            if (!line_opt.isPresent()) {
              this.onEOF();
              return;
            }

            LOG.debug("line: {}", line_opt.get());
            final List<String> line = line_opt.get();
            if (line.isEmpty()) {
              continue;
            }

            this.parseAttributeElement(attribute, line);
            parsed_vertices = Math.addExact(parsed_vertices, 1L);
          }

          LOG.debug("finished attribute {}", name.value());
          this.attributes_ok = this.attributes_ok.put(name, Boolean.TRUE);
        } finally {
          super.events.onDataAttributeFinish(attribute);
        }

      } else {
        super.fail("No such attribute: " + name.value());
      }
    }

    private void onEOF()
    {
      if (!this.isBodyDone()) {
        this.failMissedAttributes();
        this.failMissedTriangles();
        this.fail("Unexpected EOF");
      }
    }

    private void failMissedTriangles()
    {
      if (!this.ok_triangles) {
        this.fail("Too few triangles specified");
      }
    }

    private void failMissedAttributes()
    {
      final Set<SMFAttributeName> names = this.attributes.keySet().diff(
        this.attributes_attempted.keySet());
      if (!names.isEmpty()) {
        names.forEach(
          name -> this.fail("No data specified for attribute: " + name.value()));
      }
    }

    private void parseAttributeElement(
      final SMFAttributeType attribute,
      final Seq<String> line)
    {
      switch (attribute.componentType()) {
        case ELEMENT_TYPE_INTEGER_SIGNED: {
          switch (attribute.componentCount()) {
            case 1: {
              this.parseAttributeElementSigned1(line);
              break;
            }

            case 2: {
              this.parseAttributeElementSigned2(line);
              break;
            }

            case 3: {
              this.parseAttributeElementSigned3(line);
              break;
            }

            case 4: {
              this.parseAttributeElementSigned4(line);
              break;
            }

            default: {
              throw new UnreachableCodeException();
            }
          }
          break;
        }

        case ELEMENT_TYPE_INTEGER_UNSIGNED: {
          switch (attribute.componentCount()) {
            case 1: {
              this.parseAttributeElementUnsigned1(line);
              break;
            }

            case 2: {
              this.parseAttributeElementUnsigned2(line);
              break;
            }

            case 3: {
              this.parseAttributeElementUnsigned3(line);
              break;
            }

            case 4: {
              this.parseAttributeElementUnsigned4(line);
              break;
            }

            default: {
              throw new UnreachableCodeException();
            }
          }
          break;
        }

        case ELEMENT_TYPE_FLOATING: {
          switch (attribute.componentCount()) {
            case 1: {
              this.parseAttributeElementFloat1(line);
              break;
            }

            case 2: {
              this.parseAttributeElementFloat2(line);
              break;
            }

            case 3: {
              this.parseAttributeElementFloat3(line);
              break;
            }

            case 4: {
              this.parseAttributeElementFloat4(line);
              break;
            }

            default: {
              throw new UnreachableCodeException();
            }
          }
          break;
        }
      }
    }

    private void parseAttributeElementFloat4(
      final Seq<String> line)
    {
      if (line.length() == 4) {
        try {
          final double x = Double.parseDouble(line.get(0));
          final double y = Double.parseDouble(line.get(1));
          final double z = Double.parseDouble(line.get(2));
          final double w = Double.parseDouble(line.get(3));
          super.events.onDataAttributeValueFloat4(x, y, z, w);
        } catch (final NumberFormatException e) {
          super.failExpectedGot(
            "Cannot parse number: " + e.getMessage(),
            "<float> <float> <float> <float>",
            line.toJavaStream().collect(Collectors.joining(" ")));
        }
      } else {
        super.failExpectedGot(
          "Incorrect number of arguments",
          "<float> <float> <float> <float>",
          line.toJavaStream().collect(Collectors.joining(" ")));
      }
    }

    private void parseAttributeElementFloat3(
      final Seq<String> line)
    {
      if (line.length() == 3) {
        try {
          final double x = Double.parseDouble(line.get(0));
          final double y = Double.parseDouble(line.get(1));
          final double z = Double.parseDouble(line.get(2));
          super.events.onDataAttributeValueFloat3(x, y, z);
        } catch (final NumberFormatException e) {
          super.failExpectedGot(
            "Cannot parse number: " + e.getMessage(),
            "<float> <float> <float>",
            line.toJavaStream().collect(Collectors.joining(" ")));
        }
      } else {
        super.failExpectedGot(
          "Incorrect number of arguments",
          "<float> <float> <float>",
          line.toJavaStream().collect(Collectors.joining(" ")));
      }
    }

    private void parseAttributeElementFloat2(
      final Seq<String> line)
    {
      if (line.length() == 2) {
        try {
          final double x = Double.parseDouble(line.get(0));
          final double y = Double.parseDouble(line.get(1));
          super.events.onDataAttributeValueFloat2(x, y);
        } catch (final NumberFormatException e) {
          super.failExpectedGot(
            "Cannot parse number: " + e.getMessage(),
            "<float> <float>",
            line.toJavaStream().collect(Collectors.joining(" ")));
        }
      } else {
        super.failExpectedGot(
          "Incorrect number of arguments",
          "<float> <float>",
          line.toJavaStream().collect(Collectors.joining(" ")));
      }
    }

    private void parseAttributeElementFloat1(
      final Seq<String> line)
    {
      if (line.length() == 1) {
        try {
          final double x = Double.parseDouble(line.get(0));
          super.events.onDataAttributeValueFloat1(x);
        } catch (final NumberFormatException e) {
          super.failExpectedGot(
            "Cannot parse number: " + e.getMessage(),
            "<float>",
            line.toJavaStream().collect(Collectors.joining(" ")));
        }
      } else {
        super.failExpectedGot(
          "Incorrect number of arguments",
          "<float>",
          line.toJavaStream().collect(Collectors.joining(" ")));
      }
    }

    private void parseAttributeElementUnsigned4(
      final Seq<String> line)
    {
      if (line.length() == 4) {
        try {
          final long x = Long.parseUnsignedLong(line.get(0));
          final long y = Long.parseUnsignedLong(line.get(1));
          final long z = Long.parseUnsignedLong(line.get(2));
          final long w = Long.parseUnsignedLong(line.get(3));
          super.events.onDataAttributeValueIntegerUnsigned4(x, y, z, w);
        } catch (final NumberFormatException e) {
          super.failExpectedGot(
            "Cannot parse number: " + e.getMessage(),
            "<integer-unsigned> <integer-unsigned> <integer-unsigned> <integer-unsigned>",
            line.toJavaStream().collect(Collectors.joining(" ")));
        }
      } else {
        super.failExpectedGot(
          "Incorrect number of arguments",
          "<integer-unsigned> <integer-unsigned> <integer-unsigned> <integer-unsigned>",
          line.toJavaStream().collect(Collectors.joining(" ")));
      }
    }

    private void parseAttributeElementUnsigned3(
      final Seq<String> line)
    {
      if (line.length() == 3) {
        try {
          final long x = Long.parseUnsignedLong(line.get(0));
          final long y = Long.parseUnsignedLong(line.get(1));
          final long z = Long.parseUnsignedLong(line.get(2));
          super.events.onDataAttributeValueIntegerUnsigned3(x, y, z);
        } catch (final NumberFormatException e) {
          super.failExpectedGot(
            "Cannot parse number: " + e.getMessage(),
            "<integer-unsigned> <integer-unsigned> <integer-unsigned>",
            line.toJavaStream().collect(Collectors.joining(" ")));
        }
      } else {
        super.failExpectedGot(
          "Incorrect number of arguments",
          "<integer-unsigned> <integer-unsigned> <integer-unsigned>",
          line.toJavaStream().collect(Collectors.joining(" ")));
      }
    }

    private void parseAttributeElementUnsigned2(
      final Seq<String> line)
    {
      if (line.length() == 2) {
        try {
          final long x = Long.parseUnsignedLong(line.get(0));
          final long y = Long.parseUnsignedLong(line.get(1));
          super.events.onDataAttributeValueIntegerUnsigned2(x, y);
        } catch (final NumberFormatException e) {
          super.failExpectedGot(
            "Cannot parse number: " + e.getMessage(),
            "<integer-unsigned> <integer-unsigned>",
            line.toJavaStream().collect(Collectors.joining(" ")));
        }
      } else {
        super.failExpectedGot(
          "Incorrect number of arguments",
          "<integer-unsigned> <integer-unsigned>",
          line.toJavaStream().collect(Collectors.joining(" ")));
      }
    }

    private void parseAttributeElementUnsigned1(
      final Seq<String> line)
    {
      if (line.length() == 1) {
        try {
          final long x = Long.parseUnsignedLong(line.get(0));
          super.events.onDataAttributeValueIntegerUnsigned1(x);
        } catch (final NumberFormatException e) {
          super.failExpectedGot(
            "Cannot parse number: " + e.getMessage(),
            "<integer-unsigned>",
            line.toJavaStream().collect(Collectors.joining(" ")));
        }
      } else {
        super.failExpectedGot(
          "Incorrect number of arguments",
          "<integer-unsigned>",
          line.toJavaStream().collect(Collectors.joining(" ")));
      }
    }

    private void parseAttributeElementSigned4(
      final Seq<String> line)
    {
      if (line.length() == 4) {
        try {
          final long x = Long.parseLong(line.get(0));
          final long y = Long.parseLong(line.get(1));
          final long z = Long.parseLong(line.get(2));
          final long w = Long.parseLong(line.get(3));
          super.events.onDataAttributeValueIntegerSigned4(x, y, z, w);
        } catch (final NumberFormatException e) {
          super.failExpectedGot(
            "Cannot parse number: " + e.getMessage(),
            "<integer-signed> <integer-signed> <integer-signed> <integer-signed>",
            line.toJavaStream().collect(Collectors.joining(" ")));
        }
      } else {
        super.failExpectedGot(
          "Incorrect number of arguments",
          "<integer-signed> <integer-signed> <integer-signed> <integer-signed>",
          line.toJavaStream().collect(Collectors.joining(" ")));
      }
    }

    private void parseAttributeElementSigned3(
      final Seq<String> line)
    {
      if (line.length() == 3) {
        try {
          final long x = Long.parseLong(line.get(0));
          final long y = Long.parseLong(line.get(1));
          final long z = Long.parseLong(line.get(2));
          super.events.onDataAttributeValueIntegerSigned3(x, y, z);
        } catch (final NumberFormatException e) {
          super.failExpectedGot(
            "Cannot parse number: " + e.getMessage(),
            "<integer-signed> <integer-signed> <integer-signed>",
            line.toJavaStream().collect(Collectors.joining(" ")));
        }
      } else {
        super.failExpectedGot(
          "Incorrect number of arguments",
          "<integer-signed> <integer-signed> <integer-signed>",
          line.toJavaStream().collect(Collectors.joining(" ")));
      }
    }

    private void parseAttributeElementSigned2(
      final Seq<String> line)
    {
      if (line.length() == 2) {
        try {
          final long x = Long.parseLong(line.get(0));
          final long y = Long.parseLong(line.get(1));
          super.events.onDataAttributeValueIntegerSigned2(x, y);
        } catch (final NumberFormatException e) {
          super.failExpectedGot(
            "Cannot parse number: " + e.getMessage(),
            "<integer-signed> <integer-signed>",
            line.toJavaStream().collect(Collectors.joining(" ")));
        }
      } else {
        super.failExpectedGot(
          "Incorrect number of arguments",
          "<integer-signed> <integer-signed>",
          line.toJavaStream().collect(Collectors.joining(" ")));
      }
    }

    private void parseAttributeElementSigned1(
      final Seq<String> line)
    {
      if (line.length() == 1) {
        try {
          final long x = Long.parseLong(line.get(0));
          super.events.onDataAttributeValueIntegerSigned1(x);
        } catch (final NumberFormatException e) {
          super.failExpectedGot(
            "Cannot parse number: " + e.getMessage(),
            "<integer-signed>",
            line.toJavaStream().collect(Collectors.joining(" ")));
        }
      } else {
        super.failExpectedGot(
          "Incorrect number of arguments",
          "<integer-signed>",
          line.toJavaStream().collect(Collectors.joining(" ")));
      }
    }

    private void parseTriangles()
      throws Exception
    {
      try {
        super.events.onDataTrianglesStart();

        long parsed_triangles = 0L;
        while (parsed_triangles != this.triangle_count) {
          final Optional<List<String>> line_opt = super.reader.line();
          if (!line_opt.isPresent()) {
            this.onEOF();
            return;
          }

          LOG.debug("line: {}", line_opt.get());
          final List<String> line = line_opt.get();
          if (line.isEmpty()) {
            continue;
          }

          if (line.length() == 3) {
            try {
              final long v0 = Long.parseUnsignedLong(line.get(0));
              final long v1 = Long.parseUnsignedLong(line.get(1));
              final long v2 = Long.parseUnsignedLong(line.get(2));
              super.events.onDataTriangle(v0, v1, v2);
            } catch (final NumberFormatException e) {
              super.failExpectedGot(
                "Cannot parse number: " + e.getMessage(),
                "<integer-unsigned> <integer-unsigned> <integer-unsigned>",
                line.toJavaStream().collect(Collectors.joining(" ")));
              return;
            }
          } else {
            super.failExpectedGot(
              "Incorrect number of arguments",
              "<integer-unsigned> <integer-unsigned> <integer-unsigned>",
              line.toJavaStream().collect(Collectors.joining(" ")));
            return;
          }

          parsed_triangles = Math.addExact(parsed_triangles, 1L);
        }

        LOG.debug("finished all triangles");
        this.ok_triangles = true;
      } finally {
        super.events.onDataTrianglesFinish();
      }
    }

    private boolean isBodyDone()
    {
      final boolean attribute_size_ok =
        this.attributes_ok.size() == this.attributes.size();
      final boolean attribute_all_done =
        this.attributes_ok.foldRight(
          Boolean.TRUE,
          (p, x) -> Boolean.valueOf(p._2.booleanValue() && x.booleanValue())).booleanValue();

      LOG.trace("triangles done: {}", Boolean.valueOf(this.ok_triangles));
      LOG.trace("attributes size: {}", Boolean.valueOf(attribute_size_ok));
      LOG.trace("attributes done: {}", Boolean.valueOf(attribute_all_done));
      return this.ok_triangles && attribute_size_ok && attribute_all_done;
    }
  }

  private static final class V1Parser extends AbstractParser
  {
    private final AbstractParser parent;
    private final SMFFormatVersion version;
    private final V1HeaderParser header_parser;

    V1Parser(
      final AbstractParser in_parent,
      final SMFParserEventsType in_events,
      final LineReader in_reader,
      final SMFFormatVersion in_version)
    {
      super(in_events, in_reader, in_parent.state);
      this.parent = NullCheck.notNull(in_parent, "Parent");
      this.version = NullCheck.notNull(in_version, "Version");
      this.header_parser =
        new V1HeaderParser(this, in_events, in_reader, in_version);
    }

    @Override
    public void parse()
    {
      this.header_parser.parse();

      if (super.state.get() != ParserState.STATE_FINISHED) {
        final SMFParserSequentialType body_parser =
          new V1BodyParser(
            this,
            super.events,
            super.reader,
            this.version,
            this.header_parser.attributes,
            this.header_parser.vertex_count,
            this.header_parser.triangle_count,
            this.header_parser.triangle_size);
        body_parser.parse();
      }
    }
  }

  private static final class Parser extends AbstractParser
  {
    Parser(
      final SMFParserEventsType in_events,
      final LineReader in_reader,
      final AtomicReference<ParserState> in_state)
    {
      super(in_events, in_reader, in_state);
    }

    @Override
    public void parse()
    {
      if (super.state.get() != ParserState.STATE_INITIAL) {
        throw new IllegalStateException("Parser has already executed");
      }

      super.state.set(ParserState.STATE_PARSING);
      try {
        super.events.onStart();

        final Optional<List<String>> line = super.reader.line();
        if (line.isPresent()) {
          final Validation<List<SMFParseError>, SMFFormatVersion> v_version =
            this.onParseVersion(line.get());

          if (v_version.isValid()) {
            final SMFFormatVersion version = v_version.get();
            super.events.onVersionReceived(version);
            switch (version.major()) {
              case 1: {
                LOG.debug("instantiating 1.* parser");
                new V1Parser(this, super.events, super.reader, version).parse();
                return;
              }
              default: {
                LOG.debug("no parser for version {}", version);
                this.fail("Unsupported version");
                return;
              }
            }
          }

          this.failErrors(v_version.getError());
          return;
        }
        this.fail("Unexpected EOF");
      } catch (final Exception e) {
        this.fail(e.getMessage());
      }
    }

    private Validation<List<SMFParseError>, SMFFormatVersion> onParseVersion(
      final List<String> line)
    {
      if (line.isEmpty()) {
        return invalid(List.of(this.makeErrorExpectedGot(
          "The first line must be a version declaration.",
          "smf <version-major> <version-minor>",
          line.toJavaStream().collect(Collectors.joining(" ")))));
      }

      switch (line.get(0)) {
        case "smf": {
          if (line.length() != 3) {
            return invalid(List.of(this.makeErrorExpectedGot(
              "Incorrect number of arguments.",
              "smf <version-major> <version-minor>",
              line.toJavaStream().collect(Collectors.joining(" ")))));
          }

          try {
            final int major = Integer.parseUnsignedInt(line.get(1));
            final int minor = Integer.parseUnsignedInt(line.get(2));
            return valid(SMFFormatVersion.of(major, minor));
          } catch (final NumberFormatException e) {
            return invalid(List.of(this.makeErrorExpectedGot(
              "Cannot parse number: " + e.getMessage(),
              "smf <version-major> <version-minor>",
              line.toJavaStream().collect(Collectors.joining(" ")))));
          }
        }
        default: {
          return invalid(List.of(this.makeErrorExpectedGot(
            "Unrecognized command.",
            "smf <version-major> <version-minor>",
            line.toJavaStream().collect(Collectors.joining(" ")))));
        }
      }
    }

    @Override
    public void close()
      throws IOException
    {
      LOG.debug("closing parser");
      super.events.onFinish();
    }
  }
}
