package process;

import bean.node.Node;

public class CountResultProcess implements ProcessResult{

	private long count;
	
	public CountResultProcess() {
		super();
		count = 0;
	}
	

	@Override
	public void doProcess(Node q, Node d) {
		count++;
	}

	public long getResults() {
		return count;
	}
}
