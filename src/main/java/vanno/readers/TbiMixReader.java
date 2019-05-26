package main.java.vanno.readers;

import htsjdk.tribble.util.TabixUtils.TIndex;
import htsjdk.tribble.util.TabixUtils.TPair64;
import main.java.vanno.bean.database.Database;
import main.java.vanno.bean.database.DatabaseConfig;
import main.java.vanno.bean.database.DatabaseFactory;
import main.java.vanno.bean.node.Node;
import main.java.vanno.bean.query.Log;
import main.java.vanno.constants.BasicUtils;

import java.io.IOException;
import java.util.Arrays;

public final class TbiMixReader extends TbiReader{
	private final static int MAX_BIN = 37450;
	private final static int TAD_LIDX_SHIFT = 14;
	private int pb_num;
	private TIndex tindex;

	public TbiMixReader(final String db) throws IOException {
		this(db, defaultUseJDK, defaultLog);
	}
	
	public TbiMixReader(final String db, final boolean useJDK, final Log log) throws IOException {
		this(DatabaseFactory.readDatabase(new DatabaseConfig(db)), useJDK, log);
	}
	
	public TbiMixReader(final Database db) throws IOException {
		this(db, defaultUseJDK, defaultLog);
	}
	
	public TbiMixReader(final Database db, final boolean useJDK, final Log log) throws IOException {
		super(db, useJDK, log);
	}
	
	@Override
	public void initForChr(final int tid) {
		if(tid < 0 || tid >= idxArr.length) {
			tindex = null;
		} else {
			tindex = idxArr[tid];
		}
		
		pb_num = -1;
	}
	
	public void initStack(final String chr, final int min, final int max, final int b_num) {
		pb_num = b_num;
		stack.clearST();
		TPair64[] off = getBins(min, max);

		if(off == null) {
			stack.setIterator(EOF_ITERATOR);
		} else {
		 	stack.setIterator(new MixIteratorImpl(off, chr));
		}
	}

	public boolean query(Node query) throws IOException {
		if(!super.query(query)) return false;
		if(tindex == null) return false;
		
		 final int b_num = BasicUtils.regionToBin4(query.beg, query.end);
		 if(b_num < 585) {
			 initStack(query.chr, query.beg, query.end, b_num);
		 } else {
			 if(b_num != pb_num) {
				 initStack(query.chr, query.beg, (b_num - 585)*131072 + 131071, b_num);
			 }
		 }
		 stack.findOverlap(query);
		 return true;
	}

	public TPair64[] getBins(final int min, final int max) {
	 	TPair64[] off, chunks;
        long min_off;
    
        int[] bins = new int[MAX_BIN];
        int i, l, n_off, n_bins = BasicUtils.reg2bins(min, max, bins);
        if (tindex.l.length > 0)
            min_off = (min >> TAD_LIDX_SHIFT >= tindex.l.length) ? tindex.l[tindex.l.length - 1] : tindex.l[min >> TAD_LIDX_SHIFT];
        else min_off = 0;
        for (i = n_off = 0; i < n_bins; ++i) {
            if ((chunks = tindex.b.get(bins[i])) != null) {
            	n_off += chunks.length;
            }
        }
        
        if (n_off == 0) return null;
        off = new TPair64[n_off];
        for (i = n_off = 0; i < n_bins; ++i)
            if ((chunks = tindex.b.get(bins[i])) != null)
                for (int j = 0; j < chunks.length; ++j)
                    if (less64(min_off, chunks[j].v)) {
                    	 off[n_off++] = new TPair64(chunks[j]);
                    }
                       
        Arrays.sort(off, 0, n_off);

        for (i = 1, l = 0; i < n_off; ++i) {
            if (less64(off[l].v, off[i].v)) {
                ++l;
                off[l].u = off[i].u;
                off[l].v = off[i].v;
            }
        }
        n_off = l + 1;

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

        TPair64[] ret = new TPair64[n_off];
        for (i = 0; i < n_off; ++i) {
            if (off[i] != null) ret[i] = new TPair64(off[i].u, off[i].v); // in C, this is inefficient
        }
        if (ret.length == 0 || (ret.length == 1 && ret[0] == null))
            return null;
        return ret;
	}
	
	public class MixIteratorImpl extends TbiIteratorImpl {
		 private int i;
		 private TPair64[] off;
	     private long curr_off;
	     
	     public MixIteratorImpl(final TPair64[] _off, String _chr) {
		    	 super(chr2tid(_chr));
		    	 off = _off;
		    	 curr_off = 0;
		    	 i = -1;
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
                if(s != null) {
                	if (s.length() == 0 || s.charAt(0) == idx.getmMeta()) continue;
                	return s;
                } else break;
            }
            iseof = true;
            return null;
		}
	}
}
