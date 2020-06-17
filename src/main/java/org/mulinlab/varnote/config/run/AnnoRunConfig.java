package org.mulinlab.varnote.config.run;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import htsjdk.samtools.util.IOUtil;
import org.mulinlab.varnote.cmdline.txtreader.anno.AnnoConfigReader;
import org.mulinlab.varnote.config.parser.AnnoParser;
import org.mulinlab.varnote.config.anno.databse.anno.ExtractConfig;
import org.mulinlab.varnote.config.param.postDB.DBAnnoParam;
import org.mulinlab.varnote.config.param.DBParam;
import org.mulinlab.varnote.config.param.output.AnnoOutParam;
import org.mulinlab.varnote.config.param.query.QueryFileParam;
import org.mulinlab.varnote.cmdline.txtreader.anno.OverlapHeaderReader;
import org.mulinlab.varnote.config.parser.ResultParser;
import org.mulinlab.varnote.config.parser.output.AnnoOut;
import org.mulinlab.varnote.constants.GlobalParameter;
import org.mulinlab.varnote.utils.VannoUtils;
import org.mulinlab.varnote.utils.database.Database;
import org.mulinlab.varnote.utils.enumset.FormatType;
import org.mulinlab.varnote.utils.node.LocFeature;

public class AnnoRunConfig extends OverlapRunConfig{

	private String overlapFile;
	private boolean forceOverlap = GlobalParameter.DEFAULT_FORCE_OVERLAP;
	private String annoConfig;
	private String vcfHeaderPathForBED;

	private ResultParser[] annoParsers;

	public AnnoRunConfig() {
		super();
	}

	public AnnoRunConfig(final String overlapFile) {
		setOverlapFile(overlapFile);
	}

	public AnnoRunConfig(final String queryPath, final String[] dbPaths) {
		setQueryParam(new QueryFileParam(queryPath, true));
		setDbParams(dbPaths);
	}

	public AnnoRunConfig(final QueryFileParam query, final List<DBParam> dbConfigs) {
		super(query, dbConfigs);
	}

	public AnnoRunConfig(final String queryPath, final String[] dbPaths, final String annoConfig, final AnnoOutParam output, final int thread) {
		this(queryPath, dbPaths);
		setOutParam(output);
		runParam.setThread(thread);

		if(annoConfig != null) setAnnoConfig(annoConfig);
	}

	public AnnoRunConfig(final QueryFileParam query, final List<DBParam> dbConfigs, final String annoConfig, final AnnoOutParam output, final int thread) {
		this(query, dbConfigs);
		setOutParam(output);
		runParam.setThread(thread);

		if(annoConfig != null) setAnnoConfig(annoConfig);
	}

	@Override
	protected void initQuery() {
		super.initQuery();
	}

	@Override
	protected void initDB() {
		super.initDB();
	}

	@Override
	protected void initOutput() {
		if(outParam == null) {
			setOutParam(new AnnoOutParam());
		}
		outParam.setOutFileSuffix(GlobalParameter.ANNO_RESULT_SUFFIX);
	}

	public void initOther() {
		logger.info(VannoUtils.printLogHeader("ANNOTATION  SETTING"));

		QueryFileParam queryParam = (QueryFileParam)this.queryParam;
		AnnoOutParam outParam = (AnnoOutParam)this.outParam;
		outParam.setDefalutOutFormat(queryParam.getQueryFormat());

		Map<String, DBAnnoParam> map = null;
		if(annoConfig == null) annoConfig = queryParam.getQueryPath() + GlobalParameter.ANNO_CONFIG_SUFFIX;
		if(new File(annoConfig).exists()) {
			map = new AnnoConfigReader<Map<String, DBAnnoParam>>().read(annoConfig);
		}

		Map<String, ExtractConfig> extractConfigMap = new HashMap<>();
		DBAnnoParam annoParam = null;
		for (Database db: databses) {
			if(db.getFormat().type == FormatType.VCF) {
				db.setVCFLocCodec(true);
			} else {
				db.setDefaultLocCodec(true);
			}

			if(map == null) {
				annoParam = DBAnnoParam.defaultParam(db.getOutName());
			} else {
				annoParam = map.get(db.getOutName());
			}
			if(annoParam != null) {
				extractConfigMap.put(db.getOutName(), new ExtractConfig(annoParam, db));
			} else {
				logger.info(String.format("%sThe tag for database %s was not found in the configuration file, so its annotation would not be extracted.%s", GlobalParameter.KRED, db.getOutName(), GlobalParameter.KNRM));
			}
		}

		final int threadSize = queryParam.getThreadSize();
		annoParsers = new AnnoParser[threadSize];

		for (int i = 0; i < threadSize; i++) {
			annoParsers[i] = new AnnoParser(this, extractConfigMap, i);
		}
		((AnnoParser)annoParsers[0]).printLog();

		initPrintter();
	}


	@Override
	protected List<String> getHeader() {
		return ((AnnoParser)annoParsers[0]).getHeader();
	}

	public void mergeResult() {
		super.mergeResult();
	}

	public void annoRecord(final LocFeature node, final Map<String, LocFeature[]> results, final int index) throws IOException {
		printter.print(doAnno(node, results, index), index);
	}

	private String doAnno(final LocFeature node, final Map<String, LocFeature[]> results, final int index) {
		AnnoOut annoOut = (AnnoOut)annoParsers[index].processNode(node, results);
		return annoOut.getResult();
	}

	public void setAnnoConfig(final String annoConfig) {
		IOUtil.assertInputIsValid(annoConfig);
		this.annoConfig = annoConfig;
	}

	public void setOverlapFile(final String overlapFile) {
		IOUtil.assertInputIsValid(overlapFile);
		this.overlapFile = overlapFile;

		AnnoRunConfig newRunConfig = new OverlapHeaderReader<AnnoRunConfig>().read(overlapFile);

		setQueryParam(newRunConfig.getQueryParam());
		setDbParams(newRunConfig.getDbParams());

		setOutParam(newRunConfig.getOutParam());
	}

	public String getOverlapFile() {
		return overlapFile;
	}
	public boolean isForceOverlap() {
		return forceOverlap;
	}

	public void setForceOverlap(boolean forceOverlap) {
		this.forceOverlap = forceOverlap;
	}
	public void setForceOverlap(final String forceOverlap) {
		this.forceOverlap = VannoUtils.strToBool(forceOverlap);
	}

	public String getVcfHeaderPathForBED() {
		return vcfHeaderPathForBED;
	}

	public void setVcfHeaderPathForBED(String vcfHeaderPathForBED) {
		IOUtil.assertInputIsValid(vcfHeaderPathForBED);
		this.vcfHeaderPathForBED = vcfHeaderPathForBED;
	}
}
