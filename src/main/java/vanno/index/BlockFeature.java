package main.java.vanno.index;

import htsjdk.samtools.util.BlockCompressedFilePointerUtil;
import main.java.vanno.constants.BasicUtils;

import java.io.IOException;

public final class BlockFeature {
	private long blockAddress;
	private int preBeg;
	private int offsetEnd;
	private int preOffset;
	private int avgOffset;
	private VannoFeature feature;
	private boolean isReadBlock;
	
	public BlockFeature() {
		feature = new VannoFeature();
		isReadBlock = false;
	}
	
	public void readBlock(final MyBlockCompressedInputStream is, final int min, final int flag, byte[] buf) throws IOException {	
		preBeg = min;
		
		switch (flag) {
			case BasicUtils.BLOCK_START_WO_END:
				 offsetEnd = 1;
				 break;
			case BasicUtils.BLOCK_START_WITH_END_BTYE:
				 offsetEnd = (byte)is.read() & 0xFF;
				 break;
			case BasicUtils.BLOCK_START_WITH_END_SHORT:
				 offsetEnd = BasicUtils.readShort(is) & 0xFFFF;
				 break;
			case BasicUtils.BLOCK_START_WITH_END_INT:
				 offsetEnd = BasicUtils.readInt(is);
				 break;
		}
		
		final long filePointer = BasicUtils.readLong(is, buf);
		this.blockAddress = BlockCompressedFilePointerUtil.getBlockAddress(filePointer);
		this.preOffset = BlockCompressedFilePointerUtil.getBlockOffset(filePointer);
		
		avgOffset = readShort(is);
		isReadBlock = true;
	}
	
	public int readShort(final MyBlockCompressedInputStream is) throws IOException {
		int a = BasicUtils.readShort(is);
		if(a < 0) a = BasicUtils.readInt(is);
		return a;
	}
	
	public VannoFeature makeFirstVannoFeature() throws IOException {
		feature.setVannoFeature(this.preOffset, this.preBeg, this.offsetEnd);
		return feature;
	}

	public VannoFeature makeVannoFeature(final MyBlockCompressedInputStream is, final byte flag) throws IOException{
		final int begflag = flag & 7, endflag = flag >> 3 & 3, offsetSign = flag >> 5 & 1, offsetflag = flag >> 6 & 1;
		preBeg = preBeg + getBeg(is, begflag);
		offsetEnd = getEnd(is, endflag);
		
		int offset = 0;
		if(offsetflag == 0) {
			offset =  (byte)is.read() & 0xFF;
		} else if(offsetflag == 1) {
			offset = BasicUtils.readShort(is);
			if(offset == BasicUtils.INT_START) {
				offset = BasicUtils.readInt(is);
			} 
		} 
		if(offsetSign == 0) {
			preOffset = preOffset + offset + avgOffset;
		} else {
			preOffset = preOffset + avgOffset - offset;
		}
		feature.setVannoFeature(preOffset, preBeg, offsetEnd);
		return feature;
	}
	
	public int getBeg(final MyBlockCompressedInputStream is, final int val) throws IOException {
		if(val < 5) {
			return val;
		} else if(val == 5) {
			return (byte)is.read() & 0xFF;
		} else if(val == 6) {
			return BasicUtils.readShort(is) & 0xFFFF;
		} else {
			return BasicUtils.readInt(is);
		} 
	}
	
	public int getEnd(final MyBlockCompressedInputStream is, final int val) throws IOException {
		if(val == 0) {
			return 1;
		} else if(val == 1) {
			return (byte)is.read() & 0xFF;
		} else if(val == 2) {
			return BasicUtils.readShort(is) & 0xFFFF;
		} else {
			return BasicUtils.readInt(is);
		}
	}

	public final class VannoFeature {
		int blockOffset;
		int beg;
		int end;
		
		public VannoFeature() {
			
		}
		
		public void setVannoFeature(final int blockOffset, final int beg, final int end) {
			this.blockOffset = blockOffset;
			this.beg = beg;
			this.end = beg + end;
		}

		public int getBlockOffset() {
			return blockOffset;
		}
		public int getBeg() {
			return beg;
		}
		public int getEnd() {
			return end;
		}
		public void setEnd(int end) {
			this.end = end;
		}	
	}

	public long getBlockAddress() {
		return blockAddress;
	}

	public void setReadBlock(boolean isReadBlock) {
		this.isReadBlock = isReadBlock;
	}

	public boolean isReadBlock() {
		return isReadBlock;
	}
	
	
}
