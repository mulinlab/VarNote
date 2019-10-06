package org.mulinlab.varnote.operations.mapper;

import org.mulinlab.varnote.config.param.query.QueryFileParam;
import org.mulinlab.varnote.config.run.CountRunConfig;
import org.mulinlab.varnote.operations.query.VannoQuery;
import org.mulinlab.varnote.utils.node.Node;
import org.mulinlab.varnote.utils.queryreader.ThreadLineReader;

import java.io.IOException;


public final class CounterMapper<T> extends AbstractMapper {

	private Long count;

	public CounterMapper(final CountRunConfig config, final int index) {
		super(config, index);
		queryEngine = new VannoQuery(config.getDatabses(), true);
	}

	public ThreadLineReader getQueryForThread() {
		return ((QueryFileParam)config.getQueryParam()).getSpider(index);
	}

	@Override
	public void processResult(final Node node) throws IOException {
		this.count = queryEngine.getResultCount();
	}

	@Override
	public T getResult() {
		return (T)this.count;
	}
}
