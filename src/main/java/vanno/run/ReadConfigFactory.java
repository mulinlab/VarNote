package main.java.vanno.run;
import java.util.ArrayList;
import java.util.List;
import joptsimple.OptionSet;
import main.java.vanno.bean.config.config.run.AnnoRunReadConfig;
import main.java.vanno.bean.config.config.run.OverlapReadConfig;
import main.java.vanno.bean.config.run.AnnoRunConfig;
import main.java.vanno.bean.config.run.OverlapRunConfig;
import main.java.vanno.bean.config.run.QueryRunConfig;
import main.java.vanno.bean.database.DatabaseConfig;
import main.java.vanno.bean.format.Format;
import main.java.vanno.bean.query.Log;
import main.java.vanno.bean.query.Output;
import main.java.vanno.bean.query.Query;
import main.java.vanno.constants.BasicUtils;
import main.java.vanno.constants.VannoUtils;
import main.java.vanno.constants.VannoUtils.PROGRAM;
import main.java.vanno.index.IndexWriteConfig;

public final class ReadConfigFactory {
	private final static boolean defaultGZIP = BasicUtils.DEFAULT_IS_GZIP;
	private final static Log defaultLog = BasicUtils.DEFAULT_LOG;
	private final static boolean defaultLoj = BasicUtils.DEFAULT_LOJ;
	private final static boolean defaultUseJDK = BasicUtils.DEFAULT_USE_JDK;
	private final static boolean deafultIsCount = BasicUtils.DEFAULT_IS_COUNT;
	private final static Output  deafultOutput = BasicUtils.DEFAULT_OUTPUT;
	private final static boolean deafultDisplayLabel = BasicUtils.DEFAULT_DISPLAY_LABEL;
	
	public static QueryRunConfig readQueryConfig(final OptionSet options) {
		String[] requiredOPT = { "q-region", "d-file" };
		VannoUtils.checkMissing(options, requiredOPT);
		List<DatabaseConfig> dbConfigs = getDBConfigs(options);
		
		boolean useJDK = defaultUseJDK;
		Log log = defaultLog;
		if(options.has("use-jdk-inflater")) useJDK = true;
		if(options.has("verbose")) log.setVerbose(true);
		
		boolean displayLabel = deafultDisplayLabel;
		if(options.has("label")) displayLabel = true;
		
		return new QueryRunConfig((String)options.valueOf("q-region"), dbConfigs, useJDK, displayLabel, log);
	}
	
	public static OverlapRunConfig readConfig(final OptionSet options, final PROGRAM pro) {
		boolean isCount = deafultIsCount;
		Log log = defaultLog;
		boolean isGzip = defaultGZIP, isloj = defaultLoj;
		
		OverlapRunConfig runConfig;
		if(options.has("config")) {
			if(pro == PROGRAM.OVERLAP) {
				runConfig = new OverlapReadConfig<OverlapRunConfig>().read( (String)options.valueOf("config") );
			} else {
				runConfig = new AnnoRunReadConfig<AnnoRunConfig>().read((String)options.valueOf("config"));
			}
		} else {
			if(pro == PROGRAM.ANNO && options.has("overlap-file")) {
				runConfig = new AnnoRunConfig();
				if(options.has("verbose")) log.setVerbose(true);
				if(options.has("unzip")) isGzip = false;
				if(options.has("loj")) isloj = true; 
				((AnnoRunConfig)runConfig).setOverlapFile((String)options.valueOf("overlap-file"), log, isGzip, isloj);	
				
			} else {
				checkRequired(options);
				Query query = getQuery(options);
				List<DatabaseConfig> dbConfigs = getDBConfigs(options);
				
				if(options.has("count")) isCount = true; 
				if(options.has("verbose")) log.setVerbose(true);
				if(options.has("unzip")) isGzip = false;
				
				//output
				String outputFolder = null;
				if(options.has("out-floder")) outputFolder = (String)options.valueOf("out-floder");
				Output output = deafultOutput;
				if(!isCount) {
					output = new Output(outputFolder, dbConfigs, query);
					if(options.has("loj")) output.setLoj(true); 
				    if(options.has("out-mode")) output.setOutputMode((Integer) options.valueOf("out-mode"));
				    if(options.has("remove-comment")) output.setRemoveCommemt(true);
				}
				if(pro == PROGRAM.OVERLAP) {
					runConfig = new OverlapRunConfig(query, dbConfigs, output, log, isGzip);
				} else {
					runConfig = new AnnoRunConfig(query, dbConfigs, output, log, isGzip);
				}
			}
			
			if(pro == PROGRAM.ANNO) {
				if(options.has("force-overlap")) ((AnnoRunConfig)runConfig).setForceOverlap(true);  
				if(options.has("anno-format")) ((AnnoRunConfig)runConfig).setAnnoOutFormat((String)options.valueOf("anno-format")); 
				if(!isCount) {
					if(options.has("anno-out")) ((AnnoRunConfig)runConfig).getOutput().setAnnoOutput((String)options.valueOf("anno-out")); 
				}
				if(options.has("anno-config")) ((AnnoRunConfig)runConfig).setAnnoConfig((String)options.valueOf("anno-config")); 
				if(options.has("vcf-header-for-bed")) ((AnnoRunConfig)runConfig).setVcfHeaderPathForBED((String) options.valueOf("vcf-header-for-bed"));
			}

		    if(options.has("thread")) runConfig.setThread((Integer) options.valueOf("thread"));
		    if(options.has("mode")) runConfig.setMode((Integer) options.valueOf("mode"));
			
			if(options.has("use-jdk-inflater")) runConfig.setUseJDK("true");
		    if(isCount) runConfig.setCount("true");
			
		}
		
		return runConfig;
	}
	
