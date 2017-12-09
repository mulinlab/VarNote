package readers;

import htsjdk.samtools.seekablestream.SeekableStreamFactory;
import htsjdk.samtools.util.BlockCompressedInputStream;
import htsjdk.tribble.util.ParsingUtils;
import index.interval.BinIndexWriter;
import java.io.IOException;
import java.util.List;
import process.MyResultProcess;
import process.ProcessBlock;
import process.ProcessResult;
import stack.AbstractReaderStack;
import bean.config.Printter;
import bean.index.Index;
import bean.index.IndexFactory;
import bean.index.bin.BinIndex;
import bean.node.Bin;
import bean.node.Node;
import bean.node.NodePlus;

public class MixPlusReader extends MixReader implements ProcessBlock {
	private List<Bin> bins;
	private BinIteratorImpl it;
	private int index;
	private Node currentQuery;
    private BlockCompressedInputStream plus;
    private Printter dbPrintter;
	
	public MixPlusReader(final String fn, final AbstractReaderStack stack, Printter printter) throws IOException {
		this(fn, ParsingUtils.appendToPath(fn, BinIndexWriter.PLUS_INDEX_EXTENSION), ParsingUtils.appendToPath(fn, BinIndexWriter.PLUS_EXTENSION), stack, printter);
	}
	
	public MixPlusReader(final String fn, final String idxFn, final String plusFn, final AbstractReaderStack stack, Printter printter) throws IOException {
		this(fn, IndexFactory.readIndex(idxFn), plusFn, stack, printter);
	}
	
	public MixPlusReader(final String fn, final Index idx, final String plusFn, final AbstractReaderStack stack, Printter printter) throws IOException {
		super(fn, idx, stack);
		
		plus = new BlockCompressedInputStream(SeekableStreamFactory.getInstance().getBufferedStream(SeekableStreamFactory.getInstance().getStreamFor(plusFn)));
		
		if(stack.getResultProcessor() instanceof MyResultProcess) {
			((MyResultProcess)stack.getResultProcessor()).setProcessor(this);
		}
		dbPrintter = printter;
	}
	

	@Override
	public void doQuery(Node query) {
		currentQuery = query;
		stack.findOverlap(query);
	}
	
	@Override
	public void initForChr(String chr) {
		int tid = chr2tid(chr);
		if(tid != -1) {
			bins = ((BinIndex)idx).loadBin(tid);
			index = 0;
			currentQuery = null;
			it = new BinIteratorImpl(tid);
			stack.clearST();
			stack.setIterator(it);
		}
	}
	
	protected class BinIteratorImpl implements Iterator {
		private boolean iseof;
		private int tid, beg, end;
		private short range;
		private Bin bin;
		private int p;
		private boolean isLastBin;
		
	    protected BinIteratorImpl(final int tid) {
	    	iseof = false;
	    	this.tid = tid;
	    	bin = bins.get(index);
	    	seek(bin.getStart());
	    	p = 0;
	    	isLastBin = false;
	    }
	    
	    public void seek(long filePointer) {
	    	try {
				plus.seek(filePointer);
			} catch (IOException e) {
				e.printStackTrace();
			}
	    }
	    
		@Override
		public String next() throws IOException {
            return null;
		}
		
		@Override
		public Node nextNode() throws IOException {
			 if (iseof) return null;
			 
			 
			 if(!isLastBin) {
				 boolean findNew = false;
				 while(currentQuery.beg >= bin.getMax()) {
					 findNew = true;
					 index++;
					 if(index == bins.size()) {
						 isLastBin = true;
						 break;
					 }
					 bin = bins.get(index);
				 }
				 if(findNew) {
					 p = 0;
					 seek(bin.getStart());
				 }
			 }

			 int tid =  readShort(plus);
			 if(tid == BinIndexWriter.PLUS_FILE_END || this.tid != tid) {
				 iseof = true;
				 return null;
			 }
			 beg = readInt(plus);
			 range = readShort(plus);
			 
			 if(range == BinIndexWriter.INT_START) {
				 end = beg + readInt(plus);
			 } else {
				 end = beg + range;
			 }
			 if(!isLastBin) {
				 p++;
				 if(p > bin.getSize()) {
					 index++;
					 if(index < bins.size()) {
						 bin = bins.get(index);
						 p = 0;
					 } else {
						 isLastBin = true;
					 }
				 }
			 }

			 return new NodePlus(beg, end, "", readLong(plus));
		}
	}
	
    @Override
    public String toString() {
        return "MixPlusReader: filename:"+getSource();
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

	@Override
	public void doEnd() {
		
	}

	@Override
	public ProcessResult getProcess() {
		// TODO Auto-generated method stub
		return stack.getResultProcessor();
	}

}
