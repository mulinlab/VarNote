package org.mulinlab.varnote.operations.query;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.mulinlab.varnote.constants.GlobalParameter;
import org.mulinlab.varnote.utils.database.Database;
import org.mulinlab.varnote.operations.process.CountResultProcess;
import org.mulinlab.varnote.operations.readers.db.AbstractDBReader;
import org.mulinlab.varnote.utils.node.LocFeature;

public abstract class AbstractQuery implements Query{

	protected final List<Database> dbs;
	protected List<AbstractDBReader> readers;
	protected boolean isCount = GlobalParameter.DEFAULT_IS_COUNT;

	protected Map<String, String[]> results = new HashMap<String, String[]>();
	protected Map<String, LocFeature[]> resultsFeatures = new HashMap<String, LocFeature[]>();

	protected AbstractQuery(final List<Database> dbs, final boolean isCount) { //, final int threadIndex, final ThreadReader queryLineReader
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
	
	public void doQuery(final LocFeature node) throws IOException {
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
			for (AbstractDBReader tr : readers) {
				tr.close();
			}
		}
	}
	
	@Override
	public Map<String, String[]> getResults() {
		List<String> hits;
		for(int i=0; i< readers.size(); i++) {
			hits = readers.get(i).getResults();
			if(hits.size() > 0)
				results.put(readers.get(i).getDb().getOutName(), hits.toArray(new String[hits.size()]));
			else {
				results.put(readers.get(i).getDb().getOutName(), null);
			}
		}

		return results;
	}

	@Override
	public Map<String, LocFeature[]> getResultFeatures(){
		List<String> hits;
		LocFeature[] locFeatures;

		for(int i=0; i< readers.size(); i++) {
			hits = readers.get(i).getResults();
			if(hits.size() > 0) {
				locFeatures = new LocFeature[hits.size()];
				for (int j = 0; j < hits.size(); j++) {
					locFeatures[j] = readers.get(i).getDb().decode(hits.get(j)).clone();
				}
				resultsFeatures.put(readers.get(i).getDb().getOutName(), locFeatures);
			} else {
				resultsFeatures.put(readers.get(i).getDb().getOutName(), null);
			}
		}
		return resultsFeatures;
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
