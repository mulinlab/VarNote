package org.mulinlab.varnote.operations.process;

import java.util.List;

import org.mulinlab.varnote.utils.enumset.Mode;
import org.mulinlab.varnote.utils.node.Node;

public interface ProcessResult {
	public void doProcess(Node d);
	public List<String> getResult();
	public int getResultSize();
	public void initResult();
}
