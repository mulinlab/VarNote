package org.mulinlab.varnote.config.anno.databse.anno;

import java.util.*;
import htsjdk.variant.variantcontext.VariantContext;
import htsjdk.variant.vcf.VCFHeader;
import htsjdk.variant.vcf.VCFHeaderLineCount;
import htsjdk.variant.vcf.VCFInfoHeaderLine;
import org.mulinlab.varnote.config.anno.InfoField;
import org.mulinlab.varnote.config.anno.NormalField;
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

//		config.getDb().setVCFLocCodec(true, vcfParser.getCodec());
	}

	@Override
	public StringJoiner getHeader(StringJoiner joiner) {
		super.getHeader(joiner);

		for (String infoID : infoFieldsToExtract) {
			joiner.add(getInfoName(infoID));
		}
		return joiner;
	}

	public List<String> getHeaderList() {
		List<String> header = super.getHeaderList();
		for (String infoID : infoFieldsToExtract) {
			header.add(getInfoName(infoID));
		}
		return header;
	}

	@Override
	protected void processDBFeature(final LocFeature feature, final int matchFlag) {
		super.processDBFeature(feature, matchFlag);

		VariantContext ctx = feature.variantContext;
		for (String key: infoFieldsToExtract) {
			if(ctx.hasAttribute(key)) {
				if (isForceOverlap) {
					infoFieldMap.get(key).addDBValByAlt(ctx.getAttributeAsString(key, NormalField.NO_VAL), matchFlag, null);
				} else {
					infoFieldMap.get(key).addDBValByAlt(ctx.getAttributeAsString(key, NormalField.NO_VAL), matchFlag, feature.getAlts());
				}
			}
		}
	}

	@Override
	protected Map<String, String> extractDBFeature(Map<String, String> obj, final LocFeature feature, final int matchFlag) {
		obj = super.extractDBFeature(obj, feature, matchFlag);

		String infoVal;
		for (String key: infoFieldsToExtract) {
			if(infoFieldMap.get(key) != null) {
				infoVal = infoFieldMap.get(key).getVal();

				if(infoVal != null) {
					obj.put(getInfoName(key), infoVal);
				}
			}
		}
		return obj;
	}

	@Override
	protected void initFields(final LocFeature feature, final int size) {
		super.initFields(feature, size);

		if (isForceOverlap) {
			for (InfoField field: infoFieldMap.values()) {
				field.init(size, null);
			}
		} else {
			for (InfoField field: infoFieldMap.values()) {
				field.init(size, feature.getAlts());
			}
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

	public Map<String, String> getMapValue() {
		Map<String, String> map = super.getMapValue();

		for (String key: infoFieldsToExtract) {
			if(infoFieldMap.get(key) != null) {
				map.put(getInfoName(key), infoFieldMap.get(key).getVal());
			}
		}
		return map;
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


			if(outName != null) {
				try{
					if(info.getCountType() == VCFHeaderLineCount.INTEGER) {
						info = new VCFInfoHeaderLine(outName, info.getCount(), info.getType(), info.getDescription());
					} else {
						info = new VCFInfoHeaderLine(outName, info.getCountType(), info.getType(), info.getDescription());
					}
				} catch (Exception e) {
					logger.error("Add info for " + outName + " with error." );
				}
			}
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
