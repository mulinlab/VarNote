package main.java.vanno.bean.config.config.run;

import java.util.ArrayList;
import java.util.List;

import main.java.vanno.bean.config.config.AbstractReadConfig;
import main.java.vanno.bean.config.run.OverlapRunConfig;
import main.java.vanno.bean.database.DatabaseConfig;
import main.java.vanno.bean.format.Format;
import main.java.vanno.bean.query.Log;
import main.java.vanno.bean.query.Output;
import main.java.vanno.bean.query.Query;
import main.java.vanno.bean.query.Output.OutMode;
import main.java.vanno.constants.InvalidArgumentException;
import main.java.vanno.constants.VannoUtils;
import main.java.vanno.constants.VannoUtils.PROGRAM;


public class OverlapReadConfig<T> extends AbstractReadConfig<T>{
	protected Format format;
	protected int chrom = Format.DEFAULT_COL, begin = Format.DEFAULT_COL, end = Format.DEFAULT_COL, ref = Format.DEFAULT_COL, alt = Format.DEFAULT_COL;
	protected String commentIndicator = null, headerPath = null;
	protected boolean hasHeader = Format.DEFAULT_HAS_HEADER, zerobased = Format.DEFAULT_ZERO_BASED;
	protected String path = null, indexType = null, outName = null, outputFolder = null, intersectType = null;

	protected List<DatabaseConfig> dbConfig;
	protected OverlapRunConfig runConfig;
	protected boolean removeCommemt = DEFAULT_REMOVE_COMMENT, loj = DEFAULT_LOJ, unzip = !DEFAULT_IS_GZIP;
	protected OutMode outputMode;
	protected Log log = DEFAULT_LOG;
	
	@Override
	public boolean filterLine(String line) {
		if(line.equalsIgnoreCase(DB_BEGIN)) {
			if(path != null) {
				dbConfig.add(new DatabaseConfig(path, outName, intersectType, indexType));
				path = null; indexType = null; intersectType = null; outName = null;
			} 
			return false;
		} else if(!line.startsWith(COMMENT_LINE) && !line.equals("") && !(line.startsWith("[") && line.endsWith("]"))) {
			return true;
		} else {
			return false;
		}
	}
	
	public void setformat(final String path) {
		if(this.format == null) this.format = Format.defaultFormat(path, true);
		if(commentIndicator != null) format.setCommentIndicator(commentIndicator);
		if(headerPath != null) format.setHeaderPath(headerPath);
		if(hasHeader) format.setHasHeader(true);
		if(zerobased) format.setZeroBased();
		if(chrom > Format.DEFAULT_COL) format.setSequenceColumn(chrom);
		if(begin > Format.DEFAULT_COL) format.setStartPositionColumn(begin);
		if(end > Format.DEFAULT_COL) format.setEndPositionColumn(end);
		if(ref > Format.DEFAULT_COL) format.setRefColumn(ref);
		if(alt > Format.DEFAULT_COL) format.setAltColumn(alt);
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public T doEnd() {
		if(path != null) {
			dbConfig.add(new DatabaseConfig(path, outName, intersectType, indexType));
		}
		if(dbConfig.size() == 0) throw new InvalidArgumentException("No Database found, you should define database information start with " + DB_BEGIN + ".");
		Query query = new Query(this.qFile);
		setformat(query.getQueryPath());
		query.setQueryFormat(this.format);
		
		Output output = null;
		if(!runConfig.isCount()) {
			output = new Output(outputFolder, dbConfig, query, loj, removeCommemt, outputMode);
			runConfig.setOutput(output);
		}
		runConfig.setGzip(!unzip);
		runConfig.setLog(log);
		runConfig.setQuery(query);
		runConfig.setDbConfigs(dbConfig);
		runConfig.setProgram(PROGRAM.OVERLAP);
		return (T)runConfig;
	}
	
	@Override
	public void doInit() {
		format = null;
		runConfig = new OverlapRunConfig();
		dbConfig = new ArrayList<DatabaseConfig>();
		removeCommemt = DEFAULT_REMOVE_COMMENT;
		loj = DEFAULT_LOJ;
		unzip = !DEFAULT_IS_GZIP;
		outputMode = DEFALT_OUT_MODE;
		log = DEFAULT_LOG;
	}
	
	@Override
	public void putValue(String key, String val) {
		super.putValue(key, val);

		switch (key) {
			case QUERY_FORMAT:
				format =  VannoUtils.parseIndexFileFormat(val);
				break;
			case USE_JDK_INFLATER:
				runConfig.setUseJDK(val); 
				break;
			case THREAD:
				runConfig.setThread(Integer.parseInt(val));
				break;
			case COUNT:
				runConfig.setCount(val);
				break;
			case MODE:
				runConfig.setMode(Integer.parseInt(val));
				break;
			case LOJ:
				loj = VannoUtils.strToBool(val);
				break;
			case UNZIP:
				unzip = VannoUtils.strToBool(val);
				break;
			case VERBOSE:
				log.setVerbose(VannoUtils.strToBool(val));
				break;
			case REMOVE_COMMENT:
				removeCommemt = VannoUtils.strToBool(val);
				break;
			case OUTPUT_MODE:
				outputMode = VannoUtils.checkOutMode(Integer.parseInt(val));
				break;
			case OUTPUT_FOLDER:
				outputFolder = val;
				break;
			case DB_PATH:
				path = val;
				break;
			case DB_INDEX_TYPE:
				indexType = val;
				break;	
			case DB_LABEL:
				outName = val;
				break;
			case DB_INTERSECT:
				intersectType = val;
				break;
			case COMMNT_INDICATOR:
				commentIndicator = val;
				break;
			case HEADER_PATH:
				headerPath = val;
				break;
			case HEADER:
				hasHeader = VannoUtils.strToBool(val);
				break;
			case ZERO_BASED:
				zerobased = VannoUtils.strToBool(val);
				break;
			case QUERY_CHROM:
				chrom = Integer.parseInt(val);
				break;
			case QUERY_BEGIN:
				begin = Integer.parseInt(val);
				break;
			case QUERY_END:
				end = Integer.parseInt(val);
				break;
			case QUERY_REF:
				ref = Integer.parseInt(val);
				break;
			case QUERY_ALT:
				alt = Integer.parseInt(val);
				break;
			default:
				break;
		}
	}
}
