package org.mulinlab.varnote.config.anno;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import htsjdk.samtools.util.IOUtil;
import htsjdk.variant.vcf.VCFCodec;
import htsjdk.variant.vcf.VCFHeader;
import htsjdk.variant.vcf.VCFInfoHeaderLine;
//import org.mulinlab.varnote.config.anno.vcf.VCFCodec;
import org.mulinlab.varnote.utils.queryreader.LineIteratorImpl;
import org.mulinlab.varnote.constants.GlobalParameter;
import org.mulinlab.varnote.utils.VannoUtils.FileType;

public final class VCFParser {
	
	public static final String VCF_FIELD = GlobalParameter.VCF_FIELD;
	public static final String VCF_HEADER_INDICATOR = GlobalParameter.VCF_HEADER_INDICATOR;
	public static final String INFO_FIELD_SEPARATOR = GlobalParameter.INFO_FIELD_SEPARATOR;
	public static final String VCF_INFO_EQUAL = GlobalParameter.VCF_INFO_EQUAL;
	
	private VCFHeader vcfHeader;
	private List<String> infoKeys;
	private Map<String, VCFInfoHeaderLine> infoMap;
	
	public VCFParser(final String path, final FileType fileType) {
		IOUtil.assertInputIsValid(path);
		readHeaderFiles(path, fileType);
	}
	
	public void readHeaderFiles(final String path, final FileType fileType) {
//		log.printKVKCYN("Reading VCF header from file", log.isLog() ? new File(path).getName() :path);
		try {
			
			LineIteratorImpl reader = new LineIteratorImpl(path, fileType);
			vcfHeader = (VCFHeader)(new VCFCodec()).readActualHeader(reader);
			try {
				reader.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
			
			infoKeys = new ArrayList<String>();
			infoMap = new HashMap<String, VCFInfoHeaderLine>();
			
			for (VCFInfoHeaderLine info : vcfHeader.getInfoHeaderLines()) {
				infoKeys.add(info.getID());
				infoMap.put(info.getID(), info);
			}
		} catch (FileNotFoundException e) {
			System.out.println("File Path" + path);
			e.printStackTrace();
		} catch (IOException e1) {
			e1.printStackTrace();
		}
	}
	
	public List<String> getGenotypeSamples() {
		return vcfHeader.getGenotypeSamples();
	}

	public List<String> getInfoKeys() {
		return infoKeys;
	}
	
	public Map<String, VCFInfoHeaderLine> getInfoMap() {
		return infoMap;
	}

	public Map<String, String> infoToMap(final String info) {
		Map<String, String> fieldToValMap = new HashMap<String, String>();
		final String[] fields = info.split(INFO_FIELD_SEPARATOR);
		int beg;
		for (String str : fields) {
			str = str.trim();
			beg = str.indexOf(VCF_INFO_EQUAL);
			if(beg != -1) {
				fieldToValMap.put(str.substring(0, beg), str.substring(beg + 1));
			} else {
				fieldToValMap.put(str, "true");
			}
		}
		return fieldToValMap;
	}
	
	public List<String> infoToList(final String info) {
		Map<String, String> fieldToValMap = infoToMap(info);
		
		List<String> out = new ArrayList<String>();
		for (String field : infoKeys) {
			if(fieldToValMap.get(field) == null) {
				out.add(".");
			} else {
				if(fieldToValMap.get(field).equals("true")) {
					out.add(field);
				} else {
					out.add(fieldToValMap.get(field));
				}
			}
		}
		return out;
	}

	public VCFHeader getVcfHeader() {
		return vcfHeader;
	}
	
}
