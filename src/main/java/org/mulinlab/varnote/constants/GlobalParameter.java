package org.mulinlab.varnote.constants;


import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import htsjdk.tribble.Feature;
import org.mulinlab.varnote.utils.enumset.IntersectType;
import org.mulinlab.varnote.utils.enumset.Mode;
import org.mulinlab.varnote.utils.enumset.OutMode;

public final class GlobalParameter {

	//program
	public final static String PRO_NAME = "VarNote";
	public final static String PRO_CMD = "java -jar /path/to/VarNote.jar" ;
	public final static boolean DEFAULT_LOG = true;

	//gt
	public static final String GT_COMPRESS_FILE = ".bit";
	public static final String GT_IDX = ".idx";
	public static final String GT_COMPRESS_FILE_IDX = ".bit.idx";
	public static final int GT_BIN_INTERVAL = 1000;
	public static final int REF_IDX = 0;
	public static final int NO_CALL_IDX = -1;
	public static final int GT_FILE_END = -9;


	//sign
	public static final String UNDERLINE = "_";
	public static final String NULLVALUE = ".";
	public static final String TAB = "\t";
	public static final String NEWLINE = "\n";
	public static final String COMMENT_LINE = "#";
	public static final String OVERLAP_NOTE = "@";
	public static final String OVERLAP_EQUAL = "=";
	public final static String OPTION_SEPERATE = ":";

	public final static String ROAD_MAP_LABEL = "roadmap";
	public final static String REGBASE_MAP_LABEL = "regbase";
	public final static String GENOMAD_LABEL = "gnomad";
	public final static String DBNSFP_LABEL = "dbNSFP";

	public final static String COSMIC_LABEL = "cosmic";
	public final static String ICGC_LABEL = "icgc";


	//format
	public final static String DEFAULT_COMMENT_INDICATOR = "##";
	public final static int DEFAULT_COL = -1;
	public static final String COL = "col";
	public final static boolean DEFAULT_HAS_HEADER = false;
	public final static int DEFAULT_SKIP = 0;
	public final static boolean DEFAULT_ZERO_BASED = false;
	public static final int MAX_HEADER_COMPARE_LENGTH = 50;
	public static final int START_COMPARE_LENGTH = 7;


	//vcf format
	public static final String VCF_FIELD = "CHROM	POS	ID	REF	ALT	QUAL	FILTER	INFO";
	public static final String VCF_HEADER_INDICATOR = "#";
	public static final String INFO_FIELD_SEPARATOR = ";";
	public static final String VCF_INFO_EQUAL = "=";
	public static final String COMMA = ",";
	public static final int INFO_COL = 8;


	//index
	public static final int BUFFER_SIZE = 1024 * 128;
	public static final String TEMP = ".temp";


	//run overlap
	public final static boolean DEFAULT_USE_JDK = false;
	public final static boolean DEFAULT_IS_COUNT = false;
	public final static boolean DEFAULT_IS_GZIP = true;
	public final static Mode DEFAULT_MODE = Mode.MIX;
	public final static IntersectType DEFAULT_INTERSECT = IntersectType.INTERSECT;
	public final static int DEFAULT_THREAD = 1;
	public static String OVERLAP_RESULT_SUFFIX = ".overlap";


	//anno
	public final static boolean DEFAULT_FORCE_OVERLAP = false;
	public static String ANNO_RESULT_SUFFIX = ".anno";
	public static final String ANNO_CONFIG_SUFFIX = ".annoc";


	//output
	public static final String QUERY_START = "#query";
	public final static int DEFALT_OUT_MODE_VAL = 2;
	public final static OutMode DEFALT_OUT_MODE = OutMode.BOTH;
	public final static boolean DEFAULT_DISPLAY_LABEL = true;
	public final static boolean DEFAULT_REMOVE_COMMENT = false;
	public final static boolean DEFAULT_LOJ = false;
	public final static boolean DEFAULT_GZIP = true;


    public final static char HYPHEN_CHAR = '-';
    public final static String DOUBLE_HYPHEN = "--";
    public final static String HELP_OPTION_ABBR = HYPHEN_CHAR + "h";
    public final static String HELP_OPTION = DOUBLE_HYPHEN + "help";


