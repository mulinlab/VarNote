package main.java.vanno.process;

import java.util.List;

import main.java.vanno.bean.node.NodeWithFilePointer;


public interface ProcessBlock {
	public void doProcess(List<NodeWithFilePointer> results);
}
