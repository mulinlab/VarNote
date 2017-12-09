package bean.config.readconfig;

import postanno.FormatWithRef;
import bean.config.run.OverlapHeaderBean;

public class OverlapHeaderReadConfig<T> extends AbstractReadConfig<T>{

	@Override
	public boolean filterLine(final T obj, String line) {
		return line.startsWith(OVERLAP_NOTE);
	}

	@Override
	public boolean isBreak(String line) {
		return line.equalsIgnoreCase(OVERLAP_NOTE + END);
	}

	@Override
	public String[] splitLine(String line) {
		line = line.substring(1);
		if(line.indexOf(OVERLAP_EQUAL) != -1) {
			return line.split(OVERLAP_EQUAL);
		} else {
			throw new IllegalArgumentException("Cannot parse line : \"" + line +"\", properties should have a format like key=value.");
		}
	}

	@Override
	public void putValue(T obj, String key, String val) {
		super.putValue(obj, key, val);
		
		OverlapHeaderBean bean = (OverlapHeaderBean)obj;
		switch (key) {
			case QUERY_FORMAT:
				bean.setQueryFormat(new FormatWithRef(val));
				break;
			case DB_FORMAT:
				bean.setDBIndex(val);
				break;
			case DB_LABEL:
				bean.setDBLabel(val);
				break;
			case DB_PATH:
				bean.newDB(val);
				break;
			default:
				break;
		}
	}

	@Override
	public void doEnd(T obj) {
		super.doEnd(obj);
	}
}
