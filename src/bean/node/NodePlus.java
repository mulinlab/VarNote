package bean.node;


public class NodePlus extends Node {
	public final long filePointer;
	public final Node queryNode;
	
	public NodePlus(final Node queryNode, final long filePointer) {
		super();
		this.queryNode = queryNode;
		this.filePointer = filePointer;
	}

	public NodePlus(final int beg, final int end, final String chr, final long filePointer) {
		super(beg, end, chr);
		this.queryNode = null;
		this.filePointer = filePointer;
	} 
	
	public long getFilePointer() {
		return filePointer;
	}
	
	public Node getQuery() {
		return queryNode;
	}

	@Override
	public String toString() {
		return "NodePlus [filePointer=" + filePointer + ", beg=" + beg
				+ ", end=" + end + ", chr=" + chr + "]";
	}
}
