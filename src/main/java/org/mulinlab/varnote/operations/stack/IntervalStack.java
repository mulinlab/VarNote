package org.mulinlab.varnote.operations.stack;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import org.mulinlab.varnote.operations.process.ProcessResult;
import org.mulinlab.varnote.utils.node.LocFeature;

public class IntervalStack extends AbstractReaderStack {
	protected List<LocFeature> st;
	protected LocFeature tempNode;
	
	public IntervalStack(final ProcessResult resultProcessor) {
		super(resultProcessor);
		if(st == null) {
			st = new LinkedList<LocFeature>();
		}
		tempNode = null;
	}

	public void clearST() {
		st = null;
		st = new LinkedList<LocFeature>();
		tempNode = null;
	}
	
	public void findOverlaps(List<LocFeature> list) {
		for (LocFeature intvNode : list) {
			findOverlap(intvNode);
		}
	}
	
	public boolean findOverlapInST(LocFeature query) {

		for(int k=0; k<st.size(); k++) { 
			if(st.get(k).end <= query.beg) {
				st.remove(k);
				k--;
			} else {
				if(st.get(k).beg < query.end) {
					resultProcessor.doProcess(st.get(k)); 
				} else {
					return false;
				}
			}
		}
		
		if(tempNode != null) { 
			if( tempNode.beg >= query.end) {	
				return false;
			} else if(tempNode.end > query.beg) {
				resultProcessor.doProcess(tempNode); 			
				st.add(tempNode.clone());

				tempNode = null;
			}
		}
		return true;
	}
	
	public void findOverlap(LocFeature query) {

//		if(query.beg == 668629) {
//			System.out.println(query);
//			System.out.println(query.origStr);
//		}
		if(it == null) return;
		if(!findOverlapInST(query)) return;
		try {
			LocFeature curNode = null;
			while((curNode = it.nextNode()) != null) {

				if(curNode.end <= query.beg) {
					continue;
				} else {
					if(curNode.beg < query.end) {
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
