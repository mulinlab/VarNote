import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.List;

import constants.VannoUtils;
import constants.VannoUtils.PROGRAM;
import postanno.Annotation;
import postanno.FormatWithRef;
import query.AbstractQuery;
import query.IntervalQuery;
import query.SweepQuery;
import query.TabixQuery;
import query.TabixQueryExact;
import bean.config.IndexWriteBean;
import bean.config.readconfig.AbstractReadConfig;
import bean.config.run.AnnoRunBean;
import bean.config.run.CountRunBean;
import bean.config.run.CountRunBean.Mode;
import bean.config.run.DBRunConfigBean;
import bean.config.run.OverlapRunBean;
import bean.query.LineReaderBasic;
import bean.query.QueryLineReaderWithValidation;
import stream.BZIP2InputStream;
import mapreduce.*;
import joptsimple.OptionException;
import joptsimple.OptionParser;
import joptsimple.OptionSet;

public class VannoSearch {
	private MapReduce<File, Long> mr;
	private CountRunBean config;
	
	public File getResult() {
		return (File) mr.getResult();
	}

	public CountRunBean getConfig() {
		return config;
	}

	public void init() {
		try {
			int core = config.getThread();
		    BZIP2InputStream bz2_text = new BZIP2InputStream(config.getQueryFile(), core);
	
		    bz2_text.adjustPos();
		    bz2_text.creatSpider();
			   
			Reducer<File, Mapper<Long>, Long> r = new Reducer<File, Mapper<Long>, Long>() {
				public File doReducer(List<Mapper<Long>> mappers) {
					if(config.getPro() == PROGRAM.COUNT) {
						long count = 0;
						for (Mapper<Long> mapper : mappers) {
							count = count + mapper.getResult();
						}
						System.out.println("Count=" + count);
						return null;
					} else {
						try {
							for (DBRunConfigBean db : config.getDbs()) {
								db.printResults();
							}
							((OverlapRunBean)config).closeWriters();
							MergeTempFiles merge = null;
							BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(new File(((OverlapRunBean)config).getOverlapOutput()))));
							writeHeader(writer);
							for (int i = 0; i < config.getThread(); i++) {
								merge = new MergeTempFiles(((OverlapRunBean)config), writer, i);
								merge.doProcess();
							}
							writer.flush();
							writer.close();						
						} catch (IOException e) {
							e.printStackTrace();
						} finally {
							File directory = new File(((OverlapRunBean)config).getTempFolder());
							if(directory.exists()){
						        File[] files = directory.listFiles();
						        if(null != files){
						            for(int i=0; i<files.length; i++) {
						               files[i].delete();
						            }
						        }
						        directory.delete();
						        System.out.println("Delete temp folder:" + ((OverlapRunBean)config).getTempFolder());
						    }
						}
						return null;		
					}
				}	
			};
			
			mr = new SimpleMapReduce<File, Long>(core, r);
			VannoMapper<Long> mapper = null;
			BufferedWriter writer = null;
			AbstractQuery<Long> queryEngine = null;
			
