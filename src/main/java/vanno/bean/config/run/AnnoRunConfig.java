package main.java.vanno.bean.config.run;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.channels.WritableByteChannel;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import htsjdk.samtools.util.BlockCompressedOutputStream;
import htsjdk.samtools.util.IOUtil;
import htsjdk.tribble.util.LittleEndianOutputStream;
import main.java.vanno.bean.config.anno.ab.AbstractDatababseParser;
import main.java.vanno.bean.config.anno.ab.AbstractQueryParser;
import main.java.vanno.bean.config.anno.ab.AnnoParser;
import main.java.vanno.bean.config.anno.databse.DatabaseAnnoRead;
import main.java.vanno.bean.config.anno.databse.DatabaseBEDParser;
import main.java.vanno.bean.config.anno.databse.DatabaseVCFParser;
import main.java.vanno.bean.config.anno.databse.DatabseAnnoConfig;
import main.java.vanno.bean.config.anno.query.QueryBEDParser;
import main.java.vanno.bean.config.anno.query.QueryVCFParser;
import main.java.vanno.bean.config.config.run.OverlapHeaderReadConfig;
import main.java.vanno.bean.database.Database;
import main.java.vanno.bean.database.DatabaseConfig;
import main.java.vanno.bean.format.Format;
import main.java.vanno.bean.node.Node;
import main.java.vanno.bean.node.NodeFactory;
import main.java.vanno.bean.node.RefNode;
import main.java.vanno.bean.query.Log;
import main.java.vanno.bean.query.Output;
import main.java.vanno.bean.query.Query;
import main.java.vanno.constants.BasicUtils;
import main.java.vanno.constants.VannoUtils;
import main.java.vanno.constants.VannoUtils.PROGRAM;

public final class AnnoRunConfig extends OverlapRunConfig{
	public enum AnnoOutFormat {
		VCF,
		BED
	}
	
	private AnnoOutFormat annoOutFormat;
	private String annoConfig;
	private boolean forceOverlap;
	private String vcfHeaderPathForBED;
	private List<AnnoParser> parsers;
	private String overlapFile;
	
	public AnnoRunConfig() {	
	}
	
	public AnnoRunConfig(final String overlapFile) {
		setOverlapFile(overlapFile, defaultLog, deafultIsGzip, defaultLoj);
	}
	
	public AnnoRunConfig(final String overlapFile, final Log log, final boolean isGzip, final boolean isLoj) {
		setOverlapFile(overlapFile, log, isGzip, isLoj);
	}

	public AnnoRunConfig(final Query query, final List<DatabaseConfig> dbConfigs) {
		this(query, dbConfigs, deafultOutput, defaultLog, deafultIsGzip);
	}
	
	public AnnoRunConfig(final Query query, final List<DatabaseConfig> dbConfigs, final Log log, final boolean isGzip) {
		this(query, dbConfigs, deafultOutput, log, isGzip);
	}
	
	public AnnoRunConfig(final Query query, final List<DatabaseConfig> dbConfigs, final Output output, final Log log, final boolean isGzip) {
		this(query, dbConfigs, deafultMode, deafultThread, deafultIsCount, defaultUseJDK, output, deafultAnnoOutFormat, deafultForceOverlap, deafultAnnoConfig, deafultAnnoOut, deafultVCFForBED, log, isGzip);
	} 
	
	public AnnoRunConfig(Query query, List<DatabaseConfig> dbConfigs, Mode mode, int thread, boolean isCount,
			boolean useJDK, Output output, final Log log, final boolean isGzip) {
		this(query, dbConfigs, mode, thread, isCount, useJDK, output, deafultAnnoOutFormat, deafultForceOverlap, deafultAnnoConfig, deafultAnnoOut, deafultVCFForBED, log, isGzip);
	}

	public AnnoRunConfig(Query query, List<DatabaseConfig> dbConfigs, Mode mode, int thread, boolean isCount,
			boolean useJDK, Output output, AnnoOutFormat annoOutFormat, boolean forceOverlap, String annoConfig, String annoOutputFile,
			String vcfHeaderPathForBED, final Log log, final boolean isGzip) {
		super(query, dbConfigs, mode, thread, isCount, useJDK, output, log, isGzip);
		
		this.overlapFile = null;
		this.program = PROGRAM.ANNO;
		this.annoOutFormat = annoOutFormat;
		this.forceOverlap = forceOverlap;

		if(annoConfig != null) setAnnoConfig(annoConfig);
		if(annoOutputFile != null) output.setAnnoOutput(annoOutputFile);
		if(vcfHeaderPathForBED != null) setVcfHeaderPathForBED(vcfHeaderPathForBED);
	}
	
