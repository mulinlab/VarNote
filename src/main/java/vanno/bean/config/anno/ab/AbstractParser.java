package main.java.vanno.bean.config.anno.ab;

import htsjdk.variant.vcf.VCFHeader;
import htsjdk.variant.vcf.VCFInfoHeaderLine;
import main.java.vanno.bean.format.Format;
import main.java.vanno.constants.BasicUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;


public abstract class AbstractParser {

	public static final String COL = BasicUtils.COL;
	public static final String TAB = BasicUtils.TAB;
	public static final String COMMA = BasicUtils.COMMA;
	public static final String INFO_FIELD_SEPARATOR = BasicUtils.INFO_FIELD_SEPARATOR;
	public static final String VCF_INFO_EQUAL = BasicUtils.VCF_INFO_EQUAL;
	public static final String UNDERLINE = BasicUtils.UNDERLINE;
	public static final String NULLVALUE = BasicUtils.NULLVALUE;
	
	protected Format format;

	public AbstractParser() {
		
	}
	
//	public AbstractParser(final FormatWithRef format) {
//		super();
//		this.format = format;
//		checkRefAndAlt();
//		fieldsToCol = new HashMap<String, Integer>();
//		colToFields = new HashMap<Integer, String>();
//	}
	
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

	public Format getFormat() {
		return format;
	}
}
