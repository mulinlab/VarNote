package test;

import htsjdk.samtools.util.IOUtil;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import constants.ErrorMsg;
import constants.VannoUtils;
import constants.VannoUtils.PROGRAM;
import bean.config.run.DBRunConfigBean;
import bean.config.run.DBRunConfigBean.IndexFormat;
import postanno.FormatWithRef;


public class RunConfigBean {
	

	
	
	private String queryFile;
	
	private OutMode outMode;
	private String outputFolder;
	private String overlapOutput;
//	private String annoOutput;
	private String tempFolder;
//	private String annoConfig;
	private Mode mode;
	private PROGRAM pro;
	private int thread;
	private FormatWithRef queryFormat;
	private String queryColumns;
	private boolean exact;
	private List<DBRunConfigBean> dbs;
	private List<BufferedWriter> writers;
	private boolean loj;
	private boolean fc;
//	private Map<String, Integer> queryFieldsMap; 
//	private List<String> queryFieldKeysList; 

	public RunConfigBean() {
		super();
		dbs = new ArrayList<DBRunConfigBean>();
		thread = 1;
		exact = false;
		writers = new ArrayList<BufferedWriter>();
		loj = false;
		fc = false;
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
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void checkDBConfig() {
		if(queryFile == null) throw new IllegalArgumentException("Query file path is required.");
		if(queryFormat == null) queryFormat = VannoUtils.determineType(queryFile);
		if(queryFormat == null) throw new IllegalArgumentException(ErrorMsg.INDEX_FILE_FORMAT_CMD);
		else if(this.queryColumns != null) {
			 this.queryFormat = VannoUtils.updateFormat(this.queryFormat, queryColumns);
		}
		queryFormat.checkOverlap();
		
		if(mode == null) mode = Mode.MIX;
		if(thread == 0) thread = 1;
		if(outMode == null) outMode = OutMode.BOTH;
//		if(step == null) step = STEP.OVERLAP;
		
		if(pro == PROGRAM.COUNT) {
			mode = Mode.MIX;
			System.out.println("Step Count can only use mode mix.");
		}
		
		System.out.println("\n===================== Config =================");
		System.out.println("Query File Path:" + queryFile);
		System.out.println("Using mode:" + mode);
		System.out.println("Thread number:" + thread);
		System.out.println("Exact:" + exact);
		
		
		if(dbs.size() == 0) throw new IllegalArgumentException("At least one db is required.");
		File query = new File(queryFile);
		
		if(pro == PROGRAM.OVERLAP) {
			if(outputFolder == null) {
				System.out.println("outputFolder is null");
				outputFolder = queryFile + "_" + mode + "_out" ;
				System.out.println("Output folder is not defined, creating output folder: " + outputFolder);
			}
			
			File folder =  new File(outputFolder);
			if (!folder.exists()) {
			    System.out.println("Output folder is not defined, creating out folder: " + folder.getAbsolutePath());
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
			overlapOutput = outputFolder + File.separator + query.getName() + ".overlap";
			annoOutput = outputFolder + File.separator + query.getName() + ".anno";
			for (DBRunConfigBean configDBBean : dbs) {
				configDBBean.setDbOut(this.tempFolder);
			}
			
//			if(((step == STEP.ANNO) ||  (step == STEP.BOTH))&& annoConfig == null) throw new IllegalArgumentException("Missing required arguments anno-file for annotation.");
		} else if(pro == PROGRAM.COUNT){
			for (DBRunConfigBean configDBBean : dbs) {
				if(configDBBean.getIndexFormat() != IndexFormat.PLUS) {
					System.err.println("Step Count only support plus index.");
	  	    		System.exit(1);
				} 
			}
		}
	}
	
	public void addDbs(DBRunConfigBean db) {
		dbs.add(db);
	}

	public String getQueryFile() {
		return queryFile;
	}

	public void setQueryFile(String queryFile) {
		IOUtil.assertInputIsValid(queryFile);
		this.queryFile = queryFile;
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

	
	public FormatWithRef getQueryFormat() {
		return queryFormat;
	}	
	
	public String getQueryColumns() {
		return (queryColumns == null) ? "" : queryColumns;
	}

	public void setQueryColumns(String queryColumns) {
		this.queryColumns = queryColumns;
	}

	public void setQueryFormat(FormatWithRef queryFormat) {
		this.queryFormat = queryFormat;
	}
	
	public void setQueryFormat(String queryFormat) {
		queryFormat = queryFormat.trim().toLowerCase();
		this.queryFormat = VannoUtils.parseIndexFileFormat(queryFormat);

//		if(this.queryFormat != FormatWithRef.VCF) {
//			setBedFields(queryFormat.substring(beg + 1));
//		}
	}
//
//	public void setBedFields(String header) {
//		if(header == null) return;
//		if(header.equals("")) throw new IllegalArgumentException("Please check whether your header set for " + queryFile + " is correct? ");
//		queryFieldKeysList = VannoUtils.fieldsStrToList(header);
//		queryFieldsMap = VannoUtils.fieldsStrToMap(header, (this.queryFormat.tbiFormat.flags == TabixFormat.VCF_FLAGS));
//		queryFormat = VannoUtils.setFormat(queryFormat, queryFieldsMap);
//	}
	
	public boolean isExact() {
		return exact;
	}

	public void setExact(String exact) {
		this.exact = VannoUtils.strToBool(exact);
	}

	public List<DBRunConfigBean> getDbs() {
		return dbs;
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

	public boolean isFc() {
		return fc;
	}

	public void setFc(String fc) {
		this.fc = VannoUtils.strToBool(fc);
	}
	
	public void setOutMode(int outMode) {
		this.outMode = VannoUtils.checkOutMode(outMode);
	}
	
	public OutMode getOutMode() {
		return outMode;
	}
	
	public PROGRAM getPro() {
		return pro;
	}

	public void setPro(PROGRAM pro) {
		this.pro = pro;
	}

	public String getOverlapOutput() {
		return overlapOutput;
	}

	public String getAnnoOutput() {
		return annoOutput;
	}

	public void setOutputFolder(String outputFolder) {
		this.outputFolder = outputFolder;
	}

//	public String getAnnoConfig() {
//		return annoConfig;
//	}
//
//	public void setAnnoConfig(String annoConfig) {
//		IOUtil.assertInputIsValid(annoConfig);
//		this.annoConfig = annoConfig;
//	}
	
	public Integer getQueryFieldCol(String val) {
		if(queryFieldsMap.get(val) == null) {
			return -1;
		} else {
			return queryFieldsMap.get(val);
		}
	}
	
	public Map<String, Integer> getQueryFieldsMap() {
		return queryFieldsMap;
	}

	public List<String> getQueryFieldKeysList() {
		return queryFieldKeysList;
	}
}
