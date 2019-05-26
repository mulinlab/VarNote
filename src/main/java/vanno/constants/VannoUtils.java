package main.java.vanno.constants;

import htsjdk.samtools.seekablestream.SeekableStreamFactory;
import htsjdk.samtools.util.BlockCompressedStreamConstants;
import htsjdk.samtools.util.IOUtil;
import htsjdk.samtools.util.StringUtil;
import htsjdk.variant.vcf.VCFHeaderLineCount;
import htsjdk.variant.vcf.VCFHeaderLineType;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import joptsimple.OptionSet;
import main.java.vanno.bean.config.run.AnnoRunConfig.AnnoOutFormat;
import main.java.vanno.bean.config.run.OverlapRunConfig.Mode;
import main.java.vanno.bean.database.Database.IndexType;
import main.java.vanno.bean.database.DatabaseConfig.IntersectType;
import main.java.vanno.bean.database.index.IndexFactory;
import main.java.vanno.bean.format.Format;
import main.java.vanno.bean.format.Format.FormatType;
import main.java.vanno.bean.query.Output.OutMode;
import main.java.vanno.index.MyBlockCompressedInputStream;
import main.java.vanno.index.MyEndianOutputStream;

public final class VannoUtils {
	public final static String INTERSECT_ERROR = "Intersection type, valid values are " + IntersectType.OVERLAP.getVal() + "(" + IntersectType.OVERLAP.getName() + ")" +
			" or " + IntersectType.EXACT.getVal() + "(" + IntersectType.EXACT.getName() + ") or " + IntersectType.FULLCLOASE.getVal() + "(" + IntersectType.FULLCLOASE.getName() + ")" + 
			". default is:" +  IntersectType.OVERLAP.getName() + ".";
	
	public enum PROGRAM {
		OVERLAP("overlap", "record intersection for a list of intervals or variants"),
		INDEX("index", "indexes a VCF, BED or TAB-delimited annotation files and creates two VarNote index files (.vanno and .vanno.vi)"),
		ANNO("anno", "annotation extraction for a list of intervals or variants"),
		QUERY("query", "random access of intersected annotations for a specified genomic region like \"chr:beginPos-endPos\"");
		
		private final String name;
		private final String desc;
		
		PROGRAM(final String name, final String desc) {
	        this.name = name;
	        this.desc = desc;
	    }
		
		public String getName() {
			return name;
		}
		
		public String getDesc() {
			return desc;
		}
	}
	
	public enum FileType {
		BGZ, GZ, TXT
	}
	 
	public enum FileExt {
		GZ(Arrays.asList(".gz", ".gzip", ".bgz", ".bgzf")),
		VCF(Arrays.asList(".vcf", ".vcf.gz", ".vcf.bgz")),
		BED(Arrays.asList(".bed", ".bed.gz", ".bed.bgz"));
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
	
	public static BufferedReader getReader(String filePath) throws FileNotFoundException {
		return new BufferedReader(new InputStreamReader(new FileInputStream(filePath)));
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
				IndexType.VANNO.getName() + ". By default, the program will use " + BasicUtils.PRO_NAME + " index system(" + IndexType.VANNO.getName() + ").");
	}
	
