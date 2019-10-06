package org.mulinlab.varnote.cmdline.txtreader.run;

import org.mulinlab.varnote.config.param.output.AnnoOutParam;
import org.mulinlab.varnote.config.run.AnnoRunConfig;


public final class AnnoRunReader<T> extends FileQueryReader<T> {

	public static final String OVERLAP_FILE = "overlap_file";
	public static final String ANNO_CONFIG = "anno_config";
	public static final String VCF_HEADER_PATH_FOR_BED = "vcf_header_for_bed";
	public static final String FORCE_OVERLAP = "force_overlap";
	public static final String ANNO_FORMAT = "out_format";


	@Override
	public T doEnd() {

		AnnoRunConfig annoRunConfig = null;
		AnnoOutParam outParam = new AnnoOutParam();

		if(valueHash.get(OVERLAP_FILE) != null) {
			annoRunConfig = new AnnoRunConfig();
			((AnnoRunConfig)annoRunConfig).setOverlapFile(valueHash.get(OVERLAP_FILE));
		} else {
			super.doEnd();
			annoRunConfig = new AnnoRunConfig(queryFileParam, dbParams);
			annoRunConfig.setRunParam(runParam);
		}

		outParam = (AnnoOutParam)setOutParam(outParam);
		annoRunConfig.setOutParam(outParam);

		if(valueHash.get(ANNO_FORMAT) != null) outParam.setAnnoOutFormat(valueHash.get(ANNO_FORMAT));
		if(valueHash.get(FORCE_OVERLAP) != null) annoRunConfig.setForceOverlap(valueHash.get(FORCE_OVERLAP));
		if(valueHash.get(ANNO_CONFIG) != null) annoRunConfig.setAnnoConfig(valueHash.get(ANNO_CONFIG));
		if(valueHash.get(VCF_HEADER_PATH_FOR_BED) != null) annoRunConfig.setVcfHeaderPathForBED(valueHash.get(VCF_HEADER_PATH_FOR_BED));

		return (T)annoRunConfig;
	}
}
