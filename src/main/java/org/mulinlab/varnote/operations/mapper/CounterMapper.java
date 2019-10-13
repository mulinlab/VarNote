package org.mulinlab.varnote.operations.mapper;

import org.mulinlab.varnote.config.param.query.QueryFileParam;
import org.mulinlab.varnote.config.run.CountRunConfig;
import org.mulinlab.varnote.operations.query.VannoQuery;
import org.mulinlab.varnote.operations.readers.query.AbstractFileReader;
import org.mulinlab.varnote.utils.node.LocFeature;

import java.io.IOException;


public final class CounterMapper<T> extends AbstractMapper {

	private Long count;

	public CounterMapper(final CountRunConfig config, final int index) {
		super(config, index);
		queryEngine = new VannoQuery(config.getDatabses(), true);
	}

	public AbstractFileReader getQueryForThread() {
		return ((QueryFileParam)config.getQueryParam()).getThreadReader(index);
	}

	@Override
	public void processResult(final LocFeature node) throws IOException {
		this.count = queryEngine.getResultCount();
	}

	@Override
	public T getResult() {
		return (T)this.count;
	}
}
