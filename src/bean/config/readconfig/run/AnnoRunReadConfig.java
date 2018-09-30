package bean.config.readconfig.run;

import bean.config.readconfig.AbstractReadConfig;
import bean.config.run.AnnoRunBean;

public final class AnnoRunReadConfig<T> extends AbstractReadConfig<T>{

	public static final String OVERLAP_FILE = "overlap_file";
	public static final String ANNO_CONFIG = "anno_config";
	public static final String HEADER_PATH = "header_path";
	public static final String FORCE_OVERLAP = "force_overlap";
	public static final String ANNO_FORMAT = "anno_format";
	public static final String ANNO_OUT = "anno_out";
	
	
	@Override
	public boolean filterLine(final T obj, String line) {
		if(!line.startsWith(COMMENT_LINE) && !line.equals("") && !line.startsWith("[")) {
			return true;
		} else {
			return false;
		}
	}
	
	@Override
	public void putValue(T obj, String key, String val) {
		
		AnnoRunBean bean = (AnnoRunBean)obj;
		switch (key) {
			case OVERLAP_FILE:
				bean.setOverlapFile(val);
				bean.setOverlapFile(true);
				break;
			case FORCE_OVERLAP:
				bean.setForceOverlap(val);
				break;
			case ANNO_FORMAT:
				bean.setAnnoOutFormat(val);
				break;
			case ANNO_OUT:
				bean.setOutputFile(val);
				break;
			case HEADER_PATH:
				bean.setHeaderPath(val);
				break;	
			case ANNO_CONFIG:
				bean.setAnnoConfig(val);
				break;
			default:
				break;
		}
	}

	@Override
	public void doEnd(T obj) {
	}
}
