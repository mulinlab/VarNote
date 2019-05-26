package main.java.vanno.query;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import main.java.vanno.bean.config.run.AbstractConfig;
import main.java.vanno.bean.database.Database;
import main.java.vanno.bean.node.Node;
import main.java.vanno.bean.query.Log;
import main.java.vanno.constants.BasicUtils;
import main.java.vanno.process.CountResultProcess;
import main.java.vanno.readers.AbstractReader;

public abstract class AbstractQuery implements Query{
	protected final static Log defaultlog = BasicUtils.DEFAULT_LOG;
	protected final static boolean defaultUseJDK = BasicUtils.DEFAULT_USE_JDK;
	
	protected final List<Database> dbs;
	protected final boolean useJDK;
	protected final Log log;
	protected List<AbstractReader> readers;

	protected AbstractQuery(final AbstractConfig config) { //, final int threadIndex, final LineReaderBasic queryLineReader
		this(config.getDatabses(), config.isUseJDK(), config.getLog());
	}
	
	protected AbstractQuery(final List<Database> dbs) { //, final int threadIndex, final LineReaderBasic queryLineReader
		this(dbs, defaultUseJDK, defaultlog);
	}
	
	protected AbstractQuery(final List<Database> dbs, final boolean useJDK, final Log log) { //, final int threadIndex, final LineReaderBasic queryLineReader
		super();
		this.dbs = dbs;
		this.useJDK = useJDK;
		this.log = log;
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
