package main.java.vanno.bean.config.run;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import main.java.vanno.bean.config.anno.ab.AbstractParser;
import main.java.vanno.bean.database.DatabaseConfig;
import main.java.vanno.bean.query.Log;
import main.java.vanno.constants.VannoUtils.PROGRAM;

public final class QueryRunConfig extends AbstractConfig{
	private String queryRegion;
	private boolean displayLabel;
	
	public QueryRunConfig(final String queryRegion, final String[] dbPaths) {
		this(queryRegion, dbPaths, defaultUseJDK, deafultDisplayLabel, defaultLog);
	}
	
	public QueryRunConfig(final String queryRegion, final String[] dbPaths, final boolean useJDK) {
		this(queryRegion, dbPaths, useJDK, deafultDisplayLabel, defaultLog);
	}
	
	public QueryRunConfig(final String queryRegion, final String[] dbPaths, final boolean useJDK, final boolean displayLabel, final Log log) {
		super(dbPaths, useJDK, log);
		this.displayLabel = displayLabel;
		this.program = PROGRAM.QUERY;
		this.queryRegion = queryRegion;
	}
	
	public QueryRunConfig(final String queryRegion, final List<DatabaseConfig> dbConfigs, final boolean useJDK, final boolean displayLabel, final Log log) {
		super(dbConfigs, useJDK, log);
		this.displayLabel = displayLabel;
		this.program = PROGRAM.QUERY;
		this.queryRegion = queryRegion;
	}
		
	@Override
	protected void initQuery() {
	}

	@Override
	protected void initDB() {
	}

	@Override
	protected void initOther() {
	}

	public void printRecord(final Map<String, List<String>> results) throws IOException {
		List<String> list;
		for (DatabaseConfig databaseConfig : dbConfigs) {
			list = results.get(databaseConfig.getOutName());
			if(list != null && list.size() > 0)
				for (String str : list) {
					if(isDisplayLabel()) {
						System.out.println(databaseConfig.getOutName() + AbstractParser.TAB + str);
					} else {
						System.out.println(str);
					}
				}
		}
	}
	
	public String getQueryRegion() {
		return queryRegion;
	}

	public boolean isDisplayLabel() {
		return displayLabel;
	}

//	public void setQueryRegion(String queryRegion) {
//		this.queryRegion = queryRegion;
//	}

	public void setDisplayLabel(boolean displayLabel) {
		this.displayLabel = displayLabel;
	}
}
