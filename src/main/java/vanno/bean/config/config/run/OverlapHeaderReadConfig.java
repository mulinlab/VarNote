package main.java.vanno.bean.config.config.run;


import java.util.ArrayList;
import java.util.List;
import main.java.vanno.bean.config.config.AbstractReadConfig;
import main.java.vanno.bean.config.run.AnnoRunConfig;
import main.java.vanno.bean.database.DatabaseConfig;
import main.java.vanno.bean.format.Format;
import main.java.vanno.bean.query.Output;
import main.java.vanno.bean.query.Query;
import main.java.vanno.constants.InvalidArgumentException;
import main.java.vanno.constants.VannoUtils.PROGRAM;

public final class OverlapHeaderReadConfig<T> extends AbstractReadConfig<T>{

	private AnnoRunConfig runConfig;
	private String outputFolder = null;;
	private List<DatabaseConfig> dbConfig;
	private String path = null, indexType = null, outName = null, intersectType = null;
	
	@Override
	public void doInit() {
		super.doInit();
		outputFolder = null; 
		dbConfig = new ArrayList<DatabaseConfig>();
		runConfig = new AnnoRunConfig();
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public T doEnd() {
		if(path != null) {
			dbConfig.add(new DatabaseConfig(path, outName, intersectType, indexType));
		}
		if(dbConfig.size() == 0) throw new InvalidArgumentException("No Database found, you should define database information start with " + DB_BEGIN + ".");
		if(outputFolder != null) {
			runConfig.setOutput(new Output(outputFolder, dbConfig, runConfig.getQuery(), DEFAULT_LOJ, DEFAULT_REMOVE_COMMENT, DEFALT_OUT_MODE));
		}
		runConfig.setProgram(PROGRAM.ANNO);
		runConfig.setDbConfigs(dbConfig);
		return (T)runConfig;
	}
	
	@Override
	public boolean filterLine(String line) {
		return line.startsWith(OVERLAP_NOTE);
	}

	@Override
	public boolean isBreak(String line) {
		return line.equalsIgnoreCase(OVERLAP_NOTE + END);
	}

	@Override
	public String[] splitLine(String line) {
		line = line.substring(1);
		if(line.indexOf(OVERLAP_EQUAL) != -1) {
			return line.split(OVERLAP_EQUAL);
		} else {
			throw new InvalidArgumentException("Cannot parse line : \"" + line +"\", properties should have a format like key=value.");
		}
	}

	@Override
	public void putValue(String key, String val) {
		switch (key) {
			case QUERY:
				runConfig.setQuery(new Query(val));
				break;
			case QUERY_FORMAT:	
				runConfig.getQuery().setQueryFormat(Format.readFormatString(val));
				break;
			case HEADER_PATH:
				runConfig.getQuery().getQueryFormat().setHeaderPath(val);
				break;
//			case HEADER:
//				runConfig.getQuery().getQueryFormat().setColNmaes(colNmaes);
//				break;
			case DB_PATH:
				if(path != null) {
					dbConfig.add(new DatabaseConfig(path, outName, intersectType, indexType));
				}
				path = val;
				break;
			case DB_INDEX_TYPE:
				indexType = val;
				break;
			case DB_LABEL:
				outName = val;
				break;
			case OUTPUT_FOLDER:
				outputFolder = val;
				break;
			default:
				break;
		}
	}
}
