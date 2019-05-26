package main.java.vanno.bean.config.config;

import java.util.HashMap;
import java.util.Map;

import main.java.vanno.bean.config.anno.FieldFormat;
import main.java.vanno.constants.InvalidArgumentException;


public final class FormatReadConfig<T> extends AbstractReadConfig<T>{

	public static final String OUT_FORMAT = "out_format";
	public static final String FORMAT = "format";
	public static final String BEGIN = "@";
	public static final String COMMENT = AbstractReadConfig.COMMENT_LINE;
	
	private String currentField;
	private String format = "";
	private String outFormat = "";
	private Map<String, FieldFormat> formatMap;
	
	public void init() {
		formatMap = new HashMap<String, FieldFormat>();
	}
	
	@Override
	public boolean filterLine(String line) {
		if(line.startsWith(BEGIN)) {
			if(currentField != null) {
				if(format.trim().equals("") || outFormat.trim().equals("")) throw new InvalidArgumentException("Both format and out_format is required for " + currentField);
				formatMap.put(currentField, new FieldFormat(format, outFormat, currentField));
			}
			currentField = line.substring(1);
			if(currentField.trim().equals("")) throw new InvalidArgumentException("Field name is empty, please check filed name starts with @");
			format = "";
			outFormat = "";
			
			return false;
		} else if(line.equals("") || line.startsWith(COMMENT_LINE)) {
			return false;
		} else {
			return true;
		}
	}

	@Override
	public void putValue(String key, String val) {
		if(currentField == null) throw new InvalidArgumentException("Please define field name begin with @ first.");
		switch (key) {
			case FORMAT:
				format = val;
				break;
			case OUT_FORMAT:
				outFormat = val;
				break;
			default:
				break;
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public T doEnd() {
		if(currentField != null) {
			formatMap.put(currentField, new FieldFormat(format, outFormat, currentField));
		}
		return (T)formatMap;
	}
	
	public static void main(String[] args) {
	}
}
