package bean.config.run;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import bean.config.readconfig.AbstractReadConfig;
import bean.config.readconfig.OverlapReadConfig;
import bean.config.run.DBRunConfigBean.IndexFormat;
import joptsimple.OptionParser;
import joptsimple.OptionSet;
import constants.ErrorMsg;
import constants.VannoUtils;
import constants.VannoUtils.PROGRAM;

public class OverlapRunBean extends CountRunBean{
	
	public enum OutMode {
		QUERY,
		DB,
		BOTH
	}
	private OutMode outMode;
	private String outputFolder;
	private String overlapOutput;
	private String tempFolder;
	private List<BufferedWriter> writers;
	private boolean loj;
	
	public OverlapRunBean() {
		super();
		init();
	}
	
	private void init() {
		pro = PROGRAM.OVERLAP;
		writers = new ArrayList<BufferedWriter>();
		loj = false;
		outMode = OutMode.BOTH;
	}
	
	public OverlapRunBean(final OptionSet options) {
		super(options);
		init(); 	 
		
		if(options.has("loj")) setLoj("true");
	    
	    if(options.has("out-mode")) setOutMode((Integer) options.valueOf("out-mode"));
	    if(options.has("out-floder")) setOutputFolder((String) options.valueOf("out-floder"));
	  	
	    checkOutFolder();
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public void initIndex(final OptionSet options) {
		if(options.has("d-index")) dbIndex = (List<String>) options.valuesOf("d-index");
	  	if(options.has("d-label")) dbLabel = (List<String>) options.valuesOf("d-label");
	  	    
		if((dbIndex != null) && (dbIndex.size() != db.size())) throw new IllegalArgumentException("We get " + db.size() + " database files but get " + dbIndex.size() + " index files. Please check.");
		if((dbLabel != null) && (dbLabel.size() != db.size())) throw new IllegalArgumentException("We get " + db.size() + " database files but get " + dbIndex.size() + " labels. Please check.");
	}
	
	@Override
	public void setMode(final OptionSet options) {
		if(options.has("mode")) setMode((Integer) options.valueOf("mode"));
	}
	
	@Override
	public void setDBIndex(final int i, final OptionSet options) {
		
		if(mode == Mode.TABIX) 
			cdb.setIndexFormat(IndexFormat.TBI);
		else if(dbIndex != null) 
			cdb.setIndexFormat(dbIndex.get(i));
		
		
		if(dbLabel != null) cdb.setLable(dbLabel.get(i));
	}
	
	public void addWriter(final BufferedWriter writer) {
		writers.add(writer);
	}

	public String getTempFileName(int index) {
		return tempFolder + File.separator  + "temp" + index;
	}
	
	public void closeWriters() {
		try {
			for (BufferedWriter writer : writers) {
				writer.flush();
				writer.close();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void checkOutFolder() {
		if(outputFolder == null) {
			outputFolder = queryFile + "_" + mode + "_out" ;
			System.out.println("Output path is not defined, redirect output folder to: " + outputFolder);
		}
		
		File folder =  new File(outputFolder);
		if (!folder.exists()) {
		    System.out.println("Output folder hasn't created, creating out folder: " + folder.getAbsolutePath());
		    try{
		    	folder.mkdir();
		    } catch(SecurityException se) {
		    	System.out.println("Creating directory " + folder.getAbsolutePath() + " with error! please check.");
		    }        
		} 
		
		File tempFolder =  new File(folder + File.separator + "temp");
		if (!tempFolder.exists()) {
		    System.out.println("Creating temp folder: " + tempFolder.getAbsolutePath());
		    try{
		    	tempFolder.mkdir();
		    } catch(SecurityException se) {
		    	System.out.println("Creating directory " + tempFolder.getAbsolutePath() + " with error! please check.");
		    }        
		} 
		
		this.tempFolder = tempFolder.getAbsolutePath();
		
		File query = new File(queryFile);
		overlapOutput = outputFolder + File.separator + query.getName() + ".overlap";
		for (DBRunConfigBean configDBBean : dbs) {
			configDBBean.setDbOut(this.tempFolder);
		}
	}

	@Override
	public void printLog() {
		super.printLog();
	}
	
	public void printMode() {
		System.out.println("Using mode: " + mode);
	}

	public OutMode getOutMode() {
		return outMode;
	}

	public void setOutMode(int outMode) {
		this.outMode = VannoUtils.checkOutMode(outMode);
	}

	public void setOutputFolder(String outputFolder) {
		this.outputFolder = outputFolder;
	}

	public String getOverlapOutput() {
		return overlapOutput;
	}

	public String getTempFolder() {
		return tempFolder;
	}

	public boolean isLoj() {
		return loj;
	}

	public void setLoj(String loj) {
		this.loj = VannoUtils.strToBool(loj);
	}
	
	public static OptionParser getParserForIndex() {
		OptionParser parser = CountRunBean.getParserForIndex();
		parser.accepts("mode", ErrorMsg.VANNO_RUN_MODE).withOptionalArg().describedAs("Mode").ofType(Integer.class).defaultsTo(1);  //模式，0号是循环单次tabix查询 1号是两种方式结合查询		
		parser.accepts("out-mode", ErrorMsg.VANNO_RUN_OUT_MODE).withOptionalArg().describedAs("Out Mode").ofType(Integer.class).defaultsTo(2);  	
		parser.accepts("d-index", ErrorMsg.VANNO_RUN_DB_INDEX).withOptionalArg().describedAs("Index type to be used").withValuesSeparatedBy(',').ofType(String.class);   //查询的数据库文件
		parser.accepts("d-label", ErrorMsg.VANNO_RUN_DB_LABEL).withOptionalArg().describedAs(ErrorMsg.VANNO_RUN_DB_LABEL_DESC).withValuesSeparatedBy(',').ofType(String.class);   
		
		parser.acceptsAll(Arrays.asList("o", "out-floder"), ErrorMsg.VANNO_RUN_DB_OUT).withOptionalArg().describedAs("Output Folder").ofType(String.class);  //输出文件
		parser.accepts("loj", ErrorMsg.VANNO_RUN_DB_LOJ).withOptionalArg().ofType(String.class).defaultsTo("false"); 
        return parser;
	}
	
	public void setDBIndex(final String val) {
		checkDB(AbstractReadConfig.DB_BEGIN);
		cdb.setIndexFormat(val);
	}
	
	public void setDBLabel(final String val) {
		checkDB(AbstractReadConfig.DB_BEGIN);
		cdb.setLable(val);
	}
	
	public static OverlapRunBean readOverlapRunBean(String configFile) {
		OverlapReadConfig<OverlapRunBean> readFromConfig = new OverlapReadConfig<OverlapRunBean>();
		OverlapRunBean bean = new OverlapRunBean();
		readFromConfig.read(bean, configFile);
		return bean;
	}
	
	public static OverlapRunBean runOverlap(OptionSet options) {
		 OverlapRunBean overlapRunBean;
    	 if(!options.has("config")) {
    		 overlapRunBean = new OverlapRunBean(options);
    	 } else {
    		 overlapRunBean = readOverlapRunBean((String) options.valueOf("config"));
    	 }
    	 return overlapRunBean;
	}
}
