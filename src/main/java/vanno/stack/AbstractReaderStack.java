package main.java.vanno.stack;

import java.util.List;

import main.java.vanno.bean.node.Node;
import main.java.vanno.process.ProcessResult;
import main.java.vanno.readers.AbstractReader.Iterator;

public abstract class AbstractReaderStack{
	
	protected Iterator it = null;
	protected ProcessResult resultProcessor;
	protected boolean iseof;
	
	public AbstractReaderStack(ProcessResult resultProcessor) {
		super();
		this.resultProcessor = resultProcessor;
		iseof = false;
	}	
	
	public void setResultProcessor(final ProcessResult resultProcessor) {
		this.resultProcessor = resultProcessor;
	}

	public void setIterator(final Iterator it) {
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
	public abstract boolean findOverlapInST(Node query) ;
}
