package bean.index.bin;

import htsjdk.samtools.util.BlockCompressedInputStream;
import htsjdk.tribble.index.tabix.TabixFormat;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import postanno.FormatWithRef;
import bean.node.Bin;

public class BinIndexV1 extends BinIndex{
	
	public BinIndexV1(String indexPath) {
		super(indexPath);
	}

	public void readFormat(BlockCompressedInputStream is) throws IOException {
        mChr2tid = new HashMap<String, Integer>();
        mTid2chr = new HashMap<Integer, String>();
        mPreset = readInt(is);
        mSc = readInt(is);
        mBc = readInt(is);
        mEc = readInt(is);
        mMeta = readInt(is);
        mSkip = readInt(is);  //read skip
        
        format = new TabixFormat(this.mPreset, this.mSc, this.mBc, this.mEc, (char)this.mMeta, this.mSkip);
//        if((mPreset & 0xffff) == 2) {
//			this.formatWithRef = FormatWithRef.VCF;
//    	} else {
//    		if(mBc == mEc) {
//    			formatWithRef = new FormatWithRef(this.format, readInt(is), readInt(is), readInt(is), readInt(is));
//    		} else {
//    			
//    		}
//    	}
        formatWithRef = new FormatWithRef(this.format, readInt(is), readInt(is), readInt(is), readInt(is));
        
        mSeq = new String[readInt(is)]; // # sequences    
	}
	 
	public void readIndex(BlockCompressedInputStream is) throws IOException {
		int chrSize = readInt(is);
		
		for (int j = 0; j < chrSize; j++) {
			minOffForChr.put(readInt(is), readLong(is));
		}
		int tid = 0, size;
		List<Bin> binList;
		binsMap = new HashMap<Integer, List<Bin>>();

		long address;
		int max, capacity;
		for (int j = 0; j < mSeq.length; j++) {
			tid = readShort(is);
			size = readInt(is);

			binList = new ArrayList<Bin>(size);

	        for (int i = 0; i < size; i++) {
	        	address = readLong(is);
	        	max = readInt(is);
	        	capacity = readInt(is);
	        	binList.add(new Bin(address, max,  capacity));
			}
	        binsMap.put(tid, binList);
		}
	}
}
