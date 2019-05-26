package main.java.vanno.bean.config.anno.databse;

import java.util.ArrayList;
import java.util.List;

import main.java.vanno.bean.config.config.AbstractReadConfig;
import main.java.vanno.constants.InvalidArgumentException;

public final class DatabaseAnnoRead<T> extends AbstractReadConfig<T>{

	public static final String FIELDS = "fields";
	public static final String INFO_FIELDS = "info_fields";
	public static final String COLUMNS = "cols";
	public static final String OUT_NAMES = "out_names";
	public static final String HEADER = "header";
	public static final String HEADER_PATH = "header_path";
	public static final String VCFINFO = "vcf_info_path";
	public static final String FORMAT_PATH = "format_path";
	
	private DatabseAnnoConfig db;
	private List<DatabseAnnoConfig> dbList;
	
	
	@Override
	public void doInit() {
		super.doInit();
		dbList = new ArrayList<DatabseAnnoConfig>();
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public T doEnd() {
		if(db != null) {
			dbList.add(db);
		}
		return (T)dbList;
	}
	
	@Override
	public boolean filterLine(String line) {
		if(line.startsWith(OVERLAP_NOTE)) {
			if(db != null) {
				dbList.add(db);
			}
			if(line.substring(1).equals("")) throw new InvalidArgumentException("Database name is empty, please check database name starts with @");
			db = new DatabseAnnoConfig(line.substring(1));
			return false;
		} else if(line.equals("") || line.startsWith(COMMENT_LINE)) {
			return false;
		} else {
			return true;
		}
	}

	@Override
	public void putValue(String key, String val) {
		if(db == null) throw new InvalidArgumentException("Please set up database name starts with @ first!");
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
			case HEADER_PATH:
				db.setHeaderPath(val);
				break;
			case HEADER:
				db.setHasHeader(val);
				break;
			case COMMNT_INDICATOR:
				db.setCommentIndicator(val);
				break;
			case VCFINFO:
				db.setFieldVcfInfo(val);
				break;
			case FORMAT_PATH:
				db.setFieldsFormat(val);
				break;
			case INFO_FIELDS:
				db.setInfoToExtract(val);
				break;
			default:
				break;
		}
	}
}
