package process;

import index.MyBlockCompressedInputStream;
import index.MyEndianOutputStream;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import bean.node.Node;
import bean.node.NodeWithFilePointer;
import constants.BasicUtils;
import htsjdk.samtools.util.BlockCompressedOutputStream;

public final class VannoResultProcess implements ProcessResult{

	private MyBlockCompressedInputStream mFp;
	private final String dbLabel;
	private List<String> result;
	private final MyEndianOutputStream indexLos;
	
	public VannoResultProcess(final String dbLabel) {
		super();
		this.dbLabel = dbLabel;
		result = new ArrayList<String>(3);
		indexLos = new MyEndianOutputStream(new BlockCompressedOutputStream("/Users/hdd/Downloads/dandan/data/linlin2.vcf_MIX_out/" + dbLabel + ".bgz"));
	}
	
	public void setMFP(MyBlockCompressedInputStream mFp) {
		this.mFp = mFp;
	}

	public void close() {
		try {
			indexLos.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public String doProcess(Node d) throws IOException {
		NodeWithFilePointer dt = (NodeWithFilePointer)d;
		indexLos.writeLong((dt.blockAddress << 16 | dt.blockOffset));
//		result.add(String.valueOf(f));
		try {
			if(d.bgzStr != null) {
				result.add(dbLabel + "\t" + d.bgzStr); 
				return null;
			} else {
//				NodeWithFilePointer dt = (NodeWithFilePointer)d;
				mFp.seek(dt.blockAddress, dt.blockOffset);   //25238  54636  //12541 44392
				
				final String s = mFp.readLine();
//				System.out.println(s);
				result.add(dbLabel + "\t" + s);
				dt = null;
//				result.add(dbLabel + "\t" + dt.blockAddress + "\t" + dt.blockOffset );   //58429
				return s;
			}
//			System.out.println(dt.blockAddress + "," + dt.blockOffset );
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	@Override
	public List<String> getResult() {
		return result;
	}

	@Override
	public void clearResult() {
		result = null;
		result = new ArrayList<String>(3);
	}
	
	@Override
	public int getResultSize() {
		return result.size();
	}

}
