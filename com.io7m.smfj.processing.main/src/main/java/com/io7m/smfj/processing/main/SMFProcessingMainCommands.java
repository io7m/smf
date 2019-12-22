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
import com.io7m.smfj.processing.api.SMFFilterCommandParserType;
import java.util.Map;
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
    super(SMFFilterCommandModule.of("com.io7m.smf", module()));

  }

  private static Map<String, SMFFilterCommandParserType> module()
  {
    return Map.ofEntries(
      Map.entry(
        SMFMemoryMeshFilterEndiannessSet.NAME,
        SMFMemoryMeshFilterEndiannessSet::parse
      ),
      Map.entry(
        SMFMemoryMeshFilterAttributeResample.NAME,
        SMFMemoryMeshFilterAttributeResample::parse
      ),
      Map.entry(
        SMFMemoryMeshFilterApplicationInfoAdd.NAME,
        SMFMemoryMeshFilterApplicationInfoAdd::parse
      ),
      Map.entry(
        SMFMemoryMeshFilterMetadataAdd.NAME,
        SMFMemoryMeshFilterMetadataAdd::parse
      ),
      Map.entry(
        SMFMemoryMeshFilterMetadataRemove.NAME,
        SMFMemoryMeshFilterMetadataRemove::parse
      ),
      Map.entry(
        SMFMemoryMeshFilterSchemaValidate.NAME,
        SMFMemoryMeshFilterSchemaValidate::parse
      ),
      Map.entry(
        SMFMemoryMeshFilterAttributeRemove.NAME,
        SMFMemoryMeshFilterAttributeRemove::parse
      ),
      Map.entry(
        SMFMemoryMeshFilterAttributeRename.NAME,
        SMFMemoryMeshFilterAttributeRename::parse
      ),
      Map.entry(
        SMFMemoryMeshFilterAttributeTrim.NAME,
        SMFMemoryMeshFilterAttributeTrim::parse
      ),
      Map.entry(
        SMFMemoryMeshFilterCheck.NAME,
        SMFMemoryMeshFilterCheck::parse
      ),
      Map.entry(
        SMFMemoryMeshFilterSchemaCheck.NAME,
        SMFMemoryMeshFilterSchemaCheck::parse
      ),
      Map.entry(
        SMFMemoryMeshFilterSchemaSet.NAME,
        SMFMemoryMeshFilterSchemaSet::parse
      ),
      Map.entry(
        SMFMemoryMeshFilterTrianglesOptimize.NAME,
        SMFMemoryMeshFilterTrianglesOptimize::parse
      )
    );
  }
}
