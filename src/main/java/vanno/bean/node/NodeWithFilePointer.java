package main.java.vanno.bean.node;


public final class NodeWithFilePointer extends Node {
	public long blockAddress;
	public int blockOffset;
	
	public NodeWithFilePointer(final long blockAddress, final int blockOffset) {
		super();
		this.blockAddress = blockAddress;
		this.blockOffset = blockOffset;
	}

	public NodeWithFilePointer(final int beg, final int end, final long blockAddress, final int blockOffset) {
		super(beg, end, "");
		this.blockAddress = blockAddress;
		this.blockOffset = blockOffset;
	} 
	
	public Node clone()  {
		NodeWithFilePointer cloned = new NodeWithFilePointer(blockAddress, blockOffset);
		
		cloned.beg = this.beg;
		cloned.end = this.end;
		cloned.bgzStr = this.bgzStr;
		return cloned;
	}
}
