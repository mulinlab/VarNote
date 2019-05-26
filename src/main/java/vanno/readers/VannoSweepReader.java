package main.java.vanno.readers;

import main.java.vanno.bean.database.Database;
import main.java.vanno.bean.database.DatabaseConfig;
import main.java.vanno.bean.database.DatabaseFactory;
import main.java.vanno.bean.node.Node;
import main.java.vanno.bean.query.Log;
import main.java.vanno.index.MyBlockCompressedInputStream;
import main.java.vanno.process.ProcessResult;

import java.io.IOException;

public final class VannoSweepReader extends VannoReader{
	public VannoSweepReader(final String db) throws IOException {
		this(db, defaultUseJDK, defaultLog);
	}
	
	public VannoSweepReader(final String db, final boolean useJDK, final Log log) throws IOException {
		this(DatabaseFactory.readDatabase(new DatabaseConfig(db)), useJDK, log);
	}
	
	public VannoSweepReader(final Database db) throws IOException {
		this(db, defaultUseJDK, defaultLog);
	}
	
    public VannoSweepReader(final Database db, final boolean useJDK, final Log log) throws IOException {
		super(db, useJDK, log);
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
							new DatabaseConfig("N.meQTL.sort.txt.gz"), false));
			
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
