package bean.index;

import htsjdk.samtools.util.BlockCompressedInputStream;
import htsjdk.tribble.util.TabixUtils.TIndex;
import htsjdk.tribble.util.TabixUtils.TPair64;
import java.io.IOException;
import java.util.HashMap;
import postanno.FormatWithRef;

public class TbiIndex extends Index{
	
	private TIndex[] mIndex;
	 
	protected void readMagic(BlockCompressedInputStream is) throws IOException {
		byte[] buf = new byte[4];
        is.read(buf, 0, 4); // read "TBI\1"
    }
	
	protected void readIndex(BlockCompressedInputStream is) throws IOException {
		mIndex = new TIndex[mSeq.length];
		int i, j, k;
		for (i = 0; i < mSeq.length; ++i) {

			int n_bin = readInt(is);
			mIndex[i] = new TIndex();
			mIndex[i].b = new HashMap<Integer, TPair64[]>(n_bin);
			for (j = 0; j < n_bin; ++j) {
				int bin = readInt(is);
				TPair64[] chunks = new TPair64[readInt(is)];
				for (k = 0; k < chunks.length; ++k) {
					long u = readLong(is);
					long v = readLong(is);
					chunks[k] = new TPair64(u, v); // in C, this is inefficient
				}
				mIndex[i].b.put(bin, chunks);
			}
			
			// the linear index
			mIndex[i].l = new long[readInt(is)];

			for (k = 0; k < mIndex[i].l.length; ++k) {
				mIndex[i].l[k] = readLong(is);
				if (i == 0) {
					minOffForChr.put(i, 0l);
				} else {
					if (mIndex[i].l[k] > 0) {
						if (minOffForChr.get(i) == null) {
							minOffForChr.put(i, mIndex[i].l[k]);
						} else {
							if (mIndex[i].l[k] < minOffForChr.get(i)) {
								minOffForChr.put(i, mIndex[i].l[k]);
								throw new IllegalArgumentException(
										"That is  impossible! in SweepReader read index.");
							}
						}
					}
				}
			}
		}
	}
	
	public TbiIndex(String indexPath) {
		super(indexPath);
		readFile();
		
		if((mPreset & 0xffff) == 2) {
			this.formatWithRef = FormatWithRef.VCF;
    	} else {
    		if(mBc == mEc) {
    			formatWithRef = new FormatWithRef(this.format, -1, -1, -1, 3);
    		} else {
    			formatWithRef = new FormatWithRef(this.format, -1, -1, -1, 2);
    		}
    	}
	}

	public TIndex[] getmIndex() {
		return mIndex;
	}
	
	public FormatWithRef getFormatWithRef() {
		return formatWithRef;
	}
	
	public static void main(String[] args) {
		TbiIndex index  = new TbiIndex("/Users/mulin/Desktop/gwas.sort.txt.gz.tbi");
		System.out.println(index.mSc);
		System.out.println(index.mBc);
		System.out.println(index.mEc);
		System.out.println(index.mPreset);
	}
}
