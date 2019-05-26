package main.java.vanno.readers;

import java.io.IOException;

import htsjdk.samtools.seekablestream.SeekableStreamFactory;
import main.java.vanno.bean.database.Database;
import main.java.vanno.bean.database.VannoDatabase;
import main.java.vanno.bean.database.DatabaseConfig.IntersectType;
import main.java.vanno.bean.node.Node;
import main.java.vanno.bean.node.NodeWithFilePointer;
import main.java.vanno.bean.query.Log;
import main.java.vanno.constants.BasicUtils;
import main.java.vanno.constants.InvalidArgumentException;
import main.java.vanno.constants.VannoUtils;
import main.java.vanno.index.BlockCompressedFilePointerUtil;
import main.java.vanno.index.BlockFeature;
import main.java.vanno.index.MyBlockCompressedInputStream;
import main.java.vanno.index.SROB;
import main.java.vanno.index.BlockFeature.VannoFeature;
import main.java.vanno.process.VannoResultProcess;
import main.java.vanno.stack.ExactStack;
import main.java.vanno.stack.IntervalStack;
import main.java.vanno.stack.IntervalStackFC;

public abstract class VannoReader extends AbstractReader {
	protected static final int STOP = 1;
	protected static final int FIND = 2;
	protected static final int END = 0;
	
	protected MyBlockCompressedInputStream vannoFile;
	protected MyBlockCompressedInputStream vannoIndexFile;
	
	protected final byte[] buf = new byte[8];
	protected SROBListRead srobRead;
	protected SROB srob;
	protected boolean isEnd;
	protected boolean isBlockStart;
	
	protected VannoReader(final Database db, final boolean useJDK, final Log log) throws IOException {	
		super(db, useJDK, log);

		vannoIndexFile = new MyBlockCompressedInputStream(SeekableStreamFactory.getInstance().getBufferedStream(
				SeekableStreamFactory.getInstance().getStreamFor(((VannoDatabase)db).getDbIndexPath())), useJDK);
		
		srobRead = new SROBListRead();
		srob = new SROB(0, 0, 0);
		isEnd = false;
		
		if(db.getConfig().getIntersect() == IntersectType.OVERLAP) {
			stack = new IntervalStack(new VannoResultProcess());
		} else if(db.getConfig().getIntersect() == IntersectType.EXACT) {
			stack = new ExactStack(new VannoResultProcess()); 
		} else if(db.getConfig().getIntersect() == IntersectType.FULLCLOASE) {
			stack = new IntervalStackFC(new VannoResultProcess());
		} else {
			throw new InvalidArgumentException(VannoUtils.INTERSECT_ERROR);
		}
		
		vannoFile = new MyBlockCompressedInputStream(SeekableStreamFactory.getInstance().getBufferedStream(
				SeekableStreamFactory.getInstance().getStreamFor(((VannoDatabase)db).getVannoFile())), useJDK);
		((VannoResultProcess)stack.getResultProcessor()).setMFP(this.mFp);
	}

	@Override
	protected void initForChr(final int tid) throws IOException {
		srobRead.init();
		srob.setSROB(0, 0, 0);
		final Long addr =  idx.getMinOffForChr(tid);
		if(addr == null) {
			isEnd = true;
		} else {
			isEnd = false;
			vannoIndexFile.seek(addr);
		}
	}
	
	@Override
    public void close() {
		if(vannoFile != null) {
            try {
            		vannoIndexFile.close();
            		vannoFile.close();
            } catch (IOException e) {
            }
        }
		super.close();
	}
	
	protected class SROBListRead {
		private long blockAddress;
		private int preMin;
		
		public SROBListRead() {
			super();
			init();
		}
		public void init() {
			blockAddress = 0;
			preMin = 0;
		}
		public void setFilePointer(final long filePointer) {
			blockAddress = BlockCompressedFilePointerUtil.getBlockAddress(filePointer);
		}
		public int getMin(final int min) {
			preMin = preMin + min;
			return preMin;
		}
		public long getBlockAddress() {
			return blockAddress;
		}
	}
    
	protected class VannoIteratorImpl implements Iterator {
		protected boolean iseof;
		protected BlockFeature block;
		protected VannoFeature vannoFeature;
		protected final MyBlockCompressedInputStream is;
		protected NodeWithFilePointer node;

		protected VannoIteratorImpl(final MyBlockCompressedInputStream _is) {
			iseof = false;
			block = new BlockFeature();
			vannoFeature = null;
			is = _is;
			node = new NodeWithFilePointer(0, 0);
		}

		@Override
		public String next() throws IOException {
			return null;
		}

		public void seek(final long filePointer) throws IOException {
			is.seek(filePointer);
		}

		public void processBlock(final byte flag) throws IOException {

		}

		@Override
		public Node nextNode() throws IOException {
			if (iseof)
				return null;

			final byte flag = (byte) is.read();
			if (flag < 0) {
				if (flag <= BasicUtils.CHR_START) {
					iseof = true;
					return null;
				} else {
					processBlock(flag);
				}
			} else {
				vannoFeature = block.makeVannoFeature(is, flag);
			}
			node.bgzStr = null;
			node.beg = vannoFeature.getBeg();
			node.end = vannoFeature.getEnd();
			node.blockAddress = block.getBlockAddress();
			node.blockOffset = vannoFeature.getBlockOffset();
			return node;
		}
	}
	
	protected int readIndex(final Node query) throws IOException {
	 	while(!isEnd) {
	 		if(query.end < srob.getMin()) {
	 			return STOP;
			 } else {
				 if(query.beg <= srob.getMax()) {
					 return FIND;
				 }
				 isBlockStart = true;
				 nextBlock();
			 }
	 	}
	 	return END;
	}
	
	protected void nextBlock() throws IOException {
		int blockOffset = BasicUtils.readInt(vannoIndexFile);
 	 	if(blockOffset == BasicUtils.CHR_START) {
 	 		isEnd = true;
 	 	} else {
 	 		if(blockOffset == BasicUtils.BLOCK_ADDRESS) {
	 	 		srob.setVannoFilePointer(BasicUtils.readLong(vannoIndexFile, buf));
	 	 		srobRead.setFilePointer(srob.getVannoFilePointer());
	 	 	} else {
	 	 		srob.setVannoFilePointer(BlockCompressedFilePointerUtil.makeFilePointer(srobRead.getBlockAddress(), blockOffset));
	 	 	}
 	 		srob.setMin(srobRead.getMin(BasicUtils.readInt(vannoIndexFile)));
 	 	 	srob.setMax(srob.getMin() + BasicUtils.readInt(vannoIndexFile));
 	 	}
	}
}
