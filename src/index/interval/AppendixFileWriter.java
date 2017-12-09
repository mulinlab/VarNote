package index.interval;


import htsjdk.samtools.util.BlockCompressedInputStream;
import htsjdk.samtools.util.BlockCompressedOutputStream;
import htsjdk.samtools.util.StringUtil;
import htsjdk.tribble.index.tabix.TabixFormat;
import htsjdk.tribble.util.LittleEndianOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.List;
import constants.BasicUtils;
import postanno.FormatWithRef;
import bean.index.IndexFactory;
import bean.node.Node;
import bean.node.NodeFactory;

public class AppendixFileWriter extends AbstractIndexWriter{

	private final FormatWithRef formatSpec;

	public AppendixFileWriter(FormatWithRef formatSpec) {
		this.formatSpec = formatSpec;
	}
	
	public void addFeature(int tid, Node feature, long filepointer, LittleEndianOutputStream los) throws IOException {
		if(tid > BinIndexWriter.SHORT_MAX) throw new IllegalArgumentException("Currently we don't support to index file which has chr numbers larger than 32767.");
		los.writeShort(tid);
		
		//edit for case: start == end
		if(feature.beg > feature.end) {
			 throw new IllegalArgumentException(String.format("Feature start position %d > feature end position %d", feature.beg, feature.end));
		}
		los.writeInt(feature.beg);
		if((feature.end - feature.beg) > BinIndexWriter.SHORT_MAX) {
//			System.out.println("range:" + (feature.end - feature.beg));
			los.writeShort(BinIndexWriter.INT_START);
			los.writeInt(feature.end - feature.beg); 
		} else {
			los.writeShort(feature.end - feature.beg); 
		}
   
		los.writeLong(filepointer);
	}
	
	public Node decodeFeature(String s) {		
		return NodeFactory.createBasic(s, formatSpec);
	}
	
	public void readBGZInputstream(BlockCompressedInputStream reader, LittleEndianOutputStream los) throws IOException {
		String s, seqName = "", chr;
		long filepointer = 0;
		int tid = 0, linesSkiped = 0;
		Node feature, preFeature = null;
		while((s = reader.readLine()) != null) {
			if(++linesSkiped > formatSpec.tbiFormat.numHeaderLinesToSkip && (s.trim().charAt(0) != formatSpec.tbiFormat.metaCharacter) && !s.trim().equals("") ) {
				feature = decodeFeature(s);
				chr = feature.chr;
				if(!chr.equals(seqName)) {
					System.out.println("Writing plus file for chr " + chr);
					addSeqsName(chr);
					seqName = chr;
					tid = sequenceNames.size() - 1;
					preFeature = null;
				}
				
				if(preFeature != null) {
					if(feature.beg < preFeature.beg) {
						throw new IllegalArgumentException(String.format("Features added out of order: next start " + feature + " < previous start" + preFeature + ", please sorted features by chr column, start column, end column."));
					} 
//					else if(feature.beg == preFeature.beg) {
//						if(feature.end < preFeature.end) {
//							throw new IllegalArgumentException(String.format("Features added out of order: next end " + feature + " < previous end" + preFeature + ", please sorted features by chr column, start column, end column."));
//						}
//					}
				}
				addFeature(tid, feature, filepointer, los);
				preFeature = feature;
			}
			filepointer = reader.getFilePointer();
		}
		reader.close();
	}
	

	public void makeIndex(final String plusFile, final String path) {
		try {
			final File tempFile = new File(plusFile + BinIndexWriter.TEMP);
			BlockCompressedInputStream reader = BasicUtils.checkStream(new File(path));
			LittleEndianOutputStream los = new LittleEndianOutputStream(new BlockCompressedOutputStream(tempFile));
			
			readBGZInputstream(reader, los);
			los.writeShort(BinIndexWriter.PLUS_FILE_END);
			writeOthers(los);
			los.close();
			boolean success = tempFile.renameTo(new File(plusFile));
			if (!success) {
			   System.err.println("Make index has completed. But rename from '" + tempFile.getAbsolutePath() + "' to '" + plusFile + "' with error. ");
			   System.exit(1);
			} 
		} catch (IOException e) {
			e.printStackTrace();
		} 
	}
	
	
	public void writeOthers(final LittleEndianOutputStream los) {
        writeOthers(this.formatSpec, sequenceNames, los);
	}
	  
    public void writeOthers(final FormatWithRef format, final List<String> sequenceNames, final LittleEndianOutputStream los) {
    	try { 
    		 TabixFormat formatSpec = format.tbiFormat;
    		 los.writeInt(BinIndexWriter.version);
    		 los.writeInt(IndexFactory.MAGIC_NUMBER);
	         los.writeInt(formatSpec.flags);
	         los.writeInt(formatSpec.sequenceColumn);
	         los.writeInt(formatSpec.startPositionColumn);
	         los.writeInt(formatSpec.endPositionColumn);
	         los.writeInt(formatSpec.metaCharacter);
	         los.writeInt(formatSpec.numHeaderLinesToSkip);
	         los.writeInt(format.refCol);
	         los.writeInt(format.altCol);
	         los.writeInt(format.infoCol);
	         los.writeInt(format.flag);
	         
	         los.writeInt(sequenceNames.size());
	         
//	         System.out.println("magic=" + IntervalIndex.MAGIC_NUMBER);
//	         System.out.println("flags=" + formatSpec.flags);
//	         System.out.println("sequenceColumn=" + formatSpec.sequenceColumn);
	         int nameBlockSize = sequenceNames.size(); // null terminators
	         for (final String sequenceName : sequenceNames) {
	         	nameBlockSize += sequenceName.length();
	         }
	
	         los.writeInt(nameBlockSize);
	         for (final String sequenceName : sequenceNames) {
	             los.write(StringUtil.stringToBytes(sequenceName));
	             los.write(0);
	         }
    	} catch (IOException e) {
			e.printStackTrace();
		}
    }

	
	public static void main(String[] args) {
//		String path = "/Users/mulin/Desktop/dbNSFP3.0a.ANN.bgz";
//		String path = "/Users/mulin/Desktop/AF.ANN.bgz";
//		String path = "/Users/mulin/Desktop/clinvar.20160215.sv.ANN.bgz";
		
//		AppendixFileWriter write = new AppendixFileWriter(path , TabixFormat.BED);
//		write.makeIndex();
	}
}
