package bean.index.bin;

import htsjdk.samtools.util.BlockCompressedInputStream;
import htsjdk.tribble.TribbleException;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import bean.index.Index;
import bean.index.IndexFactory;
import bean.node.Bin;

public abstract class BinIndex extends Index{
	
	protected List<Bin> bins;
	protected Map<Integer, List<Bin>> binsMap;
	
	protected void readMagic(BlockCompressedInputStream is) throws IOException {
		int magicNUmber = readInt(is);
		if (magicNUmber != IndexFactory.MAGIC_NUMBER) {
            throw new TribbleException(String.format("Unexpected magic number 0x%x", magicNUmber));
        }
    }
	
	protected abstract void readFormat(BlockCompressedInputStream is) throws IOException;	 
	protected abstract void readIndex(BlockCompressedInputStream is) throws IOException;

	
	public BinIndex(String indexPath) {
		super(indexPath);
	}

	public Map<Integer, List<Bin>> getBinsMap() {
		return binsMap;
	}

	public void setBinsMap(Map<Integer, List<Bin>> binsMap) {
		this.binsMap = binsMap;
	}

	public List<Bin> getBins() {
		return bins;
	}

	public List<Bin> loadBin(final int tid) {
		bins = binsMap.get(tid);
		return bins;
	}
}
