package bean.config;

import htsjdk.samtools.util.IOUtil;
import htsjdk.variant.vcf.VCFInfoHeaderLine;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import bean.config.anno.AbstractBedParser;
import bean.config.anno.FieldFormat;
import bean.config.readconfig.FormatReadConfig;
import bean.config.readconfig.VcfInfoReadConfig;
import postanno.FormatWithRef;

public class AnnoDBBean {
	public static final String INFO_EXT = ".vcfinfo";
	public static final String FORMAT_EXT = ".format";
	
	private String dbPath;
	private String label;
	private List<String> fields;
	private List<Integer> cols;
	private Map<String, String> out_names;
	private String vcfHeaderPath;
	private List<String> columnNames;
	private boolean isExclude;
	private boolean isAll;
	private FormatWithRef format;

	private Map<String, FieldFormat> fieldFormatMaps;
	private Map<String, VCFInfoHeaderLine> vcfInfoMaps;
	
	private String fieldsFormat;
	private String fieldVcfInfo;
	
	public AnnoDBBean(String label) {
		super();
		this.label = label;

		fields = new ArrayList<String>();
		cols = new ArrayList<Integer>();
		out_names = new HashMap<String, String>();
	}

	public void checkRequired() {
		if(label == null)  throw new IllegalArgumentException("You should define database label starts with @ first!");
		if(format == null) throw new IllegalArgumentException("Format is missing!");
		
		if(fields.size() == 0) {
			if(format.flag == 1) throw new IllegalArgumentException("fields of " + label + " is missing in annotation config file.");
			else if(cols.size() == 0) throw new IllegalArgumentException("fields or cols of " + label + " should be defined in annotation config file.");
		} else {
			if(fields.size() == 1 && fields.get(0).equalsIgnoreCase("all")) isAll = true;
		}
		System.out.println();
		if(format.flag != FormatWithRef.VCF_FORMAT) {
			if(fieldVcfInfo == null && new File(dbPath + INFO_EXT).exists()) fieldVcfInfo = dbPath + INFO_EXT;
			checkFindFile("VCF info", fieldVcfInfo);
			if(fieldVcfInfo != null) vcfInfoMaps = (new VcfInfoReadConfig<Map<String, VCFInfoHeaderLine>>()).read(new HashMap<String, VCFInfoHeaderLine>(), fieldVcfInfo);	
		}
		
		if(fieldsFormat == null && new File(dbPath + FORMAT_EXT).exists()) fieldsFormat = dbPath + FORMAT_EXT;
		checkFindFile("field format", fieldsFormat);
		if(fieldsFormat != null) fieldFormatMaps = (new FormatReadConfig<Map<String,FieldFormat>>()).read(new HashMap<String, FieldFormat>(), fieldsFormat);	
		System.out.println();
	}
	
	
	public String getLabel() {
		return label;
	}
	
	public void setFields(String fields) {
		this.fields = splitString(fields, true);
	}
	
	public void setOut_names(String out_names) {
		List<String> fields = splitString(out_names, false);
		int beg;
		for (String str : fields) {
			str = str.trim();
			beg = str.indexOf(":");
			if(beg == -1)  throw new IllegalArgumentException("out_names should have a format like [a:A, b:B, c:C] or [col1:A, col3:B, col4:C]. That means change field name a to A, or change column one's name to A.");
			this.out_names.put(str.substring(0, beg).trim(), str.substring(beg + 1).trim());
		}
	}
	
	public void resetOut_names(List<String> fields) {
		String colName;
		for (int i = 0; i < fields.size(); i++) {
			colName = AbstractBedParser.COL + i;
			if(out_names.get(colName) != null) {
				out_names.put(fields.get(i), out_names.get(colName));
			}
			
			if(fieldFormatMaps != null && fieldFormatMaps.get(colName) != null) {
				fieldFormatMaps.put(fields.get(i), fieldFormatMaps.get(colName));
			}
			
			if(vcfInfoMaps != null && vcfInfoMaps.get(colName) != null) {
				vcfInfoMaps.put(fields.get(i), vcfInfoMaps.get(colName));
			}
		}
	}
	
	public void setCols(String cols) {
		this.cols = splitInteger(cols, true);
	}

	public List<Integer> getCols() {
		return cols;
	}

	public Map<String, String> getOut_names() {
		return out_names;
	}
	
	public List<String> getFields() {
		return fields;
	}

	public boolean isExclude() {
		return isExclude;
	}

	public void setExclude(boolean isExclude) {
		this.isExclude = isExclude;
	}

	public boolean isAll() {
		return isAll;
	}

	public void setAll(boolean isAll) {
		this.isAll = isAll;
	}

	public String checkFormat(String val, final boolean flag) {
		val = val.trim();
		int i = 0;
		if(flag && val.charAt(i) == '-') {
			isExclude = true;
			i++;
		}
		if(val.charAt(i) != '[') throw new IllegalArgumentException("You should set field with format like XXX = [XX1, XX2, XX3] or XXX = -[XX1, XX2, XX3]");
		return val.substring(i+1, val.length() - 1);
	}
	
	public List<String> splitString(String val, final boolean flag) {
		val = checkFormat(val, flag);
		List<String> list = new ArrayList<String>();
		for (String str : val.split(",")) {
			list.add(str.trim());
		}
		return list; 
	}
	
	public List<Integer> splitInteger(String val, final boolean flag) {
		val = checkFormat(val, flag);
		
		List<Integer> list = new ArrayList<Integer>();
		for (String str : val.split(",")) {
			list.add(Integer.parseInt(str.trim()));
		}
		return list; 
	}

	public FormatWithRef getFormat() {
		return format;
	}

	public void setFormat(FormatWithRef format) {
		this.format = format;
	}

	public void setDbPath(String dbPath) {
		this.dbPath = dbPath;
	}

	public String getDbPath() {
		return dbPath;
	}

	public String getVcfHeaderPath() {
		if(vcfHeaderPath == null) {
			return dbPath;
		}
		return vcfHeaderPath;
	}

	public void setVcfHeaderPath(String vcfHeaderPath) {
		this.vcfHeaderPath = vcfHeaderPath;
	}

	public List<String> getColumnNames() {
		return columnNames;
	}

	public void setColumnNames(List<String> columnNames) {
		this.columnNames = columnNames;
	}

	public VCFInfoHeaderLine getInfoForField(final String field) {
		if(vcfInfoMaps == null) return null;
		return vcfInfoMaps.get(field);
	}

	public FieldFormat getFormatForField(final String field) {
		if(fieldFormatMaps == null) return null;
		return fieldFormatMaps.get(field);
	}
	
	public void checkFindFile(final String label, final String path) {
		if(path == null) {
			System.out.println("Don't find " + label + " file.");
		} else {
			System.out.println("Find " + label + " file (" + path + ").");
		}
	}

	public void setFieldsFormat(String fieldsFormat) {
		IOUtil.assertInputIsValid(fieldsFormat);
		this.fieldsFormat = fieldsFormat;
	}

	public void setFieldVcfInfo(String fieldVcfInfo) {
		IOUtil.assertInputIsValid(fieldVcfInfo);
		this.fieldVcfInfo = fieldVcfInfo;
	}
}
