package readers;

import htsjdk.tribble.util.ParsingUtils;
import htsjdk.tribble.util.TabixUtils;
import htsjdk.tribble.util.TabixUtils.TIndex;
import htsjdk.tribble.util.TabixUtils.TPair64;

import java.io.IOException;
import java.util.Arrays;

import constants.BasicUtils;
import process.ProcessResult;
import stack.AbstractReaderStack;
import bean.index.Index;
import bean.index.TbiIndex;
import bean.node.Node;
import bean.node.NodeFactory;
import bean.query.MixQueryBin;

public class MixTbiReader extends MixReader{
	private final static int MAX_BIN = 37450;
	private final static int TAD_LIDX_SHIFT = 14;
	private MixQueryBin queryBin;
	private int pb_num;
	
	public MixTbiReader(final String fn, final AbstractReaderStack stack) throws IOException {
		this(fn, ParsingUtils.appendToPath(fn, TabixUtils.STANDARD_INDEX_EXTENSION), stack);
	}
	
	public MixTbiReader(final String fn, final String idxFn, final AbstractReaderStack stack) throws IOException {
		this(fn, new TbiIndex(idxFn), stack);
	}
	
	public MixTbiReader(final String fn, final Index idx, final AbstractReaderStack stack) throws IOException {
		super(fn, idx, stack);
	}
	
	@Override
	public void initForChr(String chr) {
		pb_num = -1;
		queryBin = new MixQueryBin();
	}
	
	@Override
	public void doQuery(Node query) {
		 final int b_num = queryBin.computeBinBumber(query);
		 if(b_num != pb_num) {
			 if(pb_num > 0) doQueryBin(queryBin);
			 queryBin = new MixQueryBin();
		 }
		 queryBin.addNode(query);
		 pb_num = b_num;
	}
	
	@Override
	public void doEnd() {
		if(pb_num > 0) doQueryBin(queryBin);
	}
	
	public TIndex[] getIndex() {
		return ((TbiIndex)idx).getmIndex();
	}

	public TPair64[] getBins(MixQueryBin queryBin) {
	 	TPair64[] off, chunks;
        long min_off;
        
        int tid = chr2tid(queryBin.getChr());
		if(tid < 0 || tid >= getIndex().length) return null;
		
		TIndex idx = getIndex()[tid];

        int[] bins = new int[MAX_BIN];
        int i, l, n_off, n_bins = BasicUtils.reg2bins(queryBin.getMin(), queryBin.getMax(), bins);
        if (idx.l.length > 0)
            min_off = (queryBin.getMin() >> TAD_LIDX_SHIFT >= idx.l.length) ? idx.l[idx.l.length - 1] : idx.l[queryBin.getMin() >> TAD_LIDX_SHIFT];
        else min_off = 0;
        for (i = n_off = 0; i < n_bins; ++i) {
            if ((chunks = idx.b.get(bins[i])) != null) {
            	n_off += chunks.length;
            }
        }
        
        if (n_off == 0) return null;
        off = new TPair64[n_off];
        for (i = n_off = 0; i < n_bins; ++i)
            if ((chunks = idx.b.get(bins[i])) != null)
                for (int j = 0; j < chunks.length; ++j)
                    if (less64(min_off, chunks[j].v)) {
                    	 off[n_off++] = new TPair64(chunks[j]);
                    }
                       
        Arrays.sort(off, 0, n_off);
        // resolve completely contained adjacent blocks
        for (i = 1, l = 0; i < n_off; ++i) {
            if (less64(off[l].v, off[i].v)) {
                ++l;
                off[l].u = off[i].u;
                off[l].v = off[i].v;
            }
        }
        n_off = l + 1;
        // resolve overlaps between adjacent blocks; this may happen due to the merge in indexing
        for (i = 1; i < n_off; ++i)
            if (!less64(off[i - 1].v, off[i].u)) off[i - 1].v = off[i].u;
        // merge adjacent blocks
        for (i = 1, l = 0; i < n_off; ++i) {
            if (off[l].v >> 16 == off[i].u >> 16) off[l].v = off[i].v;
            else {
                ++l;
                off[l].u = off[i].u;
                off[l].v = off[i].v;
            }
        }
        n_off = l + 1;
        // return
        TPair64[] ret = new TPair64[n_off];
        for (i = 0; i < n_off; ++i) {
            if (off[i] != null) ret[i] = new TPair64(off[i].u, off[i].v); // in C, this is inefficient
        }
        if (ret.length == 0 || (ret.length == 1 && ret[0] == null))
            return null;
        return ret;
	}
	
	protected class MixIteratorImpl implements Iterator {
		 private int i;
		 private TPair64[] off;
	     private long curr_off;
	     private boolean iseof;
	     private int tid;
	     
	     protected MixIteratorImpl(final TPair64[] _off, String _chr) {
	    	 off = _off;
	    	 iseof = false;
	    	 curr_off = 0;
	    	 i = -1;
	    	 tid = chr2tid(_chr);
	     }
	    		 
		@Override
		public String next() throws IOException {
			if(off == null) return null;
			if (iseof) return null;
            for (; ;) {
                if ((curr_off == 0 && i == -1 ) || !less64(curr_off, off[i].v)) { // then jump to the next chunk
                    if (i == off.length - 1) break; // no more chunks
                    if (i >= 0) assert (curr_off == off[i].v); // otherwise bug
                    if (i < 0 || off[i].v != off[i + 1].u) {
                    	mFp.seek(off[i + 1].u);
                    	curr_off = mFp.getFilePointer();
                    }
                    ++i;
                }
                String s = mFp.readLine();
//                String s = readLine(mFp);
                if(s != null) {
                	if (s.length() == 0 || s.charAt(0) == idx.getmMeta()) continue;
                	return s;
                } else break;
            }
            iseof = true;
            return null;
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

	public void doQueryBin(MixQueryBin queryBin) {
		stack.clearST();
		TPair64[] off = getBins(queryBin);
//		for (int i = 0; i < off.length; i++) {
//			System.out.println(off[i].u);
//		}
		if(off == null) {
			stack.setIterator(EOF_ITERATOR);
		} else {
			stack.setIterator(new MixIteratorImpl(off, queryBin.getChr()));
		}
		stack.findOverlaps(queryBin.getNodes());
	}
	
	@Override
	public ProcessResult getProcess() {
		// TODO Auto-generated method stub
		return stack.getResultProcessor();
	}
}
