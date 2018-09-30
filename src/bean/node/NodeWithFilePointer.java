package bean.node;


public class NodeWithFilePointer extends Node {
	public final long blockAddress;
	public final int blockOffset;
	public final Node queryNode;
	
	public NodeWithFilePointer(final Node queryNode, final long blockAddress, final int blockOffset) {
		super();
		this.queryNode = queryNode;
		this.blockAddress = blockAddress;
		this.blockOffset = blockOffset;
	}

	public NodeWithFilePointer(final int beg, final int end, final long blockAddress, final int blockOffset) {
		super(beg, end, "");
		this.queryNode = null;
		this.blockAddress = blockAddress;
		this.blockOffset = blockOffset;
	} 
}
