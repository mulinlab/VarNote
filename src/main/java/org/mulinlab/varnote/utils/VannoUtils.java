package org.mulinlab.varnote.utils;

import htsjdk.samtools.seekablestream.SeekableStream;
import htsjdk.samtools.util.BlockCompressedInputStream;
import org.mulinlab.varnote.constants.GlobalParameter;
import org.mulinlab.varnote.operations.decode.BEDLocCodec;
import org.mulinlab.varnote.operations.decode.LocCodec;
import org.mulinlab.varnote.operations.decode.TABLocCodec;
import org.mulinlab.varnote.operations.decode.VCFLocCodec;
import org.mulinlab.varnote.operations.query.AbstractQuery;
import org.mulinlab.varnote.operations.query.SweepQuery;
import org.mulinlab.varnote.operations.query.TabixQuery;
import org.mulinlab.varnote.operations.query.VannoQuery;
import org.mulinlab.varnote.operations.readers.itf.QueryReaderItf;
import org.mulinlab.varnote.operations.readers.query.AbstractFileReader;
import org.mulinlab.varnote.operations.readers.query.BEDFileReader;
import org.mulinlab.varnote.operations.readers.query.TABFileReader;
import org.mulinlab.varnote.operations.readers.query.VCFFileReader;
import org.mulinlab.varnote.utils.database.Database;
import org.mulinlab.varnote.utils.enumset.*;
import htsjdk.samtools.seekablestream.SeekableStreamFactory;
import htsjdk.samtools.util.BlockCompressedStreamConstants;
import htsjdk.samtools.util.IOUtil;
import htsjdk.samtools.util.StringUtil;
import htsjdk.variant.vcf.VCFHeaderLineCount;
import htsjdk.variant.vcf.VCFHeaderLineType;

import java.io.*;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.*;

import joptsimple.OptionSet;
import org.mulinlab.varnote.utils.database.index.IndexFactory;
import org.mulinlab.varnote.utils.format.Format;
import org.mulinlab.varnote.exceptions.InvalidArgumentException;
import org.mulinlab.varnote.utils.gz.MyEndianOutputStream;

public final class VannoUtils {
	public final static String INTERSECT_ERROR = "Intersection type, valid values are " + IntersectType.INTERSECT.getVal() + "(" + IntersectType.INTERSECT.getName() + ")" +
			" or " + IntersectType.EXACT.getVal() + "(" + IntersectType.EXACT.getName() + ") or " + IntersectType.FULLCLOASE.getVal() + "(" + IntersectType.FULLCLOASE.getName() + ")" +
			". default is:" +  IntersectType.INTERSECT.getName() + ".";

	public final static Map<String, ChromosomeType> chrToTypeMap = new HashMap<>();

	public enum FileExt {
		GZ(Arrays.asList(".gz", ".gzip", ".bgz", ".bgzf")),
		VCF(Arrays.asList(".vcf", ".vcf.gz", ".vcf.bgz")),
		BED(Arrays.asList(".bed", ".bed.gz", ".bed.bgz")),
		GZVCF(Arrays.asList(".vcf.gz", ".vcf.bgz")),
		GZBED(Arrays.asList(".bed.gz", ".bed.bgz"));
		private final List<String> suffix; 
		
		private FileExt(final List<String> suffix) {
			this.suffix = suffix;
		}

		public List<String> getSuffix() {
			return suffix;
		}
	}
	
	public static boolean hasExtension(final FileExt ext, final String fileName) {
		String cleanedPath = stripQueryStringIfPathIsAnHttpUrl(fileName);
        for (final String extension : ext.getSuffix()) {
            if (cleanedPath.toLowerCase().endsWith(extension))
                return true;
        }
        return false;
	}
	
	public static String stripQueryStringIfPathIsAnHttpUrl(String path) {
        if(path.startsWith("http://") || path.startsWith("https://")) {
            int qIdx = path.indexOf('?');
            if (qIdx > 0) {
                return path.substring(0, qIdx);
            }
        }
        return path;
    }
	
