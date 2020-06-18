package org.mulinlab.varnote.config.anno.query;


import htsjdk.samtools.util.StringUtil;
import htsjdk.variant.variantcontext.VariantContext;
import htsjdk.variant.vcf.VCFHeader;
import org.mulinlab.varnote.config.anno.databse.VCFParser;
import org.mulinlab.varnote.operations.readers.query.AbstractFileReader;
import org.mulinlab.varnote.operations.readers.query.VCFFileReader;
import org.mulinlab.varnote.utils.format.Format;
import org.mulinlab.varnote.utils.node.LocFeature;
import java.util.StringJoiner;

public final class QueryVCFParser extends AbstractQueryParser{

	private final VCFParser vcfParser;

	public QueryVCFParser(final Format format, final VCFParser vcfParser) {
		super(format);
		this.vcfParser = vcfParser;
	}

	@Override
	public String toVCFHeader() {
		return VCF_HEADER_INDICATOR + format.getHeaderPartStr();
	}

	@Override
	public String toBEDHeader(final StringJoiner dbHeader) {
		StringJoiner join = new StringJoiner(TAB);

		join = getBEDHeaderStart(join);

		for (int i = 3; i < 7; i++) {
			join.add(VCF_FIELD[i]);
		}

		for (String key: vcfParser.getInfoKeys()) {  //add info fields
			join.add(key);
		}

		if(format.getHeaderPartSize() > 8)  //add genotypes
			for (int i = 9; i <= format.getHeaderPartSize(); i++) {
				join.add(format.getColumnName(i));
			}

		join.merge(dbHeader);
		return join.toString();
	}


	@Override
	public String toVCFRecord(final LocFeature query, final StringJoiner dbjoiner) {

		String[] parts = query.parts;

		if(dbjoiner != null && dbjoiner.length() > 0) {
			parts[INFO_COL - 1] += INFO_FIELD_SEPARATOR + dbjoiner.toString();
		}

		return StringUtil.join(TAB, parts);
	}

	@Override
	public String toBEDRecord(final LocFeature query, final StringJoiner dbjoiner) {
		StringJoiner join = new StringJoiner(TAB);

		String[] parts = query.parts;
		join = getBEDDataStart(join, query);

		for (int i = 3; i < 7; i++) {
			join.add(parts[i]);
		}

		VariantContext ctx = query.variantContext;
		if(ctx == null) ctx = vcfParser.getCodec().decode(query.origStr);

		Object val;
		for (String key: vcfParser.getInfoKeys()) {  //add info fields
			join.add(ctx.getAttributeAsString(key, NULLVALUE));
		}

		if(format.getHeaderPartSize() > 8)  //add genotypes
			for (int i = 8; i < format.getHeaderPartSize(); i++) {
				join.add(parts[i]);
			}

		if(dbjoiner != null && dbjoiner.length() > 0) join.merge(dbjoiner);
		return join.toString();
	}

	@Override
	public VCFHeader getVCFHeader() {
		return vcfParser.getVcfHeader();
	}
}
