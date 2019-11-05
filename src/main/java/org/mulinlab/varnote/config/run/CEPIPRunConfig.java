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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public final class CEPIPRunConfig extends OverlapRunConfig {

	public final static String ROAD_MAP_LABEL = "roadmap";
	public final static String REGBASE_MAP_LABEL = "regbase";
	private final static String DEFAULT_OUT_FILE_SUFFIX = ".reg.txt";
	private final static String ALL_CELL_TYPES = "E001,E002,E003,E004,E005,E006,E007,E008,E009,E010,E011,E012,E013,E014,E015,E016,E017,E018,E019,E020,E021,E022,E023,E024,E025,E026,E027,E028,E029,E030,E031,E032,E033,E034,E035,E036,E037,E038,E039,E040,E041,E042,E043,E044,E045,E046,E047,E048,E049,E050,E051,E052,E053,E054,E055,E056,E057,E058,E059,E061,E062,E063,E065,E066,E067,E068,E069,E070,E071,E072,E073,E074,E075,E076,E077,E078,E079,E080,E081,E082,E083,E084,E085,E086,E087,E088,E089,E090,E091,E092,E093,E094,E095,E096,E097,E098,E099,E100,E101,E102,E103,E104,E105,E106,E107,E108,E109,E110,E111,E112,E113,E114,E115,E116,E117,E118,E119,E120,E121,E122,E123,E124,E125,E126,E127,E128,E129";
	//E060 E064

	private ResultParser[] regParsers;
	private List<String> cellTypes;

	public CEPIPRunConfig(final QueryFileParam query, final List<DBParam> dbConfigs, final String cellTypes) {
		super(query, dbConfigs);

		if(cellTypes == null) {
			this.cellTypes = Arrays.asList(ALL_CELL_TYPES.split(",")) ;
		} else {
			this.cellTypes = new ArrayList<String>();
			for (String cellType: cellTypes.split(",")) {
				try {
					cellType = cellType.trim().toUpperCase();
					int num = Integer.parseInt(cellType.replace("E", ""));
					if(num < 1 || num > 129 || num == 60 || num == 64) {
						logger.info(String.format("Invalid Cell ID %s, please refer https://github.com/mdozmorov/genomerunner_web/wiki/Roadmap-cell-types for details.", cellType));
					} else {
						this.cellTypes.add(cellType);
					}
				} catch (NumberFormatException e) {
					logger.info(String.format("Invalid Cell ID %s, please refer https://github.com/mdozmorov/genomerunner_web/wiki/Roadmap-cell-types for details.", cellType));
				}
			}

			if(this.cellTypes.size() == 0) {
				throw new InvalidArgumentException("Input Cell IDs are invalid, please check.");
			}
		}
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
			regParsers[i] = new RegParser(printter.getPrintter(i), cellTypes);
		}
	}


	@Override
	public List<String> getHeader() {
		List<String> header = new ArrayList<>();
		header.add(String.format("%s\t%s\t%s\t%s\t%s\t%s", ((QueryFileParam)queryParam).getQueryFormat().getHeaderPartStr(),
				"CellType", "cell_p", "reg_p1", "reg_p2", "combined_p"));
		return header;
	}

	public void processNode(final LocFeature node, final Map<String, LocFeature[]> results, final int index) throws IOException {
		regParsers[index].processNode(node, results);
	}
}
