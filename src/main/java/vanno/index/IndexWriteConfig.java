package main.java.vanno.index;

import htsjdk.samtools.util.IOUtil;
import main.java.vanno.bean.database.Database.IndexType;
import main.java.vanno.bean.database.index.IndexFactory;
import main.java.vanno.bean.database.index.vannoIndex.VannoIndex;
import main.java.vanno.bean.format.BEDHeaderParser;
import main.java.vanno.bean.format.Format;
import main.java.vanno.constants.BasicUtils;
import main.java.vanno.constants.InvalidArgumentException;
import main.java.vanno.constants.VannoUtils;
import main.java.vanno.constants.VannoUtils.FileType;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


public final class IndexWriteConfig {
	private Format format;
	private String input;
	private String inputFileName;
	private String outputDir;
	private boolean useJDK;
	
	public IndexWriteConfig(final String input) {
		this(input, null, null, false);
	}
	
	public IndexWriteConfig(final String input, final Format format) {
		this(input, format, null, false);
	}
	
	public IndexWriteConfig(final String input, final Format format, final String outputDir, final boolean useJDK) {
		super();
		setInput(input);
		File inputFile = new File(this.input); 
		this.inputFileName = inputFile.getName();
		
		if(outputDir == null) { 
			this.outputDir =  inputFile.getParent();
		} else {
			setOutput(outputDir);
		}
		if(format == null) this.format = Format.defaultFormat(this.input, false);
		else this.format = format;
		
		this.useJDK = useJDK;
	}
	
	public void init() {
        format.checkOverlap(this.input, FileType.BGZ);
	}

	public VannoIndex getIndex() {
		final String dbIndex = input + IndexType.VANNO.getExtIndex();
		if(!VannoUtils.isExist(dbIndex)) throw new InvalidArgumentException("Cannot find vanno index file: " + dbIndex + " !");
		
		return (VannoIndex)IndexFactory.readIndex(dbIndex, useJDK);
	}
	
	public void printMetaData() {
		final VannoIndex idx = getIndex();
		MetaReader.readHeader(idx.getFormat(), input, "meta data", useJDK);
	}
	
	public void printHeader() {
		final VannoIndex idx = getIndex();
		List<String> colNames = idx.getHeaderColList();
		if(colNames != null && colNames.size() > 0) {
			System.out.println("Header contains " + colNames.size() + " columns as following:");
			for (String colName : colNames) {
				System.out.println(colName);
			}
		} else {
			System.out.println("No header found!");
		}
	}
	
	public void listChromsome() {
		final VannoIndex idx = getIndex();
		String[] mSeq = idx.getmSeq();
		if(mSeq != null && mSeq.length > 0) {
			System.out.println("List sequence names: ");
			for (String seq : mSeq) {
				System.out.println(seq);
			}
		} else {
			System.out.println("No sequence names found!");
		}
	}
	
	public void replaceHeader(final String header) {
		final VannoIndex idx = getIndex();
		final String dbIndex = input + IndexType.VANNO.getExtIndex();
		final File tempFile = new File(dbIndex + BasicUtils.TEMP);
		final MyEndianOutputStream indexLos = new MyEndianOutputStream(new MyBlockCompressedOutputStream(tempFile));
		final List<String> sequenceNames = new ArrayList<String>();
		
		for (String seq : idx.getmSeq()) {
			sequenceNames.add(seq);
		}
		VannoUtils.writeFormats(indexLos, idx.getFormat(), 
				BEDHeaderParser.parserHeaderComma(header.replaceAll("\"", "").replaceAll("“", "").replaceAll("”", "")), 
				sequenceNames, idx.getMinOffForChr());
		try {
			indexLos.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		boolean success = tempFile.renameTo(new File(dbIndex));
		if (!success) {
		   System.err.println("Rename from '" + tempFile.getAbsolutePath() + "' to '" + dbIndex + "' with error. ");
		   System.exit(1);
		} 
	}
	
	public Format getFormat() {
		return format;
	}
	
	public void setFormat(Format format) {      
		this.format = format;
	}
	public String getInput() {
		return input;
	}
	
	public void setInput(String input) {
		IOUtil.assertInputIsValid(input);
		this.input = new File(input).getAbsolutePath();
	}
	
	public String getOutputDir() {
		return outputDir;
	}
	public void setOutput(String path) {
		File dir = new File(path);
		IOUtil.assertDirectoryIsWritable(dir); 
		this.outputDir = dir.getAbsolutePath();
		dir = null;
	}
	public boolean isUseJDK() {
		return useJDK;
	}
	public void setUseJDK(boolean useJDK) {
		this.useJDK = useJDK;
	}
	public void setNumHeaderLinesToSkip(final int numHeaderLinesToSkip) { 
		this.format.setNumHeaderLinesToSkip(numHeaderLinesToSkip);
	}

	public String getInputFileName() {
		return inputFileName;
	}
}
