package bean.config.anno;

import htsjdk.samtools.util.IOUtil;
import htsjdk.samtools.util.StringUtil;
import htsjdk.variant.vcf.VCFCodec;
import htsjdk.variant.vcf.VCFHeader;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;

import bean.node.BedNode;
import constants.VannoUtils;
import postanno.FormatWithRef;
import postanno.LineIteratorImpl;

public class BEDParserQuery extends AbstractBedParser{
	
	private String vcfHeaderPath;
	public BEDParserQuery(final FormatWithRef format, final List<String> queryPath) {
		super(format, queryPath);
		checkRefAndAlt(fields);
	}
	
	public BEDParserQuery(final FormatWithRef format, final File file) {
		super(format, file);
		checkRefAndAlt(fields);
	}

	public String toBEDHeader() {
		if(requiredCols == null) {
			getRequiredBEDCols();
			getOthersForBED();
		}
		String header = VCFHeader.HEADER_INDICATOR + VannoUtils.HEADER_CHR + TAB 
				+ VannoUtils.HEADER_FROM + TAB + VannoUtils.HEADER_TO + TAB;
		if(format.refCol > 0) header += VannoUtils.HEADER_REF + TAB;
		if(format.altCol > 0) header += VannoUtils.HEADER_ALT + TAB;
		
		header += StringUtil.join(TAB, otherfields);
		return header;
	}
	
	public String toVCFHeader() {
		if(requiredCols == null) {
			getRequiredVCFCols();
			getOthersForVCF();
		}
		return VCFHeader.HEADER_INDICATOR + StringUtil.join(TAB, VCFHeader.HEADER_FIELDS.values());
	}
	
	public String basicCols(final BedNode node, boolean vcf) {
		String h = node.chr + TAB + node.beg + TAB ;
		if(vcf) h += "."; else h += node.end;
		return h;
	}
	
	public String convertQueryToVCF(final BedNode node) {
		String h = basicCols(node, true);
		if(requiredCols.get(3) != -1) h +=  TAB + node.allFieldsMap.get(requiredCols.get(3)); else h += TAB + ".";
		if(requiredCols.get(4) != -1) h +=  TAB + node.allFieldsMap.get(requiredCols.get(4)); else h += TAB + ".";
		if(requiredCols.get(5) != -1) h +=  TAB + node.allFieldsMap.get(requiredCols.get(5)); else h += TAB + ".";
		if(requiredCols.get(6) != -1) h +=  TAB + node.allFieldsMap.get(requiredCols.get(6)); else h += TAB + ".";
		
		h += TAB + StringUtil.join(INFO_FIELD_SEPARATOR, getOtherFieldsVal(node, true));
		return h;
	}
	
	
	public String convertQueryToBED(final BedNode node) {
		String h = basicCols(node, false);
		if(requiredCols.get(3) != null) h +=  TAB + node.allFieldsMap.get(requiredCols.get(3));
		if(requiredCols.get(4) != null) h +=  TAB + node.allFieldsMap.get(requiredCols.get(4));
		
		h += TAB + StringUtil.join(TAB, getOtherFieldsVal(node, false));
		return h;
	}
	
	public List<String> getOtherFieldsVal(final BedNode node, final boolean isVCF) {
		List<String> info = new ArrayList<String>();
		for (String field : otherfields) {
			if(isVCF) {
				info.add(field + EQUAL_SEPERATOR + node.allFieldsMap.get(otherFieldsMap.get(field)));
			} else {
				info.add(node.allFieldsMap.get(otherFieldsMap.get(field)));
			}
		}
		return info;
	}
	

	@Override
	public void emptyNodes(final String[] query_alts) {
	}

	public String getVcfHeaderPath() {
		return vcfHeaderPath;
	}

	public void setVcfHeaderPath(String vcfHeaderPath) {
		this.vcfHeaderPath = vcfHeaderPath;
	}
	
	public VCFHeader outVCFHeader() {
		try {
			if(vcfHeaderPath == null) {
				System.out.println("*** Note: Your output format is vcf, you should define header-path for vcf header or we won't output header.");
				return null;
			} else {
				IOUtil.assertInputIsValid(vcfHeaderPath);
				return (VCFHeader)(new VCFCodec()).readActualHeader(new LineIteratorImpl(new File(vcfHeaderPath)));
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		return null;
	}
	

}
