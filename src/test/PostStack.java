package test;

import java.io.IOException;
import java.util.List;

import process.ProcessResult;
import stack.AbstractReaderStack;
import bean.node.Node;
import bean.node.ResultNode;

public class PostStack extends AbstractReaderStack {
	private ResultNode tempNode;
	private String lastChr;
	public PostStack(ProcessResult resultProcessor) {
		super(resultProcessor);
		tempNode = null;
		lastChr = "";
		iseof = false;
	}
	
	public void clearST() {
		tempNode = null;
		lastChr = "";
		iseof = false;
	}
	
	public void updateLastChr(String chr) {
		lastChr = chr;
	}
	
	public void findOverlaps(List<Node> nodes) {
	}

	public boolean nextChr(String chr) throws IOException {
		ResultNode node;
		while((node = (ResultNode)it.nextNode()) != null) {
			if(node.queryNode.chr.equals(chr)) {
				tempNode = node;
				return true;
			}
		}
		return false;
	}
	
	@Override
	public void findOverlap(Node query){
//		System.out.println("query=" + query + ", temp = " + tempNode);
		if(iseof) return;
		try {	
			if(tempNode != null) {
				if(tempNode.queryNode.chr.equals(query.chr)) {
					if(tempNode.queryNode.beg == query.beg) {
						if(tempNode.queryNode.end == query.end) {
							resultProcessor.doProcess(query, tempNode);
							tempNode = null;
						} else if(tempNode.queryNode.end < query.end) {
						} else {
							return;
						}
					} else if(tempNode.queryNode.beg < query.beg) {
					} else {
						return;
					}
				} else if(tempNode.queryNode.chr.equals(lastChr)) {
					nextChr(query.chr);
				} else {
					return;
				}
			}
			
			ResultNode node;
			while((node = (ResultNode)it.nextNode()) != null) {
				if(node.queryNode.chr.equals(query.chr)) {
					if(node.queryNode.beg == query.beg) {
						if(node.queryNode.end == query.end) {
//							System.out.println("hit node=" + tempNode);
							resultProcessor.doProcess(query, node);
						} else if(node.queryNode.end < query.end) {
							continue;
						} else {
							tempNode = node;
							break;
						}
					} else if(node.queryNode.beg < query.beg) {
						continue;
					} else {
						tempNode = node;
						break;
					}
				} else if(node.queryNode.chr.equals(lastChr)) {
					nextChr(query.chr);
				} else {
					tempNode = node;
					break;
				}
			}
			
			if(node == null) iseof = true;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
