package index;

import htsjdk.samtools.util.BlockCompressedFilePointerUtil;

import java.io.IOException;

import constants.BasicUtils;
import readers.AbstractReader;

public final class BlockFeature {
	private final long blockAddress;
	private int preBeg;
	private int offsetEnd;
	private int preOffset;
	private final int avgOffset;
	private final MyBlockCompressedInputStream is;
	
	public BlockFeature(final MyBlockCompressedInputStream _is, final int min, final int flag) throws IOException {	
		preBeg = min;
		is = _is;
		
		switch (flag) {
			case BasicUtils.BLOCK_START_WO_END:
				 offsetEnd = 1;
				 break;
			case BasicUtils.BLOCK_START_WITH_END_BTYE:
				 offsetEnd = (byte)is.read() & 0xFF;
				 break;
			case BasicUtils.BLOCK_START_WITH_END_SHORT:
				 offsetEnd = AbstractReader.readShort(is) & 0xFFFF;
				 break;
			case BasicUtils.BLOCK_START_WITH_END_INT:
				 offsetEnd = AbstractReader.readInt(is);
				 break;
		}
		
		final long filePointer = AbstractReader.readLong(is);
		this.blockAddress = BlockCompressedFilePointerUtil.getBlockAddress(filePointer);
		this.preOffset = BlockCompressedFilePointerUtil.getBlockOffset(filePointer);
		
		avgOffset = readShort();
	}
	
	public int readShort() throws IOException {
		int a = AbstractReader.readShort(is);
		if(a < 0) a = AbstractReader.readInt(is);
		return a;
	}
	
	public VannoFeature makeFirstVannoFeature() throws IOException {
		return new VannoFeature(this.preOffset, this.preBeg, this.offsetEnd);
	}

	public VannoFeature makeOtherVannoFeature(final byte flag) throws IOException{
		final int begflag = flag & 7, endflag = flag >> 3 & 3, offsetSign = flag >> 5 & 1, offsetflag = flag >> 6 & 1;
		preBeg = preBeg + getBeg(begflag);
		offsetEnd = getEnd(endflag);
		
		int offset = 0;
		if(offsetflag == 0) {
			offset =  (byte)is.read() & 0xFF;
		} else if(offsetflag == 1) {
			offset = AbstractReader.readShort(is);
			if(offset == BasicUtils.INT_START) {
				offset = AbstractReader.readInt(is);
			} 
		} 
		if(offsetSign == 0) {
			preOffset = preOffset + offset + avgOffset;
		} else {
			preOffset = preOffset + avgOffset - offset;
		}
		
		return new VannoFeature(preOffset, preBeg, offsetEnd);
	}
	
	public int getBeg(final int val) throws IOException {
		if(val < 5) {
			return val;
		} else if(val == 5) {
			return (byte)is.read() & 0xFF;
		} else if(val == 6) {
			return AbstractReader.readShort(is) & 0xFFFF;
		}  {
			return AbstractReader.readInt(is);
		} 
	}
	
	public int getEnd(final int val) throws IOException {
		if(val == 0) {
			return 1;
		} else if(val == 1) {
			return (byte)is.read() & 0xFF;
		} else if(val == 2) {
			return AbstractReader.readShort(is) & 0xFFFF;
		} else {
			return AbstractReader.readInt(is);
		}
	}

	public final class VannoFeature {
		final int blockOffset;
		final int beg;
		int end;
		public VannoFeature(final int blockOffset, final int beg, final int end) {
			super();
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
}
