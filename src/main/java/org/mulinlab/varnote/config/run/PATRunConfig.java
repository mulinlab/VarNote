package org.mulinlab.varnote.config.run;

import org.mulinlab.varnote.config.param.DBParam;
import org.mulinlab.varnote.config.param.FilterParam;
import org.mulinlab.varnote.config.param.query.QueryFileParam;
import org.mulinlab.varnote.config.parser.PATParser;
import org.mulinlab.varnote.config.parser.ResultParser;
import org.mulinlab.varnote.filters.mendelian.MendelianInheritanceADFilter;
import org.mulinlab.varnote.filters.query.gt.DepthFilter;
import org.mulinlab.varnote.filters.query.gt.GenotypeQualityFilter;
import org.mulinlab.varnote.utils.JannovarUtils;
import org.mulinlab.varnote.utils.database.Database;
import org.mulinlab.varnote.utils.enumset.IntersectType;
import org.mulinlab.varnote.utils.format.Format;
import org.mulinlab.varnote.utils.node.LocFeature;
import org.mulinlab.varnote.utils.pedigree.PedFiles;
import org.mulinlab.varnote.utils.pedigree.Pedigree;
import org.mulinlab.varnote.utils.pedigree.PedigreeConverter;
import java.io.File;
import java.util.List;
import java.util.Map;

public final class PATRunConfig extends AdvanceToolRunConfig {

	private final static String DEFAULT_OUT_FILE_SUFFIX = ".pat.txt";


	public PATRunConfig(final QueryFileParam query, final List<DBParam> dbConfigs, final JannovarUtils jannovarUtils, final File pedigree) {
		super(query, dbConfigs, jannovarUtils);
		query.setFilterParam(getFilterParam(pedigree));

		requiredDB.put(GENOMAD_LABEL, IntersectType.EXACT);
		requiredDB.put(REGBASE_MAP_LABEL, IntersectType.EXACT);
		requiredDB.put(DBNSFP_LABEL, IntersectType.EXACT);
	}

	@Override
	protected void initDB() {
		super.initDB();
	}

	@Override
	protected void initOutput() {
		outParam.setOutFileSuffix(DEFAULT_OUT_FILE_SUFFIX);
	}

	@Override
	protected void initOther() {
		super.initOther();
		super.initDatabaseByDefault();

		for (Database db: databses) {
			if(db.getOutName().equals(DBNSFP_LABEL)) {
				Format format = db.getFormat();
				format.sequenceColumn = 1;
				format.startPositionColumn = 2;
				format.endPositionColumn = 2;
			}
		}

		final int threadSize = ((QueryFileParam)queryParam).getThreadSize();
		parsers = new ResultParser[threadSize];
		for (int i = 0; i < threadSize; i++) {
			parsers[i] = new PATParser();
		}
	}

	@Override
	public List<String> getHeader() {
		return null;
	}

	public void processNode(final LocFeature node, final Map<String, LocFeature[]> results, final int index) {
		parsers[index].processNode(node, results);
	}

	public FilterParam getFilterParam(final File pedigreeFile) {
		Pedigree pedigree = PedFiles.readPedigree(pedigreeFile.toPath());

		FilterParam filterParam = new FilterParam();
		filterParam.setMiFilter(new MendelianInheritanceADFilter(PedigreeConverter.convertToJannovarPedigree(pedigree)));
		filterParam.addGenotypeFilters(new DepthFilter(4));
		filterParam.addGenotypeFilters(new GenotypeQualityFilter(20));

		return filterParam;
	}

	public int getCount() {
		int c = 0;
		for (int i = 0; i < parsers.length; i++) {
			c += ((PATParser)parsers[i]).getCount();
		}
		return c;
	}

	public void mergeResult() {
		for (int i = 0; i < parsers.length; i++) {
			System.out.println(((PATParser)parsers[i]).getResults().size());
//			for (LocFeature locFeature: ((PATParser)patParsers[i]).getResults()) {
//				System.out.println(locFeature.getOrigStr());
//			}
		}

		((QueryFileParam)queryParam).getFilterParam().printLog();
	}
}
