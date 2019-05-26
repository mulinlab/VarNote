package main.java.vanno.bean.query;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import htsjdk.samtools.util.BlockCompressedInputStream;
import htsjdk.samtools.util.IOUtil;
import htsjdk.samtools.util.StringUtil;
import htsjdk.tribble.util.LittleEndianOutputStream;
import main.java.vanno.bean.config.config.AbstractReadConfig;
import main.java.vanno.bean.database.Database;
import main.java.vanno.bean.database.DatabaseConfig;
import main.java.vanno.bean.format.BEDHeaderParser;
import main.java.vanno.bean.node.Node;
import main.java.vanno.constants.BasicUtils;
import main.java.vanno.constants.VannoUtils;
import main.java.vanno.constants.VannoUtils.PROGRAM;


public final class Output {
	private final static OutMode DEFALT_OUT_MODE = BasicUtils.DEFALT_OUT_MODE;
	private final static boolean DEFAULT_REMOVE_COMMENT = BasicUtils.DEFAULT_REMOVE_COMMENT;
	private final static boolean DEFAULT_LOJ = BasicUtils.DEFAULT_LOJ;

	private static String OVERLAP_RESULT_SUFFIX = BasicUtils.OVERLAP_RESULT_SUFFIX;
	private final static String OVERLAP_RESULT_SUFFIX_GZ = BasicUtils.OVERLAP_RESULT_SUFFIX_GZ;
	private static String ANNO_RESULT_SUFFIX = BasicUtils.ANNO_RESULT_SUFFIX;
	private final static String ANNO_RESULT_SUFFIX_GZ = BasicUtils.ANNO_RESULT_SUFFIX_GZ;
	
	private final static String TAB = BasicUtils.TAB;
	private final static String QUERY_START = BasicUtils.QUERY_START;
	
	public enum OutMode {
		QUERY(0),
		DB(1),
		BOTH(2);
		
		private final int num;
		OutMode(final int num) {
	        this.num = num;
	    }
		public int getNum() {
			return num;
		}
	}
	
	private final Query query;
	private final List<DatabaseConfig> dbConfigs;
	private OutMode outputMode;
	private boolean loj;
	private boolean removeCommemt;
	
	private List<Printter> printters;
	private String outputFolder;
	private String tempFolderPath;
	
	private String overlapOutput;
	private String annoOutput;
	
	
	public Output(final String _outputFolder, final List<DatabaseConfig> _dbConfigs, final Query query) {
		this(_outputFolder, _dbConfigs, query, DEFAULT_LOJ, DEFAULT_REMOVE_COMMENT, DEFALT_OUT_MODE);
	}
	
	public Output(final String _outputFolder, final List<DatabaseConfig> _dbConfigs, final Query _query,
			final boolean _loj, final boolean _removeCommemt, final OutMode _outMode) {
		super();
		this.query = _query;
		this.dbConfigs = _dbConfigs;
		this.outputMode = _outMode;
		this.loj = _loj;
		this.removeCommemt = _removeCommemt;
		this.outputFolder = _outputFolder;
		//IOUtil.assertInputIsValid(this.overlapOutput);
	}

	public void init(final Log log, final PROGRAM _pro, final boolean isGzip) {
		if(isGzip) {
			OVERLAP_RESULT_SUFFIX = OVERLAP_RESULT_SUFFIX_GZ;
			ANNO_RESULT_SUFFIX = ANNO_RESULT_SUFFIX_GZ;
		}
	
		log.printStrWhite("\n\n----------------------------------------------------  OUTPUT  SETTING  -----------------------------------------------------");
		log.printKVKCYN("Compress result", isGzip + "");
		log.printKVKCYN("Using loj", loj + "");
		log.printKVKCYN("Output mode", outputMode.toString());
		log.printKVKCYN("Remove commemt", removeCommemt + "");
		
		
		if(outputFolder == null) {
			this.outputFolder = query.getQueryPath() + "_out" ;
			log.printKVKCYNSystem("Output path is not defined, redirect output folder to", outputFolder);
		} else {
			File dir = new File(outputFolder);
			IOUtil.assertDirectoryIsWritable(dir); 
			this.outputFolder = dir.getAbsolutePath();
			dir = null; 
		}
		
		File folder =  new File(this.outputFolder);
		if (!folder.exists()) {
			log.printKVKCYNSystem("Output folder hasn't created, creating out folder", folder.getAbsolutePath());
			try{
		    		folder.mkdir();
		    } catch(SecurityException se) {
		    		log.printStrWhiteSystem("Creating directory " + folder.getAbsolutePath() + " with error! please check.");
		    }        
		} 
		
		File tempFolder =  new File(folder + File.separator + "temp");
		if (!tempFolder.exists()) {
			log.printKVKCYNSystem("Creating temp folder", tempFolder.getAbsolutePath());
		    try{
		    		tempFolder.mkdir();
		    } catch(SecurityException se) {
		    		log.printStrWhiteSystem("Creating directory " + tempFolder.getAbsolutePath() + " with error! please check.");
		    }        
		} 
		
		this.tempFolderPath = tempFolder.getAbsolutePath();
		if(_pro == PROGRAM.OVERLAP) {
			overlapOutput = this.outputFolder + File.separator + query.getQueryName() + OVERLAP_RESULT_SUFFIX;
			annoOutput = null;
		} else {
//			overlapOutput = this.outputFolder + File.separator + query.getQueryName() + OVERLAP_RESULT_SUFFIX;
			setDefaultAnnoOutput(log);
		}

		if(_pro == PROGRAM.ANNO) log.printKVKCYNSystem("Anno output file path is", annoOutput);
		else log.printKVKCYNSystem("Overlap output file path is", overlapOutput);
	}
	
