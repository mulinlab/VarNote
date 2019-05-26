package main.java.vanno.readers;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import htsjdk.tribble.util.TabixUtils.TIndex;
import main.java.vanno.bean.database.Database;
import main.java.vanno.bean.database.DatabaseConfig.IntersectType;
import main.java.vanno.bean.node.Node;
import main.java.vanno.bean.node.NodeFactory;
import main.java.vanno.bean.query.Log;
import main.java.vanno.constants.InvalidArgumentException;
import main.java.vanno.constants.VannoUtils;
import main.java.vanno.process.TabixResultProcess;
import main.java.vanno.stack.ExactStack;
import main.java.vanno.stack.IntervalStack;
import main.java.vanno.stack.IntervalStackFC;
import main.java.vanno.bean.database.index.TbiIndex;

public abstract class TbiReader extends AbstractReader {

	protected TIndex[] idxArr;
	
	
	protected TbiReader(final Database db, final boolean useJDK, final Log log) throws IOException {	
		super(db, useJDK, log);
		
		if(db.getConfig().getIntersect() == IntersectType.OVERLAP) {
			stack = new IntervalStack(new TabixResultProcess()); 
		} else if(db.getConfig().getIntersect() == IntersectType.EXACT) {
			stack = new ExactStack(new TabixResultProcess()); 
		} else if(db.getConfig().getIntersect() == IntersectType.FULLCLOASE) {
			stack = new IntervalStackFC(new TabixResultProcess());
		} else {
			throw new InvalidArgumentException(VannoUtils.INTERSECT_ERROR);
		}
		idxArr = ((TbiIndex)idx).getmIndex();
	}
	
	
	protected class TbiIteratorImpl implements Iterator {
		protected int tid;
		protected boolean iseof;
		protected Node node;
		protected ByteArrayOutputStream bufStream;
		protected int max;
		
		public TbiIteratorImpl(final int tid) {
		    	iseof = false;
		    	this.tid = tid;
		    	node = new Node();
		    	max = 8192;
			bufStream = new ByteArrayOutputStream(this.max);
	    }

		@Override
		public String next() throws IOException {
			return null;
		}

		@Override
		public Node nextNode() throws IOException {
			if (iseof) return null;
			
			final String s = this.next();	
	
			if(s == null) {
				iseof = true;
				return null;
			} else {
				if(s.length() > this.max) {
					this.max = s.length() + 500;
					this.bufStream = new ByteArrayOutputStream(this.max);
				}
//				System.out.println(s);
				node = NodeFactory.createBasic(s, idx.getFormat(), node, bufStream);
				node.origStr = s;
				if(chr2tid(node.chr) != tid) {
					iseof = true;
					return null;
				}
				return node;
			}
		}
	}
}
