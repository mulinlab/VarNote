package stack;

import java.util.List;

import process.ProcessResult;
import readers.AbstractReader.Iterator;
import bean.node.Node;

public abstract class AbstractReaderStack{
	
	protected Iterator it = null;
	protected ProcessResult resultProcessor;
	protected boolean iseof;
	
	public AbstractReaderStack(ProcessResult resultProcessor) {
		super();
		this.resultProcessor = resultProcessor;
		iseof = false;
	}	
	
	public void setIterator(Iterator it) {
		this.it = it;
		iseof = false;
	}
	
	public void clearST() {
	}
	
	public void setIseof(boolean iseof) {
		this.iseof = iseof;
	}

	public Iterator getIt() {
		return it;
	}

	public ProcessResult getResultProcessor() {
		return resultProcessor;
	}

	public abstract void findOverlaps(List<Node> nodes);
	public abstract void findOverlap(Node node);
	
	
}
