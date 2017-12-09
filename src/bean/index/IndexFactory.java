package bean.index;

import index.interval.BinIndexWriter;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import bean.index.bin.BinIndex;
import bean.index.bin.BinIndexV1;
import constants.BasicUtils;
import htsjdk.samtools.seekablestream.ISeekableStreamFactory;
import htsjdk.samtools.seekablestream.SeekableStream;
import htsjdk.samtools.seekablestream.SeekableStreamFactory;
import htsjdk.samtools.util.BlockCompressedInputStream;
import htsjdk.tribble.util.TabixUtils;

public class IndexFactory {
	private static final byte[] MAGIC = {'I', 'N', 'T', 'E', 'R', 1};
    public static final int MAGIC_NUMBER;
	
    static {
        final ByteBuffer bb = ByteBuffer.allocate(MAGIC.length);
        bb.put(MAGIC);
        bb.flip();
        MAGIC_NUMBER = bb.order(ByteOrder.LITTLE_ENDIAN).getInt();
    }
	   
	public static Index readIndex(final String path) {
		if(path.endsWith(TabixUtils.STANDARD_INDEX_EXTENSION)) {
			TbiIndex index = new TbiIndex(path);
			index.readFile();
			return index;
			
		} else if(path.endsWith(BinIndexWriter.PLUS_INDEX_EXTENSION)) {
			ISeekableStreamFactory ssf = SeekableStreamFactory.getInstance();
		    SeekableStream fp;
		    BinIndex index = null;
			try {
				fp = ssf.getBufferedStream(ssf.getStreamFor(path), 128000);
				if (fp == null)  return null;
				
				BlockCompressedInputStream is = new BlockCompressedInputStream(fp);
				int version = BasicUtils.readInt(is);
		
				if(version == 1) {
					index = new BinIndexV1(path);
				}
				index.readFile(is);
		        is.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
			return index;
			
		} else {
			throw new IllegalArgumentException("Index file should be end with .tbi or .plus.index");
		}
		
	}
}
