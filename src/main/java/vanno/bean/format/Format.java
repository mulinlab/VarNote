package main.java.vanno.bean.format;


import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import htsjdk.samtools.seekablestream.SeekableStreamFactory;
import htsjdk.samtools.util.IOUtil;
import htsjdk.tribble.util.TabixUtils;
import main.java.vanno.bean.database.index.TbiIndex;
import main.java.vanno.constants.BasicUtils;
import main.java.vanno.constants.InvalidArgumentException;
import main.java.vanno.constants.VannoUtils;
import main.java.vanno.constants.VannoUtils.FileType;

public class Format{
	
	public static final String VCF_HEADER_INDICATOR = BasicUtils.VCF_HEADER_INDICATOR;
	public final static boolean DEFAULT_ZERO_BASED = BasicUtils.DEFAULT_ZERO_BASED;
	public final static boolean DEFAULT_HAS_HEADER = BasicUtils.DEFAULT_HAS_HEADER;
	public final static String DEFAULT_COMMENT_INDICATOR = BasicUtils.DEFAULT_COMMENT_INDICATOR;
	public static final String COL = BasicUtils.COL;
	public final static int MAX_HEADER_COMPARE_LENGTH = BasicUtils.MAX_HEADER_COMPARE_LENGTH;
	public final static int START_COMPARE_LENGTH = BasicUtils.START_COMPARE_LENGTH;
	public final static int DEFAULT_COL = -1;
	
	public enum FormatType { 
		VCF("vcf"), BED("bed"), TAB("tab");
		private final String name;
		
		FormatType(final String name) {
	        this.name = name;
	    }
		public String getName() {
			return name;
		}
	}
	
    public static final int ZERO_BASED    = 0x10000;
    public static final int GENERIC_FLAGS = 0;
    public static final int VCF_FLAGS     = 2;
    public static final int UCSC_FLAGS    = GENERIC_FLAGS | ZERO_BASED;
    
	public static final String HEADER_POS = "POS";
	public static final String HEADER_ID = "ID";

//	public static Format VCF = new Format(VCF_FLAGS, 1, 2, 0, 0, DEFAULT_COMMENT_INDICATOR, 4, 5, false);
//	public static Format BED = new Format(UCSC_FLAGS, 1, 2, 3, 0, DEFAULT_COMMENT_INDICATOR, DEFAULT_COL, DEFAULT_COL, false);
//	public static Format TAB = new Format(GENERIC_FLAGS, -1, -1, -1, 0, "##", -1, -1, false);
	
	public enum H_FIELD { CHROM, BEGIN, END, REF, ALT, QUAL, FILTER, INFO }
	
	private List<String> originalField;
	private List<String> convertField;
	
	private Map<String, Integer> fieldToCol;
	private Map<Integer, String> colToField;

	private String commentIndicator;
	private boolean hasHeader = DEFAULT_HAS_HEADER;
	private String headerPath = null;
	private boolean isRefAndAltExsit = false;
	private int flags;
	private int sequenceColumn;
	private int startPositionColumn;
	private int endPositionColumn;
	private int numHeaderLinesToSkip;
	private String header;
	private String headerStart;
	private boolean hasHeaderInFile = true; 
	
	public static Format newTAB() {
		return new Format(GENERIC_FLAGS, DEFAULT_COL, DEFAULT_COL, DEFAULT_COL, 0, DEFAULT_COMMENT_INDICATOR, DEFAULT_COL, DEFAULT_COL, DEFAULT_HAS_HEADER);
	}
	
	public static Format newBED() {
		return new Format(UCSC_FLAGS, 1, 2, 3, 0, DEFAULT_COMMENT_INDICATOR, DEFAULT_COL, DEFAULT_COL, false);
	}
	
	public static Format newVCF() {
		return new Format(VCF_FLAGS, 1, 2, 0, 0, DEFAULT_COMMENT_INDICATOR, 4, 5, false);
	}
	
	public Format(final int flags, int sequenceColumn, int startPositionColumn, int endPositionColumn, final int numHeaderLinesToSkip,
		final String commentIndicator, int refColumn, int altColumn, boolean hasHeader) {	
		init();
		setFlags(flags);
		updateCols(sequenceColumn, startPositionColumn, endPositionColumn, refColumn, altColumn);
		setCommentIndicator(commentIndicator);
		setHasHeader(hasHeader);
		setNumHeaderLinesToSkip(numHeaderLinesToSkip);
    }
	