	public static IndexType checkIndexType(String indexType) {
		if(indexType == null) return null;
		indexType = trimAndLC(indexType);
		for (IndexType t: IndexType.values()) {
			if(indexType.equals(t.getName())) {
				return t;
			}
		}	
		throw new InvalidArgumentException("Index system to use, valid values are "+ IndexType.TBI.getName() +" or " +
				IndexType.VARNOTE.getName() + ". By default, the program will use " + GlobalParameter.PRO_NAME + " index system(" + IndexType.VARNOTE.getName() + ").");
	}
	
	public static IntersectType checkIntersectType(String intersectType) {
		if(intersectType == null) return GlobalParameter.DEFAULT_INTERSECT;
		int type = Integer.parseInt(trimAndLC(intersectType));
		for (IntersectType t: IntersectType.values()) {
			if(type == t.getVal()) {
				return t;
			}
		}
		throw new InvalidArgumentException(INTERSECT_ERROR);
	}
	
	public static boolean isExist(String path) {
		if(!SeekableStreamFactory.isFilePath(path)) {
			return exists(path);
		} else {
			return new File(path).exists();
		}
	}
	
	public static boolean exists(String urlName) {
	    try {
	        new URL(urlName).openStream().close();
	        return true;
	    } catch (IOException e) {
	        return false;
	    }
	}
	
	public static OutMode checkOutMode(final int outMode) {
		for (OutMode m : OutMode.values()) {
			if(outMode == m.getNum()) {
				return m;
			}
		}	
		throw new InvalidArgumentException("Valid output recording mode are " + OutMode.QUERY.getNum() + "," + OutMode.DB.getNum() + " and " +
				OutMode.BOTH.getNum() +  ". " + OutMode.QUERY.getNum() + " for \"only output query records\"; " + 
				OutMode.DB.getNum() + " for \"only output matched database records\"; " + OutMode.BOTH.getNum() + " for \"output both query records and matched database records\". ");
	}
	
	public static VCFHeaderLineType checkType(String type) {
		type = trimAndLC(type);
		if(type.equals("integer")) {
			return VCFHeaderLineType.Integer;
		} else if(type.equals("float")) {
			return VCFHeaderLineType.Float;
		} else if(type.equals("string")) {
			return VCFHeaderLineType.String;
		} else if(type.equals("character")) {
			return VCFHeaderLineType.Character;
		} else if(type.equals("flag")) {
			return VCFHeaderLineType.Flag;
		} else {
			throw new InvalidArgumentException("We only support VCF Header Type equals 'integer', 'float', 'string', 'character' or 'flag', "
					+ "but we get " + type);
		}
	}

	public static VCFHeaderLineCount checkCount(String count) {
		count = trimAndLC(count);
		if(count.equals("integer")) {
			return VCFHeaderLineCount.INTEGER;
		} else if(count.equals("a")) {
			return VCFHeaderLineCount.A;
		} else if(count.equals("r")) {
			return VCFHeaderLineCount.R;
		} else if(count.equals("g")) {
			return VCFHeaderLineCount.G;
		} else if(count.equals(".")) {
			return VCFHeaderLineCount.UNBOUNDED;
		} else {
			throw new InvalidArgumentException("We only support VCF Header Number equals 'integer', '.', 'r', 'g' or 'a', " + "but we get " + count);
		}
	}
	
	public static Mode checkMode(int mode) {
		for (Mode m : Mode.values()) {
			if(mode == m.getNum()) {
				return m;
			}
		}	
		throw new InvalidArgumentException("Valid mode are " + Mode.TABIX.getNum() + "," + Mode.MIX.getNum() + " and " +
				Mode.SWEEP.getNum() +  ". mode=" + Mode.TABIX.getNum() + " means random access search, " + 
				"mode=" + Mode.MIX.getNum() + " means mix search, mode=" + Mode.SWEEP.getNum() + " means sweep search.");
	}
	
