package index.interval;

import htsjdk.samtools.seekablestream.ISeekableStreamFactory;
import htsjdk.samtools.seekablestream.SeekableStream;
import htsjdk.samtools.seekablestream.SeekableStreamFactory;
import htsjdk.samtools.util.BlockCompressedFilePointerUtil;
import htsjdk.samtools.util.BlockCompressedInputStream;
import htsjdk.samtools.util.BlockCompressedOutputStream;
import htsjdk.samtools.util.StringUtil;
import htsjdk.tribble.index.tabix.TabixFormat;
import htsjdk.tribble.util.LittleEndianOutputStream;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import postanno.FormatWithRef;
import constants.BasicUtils;
import bean.node.Bin;

public class BinIndexWriter extends AbstractIndexWriter{
	
// 	private static final byte[] MAGIC = {'I', 'N', 'T', 'E', 'R', 1};
	public static final int version = 1;
    public static final String STANDARD_INDEX_EXTENSION = ".bi";
    public static final String PLUS_EXTENSION = ".plus";
    public static final String TEMP = ".temp";
    public static final int PLUS_FILE_END = -99;
    public static final int SHORT_MAX = 32767;
    public static final int INT_START = -101;
    public static final int BLOCK_START = -100;
    public static final String PLUS_INDEX_EXTENSION = ".plus.bi";
    public static final int INTERVAL_LIDX_SHIFT = 16;
    public static final int MAX_INTERVAL_INDEX_SIZE =  4681; //GenomicIndexUtil.MAX_BINS;
//    private static final String HEADER = "header";
    
	private final String path;
	private String outputPath;
	private Map<Integer, List<Bin>> bins;
	private Map<Integer, Long> addressOfChr;
	
 
    
	public BinIndexWriter(final String path, final String plusIndex) {
		this.path = path;
		this.outputPath = plusIndex;
		this.bins = new HashMap<Integer, List<Bin>>();
		this.addressOfChr = new HashMap<Integer, Long>();
	}