	public void init() {
		this.fieldToCol = new HashMap<String, Integer>();
		for (H_FIELD key : H_FIELD.values()) {
			this.fieldToCol.put(key.toString(), -1);
		}
		this.colToField = new HashMap<Integer, String>();
	}
	
	public Format() {
		this(GENERIC_FLAGS, DEFAULT_COL, DEFAULT_COL, DEFAULT_COL, 0, DEFAULT_COMMENT_INDICATOR, DEFAULT_COL, DEFAULT_COL, DEFAULT_HAS_HEADER);
	}
	
	public Format(final String headerPath) {
		this(headerPath, DEFAULT_COMMENT_INDICATOR, DEFAULT_ZERO_BASED);
	}
	
	public Format(final String headerPath, final boolean zeroBased) {
		this(headerPath, DEFAULT_COMMENT_INDICATOR, zeroBased);
	}

	public Format(final String headerPath, final String commentIndicator, final boolean zeroBased) {
		this(GENERIC_FLAGS, DEFAULT_COL, DEFAULT_COL, DEFAULT_COL, 0, commentIndicator, DEFAULT_COL, DEFAULT_COL, true);
		if(zeroBased) setZeroBased();
		setHeaderPath(headerPath);
	}
	
	public static Format defaultFormat(final String input, final boolean isQuery) {    
		Format defaultFormat;
		
		File tbiIndex = new File(input + TabixUtils.STANDARD_INDEX_EXTENSION);
	 	if(tbiIndex.exists()) {
	 		defaultFormat = new TbiIndex(tbiIndex.getPath()).getFormat();
	 	} else {
	 		if(isQuery) {
	 			defaultFormat = VannoUtils.determineFileType(input);
	 		} else {
	 			defaultFormat = VannoUtils.determineFileType(input);
	 		}
	 		
	 		if(defaultFormat == null) defaultFormat = Format.newTAB();
	 	}
	 	return defaultFormat;
	}

	@Override
	public String toString() {
		String str = "";
		for (H_FIELD key : H_FIELD.values()) {
			str = str + fieldToCol.get(key.toString()) + ",";
		}
		str = str + commentIndicator + "," + flags + "," + numHeaderLinesToSkip + ", " + hasHeader;
		return str;
	}
	
	public static Format readFormatString(final String str) {
		Format format = new Format();
		String[] cols = str.split(",");
		if(cols.length != 12) throw new InvalidArgumentException("Parsing format with error, format should have 12 columns.");
		format.init();
		format.setFlags(Integer.parseInt(cols[9]));
		format.updateCols(Integer.parseInt(cols[0]), Integer.parseInt(cols[1]), Integer.parseInt(cols[2]), Integer.parseInt(cols[3]), Integer.parseInt(cols[4]));
		format.setCommentIndicator(cols[8]);
		format.setNumHeaderLinesToSkip(Integer.parseInt(cols[10]));
		format.setHasHeader(VannoUtils.strToBool(cols[11]));
		format.updateColToField();
		return format;
	}
	
	public void updateCols(final int seqCol, final int bCol, final int eCol, final int rCol, final int aCol) {
		setSequenceColumn(seqCol);
		setStartPositionColumn(bCol);
		setEndPositionColumn(eCol);
		setRefColumn(rCol);
		setAltColumn(aCol);
	}
	
	
	public void readFormatFromFile(final String path, final FileType fileType) {
		setField(BEDHeaderParser.readActualHeader(path, this, false, fileType));
	}
	
	public void readFormatFromHeader(final FileType fileType) {
		setField(BEDHeaderParser.readActualHeader(headerPath, this, true, VannoUtils.checkFileType(headerPath)));
	}	
	

	public List<String> getOriginalField() {
		return originalField;
	}

	public List<String> getConvertField() {
		return convertField;
	}

	public String getCommentIndicator() {
		return commentIndicator;
	}

	public boolean isHasHeader() {
		return hasHeader;
	}

	public void setHasHeader(boolean hasHeader) {
		this.hasHeader = hasHeader;
	}

	public String getHeaderPath() {
		return headerPath;
	}

	public void setHeaderPath(String headerPath) {
		if(SeekableStreamFactory.isFilePath(headerPath)) {
			this.headerPath = new File(headerPath).getAbsolutePath();
		} else {
			this.headerPath = headerPath;
		}
		IOUtil.assertInputIsValid(this.headerPath);
	}
	
