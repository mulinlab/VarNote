package bean.config.run;

import htsjdk.samtools.util.IOUtil;
import htsjdk.samtools.util.StringUtil;

import java.util.ArrayList;
import java.util.List;

import joptsimple.OptionSet;
import constants.ErrorMsg;
import constants.VannoUtils;
import constants.VannoUtils.PROGRAM;
import postanno.FormatWithRef;

public abstract class AbstractRunCongig {
	
	protected String queryFile;
	protected FormatWithRef queryFormat;
	protected List<String> queryColumns;
	protected List<DBRunConfigBean> dbs;
	protected PROGRAM pro;
	protected DBRunConfigBean cdb;
	
	public AbstractRunCongig() {
		init();
	}
	
	public void addDB() {
		if(cdb != null)  VannoUtils.addFileForPattern(cdb, dbs);
	}
	
	public void checkDB(String label) {
		if(cdb == null)  throw new IllegalArgumentException("Database object is null, you should define db start with " + label);
	}
	
	private void init() {
		queryColumns = null;
        dbs = new ArrayList<DBRunConfigBean>();
	}
	
	@SuppressWarnings("unchecked")
	public AbstractRunCongig(OptionSet options) {
		 init();
		 String[] requiredOPT = {"q-file","d-file"};
         VannoUtils.checkMissing(options, requiredOPT);

         setQueryFile((String) options.valueOf("q-file"));
         
         if(options.has("q-format")) setQueryFormat((String) options.valueOf("q-format"));
		 if(options.has("columns")) setQueryColumns((List<String>) options.valuesOf("columns"));
		 
		 checkFormat();
	}
	
	public void checkRequired() {
		if(queryFile == null) throw new IllegalArgumentException("Query file path is required.");
		if(dbs.size() == 0) throw new IllegalArgumentException("At least one db is required.");
	}
	
	public void checkFormat() {
		 if(queryFormat == null) queryFormat = VannoUtils.determineType(queryFile);
		 if(queryFormat == null) throw new IllegalArgumentException(ErrorMsg.INDEX_FILE_FORMAT_CMD);
		 else if(this.queryColumns != null && queryFormat.flag != FormatWithRef.VCF_FORMAT) {
			 this.queryFormat = VannoUtils.updateFormat(queryFile, this.queryFormat, queryColumns);
		 }
		 queryFormat.checkOverlap();
	}
	
	public void setQueryFile(String queryFile) {
		IOUtil.assertInputIsValid(queryFile);
		this.queryFile = queryFile;
	}
	
	public void setQueryFormat(FormatWithRef queryFormat) {
		this.queryFormat = queryFormat;
	}
	
	public void setQueryFormat(String queryFormat) {
		this.queryFormat = VannoUtils.parseIndexFileFormat(queryFormat);
	}
	
	public void setQueryColumns(List<String> queryColumns) {
		this.queryColumns = queryColumns;
	}
	
	public String getQueryFile() {
		return queryFile;
	}
	
	public FormatWithRef getQueryFormat() {
		return queryFormat;
	}

	public List<String> getQueryColumns() {
		return queryColumns;
	}
	
	public String getQueryColumnsToString(final String regex) {
		if(queryColumns == null) {
			return "";
		} else {
			return StringUtil.join(regex, queryColumns);
		}
	}

	public List<DBRunConfigBean> getDbs() {
		return dbs;
	}

	public abstract void printLog();

	public PROGRAM getPro() {
		return pro;
	}
}
