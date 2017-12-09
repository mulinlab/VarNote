package stack;

import java.io.IOException;
import java.util.List;
import process.ProcessResult;
import bean.node.Node;

public class MergeStack extends AbstractReaderStack {
	private Node tempNode;
	public MergeStack(ProcessResult resultProcessor) {
		super(resultProcessor);
		tempNode = null;
		iseof = false;
	}
	
	public void clearST() {
		tempNode = null;
		iseof = false;
	}

	public void findOverlaps(List<Node> nodes) {
	}

	
	@Override
	public void findOverlap(Node query){
		if(iseof) return;
		try {	
			if(tempNode != null) {
				if(tempNode.index > query.index) {
					return;
				} else if(tempNode.index == query.index) {
					
					resultProcessor.doProcess(query, tempNode);
					tempNode = null;
				} 
			}
			
			Node node;
	
			while((node = (Node)it.nextNode()) != null) {
//				System.out.println(query.index + "===" + node.index);
				if(node.index > query.index) {
					tempNode = node;
					return;
				} else if(node.index == query.index) {
					resultProcessor.doProcess(query, node);
					tempNode = null;
				} 
			}
			
			if(node == null) iseof = true;
		} catch (IOException e) {
			
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
