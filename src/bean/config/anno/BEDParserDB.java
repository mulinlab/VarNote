package bean.config.anno;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import htsjdk.samtools.util.StringUtil;
import htsjdk.variant.vcf.VCFHeaderLineCount;
import htsjdk.variant.vcf.VCFHeaderLineType;
import htsjdk.variant.vcf.VCFInfoHeaderLine;
import bean.config.AnnoDBBean;
import bean.node.BedNode;

public class BEDParserDB extends AbstractBedParser{

	private final AnnoDBBean db;
	private List<String> extractFields;
	private Map<String, Integer> extractFieldsCol;
	private Map<String, FieldVal> fieldVals;
	
	private Map<String, VCFInfoHeaderLine> fieldInfoMap;
	private final boolean isForceOverlap;
	
	public BEDParserDB(final AnnoDBBean db, final List<String> columnNames, final boolean isForceOverlap) {
		super(db.getFormat(), columnNames);
		this.db = db; 
		this.isForceOverlap = isForceOverlap;
		init(db);
	}
	
	public BEDParserDB(final AnnoDBBean db, final File file, final boolean isForceOverlap) {
		super(db.getFormat(), file);
		this.db = db; 
		this.isForceOverlap = isForceOverlap;
		init(db);
	}
	
	public void init(final AnnoDBBean db) {	
		checkRefAndAlt(fields);
		
		if(db.isAll()) {
			extractFields = fields;
			extractFieldsCol = fieldToCol;
		} else {
			extractFields = new ArrayList<String>();
			extractFieldsCol = new HashMap<String, Integer>();
			if(db.isExclude()) {
				if(db.getFields().size() > 0) {
					for (int i = 0; i < fields.size(); i++) {
						if(!db.getFields().contains(fields.get(i))) addField(fields.get(i), i);
					}
				} else if(db.getCols().size() > 0) {
					for (int i = 0; i < fields.size(); i++) {
						if(!db.getCols().contains(i)) addField(fields.get(i), i);
					}
				}
			} else {
				if(db.getFields().size() > 0) {
					for (String str : db.getFields()) {
						if(fields.contains(str)) addField(str, fields.indexOf(str));
						else throw new IllegalArgumentException("We can't find field " + str + " in database file: " + db.getDbPath());
					} 
				} else if(db.getCols().size() > 0) {
					for (int col : db.getCols()) {
						if(col < fields.size()) addField(fields.get(col), col);
						else throw new IllegalArgumentException("col " + col + " is out of size of database file: " + db.getDbPath());
					} 
				}
			}
		}
		db.resetOut_names(fields);
		
		fieldInfoMap = new HashMap<String, VCFInfoHeaderLine>();
		fieldVals = new HashMap<String, FieldVal>();
		
		VCFInfoHeaderLine info;
		for (String field : extractFields) {
			info = db.getInfoForField(field);
			if(info == null) {
				info = new VCFInfoHeaderLine(field, VCFHeaderLineCount.UNBOUNDED, VCFHeaderLineType.String, "");
				System.out.println("VCF Info information is not find for field " + field + ", we will use " + info + " instead.");
			}
			System.out.println();
			
			fieldInfoMap.put(field, info);
			fieldVals.put(field, new FieldVal(field, info, db.getFormatForField(field)));
		}
	}
	
	public void addField(final String field, final int col) {
		extractFields.add(field);
		extractFieldsCol.put(field, col);
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
	
	public void addNode(final BedNode node) {
		if(isForceOverlap) {
			for (String field : extractFields) {
				fieldVals.get(field).addDBValForceoverlap(node.allFieldsMap.get(extractFieldsCol.get(field)));
			}
		} else {
			for (String field : extractFields) {
				fieldVals.get(field).addDBVal(node.alts, node.allFieldsMap.get(extractFieldsCol.get(field)));
			}
		}
	}

	public String convertDBNodesToVCF() {
		if(extractFields.size() == 0) return null;
		return StringUtil.join(INFO_FIELD_SEPARATOR, multiMapToList(fieldVals, extractFields, db.getOut_names()));
	}
	
	public String convertDBNodesToBED() {
		if(extractFields.size() == 0) return null;
		return StringUtil.join(TAB, multiMapToList(fieldVals, extractFields, null));
	}

	public String toBEDHeader() {
		if(extractFields.size() == 0) return null;
		return StringUtil.join(TAB, changeFiledName(extractFields, db.getOut_names()));
	}

	public List<VCFInfoHeaderLine> getVCFInfos() {
		return getVCFInfos(extractFields, db.getOut_names(), fieldInfoMap);
	}
}
