package main.java.vanno.index;

import htsjdk.samtools.util.BlockCompressedFilePointerUtil;
import htsjdk.samtools.util.zip.DeflaterFactory;
import main.java.vanno.bean.database.Database.IndexType;
import main.java.vanno.bean.format.BEDHeaderParser;
import main.java.vanno.bean.format.Format;
import main.java.vanno.bean.node.Node;
import main.java.vanno.bean.node.NodeFactory;
import main.java.vanno.constants.BasicUtils;
import main.java.vanno.constants.InvalidArgumentException;
import main.java.vanno.constants.VannoUtils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import com.intel.gkl.compression.IntelDeflaterFactory;

public final class IndexWriter {
	private final IndexWriteConfig config;
	private final Format formatSpec;
	private final List<String> sequenceNames = new ArrayList<String>();
	private final Set<String> sequenceNamesSeen = new HashSet<String>();
	private ByteArrayOutputStream bufStream;
	private int max;
	
	private Map<Integer, Long> addressOfChr;
	private MyEndianOutputStream vannoOS;
	private MyEndianOutputStream vannoIndexOS;
	private boolean isBlockStart;
	private List<String> headerColList;
	
	private SROB bin;
	private SROBListBean listBean;
	
	public IndexWriter(final IndexWriteConfig config) {
		this.config = config;
		this.formatSpec = config.getFormat();
		this.isBlockStart = true;
		this.addressOfChr = new HashMap<Integer, Long>();
		this.bin = new SROB(-1, 0, 0, 0);
		this.headerColList = null;
		this.max = 0;
		this.listBean = new SROBListBean();
	}

	public void setHeadColList(final String line) {
		if(formatSpec.getHeaderPath() != null) {
			headerColList = formatSpec.getOriginalField();
		} else {
			if(line.startsWith(Format.VCF_HEADER_INDICATOR)) {
				headerColList = BEDHeaderParser.parserHeader(line.substring(Format.VCF_HEADER_INDICATOR.length()));
    			} else {
    				headerColList = BEDHeaderParser.parserHeader(line);
    			}
		}
	}
	
	public void writeIndexForBGZ(final MyBlockCompressedInputStream reader) throws IOException {
		String line,  seqName = "";
		BlockBean blockBean = new BlockBean(0), preBlockBean = new BlockBean(0);
		int tid = 0, preBeg = -1;
		Node feature = new Node();
		boolean readHeader = false;
		
		for(int i=0; i< formatSpec.getNumHeaderLinesToSkip(); i++) {
			line = reader.readLine();
			if(line == null) break;
			
			line = line.trim();
			if(line.startsWith(formatSpec.getCommentIndicator()) || line.equals("")) {
			} else if((formatSpec.isHasHeader() ||  line.startsWith(Format.VCF_HEADER_INDICATOR)) && !readHeader) {
				setHeadColList(line);
				readHeader = true;
			} 
			blockBean.setFilePointer(reader.getFilePointer());
		}

		while((line = reader.readLine()) != null) {
			line = line.trim();
		
			if(line.startsWith(formatSpec.getCommentIndicator()) || line.equals("")) {
				blockBean.setFilePointer(reader.getFilePointer());
			} else if((formatSpec.isHasHeader() ||  line.startsWith(Format.VCF_HEADER_INDICATOR)) && !readHeader) {
				setHeadColList(line);
				readHeader = true; 
				
				blockBean.setFilePointer(reader.getFilePointer());
			} else {
				if(line.length() > this.max) {
					this.max = line.length() + 100;
					this.bufStream = new ByteArrayOutputStream(this.max);
				}
				
				feature = decodeFeature(line, feature, bufStream);
				if(!feature.chr.equals(seqName)) {
					
					if(!seqName.equals("")) {
						addBin(bin);
						vannoOS.write(BasicUtils.CHR_START); //new chr start
						vannoIndexOS.writeInt(BasicUtils.CHR_START);
						listBean.init();
						bin.initSROB(-1, 0, 0, 0);
					}

					isBlockStart = true;
					preBeg = 0;
					
					addSeqsName(feature.chr);
					seqName = feature.chr; tid = sequenceNames.size() - 1;

					addressOfChr.put(tid, vannoIndexOS.getOut().getFilePointer());
					System.out.println("Writing vanno file for chr " + seqName);
				}
				
				if(feature.orgBeg < preBeg) throw new InvalidArgumentException(String.format("Features added out of order: next start " + feature.orgBeg + " < previous start " + preBeg + ", please sorted features by chr column, start column."));
				if(feature.beg > feature.end) throw new InvalidArgumentException(String.format("Feature start position %d > feature end position %d", feature.beg, feature.end));
				
				if(isBlockStart) {
					addBin(bin);
					bin.initSROB(feature.beg, feature.end, vannoOS.getOut().getFilePointer(), blockBean.getFilePointer());
				} else {
					bin.updateMax(feature, blockBean.getBlockOffset() - preBlockBean.getBlockOffset());
				}
				
				preBlockBean.setFilePointer(blockBean.getFilePointer());
				preBeg = feature.orgBeg;
				blockBean.setFilePointer(reader.getFilePointer());

				if(blockBean.getBlockAddress() == preBlockBean.getBlockAddress()) {
					isBlockStart = false;
				} else {
					isBlockStart = true;
				}
			}
		}
		if(!seqName.equals("")) {
			addBin(bin);
			vannoIndexOS.writeInt(BasicUtils.CHR_START);
		}
		reader.close();
	}
	
