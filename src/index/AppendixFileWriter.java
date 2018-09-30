package index;

import htsjdk.samtools.util.BlockCompressedFilePointerUtil;
import htsjdk.samtools.util.BlockCompressedInputStream;
import htsjdk.samtools.util.BlockCompressedOutputStream;
import htsjdk.samtools.util.StringUtil;
import htsjdk.tribble.index.tabix.TabixFormat;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import com.intel.gkl.compression.IntelDeflaterFactory;
import constants.BasicUtils;
import postanno.FormatWithRef;
import bean.index.IndexFactory;
import bean.node.Bin;
import bean.node.Node;
import bean.node.NodeFactory;

public final class AppendixFileWriter {
	private final FormatWithRef formatSpec;
	private final List<String> sequenceNames = new ArrayList<String>();
	private final Set<String> sequenceNamesSeen = new HashSet<String>();
	
	private Map<Integer, List<Bin>> bins;
	private List<Bin> list;
	private Bin bin;
	private Map<Integer, Long> addressOfChr;
	
	private MyEndianOutputStream los;
	private boolean isBlockStart;
	
	public AppendixFileWriter(FormatWithRef formatSpec) {
		this.formatSpec = formatSpec;
		this.isBlockStart = true;
		this.bins = new HashMap<Integer, List<Bin>>();
		this.addressOfChr = new HashMap<Integer, Long>();
		this.list = null;
		this.bin = null;
	}

	public void writeIndexForBGZ(final BlockCompressedInputStream reader) throws IOException {
		String line,  seqName = "";
		long blockAddress = 0, preBlockAddress = 0, filePointer = 0;
		int tid = 0, blockOffset = 0, preOffset = 0, preBeg = -1;
		Node feature;
		
		for(int i=0; i<formatSpec.tbiFormat.numHeaderLinesToSkip; i++) {
			line = reader.readLine();
			if(line == null) break;
			filePointer = reader.getFilePointer();
		}
		
		while((line = reader.readLine()) != null) {
			if((line.trim().charAt(0) == formatSpec.tbiFormat.metaCharacter) || line.trim().equals("")) {
				filePointer = reader.getFilePointer();
			} else {
				feature = decodeFeature(line);
				
				if(!feature.chr.equals(seqName)) {
					if(list != null) {
						addBin(bin);
						bins.put(tid, list);
					}
					
					isBlockStart = true;
					preOffset = 0;
					preBeg = 0;
					
					addSeqsName(feature.chr);
					seqName = feature.chr;
					tid = sequenceNames.size() - 1;
				
					writeChr(tid, seqName);

					list = new ArrayList<Bin>(BasicUtils.MAX_BINS);
					bin = null;
				}
				
				if(feature.beg < preBeg) throw new IllegalArgumentException(String.format("Features added out of order: next start " + feature.beg + " < previous start" + preBeg + ", please sorted features by chr column, start column."));
				if(feature.beg > feature.end) {
					 throw new IllegalArgumentException(String.format("Feature start position %d > feature end position %d", feature.beg, feature.end));
				}
				
				if(isBlockStart) {
					addBin(bin);
					bin = new Bin(feature.beg, feature.end, los.getOut().getFilePointer(), filePointer);
				} else {
					bin.updateMax(feature, blockOffset - preOffset);
				}
				
				preBlockAddress = blockAddress;
				preBeg = feature.beg;
				preOffset = blockOffset;
				
				filePointer = reader.getFilePointer();
				blockAddress = BlockCompressedFilePointerUtil.getBlockAddress(filePointer);
				blockOffset = BlockCompressedFilePointerUtil.getBlockOffset(filePointer);
				
				if(blockAddress == preBlockAddress) {
					isBlockStart = false;
				} else {
					isBlockStart = true;
					preOffset = blockOffset;
				}
			}
		
		}
		if(list != null) {
			addBin(bin);
			bins.put(tid, list);
		}
		reader.close();
	}

	public void writeShort(final int val) throws IOException {
		if(val <= BasicUtils.MAX_SHORT) {
			los.writeShort(val);
		} else {
			los.writeShort(BasicUtils.INT_START);
			los.writeInt(val);
		}
	}
	
	
	public void writeChr(final int tid, final String chr) throws IOException {
		los.write(BasicUtils.CHR_START);
		System.out.println("Writing vanno file for chr " + chr );
	}
	
