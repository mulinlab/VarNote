package org.mulinlab.varnote.utils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.logging.log4j.Logger;
import org.mulinlab.varnote.config.param.query.QueryFileParam;
import org.mulinlab.varnote.config.run.*;
import org.mulinlab.varnote.operations.decode.LocCodec;
import org.mulinlab.varnote.operations.mapper.*;
import org.mulinlab.varnote.filters.iterator.NoFilterIterator;
import org.mulinlab.varnote.constants.GlobalParameter;
import org.mulinlab.varnote.config.index.IndexWriteConfig;
import org.mulinlab.varnote.operations.index.IndexWriter;
import org.mulinlab.varnote.utils.database.Database;
import org.mulinlab.varnote.utils.mapreduce.MapReduce;
import org.mulinlab.varnote.utils.mapreduce.SimpleMapReduce;
import org.mulinlab.varnote.operations.query.VannoQuery;
import org.mulinlab.varnote.utils.node.LocFeature;

public final class RunFactory {
	public static final String TAB = GlobalParameter.TAB;
	public static final String QUERY_START = GlobalParameter.QUERY_START;
	public static final String OVERLAP_NOTE = GlobalParameter.OVERLAP_NOTE;
	final static Logger logger = LoggingUtils.logger;

	public static void runAnnoFromOverlapFile(final AnnoRunConfig config) {
		QueryFileParam queryParam = (QueryFileParam)config.getQueryParam();

		final int thread = 1;
		config.setThread(thread);
		config.init();

		Map<String, Database> databaseMap = new HashMap<>();
		for (Database db: config.getDatabses()) {
			databaseMap.put(db.getOutName(), db);
		}

		try {
			final NoFilterIterator reader = new NoFilterIterator(config.getOverlapFile(), VannoUtils.checkFileType(config.getOverlapFile()));
			final LocCodec queryCodec = VannoUtils.getDefaultLocCodec(queryParam.getQueryFormat(), true);

			String line, dbOutName, s;
			LocFeature queryNode = null;

			Map<String, List<LocFeature>> dbResultsMap = null;
			List<LocFeature> result;

			int beg;
			while(reader.hasNext()) {
				line = reader.peek();
				if(!line.startsWith(OVERLAP_NOTE)) {
					beg = line.indexOf(TAB);
					if(line.startsWith(QUERY_START)) {
						if(queryNode != null) {
							config.annoRecord(queryNode, convertList(dbResultsMap), thread - 1);
						}
						dbResultsMap = new HashMap<String, List<LocFeature>>();
						queryNode = queryCodec.decode(line.substring(beg + 1));
					} else {
						dbOutName = line.substring(0, beg);
						result = dbResultsMap.get(dbOutName);
						if(result == null) result = new ArrayList<>();
						result.add(databaseMap.get(dbOutName).decode(line.substring(beg + 1)));
						dbResultsMap.put(dbOutName, result);
					}
				}
				reader.next();
			}

			if(queryNode != null) {
				config.annoRecord(queryNode, convertList(dbResultsMap), thread - 1);
			}
			reader.close();
			config.mergeResult();
			if(config.getOutParam() != null) config.getOutParam().printLog();

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static Map<String, LocFeature[]> convertList(final Map<String, List<LocFeature>> map) {
		Map<String, LocFeature[]> convertMap = new HashMap<>();
		for (String key:map.keySet()) {
			convertMap.put(key, map.get(key).toArray(new LocFeature[map.get(key).size()]));
		}
		return convertMap;
	}

	public static void writeIndex(final IndexWriteConfig config) {
		config.init();

		logger.info(String.format("write vanno file for: %s begin...\n", config.getIndexParam().getInput()));
        IndexWriter write = new IndexWriter(config.getIndexParam());
        write.makeIndex();
		logger.info(String.format("write vanno file for: %s end", config.getIndexParam().getInput()));
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

		MapReduce<File, Long> mr = new SimpleMapReduce<File, Long>(thread, ReducerFactory.getCountReducer());
		CounterMapper mapper = null;
		for(int i=0; i<thread; i++) {
			mapper = new CounterMapper(config, i);
			mr.addMapper(mapper);
		}

		mr.getResult();
	}

	public static void runCEPIP(final CEPIPRunConfig config) {

		logger.info(VannoUtils.printLogHeader("RUN CEPIP"));

		config.init();
		int thread = config.getThread();

		MapReduce<File, Long> mr = new SimpleMapReduce<File, Long>(thread, ReducerFactory.getMergeReducer(config));
		CEPIPMapper mapper = null;
		for(int i=0; i<thread; i++) {
			mapper = new CEPIPMapper(config, i);
			mr.addMapper(mapper);
		}

		mr.getResult();
	}

	public static void runPAT(final PATRunConfig config) {

		logger.info(VannoUtils.printLogHeader("RUN PAT"));

		config.init();
		int thread = config.getThread();

		MapReduce<File, Long> mr = new SimpleMapReduce<File, Long>(thread, ReducerFactory.gePATReducer(config));
		PATMapper mapper = null;
		for(int i=0; i<thread; i++) {
			mapper = new PATMapper(config, i);
			mr.addMapper(mapper);
		}

		mr.getResult();
	}

	public static void run(final OverlapRunConfig config) {

		config.init();
		logger.info(VannoUtils.printLogHeader("RUN"));

		int thread = config.getThread();
		MapReduce<File, Long> mr = new SimpleMapReduce<File, Long>(thread, ReducerFactory.getMergeReducer(config));

		AbstractMapper mapper = null;
		for(int i=0; i<thread; i++) {
			if (config instanceof AnnoRunConfig) {
				mapper = new AnnoMapper(config, i);
			} else {
				mapper = new IntersetMapper(config, i);
			}
			mr.addMapper(mapper);
		}

		mr.getResult();
	}
}
