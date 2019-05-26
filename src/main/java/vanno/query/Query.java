package main.java.vanno.query;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import main.java.vanno.bean.node.Node;

public interface Query {
	public void doQuery(Node node) throws IOException;
	public Map<String, List<String>> getResults();
	public long getResultCount();
}
