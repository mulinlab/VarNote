package org.mulinlab.varnote.config.run;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.mulinlab.varnote.cmdline.txtreader.abs.QueryReader;
import org.mulinlab.varnote.config.io.temp.ThreadPrintter;
import org.mulinlab.varnote.config.param.DBParam;
import org.mulinlab.varnote.config.param.output.IntersetOutParam;
import org.mulinlab.varnote.config.param.query.QueryFileParam;
import org.mulinlab.varnote.cmdline.txtreader.abs.AbstractQueryReader;
import org.mulinlab.varnote.constants.GlobalParameter;
import org.mulinlab.varnote.utils.VannoUtils;
import org.mulinlab.varnote.utils.enumset.*;
import org.mulinlab.varnote.utils.node.LocFeature;

public class OverlapRunConfig extends RunConfig {

	protected final static String OVERLAP_EQUAL = GlobalParameter.OVERLAP_EQUAL;
	protected final static String OVERLAP_NOTE = GlobalParameter.OVERLAP_NOTE;
	private final static String QUERY_START = GlobalParameter.QUERY_START;

	private boolean isTab = true;
	public OverlapRunConfig() {
		super();
	}
	
	public OverlapRunConfig(final String queryPath, final String[] dbPaths) {
		super();
		setQueryParam(new QueryFileParam(queryPath, false));
		setDbParams(dbPaths);
	}

	public OverlapRunConfig(final QueryFileParam query, final List<DBParam> dbConfigs) {
		super();
		setQueryParam(query);
		setDbParams(dbConfigs);
	}

	public OverlapRunConfig(final String queryPath, final String[] dbPaths, final IntersetOutParam output, final int thread) {
		this(queryPath, dbPaths);
		setOutParam(output);
		runParam.setThread(thread);
	}

	public OverlapRunConfig(final QueryFileParam query, final List<DBParam> dbConfigs, final IntersetOutParam output, final int thread) {
		this(query, dbConfigs);
		setOutParam(output);
		runParam.setThread(thread);
	}

	@Override
	protected void initQuery() {
		QueryFileParam queryParam = (QueryFileParam)this.queryParam;
		initQueryFileForThread();

		if(queryParam.getQueryFormat().getDelimiter() != Delimiter.TAB) {
			isTab = false;
		}
	}

	@Override
	protected void initDB() {
		for (int i = 0; i < dbParams.size(); i++) {
			if(runParam.getMode() == Mode.TABIX) {
				dbParams.get(i).setIndexType(IndexType.TBI);
			}
		}
	}

	@Override
	protected void initOutput() {
		if(outParam == null) setOutParam(new IntersetOutParam());
		outParam.setOutFileSuffix(GlobalParameter.OVERLAP_RESULT_SUFFIX);
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

	@Override
	protected List<String> getHeader() {
		List<String> comments = null;
		if(!((IntersetOutParam)outParam).isRemoveCommemt()) {
			comments = new ArrayList<>();

			QueryFileParam queryParam = (QueryFileParam)this.queryParam;

			comments.add(getComment(QueryReader.QUERY, queryParam.getQueryPath()));
			comments.add(getComment(QueryReader.QUERY_FORMAT, queryParam.getQueryFormat().toString()));
			if(queryParam.getQueryFormat().getHeaderPath() != null) comments.add(getComment(QueryReader.HEADER_PATH, queryParam.getQueryFormat().getHeaderPath()));
			comments.add(getComment(QueryReader.HEADER, queryParam.getQueryFormat().getHeaderPartStr()));

			for (DBParam db : dbParams) {
				comments.add(getComment(QueryReader.DB_PATH, db.getDbPath()));
				comments.add(getComment(QueryReader.DB_INDEX_TYPE, db.getIndexType().toString()));
				comments.add(getComment(QueryReader.DB_LABEL, db.getOutName()));
			}

			comments.add(getComment(QueryReader.OUTPUT_PATH, outParam.getOutputPath()));
			comments.add(String.format("%s%s",  OVERLAP_NOTE, AbstractQueryReader.END));
		}

		return comments;
	}

	public String getComment(final String key, final String val) {
		return String.format("%s%s%s%s",  OVERLAP_NOTE, key, OVERLAP_EQUAL, val);
	}

	protected void printRecord(final LocFeature node, String result, final int index) throws IOException {
		printter.getPrintter(index).print(result);
	}
	
	public void printRecord(final LocFeature node, final Map<String, String[]> results, final int index) throws IOException {
		IntersetOutParam outParam = (IntersetOutParam)this.outParam;
		ThreadPrintter threadPrintter = printter.getPrintter(index);

		int count = 0;
		for (String r:results.keySet()) {
			if(results.get(r) != null) {
				count += results.get(r).length;
			}
		}
		if(outParam.isLoj()) {
			threadPrintter.print(String.format("%s%s%s", QUERY_START, TAB, getOrigStr(node)));
		} else if(count > 0 && outParam.getOutputMode() != OutMode.DB) {
			threadPrintter.print(String.format("%s%s%s", QUERY_START, TAB, getOrigStr(node)));
		}

		if(outParam.getOutputMode() != OutMode.QUERY) {
			for (DBParam databaseConfig : this.dbParams) {
				if (results.get(databaseConfig.getOutName()) != null)
					for (String str : results.get(databaseConfig.getOutName())) {
						threadPrintter.print(String.format("%s%s%s", databaseConfig.getOutName(), TAB, str));
					}
			}
		}
	}

	public String getOrigStr(final LocFeature node) {
		if(!isTab) {
			return node.origStr.replaceAll(GlobalParameter.COMMA, GlobalParameter.TAB);
		} else {
			return node.origStr;
		}
	}
}
