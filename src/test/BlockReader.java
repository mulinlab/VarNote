package test;

import htsjdk.samtools.seekablestream.SeekableStreamFactory;
import htsjdk.samtools.util.BlockCompressedInputStream;
import htsjdk.tribble.TribbleException;
import index.interval.IntervalIndex;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import process.MyResultProcess;
import process.ProcessBlock;
import stack.AbstractReaderStack;
import bean.config.Printter;
import bean.node.Block;
import bean.node.Node;
import bean.node.NodePlus;
import bean.query.QueryLineReaderWithValidation;

public class BlockReader extends AbstractReader implements ProcessBlock{

	private Map<Integer, List<Block>> blockMap;
	private List<Block> blocks;
	private Block currentBlock;
	private BlockIteratorImpl it;
	private int index;
    private BlockCompressedInputStream plus;
    private Printter dbPrintter;
    private int skip = 0;
    public BlockReader(final String fn, final String idxFn, final String plusFn, final AbstractReaderStack stack, Printter printter) throws IOException {
    	super(fn, idxFn);
    	this.stack = stack;
    	plus = new BlockCompressedInputStream(SeekableStreamFactory.getInstance().getBufferedStream(SeekableStreamFactory.getInstance().getStreamFor(plusFn)));

		if(stack.getResultProcessor() instanceof MyResultProcess) {
			((MyResultProcess)stack.getResultProcessor()).setProcessor(this);
		}
		dbPrintter = printter;
    	readAllIndex();
    }
    
    protected void readMagic(BlockCompressedInputStream is) throws IOException {
    	int magicNUmber = readInt(is);
		if (magicNUmber != IntervalIndex.MAGIC_NUMBER) {
            throw new TribbleException(String.format("Unexpected magic number 0x%x", magicNUmber));
        }
    }
    
    protected void readFormat(BlockCompressedInputStream is) throws IOException {
      	 
        mChr2tid = new HashMap<String, Integer>();
        mTid2chr = new HashMap<Integer, String>();
        mPreset = readInt(is);
        mSc = readInt(is);
        mBc = readInt(is);
        mEc = readInt(is);
        mMeta = readInt(is);
        readInt(is);  //read skip
        
        mSeq = new String[readInt(is)]; // # sequences    
        System.out.println("mSeq=" + mSeq.length);
   }
	
	protected void readIndex(BlockCompressedInputStream is) throws IOException {
		int tid, size;
		List<Block> list;
		blockMap = new HashMap<Integer, List<Block>>();
		for (int j = 0; j < mSeq.length; j++) {
			tid = readInt(is);
			size = readInt(is);
	        list = new ArrayList<Block>(size);
	        for (int i = 0; i < size; i++) {
	        	list.add(new Block(readInt(is), readInt(is), readInt(is), readLong(is), tid));
			}
	        blockMap.put(tid, list);
		}
	}
   
//	public int compareBin(RegBean a, int binNum) {
//	int level = BasicUtils.binLevel(binNum);
//	if(level == 0) return -1;
//	if(level == a.getLevel()) {
//		if(binNum > a.getBin()) {
//			return -1;
//		} else if(binNum == a.getBin()) {
//			return 0;
//		} else {
//			return 1;
//		}
//	} else if(level > a.getLevel()) {
//		if(a.getLevel() == 0) return 1;
//		int newBinNum = BasicUtils.toLevel(binNum, a.getLevel());
//		if(newBinNum > a.getBin()) {
//			return -1;
//		} else if(newBinNum == a.getBin()) {
//			return 0;
//		} else {
//			return 1;
//		}
//	} else {
//		int aBin = BasicUtils.toLevel(a.getBin(), level);
//		if(binNum > aBin) {
//			return -1;
//		} else if(binNum == aBin) {
//			return 0;
//		} else {
//			return 1;
//		}
//	}
//}
	
    public void ajustPos(final String chr) throws IOException {
    }

    @Override
    public String toString() {
        return "BlockReader: filename:"+getSource();
    }

