package bean.config.run;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import postanno.FormatWithRef;
import htsjdk.samtools.util.IOUtil;
import htsjdk.samtools.util.StringUtil;
import joptsimple.OptionParser;
import joptsimple.OptionSet;
import constants.ErrorMsg;
import constants.VannoUtils;
import constants.VannoUtils.PROGRAM;
import bean.config.AnnoDBBean;
import bean.config.anno.AbstractParser;
import bean.config.anno.BEDParserDB;
import bean.config.anno.BEDParserQuery;
import bean.config.anno.VCFParserDB;
import bean.config.anno.VCFParserQuery;
import bean.config.readconfig.AnnoConfigReadConfig;
import bean.config.readconfig.AnnoRunReadConfig;
import bean.config.readconfig.OverlapHeaderReadConfig;

public class AnnoRunBean extends AbstractRunCongig {
	public enum FileFormatSupport {
		VCF,
		BED
	}
	
	private boolean isOverlapFile;
	
	private String overlapFile;
	private OverlapRunBean overlapBean;

	private String outputFile;
	private FileFormatSupport annoOutFormat;
	
	private List<String> dbNames;	
	private Map<String, AnnoDBBean> dbsMap;
	
	private String annoConfig;
	private String headerPath;
	private boolean forceOverlap;
	
	private AbstractParser queryParser;
	private Map<String, AbstractParser> dbPareser;
	
	public AnnoRunBean() {
		super();
		init();
	}
	
	private void init() {
		pro = PROGRAM.ANNO;
		forceOverlap = false;
		annoOutFormat = FileFormatSupport.VCF;
		isOverlapFile = false;
	}
	
	public AnnoRunBean(final OptionSet options) {
		this();
		if(options.has("overlap-file")) {
			 setOverlapFile((String) options.valueOf("overlap-file"));
			 isOverlapFile = true;
		} else {
			 overlapBean = new OverlapRunBean(options);
		}

		if(options.has("force-overlap")) setForceOverlap((String)options.valueOf("force-overlap"));
		if(options.has("anno-format")) setAnnoOutFormat((String)options.valueOf("anno-format")); 
		if(options.has("anno-out")) setOutputFile((String)options.valueOf("anno-out"));
		if(options.has("header-path")) setHeaderPath((String) options.valueOf("header-path"));
		if(options.has("anno-config")) setAnnoConfig((String)options.valueOf("anno-config"));
	}
	
	public void readConfig() {
		if(outputFile == null) {
			outputFile = overlapFile + ".anno";
			System.out.println("Output file is not defined, redirect output file path to: " + outputFile);
		}
		VannoUtils.assertOutputIsValid(outputFile);
		
		System.out.println("\n=============== Reading query file ================");
		final List<DBRunConfigBean> dbs;

		if(overlapBean != null) {
			queryFile = overlapBean.getQueryFile();
			queryFormat = overlapBean.getQueryFormat();
			queryColumns = overlapBean.getQueryColumns();
			dbs = overlapBean.getDbs();
			
		} else {
			OverlapHeaderReadConfig<OverlapHeaderBean> readFromConfig = new OverlapHeaderReadConfig<OverlapHeaderBean>();
			OverlapHeaderBean overlapHeader = readFromConfig.read(new OverlapHeaderBean(), overlapFile);
			
			queryFile = overlapHeader.getQueryFile();
			queryFormat = overlapHeader.getQueryFormat();
			queryColumns = overlapHeader.getQueryColumns();
			dbs = overlapHeader.getDbs();
		}
		if(queryFormat.flag != FormatWithRef.VCF_FORMAT) System.out.println("Query Column Name is:" + ((queryColumns == null) ? "" : StringUtil.join(", ", queryColumns)));
		
		//new query parser
		if(queryFormat.flag == FormatWithRef.VCF_FORMAT) {
			if(headerPath != null) {
				queryParser = new VCFParserQuery(queryFormat, headerPath);
			} else {
				queryParser = new VCFParserQuery(queryFormat, queryFile);
			}
		} else {
			if(queryColumns != null) {
				queryParser = new BEDParserQuery(queryFormat, queryColumns);
			} else {
				queryParser = new BEDParserQuery(queryFormat, new File(queryFile));
			}
			
			if(headerPath != null) ((BEDParserQuery)queryParser).setVcfHeaderPath(headerPath);
		}
		
	//	System.out.println("\n============ Reading database file ================");
		setDBList();
		
		//new db parsers
		dbPareser = new HashMap<String, AbstractParser>();
		
		final Map<String, DBRunConfigBean> dbConfigMap = new HashMap<String, DBRunConfigBean>();
		for (DBRunConfigBean dbRunConfigBean : dbs) {
			dbConfigMap.put(dbRunConfigBean.getLable(), dbRunConfigBean);
		}
		
		AnnoDBBean db;
		DBRunConfigBean dbConfig;

		for (String db_name : dbNames) {
			System.out.println("====== Reading configuration for: " +  db_name + " ======");
			dbConfig = dbConfigMap.get(db_name);
			if(dbConfig == null) throw new IllegalArgumentException("We can't find database file for " + db_name + ", please check whehter the label is correct.");
			
			db = dbsMap.get(db_name);
			db.setDbPath(dbConfig.getDbPath());
			db.setFormat(dbConfig.getDbFormat());
			
			db.checkRequired();
			if(db.getFormat().flag == FormatWithRef.VCF_FORMAT) {
				dbPareser.put(db_name, new VCFParserDB(db, forceOverlap));
			} else {
				if(db.getColumnNames() == null) {
					dbPareser.put(db_name, new BEDParserDB(db, new File(db.getDbPath()), forceOverlap));
				} else {
					dbPareser.put(db_name, new BEDParserDB(db, db.getColumnNames(), forceOverlap));
				}
			}
		}
	}
	
