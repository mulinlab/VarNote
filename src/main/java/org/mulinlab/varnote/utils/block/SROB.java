package org.mulinlab.varnote.utils.block;


import java.util.ArrayList;
import java.util.List;

import org.mulinlab.varnote.utils.node.LocFeature;

public class SROB {
	private long vannoFilePointer;
	private long bgzFilePointer;
	private int min;
	private int max;
	
	private List<Feature> blockFeature;
	private int preBeg;
	private int totalOffset;
	
	public SROB(final int min, final int max, final long vannoFilePointer, final long bgzFilePointer) { 
		super();
		initSROB(min, max, vannoFilePointer, bgzFilePointer);
	}
	
	public SROB(final long vannoFilePointer, final int min, final int max) { 
		super();
		setSROB(vannoFilePointer, min, max);
		this.blockFeature = null;
	}
	
	public void setSROB(final long vannoFilePointer, final int min, final int max) { 
		this.vannoFilePointer = vannoFilePointer;
		this.min = min;
		this.max = max;
	}
	
	public void initSROB(final int min, final int max, final long vannoFilePointer, final long bgzFilePointer) {
		this.vannoFilePointer = vannoFilePointer; 
		this.bgzFilePointer = bgzFilePointer;
		this.min = min;
		this.max = max;
		
		this.preBeg = min;	
		this.totalOffset = 0;
		
		this.blockFeature = new ArrayList<SROB.Feature>();
		blockFeature.add(new Feature(0, max - min, 0));
	}
	
	public void clear() {
		this.preBeg = 0;	
		this.totalOffset = 0;
		this.bgzFilePointer = 0;
		this.blockFeature = null;
	}
	
	public int getAVGOffset() {
		return totalOffset/blockFeature.size();
	}
	
	public void updateMax(final LocFeature feature, final int offset) {
		if(feature.end > this.max) this.max = feature.end;
		blockFeature.add(new Feature(feature.beg - this.preBeg, feature.end - feature.beg, offset));
		this.preBeg = feature.beg;
	}
	
	public int getMin() {
		return min;
	}

	public int getMax() {
		return max;
	}
	
	public long getVannoFilePointer() {
		return vannoFilePointer;
	}

	public void setVannoFilePointer(long vannoFilePointer) {
		this.vannoFilePointer = vannoFilePointer;
	}

	public void setMin(int min) {
		this.min = min;
	}

	public void setMax(int max) {
		this.max = max;
	}


	public class Feature {
		final int beg;
		final int end;
		final int offset;
		
		public Feature(final int beg, final int end, final int offset) {
			super();
			this.beg = beg;
			this.end = end;
			this.offset = offset;		
			totalOffset += offset;
		}

		public int getBeg() {
			return beg;
		}

		public int getEnd() {
			return end;
		}

		public int getOffset() {
			return offset;
		}
	}

	public List<Feature> getBlockFeature() { 
		return blockFeature;
	}
	
	public boolean hasFeature() {
		return blockFeature.size() > 0;
	}

	public long getBGZFilePointer() {
		return bgzFilePointer;
	}
}
