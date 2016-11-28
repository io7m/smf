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
import com.io7m.jcoords.core.conversion.CAxis;
import com.io7m.jcoords.core.conversion.CAxisSystem;
import com.io7m.jnull.NullCheck;
import com.io7m.jnull.Nullable;
import com.io7m.smfj.core.SMFAttribute;
import com.io7m.smfj.core.SMFAttributeName;
import com.io7m.smfj.core.SMFComponentType;
import com.io7m.smfj.core.SMFCoordinateSystem;
import com.io7m.smfj.core.SMFFaceWindingOrder;
import com.io7m.smfj.core.SMFFormatVersion;
import com.io7m.smfj.core.SMFHeader;
import com.io7m.smfj.core.SMFSchemaIdentifier;
import com.io7m.smfj.parser.api.SMFParserEventsType;
import javaslang.collection.HashMap;
import javaslang.collection.List;
import javaslang.collection.Map;
import javaslang.collection.Seq;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.HashSet;
import java.util.Optional;
import java.util.stream.Collectors;

final class SMFTV1HeaderParser extends SMFTAbstractParser
{
  private static final Logger LOG;

  static {
    LOG = LoggerFactory.getLogger(SMFTV1HeaderParser.class);
  }

  private final SMFTAbstractParser parent;
  private final SMFFormatVersion version;
  protected Map<SMFAttributeName, SMFAttribute> attributes;
  protected Map<SMFAttributeName, Integer> attribute_lines;
  protected List<SMFAttribute> attributes_list;
  protected long vertex_count;
  protected long triangle_count;
  protected long triangle_size;
  private boolean ok_triangles;
  private boolean ok_vertices;
  private SMFSchemaIdentifier schema_id;
  private @Nullable SMFCoordinateSystem coords;

  SMFTV1HeaderParser(
    final SMFTAbstractParser in_parent,
    final SMFParserEventsType in_events,
    final SMFTLineReader in_reader,
    final SMFFormatVersion in_version)
  {
    super(in_events, in_reader, in_parent.state);
    this.parent = NullCheck.notNull(in_parent, "Parent");
    this.version = NullCheck.notNull(in_version, "Version");
    this.attribute_lines = HashMap.empty();
    this.attributes_list = List.empty();
    this.attributes = HashMap.empty();
    this.ok_triangles = false;
    this.ok_vertices = false;
    this.schema_id = SMFSchemaIdentifier.of(0, 0, 0, 0);
  }

  private static SMFFaceWindingOrder parseWindingOrder(
    final String name)
  {
    switch (name) {
      case "clockwise":
        return SMFFaceWindingOrder.FACE_WINDING_ORDER_CLOCKWISE;
      case "counter-clockwise":
        return SMFFaceWindingOrder.FACE_WINDING_ORDER_COUNTER_CLOCKWISE;
      default: {
        throw new IllegalArgumentException("Unrecognized winding order: " + name);
      }
    }
  }

  private static CAxis parseAxis(
    final String name)
  {
    switch (name) {
      case "+x":
        return CAxis.AXIS_POSITIVE_X;
      case "+y":
        return CAxis.AXIS_POSITIVE_Y;
      case "+z":
        return CAxis.AXIS_POSITIVE_Z;
      case "-x":
        return CAxis.AXIS_NEGATIVE_X;
      case "-y":
        return CAxis.AXIS_NEGATIVE_Y;
      case "-z":
        return CAxis.AXIS_NEGATIVE_Z;
      default: {
        throw new IllegalArgumentException("Unrecognized axis name: " + name);
      }
    }
  }

