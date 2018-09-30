package readers;

import htsjdk.samtools.seekablestream.SeekableStreamFactory;
import htsjdk.tribble.util.ParsingUtils;
import index.BlockFeature;
import index.MyBlockCompressedInputStream;
import java.io.IOException;
import java.util.List;
import constants.BasicUtils;
import process.VannoResultProcess;
import process.ProcessResult;
import stack.AbstractReaderStack;
import bean.index.Index;
import bean.index.IndexFactory;
import bean.node.Bin;
import bean.node.Node;

public final class SweepVannoReader extends SweepReader{
	
	private MyBlockCompressedInputStream vannoFile;
	private List<Bin> bins;
    
    public SweepVannoReader(final String fn, final AbstractReaderStack stack) throws IOException {
		this(fn, ParsingUtils.appendToPath(fn, BasicUtils.VANNO_INDEX_EXTENSION), ParsingUtils.appendToPath(fn, BasicUtils.VANNO_EXTENSION), stack);
	}
    
    public SweepVannoReader(final String fn, final String idxFn, final String vannoFn, final AbstractReaderStack stack) throws IOException {
    	this(fn, IndexFactory.readIndex(idxFn), vannoFn, stack);
    }
    
    public SweepVannoReader(final String fn, final Index idx, final String vannoFn, final AbstractReaderStack stack) throws IOException {
		super(fn, idx, stack);
		vannoFile = new MyBlockCompressedInputStream(SeekableStreamFactory.getInstance().getBufferedStream(SeekableStreamFactory.getInstance().getStreamFor(vannoFn)));

		if(stack.getResultProcessor() instanceof VannoResultProcess) {
			((VannoResultProcess)stack.getResultProcessor()).setMFP(this.mFp);
		}
	}

    public final class SweepIteratorImpl extends VannoIteratorImpl {
		 private int index;
		 private Bin bin;
		 
		 public SweepIteratorImpl(final MyBlockCompressedInputStream _is) {
	    	 super(_is);
	    	 index = 0;
	    	 bin = bins.get(index);
	    	 try {
	    		 _is.seek(bin.getFilePointer());
	 		 } catch (IOException e) {
	 			e.printStackTrace(); 
	 		 }	
	     }
		
		 @Override
		 public void processBlock(final byte flag) throws IOException {
			 block = new BlockFeature(is, bin.getMin(), flag);
			 vannoFeature = block.makeFirstVannoFeature();
			 if(++index < bins.size()) {
				 bin = bins.get(index);
			 }
		 }
	}
	

	@Override
	public void initForChr(final String chr) throws IOException {
		bins = null;
		int tid = chr2tid(chr);
		if(tid != -1) {
			bins = readIndex(tid, vannoFile); 
			stack.clearST();
			stack.setIterator(new SweepIteratorImpl(vannoFile));
		}
	}
	

	@Override
	public void doQuery(final Node query) throws IOException {
		if(bins == null) return;
		stack.findOverlap(query);
	}

    @Override
    public String toString() {
        return "SweepVannoReader: filename:"+getSource();
    }
	
	public void close() {
		if(vannoFile != null) {
            try {
            	vannoFile.close();
            } catch (IOException e) {
            }
        }
		super.close();
	}
	
	@Override
	public ProcessResult getProcess() {
		return stack.getResultProcessor();
	}
}
