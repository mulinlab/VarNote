package org.mulinlab.varnote.config.run;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import htsjdk.samtools.util.StringUtil;
import org.mulinlab.varnote.cmdline.txtreader.abs.QueryReader;
import org.mulinlab.varnote.config.io.temp.ThreadPrintter;
import org.mulinlab.varnote.config.param.DBParam;
import org.mulinlab.varnote.config.param.output.IntersetOutParam;
import org.mulinlab.varnote.config.param.query.QueryFileParam;
import org.mulinlab.varnote.cmdline.txtreader.abs.AbstractQueryReader;
import org.mulinlab.varnote.constants.GlobalParameter;
import org.mulinlab.varnote.utils.enumset.IndexType;
import org.mulinlab.varnote.utils.enumset.Mode;
import org.mulinlab.varnote.utils.enumset.OutMode;
import org.mulinlab.varnote.utils.headerparser.BEDHeaderParser;
import org.mulinlab.varnote.utils.node.Node;

public class OverlapRunConfig extends RunConfig {
	protected final static String OVERLAP_EQUAL = GlobalParameter.OVERLAP_EQUAL;
	protected final static String OVERLAP_NOTE = GlobalParameter.OVERLAP_NOTE;

	private final static String QUERY_START = GlobalParameter.QUERY_START;

	public OverlapRunConfig() {
		super();
	}
	
	public OverlapRunConfig(final String queryPath, final String[] dbPaths) {
		super();
		setQueryParam(new QueryFileParam(queryPath));
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
		initQueryFileForThread();
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
		if(outParam == null) {
			setOutParam(new IntersetOutParam());
		}
	}


	@Override
	protected void initOther() {
		super.initOther();
		outParam.setDefalutOutPath(((QueryFileParam)queryParam).getQueryPath());
		outParam.checkParam();
		initPrintter(outParam, runParam.getThread());
	}

	@Override
	public List<String> getHeader() {
		List<String> comments = new ArrayList<>();
		QueryFileParam queryParam = (QueryFileParam)this.queryParam;

		comments.add(getComment(QueryReader.QUERY, queryParam.getQueryPath()));
		comments.add(getComment(QueryReader.QUERY_FORMAT, queryParam.getQueryFormat().toString()));
		if(queryParam.getQueryFormat().getHeaderPath() != null) comments.add(getComment(QueryReader.HEADER_PATH, queryParam.getQueryFormat().getHeaderPath()));
		comments.add(getComment(QueryReader.HEADER, StringUtil.join(BEDHeaderParser.COMMA, queryParam.getQueryFormat().getOriginalField())));

		for (DBParam db : dbParams) {
			comments.add(getComment(QueryReader.DB_PATH, db.getDbPath()));
			comments.add(getComment(QueryReader.DB_INDEX_TYPE, db.getIndexType().toString()));
			comments.add(getComment(QueryReader.DB_LABEL, db.getOutName()));
		}

		comments.add(getComment(QueryReader.OUTPUT_PATH, outParam.getOutputPath()));
		comments.add(String.format("%s%s",  OVERLAP_NOTE, AbstractQueryReader.END));
		return comments;
	}

	public String getComment(final String key, final String val) {
		return String.format("%s%s%s%s",  OVERLAP_NOTE, key, OVERLAP_EQUAL, val);
	}

	public void mergeResult() {
		try {
			printter.mergeFile(getHeader());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void printRecord(final Node node, final Map<String, List<String>> results, final int index) throws IOException {
		IntersetOutParam outParam = (IntersetOutParam)this.outParam;
		ThreadPrintter threadPrintter = printter.getPrintter(index);
		if(outParam.isLoj()) {
			threadPrintter.print(String.format("%s%s%s", QUERY_START, TAB, node.origStr));
		} else if(results.size() > 0 && outParam.getOutputMode() != OutMode.DB) {
			threadPrintter.print(String.format("%s%s%s", QUERY_START, TAB, node.origStr));
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
}
