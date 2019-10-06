package org.mulinlab.varnote.cmdline.txtreader.anno;

import java.util.HashMap;
import java.util.Map;

import org.mulinlab.varnote.config.anno.FieldFormat;
import org.mulinlab.varnote.cmdline.txtreader.abs.AbstractQueryReader;
import org.mulinlab.varnote.exceptions.InvalidArgumentException;


public final class FormatReader<T> extends AbstractQueryReader<T> {

	public static final String OUT_FORMAT = "out_format";
	public static final String FORMAT = "format";
	public static final String BEGIN = "@";
	public static final String COMMENT = AbstractQueryReader.COMMENT_LINE;
	
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
			
			return true;
		} else if(line.equals("") || line.startsWith(COMMENT_LINE)) {
			return true;
		} else {
			return false;
		}
	}

	@Override
	public void putValue(String key, String val) {
		super.putValue(key, val);
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
