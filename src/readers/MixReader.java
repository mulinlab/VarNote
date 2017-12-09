package readers;

import java.io.IOException;

import process.ProcessResult;
import stack.AbstractReaderStack;
import bean.index.Index;
import bean.node.Node;

public abstract class MixReader extends AbstractReader {
	
	public MixReader(final String fn, final Index idx, final AbstractReaderStack stack) throws IOException {	
		super(fn, idx);
		this.stack = stack;
	}

	public abstract void initForChr(String chr);
	public abstract void doQuery(Node query);
	public abstract void doEnd();
	public abstract ProcessResult getProcess();
}
