package main.java.vanno.bean.query;


import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import main.java.vanno.bean.format.Format;
import main.java.vanno.bean.node.Node;
import main.java.vanno.bean.node.NodeFactory;
import main.java.vanno.constants.BasicUtils;
import main.java.vanno.stream.BZIP2InputStream.Spider;
import main.java.vanno.stream.GZInputStream.GZReader;

public class LineReaderBasic {
	public final static int MAX_HEADER_COMPARE_LENGTH = BasicUtils.MAX_HEADER_COMPARE_LENGTH;
	
	protected final Spider spider;
	protected final BufferedReader reader;
	protected final GZReader gzReader;
	
	protected Format format; 
	protected int index;
	protected ByteArrayOutputStream bufStream;
	protected int max;
	protected int thread;
	protected static boolean readHeader = false; 
	
	public LineReaderBasic(final Spider spider, final Format format, final int thread) {
		this.spider = spider;
		this.reader = null;
		this.gzReader = null;
		
		init(format, thread);
	}
	
	public LineReaderBasic(final GZReader gzReader, final Format format, final int thread) {
		this.reader = null;
		this.spider = null;
		this.gzReader = gzReader;
		
		init(format, thread);
	}
	
	public LineReaderBasic(final BufferedReader reader, final Format format, final int thread) {
		this.reader = reader;
		this.spider = null;
		this.gzReader = null;
		
		init(format, thread);
	}
	
	public void init(final Format format, final int thread) {
		this.format = format;
		this.index = 0;
		this.max = 8192;
		this.bufStream = new ByteArrayOutputStream(this.max);
		this.thread = thread;
		
		if(!this.format.isHasHeaderInFile()) readHeader = true;
	}
	
	public Node createNode(final String s, Node intv) {
		if(s.length() > this.max) {
			this.max = s.length() + 500;
			this.bufStream = new ByteArrayOutputStream(this.max);
		}
		Node node = NodeFactory.createBasic(s, format, intv, this.bufStream);
		node.origStr = s;
		node.index = index++;
		return node;
	}
	
	public Node processNode(Node node) {
		return node;
	}
	
	public String readLine() throws Exception {
		if(this.reader != null) {
			return this.reader.readLine();
		} else if(this.gzReader != null) {
			return this.gzReader.readLine();
		} else {
			byte[] bytTemp = this.spider.readLine();
			if(bytTemp != null) {
				return new String(bytTemp); 
			} else return null;
		}
	}
	
	public void close() throws IOException {
		if(this.reader != null) {
			this.reader.close();
		} else if(this.gzReader != null) {
			this.gzReader.close();
		} else {
			this.spider.closeInputStream();
		}
	}
	
	public Node nextNode(Node intv) throws Exception {
		String s;
		
		while((s = readLine()) != null) {
			s = s.trim();
//			System.out.println("s=" + s);
			if(s.startsWith(format.getCommentIndicator()) || s.equals("")) {
				
			} else if((!readHeader) && ( s.startsWith(Format.VCF_HEADER_INDICATOR))) {
				readHeader = true;
			} else if((!readHeader) && ( s.startsWith(format.getHeaderStart()))) {
				String headerMax = s.substring(0, (s.length() > MAX_HEADER_COMPARE_LENGTH) ? MAX_HEADER_COMPARE_LENGTH : s.length());
				if(headerMax.equals(format.getHeader())) {
					readHeader = true;
				} else {
					return processNode(createNode(s, intv));
				}
			} else {
				return processNode(createNode(s, intv));
			}
		}
		close();
		return null;
	}
}
