package org.mulinlab.varnote.utils.gz;


import htsjdk.samtools.SAMFormatException;
import htsjdk.samtools.util.BlockCompressedStreamConstants;
import htsjdk.samtools.util.RuntimeIOException;
import htsjdk.samtools.util.zip.InflaterFactory;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.zip.CRC32;
import java.util.zip.DataFormatException;
import java.util.zip.Inflater;


/**
 * Alternative to GZIPInputStream, for decompressing GZIP blocks that are already loaded into a byte[].
 * The main advantage is that this object can be used over and over again to decompress many blocks,
 * whereas a new GZIPInputStream and ByteArrayInputStream would otherwise need to be created for each
 * block to be decompressed.
 *
 * This code requires that the GZIP header conform to the GZIP blocks written to BAM files, with
 * a specific subfield and no other optional stuff.
 *
 * @author alecw@broadinstitute.org
 */
public final class BlockGunzipper {

    private static InflaterFactory defaultInflaterFactory = new InflaterFactory();

    private final Inflater inflater;
    private final CRC32 crc32 = new CRC32();
    private boolean checkCrcs = false;

    public static void setDefaultInflaterFactory(InflaterFactory inflaterFactory) {
        if (inflaterFactory == null) {
            throw new IllegalArgumentException("null inflaterFactory");
        } else {
            defaultInflaterFactory = inflaterFactory;
        }
    }

    BlockGunzipper() {
        this.inflater = defaultInflaterFactory.makeInflater(true);
    }

    /**
     * Create a BlockGunzipper using the provided inflaterFactory
     * @param inflaterFactory
     */
//    BlockGunzipper(InflaterFactory inflaterFactory) {
//        inflater = inflaterFactory.makeInflater(true); // GZIP mode
//    }

    /** Allows the caller to decide whether or not to check CRCs on when uncompressing blocks. */
    public void setCheckCrcs(final boolean check) {
        this.checkCrcs = check;
    }

    /**
     * Decompress GZIP-compressed data
     * @param uncompressedBlock must be big enough to hold decompressed output.
     * @param compressedBlock compressed data starting at offset 0
     * @param compressedLength size of compressed data, possibly less than the size of the buffer.
     * @return the uncompressed data size.
     */
    public int unzipBlock(byte[] uncompressedBlock, byte[] compressedBlock, int compressedLength) {
        return unzipBlock(uncompressedBlock, 0, compressedBlock, 0, compressedLength);
    }

    /**
     * Decompress GZIP-compressed data
     * @param uncompressedBlock must be big enough to hold decompressed output.
     * @param uncompressedBlockOffset the offset into uncompressedBlock.
     * @param compressedBlock compressed data starting at offset 0.
     * @param compressedBlock  the offset into the compressed data.
     * @param compressedLength size of compressed data, possibly less than the size of the buffer.
     * @return the uncompressed data size.
     */
    public int unzipBlock(byte[] uncompressedBlock, int uncompressedBlockOffset,
                           byte[] compressedBlock, int compressedBlockOffset, int compressedLength) {
        int uncompressedSize;
        try {
            ByteBuffer byteBuffer = ByteBuffer.wrap(compressedBlock, compressedBlockOffset, compressedLength);
            byteBuffer.order(ByteOrder.LITTLE_ENDIAN);

            // Validate GZIP header
            if (byteBuffer.get() != BlockCompressedStreamConstants.GZIP_ID1 ||
                    byteBuffer.get() != (byte)BlockCompressedStreamConstants.GZIP_ID2 ||
                    byteBuffer.get() != BlockCompressedStreamConstants.GZIP_CM_DEFLATE ||
                    byteBuffer.get() != BlockCompressedStreamConstants.GZIP_FLG
                    ) {
                throw new SAMFormatException("Invalid GZIP header");
            }
            // Skip MTIME, XFL, OS fields
            byteBuffer.position(byteBuffer.position() + 6);
            if (byteBuffer.getShort() != BlockCompressedStreamConstants.GZIP_XLEN) {
                throw new SAMFormatException("Invalid GZIP header");
            }
            // Skip blocksize subfield intro
            byteBuffer.position(byteBuffer.position() + 4);
            // Read ushort
            final int totalBlockSize = (byteBuffer.getShort() & 0xffff) + 1;
            if (totalBlockSize != compressedLength) {
                throw new SAMFormatException("GZIP blocksize disagreement");
            }

            // Read expected size and CRD from end of GZIP block
            final int deflatedSize = compressedLength - BlockCompressedStreamConstants.BLOCK_HEADER_LENGTH - BlockCompressedStreamConstants.BLOCK_FOOTER_LENGTH;
            byteBuffer.position(byteBuffer.position() + deflatedSize);
            int expectedCrc = byteBuffer.getInt();
            uncompressedSize = byteBuffer.getInt();
            inflater.reset();

            // Decompress
            inflater.setInput(compressedBlock, compressedBlockOffset + BlockCompressedStreamConstants.BLOCK_HEADER_LENGTH, deflatedSize);
            final int inflatedBytes = inflater.inflate(uncompressedBlock, uncompressedBlockOffset, uncompressedSize);
            if (inflatedBytes != uncompressedSize) {
                throw new SAMFormatException("Did not inflate expected amount");
            }

            // Validate CRC if so desired
            if (this.checkCrcs) {
                crc32.reset();
                crc32.update(uncompressedBlock, uncompressedBlockOffset, uncompressedSize);
                final long crc = crc32.getValue();
                if ((int)crc != expectedCrc) {
                    throw new SAMFormatException("CRC mismatch");
                }
            }
        } catch (DataFormatException e)
        {
            throw new RuntimeIOException(e);
        }
        return uncompressedSize;
    }
}
