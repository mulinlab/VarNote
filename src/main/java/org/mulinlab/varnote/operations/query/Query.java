package org.mulinlab.varnote.operations.query;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.mulinlab.varnote.utils.enumset.Mode;
import org.mulinlab.varnote.utils.node.Node;

public interface Query {
	public void doQuery(Node node) throws IOException;
	public Map<String, List<String>> getResults();
	public long getResultCount();
}
