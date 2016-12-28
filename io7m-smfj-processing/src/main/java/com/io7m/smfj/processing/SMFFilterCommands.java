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

package com.io7m.smfj.processing;

import com.io7m.jlexing.core.LexicalPosition;
import com.io7m.jnull.NullCheck;
import com.io7m.smfj.parser.api.SMFParseError;
import javaslang.collection.HashMap;
import javaslang.collection.List;
import javaslang.collection.Map;
import javaslang.control.Validation;

import java.nio.file.Path;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * A parser for filter commands.
 */

public final class SMFFilterCommands
{
  private static final Map<String, SMFFilterCommandParserType> COMMANDS;

  static {
    HashMap<String, SMFFilterCommandParserType> cmds = HashMap.empty();
    cmds = cmds.put(
      SMFMemoryMeshFilterAttributeRename.NAME,
      SMFMemoryMeshFilterAttributeRename::parse);
    cmds = cmds.put(
      SMFMemoryMeshFilterAttributeRemove.NAME,
      SMFMemoryMeshFilterAttributeRemove::parse);
    cmds = cmds.put(
      SMFMemoryMeshFilterTrianglesOptimize.NAME,
      SMFMemoryMeshFilterTrianglesOptimize::parse);
    cmds = cmds.put(
      SMFMemoryMeshFilterCheck.NAME,
      SMFMemoryMeshFilterCheck::parse);
    COMMANDS = cmds;
  }

  private SMFFilterCommands()
  {

  }

  /**
   * Parse a filter command.
   *
   * @param file The current file
   * @param line The current line
   * @param text The current line text
   *
   * @return A filter, or a list of reasons why parsing failed
   */

  public static Validation<List<SMFParseError>, SMFMemoryMeshFilterType> parse(
    final Optional<Path> file,
    final int line,
    final List<String> text)
  {
    NullCheck.notNull(text, "text");

    if (text.isEmpty()) {
      return Validation.invalid(List.of(
        SMFParseError.of(LexicalPosition.of(line, 0, file), "Line is empty")));
    }

    final String name = text.get(0);
    if (COMMANDS.containsKey(name)) {
      return COMMANDS.get(name).get().parse(file, line, text);
    }

    return errorUnknownCommand(file, line, text);
  }

  private static Validation<List<SMFParseError>, SMFMemoryMeshFilterType> errorUnknownCommand(
    final Optional<Path> file,
    final int line,
    final List<String> text)
  {
    final StringBuilder sb = new StringBuilder(128);
    sb.append("Unrecognized command.");
    sb.append(System.lineSeparator());
    sb.append("  Command: ");
    sb.append(text.get(0));
    sb.append(System.lineSeparator());
    sb.append("  Expected: ");
    sb.append(text.toJavaStream().collect(Collectors.joining("|")));
    sb.append(System.lineSeparator());
    return Validation.invalid(List.of(
      SMFParseError.of(LexicalPosition.of(line, 0, file), sb.toString())));
  }
}
