package org.mulinlab.varnote.operations.readers.db;

import htsjdk.samtools.util.BlockCompressedInputStream;
import org.mulinlab.varnote.utils.database.Database;
import org.mulinlab.varnote.utils.node.LocFeature;

import java.io.IOException;

public final class VannoMixReader extends VannoReader{
    private MixVannoIteratorImpl it;
//	private BGZReader cReader;
    
	public VannoMixReader(final String db) throws IOException {
		super(db);
	}
	
	public VannoMixReader(final String db, final boolean isCount) throws IOException {
		super(db, isCount);
	}

	public VannoMixReader(final Database db) throws IOException {
		super(db);
	}
	
	public VannoMixReader(final Database db, final boolean isCount) throws IOException {
		super(db, isCount);
	}
	
	@Override
	protected void initForChr(final int tid) throws IOException {
		super.initForChr(tid);
		isBlockStart = true;
		
		it = new MixVannoIteratorImpl(vannoFile);
		stack.clearST();
		stack.setIterator(it);
	}
	
	
	@Override
	public boolean query(final LocFeature query) throws IOException {
		if(!super.query(query)) return false;
		
		if(isEnd) return false;
		int r = readIndex(query);
		if(r == STOP) {
			 stack.findOverlapInST(query);
			 return false;
		} else if(r == FIND) {
			if(isBlockStart) {
				 isBlockStart = false;
				 it.seek(srob.getVannoFilePointer());
				 it.clear();
			 }
		} else {
			return false;
		}
		stack.findOverlap(query);
		return true;
	}
	
	public final class MixVannoIteratorImpl extends VannoIteratorImpl {
		public MixVannoIteratorImpl(final BlockCompressedInputStream _is) {
		    super(_is);
	    }

		public void clear() {
    			block.setReadBlock(false);;
		}
    	
		@Override
		public void processBlock(final byte flag) throws IOException {
			if(block.isReadBlock()) {
				 nextBlock();
				 isBlockStart = false;
			 }
			 
			 block.readBlock(is, srob.getMin(), flag, buf);
			 vannoFeature = block.makeFirstVannoFeature();
		}
	}
	
    @Override
    public String toString() {
        return "MixVannoReader: filename:"+getSource();
    }
    
    public static void main(String[] args) {
    		try {
    			
			VannoMixReader reader = new VannoMixReader("HT.meQTL.sort.txt.gz");
			reader.query("2:31235323-31235324");
			for (String r : reader.getProcess().getResult()) {
				System.out.println(r);
			}
			
			
			reader.close();
    		} catch (IOException e) {
			e.printStackTrace();
		}
    }
}
