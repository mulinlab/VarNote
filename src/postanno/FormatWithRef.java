package postanno;

import constants.ErrorMsg;
import htsjdk.tribble.index.tabix.TabixFormat;

public class FormatWithRef {
	public static final int VCF_FORMAT = 1;
	public static final int BED_POS = 3;
	public static final int BED_FROM_TO = 2;
	
	public TabixFormat tbiFormat = null;
	public int refCol = -1;
	public int altCol = -1;
	public int infoCol = -1;
	public int flag = -1;
	
	public static FormatWithRef VCF = new FormatWithRef(TabixFormat.VCF, 4, 5, 8, 1);
	public static FormatWithRef BED = new FormatWithRef(TabixFormat.BED, -1, -1, -1, 2);
	public static FormatWithRef TAB = new FormatWithRef(null, -1, -1, -1, 3);
	
	public FormatWithRef(final TabixFormat tbiFormat, final int refCol, final int altCol, final int infoCol, final int flag) {
		super();
		this.tbiFormat = tbiFormat;
		this.refCol = refCol;
		this.altCol = altCol;
		this.infoCol = infoCol;
		this.flag = flag;
	}
	
	public FormatWithRef(final TabixFormat tbiFormat) {
		super();
		this.tbiFormat = tbiFormat;
	}
	
	public void checkOverlap() {
		if(this.tbiFormat == null) throw new IllegalArgumentException(ErrorMsg.INDEX_FILE_COLUMN_CMD);
		if(this.tbiFormat.sequenceColumn == -1) throw new IllegalArgumentException(ErrorMsg.MISS_CHR);
		if((this.tbiFormat.startPositionColumn == -1) || (this.tbiFormat.endPositionColumn == -1)) throw new IllegalArgumentException(ErrorMsg.MISS_POS);	
	}
	
	
	public void checkAnno() {
		if(this.refCol == -1) throw new IllegalArgumentException(ErrorMsg.MISS_REF);
		if((this.altCol == -1)) throw new IllegalArgumentException(ErrorMsg.MISS_ALT);
	}

	@Override
	public String toString() {
		return tbiFormat.sequenceColumn + "," + tbiFormat.startPositionColumn + "," + tbiFormat.endPositionColumn + ","
				 + tbiFormat.flags + "," + tbiFormat.numHeaderLinesToSkip + ","
				  + refCol + "," + altCol + "," + infoCol + "," + flag;
	}
	
	
	public FormatWithRef(final String str) {
		super();
		String[] cols = str.split(",");
		if(cols.length != 9) throw new IllegalArgumentException("Parsing format with error, format should have 9 columns.");
		
		this.tbiFormat = new TabixFormat(Integer.parseInt(cols[3]), Integer.parseInt(cols[0]), Integer.parseInt(cols[1]), Integer.parseInt(cols[2]), '#', Integer.parseInt(cols[4]));
		this.refCol = Integer.parseInt(cols[5]);
		this.altCol = Integer.parseInt(cols[6]);
		this.infoCol = Integer.parseInt(cols[7]);
		this.flag = Integer.parseInt(cols[8]);
	}
}
