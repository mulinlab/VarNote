package bean.config.anno;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import constants.VannoUtils;
import postanno.FormatWithRef;
import postanno.LineIteratorImpl;

public abstract class AbstractBedParser extends AbstractParser{

	protected List<String> fields;
	protected Map<String, Integer> fieldToCol;
	protected int totalCols;
//	private boolean isFindHeader;
	
	protected List<Integer> requiredCols;
	
	protected List<String> otherfields;
	protected Map<String, Integer> otherFieldsMap;
	protected Map<String, Integer> defaultFieldsMap;
	
	public AbstractBedParser(final FormatWithRef format) {
		super(format);
	}
	
	public AbstractBedParser(final FormatWithRef format, final List<String> columns) {
		this(format);
		splitHeader(columns);
		if(fields.size() <= 1) throw new IllegalArgumentException("columns should be seperate by comma.");
		checkFormat(defaultFieldsMap);
	}
	
	public AbstractBedParser(final FormatWithRef format, final File file) {
		this(format);
		String header = readBedHeader(file);
		
		if((header.startsWith("#") || header.startsWith("@")) && !header.startsWith("##") && !header.startsWith("@@")) {
			header = header.substring(1);
			splitHeader(VannoUtils.split(header, "File Header:" + file.getName(), TAB));
			if(fields.size() <= 1) {
				splitHeader(VannoUtils.split(header, "File Header:" + file.getName(), VannoUtils.COMMA_SPILT));
			}
			if(fields.size() <= 1) throw new IllegalArgumentException("Header should be start with '#' and seperate by tab or comma");
			checkFormat(defaultFieldsMap);
		} else {
//			isFindHeader = false;
			totalCols = header.split(TAB).length;
			
			fields = new ArrayList<String>();
			fieldToCol = new HashMap<String, Integer>();
			for (int i = 0; i < totalCols; i++) {
				fields.add(COL + i);
				fieldToCol.put(COL + i, i);
			}
		}	
	}
	
	public void getRequiredVCFCols() {
		requiredCols = new ArrayList<Integer>();
		requiredCols.add(format.tbiFormat.sequenceColumn - 1);
		requiredCols.add(format.tbiFormat.startPositionColumn - 1);
		requiredCols.add(-1);
		
		requiredCols.add((format.refCol == -1) ? -1 : (format.refCol - 1));
		requiredCols.add((format.altCol == -1) ? -1 : (format.altCol - 1));
		
		requiredCols.add((defaultFieldsMap.get(VannoUtils.HEADER_QUAL) == -1) ? -1 : defaultFieldsMap.get(VannoUtils.HEADER_QUAL));
		requiredCols.add((defaultFieldsMap.get(VannoUtils.HEADER_FILTER) == -1) ? -1 : defaultFieldsMap.get(VannoUtils.HEADER_FILTER));
	}

	public void getRequiredBEDCols() {
		requiredCols = new ArrayList<Integer>();
		requiredCols.add(format.tbiFormat.sequenceColumn - 1);
		requiredCols.add(format.tbiFormat.startPositionColumn - 1);
		requiredCols.add(format.tbiFormat.endPositionColumn - 1);
		
		if(format.refCol > 0) requiredCols.add(format.refCol - 1);
		if(format.altCol > 0) requiredCols.add(format.altCol - 1);	
	}
	
	public void getOthersForBED() {
		otherfields = new ArrayList<String>();
		otherFieldsMap = new HashMap<String, Integer>();
		for (int i = 0; i < fields.size(); i++) {
			if(!requiredCols.contains(i)) {
				otherfields.add(fields.get(i));
				otherFieldsMap.put(fields.get(i), i);
			}
		}
	}
	
	public void getOthersForVCF() {
		otherfields = new ArrayList<String>();
		otherFieldsMap = new HashMap<String, Integer>();
		for (int i = 0; i < fields.size(); i++) {
			if(!requiredCols.contains(i)) {
				otherfields.add(fields.get(i));
				otherFieldsMap.put(fields.get(i), i);
			}
		}
	}
	
	public void checkFormat(final Map<String, Integer> map) {
		compareCol(map.get(VannoUtils.HEADER_CHR), format.tbiFormat.sequenceColumn, VannoUtils.HEADER_CHR);
		if(map.get(VannoUtils.HEADER_POS) != -1) compareCol(map.get(VannoUtils.HEADER_POS), format.tbiFormat.startPositionColumn, VannoUtils.HEADER_POS);
		if(map.get(VannoUtils.HEADER_FROM) != -1) {
			compareCol(map.get(VannoUtils.HEADER_FROM), format.tbiFormat.startPositionColumn, VannoUtils.HEADER_FROM);
			compareCol(map.get(VannoUtils.HEADER_TO), format.tbiFormat.endPositionColumn, VannoUtils.HEADER_TO);
		}
		
		if(map.get(VannoUtils.HEADER_REF) != -1) {  //compare ref
			if(format.refCol != -1) {
				compareCol(map.get(VannoUtils.HEADER_REF), format.refCol, VannoUtils.HEADER_REF);
			} else {
				format.refCol = map.get(VannoUtils.HEADER_REF) + 1;
			}
		}
		
		if(map.get(VannoUtils.HEADER_ALT) != -1) { //compare col
			if(format.altCol != -1) {
				compareCol(map.get(VannoUtils.HEADER_ALT), format.altCol, VannoUtils.HEADER_ALT);
			} else {
				format.altCol = map.get(VannoUtils.HEADER_ALT) + 1;
			}
		}
		
		if(map.get(VannoUtils.HEADER_INFO) != -1) { //compare info
			if(format.infoCol != -1) {
				compareCol(map.get(VannoUtils.HEADER_INFO), format.infoCol, VannoUtils.HEADER_INFO);
			} else {
				format.infoCol = map.get(VannoUtils.HEADER_INFO) + 1;
			}
		}
		
//		isFindHeader = true;
		totalCols = fields.size();
	}
	
	public void compareCol(final int i, final int j, final String name) {
		if( i != (j-1)) throw new IllegalArgumentException(  name + " is supposed to be the " + j + " column.(1-based)." );
	}
	
	public void splitHeader(final List<String> columns) {
		fields = new ArrayList<String>();
		fieldToCol = new HashMap<String, Integer>();
		
		defaultFieldsMap = VannoUtils.getHeaderMap();
		String val;
		for (int i = 0; i < columns.size(); i++) {
			val = columns.get(i).trim();
			fields.add(val);
			fieldToCol.put(val, i);
			defaultFieldsMap.put(val.toUpperCase(), i);
		}
	}

	public String readBedHeader(final File file) {
		try {
			LineIteratorImpl reader = new LineIteratorImpl(file);
			String line = null;
			if(reader.hasNext()) {
				line = reader.next();
				
				reader.close();
				return line;
			} else {
				reader.close();
				throw new IllegalArgumentException("File is empty! Please check: " + file.getAbsolutePath());
			}
			
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	@Override
	public abstract void emptyNodes(final String[] query_alts);
}