	public void addBin(final Bin bin) throws IOException {
		if((bin != null) && bin.hasFeature()) {
			
			final List<bean.node.Bin.Feature> features = bin.getBlockFeature();
			
			//write block start
			int end = features.get(0).getEnd(), avgOffset = bin.getAVGOffset();
			if(end == 1) {
				los.write(BasicUtils.BLOCK_START_WO_END); 
			} else {
				if(end <= BasicUtils.MAX_BYTE_UNSIGNED) {
					los.write(BasicUtils.BLOCK_START_WITH_END_BTYE); 
					los.write(end); 
				} else if(end <= BasicUtils.MAX_SHORT_UNSIGNED) {
					los.write(BasicUtils.BLOCK_START_WITH_END_SHORT); 
					los.writeShort(end); 
				} else {
					los.write(BasicUtils.BLOCK_START_WITH_END_INT); 
					los.writeInt(end); 
				}
			}
			
			los.writeLong(bin.getBfilePointer());
			writeShort(avgOffset);
			
			//write offset
			int beg, offset , flagBeg = 0, flagEnd = 0;
			int flag = 0;
			bean.node.Bin.Feature feature = null;
			for (int i = 1; i < features.size(); i++) {
				flagBeg = 0;  flagEnd = 0; flag = 0;
				feature = features.get(i);
				
				beg = feature.getBeg();     end = feature.getEnd();    offset = feature.getOffset() - avgOffset;
				
				if(beg <= 4 ) {
					flag += beg;   
				} else if(beg <= BasicUtils.MAX_BYTE_UNSIGNED) {
					flagBeg = 1; //101  
				} else if(beg <= BasicUtils.MAX_SHORT_UNSIGNED) {
					flagBeg = 2; //110   
				} else {
					flagBeg = 3; //111   
				}
				if(flagBeg > 0) flag = flag + flagBeg + 4;
	
				if(end == 1) {      
											//00
				} else if(end <= BasicUtils.MAX_BYTE_UNSIGNED) {
					flagEnd = 1;          //01         
				} else if(end <= BasicUtils.MAX_SHORT_UNSIGNED) {
					flagEnd = 2;         //10         
				} else {
					flagEnd = 3;          //11
				}
				flag += flagEnd*8; 
				
				if(offset < 0) {       //offset sign
					flag += 32;        //1 = negative 
				} 
				
				offset = Math.abs(offset);
				if(offset > BasicUtils.MAX_BYTE_UNSIGNED) {
					flag += 64;      //0 = byte  //1 = short or int
				} 
				
				los.write(flag);

				if(flagBeg > 0) write(flagBeg, beg);
				if(flagEnd > 0) write(flagEnd, end);
				
				if(offset <= BasicUtils.MAX_BYTE_UNSIGNED) {
					los.write(offset);
				} else if(offset <= BasicUtils.MAX_SHORT) {
					los.writeShort(offset);
				} else {
					los.writeShort(BasicUtils.INT_START);	
					los.writeInt(offset);
				}
			}
			
			bin.clear();
			list.add(bin);
		}
	}
	

//    public void writeFlag(boolean[] flags) throws IOException {
//        byte num = 0;
//        for (int index = 1; index < 8; index++){
//           if(flags[index]) {
//        	   num += 1<<(7-index);
//           }  
//        }
//        los.write(num);
//    }
    
	public void write(final int flag, final int val) throws IOException {
		if(flag == 1) {
			los.write(val);
		} else if(flag == 2) {
			los.writeShort(val);
		} else if(flag == 3) {
			los.writeInt(val);
		}
	}
	
