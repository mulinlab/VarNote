package org.mulinlab.varnote.utils.format;


import java.io.File;
import htsjdk.samtools.util.StringUtil;
import htsjdk.tribble.index.tabix.TabixFormat;
import org.mulinlab.varnote.constants.GlobalParameter;
import htsjdk.samtools.util.IOUtil;
import org.mulinlab.varnote.utils.database.index.TbiIndex;
import org.mulinlab.varnote.utils.enumset.FormatType;
import org.mulinlab.varnote.utils.enumset.IndexType;
import org.mulinlab.varnote.exceptions.InvalidArgumentException;
import org.mulinlab.varnote.utils.VannoUtils;

public class Format extends TabixFormat {
	public final static String DEFAULT_COMMENT_INDICATOR = GlobalParameter.DEFAULT_COMMENT_INDICATOR;
	public final static int MAX_HEADER_COMPARE_LENGTH = 128;
	public final static int START_COMPARE_LENGTH = 7;

	public static Format VCF = new Format(VCF_FLAGS, 1, 2, 0, 0, DEFAULT_COMMENT_INDICATOR, 4, 5, true, FormatType.VCF);
	public static Format BED = new Format(UCSC_FLAGS, 1, 2, 3, 0, DEFAULT_COMMENT_INDICATOR, -1, -1, false, FormatType.BED);


	public int refPositionColumn;
	public int altPositionColumn;

	private String extHeaderPath;
	private String commentIndicator;
	private boolean hasHeaderInFile;

	private String[] headerPart;
	public FormatType type;

	private String headerStr;
	private String headerStart;
	private String dataStr;

	public static Format newTAB() {
		return new Format(GENERIC_FLAGS, -1, -1, -1, 0, DEFAULT_COMMENT_INDICATOR, -1, -1, false, FormatType.TAB);
	}

	public Format(final int flags, int sequenceColumn, int startPositionColumn, int endPositionColumn, final int numHeaderLinesToSkip,
				  final String commentIndicator, int refColumn, int altColumn, boolean hasHeaderInFile) {
		super(flags, sequenceColumn, startPositionColumn, endPositionColumn, '#', numHeaderLinesToSkip);

		this.refPositionColumn = refColumn;
		this.altPositionColumn = altColumn;
		this.commentIndicator = commentIndicator;
		this.hasHeaderInFile = hasHeaderInFile;

		if(flags == VCF_FLAGS) {
			type = FormatType.VCF;
		} else if(flags == UCSC_FLAGS && sequenceColumn == 1 && startPositionColumn == 2 && endPositionColumn == 3) {
			type = FormatType.BED;
		} else {
			type = FormatType.TAB;
		}
	}
	
	public Format(final int flags, int sequenceColumn, int startPositionColumn, int endPositionColumn, final int numHeaderLinesToSkip,
		final String commentIndicator, int refColumn, int altColumn, boolean hasHeader, FormatType type) {
		this(flags, sequenceColumn, startPositionColumn, endPositionColumn, numHeaderLinesToSkip, commentIndicator, refColumn, altColumn, hasHeader);
		this.type = type;
    }
	
	public static Format defaultFormat(final String input, final boolean isQuery) {
		Format defaultFormat;

		defaultFormat = VannoUtils.determineFileType(input);
		if(defaultFormat == null) {
			if(isQuery) {
				return Format.newTAB();
			} else {
				File tbiIndex = new File(input + IndexType.TBI.getExtIndex());
				if(tbiIndex.exists()) {
					return new TbiIndex(tbiIndex.getPath()).getFormat();
				} else {
					return Format.newTAB();
				}
			}
		}
		return defaultFormat;
	}

	@Override
	public String toString() {
		return String.format("%d,%d,%d,%d,%d,%s,%d,%d,%s", flags, sequenceColumn, startPositionColumn, endPositionColumn, numHeaderLinesToSkip, commentIndicator,
				refPositionColumn, altPositionColumn, hasHeaderInFile);
	}

