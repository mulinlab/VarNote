package readers;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import stack.AbstractReaderStack;
import bean.index.Index;
import bean.node.Node;
import htsjdk.samtools.seekablestream.SeekableStreamFactory;
import htsjdk.samtools.util.BlockCompressedInputStream;

public abstract class AbstractReader {
	protected String mFn;
	protected BlockCompressedInputStream mFp;
	protected AbstractReaderStack stack;
	protected Index idx;
	
    public void setStack(AbstractReaderStack stack) {
		this.stack = stack;
	}

	public AbstractReader(final String fn, final Index idx) throws IOException {
    	this.mFn = fn;
    	this.idx = idx;
    	
    	if(this.mFp == null) {
    		mFp = new BlockCompressedInputStream(SeekableStreamFactory.getInstance().getBufferedStream(SeekableStreamFactory.getInstance().getStreamFor(this.mFn)));
    	}
    }
    
    public int chr2tid(final String chr) {
    	return idx.chr2tid(chr);
    }
    
    protected static class TIntv {
        int tid, beg, end;
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
        return this.mFn;
    }
    
    public static short readShort(final InputStream is) throws IOException {
		byte[] buf = new byte[2];
		is.read(buf);
		return ByteBuffer.wrap(buf).order(ByteOrder.LITTLE_ENDIAN).getShort();
	}
    
    public static int readInt(final InputStream is) throws IOException {
		byte[] buf = new byte[4];
		is.read(buf);
		return ByteBuffer.wrap(buf).order(ByteOrder.LITTLE_ENDIAN).getInt();
	}

	public static long readLong(final InputStream is) throws IOException {
		byte[] buf = new byte[8];
		is.read(buf);
		return ByteBuffer.wrap(buf).order(ByteOrder.LITTLE_ENDIAN).getLong();
	}

    public static String readLine(final InputStream is) throws IOException {
        StringBuffer buf = new StringBuffer();
        int c;
        while ((c = is.read()) >= 0 && c != '\n')
            buf.append((char) c);
        if (c < 0) return null;
        return buf.toString();
    }

    /**
     * Read one line from the data file.
     */
    public String readLine() throws IOException {
        return readLine(mFp);
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
}
