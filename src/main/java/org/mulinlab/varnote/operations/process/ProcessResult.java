package org.mulinlab.varnote.operations.process;

import java.util.List;

import org.mulinlab.varnote.utils.node.LocFeature;

public interface ProcessResult {
	public void doProcess(LocFeature d);
	public List<String> getResult();
	public int getResultSize();
	public void initResult();
}
