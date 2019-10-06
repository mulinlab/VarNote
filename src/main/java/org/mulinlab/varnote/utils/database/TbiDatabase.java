package org.mulinlab.varnote.utils.database;



import org.mulinlab.varnote.config.param.DBParam;
import org.mulinlab.varnote.utils.database.index.TbiIndex;
import org.mulinlab.varnote.exceptions.InvalidArgumentException;
import org.mulinlab.varnote.utils.VannoUtils;
import org.mulinlab.varnote.utils.enumset.IndexType;

public final class TbiDatabase extends Database{
	
	public TbiDatabase(final DBParam config) {
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
