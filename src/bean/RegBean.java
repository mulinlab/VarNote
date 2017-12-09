package bean;

import java.util.List;


public class RegBean {
	private int level;
	private int bin;
	private List<Integer> bins;
	private int maxBinNum;
	
	public RegBean(final int bin, final int level) {
		super();
		this.bin = bin;
		this.level = level;
	}

	public int getLevel() {
		return level;
	}
	
	public int getBin() {
		return bin;
	}

	public void setLevel(int level) {
		this.level = level;
	}

	public void setBin(int bin) {
		this.bin = bin;
	}

	public List<Integer> getBins() {
		return bins;
	}

	public void setBins(List<Integer> bins) {
		this.bins = bins;
		this.bins.add(this.bin);
		this.maxBinNum = (this.bins.get(level - 1) + 1)*8;
		System.out.println(this.maxBinNum);
	}
	
	public void addBin(Integer bin) {
		this.bins.add(bin);
	}

	public int getMaxBinNum() {
		return maxBinNum;
	}
}
