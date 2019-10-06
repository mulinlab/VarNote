package org.mulinlab.varnote.config.run;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import org.mulinlab.varnote.config.anno.ab.AbstractParser;
import org.mulinlab.varnote.config.param.DBParam;
import org.mulinlab.varnote.config.param.query.QueryRegionParam;
import org.mulinlab.varnote.constants.GlobalParameter;

public final class QueryRegionConfig extends RunConfig {
	private boolean displayLabel = GlobalParameter.DEFAULT_DISPLAY_LABEL;
	
	public QueryRegionConfig(final String queryRegion, final String[] dbPaths) {
		super();
		setQueryParam(new QueryRegionParam(queryRegion));
		setDbParams(dbPaths);
	}

	public QueryRegionConfig(final String queryRegion, final String[] dbPaths, final boolean displayLabel) {
		this(queryRegion, dbPaths);
		setDisplayLabel(displayLabel);
	}
	
	public QueryRegionConfig(final String queryRegion, final List<DBParam> dbConfigs, final boolean displayLabel) {
		setQueryParam(new QueryRegionParam(queryRegion));
		setDbParams(dbConfigs);
		setDisplayLabel(displayLabel);
	}
		
	@Override
	protected void initQuery() {
	}

	@Override
	protected void initDB() {
	}

	@Override
	protected void initOutput() {

	}

	@Override
	protected List<String> getHeader() {
		return null;
	}

	@Override
	protected void initOther() {
		super.initOther();
	}

	public void printRecord(final Map<String, List<String>> results) throws IOException {
		List<String> list;
		for (DBParam databaseConfig : dbParams) {
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
		return ((QueryRegionParam)queryParam).getRegion();
	}

	public boolean isDisplayLabel() {
		return displayLabel;
	}

	public void setDisplayLabel(boolean displayLabel) {
		this.displayLabel = displayLabel;
	}
}
