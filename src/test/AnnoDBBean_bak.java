package test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import bean.config.AnnoFieldBean_bak;
import postanno.FormatWithRef;
import htsjdk.variant.vcf.VCFConstants;
import htsjdk.variant.vcf.VCFHeader;
import htsjdk.variant.vcf.VCFHeaderLineType;
import htsjdk.variant.vcf.VCFInfoHeaderLine;


public class AnnoDBBean_bak {
	private String label;

	private List<String> keys;
//	private List<String> outNamekeys;
	private Map<String, AnnoFieldBean_bak> fields;
	private boolean isAll;
	private List<AnnNode> results;
	private VCFHeader vcfHeader;
	private Map<String, Integer> bedFieldsMap; 
	private List<String> bedFieldKeys;
	private FormatWithRef dbFormat;
	
	public AnnoDBBean_bak() {
		super();
		fields = new HashMap<String, AnnoFieldBean_bak>();
		keys = new ArrayList<String>();
//		outNamekeys = new ArrayList<String>();
		isAll = false;
	}
	
	public Map<String, Integer> getBedFieldsMap() {
		return bedFieldsMap;
	}

	public void setBedFields(String header) {
		if(header == null) return;
		if(header.equals("")) throw new IllegalArgumentException("Please check whether your header set for " + label + " is correct? ");
		bedFieldKeys = ParseConfigFile.fieldsStrToList(header);
		bedFieldsMap = ParseConfigFile.fieldsStrToMap(header, false);
		formatWithRef = FormatWithRef.BED;
		formatWithRef = ParseConfigFile.setFormat(formatWithRef, bedFieldsMap);
	}

	public FormatWithRef getFormatWithRef() {
		return formatWithRef;
	}

	public void setFormatWithRef(FormatWithRef formatWithRef) {
		this.formatWithRef = formatWithRef;
	}

	public void checkConfig() {
		if(label == null) {
			throw new IllegalArgumentException("Please set up db label for each [anno].");
		}
		
		if(dbFormat == null) throw new IllegalArgumentException("Please set up db format for " + label + " .");
		if(fields.keySet().size() == 0) {
			System.out.println("You did not define any field for db " + label + ", we will extract all information.");
			isAll = true;
		} else {
			for (AnnoFieldBean_bak field : fields.values()) {
				field.checkName();
			}
		}
	}
	
	public void checkBed() {
		if(bedFieldKeys == null || bedFieldKeys.size() == 0) throw new IllegalArgumentException("Please set up header_fields for bed file " + label);

		for (AnnoFieldBean_bak field : fields.values()) {
			if(!bedFieldKeys.contains(field.getName())) throw new IllegalArgumentException ("We can't find Field " + field.getName() + " in DB " + label);
			field.setBedHeaderInfo(label);
		}
	}
	
	public void setVcfInfos() {
		VCFInfoHeaderLine info;
		for (AnnoFieldBean_bak field : fields.values()) {
			info = vcfHeader.getInfoHeaderLine(field.getName());
			if(info == null) throw new IllegalArgumentException("We can't find vcf info for " + field.getName());
			field.setVcfInfo(info);
		}
	}

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	public void addField(AnnoFieldBean_bak field) {
		field.checkConfig(label);
		keys.add(field.getName());
		fields.put(field.getName(), field);
//		outNamekeys.add((field.getOutName() == null) ? field.getName() : field.getOutName());
	}
	
	public boolean containsField(String name) {
		return keys.contains(name);
	}

	public VCFHeader getVcfHeader() {
		return vcfHeader;
	}

	public FileFormatSupport getDbFormat() {
		return dbFormat;
	}

	public void setDbFormat(String dbFormat) {
		this.dbFormat = ParseConfigFile.checkFileFormat(dbFormat);
	}
	
	public static boolean hasIntersection(List<String> a, String[] b) {
		for (int i = 0; i < b.length; i++) {
			if(a.contains(b[i])) {
				return true;
			}
		}
		return false;
	}

