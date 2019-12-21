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


package com.io7m.smfj.cmdline;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;
import com.io7m.smfj.core.SMFPartialLogged;
import com.io7m.smfj.frontend.SMFFilterCommandFile;
import com.io7m.smfj.frontend.SMFParserProviders;
import com.io7m.smfj.frontend.SMFSerializerProviders;
import com.io7m.smfj.parser.api.SMFParserProviderType;
import com.io7m.smfj.processing.api.SMFFilterCommandContext;
import com.io7m.smfj.processing.api.SMFFilterCommandModuleResolver;
import com.io7m.smfj.processing.api.SMFFilterCommandModuleResolverType;
import com.io7m.smfj.processing.api.SMFMemoryMesh;
import com.io7m.smfj.processing.api.SMFMemoryMeshFilterType;
import com.io7m.smfj.processing.api.SMFMemoryMeshProducer;
import com.io7m.smfj.processing.api.SMFMemoryMeshProducerType;
import com.io7m.smfj.processing.api.SMFMemoryMeshSerializer;
import com.io7m.smfj.serializer.api.SMFSerializerProviderType;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Parameters(commandDescription = "Filter mesh data")
public final class CommandFilter extends CommandRoot
{
  private static final Logger LOG = LoggerFactory.getLogger(CommandFilter.class);
  private int exitCode;

  @Parameter(
    names = "--input-file",
    required = true,
    description = "The input file")
  private Path fileIn;

  @Parameter(
    names = "--input-format",
    description = "The input file format")
  private String formatIn;

  @Parameter(
    names = "--output-file",
    description = "The output file")
  private Path fileOut;

  @Parameter(
    names = "--output-format",
    description = "The output file format")
  private String formatOut;

  @Parameter(
    names = "--commands",
    required = true,
    description = "The filter commands")
  private Path fileCommands;

  @Parameter(
    names = "--source-directory",
    description = "The source directory")
  private final Path sourceDirectory = Paths.get("");

  CommandFilter()
  {
    this.exitCode = 0;
  }

  @Override
  public Integer call()
    throws Exception
  {
    super.call();

    final Optional<List<SMFMemoryMeshFilterType>> filtersOpt =
      this.parseFilterCommands();

    if (filtersOpt.isEmpty()) {
      return this.fail();
    }

    final List<SMFMemoryMeshFilterType> filters = filtersOpt.get();

    final Optional<SMFParserProviderType> providerParserOpt =
      SMFParserProviders.findParserProvider(
        Optional.ofNullable(this.formatIn),
        this.fileIn.toString());

    if (providerParserOpt.isEmpty()) {
      return this.fail();
    }

    final SMFParserProviderType providerParser = providerParserOpt.get();

    final Optional<SMFMemoryMesh> meshOpt =
      this.loadMemoryMesh(providerParser, this.fileIn);

    if (meshOpt.isEmpty()) {
      return this.fail();
    }

    final SMFFilterCommandContext context =
      SMFFilterCommandContext.of(
        this.sourceDirectory.toAbsolutePath(),
        this.fileCommands.toAbsolutePath());

    final Optional<SMFMemoryMesh> filteredOpt =
      this.runFilters(context, filters, meshOpt.get());

    if (filteredOpt.isEmpty()) {
      return this.fail();
    }

    this.serializeMesh(filteredOpt.get());
    return Integer.valueOf(this.exitCode);
  }

  private void serializeMesh(
    final SMFMemoryMesh filtered)
  {
    if (this.fileOut != null) {
      final Optional<SMFSerializerProviderType> providerSerializerOpt =
        SMFSerializerProviders.findSerializerProvider(
          Optional.ofNullable(this.formatOut),
          this.fileOut.toString());

      if (providerSerializerOpt.isEmpty()) {
        this.fail();
        return;
      }

      final SMFSerializerProviderType serializers =
        providerSerializerOpt.get();

      LOG.debug("serializing to {}", this.fileOut);
      final var timeThen = LocalDateTime.now();
      try (var os = Files.newOutputStream(this.fileOut)) {
        try (var serializer =
               serializers.serializerCreate(
                 serializers.serializerSupportedVersions().last(),
                 this.fileOut.toUri(),
                 os)) {
          SMFMemoryMeshSerializer.serialize(filtered, serializer);
        }
      } catch (final IOException e) {
        LOG.error("could not serialize mesh: {}", e.getMessage());
        LOG.debug("i/o error: ", e);
        this.fail();
        return;
      }
      final var timeNow = LocalDateTime.now();
      LOG.debug("serialized in {}", Duration.between(timeThen, timeNow));
    }
  }

