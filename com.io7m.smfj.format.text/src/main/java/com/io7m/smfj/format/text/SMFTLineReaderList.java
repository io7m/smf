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

package com.io7m.smfj.format.text;

import com.io7m.jnull.NullCheck;
import javaslang.collection.Iterator;
import javaslang.collection.List;

import java.io.IOException;
import java.nio.file.Path;

/**
 * A line reader based on a list of strings.
 */

public final class SMFTLineReaderList extends SMFTLineReaderAbstract
{
  private final Iterator<String> lines;

  private SMFTLineReaderList(
    final Path in_path,
    final List<String> in_lines,
    final int in_start)
  {
    super(in_path, in_start);
    this.lines = NullCheck.notNull(in_lines, "Lines").iterator();
  }

  /**
   * Construct a new line reader.
   *
   * @param in_path  The file path, for diagnostic messages
   * @param in_lines The stream of lines
   * @param in_start The number of the starting line, for diagnostic messages
   *
   * @return A line reader
   */

  public static SMFTLineReaderType create(
    final Path in_path,
    final List<String> in_lines,
    final int in_start)
  {
    return new SMFTLineReaderList(in_path, in_lines, in_start);
  }

  @Override
  protected String lineNextRaw()
    throws IOException
  {
    if (this.lines.hasNext()) {
      return this.lines.next();
    }
    return null;
  }
}
