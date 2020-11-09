package org.mulinlab.varnote.utils;

import de.charite.compbio.jannovar.pedigree.Disease;
import de.charite.compbio.jannovar.pedigree.Pedigree;
import de.charite.compbio.jannovar.pedigree.Person;
import htsjdk.samtools.seekablestream.SeekableStream;
import htsjdk.samtools.util.BlockCompressedInputStream;
import htsjdk.variant.vcf.VCFHeader;
import org.mulinlab.varnote.config.param.DBParam;
import org.mulinlab.varnote.constants.GlobalParameter;
import org.mulinlab.varnote.operations.decode.*;
import org.mulinlab.varnote.operations.query.AbstractQuery;
import org.mulinlab.varnote.operations.query.SweepQuery;
import org.mulinlab.varnote.operations.query.TabixQuery;
import org.mulinlab.varnote.operations.query.VannoQuery;
import org.mulinlab.varnote.operations.readers.itf.QueryReaderItf;
import org.mulinlab.varnote.operations.readers.query.*;
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
import java.util.regex.Pattern;
import joptsimple.OptionSet;
import org.mulinlab.varnote.utils.database.index.IndexFactory;
import org.mulinlab.varnote.utils.format.Format;
import org.mulinlab.varnote.exceptions.InvalidArgumentException;
import org.mulinlab.varnote.utils.gz.MyEndianOutputStream;
import org.mulinlab.varnote.utils.node.LocFeature;

public final class VannoUtils {
	public final static String INTERSECT_ERROR = "Invalid intersection type, effective value should be " + IntersectType.INTERSECT.getVal() + "(" + IntersectType.INTERSECT.getName() + ")" +
			", " + IntersectType.EXACT.getVal() + "(" + IntersectType.EXACT.getName() + ") and " + IntersectType.FULLCLOASE.getVal() + "(" + IntersectType.FULLCLOASE.getName() + ")" +
			". Default value is:" +  IntersectType.INTERSECT.getName() + ".";

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