	public static Format determineFileType(String fileName, final boolean isQuery) {
		fileName = trimAndLC(fileName);

		if(isQuery) {
			if(hasExtension(FileExt.VCF, fileName)) return Format.VCF;
			else if(hasExtension(FileExt.BED, fileName)) return Format.BED;
			else return null;
		} else {
			if(hasExtension(FileExt.GZVCF, fileName)) return Format.VCF;
			else if(hasExtension(FileExt.GZBED, fileName)) return Format.BED;
			else return null;
		}
	}

	public static Format checkQueryFormat(String format) {
		format = format.toUpperCase();
		if(format.equals(FormatType.VCF.toString())) {
			return Format.VCF;
		} else if(format.equals(FormatType.BED.toString())) {
			return Format.BED;
		} else if(format.equals(FormatType.TAB.toString())) {
			return Format.newTAB();
		} else {
			return null;
		}
	}

	public static AnnoOutFormat checkOutFormat(String format) {
		format = trimAndLC(format);
		if (format.equals("vcf")) {
			return AnnoOutFormat.VCF;
		} else if(format.equals("bed")) {
			return AnnoOutFormat.BED;
		} else {
			throw new InvalidArgumentException("We only support vcf and bed file format currently.");
		}
	}
	
	public static boolean strToBool(String str) {
		str = trimAndLC(str);
		if(str.equals("true")) {
			return true;
		} else if(str.equals("false")) {
			return false;
		} else {
			throw new InvalidArgumentException("Parameter expect true or false but get: " + str + " .");
		}
	}
	
	public static String trimAndLC(String str) {
		return str.trim().toLowerCase();
	}

	public static Format parseIndexFileFormat(String str) {
		str = trimAndLC(str);
        if(str.equalsIgnoreCase("bed")) {
			return Format.BED;
        } else if(str.equalsIgnoreCase("vcf")) {
			return Format.VCF;
        } else if(str.equalsIgnoreCase("tab")) {
			return Format.newTAB();
        } else {
			throw new InvalidArgumentException("We support bed, vcf or tab format. but we get: " + str);
		}
	}
	
	public static String replaceQuote(String str) {
		return str.replaceAll("“", "").replaceAll("”", "").replaceAll("\"", "").trim(); 
	}
	
	public static int adjustCol(int col) {
		return (col == -1) ? col : (col + 1);
	}	
	
	public static void checkMissing(OptionSet options, String[] requiredOPT) {
        Set<String> missing = new HashSet<String>();
        for (String opt : requiredOPT) {
	       	 if(!options.has(opt)) {
	       		 missing.add(opt);
	       	 } 
		}         
       
        if(missing.size() > 0) throw new InvalidArgumentException("Missing required arguments: " + String.join(", ", missing.toArray(new String[0])));
	}
	
	public static void assertOutputIsValid(final String output) {
	      if (output == null) {
	    	  		throw new InvalidArgumentException("Cannot check validity of null output.");
	      }
	      if (!IOUtil.isUrl(output)) {
	    	  		IOUtil.assertFileIsWritable(new File(output));
	      }
	}

