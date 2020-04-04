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

package com.io7m.smfj.cmdline;

import com.beust.jcommander.Parameters;
import com.io7m.smfj.processing.api.SMFFilterCommandModuleResolver;
import com.io7m.smfj.processing.api.SMFFilterCommandModuleResolverType;
import com.io7m.smfj.processing.api.SMFFilterCommandModuleType;
import java.util.Map;

@Parameters(commandDescription = "List available filters")
public final class CommandListFilters extends CommandRoot
{
  CommandListFilters()
  {

  }

  @Override
  public Integer call()
    throws Exception
  {
    super.call();

    final SMFFilterCommandModuleResolverType resolver =
      SMFFilterCommandModuleResolver.create();
    final Map<String, SMFFilterCommandModuleType> available =
      resolver.available();

    available.keySet().stream().sorted().forEach(moduleName -> {
      final SMFFilterCommandModuleType module = available.get(moduleName);
      module.parsers().keySet().stream().sorted().forEach(commandName -> {
        System.out.print(moduleName);
        System.out.print(":");
        System.out.print(commandName);
        System.out.println();
      });
    });

    return Integer.valueOf(0);
  }
}
