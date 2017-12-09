package test;

import htsjdk.samtools.Defaults;
import htsjdk.samtools.seekablestream.ISeekableStreamFactory;
import htsjdk.samtools.seekablestream.SeekableStream;
import htsjdk.samtools.seekablestream.SeekableStreamFactory;
import htsjdk.samtools.util.BlockCompressedInputStream;
import htsjdk.samtools.util.BlockCompressedOutputStream;
import htsjdk.samtools.util.BlockCompressedStreamConstants;
import htsjdk.samtools.util.StringUtil;
import htsjdk.tribble.AbstractFeatureReader;
import htsjdk.tribble.TribbleException;
import htsjdk.tribble.index.tabix.TabixFormat;
import htsjdk.tribble.util.LittleEndianOutputStream;
import htsjdk.variant.vcf.VCFHeader;
import index.interval.AbstractIndexWriter;
import index.interval.IntervalIndex;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;

import bean.node.Node;
import bean.node.NodeFactory;

public class CollectionFileWriter extends AbstractIndexWriter{

	private final List<TabixFormat> formatSpecList;
	private final List<String> pathList;
	
	public CollectionFileWriter(final List<String> pathList, final List<TabixFormat> formatSpecList) {
		this.pathList = pathList;
		this.formatSpecList = formatSpecList;
	}
	
	@SuppressWarnings("resource")
	public BlockCompressedInputStream checkStream(final File inputFile) {
		try {
			if (AbstractFeatureReader.hasBlockCompressedExtension(inputFile)) {
	            final int bufferSize = Math.max(Defaults.BUFFER_SIZE, BlockCompressedStreamConstants.MAX_COMPRESSED_BLOCK_SIZE);
	
	            if (!BlockCompressedInputStream.isValidFile(new BufferedInputStream(new FileInputStream(inputFile), bufferSize))) {
	                throw new TribbleException.MalformedFeatureFile("Input file is not in valid block compressed format.", inputFile.getAbsolutePath());
	            }
	
	            final ISeekableStreamFactory ssf = SeekableStreamFactory.getInstance();
	            final SeekableStream seekableStream =
	                    ssf.getBufferedStream(ssf.getStreamFor(inputFile.getAbsolutePath()));
	            return new BlockCompressedInputStream(seekableStream);
	        } else {
	        	throw new IllegalArgumentException(".gz .gzip .bgz .bgzf file format is support for index, please check your file type");
	        }

		} catch (final FileNotFoundException e) {
            throw new TribbleException.FeatureFileDoesntExist("Unable to open the input file, most likely the file doesn't exist.", inputFile.getAbsolutePath());
        } catch (final IOException e) {
            throw new TribbleException.MalformedFeatureFile("Error initializing stream", inputFile.getAbsolutePath(), e);
        }
	}
	
	public void addFeature(int tid, Node feature, long filepointer, LittleEndianOutputStream los) throws IOException {
		los.writeInt(tid);
		if(feature.beg >= feature.end) {
			 throw new IllegalArgumentException(String.format("Feature start position %d >= feature end position %d", feature.beg, feature.end));
		}
		los.writeInt(feature.beg);
//		System.out.println(feature.beg + "," + feature.end);
		los.writeInt(feature.end-feature.beg);
		los.writeLong(filepointer);
	}
	
	public Node decodeFeature(String s) {		
		return NodeFactory.createBasic(s, formatSpec);
	}
	
	public void readBGZInputstream(BlockCompressedInputStream reader, LittleEndianOutputStream los) throws IOException {
		String s, seqName = "", chr;
		long filepointer = 0;
		int tid = 0;
		Node feature, preFeature = null;
		while((s = reader.readLine()) != null) {
			if(!s.trim().startsWith(VCFHeader.HEADER_INDICATOR) && !s.trim().equals("")) {
				feature = decodeFeature(s);
				chr = feature.chr;
				if(!chr.equals(seqName)) {
					addSeqsName(chr);
					seqName = chr;
					tid = sequenceNames.size() - 1;
					preFeature = null;
				}
				
				if(preFeature != null) {
					if(feature.beg < preFeature.beg) {
						throw new IllegalArgumentException(String.format("Features added out of order: next start " + feature + " < previous start" + preFeature + ", please sorted features by chr column, start column, end column."));
					} else if(feature.beg == preFeature.beg) {
						if(feature.end < preFeature.end) {
							throw new IllegalArgumentException(String.format("Features added out of order: next end " + feature + " < previous end" + preFeature + ", please sorted features by chr column, start column, end column."));
						}
					}
				}
				addFeature(tid, feature, filepointer, los);
				preFeature = feature;
			}
			filepointer = reader.getFilePointer();
		}
		reader.close();
	}
	

	public void makeIndex() {
		try {
			final String outputPath = path + IntervalIndex.PLUS_EXTENSION;
			final File outputFile = new File(outputPath + IntervalIndex.TEMP);
				
			BlockCompressedInputStream reader = checkStream(new File(path));
			LittleEndianOutputStream los = new LittleEndianOutputStream(new BlockCompressedOutputStream(outputFile));
			readBGZInputstream(reader, los);
				
			los.writeInt(IntervalIndex.PLUS_FILE_END);
			writeOthers(los);
			los.close();
			
			boolean success = outputFile.renameTo(new File(outputPath));
			if (!success) {
			   System.err.println("Make index has completed. But rename from '" + outputFile.getAbsolutePath() + "' to '" + outputPath + "' with error. ");
			} else {
				throw new IllegalArgumentException("We currently support bgz file format. please use bgzip to compress your file!");
			} 
		} catch (IOException e) {
			e.printStackTrace();
		} 
	}
	
	
	public void writeOthers(LittleEndianOutputStream los) {
        writeOthers(this.formatSpec, sequenceNames, los);
	}
	  
    public void writeOthers(TabixFormat formatSpec, List<String> sequenceNames, LittleEndianOutputStream los) {
    	try { 
    		 los.writeInt(IntervalIndex.MAGIC_NUMBER);
	         los.writeInt(formatSpec.flags);
	         los.writeInt(formatSpec.sequenceColumn);
	         los.writeInt(formatSpec.startPositionColumn);
	         los.writeInt(formatSpec.endPositionColumn);
	         los.writeInt(formatSpec.metaCharacter);
	         los.writeInt(formatSpec.numHeaderLinesToSkip);
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
		String path = "/Users/mulin/Desktop/dbNSFP3.0a.ANN.bgz";
//		String path = "/Users/mulin/Desktop/AF.ANN.bgz";
//		String path = "/Users/mulin/Desktop/clinvar.20160215.sv.ANN.bgz";
		
		CollectionFileWriter write = new CollectionFileWriter(path , TabixFormat.BED);
		write.makeIndex();
	}
}