  @Override
  public void parse()
  {
    this.log().debug("parsing header");

    try {
      this.parseHeaderCommands();

      if (super.state.get() != ParserState.STATE_FINISHED) {
        this.parseHeaderCheckAll();
      }

      if (super.state.get() != ParserState.STATE_FINISHED) {
        final SMFHeader.Builder hb = SMFHeader.builder();
        hb.setAttributesInOrder(this.attributes_list);
        hb.setAttributesByName(this.attributes);
        hb.setTriangleIndexSizeBits(this.triangle_size);
        hb.setTriangleCount(this.triangle_count);
        hb.setVertexCount(this.vertex_count);
        hb.setSchemaIdentifier(this.schema_id);
        hb.setCoordinateSystem(this.coords);
        super.events.onHeaderParsed(hb.build());
      }

    } catch (final Exception e) {
      this.fail(e.getMessage());
    }
  }

  private void parseHeaderCheckAll()
  {
    this.parseHeaderCheckUniqueAttributeNames();
    this.parseHeaderCheckRequired();
  }

  private void parseHeaderCheckRequired()
  {
    if (!this.ok_triangles) {
      this.fail("No triangle count was specified");
      return;
    }
    if (!this.ok_vertices) {
      this.fail("No vertex count was specified");
      return;
    }
    if (this.coords == null) {
      this.fail("No coordinate system was specified");
      return;
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

      this.log().debug("line: {}", line_opt.get());
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

        case "coordinates": {
          this.parseHeaderCommandCoordinates(line);
          break;
        }

        case "schema": {
          this.parseHeaderCommandSchema(line);
          break;
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
            "attribute | coordinates | data | triangles | vertices | schema",
            line.toJavaStream().collect(Collectors.joining(" ")));
          return;
        }
      }
    }
  }

  private void parseHeaderCommandCoordinates(
    final List<String> line)
  {
    if (line.size() == 5) {
      try {
        final CAxis axis_right = parseAxis(line.get(1));
        final CAxis axis_up = parseAxis(line.get(2));
        final CAxis axis_forward = parseAxis(line.get(3));
        final SMFFaceWindingOrder order = parseWindingOrder(line.get(4));

        boolean bad = false;
        bad = bad || axis_right.axis() == axis_up.axis();
        bad = bad || axis_up.axis() == axis_forward.axis();
        bad = bad || axis_forward.axis() == axis_right.axis();

        if (bad) {
          throw new IllegalArgumentException("Axes must be perpendicular");
        }

        this.coords = SMFCoordinateSystem.of(
          CAxisSystem.of(axis_right, axis_up, axis_forward),
          order);
      } catch (final IllegalArgumentException e) {
        super.failExpectedGot(
          e.getMessage(),
          "coordinates <axis> <axis> <axis> <winding-order>",
          line.toJavaStream().collect(Collectors.joining(" ")));
      }
    } else {
      super.failExpectedGot(
        "Incorrect number of arguments",
        "coordinates <axis> <axis> <axis> <winding-order>",
        line.toJavaStream().collect(Collectors.joining(" ")));
    }
  }

  private void parseHeaderCommandSchema(final List<String> line)
  {
    if (line.size() == 5) {
      try {
        final int vendor_id =
          Integer.parseUnsignedInt(line.get(1), 16);
        final int vendor_schema =
          Integer.parseUnsignedInt(line.get(2), 16);
        final int vendor_schema_version_major =
          Integer.parseUnsignedInt(line.get(3));
        final int vendor_schema_version_minor =
          Integer.parseUnsignedInt(line.get(4));

        this.schema_id = SMFSchemaIdentifier.of(
          vendor_id,
          vendor_schema,
          vendor_schema_version_major,
          vendor_schema_version_minor);
      } catch (final NumberFormatException e) {
        super.failExpectedGot(
          "Could not parse number: " + e.getMessage(),
          "schema <vendor-id> <schema-id> <schema-version-major> <schema-version-minor>",
          line.toJavaStream().collect(Collectors.joining(" ")));
      }
    } else {
      super.failExpectedGot(
        "Incorrect number of arguments",
        "schema <vendor-id> <schema-id> <schema-version-major> <schema-version-minor>",
        line.toJavaStream().collect(Collectors.joining(" ")));
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
          name, Integer.valueOf(super.reader.position().line()));
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

  @Override
  protected Logger log()
  {
    return LOG;
  }
}
