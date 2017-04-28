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

package com.io7m.smfj.tests.format.text;

import com.io7m.smfj.format.text.SMFLineLexer;
import javaslang.collection.List;
import org.junit.Assert;
import org.junit.Test;


public final class SMFLineLexerTest
{
  @Test
  public void testLexEmpty()
  {
    Assert.assertEquals(
      List.empty(), new SMFLineLexer().lex(""));
  }

  @Test
  public void testLexABC()
  {
    Assert.assertEquals(
      List.of("a", "b", "c"), new SMFLineLexer().lex("a b c"));
  }

  @Test
  public void testLexAAA()
  {
    Assert.assertEquals(
      List.of("aaa"), new SMFLineLexer().lex("aaa"));
  }

  @Test
  public void testLexqAAA()
  {
    Assert.assertEquals(
      List.of("aaa"), new SMFLineLexer().lex("\"aaa\""));
  }

  @Test
  public void testLexAqBC()
  {
    Assert.assertEquals(
      List.of("a", "b", "c"), new SMFLineLexer().lex("a \"b\" c"));
  }

  @Test
  public void testLexAqB()
  {
    Assert.assertEquals(
      List.of("a", "b"), new SMFLineLexer().lex("a\"b\""));
  }

  @Test
  public void testLexQEscape0()
  {
    Assert.assertEquals(
      List.of("a", "\\b"), new SMFLineLexer().lex("a \"\\\\b\""));
  }

  @Test
  public void testLexQEscape1()
  {
    Assert.assertEquals(
      List.of("a", "\"b"), new SMFLineLexer().lex("a \"\\\"b\""));
  }

}