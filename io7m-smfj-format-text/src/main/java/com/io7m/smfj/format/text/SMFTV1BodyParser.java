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

import com.io7m.jnull.NullCheck;
import com.io7m.junreachable.UnreachableCodeException;
import com.io7m.smfj.core.SMFAttribute;
import com.io7m.smfj.core.SMFAttributeName;
import com.io7m.smfj.core.SMFAttributeType;
import com.io7m.smfj.core.SMFFormatVersion;
import com.io7m.smfj.parser.api.SMFParserEventsType;
import javaslang.collection.HashMap;
import javaslang.collection.List;
import javaslang.collection.Map;
import javaslang.collection.Seq;
import javaslang.collection.Set;
import javaslang.control.Option;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;
import java.util.stream.Collectors;

final class SMFTV1BodyParser extends SMFTAbstractParser
{
  private static final Logger LOG;

  static {
    LOG = LoggerFactory.getLogger(SMFTV1BodyParser.class);
  }

  protected final Map<SMFAttributeName, SMFAttribute> attributes;
  private final SMFTAbstractParser parent;
  private final SMFFormatVersion version;
  private final long vertex_count;
  private final long triangle_count;
  private final long triangle_size;
  private Map<SMFAttributeName, Boolean> attributes_ok;
  private Map<SMFAttributeName, Boolean> attributes_attempted;
  private long parsed_triangles;

  SMFTV1BodyParser(
    final SMFTAbstractParser in_parent,
    final SMFParserEventsType in_events,
    final SMFTLineReader in_reader,
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

          this.log().debug("line: {}", line_opt.get());
          final List<String> line = line_opt.get();
          if (line.isEmpty()) {
            continue;
          }

          this.parseAttributeElement(attribute, line);
          parsed_vertices = Math.addExact(parsed_vertices, 1L);
        }

        this.log().debug("finished attribute {}", name.value());
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
    if (this.parsed_triangles != this.triangle_count) {
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

      this.parsed_triangles = 0L;
      while (this.parsed_triangles != this.triangle_count) {
        final Optional<List<String>> line_opt = super.reader.line();
        if (!line_opt.isPresent()) {
          this.onEOF();
          return;
        }

        this.log().debug("line: {}", line_opt.get());
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

        this.parsed_triangles = Math.addExact(this.parsed_triangles, 1L);
      }

      this.log().debug("finished all triangles");
    } finally {
      super.events.onDataTrianglesFinish();
    }
  }

  private boolean isBodyDone()
  {
    final boolean ok_triangles =
      this.triangle_count == this.parsed_triangles;

    final boolean attribute_size_ok =
      this.attributes_ok.size() == this.attributes.size();
    final boolean attribute_all_done =
      this.attributes_ok.foldRight(
        Boolean.TRUE,
        (p, x) -> Boolean.valueOf(p._2.booleanValue() && x.booleanValue())).booleanValue();

    this.log().trace("triangles done: {}", Boolean.valueOf(ok_triangles));
    this.log().trace("attributes size: {}", Boolean.valueOf(attribute_size_ok));
    this.log().trace(
      "attributes done: {}",
      Boolean.valueOf(attribute_all_done));
    return ok_triangles && attribute_size_ok && attribute_all_done;
  }

  @Override
  protected Logger log()
  {
    return LOG;
  }

  @Override
  public void parseHeader()
  {
    throw new UnsupportedOperationException();
  }

  @Override
  public void parseData()
    throws IllegalStateException
  {
    switch (this.state.get()) {
      case STATE_INITIAL:
        throw new IllegalStateException("Header has not been parsed");
      case STATE_HEADER_PARSING:
        throw new IllegalStateException("Header has not been parsed");
      case STATE_HEADER_PARSED: {
        this.log().debug("parsing body");
        this.parseDataActual();

        if (!this.parserHasFailed()) {
          super.state.set(ParserState.STATE_FINISHED);
        }
        return;
      }
      case STATE_FAILED:
        throw new IllegalStateException("Parser has failed");
      case STATE_FINISHED:
        throw new IllegalStateException("Parser has already completed");
    }
  }

  private void parseDataActual()
  {
    try {
      while (true) {
        final Optional<List<String>> line_opt = super.reader.line();
        if (!line_opt.isPresent()) {
          this.onEOF();
          return;
        }

        this.log().debug("line: {}", line_opt.get());
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
}
