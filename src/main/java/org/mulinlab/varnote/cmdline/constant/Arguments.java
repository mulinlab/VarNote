package org.mulinlab.varnote.cmdline.constant;

public final class Arguments {

    public static final String LOG_LONG = "log";

    //index
    public static final String USE_JDK_SHORT = "UJD";
    public static final String USE_JDK_LONG = "use-jdk-deflater";

    public static final String INDEX_INPUT_SHORT = "I";
    public static final String INDEX_INPUT_LONG = "input";

    public static final String INDEX_OUTPUT_SHORT = "O";
    public static final String INDEX_OUTPUT_LONG = "out-floder";

    public static final String INDEX_PRINT_HEADER_SHORT = "PH";
    public static final String INDEX_PRINT_HEADER_LONG = "print-header";

    public static final String INDEX_PRINT_META_SHORT = "PM";
    public static final String INDEX_PRINT_META_LONG = "print-meta-data";

    public static final String INDEX_LITS_CHROM_SHORT = "LC";
    public static final String INDEX_LIST_CHROM_LONG = "list-chroms";

    public static final String INDEX_REPLACE_HEADER_SHORT = "RH";
    public static final String INDEX_REPLACE_HEADER_LONG = "reheader";


    //query
    public static final String USE_JDKI_SHORT = "UJI";
    public static final String USE_JDKI_LONG = "use-jdk-inflater";

    public static final String QUERY_INPUT_SHORT = "Q";
    public static final String QUERY_INPUT_LONG = "q-region";

    public static final String QUERY_LABEL_SHORT = "L";
    public static final String QUERY_LABEL_LONG = "is-label";

    //intersect
    public static final String INTERSECT_INPUT_SHORT = "Q";
    public static final String INTERSECT_INPUT_LONG = "query-file";

    public static final String INTERSECT_THREAD_SHORT = "T";
    public static final String INTERSECT_THREAD_LONG = "thread";

    public static final String INTERSECT_COUNT_SHORT = "C";
    public static final String INTERSECT_COUNT_LONG = "is-count";

    public static final String INTERSECT_ZIP_SHORT = "Z";
    public static final String INTERSECT_ZIP_LONG = "is-zip";

    public static final String INTERSECT_OUT_SHORT = "O";
    public static final String INTERSECT_OUT_LONG = "out-file";

    public static final String INTERSECT_OUT_MODE_SHORT = "OM";
    public static final String INTERSECT_OUT_MODE_LONG = "out-mode";

    public static final String INTERSECT_LOJ_SHORT = "loj";
    public static final String INTERSECT_LOJ_LONG = "is-loj";

    public static final String INTERSECT_RC_SHORT = "RC";
    public static final String INTERSECT_RC_LONG = "is-remove-comment";

    //DB
    public static final String DB_INPUT_SHORT = "D";
    public static final String DB_INPUT_LONG = "d-files";


    //format
    public static final String FORMAT_SHORT = "F";
    public static final String FORMAT_LONG = "format";

    public static final String FORMAT_CHROM_SHORT = "C";
    public static final String FORMAT_CHROM_LONG = "chrom";

    public static final String FORMAT_BEGIN_SHORT = "B";
    public static final String FORMAT_BEGIN_LONG = "begin";

    public static final String FORMAT_END_SHORT = "E";
    public static final String FORMAT_END_LONG = "end";

    public static final String FORMAT_REF_LONG = "ref";
    public static final String FORMAT_ALT_LONG = "alt";

    public static final String FORMAT_ZERO_SHORT = "0";
    public static final String FORMAT_ZERO_LONG = "is-zero-based";

    public static final String FORMAT_HEADER_SHORT = "header";
    public static final String FORMAT_HEADER_LONG = "has-header";

    public static final String FORMAT_HEADER_PATH_SHORT = "HP";
    public static final String FORMAT_HEADER_PATH_LONG = "header-path";

    public static final String FORMAT_COMMENT_SHORT = "CI";
    public static final String FORMAT_COMMENT_LONG = "comment-indicator";

    public static final String FORMAT_SKIP_SHORT = "S";
    public static final String FORMAT_SKIP_LONG = "skip";


    //anno
    public static final String ANNO_CONFIG_SHORT = "A";
    public static final String ANNO_CONFIG_LONG = "anno-config";

    public static final String ANNO_FO_SHORT = "FO";
    public static final String ANNO_FO_LONG = "force-overlap";

    public static final String ANNO_VH_SHORT = "VH";
    public static final String ANNO_VH_LONG = "vcf-header-for-bed";

    public static final String ANNO_OF_SHORT = "OF";
    public static final String ANNO_OF_LONG = "out-format";
}
