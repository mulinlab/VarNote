package bean.config;

import htsjdk.samtools.util.IOUtil;
import htsjdk.tribble.util.TabixUtils;
import index.interval.AppendixFileWriter;
import index.interval.BinIndexWriter;

import java.io.File;
import java.util.Arrays;
import java.util.List;

import bean.index.TbiIndex;
import constants.ErrorMsg;
import constants.VannoUtils;
import constants.VannoUtils.INDEXTYPE;
import joptsimple.OptionParser;
import joptsimple.OptionSet;
import postanno.FormatWithRef;

public class IndexWriteBean {
	private String path;
	private FormatWithRef format;
	private INDEXTYPE type;
	private String outputPathPlus;
	private String outputPathPlusIndex;
	
	@SuppressWarnings("unchecked")
	public IndexWriteBean(OptionSet options) {
		super();
		String[] requiredOPT = {"path"};
		VannoUtils.checkMissing(options, requiredOPT);
        
        String path = (String) options.valueOf("path");
        IOUtil.assertInputIsValid(path);
        this.path = path;
        
        if(options.has("f")) {
        	format = VannoUtils.parseIndexFileFormat((String)options.valueOf("f"));
        } else {
        	 File tbiIndex = new File(path + TabixUtils.STANDARD_INDEX_EXTENSION);
        	 if(tbiIndex.exists()) {
            	TbiIndex index  = new TbiIndex(tbiIndex.getAbsolutePath());
            	format = index.getFormatWithRef();
             } 
        }
        if(format == null) throw new IllegalArgumentException("File format is missing, format can be " + ErrorMsg.INDEX_FILE_FORMAT_CMD);
        
        if(options.has("columns") && format.flag != FormatWithRef.VCF_FORMAT) {
        	format = VannoUtils.updateFormat(path, format, (List<String>)options.valuesOf("columns"));
        }
		format.checkOverlap();

		if(options.has("comment")) {
			String comment = (String)options.valueOf("comment");
			if(comment.length() > 1) throw new IllegalArgumentException("metaCharacter should be a char, not " + comment);
			format.tbiFormat.metaCharacter = comment.charAt(0);
		} else {
			format.tbiFormat.metaCharacter = '#';
		}
		if(options.has("skip")) {
			format.tbiFormat.numHeaderLinesToSkip = (Integer)options.valueOf("skip");
		} else {
			format.tbiFormat.numHeaderLinesToSkip = 0;
		}
			
		type = INDEXTYPE.ALL;
        if(options.has("t")) type = VannoUtils.checkWriteIndexType((String)options.valueOf("t"));
        
        String pathDir = null;
        if(options.has("o")) {
        	pathDir = (String)options.valueOf("o");
    		IOUtil.assertDirectoryIsWritable(new File(pathDir));
        }
        
        if(pathDir == null) {
        	outputPathPlus = path + BinIndexWriter.PLUS_EXTENSION;
        	outputPathPlusIndex = path + BinIndexWriter.PLUS_INDEX_EXTENSION;
        } else {
        	if(type == INDEXTYPE.PLUS_INDEX) {
        		outputPathPlus = path + BinIndexWriter.PLUS_EXTENSION;
        		IOUtil.assertInputIsValid(outputPathPlus);
        	} else {
        		outputPathPlus = pathDir + new File(path).getName() + BinIndexWriter.PLUS_EXTENSION;
        	}
        	
        	outputPathPlusIndex = pathDir + new File(path).getName() + BinIndexWriter.PLUS_INDEX_EXTENSION;
        }
	}
	
	public void run() {
		if(type == INDEXTYPE.PLUS) {
        	writePlus();
        } else if(type == INDEXTYPE.PLUS_INDEX) {
        	writePlusIndex();
        } else { //if(t == INDEXTYPE.BOTH)
        	writePlus();
        	writePlusIndex();
        } 
	}
	
	public void writePlus() {
		System.out.println("write plus file for file : " + path + " begin...");
		AppendixFileWriter write = new AppendixFileWriter(format);
    	write.makeIndex(outputPathPlus, path);
    	System.out.println("write plus file for file : " + path + " end");
	}
	
	public void writePlusIndex() {
		System.out.println("write plus index file for file : " + outputPathPlus + " begin...");
	
		BinIndexWriter blockWriter = new BinIndexWriter(path, outputPathPlusIndex);
    	blockWriter.initStream(new File(outputPathPlus));
		System.out.println("write plus index file for file : " + outputPathPlus + " end");
	}
	
	public static OptionParser getParserForIndex() {
		OptionParser parser = new OptionParser();
		parser.accepts("help", ErrorMsg.PRINT_HELP);
		parser.acceptsAll(Arrays.asList("p", "path"), ErrorMsg.INDEX_FILE_PATH_CMD)
				.withRequiredArg().describedAs(ErrorMsg.INDEX_FILE_PATH_DESC).ofType(String.class);  //待索引的文件
		
		parser.acceptsAll(Arrays.asList("f", "format"), ErrorMsg.INDEX_FILE_FORMAT_CMD)
				.withOptionalArg().describedAs(ErrorMsg.INDEX_FILE_FORMAT_DESC).ofType(String.class);  //查询文件
		
		parser.acceptsAll(Arrays.asList("c", "columns"), ErrorMsg.INDEX_FILE_COLUMN_CMD)
				.withOptionalArg().describedAs(ErrorMsg.INDEX_FILE_COLUMN_DESC).withValuesSeparatedBy(',').ofType(String.class); 
		
		parser.acceptsAll(Arrays.asList("t", "type"), ErrorMsg.INDEX_FILE_TYPE_CMD)
				.withOptionalArg().describedAs(ErrorMsg.INDEX_FILE_TYPE_DESC).ofType(String.class).defaultsTo("all"); 
		
		parser.acceptsAll(Arrays.asList("o", "out"), ErrorMsg.INDEX_OUTPUT_CMD)
				.withOptionalArg().describedAs(ErrorMsg.INDEX_OUTPUT_CMD).ofType(String.class);
		
		parser.acceptsAll(Arrays.asList("m", "comment"), ErrorMsg.INDEX_COMMENT_CMD)
				.withOptionalArg().describedAs("Comment").ofType(String.class).defaultsTo("#");
		
		parser.acceptsAll(Arrays.asList("s", "skip"), ErrorMsg.INDEX_SKIP_CMD)
				.withOptionalArg().describedAs("Skip Lines").ofType(Integer.class).defaultsTo(0);
		
		parser.accepts("version", ErrorMsg.PRINT_VERSION);
        return parser;
	}
}