	public static IntersectType checkIntersectType(String intersectType) {
		if(intersectType == null) return BasicUtils.DEFAULT_INTERSECT;
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
	
	public static Format determineFileType(String fileName) {
		fileName = trimAndLC(fileName);
		if(hasExtension(FileExt.VCF, fileName)) return Format.newVCF();
		else if(hasExtension(FileExt.BED, fileName)) return Format.newBED(); 
		else return null;
	}
	
	public static AnnoOutFormat checkFileFormat(String format) {
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
        if(str.equalsIgnoreCase(FormatType.BED.getName())) {
        		return Format.newBED();
        } else if(str.equalsIgnoreCase(FormatType.VCF.getName())) {
        		return Format.newVCF();
        } else if(str.equalsIgnoreCase(FormatType.TAB.getName())) { 	
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
	
	public static PROGRAM checkARG(String arg) {
		for (PROGRAM p : PROGRAM.values()) {
			if(arg.equalsIgnoreCase(p.getName())) {
				return p;
			}
		}	
		throw new InvalidArgumentException("Invalid arguments, please use " + BasicUtils.PRO_CMD + " " + BasicUtils.HELP_OPTION_ABBR + " for help.");
	}
	
	public static void assertOutputIsValid(final String output) {
	      if (output == null) {
	    	  		throw new InvalidArgumentException("Cannot check validity of null output.");
	      }
	      if (!IOUtil.isUrl(output)) {
	    	  		IOUtil.assertFileIsWritable(new File(output));
	      }
	}

	public static void writeFormats(final MyEndianOutputStream indexLos, final Format formatSpec, final List<String> headerColList, final List<String> sequenceNames, final Map<Integer, Long> addressOfChr) {
	    	try { 
	    		 indexLos.writeInt(BasicUtils.version);
	    		 indexLos.writeInt(IndexFactory.MAGIC_NUMBER);
	    		 indexLos.writeInt(formatSpec.getFlags());
	    		 indexLos.writeInt(formatSpec.getFieldCol(Format.H_FIELD.CHROM.toString()));
	    		 indexLos.writeInt(formatSpec.getFieldCol(Format.H_FIELD.BEGIN.toString()));
	    		 indexLos.writeInt(formatSpec.getFieldCol(Format.H_FIELD.END.toString()));
	    		 
	    		 indexLos.writeInt(formatSpec.getCommentIndicator().length());
	    		 indexLos.write(StringUtil.stringToBytes(formatSpec.getCommentIndicator()));
	
	        	 if(headerColList != null && headerColList.size() > 0) {
	        		 indexLos.writeInt(headerColList.size());
	        		 
	        		 int headerBlockSize = headerColList.size(); // null terminators
	    	         for (final String colName : headerColList) {
	    	        	 	 headerBlockSize += colName.length(); 
	    	         }
	    	
	    	         indexLos.writeInt(headerBlockSize);
	    	         
	        		 for (String colName : headerColList) {
	        			 indexLos.write(StringUtil.stringToBytes(colName));
	        			 indexLos.write(0);
				 }
	        	 } else {
	        		 indexLos.writeInt(0);
	        	 }
	    		 
	    		 indexLos.writeInt(formatSpec.getNumHeaderLinesToSkip());
	    		 indexLos.writeInt(formatSpec.getFieldCol(Format.H_FIELD.REF.toString()));
	    		 indexLos.writeInt(formatSpec.getFieldCol(Format.H_FIELD.ALT.toString()));
	    		 indexLos.writeInt(formatSpec.getFieldCol(Format.H_FIELD.INFO.toString()));
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
	
	public static List<String> getOptionList(final OptionSet options, final String key, final int size, final String lable) {
		if(options.has(key)) {
			@SuppressWarnings("unchecked")
			List<String> value = (List<String>) options.valuesOf(key);
			if((value != null) && (value.size() != size))
				throw new InvalidArgumentException("We get " + size + " database files but get " + value.size() + lable + ". Please check.");
			return value;
		} else return null;
	}
	
	public static void checkValidBGZ(final String path) {
		if(VannoUtils.checkFileType(path) != FileType.BGZ) 
			throw new InvalidArgumentException("Input file " + path + " is not in valid block compressed format(.gz, .bgz). ");
	}
	
	public static FileType checkFileType(final String path) {
		try {
			if(hasExtension(FileExt.GZ, path)) {
				final int bufferSize = Math.max(BasicUtils.BUFFER_SIZE, BlockCompressedStreamConstants.MAX_COMPRESSED_BLOCK_SIZE);
				BufferedInputStream stream;
				
					stream = new BufferedInputStream(new FileInputStream(path), bufferSize);
				
				
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
	
	public static MyBlockCompressedInputStream makeBGZ(final String path, final boolean useJDK) {
		try {
			return new MyBlockCompressedInputStream(SeekableStreamFactory.getInstance()
					.getBufferedStream(SeekableStreamFactory.getInstance().getStreamFor(path)), useJDK);
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

}
