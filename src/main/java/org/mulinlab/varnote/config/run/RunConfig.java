package org.mulinlab.varnote.config.run;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.Logger;
import org.mulinlab.varnote.config.io.temp.FlatPrintter;
import org.mulinlab.varnote.config.io.temp.Printter;
import org.mulinlab.varnote.config.io.temp.ZipPrintter;
import org.mulinlab.varnote.config.param.DBParam;
import org.mulinlab.varnote.config.param.RunParam;
import org.mulinlab.varnote.config.param.output.OutParam;
import org.mulinlab.varnote.config.param.query.QueryFileParam;
import org.mulinlab.varnote.config.param.query.QueryParam;
import org.mulinlab.varnote.constants.GlobalParameter;
import org.mulinlab.varnote.utils.LoggingUtils;
import org.mulinlab.varnote.utils.VannoUtils;
import org.mulinlab.varnote.utils.database.Database;
import org.mulinlab.varnote.utils.database.DatabaseFactory;
import org.mulinlab.varnote.exceptions.InvalidArgumentException;


public abstract class RunConfig {

	protected final Logger logger = LoggingUtils.logger;
	protected final static String TAB = GlobalParameter.TAB;

	protected RunParam runParam = new RunParam();
	protected QueryParam queryParam;
	protected List<DBParam> dbParams;
	protected OutParam outParam;
	protected Printter printter;
	protected List<Database> databses;

	protected RunConfig() { }

	protected RunConfig(QueryParam queryParam, List<DBParam> dbParams) {
		setQueryParam(queryParam);
		setDbParams(dbParams);
	}
	
	public void init() {
		initQuery();
		queryParam.printLog();

		initDB();
		databses = DatabaseFactory.readDatabaseFromConfig(dbParams);
		if(databses.size() == 0) throw new InvalidArgumentException("At least one db is required.");

		for (DBParam dbParam: dbParams) {
			dbParam.printLog();
		}

		initOutput();
		initOther();

		logger.info(VannoUtils.printLogHeader("OTHER  SETTING"));
		if(runParam != null) logger.info(String.format("Thread number: %d", runParam.getThread()));
	}
	
	protected abstract void initQuery();
	protected abstract void initDB();
	protected abstract void initOutput();

	protected abstract List<String> getHeader();

	protected void initQueryFileForThread() {
		QueryFileParam queryParam = (QueryFileParam)this.queryParam;

		if(queryParam == null) throw new InvalidArgumentException("Query file is required.");
		queryParam.splitFile(runParam.getThread());
		runParam.checkThreadNum(queryParam.getThreadSize());
	}

	protected void initPrintter(OutParam outParam, final int thread) {
		try {
			if(outParam.isGzip()) {
				printter = new ZipPrintter(outParam);
			} else {
				printter = new FlatPrintter(outParam);
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		printter.init();
		printter.setPrintter(thread);
	}

	protected void initOther() {

	}
	
	public List<Database> getDatabses() {
		return databses;
	}

	public void setDbParams(String[] dbPaths) {
		List<DBParam> dbConfigs = new ArrayList<DBParam>();
		for (String dbPath : dbPaths) {
			dbConfigs.add(new DBParam(new File(dbPath)));
		}
		this.dbParams = dbConfigs;
	}

	public void setDbParams(List<DBParam> dbParams) {
		this.dbParams = dbParams;
	}

	public QueryParam getQueryParam() {
		return queryParam;
	}

	public void setQueryParam(QueryParam queryParam) {
		this.queryParam = queryParam;
	}

	public OutParam getOutParam() {
		return outParam;
	}

	public void setOutParam(OutParam outParam) {
		this.outParam = outParam;
	}

	public List<DBParam> getDbParams() {
		return dbParams;
	}

	public void setThread(int thread) {
		runParam.setThread(thread);
	}

	public RunParam getRunParam() {
		return runParam;
	}

	public void setRunParam(RunParam runParam) {
		this.runParam = runParam;
	}

	public int getThread() {
		return runParam.getThread();
	}
}
