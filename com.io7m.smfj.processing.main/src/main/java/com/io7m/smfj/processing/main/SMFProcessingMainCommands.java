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

package com.io7m.smfj.processing.main;

import com.io7m.smfj.processing.api.SMFFilterCommandModule;
import com.io7m.smfj.processing.api.SMFFilterCommandModuleProviderAbstract;
import javaslang.Tuple;
import javaslang.collection.TreeMap;
import org.osgi.service.component.annotations.Component;

/**
 * A module provider for the core commands.
 */

@Component
public final class SMFProcessingMainCommands extends
  SMFFilterCommandModuleProviderAbstract
{
  /**
   * Construct the module provider.
   */

  public SMFProcessingMainCommands()
  {
    super(SMFFilterCommandModule.of(
      "com.io7m.smf",
      TreeMap.ofEntries(
        Tuple.of(
          SMFMemoryMeshFilterAttributeResample.NAME,
          SMFMemoryMeshFilterAttributeResample::parse),
        Tuple.of(
          SMFMemoryMeshFilterApplicationInfoAdd.NAME,
          SMFMemoryMeshFilterApplicationInfoAdd::parse),
        Tuple.of(
          SMFMemoryMeshFilterMetadataAdd.NAME,
          SMFMemoryMeshFilterMetadataAdd::parse),
        Tuple.of(
          SMFMemoryMeshFilterMetadataRemove.NAME,
          SMFMemoryMeshFilterMetadataRemove::parse),
        Tuple.of(
          SMFMemoryMeshFilterSchemaValidate.NAME,
          SMFMemoryMeshFilterSchemaValidate::parse),
        Tuple.of(
          SMFMemoryMeshFilterAttributeRemove.NAME,
          SMFMemoryMeshFilterAttributeRemove::parse),
        Tuple.of(
          SMFMemoryMeshFilterAttributeRename.NAME,
          SMFMemoryMeshFilterAttributeRename::parse),
        Tuple.of(
          SMFMemoryMeshFilterAttributeTrim.NAME,
          SMFMemoryMeshFilterAttributeTrim::parse),
        Tuple.of(
          SMFMemoryMeshFilterCheck.NAME,
          SMFMemoryMeshFilterCheck::parse),
        Tuple.of(
          SMFMemoryMeshFilterSchemaCheck.NAME,
          SMFMemoryMeshFilterSchemaCheck::parse),
        Tuple.of(
          SMFMemoryMeshFilterSchemaSet.NAME,
          SMFMemoryMeshFilterSchemaSet::parse),
        Tuple.of(
          SMFMemoryMeshFilterTrianglesOptimize.NAME,
          SMFMemoryMeshFilterTrianglesOptimize::parse))));
  }
}
