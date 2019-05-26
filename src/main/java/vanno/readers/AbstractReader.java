package main.java.vanno.readers;
import main.java.vanno.bean.database.Database;
import main.java.vanno.bean.database.index.Index;
import main.java.vanno.bean.node.Node;
import main.java.vanno.bean.query.Log;
import main.java.vanno.constants.BasicUtils;
import main.java.vanno.index.MyBlockCompressedInputStream;
import main.java.vanno.process.ProcessResult;
import main.java.vanno.stack.AbstractReaderStack;
import java.io.IOException;
import java.util.List;
import htsjdk.samtools.seekablestream.SeekableStreamFactory;


public abstract class AbstractReader {
	protected final static Log defaultLog = BasicUtils.DEFAULT_LOG;
	protected final static boolean defaultUseJDK = BasicUtils.DEFAULT_USE_JDK;
	
	protected final Database db;
	protected MyBlockCompressedInputStream mFp;
	protected AbstractReaderStack stack;
	protected final Index idx;
	protected byte[] buf = new byte[8];
	protected int currentTid;
	protected int preBeg;
	protected Log log;
	
	protected AbstractReader(final Database db, final boolean useJDK) throws IOException {
		this(db, useJDK, defaultLog);
	}
	
	protected AbstractReader(final Database db, final boolean useJDK, final Log log) throws IOException {
    		this.db = db;
    		this.log = log;
    		
    		idx = db.getIndex();

    		this.currentTid = -1;
    		this.preBeg = 0;
    		
	    	if(this.mFp == null) {
	    		mFp = new MyBlockCompressedInputStream(SeekableStreamFactory.getInstance().getBufferedStream(SeekableStreamFactory.getInstance().getStreamFor(db.getConfig().getDbPath())), useJDK);
	    	}
    }

	protected abstract void initForChr(final int tid) throws IOException;
	
	public ProcessResult getProcess() {
		return stack.getResultProcessor();
	}
	
	public void query(final String query) throws IOException {
		query(regionToNode(query));
	}
	
	public boolean query(final Node query) throws IOException {
		getProcess().initResult();
		
		int tid = chr2tid(query.chr);
		if(tid == -1) {
			currentTid = tid;
			return false;
		} else if(tid != currentTid) {
			currentTid = tid;
			log.printKVKCYN(db.getConfig().getOutName() + " reading index for chr", query.chr);
			initForChr(tid);
			this.preBeg = 0;
		} else {
			if(query.orgBeg < this.preBeg) {
				log.printStrNon("Warning: " + query.chr + ":" + query.orgBeg + "-" + query.end + " was added out of order, sort data in order could greatly increased the query speed.");
				initForChr(tid);
				this.preBeg = 0;
			} 
		}

		this.preBeg = query.orgBeg;
		return true;
	}
	
	public static Node regionToNode(final String reg) {
		Node query = new Node();
		int colon, hyphen;

        colon = reg.indexOf(':');
        hyphen = reg.indexOf('-');
        query.chr = colon >= 0 ? reg.substring(0, colon) : reg;
        query.beg = colon >= 0 ? Integer.parseInt(reg.substring(colon + 1, hyphen >= 0 ? hyphen : reg.length())) : 0;
        query.end = hyphen >= 0 ? Integer.parseInt(reg.substring(hyphen + 1)) : 0x7fffffff;
        query.orgBeg = query.beg;
        return query;
	}
	
	protected boolean checkChr() throws IOException{
		return currentTid != -1;
	}
	
    public int chr2tid(final String chr) {
    		return idx.chr2tid(chr);
    }
    
    protected static boolean less64(final long u, final long v) { // unsigned 64-bit comparison
        return (u < v) ^ (u < 0) ^ (v < 0);
    }
    
    protected static boolean lesseq64(final long u, final long v) { // unsigned 64-bit comparison
        return (u <= v) ^ (u < 0) ^ (v < 0);
    }
    
    /** return the source (filename/URL) of that reader */
    public String getSource()
    {
        return this.db.getConfig().getDbPath();
    }

    public void close() {
        if(mFp != null) {
            try {
                mFp.close();
            } catch (IOException e) {
            }
        }
    }
    
    public interface Iterator
    {
        /** return null when there is no more data to read */
        public String next() throws IOException;
        public Node nextNode() throws IOException;
    }

    /** iterator returned instead of null when there is no more data */
    protected static final Iterator EOF_ITERATOR=new Iterator()  {
        @Override
        public String next() throws IOException {
            return null;
        }
        
        @Override
        public Node nextNode() throws IOException {
            return null;
        }
    };
    
    public List<String> getResults() {
    		return stack.getResultProcessor().getResult();
    }
	public Database getDb() {
		return db;
	}
}
