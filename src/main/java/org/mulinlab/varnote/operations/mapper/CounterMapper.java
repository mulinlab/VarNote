package org.mulinlab.varnote.operations.mapper;

import org.mulinlab.varnote.config.param.query.QueryFileParam;
import org.mulinlab.varnote.config.run.AnnoRunConfig;
import org.mulinlab.varnote.config.run.CountRunConfig;
import org.mulinlab.varnote.operations.query.VannoQuery;
import org.mulinlab.varnote.operations.readers.query.AbstractFileReader;
import org.mulinlab.varnote.utils.node.LocFeature;

import java.io.IOException;


public final class CounterMapper extends AbstractMapper {

	public CounterMapper(final CountRunConfig config, final int index) {
		super(config, index);
		queryEngine = new VannoQuery(config.getDatabses(), true);
	}

	public AbstractFileReader getQueryForThread() {
		return ((QueryFileParam)config.getQueryParam()).getThreadReader(index);
	}


	@Override
	public void doQuery(final LocFeature node) throws IOException {
		super.doQuery(node);
		((CountRunConfig)this.config).processNode(node, queryEngine.getResultCount(), index);
	};


	@Override
	public Object getResult() {
		return null;
	}
}