	public static void writeFormats(final MyEndianOutputStream indexLos, final Format formatSpec, final String[] headerParts, final List<String> sequenceNames, final Map<Integer, Long> addressOfChr) {
		try {
			indexLos.writeInt(GlobalParameter.version);
			indexLos.writeInt(IndexFactory.MAGIC_NUMBER);
			indexLos.writeInt(formatSpec.getFlags());
			indexLos.writeInt(formatSpec.sequenceColumn);
			indexLos.writeInt(formatSpec.startPositionColumn);
			indexLos.writeInt(formatSpec.endPositionColumn);

			indexLos.writeInt(formatSpec.getCommentIndicator().length());
			indexLos.write(StringUtil.stringToBytes(formatSpec.getCommentIndicator()));

			if (headerParts != null && headerParts.length > 0) {
				indexLos.writeInt(headerParts.length);

				int headerBlockSize = headerParts.length; // null terminators
				for (final String colName : headerParts) {
					headerBlockSize += colName.length();
				}
				indexLos.writeInt(headerBlockSize);

				for (String colName : headerParts) {
					indexLos.write(StringUtil.stringToBytes(colName));
					indexLos.write(0);
				}
			} else {
				indexLos.writeInt(0);
			}

			indexLos.writeInt(formatSpec.numHeaderLinesToSkip);
			indexLos.writeInt(formatSpec.refPositionColumn);
			indexLos.writeInt(formatSpec.altPositionColumn);
			indexLos.writeInt(8);
			indexLos.writeBoolean(formatSpec.isHasHeader());

			indexLos.writeInt(sequenceNames.size());

			int nameBlockSize = sequenceNames.size(); // null terminators
			for (final String sequenceName : sequenceNames) {
				nameBlockSize += sequenceName.length();
			}

			indexLos.writeInt(nameBlockSize);
			for (final String sequenceName : sequenceNames) {
				indexLos.write(StringUtil.stringToBytes(sequenceName));
				indexLos.write(0);
			}

			indexLos.writeInt(addressOfChr.keySet().size());
			for (Integer chrID : addressOfChr.keySet()) {
				indexLos.writeInt(chrID);
				indexLos.writeLong(addressOfChr.get(chrID));
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	
	public static void checkValidBGZ(final String path) {
		if(VannoUtils.checkFileType(path) != FileType.BGZ) 
			throw new InvalidArgumentException(String.format("Input file %s is not in valid block compressed format(.gz, .bgz). ", path));
	}
	
	public static FileType checkFileType(final String path) {
		try {
			if(hasExtension(FileExt.GZ, path)) {
				final int bufferSize = Math.max(GlobalParameter.BUFFER_SIZE, BlockCompressedStreamConstants.MAX_COMPRESSED_BLOCK_SIZE);
				BufferedInputStream stream = new BufferedInputStream(new FileInputStream(path), bufferSize);
				
				
				stream.mark(BlockCompressedStreamConstants.BLOCK_HEADER_LENGTH);
			    final byte[] buffer = new byte[BlockCompressedStreamConstants.BLOCK_HEADER_LENGTH];
			    final int count = readBytes(stream, buffer, 0, BlockCompressedStreamConstants.BLOCK_HEADER_LENGTH);
			    stream.reset();
			    if(count == BlockCompressedStreamConstants.BLOCK_HEADER_LENGTH && isValidBlockHeader(buffer)) {
			    	return FileType.BGZ;
			    } else {
			    	return FileType.GZ;
			    }
			} else return FileType.TXT;
		} catch (Exception e) {
			e.printStackTrace();
			throw new InvalidArgumentException("Read file " + path + " with error.");
		}
	}

	public static BlockCompressedInputStream makeBGZ(final String path) {
		try {
			return new BlockCompressedInputStream(SeekableStreamFactory.getInstance()
					.getBufferedStream(SeekableStreamFactory.getInstance().getStreamFor(path)));
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	private static boolean isValidBlockHeader(final byte[] buffer) {
        return (buffer[0] == BlockCompressedStreamConstants.GZIP_ID1 &&
                (buffer[1] & 0xFF) == BlockCompressedStreamConstants.GZIP_ID2 &&
                (buffer[3] & BlockCompressedStreamConstants.GZIP_FLG) != 0 &&
                buffer[10] == BlockCompressedStreamConstants.GZIP_XLEN &&
                buffer[12] == BlockCompressedStreamConstants.BGZF_ID1 &&
                buffer[13] == BlockCompressedStreamConstants.BGZF_ID2);
    }

	public static long getAddress(final SeekableStream mFile) {
		try {
			mFile.seek(mFile.length() - BlockCompressedStreamConstants.EMPTY_GZIP_BLOCK.length - 8);
			byte[] buf = new byte[8];
			mFile.read(buf);
			return ByteBuffer.wrap(buf).order(ByteOrder.LITTLE_ENDIAN).getLong();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return 0;
	}

	public static String getAbsolutePath(final String path) {
		if(SeekableStreamFactory.isFilePath(path)) {
			return new File(path).getAbsolutePath();
		} else {
			return path;
		}
	}

	public static int readBytes(final InputStream stream, final byte[] buffer, final int offset, final int length) throws IOException {
        int bytesRead = 0;
        while (bytesRead < length) {
            final int count = stream.read(buffer, offset + bytesRead, length - bytesRead);
            if (count <= 0) {
                break;
            }
            bytesRead += count;
        }
        return bytesRead;
    }

	public static <T> T nonNull(final T object, final String message) {
		if (object == null) {
			throw new IllegalArgumentException(message);
		}
		return object;
	}

	public static String printLogHeader(String header) {
		int size = (int)((30 - header.trim().length())/2);
		for (int i = 0; i < size ; i++) {
			header = " " + header;
		}
		for (int i = 0; i < size ; i++) {
			header = header + " ";
		}
		return String.format("\n\n----------------------------------------------------%s----------------------------------------------------", header);
	}

	public static boolean isEmptyLine(final String line) {
		return line.trim().equals("");
	}

	public static String[] parserHeader(final String header, final String splitChar) {
		String[] parts = header.split(splitChar);

		if (parts.length < 2)
			throw new InvalidArgumentException(String.format("there are not enough columns present in the header line: %s", header));

		return parts;
	}

	public static String[] parserHeaderComma(final String header) {
		String[] strings = header.split(GlobalParameter.TAB);
		if(strings.length < 2) {
			strings = header.split(GlobalParameter.COMMA);

			if (strings.length < 2)
				throw new InvalidArgumentException(String.format("there are not enough columns present in the header line: %s", header));
		}
		return strings;
	}


	public static String[] setDefaultCol(final String[] parts) {
		for (int i = 0; i < parts.length; i++) {
			parts[i] = GlobalParameter.COL + (i + 1);
		}
		return parts;
	}

	public static AbstractFileReader getReader(final String path, final FileType fileType, final Format format) throws FileNotFoundException {
		if(format.getType() == FormatType.VCF)  {
			return new VCFFileReader(path, fileType, format);
		} else if(format.getType() == FormatType.BED)  {
			return new BEDFileReader(path, fileType, format);
		} else {
			return new TABFileReader(path, fileType, format);
		}
	}

	public static AbstractFileReader getReader(QueryReaderItf itf, Format format) throws FileNotFoundException {
		if(format.getType() == FormatType.VCF)  {
			return new VCFFileReader(itf, format);
		} else if(format.getType() == FormatType.BED)  {
			return new BEDFileReader(itf, format);
		} else {
			return new TABFileReader(itf, format);
		}
	}

	public static LocCodec getDefaultLocCodec(final Format format, final boolean isFull) {
		LocCodec locCodec;
		if(format.type == FormatType.VCF) {
			locCodec = new VCFLocCodec(format, isFull);
		} else if(format.type == FormatType.BED) {
			locCodec = new BEDLocCodec(format, isFull);
		} else {
			locCodec = new TABLocCodec(format, isFull);
		}
		return locCodec;
	}

	public static AbstractQuery getQuery(final Mode mode, final List<Database> dbs, final boolean isCount) {
		if(mode == Mode.TABIX) {
			return new TabixQuery(dbs);
		} else if(mode == Mode.SWEEP) {
			return new SweepQuery(dbs);
		} else {
			return new VannoQuery(dbs, isCount);
		}
	}

	public static ChromosomeType toChromosomeType(final String chr) {
		if(chrToTypeMap.get(chr) != null) chrToTypeMap.get(chr);

		String uppercase = chr.toUpperCase().replaceFirst("CHR", "");
		if(uppercase.equals("X")) {
			chrToTypeMap.put(chr, ChromosomeType.X_CHROMOSOMAL);
		} else if(uppercase.equals("Y")) {
			chrToTypeMap.put(chr, ChromosomeType.Y_CHROMOSOMAL);
		} else {
			try {
				int chrno = Integer.parseInt(uppercase);
				if(chrno >= 1 && chrno <= 22) {
					chrToTypeMap.put(chr, ChromosomeType.AUTOSOMAL);
				} else {
					throw new InvalidArgumentException(String.format("Unknown chromosome: %s", chr));
				}
			} catch (NumberFormatException e) {
				chrToTypeMap.put(chr, ChromosomeType.OTHER);
			}
		}

		return chrToTypeMap.get(chr);
	}
}
