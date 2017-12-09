package bean.writeIndex;

public class Chunk {
	private long chunkStart;
	private long chunkEnd;
	public Chunk(long chunkStart, long chunkEnd) {
		super();
		this.chunkStart = chunkStart;
		this.chunkEnd = chunkEnd;
	}
	public long getChunkEnd() {
		return chunkEnd;
	}
	public void setChunkEnd(long chunkEnd) {
		this.chunkEnd = chunkEnd;
	}
	public long getChunkStart() {
		return chunkStart;
	}
}