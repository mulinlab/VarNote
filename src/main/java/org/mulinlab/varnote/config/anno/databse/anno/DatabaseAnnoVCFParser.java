package org.mulinlab.varnote.config.anno.databse.anno;

import java.util.*;

import htsjdk.variant.variantcontext.VariantContext;
import htsjdk.variant.vcf.VCFHeader;
import htsjdk.variant.vcf.VCFInfoHeaderLine;
import org.mulinlab.varnote.config.anno.InfoField;
import org.mulinlab.varnote.config.anno.databse.VCFParser;
import org.mulinlab.varnote.utils.enumset.AnnoOutFormat;
import org.mulinlab.varnote.utils.node.LocFeature;


public final class DatabaseAnnoVCFParser extends AbstractDatababseAnnoParser {

	private VCFParser vcfParser;

	private List<String> infoFieldsToExtract;
	private Map<String, InfoField> infoFieldMap;

	public DatabaseAnnoVCFParser(final ExtractConfig config, final boolean isForceOverlap, final AnnoOutFormat annoOutFormat) {
		super(config, isForceOverlap, annoOutFormat);
	}

	@Override
	protected void init() {
		super.init();

		vcfParser = config.getDb().getVcfParser();
		infoFieldsToExtract = config.getInfoFiledToExtract();

		infoFieldMap = new HashMap<>();
		for (String key: infoFieldsToExtract) {
			infoFieldMap.put(key, new InfoField(key, vcfParser.getInfoHeaderLine(key)));
		}

		config.getDb().setVCFLocCodec(true, vcfParser.getCodec());
	}

	@Override
	public StringJoiner getHeader(StringJoiner joiner) {
		super.getHeader(joiner);

		for (String infoID : infoFieldsToExtract) {
			joiner.add(getInfoName(infoID));
		}
		return joiner;
	}

	@Override
	protected void processDBFeature(final LocFeature feature) {
		super.processDBFeature(feature);

		VariantContext ctx = feature.variantContext;
		for (String key: infoFieldsToExtract) {
			infoFieldMap.get(key).addDB(ctx.getAttributeAsString(key, null));
		}
	}

	@Override
	protected void initFields(final int size) {
		super.initFields(size);

		for (InfoField field: infoFieldMap.values()) {
			field.init(size);
		}
	}


	@Override
	public StringJoiner joinFields(StringJoiner joiner) {
		joiner = super.joinFields(joiner);

		String infoVal;

		for (String key: infoFieldsToExtract) {
			if(infoFieldMap.get(key) != null) {
				infoVal = infoFieldMap.get(key).getVal();

				if(annoOutFormat == AnnoOutFormat.VCF) {
					if(infoVal != null) {

						if(infoFieldMap.get(key).isFlag()) {
							joiner.add(infoVal);
						} else {
							joiner.add(getInfoName(key) + VCF_INFO_EQUAL + infoVal);
						}
					}
				} else {
					joiner.add(infoVal == null ? NULLVALUE : infoVal);
				}
			}
		}

		return joiner;
	}

	private String getInfoName(final String infoID) {
		if(config.getInfoOutputName(infoID) == null) {
			return config.getLabel() + UNDERLINE + infoID;
		} else {
			return config.getInfoOutputName(infoID);
		}
	}

	@Override
	public VCFHeader addVCFHeader(VCFHeader header) {
		header = super.addVCFHeader(header);

		VCFInfoHeaderLine info;
		String outName;

		for (String field : infoFieldsToExtract) {  //remove info
			outName = config.getInfoOutputName(field);
			info = vcfParser.getInfoHeaderLine(field);

			if(outName != null)  info = new VCFInfoHeaderLine(outName, info.getCountType(), info.getType(), info.getDescription());
			header.addMetaDataLine(info);
		}
		return header;
	}

	public StringJoiner joinNullVals(StringJoiner joiner) {
		if(annoOutFormat == AnnoOutFormat.BED) {
			joiner = super.joinNullVals(joiner);
			for (int i = 0; i < infoFieldsToExtract.size(); i++) {
				joiner.add(NULLVALUE);
			}
		}
		return joiner;
	}
}
