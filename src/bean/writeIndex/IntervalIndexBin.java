package bean.writeIndex;


import java.util.ArrayList;
import java.util.List;

public class IntervalIndexBin {
	private final int referenceSequence;
	private final int binNumber;
	private List<Chunk> chunks;
	private Chunk lastChunk;
	
	public IntervalIndexBin(final int referenceSequence, final int binNumber) {
		this.referenceSequence = referenceSequence;
		this.binNumber = binNumber;
    }

	public int getReferenceSequence() {
		return referenceSequence;
	}

	public int getBinNumber() {
		return binNumber;
	}
	
	public Chunk getLastChunk() {
		return lastChunk;
	}
	
	public List<Chunk> getChunks() {
		return chunks;
	}
	
	public boolean containsChunks() {
	    return chunks != null;
	}
	 
	public void addInitialChunk(final Chunk newChunk){
		chunks = new ArrayList<Chunk>();
		putChunk(newChunk);
    }

	public void putOffset(int offset, long filePointer) {

		
	}
	
	public void putChunk(Chunk chunk) {
		chunks.add(chunk);
		lastChunk = chunk;
	}
}	