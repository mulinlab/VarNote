package org.mulinlab.varnote.utils.queryreader;


import java.io.ByteArrayOutputStream;
import java.io.IOException;
import org.mulinlab.varnote.utils.format.Format;
import org.mulinlab.varnote.utils.node.Node;
import org.mulinlab.varnote.utils.node.NodeFactory;
import org.mulinlab.varnote.constants.GlobalParameter;
import org.mulinlab.varnote.utils.queryreader.reader.QueryReader;


public class ThreadLineReader {
	public final static int MAX_HEADER_COMPARE_LENGTH = GlobalParameter.MAX_HEADER_COMPARE_LENGTH;
	
	protected final QueryReader reader;
	protected Format format; 
	protected int index;
	protected ByteArrayOutputStream bufStream;
	protected int max;
	protected int thread;
	public static boolean readHeader = false;
	
	public ThreadLineReader(final QueryReader reader, final Format format, final int thread) {
		this.reader = reader;
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
		return reader.readLine();
	}
	
	public void close() throws IOException {
		reader.closeReader();
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
