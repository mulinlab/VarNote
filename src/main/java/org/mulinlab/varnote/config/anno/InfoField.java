package org.mulinlab.varnote.config.anno;


import htsjdk.variant.vcf.VCFHeaderLineCount;
import htsjdk.variant.vcf.VCFInfoHeaderLine;


public final class InfoField extends NormalField {

	private String[] queryAlts;
	private final VCFInfoHeaderLine info;
	private boolean isFlag;
	
	
	public InfoField(final String fileName, final VCFInfoHeaderLine info) {
		super(fileName);
		this.info = info;
		if(info.getCountType() == VCFHeaderLineCount.INTEGER && info.getCount() == 0){
			isFlag = true;
		}
	}
	
	public void init(final String[] query_alts) {
		queryAlts = query_alts;
	}
	
//	public void addDBValForceoverlap(final String val) {
//
//		if(val == null || val.trim().equals("")) return;
//		final String[] vals = val.split(COMMA); //C|downstream_gene_variant|MODIFIER|WASH7P, T|non_coding_transcript_exon_variant|MODIFIER|DDX11L1|ENSG00000223972
//		
//		for (String str : vals) {
//			if(format != null) {
//				ValWithAllele va = format.getFieldValWithFormat(str);
//				if(va.allele != null) valList.add(va.getStrWithAllele());
//				else valList.add(va.getStr());
//			} else {
//				valList.add(str);
//			}
//		}
//	}
	
	public void addDBVal(final String[] db_alts, final String val) {
//		if(val == null || val.trim().equals("")) return;
//		final String[] vals = val.split(COMMA); //C|downstream_gene_variant|MODIFIER|WASH7P, T|non_coding_transcript_exon_variant|MODIFIER|DDX11L1|ENSG00000223972
//
//		List<ValWithAllele> valList = new ArrayList<ValWithAllele>();
//		for (String str : vals) {
//			if(format != null) {
//				valList.add(format.getFieldValWithFormat(str));
//			} else {
//				valList.add(new ValWithAllele(null, str));
//			}
//		}
		
		if(db_alts != null) {
//			int num = -1;
//			System.out.println("num=" + num + ", db_alts=" + db_alts.length + ", val=" + val);
//			if(num > 0) if(num != db_alts.length) throw new InvalidArgumentException("The number of alt can't match Type in Info for field " + fieldName);
//			
//			if((count == VCFHeaderLineCount.R) || (count == VCFHeaderLineCount.A)) {
//				if(count == VCFHeaderLineCount.R) {
//					valList.remove(0);
//				}
//				for (int i = 0; i < db_alts.length; i++) {
//					valList.get(i).setAllele(db_alts[i]);
//				}
//			}
		}
		
//		List<String> altVals;
//		for (String query_alt : queryAlts) {
//			altVals = altValMap.get(query_alt);
//
//			for (ValWithAllele valWithAllele : valList) {
//				if(valWithAllele.allele == null) {
//					if(format != null && format.isOutFormatHasAllele()) {
//						altVals.add(valWithAllele.getStrWithAllele(query_alt));
//					} else {
//						altVals.add(valWithAllele.getStr());
//					}
//				} else if(query_alt.equals(valWithAllele.allele)){
//					altVals.add(valWithAllele.getStrWithAllele());
//				}
//			}
//			altValMap.put(query_alt, altVals);
//		}
	}
	

	public String nodesTostr() {
//		if(useList) {
//			return StringUtil.join(COMMA, valList);
//		}
//		else {
//			List<String> out = new ArrayList<String>();
//			String val;
//			for (String string : queryAlts) {
//				val = StringUtil.join(COMMA, altValMap.get(string));
//				if(!val.equals("")) out.add(val);
//			}
//			return StringUtil.join(COMMA, out);
//		}
		return null;
	}

	public boolean isFlag() {
		return isFlag;
	}

}
