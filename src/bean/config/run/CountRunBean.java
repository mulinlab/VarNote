package bean.config.run;


import java.util.Arrays;
import java.util.List;

import bean.config.readconfig.AbstractReadConfig;
import bean.config.readconfig.CountReadConfig;
import bean.config.run.DBRunConfigBean.IndexFormat;
import joptsimple.OptionParser;
import joptsimple.OptionSet;
import constants.ErrorMsg;
import constants.VannoUtils;
import constants.VannoUtils.PROGRAM;

public class CountRunBean extends AbstractRunCongig{
	public enum Mode {
		TABIX,
		MIX,
		SWEEP
	}
	protected Mode mode;
	protected int thread;
	protected boolean exact;
	protected boolean fc;

	protected List<String> dbIndex = null;
	protected List<String> dbLabel = null;
	protected List<String> db = null;
	
	public CountRunBean() {
		super();
		init();
	}
	
	private void init() {
		pro = PROGRAM.COUNT;
		thread = 1;
		fc = false;
		exact = false;
		mode = Mode.MIX;
	}
	

	public CountRunBean(OptionSet options) {
		super(options);
		init();
		setMode(options);    	 
		setDB(options);
		if(options.has("thread")) setThread((Integer) options.valueOf("thread"));
		if(options.has("exact")) setExact("true");
		if(options.has("fc")) setFc("true");
	}
	
	@SuppressWarnings("unchecked")
	public void setDB(final OptionSet options) {
		db = (List<String>) options.valuesOf("d-file");
		initIndex(options);
        for (int i = 0; i < db.size(); i++) {
       	 	cdb = new DBRunConfigBean();
       	    setDBIndex(i, options);
 	    	cdb.setDb(db.get(i));
 	    	 
 	    	VannoUtils.addFileForPattern(cdb, dbs);
        }
	}
	
	public void setMode(final OptionSet options) {
		mode = Mode.MIX;
	}
	
	public void initIndex(final OptionSet options) {
		
	}
	
	public void setDBIndex(final int i, final OptionSet options) {
		cdb.setIndexFormat(IndexFormat.PLUS);
	}
	
	public void newDB() {
		addDB();
		cdb = new DBRunConfigBean();
	}
	
	public void setDBPath(final String val) {
		checkDB(AbstractReadConfig.DB_BEGIN);
		cdb.setDb(val);
	}

	public Mode getMode() {
		return mode;
	}

	public void setMode(int mode) {
		this.mode = VannoUtils.checkMode(mode);
	}

	public int getThread() {
		return thread;
	}

	public void setThread(int thread) {
		if(thread < 1) 
			throw new IllegalArgumentException("Thread should not less than 1.");

		this.thread = thread;
	}

	public boolean isExact() {
		return exact;
	}

	public void setExact(String exact) {
		this.exact = VannoUtils.strToBool(exact);
	}

	public boolean isFc() {
		return fc;
	}

	public void setFc(String fc) {
		this.fc = VannoUtils.strToBool(fc);
	}

	@Override
	public void printLog() {
		System.out.println("\n===================== Query =================");
		System.out.println("Query File Path:" + queryFile);
		System.out.println("Query Columns:" + queryColumns);
		
		for (DBRunConfigBean db : dbs) {
			System.out.println("======================== DB ===================");
		    System.out.println("Using database file:" + db.getDbPath());
		    System.out.println("Using index file:" + db.getDbIndex());
		    System.out.println("");
		}

		printMode();
		System.out.println("Thread number:" + thread);
		System.out.println("Exact:" + exact);
	}
	
	public void printMode() {
		System.out.println("Using mode: MIX (Count program only support mode mix)");
	}
	
	public static OptionParser getParserForIndex() {
		OptionParser parser = new OptionParser();
		parser.accepts("help", ErrorMsg.PRINT_HELP);
		parser.accepts("config", ErrorMsg.VANNO_RUN_CONFIG).withOptionalArg().describedAs(ErrorMsg.VANNO_RUN_CONFIG_DESC).ofType(String.class); 
		
		parser.acceptsAll(Arrays.asList("q", "q-file"), ErrorMsg.VANNO_RUN_QUERY).withRequiredArg().describedAs("Query File").ofType(String.class);  //查询文件
		parser.acceptsAll(Arrays.asList("f", "q-format"), ErrorMsg.VANNO_RUN_QUERY_FORMAT).withOptionalArg().describedAs("Query File Format").ofType(String.class); 
		
		parser.acceptsAll(Arrays.asList("c", "columns"), ErrorMsg.INDEX_FILE_COLUMN_CMD)
			.withOptionalArg().describedAs(ErrorMsg.INDEX_FILE_COLUMN_DESC).withValuesSeparatedBy(',').ofType(String.class); 
		
		parser.accepts("exact", ErrorMsg.VANNO_RUN_EXACT).withOptionalArg().describedAs("Exact Mode");
		
		parser.acceptsAll(Arrays.asList("d", "d-file"),  ErrorMsg.VANNO_RUN_DB).withRequiredArg().describedAs("Database Files(separated by comma, no space)").withValuesSeparatedBy(',').ofType(String.class);   //查询的数据库文件
		
		parser.accepts("fc", ErrorMsg.VANNO_RUN_DB_FC).withOptionalArg().ofType(String.class).defaultsTo("false"); 
		parser.acceptsAll(Arrays.asList("t", "thread"), ErrorMsg.VANNO_RUN_THREAD).withOptionalArg().describedAs("Thread").ofType(Integer.class).defaultsTo(1); //程序所需线程数

		parser.accepts("version", ErrorMsg.PRINT_VERSION);
		return parser;
	}
	
	public static CountRunBean readCountRunBean(String configFile) {
		CountRunBean bean = new CountRunBean();
		(new CountReadConfig<CountRunBean>()).read(bean, configFile);
		return bean;
	}
	
	public static CountRunBean runCount(OptionSet options) {
		 CountRunBean bean;
	   	 if(!options.has("config")) {
	   		bean = new CountRunBean(options);
	   	 } else {
	   		bean = readCountRunBean((String) options.valueOf("config"));
	   	 }
	   	 return bean;
	}
}
