package org.mulinlab.varnote.operations.process;

import java.util.List;

import org.mulinlab.varnote.utils.node.LocFeature;

public final class CountResultProcess implements ProcessResult{

	private long count;
	
	public CountResultProcess() {
		super();
		count = 0;
	}
	

	@Override
	public void doProcess(LocFeature d) {
		count++;
	}

	public long getResults() {
		return count;
	}


	@Override
	public List<String> getResult() {
		return null;
	}


	@Override
	public int getResultSize() {
		return 0;
	}


	@Override
	public void initResult() {
		count = 0;
	}
}
