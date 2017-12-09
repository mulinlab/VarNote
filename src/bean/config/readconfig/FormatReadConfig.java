package bean.config.readconfig;

import java.util.Map;
import bean.config.anno.FieldFormat;

public class FormatReadConfig<T> extends AbstractReadConfig<T>{

	public static final String OUT_FORMAT = "out_format";
	public static final String FORMAT = "format";
	public static final String BEGIN = "@";
	public static final String COMMENT = AbstractReadConfig.COMMENT_LINE;
	
	private String currentField;
	private String format = "";
	private String outFormat = "";
	
	@SuppressWarnings("unchecked")
	@Override
	public boolean filterLine(final T obj, String line) {
		
		if(line.startsWith(BEGIN)) {
			if(currentField != null) {
				if(format.trim().equals("") || outFormat.trim().equals("")) throw new IllegalArgumentException("Both format and out_format is required for " + currentField);
				Map<String, FieldFormat> formatMap = (Map<String, FieldFormat>)obj;
				formatMap.put(currentField, new FieldFormat(format, outFormat, currentField));
			}
			currentField = line.substring(1);
			if(currentField.trim().equals("")) throw new IllegalArgumentException("Field name is empty, please check filed name starts with @");
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
	public void putValue(T obj, String key, String val) {
		if(currentField == null) throw new IllegalArgumentException("Please define field name begin with @ first.");
	
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
	public void doEnd(T obj) {
		if(currentField != null) {
			Map<String, FieldFormat> formatMap = (Map<String, FieldFormat>)obj;
			formatMap.put(currentField, new FieldFormat(format, outFormat, currentField));
		}
	}
	
	public static void main(String[] args) {
	}
}
