package main.java.vanno.bean.config.anno.query;

import java.util.ArrayList;
import java.util.List;

import htsjdk.samtools.util.StringUtil;
import htsjdk.variant.vcf.VCFHeader;
import main.java.vanno.bean.config.anno.VCFParser;
import main.java.vanno.bean.config.anno.ab.AbstractQueryParser;
import main.java.vanno.bean.format.Format;
import main.java.vanno.bean.node.RefNode;
import main.java.vanno.bean.query.Log;
import main.java.vanno.bean.query.Query;

public final class QueryVCFParser extends AbstractQueryParser{

	private final VCFParser vcfParser;
	
	public QueryVCFParser(Query query, final Log log, final boolean useJDK) {
		super(query);
		vcfParser = new VCFParser((query.getQueryFormat().getHeaderPath() != null) ? query.getQueryFormat().getHeaderPath() : query.getQueryPath(), log, useJDK, query.getFileType());
	}

	@Override
	public String toVCFHeader() {
		String format = "";
		if(vcfParser.getGenotypeSamples() != null && vcfParser.getGenotypeSamples().size() > 0) {
			format =  TAB + "FORMAT" + TAB + StringUtil.join(TAB, vcfParser.getGenotypeSamples());
		}
		return VCFParser.VCF_HEADER_INDICATOR + VCFParser.VCF_FIELD +  format ;
	}
	
	@Override
	public String toVCFRecord(final RefNode query, final List<String> extractDB) { 
		
		List<String> fieldList = basicCol(query, true, true);
		for (int i = 2; i < requiredColForVCF.size() - 1; i++) fieldList.add(query.getField(requiredColForVCF.get(i)));
		
		String info = query.getField( format.getFieldCol( Format.H_FIELD.INFO.toString()));
		if(extractDB.size() > 0) {
			info = info + INFO_FIELD_SEPARATOR + StringUtil.join(INFO_FIELD_SEPARATOR, extractDB);
		}
		fieldList.add(info);
		
		for (Integer col : otherColForVCF) fieldList.add(query.getField(col));
		
		return StringUtil.join(TAB, fieldList);
	}
	
	@Override
	public String toBEDHeader(final String extractDBHeader) {
		List<String> headerList = new ArrayList<String>();
		
		for (Integer col : requiredColForBED) headerList.add(format.getColField(col));
		
		String infoHeader = null;
		for (Integer col : otherColForBED) {
			if(col == format.getFieldCol(Format.H_FIELD.INFO.toString())) {
				infoHeader =  StringUtil.join(TAB, vcfParser.getInfoKeys());
				infoHeader = ((extractDBHeader) == null || extractDBHeader.trim().equals("")) ? infoHeader : infoHeader + TAB + extractDBHeader;
				headerList.add(infoHeader);
			} else {
				headerList.add(format.getColOriginalField(col));
			}
		}

		return StringUtil.join(TAB, headerList);
	}
	
	@Override
	public String toBEDRecord(RefNode query, final List<String> extractDB) {
		List<String> fieldList = basicCol(query, true, false);
		
		for (Integer col : otherColForBED) {
			if(col == format.getFieldCol(Format.H_FIELD.INFO.toString())) {
				fieldList.add(StringUtil.join(TAB, vcfParser.infoToList( query.getField(col) )));
			} else {
				fieldList.add(query.getField(col));
			}
		}
		if(extractDB.size() > 0) {
			for (String s : extractDB) {
				fieldList.add(s);
			}
		}

		return StringUtil.join(TAB, fieldList);
	}

	@Override
	public VCFHeader getVCFHeader() {	
		return vcfParser.getVcfHeader();
	}
}