	public void addBin(final SROB bin) throws IOException {
		if((bin != null) && bin.getMin() != -1 && bin.hasFeature()) {
			
			final List<main.java.vanno.index.SROB.Feature> features = bin.getBlockFeature();
			
			int end = features.get(0).getEnd(), avgOffset = bin.getAVGOffset();
			if(end == 1) {
				vannoOS.write(BasicUtils.BLOCK_START_WO_END); 
			} else {
				if(end <= BasicUtils.MAX_BYTE_UNSIGNED) {
					vannoOS.write(BasicUtils.BLOCK_START_WITH_END_BTYE); 
					vannoOS.write(end); 
				} else if(end <= BasicUtils.MAX_SHORT_UNSIGNED) {
					vannoOS.write(BasicUtils.BLOCK_START_WITH_END_SHORT); 
					vannoOS.writeShort(end); 
				} else {
					vannoOS.write(BasicUtils.BLOCK_START_WITH_END_INT); 
					vannoOS.writeInt(end); 
				}
			}
			
			vannoOS.writeLong(bin.getBGZFilePointer());
			writeShort(vannoOS, avgOffset);

			int beg, offset , flagBeg = 0, flagEnd = 0;
			int flag = 0;
			main.java.vanno.index.SROB.Feature feature = null;
			for (int i = 1; i < features.size(); i++) {
				flagBeg = 0;  flagEnd = 0; flag = 0;
				feature = features.get(i);
				
				beg = feature.getBeg();     end = feature.getEnd();    offset = feature.getOffset() - avgOffset;
				if(beg <= 4 && beg >= 0) {
					flag += beg;   
				} else if(beg <= BasicUtils.MAX_BYTE_UNSIGNED && beg >= 0) {
					flagBeg = 1; 
				} else if(beg <= BasicUtils.MAX_SHORT_UNSIGNED && beg >= 0) {
					flagBeg = 2; 
				} else {
					flagBeg = 3; 
				}
				if(flagBeg > 0) flag = flag + flagBeg + 4;
	
				if(end == 1) {      
											
				} else if(end <= BasicUtils.MAX_BYTE_UNSIGNED && beg >= 0 ) {
					flagEnd = 1;             
				} else if(end <= BasicUtils.MAX_SHORT_UNSIGNED && beg >= 0 ) {
					flagEnd = 2;            
				} else {
					flagEnd = 3;         
				}
				flag += flagEnd*8; 
				
				if(offset < 0) {       
					flag += 32;      
				} 
				
				offset = Math.abs(offset);
				if(offset > BasicUtils.MAX_BYTE_UNSIGNED) {
					flag += 64;      
				} 
				
				vannoOS.write(flag);

				if(flagBeg > 0) write(vannoOS, flagBeg, beg);
				if(flagEnd > 0) write(vannoOS, flagEnd, end);
				
				if(offset <= BasicUtils.MAX_BYTE_UNSIGNED) {
					vannoOS.write(offset);
				} else if(offset <= BasicUtils.MAX_SHORT) {
					vannoOS.writeShort(offset);
				} else {
					vannoOS.writeShort(BasicUtils.INT_START);	
					vannoOS.writeInt(offset);
				}
				feature = null;
			}
			
			bin.clear();
			if(listBean.isBlockChanged(bin.getVannoFilePointer())) {
				vannoIndexOS.writeInt(BasicUtils.BLOCK_ADDRESS);
				vannoIndexOS.writeLong(bin.getVannoFilePointer());
			} else {
				vannoIndexOS.writeInt(listBean.getBlockOffset());
			}
			vannoIndexOS.writeInt(listBean.getOffsetMin(bin.getMin()));
			vannoIndexOS.writeInt(bin.getMax() - bin.getMin());
		}
	}
	
	public void writeShort(final MyEndianOutputStream os, final int val) throws IOException {
		if(val <= BasicUtils.MAX_SHORT) {
			os.writeShort(val);
		} else {
			os.writeShort(BasicUtils.INT_START);
			os.writeInt(val);
		}
	}
	
	public void write(final MyEndianOutputStream os, final int flag, final int val) throws IOException {
		if(flag == 1) {
			os.write(val);
		} else if(flag == 2) {
			os.writeShort(val);
		} else if(flag == 3) {
			os.writeInt(val);
		}
	}
	
