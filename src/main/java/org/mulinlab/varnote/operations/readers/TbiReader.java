package org.mulinlab.varnote.operations.readers;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import org.mulinlab.varnote.config.param.DBParam;
import org.mulinlab.varnote.constants.GlobalParameter;
import org.mulinlab.varnote.utils.database.DatabaseFactory;
import org.mulinlab.varnote.utils.enumset.IntersectType;
import htsjdk.tribble.util.TabixUtils.TIndex;
import org.mulinlab.varnote.utils.database.Database;
import org.mulinlab.varnote.utils.node.Node;
import org.mulinlab.varnote.utils.node.NodeFactory;
import org.mulinlab.varnote.exceptions.InvalidArgumentException;
import org.mulinlab.varnote.utils.VannoUtils;
import org.mulinlab.varnote.operations.process.TabixResultProcess;
import org.mulinlab.varnote.operations.stack.ExactStack;
import org.mulinlab.varnote.operations.stack.IntervalStack;
import org.mulinlab.varnote.operations.stack.IntervalStackFC;
import org.mulinlab.varnote.utils.database.index.TbiIndex;

public abstract class TbiReader extends AbstractReader {

	protected TIndex[] idxArr;

	protected TbiReader(final Database db, final boolean isCount) throws IOException {
		super(db, isCount);

		if(db.getConfig().getIntersect() == IntersectType.INTERSECT) {
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

	protected TbiReader(final Database db) throws IOException {
		this(db, GlobalParameter.DEFAULT_IS_COUNT);
	}

	protected TbiReader(final String db) throws IOException {
		this(DatabaseFactory.readDatabase(new DBParam(db)));
	}

	protected TbiReader(final String db, final boolean isCount) throws IOException {
		this(DatabaseFactory.readDatabase(new DBParam(db)), isCount);
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
