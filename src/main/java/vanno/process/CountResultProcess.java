package main.java.vanno.process;

import java.util.List;

import main.java.vanno.bean.node.Node;

public final class CountResultProcess implements ProcessResult{

	private long count;
	
	public CountResultProcess() {
		super();
		count = 0;
	}
	

	@Override
	public void doProcess(Node d) {
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
	}
}
