package constants;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.List;

import bean.RegBean;
import htsjdk.samtools.Defaults;
import htsjdk.samtools.seekablestream.ISeekableStreamFactory;
import htsjdk.samtools.seekablestream.SeekableStream;
import htsjdk.samtools.seekablestream.SeekableStreamFactory;
import htsjdk.samtools.util.BlockCompressedInputStream;
import htsjdk.samtools.util.BlockCompressedStreamConstants;
import htsjdk.tribble.AbstractFeatureReader;
import htsjdk.tribble.Feature;
import htsjdk.tribble.TribbleException;
import htsjdk.variant.vcf.VCFHeader;

public class BasicUtils {
	public static final int INDEX_END = -2;
	public static final int SKIPMAX = 100;
	public static final int BIN_GENOMIC_SPAN = 512 * 1024 * 1024;
	public static final int[] LEVEL_STARTS = { 0, 1, 9, 73, 585, 4681 };
	public static final int MAX_BINS = 37450; // =(8^6-1)/7+1
	public static final int MAX_LINEAR_INDEX_SIZE = MAX_BINS + 1  - LEVEL_STARTS[LEVEL_STARTS.length - 1];
	public static final int BAM_LIDX_SHIFT = 14;
	public static final int[] bins = new int[MAX_BINS];
	public static final int UNSET_GENOMIC_LOCATION = 0;
	private static final int SHIFT_AMOUNT = 16;
    private static final long ADDRESS_MASK = 0xFFFFFFFFFFFFL;


    public static short readShort(final InputStream is) throws IOException {
		byte[] buf = new byte[2];
		is.read(buf);
		return ByteBuffer.wrap(buf).order(ByteOrder.LITTLE_ENDIAN).getShort();
	}
    
    public static int readInt(final InputStream is) throws IOException {
		byte[] buf = new byte[4];
		is.read(buf);
		return ByteBuffer.wrap(buf).order(ByteOrder.LITTLE_ENDIAN).getInt();
	}

	public static long readLong(final InputStream is) throws IOException {
		byte[] buf = new byte[8];
		is.read(buf);
		return ByteBuffer.wrap(buf).order(ByteOrder.LITTLE_ENDIAN).getLong();
	}

	public static int binLevel(int binNumer) {
		for(int i = 5; i>0; i--) {
			if(binNumer >= LEVEL_STARTS[i]) {
				return i;
			}
		}
		return 0;
	}
	
	public static int toLevel(int binNumer, int level) {
		return (int)(binNumer - LEVEL_STARTS[level] + 1)/8 + LEVEL_STARTS[level - 1]; 
	}
	
	public static List<Integer> reg2bins(final int beg, final int _end) {
		List<Integer> binNumbers = new ArrayList<Integer>();
		int k, end = _end;
		if (beg >= end)
			return null;
		if (end >= 1 << 29)
			end = 1 << 29;
		--end;
		binNumbers.add(0);

		for (k = 1 + (beg >> 26); k <= 1 + (end >> 26); ++k)
			binNumbers.add(k);
		for (k = 9 + (beg >> 23); k <= 9 + (end >> 23); ++k)
			binNumbers.add(k);
		for (k = 73 + (beg >> 20); k <= 73 + (end >> 20); ++k)
			binNumbers.add(k);
		for (k = 585 + (beg >> 17); k <= 585 + (end >> 17); ++k)
			binNumbers.add(k);
		for (k = 4681 + (beg >> 14); k <= 4681 + (end >> 14); ++k)
			binNumbers.add(k);
		return binNumbers;
	}

	public static int reg2bins(final int beg, final int _end, final int[] list) {
		int i = 0, k, end = _end;
		if (beg >= end)
			return 0;
		if (end >= 1 << 29)
			end = 1 << 29;
		--end;
		list[i++] = 0;
		for (k = 1 + (beg >> 26); k <= 1 + (end >> 26); ++k)
			list[i++] = k;
		for (k = 9 + (beg >> 23); k <= 9 + (end >> 23); ++k)
			list[i++] = k;
		for (k = 73 + (beg >> 20); k <= 73 + (end >> 20); ++k)
			list[i++] = k;
		for (k = 585 + (beg >> 17); k <= 585 + (end >> 17); ++k)
			list[i++] = k;
		for (k = 4681 + (beg >> 14); k <= 4681 + (end >> 14); ++k)
			list[i++] = k;
		return i;
	}

	public static int computeIndexingBin(final Feature feature) {
		// reg2bin has zero-based, half-open API
		return regionToBin(convertBeg(feature), convertEnd(feature));
	}

