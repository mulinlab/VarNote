package org.mulinlab.varnote.config.anno.databse;

import htsjdk.samtools.util.IOUtil;
import htsjdk.variant.vcf.VCFInfoHeaderLine;
import org.mulinlab.varnote.config.anno.FieldFormat;
import org.mulinlab.varnote.config.anno.ab.AbstractParser;
import org.mulinlab.varnote.cmdline.txtreader.anno.FormatReader;
import org.mulinlab.varnote.cmdline.txtreader.anno.VcfInfoReader;
import org.mulinlab.varnote.utils.headerparser.BEDHeaderParser;
import org.mulinlab.varnote.utils.format.Format;
import org.mulinlab.varnote.utils.format.Format.H_FIELD;
import org.mulinlab.varnote.exceptions.InvalidArgumentException;
import org.mulinlab.varnote.utils.VannoUtils;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public final class DatabseAnnoConfig {
	public static final String INFO_EXT = ".vcfinfo";
	public static final String FORMAT_EXT = ".format";
	
//	private String dbPath;
	private String label;
	
	private List<String> fields;
	private List<String> fieldsUppercase;
	private List<Integer> cols;
	private Map<String, String> out_names;
	
//	private List<String> columnNames;
	private String headerPath;
	private boolean hasHeader;
	private String commentIndicator;
	
	private boolean isAll;
	private boolean isExcludeFields;
	private boolean isExcludeInfoFields;
	
	private List<String> infoToExtract;

	private Map<String, FieldFormat> fieldFormatMaps;
	private Map<String, VCFInfoHeaderLine> vcfInfoMaps;
	
	private String fieldsFormatPath;
	private String fieldVcfInfoPath;
	
	public DatabseAnnoConfig(final String label) {
		super();
		this.label = label;

		fields = new ArrayList<String>();
		fieldsUppercase = new ArrayList<>();
		
		infoToExtract = new ArrayList<String>();
		cols = new ArrayList<Integer>();
		out_names = new HashMap<String, String>();
		
		isExcludeFields = false;
		isExcludeInfoFields = false;
		
		fields.add("all");
		hasHeader = false;
	}
	
	public void checkRequired() {
		if(label == null)  throw new InvalidArgumentException("Database label should be defined starts with @");

		if(fields.size() == 0) {
			if(cols.size() == 0) throw new InvalidArgumentException("fields or cols of " + label + " should be defined in annotation config file.");
		} else if(fields.size() == 1 && fields.get(0).equalsIgnoreCase("all")) { 
			fields.remove(0);
			isAll = true;
		}
	}
	
	public void setDBPath(final String dbPath, final boolean isVCF, final boolean isOutVCF) {

		if(!isVCF && isOutVCF) {
			if(fieldVcfInfoPath == null && new File(dbPath + INFO_EXT).exists()) fieldVcfInfoPath = dbPath + INFO_EXT;
			if(fieldVcfInfoPath != null) {
				vcfInfoMaps = (new VcfInfoReader<Map<String, VCFInfoHeaderLine>>()).read(fieldVcfInfoPath);
				for (String key : vcfInfoMaps.keySet()) {
					vcfInfoMaps.put(key.toUpperCase(), vcfInfoMaps.get(key));
				}
			}
		}
		
		if(fieldsFormatPath == null && new File(dbPath + FORMAT_EXT).exists()) fieldsFormatPath = dbPath + FORMAT_EXT;
		if(fieldsFormatPath != null) {
			fieldFormatMaps = (new FormatReader<Map<String,FieldFormat>>()).read(fieldsFormatPath);
			for (String key : fieldFormatMaps.keySet()) {
				fieldFormatMaps.put(key.toUpperCase(), fieldFormatMaps.get(key));
			}
		}

	}
	
	public String getLabel() {
		return label;
	}
	
	public List<String> getInfoToExtract() {
		return infoToExtract;
	}

	public void setInfoToExtract(String infoToExtract) {
		if(infoToExtract.startsWith("-")) isExcludeInfoFields = true;
		infoToExtract = checkFormat(infoToExtract, true);
		this.infoToExtract = BEDHeaderParser.splitString(infoToExtract);
	}

	public void setFields(String fields) {
		if(fields.startsWith("-")) isExcludeFields = true;
		fields = checkFormat(fields, true);
		this.fields = BEDHeaderParser.splitString(fields);

		
		int hasPosAndBegin = 0;
		
		String field;
		for (int i = 0; i < this.fields.size(); i++) {
			field = this.fields.get(i).toUpperCase();
			
			if(field.equals("CHR")) {
				fieldsUppercase.add(H_FIELD.CHROM.toString());
			} else if(field.equals(Format.HEADER_POS)) {
				
				hasPosAndBegin ++;
				fieldsUppercase.add(H_FIELD.BEGIN.toString());
				fieldsUppercase.add(H_FIELD.END.toString());
			} else if(field.equals( H_FIELD.BEGIN.toString() )) {
				hasPosAndBegin++;
				fieldsUppercase.add(H_FIELD.BEGIN.toString());
			} else {
				fieldsUppercase.add(field);
			}
		}
		
		if(hasPosAndBegin >= 2) {
			throw new InvalidArgumentException("Fields to extract shouldn't have POS and BEGIN both!");
		}
	}
	
	public void setCols(String cols) {
		if(cols.startsWith("-")) isExcludeFields = true;
		cols = checkFormat(cols, true);
		this.cols = BEDHeaderParser.splitInteger(cols);
	}
	
	public void setOut_names(String out_names) {
		out_names = checkFormat(out_names, false);
		List<String> fields = BEDHeaderParser.splitString(out_names);
		int beg;
		String key, out;
		for (String str : fields) {
			beg = str.indexOf(":");
			if(beg == -1)  throw new InvalidArgumentException("out_names should have a format like [a:A, b:B, c:C] or [col1:A, col3:B, col4:C]. That means change field name a to A, or change column one's name to A.");
			key = str.substring(0, beg).trim();
			out = str.substring(beg + 1).trim();
			this.out_names.put(key, out);
			this.out_names.put(key.toUpperCase(), out);
			
			if(key.toUpperCase().equals("CHR")) {
				this.out_names.put(H_FIELD.CHROM.toString(), out);
			} else if(key.equals(Format.HEADER_POS) && !fieldsUppercase.contains(H_FIELD.BEGIN.toString())) {
				this.out_names.put(H_FIELD.BEGIN.toString(), out);
			}
		}
	}
	
	public void resetOut_names(List<String> convertField) {
		String colName;

		for (int i = 0; i < convertField.size(); i++) {
			colName = AbstractParser.COL + (i+1);
			
			if(out_names.get(colName) != null) {
				out_names.put(convertField.get(i), out_names.get(colName));
			}
			
			if(fieldFormatMaps != null && fieldFormatMaps.get(colName) != null) {
				fieldFormatMaps.put(convertField.get(i), fieldFormatMaps.get(colName));
			}
			
			if(vcfInfoMaps != null && vcfInfoMaps.get(colName) != null) {
				vcfInfoMaps.put(convertField.get(i), vcfInfoMaps.get(colName));
			}
		}
	}

	public List<Integer> getCols() {
		return cols;
	}

	public Map<String, String> getOut_names() {
		return out_names;
	}
	
//	public List<String> getFields() {
//		return fields;
//	}

	public String checkFormat(String val, final boolean flag) {
		val = val.trim();
		int i = 0;
		if(flag && val.charAt(i) == '-') {
			i++;
		}
		if(val.charAt(i) != '[') throw new InvalidArgumentException("You should set field with format like XXX = [XX1, XX2, XX3] or XXX = -[XX1, XX2, XX3]");
		return val.substring(i+1, val.length() - 1);
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
			System.out.println("Don't find " + label + " file for " + this.label + ".");
		} else {
			System.out.println("Find " + label + " file (" + path + ").");
		}
	}

	public void setFieldsFormat(String fieldsFormatPath) {
		IOUtil.assertInputIsValid(fieldsFormatPath);
		this.fieldsFormatPath = fieldsFormatPath;
	}

	public void setFieldVcfInfo(String fieldVcfInfoPath) {
		IOUtil.assertInputIsValid(fieldVcfInfoPath);
		this.fieldVcfInfoPath = fieldVcfInfoPath;
	}

	public boolean isExcludeFields() {
		return isExcludeFields;
	}

	public boolean isExcludeInfoFields() {
		return isExcludeInfoFields;
	}
	
	public void setHeaderPath(final String headerPath) {	
		IOUtil.assertInputIsValid(headerPath);
		this.headerPath = headerPath;
		this.hasHeader = true;
	}
	
	public String getHeaderPath() {
		return headerPath;
	}
	
	public boolean isAll() {
		return isAll;
	}

	public void setAll(final boolean isAll) {
		this.isAll = isAll;
	}

	public List<String> getFieldsUppercase() {
		return fieldsUppercase;
	}

	public boolean isHasHeader() {
		return hasHeader;
	}

	public void setHasHeader(final boolean hasHeader) {
		this.hasHeader = hasHeader;
	}
	
	public void setHasHeader(final String hasHeader) {
		this.hasHeader =  VannoUtils.strToBool(hasHeader);
	}

	public String getCommentIndicator() {
		return commentIndicator;
	}

	public void setCommentIndicator(String commentIndicator) {
		this.commentIndicator = commentIndicator;
	}
}