	public void checkRefAndAlt() {
		if ((fieldToCol.get(H_FIELD.REF.toString()) > 0) && (fieldToCol.get(H_FIELD.ALT.toString()) > 0)) {
			isRefAndAltExsit = true;
		} else {
			isRefAndAltExsit = false;
		}
	}

	public boolean isRefAndAltExsit() {
		return isRefAndAltExsit;
	}

	public void setField(List<String> colNmaes) {
		this.originalField = colNmaes;
		updateFormatByColNames();
	}
	
	public void updateFormatByColNames() {
		this.convertField = new ArrayList<String>();
		
		int hasPosAndBegin = 0;
		
		String field;
		for (int i = 0; i < originalField.size(); i++) {
			field = originalField.get(i).toUpperCase();
			if(field.equals("CHR")) {
				field =  H_FIELD.CHROM.toString();
			} else if(field.equals(Format.HEADER_POS)) {
				hasPosAndBegin ++;
				field =  H_FIELD.BEGIN.toString();
				fieldToCol.put(H_FIELD.END.toString(), 0);
			} else if(field.equals( H_FIELD.BEGIN.toString() )) {
				hasPosAndBegin++;
			}
			
			if(fieldToCol.get(field) == null || fieldToCol.get(field) == DEFAULT_COL) {
				fieldToCol.put(field, i + 1);
			}
			this.convertField.add(field);
		}
		
		if(hasPosAndBegin >= 2) {
			throw new InvalidArgumentException("Header shouldn't have POS and BEGIN both!");
		}
		updateColToField();
	}
	
	public void updateColToField() {
		int col;
		for (String key : fieldToCol.keySet()) {
			col = fieldToCol.get(key);

			if((colToField.get(col) == null) || (colToField.get(col).equals(COL + col))) {
				colToField.put(fieldToCol.get(key), key);
			}
		}
	}
	
	public void checkOverlap(final String path, final FileType fileType) { //fields required for overlap
		if(headerPath != null) {
			readFormatFromHeader(fileType);
		} else if(originalField == null) {
			readFormatFromFile(path, fileType);
		}
		
		if(sequenceColumn < 1) {
			if(fieldToCol.get(H_FIELD.CHROM.toString()) > 0) {
				sequenceColumn = fieldToCol.get(H_FIELD.CHROM.toString());
			} else {
				throw new InvalidArgumentException("You should define -c in command line or define CHROM in header");
			}
		} else if(sequenceColumn != fieldToCol.get(H_FIELD.CHROM.toString())) {
			throw new InvalidArgumentException(" Sequence column is " + sequenceColumn + " but CHROM column in header is " + fieldToCol.get(H_FIELD.CHROM.toString()) + ", please check.");
		}
		
		if(startPositionColumn < 1) {
			if(fieldToCol.get(H_FIELD.BEGIN.toString()) > 0) {
				startPositionColumn = fieldToCol.get(H_FIELD.BEGIN.toString());
			} else {
				throw new InvalidArgumentException("You should define -b and -e in command line or define POS or BEGIN,END in header");
			}
		} else if(startPositionColumn != fieldToCol.get(H_FIELD.BEGIN.toString())) {
			throw new InvalidArgumentException(" Start position column is " + startPositionColumn + " but BEGIN column in header is " + fieldToCol.get(H_FIELD.BEGIN.toString()) + ", please check.");
		}
		
		if((endPositionColumn == startPositionColumn) || (fieldToCol.get(H_FIELD.END.toString())  == startPositionColumn)) {
			setEndPositionColumn(0);
		} else if((endPositionColumn == DEFAULT_COL) || endPositionColumn == 0) {
			if(fieldToCol.get(H_FIELD.END.toString()) == DEFAULT_COL )
				setEndPositionColumn(0);
			else if(fieldToCol.get(H_FIELD.END.toString()) == 0){
				endPositionColumn = 0;
			} else if(fieldToCol.get(H_FIELD.END.toString())  > 0) {
				endPositionColumn = fieldToCol.get(H_FIELD.END.toString());
			}
		} else if(endPositionColumn > 0) {
			if(fieldToCol.get(H_FIELD.END.toString()) == DEFAULT_COL || fieldToCol.get(H_FIELD.END.toString()) == 0) {
				setEndPositionColumn(endPositionColumn);
			} else if(endPositionColumn != fieldToCol.get(H_FIELD.END.toString())) {
				throw new InvalidArgumentException(" End position column is " + endPositionColumn + " but END column in header is " + fieldToCol.get(H_FIELD.END.toString()) + ", please check.");
			}
		}
	}
	
