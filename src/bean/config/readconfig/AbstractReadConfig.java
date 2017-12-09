package bean.config.readconfig;

import htsjdk.samtools.util.IOUtil;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;

import bean.config.run.AbstractRunCongig;
import constants.VannoUtils;

public class AbstractReadConfig<T> implements ReadConfigInterface<T>{
	public static final String COMMENT_LINE = "#";
	public static final String OVERLAP_NOTE = "@";
	public static final String OVERLAP_EQUAL = "=";
	public static final String REQUIRED_BEGIN = "[required]";
	public static final String OPTIONAL_BEGIN = "[optional]";
	public static final String DB_BEGIN = "[db]";
	public static final String QUERY = "query_file";
	public static final String QUERY_FORMAT = "query_format";
	public static final String QUERY_COLUMN = "query_columns";    
	public static final String DB_PATH = "db_path";
	public static final String DB_FORMAT = "db_format";
	public static final String DB_INDEX_TYPE = "db_index_type";
	public static final String DB_LABEL= "db_label";
	public static final String OUTPUT_FOLDER = "out_folder";
	public static final String OUTPUT_MODE = "out_mode";
	public static final String LOJ = "loj";
	public static final String FC = "fc";
	public static final String MODE = "mode";
	public static final String THREAD = "thread";
	public static final String EXACT = "exact";
	public static final String END = "end";
	public static final String ANNO_CONFIG = "anno_config";
	public static final String ANNO_OUTPUT_FORMAT = "anno_out_format";
	
	@Override
	public T read(T obj, String filePath) {
		IOUtil.assertInputIsValid(filePath);
	
		try {
			final BufferedReader reader = VannoUtils.getReader(filePath);
			String line;
			while((line = reader.readLine()) != null) {
				line = line.trim();
				if(isBreak(line)) break;
				if(filterLine(obj, line)) {
					processLine(obj, line);
				}
			}
			
			reader.close();
			doEnd(obj);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return obj;
	}

	@Override
	public boolean filterLine(final T obj, String line) {
		return false;
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
			throw new IllegalArgumentException("Cannot parse line : \"" + line +"\", properties should have a format like key=value or key:value.");
		}
	}

	public void putValue(T obj, String key, String val) {
		AbstractRunCongig bean = (AbstractRunCongig)obj;
		switch (key) {
			case QUERY:
				bean.setQueryFile(val);
				break;
			case QUERY_COLUMN:
				bean.setQueryColumns(VannoUtils.split(val, QUERY_COLUMN, VannoUtils.COMMA_SPILT));
				break;
			default:
				break;
		}
	}

	@Override
	public void doEnd(T obj) {
		AbstractRunCongig bean = (AbstractRunCongig)obj;
		bean.addDB();
		bean.checkRequired();
		bean.checkFormat();
	}

	@Override
	public void processLine(T obj, String line) {
		String[] lineSplit = splitLine(line);
		if(lineSplit.length > 1)
			putValue(obj, lineSplit[0].trim().toLowerCase(), lineSplit[1].trim());
	}
}
