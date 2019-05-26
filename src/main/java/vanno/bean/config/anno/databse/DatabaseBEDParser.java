package main.java.vanno.bean.config.anno.databse;

import java.util.HashMap;
import java.util.List;
import htsjdk.variant.vcf.VCFHeaderLineCount;
import htsjdk.variant.vcf.VCFHeaderLineType;
import htsjdk.variant.vcf.VCFInfoHeaderLine;
import main.java.vanno.bean.config.anno.ab.AbstractDatababseParser;
import main.java.vanno.bean.config.anno.ab.ExtractValue;
import main.java.vanno.bean.config.run.AnnoRunConfig.AnnoOutFormat;
import main.java.vanno.bean.database.Database;
import main.java.vanno.bean.node.RefNode;
import main.java.vanno.constants.BasicUtils;

public class DatabaseBEDParser extends AbstractDatababseParser {

	public DatabaseBEDParser(Database db, DatabseAnnoConfig config, boolean isForceOverlap, final AnnoOutFormat annoOutFormat) {
		super(db, config, isForceOverlap, annoOutFormat);
		
		if(!format.isRefAndAltExsit()) this.isForceOverlap = true;
		if(this.annoOutFormat == AnnoOutFormat.VCF) {
			vcfInfoMap = new HashMap<String, VCFInfoHeaderLine>();	
			VCFInfoHeaderLine info;
			for (Integer col : columnsToExtract) {  
				info = config.getInfoForField(format.getColField(col));
				
				if(info == null) {
					String id = "";
					if(out_names.get(format.getColField(col)) == null) {
						id = db.getConfig().getOutName() + UNDERLINE + format.getColOriginalField(col);
					} else {
						id = out_names.get(format.getColField(col));
					}
					info = new VCFInfoHeaderLine(id, VCFHeaderLineCount.UNBOUNDED, VCFHeaderLineType.String, "");
					this.log.add("VCF Info information is not find for field " + BasicUtils.KCYN + format.getColField(col) + BasicUtils.KNRM + ", we will use " + BasicUtils.KCYN + info + BasicUtils.KNRM + " instead.");
				}
				
				vcfInfoMap.put(format.getColField(col), info);
			}
		}
	}

	@Override
	public List<String> addExtractHeader(List<String> headerList) {
		return  addFieldsToHeader(headerList);
	}

	@Override
	public ExtractValue extractFieldsValueToVCF(RefNode query, List<String> dbNodeList) {
		extractFieldsValue(query, dbNodeList);
		return combineNormalFields(false); // StringUtil.join(VCFParser.INFO_FIELD_SEPARATOR, );
	}

	@Override
	public ExtractValue extractFieldsValueToBED(RefNode query, List<String> dbNodeList) {
		extractFieldsValue(query, dbNodeList);
		return combineNormalFields(true); //StringUtil.join(TAB, );
	} 
	
}
