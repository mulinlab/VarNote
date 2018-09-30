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

public final class MixVannoReader extends MixReader{

    private MyBlockCompressedInputStream vannoFile;
	private List<Bin> bins;
    private Bin bin;
    private int index;
    private MixVannoIteratorImpl it;
	private boolean isBlockStart;
	
	public MixVannoReader(final String fn, final AbstractReaderStack stack) throws IOException {
		this(fn, ParsingUtils.appendToPath(fn, BasicUtils.VANNO_INDEX_EXTENSION), ParsingUtils.appendToPath(fn, BasicUtils.VANNO_EXTENSION), stack);
	}
	
	public MixVannoReader(final String fn, final String idxFn, final String vannoFn, final AbstractReaderStack stack) throws IOException {
		this(fn, IndexFactory.readIndex(idxFn), vannoFn, stack);
	}
	
	public MixVannoReader(final String fn, final Index idx, final String vannoFn, final AbstractReaderStack stack) throws IOException {
		super(fn, idx, stack);
		vannoFile = new MyBlockCompressedInputStream(SeekableStreamFactory.getInstance().getBufferedStream(SeekableStreamFactory.getInstance().getStreamFor(vannoFn)));
		if(stack.getResultProcessor() instanceof VannoResultProcess) {
			((VannoResultProcess)stack.getResultProcessor()).setMFP(this.mFp);
		}
	}
	
	@Override
	public void initForChr(final String chr) throws IOException {
		int tid = chr2tid(chr);
		bins = null;
		if(tid != -1) {    
			bins = readIndex(tid, vannoFile);
			if(bins == null) return;

			isBlockStart = true;
			index = 0;
			bin = bins.get(index);
			
			it = new MixVannoIteratorImpl(vannoFile);
			stack.clearST();
			stack.setIterator(it);
		}
	}
	
	
	@Override
	public void doQuery(final Node query) throws IOException {
		if(bins == null) return;
		
		while(index < bins.size()) {
			 if(query.end < bin.getMin()) {
				 stack.findOverlapInST(query);
				 return;
			 } else {
				 if(query.beg <= bin.getMax()) {
					 if(isBlockStart) {
						 isBlockStart = false;
						 it.seek(bin.getFilePointer());
						 it.clear();
					 }
					 break;
				 }
				
				 isBlockStart = true;
				 index++;
				 if(index == bins.size()) break;
				 bin = bins.get(index);
			 }
		}
		stack.findOverlap(query);
	}
	
	public final class MixVannoIteratorImpl extends VannoIteratorImpl {
		
		public MixVannoIteratorImpl(final MyBlockCompressedInputStream _is) {
		    super(_is);
	    }

    	public void clear() {
    		block = null;
		}
    	
		@Override
		public void processBlock(final byte flag) throws IOException {
			if(block != null) {
				 index++;
				 if(index < bins.size()) {
					 bin = bins.get(index);
					 isBlockStart = false;
				 }
			 }
			 block = new BlockFeature(is, bin.getMin(), flag);
			 vannoFeature = block.makeFirstVannoFeature();
		}
	}
	
    @Override
    public String toString() {
        return "MixVannoReader: filename:"+getSource();
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
