package org.mulinlab.varnote.utils.database;

import java.io.File;
import htsjdk.samtools.seekablestream.SeekableStreamFactory;
import htsjdk.samtools.util.IOUtil;
import org.mulinlab.varnote.config.param.DBParam;
import org.mulinlab.varnote.utils.database.index.Index;
import org.mulinlab.varnote.utils.database.index.IndexFactory;
import org.mulinlab.varnote.utils.format.Format;

public abstract class Database {

	protected final File db;
	protected String dbIndexPath;
	protected final String dbName;
	protected Index index;
	protected DBParam config;
	
	public Database(final DBParam config) {
		super();
		this.config = config;
		if(SeekableStreamFactory.isFilePath(this.config.getDbPath()))
			this.config.setDbPath(new File(this.config.getDbPath()).getAbsolutePath());

		IOUtil.assertInputIsValid(this.config.getDbPath());
		this.db = new File(this.config.getDbPath());

		dbName = this.db.getName();
		if(this.config.getOutName() == null) this.config.setOutName(dbName);
	}
	
	public void readIndex() {
		index = IndexFactory.readIndex(dbIndexPath);
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


	public DBParam getConfig() {
		return config;
	}

	public Index getIndex() {
		return index;
	}

	public String getDbIndexPath() {
		return dbIndexPath;
	}
}
