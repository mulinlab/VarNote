package constants;

import htsjdk.samtools.seekablestream.SeekableStreamFactory;
import htsjdk.samtools.util.IOUtil;
import htsjdk.tribble.index.tabix.TabixFormat;
import htsjdk.variant.vcf.VCFHeaderLineCount;
import htsjdk.variant.vcf.VCFHeaderLineType;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import joptsimple.OptionSet;
import bean.config.run.AnnoRunBean.FileFormatSupport;
import bean.config.run.DBRunConfigBean;
import bean.config.run.CountRunBean.Mode;
import bean.config.run.OverlapRunBean.OutMode;
import bean.config.run.DBRunConfigBean.IndexFormat;
import postanno.FormatWithRef;


public class VannoUtils {
	public enum PROGRAM {
		OVERLAP,
		INDEX,
		ANNO,
		COUNT
	}
	
	public enum INDEXTYPE {
		PLUS,
		PLUS_INDEX,
		ALL
	}
	
	public static final String HEADER_CHR = "CHROM";
	public static final String HEADER_POS = "POS";
	public static final String HEADER_FROM = "FROM";
	public static final String HEADER_TO = "TO";
	public static final String HEADER_REF = "REF";
	public static final String HEADER_ALT = "ALT";
	public static final String HEADER_INFO = "INFO";
	public static final String HEADER_QUAL = "QUAL";
	public static final String HEADER_FILTER = "FILTER";

	public static final String COMMA_SPILT = ",";
	public static final String TAB_SPILT = "\t";
	
	public static BufferedReader getReader(String filePath) throws FileNotFoundException {
		return new BufferedReader(new InputStreamReader(new FileInputStream(filePath)));
	}
	
	public static IndexFormat checkIndexType(String indexType) {
		indexType = indexType.toLowerCase().trim();
		if(indexType.equals("tbi")) {
			return IndexFormat.TBI;
		} else if(indexType.equals("plus")) {
			return IndexFormat.PLUS;
		} else {
			throw new IllegalArgumentException(ErrorMsg.VU_FORMAT_ERR + indexType);
		}
	}
	
	public static OutMode checkOutMode(int outMode) {
		if(outMode == 0) {
			return OutMode.QUERY;
		} else if(outMode == 1) {
			return OutMode.DB;
		} else if(outMode == 2) {
			return OutMode.BOTH;
		} else {
			throw new IllegalArgumentException(ErrorMsg.VU_OUT_MODE_ERR);
		}
	}
	
	public static VCFHeaderLineType checkType(String type) {
		type = type.trim().toLowerCase();
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
			throw new IllegalArgumentException(ErrorMsg.VU_LINE_TYPE_ERR);
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
			throw new IllegalArgumentException(ErrorMsg.VU_NUM_ERR);
		}
	}
	
	public static Mode checkMode(int mode) {
		if(mode == 0) {
			return Mode.TABIX;
		} else if(mode == 1) {
			return Mode.MIX;
		} else if(mode == 2) {
			return Mode.SWEEP;
		} else {
			throw new IllegalArgumentException(ErrorMsg.VU_MODE_ERR);
		}
	}
	
	public static FormatWithRef determineType(String fileName) {
		fileName = trimAndLC(fileName);
		if (fileName.endsWith(".vcf") || fileName.endsWith(".vcf.gz")) {
			 return FormatWithRef.VCF;
        } else if (fileName.endsWith(".bed") ||fileName.endsWith(".bed.gz")) {
        	 return FormatWithRef.BED;
        } else {
       	 	 throw new IllegalArgumentException(ErrorMsg.VU_DT_ERR);
        }
	}
	
	public static FileFormatSupport checkFileFormat(String format) {
		format = trimAndLC(format);
		if (format.equals("vcf")) {
			return FileFormatSupport.VCF;
		} else if(format.equals("bed")) {
			return FileFormatSupport.BED;
		} else {
			throw new IllegalArgumentException("We only support vcf and bed file format currently.");
		}
	}
	
	public static boolean strToBool(String str) {
		str = trimAndLC(str);
		if(str.equals("true")) {
			return true;
		} else if(str.equals("false")) {
			return false;
		} else {
			throw new IllegalArgumentException(ErrorMsg.VU_BOOL_ERR + str + " .");
		}
	}
	
	public static String trimAndLC(String str) {
		return str.trim().toLowerCase();
	}
	
	public static FormatWithRef parseIndexFileFormat(String str) {
		str = trimAndLC(str);
        if(str.equals("bed")) {
        	return FormatWithRef.BED;
        } else if(str.equals("vcf")) {
        	return FormatWithRef.VCF;
        } else if(str.equals("tab")) { 	
        	return FormatWithRef.TAB;
        } else {
        	throw new IllegalArgumentException(ErrorMsg.INDEX_FILE_FORMAT_ERR + str);
        }
	}
	
	public static Map<String, Integer> getHeaderMap() {
		Map<String, Integer> map = new HashMap<String, Integer>();
		map.put(HEADER_CHR, -1);
		map.put(HEADER_REF, -1);
		map.put(HEADER_ALT, -1);
		map.put(HEADER_FROM, -1);
		map.put(HEADER_TO, -1);
		map.put(HEADER_POS, -1);
		map.put(HEADER_INFO, -1);
		map.put(HEADER_QUAL, -1);
		map.put(HEADER_FILTER, -1);
		return map;
	}
	
	public static List<String> split(final String columns, final String label, String regex) {
		if(columns.trim().equals("")) throw new IllegalArgumentException("Cann't process empty value: " + label);
    	
    	String[] cols = columns.split(regex);
    	if (regex == TAB_SPILT) regex = "TAB";
    	if(cols.length == 1) throw new IllegalArgumentException(columns + " should be seperated by " + regex);
    	List<String> list = new ArrayList<String>();
    	for (String col : cols) {
    		list.add(col.trim());
		}
    	return list;
	}
	
