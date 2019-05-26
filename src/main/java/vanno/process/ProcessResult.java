package main.java.vanno.process;

import java.util.List;

import main.java.vanno.bean.node.Node;

public interface ProcessResult {
	public void doProcess(Node d);
	public List<String> getResult();
	public int getResultSize();
	public void initResult();
}
