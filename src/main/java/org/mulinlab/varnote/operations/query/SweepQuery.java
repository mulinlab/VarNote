package org.mulinlab.varnote.operations.query;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.mulinlab.varnote.utils.enumset.IndexType;
import org.mulinlab.varnote.config.run.RunConfig;
import org.mulinlab.varnote.utils.database.Database;

import org.mulinlab.varnote.operations.readers.AbstractReader;
import org.mulinlab.varnote.operations.readers.TbiSweepReader;
import org.mulinlab.varnote.operations.readers.VannoSweepReader;

public final class SweepQuery extends AbstractQuery{
	
	public SweepQuery(final List<Database> dbs) {
		super(dbs, false);
	}
	
	public void init() throws IOException {
		super.init();
		readers = new ArrayList<AbstractReader>(dbs.size());

		for (Database dbf : dbs) {
			if(dbf.getConfig().getIndexType() == IndexType.TBI) {
				readers.add(new TbiSweepReader(dbf));
			} else {
				readers.add(new VannoSweepReader(dbf));
			}
		}
	}
	
}