	public static IndexWriteConfig readIndexConfig(final OptionSet options) {
		String[] requiredOPT = {"input"};
		VannoUtils.checkMissing(options, requiredOPT);
        
		IndexWriteConfig config = new IndexWriteConfig((String) options.valueOf("input"));
        
		if(options.has("print-header") || options.has("list-chroms") || options.has("replace-header") || options.has("print-meta-data")) {
			if(options.has("print-meta-data")) {
				config.printMetaData();
			}
			
			if(options.has("print-header")) {
				config.printHeader();
			}
			
			if(options.has("list-chroms")) {
				config.listChromsome();
			}
			
			if(options.has("replace-header")) {
				config.replaceHeader((String)options.valueOf("replace-header"));
			}
			return null;
		} else {
	        config.setFormat(readFormatFromOptionSet(config.getInput(), options, false));
	        
	        if(options.has("o")) {
	        		config.setOutput( (String)options.valueOf("o"));
	        }

	        if(options.has("use-jdk-deflater")) config.setUseJDK(true);
	        return config;
		}
	}
	
	public static void checkRequired(final OptionSet options) {
		String[] requiredOPT = { "q-file", "d-file" };
		VannoUtils.checkMissing(options, requiredOPT);
	}
	
	public static Query getQuery(final OptionSet options) {
		Query query = new Query((String) options.valueOf("q-file"));
		Format format = readFormatFromOptionSet(query.getQueryPath(), options, true);
		query.setQueryFormat(format);
		return query;
	}
	
	public static List<DatabaseConfig> getDBConfigs(final OptionSet options) {
		
		//database
		@SuppressWarnings("unchecked")
		final List<String> db = (List<String>) options.valuesOf("d-file");
		List<String> dbIndex = null, dbOutName = null, dbIntersect = null;
		
		List<DatabaseConfig> dbConfigs = new ArrayList<DatabaseConfig>();
		if(db != null && db.size() > 0) {
			dbIndex = VannoUtils.getOptionList(options, "d-index", db.size(), " index type (tbi or vanno)");
			dbOutName = VannoUtils.getOptionList(options, "d-label", db.size(), " database output name");
			dbIntersect = VannoUtils.getOptionList(options, "d-type", db.size(), " intersection type");
			
			for (int i = 0; i < db.size(); i++) {
				dbConfigs.add(new DatabaseConfig(db.get(i), 
						(dbOutName == null) ? null : dbOutName.get(i), 
						(dbIntersect == null) ? null : dbIntersect.get(i), 
						(dbIndex == null) ? null : dbIndex.get(i)));
			}
		}
		return dbConfigs;
	}
	
	public static Format readFormatFromOptionSet(final String input, final OptionSet options, final boolean isQuery) {
		Format format;
		if (options.has("format")) format = VannoUtils.parseIndexFileFormat((String) options.valueOf("format"));
		else format = Format.defaultFormat(input, isQuery);
		
		if (options.has("comment-indicator")) format.setCommentIndicator((String) options.valueOf("comment-indicator")); 
		if (options.has("header")) format.setHasHeader(true);
		
		if (options.has("zero-based") ) format.setZeroBased(); 
		if (options.has("chrom")) format.setSequenceColumn((Integer) options.valueOf("chrom")); 
		if (options.has("begin")) format.setStartPositionColumn((Integer) options.valueOf("begin"));
		if (options.has("end")) format.setEndPositionColumn((Integer) options.valueOf("end")); 
		if (options.has("ref")) format.setRefColumn((Integer) options.valueOf("ref")); 
		if (options.has("alt")) format.setAltColumn((Integer) options.valueOf("alt")); 
		
		if (options.has("header-path")) format.setHeaderPath((String) options.valueOf("header-path"));
		if(options.has("skip")) format.setNumHeaderLinesToSkip((Integer)options.valueOf("skip"));
		
		return format;
	}
}
