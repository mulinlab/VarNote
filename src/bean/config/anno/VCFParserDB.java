package bean.config.anno;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import htsjdk.samtools.util.StringUtil;
import htsjdk.variant.vcf.VCFInfoHeaderLine;
import bean.config.AnnoDBBean;
import bean.node.VCFNode;

public class VCFParserDB extends AbstractVCFParser{

	private final AnnoDBBean db;
	private Map<String, VCFInfoHeaderLine> fieldInfoMap;
	private List<String> fields;
	private Map<String, FieldVal> fieldVals;
	private final boolean isForceOverlap;
	public VCFParserDB(final AnnoDBBean db, final boolean isForceOverlap) {
		super(db.getFormat(), db.getVcfHeaderPath());
		this.db = db;
		this.isForceOverlap = isForceOverlap;
		
		fields = new ArrayList<String>();
		fieldInfoMap = new HashMap<String, VCFInfoHeaderLine>();

		if(vcfHeader != null) {
			if(db.isAll()) {
				for (VCFInfoHeaderLine info1 : vcfHeader.getInfoHeaderLines()) {
					addField(info1.getID(), info1);
				}
			} else if(db.isExclude()) {
				for (VCFInfoHeaderLine info1 : vcfHeader.getInfoHeaderLines()) {
					if(!db.getFields().contains(info1.getID())) {
						addField(info1.getID(), info1);
					}
				}
			} else {
				VCFInfoHeaderLine info = null;
				for (String field : db.getFields()) {
					info = vcfHeader.getInfoHeaderLine(field);
					if(info == null) throw new IllegalArgumentException("We can't find vcf info for " + field);
					addField(field, info);
				}
			}
		}
		isRefAndAltExsit = true;
		
		fieldVals = new HashMap<String, FieldVal>();
		for (String field : fields) {
			fieldVals.put(field, new FieldVal(field, fieldInfoMap.get(field), db.getFormatForField(field)));
		}
	}
	
	public void addField(final String field, final VCFInfoHeaderLine info) {
		fields.add(field);
		fieldInfoMap.put(field, info);
	}
	
	public void emptyNodes(final String[] query_alts) {
		if(isForceOverlap) {
			for (String string : fieldVals.keySet()) {
				fieldVals.get(string).initForceOverlap();
			}
		} else {
			for (String string : fieldVals.keySet()) {
				fieldVals.get(string).init(query_alts);
			}
		}
	}
	
	public void addNode(final VCFNode node) {
		Map<String, String> fieldMap = allInfoToMap(node.info);
		if(isForceOverlap) {
			for (String field : fields) {
				fieldVals.get(field).addDBValForceoverlap(fieldMap.get(field));
			}
		} else {
			for (String field : fields) {
				fieldVals.get(field).addDBVal(node.alts, fieldMap.get(field));
			}
		}
		
	}
	

//	public Map<String, List<String>> nodesToFieldVals() {
//		Map<String, List<String>> fieldValuesMap = new HashMap<String, List<String>>();
//		
//		Map<String, String> fieldMap;
//		List<String> vals;
//		for (VCFNode node : nodes) {
//			fieldMap = allInfoToMap(node.info);
//			
//			for (String field : fields) {
//				if(fieldValuesMap.get(field) == null) {
//					vals = new ArrayList<String>();
//				} else {
//					vals = fieldValuesMap.get(field);
//				}
//				if(fieldMap.get(field) != null) vals.add(fieldMap.get(field));
//				fieldValuesMap.put(field, vals);
//			}
//		}
//		return fieldValuesMap;
//	}

	public String convertDBNodesToVCF() {
		if(fields.size() == 0) return null;
		else return StringUtil.join(INFO_FIELD_SEPARATOR, multiMapToList(fieldVals, fields, db.getOut_names()));
	}
	
	public String convertDBNodesToBED() {
		if(fields.size() == 0) return null;
		return StringUtil.join(TAB, multiMapToList(fieldVals, fields, null));
	}

	public String toBEDHeader() {
		if(fields.size() == 0) return null;
		return StringUtil.join(TAB, changeFiledName(fields, db.getOut_names()));
	}
	
	public List<VCFInfoHeaderLine> getVCFInfos() {
		return getVCFInfos(fields, db.getOut_names(), fieldInfoMap);
	}
}
