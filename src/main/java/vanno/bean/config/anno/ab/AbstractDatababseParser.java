package main.java.vanno.bean.config.anno.ab;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import htsjdk.samtools.util.StringUtil;
import htsjdk.variant.vcf.VCFHeader;
import htsjdk.variant.vcf.VCFInfoHeaderLine;
import main.java.vanno.bean.config.anno.NormalField;
import main.java.vanno.bean.config.anno.databse.DatabseAnnoConfig;
import main.java.vanno.bean.config.run.AnnoRunConfig.AnnoOutFormat;
import main.java.vanno.bean.database.Database;
import main.java.vanno.bean.database.Database.IndexType;
import main.java.vanno.bean.database.index.vannoIndex.VannoIndex;
import main.java.vanno.bean.node.NodeFactory;
import main.java.vanno.bean.node.RefNode;
import main.java.vanno.constants.BasicUtils;
import main.java.vanno.constants.InvalidArgumentException;
import main.java.vanno.constants.VannoUtils.FileType;

public abstract class AbstractDatababseParser extends AbstractParser{
	protected List<Integer> columnsToExtract;
	protected Map<Integer, NormalField> normalFieldMap;
	protected Database db;
	protected AnnoOutFormat annoOutFormat;
	protected boolean isForceOverlap;
	protected DatabseAnnoConfig config;
	protected Map<String, String> out_names;
	protected Map<String, VCFInfoHeaderLine> vcfInfoMap;
	protected List<String> log;

	public AbstractDatababseParser(final Database db,  final DatabseAnnoConfig config, final boolean isForceOverlap, final AnnoOutFormat annoOutFormat) {
		super();
		this.db = db;
		this.format = db.getFormat();
		this.isForceOverlap = isForceOverlap;
		this.config = config;
		this.log = new ArrayList<String>();
		this.annoOutFormat = annoOutFormat;
		init();
	}
	
	public void init() {
		if(config.getCommentIndicator() != null) this.format.setCommentIndicator(config.getCommentIndicator()); 
//		if(config.getHeaderIndicator() != null) this.format.setHeaderIndicator(config.getHeaderIndicator()); 
		if(config.isHasHeader()) this.format.setHasHeader(true); 
		if(config.getHeaderPath() != null) this.format.setHeaderPath(config.getHeaderPath());
		if(this.db.getConfig().getIndexType() == IndexType.VANNO) {
			this.format.setField(((VannoIndex)this.db.getIndex()).getHeaderColList());
		} 
		
		format.checkOverlap(db.getDbPath(), FileType.BGZ);
		format.checkRefAndAlt();
		
		if(!format.isRefAndAltExsit()) {
			this.log.add(BasicUtils.KRED + "Note: REF or ALT column is not defined, we will perform the annotation without REF and ALT." + BasicUtils.KNRM);
		}
		
		columnsToExtract = new ArrayList<Integer>();
		List<String> colNames = format.getConvertField();
		
		if(config.isAll()) {
			for (int i = 0; i < colNames.size(); i++) {
				columnsToExtract.add(i+1);
			}
		} else {
			if(config.isExcludeFields()) {
				if(config.getFieldsUppercase().size() > 0) {
					for (int i = 0; i < colNames.size(); i++) {
						if(!config.getFieldsUppercase().contains(colNames.get(i).toUpperCase())) columnsToExtract.add(i+1);
					}
				} else if(config.getCols().size() > 0) {
					for (int i = 0; i < colNames.size(); i++) {
						if(!config.getCols().contains(i + 1)) columnsToExtract.add(i + 1);
					}
				}
			} else {
				if(config.getFieldsUppercase().size() > 0) {
					for (String field : config.getFieldsUppercase()) {
						if(format.getFieldCol(field) > -1) {
							columnsToExtract.add(format.getFieldCol(field));
						} else {
							throw new InvalidArgumentException("We can't find field " + field + " in database file: " + db.getDbPath() + ". Availuable columns are " + StringUtil.join(",", colNames));
						}
					}
				} else if(config.getCols().size() > 0) {
					for (int col : config.getCols()) {
						if((col-1) < colNames.size()) columnsToExtract.add(col);
						else throw new InvalidArgumentException("col " + col + " is out of size of database file: " + db.getDbPath());
					} 
				}
			}
		}
		config.resetOut_names(colNames);
		this.out_names = config.getOut_names();
		
		normalFieldMap = new HashMap<Integer, NormalField>();
		for (Integer col : columnsToExtract) {  //remove info
			normalFieldMap.put(col, new NormalField(config.getFormatForField( format.getColField(col))));
		}
	}
	
