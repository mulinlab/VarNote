package org.mulinlab.varnote.operations.readers.db;
import htsjdk.samtools.util.BlockCompressedInputStream;
import org.apache.logging.log4j.Logger;
import org.mulinlab.varnote.constants.GlobalParameter;
import org.mulinlab.varnote.utils.LoggingUtils;
import org.mulinlab.varnote.utils.database.Database;
import org.mulinlab.varnote.utils.database.index.Index;
import org.mulinlab.varnote.operations.process.ProcessResult;
import org.mulinlab.varnote.operations.stack.AbstractReaderStack;
import java.io.IOException;
import java.util.List;
import htsjdk.samtools.seekablestream.SeekableStreamFactory;
import org.mulinlab.varnote.utils.node.LocFeature;


public abstract class AbstractDBReader {
	final Logger logger = LoggingUtils.logger;

	protected final Database db;
	protected BlockCompressedInputStream mFp;
	protected AbstractReaderStack stack;
	protected final Index idx;
	protected byte[] buf = new byte[8];
	protected int currentTid;
	protected int preBeg;
	protected boolean isCount = GlobalParameter.DEFAULT_IS_COUNT;

	protected AbstractDBReader(final Database db) throws IOException {
		this.db = db;
		this.isCount = isCount;

		idx = db.getIndex();

		this.currentTid = -1;
		this.preBeg = 0;

		if(this.mFp == null) {
			mFp = new BlockCompressedInputStream(SeekableStreamFactory.getInstance().getBufferedStream(SeekableStreamFactory.getInstance().getStreamFor(db.getConfig().getDbPath())));
		}
	}

	protected AbstractDBReader(final Database db, boolean isCount) throws IOException {
		this(db);
		this.isCount = isCount;
    }

	protected abstract void initForChr(final int tid) throws IOException;
	
	public ProcessResult getProcess() {
		return stack.getResultProcessor();
	}
	
	public void query(final String query) throws IOException {
		query(regionToNode(query));
	}
	
	public boolean query(final LocFeature query) throws IOException {
		getProcess().initResult();
		if(query.beg < 1) query.beg = 1;
//		System.out.println(query.toString());
		int tid = chr2tid(query.chr);
		if(tid == -1) {
			currentTid = tid;
			return false;
		} else if(tid != currentTid) {
			currentTid = tid;
			logger.info(String.format("%s reading index for chr %s.", db.getConfig().getOutName(), query.chr));
			initForChr(tid);
			this.preBeg = 0;
		} else {
			if(query.beg < this.preBeg) {
				logger.info(String.format("Warning: %s:%d-%d was added out of order, sort data in order could greatly increased the query speed.", query.chr, query.beg, query.end));
				initForChr(tid);
				this.preBeg = 0;
			} 
		}

		this.preBeg = query.beg;
		return true;
	}
	
	public static LocFeature regionToNode(final String reg) {
		LocFeature query = new LocFeature();
		int colon, hyphen;

        colon = reg.indexOf(':');

        if(colon == -1) {
			colon = reg.indexOf('-');
		}
		hyphen = reg.indexOf('-', colon + 1);

        query.chr = colon >= 0 ? reg.substring(0, colon) : reg;
        query.beg = colon >= 0 ? Integer.parseInt(reg.substring(colon + 1, hyphen >= 0 ? hyphen : reg.length())) : 0;
        query.end = hyphen >= 0 ? Integer.parseInt(reg.substring(hyphen + 1)) : 0x7fffffff;
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
        public LocFeature nextNode() throws IOException;
    }

    /** iterator returned instead of null when there is no more data */
    protected static final Iterator EOF_ITERATOR=new Iterator()  {
        @Override
        public String next() throws IOException {
            return null;
        }
        
        @Override
        public LocFeature nextNode() throws IOException {
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
