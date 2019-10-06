package org.mulinlab.varnote.cmdline.txtreader.anno;

import java.util.ArrayList;
import java.util.List;

import org.mulinlab.varnote.cmdline.txtreader.abs.AbstractQueryReader;
import org.mulinlab.varnote.cmdline.txtreader.abs.QueryReader;
import org.mulinlab.varnote.config.anno.databse.DatabseAnnoConfig;
import org.mulinlab.varnote.exceptions.InvalidArgumentException;

public final class AnnoConfigReader<T> extends AbstractQueryReader<T> {

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
			if(line.substring(1).equals("")) throw new InvalidArgumentException("Database name should be defined first, please check whether there are databases start with a '@'");
			db = new DatabseAnnoConfig(line.substring(1));
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
				db.setOut_names(val);
				break;
			case HEADER_PATH:
				db.setHeaderPath(val);
				break;
			case HEADER:
				db.setHasHeader(val);
				break;
			case QueryReader.COMMNT_INDICATOR:
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
