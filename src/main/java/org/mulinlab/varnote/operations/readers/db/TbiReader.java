package org.mulinlab.varnote.operations.readers.db;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import org.mulinlab.varnote.config.param.DBParam;
import org.mulinlab.varnote.constants.GlobalParameter;
import org.mulinlab.varnote.operations.decode.LocCodec;
import org.mulinlab.varnote.utils.database.DatabaseFactory;
import org.mulinlab.varnote.utils.enumset.IntersectType;
import htsjdk.tribble.util.TabixUtils.TIndex;
import org.mulinlab.varnote.utils.database.Database;
import org.mulinlab.varnote.utils.node.LocFeature;
import org.mulinlab.varnote.exceptions.InvalidArgumentException;
import org.mulinlab.varnote.utils.VannoUtils;
import org.mulinlab.varnote.operations.process.TabixResultProcess;
import org.mulinlab.varnote.operations.stack.ExactStack;
import org.mulinlab.varnote.operations.stack.IntervalStack;
import org.mulinlab.varnote.operations.stack.IntervalStackFC;
import org.mulinlab.varnote.utils.database.index.TbiIndex;

public abstract class TbiReader extends AbstractDBReader {

	protected TIndex[] idxArr;
	protected LocCodec codec;

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
		codec = db.getLocCodec().clone();
		codec.setFull(false);

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
		protected LocFeature node;
		
		public TbiIteratorImpl(final int tid) {
			iseof = false;
			this.tid = tid;
	    }

		@Override
		public String next() throws IOException {
			return null;
		}

		@Override
		public LocFeature nextNode() throws IOException {
			if (iseof) return null;
			
			final String s = this.next();	
	
			if(s == null) {
				iseof = true;
				return null;
			} else {
				node = codec.decode(s);
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