	public String logFormat() {
		if(this.type == FormatType.VCF) {
			return "VCF";
		} else if(this.type == FormatType.BED) {
			return "BED";
		} else {
			return String.format("TAB, CHROM:%s BEGIN:%s END:%s REF:%s ALT:%s ZeroBased:%s", sequenceColumn, startPositionColumn, endPositionColumn,
					refPositionColumn == -1 ? '-' : refPositionColumn + "",
					altPositionColumn == -1 ? '-' : altPositionColumn + "", isZeroBased());
		}
	}
	
	public static Format readFormatString(final String str) {
		String[] cols = str.split(",");
		if(cols.length != 9) throw new InvalidArgumentException("Parsing format with error, format should have 9 columns.");

		return new Format(Integer.parseInt(cols[0]), Integer.parseInt(cols[1]), Integer.parseInt(cols[2]), Integer.parseInt(cols[3]), Integer.parseInt(cols[4]),
				cols[5], Integer.parseInt(cols[6]), Integer.parseInt(cols[7]), VannoUtils.strToBool(cols[8]));
	}

	public String getCommentIndicator() {
		return commentIndicator;
	}

	public boolean isHasHeader() {
		return hasHeaderInFile;
	}

	public void setHasHeaderInFile(boolean hasHeaderInFile) {
		this.hasHeaderInFile = hasHeaderInFile;
	}

	public String getHeaderPath() {
		return extHeaderPath;
	}

	public void setHeaderPath(String headerPath) {
		IOUtil.assertInputIsValid(headerPath);
		this.extHeaderPath = VannoUtils.getAbsolutePath(headerPath);
		this.setHasHeaderInFile(false);
	}

	public boolean isRefAndAltExsit() {
		return (refPositionColumn > 0) && (altPositionColumn > 0);
	}

	public boolean hasLoc() {
		return (sequenceColumn > 0) && (startPositionColumn > 0) && (endPositionColumn > -1);
	}

	public void checkLoc() {
		if(sequenceColumn < 1) throw new InvalidArgumentException("You should define column of sequence name with -c");
		if(startPositionColumn < 1) throw new InvalidArgumentException("You should define column of start chromosomal position with -b");
		if(endPositionColumn < 0) throw new InvalidArgumentException("You should define column of end chromosomal position with -e");

		if(endPositionColumn == 0) {
			endPositionColumn = startPositionColumn;
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

	public int getFlags() {
		return flags;
	}

	public FormatType getType() {
		return type;
	}

	public String[] getHeaderPart() {
		return headerPart;
	}

	public String getHeaderPartStr() {
		return StringUtil.join(GlobalParameter.TAB, headerPart);
	}

	public int getHeaderPartSize() {
		return headerPart.length;
	}

	public String getColumnName(final int col) {
		return headerPart[col - 1];
	}

	public void setHeaderPart(final String[] headerPart) {
		this.headerPart = headerPart;
	}

	public void setHeader(String header) {
		this.headerStr = header.substring(0, (header.length() > MAX_HEADER_COMPARE_LENGTH) ? MAX_HEADER_COMPARE_LENGTH : header.length());
		this.headerStart = header.substring(0, (header.length() > START_COMPARE_LENGTH) ? START_COMPARE_LENGTH : header.length());
	}

	public String getHeaderStr() {
		return headerStr;
	}

	public String getHeaderStart() {
		return headerStart;
	}

	public boolean isPos() {
		return ((endPositionColumn == 0) || (endPositionColumn == startPositionColumn));
	}

	public String getDataStr() {
		return dataStr;
	}

	public void setDataStr(final String dataStr) {
		this.dataStr = dataStr;
	}

	public int getCol(String str) {
		str = str.toUpperCase();
		for (int i = 0; i < headerPart.length; i++) {
			if(headerPart[i].toUpperCase().equals(str)) {
				return i+1;
			}
		}
		return -1;
	}
}
