package main.java.vanno.bean.database;



import main.java.vanno.bean.database.index.TbiIndex;
import main.java.vanno.constants.InvalidArgumentException;
import main.java.vanno.constants.VannoUtils;

public final class TbiDatabase extends Database{
	
	public TbiDatabase(final DatabaseConfig config) {
		super(config);
		config.setIndexType(IndexType.TBI);
		this.dbIndexPath = this.config.getDbPath() + indexExtension();
	}

	@Override
	protected boolean isIndexExsit() {
		if(VannoUtils.isExist(this.dbIndexPath))  return true;
		else return false;	
	}

	@Override
	protected String indexExtension() {
		return IndexType.TBI.getExtIndex();
	}

	public TbiIndex getIdx() {
		return (TbiIndex)index;
	}

	@Override
	protected void checkIndexFile() {
		if(!isIndexExsit()) throw new InvalidArgumentException("Cannot find tabix index file: " + this.dbIndexPath + " !");
	}
}
