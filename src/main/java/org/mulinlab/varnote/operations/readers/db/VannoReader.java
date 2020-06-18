package org.mulinlab.varnote.operations.readers.db;

import java.io.IOException;

import htsjdk.samtools.util.BlockCompressedInputStream;
import org.mulinlab.varnote.config.param.DBParam;
import org.mulinlab.varnote.constants.GlobalParameter;
import org.mulinlab.varnote.operations.process.CountResultProcess;
import org.mulinlab.varnote.operations.process.ProcessResult;
import org.mulinlab.varnote.utils.database.DatabaseFactory;
import org.mulinlab.varnote.utils.enumset.IntersectType;
import htsjdk.samtools.seekablestream.SeekableStreamFactory;
import org.mulinlab.varnote.utils.database.Database;
import org.mulinlab.varnote.utils.database.VannoDatabase;
import org.mulinlab.varnote.utils.node.LocFeature;
import org.mulinlab.varnote.utils.node.NodeWithFilePointer;
import org.mulinlab.varnote.exceptions.InvalidArgumentException;
import org.mulinlab.varnote.utils.VannoUtils;
import org.mulinlab.varnote.utils.gz.BlockCompressedFilePointerUtil;
import org.mulinlab.varnote.utils.block.BlockFeature;
import org.mulinlab.varnote.utils.block.SROB;
import org.mulinlab.varnote.utils.block.BlockFeature.VannoFeature;
import org.mulinlab.varnote.operations.process.VannoResultProcess;
import org.mulinlab.varnote.operations.stack.ExactStack;
import org.mulinlab.varnote.operations.stack.IntervalStack;
import org.mulinlab.varnote.operations.stack.IntervalStackFC;

public abstract class VannoReader extends AbstractDBReader {
	protected static final int STOP = 1;
	protected static final int FIND = 2;
	protected static final int END = 0;
	
	protected BlockCompressedInputStream vannoFile;
	protected BlockCompressedInputStream vannoIndexFile;
	
	protected final byte[] buf = new byte[8];
	protected SROBListRead srobRead;
	protected SROB srob;
	protected boolean isEnd;
	protected boolean isBlockStart;

	protected VannoReader(final String db) throws IOException {
		this(DatabaseFactory.readDatabase(new DBParam(db)));
	}

	protected VannoReader(final String db, final boolean isCount) throws IOException {
		this(DatabaseFactory.readDatabase(new DBParam(db)), isCount);
	}

	protected VannoReader(final Database db) throws IOException {
		this(db, GlobalParameter.DEFAULT_IS_COUNT);
	}

	protected VannoReader(final Database db, final boolean isCount) throws IOException {
		super(db, isCount);

		vannoIndexFile = new BlockCompressedInputStream(SeekableStreamFactory.getInstance().getBufferedStream(
				SeekableStreamFactory.getInstance().getStreamFor(((VannoDatabase)db).getDbIndexPath())));

		srobRead = new SROBListRead();
		srob = new SROB(0, 0, 0);
		isEnd = false;

		vannoFile = new BlockCompressedInputStream(SeekableStreamFactory.getInstance().getBufferedStream(
				SeekableStreamFactory.getInstance().getStreamFor(((VannoDatabase)db).getVannoFile())));

		ProcessResult processResult;

		if(isCount) {
			processResult = new CountResultProcess();
		} else {
			processResult = new VannoResultProcess();
		}

		if(db.getConfig().getIntersect() == IntersectType.INTERSECT) {
			stack = new IntervalStack(processResult);
		} else if(db.getConfig().getIntersect() == IntersectType.EXACT) {
			stack = new ExactStack(processResult);
		} else if(db.getConfig().getIntersect() == IntersectType.FULLCLOASE) {
			stack = new IntervalStackFC(processResult);
		} else {
			throw new InvalidArgumentException(VannoUtils.INTERSECT_ERROR);
		}

		if(!isCount) {
			((VannoResultProcess)stack.getResultProcessor()).setMFP(this.mFp);
		}
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
		protected final BlockCompressedInputStream is;
		protected NodeWithFilePointer node;

		protected VannoIteratorImpl(final BlockCompressedInputStream _is) {
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
		public LocFeature nextNode() throws IOException {
			if (iseof)
				return null;

			final byte flag = (byte) is.read();
			if (flag < 0) {
				if (flag <= GlobalParameter.CHR_START) {
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
	
	protected int readIndex(final LocFeature query) throws IOException {
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
		int blockOffset = GlobalParameter.readInt(vannoIndexFile);
 	 	if(blockOffset == GlobalParameter.CHR_START) {
 	 		isEnd = true;
 	 	} else {
 	 		if(blockOffset == GlobalParameter.BLOCK_ADDRESS) {
	 	 		srob.setVannoFilePointer(GlobalParameter.readLong(vannoIndexFile, buf));
	 	 		srobRead.setFilePointer(srob.getVannoFilePointer());
	 	 	} else {
	 	 		srob.setVannoFilePointer(BlockCompressedFilePointerUtil.makeFilePointer(srobRead.getBlockAddress(), blockOffset));
	 	 	}
 	 		srob.setMin(srobRead.getMin(GlobalParameter.readInt(vannoIndexFile)));
 	 	 	srob.setMax(srob.getMin() + GlobalParameter.readInt(vannoIndexFile));
 	 	}
	}
}
