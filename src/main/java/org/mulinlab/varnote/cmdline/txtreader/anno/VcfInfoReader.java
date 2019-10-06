package org.mulinlab.varnote.cmdline.txtreader.anno;

import htsjdk.variant.vcf.VCFHeaderLineCount;
import htsjdk.variant.vcf.VCFHeaderLineType;
import htsjdk.variant.vcf.VCFInfoHeaderLine;
import org.mulinlab.varnote.cmdline.txtreader.abs.AbstractQueryReader;
import org.mulinlab.varnote.exceptions.InvalidArgumentException;
import org.mulinlab.varnote.utils.VannoUtils;

import java.util.HashMap;
import java.util.Map;


public final class VcfInfoReader<T> extends AbstractQueryReader<T> {

	public static final String ID = "id";
	public static final String COUNT = "count";
	public static final String TYPE = "type";
	public static final String DESC = "descrition";
	public static final String BEGIN = "@";
	public static final String COMMENT = AbstractQueryReader.COMMENT_LINE;
	
	private String currentField;
	private VCFHeaderLineType type;
	private VCFHeaderLineCount count;
	private String desc;
	private Map<String, VCFInfoHeaderLine> infoMap;
	
	public void init() {
		infoMap = new HashMap<String, VCFInfoHeaderLine>();
	}
	
	@Override
	public boolean filterLine(String line) {
		if(line.startsWith(BEGIN)) {
			if(currentField != null) {
				infoMap.put(currentField, new VCFInfoHeaderLine(currentField, count, type, desc));
			}
			currentField = line.substring(1);
			if(currentField.trim().equals("")) throw new InvalidArgumentException("Field name is empty, please check filed name starts with @");
			desc = "";
			count = VCFHeaderLineCount.UNBOUNDED;
			type = VCFHeaderLineType.String;
			
			return true;
		} else if(line.equals("") || line.startsWith(COMMENT_LINE)) {
			return true;
		} else {
			return false;
		}
	}

	@Override
	public void putValue(String key, String val) {
		if(currentField == null) throw new InvalidArgumentException("Please define field name begin with @ first.");
	
		switch (key) {
			case COUNT:
				count = VannoUtils.checkCount(val);
				break;
			case TYPE:
				type = VannoUtils.checkType(val);
				break;
			case DESC:
				desc = val;
				break;
			default:
				break;
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public T doEnd() {
		if(currentField != null) {
			infoMap.put(currentField, new VCFInfoHeaderLine(currentField, count, type, desc));
		}
		return (T)infoMap;
	}

	public static void main(String[] args) {
		VcfInfoReader<Map<String, VCFInfoHeaderLine>> readFromConfig = new VcfInfoReader<Map<String, VCFInfoHeaderLine>>();
		Map<String, VCFInfoHeaderLine> map = readFromConfig.read("/Users/mulin/Desktop/config/t100k.vcfinfo");
		for (String string : map.keySet()) {
			System.out.println(string + "=" + map.get(string));
		}
	}
}
