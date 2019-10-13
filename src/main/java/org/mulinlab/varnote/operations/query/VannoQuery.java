package org.mulinlab.varnote.operations.query;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.mulinlab.varnote.utils.enumset.IndexType;
import org.mulinlab.varnote.utils.database.Database;

import org.mulinlab.varnote.operations.readers.db.AbstractDBReader;
import org.mulinlab.varnote.operations.readers.db.TbiMixReader;
import org.mulinlab.varnote.operations.readers.db.VannoMixReader;


public final class VannoQuery extends AbstractQuery {



	public VannoQuery(final List<Database> dbs) {
		super(dbs, false);
	}
	
	public VannoQuery(final List<Database> dbs, final boolean isCount) {
		super(dbs, isCount);
	}

	public void init() throws IOException {
		super.init();
		readers = new ArrayList<AbstractDBReader>(dbs.size());

		for (Database dbf : dbs) {
			if(dbf.getConfig().getIndexType() == IndexType.TBI) {
				readers.add(new TbiMixReader(dbf, isCount));
			} else {
				readers.add(new VannoMixReader(dbf, isCount));
			}
		}
	}
}
