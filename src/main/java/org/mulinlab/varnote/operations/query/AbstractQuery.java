package org.mulinlab.varnote.operations.query;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.mulinlab.varnote.constants.GlobalParameter;
import org.mulinlab.varnote.utils.database.Database;
import org.mulinlab.varnote.operations.process.CountResultProcess;
import org.mulinlab.varnote.operations.readers.AbstractReader;
import org.mulinlab.varnote.utils.node.Node;

public abstract class AbstractQuery implements Query{


	protected final List<Database> dbs;
	protected List<AbstractReader> readers;
	protected boolean isCount = GlobalParameter.DEFAULT_IS_COUNT;
	
	protected AbstractQuery(final List<Database> dbs, final boolean isCount) { //, final int threadIndex, final ThreadLineReader queryLineReader
		super();
		this.dbs = dbs;
		this.isCount = isCount;
		try {
			this.init();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	protected void init() throws IOException {
	}
	
	public void doQuery(final Node node) throws IOException {
		if(readers != null)
			for(int i=0; i<readers.size(); i++) {
				readers.get(i).query(node);
			}
	}
	
	public void doQuery(final String res) throws IOException {
		if(readers != null)
			for(int i=0; i<readers.size(); i++) {
				readers.get(i).query(res);
			}
	}
	
	public void teardown() {
		if(this.readers != null && this.readers.size() > 0) {
			for (AbstractReader tr : readers) {
				tr.close();
			}
		}
	}

	
	@Override
	public Map<String, List<String>> getResults() {
		Map<String, List<String>> results = new HashMap<String, List<String>>();

		List<String> hits;
		if(readers != null) {
			for(int i=0; i< readers.size(); i++) {
				hits = readers.get(i).getResults();
				if(hits.size() > 0)
					results.put(readers.get(i).getDb().getConfig().getOutName(), readers.get(i).getResults());
			}
		}
		return results;
	}
	
	@Override
	public long getResultCount() {
		long count = 0;
		for(int i=0; i<readers.size(); i++) {
			count = count + ((CountResultProcess)readers.get(i).getProcess()).getResults();
		}
		return count;
	}
}