	public static String getExtension(final FileExt ext, final String fileName) {
		String cleanedPath = stripQueryStringIfPathIsAnHttpUrl(fileName);
		for (final String extension : ext.getSuffix()) {
			if (cleanedPath.toLowerCase().endsWith(extension))
				return extension;
		}
		return null;
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

	public static boolean validRsid(final String rsid) {
		if(!Pattern.matches("^rs\\d+$", rsid.toLowerCase())) {
			return false;
		}

		return true;
	}

	public static LocFeature regionToNode(String reg) {
		reg = reg.toUpperCase();
		if(!Pattern.matches("^(CHR)?([0-9]{1,2}|X|Y|MT|\\w+):\\d+-\\d+$", reg)) {
			throw new InvalidArgumentException(String.format("Invalid format for chromosome region %s, please type correct one as chr1:4380800-4380801 or 1:4380800-4380801", reg));
		}
		LocFeature query = new LocFeature();
		int colon, hyphen;

		colon = reg.indexOf(':');

		if(colon == -1) {
			colon = reg.indexOf('-');
		}
		hyphen = reg.indexOf('-', colon + 1);

		query.chr = colon >= 0 ? reg.substring(0, colon) : reg;
		query.beg = colon >= 0 ? Integer.parseInt(reg.substring(colon + 1, hyphen >= 0 ? hyphen : reg.length())) : 0;
		query.end = hyphen >= 0 ? Integer.parseInt(reg.substring(hyphen + 1)) : 0x7fffffff;

		query.chr = query.chr.toLowerCase().replace("chr", "");
		return query;
	}

	public static LocFeature posToNode(String reg) {
		reg = reg.toUpperCase();

		if(!Pattern.matches("^(CHR)?([0-9]{1,2}|X|Y|MT|\\w+):\\d+$", reg)) {
			throw new InvalidArgumentException(String.format("Invalid format for chromosome position %s, please type correct one as chr1:4380800 or 1:4380800", reg));
		}

		LocFeature query = new LocFeature();
		int colon;

		colon = reg.indexOf(':');
		query.chr = colon >= 0? reg.substring(0, colon) : reg;
		query.beg = colon >= 0? Integer.parseInt(reg.substring(colon + 1)) - 1 : 0;
		query.end = query.beg + 1;

		query.chr = query.chr.toLowerCase().replace("chr", "");
		return query;
	}

	public static LocFeature posAlleleToNode(String reg) {
		reg = reg.toUpperCase();

		if(!Pattern.matches("^(CHR)?([0-9]{1,2}|X|Y|MT|\\w+):\\d+-[ATCG]+-[ATCG]+$", reg)) {
			throw new InvalidArgumentException(String.format("Invalid format for chromosome position with alleles %s, please type correct one as chr1:4380800-A-G or 1:4380800-A-G", reg));
		}

		LocFeature query = new LocFeature();
		int colon, hyphen;

		//chr1:54132-A-G
		colon = reg.indexOf(':');  hyphen = reg.indexOf('-');
		query.chr = colon >= 0? reg.substring(0, colon) : reg;
		query.beg = colon >= 0? Integer.parseInt(reg.substring(colon + 1, hyphen)) - 1 : 0;
		String[] refalt = reg.substring(hyphen+1).split("-");

		query.ref = refalt[0];
		query.alt = refalt[1];
		query.end = query.beg + query.ref.length();

		query.chr = query.chr.toLowerCase().replace("chr", "");
		return query;
	}
	
	public static IndexType checkIndexType(String indexType) {
		if(indexType == null) return null;
		indexType = trimAndLC(indexType);
		for (IndexType t: IndexType.values()) {
			if(indexType.equals(t.getName())) {
				return t;
			}
		}	
		throw new InvalidArgumentException("Invalid index type, effective value should be tbi or varnote. Default value is varnote.");
	}
	
	public static IntersectType checkIntersectType(String intersectType) {
		if(intersectType == null) return GlobalParameter.DEFAULT_INTERSECT;

        IntersectType intersectType1 = IntersectType.toIntersectType(Integer.parseInt(trimAndLC(intersectType)));
		if(intersectType == null) throw new InvalidArgumentException(INTERSECT_ERROR);
		else return intersectType1;
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
			throw new InvalidArgumentException("Unsupported VCF INFO Type for XX, following ones are supported: 'integer', 'float', 'string', 'character' or 'flag'");
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
			throw new InvalidArgumentException("Unsupported VCF INFO Number for XX, following ones are supported: 'integer', '.', 'r', 'g', 'a' or ‘+’");
		}
	}
	
	public static Mode checkMode(int mode) {
		for (Mode m : Mode.values()) {
			if(mode == m.getNum()) {
				return m;
			}
		}	
		throw new InvalidArgumentException("Invalid searching mode, effective value should be 0, 1 or 2. 0:random access mode, 1:mix mode, 2:sweep mode.");
	}
	
	public static Format determineFileType(String fileName) {
		fileName = trimAndLC(fileName);

		if(hasExtension(FileExt.VCF, fileName)) return Format.newVCF();
		else if(hasExtension(FileExt.BED, fileName)) return Format.newBED();
		else return null;
	}

	public static boolean formatIsMatch(String fileName, final Format format) {
		fileName = trimAndLC(fileName);
		String ext = getExtension(FileExt.VCF, fileName);
		if(ext != null && format.type != FormatType.VCF) {
			throw new InvalidArgumentException(String.format("The input file name contains suffix of %s, but a %s format was identified. Please check!", ext, format.type));
		}

		ext = getExtension(FileExt.BED, fileName);
		if(ext != null && format.type != FormatType.BED) {
			throw new InvalidArgumentException(String.format("The input file name contains suffix of %s, but a %s format was identified. Please check!", ext, format.type));
		}
		return true;
	}

	public static Format checkQueryFormat(String format) {
		format = format.toUpperCase();
		if(format.equals(FormatType.VCF.toString())) {
			return Format.newVCF();
		} else if(format.equals(FormatType.VCFLIKE.toString())) {
			return VannoUtils.getVCFLikeFormat();
		} else if(format.equals(FormatType.BED.toString())) {
			return Format.newBED();
		} else if(format.equals(FormatType.BEDALLELE.toString())) {
			return VannoUtils.getBedAlleleFormat();
		} else if(format.equals(FormatType.COORDONLY.toString())) {
			return VannoUtils.getCoordOnlyFormat();
		} else if(format.equals(FormatType.COORDALLELE.toString())) {
			return VannoUtils.getCoordAlleleFormat();
		} else if(format.equals(FormatType.TAB.toString())) {
			return Format.newTAB();
		} else {
			return null;
		}
	}

