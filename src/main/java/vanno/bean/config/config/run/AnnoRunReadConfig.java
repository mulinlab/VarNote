package main.java.vanno.bean.config.config.run;

import main.java.vanno.bean.config.run.AnnoRunConfig;
import main.java.vanno.bean.database.DatabaseConfig;
import main.java.vanno.bean.query.Output;
import main.java.vanno.bean.query.Query;
import main.java.vanno.constants.InvalidArgumentException;
import main.java.vanno.constants.VannoUtils.PROGRAM;


public final class AnnoRunReadConfig<T> extends OverlapReadConfig<T>{

	public static final String OVERLAP_FILE = "overlap_file";
	public static final String ANNO_CONFIG = "anno_config";
	public static final String VCF_HEADER_PATH_FOR_BED = "vcf_header_for_bed";
	public static final String FORCE_OVERLAP = "force_overlap";
	public static final String ANNO_FORMAT = "anno_format";
	public static final String ANNO_OUT = "anno_out";
	private String annoOutputFile, overlapFile;
	
	@Override
	public void doInit() {
		super.doInit();
		runConfig = new AnnoRunConfig();
		annoOutputFile = null; 
		overlapFile = null;
	}
	
	
	@SuppressWarnings("unchecked")
	@Override
	public T doEnd() {
		if(overlapFile != null) {
			((AnnoRunConfig)runConfig).setOverlapFile(overlapFile, log, !unzip, loj);
			
		} else {
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
			runConfig.setQuery(query);
			runConfig.setDbConfigs(dbConfig);
		}
		runConfig.setGzip(!unzip);
		runConfig.setLog(log);
		runConfig.setProgram(PROGRAM.ANNO);
		// todo  if overlap
		if(annoOutputFile != null) ((AnnoRunConfig)runConfig).getOutput().setAnnoOutput(annoOutputFile); 
		return (T)runConfig;
	}
	
	
	@Override
	public boolean filterLine(String line) {
		return super.filterLine(line);
	}
	
	
	@Override
	public void putValue(String key, String val) {
		AnnoRunConfig bean = (AnnoRunConfig)runConfig;
		super.putValue(key, val);
		switch (key) {
			case FORCE_OVERLAP:
				bean.setForceOverlap(val);
				break;
			case ANNO_FORMAT:
				bean.setAnnoOutFormat(val);
				break;
			case ANNO_OUT:
				annoOutputFile = val;
				break;
			case VCF_HEADER_PATH_FOR_BED:
				bean.setVcfHeaderPathForBED(val);
				break;	
			case ANNO_CONFIG:
				bean.setAnnoConfig(val);
				break;
			case OVERLAP_FILE:
				overlapFile = val;
			default:
				break;
		}
	}
}
