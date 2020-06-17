package org.mulinlab.varnote.utils.database.index;


import htsjdk.tribble.util.TabixUtils.TIndex;
import htsjdk.tribble.util.TabixUtils.TPair64;
import org.mulinlab.varnote.constants.GlobalParameter;
import org.mulinlab.varnote.utils.format.Format;
import org.mulinlab.varnote.exceptions.InvalidArgumentException;
import java.io.IOException;
import java.util.HashMap;

public final class TbiIndex extends Index{
	
	private TIndex[] mIndex;
	
	public void readMagic() throws IOException {
		byte[] buf = new byte[4];
        is.read(buf, 0, 4); // read "TBI\1"
    }
	
	public void readFormat() throws IOException {
		mSeq = new String[GlobalParameter.readInt(is)]; // # sequences
		mChr2tid = new HashMap<String, Integer>();
		mTid2chr = new HashMap<Integer, String>();
		mPreset = GlobalParameter.readInt(is);
		mSc = GlobalParameter.readInt(is);
		mBc = GlobalParameter.readInt(is);
		mEc = GlobalParameter.readInt(is);

		mMeta = GlobalParameter.readInt(is);
		mSkip = GlobalParameter.readInt(is); // read skip
	}
	
	public TbiIndex(String indexPath) {
		super(indexPath, true);
		try {
			read();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void read() throws IOException {
		super.read();

		if((mPreset & 0xffff) == 2) {
			this.format = Format.newVCF();
			this.format.numHeaderLinesToSkip = mSkip;
		} else {
			this.format = new Format(mPreset, mSc, mBc, mEc, mSkip, Character.toString((char) mMeta), -1, -1, false);
		}
	}
	
	public void readIndex() throws IOException {
		mIndex = new TIndex[mSeq.length];
		int i, j, k;
		for (i = 0; i < mSeq.length; ++i) {

			int n_bin = GlobalParameter.readInt(is);
			mIndex[i] = new TIndex();
			mIndex[i].b = new HashMap<Integer, TPair64[]>(n_bin);
			for (j = 0; j < n_bin; ++j) {
				int bin = GlobalParameter.readInt(is);
				TPair64[] chunks = new TPair64[GlobalParameter.readInt(is)];
				for (k = 0; k < chunks.length; ++k) {
					long u = GlobalParameter.readLong(is, buf);
					long v = GlobalParameter.readLong(is, buf);
					chunks[k] = new TPair64(u, v); // in C, this is inefficient
				}
				mIndex[i].b.put(bin, chunks);
			}
			
			// the linear index
			mIndex[i].l = new long[GlobalParameter.readInt(is)];

			for (k = 0; k < mIndex[i].l.length; ++k) {
				mIndex[i].l[k] = GlobalParameter.readLong(is, buf);
				if (i == 0) {
					minOffForChr.put(i, 0l);
				} else {
					if (mIndex[i].l[k] > 0) {
						if (minOffForChr.get(i) == null) {
							minOffForChr.put(i, mIndex[i].l[k]);
						} else {
							if (mIndex[i].l[k] < minOffForChr.get(i)) {
								minOffForChr.put(i, mIndex[i].l[k]);
								throw new InvalidArgumentException("That is  impossible! in SweepReader read index.");
							}
						}
					}
				}
			}
		}
	}

	public TIndex[] getmIndex() {
		return mIndex;
	}

	public static void main(String[] args) {
		TbiIndex index  = new TbiIndex("/Users/hdd/Desktop/data/ALL.chr1.phase3_shapeit2_mvncall_integrated_v3plus_nounphased.rsID.genotypes.GRCh38_dbSNP.vcf.gz.tbi");
		
		System.out.println(index.mSc);
		System.out.println(index.mBc);
		System.out.println(index.mEc);
		System.out.println(index.mPreset);
	}

	@Override
	public String[] getColumnNames() {
		return null;
	}
}