	public void makeIndex(final String outputPath, final String bgzFilePath, final String inputName) {
		try {
			final String vannoFile = outputPath + BasicUtils.VANNO_EXTENSION; // + inputName
			final String vannoIndexFile = outputPath + BasicUtils.VANNO_INDEX_EXTENSION;
			
			final File tempFile = new File(vannoFile + BasicUtils.TEMP);

			BlockCompressedOutputStream.setDefaultDeflaterFactory(new IntelDeflaterFactory());
			
			los = new MyEndianOutputStream(new BlockCompressedOutputStream(tempFile));
			BlockCompressedInputStream reader = BasicUtils.checkStream(new File(bgzFilePath));

			writeIndexForBGZ(reader);
			los.write(BasicUtils.VANNO_FILE_END);
			writeBins(los);
			los.close();
			
			final MyEndianOutputStream indexLos = new MyEndianOutputStream(new BlockCompressedOutputStream(vannoIndexFile));
			writeFormats(indexLos);
			indexLos.close();
		
			boolean success = tempFile.renameTo(new File(vannoFile));
			if (!success) {
			   System.err.println("Rename from '" + tempFile.getAbsolutePath() + "' to '" + vannoFile + "' with error. ");
			   System.exit(1);
			} 

			System.out.println("\nVanno file is done, please find it in " + vannoFile);
			System.out.println("Vanno index file is done, please find it in " + vannoIndexFile + "\n");
			
		} catch (IOException e) {
			e.printStackTrace();
		} 
	}
	
	public void writeBins(final MyEndianOutputStream binLos) throws IOException {
		List<Bin> list;
		long filePointer = binLos.getOut().getFilePointer();
		int preMin = 0;
		for (Integer key : this.bins.keySet()) {
			list = bins.get(key);
			preMin = 0;
			if (list != null && list.size() > 0) {
				addressOfChr.put(key, filePointer);
				binLos.writeInt(list.size());
				for (Bin b : list) {
					binLos.writeLong(b.getFilePointer());
					binLos.writeInt(b.getMin() - preMin);
					binLos.writeInt(b.getMax() - b.getMin());
					preMin = b.getMin();
				}
				filePointer = binLos.getOut().getFilePointer();
			}
		}
	}

    public void writeFormats(final MyEndianOutputStream indexLos) {
    	try { 
    		 TabixFormat formatSpec = this.formatSpec.tbiFormat;
    		 indexLos.writeInt(BasicUtils.version);
    		 indexLos.writeInt(IndexFactory.MAGIC_NUMBER);
    		 indexLos.writeInt(formatSpec.flags);
    		 indexLos.writeInt(formatSpec.sequenceColumn);
    		 indexLos.writeInt(formatSpec.startPositionColumn);
    		 indexLos.writeInt(formatSpec.endPositionColumn);
    		 indexLos.writeInt(formatSpec.metaCharacter);
    		 indexLos.writeInt(formatSpec.numHeaderLinesToSkip);
    		 indexLos.writeInt(this.formatSpec.refCol);
    		 indexLos.writeInt(this.formatSpec.altCol);
    		 indexLos.writeInt(this.formatSpec.infoCol);
    		 indexLos.writeInt(this.formatSpec.flag);
	         
    		 indexLos.writeInt(sequenceNames.size());
	         
	         int nameBlockSize = sequenceNames.size(); // null terminators
	         for (final String sequenceName : sequenceNames) {
	         	nameBlockSize += sequenceName.length();
	         }
	
	         indexLos.writeInt(nameBlockSize);
	         for (final String sequenceName : sequenceNames) {
	        	 indexLos.write(StringUtil.stringToBytes(sequenceName));
	        	 indexLos.write(0);
	         }
	         
	         indexLos.writeInt(addressOfChr.keySet().size());
	         for (Integer chrID : addressOfChr.keySet()) {
//	         	 System.out.println(chrID + ", " + addressOfChr.get(chrID));
	        	 indexLos.writeInt(chrID);	
	        	 indexLos.writeLong(addressOfChr.get(chrID));	
	 		}
    	} catch (IOException e) {
			e.printStackTrace();
		}
    }

    public void addSeqsName(String sequenceName) {
		if(sequenceNamesSeen.contains(sequenceName)) {
			throw new IllegalArgumentException("chr must in order!");
		}
		sequenceNames.add(sequenceName);
		sequenceNamesSeen.add(sequenceName);
	}
    
    public Node decodeFeature(String s) {		
		return NodeFactory.createBasic(s, formatSpec);
	}
    
	public static void main(String[] args) {
	}
}
