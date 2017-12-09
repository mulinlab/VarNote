package bean.config.readconfig;

import bean.config.run.CountRunBean;

public class CountReadConfig<T> extends AbstractReadConfig<T>{

	@Override
	public boolean filterLine(final T obj, String line) {
		if(line.equalsIgnoreCase(DB_BEGIN)) {
			((CountRunBean)obj).newDB();
			return false;
		} else if(!line.startsWith(COMMENT_LINE) && !line.equals("") && !line.startsWith("[")) {
			return true;
		} else {
			return false;
		}
	}

	@Override
	public void putValue(T obj, String key, String val) {
		super.putValue(obj, key, val);
		
		CountRunBean bean = (CountRunBean)obj;
		switch (key) {
			case QUERY_FORMAT:
				bean.setQueryFormat(val);
				break;
			case THREAD:
				bean.setThread(Integer.parseInt(val));
				break;
			case EXACT:
				bean.setExact(val);
				break;
			case FC:
				bean.setFc(val);
				break;
			case DB_PATH:
				bean.setDBPath(val);
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
