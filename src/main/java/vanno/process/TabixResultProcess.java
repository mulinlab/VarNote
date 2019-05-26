package main.java.vanno.process;

import java.util.ArrayList;
import java.util.List;

import main.java.vanno.bean.node.Node;

public final class TabixResultProcess implements ProcessResult{

	private List<String> result;
	
	public TabixResultProcess() {
		super();
	}

	@Override
	public void doProcess(Node d) {
		result.add(d.origStr);
	}

	@Override
	public List<String> getResult() {
		return result;
	}

	@Override
	public void initResult() {
		result = null;
		result = new ArrayList<String>(3);
	}

	@Override
	public int getResultSize() {
		return result.size();
	}
}
