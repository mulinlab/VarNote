package org.mulinlab.varnote.utils.database;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.mulinlab.varnote.config.param.DBParam;
import org.mulinlab.varnote.constants.GlobalParameter;
import org.mulinlab.varnote.utils.enumset.IndexType;
import org.mulinlab.varnote.utils.enumset.IntersectType;
import htsjdk.samtools.seekablestream.SeekableStreamFactory;
import org.mulinlab.varnote.utils.IOutils;
import org.mulinlab.varnote.exceptions.InvalidArgumentException;

public final class DatabaseFactory {
	private final static boolean DEFAULT_IS_COUNT = GlobalParameter.DEFAULT_IS_COUNT;
	private final static IntersectType DEFAULT_INTERSECT = GlobalParameter.DEFAULT_INTERSECT;

	
	public static List<Database> readDatabaseFromConfig(final List<DBParam> dbConfigs) {
		List<Database> dbs = new ArrayList<Database>();
		
		for (DBParam config : dbConfigs) {
			if(!SeekableStreamFactory.isFilePath(config.getDbPath()) || config.getDbPath().indexOf("*") == -1) {
				dbs.add(DatabaseFactory.readDatabase(config));
			} else {
				final List<String> files = IOutils.readFileWithPattern(config.getDbPath());
				for (int j = 0; j < files.size(); j++) {
					dbs.add(DatabaseFactory.readDatabase(new DBParam(new File(files.get(j)))));
				}
			}
		}
	 	return dbs;
	}

	public static Database readDatabase(final DBParam config) {
		Database db = null;

		if(config.getIndexType() == null) {
			db = new VannoDatabase(config);
			if(!db.isIndexExsit()) {
				db = new TbiDatabase(config);
			} 
			
			if(!db.isIndexExsit())
				throw new InvalidArgumentException("We support two types of index file. The first is the tabix index file, the second type needs vanno file and vanno index file both. " +
						"Please put the index in the same folder with the database file!");
		} else if(config.getIndexType() == IndexType.VARNOTE) {
			db = new VannoDatabase(config);
		} else if(config.getIndexType() == IndexType.TBI) {
			db = new TbiDatabase(config);
		}

		db.checkIndexFile();
		db.readIndex();
		return db;
	}
}
