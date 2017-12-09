package bean.config.anno;

import java.util.List;

import bean.node.VCFNode;
import htsjdk.samtools.util.StringUtil;
import htsjdk.variant.vcf.VCFHeader;
import htsjdk.variant.vcf.VCFHeader.HEADER_FIELDS;
import htsjdk.variant.vcf.VCFInfoHeaderLine;
import constants.VannoUtils;
import postanno.FormatWithRef;

public class VCFParserQuery extends AbstractVCFParser{
	
	public VCFParserQuery(final FormatWithRef format, final String queryPath) {
		super(format, queryPath);
		isRefAndAltExsit = true;
	}
	
	public String toBEDHeader1() {
		String header = StringUtil.join(TAB, VCFHeader.HEADER_FIELDS.values());
		header = header.replace(HEADER_FIELDS.POS.toString(), VannoUtils.HEADER_FROM);
		header = header.replace(HEADER_FIELDS.ID.toString(), VannoUtils.HEADER_TO);
		header = header.replace(HEADER_FIELDS.INFO.toString(), StringUtil.join(TAB, infoKeys));
		return header;
	}
	
	public String toBEDHeader2() {
		return StringUtil.join(TAB, vcfHeader.getGenotypeSamples());
	}
	
	public String toVCFHeader() {
		return VCFHeader.HEADER_INDICATOR + StringUtil.join(TAB, VCFHeader.HEADER_FIELDS.values()) 
				+ TAB  + StringUtil.join(TAB, vcfHeader.getGenotypeSamples());
	}
	
	public String basicCols(final VCFNode node, boolean vcf) {
		String h = node.chr + TAB + node.beg + TAB ;
		if(vcf) h += "."; else h += node.end;
		return h + TAB + node.ref + TAB + StringUtil.join(COMMA, node.alts) + TAB + node.qulity + TAB + node.filter;
	}

	public String convertQueryToVCF1(final VCFNode node) {
		return basicCols(node, true) + TAB + node.info; 
	}
	
	public String convertQueryPart2(final VCFNode node) {
		return StringUtil.join(TAB, node.otherFields);
	}
	
	public String convertQueryToBED1(final VCFNode node) {
		return basicCols(node, false) + TAB + StringUtil.join(TAB, oneMapToList(allInfoToMap(node.info), infoKeys)); 
	}

	@Override
	public void emptyNodes(final String[] query_alts) {
	}
	
	public VCFHeader getOutVCFHeader(final List<VCFInfoHeaderLine> dbInfos) {
		return addDBInfos(vcfHeader, dbInfos);
	}
}
