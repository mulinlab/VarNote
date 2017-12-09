package bean.query;


import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;

import postanno.FormatWithRef;
import constants.BasicUtils;
import bean.node.Node;
import bean.node.NodeFactory;
import stream.BZIP2InputStream.Spider;

public class LineReaderBasic {
	protected final Spider spider;
	protected final BufferedReader reader;
	protected final FormatWithRef format;
	protected final BufferedWriter writer; 
	protected int index;
	
	public LineReaderBasic(final Spider spider, final FormatWithRef format, final BufferedWriter writer) {
		this.spider = spider;
		this.format = format;
		this.reader = null;
		this.index = 0;
		this.writer = writer;
	}
	
	public LineReaderBasic(final BufferedReader reader, final FormatWithRef format, final BufferedWriter writer) {
		this.reader = reader;
		this.format = format;
		this.spider = null;
		this.index = 0;
		this.writer = writer;
	}
	
	public Node createNode(String s) {
		Node node = NodeFactory.createBasic(s, format);
		node.index = index++;
		return node;
	}
	
	public Node processNode(Node node) {
		return node;
	}
	
	public Node nextNodeSpider() throws IOException {
		byte[] bytTemp = this.spider.readLine();
		String s;
		while(bytTemp != null) {
			s = new String(bytTemp);
	
			if(!BasicUtils.isHeader(s)) {      //  skip header
				if(writer != null) writer.write(index + "\t" + s + "\n");
				return processNode(createNode(s));
			} 
//			else {
//				writer.write(s + "\n");
//			}
			bytTemp = this.spider.readLine();
		}
		return null;
	}
	
	public Node nextNodeReader() throws IOException {
		String s;
		while((s = reader.readLine()) != null) {
			if(!BasicUtils.isHeader(s)) {      //  skip header
				if(writer != null) writer.write(index + "\t" + s + "\n");
				return  processNode(createNode(s));
			} 
//			else {
//				writer.write(s + "\n");
//			}
		}
		reader.close();
		return null;
	}
}
