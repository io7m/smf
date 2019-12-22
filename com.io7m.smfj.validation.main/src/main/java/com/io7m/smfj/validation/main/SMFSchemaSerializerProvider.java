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

import com.io7m.smfj.core.SMFAttributeName;
import com.io7m.smfj.core.SMFComponentType;
import com.io7m.smfj.core.SMFCoordinateSystem;
import com.io7m.smfj.core.SMFSchemaIdentifier;
import com.io7m.smfj.validation.api.SMFSchema;
import com.io7m.smfj.validation.api.SMFSchemaAttribute;
import com.io7m.smfj.validation.api.SMFSchemaSerializerProviderType;
import com.io7m.smfj.validation.api.SMFSchemaSerializerType;
import com.io7m.smfj.validation.api.SMFSchemaVersion;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.SortedSet;
import java.util.TreeSet;
import org.osgi.service.component.annotations.Component;

/**
 * The default implementation of the {@link SMFSchemaSerializerProviderType} interface.
 */

@Component
public final class SMFSchemaSerializerProvider implements
  SMFSchemaSerializerProviderType
{
  private static final SortedSet<SMFSchemaVersion> SUPPORTED = makeSupported();

  /**
   * Construct a serializer provider.
   */

  public SMFSchemaSerializerProvider()
  {

  }

  private static SortedSet<SMFSchemaVersion> makeSupported()
  {
    final var versions = new TreeSet<SMFSchemaVersion>();
    versions.add(SMFSchemaVersion.of(1, 0));
    return Collections.unmodifiableSortedSet(versions);
  }

  @Override
  public SortedSet<SMFSchemaVersion> schemaSupportedVersions()
  {
    return SUPPORTED;
  }

  @Override
  public SMFSchemaSerializerType schemaSerializerCreate(
    final SMFSchemaVersion version,
    final Path path,
    final OutputStream stream)
  {
    Objects.requireNonNull(version, "Version");
    Objects.requireNonNull(path, "Path");
    Objects.requireNonNull(stream, "Stream");

    if (version.major() == 1) {
      return new SerializerV1(version, stream);
    }

    throw new UnsupportedOperationException("Unsupported schema version");
  }

  private static final class SerializerV1 implements SMFSchemaSerializerType
  {
    private final SMFSchemaVersion version;
    private final OutputStream stream;

    SerializerV1(
      final SMFSchemaVersion in_version,
      final OutputStream in_stream)
    {
      this.version = in_version;
      this.stream = in_stream;
    }

    private static void serializeAttribute(
      final BufferedWriter writer,
      final String requirement,
      final Map.Entry<SMFAttributeName, SMFSchemaAttribute> entry)
      throws IOException
    {
      final SMFAttributeName name = entry.getKey();
      final SMFSchemaAttribute attr = entry.getValue();

      writer.append("attribute ");
      writer.append(requirement);
      writer.append(" \"");
      writer.append(name.value());
      writer.append("\" ");

      final Optional<SMFComponentType> opt_type = attr.requiredComponentType();
      if (opt_type.isPresent()) {
        writer.append(opt_type.get().getName());
      } else {
        writer.append("-");
      }
      writer.append(" ");

      final OptionalInt opt_count = attr.requiredComponentCount();
      if (opt_count.isPresent()) {
        writer.append(Integer.toUnsignedString(opt_count.getAsInt()));
      } else {
        writer.append("-");
      }
      writer.append(" ");

      final OptionalInt opt_size = attr.requiredComponentSize();
      if (opt_size.isPresent()) {
        writer.append(Integer.toUnsignedString(opt_size.getAsInt()));
      } else {
        writer.append("-");
      }
      writer.newLine();
    }

    @Override
    public void close()
      throws IOException
    {
      this.stream.close();
    }

    @Override
    public void serializeSchema(final SMFSchema schema)
      throws IOException
    {
      final BufferedWriter writer = new BufferedWriter(
        new OutputStreamWriter(this.stream, StandardCharsets.UTF_8));

      writer.append("smf-schema ");
      writer.append(Integer.toUnsignedString(this.version.major()));
      writer.append(" ");
      writer.append(Integer.toUnsignedString(this.version.minor()));
      writer.newLine();

      final SMFSchemaIdentifier id = schema.schemaIdentifier();
      writer.append("schema ");
      writer.append(id.name().value());
      writer.append(" ");
      writer.append(Integer.toUnsignedString(id.versionMajor()));
      writer.append(" ");
      writer.append(Integer.toUnsignedString(id.versionMinor()));
      writer.newLine();

      final Optional<SMFCoordinateSystem> coords_opt =
        schema.requiredCoordinateSystem();
      if (coords_opt.isPresent()) {
        final SMFCoordinateSystem coords = coords_opt.get();
        writer.append("coordinates ");
        writer.append(coords.toHumanString());
        writer.newLine();
      }

      switch (schema.requireVertices()) {
        case SMF_VERTICES_REQUIRED: {
          writer.append("require-vertices true");
          break;
        }
        case SMF_VERTICES_NOT_REQUIRED: {
          writer.append("require-vertices false");
          break;
        }
      }
      writer.newLine();

      switch (schema.requireTriangles()) {
        case SMF_TRIANGLES_REQUIRED: {
          writer.append("require-triangles true");
          break;
        }
        case SMF_TRIANGLES_NOT_REQUIRED: {
          writer.append("require-triangles false");
          break;
        }
      }
      writer.newLine();

      for (final Map.Entry<SMFAttributeName, SMFSchemaAttribute> entry : schema.requiredAttributes().entrySet()) {
        serializeAttribute(writer, "required", entry);
      }

      for (final Map.Entry<SMFAttributeName, SMFSchemaAttribute> entry : schema.optionalAttributes().entrySet()) {
        serializeAttribute(writer, "optional", entry);
      }

      writer.flush();
    }
  }
}
