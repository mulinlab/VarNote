package org.mulinlab.varnote.config.parser;

import java.util.*;
import htsjdk.variant.vcf.VCFHeader;
import htsjdk.variant.vcf.VCFHeaderLine;
import org.mulinlab.varnote.config.anno.databse.anno.AbstractDatababseAnnoParser;
import org.mulinlab.varnote.config.anno.databse.anno.DatabaseAnnoBEDParser;
import org.mulinlab.varnote.config.anno.databse.anno.DatabaseAnnoVCFParser;
import org.mulinlab.varnote.config.anno.databse.anno.ExtractConfig;
import org.mulinlab.varnote.config.anno.query.AbstractQueryParser;
import org.mulinlab.varnote.config.anno.query.QueryBEDParser;
import org.mulinlab.varnote.config.anno.query.QueryVCFParser;
import org.mulinlab.varnote.config.param.output.AnnoOutParam;
import org.mulinlab.varnote.config.param.query.QueryFileParam;
import org.mulinlab.varnote.config.parser.output.AnnoOut;
import org.mulinlab.varnote.config.run.AnnoRunConfig;
import org.mulinlab.varnote.operations.readers.query.VCFFileReader;
import org.mulinlab.varnote.utils.database.Database;
import org.mulinlab.varnote.utils.enumset.AnnoOutFormat;
import org.mulinlab.varnote.constants.GlobalParameter;
import org.mulinlab.varnote.utils.enumset.FormatType;
import org.mulinlab.varnote.utils.node.LocFeature;

public class AnnoParser implements ResultParser {
	public static final String NULLVALUE = GlobalParameter.NULLVALUE;
	public static final String INFO_FIELD_SEPARATOR = GlobalParameter.INFO_FIELD_SEPARATOR;
	public static final String TAB = GlobalParameter.TAB;
	private static String sign;

	private final AbstractQueryParser queryParser;
	private final Map<String, AbstractDatababseAnnoParser> dbParesers;
	private final boolean loj;
	private final AnnoOutFormat outFormat;

	public AnnoParser(final AnnoRunConfig config, final Map<String, ExtractConfig> extractConfigMap, final int index) {
		super();

		final QueryFileParam query = (QueryFileParam)config.getQueryParam();
		if(query.getQueryFormat().type == FormatType.VCF) {
			this.queryParser = new QueryVCFParser(query.getQueryFormat(), ((VCFFileReader)query.getThreadReader(index)).getVcfParser());
		} else {
			this.queryParser = new QueryBEDParser(query.getQueryFormat(), config.getVcfHeaderPathForBED());
		}

		boolean isForceOverlap = config.isForceOverlap();
		if(!query.getQueryFormat().isRefAndAltExsit()) isForceOverlap = true;
		outFormat = ((AnnoOutParam)config.getOutParam()).getAnnoOutFormat();

		dbParesers = new HashMap<>();
		final List<Database> dbs = config.getDatabses();
		String label;

		for (Database db: dbs) {
			label = db.getOutName();

			if(extractConfigMap.get(label) != null) {
				if(db.getFormat().type == FormatType.VCF) {
					dbParesers.put(label, new DatabaseAnnoVCFParser(extractConfigMap.get(label), isForceOverlap, outFormat));
				} else {
					dbParesers.put(label, new DatabaseAnnoBEDParser(extractConfigMap.get(label), isForceOverlap, outFormat));
				}
			}
		}

		this.loj = config.getOutParam().isLoj();
		init();
	}

	public void init() {
		sign = (outFormat == AnnoOutFormat.BED) ? TAB : INFO_FIELD_SEPARATOR;
	}

	public void printLog() {
		for (String key : dbParesers.keySet()) {
			dbParesers.get(key).printLog();
		}
	}

	private String printRecode(final LocFeature query, StringJoiner joiner) {
		if(outFormat == AnnoOutFormat.BED) {
			return queryParser.toBEDRecord(query, joiner);
		} else {
			return queryParser.toVCFRecord(query, joiner);
		}
	}

	private boolean isNotNull(final String[] fields) {
		for (String s: fields) {
			if(!s.equals(NULLVALUE)) {
				return true;
			}
		}
		return false;
	}
	
	public List<String> getHeader() {
		List<String> header = new ArrayList<String>();

		AbstractDatababseAnnoParser parser;
		if(outFormat == AnnoOutFormat.BED) {
			StringJoiner joiner = new StringJoiner(TAB);

			for (String key : dbParesers.keySet()) {
				parser = dbParesers.get(key);
				if(parser != null)
					parser.getHeader(joiner);
			}
			header.add(queryParser.toBEDHeader(joiner));
		} else {
			VCFHeader vcfHeader = queryParser.getVCFHeader();
			if(vcfHeader != null) {

				for (String key : dbParesers.keySet()) {
					parser = dbParesers.get(key);
					if(parser != null)
						vcfHeader = parser.addVCFHeader(vcfHeader);
				}

				for (VCFHeaderLine str : vcfHeader.getMetaDataInSortedOrder()) {
					header.add(VCFHeader.METADATA_INDICATOR + str.toString());
				}
			}
			header.add(queryParser.toVCFHeader());
		}
		return header;
	}

	public AbstractQueryParser getQueryParser() {
		return queryParser;
	}

	public Map<String, AbstractDatababseAnnoParser> getDbParesers() {
		return dbParesers;
	}

	public boolean isLoj() {
		return loj;
	}

	public AnnoOutFormat getOutFormat() {
		return outFormat;
	}

	@Override
	public AnnoOut processNode(final LocFeature query, final Map<String, LocFeature[]> dbNodeMap) {
		AnnoOut annoOut = new AnnoOut(query);

		StringJoiner joiner = new StringJoiner(sign);

		if (dbNodeMap != null) {
			for (String key : dbParesers.keySet()) {

				if (dbNodeMap.get(key) != null && dbNodeMap.get(key).length > 0) {
					dbParesers.get(key).extractFieldsValue(query, dbNodeMap.get(key));
					joiner = dbParesers.get(key).joinFields(joiner);
				} else if(loj) {
					joiner = dbParesers.get(key).joinNullVals(joiner);
				}
			}
		} else {
			if(loj) {
				for (String key : dbParesers.keySet()) {
					joiner = dbParesers.get(key).joinNullVals(joiner);
				}
			}
		}

		if (loj) {
			annoOut.setResult(printRecode(query, joiner));
		} else {
			if (joiner.length() > 0 && isNotNull(joiner.toString().split(sign))) {
				annoOut.setResult(printRecode(query, joiner));
			} else {
				annoOut.setResult(null);
			}
		}

		return annoOut;
	}
}
