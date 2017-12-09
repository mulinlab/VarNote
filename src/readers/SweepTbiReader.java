package readers;

import htsjdk.tribble.util.ParsingUtils;
import htsjdk.tribble.util.TabixUtils;
import htsjdk.tribble.util.TabixUtils.TIndex;

import java.io.IOException;

import stack.AbstractReaderStack;
import bean.index.Index;
import bean.index.TbiIndex;
import bean.node.Node;
import bean.node.NodeFactory;

public class SweepTbiReader extends SweepReader {

    public SweepTbiReader(final String fn, final AbstractReaderStack stack) throws IOException {
    	 this(fn, ParsingUtils.appendToPath(fn, TabixUtils.STANDARD_INDEX_EXTENSION), stack);
    }
    
    public SweepTbiReader(final String fn, final String idxFn, final AbstractReaderStack stack) throws IOException {
		this(fn, new TbiIndex(idxFn), stack);
	}

    public SweepTbiReader(final String fn, final Index idx, final AbstractReaderStack stack) throws IOException {
		super(fn, idx, stack);
	}
	
	public TIndex[] getIndex() {
		return ((TbiIndex)idx).getmIndex();
	}
	
	protected class SweepIteratorImpl implements Iterator {
		private int tid;
		private boolean iseof;
		
	    protected SweepIteratorImpl(final int tid) {
	    	iseof = false;
	    	this.tid = tid;
	    }
	    		 
		@Override
		public String next() throws IOException {
            return mFp.readLine();
		}
		
		@Override
		public Node nextNode() throws IOException {
			if (iseof) return null;
			
			String s = this.next();	
	
			if(s == null) {
				iseof = true;
				return null;
			} else {
				Node t = NodeFactory.createBasicWithOri(s, idx.getFormatWithRef());
				if(chr2tid(t.chr) != tid) {
					iseof = true;
					return null;
				}
				return t;
			}
		}
	}
	 
    @Override
    public String toString() {
        return "SweepTbiReader: filename:"+getSource();
    }

	@Override
	public void initForChr(String chr) {
		int tid = chr2tid(chr);
    	if(tid < 0 || tid >= idx.getmSeqLen()) return;
    	try {
			mFp.seek(idx.getMinOffForChr(tid));
		} catch (IOException e) {
			e.printStackTrace();
		}	
    	
		stack.clearST();
		stack.setIterator(new SweepIteratorImpl(tid));
	}

	@Override
	public void doQuery(Node query) {
		stack.findOverlap(query);
	}
}
