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

package com.io7m.smfj.format.text.implementation;

/**
 * State flags.
 */

public final class Flags
{
  private Flags()
  {

  }

  /**
   * Are vertices required?
   */

  public static final int VERTICES_REQUIRED = 0;

  /**
   * Are vertices received?
   */

  public static final int VERTICES_RECEIVED = 1;

  /**
   * Are triangles required?
   */

  public static final int TRIANGLES_REQUIRED = 2;

  /**
   * Are triangles received?
   */

  public static final int TRIANGLES_RECEIVED = 3;
}
