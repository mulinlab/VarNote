package main.java.vanno.bean.config.config;

import htsjdk.samtools.util.IOUtil;
import main.java.vanno.bean.database.DatabaseConfig.IntersectType;
import main.java.vanno.bean.query.LineIteratorImpl;
import main.java.vanno.bean.query.Log;
import main.java.vanno.bean.query.Output.OutMode;
import main.java.vanno.constants.BasicUtils;
import main.java.vanno.constants.InvalidArgumentException;
import main.java.vanno.constants.VannoUtils;
import java.io.FileNotFoundException;
import java.io.IOException;

public class AbstractReadConfig<T> implements ReadConfigInterface<T>{
	
	public final static IntersectType DEFAULT_INTERSECT = BasicUtils.DEFAULT_INTERSECT;
	public final static Log DEFAULT_LOG = BasicUtils.DEFAULT_LOG;
	public final static OutMode DEFALT_OUT_MODE = BasicUtils.DEFALT_OUT_MODE;
	public final static boolean DEFAULT_REMOVE_COMMENT = BasicUtils.DEFAULT_REMOVE_COMMENT;
	public final static boolean DEFAULT_LOJ = BasicUtils.DEFAULT_LOJ;
	public final static boolean DEFAULT_IS_GZIP = BasicUtils.DEFAULT_IS_GZIP;
	
	public static final String COMMENT_LINE = BasicUtils.COMMENT_LINE;
	public static final String OVERLAP_NOTE = BasicUtils.OVERLAP_NOTE;
	public static final String OVERLAP_EQUAL = BasicUtils.OVERLAP_EQUAL;
	
//	public static final String REQUIRED_BEGIN = "[required]";
//	public static final String OPTIONAL_BEGIN = "[optional]";
	public static final String DB_BEGIN = "[db]";
	public static final String QUERY = "query_file";
	public static final String QUERY_FORMAT = "query_format";
	public static final String ZERO_BASED = "zero_based";
	public static final String USE_JDK_INFLATER = "use_jdk_inflater";
	public static final String REMOVE_COMMENT = "remove_comment";
	public static final String QUERY_CHROM = "chrom";
	public static final String QUERY_POS = "pos";
	public static final String QUERY_BEGIN = "begin";
	public static final String QUERY_END = "end";
	public static final String QUERY_REF = "ref";
	public static final String QUERY_ALT = "alt";
	public static final String HEADER_PATH = "header_path";
	public static final String HEADER = "has_header";
	public static final String COMMNT_INDICATOR = "comment_indicator";
//	public static final String HEADER_INDICATOR = "header_indicator";
	
	public static final String DB_PATH = "db_path";
	public static final String DB_FORMAT = "db_format";
	public static final String DB_INDEX_TYPE = "db_index_type";
	public static final String DB_LABEL= "db_label";
	public static final String DB_INTERSECT = "db_type";
	
	public static final String OUTPUT_FOLDER = "out_folder";
	public static final String OUTPUT_MODE = "out_mode";
	public static final String LOJ = "loj";
	public static final String UNZIP = "unzip";
	public static final String VERBOSE = "verbose";
	public static final String MODE = "mode";
	public static final String THREAD = "thread";
	public static final String COUNT = "count";
	public static final String END = "end";
	public static final String ANNO_CONFIG = "anno_config";
	public static final String ANNO_OUTPUT_FORMAT = "anno_out_format";
//	public static final String COLUMN_NAME = "column_name";
	
	protected String qFile;
	
	@Override
	public T read(final String filePath) {
		IOUtil.assertInputIsValid(filePath);
		T obj = null;
		try {
			doInit();
			final LineIteratorImpl reader = new LineIteratorImpl(filePath, true, VannoUtils.checkFileType(filePath));
			String line;
			while((line = reader.advance()) != null) {
				line = line.trim();
				if(isBreak(line)) break;
				if(filterLine(line)) {
					processLine(line);
				}
			}
			reader.close();
			obj = doEnd();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return obj;
	}

	

	@Override
	public boolean isBreak(String line) {
		return false;
	}

	public String[] splitLine(String line) {
		if(line.indexOf(OVERLAP_EQUAL) != -1) {
			return line.split(OVERLAP_EQUAL);
		} else if(line.indexOf(':') != -1) {
			return line.split(":");
		} else {
			throw new InvalidArgumentException("Cannot parse line : \"" + line +"\", properties should have a format like key=value or key:value.");
		}
	}

	public void putValue(String key, String val) {
		switch (key) {
			case QUERY:
				qFile = val;
				break;
			default:
				break;
		}
	}

	@Override
	public void doInit() {
	}

	@Override
	public void processLine(String line) {
		String[] lineSplit = splitLine(line);
		if(lineSplit.length > 1)
			putValue(lineSplit[0].trim().toLowerCase(), lineSplit[1].trim());
	}

	@Override
	public boolean filterLine(String line) {
		return false;
	}

	@Override
	public T doEnd() {
		return null;
	}
}