  private Integer fail()
  {
    this.exitCode = 1;
    return Integer.valueOf(this.exitCode);
  }

  private Optional<SMFMemoryMesh> runFilters(
    final SMFFilterCommandContext context,
    final List<SMFMemoryMeshFilterType> filters,
    final SMFMemoryMesh mesh)
  {
    SMFMemoryMesh meshCurrent = mesh;
    for (int index = 0; index < filters.size(); ++index) {
      final SMFMemoryMeshFilterType filter = filters.get(index);
      LOG.debug("evaluating filter: {}", filter.name());

      final SMFPartialLogged<SMFMemoryMesh> result =
        filter.filter(context, meshCurrent);

      result.warnings().forEach(e -> {
        LOG.warn("{}", e.fullMessage());
        final Optional<Exception> exceptionOpt = e.exception();
        if (exceptionOpt.isPresent()) {
          LOG.error("exception: ", exceptionOpt.get());
        }
      });

      result.errors().forEach(e -> {
        LOG.error("{}", e.fullMessage());
        final Optional<Exception> exceptionOpt = e.exception();
        if (exceptionOpt.isPresent()) {
          LOG.error("exception: ", exceptionOpt.get());
        }
      });

      if (result.isSucceeded()) {
        meshCurrent = result.get();
      } else {
        this.exitCode = 1;
        return Optional.empty();
      }
    }

    return Optional.of(meshCurrent);
  }

  private Optional<SMFMemoryMesh> loadMemoryMesh(
    final SMFParserProviderType parsers,
    final Path path)
    throws IOException
  {
    final SMFMemoryMeshProducerType loader =
      SMFMemoryMeshProducer.create();

    LOG.debug("open {}", path);
    try (var stream = Files.newInputStream(path)) {
      try (var parser = parsers.parserCreateSequential(
        loader, path.toUri(), stream)) {
        parser.parse();
      }

      loader.warnings().forEach(e -> {
        LOG.warn("{}", e.fullMessage());
        final Optional<Exception> exceptionOpt = e.exception();
        if (exceptionOpt.isPresent()) {
          LOG.error("exception: ", exceptionOpt.get());
        }
      });

      loader.errors().forEach(e -> {
        LOG.error("{}", e.fullMessage());
        final Optional<Exception> exceptionOpt = e.exception();
        if (exceptionOpt.isPresent()) {
          LOG.error("exception: ", exceptionOpt.get());
        }
      });

      if (!loader.errors().isEmpty()) {
        this.exitCode = 1;
        return Optional.empty();
      }
    }
    return Optional.of(loader.mesh());
  }

  private Optional<List<SMFMemoryMeshFilterType>> parseFilterCommands()
    throws IOException
  {
    final SMFFilterCommandModuleResolverType resolver =
      SMFFilterCommandModuleResolver.create();

    try (var stream = Files.newInputStream(this.fileCommands)) {
      final SMFPartialLogged<List<SMFMemoryMeshFilterType>> result =
        SMFFilterCommandFile.parseFromStream(
          resolver, Optional.of(this.fileCommands.toUri()), stream);

      result.warnings().forEach(e -> {
        LOG.warn("{}", e.fullMessage());
        final Optional<Exception> exceptionOpt = e.exception();
        if (exceptionOpt.isPresent()) {
          LOG.error("exception: ", exceptionOpt.get());
        }
      });

      result.errors().forEach(e -> {
        LOG.error("{}", e.fullMessage());
        final Optional<Exception> exceptionOpt = e.exception();
        if (exceptionOpt.isPresent()) {
          LOG.error("exception: ", exceptionOpt.get());
        }
      });

      if (result.isSucceeded()) {
        return Optional.of(result.get());
      }

      return Optional.empty();
    }
  }
}
