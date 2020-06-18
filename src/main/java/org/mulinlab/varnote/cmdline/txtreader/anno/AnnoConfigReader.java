package org.mulinlab.varnote.cmdline.txtreader.anno;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.mulinlab.varnote.cmdline.txtreader.abs.AbstractQueryReader;
import org.mulinlab.varnote.config.param.postDB.DBAnnoParam;
import org.mulinlab.varnote.exceptions.InvalidArgumentException;
import org.mulinlab.varnote.utils.VannoUtils;

public final class AnnoConfigReader<T> extends AbstractQueryReader<T> {

	public static final String FIELDS = "fields";
	public static final String INFO_FIELDS = "info_fields";
	public static final String COLUMNS = "cols";
	public static final String OUT_NAMES = "out_names";
	public static final String HAS_HEADER = "has_header";
	public static final String HEADER_PATH = "header_path";
	public static final String COMMNT_INDICATOR = "comment_indicator";
	public static final String VCFINFO = "vcf_info_path";

//	public static final String FORMAT_PATH = "format_path";
	
	private DBAnnoParam db;
	private List<DBAnnoParam> dbList;
	
	
	@Override
	public void doInit() {
		super.doInit();
		dbList = new ArrayList<DBAnnoParam>();
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public T doEnd() {
		if(db != null) {
			dbList.add(db);
		}
		Map<String, DBAnnoParam> map = new HashMap<>();
		for (DBAnnoParam dbAnnoParam: dbList) {
			map.put(dbAnnoParam.getLabel(), dbAnnoParam);
		}
		return (T)map;
	}
	
	@Override
	public boolean filterLine(String line) {
		if(line.startsWith(OVERLAP_NOTE)) {
			if(db != null) {
				dbList.add(db);
			}
			if(line.substring(1).equals("")) throw new InvalidArgumentException("Database name should be defined first, please check whether there are databases start with a '@'");
			db = new DBAnnoParam(line.substring(1));
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
		if(db == null) throw new InvalidArgumentException("Please set up database name starts with @ first!");
		switch (key) {
			case FIELDS:
				db.setFields(val);
				break;
			case COLUMNS:
				db.setCols(val);
				break;
			case OUT_NAMES:
				db.setOutNames(val);
				break;
			case HEADER_PATH:
				db.setHeaderPath(val);
				break;
			case HAS_HEADER:
				db.setHasHeader(VannoUtils.strToBool(val));
				break;
			case COMMNT_INDICATOR:
				db.setCommentIndicator(val);
				break;
			case VCFINFO:
				db.setVcfInfoPath(val);
				break;
//			case FORMAT_PATH:
//				db.setFieldsFormat(val);
//				break;
			case INFO_FIELDS:
				db.setInfofields(val);
				break;
			default:
				break;
		}
	}
}
