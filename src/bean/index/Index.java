package bean.index;

import htsjdk.samtools.seekablestream.ISeekableStreamFactory;
import htsjdk.samtools.seekablestream.SeekableStream;
import htsjdk.samtools.seekablestream.SeekableStreamFactory;
import htsjdk.samtools.util.BlockCompressedInputStream;
import htsjdk.tribble.index.tabix.TabixFormat;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import postanno.FormatWithRef;
import constants.BasicUtils;


public abstract class Index {
	protected int mPreset;
	protected int mSc;
	protected int mBc;
	protected int mEc;
	protected int mMeta;
	protected String[] mSeq;
	protected Map<String, Integer> mChr2tid;
	protected Map<Integer, String> mTid2chr;
	protected int mSkip = 0;
	protected final String mIdxFn;
	protected Map<Integer, Long> minOffForChr;
	protected TabixFormat format;
	protected FormatWithRef formatWithRef;
	
	public Map<String, Integer> getmChr2tid() {
		return mChr2tid;
	}
	
	public Index(final String indexPath) {
		this.mIdxFn = indexPath;
		this.minOffForChr = new HashMap<Integer, Long>();
	}
	
	public void readFile() {
        ISeekableStreamFactory ssf = SeekableStreamFactory.getInstance();
        SeekableStream fp;
		try {
			fp = ssf.getBufferedStream(ssf.getStreamFor(mIdxFn), 128000);
			if (fp == null)  return ;
			
			BlockCompressedInputStream is = new BlockCompressedInputStream(fp);
			readFile(is);
	        is.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void readFile(BlockCompressedInputStream is) throws IOException {
		readMagic(is);
        readFormat(is);
        readSeq(is);
        readIndex(is);
        format = new TabixFormat(this.mPreset, this.mSc, this.mBc, this.mEc, (char)this.mMeta, this.mSkip);
	}

	public TabixFormat getFormat() {
		return format;
	}
	
	public FormatWithRef getFormatWithRef() {
		return formatWithRef;
	}
	
	protected abstract void readMagic(BlockCompressedInputStream is) throws IOException;

	protected void readFormat(BlockCompressedInputStream is) throws IOException {
		mSeq = new String[readInt(is)]; // # sequences
		mChr2tid = new HashMap<String, Integer>();
		mTid2chr = new HashMap<Integer, String>();
		mPreset = readInt(is);
		mSc = readInt(is);
		mBc = readInt(is);
		mEc = readInt(is);

		mMeta = readInt(is);
		mSkip = readInt(is); // read skip
	}

	protected void readSeq(BlockCompressedInputStream is) throws IOException {
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
				if (s.toLowerCase().startsWith("chr")) {
					mChr2tid.put(s.substring(3), k);
				} else {
					mChr2tid.put("chr" + s, k);
					mChr2tid.put("Chr" + s, k);
					mChr2tid.put("CHR" + s, k);
				}
				mTid2chr.put(k, s);
				mSeq[k++] = s;
				j = i + 1;
			}
		}
	}

	protected abstract void readIndex(BlockCompressedInputStream is) throws IOException;
	
	public static short readShort(final InputStream is) throws IOException {
		return BasicUtils.readShort(is);
	}
    
    public static int readInt(final InputStream is) throws IOException {
    	return BasicUtils.readInt(is);
	}

	public static long readLong(final InputStream is) throws IOException {
		return BasicUtils.readLong(is);
	}
	
	   /** return chromosome ID or -1 if it is unknown */
    public int chr2tid(final String chr) {
        Integer tid = this.mChr2tid.get(chr);
        return tid == null ? -1 : tid;
    }
    
    public String tid2chr(final int tid) {
        return this.mTid2chr.get(tid);
    }
    
    /** return the chromosomes in that tabix file */
    public Set<String> getChromosomes()
    {
        return Collections.unmodifiableSet(this.mChr2tid.keySet());
    }
    
    /**
     * Parse a region in the format of "chr1", "chr1:100" or "chr1:100-1000"
     *
     * @param reg Region string
     * @return An array where the three elements are sequence_id,
     *         region_begin and region_end. On failure, sequence_id==-1.
     */
    public int[] parseReg(final String reg) { // FIXME: NOT working when the sequence name contains : or -.
        String chr;
        int colon, hyphen;
        int[] ret = new int[3];
        colon = reg.indexOf(':');
        hyphen = reg.indexOf('-');
        chr = colon >= 0 ? reg.substring(0, colon) : reg;
        ret[1] = colon >= 0 ? Integer.parseInt(reg.substring(colon + 1, hyphen >= 0 ? hyphen : reg.length())) - 1 : 0;
        ret[2] = hyphen >= 0 ? Integer.parseInt(reg.substring(hyphen + 1)) : 0x7fffffff;
        ret[0] = this.chr2tid(chr);
        return ret;
    }

	public int getmMeta() {
		return mMeta;
	}
    
	public long getMinOffForChr(int tid) {
		return minOffForChr.get(tid);
	}

	public String[] getmSeq() {
		return mSeq;
	}
	
	public int getmSeqLen() {
		return mSeq.length;
	}
}
