package org.mulinlab.varnote.operations.stack;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.mulinlab.varnote.operations.process.ProcessResult;
import org.mulinlab.varnote.utils.node.LocFeature;

public final class ExactStack extends AbstractReaderStack {
	protected List<LocFeature> st;
	protected LocFeature tempNode;
	
	public ExactStack(final ProcessResult resultProcessor) {
		super(resultProcessor);
		if(st == null) {
			st = new ArrayList<LocFeature>();
		}
		tempNode = null;
	}

	public void clearST() {
		st = null;
		st = new ArrayList<LocFeature>();
		tempNode = null;
	}

	
	public void findOverlaps(List<LocFeature> list) {
		for (LocFeature intvNode : list) {
			findOverlap(intvNode);
		}
	}
	
	public void loadDB(LocFeature query) {
		LocFeature curNode = null;
		try {
			while((curNode = it.nextNode()) != null) {
				if(query.beg == curNode.beg) {
					if(query.end == curNode.end) resultProcessor.doProcess(curNode); 
					st.add(curNode.clone());
					
				} else if(query.beg < curNode.beg){
					tempNode = curNode.clone();
					break;
				} else {
					continue;
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void findOverlap(LocFeature query) {
		if(it == null) return;
		
		if(tempNode == null) {
			loadDB(query);
			return;
		}
		if(query.beg > tempNode.beg) {
			st = null;
			st = new ArrayList<LocFeature>();
			loadDB(query);
			
		} else if(query.beg == tempNode.beg) {
			if(query.end == tempNode.end) resultProcessor.doProcess(tempNode); 
			st.add(tempNode.clone());
			
			loadDB(query);
		} else {
			for(int k=0; k<st.size(); k++) {
				if(query.beg == st.get(k).beg && query.end == st.get(k).end) resultProcessor.doProcess(st.get(k)); 
			}
		}	
	}

	@Override
	public boolean findOverlapInST(LocFeature query) {
		// TODO Auto-generated method stub
		return false;
	}
}