	public static List<DBParam> removeRSParam(List<DBParam> dbParams) {
		List<DBParam> dbParamsNew = new ArrayList<>();
		for (DBParam dbParam:dbParams) {
			if(!dbParam.getOutName().equals(GlobalParameter.MERGE_LABEL) && !dbParam.getOutName().equals(GlobalParameter.RSID_POS_LABEL)) {
				dbParamsNew.add(dbParam);
			}
		}
		return dbParamsNew;
	}

	public static AnnoOutFormat checkOutFormat(String format) {
		format = trimAndLC(format);
		if (format.equals("vcf")) {
			return AnnoOutFormat.VCF;
		} else if(format.equals("bed")) {
			return AnnoOutFormat.BED;
		} else {
			throw new InvalidArgumentException("Only VCF and BED format are supported as output currently.");
		}
	}

	public static String getName(String str) {
		int end = str.lastIndexOf(".");
		if(end != -1) {
			return str.substring(0, end);
		} else {
			return str;
		}
	}

	public static boolean strToBool(String str) {
		str = trimAndLC(str);
		if(str.equals("true")) {
			return true;
		} else if(str.equals("false")) {
			return false;
		} else {
			throw new InvalidArgumentException("Invalid parameter for " + str + ", only true or false are supported.");
		}
	}
	
	public static String trimAndLC(String str) {
		return str.trim().toLowerCase();
	}

