package org.mulinlab.varnote.utils.database;

import java.io.File;
import htsjdk.variant.vcf.VCFCodec;
import org.mulinlab.varnote.config.anno.databse.HeaderFormatReader;
import org.mulinlab.varnote.config.anno.databse.VCFParser;
import org.mulinlab.varnote.config.param.DBParam;
import org.mulinlab.varnote.operations.decode.LocCodec;
import org.mulinlab.varnote.operations.decode.VCFLocCodec;
import org.mulinlab.varnote.utils.VannoUtils;
import org.mulinlab.varnote.utils.database.index.Index;
import org.mulinlab.varnote.utils.database.index.IndexFactory;
import org.mulinlab.varnote.utils.enumset.FileType;
import org.mulinlab.varnote.utils.enumset.FormatType;
import org.mulinlab.varnote.utils.format.Format;
import org.mulinlab.varnote.utils.node.LocFeature;

public abstract class Database {

	protected final File db;
	protected String dbIndexPath;
	protected final String dbName;
	protected Format format;

	protected Index index;
	protected DBParam config;
	protected LocCodec locCodec;
	protected VCFParser vcfParser;

	public Database(final DBParam config) {
		super();
		this.config = config;
		this.db = new File(this.config.getDbPath());

		dbName = this.db.getName();
		if(this.config.getOutName() == null) this.config.setOutName(dbName);
	}

	public abstract Database clone();

	public Database setParam(Database db) {
		db.index = this.index;
		db.dbIndexPath = dbIndexPath;
		db.format = this.format;

		if(locCodec != null) db.locCodec = locCodec.clone();
		return db;
	}

	public void readIndex() {
		index = IndexFactory.readIndex(dbIndexPath);
		format = index.getFormat();
	}
	
	protected abstract void checkIndexFile();
	protected abstract String indexExtension();	
	protected abstract boolean isIndexExsit();	

	public Format getFormat() {
		if(format == null) {
			format = index.getFormat();
		}
		return format;
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

	public String getOutName() {
		return config.getOutName();
	}

	public Index getIndex() {
		return index;
	}

	public String getDbIndexPath() {
		return dbIndexPath;
	}

	public void setDefaultLocCodec(final boolean isFull) {
		locCodec = VannoUtils.getDefaultLocCodec(getFormat(), isFull);
	}

	public void setVCFLocCodec(final boolean isFull, final VCFCodec vcfCodec) {
		locCodec = new VCFLocCodec(getFormat(), isFull, vcfCodec);
	}

	public LocFeature decode(final String s) {
		if(locCodec == null) {
			getLocCodec();
		}
		return locCodec.decode(s);
	}

	public void setFormat(Format format) {
		this.format = format;
		this.vcfParser = null;
	}

	public VCFParser getVcfParser() {
		if(vcfParser == null) {
			vcfParser = VCFParser.defaultVCFParser(getFormat(), getDbPath(), FileType.BGZ);
		}
		return vcfParser;
	}

	public LocCodec getLocCodec() {
		if(locCodec == null) {
			if(format.type == FormatType.VCF) {
				setVCFLocCodec(true, getVcfParser().getCodec());
			} else {
				setDefaultLocCodec(true);
			}
		}
		return locCodec;
	}

	public void readFormatFromHeader() {
		this.format = HeaderFormatReader.readFormatFromHeader(this.format, getDbPath(), FileType.BGZ);
	}
}