	public List<AnnNode> getResult(NodeWithRefAlt query, NodeWithRefAlt db, boolean forceOverlap) {
		results = new ArrayList<AnnoDBBean_bak.AnnNode>();
		
		//如果ref对不上或者alt全部对不上，并且不是forceOverlap的时候 直接返回null
		if ((!query.ref.equals(db.ref) || !hasIntersection(Arrays.asList(query.alts), db.alts)) && !forceOverlap) { //alts cannot match
			return null;
		}
		if(dbFormat == FileFormatSupport.VCF) {
			getFieldsValVCF(query, db, forceOverlap);
		} else {
			getFieldsValBED(query, db, forceOverlap);
		}
		return results;
	}
	
	public void getFieldsValBED(NodeWithRefAlt query, NodeWithRefAlt db, boolean forceOverlap) {
		String val, name;
		AnnoFieldBean_bak field;
	
		for (String key : keys) {
			field = fields.get(key);
			val = db.colToValMap.get(bedFieldsMap.get(key) - 1);
			if(val == null) throw new IllegalArgumentException("Can't find val for column " + bedFieldsMap.get(key));
			
			name = field.getOutName() != null ? field.getOutName() : field.getName();
			if(field.getVcfInfo().getType() == VCFHeaderLineType.Flag) {
				results.add(new AnnNode(name, name));
			} else {
				if(forceOverlap) results.add(new AnnNode(name, field.getValForForceOverlap(val, query, db)));
				else results.add(new AnnNode(name, field.getVal(val, query, db)));
			}
		}
	}
	
	public void getFieldsValVCF(NodeWithRefAlt query, NodeWithRefAlt db, boolean forceOverlap) {
		int beg, end;
		String val, info = db.info, name;
		AnnoFieldBean_bak field;
		for (String key : keys) {
			field = fields.get(key);
			name = field.getOutName() != null ? field.getOutName() : field.getName();
			beg = info.indexOf(field.getName());
			if(beg != -1) {
				if(field.getVcfInfo().getType() == VCFHeaderLineType.Flag) {
					results.add(new AnnNode(name, name));
				} else {
					beg = info.indexOf("=", beg);
					end = info.indexOf(VCFConstants.INFO_FIELD_SEPARATOR, beg);
					if(end == -1) {
						val = info.substring(beg + 1);
					} else {
						val = info.substring(beg + 1, end);
					}
					
					if(forceOverlap) results.add(new AnnNode(name, field.getValForForceOverlap(val, query, db)));
					else results.add(new AnnNode(name, field.getVal(val, query, db)));
				}
			} else {
				if(field.getVcfInfo().getType() == VCFHeaderLineType.Flag) {
					results.add(new AnnNode(name, null));
				} else {
					throw new IllegalArgumentException("Can't find field " + name + " in " + db.origStr);
				}
			}
		}
	}
	
	public void setVcfHeader(VCFHeader vcfHeader) {
		this.vcfHeader = vcfHeader;
	}

	public void setVcfInfo(String fieldName, VCFInfoHeaderLine vcfInfo) {
		fields.get(fieldName).setVcfInfo(vcfInfo);
	}
	
	public AnnoFieldBean_bak getField(String name) {
		return fields.get(name);
	}
	
	public class AnnNode {
		final String name,val;
		
		public AnnNode(final String name, final String val) {
			super();
			this.name = name;
			this.val = val;
		}	
		public String getName() {
			return name;
		}
		public String getVal() {
			return val;
		}
	}

	public boolean isAll() {
		return isAll;
	}

	public void setAll(String isAll) {
		this.isAll = ParseConfigFile.strToBool(isAll);
	}

	public List<String> getKeys() {
		return keys;
	}

//	public List<String> getOutNamekeys() {
//		return outNamekeys;
//	}

	public List<String> getBedFieldKeys() {
		return bedFieldKeys;
	}
}
