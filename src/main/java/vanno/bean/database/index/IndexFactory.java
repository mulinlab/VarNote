package main.java.vanno.bean.database.index;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import htsjdk.samtools.seekablestream.ISeekableStreamFactory;
import htsjdk.samtools.seekablestream.SeekableStreamFactory;
import main.java.vanno.bean.database.Database.IndexType;
import main.java.vanno.bean.database.index.vannoIndex.VannoIndex;
import main.java.vanno.bean.database.index.vannoIndex.VannoIndexV1;
import main.java.vanno.constants.BasicUtils;
import main.java.vanno.constants.InvalidArgumentException;
import main.java.vanno.index.MyBlockCompressedInputStream;

public final class IndexFactory {
	private static final byte[] MAGIC = {'I', 'N', 'T', 'E', 'R', 1};
    public static final int MAGIC_NUMBER;
	
    static {
        final ByteBuffer bb = ByteBuffer.allocate(MAGIC.length);
        bb.put(MAGIC);
        bb.flip(); 
        MAGIC_NUMBER = bb.order(ByteOrder.LITTLE_ENDIAN).getInt();
    }
	   
	public static Index readIndex(final String path, final boolean useJDK) {
		if(path.endsWith(IndexType.TBI.getExtIndex())) {
			return new TbiIndex(path);
		} else if(path.endsWith(IndexType.VANNO.getExtIndex())) {
			ISeekableStreamFactory ssf = SeekableStreamFactory.getInstance();
		    VannoIndex index = null;
			try {	
				MyBlockCompressedInputStream is = new MyBlockCompressedInputStream(ssf.getBufferedStream(ssf.getStreamFor(path), 128000), useJDK);
				long addr = is.getAddress();
				is.seek(addr);
				int version = BasicUtils.readInt(is);
		
				if(version == 1) {
					index = new VannoIndexV1(is, version);
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
			return index;
			
		} else {
			throw new InvalidArgumentException("Index file should be end with " + IndexType.TBI.getExtIndex() + " or " + IndexType.VANNO.getExtIndex());
		}
	}
}
