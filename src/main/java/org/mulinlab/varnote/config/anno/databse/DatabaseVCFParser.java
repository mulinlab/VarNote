package org.mulinlab.varnote.config.anno.databse;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import htsjdk.variant.vcf.VCFHeader;
import htsjdk.variant.vcf.VCFHeaderLineCount;
import htsjdk.variant.vcf.VCFHeaderLineType;
import htsjdk.variant.vcf.VCFInfoHeaderLine;
import org.mulinlab.varnote.config.anno.InfoField;
import org.mulinlab.varnote.config.anno.VCFParser;
import org.mulinlab.varnote.config.anno.ab.AbstractDatababseParser;
import org.mulinlab.varnote.config.anno.ab.ExtractValue;
import org.mulinlab.varnote.utils.enumset.AnnoOutFormat;
import org.mulinlab.varnote.utils.database.Database;
import org.mulinlab.varnote.utils.format.Format;
import org.mulinlab.varnote.utils.node.RefNode;

import org.mulinlab.varnote.exceptions.InvalidArgumentException;
import org.mulinlab.varnote.utils.VannoUtils.FileType;


public class DatabaseVCFParser extends AbstractDatababseParser{
	private final VCFParser vcfParser;
	private List<String> infoFieldsToExtract;
	private Map<String, InfoField> infoFieldMap;
	
	public DatabaseVCFParser(final Database db,  final DatabseAnnoConfig config, final boolean isForceOverlap, final AnnoOutFormat annoOutFormat) {
		super(db, config, isForceOverlap, annoOutFormat);
		
		vcfParser = new VCFParser(db.getDbPath(), FileType.BGZ);
		infoFieldsToExtract = new ArrayList<>();
		
		Integer infoColumn = new Integer(format.getFieldCol(Format.H_FIELD.INFO.toString()));
		if(config.getInfoToExtract() == null || config.getInfoToExtract().size() == 0) {
			if(columnsToExtract.contains(infoColumn)) {
				columnsToExtract.remove(infoColumn);
				for (VCFInfoHeaderLine info : vcfParser.getVcfHeader().getInfoHeaderLines()) {
					infoFieldsToExtract.add(info.getID());
				}
			}
		} else {
			columnsToExtract.remove(infoColumn);
			if(config.isExcludeInfoFields()) {
				for (VCFInfoHeaderLine info : vcfParser.getVcfHeader().getInfoHeaderLines()) {
					if(!config.getInfoToExtract().contains(info.getID())) {
						infoFieldsToExtract.add(info.getID());
					}
				}
			} else {
				VCFInfoHeaderLine info = null;
				for (String infoField : config.getInfoToExtract()) {
					info = vcfParser.getVcfHeader().getInfoHeaderLine(infoField);
					if(info == null) throw new InvalidArgumentException("We can't find VCF info for " + infoField + " in VCF header.");
					infoFieldsToExtract.add(info.getID());
				}
			}
		} 
		
		
		vcfInfoMap = vcfParser.getInfoMap();
		for (Integer col : columnsToExtract) {  
			vcfInfoMap.put(format.getColField(col), new VCFInfoHeaderLine(format.getColField(col), VCFHeaderLineCount.UNBOUNDED, VCFHeaderLineType.String, ""));
		}
		
		infoFieldMap = new HashMap<String, InfoField>();
		for (String field : infoFieldsToExtract) {
			infoFieldMap.put(field, new InfoField(vcfInfoMap.get(field), config.getFormatForField(field)));
		}
	}

	@Override
	public List<String> addExtractHeader(List<String> headerList) {
		headerList = addFieldsToHeader(headerList);
		
		if(infoFieldsToExtract.size() > 0) {
			for (String infoID : infoFieldsToExtract) {
				if(out_names.get(infoID) == null) {
					headerList.add(db.getConfig().getOutName() + UNDERLINE + infoID);
				} else {
					headerList.add(out_names.get(infoID));
				}
			}
		}
		return headerList;
	}
	
	@Override
	public void addNode(final RefNode query, final RefNode node) {
		super.addNode(query, node);
		Map<String, String> fieldToValMap = vcfParser.infoToMap(node.getField( format.getFieldCol(Format.H_FIELD.INFO.toString() )));

		if(isForceOverlap) {
			for (String field : infoFieldsToExtract) {
				infoFieldMap.get(field).addDBVal(fieldToValMap.get(field));
			}
		} else {
			for (String field : infoFieldsToExtract) {
				infoFieldMap.get(field).addDBVal(node.alts, fieldToValMap.get(field));
			}
		}
	}

	@Override
	public ExtractValue extractFieldsValueToVCF(final RefNode query, final List<String> dbNodeList) {
		extractFieldsValue(query, dbNodeList);
		ExtractValue fields = combineNormalFields(false);
		return combineInfoFields(false, fields);
	}

	@Override
	public ExtractValue extractFieldsValueToBED(final RefNode query, final List<String> dbNodeList) {
		extractFieldsValue(query, dbNodeList);
		ExtractValue fields = combineNormalFields(true);
		return combineInfoFields(true, fields);
		 //StringUtil.join(TAB, fields);
	}
	
	@Override
	public void emptyNodes(final String[] query_alts) {
		super.emptyNodes(query_alts);
		if(isForceOverlap) {
			for (String string : infoFieldMap.keySet()) {
				infoFieldMap.get(string).initNodeList();
			}
		} else {
			for (String string : infoFieldMap.keySet()) {
				infoFieldMap.get(string).init(query_alts);
			}
		}
	}
	
	@Override
	public VCFHeader addVCFHeader(VCFHeader header) {
		header = super.addVCFHeader(header);
		
		VCFInfoHeaderLine info;
		String outName;
		
		for (String field : infoFieldsToExtract) {  //remove info
			outName = out_names.get(field);
			info = vcfInfoMap.get(field);
		
			if(outName != null)  info = new VCFInfoHeaderLine(outName, info.getCountType(), info.getType(), info.getDescription());
			header.addMetaDataLine(info);
		}
		return header;
	}
	
	public ExtractValue combineInfoFields(final boolean bed, ExtractValue out) {

		out.setClo(out.getClo() + infoFieldsToExtract.size());
		
		String val;
		
		for (String key : infoFieldsToExtract) {
			val = infoFieldMap.get(key).nodesTostr();
			
			if(bed) { 
				out.add(val); 
			} else if(!val.equals("")){
				if(infoFieldMap.get(key).isFlag()) {
					out.add(key);
				}  else {
					if(out_names.get(key) != null) {
						out.add(out_names.get(key) + VCF_INFO_EQUAL + val);
					} else {
						out.add(db.getConfig().getOutName() + UNDERLINE + key + VCF_INFO_EQUAL + val);
					}
				}
			}
		}
		return out;
	}
	
	public int getExtractCol() {
		return columnsToExtract.size() + infoFieldsToExtract.size();
	}
}
