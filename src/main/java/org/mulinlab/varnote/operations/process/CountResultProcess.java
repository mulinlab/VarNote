package org.mulinlab.varnote.operations.process;

import java.util.List;

import org.mulinlab.varnote.utils.node.LocFeature;

public final class CountResultProcess implements ProcessResult{

	private int count;
	
	public CountResultProcess() {
		super();
		count = 0;
	}
	

	@Override
	public void doProcess(LocFeature d) {
		count++;
	}


	@Override
	public List<String> getResult() {
		return null;
	}


	@Override
	public int getResultSize() {
		return count;
	}


	@Override
	public void initResult() {
		count = 0;
	}
}
