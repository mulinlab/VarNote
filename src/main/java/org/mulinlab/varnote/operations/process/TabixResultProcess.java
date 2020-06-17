package org.mulinlab.varnote.operations.process;

import java.util.ArrayList;
import java.util.List;

import org.mulinlab.varnote.utils.node.LocFeature;

public final class TabixResultProcess implements ProcessResult{

	private List<String> result;
	
	public TabixResultProcess() {
		super();
	}

	@Override
	public void doProcess(LocFeature d) {
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
		if(result == null) {
			return 0;
		} else {
			return result.size();
		}
	}
}
