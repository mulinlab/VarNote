package bean.node;

import java.util.List;
import java.util.Map;

public class VCFNode extends RefNode{
	public String filter;
	public String qulity;
	public String info;
	
	public List<String> otherFields;
	public Map<Integer, String> otherFieldsMap;
	
	public List<String> infoFields;
	public Map<String, String> infoFieldsMap;
}
