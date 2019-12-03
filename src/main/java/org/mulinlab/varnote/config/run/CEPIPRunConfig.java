package org.mulinlab.varnote.config.run;

import org.mulinlab.varnote.config.param.DBParam;
import org.mulinlab.varnote.config.param.query.QueryFileParam;
import org.mulinlab.varnote.config.parser.REGParser;
import org.mulinlab.varnote.config.parser.ResultParser;
import org.mulinlab.varnote.utils.JannovarUtils;
import org.mulinlab.varnote.utils.enumset.CellType;
import org.mulinlab.varnote.utils.enumset.IntersectType;
import org.mulinlab.varnote.utils.node.LocFeature;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public final class CEPIPRunConfig extends AdvanceToolRunConfig {

	private final static String DEFAULT_OUT_FILE_SUFFIX = ".reg.txt";
	private List<CellType> cellTypes;

	public CEPIPRunConfig(final QueryFileParam query, final List<DBParam> dbConfigs, final JannovarUtils jannovarUtils, List<CellType> cellTypes) {
		super(query, dbConfigs, jannovarUtils);

		if(cellTypes == null) {
			this.cellTypes = Arrays.asList(CellType.values()) ;
		} else {
			this.cellTypes = cellTypes;
		}

		requiredDB.put(ROAD_MAP_LABEL, IntersectType.INTERSECT);
		requiredDB.put(REGBASE_MAP_LABEL, IntersectType.EXACT);
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
			parsers[i] = new REGParser(printter.getPrintter(i), cellTypes, jannovarUtils);
		}
	}


	@Override
	public List<String> getHeader() {
		List<String> header = new ArrayList<>();

		header.add(String.format("%s\t%s%s\t%s\t%s\t%s\t%s", ((QueryFileParam)queryParam).getQueryFormat().getHeaderPartStr(), getVariantEffectHeader(),
				"CellType", "cell_p", "reg_p1", "reg_p2", "combined_p"));
		return header;
	}

	public void processNode(final LocFeature node, final Map<String, LocFeature[]> results, final int index) {
		parsers[index].processNode(node, results);
	}
}