	public static int regionToBin(final int beg, int end) {
		--end;

		if (beg >> 14 == end >> 14)
			return LEVEL_STARTS[5] + (beg >> 14);
		if (beg >> 17 == end >> 17)
			return LEVEL_STARTS[4] + (beg >> 17);
		if (beg >> 20 == end >> 20)
			return LEVEL_STARTS[3] + (beg >> 20);
		if (beg >> 23 == end >> 23)
			return LEVEL_STARTS[2] + (beg >> 23);
		if (beg >> 26 == end >> 26)
			return LEVEL_STARTS[1] + (beg >> 26);
		return 0;
	}
	
	public static RegBean regionToRegBin(final int beg, int end) {
		--end;

		if (beg >> 14 == end >> 14)
			return new RegBean(LEVEL_STARTS[5] + (beg >> 14), 5);
		if (beg >> 17 == end >> 17)
			return new RegBean(LEVEL_STARTS[4] + (beg >> 17), 4);
		if (beg >> 20 == end >> 20)
			return new RegBean(LEVEL_STARTS[3] + (beg >> 20), 3);
		if (beg >> 23 == end >> 23)
			return new RegBean(LEVEL_STARTS[2] + (beg >> 23), 2);
		if (beg >> 26 == end >> 26)
			return new RegBean(LEVEL_STARTS[1] + (beg >> 26), 1);
		return new RegBean(0, 0);
	}

	public static int regionToBin4(final int beg, int end) {
		--end;

		if ((beg >> 14 == end >> 14) || (beg >> 17 == end >> 17))
			return LEVEL_STARTS[4] + (beg >> 17);
		if (beg >> 20 == end >> 20)
			return LEVEL_STARTS[3] + (beg >> 20);
		if (beg >> 23 == end >> 23)
			return LEVEL_STARTS[2] + (beg >> 23);
		if (beg >> 26 == end >> 26)
			return LEVEL_STARTS[1] + (beg >> 26);
		return 0;
	}

	public static int convertBeg(Feature feature) {
		return (feature.getStart() <= 0) ? 0 : (feature.getStart() - 1);
	}

	public static int convertEnd(Feature feature) {
		int end = feature.getEnd();
		if (end <= 0) {
			// If feature end cannot be determined (e.g. because a read is not
			// really aligned),
			// then treat this as a one base feature for indexing purposes.
			end = feature.getStart() + 1;
		}
		return end;
	}

	public static int convertToLinearIndexOffset(final int contigPos) {
		final int indexPos = (contigPos <= 0) ? 0 : contigPos - 1;
		return indexPos >> BAM_LIDX_SHIFT;
	}

	public static int convertToLinearIndexDirectly(final int indexPos) {
		return indexPos >> BAM_LIDX_SHIFT;
	}

	public static boolean areInSameBlock(final long vfp1,
			final long vfp2) {

		final long block1 = getBlockAddress(vfp1);
		final long block2 = getBlockAddress(vfp2);

		return block1 == block2;
	}
	
	public static long getBlockAddress(final long virtualFilePointer) {
        return (virtualFilePointer >> SHIFT_AMOUNT) & ADDRESS_MASK;
    }
	
	public static boolean isHeader(String s) {
		return (s.trim().equals("") || s.startsWith(VCFHeader.HEADER_INDICATOR) || s.startsWith("track") || s.startsWith("browser"));
	}
	
	@SuppressWarnings("resource")
	public static BlockCompressedInputStream checkStream(final File inputFile) {
		try {
			if (AbstractFeatureReader.hasBlockCompressedExtension(inputFile)) {
	            final int bufferSize = Math.max(Defaults.BUFFER_SIZE, BlockCompressedStreamConstants.MAX_COMPRESSED_BLOCK_SIZE);
	
	            if (!BlockCompressedInputStream.isValidFile(new BufferedInputStream(new FileInputStream(inputFile), bufferSize))) {
	                throw new TribbleException.MalformedFeatureFile("Input file is not in valid block compressed format.", inputFile.getAbsolutePath());
	            }
	
	            final ISeekableStreamFactory ssf = SeekableStreamFactory.getInstance();
	            final SeekableStream seekableStream =
	                    ssf.getBufferedStream(ssf.getStreamFor(inputFile.getAbsolutePath()));
	            return new BlockCompressedInputStream(seekableStream);
	        } else {
	        	throw new IllegalArgumentException(".gz .gzip .bgz .bgzf file format is support for index, please check your file type");
	        }
		} catch (final FileNotFoundException e) {
            throw new TribbleException.FeatureFileDoesntExist("Unable to open the input file, most likely the file doesn't exist.", inputFile.getAbsolutePath());
        } catch (final IOException e) {
            throw new TribbleException.MalformedFeatureFile("Error initializing stream", inputFile.getAbsolutePath(), e);
        }
	}
}
