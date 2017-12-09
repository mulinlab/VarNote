package bean.config.anno;


import htsjdk.samtools.util.StringUtil;
import htsjdk.variant.vcf.VCFHeader;
import htsjdk.variant.vcf.VCFInfoHeaderLine;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import constants.VannoUtils;
import postanno.FormatWithRef;

public abstract class AbstractParser {

	public static final String COL = "col";
	public static final String TAB = VannoUtils.TAB_SPILT;
	public static final String COMMA = VannoUtils.COMMA_SPILT;
	public static final String INFO_FIELD_SEPARATOR = ";";
	public static final String EQUAL_SEPERATOR = "=";
	
	protected FormatWithRef format;
	protected boolean isRefAndAltExsit;
	
	public AbstractParser(final FormatWithRef format) {
		super();
		this.format = format;
	}
	
	public void checkRefAndAlt(final List<String> fields) {
		System.out.println("Column Name is: " + StringUtil.join(COMMA, fields));
		if ((format.refCol != -1) && (format.altCol != -1)) {
			isRefAndAltExsit = true;
		} else {
			isRefAndAltExsit = false;
			System.out.println("*** Note: REF or ALT column is not find, we will do the annotation without REF and ALT. \nYou have two ways to define it. 1. define column_names in config file or -columns option in command line. 2. set column name starts with # in data file (should be the first line). \n");
		}
	}
	
	public Map<String, String> allInfoToMap(final String info) {
		Map<String, String> fieldToValMap = new HashMap<String, String>();
		final String[] fields = info.split(INFO_FIELD_SEPARATOR);
		int beg;
		for (String str : fields) {
			str = str.trim();
			beg = str.indexOf(EQUAL_SEPERATOR);
			if(beg != -1) {
				fieldToValMap.put(str.substring(0, beg), str.substring(beg + 1));
			} else {
				fieldToValMap.put(str, "true");
			}
		}
		return fieldToValMap;
	}

	public List<String> oneMapToList(final Map<String, String> fieldToValMap, final List<String> keys) {
		List<String> out = new ArrayList<String>();
		
		for (String infoKey : keys) {
			if(fieldToValMap.get(infoKey) == null) {
				out.add(".");
			} else {
				if(fieldToValMap.get(infoKey).equals("true")) {
					out.add(infoKey);
				} else {
					out.add(fieldToValMap.get(infoKey));
				}
			}
		}
		return out;
	}
	
	public List<String> multiMapToList(final Map<String, FieldVal> fieldToValsMap, final List<String> keys, final Map<String, String> outNames) {
		List<String> out = new ArrayList<String>();

		FieldVal fieldVal;
		String val;
		for (String key : keys) {
			fieldVal = fieldToValsMap.get(key);
			val = fieldVal.mapTostr();
			if(val.equals("")) {
				if(outNames == null) out.add(".");
			} else {
				if(fieldVal.isFlag()) {
					out.add(key);
				} else {
					if(outNames == null) {
						out.add(val);
					} else {
						if(outNames.get(key) != null) {
							out.add(outNames.get(key) + EQUAL_SEPERATOR + val);
						} else {
							out.add(key + EQUAL_SEPERATOR + val);
						}
					}
				}
			}
		}
		return out;
	}
	
//	public List<String> multiMapToList(final Map<String, List<String>> fieldToValsMap, final List<String> keys, final Map<String, String> outNames) {
//		List<String> out = new ArrayList<String>();
//		List<String> vals;
//		for (String key : keys) {
//			vals = fieldToValsMap.get(key);
//
//			if((vals == null) || (vals.size() == 0)) {
//				if(outNames == null) out.add(".");
//			} else {
//				if(vals.get(0).equals("true")) {
//					out.add(key);
//				} else {
//					if(outNames == null) {
//						out.add(StringUtil.join(COMMA, vals));
//					} else {
//						if(outNames.get(key) != null) {
//							out.add(outNames.get(key) + EQUAL_SEPERATOR + StringUtil.join(COMMA, vals));
//						} else {
//							out.add(key + EQUAL_SEPERATOR + StringUtil.join(COMMA, vals));
//						}
//					}
//				}
//			}
//		}
//		return out;
//	}

	public List<String> changeFiledName(final List<String> fields, final Map<String, String> outName) {
		if(outName == null) return fields;
		List<String> newFields = new ArrayList<String>(fields.size());
		for (String str : fields) {
			if(outName.get(str) == null) {
				newFields.add(str);
			} else {
				newFields.add(outName.get(str));
			}
		}
		return newFields;
	}
	
	public abstract void emptyNodes(final String[] query_alts);


	public FormatWithRef getFormat() {
		return format;
	}


	public boolean isRefAndAltExsit() {
		return isRefAndAltExsit;
	}
	
	public VCFHeader addDBInfos(final VCFHeader vcfHeader, final List<VCFInfoHeaderLine> dbInfos) {
		for (VCFInfoHeaderLine dbInfo : dbInfos) {
			vcfHeader.addMetaDataLine(dbInfo);
		}
		return vcfHeader;
	}
	
	public List<VCFInfoHeaderLine> getVCFInfos(final List<String> fields, final Map<String, String> outNames, Map<String, VCFInfoHeaderLine> infoMap) {
		VCFInfoHeaderLine info;
		String outName;
		List<VCFInfoHeaderLine> dbInfos = new ArrayList<VCFInfoHeaderLine>();
		for (String field : fields) {
			info = infoMap.get(field);
			outName = outNames.get(field);
			
			if(outName != null)  info = new VCFInfoHeaderLine(outName, info.getCountType(), info.getType(), info.getDescription());
			dbInfos.add(info);
		}
		return dbInfos;
	}
	
}
