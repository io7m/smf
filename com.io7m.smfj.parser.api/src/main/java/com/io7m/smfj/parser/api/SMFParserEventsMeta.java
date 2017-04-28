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

package com.io7m.smfj.parser.api;

import com.io7m.jnull.NullCheck;
import com.io7m.junreachable.UnreachableCodeException;
import javaslang.collection.List;

/**
 * Convenient metadata listener implementations.
 */

public final class SMFParserEventsMeta
{
  private SMFParserEventsMeta()
  {
    throw new UnreachableCodeException();
  }

  /**
   * @return A metadata listener that ignores all metadata
   */

  public static SMFParserEventsMetaType ignore()
  {
    return new Ignore();
  }

  /**
   * A metadata listener that delegates to a list of listeners. Each delegate
   * will be asked in turn (via {@link SMFParserEventsMetaType#onMeta(long,
   * long, long)}) whether or not it wants a particular piece of metadata. Each
   * delegate that wanted a piece of metadata will receive it via {@link
   * SMFParserEventsMetaType#onMetaData(long, long, byte[])}. If any delegate
   * raises an exception, the exception is propagated and the rest of the
   * delegates are not called.
   *
   * @param in_delegates A list of delegates
   *
   * @return A metadata listener that delegates to a list of listeners
   */

  public static SMFParserEventsMetaType delegating(
    final List<SMFParserEventsMetaType> in_delegates)
  {
    return new Delegating(in_delegates);
  }

  private static final class Ignore implements SMFParserEventsMetaType
  {
    private Ignore()
    {
      // Nothing
    }

    @Override
    public boolean onMeta(
      final long vendor,
      final long schema,
      final long length)
    {
      return false;
    }

    @Override
    public void onMetaData(
      final long vendor,
      final long schema,
      final byte[] data)
    {
      throw new UnreachableCodeException();
    }
  }

  private static final class Delegating implements SMFParserEventsMetaType
  {
    private final List<SMFParserEventsMetaType> delegates;
    private final boolean[] want;

    private Delegating(
      final List<SMFParserEventsMetaType> in_delegates)
    {
      this.delegates = NullCheck.notNull(in_delegates, "Delegates");
      this.want = new boolean[this.delegates.size()];
    }

    @Override
    public boolean onMeta(
      final long vendor,
      final long schema,
      final long length)
    {
      boolean any = false;
      for (int index = 0; index < this.want.length; ++index) {
        final boolean want_now =
          this.delegates.get(index).onMeta(vendor, schema, length);
        this.want[index] = want_now;
        any = any || want_now;
      }
      return any;
    }

    @Override
    public void onMetaData(
      final long vendor,
      final long schema,
      final byte[] data)
    {
      for (int index = 0; index < this.want.length; ++index) {
        if (this.want[index]) {
          this.delegates.get(index).onMetaData(vendor, schema, data);
        }
      }
    }
  }
}
