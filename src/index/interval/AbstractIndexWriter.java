package index.interval;

import htsjdk.samtools.util.BlockCompressedInputStream;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import readers.AbstractReader;

public abstract class AbstractIndexWriter {
	protected final List<String> sequenceNames = new ArrayList<String>();
	protected final Set<String> sequenceNamesSeen = new HashSet<String>();
	
	
	public AbstractIndexWriter() {
	}

	
	public void addSeqsName(String sequenceName) {
		if(sequenceNamesSeen.contains(sequenceName)) {
			throw new IllegalArgumentException("chr must in order!");
		}
		sequenceNames.add(sequenceName);
		sequenceNamesSeen.add(sequenceName);
	}
	
	public short readShort(BlockCompressedInputStream _is) throws IOException {
		return AbstractReader.readShort(_is);
	}
	
	public int readInt(BlockCompressedInputStream _is) throws IOException {
		return AbstractReader.readInt(_is);
	}
	
	public long readLong(BlockCompressedInputStream _is) throws IOException {
		return AbstractReader.readLong(_is);
	}
}
