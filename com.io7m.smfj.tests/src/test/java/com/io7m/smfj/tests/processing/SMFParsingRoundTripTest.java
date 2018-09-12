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

package com.io7m.smfj.tests.processing;

import com.io7m.smfj.core.SMFAttributeName;
import com.io7m.smfj.core.SMFFormatVersion;
import com.io7m.smfj.format.text.SMFFormatText;
import com.io7m.smfj.parser.api.SMFParserSequentialType;
import com.io7m.smfj.processing.api.SMFAttributeArrayType;
import com.io7m.smfj.processing.api.SMFMemoryMesh;
import com.io7m.smfj.processing.api.SMFMemoryMeshProducer;
import com.io7m.smfj.processing.api.SMFMemoryMeshProducerType;
import com.io7m.smfj.processing.api.SMFMemoryMeshSerializer;
import com.io7m.smfj.serializer.api.SMFSerializerType;
import io.vavr.Tuple2;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;

import static java.nio.file.StandardOpenOption.READ;

public final class SMFParsingRoundTripTest
{
  private static final Logger LOG;

  static {
    LOG = LoggerFactory.getLogger(SMFParsingRoundTripTest.class);
  }

  @Test
  public void testRoundTripSequentialText()
    throws Exception
  {
    final SMFMemoryMeshProducerType loader0 = SMFMemoryMeshProducer.create();

    try (SMFParserSequentialType parser =
           SMFTestFiles.createParser(loader0, "all.smft")) {
      // createParser calls parse()
    }

    loader0.errors().forEach(e -> {
      LOG.error("parse: {}", e.fullMessage());
      e.exception().ifPresent(ex -> LOG.error("parse exception: ", ex));
    });

    final SMFMemoryMesh mesh0 = loader0.mesh();

    final Path tmp = Files.createTempFile("smf-roundtrip-", ".smft");
    LOG.debug("tmp file: {}", tmp);

    final SMFFormatText fmt = new SMFFormatText();

    try (OutputStream out = Files.newOutputStream(tmp)) {
      final SMFFormatVersion version = SMFFormatVersion.of(1, 0);
      try (SMFSerializerType s =
             fmt.serializerCreate(version, tmp.toUri(), out)) {
        SMFMemoryMeshSerializer.serialize(loader0.mesh(), s);
      }
    }

    final SMFMemoryMeshProducerType loader1 = SMFMemoryMeshProducer.create();

    try (InputStream stream = Files.newInputStream(tmp, READ)) {
      try (SMFParserSequentialType p =
             fmt.parserCreateSequential(loader1, tmp.toUri(), stream)) {
        p.parse();
      }
    }

    loader1.errors().forEach(e -> {
      LOG.error("parse: {}", e.fullMessage());
      e.exception().ifPresent(ex -> LOG.error("parse exception: ", ex));
    });

    final SMFMemoryMesh mesh1 = loader1.mesh();
    Assert.assertEquals(mesh0.header(), mesh1.header());
    Assert.assertEquals(
      (long) mesh0.arrays().size(),
      (long) mesh1.arrays().size());

    for (final Tuple2<SMFAttributeName, SMFAttributeArrayType> pair : mesh0.arrays()) {
      final SMFAttributeArrayType array0 = pair._2;
      final SMFAttributeArrayType array1 = mesh1.arrays().get(pair._1).get();
      Assert.assertEquals(array0, array1);
    }

    Assert.assertEquals(mesh0.triangles(), mesh1.triangles());
    Assert.assertEquals(mesh0.metadata(), mesh1.metadata());
    Assert.assertEquals(mesh0, mesh1);
  }
}
