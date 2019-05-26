package main.java.vanno.bean.database;

import main.java.vanno.bean.database.Database.IndexType;
import main.java.vanno.constants.BasicUtils;
import main.java.vanno.constants.InvalidArgumentException;
import main.java.vanno.constants.VannoUtils;

public final class DatabaseConfig {
	private final static boolean DEFAULT_IS_COUNT = BasicUtils.DEFAULT_IS_COUNT;
	private final static IntersectType DEFAULT_INTERSECT = BasicUtils.DEFAULT_INTERSECT;
	
	public enum IntersectType {
		OVERLAP("Overlap", 0),
		EXACT("Exact Match", 1), 
		FULLCLOASE("Full Close", 2);
		
		private final String name;
		private final int val;

		IntersectType(final String name, final int val) {
	        this.name = name;
	        this.val = val;
	    }
		public String getName() {
			return name;
		}
		public int getVal() {
			return val;
		}
	}
	
	private String dbPath;
	private String outName;
	private IntersectType intersect;
	private IndexType indexType;
	private boolean count;
	
	public DatabaseConfig(String dbPath) {
		this(dbPath, null, DEFAULT_INTERSECT, null, DEFAULT_IS_COUNT);
	}
	
	public DatabaseConfig(String dbPath, IndexType indexType) {
		this(dbPath, null, DEFAULT_INTERSECT, indexType, DEFAULT_IS_COUNT);
	}
	
	public DatabaseConfig(final String dbPath, final String outName, final String intersect, final String indexType) {
		this(dbPath, outName, VannoUtils.checkIntersectType(intersect), VannoUtils.checkIndexType(indexType), DEFAULT_IS_COUNT);
	}
	
	public DatabaseConfig(final String dbPath, final String outName, final IntersectType intersect, final IndexType indexType, final boolean count) {
		super();
		if(dbPath == null) throw new InvalidArgumentException("Please set up database path.");
		this.dbPath = dbPath;
		this.outName = outName;
		this.intersect = intersect;
		this.indexType = indexType;
		this.count = count;
	}

	public String getOutName() {
		return outName;
	}

	public void setOutName(String outName) {
		this.outName = outName;
	}

	public IndexType getIndexType() {
		return indexType;
	}

	public void setIndexType(IndexType indexType) {
		this.indexType = indexType;
	}

	public String getDbPath() {
		return dbPath;
	}

	public void setDbPath(String dbPath) {
		this.dbPath = dbPath;
	}

	public boolean isCount() {
		return count;
	}

	public void setCount(boolean count) {
		this.count = count;
	}

	public IntersectType getIntersect() {
		return intersect;
	}
	
	public String getIntersectStr() {
		return intersect.getName();
	}

	public void setIntersect(IntersectType intersect) {
		this.intersect = intersect;
	}
}
