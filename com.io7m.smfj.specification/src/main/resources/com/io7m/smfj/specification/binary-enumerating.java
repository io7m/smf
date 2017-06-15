void showSections()
{
  // Read SMF header
  long smf = readUnsigned64();
  int major = readUnsigned32();
  int minor = readUnsigned32();

  // Read all sections
  while (bytesRemaining()) {
    long offset = currentOffset();
    long magic = readUnsigned64();
    long size = readUnsigned64();

    System.out.printf(
    "section: %s %s %s\n",
    Long.toUnsignedString(offset),
    Long.toUnsignedString(magic, 16),
    Long.toUnsignedString(size));

    seek(size);
  }
}