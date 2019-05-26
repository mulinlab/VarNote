package main.java.vanno.bean.config.run;


import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.channels.WritableByteChannel;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import htsjdk.samtools.util.BlockCompressedOutputStream;
import htsjdk.tribble.util.LittleEndianOutputStream;
import main.java.vanno.bean.database.DatabaseConfig;
import main.java.vanno.bean.database.Database.IndexType;
import main.java.vanno.bean.node.Node;
import main.java.vanno.bean.query.Log;
import main.java.vanno.bean.query.Output;
import main.java.vanno.bean.query.Query;
import main.java.vanno.constants.InvalidArgumentException;
import main.java.vanno.constants.VannoUtils;
import main.java.vanno.constants.VannoUtils.PROGRAM;

public class OverlapRunConfig extends AbstractConfig{
	public enum Mode {
		TABIX(0),
		MIX(1),
		SWEEP(2);
		
		private final int num;
		Mode(final int num) {
	        this.num = num;
	    }
		public int getNum() {
			return num;
		}
	}
	
	protected Query query;
	protected Mode mode;
	protected int thread = 1;
	protected boolean isCount;
	protected Output output;
	protected boolean isGzip;
	
	public OverlapRunConfig() {
	}
	
	public OverlapRunConfig(final String queryPath, final String[] dbPaths) {
		this(queryPath, dbPaths, deafultOutput, defaultLog, deafultIsGzip);
	}
	
	public OverlapRunConfig(final String queryPath, final String[] dbPaths, final Log log, final boolean isGzip) {
		this(queryPath, dbPaths, deafultOutput, log, isGzip);
	}
	
	public OverlapRunConfig(final String queryPath, final String[] dbPaths, final Output output, final Log log, final boolean isGzip) {
		this(null, null,deafultMode, deafultThread, deafultIsCount, defaultUseJDK, output, log, isGzip);
		List<DatabaseConfig> dbConfigs = new ArrayList<DatabaseConfig>();
		for (String dbPath : dbPaths) {
			dbConfigs.add(new DatabaseConfig(dbPath));
		}
		this.query = new Query(queryPath);
		this.dbConfigs = dbConfigs;
	}
	
	public OverlapRunConfig(final Query query, final List<DatabaseConfig> dbConfigs) {
		this(query, dbConfigs, deafultOutput, defaultLog, deafultIsGzip);
	}
	
	public OverlapRunConfig(final Query query, final List<DatabaseConfig> dbConfigs, final Log log, final boolean isGzip) {
		this(query, dbConfigs, deafultOutput, log, isGzip);
	}
	
	public OverlapRunConfig(final Query query, final List<DatabaseConfig> dbConfigs, final Output output, final Log log, final boolean isGzip) {
		this(query, dbConfigs, deafultMode, deafultThread, deafultIsCount, defaultUseJDK, output, log, isGzip);
	}
	
	public OverlapRunConfig(final Query query, final List<DatabaseConfig> dbConfigs, final Mode mode, final int thread
			, final boolean isCount, final boolean useJDK, final Output output, final Log log, final boolean isGzip) {
		super(dbConfigs, useJDK, log);
		this.query = query;
		this.mode = mode;
		this.thread = thread;
		this.isCount = isCount;
		this.output = output;
		this.program = PROGRAM.OVERLAP;
		this.isGzip = isGzip;
		this.log = log;
	}

	
	@Override
	protected void initQuery() {
		if(query == null) throw new InvalidArgumentException("Query file path is required.");
		query.getQueryFormat().checkOverlap(query.getQueryPath(), query.getFileType());
		
		if(log.isLog()) {
			try {
				log.setLogOut(new PrintStream(new BufferedOutputStream(new FileOutputStream(query.getQueryPath() + ".log")), true, "utf-8"));
			} catch (Exception e) {
				e.printStackTrace();
			}  
		} 
		
		query.loadSpiderWithThread(thread);
		checkThreadNum();
		
		query.printLog(log);
	}

	@Override
	protected void initDB() {
		if(isCount) {
			mode = Mode.MIX;
		} 
		
		for (int i = 0; i < dbConfigs.size(); i++) {
			if(isCount) {
				dbConfigs.get(i).setIndexType(IndexType.VANNO);
				dbConfigs.get(i).setCount(true);
			} else if(mode == Mode.TABIX) {
				dbConfigs.get(i).setIndexType(IndexType.TBI);
			}
		}
	}

	@Override
	protected void initOther() {
		if(thread == -1) setAutoThread();
		
		if(!isCount && output == null) {
			output = new Output(null, dbConfigs, query);
		}
		output.init(log, program, isGzip);
		
		log.printStrWhite("\n\n----------------------------------------------------  OTHER  SETTING  ------------------------------------------------------");
		log.printKVKCYN("Thread number", thread + "");
		log.printKVKCYN("Is Count Mode", isCount + "");
	}
	
	public void setAutoThread() {
		thread = Runtime.getRuntime().availableProcessors();
		if(thread < 1) thread = 1;
	}

	public void mergeResult() {
		if(!isCount && output != null) {
			try {
				if(isGzip) {
					LittleEndianOutputStream out = new LittleEndianOutputStream(new BlockCompressedOutputStream(new File(output.getOverlapOutput())));
					output.printOverlapHeader(out, databses);
					output.mergeTempZipFile(out, log);
					out.close();
				} else {
					WritableByteChannel outChannel = output.initChannels(output.getOverlapOutput());
					output.printOverlapHeader(outChannel, databses);
					output.mergeTempFile(outChannel, log);
					outChannel.close();
				}
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	public void printRecord(final Node node, final Map<String, List<String>> results, final int index) throws IOException {
		output.print(node, results, index);	
	}

	public boolean isCount() {
		return isCount;
	}
	
	public void setCount(String isCount) {
		this.isCount = VannoUtils.strToBool(isCount);
	}
	
	public Mode getMode() {
		return mode;
	}

	public void setMode(final Mode mode) {
		this.mode = mode;
	}
	
	public void setMode(final int mode) {
		this.mode = VannoUtils.checkMode(mode);
	}

	public int getThread() {
		return thread;
	}

	public void setThread(int thread) {
		if(thread < 1)  {
			if(thread == -1) setAutoThread();
			else throw new InvalidArgumentException("Thread should be -1(automatically get thread number by available processors) or a number greater than zero, but we get " + thread);
		}
		this.thread = thread;
	}
	
	public void checkThreadNum() {
		if(query.getSpiderSize() != thread) {
			thread = query.getSpiderSize();
		}
	}
	
	public Query getQuery() {
		return query;
	}

	public Output getOutput() {
		return output;
	}
	
	public void setQuery(Query query) {
		this.query = query;
	}

	public void setOutput(Output output) {
		this.output = output;
	}

	public void setCount(boolean isCount) {
		this.isCount = isCount;
	}

	public boolean isGzip() {
		return isGzip;
	}

	public void setGzip(boolean isGzip) {
		this.isGzip = isGzip;
	}
	
	
}
