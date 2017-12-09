package stack;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import process.ProcessResult;
import bean.node.Node;

public class IntervalStack extends AbstractReaderStack {
	protected List<Node> st;
	protected Node tempNode;
	
	public IntervalStack(ProcessResult resultProcessor) {
		super(resultProcessor);
		if(st == null) {
			st = new ArrayList<Node>();
		}
		tempNode = null;
	}
	
	public void clearST() {
		st = new ArrayList<Node>();
		tempNode = null;
	}
	
	public void findOverlaps(List<Node> list) {
		for (Node intvNode : list) {
			findOverlap(intvNode);
		}
	}
	
	public void findOverlap(Node query) {
//		System.out.println("query==========" + query.beg);
		if(it == null) return;
//		if(query.chr.equals("2")) 
//			System.out.println("query = " + query.beg + ", " + query.end + ", " + query.chr);
		
		for(int k=0; k<st.size(); k++) { //每一条query进来的时候，堆栈里面不符合要求的都出堆栈
			if(st.get(k).end <= query.beg) {
				st.remove(k);
				k--;
			} else if(st.get(k).end > query.beg && st.get(k).beg < query.end) {
				resultProcessor.doProcess(query, st.get(k)); 
			} else if(st.get(k).beg >= query.end) {
//				System.out.println("st.get(k)=" +st.get(k));
				return;
			}
		}
		
		try {
			if(tempNode != null) { //tempNode 是最后一条记录 start 会大于 query end
//				if(query.chr.equals("2"))	
//					System.out.println("tempNode = " + tempNode.beg + ", " + tempNode.end + ", " + tempNode.chr);
				if( tempNode.beg >= query.end) {	//当前记录的起始位置比查询的结尾还大，则不需要继续
					return;
				} else if(tempNode.end > query.beg && tempNode.beg < query.end) {
//					if(!st.contains(tempNode)) {
						st.add(tempNode);
						resultProcessor.doProcess(query, tempNode); 
						tempNode = null;
//					}
				}
			}
	
			Node curNode = null;
			while((curNode = it.nextNode()) != null) {
//				if(!curNode.chr.equals(query.chr)) {
//					tempNode = curNode;
//					return;
//				}
//				if(query.chr.equals("2")) 
//					System.out.println("curNode = " + curNode.beg + ", " + curNode.end + ", " + curNode.chr);
				if(curNode.end > query.beg && curNode.beg < query.end) {
					st.add(curNode);
					resultProcessor.doProcess(query, curNode); 
				} else if(curNode.beg >= query.end) {
					tempNode = curNode;
					break;
				} else { //if(curNode.end <= query.beg) {
					continue;
				} 
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
