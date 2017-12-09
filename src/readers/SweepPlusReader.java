package readers;

import htsjdk.samtools.seekablestream.SeekableStreamFactory;
import htsjdk.samtools.util.BlockCompressedInputStream;
import htsjdk.tribble.util.ParsingUtils;
import index.interval.BinIndexWriter;
import java.io.IOException;
import java.util.List;
import process.MyResultProcess;
import process.ProcessBlock;
import stack.AbstractReaderStack;
import bean.config.Printter;
import bean.index.Index;
import bean.index.IndexFactory;
import bean.node.Node;
import bean.node.NodePlus;

public class SweepPlusReader extends SweepReader implements ProcessBlock{
	
	private BlockCompressedInputStream plus;
    private Printter dbPrintter;
    
    public SweepPlusReader(final String fn, final AbstractReaderStack stack, Printter printter) throws IOException {
		this(fn, ParsingUtils.appendToPath(fn, BinIndexWriter.PLUS_INDEX_EXTENSION), ParsingUtils.appendToPath(fn, BinIndexWriter.PLUS_EXTENSION), stack, printter);
	}
    
    public SweepPlusReader(final String fn, final String idxFn, final String plusFn, final AbstractReaderStack stack, Printter printter) throws IOException {
    	this(fn, IndexFactory.readIndex(idxFn), plusFn, stack, printter);
    }
    
    public SweepPlusReader(final String fn, final Index idx, final String plusFn, final AbstractReaderStack stack, Printter printter) throws IOException {
		super(fn, idx, stack);
		plus = new BlockCompressedInputStream(SeekableStreamFactory.getInstance().getBufferedStream(SeekableStreamFactory.getInstance().getStreamFor(plusFn)));

		if(stack.getResultProcessor() instanceof MyResultProcess) {
			((MyResultProcess)stack.getResultProcessor()).setProcessor(this);
		}
		dbPrintter = printter;
	}

	protected class SweepIteratorImpl implements Iterator {
		 private boolean iseof;
		 private int tid, beg, end;
		 private short range;
	     protected SweepIteratorImpl(final int tid) {
	    	 iseof = false;
	    	 this.tid = tid;
	     }
	    		 
		@Override
		public String next() throws IOException {
             return null;
		}
		
		@Override
		public Node nextNode() throws IOException {
			 if (iseof) return null;
//			 int tid =  readInt(plus);
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
			 return new NodePlus(beg, end, "", readLong(plus));
		}
	}
	

	@Override
	public void initForChr(String chr) {
		int tid = chr2tid(chr);
    	if(tid < 0 || tid >= idx.getmSeqLen()) return;
    	try {
			plus.seek(idx.getMinOffForChr(tid));
		} catch (IOException e) {
			e.printStackTrace();
		}	
    	
    	stack.clearST();
		stack.setIterator(new SweepIteratorImpl(tid));
	}

	@Override
	public void doQuery(Node query) {
		stack.findOverlap(query);
	}

    @Override
    public String toString() {
        return "SweepPlusReader: filename:"+getSource();
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
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
