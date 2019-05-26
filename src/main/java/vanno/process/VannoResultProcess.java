package main.java.vanno.process;


import main.java.vanno.bean.node.Node;
import main.java.vanno.bean.node.NodeWithFilePointer;
import main.java.vanno.index.MyBlockCompressedInputStream;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public final class VannoResultProcess implements ProcessResult{

	private MyBlockCompressedInputStream mFp;
//	private BGZReader cReader;
	private List<String> result;
	public VannoResultProcess() {
		super();
		result = new ArrayList<String>(3);
	}
	
	public void setMFP(final MyBlockCompressedInputStream mFp) {
		this.mFp = mFp;
	}
	
//	public void setCReader(final BGZReader cReader) {
//		this.cReader = cReader;
//	}

	@Override
	public void doProcess(Node d) {
//		NodeWithFilePointer dt = (NodeWithFilePointer)d;
//		result.add(dbLabel + "\t" +  cReader.readLine((dt.blockAddress << 16 | dt.blockOffset))); //cReader.getIndex() + "\t" +
		
		try {
			if(d.bgzStr != null) {
				result.add(d.bgzStr); 
			} else {
				NodeWithFilePointer dt = (NodeWithFilePointer)d;
				mFp.seek(dt.blockAddress, dt.blockOffset);   //25238  54636  //12541 44392
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
		return result.size();
	}
}
