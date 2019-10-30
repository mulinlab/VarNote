package org.mulinlab.varnote.config.anno.databse.anno;

import htsjdk.variant.vcf.VCFHeader;
import htsjdk.variant.vcf.VCFHeaderLineCount;
import htsjdk.variant.vcf.VCFHeaderLineType;
import htsjdk.variant.vcf.VCFInfoHeaderLine;
import org.apache.logging.log4j.Logger;
import org.mulinlab.varnote.config.anno.NormalField;
import org.mulinlab.varnote.constants.GlobalParameter;
import org.mulinlab.varnote.utils.LoggingUtils;
import org.mulinlab.varnote.utils.database.Database;
import org.mulinlab.varnote.utils.enumset.AnnoOutFormat;
import org.mulinlab.varnote.utils.enumset.FormatType;
import org.mulinlab.varnote.utils.format.Format;
import org.mulinlab.varnote.utils.node.LocFeature;

import java.util.*;

public abstract class AbstractDatababseAnnoParser {

	protected final Logger logger = LoggingUtils.logger;

	public static final String UNDERLINE = GlobalParameter.UNDERLINE;
	public static final String NULLVALUE = GlobalParameter.NULLVALUE;
	public static final String VCF_INFO_EQUAL = GlobalParameter.VCF_INFO_EQUAL;

	protected Format format;
//	protected Database db;
	protected ExtractConfig config;

	protected AnnoOutFormat annoOutFormat;
	protected boolean isForceOverlap;

	protected Map<Integer, NormalField> fileds;
	protected Map<Integer, VCFInfoHeaderLine> filedsInfoMap;

	protected int[] colsToExtract;
	protected List<String> log;


	public AbstractDatababseAnnoParser(final ExtractConfig config, final boolean isForceOverlap, final AnnoOutFormat annoOutFormat) {

		this.config = config;
//		this.db = config.getDb();
		this.format = this.config.getFormat();

		this.isForceOverlap = isForceOverlap;
		this.annoOutFormat = annoOutFormat;

		this.colsToExtract = config.getColToExtract();

		fileds = new HashMap<>();
		for (int col: colsToExtract) {
			if(isInfoFiled(col)) continue;
			fileds.put(col, new NormalField(format.getColumnName(col)));
		}

		this.log = new ArrayList<String>();
		init();
	}

	protected void init() {
		if(!format.isRefAndAltExsit()) {
			this.isForceOverlap = true;
			this.log.add(GlobalParameter.KRED + "Note: REF or ALT column is not defined, we will perform the annotation without REF and ALT." + GlobalParameter.KNRM);
		}

		if(this.annoOutFormat == AnnoOutFormat.VCF) {
			filedsInfoMap = new HashMap<Integer, VCFInfoHeaderLine>();
			VCFInfoHeaderLine info;
			for (int col : colsToExtract) {
				if(isInfoFiled(col)) continue;

				info = config.getInfoForField(format.getColumnName(col));
				if(info == null) {
					info = new VCFInfoHeaderLine(getFieldName(col), VCFHeaderLineCount.UNBOUNDED, VCFHeaderLineType.String, "");

					this.log.add(String.format("VCF Info information is not find for field %s%s%s, we will use %s%s%s instead.",
							GlobalParameter.KCYN, format.getColumnName(col), GlobalParameter.KNRM, GlobalParameter.KCYN, info, GlobalParameter.KNRM));
				}

				filedsInfoMap.put(col, info);
			}
		}
	}

	private boolean isInfoFiled(final int col) {
		return format.type == FormatType.VCF && col == GlobalParameter.INFO_COL;
	}

	public StringJoiner getHeader(StringJoiner joiner) {
		for (int col : colsToExtract) {
			if(isInfoFiled(col)) continue;

			joiner.add(getFieldName(col));
		}

		return joiner;
	}

	public StringJoiner joinFields(StringJoiner joiner) {
		String fieldVal;

		for (int col: colsToExtract) {
			if(fileds.get(col) != null) {
				fieldVal = fileds.get(col).getVal();

				if(annoOutFormat == AnnoOutFormat.VCF) {
					if(fieldVal != null) joiner.add(getFieldName(col) + VCF_INFO_EQUAL + fieldVal);
				} else {
					joiner.add(fieldVal == null ? NULLVALUE : fieldVal);
				}
			}
		}

		return joiner;
	}

	public void extractFieldsValue(final LocFeature query, final LocFeature[] dbFeatures) {
		if(dbFeatures != null && dbFeatures.length > 0) {

			initFields(dbFeatures.length);
			for (LocFeature dbFeature : dbFeatures) {
				if(checkQueryAndDBNodeMatch(query, dbFeature)) {
					processDBFeature(dbFeature);
				}
			}
		}
	}

	protected void processDBFeature(final LocFeature feature) {
		for (int col: colsToExtract) {
			if(fileds.get(col) != null) {
				fileds.get(col).addDB(feature.parts[col - 1]);
			}
		}
	}


	protected void initFields(final int size) {
		for (NormalField field: fileds.values()) {
			field.init(size);
		}
	}

	private boolean checkQueryAndDBNodeMatch(final LocFeature query, final LocFeature db) {
		if(isForceOverlap) return true;
		else {
			if(!query.ref.equals(db.ref)) {
				return false;
			} else {
				for (String qAlt : query.getAlts()) {
					for (String dbAlt: db.getAlts()) {
						if(dbAlt.equals(qAlt)) {
//							System.out.println(qAlt + ", " + dbAlt);
							return true;
						}
					}
				}
			}
			return false;
		}
	}

	protected String getFieldName(final int col) {
		if(config.getOutputName(col) == null) {
			return getDefaultName(format.getColumnName(col));
		} else {
			return config.getOutputName(col);
		}
	}

	protected String getDefaultName(final String name) {
		return config.getLabel() + UNDERLINE + name;
	}

	public VCFHeader addVCFHeader(VCFHeader header) {
		for (Integer col : colsToExtract) {
			if(isInfoFiled(col)) continue;
			header.addMetaDataLine(filedsInfoMap.get(col));
		}
		return header;
	}

	public ExtractConfig getConfig() {
		return config;
	}


	public StringJoiner joinNullVals(StringJoiner joiner) {
		if(annoOutFormat == AnnoOutFormat.BED) {
			for (int i = 0; i < colsToExtract.length; i++) {
				joiner.add(NULLVALUE);
			}
		}
		return joiner;
	}

	public void printLog() {
		for (String s: log) {
			logger.info(s);
		}
	}
}
