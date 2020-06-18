package org.mulinlab.varnote.operations.process;

import java.util.List;

import org.mulinlab.varnote.utils.node.NodeWithFilePointer;


public interface ProcessBlock {
	public void doProcess(List<NodeWithFilePointer> results);
}
