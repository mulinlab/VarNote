package org.mulinlab.varnote.utils.database;

import org.mulinlab.varnote.config.param.DBParam;
import org.mulinlab.varnote.exceptions.InvalidArgumentException;
import org.mulinlab.varnote.utils.VannoUtils;
import org.mulinlab.varnote.utils.enumset.IndexType;

public final class VannoDatabase extends Database{
	private String vannoFile;
	
	public VannoDatabase(final DBParam config) {
		super(config);
		config.setIndexType(IndexType.VARNOTE);
		
		this.dbIndexPath = this.config.getDbPath() + indexExtension();
		this.vannoFile = this.config.getDbPath() + IndexType.VARNOTE.getExt();
	}

	@Override
	protected boolean isIndexExsit() {
		if(VannoUtils.isExist(this.dbIndexPath) && VannoUtils.isExist(this.vannoFile))  return true;
		else return false;	
	}

	public VannoDatabase clone() {
		return (VannoDatabase)setParam(new VannoDatabase(this.config));
	}

	@Override
	protected void checkIndexFile() {
		if(!VannoUtils.isExist(this.vannoFile)) {
			throw new InvalidArgumentException("Vanno file: "+ this.vannoFile + " cannot be found, vanno file and vanno index file should be in the same folder!");
		} else if(!VannoUtils.isExist(this.dbIndexPath)) {
			throw new InvalidArgumentException("Cannot find vanno index file: " + this.dbIndexPath + " !");
		}
	}

	
	@Override
	protected String indexExtension() {
		return IndexType.VARNOTE.getExtIndex();
	}

	public String getVannoFile() {
		return vannoFile;
	}
}
