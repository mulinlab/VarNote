package org.mulinlab.varnote.operations.query;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.mulinlab.varnote.utils.enumset.IndexType;
import org.mulinlab.varnote.utils.database.Database;

import org.mulinlab.varnote.operations.readers.db.AbstractDBReader;
import org.mulinlab.varnote.operations.readers.db.TbiSweepReader;
import org.mulinlab.varnote.operations.readers.db.VannoSweepReader;

public final class SweepQuery extends AbstractQuery{
	
	public SweepQuery(final List<Database> dbs) {
		super(dbs, false);
	}
	
	public void init() throws IOException {
		super.init();
		readers = new ArrayList<AbstractDBReader>(dbs.size());

		for (Database dbf : dbs) {
			if(dbf.getConfig().getIndexType() == IndexType.TBI) {
				readers.add(new TbiSweepReader(dbf.clone()));
			} else {
				readers.add(new VannoSweepReader(dbf.clone()));
			}
		}
	}
	
}
