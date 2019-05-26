package main.java.vanno.bean.database;

import java.io.File;

import htsjdk.samtools.seekablestream.SeekableStreamFactory;
import htsjdk.samtools.util.IOUtil;
import htsjdk.tribble.util.TabixUtils;
import main.java.vanno.bean.database.index.Index;
import main.java.vanno.bean.database.index.IndexFactory;
import main.java.vanno.bean.format.Format;

public abstract class Database {
	public enum IndexType {
		TBI("tbi", TabixUtils.STANDARD_INDEX_EXTENSION, TabixUtils.STANDARD_INDEX_EXTENSION),
		VANNO("vanno", ".vanno", ".vanno.vi");
		
		private final String name;
		private final String ext;
		private final String extIndex;
		IndexType(final String name, final String ext, final String extIndex) {
	        this.name = name;
	        this.ext = ext;
	        this.extIndex = extIndex;
	    }
		public String getName() {
			return name;
		}
		public String getExt() {
			return ext;
		}
		public String getExtIndex() {
			return extIndex;
		}
	}
	
	protected final File db;
	protected String dbIndexPath;
	protected final String dbName;
	protected Index index;
	protected DatabaseConfig config;
	
	public Database(final DatabaseConfig config) {
		super();
		this.config = config;
		if(SeekableStreamFactory.isFilePath(this.config.getDbPath())) 
			this.config.setDbPath(new File(this.config.getDbPath()).getAbsolutePath());
			
		IOUtil.assertInputIsValid(this.config.getDbPath());
		this.db = new File(this.config.getDbPath());
		
		dbName = this.db.getName();
		if(this.config.getOutName() == null) this.config.setOutName(dbName);
	}
	
	public void readIndex(boolean useJDK) {
		index = IndexFactory.readIndex(dbIndexPath, useJDK);
	}
	
	protected abstract void checkIndexFile();
	protected abstract String indexExtension();	
	protected abstract boolean isIndexExsit();	

	public Format getFormat() {
		return index.getFormat();
	}

	public File getDb() {
		return db;
	}
	
	public String getDbPath() {
		return config.getDbPath();
	}


	public DatabaseConfig getConfig() {
		return config;
	}

	public Index getIndex() {
		return index;
	}

	public String getDbIndexPath() {
		return dbIndexPath;
	}
}
