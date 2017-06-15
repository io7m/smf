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

package com.io7m.smfj.tests.format.binary;

import com.io7m.smfj.format.binary.SMFBSection;
import com.io7m.smfj.format.binary.SMFBSectionParserType;
import com.io7m.smfj.parser.api.SMFParseError;
import javaslang.control.Validation;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;

public abstract class SMFBSectionParserContract
{
  protected abstract SMFBSectionParserType parser(String name)
    throws IOException;

  @Test
  public final void testSectionsZero()
    throws Exception
  {
    final SMFBSectionParserType p = this.parser("sections_zero.bin");

    {
      final Validation<SMFParseError, SMFBSection> r = p.parse();
      Assert.assertTrue(r.isValid());
      final SMFBSection s = r.get();
      Assert.assertEquals(0x1020304050607080L, s.id());
      Assert.assertEquals(0L, s.offset());
      Assert.assertEquals(0L, s.sizeOfData());
      Assert.assertEquals(16L, s.sizeTotal());
    }

    {
      final Validation<SMFParseError, SMFBSection> r = p.parse();
      Assert.assertTrue(r.isValid());
      final SMFBSection s = r.get();
      Assert.assertEquals(0x1121314151617181L, s.id());
      Assert.assertEquals(16L, s.offset());
      Assert.assertEquals(0L, s.sizeOfData());
      Assert.assertEquals(16L, s.sizeTotal());
    }

    {
      final Validation<SMFParseError, SMFBSection> r = p.parse();
      Assert.assertTrue(r.isValid());
      final SMFBSection s = r.get();
      Assert.assertEquals(0x1222324252627282L, s.id());
      Assert.assertEquals(32L, s.offset());
      Assert.assertEquals(0L, s.sizeOfData());
      Assert.assertEquals(16L, s.sizeTotal());
    }

    {
      final Validation<SMFParseError, SMFBSection> r = p.parse();
      Assert.assertTrue(r.isValid());
      final SMFBSection s = r.get();
      Assert.assertEquals(0x1323334353637383L, s.id());
      Assert.assertEquals(48L, s.offset());
      Assert.assertEquals(0L, s.sizeOfData());
      Assert.assertEquals(16L, s.sizeTotal());
    }

    {
      final Validation<SMFParseError, SMFBSection> r = p.parse();
      Assert.assertTrue(r.isValid());
      final SMFBSection s = r.get();
      Assert.assertEquals(0x1424344454647484L, s.id());
      Assert.assertEquals(64L, s.offset());
      Assert.assertEquals(0L, s.sizeOfData());
      Assert.assertEquals(16L, s.sizeTotal());
    }
  }

  @Test
  public final void testSectionsSized()
    throws Exception
  {
    final SMFBSectionParserType p = this.parser("sections_sized.bin");

    {
      final Validation<SMFParseError, SMFBSection> r = p.parse();
      Assert.assertTrue(r.isValid());
      final SMFBSection s = r.get();
      Assert.assertEquals(0x1020304050607080L, s.id());
      Assert.assertEquals(0L, s.offset());
      Assert.assertEquals(16L, s.sizeOfData());
      Assert.assertEquals(32L, s.sizeTotal());
    }

    {
      final Validation<SMFParseError, SMFBSection> r = p.parse();
      Assert.assertTrue(r.isValid());
      final SMFBSection s = r.get();
      Assert.assertEquals(0x1121314151617181L, s.id());
      Assert.assertEquals(32L, s.offset());
      Assert.assertEquals(32L, s.sizeOfData());
      Assert.assertEquals(48L, s.sizeTotal());
    }

    {
      final Validation<SMFParseError, SMFBSection> r = p.parse();
      Assert.assertTrue(r.isValid());
      final SMFBSection s = r.get();
      Assert.assertEquals(0x1222324252627282L, s.id());
      Assert.assertEquals(80L, s.offset());
      Assert.assertEquals(48L, s.sizeOfData());
      Assert.assertEquals(64L, s.sizeTotal());
    }

    {
      final Validation<SMFParseError, SMFBSection> r = p.parse();
      Assert.assertTrue(r.isValid());
      final SMFBSection s = r.get();
      Assert.assertEquals(0x1323334353637383L, s.id());
      Assert.assertEquals(144L, s.offset());
      Assert.assertEquals(64L, s.sizeOfData());
      Assert.assertEquals(80L, s.sizeTotal());
    }

    {
      final Validation<SMFParseError, SMFBSection> r = p.parse();
      Assert.assertTrue(r.isValid());
      final SMFBSection s = r.get();
      Assert.assertEquals(0x1424344454647484L, s.id());
      Assert.assertEquals(224L, s.offset());
      Assert.assertEquals(80L, s.sizeOfData());
      Assert.assertEquals(96L, s.sizeTotal());
    }
  }

  @Test
  public final void testSectionsBadSize()
    throws Exception
  {
    final SMFBSectionParserType p = this.parser("sections_bad_size.bin");

    {
      final Validation<SMFParseError, SMFBSection> r = p.parse();
      Assert.assertTrue(r.isInvalid());
      Assert.assertTrue(r.getError().message().contains("Section sizes must be multiples of"));
    }
  }
}
