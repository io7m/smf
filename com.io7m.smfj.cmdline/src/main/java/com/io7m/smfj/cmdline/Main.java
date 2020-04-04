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

package com.io7m.smfj.cmdline;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.ParameterException;
import com.beust.jcommander.internal.Console;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The main command line program.
 */

public final class Main implements Runnable
{
  private static final Logger LOG;

  static {
    LOG = LoggerFactory.getLogger(Main.class);
  }

  private final Map<String, CommandType> commands;
  private final JCommander commander;
  private final String[] args;
  private final StringConsole console;
  private int exitCode;

  private Main(final String[] in_args)
  {
    this.args = Objects.requireNonNull(in_args, "Arguments");

    final CommandRoot root = new CommandRoot();
    final CommandFormats formats = new CommandFormats();
    final CommandFilter filter = new CommandFilter();
    final CommandListFilters listFilters = new CommandListFilters();
    final CommandProbe probe = new CommandProbe();

    this.commands = new HashMap<>(8);
    this.commands.put("filter", filter);
    this.commands.put("list-formats", formats);
    this.commands.put("list-filters", listFilters);
    this.commands.put("probe", probe);

    this.console = new StringConsole();
    this.commander = new JCommander(root);
    this.commander.setConsole(this.console);
    this.commander.setProgramName("smf");
    this.commander.addCommand("filter", filter);
    this.commander.addCommand("list-formats", formats);
    this.commander.addCommand("list-filters", listFilters);
    this.commander.addCommand("probe", probe);
  }

  /**
   * The main entry point.
   *
   * @param args Command line arguments
   */

  public static void main(final String[] args)
  {
    final Main cm = new Main(args);
    cm.run();
    System.exit(cm.exitCode());
  }

  /**
   * @return The program exit code
   */

  public int exitCode()
  {
    return this.exitCode;
  }

  @Override
  public void run()
  {
    try {
      this.commander.parse(this.args);

      final String cmd = this.commander.getParsedCommand();
      if (cmd == null) {
        this.commander.usage();
        LOG.info(
          "Arguments required.\n{}",
          this.console.stringBuilder().toString());
        return;
      }

      final CommandType command = this.commands.get(cmd);
      this.exitCode = command.call().intValue();
    } catch (final ParameterException e) {
      this.commander.usage();
      LOG.error(
        "{}\n{}",
        e.getMessage(),
        this.console.stringBuilder().toString());
      this.exitCode = 1;
    } catch (final Exception e) {
      LOG.error("{}: ", e.getMessage(), e);
      this.exitCode = 1;
    }
  }

  private static final class StringConsole implements Console
  {
    private final StringBuilder stringBuilder;

    StringConsole()
    {
      this.stringBuilder = new StringBuilder();
    }

    public StringBuilder stringBuilder()
    {
      return this.stringBuilder;
    }

    @Override
    public void print(final String s)
    {
      this.stringBuilder.append(s);
    }

    @Override
    public void println(final String s)
    {
      this.stringBuilder.append(s);
      this.stringBuilder.append(System.lineSeparator());
    }

    @Override
    public char[] readPassword(final boolean b)
    {
      return new char[0];
    }
  }
}
