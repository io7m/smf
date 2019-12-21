package com.io7m.smfj.tests.format.text;

import com.io7m.smfj.core.SMFAttribute;
import com.io7m.smfj.core.SMFErrorType;
import com.io7m.smfj.core.SMFFormatVersion;
import com.io7m.smfj.core.SMFHeader;
import com.io7m.smfj.core.SMFSchemaIdentifier;
import com.io7m.smfj.core.SMFWarningType;
import com.io7m.smfj.format.text.SMFFormatText;
import com.io7m.smfj.parser.api.SMFParserEventsBodyType;
import com.io7m.smfj.parser.api.SMFParserEventsDataAttributeValuesType;
import com.io7m.smfj.parser.api.SMFParserEventsDataAttributesNonInterleavedType;
import com.io7m.smfj.parser.api.SMFParserEventsDataMetaType;
import com.io7m.smfj.parser.api.SMFParserEventsDataTrianglesType;
import com.io7m.smfj.parser.api.SMFParserEventsHeaderType;
import com.io7m.smfj.parser.api.SMFParserEventsType;
import com.io7m.smfj.parser.api.SMFParserSequentialType;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Base64;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class FMT
{
  private static final Logger LOG;

  static {
    LOG = LoggerFactory.getLogger(FMT.class);
  }

  private FMT()
  {

  }

  public static void main(
    final String[] args)
    throws IOException
  {
    final SMFFormatText fmt = new SMFFormatText();
    final Path path = Paths.get("mesh-ascii.txt");
    try (InputStream stream = Files.newInputStream(path)) {

      final SMFParserSequentialType p =
        fmt.parserCreateSequential(new Events(), path.toUri(), stream);
      p.parse();
    }
  }

  private static final class Events implements SMFParserEventsType,
    SMFParserEventsHeaderType,
    SMFParserEventsBodyType,
    SMFParserEventsDataAttributeValuesType,
    SMFParserEventsDataTrianglesType,
    SMFParserEventsDataAttributesNonInterleavedType,
    SMFParserEventsDataMetaType
  {
    private long meta_vendor;
    private long meta_schema;
    private SMFAttribute attribute_current;

    @Override
    public void onStart()
    {
      LOG.debug("parser started");
    }

    @Override
    public Optional<SMFParserEventsHeaderType> onVersionReceived(
      final SMFFormatVersion version)
    {
      LOG.debug(
        "version: {}.{}",
        Integer.valueOf(version.major()),
        Integer.valueOf(version.minor()));
      return Optional.of(this);
    }

    @Override
    public void onFinish()
    {
      LOG.debug("parser finished");
    }

    @Override
    public void onError(
      final SMFErrorType e)
    {
      LOG.error(e.fullMessage());
      e.exception().ifPresent(ex -> LOG.error("exception: ", ex));
    }

    @Override
    public void onWarning(
      final SMFWarningType w)
    {
      LOG.warn(w.fullMessage());
      w.exception().ifPresent(ex -> LOG.warn("exception: ", ex));
    }

    @Override
    public Optional<SMFParserEventsBodyType> onHeaderParsed(
      final SMFHeader header)
    {
      LOG.debug("data start: {}", header);
      return Optional.of(this);
    }

    @Override
    public Optional<SMFParserEventsDataAttributesNonInterleavedType> onAttributesNonInterleaved()
    {
      LOG.debug("attributes non-interleaved start");
      return Optional.of(this);
    }

    @Override
    public Optional<SMFParserEventsDataTrianglesType> onTriangles()
    {
      LOG.debug("triangles start");
      return Optional.of(this);
    }

    @Override
    public void onDataTriangle(
      final long v0,
      final long v1,
      final long v2)
    {
      LOG.debug(
        "triangle: {} {} {}",
        Long.valueOf(v0),
        Long.valueOf(v1),
        Long.valueOf(v2));
    }

    @Override
    public void onDataTrianglesFinish()
    {
      LOG.debug("triangles finish");
    }

    @Override
    public void onDataAttributeValueIntegerSigned1(
      final long x)
    {
      LOG.debug(
        "data signed 1: {}",
        Long.valueOf(x));
    }

    @Override
    public void onDataAttributeValueIntegerSigned2(
      final long x,
      final long y)
    {
      LOG.debug(
        "data signed 2: {} {}",
        Long.valueOf(x),
        Long.valueOf(y));
    }

    @Override
    public void onDataAttributeValueIntegerSigned3(
      final long x,
      final long y,
      final long z)
    {
      LOG.debug(
        "data signed 3: {} {} {}",
        Long.valueOf(x),
        Long.valueOf(y),
        Long.valueOf(z));
    }

    @Override
    public void onDataAttributeValueIntegerSigned4(
      final long x,
      final long y,
      final long z,
      final long w)
    {
      LOG.debug(
        "data signed 4: {} {} {} {}",
        Long.valueOf(x),
        Long.valueOf(y),
        Long.valueOf(z),
        Long.valueOf(w));
    }

    @Override
    public void onDataAttributeValueIntegerUnsigned1(
      final long x)
    {
      LOG.debug(
        "data unsigned 1: {}",
        Long.valueOf(x));
    }

    @Override
    public void onDataAttributeValueIntegerUnsigned2(
      final long x,
      final long y)
    {
      LOG.debug(
        "data unsigned 2: {} {}",
        Long.valueOf(x),
        Long.valueOf(y));
    }

    @Override
    public void onDataAttributeValueIntegerUnsigned3(
      final long x,
      final long y,
      final long z)
    {
      LOG.debug(
        "data unsigned 3: {} {} {}",
        Long.valueOf(x),
        Long.valueOf(y),
        Long.valueOf(z));
    }

    @Override
    public void onDataAttributeValueIntegerUnsigned4(
      final long x,
      final long y,
      final long z,
      final long w)
    {
      LOG.debug(
        "data unsigned 4: {} {} {} {}",
        Long.valueOf(x),
        Long.valueOf(y),
        Long.valueOf(z),
        Long.valueOf(w));
    }

    @Override
    public void onDataAttributeValueFloat1(
      final double x)
    {
      LOG.debug(
        "data float 1: {}",
        Double.valueOf(x));
    }

    @Override
    public void onDataAttributeValueFloat2(
      final double x,
      final double y)
    {
      LOG.debug(
        "data float 2: {} {}",
        Double.valueOf(x),
        Double.valueOf(y));
    }

    @Override
    public void onDataAttributeValueFloat3(
      final double x,
      final double y,
      final double z)
    {
      LOG.debug(
        "data float 3: {} {} {}",
        Double.valueOf(x),
        Double.valueOf(y),
        Double.valueOf(z));
    }

    @Override
    public void onDataAttributeValueFloat4(
      final double x,
      final double y,
      final double z,
      final double w)
    {
      LOG.debug(
        "data float 4: {} {} {} {}",
        Double.valueOf(x),
        Double.valueOf(y),
        Double.valueOf(z),
        Double.valueOf(w));
    }

    @Override
    public void onDataAttributeValueFinish()
    {
      LOG.debug("data finish: {}", this.attribute_current);
    }

    @Override
    public Optional<SMFParserEventsDataAttributeValuesType> onDataAttributeStart(
      final SMFAttribute attribute)
    {
      this.attribute_current = attribute;
      return Optional.of(this);
    }

    @Override
    public void onDataAttributesNonInterleavedFinish()
    {
      LOG.debug("data non-interleaved finished");
    }

    @Override
    public void onMetaData(
      final SMFSchemaIdentifier schema,
      final byte[] data)
    {
      LOG.debug(
        "metadata: {} {}",
        schema.toHumanString(),
        Base64.getUrlEncoder().encodeToString(data));
    }

    @Override
    public Optional<SMFParserEventsDataMetaType> onMeta(
      final SMFSchemaIdentifier schema)
    {
      LOG.debug(
        "metadata request: {}",
        schema.toHumanString());

      return Optional.of(this);
    }
  }
}