	public abstract List<String> addExtractHeader(final List<String> headerList);
	public abstract ExtractValue extractFieldsValueToVCF(final RefNode query, final List<String> dbNodeList);
	public abstract ExtractValue extractFieldsValueToBED(final RefNode query, final List<String> dbNodeList);
	
	public List<String> addFieldsToHeader(List<String> headerList) {
		if(columnsToExtract.size() > 0) {
			for (Integer col : columnsToExtract) {
				if(out_names.get(format.getColField(col)) == null) {
					headerList.add(db.getConfig().getOutName() + UNDERLINE + format.getColOriginalField(col));
				} else {
					headerList.add(out_names.get(format.getColField(col)));
				}
			}
		}
		return headerList;
	}
	
	public void extractFieldsValue(final RefNode query, final List<String> dbNodeList) {
		emptyNodes(query.alts);
		RefNode refNode;
		
		if(dbNodeList != null)
		for (String nodeStr : dbNodeList) {
			refNode = NodeFactory.createRefAlt(nodeStr, db.getFormat());
			if(checkQueryAndDBNodeMatch(query, refNode)) {
				addNode(query, refNode);
			}
		}
	}
	
	public void emptyNodes(final String[] query_alts) {
		for (Integer col : normalFieldMap.keySet()) {
			normalFieldMap.get(col).initNodeList();
		}
	}
	
	public boolean checkQueryAndDBNodeMatch(final RefNode query, final RefNode db) {
		if(isForceOverlap || !format.isRefAndAltExsit()) return true;
		else {
			boolean flag = true;
			if(!query.ref.equals(db.ref)) {
				flag = false;
			} else {
				if(query.alts.length != db.alts.length) {
					flag = false;
				} else {
					for (int i = 0; i < query.alts.length; i++) {
						if(!query.alts[i].equals(db.alts[i])) {
							flag = false;
							break;
						}
					}
				}
			}
			return flag;
		}
	}
	
	public void addNode(final RefNode query, final RefNode node) {
		for (Integer col : columnsToExtract) {
			normalFieldMap.get(col).addDBVal(node.getField(col));
		}
	}
	
	public ExtractValue combineNormalFields(final boolean bed) {
		ExtractValue e = new ExtractValue(bed);
		e.setClo(columnsToExtract.size());
		
		String val;
		for (Integer col : columnsToExtract) {
			val = normalFieldMap.get(col).nodesTostr();

			if(bed) {
				e.add(val);
			} else {
				if(!val.equals("")) {
					if(out_names.get(format.getColField(col)) != null) {
						e.add(out_names.get(format.getColField(col)) + VCF_INFO_EQUAL + val);
					} else {
						e.add(db.getConfig().getOutName() + UNDERLINE + format.getColOriginalField(col) + VCF_INFO_EQUAL + val);
					}
				}
			}
		}
		return e;
	}

	public VCFHeader addVCFHeader(VCFHeader header) {
		VCFInfoHeaderLine info;
		String outName, field;
		for (Integer col : columnsToExtract) {  //remove info
			field = format.getColField(col);
			outName = out_names.get(field);
			info = vcfInfoMap.get(field);
		
			if(outName != null)  info = new VCFInfoHeaderLine(outName, info.getCountType(), info.getType(), info.getDescription());
			header.addMetaDataLine(info);
		}
		return header;
	}

	public List<String> getLog() {
		return log;
	}

	public DatabseAnnoConfig getConfig() {
		return config;
	}
	
	public int getExtractCol() {
		return columnsToExtract.size();
	}
}
