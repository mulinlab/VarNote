package org.mulinlab.varnote.config.anno.query;

import java.util.ArrayList;
import java.util.List;

import htsjdk.samtools.util.StringUtil;
import htsjdk.variant.vcf.VCFHeader;
import org.mulinlab.varnote.config.anno.VCFParser;
import org.mulinlab.varnote.config.anno.ab.AbstractQueryParser;
import org.mulinlab.varnote.config.param.query.QueryFileParam;
import org.mulinlab.varnote.utils.format.Format;
import org.mulinlab.varnote.utils.node.RefNode;

public final class QueryVCFParser extends AbstractQueryParser{

	private final VCFParser vcfParser;
	
	public QueryVCFParser(QueryFileParam query) {
		super(query);
		vcfParser = new VCFParser((query.getQueryFormat().getHeaderPath() != null) ? query.getQueryFormat().getHeaderPath() : query.getQueryPath(), query.getFileType());
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
