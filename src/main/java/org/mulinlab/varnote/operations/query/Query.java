package org.mulinlab.varnote.operations.query;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.mulinlab.varnote.utils.node.LocFeature;

public interface Query {
	public void doQuery(LocFeature node) throws IOException;
	public Map<String, String[]> getResults();
	public Map<String, LocFeature[]> getResultFeatures();
	public long getResultCount();
}
