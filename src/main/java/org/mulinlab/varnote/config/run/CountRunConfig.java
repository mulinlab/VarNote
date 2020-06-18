package org.mulinlab.varnote.config.run;


import org.apache.commons.lang3.StringUtils;
import org.mulinlab.varnote.config.param.DBParam;
import org.mulinlab.varnote.config.param.output.OutParam;
import org.mulinlab.varnote.config.param.query.QueryFileParam;
import org.mulinlab.varnote.constants.GlobalParameter;
import org.mulinlab.varnote.utils.enumset.IndexType;
import org.mulinlab.varnote.utils.enumset.Mode;
import org.mulinlab.varnote.utils.node.LocFeature;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class CountRunConfig extends RunConfig {

	private long count = 0;

	public CountRunConfig() {
		super();
	}

	public CountRunConfig(final String queryPath, final String[] dbPaths) {
		super();
		setQueryParam(new QueryFileParam(queryPath, false));
		setDbParams(dbPaths);
	}

	public CountRunConfig(final QueryFileParam query, final List<DBParam> dbConfigs) {
		super(query, dbConfigs);
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
		if(outParam == null) setOutParam(new OutParam());
		outParam.setOutFileSuffix(GlobalParameter.COUNT_RESULT_SUFFIX);
	}

	@Override
	protected void initOther() {
		initPrintter();
	}

	protected void initPrintter() {
		try {
			initPrintter(outParam, runParam.getThread());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}


	public void processNode(final LocFeature node, Integer[] count, final int index) throws IOException {
		int total = 0;
		for (Integer c: count) {
			total += c;
		}
		printter.getPrintter(index).print(String.format("%s\t%s\t%d", node, StringUtils.join(count, "\t"), total));
		this.count += total;
	}

	public List<String> getDBNames() {
		List<String> dbNames = new ArrayList<>();
		for (DBParam dbParam: dbParams) {
			dbNames.add(dbParam.getOutName());
		}
		return dbNames;
	}

	@Override
	public List<String> getHeader() {
		List<String> header = new ArrayList<>();
		header.add(String.format("Chr\tBegin\tEnd\t%s\tTotal", StringUtils.join(getDBNames(), "\t")));
		return header;
	}

	public long getCount() {
		return count;
	}

	public void mergeResult() {
		super.mergeResult();
		System.out.println("Count: " + count);
	}
}
