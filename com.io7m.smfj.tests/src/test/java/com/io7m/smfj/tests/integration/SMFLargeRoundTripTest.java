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


package com.io7m.smfj.tests.integration;

import com.io7m.junreachable.UnreachableCodeException;
import com.io7m.smfj.core.SMFAttribute;
import com.io7m.smfj.format.binary2.SMFFormatBinary2;
import com.io7m.smfj.format.text.SMFFormatText;
import com.io7m.smfj.format.xml.SMFFormatXML;
import com.io7m.smfj.parser.api.SMFParserProviderType;
import com.io7m.smfj.processing.api.SMFAttributeArrayFloating1Type;
import com.io7m.smfj.processing.api.SMFAttributeArrayFloating2Type;
import com.io7m.smfj.processing.api.SMFAttributeArrayFloating3Type;
import com.io7m.smfj.processing.api.SMFAttributeArrayFloating4Type;
import com.io7m.smfj.processing.api.SMFAttributeArrayType;
import com.io7m.smfj.processing.api.SMFMemoryMesh;
import com.io7m.smfj.processing.api.SMFMemoryMeshProducer;
import com.io7m.smfj.processing.api.SMFMemoryMeshSerializer;
import com.io7m.smfj.serializer.api.SMFSerializerProviderType;
import com.io7m.smfj.tests.TestDirectories;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.net.URI;
import java.nio.ByteOrder;
import java.nio.file.Files;
import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class SMFLargeRoundTripTest
{
  private static final Logger LOG =
    LoggerFactory.getLogger(SMFLargeRoundTripTest.class);

  private static InputStream resource(
    final String name)
    throws Exception
  {
    final var path = String.format("/com/io7m/smfj/tests/integration/%s", name);
    final var url = SMFLargeRoundTripTest.class.getResource(path);
    if (url == null) {
      throw new FileNotFoundException(path);
    }
    return url.openStream();
  }

  private static <T extends SMFParserProviderType & SMFSerializerProviderType>
  void runTrips(
    final String fileName,
    final ByteOrder byteOrder,
    final int triangleSize,
    final T initialFormat,
    final List<T> formats)
    throws Exception
  {
    final var directory =
      TestDirectories.temporaryDirectory();

    final var memoryMeshes = SMFMemoryMeshProducer.create();

    try (var stream = resource(fileName)) {
      try (var parser = initialFormat.parserCreateSequential(
        memoryMeshes,
        URI.create("urn:initial"),
        stream)) {
        parser.parse();
      }
    }

    final var origHeader =
      memoryMeshes.mesh()
        .header();

    final var newHeader =
      origHeader
        .withDataByteOrder(byteOrder)
        .withTriangles(origHeader.triangles()
                         .withTriangleIndexSizeBits(triangleSize));

    var meshCurrent =
      memoryMeshes.mesh()
        .withHeader(newHeader);

    var meshPrevious = meshCurrent;
    int process = 0;
    for (final var format : formats) {
      final var memoryMeshesNow = SMFMemoryMeshProducer.create();

      final var file =
        directory.resolve(String.format("%d.out", Integer.valueOf(process)));

      LOG.debug("writing {}", file);

      try (var output = Files.newOutputStream(file)) {
        try (var writer = format.serializerCreate(
          format.parserSupportedVersions().last(),
          file.toUri(),
          output)) {
          SMFMemoryMeshSerializer.serialize(meshCurrent, writer);
        }
      }

      try (var input = Files.newInputStream(file)) {
        try (var reader = format.parserCreateSequential(
          memoryMeshesNow,
          file.toUri(),
          input)) {
          reader.parse();
        }
      }

      meshPrevious = meshCurrent;
      meshCurrent = memoryMeshesNow.mesh();
      compareMeshes(meshPrevious, meshCurrent);
      ++process;
    }
  }

  private static void compareMeshes(
    final SMFMemoryMesh meshPrevious,
    final SMFMemoryMesh meshCurrent)
  {
    Assertions.assertEquals(
      meshPrevious.header(),
      meshCurrent.header(),
      "Header matches"
    );
    Assertions.assertEquals(
      meshPrevious.triangles(),
      meshCurrent.triangles(),
      "Triangles match"
    );
    Assertions.assertEquals(
      meshPrevious.arrays().keySet(),
      meshCurrent.arrays().keySet(),
      "Attribute names match"
    );

    for (final var name : meshPrevious.arrays().keySet()) {
      final var arrayPrevious =
        meshPrevious.arrays().get(name);
      final var arrayCurrent =
        meshCurrent.arrays().get(name);
      final var attribute =
        meshCurrent.header().attributesByName().get(name);

      switch (attribute.componentType()) {
        case ELEMENT_TYPE_INTEGER_UNSIGNED:
        case ELEMENT_TYPE_INTEGER_SIGNED:
          Assertions.assertEquals(
            arrayPrevious,
            arrayCurrent,
            String.format("Array for attribute %s matches", name.value()));
          break;
        case ELEMENT_TYPE_FLOATING:
          compareFloatingArrays(
            meshCurrent,
            arrayPrevious,
            arrayCurrent,
            attribute);
          break;
      }
    }

    Assertions.assertEquals(
      meshPrevious.metadata(),
      meshCurrent.metadata(),
      "Metadata matches"
    );
  }

  private static void compareFloatingArrays(
    final SMFMemoryMesh meshCurrent,
    final SMFAttributeArrayType arrayPrevious,
    final SMFAttributeArrayType arrayCurrent,
    final SMFAttribute attribute)
  {
    var delta = 0.000_000_000_001;
    if (attribute.componentSizeBits() <= 32) {
      delta = 0.000_001;
    }
    if (attribute.componentSizeBits() <= 16) {
      delta = 0.001;
    }

    final var vertexCount = meshCurrent.header().vertexCount();
    switch (attribute.componentCount()) {
      case 1: {
        final var arrayPrev =
          (SMFAttributeArrayFloating1Type) arrayPrevious;
        final var arrayCurr =
          (SMFAttributeArrayFloating1Type) arrayCurrent;

        for (long index = 0L;
             Long.compareUnsigned(index, vertexCount) < 0;
             ++index) {
          final var vp = arrayPrev.values().get((int) index);
          final var vc = arrayCurr.values().get((int) index);
          Assertions.assertEquals(
            vp.doubleValue(),
            vc.doubleValue(),
            delta);
        }
        break;
      }
      case 2: {
        final var arrayPrev =
          (SMFAttributeArrayFloating2Type) arrayPrevious;
        final var arrayCurr =
          (SMFAttributeArrayFloating2Type) arrayCurrent;

        for (long index = 0L;
             Long.compareUnsigned(index, vertexCount) < 0;
             ++index) {
          final var vp = arrayPrev.values().get((int) index);
          final var vc = arrayCurr.values().get((int) index);
          Assertions.assertEquals(vp.x(), vc.x(), delta);
          Assertions.assertEquals(vp.y(), vc.y(), delta);
        }
        break;
      }
      case 3: {
        final var arrayPrev =
          (SMFAttributeArrayFloating3Type) arrayPrevious;
        final var arrayCurr =
          (SMFAttributeArrayFloating3Type) arrayCurrent;

        for (long index = 0L;
             Long.compareUnsigned(index, vertexCount) < 0;
             ++index) {
          final var vp = arrayPrev.values().get((int) index);
          final var vc = arrayCurr.values().get((int) index);
          Assertions.assertEquals(vp.x(), vc.x(), delta);
          Assertions.assertEquals(vp.y(), vc.y(), delta);
          Assertions.assertEquals(vp.z(), vc.z(), delta);
        }
        break;
      }
      case 4: {
        final var arrayPrev =
          (SMFAttributeArrayFloating4Type) arrayPrevious;
        final var arrayCurr =
          (SMFAttributeArrayFloating4Type) arrayCurrent;

        for (long index = 0L;
             Long.compareUnsigned(index, vertexCount) < 0;
             ++index) {
          final var vp = arrayPrev.values().get((int) index);
          final var vc = arrayCurr.values().get((int) index);
          Assertions.assertEquals(vp.x(), vc.x(), delta);
          Assertions.assertEquals(vp.y(), vc.y(), delta);
          Assertions.assertEquals(vp.z(), vc.z(), delta);
          Assertions.assertEquals(vp.w(), vc.w(), delta);
        }
        break;
      }
      default: {
        throw new UnreachableCodeException();
      }
    }
  }

  @Test
  public void testRoundTrip_BE_8_TBXTBX()
    throws Exception
  {
    runTrips(
      "all.smft",
      ByteOrder.BIG_ENDIAN,
      8,
      new SMFFormatText(),
      List.of(
        new SMFFormatText(),
        new SMFFormatBinary2(),
        new SMFFormatXML(),
        new SMFFormatText(),
        new SMFFormatBinary2(),
        new SMFFormatXML()
      ));
  }

  @Test
  public void testRoundTrip_BE_8_BTXBTX()
    throws Exception
  {
    runTrips(
      "smfFull_validAll0.smfb",
      ByteOrder.BIG_ENDIAN,
      8,
      new SMFFormatBinary2(),
      List.of(
        new SMFFormatBinary2(),
        new SMFFormatText(),
        new SMFFormatXML(),
        new SMFFormatText(),
        new SMFFormatBinary2(),
        new SMFFormatXML()
      ));
  }

  @Test
  public void testRoundTrip_LE_8_TBXTBX()
    throws Exception
  {
    runTrips(
      "all.smft",
      ByteOrder.LITTLE_ENDIAN,
      8,
      new SMFFormatText(),
      List.of(
        new SMFFormatText(),
        new SMFFormatBinary2(),
        new SMFFormatXML(),
        new SMFFormatText(),
        new SMFFormatBinary2(),
        new SMFFormatXML()
      ));
  }

  @Test
  public void testRoundTrip_LE_8_BTXBTX()
    throws Exception
  {
    runTrips(
      "smfFull_validAll0.smfb",
      ByteOrder.LITTLE_ENDIAN,
      8,
      new SMFFormatBinary2(),
      List.of(
        new SMFFormatBinary2(),
        new SMFFormatText(),
        new SMFFormatXML(),
        new SMFFormatText(),
        new SMFFormatBinary2(),
        new SMFFormatXML()
      ));
  }


  @Test
  public void testRoundTrip_BE_16_TBXTBX()
    throws Exception
  {
    runTrips(
      "all.smft",
      ByteOrder.BIG_ENDIAN,
      16,
      new SMFFormatText(),
      List.of(
        new SMFFormatText(),
        new SMFFormatBinary2(),
        new SMFFormatXML(),
        new SMFFormatText(),
        new SMFFormatBinary2(),
        new SMFFormatXML()
      ));
  }

  @Test
  public void testRoundTrip_BE_16_BTXBTX()
    throws Exception
  {
    runTrips(
      "smfFull_validAll0.smfb",
      ByteOrder.BIG_ENDIAN,
      16,
      new SMFFormatBinary2(),
      List.of(
        new SMFFormatBinary2(),
        new SMFFormatText(),
        new SMFFormatXML(),
        new SMFFormatText(),
        new SMFFormatBinary2(),
        new SMFFormatXML()
      ));
  }

  @Test
  public void testRoundTrip_LE_16_TBXTBX()
    throws Exception
  {
    runTrips(
      "all.smft",
      ByteOrder.LITTLE_ENDIAN,
      16,
      new SMFFormatText(),
      List.of(
        new SMFFormatText(),
        new SMFFormatBinary2(),
        new SMFFormatXML(),
        new SMFFormatText(),
        new SMFFormatBinary2(),
        new SMFFormatXML()
      ));
  }

  @Test
  public void testRoundTrip_LE_16_BTXBTX()
    throws Exception
  {
    runTrips(
      "smfFull_validAll0.smfb",
      ByteOrder.LITTLE_ENDIAN,
      16,
      new SMFFormatBinary2(),
      List.of(
        new SMFFormatBinary2(),
        new SMFFormatText(),
        new SMFFormatXML(),
        new SMFFormatText(),
        new SMFFormatBinary2(),
        new SMFFormatXML()
      ));
  }


  @Test
  public void testRoundTrip_BE_32_TBXTBX()
    throws Exception
  {
    runTrips(
      "all.smft",
      ByteOrder.BIG_ENDIAN,
      32,
      new SMFFormatText(),
      List.of(
        new SMFFormatText(),
        new SMFFormatBinary2(),
        new SMFFormatXML(),
        new SMFFormatText(),
        new SMFFormatBinary2(),
        new SMFFormatXML()
      ));
  }

  @Test
  public void testRoundTrip_BE_32_BTXBTX()
    throws Exception
  {
    runTrips(
      "smfFull_validAll0.smfb",
      ByteOrder.BIG_ENDIAN,
      32,
      new SMFFormatBinary2(),
      List.of(
        new SMFFormatBinary2(),
        new SMFFormatText(),
        new SMFFormatXML(),
        new SMFFormatText(),
        new SMFFormatBinary2(),
        new SMFFormatXML()
      ));
  }

  @Test
  public void testRoundTrip_LE_32_TBXTBX()
    throws Exception
  {
    runTrips(
      "all.smft",
      ByteOrder.LITTLE_ENDIAN,
      32,
      new SMFFormatText(),
      List.of(
        new SMFFormatText(),
        new SMFFormatBinary2(),
        new SMFFormatXML(),
        new SMFFormatText(),
        new SMFFormatBinary2(),
        new SMFFormatXML()
      ));
  }

  @Test
  public void testRoundTrip_LE_32_BTXBTX()
    throws Exception
  {
    runTrips(
      "smfFull_validAll0.smfb",
      ByteOrder.LITTLE_ENDIAN,
      32,
      new SMFFormatBinary2(),
      List.of(
        new SMFFormatBinary2(),
        new SMFFormatText(),
        new SMFFormatXML(),
        new SMFFormatText(),
        new SMFFormatBinary2(),
        new SMFFormatXML()
      ));
  }


  @Test
  public void testRoundTrip_BE_64_TBXTBX()
    throws Exception
  {
    runTrips(
      "all.smft",
      ByteOrder.BIG_ENDIAN,
      64,
      new SMFFormatText(),
      List.of(
        new SMFFormatText(),
        new SMFFormatBinary2(),
        new SMFFormatXML(),
        new SMFFormatText(),
        new SMFFormatBinary2(),
        new SMFFormatXML()
      ));
  }

  @Test
  public void testRoundTrip_BE_64_BTXBTX()
    throws Exception
  {
    runTrips(
      "smfFull_validAll0.smfb",
      ByteOrder.BIG_ENDIAN,
      64,
      new SMFFormatBinary2(),
      List.of(
        new SMFFormatBinary2(),
        new SMFFormatText(),
        new SMFFormatXML(),
        new SMFFormatText(),
        new SMFFormatBinary2(),
        new SMFFormatXML()
      ));
  }

  @Test
  public void testRoundTrip_LE_64_TBXTBX()
    throws Exception
  {
    runTrips(
      "all.smft",
      ByteOrder.LITTLE_ENDIAN,
      64,
      new SMFFormatText(),
      List.of(
        new SMFFormatText(),
        new SMFFormatBinary2(),
        new SMFFormatXML(),
        new SMFFormatText(),
        new SMFFormatBinary2(),
        new SMFFormatXML()
      ));
  }

  @Test
  public void testRoundTrip_LE_64_BTXBTX()
    throws Exception
  {
    runTrips(
      "smfFull_validAll0.smfb",
      ByteOrder.LITTLE_ENDIAN,
      64,
      new SMFFormatBinary2(),
      List.of(
        new SMFFormatBinary2(),
        new SMFFormatText(),
        new SMFFormatXML(),
        new SMFFormatText(),
        new SMFFormatBinary2(),
        new SMFFormatXML()
      ));
  }
}