	public void makeIndex() {
		try {
			VannoUtils.checkValidBGZ(config.getInput());
			
			final String vannoFile = config.getOutputDir() + File.separator + config.getInputFileName() + IndexType.VANNO.getExt(); // + inputName
			final String vannoIndexFile = config.getOutputDir() + File.separator + config.getInputFileName() + IndexType.VANNO.getExtIndex();
			
			final File vannoFileTemp = new File(vannoFile + BasicUtils.TEMP);
			final File vannoIndexFileTemp = new File(vannoIndexFile + BasicUtils.TEMP); 
			
			if(!config.isUseJDK()) {
				MyBlockCompressedOutputStream.setDefaultDeflaterFactory(new IntelDeflaterFactory());
			} else {
				MyBlockCompressedOutputStream.setDefaultDeflaterFactory(new DeflaterFactory());
			}
			
			vannoOS = new MyEndianOutputStream(new MyBlockCompressedOutputStream(vannoFileTemp));
			vannoIndexOS = new MyEndianOutputStream(new MyBlockCompressedOutputStream(vannoIndexFileTemp));
			
			writeIndexForBGZ(VannoUtils.makeBGZ(config.getInput(), config.isUseJDK()));
			vannoOS.write(BasicUtils.VANNO_FILE_END);
			vannoOS.close();
			
			vannoIndexOS.write(BasicUtils.VANNO_FILE_END);
			long address = vannoIndexOS.getOut().getFilePointer();
			if((headerColList == null) || (headerColList.size() == 0)) {
				headerColList = formatSpec.getOriginalField();
			}
			
			VannoUtils.writeFormats(vannoIndexOS, formatSpec, headerColList, sequenceNames, addressOfChr);
			vannoIndexOS.close(address);
		
			boolean success = vannoFileTemp.renameTo(new File(vannoFile));
			if (!success) {
			   System.err.println("Rename from '" + vannoFileTemp.getAbsolutePath() + "' to '" + vannoFile + "' with error. ");
			   System.exit(1);
			} 

			success = vannoIndexFileTemp.renameTo(new File(vannoIndexFile));
			if (!success) {
			   System.err.println("Rename from '" + vannoIndexFileTemp.getAbsolutePath() + "' to '" + vannoIndexFile + "' with error. ");
			   System.exit(1);
			} 
			
			System.out.println("\nVanno file is done, please find it in " + vannoFile);
			System.out.println("Vanno index file is done, please find it in " + vannoIndexFile + "\n");
		} catch (IOException e) {
			e.printStackTrace();
		} 
	}

    public void addSeqsName(String sequenceName) {
		if(sequenceNamesSeen.contains(sequenceName)) {
			throw new InvalidArgumentException("chr must in order!");
		}
		sequenceNames.add(sequenceName);
		sequenceNamesSeen.add(sequenceName);
	}
    
    public Node decodeFeature(final String s, Node node, ByteArrayOutputStream bufStream) {		
		return NodeFactory.createBasic(s, formatSpec, node, bufStream);
	}
    
    public final class BlockBean {
    		private long filePointer;
    		
		public BlockBean(final long filePointer) {
			this.filePointer = filePointer;
		}
		public void setFilePointer(final long filePointer) {
			this.filePointer = filePointer;
		}
		public long getFilePointer() {
			return filePointer;
		}
		public long getBlockAddress() {
			return BlockCompressedFilePointerUtil.getBlockAddress(filePointer);
		}
		public int getBlockOffset() {
			return BlockCompressedFilePointerUtil.getBlockOffset(filePointer);
		}   		  		
    }
    
    public final class SROBListBean {
    		private long blockAddress = -1, preBlockAddress = -1;
    		private int blockOffset = 0, preMin = 0;
    		
    		public boolean isBlockChanged(final long filePointer) {
    			blockAddress = BlockCompressedFilePointerUtil.getBlockAddress(filePointer);
    			if(blockAddress != preBlockAddress) {
    				preBlockAddress = blockAddress;
    				blockOffset = BlockCompressedFilePointerUtil.getBlockOffset(filePointer);
    				return true;
    			} else {
    				blockOffset = BlockCompressedFilePointerUtil.getBlockOffset(filePointer);
    				return false;
    			}
    		}
    		
    		public int getOffsetMin(final int min) {
    			int offsetMin = min - preMin;
    			preMin = min;
    			return offsetMin;
    		}
    		
    		public SROBListBean() {
			super();
			init();
		}

		public void init() {
    			blockAddress = -1; preBlockAddress = -1; blockOffset = 0;  preMin = 0;
    		}

		public int getBlockOffset() {
			return blockOffset;
		}
    }
    
	public static void main(String[] args) {

		long t1 = System.currentTimeMillis();
		IndexWriteConfig config = new IndexWriteConfig("database4.vcf.gz");
		
		config.init();
        System.out.println("write vanno file for: " + config.getInput() + " begin...");
        IndexWriter write = new IndexWriter(config);
	    	write.makeIndex();
	    	System.out.println("write vanno file for: " + config.getInput() + " end"); 
	    	long t2 = System.currentTimeMillis();
		System.out.println(" Time:" + (t2 - t1));
	}
}
