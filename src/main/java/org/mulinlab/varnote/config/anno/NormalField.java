package org.mulinlab.varnote.config.anno;

import htsjdk.samtools.util.StringUtil;
import java.util.ArrayList;
import java.util.List;


public class NormalField {
	public static final String COMMA = ",";
	
	protected List<String> valList;
	protected final FieldFormat format;
	protected boolean useList;
	
	public NormalField(final FieldFormat format) {
		super();
		this.format = format;
		initNodeList();
	}
	
	public void initNodeList() {
		valList = new ArrayList<String>();
		useList = true;
	}
	
	public void addDBVal(final String val) {

	
		if(val != null && !val.trim().equals("")) {
			if(format != null) {
				final String[] vals = val.split(COMMA);
				for (String str : vals) {
					ValWithAllele va = format.getFieldValWithFormat(str);
					if(va.allele != null) valList.add(va.getStrWithAllele());
					else valList.add(va.getStr());
				}
			} else {
				valList.add(val);
			}
		}
	}
	
	public String nodesTostr() {
		if(valList != null && valList.size() > 0) return StringUtil.join(COMMA, valList);
		else return "";
	}
}
