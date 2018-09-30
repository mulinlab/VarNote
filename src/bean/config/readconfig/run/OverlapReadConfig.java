package bean.config.readconfig.run;

import bean.config.readconfig.AbstractReadConfig;
import bean.config.run.DBRunConfigBean;
import bean.config.run.OverlapRunBean;

public final class OverlapReadConfig<T> extends AbstractReadConfig<T>{

	private DBRunConfigBean cdb;
	
	@Override
	public boolean filterLine(final T obj, String line) {
		if(line.equalsIgnoreCase(DB_BEGIN)) {
			OverlapRunBean bean = (OverlapRunBean)obj;
			if(cdb != null) {
				bean.addTempDB(cdb);
			}
			cdb = new DBRunConfigBean();
			return false;
		} else if(!line.startsWith(COMMENT_LINE) && !line.equals("") && !line.startsWith("[")) {
			return true;
		} else {
			return false;
		}
	}
	
	public void checkDB() {
		if(cdb == null)  throw new IllegalArgumentException("Database object is null, you should define " + DB_BEGIN + " first.");
	}
	
	@Override
	public void putValue(T obj, String key, String val) {
		super.putValue(obj, key, val);
		
		OverlapRunBean bean = (OverlapRunBean)obj;
		switch (key) {
			case QUERY_FORMAT:
				bean.setQueryFormat(val);
				break;
			case QUERY_CHROM:
				bean.setSeqCol(Integer.parseInt(val));
				break;
			case QUERY_BEGIN:
				bean.setBegCol(Integer.parseInt(val));
				break;
			case QUERY_END:
				bean.setEndCol(Integer.parseInt(val));
				break;
			case QUERY_REF:
				bean.setRefCol(Integer.parseInt(val));
				break;
			case QUERY_ALT:
				bean.setAltCol(Integer.parseInt(val));
				break;
			case THREAD:
				bean.setThread(Integer.parseInt(val));
				break;
			case COUNT:
				bean.setCount(val);
				break;
			case MODE:
				bean.setMode(Integer.parseInt(val));
				break;
			case EXACT:
				bean.setExact(val);
				break;
			case FC:
				bean.setFc(val);
				break;
			case LOJ:
				bean.setLoj(val);
				break;
			case OUTPUT_MODE:
				bean.setOutMode(Integer.parseInt(val));
				break;
			case OUTPUT_FOLDER:
				bean.setOutputFolder(val);
				break;
			case DB_PATH:
				checkDB();
				cdb.setDb(val);
				break;
			case DB_INDEX_TYPE:
				checkDB();
				cdb.setIndexFormat(val);
				break;	
			case DB_LABEL:
				checkDB();
				cdb.setLable(val);
				break;
			default:
				break;
		}
	}

	@Override
	public void doEnd(T obj) {
		super.doEnd(obj);

	    ((OverlapRunBean)obj).checkTabFormat();
		if(cdb != null) ((OverlapRunBean)obj).addTempDB(cdb);
	}
}
