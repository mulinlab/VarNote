package org.mulinlab.varnote.config.run;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import htsjdk.samtools.util.IOUtil;
import org.mulinlab.varnote.config.anno.ab.AbstractDatababseParser;
import org.mulinlab.varnote.config.anno.ab.AbstractQueryParser;
import org.mulinlab.varnote.config.anno.ab.AnnoParser;
import org.mulinlab.varnote.cmdline.txtreader.anno.AnnoConfigReader;
import org.mulinlab.varnote.config.anno.databse.DatabaseBEDParser;
import org.mulinlab.varnote.config.anno.databse.DatabaseVCFParser;
import org.mulinlab.varnote.config.anno.databse.DatabseAnnoConfig;
import org.mulinlab.varnote.config.anno.query.QueryBEDParser;
import org.mulinlab.varnote.config.anno.query.QueryVCFParser;
import org.mulinlab.varnote.config.param.DBParam;
import org.mulinlab.varnote.config.param.output.AnnoOutParam;
import org.mulinlab.varnote.config.param.query.QueryFileParam;
import org.mulinlab.varnote.cmdline.txtreader.anno.OverlapHeaderReader;
import org.mulinlab.varnote.constants.GlobalParameter;
import org.mulinlab.varnote.utils.database.Database;
import org.mulinlab.varnote.utils.enumset.AnnoOutFormat;
import org.mulinlab.varnote.utils.format.Format;
import org.mulinlab.varnote.utils.node.Node;
import org.mulinlab.varnote.utils.node.NodeFactory;
import org.mulinlab.varnote.utils.node.RefNode;
import org.mulinlab.varnote.utils.VannoUtils;

public final class AnnoRunConfig extends OverlapRunConfig{


	private List<AnnoParser> parsers;
	private String overlapFile;
	private boolean forceOverlap = GlobalParameter.DEFAULT_FORCE_OVERLAP;
	private String annoConfig;
	private String vcfHeaderPathForBED;

	public AnnoRunConfig() {
		super();
	}

	public AnnoRunConfig(final String overlapFile) {
		setOverlapFile(overlapFile);
	}

	public AnnoRunConfig(final String queryPath, final String[] dbPaths) {
		super();
		this.overlapFile = null;
		setQueryParam(new QueryFileParam(queryPath));
		setDbParams(dbPaths);
	}

	public AnnoRunConfig(final QueryFileParam query, final List<DBParam> dbConfigs) {
		super();
		this.overlapFile = null;
		setQueryParam(query);
		setDbParams(dbConfigs);
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
			outParam = new AnnoOutParam();
			setOutParam(outParam);
		}
	}

	public void initOther() {
		super.initOther();

		QueryFileParam queryParam = (QueryFileParam)this.queryParam;
		AnnoOutParam outParam = (AnnoOutParam)this.outParam;
		outParam.setDefalutOutFormat(((QueryFileParam)queryParam).getQueryFormat());

		AnnoOutFormat annoOutFormat = outParam.getAnnoOutFormat();
		Integer thread = runParam.getThread();

		//get database anno config
		Map<String, DatabseAnnoConfig> dbAnnoConfigMap = null;
		if(annoConfig == null) annoConfig = queryParam.getQueryPath() + GlobalParameter.ANNO_CONFIG_SUFFIX;
		if(new File(annoConfig).exists()) {
			final List<DatabseAnnoConfig> dbAnnoConfigList = new AnnoConfigReader<List<DatabseAnnoConfig>>().read(annoConfig);
			if(dbAnnoConfigList.size() > 0) {
				dbAnnoConfigMap = new HashMap<String, DatabseAnnoConfig>();
				for (DatabseAnnoConfig config : dbAnnoConfigList) {
					dbAnnoConfigMap.put(config.getLabel(), config);
				}
			}
		}
		
		//set forceOverlap
		queryParam.getQueryFormat().checkRefAndAlt();
		if(!queryParam.getQueryFormat().isRefAndAltExsit()) forceOverlap = true;
		
		DatabseAnnoConfig dnAnnoconfig;
		String dbOutName;
		
		List<Map<String, AbstractDatababseParser>> dbPareserList = new ArrayList<Map<String, AbstractDatababseParser>>();
		for (int i = 0; i < thread; i++) {
			dbPareserList.add(new HashMap<String, AbstractDatababseParser>());
		}

		List<String> databaseNames = new ArrayList<String>();
		for (Database db : databses) {
			dbOutName = db.getConfig().getOutName();
			databaseNames.add(dbOutName);

			logger.info(VannoUtils.printLogHeader("ANNO  CONFIGURATION: " + dbOutName));
			if(dbAnnoConfigMap == null) {
				dnAnnoconfig = new DatabseAnnoConfig(dbOutName); //get all information for annotation
			} else {
				dnAnnoconfig = dbAnnoConfigMap.get(dbOutName);
			}
			if(dnAnnoconfig == null) {
				logger.info(String.format("We can't find annotation config for %s, please check whehter the label is correct.", dbOutName));
			} else dnAnnoconfig.checkRequired();
			
			if(dnAnnoconfig != null)	 {
				dnAnnoconfig.setDBPath(db.getDbPath(), (db.getFormat().getFlags() == Format.VCF_FLAGS), (annoOutFormat == AnnoOutFormat.VCF));
				for (int i = 0; i < thread; i++) {
					if(db.getFormat().getFlags() == Format.VCF_FLAGS) {
						dbPareserList.get(i).put(dbOutName, new DatabaseVCFParser(db, dnAnnoconfig, forceOverlap, annoOutFormat));
					} else {
						dbPareserList.get(i).put(dbOutName, new DatabaseBEDParser(db, dnAnnoconfig, forceOverlap, annoOutFormat));
					}
				}
			}	
			
			if(dbPareserList.size() > 0) {
				if(dbPareserList.get(0).get(dbOutName) != null)
				for (String s : dbPareserList.get(0).get(dbOutName).getLog()) {
					logger.info(s);
				}
			}	
		}
	
		AbstractQueryParser queryParser;
		if(queryParam.getQueryFormat().getFlags() == Format.VCF_FLAGS)  {
			queryParser = new QueryVCFParser(queryParam);
		} else {
			queryParser = new QueryBEDParser(queryParam, vcfHeaderPathForBED);
		}
		
		parsers = new ArrayList<AnnoParser>(thread);
		for (int i = 0; i < thread; i++) {
			parsers.add(new AnnoParser(queryParser, dbPareserList.get(i), databaseNames, annoOutFormat, outParam.isLoj()));
		}
	}


	@Override
	public List<String> getHeader() {
		return parsers.get(runParam.getThread() - 1).getHeader();
	}

	public void mergeResult() {
		super.mergeResult();
	}

	public void printRecord(final Node node, final Map<String, List<String>> results, final int index) throws IOException {
		String r = doQuery(NodeFactory.createRefAlt(node.origStr, ((QueryFileParam)queryParam).getQueryFormat()), results, index);
		printter.print(r, index);
	}
	
	public String doQuery(final RefNode node, final Map<String, List<String>> results, final int index) {
		return parsers.get(index).doQuery(node, results);
	}

	public String getAnnoConfig() {
		return annoConfig;
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
