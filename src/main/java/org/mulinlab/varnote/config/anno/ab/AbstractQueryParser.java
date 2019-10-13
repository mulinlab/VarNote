package org.mulinlab.varnote.config.anno.ab;

import java.util.ArrayList;
import java.util.List;

import htsjdk.variant.vcf.VCFHeader;
import org.mulinlab.varnote.config.param.query.QueryFileParam;
import org.mulinlab.varnote.utils.format.Format;
import org.mulinlab.varnote.utils.node.RefNode;

public abstract class AbstractQueryParser extends AbstractParser{
	public static final String VCF_R = "CHROM,BEGIN,ID,REF,ALT,QUAL,FILTER,INFO";
	public static final String BED_R = "CHROM,BEGIN,END";
	
	protected final QueryFileParam query;
	protected List<Integer> requiredColForVCF;
	protected List<Integer> otherColForVCF;
	
	protected List<Integer> requiredColForBED;
	protected List<Integer> otherColForBED;
	
	
	public AbstractQueryParser(QueryFileParam query) {
		super();
		this.query = query;
		this.format = query.getQueryFormat();
		
		init();
	}

	public void init() {
		requiredColForVCF = new ArrayList<Integer>();
		requiredColForBED = new ArrayList<Integer>();
		otherColForVCF = new ArrayList<Integer>();
		otherColForBED = new ArrayList<Integer>();
		
		for (String key : VCF_R.split(",")) {
			requiredColForVCF.add(format.getFieldCol(key));
		}
	
		
		for (String key : BED_R.split(",")) {
			requiredColForBED.add(format.getFieldCol(key));
		}
		
//		for (Integer col : format.getColToField().keySet()) {
//			if(!requiredColForVCF.contains(col) && (col > -1) && (col != format.getFieldCol(Format.H_FIELD.END.toString()))) otherColForVCF.add(col);
//			if(!requiredColForBED.contains(col) && (col > -1)) otherColForBED.add(col);
//		}
	}
	
	public List<String> basicCol(final RefNode query, final boolean queryVCF, final boolean outVCF) {
		List<String> fieldList = new ArrayList<String>();
		fieldList.add(query.chr);
		if(queryVCF) {
			if(outVCF) { //vcf to vcf
				fieldList.add((query.beg + 1) + "");
			} else { //vcf to bed
				fieldList.add(query.beg + "");  
				fieldList.add(query.end + "");  
			}
		} else { //bed 
			if(outVCF) {  //bed to vcf          todo
				fieldList.add((query.beg + 1) + "");  
			} else {
				fieldList.add(query.beg + "");  
				fieldList.add(query.end + "");  
			}
		}
		return fieldList;
	}
	
	public abstract String toVCFHeader();
	public abstract String toVCFRecord(final RefNode query, final List<String> extractDB);
	
	public abstract String toBEDHeader(final String extractDBHeader);
	public abstract String toBEDRecord(final RefNode query, final List<String> extractDB);
	
	public abstract VCFHeader getVCFHeader();
}
