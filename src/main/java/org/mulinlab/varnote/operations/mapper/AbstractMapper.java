package org.mulinlab.varnote.operations.mapper;

import org.mulinlab.varnote.config.run.RunConfig;
import org.mulinlab.varnote.filters.iterator.LineFilterIterator;
import org.mulinlab.varnote.operations.query.AbstractQuery;
import org.mulinlab.varnote.operations.readers.query.AbstractFileReader;
import org.mulinlab.varnote.utils.mapreduce.Mapper;
import org.mulinlab.varnote.utils.node.LocFeature;

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

	public abstract AbstractFileReader getQueryForThread();
	public abstract void processResult(final LocFeature node) throws IOException;

	@Override
	public void doMap() {
		try {
			AbstractFileReader reader = getQueryForThread();
			LineFilterIterator it = reader.getFilterIterator();
			
			LocFeature node;
			while(it.hasNext()) {
				node = it.next();
				if(node != null) {
					queryEngine.doQuery(node);
					processResult(node);
				}
			}
			queryEngine.teardown();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
