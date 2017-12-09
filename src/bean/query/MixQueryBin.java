package bean.query;

import java.util.ArrayList;
import java.util.List;

import constants.BasicUtils;
import bean.node.Node;

public class MixQueryBin {
	private List<Node> nodes;
	private int min;
	private int max;
	private String chr;
	
	public MixQueryBin() {
		super();
		nodes = new ArrayList<Node>(20);
		min = 0;
		max = 0;
	}
	
	public void addNode(Node node) {
		if(nodes.size() == 0) {
			min = node.beg;
			chr = node.chr;
		}
		
		if(node.end > max) {
			max = node.end;
		}
		nodes.add(node);
	}

	public int getMin() {
		return min;
	}

	public int getMax() {
		return max;
	}

	public String getChr() {
		return chr;
	}

	public boolean hasData() {
		if(nodes.size() > 0) {
			return true;
		} else {
			return false;
		}
	}
	
	public int computeBinBumber(Node node) {
		return BasicUtils.regionToBin4(node.beg, node.end);
	}
	
	public List<Integer> getBinNumbers() {
		return BasicUtils.reg2bins(min, max);
	}

	public int getNodesSize() {
		return nodes.size();
	}
	
	public List<Node> getNodes() {
		return nodes;
	}
}
