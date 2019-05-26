package main.java.vanno.bean.database;

import main.java.vanno.bean.database.index.vannoIndex.VannoIndex;
import main.java.vanno.constants.InvalidArgumentException;
import main.java.vanno.constants.VannoUtils;

public final class VannoDatabase extends Database{
	private VannoIndex idx;
	private String vannoFile;
	
	public VannoDatabase(final DatabaseConfig config) {
		super(config);
		config.setIndexType(IndexType.VANNO);
		
		this.dbIndexPath = this.config.getDbPath() + indexExtension();
		this.vannoFile = this.config.getDbPath() + IndexType.VANNO.getExt();
	}

	@Override
	protected boolean isIndexExsit() {
		if(VannoUtils.isExist(this.dbIndexPath) && VannoUtils.isExist(this.vannoFile))  return true;
		else return false;	
	}
	
	@Override
	protected void checkIndexFile() {
		if(!VannoUtils.isExist(this.vannoFile)) {
			throw new InvalidArgumentException("We cannot find vanno file: "+ this.vannoFile + ", you must put vanno file and vanno index file in the same folder!");
		} else if(!VannoUtils.isExist(this.dbIndexPath)) {
			throw new InvalidArgumentException("Cannot find vanno index file: " + this.dbIndexPath + " !");
		}
	}

	
	@Override
	protected String indexExtension() {
		return IndexType.VANNO.getExtIndex();
	}

	public VannoIndex getIdx() {
		return idx;
	}

	public String getVannoFile() {
		return vannoFile;
	}
}
