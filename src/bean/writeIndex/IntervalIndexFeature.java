package bean.writeIndex;

import constants.BasicUtils;

public class IntervalIndexFeature {
	private final int start;
	private final int end;
	private final int binNumber;
	private final int tid;
	private final Chunk chunk;

	public IntervalIndexFeature(final int tid, final int start, final int end, final Chunk chunk) {
		super();
		this.start = start;
		this.end = end;
		this.binNumber =  BasicUtils.regionToBin(start, end);
		this.tid = tid;
		this.chunk = chunk;
	}
	
	public int getStart() {
		return start;
	}

	public int getEnd() {
		return end;
	}

	public int getBinNumber() {
		return binNumber;
	}

	public int getTid() {
		return tid;
	}

	public Chunk getChunk() {
		return chunk;
	}

	public void setChunkEnd(long end){
		this.chunk.setChunkEnd(end);
	}
}
