package org.mulinlab.varnote.operations.query;


import org.mulinlab.varnote.utils.enumset.IndexType;
import org.mulinlab.varnote.utils.enumset.IntersectType;
import htsjdk.tribble.readers.TabixReader;
import htsjdk.tribble.readers.TabixReader.Iterator;
import org.mulinlab.varnote.utils.database.Database;
import org.mulinlab.varnote.utils.node.LocFeature;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class TabixQuery extends AbstractQuery{

	private List<TabixReader> tbiReaders;
	private List<Database> databases;

	private Map<String, String[]> results = new HashMap<String, String[]>();
	
	public TabixQuery(final List<Database> dbs) {
		super(dbs, false);
	}

	public void init() throws IOException {
		super.init(); 
		
		tbiReaders = new ArrayList<TabixReader>(dbs.size());
		for (int i = 0; i < this.dbs.size(); i++) {
			this.dbs.get(i).setDefaultLocCodec(false);

			if(this.dbs.get(i).getConfig().getIndexType() == IndexType.TBI) {
				tbiReaders.add(new TabixReader(this.dbs.get(i).getConfig().getDbPath(), this.dbs.get(i).getDbIndexPath()));
			} else {
				throw new IllegalArgumentException("You must have a tabix index file(.tbi) first to use mode 0!");
			}
		}

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
	public Map<String, String[]> getResults() {
		return results;
	}
	
	@Override
	public void doQuery(final LocFeature node) throws IOException {
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
	
	public void randomAccessQuery(LocFeature node) {
		try {
			Iterator it;
			List<String> list;
			Database db;

			for (int i = 0; i < databases.size(); i++) {

				db = databases.get(i);
				if(db.getConfig().getIntersect() == IntersectType.FULLCLOASE) {
					it = tbiReaders.get(i).query(node.chr, node.beg-1, node.end+1);
				} else {
					it = tbiReaders.get(i).query(node.chr, node.beg, node.end);
				}
				
				list = new ArrayList<String>();
				LocFeature feature;
				String s = null;

				while((s = it.next()) != null) {
					if(db.getConfig().getIntersect() == IntersectType.EXACT) {
						feature = db.decode(s);
						if((node.beg == feature.beg) && (node.end == feature.end)) {
							list.add(s);
						}
					} else {
						list.add(s);
					}
				}

				if(list.size() > 0) {
					results.put(db.getConfig().getOutName(), list.toArray(new String[list.size()]));
				} else {
					results.put(db.getConfig().getOutName(), null);
				}
			}

//			printResult(node, results);	
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
