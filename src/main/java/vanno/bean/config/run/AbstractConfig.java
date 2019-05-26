package main.java.vanno.bean.config.run;

import java.util.ArrayList;
import java.util.List;
import main.java.vanno.bean.config.run.AnnoRunConfig.AnnoOutFormat;
import main.java.vanno.bean.config.run.OverlapRunConfig.Mode;
import main.java.vanno.bean.database.Database;
import main.java.vanno.bean.database.DatabaseConfig;
import main.java.vanno.bean.database.DatabaseFactory;
import main.java.vanno.bean.query.Log;
import main.java.vanno.bean.query.Output;
import main.java.vanno.constants.BasicUtils;
import main.java.vanno.constants.InvalidArgumentException;
import main.java.vanno.constants.VannoUtils;
import main.java.vanno.constants.VannoUtils.PROGRAM;

public abstract class AbstractConfig {
	protected final static Log defaultLog = BasicUtils.DEFAULT_LOG;
	protected final static boolean defaultUseJDK = BasicUtils.DEFAULT_USE_JDK;
	protected final static boolean deafultIsCount = BasicUtils.DEFAULT_IS_COUNT;
	protected final static boolean deafultIsGzip = BasicUtils.DEFAULT_IS_GZIP;
	protected final static Output  deafultOutput = BasicUtils.DEFAULT_OUTPUT;
	protected final static Mode    deafultMode = BasicUtils.DEFAULT_MODE;
	protected final static int     deafultThread = BasicUtils.DEFAULT_THREAD;
	protected final static boolean deafultDisplayLabel = BasicUtils.DEFAULT_DISPLAY_LABEL;
	protected final static boolean defaultLoj = BasicUtils.DEFAULT_LOJ;
	
	protected final static String  deafultAnnoConfig = BasicUtils.DEFAULT_ANNO_CONFIG;
	protected final static String  deafultAnnoOut = BasicUtils.DEFAULT_ANNO_OUT;
	protected final static String  deafultVCFForBED = BasicUtils.DEFAULT_VCF_FOR_BED;
	protected final static boolean deafultForceOverlap = BasicUtils.DEFAULT_FORCE_OVERLAP;
	protected final static AnnoOutFormat deafultAnnoOutFormat = BasicUtils.DEFAULT_ANNO_OUT_FORMAT;
	
	protected List<Database> databses;
	protected List<DatabaseConfig> dbConfigs;
	protected boolean useJDK;
	protected PROGRAM program;
	protected Log log;
	
	protected AbstractConfig() {
	}
	
	public AbstractConfig(final String[] dbPaths) {
		this(dbPaths, defaultUseJDK, defaultLog);
	}

	public AbstractConfig(final String[] dbPaths, final boolean useJDK, final Log log) {
		List<DatabaseConfig> dbConfigs = new ArrayList<DatabaseConfig>();
		for (String dbPath : dbPaths) {
			dbConfigs.add(new DatabaseConfig(dbPath));
		}
		this.dbConfigs = dbConfigs;
		this.useJDK = useJDK;
		this.log = log;
	}
	
	protected AbstractConfig(final List<DatabaseConfig> dbConfigs, final boolean useJDK, final Log log) { 
		this.dbConfigs = dbConfigs;
		this.useJDK = useJDK;
		this.log = log;
	}
	
	public void init() {
		initQuery();
		initDB();
		initOther();
		checkAll();
	}
	
	protected abstract void initQuery();
	protected abstract void initDB();
	protected abstract void initOther();
	
	protected void checkAll() {
		databses = DatabaseFactory.readDatabaseFromConfig(dbConfigs, useJDK, log);
		if(databses.size() == 0) throw new InvalidArgumentException("At least one db is required.");
	}
	
	public boolean isUseJDK() {
		return useJDK;
	}
	
	public List<Database> getDatabses() {
		return databses;
	}

	public Log getLog() {
		return log;
	}

	public void setLog(Log log) {
		this.log = log;
	}

	public void setDatabses(List<Database> databses) {
		this.databses = databses;
	}

	public void setDbConfigs(List<DatabaseConfig> dbConfigs) {
		this.dbConfigs = dbConfigs;
	}

	public void setUseJDK(boolean useJDK) {
		this.useJDK = useJDK;
	}
	
	public void setUseJDK(String useJDK) {
		this.useJDK = VannoUtils.strToBool(useJDK);
	}

	public List<DatabaseConfig> getDbConfigs() {
		return dbConfigs;
	}

	public PROGRAM getProgram() {
		return program;
	}

	public void setProgram(PROGRAM program) {
		this.program = program;
	}
	
	
}
