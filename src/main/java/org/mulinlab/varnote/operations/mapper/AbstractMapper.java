package org.mulinlab.varnote.operations.mapper;

import org.mulinlab.varnote.config.run.RunConfig;
import org.mulinlab.varnote.operations.query.AbstractQuery;
import org.mulinlab.varnote.utils.mapreduce.Mapper;
import org.mulinlab.varnote.utils.node.Node;
import org.mulinlab.varnote.utils.queryreader.ThreadLineReader;

import java.io.IOException;


public abstract class AbstractMapper<T> implements Mapper<T>{
	protected AbstractQuery queryEngine;
	protected RunConfig config;
	protected Integer index;

	public AbstractMapper(final RunConfig config, final int index) {
		super();
		this.config = config;
		this.index = index;
	}

	public abstract ThreadLineReader getQueryForThread();
	public abstract void processResult(final Node node) throws IOException;

	@Override
	public void doMap() {
		try {
			ThreadLineReader spider = getQueryForThread();
			
			Node node = new Node();
			while((node = spider.nextNode(node)) != null) {
				queryEngine.doQuery(node);
				processResult(node);
			}
			queryEngine.teardown();

		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
