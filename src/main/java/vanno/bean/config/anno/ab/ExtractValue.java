package main.java.vanno.bean.config.anno.ab;

import java.util.ArrayList;
import java.util.List;

import main.java.vanno.constants.BasicUtils;

public final class ExtractValue {
	
	public static final String NULLVALUE = BasicUtils.NULLVALUE;
	
	private boolean hasValue;
	private List<String> val;
	private int clo;
	private boolean isBed;
	
	public ExtractValue(final boolean isBed) {
		super();
		val = new ArrayList<String>();
		clo = 0;
		hasValue = false;
		this.isBed = isBed;
	}


	public ExtractValue(boolean hasValue, List<String> val, int clo, final boolean isBed) {
		super();
		this.hasValue = hasValue;
		this.val = val;
		this.clo = clo;
		this.isBed = isBed;
	}


	public boolean isHasValue() {
		return hasValue;
	}


	public void setHasValue(boolean hasValue) {
		this.hasValue = hasValue;
	}


	public List<String> getVal() {
		return val;
	}


	public void setVal(List<String> val) {
		this.val = val;
	}


	public int getClo() {
		return clo;
	}


	public void setClo(int clo) {
		this.clo = clo;
	}
	
	public void add(final String e) {
		if(e.equals("")) {
			if(isBed) {
				this.val.add(".");
			} 
		} else {
			hasValue = true;
			this.val.add(e);
		}
		
	}
}
