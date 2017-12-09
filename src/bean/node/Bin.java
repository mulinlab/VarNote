package bean.node;

public class Bin {
	private final long start;
	private int max;
	private int size;
	
	public Bin(final long start, final int max) { //
		super();
		this.start = start;
		this.max = max;
		this.size = 0;
	}
	
	public Bin(final long start, final int max, final int size) { //
		super();
		this.start = start;
		this.max = max;
		this.size = size;
	}

	public void updateMax(int end) {
		if(end > this.max) {
			this.max = end;
		}
		this.size++;
	}
	
	public long getStart() {
		return start;
	}

	
	public int getMax() {
		return max;
	}

	public int getSize() {
		return size;
	}
}
