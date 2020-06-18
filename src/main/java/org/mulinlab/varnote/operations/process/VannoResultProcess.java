package org.mulinlab.varnote.operations.process;


import htsjdk.samtools.util.BlockCompressedInputStream;
import org.mulinlab.varnote.utils.gz.BlockCompressedFilePointerUtil;
import org.mulinlab.varnote.utils.node.LocFeature;
import org.mulinlab.varnote.utils.node.NodeWithFilePointer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public final class VannoResultProcess implements ProcessResult{

	private BlockCompressedInputStream mFp;
//	private BGZReader cReader;
	private List<String> result;
	public VannoResultProcess() {
		super();
		result = new ArrayList<String>(3);
	}
	
	public void setMFP(final BlockCompressedInputStream mFp) {
		this.mFp = mFp;
	}
	
//	public void setCReader(final BGZReader cReader) {
//		this.cReader = cReader;
//	}

	@Override
	public void doProcess(LocFeature d) {
//		NodeWithFilePointer dt = (NodeWithFilePointer)d;
//		result.add(dbLabel + "\t" +  cReader.readLine((dt.blockAddress << 16 | dt.blockOffset))); //cReader.getIndex() + "\t" +
		
		try {
			if(d.bgzStr != null) {
				result.add(d.bgzStr); 
			} else {
				NodeWithFilePointer dt = (NodeWithFilePointer)d;
//				System.out.println(d.toString() + " "  + BlockCompressedFilePointerUtil.makeFilePointer(dt.blockAddress, dt.blockOffset));
				mFp.seek(BlockCompressedFilePointerUtil.makeFilePointer(dt.blockAddress, dt.blockOffset));   //25238  54636  //12541 44392
//				System.out.println(dt.blockAddress + "," + dt.blockOffset );
				dt.bgzStr = mFp.readLine();
				result.add(dt.bgzStr);
			}
			
		} catch (IOException e) {
			e.printStackTrace();
		}
//		result.add(dt.blockAddress + "");
	}
	
	@Override
	public List<String> getResult() {
		return result;
	}

	@Override
	public void initResult() {
		result = null;
		result = new ArrayList<String>(3);
	}

	@Override
	public int getResultSize() {
		if(result == null) {
			return 0;
		} else {
			return result.size();
		}
	}
}
