package bean.config.readconfig;

import bean.config.run.OverlapRunBean;

public class OverlapReadConfig<T> extends CountReadConfig<T>{

	@Override
	public void putValue(T obj, String key, String val) {
		super.putValue(obj, key, val);
		
		OverlapRunBean bean = (OverlapRunBean)obj;
		switch (key) {
			case LOJ:
				bean.setLoj(val);
				break;
			case OUTPUT_MODE:
				bean.setOutMode(Integer.parseInt(val));
				break;
			case OUTPUT_FOLDER:
				bean.setOutputFolder(val);
				break;
			case MODE:
				bean.setMode(Integer.parseInt(val));
				break;
			case DB_INDEX_TYPE:
				bean.setDBIndex(val);
				break;	
			case DB_LABEL:
				bean.setDBLabel(val);
				break;
			default:
				break;
		}
	}

	@Override
	public void doEnd(T obj) {
		super.doEnd(obj);
		((OverlapRunBean)obj).checkOutFolder();
	}
}
