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
import com.io7m.smfj.frontend.SMFFilterCommandFile;
import com.io7m.smfj.frontend.SMFParserProviders;
import com.io7m.smfj.frontend.SMFSerializerProviders;
import com.io7m.smfj.parser.api.SMFParseError;
import com.io7m.smfj.parser.api.SMFParserProviderType;
import com.io7m.smfj.parser.api.SMFParserSequentialType;
import com.io7m.smfj.processing.api.SMFFilterCommandContext;
import com.io7m.smfj.processing.api.SMFFilterCommandModuleResolver;
import com.io7m.smfj.processing.api.SMFFilterCommandModuleResolverType;
import com.io7m.smfj.processing.api.SMFMemoryMesh;
import com.io7m.smfj.processing.api.SMFMemoryMeshFilterType;
import com.io7m.smfj.processing.api.SMFMemoryMeshProducer;
import com.io7m.smfj.processing.api.SMFMemoryMeshProducerType;
import com.io7m.smfj.processing.api.SMFMemoryMeshSerializer;
import com.io7m.smfj.processing.api.SMFProcessingError;
import com.io7m.smfj.serializer.api.SMFSerializerProviderType;
import com.io7m.smfj.serializer.api.SMFSerializerType;
import io.vavr.collection.List;
import io.vavr.collection.Seq;
import io.vavr.control.Validation;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
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
  private String fileIn;

  @Parameter(
    names = "--input-format",
    description = "The input file format")
  private String formatIn;

  @Parameter(
    names = "--output-file",
    description = "The output file")
  private String fileOut;

  @Parameter(
    names = "--output-format",
    description = "The output file format")
  private String formatOut;

  @Parameter(
    names = "--commands",
    required = true,
    description = "The filter commands")
  private String fileCommands;

  @Parameter(
    names = "--source-directory",
    description = "The source directory")
  private String sourceDirectory = System.getProperty("user.dir");

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
        this.fileIn);

    if (providerParserOpt.isEmpty()) {
      return this.fail();
    }

    final SMFParserProviderType providerParser = providerParserOpt.get();
    final Path pathIn = Paths.get(this.fileIn);

    final Optional<SMFMemoryMesh> meshOpt =
      this.loadMemoryMesh(providerParser, pathIn);

    if (meshOpt.isEmpty()) {
      return this.fail();
    }

    final SMFFilterCommandContext context =
      SMFFilterCommandContext.of(
        Paths.get(this.sourceDirectory).toAbsolutePath(),
        Paths.get(this.fileCommands).toAbsolutePath());

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
          Optional.ofNullable(this.formatOut), this.fileOut);

      if (providerSerializerOpt.isEmpty()) {
        this.fail();
        return;
      }

      final SMFSerializerProviderType providerSerializer =
        providerSerializerOpt.get();
      final Path pathOut =
        Paths.get(this.fileOut);

      LOG.debug("serializing to {}", pathOut);
      try (OutputStream os = Files.newOutputStream(pathOut)) {
        try (SMFSerializerType serializer =
               providerSerializer.serializerCreate(
                 providerSerializer.serializerSupportedVersions().last(),
                 pathOut.toUri(),
                 os)) {
          SMFMemoryMeshSerializer.serialize(filtered, serializer);
        }
      } catch (final IOException e) {
        LOG.error("could not serialize mesh: {}", e.getMessage());
        LOG.debug("i/o error: ", e);
        this.fail();
        return;
      }
    }
  }

  private Integer fail()
  {
    this.exitCode = 1;
    return Integer.valueOf(this.exitCode);
  }

  private Optional<SMFMemoryMesh> runFilters(
    final SMFFilterCommandContext context,
    final Seq<SMFMemoryMeshFilterType> filters,
    final SMFMemoryMesh mesh)
  {
    SMFMemoryMesh mesh_current = mesh;
    for (int index = 0; index < filters.size(); ++index) {
      final SMFMemoryMeshFilterType filter = filters.get(index);
      LOG.debug("evaluating filter: {}", filter.name());

      final Validation<Seq<SMFProcessingError>, SMFMemoryMesh> result =
        filter.filter(context, mesh_current);
      if (result.isValid()) {
        mesh_current = result.get();
      } else {
        result.getError().map(e -> {
          LOG.error("filter: {}: {}", filter.name(), e.message());
          return null;
        });
        this.exitCode = 1;
        return Optional.empty();
      }
    }

    return Optional.of(mesh_current);
  }

  private Optional<SMFMemoryMesh> loadMemoryMesh(
    final SMFParserProviderType provider_parser,
    final Path path_in)
    throws IOException
  {
    final SMFMemoryMeshProducerType loader =
      SMFMemoryMeshProducer.create();

    try (InputStream is = Files.newInputStream(path_in)) {
      try (SMFParserSequentialType parser =
             provider_parser.parserCreateSequential(
               loader, path_in.toUri(), is)) {
        parser.parse();
      }
      if (!loader.errors().isEmpty()) {
        loader.errors().forEach(e -> LOG.error(e.fullMessage()));
        this.exitCode = 1;
        return Optional.empty();
      }
    }
    return Optional.of(loader.mesh());
  }

  private Optional<List<SMFMemoryMeshFilterType>> parseFilterCommands()
    throws IOException
  {
    final Path path_commands = Paths.get(this.fileCommands);
    final SMFFilterCommandModuleResolverType resolver =
      SMFFilterCommandModuleResolver.create();

    try (InputStream stream = Files.newInputStream(path_commands)) {
      final Validation<Seq<SMFParseError>, List<SMFMemoryMeshFilterType>> r =
        SMFFilterCommandFile.parseFromStream(
          resolver, Optional.of(path_commands.toUri()), stream);
      if (r.isValid()) {
        return Optional.of(r.get());
      }

      r.getError().forEach(e -> LOG.error(e.fullMessage()));
      return Optional.empty();
    }
  }
}
