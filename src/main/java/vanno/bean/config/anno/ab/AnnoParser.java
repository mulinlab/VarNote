package main.java.vanno.bean.config.anno.ab;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import htsjdk.samtools.util.StringUtil;
import htsjdk.variant.vcf.VCFHeader;
import htsjdk.variant.vcf.VCFHeaderLine;
import main.java.vanno.bean.config.run.AnnoRunConfig.AnnoOutFormat;
import main.java.vanno.bean.node.RefNode;
import main.java.vanno.constants.BasicUtils;

public class AnnoParser {
	public static final String NULLVALUE = BasicUtils.NULLVALUE;
	
	private final AbstractQueryParser queryParser;
	private final Map<String, AbstractDatababseParser> dbPareser;
	private final List<String> databaseNames;
	private final AnnoOutFormat annoOutFormat;
	private final boolean loj;
	
	public AnnoParser(final AbstractQueryParser queryParser, final Map<String, AbstractDatababseParser> dbPareser, final List<String> databaseNames,
			final AnnoOutFormat annoOutFormat, final boolean loj) {
		super();
		this.queryParser = queryParser;
		this.dbPareser = dbPareser;
		this.databaseNames = databaseNames;
		this.annoOutFormat = annoOutFormat;
		this.loj = loj;
	}
	
	
	public List<String> extractResult(final List<ExtractValue> allExtract, final boolean hasValue, final int totalCol) {
		List<String> extractFields = new ArrayList<String>();
		if(!hasValue) {
			if(loj && annoOutFormat == AnnoOutFormat.BED) {
				for (int i = 0; i < totalCol; i++) {
					extractFields.add(NULLVALUE);
				}
			}
		} else {
			for (ExtractValue e : allExtract) {
				if(e.isHasValue()) {
					for (String s : e.getVal()) {
						extractFields.add(s);
					}
				} else if(annoOutFormat == AnnoOutFormat.BED)  {
					for (int i = 0; i < e.getClo(); i++) {
						extractFields.add(NULLVALUE);
					}
				}
			}
		}
		return extractFields;
	}
	
	public String doQuery(final RefNode query, final Map<String, List<String>> dbNodeMap) {

		List<ExtractValue> allExtract = new ArrayList<ExtractValue>();
		AbstractDatababseParser parser;
		ExtractValue dbExtract;
		boolean hasValue = false;
		int totalCol = 0;
		
		for (String dbName : databaseNames) {
			parser = dbPareser.get(dbName);
			
			if(parser != null) {
				dbExtract = new ExtractValue(false, null, parser.getExtractCol(), true);
				if(annoOutFormat == AnnoOutFormat.BED) {
					if(dbNodeMap != null && dbNodeMap.get(dbName) != null) dbExtract = parser.extractFieldsValueToBED(query, dbNodeMap.get(dbName));
				} else {
					if(dbNodeMap != null && dbNodeMap.get(dbName) != null) dbExtract = parser.extractFieldsValueToVCF(query, dbNodeMap.get(dbName));
				}
				
				if(dbExtract.isHasValue()) hasValue = true;
				totalCol += dbExtract.getClo();
				allExtract.add(dbExtract);
			}	
		}
		
		List<String> extractFields = extractResult(allExtract, hasValue, totalCol);
		if(extractFields.size() > 0 || loj) {
			if(annoOutFormat == AnnoOutFormat.BED) {
				return queryParser.toBEDRecord(query, extractFields);
			} else {
				return queryParser.toVCFRecord(query, extractFields);
			}
		} else {
			return null;
		}
	}
	
	public List<String> getHeader() {
		List<String> header = new ArrayList<String>();
		AbstractDatababseParser parser;
		if(annoOutFormat == AnnoOutFormat.BED) {
			List<String> dbHeader = new ArrayList<String>();
			for (String dbname : databaseNames) {
				parser = dbPareser.get(dbname);
				if(parser != null)
					parser.addExtractHeader(dbHeader);
			}
			header.add(queryParser.toBEDHeader(StringUtil.join(AbstractParser.TAB, dbHeader)));
		} else {
			VCFHeader vcfHeader = queryParser.getVCFHeader();
			if(vcfHeader != null) {
				for (String dbname : databaseNames) {
					parser = dbPareser.get(dbname);
					if(parser != null)
						vcfHeader = parser.addVCFHeader(vcfHeader);
				}
				for (VCFHeaderLine str : vcfHeader.getMetaDataInSortedOrder()) {
					header.add(VCFHeader.METADATA_INDICATOR + str.toString());
				}
			}
			header.add(queryParser.toVCFHeader());
		}
		return header;
	}
}
