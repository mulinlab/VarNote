package org.mulinlab.varnote.utils.database.index.vannoIndex;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import htsjdk.samtools.util.BlockCompressedInputStream;
import org.mulinlab.varnote.constants.GlobalParameter;
import org.mulinlab.varnote.utils.database.index.IndexFactory;
import org.mulinlab.varnote.utils.format.Format;

public final class VannoIndexV1 extends VannoIndex{
	
	public VannoIndexV1(final BlockCompressedInputStream is, final int version) {
		super(is, version);
	}
	
	@Override
	public void readFormat() throws IOException { 
        mChr2tid = new HashMap<String, Integer>();
        mTid2chr = new HashMap<Integer, String>();
        mPreset = GlobalParameter.readInt(is);
        mSc = GlobalParameter.readInt(is);
        mBc = GlobalParameter.readInt(is);
        mEc = GlobalParameter.readInt(is);
        
        int l = GlobalParameter.readInt(is);
        byte[] buf = new byte[l];
		is.read(buf);
		commentIndicator = new String(buf);
		
//		l = GlobalParameter.readInt(is);
//        buf = new byte[l];
//		is.read(buf);
//		headerIndicator = new String(buf);
		
		l = GlobalParameter.readInt(is);
		if(l > 0 ) {
			headerParts = new String[l];
			readHeader();
		}
		
        mSkip = GlobalParameter.readInt(is);  //read skip

		int ref = GlobalParameter.readInt(is);
		int alt = GlobalParameter.readInt(is);
		int info = GlobalParameter.readInt(is);
        this.format = new Format(mPreset, mSc, mBc, mEc, mSkip, commentIndicator, ref, alt, GlobalParameter.readBoolean(is));
		this.format.setHeaderPart(headerParts, false);
//        GlobalParameter.readInt(is); //type
//        GlobalParameter.readInt(is);
        mSeq = new String[GlobalParameter.readInt(is)]; // # sequences
	}
	
	protected void readHeader() throws IOException {
		byte[] buf = new byte[4];
		int i, j, l = GlobalParameter.readInt(is), k=0;
		buf = new byte[l];
		is.read(buf);
		for (i = j = 0; i < buf.length; ++i) {
			if (buf[i] == 0) {
				byte[] b = new byte[i - j];
				System.arraycopy(buf, j, b, 0, b.length);

				headerParts[k++] = new String(b);
				j = i + 1;
			}
		}
	}

	 
	@Override
	public void readIndex() throws IOException {
		int chrSize = GlobalParameter.readInt(is);
		
		for (int j = 0; j < chrSize; j++) {
			minOffForChr.put(GlobalParameter.readInt(is), GlobalParameter.readLong(is, buf));
		}
	}

}