	public void init() {
		super.init();

		//out format
		if(annoOutFormat == null) 
			if(query.getQueryFormat().getFlags() == Format.VCF_FLAGS) 
				this.annoOutFormat = AnnoOutFormat.VCF;
			else this.annoOutFormat = AnnoOutFormat.BED;
		
		log.printKVKCYN("Force Overlap", forceOverlap + "");
		log.printKVKCYN("Out Format", annoOutFormat + "");
		
		//output
		if((output != null) && (output.getAnnoOutput() == null)) output.setDefaultAnnoOutput(log);
		
		//get database anno config
		Map<String, DatabseAnnoConfig> dbAnnoConfigMap = null;
		if(annoConfig == null) annoConfig = query.getQueryPath() + BasicUtils.ANNO_CONFIG_SUFFIX;
		if(new File(annoConfig).exists()) {
			final List<DatabseAnnoConfig> dbAnnoConfigList = new DatabaseAnnoRead<List<DatabseAnnoConfig>>().read(annoConfig);
			if(dbAnnoConfigList.size() > 0) {
				dbAnnoConfigMap = new HashMap<String, DatabseAnnoConfig>();
				for (DatabseAnnoConfig config : dbAnnoConfigList) {
					dbAnnoConfigMap.put(config.getLabel(), config);
				}
			}
		}
		
		//set forceOverlap
		query.getQueryFormat().checkRefAndAlt();
		if(!query.getQueryFormat().isRefAndAltExsit()) forceOverlap = true;
		
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
	
			log.printStrWhite("\n\n----------------------------------------------------  READING  CONFIGURATION: " + dbOutName + "  ------------------------------------");
			
			if(dbAnnoConfigMap == null) {
				dnAnnoconfig = new DatabseAnnoConfig(dbOutName); //get all information for annotation
			} else {
				dnAnnoconfig = dbAnnoConfigMap.get(dbOutName);
			}
			if(dnAnnoconfig == null) {
				log.printStrNon("We can't find annotation config for " + dbOutName + ", please check whehter the label is correct.");
			}
			else dnAnnoconfig.checkRequired();
			
			if(dnAnnoconfig != null)	 {
				dnAnnoconfig.setDBPath(db.getDbPath(), (db.getFormat().getFlags() == Format.VCF_FLAGS), (annoOutFormat == AnnoOutFormat.VCF));
				for (int i = 0; i < thread; i++) {
					if(db.getFormat().getFlags() == Format.VCF_FLAGS) {
						dbPareserList.get(i).put(dbOutName, new DatabaseVCFParser(db, dnAnnoconfig, forceOverlap, annoOutFormat, log, useJDK));
					} else {
						dbPareserList.get(i).put(dbOutName, new DatabaseBEDParser(db, dnAnnoconfig, forceOverlap, annoOutFormat));
					}
				}
			}	
			
			if(dbPareserList.size() > 0) {
				if(dbPareserList.get(0).get(dbOutName) != null)
				for (String s : dbPareserList.get(0).get(dbOutName).getLog()) {
					log.printStrSystemOri(s);
				}
			}	
		}
	
		AbstractQueryParser queryParser;
		if(query.getQueryFormat().getFlags() == Format.VCF_FLAGS)  {
			queryParser = new QueryVCFParser(query, log, useJDK);
		} else {
			queryParser = new QueryBEDParser(query, vcfHeaderPathForBED);
		}
		
		parsers = new ArrayList<AnnoParser>(thread);
		for (int i = 0; i < thread; i++) {
			parsers.add(new AnnoParser(queryParser, dbPareserList.get(i), databaseNames, annoOutFormat, output.isLoj()));
		}
	}
	
	
	public List<String> getHeader() {
		return parsers.get(thread - 1).getHeader();
	}
	
	@Override
	public void mergeResult() {
		if(!isCount && output != null) {
			try {
				if(isGzip) {
					LittleEndianOutputStream out = new LittleEndianOutputStream(new BlockCompressedOutputStream(new File(output.getAnnoOutput())));
					output.printAnnoHeader(out, getHeader());
					output.mergeTempZipFile(out, log);
					out.close();
				} else {
					WritableByteChannel outChannel = output.initChannels(output.getAnnoOutput());
					output.printAnnoHeader(outChannel, getHeader());
					output.mergeTempFile(outChannel, log);
					outChannel.close();
				}
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	@Override
	public void printRecord(final Node node, final Map<String, List<String>> results, final int index) throws IOException {
		output.print(doQuery(NodeFactory.createRefAlt(node.origStr, query.getQueryFormat()), results, index), index); 
	}
	
	public String doQuery(final RefNode node, final Map<String, List<String>> results, final int index) {
		return parsers.get(index).doQuery(node, results);
	}
	
	public void setAnnoOutFormat(final String _annoOutFormat) {
		setAnnoOutFormat(VannoUtils.checkFileFormat(_annoOutFormat));
	}
	
	public void setAnnoOutFormat(final AnnoOutFormat annoOutFormat) {
		this.annoOutFormat = annoOutFormat;
	}

	public String getAnnoConfig() {
		return annoConfig;
	}

	public void setAnnoConfig(final String annoConfig) {
		IOUtil.assertInputIsValid(annoConfig);
		this.annoConfig = annoConfig;
	}

	public boolean isForceOverlap() {
		return forceOverlap;
	}

	public void setForceOverlap(final boolean forceOverlap) {
		this.forceOverlap = forceOverlap;
	}
	
	public void setForceOverlap(final String forceOverlap) {
		this.forceOverlap = VannoUtils.strToBool(forceOverlap);
	}

	public void setVcfHeaderPathForBED(String vcfHeaderPathForBED) {
		IOUtil.assertInputIsValid(vcfHeaderPathForBED);
		this.vcfHeaderPathForBED = vcfHeaderPathForBED;
	}

	public void setOverlapFile(final String overlapFile, final Log log, final boolean isGzip, final boolean isLoj) {
		IOUtil.assertInputIsValid(overlapFile);
		this.overlapFile = overlapFile;

		AnnoRunConfig newrunConfig = new OverlapHeaderReadConfig<AnnoRunConfig>().read(overlapFile);

		setQuery(newrunConfig.getQuery());
		setDbConfigs(newrunConfig.getDbConfigs());
		newrunConfig.getOutput().setLoj(isLoj);
		setOutput(newrunConfig.getOutput());
		setCount(false);
		setLog(log);
		setGzip(isGzip);
	}

	public String getOverlapFile() {
		return overlapFile;
	}
	
}