//	public static FormatWithRef updateFormat(final String file, FormatWithRef format, final String columns) {
//		if(columns.trim().equals("")) throw new IllegalArgumentException(ErrorMsg.INDEX_FILE_COLUMN_CMD);
//  
//    	return updateFormat(file, format, split(columns, "columns", VannoUtils.COMMA_SPILT));
//	}
	
	
	public static FormatWithRef updateFormat(final String file, FormatWithRef format, final List<String> cols) {
		
    	Map<String, Integer> map = getHeaderMap();
    	
		for (int j = 0; j < cols.size(); j++) {
			map.put(cols.get(j).trim().toUpperCase(), j + 1);
		}
		
		if(format.tbiFormat == null) {
			format.tbiFormat = new TabixFormat();
			format.tbiFormat.flags = TabixFormat.GENERIC_FLAGS;
		}
		
		format.tbiFormat.sequenceColumn = map.get(HEADER_CHR);
		
		if(map.get(HEADER_POS) != -1) {
			format.tbiFormat.startPositionColumn = map.get(HEADER_POS);
			format.tbiFormat.endPositionColumn = 0;
			format.flag = 3;
		} else if((map.get(HEADER_FROM) != -1) && (map.get(HEADER_TO) != -1)) {
			format.tbiFormat.startPositionColumn = map.get(HEADER_FROM);
			format.tbiFormat.endPositionColumn = map.get(HEADER_TO);
			format.flag = 2;
		} else {
			throw new IllegalArgumentException(ErrorMsg.MISS_POS + ErrorMsg.MISS);
		}
		
		format.refCol = map.get(HEADER_REF);
		format.altCol = map.get(HEADER_ALT);
		format.infoCol = map.get(HEADER_INFO);
		return format;
	}
	
	public static int adjustCol(int col) {
		return (col == -1) ? col : (col + 1);
	}
	
	public static void addFileForPattern(DBRunConfigBean db, List<DBRunConfigBean> dbs) {
		final String path = db.getDbPath().trim();
		if(!SeekableStreamFactory.isFilePath(path) || path.indexOf("*") == -1) {
   		 	db.checkDBConfig();
   		 	dbs.add(db);
		} else {
			final IndexFormat format = db.getIndexFormat();
			final List<String> files = IOutils.readFileWithPattern(db.getDb());
	    	
			for (String file : files) {
	    		 db = new DBRunConfigBean();
	    		 db.setDb(file);
	    		 if(format != null) db.setIndexFormat(format); 
				 else db.setDbIndex();
	    		 db.checkDBConfig();
	    		 dbs.add(db);
	    	}
		}
	}
	
	public static INDEXTYPE checkWriteIndexType(String type) {
		type = type.trim().toLowerCase();
		if(type.equals("plus")) {
			return INDEXTYPE.PLUS;
		} else if(type.equals("pindex")) {
			return INDEXTYPE.PLUS_INDEX;
		} else if(type.equals("all")) {
			return INDEXTYPE.ALL;
		} else {
			throw new IllegalArgumentException(ErrorMsg.INDEX_TYPE_ERR + type);
		}
	}
	
	public static void checkMissing(OptionSet options, String[] requiredOPT) {
        Set<String> missing = new HashSet<String>();
        for (String opt : requiredOPT) {
	       	 if(!options.has(opt)) {
	       		 missing.add(opt);
	       	 } 
		}         
       
        if(missing.size() > 0) {
		     System.err.println(ErrorMsg.MISS_ERROR + String.join(", ", missing.toArray(new String[0])));
		     System.exit(1);
		}
	}
	
	public static PROGRAM checkARG(String arg) {
		PROGRAM program = null;
		 if(arg.equals("overlap")) {
			 program = PROGRAM.OVERLAP;
		 } else if(arg.equals("index")) {
			 program = PROGRAM.INDEX;
		 } else if(arg.equals("anno")) {
			 program = PROGRAM.ANNO;
		 } else if(arg.equals("count")) {
			 program = PROGRAM.COUNT;
		 } else {
			 System.err.println(ErrorMsg.ARG_ERROR);
			 System.exit(1);
		 }
		 return program;
	}
	
	public static void assertOutputIsValid(final String output) {
	      if (output == null) {
	    	  throw new IllegalArgumentException("Cannot check validity of null output.");
	      }
	      if (!IOUtil.isUrl(output)) {
	    	  IOUtil.assertFileIsWritable(new File(output));
	      }
	}

}
