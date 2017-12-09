package process;

import bean.config.Printter;
import bean.node.Node;

public class TabixResultProcess implements ProcessResult{

	private Printter printter;
	
	public TabixResultProcess(Printter printter) {
		super();
		this.printter = printter;
	}

	@Override
	public void doProcess(Node q, Node d) {
		printter.print(q.coreInfo() + d.origStr);
//		printter.print(d.toString());
//		System.out.println(d.toString());
	}
}
