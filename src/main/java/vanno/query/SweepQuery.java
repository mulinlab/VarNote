package main.java.vanno.query;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import main.java.vanno.bean.config.run.AbstractConfig;
import main.java.vanno.bean.database.Database;
import main.java.vanno.bean.database.Database.IndexType;
import main.java.vanno.bean.query.Log;
import main.java.vanno.readers.AbstractReader;
import main.java.vanno.readers.TbiSweepReader;
import main.java.vanno.readers.VannoSweepReader;

public final class SweepQuery extends AbstractQuery{

	public SweepQuery(final AbstractConfig config) {
		super(config);
	}
	
	public SweepQuery(final List<Database> dbs) {
		super(dbs);
	}
	
	public SweepQuery(final List<Database> dbs, final boolean useJDK, final Log log) {
		super(dbs, useJDK, log);
	}
	
	public void init() throws IOException {
		super.init();
		readers = new ArrayList<AbstractReader>(dbs.size());

		for (Database dbf : dbs) {
			if(dbf.getConfig().getIndexType() == IndexType.TBI) {
				readers.add(new TbiSweepReader(dbf, useJDK, log));
			} else {
				readers.add(new VannoSweepReader(dbf, useJDK, log));
			}
		}
	}
	
}
