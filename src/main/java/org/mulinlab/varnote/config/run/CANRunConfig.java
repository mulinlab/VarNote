package org.mulinlab.varnote.config.run;

import org.mulinlab.varnote.config.param.DBParam;
import org.mulinlab.varnote.config.param.query.QueryFileParam;
import org.mulinlab.varnote.config.parser.CANParser;
import org.mulinlab.varnote.config.parser.PATParser;
import org.mulinlab.varnote.config.parser.ResultParser;
import org.mulinlab.varnote.utils.JannovarUtils;
import org.mulinlab.varnote.utils.enumset.IntersectType;
import org.mulinlab.varnote.utils.node.LocFeature;
import java.util.List;
import java.util.Map;

public final class CANRunConfig extends AdvanceToolRunConfig {

	private final static String DEFAULT_OUT_FILE_SUFFIX = ".can.txt";

	public CANRunConfig(final QueryFileParam query, final List<DBParam> dbConfigs, final JannovarUtils jannovarUtils) {
		super(query, dbConfigs, jannovarUtils);

		requiredDB.put(GENOMAD_LABEL, IntersectType.EXACT);
		requiredDB.put(REGBASE_MAP_LABEL, IntersectType.EXACT);
		requiredDB.put(COSMIC_LABEL, IntersectType.EXACT);
		requiredDB.put(ICGC_LABEL, IntersectType.EXACT);
	}


	@Override
	protected void initOutput() {
		outParam.setOutFileSuffix(DEFAULT_OUT_FILE_SUFFIX);
	}

	@Override
	protected void initOther() {
		super.initOther();
		super.initDatabaseByDefault();

		final int threadSize = ((QueryFileParam)queryParam).getThreadSize();
		parsers = new ResultParser[threadSize];
		for (int i = 0; i < threadSize; i++) {
			parsers[i] = new CANParser();
		}
	}

	@Override
	public List<String> getHeader() {
		return null;
	}

	public void processNode(final LocFeature node, final Map<String, LocFeature[]> results, final int index) {
		parsers[index].processNode(node, results);
	}

	public void mergeResult() {

	}
}
