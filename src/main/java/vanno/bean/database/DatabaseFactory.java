package main.java.vanno.bean.database;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import htsjdk.samtools.seekablestream.SeekableStreamFactory;
import main.java.vanno.bean.database.Database.IndexType;
import main.java.vanno.bean.database.DatabaseConfig.IntersectType;
import main.java.vanno.bean.query.Log;
import main.java.vanno.constants.BasicUtils;
import main.java.vanno.constants.IOutils;
import main.java.vanno.constants.InvalidArgumentException;

public final class DatabaseFactory {
	private final static boolean DEFAULT_IS_COUNT = BasicUtils.DEFAULT_IS_COUNT;
	private final static IntersectType DEFAULT_INTERSECT = BasicUtils.DEFAULT_INTERSECT;
	
//	public static List<Database> readDatabaseFromConfig(final List<DatabaseConfig> dbConfigs) {
//		return readDatabaseFromConfig(dbConfigs, false, false, false, null);
//	}
	
	public static List<Database> readDatabaseFromConfig(final List<DatabaseConfig> dbConfigs, final boolean useJDK, final Log log) {
		List<Database> dbs = new ArrayList<Database>();
		
		for (DatabaseConfig config : dbConfigs) {
			if(!SeekableStreamFactory.isFilePath(config.getDbPath()) || config.getDbPath().indexOf("*") == -1) {
				dbs.add(DatabaseFactory.readDatabase(config, useJDK));
		    	} else { 
		    		// add database for pattern: "/Users/hdd/Desktop/*.gz" default setting: indexType: auto, exact: false, fc:false
		    		final List<String> files = IOutils.readFileWithPattern(config.getDbPath());
		    		for (int j = 0; j < files.size(); j++) {
		    			dbs.add(DatabaseFactory.readDatabase(new DatabaseConfig(files.get(j), null, DEFAULT_INTERSECT, null, DEFAULT_IS_COUNT), useJDK));
				}
			}
			printLog(config, log);
		}
	 	return dbs;
	}
	
	public static void printLog(final DatabaseConfig config, final  Log log) {
		log.printStrWhite("\n\n----------------------------------------------------  DATABASE  ------------------------------------------------------------");
		log.printKVKCYN("Reading database file", log.isLog() ? new File(config.getDbPath()).getName() : config.getDbPath());
		log.printKVKCYN("Reading index file", config.getIndexType() + " (Count program only support vanno index)");
		log.printKVKCYN("Intersection", config.getIntersectStr());
	}
	
	public static Database readDatabase(final DatabaseConfig config) {
		return readDatabase(config, false);
	}
	
	public static Database readDatabase(final DatabaseConfig config, final boolean useJDK) {
		Database db = null;

		if(config.getIndexType() == null) {
			db = new VannoDatabase(config);
			if(!db.isIndexExsit()) {
				db = new TbiDatabase(config);
			} 
			
			if(!db.isIndexExsit())
				throw new InvalidArgumentException("We support two types of index file. The first is the tabix index file, the second type needs vanno file and vanno index file both. Please put the index in the same folder with the database file!");
		} else if(config.getIndexType() == IndexType.VANNO) {
			db = new VannoDatabase(config);

		} else if(config.getIndexType() == IndexType.TBI) {
			db = new TbiDatabase(config);
			
		} 

		db.checkIndexFile();
		db.readIndex(useJDK);
		return db;
	}
}
