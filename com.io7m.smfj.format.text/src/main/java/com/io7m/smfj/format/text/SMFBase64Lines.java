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

package com.io7m.smfj.format.text;

import com.io7m.junreachable.UnreachableCodeException;

import java.util.Arrays;
import java.util.Base64;
import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Base64 encoding.
 */

public final class SMFBase64Lines
{
  private static final Pattern SPLIT_PATTERN = Pattern.compile(
    "(?<=\\G.{72})");

  private SMFBase64Lines()
  {
    throw new UnreachableCodeException();
  }

  /**
   * Serialize the given binary data into lines of at most 72 characters of Base64 text. The lines
   * do not have terminating line breaks.
   *
   * @param data The data
   *
   * @return A set of lines
   */

  public static List<String> toBase64Lines(
    final byte[] data)
  {
    Objects.requireNonNull(data, "Data");
    final Base64.Encoder encoder = Base64.getUrlEncoder();
    final String text = encoder.encodeToString(data);
    return Arrays.asList(SPLIT_PATTERN.split(text));
  }

  /**
   * Parse binary data from the given lines of Base64 encoded data.
   *
   * @param lines A set of lines of Base64 encoded data
   *
   * @return The decoded binary data
   */

  public static byte[] fromBase64Lines(
    final List<String> lines)
  {
    Objects.requireNonNull(lines, "Lines");

    final String text =
      lines.stream().map(String::trim).collect(Collectors.joining()).trim();
    final Base64.Decoder decoder = Base64.getUrlDecoder();
    return decoder.decode(text);
  }
}
