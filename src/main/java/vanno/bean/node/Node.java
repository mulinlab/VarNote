package main.java.vanno.bean.node;


public class Node {
	public int orgBeg;
	public int beg, end;
	public String chr;
	public String origStr;
	public int index;
	public String bgzStr;
	
	public Node() {
		index = 0;
		bgzStr = null;
		orgBeg = 0;
		beg = 0;
		end = 0;
		chr = null;
	}

	public Node(int beg, int end, String chr) {
		this.beg = beg;
		this.end = end;
		this.chr = chr;
	}
	
//	public Node(int beg, int end, String chr, String bgzStr) {
//		this(beg, end, chr);
//		this.bgzStr = bgzStr;
//	}
//	
//	public Node(int beg, int end, String chr, String origStr) {
//		this(beg, end, chr);
//		this.origStr = origStr;
//	}

	@Override
	public String toString() {
		return chr + "\t" + beg + "\t" + end;
	}
	
	public void clear() {
		bgzStr = null;
		orgBeg = -1;
		beg = -1;
		end = -1;
		chr = null;
		origStr = null;
	}

	public String getOrigStr() {
//		return this.toString();
		return this.origStr;
	}

	public Node clone()   {
		Node cloned = new Node(this.beg, this.end, null);
		cloned.bgzStr = this.bgzStr;
		cloned.origStr = this.origStr;
		cloned.index = this.index;
		return cloned;
	}
	
	
}
