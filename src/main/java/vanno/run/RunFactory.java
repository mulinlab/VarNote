package main.java.vanno.run;


import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import main.java.vanno.bean.config.config.run.AnnoRunReadConfig;
import main.java.vanno.bean.config.config.run.OverlapReadConfig;
import main.java.vanno.bean.config.run.AnnoRunConfig;
import main.java.vanno.bean.config.run.OverlapRunConfig;
import main.java.vanno.bean.config.run.QueryRunConfig;
import main.java.vanno.bean.config.run.AnnoRunConfig.AnnoOutFormat;
import main.java.vanno.bean.database.DatabaseConfig;
import main.java.vanno.bean.database.Database.IndexType;
import main.java.vanno.bean.database.DatabaseConfig.IntersectType;
import main.java.vanno.bean.format.Format;
import main.java.vanno.bean.node.NodeFactory;
import main.java.vanno.bean.node.RefNode;
import main.java.vanno.bean.query.LineIteratorImpl;
import main.java.vanno.bean.query.Log;
import main.java.vanno.bean.query.PrintGZ;
import main.java.vanno.bean.query.Query;
import main.java.vanno.constants.BasicUtils;
import main.java.vanno.constants.VannoUtils;
import main.java.vanno.index.IndexWriteConfig;
import main.java.vanno.index.IndexWriter;
import main.java.vanno.mapreduce.MapReduce;
import main.java.vanno.mapreduce.Mapper;
import main.java.vanno.mapreduce.Reducer;
import main.java.vanno.mapreduce.SimpleMapReduce;
import main.java.vanno.query.VannoQuery;
import main.java.vanno.readers.VannoMixReader;

public final class RunFactory {
	public static final String TAB = BasicUtils.TAB;
	public static final String QUERY_START = BasicUtils.QUERY_START;
	public static final String OVERLAP_NOTE = BasicUtils.OVERLAP_NOTE;
	
	public static void runAnnoFromOverlapFile(final AnnoRunConfig config) {
		config.init();

		int thread = 1;
		config.setThread(thread);

		try {
			final LineIteratorImpl reader = new LineIteratorImpl(config.getOverlapFile(), true, VannoUtils.checkFileType(config.getOverlapFile()));
			final PrintGZ out = new PrintGZ(config.getOutput().getAnnoOutput(), config.isGzip());
			
			if(config.isGzip()) {
				config.getOutput().printAnnoHeader(out.getGzOut(), config.getHeader());
			} else {
				config.getOutput().printAnnoHeader(out.getNonGZWriter(), config.getHeader());
			}
			
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
						queryNode = NodeFactory.createRefAlt(line.substring(beg + 1), config.getQuery().getQueryFormat());
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
        System.out.println("write vanno file for: " + config.getInput() + " begin...\n");
        IndexWriter write = new IndexWriter(config);
	    	write.makeIndex();
	    	System.out.println("write vanno file for: " + config.getInput() + " end");
	}
	
	public static void runQuery(final QueryRunConfig queryRunConfig) {
		queryRunConfig.init();
		try {
			VannoQuery queryEngine = new VannoQuery(queryRunConfig);
			queryEngine.doQuery(queryRunConfig.getQueryRegion());
			queryRunConfig.printRecord(queryEngine.getResults());
			queryEngine.teardown();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static void run(final OverlapRunConfig config) {
		long t1 = System.currentTimeMillis();
		config.init();

		int thread = config.getThread();	
		if(!config.isCount()) config.getOutput().setPrintter(thread, config.isGzip()); 
		
		config.getLog().printStrWhite("\n\n----------------------------------------------------  RUN  ------------------------------------------------------------------");
		Reducer<File, Mapper<Long>, Long> r = new Reducer<File, Mapper<Long>, Long>() {
			public File doReducer(List<Mapper<Long>> mappers) {
				config.getLog().printStrWhite("\n\n----------------------------------------------------  MERGE RESULT  ----------------------------------------------------------");
				if(config.isCount()) {
					long count = 0;
					for (Mapper<Long> mapper : mappers) {
						count = count + mapper.getResult();
					}
					System.out.println("Count=" + count);
				} else {
					config.mergeResult();
				}

				long t2 = System.currentTimeMillis();
				if(t2 - t1 > 1000) {
					config.getLog().printStrNon("Time:" + (t2 - t1)/1000 + "s");
				} else {
					config.getLog().printStrNon("Time:" + (t2 - t1) + "ms");
				}
				
				
				return null;
			}	
		};
		
		MapReduce<File, Long> mr = new SimpleMapReduce<File, Long>(thread, r);
		VannoMapper<Long> mapper = null;
		for(int i=0; i<thread; i++) {
			mapper = new VannoMapper<>(config, i);
			mr.addMapper(mapper);
		}
		
		mr.getResult();
	}
	
	public static void main(String[] args) {
		
	}

}