	public void setSequenceColumn(final int sequenceColumn) {
		this.sequenceColumn = sequenceColumn;
		fieldToCol.put(H_FIELD.CHROM.toString(), sequenceColumn);
		colToField.put(sequenceColumn, H_FIELD.CHROM.toString());
	}
	
	public void setStartPositionColumn(final int startPositionColumn) {
		this.startPositionColumn = startPositionColumn;
		fieldToCol.put(H_FIELD.BEGIN.toString(), startPositionColumn);
		colToField.put(startPositionColumn, H_FIELD.BEGIN.toString());
	}
	
	public void setEndPositionColumn(final int endPositionColumn) {
		this.endPositionColumn = endPositionColumn;
		fieldToCol.put(H_FIELD.END.toString(), endPositionColumn);
		colToField.put(endPositionColumn, H_FIELD.END.toString());
	}
	
	public void setRefColumn(final int refColumn) {
		fieldToCol.put(H_FIELD.REF.toString(), refColumn);
		colToField.put(refColumn, H_FIELD.REF.toString());
	}
	
	public void setAltColumn(final int altColumn) {
		fieldToCol.put(H_FIELD.ALT.toString(), altColumn);
		colToField.put(altColumn, H_FIELD.ALT.toString());
	}
	
	public void setFlags(final int flags) {
		this.flags = flags;
		if(this.flags == VCF_FLAGS) {
			fieldToCol.put(H_FIELD.QUAL.toString(), 6);
			fieldToCol.put(H_FIELD.FILTER.toString(), 7);
			fieldToCol.put(H_FIELD.INFO.toString(), 8);
		}
	}

	public void setZeroBased() {
		if(this.flags == GENERIC_FLAGS) {
			this.flags = GENERIC_FLAGS | ZERO_BASED;
		}
	}
	
	public boolean isZeroBased() {
		return ((this.flags & 0x10000) != 0 );
	}
	
	public void setCommentIndicator(String commentIndicator) {
		if((commentIndicator == null) || commentIndicator.equals(""))  return;
		this.commentIndicator = VannoUtils.replaceQuote(commentIndicator.trim());
	}

	public int getNumHeaderLinesToSkip() {
		return numHeaderLinesToSkip;
	}

	public void setNumHeaderLinesToSkip(int numHeaderLinesToSkip) {
		this.numHeaderLinesToSkip = numHeaderLinesToSkip;
	}

	public int getSequenceColumn() {
		return sequenceColumn;
	}

	public int getStartPositionColumn() {
		return startPositionColumn;
	}

	public int getEndPositionColumn() {
		return endPositionColumn;
	}
	
	public boolean isPos() {
		return ((endPositionColumn == 0) || (endPositionColumn == startPositionColumn));
	}
	
	public int getRefColumn() {
		return fieldToCol.get(H_FIELD.REF.toString());
	}
	
	public int getAltColumn() {
		return fieldToCol.get(H_FIELD.ALT.toString());
	}

	public int getFlags() {
		return flags;
	}
	
	public int getFieldCol(final String field) {
		if(fieldToCol.get(field.toUpperCase()) == null) return -1;
		else return fieldToCol.get(field.toUpperCase());
	}
	
	public String getColField(final int col) {
		if(col == -1) return null;
		else return colToField.get(col);
	}

	public String getColOriginalField(final int col) {
		if(col == -1) return null;
		if(originalField != null && originalField.size() > 0 && col > 0) {
			if(originalField.get(col - 1) != null && !originalField.get(col - 1).equals(COL + col)) {
				return originalField.get(col - 1);
			} else {
				return colToField.get(col);
			}
		} else return colToField.get(col);
	}
	
	public String getHeader() {
		return header;
	}

	public void setHeader(String header) {
		this.header = header.substring(0, (header.length() > MAX_HEADER_COMPARE_LENGTH) ? MAX_HEADER_COMPARE_LENGTH : header.length());
		this.headerStart = header.substring(0, (header.length() > START_COMPARE_LENGTH) ? START_COMPARE_LENGTH : header.length());
	}

	public String getHeaderStart() {
		return headerStart;
	}

	public boolean isHasHeaderInFile() {
		return hasHeaderInFile;
	}

	public void setHasHeaderInFile(boolean hasHeaderInFile) {
		this.hasHeaderInFile = hasHeaderInFile;
	}

	public Map<Integer, String> getColToField() {
		return colToField;
	}
	
	
}