	public void setDefaultAnnoOutput(final Log log) {
		annoOutput = this.outputFolder + File.separator + query.getQueryName() + ANNO_RESULT_SUFFIX;
//		IOUtil.assertInputIsValid(annoOutput);
		log.printKVKCYNSystem("Output file is not defined, redirect output file path to", annoOutput);
	}
	
	public void setPrintter(final int thread, final boolean isGzip) {
		printters = new ArrayList<Printter>();
		try {
			for (int i = 0; i < thread; i++) {
				printters.add(new Printter(this.tempFolderPath + File.separator + query.getQueryName(), i, isGzip));
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}
	
	public Printter getPrintter(int i) {
		return printters.get(i);
	}

	public List<Printter> getPrintters() {
		return printters;
	}

	public String getOverlapOutput() {
		return overlapOutput;
	}
	
	public WritableByteChannel initChannels(final String outPath) throws FileNotFoundException {
		return Channels.newChannel(new FileOutputStream(new File(outPath)));
	}
	
	public void mergeTempZipFile(final LittleEndianOutputStream out, final Log log) throws IOException {
		byte[] buf = new byte[1024 * 128];
		for (int i = 0; i < printters.size(); i++) {
			printters.get(i).tearDownPrintter();
			File tempFile = printters.get(i).getFile();
			if (tempFile != null) {
				BlockCompressedInputStream in = new BlockCompressedInputStream(tempFile);
				int n = 0;
				while((n = in.read(buf)) != -1) {
					out.write(buf, 0, n);
				}
//				String line;
//				while((line = in.readLine()) != null) {
//					out.writeBytes(line + "\n");
//				}
				in.close();
				if (!tempFile.delete()) {
					System.err.println("Delete " + tempFile.getAbsolutePath() + " is failed.");
				} 
			}
		}
		if (!new File(tempFolderPath).delete()) {
			System.err.println("Delete " + tempFolderPath + " is failed.");
		} else {
			log.printKVKCYNSystem("Delete temp folder", tempFolderPath);
		}
	}
	
	public void mergeTempFile(final WritableByteChannel outChannel, final Log log) throws IOException {
		ReadableByteChannel inChannel = null;
		ByteBuffer byteBuffer = ByteBuffer.allocate(1024);
		for (int i = 0; i < printters.size(); i++) {
			printters.get(i).tearDownPrintter();
			File tempFile = printters.get(i).getFile();
			if (tempFile != null) {
				inChannel = Channels.newChannel(new FileInputStream(tempFile));

				while (inChannel.read(byteBuffer) > 0) {
					byteBuffer.flip();
					outChannel.write(byteBuffer);
					byteBuffer.clear();
				}
				// outChannel.write(ByteBuffer.wrap(new
				// String("\n").getBytes()));
				inChannel.close();
				if (!tempFile.delete()) {
					System.err.println("Delete " + tempFile.getAbsolutePath() + " is failed.");
				} 
			}
		}

		if (!new File(tempFolderPath).delete()) {
			System.err.println("Delete " + tempFolderPath + " is failed.");
		} else {
			log.printKVKCYNSystem("Delete temp folder", tempFolderPath);
		}
	}

	public void printOverlapHeader(final LittleEndianOutputStream out, final List<Database> dbs) throws IOException {
		if(removeCommemt) return;
		out.writeBytes(appendBufStr(AbstractReadConfig.QUERY, query.getQueryPath()));
		out.writeBytes(appendBufStr(AbstractReadConfig.QUERY_FORMAT, query.getQueryFormat().toString()));
		if(query.getQueryFormat().getHeaderPath() != null)
			out.writeBytes(appendBufStr(AbstractReadConfig.HEADER_PATH, query.getQueryFormat().getHeaderPath()));
		
		if(query.getQueryFormat().getOriginalField() != null && query.getQueryFormat().getOriginalField().size() > 0) {
			out.writeBytes(appendBufStr(AbstractReadConfig.HEADER, StringUtil.join(BEDHeaderParser.COMMA, query.getQueryFormat().getOriginalField())));
		} 
		for (Database db : dbs) {
			out.writeBytes(appendBufStr(AbstractReadConfig.DB_PATH, db.getConfig().getDbPath()));
			out.writeBytes(appendBufStr(AbstractReadConfig.DB_INDEX_TYPE, db.getConfig().getIndexType().toString()));
			out.writeBytes(appendBufStr(AbstractReadConfig.DB_LABEL, db.getConfig().getOutName()));
		}
		
		out.writeBytes(appendBufStr(AbstractReadConfig.OUTPUT_FOLDER, outputFolder));
		out.writeBytes(AbstractReadConfig.OVERLAP_NOTE + AbstractReadConfig.END + "\n");
	}
	
	public void printOverlapHeader(final WritableByteChannel outChannel, final List<Database> dbs) throws IOException {
		if(removeCommemt) return;
		outChannel.write(ByteBuffer.wrap(appendBuf(AbstractReadConfig.QUERY, query.getQueryPath())));
		outChannel.write(ByteBuffer.wrap(appendBuf(AbstractReadConfig.QUERY_FORMAT, query.getQueryFormat().toString())));

		if(query.getQueryFormat().getHeaderPath() != null)
			outChannel.write(ByteBuffer.wrap(appendBuf(AbstractReadConfig.HEADER_PATH, query.getQueryFormat().getHeaderPath())));
		
		if(query.getQueryFormat().getOriginalField() != null && query.getQueryFormat().getOriginalField().size() > 0) {
			outChannel.write(ByteBuffer.wrap(appendBuf(AbstractReadConfig.HEADER, StringUtil.join(BEDHeaderParser.COMMA, query.getQueryFormat().getOriginalField()))));
		} 

		for (Database db : dbs) {
			outChannel.write(ByteBuffer.wrap(appendBuf(AbstractReadConfig.DB_PATH, db.getConfig().getDbPath())));
			outChannel.write(ByteBuffer.wrap(appendBuf(AbstractReadConfig.DB_INDEX_TYPE, db.getConfig().getIndexType().toString())));
			outChannel.write(ByteBuffer.wrap(appendBuf(AbstractReadConfig.DB_LABEL, db.getConfig().getOutName())));
		}
		
		outChannel.write(ByteBuffer.wrap(appendBuf(AbstractReadConfig.OUTPUT_FOLDER, outputFolder)));
		StringBuffer buf = new StringBuffer(AbstractReadConfig.OVERLAP_NOTE);
		buf.append(AbstractReadConfig.END);
		buf.append("\n");
		outChannel.write(ByteBuffer.wrap(buf.toString().getBytes("utf-8")));
	}

	public void printAnnoHeader(final WritableByteChannel outChannel, final List<String> header) throws IOException {
		for (String string : header) {
			outChannel.write(ByteBuffer.wrap(new StringBuffer(string).append("\n").toString().getBytes("utf-8")));
		}
	}
	
	public void printAnnoHeader(final LittleEndianOutputStream out, final List<String> header) throws IOException {
		for (String string : header) {
			out.writeBytes(string + "\n");
		}
	}
	
	public void printAnnoHeader(final BufferedWriter writer, final List<String> header) throws IOException {
		for (String string : header) {
			writer.write(string + "\n");
		}
	}

	public void setOutputMode(OutMode outputMode) {
		this.outputMode = outputMode;
	}
	
	public void setOutputMode(int outMode) {
		this.outputMode = VannoUtils.checkOutMode(outMode);
	}

	public void setLoj(boolean loj) {
		this.loj = loj;
	}

	public void setRemoveCommemt(boolean removeCommemt) {
		this.removeCommemt = removeCommemt;
	}
	
	public String getAnnoOutput() {
		return annoOutput;
	}

	public boolean isLoj() {
		return loj;
	}

	public byte[] appendBuf(final String key, final String val) throws UnsupportedEncodingException {
		return appendBufStr(key, val).getBytes("utf-8");
	}
	
	public String appendBufStr(final String key, final String val) throws UnsupportedEncodingException {
		StringBuffer buf = new StringBuffer(AbstractReadConfig.OVERLAP_NOTE);
		buf.append(key);
		buf.append(AbstractReadConfig.OVERLAP_EQUAL);
		buf.append(val);
		buf.append("\n");
		return buf.toString();
	}

	public OutMode getOutputMode() {
		return outputMode;
	}
	
	public void print(final Node node, final Map<String, List<String>> results, final int index) throws IOException {
		Printter printter = getPrintter(index);
		
		if(loj) {
			printter.print(QUERY_START + TAB + node.origStr);
		} else if(results.size() > 0 && outputMode != OutMode.DB) {
			printter.print(QUERY_START + TAB + node.origStr); //+ results.size() + " "
		}
		
		if(outputMode != OutMode.QUERY)
		for (DatabaseConfig databaseConfig : dbConfigs) {
			if(results.get(databaseConfig.getOutName()) != null)
			for (String str : results.get(databaseConfig.getOutName())) {
				printter.print(databaseConfig.getOutName() + "\t" + str); 
			}
		}
	}
	
	public void print(final String result, final int index) throws IOException {
		if(result != null) {
			Printter printter = getPrintter(index);
			printter.print(result);
		}
	}

	public void setAnnoOutput(String annoOutput) {
		IOUtil.assertInputIsValid(annoOutput);
		this.annoOutput = annoOutput;
	}
}
