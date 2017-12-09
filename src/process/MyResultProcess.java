package process;

import java.util.ArrayList;
import java.util.List;

import constants.BasicUtils;
import bean.node.Node;
import bean.node.NodePlus;

public class MyResultProcess implements ProcessResult{

	private List<NodePlus> results;
	private long firstBlock;
	private long currentBlock;
	private ProcessBlock processor;
	
	public MyResultProcess() {
		super();
		results = new ArrayList<NodePlus>();
	}
	
	public void setProcessor(ProcessBlock processor) {
		this.processor = processor;
	}

	@Override
	public void doProcess(Node q, Node d) {
//		System.out.println("hit====" + q + ", " + d);
		NodePlus dt = (NodePlus)d;
		if(results.size() == 0) {
			firstBlock = BasicUtils.getBlockAddress(dt.filePointer);
		} else {
			currentBlock = BasicUtils.getBlockAddress(dt.filePointer);
			
			if(currentBlock != firstBlock) {
				processor.doProcess(results);
				results = new ArrayList<NodePlus>();
				firstBlock = currentBlock;
			}
		}
		results.add(new NodePlus(q, dt.filePointer));
	}

	public List<NodePlus> getResults() {
		return results;
	}
}
