package org.mulinlab.varnote.utils;


import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.Logger;
import org.mulinlab.varnote.config.param.output.AnnoOutParam;
import org.mulinlab.varnote.config.param.query.QueryFileParam;
import org.mulinlab.varnote.config.run.AnnoRunConfig;
import org.mulinlab.varnote.config.run.CountRunConfig;
import org.mulinlab.varnote.config.run.OverlapRunConfig;
import org.mulinlab.varnote.config.run.QueryRegionConfig;
import org.mulinlab.varnote.operations.mapper.CounterMapper;
import org.mulinlab.varnote.utils.node.NodeFactory;
import org.mulinlab.varnote.utils.node.RefNode;
import org.mulinlab.varnote.utils.queryreader.LineIteratorImpl;
import org.mulinlab.varnote.config.io.PrintGZ;
import org.mulinlab.varnote.constants.GlobalParameter;
import org.mulinlab.varnote.config.index.IndexWriteConfig;
import org.mulinlab.varnote.operations.index.IndexWriter;
import org.mulinlab.varnote.utils.mapreduce.MapReduce;
import org.mulinlab.varnote.utils.mapreduce.Mapper;
import org.mulinlab.varnote.utils.mapreduce.Reducer;
import org.mulinlab.varnote.utils.mapreduce.SimpleMapReduce;
import org.mulinlab.varnote.operations.query.VannoQuery;
import org.mulinlab.varnote.operations.mapper.IntersetMapper;

public final class RunFactory {
	public static final String TAB = GlobalParameter.TAB;
	public static final String QUERY_START = GlobalParameter.QUERY_START;
	public static final String OVERLAP_NOTE = GlobalParameter.OVERLAP_NOTE;



	public static void runAnnoFromOverlapFile(final AnnoRunConfig config) {
		AnnoOutParam outParam = (AnnoOutParam)config.getOutParam();
		QueryFileParam queryParam = (QueryFileParam)config.getQueryParam();

		config.init();

		int thread = 1;
		config.setThread(thread);

		try {
			final LineIteratorImpl reader = new LineIteratorImpl(config.getOverlapFile(), VannoUtils.checkFileType(config.getOverlapFile()));
			final PrintGZ out = new PrintGZ(outParam.getOutputPath(), outParam.isGzip());
			
//			if(outParam.isGzip()) {
//				outParam.printAnnoHeader(out.getGzOut(), config.getHeader());
//			} else {
//				outParam.printAnnoHeader(out.getNonGZWriter(), config.getHeader());
//			}
			
			String line, dbOutName, s;
			RefNode queryNode = null;
			Map<String, List<String>> dbResultsMap = null;
			List<String> result;
			int beg;
			while(((line = reader.advance()) != null)) {
				if(!line.startsWith(OVERLAP_NOTE)) {
					beg = line.indexOf(TAB);
					if(line.startsWith(QUERY_START)) {
						if(queryNode != null) {
							s = config.doQuery(queryNode, dbResultsMap, (thread-1));
							if(s != null) out.writeLine(s);
						}
						dbResultsMap = new HashMap<String, List<String>>();
						queryNode = NodeFactory.createRefAlt(line.substring(beg + 1), queryParam.getQueryFormat());
					} else {
						dbOutName = line.substring(0, beg);
						result = dbResultsMap.get(dbOutName);
						if(result == null) result = new ArrayList<>();
						result.add(line.substring(beg + 1));
						dbResultsMap.put(dbOutName, result);
					}
				}
			}
	
			if(queryNode != null) {
				s = config.doQuery(queryNode, dbResultsMap, (thread - 1));
				if(s != null) out.writeLine(s);		
			}
			
			out.close();	
			reader.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	

	public static void writeIndex(final IndexWriteConfig config) {
		config.init();
        System.out.println("write vanno file for: " + config.getIndexParam().getInput() + " begin...\n");
        IndexWriter write = new IndexWriter(config);
        write.makeIndex();
        System.out.println("write vanno file for: " + config.getIndexParam().getInput() + " end");
	}
	
	public static void runQuery(final QueryRegionConfig queryRegionConfig) {
		queryRegionConfig.init();
		try {
			VannoQuery queryEngine = new VannoQuery(queryRegionConfig.getDatabses());
			queryEngine.doQuery(queryRegionConfig.getQueryRegion());
			queryRegionConfig.printRecord(queryEngine.getResults());
			queryEngine.teardown();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void runCount(final CountRunConfig config) {
		config.init();

		int thread = config.getThread();
		Reducer<File, Mapper<Long>, Long> r = new Reducer<File, Mapper<Long>, Long>() {
			public File doReducer(List<Mapper<Long>> mappers) {
				long count = 0;
				for (Mapper<Long> mapper : mappers) {
					count = count + mapper.getResult();
				}
				System.out.println("Count=" + count);
				return null;
			}
		};

		MapReduce<File, Long> mr = new SimpleMapReduce<File, Long>(thread, r);
		CounterMapper<Long> mapper = null;
		for(int i=0; i<thread; i++) {
			mapper = new CounterMapper<Long>(config, i);
			mr.addMapper(mapper);
		}

		mr.getResult();
	}
	
	public static void run(final OverlapRunConfig config) {
		final Logger logger = LoggingUtils.logger;

		long t1 = System.currentTimeMillis();
		config.init();

		int thread = config.getThread();

		logger.info(VannoUtils.printLogHeader("RUN"));

		Reducer<File, Mapper<Long>, Long> r = new Reducer<File, Mapper<Long>, Long>() {
			public File doReducer(List<Mapper<Long>> mappers) {
				logger.info(VannoUtils.printLogHeader("MERGE RESULT"));
				config.mergeResult();
				long t2 = System.currentTimeMillis();
//				if(t2 - t1 > 1000) {
//					config.getLog().printStrNon("Time:" + (t2 - t1)/1000 + "s");
//				} else {
//					config.getLog().printStrNon("Time:" + (t2 - t1) + "ms");
//				}
				if(config.getOutParam() != null) config.getOutParam().printLog();
				logger.info(String.format("\n\nDone! Time: %ds\n", (t2 - t1)/1000));
				return null;
			}	
		};
		
		MapReduce<File, Long> mr = new SimpleMapReduce<File, Long>(thread, r);
		IntersetMapper<Long> mapper = null;
		for(int i=0; i<thread; i++) {
			mapper = new IntersetMapper<>(config, i);
			mr.addMapper(mapper);
		}
		
		mr.getResult();
	}
	
	public static void main(String[] args) {
		
	}
}
