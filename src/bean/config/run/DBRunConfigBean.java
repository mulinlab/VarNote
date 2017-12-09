package bean.config.run;

import htsjdk.samtools.seekablestream.SeekableStreamFactory;
import htsjdk.samtools.util.IOUtil;
import htsjdk.tribble.util.TabixUtils;
import index.interval.BinIndexWriter;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import postanno.FormatWithRef;
import constants.VannoUtils;
import bean.config.Printter;
import bean.index.Index;
import bean.index.IndexFactory;


public class DBRunConfigBean {
	
	public enum IndexFormat {
		TBI,
		PLUS
	}
	
	private String tempOut;
	private String db;
	private String dbIndex;
	private FormatWithRef dbFormat;
	private Index idx;
	private String plusFile;
	private IndexFormat indexFormat;
	private List<Printter> printters;
	private String lable;
	
	public DBRunConfigBean() {
		super();
		printters = new ArrayList<Printter>();
	}
	
	public void checkDBConfig() {
		if(db == null) {
			throw new IllegalArgumentException("DB path is required.");
		}

		if(dbIndex == null) {
			setDbIndex();
		}

	    if(lable == null) lable = getDb().getName();
	}
	
	public File getDb() {
		return new File(db);
	}

	public String getDbPath() {
		return db;
	}
	
	public void setDb(String dbPath) {
		IOUtil.assertInputIsValid(dbPath);
		this.db = dbPath;
	}
	
	public void addPrintter(int index) {
		if(tempOut == null) {
			throw new IllegalArgumentException("Please set up temp output path for db " + tempOut);
		}
		this.printters.add(new Printter(tempOut, index));
	}
	
	public void printResults() {
		for (Printter printter : printters) {
			printter.tearDownPrintter();
		}
//		WritableByteChannel outChannel = null;
//		ReadableByteChannel inChannel = null;
//		ByteBuffer byteBuffer = ByteBuffer.allocate(1024);
//		try {
//			outChannel = Channels.newChannel(new FileOutputStream(dbOut));
//			for (int i = 0; i < printters.size(); i++) {
//				printters.get(i).tearDownPrintter();
//				File tempFile = printters.get(i).getFile();
//				if (tempFile != null) {
//					inChannel = Channels.newChannel(new FileInputStream(
//							tempFile));
//
//					while (inChannel.read(byteBuffer) > 0) {
//						byteBuffer.flip();
//						outChannel.write(byteBuffer);
//						byteBuffer.clear();
//					}
//					// outChannel.write(ByteBuffer.wrap(new
//					// String("\n").getBytes()));
//					inChannel.close();
//					if (tempFile.delete()) {
//
//					} else {
//						System.err.println("Delete " + tempFile.getAbsolutePath() + " is failed.");
//					}
//				}
//			}
//			outChannel.close();
//
//		} catch (IOException e) {
//			System.err.println("create output file with error");
//			e.printStackTrace();
//		}
	}
	
	public String getDbIndex() {
		return dbIndex;
	}

	public void setIndexFormat(String indexType) {
		this.indexFormat = VannoUtils.checkIndexType(indexType);
	}
	
	public void setIndexFormat(IndexFormat indexType) {
		this.indexFormat = indexType;
	}
	
	public static boolean isExist(String path) {
		if(!SeekableStreamFactory.isFilePath(path)) {
			return exists(path);
		} else {
			return new File(path).exists();
		}
	}
	
	public static boolean exists(String urlName) {
	    try {
	        new URL(urlName).openStream().close();
	        return true;
	    } catch (IOException e) {
	        return false;
	    }
	}
	
	public void checkPlus() {
		this.dbIndex = this.db + BinIndexWriter.PLUS_INDEX_EXTENSION;
		if(!isExist(this.dbIndex)) {
			throw new IllegalArgumentException("Cannot find plus index file: " + this.dbIndex + " !");
		}
		this.plusFile = this.db + BinIndexWriter.PLUS_EXTENSION;
		if(!isExist(this.plusFile)) {
			throw new IllegalArgumentException("We cannot find plus file: "+ this.plusFile + ", you must put plus file and plus index file in the same folder!");
		}
	}
	
	public void checkTbi() {
		this.dbIndex = this.db + TabixUtils.STANDARD_INDEX_EXTENSION;
		if(!isExist(this.dbIndex)) {
			throw new IllegalArgumentException("Cannot find tabix index file: " + this.dbIndex + " !");
		}
	}
	
	public void setDbIndex() {
		if(!SeekableStreamFactory.isFilePath(db)) {
			if(this.indexFormat == null) {
				throw new IllegalArgumentException("Please set up index format for remote file.");
			} else if(this.indexFormat == IndexFormat.TBI) {
				this.dbIndex = this.db + TabixUtils.STANDARD_INDEX_EXTENSION;
				if(!isExist(this.dbIndex))
					throw new IllegalArgumentException("Index file: " + this.dbIndex + " is not find.");
			} else {
				this.dbIndex = this.db + BinIndexWriter.PLUS_INDEX_EXTENSION;
				this.plusFile = this.db + BinIndexWriter.PLUS_EXTENSION;
				if(!isExist(this.dbIndex)) throw new IllegalArgumentException("Index file: " + this.dbIndex + " is not find.");
				if(!isExist(this.plusFile)) throw new IllegalArgumentException("Plus file: " + this.plusFile + " is not find.");
			}
		} else {
			if(this.indexFormat == IndexFormat.PLUS) {
				checkPlus();
			} else if(this.indexFormat == IndexFormat.TBI){
				checkTbi();
			} else {
				this.dbIndex = this.db + BinIndexWriter.PLUS_INDEX_EXTENSION;
				if(!isExist(this.dbIndex)) {
					this.dbIndex = this.db + TabixUtils.STANDARD_INDEX_EXTENSION;
					if(!isExist(this.dbIndex)) {
						throw new IllegalArgumentException("We support two types of index file. The first is the tabix index file, the second type needs plus file and plus index file both. Please put the index in the same folder with the database file!");
					} else {
						this.indexFormat = IndexFormat.TBI;
					}
				} else {
					this.plusFile = this.db + BinIndexWriter.PLUS_EXTENSION;
					if(!isExist(this.plusFile)) {
						throw new IllegalArgumentException("We cannot find plus file: "+ this.plusFile + ", you must put plus file and plus index file in the same folder!");
					} else {
						this.indexFormat = IndexFormat.PLUS;
					}
				}
			}
		}
		idx = IndexFactory.readIndex(dbIndex);
	}

	public Printter getPrintter(int i) {
		return printters.get(i);
	}


	public String getPlusFile() {
		return plusFile;
	}

	public IndexFormat getIndexFormat() {
		return indexFormat;
	}

	public void setDbOut(String dbOut) {
		this.tempOut = dbOut + File.separator + new File(db).getName() + "_" + indexFormat ;
	}

	public Index getIdx() {
		return idx;
	}

	public String getLable() {
		return lable;
	}

	public void setLable(String lable) {
		this.lable = lable;
	}

	public FormatWithRef getDbFormat() {
		return idx.getFormatWithRef();
	}

	public void setDbFormat(FormatWithRef dbFormat) {
		this.dbFormat = dbFormat;
	}
}
