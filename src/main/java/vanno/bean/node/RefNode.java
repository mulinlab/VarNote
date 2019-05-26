package main.java.vanno.bean.node;

import java.util.HashMap;
import java.util.Map;

public  class RefNode extends Node{
	public String ref;
	public String[] alts;
	
	private Map<Integer, String> colFieldMapOneBased;
	
	public String getField(final int col) {
		if(colFieldMapOneBased.get(col) == null) {
			return "NA";
		} else {
			return colFieldMapOneBased.get(col);
		}
	}

	public void putColFiled(final int col, final String field) {
		colFieldMapOneBased.put(col, field);
	}
	
	public void init() {
		colFieldMapOneBased = new HashMap<Integer, String>();
	}
}
