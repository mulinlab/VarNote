package org.mulinlab.varnote.config.anno.query;


import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import htsjdk.samtools.util.StringUtil;
import htsjdk.variant.vcf.VCFCodec;
import htsjdk.variant.vcf.VCFHeader;
import org.mulinlab.varnote.config.anno.VCFParser;
import org.mulinlab.varnote.config.anno.ab.AbstractQueryParser;
import org.mulinlab.varnote.config.param.query.QueryFileParam;
import org.mulinlab.varnote.utils.node.RefNode;
import org.mulinlab.varnote.utils.queryreader.LineIteratorImpl;
import org.mulinlab.varnote.constants.GlobalParameter;


public final class QueryBEDParser extends AbstractQueryParser{
	private String vcfHeaderPathForBED;
	public QueryBEDParser(final QueryFileParam query, final String vcfHeaderPathForBED) {
		super(query);
		this.vcfHeaderPathForBED = vcfHeaderPathForBED;
	}
	
	@Override
	public String toVCFHeader() {
		return VCFParser.VCF_HEADER_INDICATOR + VCFParser.VCF_FIELD ;
	}

	@Override
	public String toVCFRecord(final RefNode query, final List<String> extractDB) {
		List<String> fieldList = basicCol(query, false, true);
		
		//get basic
		for (int i = 2; i < requiredColForVCF.size() - 1; i++) {
			if(requiredColForVCF.get(i) == -1) {
				fieldList.add(".");
			} else {
				fieldList.add(query.getField(requiredColForVCF.get(i)));
			}
		}
		
		//get info 
		List<String> infoList = new ArrayList<String>();
		for (Integer col : otherColForVCF) {
			infoList.add(format.getColOriginalField(col) + VCF_INFO_EQUAL + query.getField(col));
		}
		
		if(extractDB.size() > 0) {
			for (String s : extractDB) {
				infoList.add(s);
			}
		}
		
		if(infoList.size() == 0) {
			fieldList.add(NULLVALUE);
		} else {
			fieldList.add(StringUtil.join(VCFParser.INFO_FIELD_SEPARATOR, infoList));
		}
		
		//combine
		return StringUtil.join(TAB, fieldList);
	}

	@Override
	public String toBEDHeader(final String extractDBHeader) {
		List<String> headerList = new ArrayList<String>();
		for (Integer col : requiredColForBED) {
			headerList.add(format.getColField(col));
		}
		for (Integer col : otherColForBED) headerList.add(format.getColOriginalField(col));
		
		if((extractDBHeader != null) && !extractDBHeader.trim().equals("")) headerList.add(extractDBHeader);
		return StringUtil.join(TAB, headerList);
	}

	@Override
	public String toBEDRecord(RefNode query, final List<String> extractDB) {
		List<String> fieldList = basicCol(query, false, false);
		for (Integer col : otherColForBED) fieldList.add(query.getField(col));
		
		if(extractDB.size() > 0) {
			for (String s : extractDB) {
				fieldList.add(s);
			}
		}

		return StringUtil.join(TAB, fieldList);
	}
	

	@Override
	public VCFHeader getVCFHeader() {
		try {
			if(vcfHeaderPathForBED == null) {
				System.out.println(GlobalParameter.KRED + "Note: Your output format is vcf, you should define --vcf-header-for-bed or -V for vcf header or we won't output header." + GlobalParameter.KNRM);
				return null;
			} else {
				LineIteratorImpl reader = new LineIteratorImpl(vcfHeaderPathForBED, query.getFileType());
				VCFHeader header = (VCFHeader) (new VCFCodec()).readActualHeader(reader);
				reader.close();
				return header;
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}
}
