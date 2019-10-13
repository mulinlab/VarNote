package org.mulinlab.varnote.operations.mapper;
import org.mulinlab.varnote.config.param.RunParam;
import org.mulinlab.varnote.config.param.query.QueryFileParam;
import org.mulinlab.varnote.config.run.OverlapRunConfig;
import org.mulinlab.varnote.operations.readers.query.AbstractFileReader;
import org.mulinlab.varnote.utils.database.Database;
import org.mulinlab.varnote.utils.enumset.Mode;
import org.mulinlab.varnote.utils.node.LocFeature;
import org.mulinlab.varnote.operations.query.VannoQuery;
import org.mulinlab.varnote.operations.query.SweepQuery;
import org.mulinlab.varnote.operations.query.TabixQuery;

import java.io.IOException;
import java.util.List;


public final class IntersetMapper<T> extends AbstractMapper{

	public IntersetMapper(final OverlapRunConfig config, final int index) {
		super(config, index);

		final RunParam runParam = config.getRunParam();
		final List<Database> dbs = config.getDatabses();
		if(runParam.getMode() == Mode.TABIX) {
			queryEngine = new TabixQuery(dbs);
		} else if(runParam.getMode() == Mode.SWEEP) {
			queryEngine = new SweepQuery(dbs);
		} else {
			queryEngine = new VannoQuery(dbs, false);
		}
	}

	@Override
	public AbstractFileReader getQueryForThread() {
		return ((QueryFileParam)config.getQueryParam()).getThreadReader(index);
	}

	@Override
	public void processResult(final LocFeature node) throws IOException {
//		System.out.println(node.toString() + "," + queryEngine.getResults().size());
		((OverlapRunConfig)this.config).printRecord(node, queryEngine.getResults(), index);
	}

	@Override
	public T getResult() {
		return null;
	}
}
