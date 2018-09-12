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

import com.io7m.smfj.processing.api.SMFFilterCommandModuleResolver;
import com.io7m.smfj.processing.api.SMFFilterCommandModuleResolverType;
import com.io7m.smfj.processing.api.SMFFilterCommandModuleType;
import io.vavr.collection.SortedMap;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public final class SMFFilterCommandModuleResolverTest
{
  @Rule public ExpectedException expected = ExpectedException.none();

  @Test
  public void testResolver()
    throws Exception
  {
    final SMFFilterCommandModuleResolverType r = SMFFilterCommandModuleResolver.create();
    final SortedMap<String, SMFFilterCommandModuleType> m = r.available();

    Assert.assertEquals(1L, (long) m.size());
    Assert.assertTrue(m.containsKey("com.io7m.smf"));
  }
}
