package stack;

import java.io.IOException;
import process.ProcessResult;
import bean.node.Node;

public class IntervalStackFC extends IntervalStack {
	
	public IntervalStackFC(ProcessResult resultProcessor) {
		super(resultProcessor);
	}
	
	public void findOverlap(Node query) {
		if(it == null) return;
		
		for(int k=0; k<st.size(); k++) { 
			if(st.get(k).end < query.beg) {
				st.remove(k);
				k--;
			} else if(st.get(k).end >= query.beg && st.get(k).beg <= query.end) {
				resultProcessor.doProcess(query, st.get(k)); 
			} else if(st.get(k).beg > query.end) {
				return;
			}
		}
		
		try {
			if(tempNode != null) { //tempNode 是最后一条记录 start 会大于 query end
//				System.out.println("tempNode = " + tempNode.beg + ", " + tempNode.end + ", " + tempNode.chr);
				if( tempNode.beg > query.end) {	//当前记录的起始位置比查询的结尾还大，则不需要继续
					return;
				} else if(tempNode.end >= query.beg && tempNode.beg <= query.end) {
//					if(!st.contains(tempNode)) {
						st.add(tempNode);
						resultProcessor.doProcess(query, tempNode); 
						tempNode = null;
//					}
				}
			}
	
			Node curNode = null;
			while((curNode = it.nextNode()) != null) {
//				System.out.println("curNode = " + curNode.beg + ", " + curNode.end + ", " + curNode.chr);
				if(curNode.end >= query.beg && curNode.beg <= query.end) {
					st.add(curNode);
					resultProcessor.doProcess(query, curNode); 
				} else if(curNode.beg > query.end) {
					tempNode = curNode;
					break;
				} else if(curNode.end < query.beg) {
					continue;
				} 
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
