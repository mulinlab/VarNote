package org.mulinlab.varnote.operations.mapper;

import org.apache.logging.log4j.Logger;
import org.mulinlab.varnote.config.run.RunConfig;
import org.mulinlab.varnote.filters.iterator.LineFilterIterator;
import org.mulinlab.varnote.operations.query.AbstractQuery;
import org.mulinlab.varnote.operations.readers.query.AbstractFileReader;
import org.mulinlab.varnote.utils.LoggingUtils;
import org.mulinlab.varnote.utils.TimeMetric;
import org.mulinlab.varnote.utils.enumset.VariantType;
import org.mulinlab.varnote.utils.mapreduce.Mapper;
import org.mulinlab.varnote.utils.node.LocFeature;
import java.io.IOException;


public abstract class AbstractMapper<T> implements Mapper<T>{
	final static Logger logger = LoggingUtils.logger;

	protected AbstractQuery queryEngine;
	protected RunConfig config;
	protected Integer index;
	protected TimeMetric timeMetric;

	public AbstractMapper(final RunConfig config, final int index) {
		super();
		this.config = config;
		this.index = index;
		this.timeMetric = new TimeMetric("Thread " + index);
	}

	public abstract AbstractFileReader getQueryForThread();

	public void doQuery(final LocFeature node) throws IOException {
		queryEngine.doQuery(node);
	}

	@Override
	public void doMap() {
		try {
			AbstractFileReader reader = getQueryForThread();
			LineFilterIterator it = reader.getFilterIterator();
			
			LocFeature node;
			while(it.hasNext()) {
				node = it.next();
				if(node != null) {
					if(!isLargeVariants(node)) {
						doQuery(node);
						timeMetric.addRecord(it.getCount());
					} else {
						printLVLog(node);
					}
				}
			}
			queryEngine.teardown();
			timeMetric.doEnd(it.getCount());
			timeMetric.printLVCount();
			it.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	protected void printLVLog(final LocFeature node) {
		timeMetric.addLVCount();
		if(node.vt == VariantType.OML) {
			logger.info(String.format("%s is not processed because of over the max length of variant, please set option --maxVariantLength to change it.", node.chr + ":" + node.beg + "-" + node.end));
		} else if(node.vt == VariantType.LV) {
			logger.info(node.alt + " is not processed, please turn option --allowLargeVariants on to enable it.");
		}
	}

	protected boolean isLargeVariants(final LocFeature node) {
		if(node.vt == VariantType.NM) {
			return false;
		} else {
			return true;
		}
	}

	public long getCount() {
		return timeMetric.getCount();
	}

	public long getLvCount() {
		return timeMetric.getLvCount();
	}
}