	public void query(QueryLineReaderWithValidation query) {
		Node node;
		String chr = "";
		List<Node> querylist = new ArrayList<Node>();
		try {
			while((node = query.nextNodeSpider()) != null) {
//				System.out.println(node );
				if(!node.chr.equals(chr)) {
					stack.findOverlaps(querylist);
					querylist = new ArrayList<Node>();
					
					chr = node.chr;
					initForChr(chr);
				}
				if((blocks == null) || (index == blocks.size())) continue;
				if(currentBlock == null) {
					currentBlock = getNewBlock(node);
					if(currentBlock != null) {
						stack.clearST();
						it = new BlockIteratorImpl(currentBlock.getBlockStart(), currentBlock.getCount());
						stack.setIterator(it);
					}
				}

//				System.out.println(node + ", " + currentBlock);
				if(currentBlock == null) 
					continue;

				if(node.end <= currentBlock.getMax()) {
					querylist.add(node);
				} else if(node.beg < currentBlock.getMax()){ // end > max
					stack.findOverlaps(querylist);
					querylist = new ArrayList<Node>();
					
					currentBlock = addBlock(node, currentBlock);
					if(currentBlock != null) {
						it.updateMax(currentBlock.getMax());
						stack.setIterator(it);
					}
					
				} else { //beg >= max 
					stack.findOverlaps(querylist);
					querylist = new ArrayList<Node>();
					currentBlock = getNewBlock(node);
					if(currentBlock != null) {
						stack.clearST();
						it = new BlockIteratorImpl(currentBlock.getBlockStart(), currentBlock.getCount());
						stack.setIterator(it);
						querylist.add(node);
					}
				}
			}
			stack.findOverlaps(querylist);
			querylist = new ArrayList<Node>();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	
	public void initForChr(String chr) {
		blocks =  blockMap.get(chr2tid(chr));
		index = 0;
		currentBlock = null;
		it = null;
		stack.clearST();
		stack.setIterator(null);
	}
	
	public Block mergeBlock(List<Block> list) {
		if(list.size() == 0) {
			return null;
		} else if(list.size() == 1) {
			return list.get(0);
		} else {
			Block a = list.get(0);
			for (int i = 1; i < list.size(); i++) {
				a.mergeBlock(list.get(i));
			}
			return a;
		}
	}
	
	public Block addBlock(Node query, Block a) {
		
		List<Block> list = new ArrayList<Block>(5);
		list.add(a);
		Block b;
		while(index < blocks.size()) {
//			System.out.println(query + ", " + b + ", " + index);
			b = blocks.get(index++);
			
			if(query.end > b.getMin()) {
				if(query.beg >= b.getMax()) {
					skip++;
					continue;
				} else {
					list.add(b);
				}
			} else { //query.end <= b.min
				index--;
				return mergeBlock(list);
			}
		}
		return null;
	}
	
	public Block getNewBlock(Node query) {

//		if(query.chr.equals("2")) {
//			System.out.println();
//		}
		List<Block> list = new ArrayList<Block>(5);
		Block b;
		while(index < blocks.size()) {
			
		
			b = blocks.get(index++);
//			System.out.println(query + ", " + b + ", " + index);
			if(query.end > b.getMin()) {
				if(query.beg >= b.getMax()) {
					continue;
				} else {
					list.add(b);
				}
			} else { //query.end <= b.min
				index--;
				return mergeBlock(list);
			}
		}
		return null;
	}

	
	public void doQuery(List<Node> querylist, Block block) {
//		System.out.println(block);
		if(querylist.size() > 0) {
			stack.findOverlaps(querylist);
			querylist = new ArrayList<Node>();
		}
	}
	
	protected class BlockIteratorImpl implements Iterator {
		private boolean iseof;
		private int count, beg, end;
		private int max;
		private short range;
	    protected BlockIteratorImpl(final long filePointer, final int max) {
	    	iseof = false;
			try {
				plus.seek(filePointer);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			this.max = max;	
			this.count = 0;
	    }
	    
	    public void updateMax(int max) {
	    	this.max = max;
	    }
	    
		@Override
		public String next() throws IOException {
            return null;
		}
		
		@Override
		public Node nextNode() throws IOException {
			 if (iseof) {
				 return null;	
			 }
			 readShort(plus);
			 beg = readInt(plus);
			 range = readShort(plus);
			 
			 if(range == IntervalIndex.INT_START) {
				 end = beg + readInt(plus);
			 } else {
				 end = beg + range;
			 }
			 NodePlus node = new NodePlus(beg, end, "", readLong(plus));
			 count++; 
			 if(count == max) iseof = true;
			 return node;
		}
	}
	
	public static void main(String[] args)  {
//		try {
//			BlockReader block = new BlockReader("/Users/mulin/Desktop/AF.ANN.bgz.plus", "/Users/mulin/Desktop/AF.ANN.bgz.plus.plus.bi", null);
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
	}

	public void close() {
		try {
			if(stack.getResultProcessor() instanceof MyResultProcess) {
				List<NodePlus> results = ((MyResultProcess)stack.getResultProcessor()).getResults();
				if(results.size() > 0) {
					for (NodePlus plusNode : results) {
						mFp.seek(plusNode.getFilePointer());
						dbPrintter.print(plusNode.getQuery().coreInfo() + mFp.readLine());
					}
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		System.out.println("skip=" + skip);
		if(plus != null) {
            try {
            	plus.close();
            } catch (IOException e) {
            }
        }
		super.close();
	}
	@Override
	public void doProcess(List<NodePlus> results) {
		try {
			for (NodePlus plusNode : results) {
				mFp.seek(plusNode.getFilePointer());
				dbPrintter.print(plusNode.getQuery().coreInfo() + mFp.readLine());
	
//				dbPrintter.print(plusNode.getQuery().coreInfo() + plusNode.filePointer);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