	@Override
	public void printLog() {
		System.out.println("\n===================== Annotation =================");
		System.out.println("Overlap File:" + overlapFile);
//		System.out.println("Annotation Config:" + annoConfig.);
		System.out.println("Force Overlap:" + forceOverlap);
		System.out.println("Out Format:" + annoOutFormat);
	}

	
	public String getOverlapFile() {
		return overlapFile;
	}

	public void setOverlapFile(String overlapFile) {
		IOUtil.assertInputIsValid(overlapFile);
		this.overlapFile = overlapFile;
	}

	public List<String> getDbNames() {
		return dbNames;
	}

	public Map<String, AnnoDBBean> getDbsMap() {
		return dbsMap;
	}

	public void setDBList() {
		if(dbNames == null) {
			dbNames = new ArrayList<String>();
			dbsMap = new HashMap<String, AnnoDBBean>();
			
			AnnoConfigReadConfig<List<AnnoDBBean>> readFromConfig = new AnnoConfigReadConfig<List<AnnoDBBean>>();
			
			if(annoConfig == null) 
				annoConfig = queryFile + ".anno";
	
			if(!new File(annoConfig).exists()) throw new IllegalArgumentException("Annotation configuration file is required, default path is " + annoConfig );
			final List<AnnoDBBean>dbList = readFromConfig.read(new ArrayList<AnnoDBBean>(), annoConfig);
			for (AnnoDBBean annoDBBean : dbList) {
				dbNames.add(annoDBBean.getLabel());
				dbsMap.put(annoDBBean.getLabel(), annoDBBean);
			}
		}
	}

	public FileFormatSupport getAnnoOutFormat() {
		return annoOutFormat;
	}

	public void setAnnoOutFormat(String annoOutFormat) {
		this.annoOutFormat = VannoUtils.checkFileFormat(annoOutFormat);
	}

	public static OptionParser getParserForIndex(OptionParser parser) {
//		OptionParser parser = new OptionParser();
		parser.accepts("help", ErrorMsg.PRINT_HELP);
		parser.acceptsAll(Arrays.asList("l", "overlap-file"), ErrorMsg.VANNO_RUN_OL).withRequiredArg().describedAs("Overlap File Path").ofType(String.class);
		parser.acceptsAll(Arrays.asList("a", "anno-config"), ErrorMsg.VANNO_RUN_ANNO).withRequiredArg().describedAs("Annotation Config Path").ofType(String.class); 
		
		parser.acceptsAll(Arrays.asList("r", "force-overlap"), "Force Overlap").withOptionalArg().describedAs("Force Overlap").ofType(String.class); 
//		parser.accepts("query-columns", "Query Column Name").withOptionalArg().describedAs("Query Column Name").withValuesSeparatedBy(',').ofType(String.class); 
		parser.acceptsAll(Arrays.asList("h", "header-path"), "Header Path").withOptionalArg().describedAs("Header Path").ofType(String.class); 
		parser.acceptsAll(Arrays.asList("u", "anno-format"), "Annotation Output File Format").withOptionalArg().describedAs("Annotation Output File Format").ofType(String.class);
		parser.accepts("anno-out", "Annotation Out File Path").withOptionalArg().describedAs("Annotation Out File Path").ofType(String.class); 
		parser.accepts("version", ErrorMsg.PRINT_VERSION);
		return parser;
	}

	public boolean isForceOverlap() {
		return forceOverlap;
	}

	public AbstractParser getQueryParser() {
		return queryParser;
	}

	public Map<String, AbstractParser> getDbPareser() {
		return dbPareser;
	}

	public String getOutputFile() {
		return outputFile;
	}

	public void setOutputFile(String outputFile) {
		this.outputFile = outputFile;
	}

	public void setAnnoOutFormat(FileFormatSupport annoOutFormat) {
		this.annoOutFormat = annoOutFormat;
	}
	
	public void setForceOverlap(boolean forceOverlap) {
		this.forceOverlap = forceOverlap;
	}
	
	public void setForceOverlap(String forceOverlap) {
		this.forceOverlap = VannoUtils.strToBool(forceOverlap);
	}

	public void setQueryParser(AbstractParser queryParser) {
		this.queryParser = queryParser;
	}

	public void setHeaderPath(String headerPath) {
		IOUtil.assertInputIsValid(headerPath);
		this.headerPath = headerPath;
	}

	public boolean isOverlapFile() {
		return isOverlapFile;
	}

	public OverlapRunBean getOverlapBean() {
		return overlapBean;
	}

	public void setAnnoConfig(String annoConfig) {
		IOUtil.assertInputIsValid(annoConfig);
		this.annoConfig = annoConfig;
	}

	public void setOverlapFile(boolean isOverlapFile) {
		this.isOverlapFile = isOverlapFile;
	}
	
	public void setOverlapBean(OverlapRunBean overlapBean) {
		this.overlapBean = overlapBean;
	}

	public static AnnoRunBean readAnnoRunBean(final String configFile) {
		AnnoRunReadConfig<AnnoRunBean> readFromConfig = new AnnoRunReadConfig<AnnoRunBean>();
		AnnoRunBean bean = new AnnoRunBean();
		readFromConfig.read(bean, configFile);
		return bean;
	}
}
