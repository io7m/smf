/*
 * Copyright © 2019 Mark Raynsford <code@io7m.com> http://io7m.com
 *
 * Permission to use, copy, modify, and/or distribute this software for
 * any purpose with or without fee is hereby granted, provided that the
 * above copyright notice and this permission notice appear in all copies.
 *
 * THE SOFTWARE IS PROVIDED "AS IS" AND THE AUTHOR DISCLAIMS ALL
 * WARRANTIES WITH REGARD TO THIS SOFTWARE INCLUDING ALL IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS. IN NO EVENT SHALL THE AUTHOR
 * BE LIABLE FOR ANY SPECIAL, DIRECT, INDIRECT, OR CONSEQUENTIAL DAMAGES
 * OR ANY DAMAGES WHATSOEVER RESULTING FROM LOSS OF USE, DATA OR PROFITS,
 * WHETHER IN AN ACTION OF CONTRACT, NEGLIGENCE OR OTHER TORTIOUS ACTION,
 * ARISING OUT OF OR IN CONNECTION WITH THE USE OR PERFORMANCE OF THIS
 * SOFTWARE.
 */


package com.io7m.smfj.tests;

import com.io7m.junreachable.UnreachableCodeException;
import com.io7m.smfj.core.SMFAttributeNames;
import com.io7m.smfj.core.SMFSchemaNames;
import com.io7m.smfj.core.SMFSupportedSizes;
import com.io7m.smfj.format.binary2.internal.SMFB2Alignment;
import com.io7m.smfj.format.text.SMFBase64Lines;
import java.lang.reflect.InvocationTargetException;
import java.util.stream.Stream;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;

public final class UnreachableTest
{
  @TestFactory
  public Stream<DynamicTest> testUnreachableConstructors()
  {
    return Stream.of(
      SMFAttributeNames.class,
      SMFSchemaNames.class,
      SMFSupportedSizes.class,
      SMFB2Alignment.class,
      SMFBase64Lines.class
    ).map((Class<?> clazz) -> {
      final String name = "test" + clazz.getCanonicalName();
      return DynamicTest.dynamicTest(
        name,
        () -> callPrivateConstructor(clazz)
      );
    });
  }

  private static void callPrivateConstructor(final Class<?> clazz)
    throws NoSuchMethodException, InstantiationException, IllegalAccessException
  {
    try {
      final var constructor = clazz.getDeclaredConstructor();
      constructor.setAccessible(true);
      constructor.newInstance();
    } catch (final NoSuchMethodException | SecurityException | InstantiationException | IllegalAccessException | IllegalArgumentException e) {
      throw e;
    } catch (final InvocationTargetException e) {
      Assertions.assertEquals(UnreachableCodeException.class, e.getCause().getClass());
    }
  }
}
