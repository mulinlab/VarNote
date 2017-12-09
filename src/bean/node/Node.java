package bean.node;


public class Node {
	public int beg, end;
	public String chr;
	public String origStr;
	public int index;
	public String dbID;
	
	public Node() {
	}

	public Node(int beg, int end, String chr) {
		this.beg = beg;
		this.end = end;
		this.chr = chr;
	}
	
	public Node(int beg, int end, String chr, String origStr) {
		this(beg, end, chr);
		this.origStr = origStr;
	}

	@Override
	public String toString() {
		return chr + "\t" + beg + "\t" + end;
	}
	
	public String coreInfo() {
//		return chr + "\t" + beg + "\t" + end + "\t";
		return index + "\t";
	}
	
	public boolean isPositionEq(Node obj) {
		if(obj.chr.equals(chr) && (obj.beg == beg) && (obj.end == end))
			return true;
		else 
			return false;
	}

	public String getOrigStr() {
//		return this.toString();
		return this.origStr;
	}
	
	
}
