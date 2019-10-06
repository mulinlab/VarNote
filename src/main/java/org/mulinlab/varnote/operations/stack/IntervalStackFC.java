package org.mulinlab.varnote.operations.stack;

import java.io.IOException;
import org.mulinlab.varnote.operations.process.ProcessResult;
import org.mulinlab.varnote.utils.node.Node;

public final class IntervalStackFC extends IntervalStack {	
	public IntervalStackFC(final ProcessResult resultProcessor) {
		super(resultProcessor);
	}
	
	public boolean findOverlapInST(Node query) {
		for(int k=0; k<st.size(); k++) { 
			if(st.get(k).end < query.beg) {
				st.remove(k);
				k--;
			} else {
				if(st.get(k).beg <= query.end) {
					resultProcessor.doProcess(st.get(k)); 
				} else {
					return false;
				}
			}
		}
		if(tempNode != null) { 
			if( tempNode.beg > query.end) {
				return false;
			} else if(tempNode.end >= query.beg) {
				resultProcessor.doProcess(tempNode); 
				st.add(tempNode.clone());
				tempNode = null;
			}
		}
		return true;
	}
	
	public void findOverlap(Node query) {
		if(it == null) return;
		if(!findOverlapInST(query)) return;
		try {
			Node curNode = null;
			while((curNode = it.nextNode()) != null) {
				if(curNode.end < query.beg) {
					continue;
				} else {
					if(curNode.beg <= query.end) { 
						resultProcessor.doProcess(curNode); 
						st.add(curNode.clone());
					} else {
						tempNode = curNode.clone();
						break;
					}
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
