package bean.query;


import java.io.BufferedWriter;
import java.util.ArrayList;
import java.util.List;

import postanno.FormatWithRef;
import bean.node.Node;
import stream.BZIP2InputStream.Spider;

public class QueryLineReaderWithValidation extends LineReaderBasic {

	private final List<String> hasSeen;
	private String chr;
	private Node preNode;
	
	public QueryLineReaderWithValidation(final Spider spider, final FormatWithRef format, final BufferedWriter writer) {
		super(spider, format, writer);
		this.hasSeen = new ArrayList<String>();
		this.chr = "";
		this.preNode = new Node(0, 0, "");
	}
	
	public Node processNode(Node node) {
		if(!node.chr.equals(chr)) {
			hasSeen.add(chr);
			this.preNode =  new Node(0, 0, "");
		}
		if(hasSeen.contains(node.chr)) 
			 throw new IllegalArgumentException("Sequence " + node.chr + " added out of sequence order.");
	
		if(node.beg < this.preNode.beg) {
			throw new IllegalArgumentException("Start position doesn't in order! line = \"" + node + "\"" );
		}
//		else if((node.beg == this.preNode.beg) && (node.end < this.preNode.end)) {
//			throw new IllegalArgumentException("End position doesn't in order! line = " + node + "" );
//		}
		
		chr = node.chr;
		preNode = node;
		return node;
	}
}
