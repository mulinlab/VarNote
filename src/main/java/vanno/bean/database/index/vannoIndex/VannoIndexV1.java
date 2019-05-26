package main.java.vanno.bean.database.index.vannoIndex;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import main.java.vanno.bean.database.index.IndexFactory;
import main.java.vanno.bean.format.Format;
import main.java.vanno.constants.BasicUtils;
import main.java.vanno.index.MyBlockCompressedInputStream;

public class VannoIndexV1 extends VannoIndex{
	
	public VannoIndexV1(final MyBlockCompressedInputStream is, final int version) {
		super(is, version);
	}
	
	@Override
	public void readFormat() throws IOException { 
        mChr2tid = new HashMap<String, Integer>();
        mTid2chr = new HashMap<Integer, String>();
        mPreset = BasicUtils.readInt(is);
        mSc = BasicUtils.readInt(is);
        mBc = BasicUtils.readInt(is);
        mEc = BasicUtils.readInt(is);
        
        int l = BasicUtils.readInt(is);
        byte[] buf = new byte[l];
		is.read(buf);
		commentIndicator = new String(buf);
		
//		l = BasicUtils.readInt(is);
//        buf = new byte[l];
//		is.read(buf);
//		headerIndicator = new String(buf);
		
		l = BasicUtils.readInt(is);
		if(l > 0 ) {
			headerColList = new ArrayList<String>(l);
			readHeader();
		}
		
        mSkip = BasicUtils.readInt(is);  //read skip
        this.format = new Format(mPreset, mSc, mBc, mEc, mSkip, commentIndicator, BasicUtils.readInt(is), BasicUtils.readInt(is), BasicUtils.readBoolean(is));
//        BasicUtils.readInt(is); //type
        BasicUtils.readInt(is);
        mSeq = new String[BasicUtils.readInt(is)]; // # sequences    
	}
	
	protected void readHeader() throws IOException {
		byte[] buf = new byte[4];
		int i, j, l = BasicUtils.readInt(is);
		buf = new byte[l];
		is.read(buf);
		for (i = j = 0; i < buf.length; ++i) {
			if (buf[i] == 0) {
				byte[] b = new byte[i - j];
				System.arraycopy(buf, j, b, 0, b.length);
				String col = new String(b);
				headerColList.add(col);
				j = i + 1;
			}
		}
	}

	 
	@Override
	public void readIndex() throws IOException {
		int chrSize = BasicUtils.readInt(is);
		
		for (int j = 0; j < chrSize; j++) {
			minOffForChr.put(BasicUtils.readInt(is), BasicUtils.readLong(is, buf));
		}
	}
	
	public static void main(String[] args) {
		VannoIndexV1 index = (VannoIndexV1)IndexFactory.readIndex("/Users/hdd/Desktop/vanno/vanno_help/AF.ANN.bgz.vanno.vi", false) ;
		System.out.println(index.mSc);
		System.out.println(index.mBc);
		System.out.println(index.mEc);
		System.out.println(index.commentIndicator);
		Map<Integer, Long> map = index.getMinOffForChr();
		for (Integer key : map.keySet()) {
			System.out.println(key + ", " +  map.get(key));
		}
	}
}
