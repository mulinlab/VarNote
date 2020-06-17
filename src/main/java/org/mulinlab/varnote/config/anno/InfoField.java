package org.mulinlab.varnote.config.anno;


import htsjdk.variant.vcf.VCFHeaderLineCount;
import htsjdk.variant.vcf.VCFInfoHeaderLine;
import org.apache.commons.lang3.StringUtils;
import java.util.ArrayList;
import java.util.List;


public final class InfoField extends NormalField {

	private final VCFInfoHeaderLine info;
	private boolean isFlag;
	private String[] qalts;

	public InfoField(final String fileName, final VCFInfoHeaderLine info) {
		super(fileName);
		this.info = info;
		if(info.getCountType() == VCFHeaderLineCount.INTEGER && info.getCount() == 0){
			isFlag = true;
		}
	}

	public void init(final int length, final String[] qalts) {
		super.init(length);
		this.qalts = qalts;
	}

	
	public void addDBValByAlt(final String val, final int matchFlag, final String[] dbalts) {

		VCFHeaderLineCount count = info.getCountType();
		if(val != null && !val.equals(NO_VAL)) {
			if(matchFlag == FORCE_OVERLAP || qalts == null || dbalts == null) dbValues[index++] = val;
			else {
				final String[] vals = val.split(COMMA);
				if(vals.length > 1) {
					vals[0] = vals[0].substring(1);
					int len = vals[vals.length - 1].length();
					vals[vals.length - 1] = vals[vals.length - 1].substring(0, len - 1);
				}

				if(((count == VCFHeaderLineCount.R) && vals.length == (dbalts.length + 1)) || ((count == VCFHeaderLineCount.A) && vals.length == dbalts.length))  {
					final List<String> result = new ArrayList<>();
					for (int i = 0; i < this.qalts.length; i++) {
						for (int j = 0; j < dbalts.length; j++) {
							if(qalts[i].equals(dbalts[j])) {
								if ((count == VCFHeaderLineCount.R) && vals.length == (dbalts.length + 1)) {
									result.add(vals[j + 1].trim());
								} else if ((count == VCFHeaderLineCount.A) && vals.length == dbalts.length) {
									result.add(vals[j].trim());
								}
							}
						}
					}
					dbValues[index++] = StringUtils.join(result, ",");
				} else {
					dbValues[index++] = val;
				}
			}
		}

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
		
//		if(db_alts != null) {
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
//		}
		
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


	public boolean isFlag() {
		return isFlag;
	}

}