	public static String KNRM = "\u001B[0m"; // reset
	//public final static String KBLD = "\u001B[1m"; // Bold
	public static String KRED = "\u001B[31m";
	public static String KGRN = "\u001B[32m";
	//public final static String KYEL = "\u001B[33m";
	public static String KBLU = "\u001B[34m";
	//public final static String KMAG = "\u001B[35m";
	public static String KCYN = "\u001B[36m";
	public static String KWHT = "\u001B[37m";
	public static String KBLDRED = "\u001B[1m\u001B[31m";


	
	public static final int BIN_GENOMIC_SPAN = 512 * 1024 * 1024;
	public static final int[] LEVEL_STARTS = { 0, 1, 9, 73, 585, 4681 };
	public static final int MAX_BINS = 37450; // =(8^6-1)/7+1
//	public static final int MAX_LINEAR_INDEX_SIZE = MAX_BINS + 1  - LEVEL_STARTS[LEVEL_STARTS.length - 1];
//	public static final int[] bins = new int[MAX_BINS];
	public static final int UNSET_GENOMIC_LOCATION = 0;
    
	public static final int version = 1;
    
    public static final int MAX_SHORT = 32767;
    public static final byte MAX_BYTE = 127;
    public static final short MAX_BYTE_UNSIGNED = 255;
    public static final int MAX_SHORT_UNSIGNED = 65535;
    
    public static final byte VANNO_FILE_END = -9;
   
    public static final byte BLOCK_ADDRESS = -6;
    public static final byte CHR_START = -8;
    
    public static final byte INT_START = -7;
    
    public static final byte BLOCK_START_WO_END = -1;
    public static final byte BLOCK_START_WITH_END_BTYE = -2;
    public static final byte BLOCK_START_WITH_END_SHORT = -3;
    public static final byte BLOCK_START_WITH_END_INT = -4;
    
    
    public static boolean readBoolean(final InputStream is) throws IOException {
        return (is.read() != 0);
    }
    
    public static byte read(final InputStream is) throws IOException {
    		return (byte)is.read();
	}
    
    public static short readShort(final InputStream is) throws IOException {
        return (short)((is.read() << 0) + (is.read() << 8));
	}
    
    public static int readInt(final InputStream is) throws IOException {
        return ((is.read() << 0) + (is.read() << 8) + (is.read() << 16) + (is.read() << 24));
	}
	
	public static long readLong(final InputStream is, byte[] buf) throws IOException {
		is.read(buf);
		return ByteBuffer.wrap(buf).order(ByteOrder.LITTLE_ENDIAN).getLong();
	}	

	public static int reg2bins(final int beg, final int _end, final int[] list) {
		int i = 0, k, end = _end;
		if (beg >= end)
			return 0;
		if (end >= 1 << 29)
			end = 1 << 29;
		--end;
		list[i++] = 0;
		for (k = 1 + (beg >> 26); k <= 1 + (end >> 26); ++k)
			list[i++] = k;
		for (k = 9 + (beg >> 23); k <= 9 + (end >> 23); ++k)
			list[i++] = k;
		for (k = 73 + (beg >> 20); k <= 73 + (end >> 20); ++k)
			list[i++] = k;
		for (k = 585 + (beg >> 17); k <= 585 + (end >> 17); ++k)
			list[i++] = k;
		for (k = 4681 + (beg >> 14); k <= 4681 + (end >> 14); ++k)
			list[i++] = k;
		return i;
	}
	
	public static int regionToBin4(final int beg, int end) {
		--end;

		if ((beg >> 14 == end >> 14) || (beg >> 17 == end >> 17))
			return LEVEL_STARTS[4] + (beg >> 17);  //LEVEL_STARTS = { 0, 1, 9, 73, 585, 4681 };
		if (beg >> 20 == end >> 20)
			return LEVEL_STARTS[3] + (beg >> 20);
		if (beg >> 23 == end >> 23)
			return LEVEL_STARTS[2] + (beg >> 23);
		if (beg >> 26 == end >> 26)
			return LEVEL_STARTS[1] + (beg >> 26);
		return 0;
	}

	public static int convertBeg(Feature feature) {
		return (feature.getStart() <= 0) ? 0 : (feature.getStart() - 1);
	}

	public static int convertEnd(Feature feature) {
		int end = feature.getEnd();
		if (end <= 0) {
			// If feature end cannot be determined (e.g. because a read is not
			// really aligned),
			// then treat this as a one base feature for indexing purposes.
			end = feature.getStart() + 1;
		}
		return end;
	}
	
}
