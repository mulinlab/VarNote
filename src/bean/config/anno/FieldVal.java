package bean.config.anno;

import htsjdk.samtools.util.StringUtil;
import htsjdk.variant.vcf.VCFHeaderLineCount;
import htsjdk.variant.vcf.VCFInfoHeaderLine;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FieldVal {
	private final String COMMA = ",";
	
	private String[] queryAlts;
	private Map<String, List<String>> altValMap;
	private List<String> valList;
	
	private final FieldFormat format;
	private final VCFHeaderLineCount count;
//	private final VCFInfoHeaderLine info;
//	private final String fieldName;
	private boolean isFlag;
	
	public FieldVal(final String fieldName, final VCFInfoHeaderLine info, final FieldFormat format) {
		super();
//		this.fieldName = fieldName;
//		this.info = info;
		this.format = format;
		count = info.getCountType();
		if(count == VCFHeaderLineCount.INTEGER && info.getCount() == 0){
			isFlag = true;
		} 
	}
	
	public void initForceOverlap() {
		valList = new ArrayList<String>();
	}
	
	public void init(final String[] query_alts) {
		queryAlts = query_alts;
		altValMap = new HashMap<String, List<String>>();
		
		for (String string : query_alts) {
			altValMap.put(string, new ArrayList<String>());
		}
	}
	
	public void addDBValForceoverlap(final String val) {
		if(val == null || val.trim().equals("")) return;
		final String[] vals = val.split(COMMA); //C|downstream_gene_variant|MODIFIER|WASH7P, T|non_coding_transcript_exon_variant|MODIFIER|DDX11L1|ENSG00000223972
		
		for (String str : vals) {
			if(format != null) {
				ValWithAllele va = format.getFieldValWithFormat(str);
				if(va.allele != null) valList.add(va.getStrWithAllele());
				else valList.add(va.getStr());
			} else {
				valList.add(str);
			}
		}
	}
	
	public void addDBVal(final String[] db_alts, final String val) {
		if(val == null || val.trim().equals("")) return;
		final String[] vals = val.split(COMMA); //C|downstream_gene_variant|MODIFIER|WASH7P, T|non_coding_transcript_exon_variant|MODIFIER|DDX11L1|ENSG00000223972
		
		List<ValWithAllele> valList = new ArrayList<ValWithAllele>();
		for (String str : vals) {
			if(format != null) {
				valList.add(format.getFieldValWithFormat(str));
			} else {
				valList.add(new ValWithAllele(null, str));
			}
		}
		
		if(db_alts != null) {
			
//			int num = -1;
//			if(count == VCFHeaderLineCount.R) {
//				valList.remove(0);
//				num = valList.size();
//			} else if(count == VCFHeaderLineCount.A) {
//				num = valList.size();
//			} else if(count == VCFHeaderLineCount.G) {
//			
//			} else if(count == VCFHeaderLineCount.INTEGER){
//				num = info.getCount();
//			} 
//			System.out.println("num=" + num + ", db_alts=" + db_alts.length + ", val=" + val);
//			if(num > 0) if(num != db_alts.length) throw new IllegalArgumentException("The number of alt can't match Type in Info for field " + fieldName);
//			
			if((count == VCFHeaderLineCount.R) || (count == VCFHeaderLineCount.A)) {
				if(count == VCFHeaderLineCount.R) {
					valList.remove(0);
				}
				for (int i = 0; i < db_alts.length; i++) {
					valList.get(i).setAllele(db_alts[i]);
				}
			}
		}
		
		List<String> altVals;
		for (String query_alt : queryAlts) {
			altVals = altValMap.get(query_alt);
			
			for (ValWithAllele valWithAllele : valList) {
				if(valWithAllele.allele == null) {
					if(format != null && format.isOutFormatHasAllele()) {
						altVals.add(valWithAllele.getStrWithAllele(query_alt));
					} else {
						altVals.add(valWithAllele.getStr());
					}
				} else if(query_alt.equals(valWithAllele.allele)){
					altVals.add(valWithAllele.getStrWithAllele());
				}
			}
			altValMap.put(query_alt, altVals);
		}
	}
	
	public String mapTostr() {
		if(valList != null) return StringUtil.join(COMMA, valList);
		else {
			List<String> out = new ArrayList<String>();
			String val;
			for (String string : queryAlts) {
				val = StringUtil.join(COMMA, altValMap.get(string));
				if(!val.equals("")) out.add(val);
			}
			return StringUtil.join(COMMA, out);
		}
	}

	public boolean isFlag() {
		return isFlag;
	}

	public static void main(String[] args) {
//		FieldFormat format;
//		FieldVal fv;
//		
//		format = new FieldFormat("$allele|$1|$2..$n", "$allele|$1|$2", "col5");
//		fv = new FieldVal("col5");
//		fv.init(new String[]{"C", "G"});
//		fv.addDBVal(new String[]{"C,T"}, "G|G0.06968|0.06|0.02|0.07|0.07|0.09|0.0806,G|G0.06968|0.06|0.02|0.07|0.07|0.09|0.0806,G|0.06968|0.06|0.02|0.07|0.07|0.09|0.0806,T|T0.06968|0.06|0.02|0.07|0.07|0.09|0.0806,C|C0.06968|0.06|0.02|0.07|0.07|0.09|0.0806", 
//				new VCFInfoHeaderLine("col5", VCFHeaderLineCount.UNBOUNDED, VCFHeaderLineType.String, ""), format);
//		
//		fv.addDBVal(new String[]{"C,T"}, "C|C111|22|33|44|0.09|0.0806,G|G111|22|33|44|0.09|0.0806,T|T111|22|33|44|0.09|0.0806,C|C111|22|33|44|0.09|0.0806", 
//				new VCFInfoHeaderLine("col5", VCFHeaderLineCount.UNBOUNDED, VCFHeaderLineType.String, ""), format);
//		
//		System.out.println(fv.mapTostr());
//		
//		format = new FieldFormat("$1|$2", "$allele|$1|$2", "col5");
//		fv = new FieldVal("col5");
//		fv.init(new String[]{"C"});
//		fv.addDBVal(new String[]{"C", "T"}, "a|b,c|d", 
//				new VCFInfoHeaderLine("col5", VCFHeaderLineCount.A, VCFHeaderLineType.String, ""), format); 
//		fv.addDBVal(new String[]{"T", "C"}, "1|2,3|4,5|6", 
//				new VCFInfoHeaderLine("col5", VCFHeaderLineCount.R, VCFHeaderLineType.String, ""), format);
//		System.out.println(fv.mapTostr());
//		
//		format = null;
////		format = new FieldFormat("$1", "$allele|$1", "col5");
//		fv = new FieldVal("col5");
//		fv.init(new String[]{"C"});
//		fv.addDBVal(new String[]{"C", "T"}, "2,3,4", 
//				new VCFInfoHeaderLine("col5", VCFHeaderLineCount.INTEGER, VCFHeaderLineType.Integer, ""), format); 
//		fv.addDBVal(new String[]{"T", "C", "G"}, "1|2,3|4,5|6,7|8", 
//				new VCFInfoHeaderLine("col5", VCFHeaderLineCount.UNBOUNDED, VCFHeaderLineType.String, ""), format);
//		System.out.println(fv.mapTostr());
	}
}
