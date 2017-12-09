package bean.config.readconfig;

import java.util.List;
import constants.VannoUtils;
import bean.config.AnnoDBBean;

public class AnnoConfigReadConfig<T> extends AbstractReadConfig<T>{

	public static final String FIELDS = "fields";
	public static final String COLUMNS = "cols";
	public static final String OUT_NAMES = "out_names";
	public static final String VCF_HEADER_PATH = "vcf_header_path";
	public static final String COLUMN_NAMES = "column_names";
	public static final String VCFINFO = "vcfinfo_path";
	public static final String FORMAT_PATH = "format_path";
	
	private AnnoDBBean db;
	
	@SuppressWarnings("unchecked")
	@Override
	public boolean filterLine(final T obj, String line) {
		if(line.startsWith(OVERLAP_NOTE)) {
			if(db != null) {
				List<AnnoDBBean> dbList = (List<AnnoDBBean>)obj;
				dbList.add(db);
			}
			if(line.substring(1).equals("")) throw new IllegalArgumentException("Database name is empty, please check database name starts with @");
			db = new AnnoDBBean(line.substring(1));
			return false;
		} else if(line.equals("") || line.startsWith(COMMENT_LINE)) {
			return false;
		} else {
			return true;
		}
	}

	@Override
	public void putValue(T obj, String key, String val) {
		if(db == null) throw new IllegalArgumentException("Please set up database name starts with @ first!");
		switch (key) {
			case FIELDS:
				db.setFields(val);
				break;
			case COLUMNS:
				db.setCols(val);
				break;
			case OUT_NAMES:
				db.setOut_names(val);
				break;
			case VCF_HEADER_PATH:
				db.setVcfHeaderPath(val);
				break;
			case COLUMN_NAMES:
				db.setColumnNames(VannoUtils.split(val, COLUMN_NAMES, VannoUtils.COMMA_SPILT));
				break;
			case VCFINFO:
				db.setFieldVcfInfo(val);
				break;
			case FORMAT_PATH:
				db.setFieldsFormat(val);
				break;
			default:
				break;
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public void doEnd(T obj) {
		if(db != null) {
			List<AnnoDBBean> dbList = (List<AnnoDBBean>)obj;
			dbList.add(db);
		}
	}
}
