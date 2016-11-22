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

package com.io7m.smfj.tests.core;

import com.io7m.smfj.core.SMFAttributeName;
import com.io7m.smfj.core.SMFAttributeNames;
import org.hamcrest.core.StringStartsWith;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class SMFAttributeNameTest
{
  private static final Logger LOG;

  static {
    LOG = LoggerFactory.getLogger(SMFAttributeNameTest.class);
  }

  @Rule public final ExpectedException expected = ExpectedException.none();

  @Test public void testValid()
  {
    final String[] valids = {
      "abcdefghijklmnopqrstuvwxyz",
      "0123456789",
      "_",
      "-",
      ".",
      "Ямогуестьстекло",
      "Μπορώ"
    };

    final boolean[] ok = new boolean[valids.length];
    for (int index = 0; index < valids.length; ++index) {
      ok[index] = SMFAttributeNames.isValid(valids[index]);

      try {
        SMFAttributeName.of(valids[index]);
      } catch (final IllegalArgumentException e) {
        ok[index] = false;
      }

      if (!ok[index]) {
        LOG.error("'{}' erroneously invalid", valids[index]);
      } else {
        LOG.info("'{}' correctly valid", valids[index]);
      }
    }

    boolean all_ok = true;
    for (int index = 0; index < valids.length; ++index) {
      all_ok = ok[index] && all_ok;
    }

    if (!all_ok) Assert.fail();
  }

  @Test public void testInvalid()
  {
    final String[] invalids = {
      "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA",
      "\\",
      "\"",
      ",",
      "@",
      "$",
      "a a",
      "",
      "'"
    };

    final boolean[] ok = new boolean[invalids.length];
    for (int index = 0; index < invalids.length; ++index) {
      ok[index] = !SMFAttributeNames.isValid(invalids[index]);

      try {
        SMFAttributeName.of(invalids[index]);
        ok[index] = false;
      } catch (final IllegalArgumentException e) {
        ok[index] = ok[index] && true;
      }

      if (!ok[index]) {
        LOG.error("'{}' erroneously valid", invalids[index]);
      } else {
        LOG.info("'{}' correctly invalid", invalids[index]);
      }
    }

    boolean all_ok = true;
    for (int index = 0; index < invalids.length; ++index) {
      all_ok = ok[index] && all_ok;
    }

    if (!all_ok) Assert.fail();
  }
}
