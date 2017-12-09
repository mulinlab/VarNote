package stack;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import process.ProcessResult;
import bean.node.Node;

public class ExactStack extends AbstractReaderStack {
	protected List<Node> st;
	protected Node tempNode;
	
	public ExactStack(ProcessResult resultProcessor) {
		super(resultProcessor);
		tempNode = null;
	}
	
	public void clearST() {
		st = new ArrayList<Node>(10);
		tempNode = null;
	}
	
	public void findOverlaps(List<Node> list) {
		for (Node intvNode : list) {
			findOverlap(intvNode);
		}
	}
	
	public void loadDB(Node query) {
		Node curNode = null;
		try {
			while((curNode = it.nextNode()) != null) {
//				System.out.println("curNode = " + curNode);
				if(query.beg == curNode.beg) {
					st.add(curNode);
//					System.out.println("print");
					if(query.end == curNode.end) resultProcessor.doProcess(query, curNode); 
				} else if(query.beg < curNode.beg){
					tempNode = curNode;
					break;
				} else {
					continue;
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void findOverlap(Node query) {
		if(it == null) return;
//		if(query.chr.equals("2")) 
//			System.out.println("query = " + query.beg + ", " + query.end + ", " + query.chr);
		if(tempNode == null) {
			loadDB(query);
			return;
		}
		if(query.beg > tempNode.beg) {
			st = new ArrayList<Node>(10);
			loadDB(query);
		} else if(query.beg == tempNode.beg) {
			st.add(tempNode);
			if(query.end == tempNode.end) resultProcessor.doProcess(query, tempNode); 
			loadDB(query);
		} else {
			for(int k=0; k<st.size(); k++) {
				if(query.beg == st.get(k).beg && query.end == st.get(k).end) resultProcessor.doProcess(query, st.get(k)); 
			}
		}
			
			
	}
}
