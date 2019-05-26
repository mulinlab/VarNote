package main.java.vanno.readers;

import main.java.vanno.bean.database.Database;
import main.java.vanno.bean.database.DatabaseConfig;
import main.java.vanno.bean.database.DatabaseFactory;
import main.java.vanno.bean.database.DatabaseConfig.IntersectType;
import main.java.vanno.bean.node.Node;
import main.java.vanno.bean.query.Log;
import main.java.vanno.constants.InvalidArgumentException;
import main.java.vanno.constants.VannoUtils;
import main.java.vanno.index.MyBlockCompressedInputStream;
import main.java.vanno.process.CountResultProcess;
import main.java.vanno.stack.ExactStack;
import main.java.vanno.stack.IntervalStack;
import main.java.vanno.stack.IntervalStackFC;

import java.io.IOException;

public final class VannoMixReader extends VannoReader{
    private MixVannoIteratorImpl it;
//	private BGZReader cReader;
    
	public VannoMixReader(final String db) throws IOException {
		this(db, defaultUseJDK, defaultLog);
	}
	
	public VannoMixReader(final String db, final boolean useJDK, final Log log) throws IOException {
		this(DatabaseFactory.readDatabase(new DatabaseConfig(db)), useJDK, log);
	}

	public VannoMixReader(final Database db) throws IOException {
		this(db, defaultUseJDK, defaultLog);
	}
	
	public VannoMixReader(final Database db, final boolean useJDK, final Log log) throws IOException {	
		super(db, useJDK, log);
		if(db.getConfig().isCount()) {
			if(db.getConfig().getIntersect() == IntersectType.OVERLAP) {
				stack = new IntervalStack(new CountResultProcess());
			} else if(db.getConfig().getIntersect() == IntersectType.EXACT) {
				stack = new ExactStack(new CountResultProcess()); 
			} else if(db.getConfig().getIntersect() == IntersectType.FULLCLOASE) {
				stack = new IntervalStackFC(new CountResultProcess());
			} else {
				throw new InvalidArgumentException(VannoUtils.INTERSECT_ERROR);
			}
		}
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
	public boolean query(final Node query) throws IOException {
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
		public MixVannoIteratorImpl(final MyBlockCompressedInputStream _is) {
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
