package org.mulinlab.varnote.operations.readers.db;

import java.io.IOException;

import org.mulinlab.varnote.utils.database.Database;

import org.mulinlab.varnote.utils.node.LocFeature;


public final class TbiSweepReader extends TbiReader {

	public TbiSweepReader(final String db) throws IOException {
		super(db);
	}

	public TbiSweepReader(final String db, final boolean isCount) throws IOException {
		super(db, isCount);
	}

	public TbiSweepReader(final Database db) throws IOException {
		super(db);
	}

	public TbiSweepReader(final Database db, final boolean isCount) throws IOException {
		super(db, isCount);
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
	public boolean query(final LocFeature query) throws IOException {
		if(!super.query(query)) return false;
		stack.findOverlap(query);
		return true;
	}
	
}
