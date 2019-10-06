package org.mulinlab.varnote.config.anno.databse;

import java.util.HashMap;
import java.util.List;
import htsjdk.variant.vcf.VCFHeaderLineCount;
import htsjdk.variant.vcf.VCFHeaderLineType;
import htsjdk.variant.vcf.VCFInfoHeaderLine;
import org.mulinlab.varnote.config.anno.ab.AbstractDatababseParser;
import org.mulinlab.varnote.config.anno.ab.ExtractValue;
import org.mulinlab.varnote.utils.enumset.AnnoOutFormat;
import org.mulinlab.varnote.constants.GlobalParameter;
import org.mulinlab.varnote.utils.database.Database;
import org.mulinlab.varnote.utils.node.RefNode;

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
					this.log.add("VCF Info information is not find for field " + GlobalParameter.KCYN + format.getColField(col) + GlobalParameter.KNRM + ", we will use " + GlobalParameter.KCYN + info + GlobalParameter.KNRM + " instead.");
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
