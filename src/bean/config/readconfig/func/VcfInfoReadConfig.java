package bean.config.readconfig.func;

import htsjdk.variant.vcf.VCFHeaderLineCount;
import htsjdk.variant.vcf.VCFHeaderLineType;
import htsjdk.variant.vcf.VCFInfoHeaderLine;

import java.util.HashMap;
import java.util.Map;

import bean.config.readconfig.AbstractReadConfig;
import constants.VannoUtils;


public final class VcfInfoReadConfig<T> extends AbstractReadConfig<T>{

	public static final String ID = "id";
	public static final String COUNT = "count";
	public static final String TYPE = "type";
	public static final String DESC = "descrition";
	public static final String BEGIN = "@";
	public static final String COMMENT = AbstractReadConfig.COMMENT_LINE;
	
	private String currentField;
	private VCFHeaderLineType type;
	private VCFHeaderLineCount count;
	private String desc;
	
	@SuppressWarnings("unchecked")
	@Override
	public boolean filterLine(final T obj, String line) {
		
		if(line.startsWith(BEGIN)) {
			if(currentField != null) {
				Map<String, VCFInfoHeaderLine> infoMap = (Map<String, VCFInfoHeaderLine>)obj;
				infoMap.put(currentField, new VCFInfoHeaderLine(currentField, count, type, desc));
			}
			currentField = line.substring(1);
			if(currentField.trim().equals("")) throw new IllegalArgumentException("Field name is empty, please check filed name starts with @");
			desc = "";
			count = VCFHeaderLineCount.UNBOUNDED;
			type = VCFHeaderLineType.String;
			
			return false;
		} else if(line.equals("") || line.startsWith(COMMENT_LINE)) {
			return false;
		} else {
			return true;
		}
	}

	@Override
	public void putValue(T obj, String key, String val) {
		if(currentField == null) throw new IllegalArgumentException("Please define field name begin with @ first.");
	
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
	public void doEnd(T obj) {
		if(currentField != null) {
			Map<String, VCFInfoHeaderLine> infoMap = (Map<String, VCFInfoHeaderLine>)obj;
			infoMap.put(currentField, new VCFInfoHeaderLine(currentField, count, type, desc));
		}
	}
	
	public static void main(String[] args) {
		VcfInfoReadConfig<Map<String, VCFInfoHeaderLine>> readFromConfig = new VcfInfoReadConfig<Map<String, VCFInfoHeaderLine>>();
		Map<String, VCFInfoHeaderLine> map = readFromConfig.read(new HashMap<String, VCFInfoHeaderLine>(), "/Users/mulin/Desktop/config/t100k.vcfinfo");
		for (String string : map.keySet()) {
			System.out.println(string + "=" + map.get(string));
		}
	}
}
