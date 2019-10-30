package org.mulinlab.varnote.config.run;

import org.mulinlab.varnote.config.param.DBParam;
import org.mulinlab.varnote.config.param.query.QueryFileParam;
import org.mulinlab.varnote.config.parser.RegParser;
import org.mulinlab.varnote.config.parser.ResultParser;
import org.mulinlab.varnote.exceptions.InvalidArgumentException;
import org.mulinlab.varnote.utils.database.Database;
import org.mulinlab.varnote.utils.enumset.IntersectType;
import org.mulinlab.varnote.utils.node.LocFeature;
import java.io.*;
import java.util.List;
import java.util.Map;

public final class CEPIPRunConfig extends OverlapRunConfig {

	public final static String ROAD_MAP_LABEL = "roadmap";
	public final static String REGBASE_MAP_LABEL = "regbase";
	private final static String DEFAULT_OUT_FILE_SUFFIX = ".reg.txt";

	private ResultParser[] regParsers;

	public CEPIPRunConfig(final QueryFileParam query, final List<DBParam> dbConfigs) {
		super(query, dbConfigs);
	}

	@Override
	protected void initDB() {
		DBParam roadmap = null;
		DBParam regbase = null;

		for (DBParam dbParam: dbParams) {
			if(dbParam.getOutName().equals(ROAD_MAP_LABEL)) {
				roadmap = dbParam;
			} else if(dbParam.getOutName().equals(REGBASE_MAP_LABEL)) {
				regbase = dbParam;
			}
		}

		if(roadmap == null) throw new InvalidArgumentException("Roadmap database is required.");
		if(regbase == null) throw new InvalidArgumentException("RegBase database is required.");

		roadmap.setIntersect(IntersectType.INTERSECT);
		regbase.setIntersect(IntersectType.EXACT);
	}

	@Override
	protected void initOutput() {
		outParam.setOutFileSuffix(DEFAULT_OUT_FILE_SUFFIX);
	}

	@Override
	protected void initOther() {
		initPrintter();

		for (Database db: databses) {
			if(db.getOutName().equals(ROAD_MAP_LABEL) || db.getOutName().equals(REGBASE_MAP_LABEL)) {
				db.readFormatFromHeader();
				db.setDefaultLocCodec(true);
			}
		}


		final int threadSize = ((QueryFileParam)queryParam).getThreadSize();
		regParsers = new ResultParser[threadSize];
		for (int i = 0; i < threadSize; i++) {
			regParsers[i] = new RegParser(printter.getPrintter(i));
		}
	}


	@Override
	public List<String> getHeader() {
		return null;
	}

	public void processNode(final LocFeature node, final Map<String, LocFeature[]> results, final int index) throws IOException {
		regParsers[index].processNode(node, results);
	}
}
