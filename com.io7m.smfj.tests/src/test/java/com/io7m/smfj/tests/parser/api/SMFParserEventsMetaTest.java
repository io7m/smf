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

package com.io7m.smfj.tests.parser.api;

import com.io7m.junreachable.UnreachableCodeException;
import com.io7m.smfj.parser.api.SMFParserEventsMeta;
import com.io7m.smfj.parser.api.SMFParserEventsMetaType;
import javaslang.collection.List;
import mockit.Mocked;
import mockit.StrictExpectations;
import org.hamcrest.core.IsInstanceOf;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

public final class SMFParserEventsMetaTest
{
  @Rule public ExpectedException expected = ExpectedException.none();

  @Test
  public void testIgnored()
  {
    final SMFParserEventsMetaType ignore = SMFParserEventsMeta.ignore();
    Assert.assertFalse(ignore.onMeta(0L, 0L, 0L));

    this.expected.expect(UnreachableCodeException.class);
    ignore.onMetaData(0L, 0L, new byte[2]);
  }

  @Test
  public void testUnreachable()
    throws Exception
  {
    final Constructor<SMFParserEventsMeta> c =
      SMFParserEventsMeta.class.getDeclaredConstructor();
    c.setAccessible(true);

    this.expected.expect(InvocationTargetException.class);
    this.expected.expectCause(IsInstanceOf.any(UnreachableCodeException.class));
    c.newInstance();
  }

  @Test
  public void testDelegatingAllWant(
    final @Mocked SMFParserEventsMetaType ev0,
    final @Mocked SMFParserEventsMetaType ev1,
    final @Mocked SMFParserEventsMetaType ev2)
  {
    final SMFParserEventsMetaType ev =
      SMFParserEventsMeta.delegating(List.of(ev0, ev1, ev2));

    final byte[] data = {(byte) 1, (byte) 2, (byte) 3};

    new StrictExpectations()
    {{
      ev0.onMeta(0L, 1L, 2L);
      this.result = Boolean.TRUE;
      ev1.onMeta(0L, 1L, 2L);
      this.result = Boolean.TRUE;
      ev2.onMeta(0L, 1L, 2L);
      this.result = Boolean.TRUE;

      ev0.onMetaData(0L, 1L, data);
      ev1.onMetaData(0L, 1L, data);
      ev2.onMetaData(0L, 1L, data);
    }};

    ev.onMeta(0L, 1L, 2L);
    ev.onMetaData(0L, 1L, data);
  }

  @Test
  public void testDelegatingNoneWant(
    final @Mocked SMFParserEventsMetaType ev0,
    final @Mocked SMFParserEventsMetaType ev1,
    final @Mocked SMFParserEventsMetaType ev2)
  {
    final SMFParserEventsMetaType ev =
      SMFParserEventsMeta.delegating(List.of(ev0, ev1, ev2));

    final byte[] data = {(byte) 1, (byte) 2, (byte) 3};
    new StrictExpectations()
    {{
      ev0.onMeta(0L, 1L, 2L);
      this.result = Boolean.FALSE;
      ev1.onMeta(0L, 1L, 2L);
      this.result = Boolean.FALSE;
      ev2.onMeta(0L, 1L, 2L);
      this.result = Boolean.FALSE;
    }};

    ev.onMeta(0L, 1L, 2L);
    ev.onMetaData(0L, 1L, data);
  }
}
