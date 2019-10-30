package org.mulinlab.varnote.utils;


import org.apache.logging.log4j.Logger;
import org.mulinlab.varnote.config.index.IndexWriteConfig;
import org.mulinlab.varnote.config.param.query.QueryFileParam;
import org.mulinlab.varnote.config.run.*;
import org.mulinlab.varnote.constants.GlobalParameter;
import org.mulinlab.varnote.operations.index.IndexWriter;
import org.mulinlab.varnote.operations.mapper.*;
import org.mulinlab.varnote.operations.query.VannoQuery;
import org.mulinlab.varnote.utils.mapreduce.MapReduce;
import org.mulinlab.varnote.utils.mapreduce.Mapper;
import org.mulinlab.varnote.utils.mapreduce.Reducer;
import org.mulinlab.varnote.utils.mapreduce.SimpleMapReduce;

import java.io.File;
import java.io.IOException;
import java.util.List;

public final class ReducerFactory {


	public static Reducer getNullReducer() {
		return new Reducer<File, Mapper<Long>, Long>() {
			public File doReducer(List<Mapper<Long>> mappers) {
				return null;
			}
		};
	}

	public static Reducer getCountReducer() {
		return new Reducer<File, Mapper<Long>, Long>() {
			public File doReducer(List<Mapper<Long>> mappers) {
				long count = 0;
				for (Mapper<Long> mapper : mappers) {
					count = count + mapper.getResult();
				}
				System.out.println("Count=" + count);
				return null;
			}
		};
	}

	public static Reducer gePATReducer(final PATRunConfig config) {
		return new Reducer<File, Mapper<Long>, Long>() {
			public File doReducer(List<Mapper<Long>> mappers) {
				((QueryFileParam)config.getQueryParam()).getFilterParam().printLog();
				System.out.println("total:" + config.getCount());
				return null;
			}
		};
	}

	public static Reducer getMergeReducer(final OverlapRunConfig config) {
		return new Reducer<File, Mapper<Long>, Long>() {
			public File doReducer(List<Mapper<Long>> mappers) {

				final Logger logger = LoggingUtils.logger;
				logger.info(VannoUtils.printLogHeader("MERGE RESULT"));

				config.mergeResult();
				if(config.getOutParam() != null) config.getOutParam().printLog();

				return null;
			}
		};
	}
}
