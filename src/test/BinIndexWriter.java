package test;

import htsjdk.samtools.seekablestream.ISeekableStreamFactory;
import htsjdk.samtools.seekablestream.SeekableStream;
import htsjdk.samtools.seekablestream.SeekableStreamFactory;
import htsjdk.samtools.util.BlockCompressedFilePointerUtil;
import htsjdk.samtools.util.BlockCompressedInputStream;
import htsjdk.samtools.util.BlockCompressedOutputStream;
import htsjdk.samtools.util.StringUtil;
import htsjdk.tribble.index.tabix.TabixFormat;
import htsjdk.tribble.util.LittleEndianOutputStream;
import index.interval.AbstractIndexWriter;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import bean.node.Block;
import bean.node.Bin;

public class BinIndexWriter extends AbstractIndexWriter{
	
	private final String path;
	private Map<Integer, List<Block>> blocks;
	public BinIndexWriter(final String path) {
		this.path = path + IntervalIndex.PLUS_EXTENSION;
//		System.out.println(this.path);
		this.blocks = new HashMap<Integer, List<Block>>();
	}

	public void initStream() {
		File inputFile = new File(path);
		if(inputFile.exists() && inputFile.getName().toLowerCase().endsWith(IntervalIndex.PLUS_EXTENSION)) {
			try {
				 final ISeekableStreamFactory ssf = SeekableStreamFactory.getInstance();
		         final SeekableStream seekableStream =
		                    ssf.getBufferedStream(ssf.getStreamFor(inputFile.getAbsolutePath()));
		         BlockCompressedInputStream is = new BlockCompressedInputStream(seekableStream);
		         writeIndex(is);
		     	 is.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		} else {
			throw new IllegalArgumentException("plus file is needed to write a plus index.");
		}
	}


	
	public void writeIndex(BlockCompressedInputStream is) throws IOException {
		int preTid = -1, tid = 0, beg, range, end;
		long filePointer = 0, ba;
		long blockAdress = -1;
//		int index = 0;
		Block block = null;
		List<Block> list;
		LittleEndianOutputStream losblock = new LittleEndianOutputStream(new BlockCompressedOutputStream(path  + ".bi"));
		while(true) {
			ba = BlockCompressedFilePointerUtil.getBlockAddress(filePointer);
//		    final int uncompressedOffset = BlockCompressedFilePointerUtil.getBlockOffset(filePointer);
		    
		    tid = readShort(is);
		    if(tid == IntervalIndex.PLUS_FILE_END)  break;

		    if(tid != preTid) {
		    	blockAdress = -1;
		    	preTid = tid;
		    }
		    
			
			beg = readInt(is);
			range = readShort(is);
			
			if(range == IntervalIndex.INT_START) {
				range = readInt(is);
			}
			end = beg + range;
			readLong(is);
		
			
		    if(ba != blockAdress) {
		    	if(block != null){
		    		list = blocks.get(block.getTid());
		    		if(list == null) {
		    			list = new ArrayList<Block>();
		    		}
		    		list.add(block);
		    		blocks.put(block.getTid(), list);
//		    		losblock.writeInt(block.getIndex());
//		    		losblock.writeInt(block.getTid());
//		    		losblock.writeInt(block.getMin());
//		    		losblock.writeInt(block.getMax());
//		    		losblock.writeLong(block.getBlockStart());
//		    		System.out.println(block);
		    	}
		    	block = new Block(beg, end, ba, BlockCompressedFilePointerUtil.getBlockOffset(filePointer), tid);
		    	blockAdress = ba;
//		    	index++;
		    }
		    block.updateCount();
		    block.updateMax(beg, end, BlockCompressedFilePointerUtil.getBlockOffset(filePointer));
			filePointer = is.getFilePointer();
		}
		
		if(block != null){
			list = blocks.get(block.getTid());
    		if(list == null) {
    			list = new ArrayList<Block>();
    		}
    		list.add(block);
    		blocks.put(block.getTid(), list);
    		
//    		losblock.writeInt(block.getIndex());
//    		losblock.writeInt(block.getTid());
//    		losblock.writeInt(block.getMin());
//    		losblock.writeInt(block.getMax());
//    		losblock.writeLong(block.getBlockStart());
    	}
		
		
		final int MAGIC_NUMBER = readInt(is); //magic number
		
		HashMap<String, Integer> mChr2tid = new HashMap<String, Integer>();
		int flags = readInt(is);
		int mSc = readInt(is);
		int mBc = readInt(is);
		int mEc = readInt(is);
		char mMeta = (char)readInt(is);
		 System.out.println("flags=" + flags + ",mSc=" + mSc + ", mBc=" + mBc + ", mEc=" +mEc );
	    TabixFormat formatSpec = new TabixFormat(flags, mSc, mBc, mEc, mMeta, readInt(is));
		List<String> seqNames = new ArrayList<String>(readInt(is));
		System.out.println("seqNames=" + seqNames.size());
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
        System.out.println("MAGIC_NUMBER=" + MAGIC_NUMBER);
        losblock.writeInt(MAGIC_NUMBER);
        losblock.writeInt(formatSpec.flags);
        losblock.writeInt(formatSpec.sequenceColumn);
        losblock.writeInt(formatSpec.startPositionColumn);
        losblock.writeInt(formatSpec.endPositionColumn);
        losblock.writeInt(formatSpec.metaCharacter);
        losblock.writeInt(formatSpec.numHeaderLinesToSkip);
        
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
        
        List<Bin> bins;
        for (Integer key : blocks.keySet()) {
        	list = blocks.get(key);
        	if(list != null && list.size() > 0) {
        		losblock.writeShort(list.get(0).getTid());
        		losblock.writeInt(list.size());
        		for (Block b : list) {
        			losblock.writeLong(b.getBlockStart());	
        			bins = b.getBins();
        			losblock.writeShort(bins.size());
        			for (Bin blockBin : bins) {
        				losblock.writeShort(blockBin.getBinNumber());
        				losblock.writeInt(blockBin.getFilePointer());
					}	
    			}
        	}
        	
		}
		losblock.close();
	}
	
	public static void main(String[] args) {
		String path = "/Users/mulin/Desktop/AF.ANN.bgz";
		BinIndexWriter blockWriter = new BinIndexWriter(path);
		blockWriter.initStream();
        
//		String path = "/Users/mulin/Desktop/clinvar.20160215.sv.ANN.bgz";
	}
	

}
