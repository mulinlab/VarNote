package org.mulinlab.varnote.config.anno.query;


import java.util.StringJoiner;
import htsjdk.variant.vcf.VCFHeader;
import org.apache.logging.log4j.Logger;
import org.mulinlab.varnote.config.param.query.QueryFileParam;
import org.mulinlab.varnote.constants.GlobalParameter;
import org.mulinlab.varnote.operations.readers.query.AbstractFileReader;
import org.mulinlab.varnote.utils.LoggingUtils;
import org.mulinlab.varnote.utils.format.Format;
import org.mulinlab.varnote.utils.node.LocFeature;


public abstract class AbstractQueryParser {

	protected final Logger logger = LoggingUtils.logger;

	public static String[] VCF_FIELD = new String[]{"CHROM", "POS", "ID", "REF", "ALT", "QUAL", "FILTER", "INFO"};
	public static String[] BED_FIELD = new String[]{"CHROM", "BEGIN", "END"};

	public static final String NULLVALUE = GlobalParameter.NULLVALUE;
	public static final String TAB = GlobalParameter.TAB;
	public static final int INFO_COL = GlobalParameter.INFO_COL;
	public static final String VCF_HEADER_INDICATOR = GlobalParameter.VCF_HEADER_INDICATOR;
	public static final String INFO_FIELD_SEPARATOR = GlobalParameter.INFO_FIELD_SEPARATOR;
	public static final String VCF_INFO_EQUAL = GlobalParameter.VCF_INFO_EQUAL;

	protected final Format format;

	public AbstractQueryParser(final Format format) {
		super();
		this.format = format;
	}

	public StringJoiner getBEDHeaderStart(final StringJoiner joiner) {
		for (String h: BED_FIELD) {
			joiner.add(h);
		}
		return joiner;
	}

	public StringJoiner getBEDDataStart(final StringJoiner joiner, final LocFeature query) {
		joiner.add(query.chr);
		joiner.add(String.valueOf(query.beg));
		joiner.add(String.valueOf(query.end));

		return joiner;
	}

	public abstract String toVCFHeader();
	public abstract String toVCFRecord(final LocFeature query, final StringJoiner dbjoiner);

	public abstract String toBEDHeader(final StringJoiner joiner);
	public abstract String toBEDRecord(final LocFeature query, final StringJoiner dbjoiner);

	public abstract VCFHeader getVCFHeader();

}
