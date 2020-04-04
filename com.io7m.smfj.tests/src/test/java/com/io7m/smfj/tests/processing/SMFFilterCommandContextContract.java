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

package com.io7m.smfj.tests.processing;

import com.io7m.smfj.processing.api.SMFFilterCommandContext;
import java.nio.file.FileSystem;
import java.nio.file.Path;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class SMFFilterCommandContextContract
{
  private static final Logger LOG;

  static {
    LOG = LoggerFactory.getLogger(SMFFilterCommandContextContract.class);
  }

  private static void tryResolve(
    final Path source_root,
    final Path current_path,
    final Path target,
    final Path resolved)
  {
    LOG.debug("source root:  {}", source_root);
    LOG.debug("current path: {}", current_path);
    LOG.debug("target:       {}", target);
    LOG.debug("resolved:     {}", resolved);

    final SMFFilterCommandContext c =
      SMFFilterCommandContext.of(source_root, current_path);

    Assertions.assertEquals(source_root, c.sourceRoot());
    Assertions.assertEquals(current_path, c.currentPath());
    Assertions.assertEquals(resolved, c.resolvePath(target));
  }

  protected abstract FileSystem newFilesystem();

  @Test
  public final void testSourceRootNotAbsolute()
    throws Exception
  {
    try (FileSystem fs = this.newFilesystem()) {
      Assertions.assertThrows(IllegalArgumentException.class, () -> {
        SMFFilterCommandContext.of(fs.getPath("a"), fs.getPath("a"));
      });
    }
  }

  @Test
  public final void testResolveOK_0()
    throws Exception
  {
    try (FileSystem fs = this.newFilesystem()) {
      final Path root = fs.getRootDirectories().iterator().next();
      final Path source_root = root.resolve("x/y");
      final Path current_path = root.resolve("x/y/a/b");
      final Path target = fs.getPath("m");
      final Path resolved = root.resolve("x/y/a/m");
      tryResolve(source_root, current_path, target, resolved);
    }
  }

  @Test
  public final void testResolveOK_1()
    throws Exception
  {
    try (FileSystem fs = this.newFilesystem()) {
      final Path root = fs.getRootDirectories().iterator().next();
      final Path source_root = root.resolve("x/y");
      final Path current_path = root.resolve("x/y/a/b");
      final Path target = root.resolve("m");
      final Path resolved = root.resolve("x/y/m");
      tryResolve(source_root, current_path, target, resolved);
    }
  }

  @Test
  public final void testResolveError_0()
    throws Exception
  {
    try (FileSystem fs = this.newFilesystem()) {
      final Path root = fs.getRootDirectories().iterator().next();
      final Path source_root = root.resolve("x/y");
      final Path current_path = root.resolve("x/y");
      final Path target = fs.getPath("../m");
      final Path resolved = root.resolve("x/m");

      Assertions.assertThrows(IllegalArgumentException.class, () -> {
        tryResolve(source_root, current_path, target, resolved);
      });
    }
  }
}
