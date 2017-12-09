package process;

import java.util.List;
import bean.node.NodePlus;


public interface ProcessBlock {
	public void doProcess(List<NodePlus> results);
}