	public void initStream(final File plusFile) {
		if(plusFile.exists()) {
			try {
				 final ISeekableStreamFactory ssf = SeekableStreamFactory.getInstance();
		         final SeekableStream seekableStream =
		                    ssf.getBufferedStream(ssf.getStreamFor(plusFile.getAbsolutePath()));
		         BlockCompressedInputStream is = new BlockCompressedInputStream(seekableStream);
		         writeIndex(is);
		     	 is.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		} else {
			throw new IllegalArgumentException("plus file in location:" + path +" is needed to write a plus index.");
		}
	}

	public void writeIndex(final BlockCompressedInputStream is) throws IOException {
		int preTid = -1, tid = 0, beg, range, end;
	
		List<Bin> list = new ArrayList<Bin>(BasicUtils.MAX_BINS);
//		int preBin = -1, bin;
		long preAddress = -1, address;
		long filePointer = 0;
		Bin bean = null;
		
		final String tempFile = outputPath + BinIndexWriter.TEMP;
		LittleEndianOutputStream losblock = new LittleEndianOutputStream(new BlockCompressedOutputStream(tempFile));
		while(true) {
		    tid = readShort(is);
		    if(tid == BinIndexWriter.PLUS_FILE_END) {
		    	break;
		    }

		    if(tid != preTid) {
		    	System.out.println("Writing index file for chr id " + tid);
		    	if(preTid > -1) {
		    		list.add(bean);
		    		bins.put(preTid, list);
		    		list = new ArrayList<Bin>(BasicUtils.MAX_BINS);
		    		bean = null;
		    	}
		    	addressOfChr.put(tid, filePointer);
		    	preTid = tid;
//		    	preBin = -1;
		    	preAddress = -1;
		    }	    
			
			beg = readInt(is);
			range = readShort(is);

			if(range == BinIndexWriter.INT_START) {
				range = readInt(is);
			}
			end = beg + range;
			readLong(is);
		
//			bin = BasicUtils.regionToBin(beg, end);
			address = BlockCompressedFilePointerUtil.getBlockAddress(filePointer);
//			if((bin != preBin) || (address != preAddress)) {
			if((address != preAddress)) {
//				if(tid == 20 && end < 21774) 
//					System.out.println("new bin =" + bin + "," + filePointer);
				if(bean != null) list.add(bean);
				bean = new Bin(filePointer, end);
				
//				if(bin != preBin) {
//					preBin = bin;
//				}
				if(address != preAddress) {
					preAddress = address;
				}
			} 
				
			bean.updateMax(end);
			filePointer = is.getFilePointer();
		}
		
		list.add(bean);
		bins.put(preTid, list);
		
//		System.out.println(preTid);
		final int version = readInt(is); //readVersion
		final int MAGIC_NUMBER = readInt(is); //magic number
		
		HashMap<String, Integer> mChr2tid = new HashMap<String, Integer>();
		final int flags = readInt(is);
		final int mSc = readInt(is);
		final int mBc = readInt(is);
		final int mEc = readInt(is);
		final char mMeta = (char)readInt(is);
//		 System.out.println("flags=" + flags + ",mSc=" + mSc + ", mBc=" + mBc + ", mEc=" +mEc );
	    FormatWithRef formatSpec = new FormatWithRef(new TabixFormat(flags, mSc, mBc, mEc, mMeta, readInt(is)), readInt(is), readInt(is), readInt(is), readInt(is));
	    
		List<String> seqNames = new ArrayList<String>(readInt(is));
//		System.out.println("seqNames=" + seqNames.size());
	    byte[] buf = new byte[4];
    	int i, j, k, l = readInt(is);
        buf = new byte[l];
        is.read(buf);
        for (i = j = k = 0; i < buf.length; ++i) {
            if (buf[i] == 0) {
                byte[] b = new byte[i - j];
                System.arraycopy(buf, j, b, 0, b.length);
                String s = new String(b);
                mChr2tid.put(s, k);
                seqNames.add(s);
                k++;
                j = i + 1;
            }
        }
//        System.out.println("MAGIC_NUMBER=" + MAGIC_NUMBER);
        losblock.writeInt(version);
        losblock.writeInt(MAGIC_NUMBER);
        losblock.writeInt(formatSpec.tbiFormat.flags);
        losblock.writeInt(formatSpec.tbiFormat.sequenceColumn);
        losblock.writeInt(formatSpec.tbiFormat.startPositionColumn);
        losblock.writeInt(formatSpec.tbiFormat.endPositionColumn);
        losblock.writeInt(formatSpec.tbiFormat.metaCharacter);
        losblock.writeInt(formatSpec.tbiFormat.numHeaderLinesToSkip);
        losblock.writeInt(formatSpec.refCol);
        losblock.writeInt(formatSpec.altCol);
        losblock.writeInt(formatSpec.infoCol);
        losblock.writeInt(formatSpec.flag);
        
        losblock.writeInt(seqNames.size());
        int nameBlockSize = seqNames.size(); // null terminators
        for (final String sequenceName : seqNames) {
        	nameBlockSize += sequenceName.length();
        }

        losblock.writeInt(nameBlockSize);
        for (final String sequenceName : seqNames) {
        	losblock.write(StringUtil.stringToBytes(sequenceName));
        	losblock.write(0);
        }
        
        losblock.writeInt(addressOfChr.keySet().size());
        for (Integer chrID : addressOfChr.keySet()) {
        	losblock.writeInt(chrID);	
        	losblock.writeLong(addressOfChr.get(chrID));	
		}
        
        for (Integer key : this.bins.keySet()) {
        	list = bins.get(key);
        	if(list != null && list.size() > 0) {
        		losblock.writeShort(key);
        		losblock.writeInt(list.size());
        		for (Bin b : list) {
        			losblock.writeLong(b.getStart());
        			losblock.writeInt(b.getMax());	
        			losblock.writeInt(b.getSize());
    			}
        	}
		}
		losblock.close();
		boolean success = new File(tempFile).renameTo(new File(outputPath));
		if (!success) {
		   System.err.println("Make index has completed. But rename from '" + tempFile + "' to '" + outputPath + "' with error. ");
		} 
	}
	
	public static void main(String[] args) {
//		String path = "/Users/mulin/Desktop/t100k.vcf.gz";
//		BinIndexWriter blockWriter = new BinIndexWriter(path);
//		blockWriter.initStream(); 
//		String path = "/Users/mulin/Desktop/clinvar.20160215.sv.ANN.bgz";
	}
}
