package test;

import java.util.List;
import java.util.Map;

import bean.node.Node;

public class NodeWithRefAltBak extends Node {
	public String ref;
	public String[] alts;
	public String info;
	public String filter;
	public String qulity;
	
	public Map<Integer, String> colToValMap;
	public List<String> vcfGS;
	
	public NodeWithRefAlt() {
		super();
	}

	public NodeWithRefAlt(int beg, int end, String chr, String origStr) {
		super(beg, end, chr, origStr);
		// TODO Auto-generated constructor stub
	}

	public NodeWithRefAlt(int beg, int end, String chr) {
		super(beg, end, chr);
	}
}
