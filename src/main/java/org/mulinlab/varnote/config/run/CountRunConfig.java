package org.mulinlab.varnote.config.run;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.mulinlab.varnote.config.param.DBParam;
import org.mulinlab.varnote.config.param.query.QueryFileParam;
import org.mulinlab.varnote.constants.GlobalParameter;
import org.mulinlab.varnote.utils.enumset.IndexType;
import org.mulinlab.varnote.utils.enumset.Mode;

import java.util.List;

public class CountRunConfig extends RunConfig {

	protected final Logger logger = LogManager.getLogger(this.getClass());

	private final static String QUERY_START = GlobalParameter.QUERY_START;
	protected boolean isCount = GlobalParameter.DEFAULT_IS_COUNT;

	public CountRunConfig() {
		super();
	}

	public CountRunConfig(final String queryPath, final String[] dbPaths) {
		super();
		setQueryParam(new QueryFileParam(queryPath));
		setDbParams(dbPaths);
	}

	public CountRunConfig(final QueryFileParam query, final List<DBParam> dbConfigs) {
		super();
		setQueryParam(query);
		setDbParams(dbConfigs);
	}

	public CountRunConfig(final String queryPath, final String[] dbPaths, final int thread) {
		this(queryPath, dbPaths);
		runParam.setThread(thread);
	}

	public CountRunConfig(final QueryFileParam query, final List<DBParam> dbConfigs, final int thread) {
		this(query, dbConfigs);
		runParam.setThread(thread);
	}

	@Override
	protected void initQuery() {
		runParam.setMode(Mode.MIX);
		initQueryFileForThread();
	}

	@Override
	protected void initDB() {
		for (int i = 0; i < dbParams.size(); i++) {
			if(dbParams.get(i).getIndexType() != null && dbParams.get(i).getIndexType() == IndexType.TBI) {
				logger.info("TBI index is not supported by count mode, change index type to VARNOTE index.");
			}
			dbParams.get(i).setIndexType(IndexType.VARNOTE);
		}
	}

	@Override
	protected void initOutput() {
	}


	@Override
	protected void initOther() {
		super.initOther();
	}

	@Override
	public List<String> getHeader() {
		return null;
	}
}
