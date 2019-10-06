package org.mulinlab.varnote.operations.readers;

import org.mulinlab.varnote.utils.database.Database;
import org.mulinlab.varnote.config.param.DBParam;
import org.mulinlab.varnote.utils.database.DatabaseFactory;
import org.mulinlab.varnote.utils.enumset.Mode;

import org.mulinlab.varnote.utils.gz.MyBlockCompressedInputStream;
import org.mulinlab.varnote.operations.process.ProcessResult;
import org.mulinlab.varnote.utils.node.Node;

import java.io.IOException;

public final class VannoSweepReader extends VannoReader{
	public VannoSweepReader(final String db) throws IOException {
		super(db);
	}

	public VannoSweepReader(final String db, final boolean isCount) throws IOException {
		super(db, isCount);
	}

	public VannoSweepReader(final Database db) throws IOException {
		super(db);
	}

	public VannoSweepReader(final Database db, final boolean isCount) throws IOException {
		super(db, isCount);
	}

    public final class SweepIteratorImpl extends VannoIteratorImpl {
    		boolean isStart;
		 public SweepIteratorImpl(final MyBlockCompressedInputStream _is) {
		    	 super(_is);
	    	 	 try {
		    		 _is.seek(srob.getVannoFilePointer());
	 		 } catch (IOException e) {
	 			e.printStackTrace(); 
	 		 }	
	    	 	 isStart = false;
	     }
		
		 @Override
		 public void processBlock(final byte flag) throws IOException {
			 if(isStart) {
				 nextBlock();
			 } else {
				 isStart = true;
			 }
			 block.readBlock(is, srob.getMin(), flag, buf);
			 vannoFeature = block.makeFirstVannoFeature();
		 }
	}
	

	@Override
	public void initForChr(final int tid) throws IOException{
		super.initForChr(tid);
		nextBlock();
		stack.clearST();
		stack.setIterator(new SweepIteratorImpl(vannoFile));
	}
	

	@Override
	public boolean query(final Node query) throws IOException{
		if(!super.query(query)) return false;
		if(isEnd) return false;
		stack.findOverlap(query);
		
		return true;
	}

    @Override
    public String toString() {
        return "SweepVannoReader: filename:"+getSource();
    }
    
    public static void main(String[] args) {
		try {
			VannoSweepReader reader = new VannoSweepReader(
					DatabaseFactory.readDatabase(
							new DBParam("N.meQTL.sort.txt.gz")));
			
			reader.query("1:100000-100500");
			
			ProcessResult result = reader.getProcess();

			for (String r : result.getResult()) {
				System.out.println(r);
			}
			
			System.out.println("\n\n\n");
			reader.query("2:100000-100500");
			result = reader.getProcess();
			System.out.println(result.getResultSize());
			for (String r : result.getResult()) {
				System.out.println(r);
			}
			
			reader.close();
			
			
		} catch (IOException e) {
			e.printStackTrace();
		}
    }
}
