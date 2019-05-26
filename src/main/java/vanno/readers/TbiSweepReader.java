package main.java.vanno.readers;

import java.io.IOException;

import main.java.vanno.bean.database.Database;
import main.java.vanno.bean.database.DatabaseConfig;
import main.java.vanno.bean.database.DatabaseFactory;
import main.java.vanno.bean.node.Node;
import main.java.vanno.bean.query.Log;


public final class TbiSweepReader extends TbiReader {
	
	public TbiSweepReader(final String db) throws IOException {
		this(db, defaultUseJDK, defaultLog);
	}
	
	public TbiSweepReader(final String db, final boolean useJDK, final Log log) throws IOException {
		this(DatabaseFactory.readDatabase(new DatabaseConfig(db)), useJDK, log);
	}
	
	public TbiSweepReader(final Database db) throws IOException {
		this(db, defaultUseJDK, defaultLog);
	}
	
	public TbiSweepReader(final Database db, final boolean useJDK, final Log log) throws IOException {	
		super(db, useJDK, log);
	}
	
	public class SweepIteratorImpl extends TbiIteratorImpl {
		public SweepIteratorImpl(final int tid) {
	    	super(tid);
	    }
	    		 
		@Override
		public String next() throws IOException {
            return mFp.readLine();
		}
	}
	 
    @Override
    public String toString() {
        return "SweepTbiReader: filename:"+getSource();
    }

	@Override
	public void initForChr(final int tid) throws IOException{
		if (tid < 0 || tid >= idx.getmSeqLen()) return;
		
		mFp.seek(idx.getMinOffForChr(tid));
		stack.clearST();
		stack.setIterator(new SweepIteratorImpl(tid));
	}

	@Override
	public boolean query(final Node query) throws IOException {
		if(!super.query(query)) return false;
		stack.findOverlap(query);
		return true;
	}
	
}