	public static Format parseIndexFileFormat(String str) {
		str = trimAndLC(str);
        if(str.equalsIgnoreCase("bed")) {
			return Format.newBED();
        } else if(str.equalsIgnoreCase("vcf")) {
			return Format.newVCF();
        } else if(str.equalsIgnoreCase("tab")) {
			return Format.newTAB();
        } else {
				throw new InvalidArgumentException("Invalid format for " + str + ", only bed, vcf and tab format are supported.");
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
			throw new InvalidArgumentException(String.format("Invalid compressed input file %s, please ensure an intact compressed format (.gz, .bgz).", path));
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
			throw new InvalidArgumentException(String.format("Insufficient columns in the header line: %s", header));

		return parts;
	}

	public static String[] parserHeaderComma(final String header) {
		String[] strings = header.split(GlobalParameter.TAB);
		if(strings.length < 2) {
			strings = header.split(GlobalParameter.COMMA);

			if (strings.length < 2)
				throw new InvalidArgumentException(String.format("Insufficient columns in the header line: %s", header));
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

	public static LocCodec getDefaultLocCodec(final Format format, final boolean isFull, final VCFHeader vcfHeader) {
		LocCodec locCodec;
		if(format.type == FormatType.VCF) {
			locCodec = new VCFLocCodec(format, isFull, vcfHeader);
		} else if(format.type == FormatType.BED) {
			locCodec = new BEDLocCodec(format, isFull);
		} else if(format.type == FormatType.RSID) {
			locCodec = new RSIDLocCodec(format);
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

    public static int getAffectPersonNumber(final Pedigree pedigree) {
	    int affectNumber = 0;
        for (Person p : pedigree.getMembers()) {
            final Disease d = p.getDisease();

            if (d == Disease.AFFECTED) {
                affectNumber++;
            }
        }

        return affectNumber;
    }

	public static final int NOT_INDEL = 0;
	public static final int INSERTION = 1;
	public static final int DELETION = 2;
	public static final int OUT_LEN = 3;
	public static final int MAX_LEN = 1000;

	public static int isIndel(final LocFeature node) {
		if(node.ref == null) return NOT_INDEL;

		if(node.alt.indexOf(",") != -1) {
			int type;
			for (String alt: node.getAlts()) {
				type = isIndel(node.ref, alt);
				if(type == OUT_LEN || type != NOT_INDEL) return type;
			}

			return NOT_INDEL;
		} else {
			return isIndel(node.ref, node.alt);
		}
	}

	public static int isIndel(final String ref, final String alt) {
		if(ref == null) return NOT_INDEL;
		if(Math.abs(ref.length() - alt.length()) > MAX_LEN) return OUT_LEN;

		if (ref.length() == alt.length()) {
			return NOT_INDEL;
		} else if(ref.length() > alt.length()) {
			return DELETION;
		} else {
			return INSERTION;
		}
	}

	public static List<LocFeature> getREMMList(final LocFeature node, final int type) {
		List<LocFeature> list = new ArrayList<>();
		int beg = node.beg;;

		if(type == VannoUtils.INSERTION) {
			for (int i = 0; i <= 1; i++) {
				list.add(new LocFeature(beg + i, beg + i + 1, node.chr));
			}
		} else {
			for (int i = 1; i < node.ref.length(); i++) {
				list.add(new LocFeature(beg + i, beg + i + 1, node.chr));
			}
		}

		return list;
	}

	public static List<LocFeature> getFeaturesMatchRefAndAltList(final LocFeature query, final LocFeature[] features) {
		List<LocFeature> locFeatures = new ArrayList<>();
		if(features != null) {
			for (LocFeature locFeature: features) {
				if(locFeature.ref.equals(query.ref) && locFeature.alt.equals(query.alt)) {
					locFeatures.add(locFeature);
				}
			}
		}
		return locFeatures;
	}

	public static LocFeature getFeatureMatchRefAndAlt(final LocFeature query, final LocFeature[] features) {
		if(features != null) {
			for (LocFeature feature: features) {
				if(feature.ref.equals(query.ref) && feature.alt.equals(query.alt)) {
					return feature;
				}
			}
		}
		return null;
	}

	public static LocFeature getFeatureInAlt(final LocFeature query, final LocFeature[] features) {
		if(features != null) {
			for (LocFeature feature: features) {
				if(feature.ref.equals(query.ref)) {
					for (String alt:query.getAlts()) {
						if (feature.alt.equals(alt)) {
							return feature;
						}
					}
				}
			}
		}
		return null;
	}

	public static Format getBedAlleleFormat() {
		Format format = Format.newBED();
		format.refPositionColumn = 4;
		format.altPositionColumn = 5;
		format.type = FormatType.BEDALLELE;
		return format;
	}

	public static Format getCoordAlleleFormat() {
		Format format = Format.newTAB();
		format.sequenceColumn = 1;
		format.startPositionColumn = 2;
		format.endPositionColumn = 2;
		format.refPositionColumn = 3;
		format.altPositionColumn = 4;
		format.type = FormatType.COORDALLELE;
		return format;
	}

	public static Format getCoordOnlyFormat() {
		Format format = Format.newTAB();

		format.sequenceColumn = 1;
		format.startPositionColumn = 2;
		format.endPositionColumn = 2;
		format.type = FormatType.COORDONLY;
		return format;
	}

	public static Format getVCFLikeFormat() {
		Format format = Format.newTAB();

		format.sequenceColumn = 1;
		format.startPositionColumn = 2;
		format.endPositionColumn = 2;

		format.refPositionColumn = 4;
		format.altPositionColumn = 5;
		format.type = FormatType.VCFLIKE;
		return format;
	}

	public static Format getRsid2POSFormat() {
		Format format = getVCFLikeFormat();
		format.rsidPositionColumn = Format.DEFAULT_RSID_COL;
		return format;
	}

	public static Format getQuickFormat(QuickFileType quickFileType) {
		if(quickFileType == QuickFileType.VCF) {
			return Format.newVCF();
		} else if(quickFileType == QuickFileType.VCFLike) {
			return getVCFLikeFormat();
		} else if(quickFileType == QuickFileType.CoordOnly) {
			return getCoordOnlyFormat();
		} else if(quickFileType == QuickFileType.CoordAllele) {
			return getCoordAlleleFormat();
		} else {
			throw new InvalidArgumentException("Only VCF, VCFLike, CoordOnly and CoordAllele format are supported.");
		}
	}
}