			for(int i=0; i< core; i++) {
				if(config.getPro() != PROGRAM.COUNT) {
					for (DBRunConfigBean db : config.getDbs()) {
						db.addPrintter(i);
					}
					writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(new File(((OverlapRunBean)config).getTempFileName(i)))));
					((OverlapRunBean)config).addWriter(writer);
				}
				
				FormatWithRef queryFormat = config.getQueryFormat();
				if(config.getMode() == Mode.MIX) {
			
					QueryLineReaderWithValidation reader = new QueryLineReaderWithValidation(bz2_text.spider[i], queryFormat, writer);
					queryEngine = new IntervalQuery<Long>(config, i, reader);
					mapper = new VannoMapper<Long>(queryEngine);
				} else if(config.getMode() == Mode.TABIX) {
					
					LineReaderBasic reader = new LineReaderBasic(bz2_text.spider[i], queryFormat, writer);
					if(config.isExact()) {
						queryEngine = new TabixQueryExact<Long>(config, i, reader);
					} else {
						queryEngine = new TabixQuery<Long>(config, i, reader);
					}
					mapper = new VannoMapper<Long>(queryEngine);
				
				} else if(config.getMode() == Mode.SWEEP) {
					QueryLineReaderWithValidation reader = new QueryLineReaderWithValidation(bz2_text.spider[i], queryFormat, writer);
					queryEngine = new SweepQuery<Long>(config, i, reader);
					mapper = new VannoMapper<Long>(queryEngine);
				}  
				
				mr.addMapper(mapper);
			}
		} catch (Exception e) {
			e.printStackTrace();
		} 
	}
	
	public void writeHeader(final BufferedWriter write) throws IOException {
		write.write(AbstractReadConfig.OVERLAP_NOTE + AbstractReadConfig.QUERY + AbstractReadConfig.OVERLAP_EQUAL + config.getQueryFile() + "\n");
		write.write(AbstractReadConfig.OVERLAP_NOTE + AbstractReadConfig.QUERY_FORMAT + AbstractReadConfig.OVERLAP_EQUAL + config.getQueryFormat().toString() + "\n");
		write.write(AbstractReadConfig.OVERLAP_NOTE + AbstractReadConfig.QUERY_COLUMN + AbstractReadConfig.OVERLAP_EQUAL + config.getQueryColumnsToString(",") + "\n");
		
		for (DBRunConfigBean db : config.getDbs()) {
			write.write(AbstractReadConfig.OVERLAP_NOTE + AbstractReadConfig.DB_PATH + AbstractReadConfig.OVERLAP_EQUAL + db.getDbPath() + "\n");
			write.write(AbstractReadConfig.OVERLAP_NOTE + AbstractReadConfig.DB_FORMAT + AbstractReadConfig.OVERLAP_EQUAL + db.getIdx().getFormatWithRef().toString() + "\n");
			write.write(AbstractReadConfig.OVERLAP_NOTE + AbstractReadConfig.DB_LABEL + AbstractReadConfig.OVERLAP_EQUAL + db.getLable() + "\n");
		}
		write.write(AbstractReadConfig.OVERLAP_NOTE + AbstractReadConfig.END + "\n");
	}

	public static void run(CountRunBean runBean) {
		 VannoSearch vs = new VannoSearch();
		 runBean.printLog();
		 System.out.println("\n===================== Stating Query =================");
		 long t1 = System.currentTimeMillis();
		 vs.config = runBean;
		 vs.init();
		 vs.getResult();
		 long t2 = System.currentTimeMillis();
		 System.out.println("Time:" + (t2 - t1));
		 
		 if(runBean.getPro() == PROGRAM.COUNT) return;
		 System.out.println("Overlap file is done, please find it in " + new File(((OverlapRunBean)vs.getConfig()).getOverlapOutput()).getAbsolutePath());
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		 PROGRAM program = VannoUtils.checkARG(args[0]);
		 
		 OptionParser parser = null;
         OptionSet options = null;
         try {
        	 if(program == PROGRAM.OVERLAP) {
        		 parser = OverlapRunBean.getParserForIndex();
        	 } else if(program == PROGRAM.ANNO) {
        		 parser = AnnoRunBean.getParserForIndex(OverlapRunBean.getParserForIndex());
        	 } else if(program == PROGRAM.COUNT) {
        		 parser = CountRunBean.getParserForIndex();
        	 } else {
        		 parser = IndexWriteBean.getParserForIndex();
        	 }   
             options = parser.parse(args);
         } catch(OptionException oe) {
             System.out.println("Exception when parsing arguments : " + oe.getMessage());
             return;
         }
         
         if(options.has("help")) {
             try {
                 parser.printHelpOn(System.out);
             } catch(IOException e) {
                 e.printStackTrace();
             }
             System.exit(0);
         }
         
         if(options.has("version")) {
             System.out.println("V1.0");
             System.exit(0);
         }
         
         if(program == PROGRAM.OVERLAP) {
        	 OverlapRunBean overlapRunBean = OverlapRunBean.runOverlap(options);
        	 run(overlapRunBean);
        	 
    	 } else if(program == PROGRAM.COUNT) {
    		 CountRunBean countRunBean = CountRunBean.runCount(options);
    		 run(countRunBean);
    		 
    	 } else if(program == PROGRAM.ANNO) {
    		 AnnoRunBean annoRun;
    		 if(!options.has("config")) {
    			 annoRun = new AnnoRunBean(options);
    			 if(!annoRun.isOverlapFile()) {
    				 System.out.println("**Note: Overlap file is not find, run overlap program first!");
        			 run(annoRun.getOverlapBean());
        			 annoRun.setOverlapFile(annoRun.getOverlapBean().getOverlapOutput());
        		 }
    		 } else {
    			 annoRun = AnnoRunBean.readAnnoRunBean((String) options.valueOf("config"));
    			 if(!annoRun.isOverlapFile()) {
    				 System.out.println("**Note: Overlap file is not find, run overlap program first!");
    				 OverlapRunBean overlapRunBean = OverlapRunBean.readOverlapRunBean((String) options.valueOf("config"));
    				 
    				 run(overlapRunBean);
    				 annoRun.setOverlapBean(overlapRunBean);
    				 annoRun.setOverlapFile(overlapRunBean.getOverlapOutput());
    			 }
    		 }
    		
    		 annoRun.readConfig();
    		 Annotation anno = new Annotation(annoRun);
    		 annoRun.printLog();
    		 anno.doAnnotation();
    		 System.out.println("Annotation file has done, please find it in " + new File(annoRun.getOutputFile()).getAbsolutePath());
    	 } else {
    		 IndexWriteBean indexWrite = new IndexWriteBean(options);
    		 indexWrite.run();
    	 }
	}
}
