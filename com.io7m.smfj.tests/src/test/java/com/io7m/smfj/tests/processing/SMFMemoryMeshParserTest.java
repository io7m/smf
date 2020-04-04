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
import com.io7m.smfj.parser.api.SMFParserSequentialType;
import com.io7m.smfj.processing.api.SMFAttributeArrayType;
import com.io7m.smfj.processing.api.SMFMemoryMesh;
import com.io7m.smfj.processing.api.SMFMemoryMeshParser;
import com.io7m.smfj.processing.api.SMFMemoryMeshProducer;
import com.io7m.smfj.processing.api.SMFMemoryMeshProducerType;
import java.util.Map;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.io7m.smfj.tests.processing.SMFMemoryMeshFilterTesting.WarningsAllowed.WARNINGS_DISALLOWED;

public final class SMFMemoryMeshParserTest
{
  private static final Logger LOG =
    LoggerFactory.getLogger(SMFMemoryMeshParserTest.class);

  @Test
  public void testRoundTripSequential()
    throws Exception
  {
    final SMFMemoryMeshProducerType loader0 = SMFMemoryMeshProducer.create();

    try (var parser =
           SMFTestFiles.createParser(loader0, "all.smft")) {
      SMFMemoryMeshFilterTesting.logEverything(LOG, loader0, WARNINGS_DISALLOWED);
    }

    final SMFMemoryMesh mesh0 = loader0.mesh();
    final SMFMemoryMeshProducerType loader1 = SMFMemoryMeshProducer.create();

    try (var parser =
           SMFMemoryMeshParser.createSequential(mesh0, loader1)) {
      parser.parse();
    }

    final SMFMemoryMesh mesh1 = loader1.mesh();
    Assertions.assertEquals(mesh0.header(), mesh1.header());
    Assertions.assertEquals(mesh0.metadata(), mesh1.metadata());

    for (final Map.Entry<SMFAttributeName, SMFAttributeArrayType> pair : mesh0.arrays().entrySet()) {
      final SMFAttributeArrayType array0 = pair.getValue();
      final SMFAttributeArrayType array1 = mesh1.arrays().get(pair.getKey());
      Assertions.assertEquals(array0, array1);
    }

    Assertions.assertEquals(mesh0.triangles(), mesh1.triangles());
  }
}
