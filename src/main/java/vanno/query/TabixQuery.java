package main.java.vanno.query;


import htsjdk.tribble.readers.TabixReader;
import htsjdk.tribble.readers.TabixReader.Iterator;
import main.java.vanno.bean.config.run.AbstractConfig;
import main.java.vanno.bean.database.Database;
import main.java.vanno.bean.database.Database.IndexType;
import main.java.vanno.bean.database.DatabaseConfig.IntersectType;
import main.java.vanno.bean.node.Node;
import main.java.vanno.bean.node.NodeFactory;
import main.java.vanno.bean.query.Log;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class TabixQuery extends AbstractQuery{

	private List<TabixReader> tbiReaders;
	private List<Database> dbFiles;
	private ByteArrayOutputStream bufStream;
	private int max;
	private Map<String, List<String>> results;
	
	public TabixQuery(final AbstractConfig config) {
		super(config);
	}
	
	public TabixQuery(final List<Database> dbs) {
		super(dbs);
	}
	
	public TabixQuery(final List<Database> dbs, final boolean useJDK, final Log log) {
		super(dbs, useJDK, log);
	}

	public void init() throws IOException {
		super.init(); 
		
		tbiReaders = new ArrayList<TabixReader>(dbs.size());
		for (int i = 0; i < this.dbs.size(); i++) {
			if(this.dbs.get(i).getConfig().getIndexType() == IndexType.TBI) {
				tbiReaders.add(new TabixReader(this.dbs.get(i).getConfig().getDbPath(), this.dbs.get(i).getDbIndexPath()));
			} else {
				throw new IllegalArgumentException("You must have a tabix index file(.tbi) first to use mode 0!");
			}
		}
		
		max = 8192;
		bufStream = new ByteArrayOutputStream(this.max);
	}
	
	@Override
	public void teardown() {
		if(this.tbiReaders != null && this.tbiReaders.size() > 0) {
			for (TabixReader reader : tbiReaders) {
				reader.close();
			}
		}
	}
	
	@Override
	public Map<String, List<String>> getResults() {
		return results;
	}
	
	@Override
	public void doQuery(final Node node) throws IOException {
		this.randomAccessQuery(node);
	}
	
//	@Override
//	public void doQuery() {
//		try {
//			while((node = queryLineReader.nextNodeSpider(node)) != null) {
//				this.randomAccessQuery(node);
//			}
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
//	}
	
	public void randomAccessQuery(Node node) {
		try {
			Iterator it ;
			results = new HashMap<String, List<String>>();
			List<String> list;
			for (int i = 0; i < dbFiles.size(); i++) {
				
				if(dbFiles.get(i).getConfig().getIntersect() == IntersectType.FULLCLOASE) {
					it = tbiReaders.get(i).query(node.chr, node.beg-1, node.end+1);
				} else {
					it = tbiReaders.get(i).query(node.chr, node.beg, node.end);
				}
				
				list = new ArrayList<String>();
				Node db;
				String s = null;
				while((s = it.next()) != null) {
					if(dbFiles.get(i).getConfig().getIntersect() == IntersectType.EXACT) {
						if(s.length() > this.max) {
							max = s.length() + 500;
							bufStream = new ByteArrayOutputStream(this.max);
						}
						db = NodeFactory.createBasic(s, dbFiles.get(i).getFormat(), node, bufStream);
						if((node.beg == db.beg) && (node.end == db.end)) {
							list.add(s);
						}
					} else {
						list.add(s);
					}
				}
				results.put(dbFiles.get(i).getConfig().getOutName(), list);
			}
//			printResult(node, results);	
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
